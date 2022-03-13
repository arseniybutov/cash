package ru.crystals.pos.bank.sberbankqr;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankQRPlugin;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankInterruptedException;
import ru.crystals.pos.bank.sberbankqr.api.core.ResBundleBankSberbankQr;
import ru.crystals.pos.bank.sberbankqr.api.core.SberbankApi;
import ru.crystals.pos.bank.sberbankqr.api.core.SberbankApiConfig;
import ru.crystals.pos.bank.sberbankqr.api.core.SberbankQrUrl;
import ru.crystals.pos.bank.sberbankqr.api.core.TimeSupplier;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderOperationParamType;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.SberbankResponse;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.revocation.OrderRevocationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRs;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.utils.time.DateConverters;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BankSberbankQRImpl extends AbstractBankPluginImpl implements BankQRPlugin {

    private static final Logger log = LoggerFactory.getLogger(BankSberbankQRImpl.class);

    static final String SBERBANK_ID_QR_PROP_NAME = "sberbank_qr.id.qr";

    private static final String OK_CODE = "00";

    private static final String PROVIDER = "sberbank_qr_sp";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CERTIFICATE = "certificate";
    private static final String CERTIFICATE_PASSWORD = "certificatePassword";
    private static final String MEMBER_ID = "memberId";
    private static final String USE_CUSTOMER_QR = "useCustomerQR";
    private static final String QR_EXPIRATION_MINUTES = "qrExpirationMinutes";

    private final DateTimeFormatter slipDateFormat = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private final SberbankApiConfig config = new SberbankApiConfig();
    private SberbankApi sberbankApi;

    private TimeSupplier timeSupplier = new TimeSupplier();

    private boolean canPayByCustomerQR;
    private int qrExpirationMinutes;

    @Override
    public void start() throws CashException {
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> properties = coreConf.getProcessingProperties(PROVIDER);
        config.setClientId(properties.get(CLIENT_ID));
        config.setClientSecret(properties.get(CLIENT_SECRET));
        config.setCertificate(properties.get(CERTIFICATE));
        config.setCertificatePassword(properties.get(CERTIFICATE_PASSWORD));
        config.setMemberId(properties.get(MEMBER_ID));
        sberbankApi = new SberbankApi(config);
        canPayByCustomerQR = Boolean.parseBoolean(properties.getOrDefault(USE_CUSTOMER_QR, "true"));
        qrExpirationMinutes = Integer.parseInt(properties.getOrDefault(QR_EXPIRATION_MINUTES, "5"));
    }

    @Override
    public AuthorizationData saleByCustomerQR(SaleData saleData) throws BankException {
        String qrPayLoad = saleData.getCustomerQR();
        int amount = saleData.getAmount().intValue();
        PayRusClientQRRs.Status payResponse = sberbankApi.pay(qrPayLoad, amount);

        final OrderOperationParamType orderOperation = payResponse.getOrderOperationParam();
        if (orderOperation == null) {
            log.error("Invalid response without expected field: order_operation_param");
            throw new BankCommunicationException(Optional.ofNullable(payResponse.getErrorCode())
                    .map(ResBundleBankSberbankQr::getForErrorCode)
                    .orElseGet(() -> ResBundleBankSberbankQr.getString("BANK_COMMUNICATION_EXCEPTION")));
        }

        AuthorizationData authData = new AuthorizationData();
        authData.setAmount((long) amount);
        authData.setOperationType(BankOperationType.SALE);
        authData.setStatus(checkErrorCode(payResponse, authData, "pay") && checkResponseCode(authData, orderOperation.getResponseCode()));
        authData.getExtendedData().put(MERCHANT_ID_PROP_NAME, payResponse.getMerchantId());
        fillCommonFields(authData,
                orderOperation.getAuthCode(),
                orderOperation.getRrn(),
                orderOperation.getOperationDateTime(),
                orderOperation.getOperationId(),
                payResponse.getOrderId());
        addSlip(authData, generateSlip(authData));

        return authData;
    }

    @Override
    public AuthorizationData refundByQR(RefundData refundData) throws BankException {
        String orderId = refundData.getExtendedData().get(ORDER_ID_PROP_NAME);
        String operationId = refundData.getExtendedData().get(OPERATION_ID_PROP_NAME);
        String originalIdQr = refundData.getExtendedData().get(SBERBANK_ID_QR_PROP_NAME);
        String authCode = refundData.getAuthCode();
        validateData(orderId, operationId, authCode, originalIdQr);
        int amount = refundData.getAmount().intValue();
        OrderCancelQrRs.Status cancelResponse = sberbankApi.cancel(orderId, operationId, authCode, amount, originalIdQr);

        AuthorizationData authData = new AuthorizationData();
        authData.setAmount((long) amount);
        authData.setOperationType(BankOperationType.REFUND);
        authData.setStatus(checkErrorCode(cancelResponse, authData, "cancel"));
        // Пока непонятно, откуда брать его из ответа (такого поля нет) - будем брать из оригинальной операции
        Optional.ofNullable(refundData.getExtendedData().get(MERCHANT_ID_PROP_NAME)).ifPresent(merchantId ->
                authData.getExtendedData().put(MERCHANT_ID_PROP_NAME, merchantId)
        );
        fillCommonFields(authData,
                cancelResponse.getAuthCode(),
                cancelResponse.getRrn(),
                cancelResponse.getOperationDateTime(),
                cancelResponse.getOperationId(),
                cancelResponse.getOrderId());
        addSlip(authData, generateSlip(authData));

        return authData;
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_SBERBANK;
    }

    private void validateData(String orderId, String operationId, String authCode, String originalIdQr) throws BankException {
        if (orderId == null || operationId == null || authCode == null || originalIdQr == null) {
            log.error("Unable to process refund without required data: orderId={}, operationId={}, authCode={}, originalIdQr={}",
                    orderId, operationId, authCode, originalIdQr);
            throw new BankException(ResBundleBankSberbankQr.getString("ARBITRARY_REFUND_DENIED"));
        }
    }

    @Override
    public AuthorizationData saleByQR(SaleData saleData, BankQRProcessingCallback callback) throws BankException {
        OrderCreationQrRs.Status creationResponse = sberbankApi.creation(saleData.getAmount().intValue());
        String orderId = creationResponse.getOrderId();
        log.debug("Payment status for orderId {} is {}", orderId, creationResponse.getOrderState());

        showQR(saleData, callback, creationResponse.getRqTm().toLocalDateTime(), creationResponse.getOrderFormUrl());

        OrderStatusRequestQrRs.Status statusResponse = waitStatusChange(orderId, callback);
        callback.eventHideQRCode();

        if (statusResponse.getOrderOperationParams() == null || statusResponse.getOrderOperationParams().isEmpty()) {
            log.error("Invalid response without expected non empty field: order_operation_params");
            throw new BankCommunicationException(Optional.ofNullable(statusResponse.getErrorCode())
                    .map(ResBundleBankSberbankQr::getForErrorCode)
                    .orElseGet(() -> ResBundleBankSberbankQr.getString("BANK_COMMUNICATION_EXCEPTION")));
        }
        // т.к. мы только что сделали запрос на создание операции, к ней может быть привязан только один OrderOperationParam
        OrderOperationParamType param = statusResponse.getOrderOperationParams().get(0);

        AuthorizationData authData = new AuthorizationData();
        authData.setOperationType(BankOperationType.SALE);
        authData.setAmount((long) param.getOperationSum());
        boolean responseOk = statusResponse.getOrderState() == OrderState.PAID
                && checkErrorCode(statusResponse, authData, "status") && checkResponseCode(authData, param.getResponseCode());
        authData.setStatus(responseOk);
        authData.getExtendedData().put(ORDER_ID_PROP_NAME, statusResponse.getOrderId());
        authData.getExtendedData().put(MERCHANT_ID_PROP_NAME, statusResponse.getMerchantId());
        fillCommonFields(authData,
                param.getAuthCode(),
                param.getRrn(),
                param.getOperationDateTime(),
                param.getOperationId(),
                statusResponse.getOrderId());
        addSlip(authData, generateSlip(authData));

        return authData;
    }

    private void fillCommonFields(AuthorizationData authData,
                                  String authCode,
                                  String rrn,
                                  ZonedDateTime operationDateTime,
                                  String operationId,
                                  String orderId) {
        final Map<String, String> extData = authData.getExtendedData();
        extData.put(SBERBANK_ID_QR_PROP_NAME, config.getIdQR());

        authData.setTerminalId(config.getTerminalId());
        authData.setAuthCode(authCode);
        authData.setRefNumber(rrn);
        authData.setDate(Optional.ofNullable(operationDateTime)
                .map(DateConverters::toDate)
                .orElseGet(Date::new));
        extData.put(OPERATION_ID_PROP_NAME, operationId);
        extData.put(ORDER_ID_PROP_NAME, orderId);
    }

    private List<String> generateSlip(AuthorizationData authData) {
        List<String> list = new ArrayList<>();

        list.add(ResBundleBankSberbankQr.getString("SLIP_TITLE_TEXT"));
        list.add(slipDateFormat.format(DateConverters.toLocalDateTime(authData.getDate())));
        list.add(ResBundleBankSberbankQr.getString(BankOperationType.SALE.equals(authData.getOperationType()) ? "SALE_TEXT" : "REFUND_TEXT"));
        list.add(ResBundleBankSberbankQr.getString("TERMINAL_ID_TEXT") + authData.getTerminalId());
        list.add(ResBundleBankSberbankQr.getString("SUM_TEXT") + CurrencyUtil.convertMoneyToText(authData.getAmount()));
        //Комиссия за операцию отсутствует в ответе от банка, пишем 0
        list.add(ResBundleBankSberbankQr.getString("COMMISSION_TEXT"));
        list.add(ResBundleBankSberbankQr.getString("RRN_TEXT") + authData.getRefNumber());
        list.add(ResBundleBankSberbankQr.getString("AUTH_TEXT") + authData.getAuthCode());
        list.add(ResBundleBankSberbankQr.getString("OPERATION_ID_TEXT") + authData.getExtendedData().get(OPERATION_ID_PROP_NAME));

        return list;
    }

    public void addSlip(AuthorizationData authData, List<String> slip) {
        List<List<String>> result = new ArrayList<>();
        if (!slip.isEmpty()) {
            result.add(slip);
        }
        authData.setSlips(result);
    }

    private void showQR(SaleData saleData, BankQRProcessingCallback bankCallback, LocalDateTime dateBegin, String payload) {
        bankCallback.eventShowQRCode(payload, getPaymentSystemLogoId(), saleData.getAmount(), dateBegin, dateBegin.plus(qrExpirationMinutes, ChronoUnit.MINUTES), PROVIDER);
    }

    private OrderStatusRequestQrRs.Status waitStatusChange(String orderId, BankQRProcessingCallback callback) throws BankException {
        ResponseEntity<OrderStatusRequestQrRs> statusResponseEntity;
        OrderStatusRequestQrRs.Status statusResponse = null;
        do {
            waitingFor(callback.isStopped(), orderId);
            statusResponseEntity = sberbankApi.statusWithRetries(orderId);
            if (statusResponseEntity.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.trace("Requests limit exceeded for Sberbank QR 'status' endpoint, retrying.");
                continue;
            }
            statusResponse = validateResponse(statusResponseEntity);
        } while (statusResponse == null || statusResponse.getOrderState() == OrderState.CREATED);
        log.debug("Payment status for orderId {} is changed to {}", orderId, statusResponse.getOrderState());
        return statusResponse;
    }

    private OrderStatusRequestQrRs.Status validateResponse(ResponseEntity<OrderStatusRequestQrRs> response) throws BankCommunicationException {
        final OrderStatusRequestQrRs statusRs = response.getBody();
        if (statusRs == null) {
            throw new BankCommunicationException(ResBundleBankSberbankQr.getString("BANK_COMMUNICATION_EXCEPTION"));
        }
        final OrderStatusRequestQrRs.Status status = statusRs.getStatus();
        if (status.getErrorCode().startsWith(OK_CODE)) {
            return status;
        }
        String errorDesc = ResBundleBankSberbankQr.getForErrorCode(status.getErrorCode());
        log.debug("Getting status failed: {}", errorDesc);
        throw new BankCommunicationException(errorDesc);
    }

    private void waitingFor(boolean isStopped, String orderId) throws BankException {
        try {
            if (isStopped) {
                revokeOperation(orderId);
                throw new BankInterruptedException();
            }
            timeSupplier.sleep(500L);
        } catch (InterruptedException e) {
            log.info("Interrupt bank QR processing!");
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleBankSberbankQr.getString("OPERATION_INTERRUPTED"));
        }
    }

    private void revokeOperation(String orderId) {
        try {
            OrderRevocationQrRs.Status revocationResponse = sberbankApi.revocation(orderId);
            if (revocationResponse.getOrderState() != OrderState.REVOKED) {
                log.warn("Revocation failed for orderId {}.", orderId);
            }
            checkErrorCode(revocationResponse, null, "revocation");
        } catch (BankCommunicationException e) {
            log.warn("Revocation request failed");
        }
    }

    private static boolean checkErrorCode(SberbankResponse status, AuthorizationData authData, String operationName) {
        String errorCode = status.getErrorCode();
        if (errorCode.startsWith(OK_CODE)) {
            return true;
        }
        String errorDesc = ResBundleBankSberbankQr.getForErrorCode(errorCode);
        if (authData != null) {
            authData.setResultCode(Long.parseLong(errorCode));
            authData.setMessage(errorDesc);
        }
        log.debug("Sberbank QR operation '{}' failed. Error code: {}, error description: {}", operationName, errorCode, errorDesc);
        return false;
    }

    private static boolean checkResponseCode(AuthorizationData authData, String responseCode) {
        if (OK_CODE.equals(responseCode)) {
            return true;
        }
        String errorDesc = ResBundleBankSberbankQr.getForResponseCode(responseCode);
        authData.setResponseCode(responseCode);
        authData.setMessage(errorDesc);
        log.debug("Authentication failed: {}", errorDesc);
        return false;
    }

    @Override
    public boolean canPayByCustomerQR() {
        return canPayByCustomerQR;
    }

    public void setTerminalId(String terminalId) {
        config.setTerminalId(terminalId);
    }

    public void setIdQR(String idQr) {
        config.setIdQR(idQr);
    }

    void setMemberId(String memberId) {
        config.setMemberId(memberId);
    }

    public void setUrl(String url) {
        try {
            config.setUrl(SberbankQrUrl.valueOf(url));
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect value for property 'url': {}. Allowed values: {}", url, StringUtils.join(SberbankQrUrl.values(), ", "));
            config.setUrl(SberbankQrUrl.PRODUCTION);
        }
    }

    public void setSberbankApi(SberbankApi sberbankApi) {
        this.sberbankApi = sberbankApi;
    }

    // unsupported

    @Override
    public AuthorizationData sale(SaleData saleData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDailyLog() {
        return false;
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return BankPaymentType.ONLY_QR_SUPPORTED;
    }
}
