package ru.crystals.pos.bank.raiffeisensbp;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankQRPlugin;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankInterruptedException;
import ru.crystals.pos.bank.raiffeisensbp.api.request.QRType;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RefundRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.ResponseStatusCode;
import ru.crystals.pos.bank.raiffeisensbp.api.response.ResponseWithMessage;
import ru.crystals.pos.bank.raiffeisensbp.api.status.PaymentStatus;
import ru.crystals.pos.bank.raiffeisensbp.api.status.RefundStatus;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.templates.parser.Document;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.templateengine.engine.TemplateEngine;
import ru.crystals.pos.templateengine.engine.TemplateEngineBuilder;
import ru.crystals.pos.templateengine.functions.ConstantLengthSupplier;
import ru.crystals.pos.templateengine.functions.LengthSupplier;
import ru.crystals.pos.templateengine.parser.DocumentParser;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Плагин реализации Системы Быстрых Платежей для Raiffeisen банка
 */
public class BankRaiffeisenSBPImpl extends AbstractBankPluginImpl implements BankQRPlugin {

    private static final Logger log = LoggerFactory.getLogger(BankRaiffeisenSBPImpl.class);
    private static final TemplateEngine TEMPLATE_ENGINE = new TemplateEngineBuilder().build();
    private static final LengthSupplier LENGTH_SUPPLIER = new ConstantLengthSupplier();
    private static final String SLIP_RESOURCE_FILE = "slip-raiffeisen_sbp.xml";

    private static final String PROVIDER = "raiffeisen_sbp_sp";
    private static final String SECRET_KEY = "secretKey";
    private static final String ACCOUNT = "account";
    private static final String MERCHANT_ID = "sbpMerchantId";
    private static final String QR_EXPIRATION = "qrExpiration";

    private static final String PAYMENT_DETAILS = "paymentDetails";
    private static final String ADDITIONAL_INFO = "additionalInfo";

    private static final String SHOP_INDEX_PLACE_HOLDER = "%SHOP_INDEX%";
    private static final String CASH_NUMBER_PLACE_HOLDER = "%CASH_NUMBER%";
    private static final String DATE_TIME_PLACE_HOLDER = "%DATE_TIME%";

    private static final String[] REPLACE_FROM = new String[]{
            SHOP_INDEX_PLACE_HOLDER,
            CASH_NUMBER_PLACE_HOLDER,
            DATE_TIME_PLACE_HOLDER,
    };

    private static final DateTimeFormatter SLIP_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private FastPaymentSystem fastPaymentSystem;
    private SBPConfig config;
    private SBPTimeSupplier sbpTimeSupplier;
    private Properties properties;

    private Document slipTemplate;

    public BankRaiffeisenSBPImpl() {
        config = new SBPConfig();
        fastPaymentSystem = new FastPaymentSystem(config);
        sbpTimeSupplier = new SBPTimeSupplier();
    }

    BankRaiffeisenSBPImpl(Properties properties, SBPConfig config, SBPTimeSupplier sbpTimeSupplier, FastPaymentSystem fastPaymentSystem) {
        this.properties = properties;
        this.config = config;
        this.sbpTimeSupplier = sbpTimeSupplier;
        this.fastPaymentSystem = fastPaymentSystem;
    }

    @Override
    public void start() {
        if (properties == null) {
            properties = BundleManager.get(Properties.class);
        }
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> props = coreConf.getProcessingProperties(PROVIDER);
        if (props.isEmpty()) {
            return;
        }
        setSecretKey(props.get(SECRET_KEY));
        setAccount(props.get(ACCOUNT));
        setSbpMerchantId(props.get(MERCHANT_ID));
        setQrExpiration(Long.parseLong(props.getOrDefault(QR_EXPIRATION, "5")));

        config.setPaymentDetails(props.get(PAYMENT_DETAILS));
        config.setAdditionalInfo(props.get(ADDITIONAL_INFO));

        prepareSlipTemplate();
    }

    @Override
    public AuthorizationData saleByQR(SaleData saleData, BankQRProcessingCallback bankCallback) throws BankException {
        String orderId = sbpTimeSupplier.getIdForOperation();
        final OffsetDateTime nowTime = sbpTimeSupplier.getNowTime();
        RegistrationQRRequest.RegistrationRequestBuilder reqBuilder = RegistrationQRRequest.builder()
                .setAccount(config.getAccount())
                .setAmount(CurrencyUtil.convertMoney(saleData.getAmount()))
                .setCreateDate(nowTime)
                .setCurrency(saleData.getCurrencyCode())
                .setOrder(orderId)
                .setQrType(QRType.QR_DYNAMIC)
                .setQrExpirationDate(nowTime.plusMinutes(config.getQrExpiration()))
                .setSbpMerchantId(config.getSbpMerchantId());

        fillAdditionalInfo(reqBuilder, nowTime);

        RegistrationQRRequest registrationRequest = reqBuilder.build();

        QRInfoResponse response = fastPaymentSystem.registrationQR(registrationRequest);
        validateResponse(response);

        showQR(saleData, bankCallback, registrationRequest, response.getPayload());

        waitWhileNoInfo(response.getQrId(), bankCallback);
        if (bankCallback.isStopped()) {
            fastPaymentSystem.cancelQr(response.getQrId());
            throw new BankInterruptedException();
        }
        bankCallback.eventHideQRCode();

        final PaymentInfoResponse paymentInfoResponse = waitWhileInProgress(response.getQrId());

        AuthorizationData data = new AuthorizationData();
        data.setAmount(saleData.getAmount());
        data.setCurrencyCode(saleData.getCurrencyCode());
        data.setCashTransId(saleData.getCashTransId());
        data.setBankid(saleData.getBankId());
        OffsetDateTime operationDateTime = registrationRequest.getCreateDate();
        data.setDate(Date.from(operationDateTime.toInstant()));
        if (paymentInfoResponse.getPaymentStatus() != PaymentStatus.SUCCESS) {
            data.setStatus(false);
            data.setMessage(ResBundleBankRaiffeisenSBP.getForStatus(paymentInfoResponse.getPaymentStatus().name(), paymentInfoResponse.getMessage()));
            return data;
        }
        data.setStatus(true);
        Map<String, String> extData = new HashMap<>();
        extData.put(MERCHANT_ID_PROP_NAME, config.getSbpMerchantId());
        extData.put(OPERATION_ID_PROP_NAME, String.valueOf(paymentInfoResponse.getTransactionId()));
        extData.put(ORDER_ID_PROP_NAME, orderId);
        data.setExtendedData(extData);
        data.setSlips(makeSlip(data.getAmount(), operationDateTime, response.getQrId()));
        return data;
    }

    private void fillAdditionalInfo(RegistrationQRRequest.RegistrationRequestBuilder reqBuilder, OffsetDateTime dateTime) {
        if (config.getAdditionalInfo() == null && config.getPaymentDetails() == null) {
            return;
        }
        final String[] replaceTo = {
                String.valueOf(properties.getShopIndex()),
                String.valueOf(properties.getCashNumber()),
                dateTime.toLocalDateTime().toString()
        };
        Optional.ofNullable(config.getAdditionalInfo())
                .map(info -> replace(info, replaceTo, 140))
                .ifPresent(reqBuilder::setAdditionalInfo);

        Optional.ofNullable(config.getPaymentDetails())
                .map(info -> replace(info, replaceTo, 185))
                .ifPresent(reqBuilder::setPaymentDetails);
    }

    private String replace(String info, String[] replaceTo, int maxLength) {
        return StringUtils.left(StringUtils.replaceEach(info, REPLACE_FROM, replaceTo), maxLength);
    }

    private void waitWhileNoInfo(String qrId, BankQRProcessingCallback bankCallback) throws BankException {
        PaymentInfoResponse response = fastPaymentSystem.checkPaymentStatus(qrId);
        log.debug("Payment status for qrId {} (order id {}) is {} ({})", qrId, response.getOrder(), response.getPaymentStatus(), response.getCode());
        validateResponse(response);
        while (response.getPaymentStatus() == PaymentStatus.NO_INFO) {
            waitingFor();
            if (bankCallback.isStopped()) {
                return;
            }
            response = fastPaymentSystem.checkPaymentStatus(qrId);
            validateResponse(response);
        }
        log.debug("Payment status for qrId {} (order id {}) is changed to {} ({})", qrId, response.getOrder(), response.getPaymentStatus(), response.getCode());
    }

    private PaymentInfoResponse waitWhileInProgress(String qrId) throws BankException {
        PaymentInfoResponse response = fastPaymentSystem.checkPaymentStatus(qrId);
        validateResponse(response);
        while (response.getPaymentStatus() == PaymentStatus.IN_PROGRESS) {
            waitingFor();
            response = fastPaymentSystem.checkPaymentStatus(qrId);
            validateResponse(response);
        }
        log.debug("Payment status for qrId {} (order id {}) is changed to {} ({})", qrId, response.getOrder(), response.getPaymentStatus(), response.getCode());
        return response;
    }

    private void showQR(SaleData saleData, BankQRProcessingCallback bankCallback, RegistrationQRRequest registrationRequest, String payload) {
        LocalDateTime dateBegin = registrationRequest.getCreateDate().toLocalDateTime();
        LocalDateTime dateEnd = registrationRequest.getQrExpirationDate() != null ? registrationRequest.getQrExpirationDate().toLocalDateTime() : null;
        bankCallback.eventShowQRCode(payload, getPaymentSystemLogoId(), saleData.getAmount(), dateBegin, dateEnd, PROVIDER);
    }

    private void validateResponse(ResponseWithMessage response) throws BankCommunicationException {
        if (response.getCode() != ResponseStatusCode.SUCCESS) {
            log.error("Response status is {} ({})", response.getCode(), response.getMessage());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(response.getMessage(), ResBundleBankRaiffeisenSBP.getString("STATUS_UNKNOWN")));
        }
    }

    @Override
    public AuthorizationData refundByQR(RefundData refundData) throws BankException {
        String orderId = refundData.getExtendedData().get(ORDER_ID_PROP_NAME);
        if (orderId == null) {
            log.error("Unable to process refund without required data: orderId=null");
            throw new BankException(ResBundleBankRaiffeisenSBP.getString("ARBITRARY_REFUND_DENIED"));
        }
        String refundId = sbpTimeSupplier.getIdForOperation();

        String paymentDetails = null;
        if (config.getPaymentDetails() != null) {
            final String[] replaceTo = {
                    String.valueOf(properties.getShopIndex()),
                    String.valueOf(properties.getCashNumber()),
                    sbpTimeSupplier.getNowTime().toLocalDateTime().toString()
            };
            paymentDetails = replace(config.getPaymentDetails(), replaceTo, 180);
        }

        RefundRequest refundRequest = new RefundRequest(CurrencyUtil.convertMoney(refundData.getAmount()), orderId, refundId, paymentDetails);
        AuthorizationData data = new AuthorizationData();
        data.setAmount(refundData.getAmount());
        data.setCurrencyCode(refundData.getCurrencyCode());
        data.setCashTransId(refundData.getCashTransId());
        data.setBankid(refundData.getBankId());
        data.setDate(refundData.getOriginalSaleTransactionDate());

        RefundInfoResponse refundResponse = fastPaymentSystem.refund(refundRequest);
        validateResponse(refundResponse);

        RefundInfoResponse refundStatusResponse = waitWhileInProgressRefund(refundId);
        if (refundStatusResponse.getRefundStatus() != RefundStatus.COMPLETED) {
            data.setStatus(false);
            data.setMessage(ResBundleBankRaiffeisenSBP.getForStatus(refundStatusResponse.getRefundStatus().name(), refundStatusResponse.getMessage()));
            return data;
        }
        data.setStatus(true);
        data.setRefNumber(refundId);
        Map<String, String> extData = new HashMap<>();
        extData.put(MERCHANT_ID_PROP_NAME, config.getSbpMerchantId());
        extData.put(ORDER_ID_PROP_NAME, refundId);
        data.setExtendedData(extData);
        return data;
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_RAIFFEISEN_SBP;
    }

    private RefundInfoResponse waitWhileInProgressRefund(String refundId) throws BankException {
        RefundInfoResponse refundStatusResponse = fastPaymentSystem.checkRefundStatus(refundId);
        validateResponse(refundStatusResponse);
        while (refundStatusResponse.getRefundStatus() == RefundStatus.IN_PROGRESS) {
            waitingFor();
            refundStatusResponse = fastPaymentSystem.checkRefundStatus(refundId);
            validateResponse(refundStatusResponse);
        }
        return refundStatusResponse;
    }

    private void waitingFor() throws BankException {
        try {
            sbpTimeSupplier.sleep(500L);
        } catch (InterruptedException e) {
            log.info("Interrupt bank QR processing!");
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleBankRaiffeisenSBP.getString("OPERATION_INTERRUPTED"));
        }
    }

    private List<List<String>> makeSlip(Long amount, OffsetDateTime operationDateTime, String qrId) {
        if (slipTemplate == null) {
            return Collections.emptyList();
        }
        try {
            List<List<String>> result = new ArrayList<>();
            List<String> slip = new ArrayList<>();
            for (DocumentSection section : TEMPLATE_ENGINE.process(slipTemplate, createDataset(amount, operationDateTime, qrId), LENGTH_SUPPLIER)) {
                for (FontLine fontLine : section.getContent()) {
                    slip.add(fontLine.getContent());
                }
            }
            result.add(slip);
            return result;
        } catch (Exception e) {
            log.error("Error generating slip for Raiffeisen SBP", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> createDataset(Long amount, OffsetDateTime operationDateTime, String qr) {
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("operationdatetime", SLIP_DATE_TIME_FORMATTER.format(operationDateTime));
        dataset.put("amount", CurrencyUtil.convertMoneyToText(amount));
        dataset.put("qrid", qr);
        return dataset;
    }

    public void setUrl(String url) {
        config.setUrl(url);
    }

    public void setSecretKey(String secretKey) {
        config.setSecretKey(secretKey);
    }

    public void setAccount(String account) {
        config.setAccount(account);
    }

    public void setSbpMerchantId(String sbpMerchantId) {
        config.setSbpMerchantId(sbpMerchantId);
    }

    public void setQrExpiration(Long qrExpiration) {
        config.setQrExpiration(qrExpiration);
    }

    @Override
    public boolean isDailyLog() {
        return false;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {
        throw new UnsupportedOperationException();
    }

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
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return BankPaymentType.ONLY_QR_SUPPORTED;
    }

    private void prepareSlipTemplate() {
        try {
            InputStream xml = getClass().getClassLoader().getResourceAsStream(SLIP_RESOURCE_FILE);
            slipTemplate = DocumentParser.unmarshal(xml);
        } catch (Exception e) {
            log.error("Failed parsing slip file for Raiffeisen SBP", e);
        }
    }
}
