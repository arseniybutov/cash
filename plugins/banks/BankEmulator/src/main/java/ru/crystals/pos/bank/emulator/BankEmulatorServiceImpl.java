package ru.crystals.pos.bank.emulator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.BankInstallmentPlugin;
import ru.crystals.pos.bank.ListItem;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.payments.PaymentSuspensionData;
import ru.crystals.pos.payments.PaymentSuspensionResult;
import ru.crystals.util.JsonMappers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BankEmulatorServiceImpl extends AbstractBankPluginImpl implements BankInstallmentPlugin {
    private static final Logger logger = LoggerFactory.getLogger(BankEmulatorServiceImpl.class);
    private static final Path TEST_SLIP_FILE_PATH = Paths.get("test_slip.txt");
    private static final String DEFAULT_PROPERTIES_PATH = "bank_emulator_properties.json";
    private static final BankEmulatorProperties DEFAULT_BANK_EMULATOR_PROPERTIES = new BankEmulatorProperties();
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private int slipsCount = 2;
    private String innerId = "Emulator";

    private Path propertiesPath = Paths.get(DEFAULT_PROPERTIES_PATH);

    private final AtomicReference<CountDownLatch> lockRef = new AtomicReference<>();
    private final AtomicReference<String> dialogResponse = new AtomicReference<>();
    // ждать пока отображается диалог
    private volatile AtomicBoolean waitDialog = new AtomicBoolean(true);
    private volatile AtomicBoolean isNeedRepeat = new AtomicBoolean(false);

    @Override
    public void start() {
        logger.info("{} started", this.getClass().getSimpleName());
    }

    public String getPropertiesPath() {
        return propertiesPath.toString();
    }

    public void setPropertiesPath(String propertiesPath) {
        if (StringUtils.isBlank(propertiesPath)) {
            return;
        }
        this.propertiesPath = Paths.get(propertiesPath);
    }

    @Override
    public boolean requestTerminalStateIfOnline() {
        return readProperties().isStatus();
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        return readProperties().isStatus();
    }

    public int getSlipsCount() {
        return slipsCount;
    }

    public void setSlipsCount(int slipsCount) {
        this.slipsCount = slipsCount;
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        return createAuthorizationDataForReversal(reversalData);
    }

    @Override
    public AuthorizationData sale(SaleData saleData) {
        if (readProperties().canBeSuspended() && saleData.getSuspensionCallback().canBeSuspended()) {
            return saleWithSuspension(saleData);
        }
        sleep(readProperties().getOperationDuration().toMillis());
        return createAuthorizationDataForSale(saleData);
    }

    private AuthorizationData saleWithSuspension(SaleData saleData) {
        final long halfSleepDuration = readProperties().getOperationDuration().toMillis() / 2;
        sleep(halfSleepDuration);
        final PaymentSuspensionData suspensionData = new PaymentSuspensionData();
        suspensionData.setAttributes(readProperties().getExtendedData());
        final long effectiveAmount = saleData.getSuspensionCallback()
                .onPaymentSuspended(suspensionData)
                .map(PaymentSuspensionResult::getNewPaymentSum)
                .orElseGet(saleData::getAmount);
        final AuthorizationData result = createAuthorizationDataForSale(saleData, effectiveAmount);
        sleep(halfSleepDuration);
        return result;
    }

    private void sleep(long sleepDuration) {
        try {
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        return createAuthorizationDataForRefund(refundData);
    }

    protected AuthorizationData createAuthorizationDataForSale(SaleData saleData) {
        return createAuthorizationDataForSale(saleData, saleData.getAmount());
    }

    private void isNeedGenerateRequestId() {
        if (readProperties().getNeedGenerateRequestId()) {
            do {
                if (readProperties().getAuthorizationData().getStatus()) {
                    break;
                }
                waitDialog.set(true);
                isNeedRepeat.set(true);
                for (BankEvent listener : getListeners()) {
                    listener.showCustomProcessScreen(new BankDialog() {
                        @Override
                        public BankDialogType getDialogType() {
                            return readProperties().getBankDialogType();
                        }
                        @Override
                        public String getMessage() {
                            return "Терминал не доступен. Проверьте подключение.";
                        }
                        @Override
                        public List<String> getButtons() {
                            List<String> buttons = new ArrayList<>();
                            buttons.add("Отмена");
                            buttons.add("Повтор");
                            return buttons;
                        }
                    });
                }
                if (readProperties().getBankDialogType() != BankDialogType.BINARY_SELECTION) {
                    break;
                }
                while (waitDialog.get()) {
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException e) {
                        logger.error("Exception on sleep", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
            while (isNeedRepeat.get());
        }
    }

    protected AuthorizationData createAuthorizationDataForSale(SaleData saleData, long effectiveAmount) {
        isNeedGenerateRequestId();

        List<List<String>> slips = new ArrayList<>();

        slips.add(generateFirstSlip(saleData, effectiveAmount));
        if (slipsCount > 1) {
            slips.add(generateSecondSlip(saleData, effectiveAmount));
        }

        BankCard bc = new BankCard();
        bc.setCardNumber("************5432");
        bc.setCardType("MasterCard");
        bc.setExpiryDate(new Date());

        AuthorizationData ad = new AuthorizationData();
        ad.setAmount(effectiveAmount);
        if (saleData != null) {
            ad.setCurrencyCode(saleData.getCurrencyCode());
        }
        ad.setDate(new Date());
        ad.setAuthCode("1111111");
        ad.setRefNumber("222");
        ad.setHostTransId(444L);
        if (saleData != null) {
            ad.setCashTransId(saleData.getCashTransId());
        }
        ad.setCard(bc);
        ad.setOperationCode(1L);
        ad.setOperationType(BankOperationType.SALE);
        ad.setTerminalId("666");
        ad.setMerchantId("777");
        ad.setResponseCode(readProperties().getAuthorizationData().getResponseCode());
        ad.setResultCode(readProperties().getAuthorizationData().getResultCode());
        ad.setStatus(readProperties().getAuthorizationData().getStatus());
        ad.setMessage(readProperties().getAuthorizationData().getMessage());
        ad.setSlips(slips);

        ad.setExtendedData(readProperties().getExtendedData());

        return ad;
    }

    private List<String> generateSlip(SaleData saleData, long effectiveAmount) {
        final Path testSlipPath = TEST_SLIP_FILE_PATH;
        if (Files.exists(testSlipPath)) {
            try {
                logger.debug("Will use slip test from file");
                return Files.readAllLines(testSlipPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        List<String> list = new ArrayList<>();
        list.add("    ОАО \"Банк  ЗООПАРК\"");
        list.add("     ул. Макакина, д.27");
        list.add("Терминал             " + innerId);
        list.add(LocalDateTime.now().format(dateFormat));
        if (saleData instanceof ReversalData) {
            list.add("        ОТМЕНА ПОКУПКИ");
        } else if (saleData instanceof RefundData) {
            list.add("        ВОЗВРАТ ПОКУПКИ");
        } else {
            list.add("        ОПЛАТА ПОКУПКИ");
        }
        if (saleData != null) {
            list.add(BigDecimalConverter.convertMoneyToText(effectiveAmount) + " " + saleData.getCurrencyCode());
        }
        list.add("Код ответа               0011");
        list.add("Код авторизации        987615");
        list.add("Карта        ***********5432");
        list.add("MasterCard             01/17");
        list.add("");
        list.add("");
        list.add("____________________________");

        return list;
    }

    private List<String> generateFirstSlip(SaleData saleData, long effectiveAmount) {
        List<String> list = generateSlip(saleData, effectiveAmount);
        list.add("           (Подпись кассира)");
        return list;
    }

    private List<String> generateSecondSlip(SaleData saleData, long effectiveAmount) {
        List<String> list = generateSlip(saleData, effectiveAmount);
        list.add("           (Подпись клиента)");
        return list;
    }

    protected AuthorizationData createAuthorizationDataForRefund(RefundData refundData) {
        AuthorizationData ad = createAuthorizationDataForSale(refundData);
        ad.setOperationCode(3L);
        ad.setOperationType(BankOperationType.REFUND);
        return ad;
    }

    protected AuthorizationData createAuthorizationDataForReversal(ReversalData reversalData) {
        AuthorizationData ad = createAuthorizationDataForSale(reversalData);
        ad.setOperationCode(3L);
        ad.setOperationType(BankOperationType.REVERSAL);
        return ad;
    }

    @Override
    public boolean isDailyLog() {
        return true;
    }

    @Override
    public long getCheckIntervalWhenOnline() {
        return readProperties().getCheckIntervalWhenOnline().toMillis();
    }

    @Override
    public long getCheckIntervalWhenOffline() {
        return readProperties().getCheckIntervalWhenOffline().toMillis();
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {

        DailyLogData result = new DailyLogData();
        result.setSlip(Arrays.asList("Data stub", "data", innerId));
        return result;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) {
        DailyLogData result = new DailyLogData();
        result.setSlip(Arrays.asList("Итоги дня", "data", innerId));
        return result;
    }

    protected BankEmulatorProperties readProperties() {
        if (!Files.exists(propertiesPath)) {
            return DEFAULT_BANK_EMULATOR_PROPERTIES;
        }
        try {
            return JsonMappers.getDefaultMapper().readValue(propertiesPath.toFile(), BankEmulatorProperties.class);
        } catch (Exception e) {
            return DEFAULT_BANK_EMULATOR_PROPERTIES;
        }
    }


    @Override
    public AuthorizationData saleInstallment(SaleData saleData) throws BankException {
        dialogResponse.set(null);
        lockRef.set(new CountDownLatch(1));
        getListeners().forEach(this::showInstallmentDialog);
        try {
            lockRef.get().await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankException("Operation interrupted");
        }
        int installmentCount = extractInstallmentCount(dialogResponse.getAndSet(null));
        final AuthorizationData result = createAuthorizationDataForSale(saleData);
        result.getExtendedData().put("installment.count", String.valueOf(installmentCount));

        return result;
    }

    private int extractInstallmentCount(String dialogResponse) throws BankException {
        if (dialogResponse == null) {
            throw new BankException("No installment conditions selected");
        }
        return Integer.parseInt(dialogResponse);
    }

    @Override
    public void sendDialogResponse(BankDialogType dialogType, String response) {
        super.sendDialogResponse(dialogType, response);
        if (dialogType == BankDialogType.EXTENDED_LIST_SELECTION) {
            dialogResponse.set(response);
            Optional.ofNullable(lockRef.get()).ifPresent(CountDownLatch::countDown);
        }
        if (dialogType == BankDialogType.BINARY_SELECTION) {
            waitDialog.set(false);
            isNeedRepeat.set(true);
        }
    }

    @Override
    public void closeDialog() {
        waitDialog.set(false);
        isNeedRepeat.set(false);
        dialogResponse.set(null);
        Optional.ofNullable(lockRef.get()).ifPresent(CountDownLatch::countDown);
    }

    @Override
    public boolean canBeUsedWithOtherBanks() {
        return readProperties().canBeUsedWithOtherBanks();
    }

    private void showInstallmentDialog(BankEvent bankEvent) {
        final InstallmentDialog dialog = new InstallmentDialog("Выберите количество платежей", IntStream.range(3, 10)
                .mapToObj(i -> new ListItem(i, String.valueOf(i)))
                .collect(Collectors.toList()));
        bankEvent.showCustomProcessScreen(dialog);
    }

    @Override
    public AuthorizationData refundInstallment(RefundData refundData) throws BankException {
        return createAuthorizationDataForRefund(refundData);
    }

    @Override
    public AuthorizationData reversalInstallment(ReversalData reversalData) throws BankException {
        return createAuthorizationDataForRefund(reversalData);
    }

    @Override
    public boolean canRefundInstallment() {
        return readProperties().canRefundInstallment();
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        Set<BankPaymentType> types = readProperties().getSupportedPaymentTypes();
        if (types.isEmpty()) {
            return EnumSet.of(BankPaymentType.CARD, BankPaymentType.CARD_INSTALLMENT);
        } else {
            return types;
        }
    }
}
