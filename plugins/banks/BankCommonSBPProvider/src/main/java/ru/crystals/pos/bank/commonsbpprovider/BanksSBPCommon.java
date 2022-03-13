package ru.crystals.pos.bank.commonsbpprovider;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankQRPlugin;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfFullAmountDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfPartAmountRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.QRInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankInterruptedException;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.utils.time.DateConverters;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Плагин реализации Системы Быстрых Платежей для банка Открытие
 */
public class BanksSBPCommon extends AbstractBankPluginImpl implements BankQRPlugin {

    private static final Logger log = LoggerFactory.getLogger(BanksSBPCommon.class);
    private SBPProvider sbpProvider;

    private SBPProviderConfig config;
    private SBPTimeSupplier sbpTimeSupplier;

    private String url;


    public BanksSBPCommon(SBPProvider sbpProvider) {
        this.sbpProvider = sbpProvider;
        sbpTimeSupplier = new SBPTimeSupplier();
        config = sbpProvider.getConfig();
    }

    @Override
    public void start() throws CashException {
        sbpProvider.start();
        config.setUrl(url);
        if (Objects.isNull(config.getSbpMerchantId())) {
            throw new CashException(ResBundleSBPCommon.getString("CONFIG_MERCHANT_ID_ERROR"));
        }
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDailyLog() {
        return false;
    }

    @Override
    public AuthorizationData saleByQR(SaleData saleData, BankQRProcessingCallback callback) throws BankException {
        String orderId = getIdForOperation();
        StateOfRequest stateOfRequest = new StateOfRequest(config.getDelayInSeconds(), config.getMaxNumberOfRequest(), config.getMaxNumberOfRetries());

        RegistrationQRRequestDTO registrationRequest = RegistrationQRRequestDTO.builder()
                .setOrderId(orderId)
                .setSbpMerchantId(config.getSbpMerchantId())
                .setAmount(saleData.getAmount())
                .setCurrency(saleData.getCurrencyCode())
                .setCreateDate(sbpTimeSupplier.getNowTime())
                .setQrExpirationDate(sbpTimeSupplier.getExpirationTime(config.getQrExpiration()))
                .setDiscountCardNumber(saleData.getDiscountCardNumber())
                .build();

        QRInfoResponseDTO response = sbpProvider.registrationQR(registrationRequest);
        validateQRInfoResponseDTO(response);

        showQR(saleData, callback, registrationRequest, response.getPayload());

        waitWhileNoInfo(response.getQrId(), callback, stateOfRequest);
        callback.eventHideQRCode();

        final PaymentInfoResponseDTO paymentInfoResponseDTO = waitWhileInProgress(response.getQrId(), stateOfRequest);

        AuthorizationData data = new AuthorizationData();
        data.setAmount(saleData.getAmount());
        data.setCurrencyCode(saleData.getCurrencyCode());
        data.setCashTransId(saleData.getCashTransId());
        data.setBankid(saleData.getBankId());
        if (Objects.nonNull(paymentInfoResponseDTO.getOperationTimestamp())) {
            data.setDate(DateConverters.toDate(paymentInfoResponseDTO.getOperationTimestamp()));
        } else {
            data.setDate(Date.from(registrationRequest.getCreateDate().toInstant()));
        }
        if (paymentInfoResponseDTO.getStatus() != Status.SUCCESS) {
            data.setStatus(false);
            data.setMessage(getMessageForPaymentData(paymentInfoResponseDTO));
            return data;
        }
        data.setStatus(true);
        Map<String, String> extData = new HashMap<>();
        extData.put(BankQRPlugin.MERCHANT_ID_PROP_NAME, config.getSbpMerchantId());
        extData.put(BankQRPlugin.OPERATION_ID_PROP_NAME, String.valueOf(paymentInfoResponseDTO.getOperationId()));
        extData.put(BankQRPlugin.ORDER_ID_PROP_NAME, orderId);
        data.setExtendedData(extData);
        return data;
    }

    @Override
    public AuthorizationData refundByQR(RefundData refundData) throws BankException {
        StateOfRequest stateOfRequest = new StateOfRequest(config.getDelayInSeconds(), config.getMaxNumberOfRequest(), config.getMaxNumberOfRetries());
        String orderId = refundData.getExtendedData().get(BankQRPlugin.ORDER_ID_PROP_NAME);
        String referenceId = refundData.getExtendedData().get(BankQRPlugin.OPERATION_ID_PROP_NAME);
        if (orderId == null) {
            log.error("Unable to process refund without required data: orderId=null");
            throw new BankException(ResBundleSBPCommon.getString("ARBITRARY_REFUND_DENIED"));
        } else if (referenceId == null) {
            log.error("Unable to process refund without required data: referenceId=null");
            throw new BankException(ResBundleSBPCommon.getString("ARBITRARY_REFUND_DENIED"));
        }
        String refundId = getIdForOperation();
        long refundAmount = refundData.getAmount().intValue();
        RefundInfoResponseDTO refundResponse;
        if (refundData.isPartial()) {
            RefundOfPartAmountRequestDTO refundRequestDTO = new RefundOfPartAmountRequestDTO(referenceId, refundId, refundAmount);
            refundResponse = sbpProvider.refundPartOfAmount(refundRequestDTO);
        } else {
            RefundOfFullAmountDTO refundRequestDTO = new RefundOfFullAmountDTO(referenceId, refundId, refundAmount);
            refundResponse = sbpProvider.refund(refundRequestDTO);
        }
        AuthorizationData data = new AuthorizationData();
        data.setAmount(refundData.getAmount());
        data.setCurrencyCode(refundData.getCurrencyCode());
        data.setCashTransId(refundData.getCashTransId());
        data.setBankid(refundData.getBankId());
        data.setDate(DateConverters.toDate(refundResponse.getOperationTimestamp()));

        validateRefundInfoResponseDTO(refundResponse);

        RefundInfoResponseDTO refundStatusResponse = waitWhileInProgressRefund(refundResponse.getOperationId(), stateOfRequest);
        if (refundStatusResponse.getStatus() != Status.SUCCESS) {
            data.setStatus(false);
            data.setMessage(getMessageForRefundData(refundStatusResponse));
            return data;
        }
        data.setStatus(true);
        data.setRefNumber(refundId);
        Map<String, String> extData = new HashMap<>();
        extData.put(BankQRPlugin.MERCHANT_ID_PROP_NAME, config.getSbpMerchantId());
        extData.put(BankQRPlugin.ORDER_ID_PROP_NAME, refundId);
        data.setExtendedData(extData);
        return data;
    }

    private void validateQRInfoResponseDTO(QRInfoResponseDTO qrInfoResponseDTO) throws BankCommunicationException {
        if (qrInfoResponseDTO.getStatus() != Status.SUCCESS) {
            log.error("Response status of Qr info is {} ({})", qrInfoResponseDTO.getStatus(), qrInfoResponseDTO.getStatus());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(qrInfoResponseDTO.getQrId(), ResBundleSBPCommon.getString("STATUS_UNKNOWN")));
        }
    }

    private void validatePaymentInfoResponseDTO(PaymentInfoResponseDTO paymentInfoResponseDTO) throws BankCommunicationException {
        if (paymentInfoResponseDTO.getStatus() == Status.REJECTED) {
            log.error("Response status of payment info is {} ({})", paymentInfoResponseDTO.getStatus(), paymentInfoResponseDTO.getMessage());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(paymentInfoResponseDTO.getMessage(), ResBundleSBPCommon.getString("STATUS_UNKNOWN")));
        }
    }

    private void validateRefundInfoResponseDTO(RefundInfoResponseDTO refundInfoResponseDTO) throws BankCommunicationException {
        if (refundInfoResponseDTO == null) {
            return;
        }
        if (!(refundInfoResponseDTO.getStatus() == Status.SUCCESS || refundInfoResponseDTO.getStatus() == Status.PROCESSING)) {
            log.error("Response status of refund info is {}", refundInfoResponseDTO.getStatus());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(refundInfoResponseDTO.getStatus().toString(),
                    ResBundleSBPCommon.getString("STATUS_UNKNOWN")));
        }
    }

    private void validateCancelInfoResponseDTO(RefundInfoResponseDTO refundInfoResponseDTO) throws BankCommunicationException {
        if (refundInfoResponseDTO.getStatus() != Status.SUCCESS) {
            log.error("Response status of cancel request is {}", refundInfoResponseDTO.getStatus());
            throw new BankCommunicationException(ResBundleSBPCommon.getString("CANCEL_ERROR"));
        }
    }


    private void showQR(SaleData saleData, BankQRProcessingCallback bankCallback, RegistrationQRRequestDTO registrationRequest, String payload) {
        LocalDateTime dateBegin = registrationRequest.getCreateDate().toLocalDateTime();
        LocalDateTime dateEnd = registrationRequest.getQrExpirationDate() != null ? registrationRequest.getQrExpirationDate().toLocalDateTime() : null;
        bankCallback.eventShowQRCode(payload, getPaymentSystemLogoId(), saleData.getAmount(), dateBegin, dateEnd, sbpProvider.getProvider());
    }

    private void waitWhileNoInfo(String qrId, BankQRProcessingCallback bankCallback, StateOfRequest stateOfRequest) throws BankException {
        PaymentInfoResponseDTO response = sbpProvider.getPaymentStatus(qrId, stateOfRequest);
        log.debug("Payment status for qrId {} (operation id {}) is {} ({})", qrId, response.getOperationId(),
                response.getStatus(), response.getMessage());
        validatePaymentInfoResponseDTO(response);
        while (response.getStatus() == Status.NOT_STARTED) {
            waitingFor(bankCallback.isStopped(), response.getOperationId());
            response = sbpProvider.getPaymentStatus(qrId, stateOfRequest);
            validatePaymentInfoResponseDTO(response);
        }
        log.debug("Payment status for qrId {} (operation id {}) is changed to {} ({})", qrId, response.getOperationId(),
                response.getStatus(), response.getMessage());
    }

    private PaymentInfoResponseDTO waitWhileInProgress(String qrId, StateOfRequest stateOfRequest) throws BankException {
        PaymentInfoResponseDTO response = sbpProvider.getPaymentStatus(qrId, stateOfRequest);
        validatePaymentInfoResponseDTO(response);
        while (response.getStatus() == Status.PROCESSING) {
            waitingFor();
            response = sbpProvider.getPaymentStatus(qrId, stateOfRequest);
            validatePaymentInfoResponseDTO(response);
        }
        log.debug("Payment status for qrId {} (operation id {}) is changed to {} ({})", qrId, response.getOperationId(),
                response.getStatus(), response.getMessage());
        return response;
    }

    private RefundInfoResponseDTO waitWhileInProgressRefund(String refundId, StateOfRequest stateOfRequest) throws BankException {
        RefundInfoResponseDTO refundStatusResponse = sbpProvider.getRefundStatus(refundId, stateOfRequest);
        validateRefundInfoResponseDTO(refundStatusResponse);
        while (refundStatusResponse == null || refundStatusResponse.getStatus() == Status.PROCESSING) {
            waitingFor();
            refundStatusResponse = sbpProvider.getRefundStatus(refundId, stateOfRequest);
            validateRefundInfoResponseDTO(refundStatusResponse);
        }
        return refundStatusResponse;
    }

    /**
     * Метод для деактивации QR кода при отмене оплаты кассиром
     */
    private void cancelQRCode(String referenceId) throws BankCommunicationException {
        RefundOfFullAmountDTO refundRequestDTO = new RefundOfFullAmountDTO(referenceId, getIdForOperation());
        RefundInfoResponseDTO refundInfoResponseDTO = sbpProvider.cancelQRCode(refundRequestDTO);
        if (refundInfoResponseDTO != null) {
            validateCancelInfoResponseDTO(refundInfoResponseDTO);
        }
    }

    private String getMessageForPaymentData(PaymentInfoResponseDTO paymentInfoResponseDTO) {
        return paymentInfoResponseDTO.getMessage() + " " + paymentInfoResponseDTO.getStatus();
    }

    public String getMessageForRefundData(RefundInfoResponseDTO refundStatusResponse) {
        return refundStatusResponse.getOperationId() + " " + refundStatusResponse.getStatus();
    }


    private void waitingFor() throws BankException {
        waitingFor(false, null);
    }

    private void waitingFor(boolean isStopped, String referenceId) throws BankException {
        try {
            if (isStopped) {
                cancelQRCode(referenceId);
                throw new BankInterruptedException();
            }
            sbpTimeSupplier.sleep(500L);
        } catch (InterruptedException e) {
            log.info("Interrupt bank QR processing!");
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleSBPCommon.getString("OPERATION_INTERRUPTED"));
        }
    }


    /**
     * Генерация уникального идентификатора для запросов на оплату/возврат
     *
     * @return уникальный идентификатор
     */
    protected String getIdForOperation() {
        return UUID.randomUUID().toString();
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return sbpProvider.getPaymentSystemLogoId();
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return BankPaymentType.ONLY_QR_SUPPORTED;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
