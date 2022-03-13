package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.info.FnDocInfo;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApi;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApiException;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApiImpl;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApiResponse;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.NotSentDocInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.PositionVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.RegisteredReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ShiftVO;
import ru.crystals.pos.payments.CashPaymentEntity;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Используется в Узбекистане
 */
@PrototypedComponent
public class UZFiscalDriveAPIConnector implements FiscalConnector, Configurable<FiscalDriveConfig> {

    private static final Logger log = LoggerFactory.getLogger(UZFiscalDriveAPIConnector.class);

    private static final String FISCAL_SIGN = "fiscalsign";
    private static final String FISCAL_BARCODE = "fiscalbarcode";

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .periodForSendCashParams(Duration.ofMinutes(60))
            .build();

    private PosApi api;

    private String regNumber;

    private TimeSupplier timeSupplier;

    private EmulatedCounters counters;

    private final AtomicReference<String> terminalId = new AtomicReference<>();

    private long maxNotSentDocumentHours = 24;
    private FiscalDriveConfig config;

    @Override
    public Class<FiscalDriveConfig> getConfigClass() {
        return FiscalDriveConfig.class;
    }

    @Override
    public void setConfig(FiscalDriveConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        EmulatedCounters counters = new EmulatedCounters();
        counters.loadState();
        init(new PosApiImpl(getUrlParameter()), counters, new TimeSupplier());
        resendUnsent();
        counters.initShiftNumber(getHardShiftNum());
        syncSoftAndHardShifts();
    }

    /**
     * Для обработки случаев когда программная и ФР смены рассинхронизированы
     *
     * @return закрыта ли ФР смена
     */
    boolean syncSoftAndHardShifts() throws FiscalPrinterException {
        boolean hardShiftOpened = isHardShiftOpened();
        boolean onlySoftShiftOpened = !hardShiftOpened && counters.isSoftShiftOpened();
        if (hardShiftOpened) {
            counters.setTryingCloseHardShift(false);
        }
        if (onlySoftShiftOpened && counters.isTryingCloseHardShift()) {
            counters.closeShift();
            counters.setTryingCloseHardShift(false);
        }
        return !hardShiftOpened;
    }

    /**
     * При некоторых манипуляциях с токеном может возникнуть ситуация, что документы перестают отправляться по расписания.
     * В этом случае нужно помочь им отправиться.
     */
    private void resendUnsent() {
        final PosApiResponse<Void> result = api.resendUnsent();
        if (result.getError() != null) {
            log.error("Unable to resend unsent docs", convert(result.getError()));
        }
    }

    void init(PosApi api, EmulatedCounters counters, TimeSupplier timeSupplier) throws FiscalPrinterException {
        this.counters = counters;
        this.api = api;
        this.timeSupplier = timeSupplier;
        regNumber = calculateSerialNumber();
    }

    private String calculateSerialNumber() throws FiscalPrinterException {
        final String serialNumber = StringUtils.trimToNull(config.getSerialNumber());
        if (serialNumber == null) {
            throw new FiscalPrinterException(ResBundleUZFiscalDrive.getString("INVALID_SERIAL_NUMBER"));
        }
        return serialNumber;
    }

    private URL getUrlParameter() throws FiscalPrinterException {
        try {
            return new URL(config.getUrl());
        } catch (Exception e) {
            throw new FiscalPrinterConfigException(ResBundleUZFiscalDrive.getString("ERROR_DEVICE_NOT_CONNECTED"));
        }
    }

    @Override
    public String getRegNum() {
        return regNumber;
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        if (this.terminalId.get() == null) {
            this.terminalId.set(api.getTerminalId().orElseThrow(this::convert));
        }
        return this.terminalId.get();
    }

    private FiscalPrinterException convert(PosApiException posApiException) {
        if (posApiException.getCode() == null) {
            return new FiscalPrinterException(ResBundleUZFiscalDrive.getString("CONNECTION_ERROR"));
        }
        return new FiscalPrinterException(ResBundleUZFiscalDrive.getOptionalString(posApiException.getMessage())
                .orElseGet(() -> ResBundleUZFiscalDrive.getString("API_ERROR")));
    }

    @Override
    public String getFNNum() throws FiscalPrinterException {
        return getFactoryNum();
    }

    @Override
    public FnInfo getFnInfo() throws FiscalPrinterException {
        final FnInfo fnInfo = new FnInfo();
        final int count = api.getNotSentDocCount().orElseThrow(this::convert);
        fnInfo.setNotSentDocCount(count);
        if (count == 0) {
            return fnInfo;
        }
        api.getFirstNotSentDoc()
                .orElseThrow(this::convert)
                .map(this::convertDoc)
                .ifPresent(fnInfo::setFirstNotSentDoc);
        return fnInfo;
    }

    protected FnDocInfo convertDoc(NotSentDocInfo notSentDoc) {
        final FnDocInfo firstNotSent = new FnDocInfo();
        firstNotSent.setDate(notSentDoc.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        firstNotSent.setNumber(notSentDoc.getNumber());
        return firstNotSent;
    }

    @Override
    public void openShift(Cashier cashier) throws FiscalPrinterException {
        checkCanSaleNow();
        openSoftShift(api.getCurrentShift().orElseThrow(this::convert));
    }

    private boolean isHardShiftOpened() throws FiscalPrinterException {
        final ShiftVO shiftVO = api.getCurrentShift().orElseThrow(this::convert);
        return shiftVO.getOpenTime() != null;
    }

    /**
     * Открытие фейковой смены.
     * Устаналивается время открытия и метка, что на данный момент будет использована именная фейковая смена.
     */
    private void openSoftShift(ShiftVO shiftData) throws FiscalPrinterException {
        counters.openShift(shiftData);
    }

    /**
     * Открытие смены на фискализаторе
     */
    private void openHardShift() throws FiscalPrinterException {
        checkCanSaleNow();
        api.openShift().orElseThrow(this::convert);
    }

    @Override
    public long getShiftNum() {
        return counters.getSoftShiftNumber();
    }

    public long getHardShiftNum() throws FiscalPrinterException {
        return api.getCurrentShift().orElseThrow(this::convert).getNumber();
    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
        return api.getLastReceiptSeq().orElseThrow(this::convert);
    }

    @Override
    public long getCashAmount() {
        return counters.getCashAmount();
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        boolean hardShiftOpened = isHardShiftOpened();
        boolean softShiftOpened = counters.isSoftShiftOpened();
        if (!hardShiftOpened && !softShiftOpened) {
            return false;
        }
        return !hardShiftOpened || softShiftOpened;
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        final ShiftVO shiftVO = api.getCurrentShift().orElseThrow(this::convert);
        final ShiftCounters result = new ShiftCounters();
        result.setShiftNum(counters.getSoftShiftNumber());
        result.setCountReturn(shiftVO.getTotalRefundCount());
        result.setCountSale(shiftVO.getTotalSaleCount());

        result.setSumCashlessPurchase(shiftVO.getTotalSaleCard());
        result.setSumCashPurchase(shiftVO.getTotalSaleCash());
        result.setSumSale(shiftVO.getTotalSaleCard() + shiftVO.getTotalSaleCash());

        result.setSumCashlessReturn(shiftVO.getTotalRefundCard());
        result.setSumCashReturn(shiftVO.getTotalRefundCash());
        result.setSumReturn(shiftVO.getTotalRefundCard() + shiftVO.getTotalRefundCash());

        result.setSumCashEnd(counters.getCashAmount());
        return result;
    }

    @Override
    public String getDeviceName() {
        return ResBundleUZFiscalDrive.getString("DEVICE_NAME");
    }

    @Override
    public void processCopyDocument(FiscalDocument doc) {
        if (doc instanceof Check) {
            doc.getMap().put(FISCAL_SIGN, doc.getFiscalizationValuesMap().get(FISCAL_SIGN));
            doc.getMap().put(FISCAL_BARCODE, doc.getFiscalizationValuesMap().get(FISCAL_BARCODE));
        }
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        checkCanSaleNow();
        if (counters.isSoftShiftOpened() && !isHardShiftOpened()) {
            openHardShift();
        }
        final CheckType type = check.getType();
        final long cardPaymentSum = check.getPayments().stream()
                .filter(p -> !Objects.equals(p.getPaymentType(), CashPaymentEntity.class.getSimpleName()))
                .mapToLong(Payment::getSum).sum();

        List<PositionVO> positions = check.getGoods().stream().map(this::mapGoods).collect(Collectors.toList());
        final long cashPaymentSum = check.getCheckSumEnd() - cardPaymentSum;
        ReceiptVO rcpt = new ReceiptVO(cashPaymentSum, cardPaymentSum, positions);

        final PosApiResponse<RegisteredReceiptVO> response;
        if (type == CheckType.SALE) {
            response = api.registerSale(rcpt);
            counters.incSale(cashPaymentSum);
        } else if (type == CheckType.RETURN) {
            response = api.registerRefund(rcpt);
            counters.incReturn(cashPaymentSum);
        } else {
            throw new IllegalArgumentException("Unsupported check type: " + type);
        }
        final RegisteredReceiptVO registered = response.orElseThrow(this::convert);
        check.setFiscalDocId(registered.getReceiptSeq());
        check.getFiscalizationValuesMap().put(FISCAL_SIGN, registered.getFiscalSign());
        check.getFiscalizationValuesMap().put(FISCAL_BARCODE, registered.getQrCodeURL());
        check.getMap().put(FISCAL_SIGN, registered.getFiscalSign());
        check.getMap().put(FISCAL_BARCODE, registered.getQrCodeURL());
    }

    private PositionVO mapGoods(Goods goods) {
        return PositionVO.builder()
                .name(goods.getName())
                .item(goods.getItem())
                .quantity(goods.getQuant())
                .startSum(goods.getEndPositionPrice())
                .vat(goods.getTaxSum())
                .build();
    }

    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        if (!report.isZReport()) {
            return;
        }
        if (!isHardShiftOpened()) {
            if (counters.isSoftShiftOpened()) {
                counters.closeShift();
            }
            return;
        }
        counters.setTryingCloseHardShift(true);
        api.closeShift().orElseThrow(this::convert);
        counters.closeShift();
        counters.setTryingCloseHardShift(false);
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        money.setBeforeCashOperationSum(counters.getCashAmount());
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            counters.incCashIn(money.getValue());
        } else if (money.getOperationType() == InventoryOperationType.CASH_OUT) {
            counters.incCashOut(money.getValue());
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        ValueAddedTaxCollection taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(0, 0.0f, "НДС0", Collections.singletonMap(0.0f, "НДС0")));
        taxes.addTax(new ValueAddedTax(1, 15.0f, "НДС15", Collections.singletonMap(15.0f, "НДС15")));
        taxes.addTax(new ValueAddedTax(2, 20.0f, "НДС20", Collections.singletonMap(20.0f, "НДС20")));
        return taxes;
    }

    private void checkCanSaleNow() throws FiscalPrinterException {
        final long notSentDocCount = api.getNotSentDocCount().orElseThrow(this::convert);
        if (notSentDocCount == 0) {
            return;
        }
        final Optional<NotSentDocInfo> firstNotSentDoc = api.getFirstNotSentDoc().orElseThrow(this::convert);
        final LocalDateTime firstDate = firstNotSentDoc.map(NotSentDocInfo::getDate).orElse(null);
        if (firstDate == null) {
            return;
        }
        log.debug("First not sent document date: {}", firstDate);
        resendUnsent();
        if (firstDate.isBefore(timeSupplier.now().minusHours(maxNotSentDocumentHours))) {
            throw new FiscalPrinterException(ResBundleUZFiscalDrive.getString("ERROR_NOT_SENT_24_HOURS"));
        }
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

}
