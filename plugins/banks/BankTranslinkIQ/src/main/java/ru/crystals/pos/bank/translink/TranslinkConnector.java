package ru.crystals.pos.bank.translink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.ListItem;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.translink.api.RequestSender;
import ru.crystals.pos.bank.translink.api.RequestSenderImpl;
import ru.crystals.pos.bank.translink.api.SlipParser;
import ru.crystals.pos.bank.translink.api.dto.OpenPosResponse;
import ru.crystals.pos.bank.translink.api.dto.PosOperation;
import ru.crystals.pos.bank.translink.api.dto.Result;
import ru.crystals.pos.bank.translink.api.dto.ResultCode;
import ru.crystals.pos.bank.translink.api.dto.TrnState;
import ru.crystals.pos.bank.translink.api.dto.commands.AuthorizeCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDayCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDocCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;
import ru.crystals.pos.bank.translink.api.dto.commands.InstallmentCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.InstallmentProvider;
import ru.crystals.pos.bank.translink.api.dto.commands.LockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.PrintTotalsCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RefundCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RemoveCardCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.UnlockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.VoidCommand;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;
import ru.crystals.pos.bank.translink.api.dto.events.EventType;
import ru.crystals.pos.bank.translink.api.dto.events.InstallmentCardFlags;
import ru.crystals.pos.bank.translink.api.dto.events.OnCardEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnKbdEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnPrintEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnTrnStatusEvent;
import ru.crystals.pos.i18n.I18nConfig;
import ru.crystals.utils.time.DateConverters;
import ru.crystals.utils.time.Timer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TranslinkConnector {

    private static final Logger log = LoggerFactory.getLogger(TranslinkConnector.class);

    private static final int TRNSTATUS_POLL_PERIOD = 1000;
    private static final int CLOSE_DAY_POLL_PERIOD = 1000;
    private static final int ONCARD_POLL_PERIOD = 1000;
    private static final String ECR_VERSION = "CSI-PASHABANK-1.1.0";
    private static final Set<String> LANGS = new HashSet<>(Arrays.asList("EN", "LT", "RU", "LV", "EE", "TM", "TJ", "GE", "KZ", "AZ", "UZ"));
    private static final Map<String, String> CURRENCIES;
    public static final String RED_BUTTON = "FR";

    private final LockDeviceCommand lockCommand;

    private final RequestSender rs;
    private final I18nConfig i18nConfig;
    private final TimerSupplier timerSupplier;
    private final TranslinkSessionProperties props;

    private final SlipParser slipParser;

    private List<BankEvent> listeners = Collections.emptyList();

    static {
        CURRENCIES = new HashMap<>();
        CURRENCIES.put("RUB", "643");
        CURRENCIES.put("AZN", "944");
        CURRENCIES.put("EUR", "978");
        CURRENCIES.put("UZS", "860");
        CURRENCIES.put("KZT", "398");
    }

    private final AtomicReference<Integer> installmentCountAnswer = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> userResponseLatch = new AtomicReference<>();

    public TranslinkConnector() {
        this(new RequestSenderImpl(), BundleManager.get(I18nConfig.class), new TimerSupplier(), new TranslinkSessionProperties());
    }

    public TranslinkConnector(RequestSender rs, I18nConfig i18nConfig, TimerSupplier timerSupplier, TranslinkSessionProperties props) {
        this.rs = rs;
        this.i18nConfig = i18nConfig;
        this.timerSupplier = timerSupplier;
        this.props = props;
        slipParser = new SlipParser();
        lockCommand = new LockDeviceCommand(ResBundleBankTranslink.getString("IDLE_TEXT_ON_LOCK"));
    }

    public void configure(TranslinkConfig config, List<BankEvent> listeners) {
        this.listeners = listeners;
        rs.setHost(config.getBaseConfig().getTerminalIp());
        rs.setPort(config.getBaseConfig().getTerminalTcpPort());
        rs.setLicenseToken(config.getLicenseToken());
        timerSupplier.configure(config);
    }

    public void configure(TranslinkConfig config) {
        configure(config, Collections.emptyList());
    }

    public boolean isOnline() {
        try {
            openPos();
            final Result result = rs.sendCommand(Command.GETPOSSTATUS, null);
            if (result.getResultCode() == ResultCode.OK) {
                return true;
            }
            log.info("Terminal is not available: {}", result);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to get terminal status", e);
            } else {
                log.error("Unable to get terminal status: {}", e.getMessage());
            }
        } finally {
            closePosSilently();
        }
        return false;
    }

    public AuthorizationData sale(SaleData sd) throws BankException {
        try {
            openPos();
            cleanUpBeforeOperation();

            final PosOperation operation = PosOperation.AUTHORIZE;
            final long amount = sd.getAmount();
            final String currencyCode = convertCurrency(sd.getCurrencyCode());

            try {
                unlock(operation, amount, currencyCode);
                final OnCardEvent onCardEvent = waitForOnCard(Command.AUTHORIZE);

                final AuthorizationData result = new AuthorizationData();
                final String docNumber = props.nextDocNumber();
                try {
                    trySendCommand(Command.AUTHORIZE, AuthorizeCommand.builder()
                            .amount(amount)
                            .currencyCode(currencyCode)
                            .documentNr(docNumber)
                            .build());

                    OnTrnStatusEvent onTrnStatus = waitForOnTrnStatus(result);
                    validate(onTrnStatus, docNumber);
                    fillAuthData(result, onTrnStatus, onCardEvent);
                    result.setOperationType(BankOperationType.SALE);
                    result.setOperationCode((long) operation.getCode());
                    return result;
                } finally {
                    closeDoc(result.getAuthCode(), docNumber);
                }
            } finally {
                lockDeviceSilently();
            }
        } finally {
            closePosSilently();
        }
    }

    public AuthorizationData installment(SaleData sd) throws BankException {
        try {
            openPos();
            cleanUpBeforeOperation();
            installmentCountAnswer.set(null);

            final PosOperation operation = PosOperation.NOOPERATION;
            final long amount = sd.getAmount();
            final long amountZero = 0;
            final String currencyCode = convertCurrency(sd.getCurrencyCode());

            try {
                unlockWithMessage(operation, amountZero, currencyCode, ResBundleBankTranslink.getString("IDLE_TEXT_ON_UNLOCK_INSERT_CARD"));
                final OnCardEvent onCardEvent = waitForOnCard(Command.INSTALLMENT);
                final BankDialog dialog = new InstallmentCountListDialog(ResBundleBankTranslink.getString("COUNT_OF_INSTALLMENT_PAYMENTS_TITLE"),
                        onCardEvent.getInstallmentFlags().getInstallmentFormMerchant().getListOfPossibleValues()
                                .stream()
                                .map(count -> new ListItem(count, String.valueOf(count)))
                                .collect(Collectors.toList()));
                for (BankEvent listener : listeners) {
                    listener.showCustomProcessScreen(dialog);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                this.userResponseLatch.set(latch);
                try {
                    log.debug("Waiting for user response on dialog");
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    removeCardSilently(ResBundleBankTranslink.getString("OPERATION_INTERRUPTED"));
                    throw new BankException(ResBundleBankTranslink.getString("OPERATION_INTERRUPTED"));
                }
                final Integer installmentCount = installmentCountAnswer.getAndSet(null);
                if (installmentCount == null || installmentCount == -1) {
                    removeCardSilently(ResBundleBankTranslink.getString("INSTALLMENT_CONDITIONS_NOT_SELECTED"));
                    throw new BankException(ResBundleBankTranslink.getString("INSTALLMENT_CONDITIONS_NOT_SELECTED"));
                }

                final AuthorizationData result = new AuthorizationData();
                final String docNumber = props.nextDocNumber();
                try {
                    trySendCommand(Command.INSTALLMENT, InstallmentCommand.builder()
                            .amount(amount)
                            .currencyCode(currencyCode)
                            .documentNr(docNumber)
                            .installmentPaymentCount(installmentCount)
                            .installmentProvider(InstallmentProvider.MERCHANT)
                            .build());
                    OnTrnStatusEvent onTrnStatus = waitForOnTrnStatus(result);
                    lockDeviceSilently();
                    validate(onTrnStatus, docNumber);
                    fillAuthData(result, onTrnStatus, onCardEvent);
                    result.setOperationType(BankOperationType.SALE);
                    result.setOperationCode((long) operation.getCode());
                    return result;
                } finally {
                    closeDoc(result.getAuthCode(), docNumber);
                }
            } finally {
                lockDeviceSilently();
            }
        } finally {
            closePosSilently();
            installmentCountAnswer.set(null);
        }
    }

    public AuthorizationData refund(RefundData rd) throws BankException {
        try {
            openPos();
            cleanUpBeforeOperation();

            final PosOperation operation = PosOperation.CREDIT;
            final long amount = rd.getAmount();
            final String currencyCode = convertCurrency(rd.getCurrencyCode());

            try {
                unlock(operation, amount, currencyCode);
                final OnCardEvent onCardEvent = waitForOnCard(Command.REFUND);
                final String rrn = prepareRRNForRefund(rd, onCardEvent);

                final AuthorizationData result = new AuthorizationData();
                final String docNumber = props.nextDocNumber();
                try {
                    trySendCommand(Command.REFUND, RefundCommand.builder()
                            .amount(rd.getAmount())
                            .currencyCode(convertCurrency(rd.getCurrencyCode()))
                            .documentNr(docNumber)
                            .rrn(rrn)
                            .build());

                    OnTrnStatusEvent onTrnStatus = waitForOnTrnStatus(result);
                    validate(onTrnStatus, docNumber);
                    fillAuthData(result, onTrnStatus, onCardEvent);
                    result.setOperationType(BankOperationType.REFUND);
                    result.setOperationCode((long) operation.getCode());
                    return result;
                } finally {
                    closeDoc(result.getAuthCode(), docNumber);
                }
            } finally {
                lockDeviceSilently();
            }
        } finally {
            closePosSilently();
        }
    }

    private String prepareRRNForRefund(RefundData rd, OnCardEvent onCardEvent) throws BankException {
        if (!onCardEvent.getFlags().isReqOriginalRRN()) {
            return null;
        }
        final String rrn = rd.getRefNumber();
        if (rrn == null) {
            throw new BankException(ResBundleBankTranslink.getString("UNABLE_TO_REFUND_WITHOUT_RRN"));
        }
        return rrn;
    }

    public AuthorizationData reversal(ReversalData rd) throws BankException {
        try {
            openPos();
            cleanUpBeforeOperation();

            final PosOperation operation = PosOperation.NOOPERATION;
            final long amount = rd.getAmount();
            final String currencyCode = convertCurrency(rd.getCurrencyCode());

            try {
                unlock(operation, amount, currencyCode);

                final AuthorizationData result = new AuthorizationData();
                trySendCommand(Command.VOID, new VoidCommand(rd.getAuthCode()));

                final OnTrnStatusEvent onTrnStatus = waitForOnTrnStatus(result);
                fillAuthData(result, onTrnStatus, null);
                result.setOperationType(BankOperationType.REVERSAL);
                result.setOperationCode((long) operation.getCode());
                return result;
            } finally {
                lockDeviceSilently();
            }
        } finally {
            closePosSilently();
        }
    }

    public List<String> closeDay() throws BankException {
        return printReport(Command.CLOSEDAY, new CloseDayCommand(null, null));
    }

    public List<String> printReport() throws BankException {
        return printReport(Command.PRINTTOTALS, new PrintTotalsCommand(null, null));
    }

    private List<String> printReport(Command command, CommandParams params) throws BankException {
        try {
            openPos();
            cleanUpBeforeOperation();
            trySendCommand(command, params);
            final Timer closeDayTimeout = timerSupplier.getCloseDayTimer();
            while (!Thread.currentThread().isInterrupted()) {
                final BaseEvent event = rs.getEvent();
                if (event.noMoreEvents()) {
                    checkTimeoutExpired(closeDayTimeout, () -> ResBundleBankTranslink.getString("TERMINAL_TIMEOUT"));
                    sleepWithException(CLOSE_DAY_POLL_PERIOD);
                    continue;
                }
                log.debug("Received event: {}", event);
                if (event.getEventType() == EventType.ONPRINT) {
                    return prepareSlip(((OnPrintEvent) event.getProperties()).getReceiptText());
                }
            }
            throw new BankException(ResBundleBankTranslink.getString("TERMINAL_TIMEOUT"));
        } finally {
            closePosSilently();
        }
    }

    private void checkTimeoutExpired(Timer closeDayTimeout, Supplier<String> message) throws BankException {
        if (closeDayTimeout.isExpired()) {
            throw new BankException(message.get());
        }
    }

    private void closePosSilently() {
        try {
            rs.closePos();
            props.clearAccessToken();
        } catch (Exception e) {
            log.error("Unable to closePos", e);
        }
    }

    private void validate(OnTrnStatusEvent onTrnStatus, String docNumber) {
        Objects.requireNonNull(onTrnStatus, "Invalid ONTRNSTATUS event (null)");
        if (onTrnStatus.getState() != TrnState.Approved) {
            return;
        }
        if (!Objects.equals(onTrnStatus.getDocumentNr(), docNumber)) {
            throw new IllegalStateException(String.format("Invalid ONTRNSTATUS event (docNr mismatch: %s instead of %s)",
                    onTrnStatus.getDocumentNr(), docNumber));
        }
    }

    private void fillAuthData(AuthorizationData result, OnTrnStatusEvent onTrnStatus, OnCardEvent onCardEvent) {
        result.setStatus(onTrnStatus.getState() == TrnState.Approved);
        result.setResponseCode(onTrnStatus.getState().toString());
        result.setAuthCode(onTrnStatus.getOperationId());
        result.setRefNumber(onTrnStatus.getRrn());
        result.setMerchantId(onTrnStatus.getDocumentNr());
        result.setMessage(onTrnStatus.getText());
        if (!result.isStatus() && result.getMessage() == null) {
            result.setMessage(ResBundleBankTranslink.getString("RC_DECLINED"));
        }
        if (onCardEvent != null) {
            BankCard card = new BankCard();
            card.setCardNumberHash(onCardEvent.getHash());
            card.setCardNumber(onCardEvent.getPan());
            result.setCard(card);
        }
        result.setDate(DateConverters.toDate(timerSupplier.getNowTime()));
    }

    private void closeDoc(String operationId, String docNumber) throws BankException {
        if (docNumber != null) {
            trySendCommand(Command.CLOSEDOC, new CloseDocCommand(operationId, docNumber));
        }
        props.closeDocNumber();
    }

    private void unlock(PosOperation operation, long amount, String currencyCode) throws BankException {
        unlockWithMessage(operation, amount, currencyCode, ResBundleBankTranslink.getString("IDLE_TEXT_ON_UNLOCK"));
    }

    private void unlockWithMessage(PosOperation operation, long amount, String currencyCode, String text) throws BankException {
        trySendCommand(Command.UNLOCKDEVICE, UnlockDeviceCommand.builder()
                .posOperation(operation)
                .idleText(text)
                .amount(amount)
                .currencyCode(currencyCode)
                .language(makeLanguage())
                .ecrVersion(ECR_VERSION)
                .build());
    }

    private OnCardEvent waitForOnCard(Command command) throws BankException {
        final Timer cardReadTimer = timerSupplier.getCardReadTimer();
        while (!Thread.currentThread().isInterrupted()) {
            final BaseEvent event = rs.getEvent();
            if (event.noMoreEvents()) {
                checkTimeoutExpired(cardReadTimer, () -> ResBundleBankTranslink.getString("CARD_READ_TIMEOUT_EXPIRED"));
                sleepWithException(ONCARD_POLL_PERIOD);
                continue;
            }
            log.debug("Received event: {}", event);
            if (event.getEventType() == EventType.ONCARD) {
                final OnCardEvent onCardEvent = (OnCardEvent) event.getProperties();
                if (isValidCard(onCardEvent, command)) {
                    return onCardEvent;
                } else {
                    trySendCommand(Command.REMOVECARD, new RemoveCardCommand(ResBundleBankTranslink.getString("NOT_ALLOWED_TO_AUTHORIZE")));
                    throw new BankAuthorizationException(ResBundleBankTranslink.getString("NOT_ALLOWED_TO_AUTHORIZE"));
                }
            }
            checkRedButtonEvent(event);
        }
        throw new BankException(ResBundleBankTranslink.getString("CARD_READ_TIMEOUT_EXPIRED"));
    }

    private boolean isValidCard(OnCardEvent onCardEvent, Command command) {
        if (command == Command.AUTHORIZE) {
            return onCardEvent.getFlags().isAllowAuthorize();
        }
        if (command == Command.REFUND) {
            return onCardEvent.getFlags().isAllowRefund();
        }
        if (command == Command.INSTALLMENT) {
            final InstallmentCardFlags flags = onCardEvent.getInstallmentFlags();
            return flags.isAllowInstallmentIssuer()
                    || flags.isAllowInstallmentMerchant()
                    || flags.isAllowInstallmentAcquirer();
        }
        return true;
    }

    private void checkRedButtonEvent(BaseEvent event) throws BankAuthorizationException {
        if (event.getEventType() == EventType.ONKBD) {
            final OnKbdEvent onKbd = (OnKbdEvent) event.getProperties();
            if (RED_BUTTON.equals(onKbd.getKbdKey())) {
                throw new BankAuthorizationException(ResBundleBankTranslink.getString("OPERATION_INTERRUPTED_RED"));
            }
        }
    }

    private void lockDeviceSilently() {
        try {
            trySendCommand(Command.LOCKDEVICE, lockCommand);
        } catch (Exception e) {
            log.error("Unable to lock device", e);
        }
    }

    private void removeCardSilently(String removeCardText) {
        try {
            trySendCommand(Command.REMOVECARD, new RemoveCardCommand(removeCardText));
        } catch (Exception e) {
            log.error("Unable to remove card", e);
        }
    }

    private String makeLanguage() {
        final String lang = i18nConfig.getLocale().getLanguage().toUpperCase();
        if (LANGS.contains(lang)) {
            return lang;
        }
        return "EN";
    }

    private String convertCurrency(String currencyCode) {
        return CURRENCIES.getOrDefault(currencyCode, "944");
    }

    private OnTrnStatusEvent waitForOnTrnStatus(AuthorizationData result) throws BankException {
        while (!Thread.currentThread().isInterrupted()) {
            final BaseEvent event = rs.getEvent();
            if (event.noMoreEvents()) {
                sleepWithException(TRNSTATUS_POLL_PERIOD);
                continue;
            }
            log.debug("Received event: {}", event);
            checkRedButtonEvent(event);
            if (event.getEventType() == EventType.ONPRINT) {
                result.setSlips(prepareSlips(((OnPrintEvent) event.getProperties()).getReceiptText()));
            }
            if (event.getEventType() == EventType.ONTRNSTATUS) {
                return Objects.requireNonNull((OnTrnStatusEvent) event.getProperties(), "Invalid ONTRNSTATUS event (null)");
            }
        }
        throw new BankException(ResBundleBankTranslink.getString("TERMINAL_TIMEOUT"));
    }

    private void cleanUpBeforeOperation() throws BankException {
        closeLostDocument();

        boolean hasAnyLostEvent = false;
        while (!Thread.currentThread().isInterrupted()) {
            final BaseEvent event = rs.getEvent();
            if (event.noMoreEvents()) {
                break;
            }
            hasAnyLostEvent = true;
            if (event.getEventType() == EventType.ONTRNSTATUS) {
                final String documentNr = ((OnTrnStatusEvent) event.getProperties()).getDocumentNr();
                if (documentNr != null) {
                    closeDoc(null, documentNr);
                }
            }
            if (event.getEventType() == EventType.ONCARD) {
                trySendCommand(Command.REMOVECARD, new RemoveCardCommand(ResBundleBankTranslink.getString("REMOVE_CARD")));
            }
            log.debug("Received previous event: {}", event);
        }
        if (hasAnyLostEvent) {
            lockDeviceSilently();
        }
    }

    private void closeLostDocument() throws BankException {
        final String lostDocument = props.getDocNumber().orElse(null);
        if (lostDocument != null) {
            closeDoc(null, lostDocument);
        }
    }

    private void openPos() throws BankException {
        OpenPosResponse openPosResponse = rs.openPos();
        final ResultCode resultCode = openPosResponse.getResult().getResultCode();
        if (openPosResponse.getResult().getResultCode() == ResultCode.ANOTHER_OPERATION_IN_PROGRESS) {
            props.getAccessToken().ifPresent(rs::setAccessToken);
            final Result closePosResult = rs.closePos();
            if (closePosResult.getResultCode() != ResultCode.OK) {
                throw new BankException(ResBundleBankTranslink.getString("RC_ANOTHER_OPERATION_IN_PROGRESS_ON_OPEN_POS"));
            }
            openPosResponse = rs.openPos();
        }
        if (openPosResponse.getResult().getResultCode() != ResultCode.OK) {
            throw new BankException(ResBundleBankTranslink.getForResultCode(resultCode));
        }
        final String accessToken = openPosResponse.getAccessToken();
        Objects.requireNonNull(accessToken);
        rs.setAccessToken(accessToken);
        props.setAccessToken(accessToken);
    }

    private Result trySendCommand(Command command, CommandParams params) throws BankException {
        return checkResult(command, rs.sendCommand(command, params));
    }

    private void sleepWithException(long time) throws BankException {
        try {
            timerSupplier.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleBankTranslink.getString("TERMINAL_TIMEOUT"));
        }
    }

    private List<List<String>> prepareSlips(String receiptText) {
        return slipParser.parse(receiptText);
    }

    private List<String> prepareSlip(String receiptText) {
        final List<List<String>> slips = prepareSlips(receiptText);
        if (slips.isEmpty()) {
            return Collections.emptyList();
        }
        return slips.get(0);
    }

    private Result checkResult(Command command, Result result) throws BankException {
        if (result.getResultCode() == ResultCode.OK) {
            return result;
        }
        log.error("Error result on {}: {}", command, result);
        throw new BankException(ResBundleBankTranslink.getForResultCode(result.getResultCode()));
    }

    public void onSelectInstallmentOption(String response) {
        installmentCountAnswer.set(response == null ? -1 : Integer.parseInt(response));
        final CountDownLatch latch = userResponseLatch.get();
        if (latch != null) {
            latch.countDown();
        }
    }

}
