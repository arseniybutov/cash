package ru.crystals.pos.bank.odengiqr;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.bundles.BundleManager;
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
import ru.crystals.pos.bank.odengiqr.api.core.ODengiApi;
import ru.crystals.pos.bank.odengiqr.api.core.ODengiConfig;
import ru.crystals.pos.bank.odengiqr.api.core.ODengiURL;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;
import ru.crystals.pos.bank.odengiqr.api.dto.RequestResponseContainer;
import ru.crystals.pos.bank.odengiqr.api.dto.response.cancel.InvoiceCancelRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.create.CreateInvoiceRs;
import ru.crystals.pos.bank.odengiqr.api.dto.response.status.Payment;
import ru.crystals.pos.bank.odengiqr.api.dto.response.status.Status;
import ru.crystals.pos.bank.odengiqr.api.dto.response.status.StatusPaymentRs;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.pos.property.Properties;
import ru.crystals.utils.time.DateConverters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class BankODengiQRImpl extends AbstractBankPluginImpl implements BankQRPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(BankODengiQRImpl.class);
    private static final String PROVIDER = "odengi_qr_sp";
    private static final String SELLER_ID = "sellerId";
    private static final String SELLER_PASSWORD = "sellerPassword";
    private static final Duration QR_EXPIRATION = Duration.ofMinutes(5);
    private static final int INVOICE_ALREADY_PAID_ERROR_CODE = 45;

    private final ODengiConfig config = new ODengiConfig();
    @Autowired
    private Properties properties;
    private final TimeSupplier timeSupplier = new TimeSupplier();
    private final ODengiApi api;

    public BankODengiQRImpl() {
        api = new ODengiApi(config);
    }

    @Override
    public void start() {
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> props = coreConf.getProcessingProperties(PROVIDER);
        if (props.isEmpty()) {
            return;
        }
        setSellerId(props.get(SELLER_ID));
        setSellerPassword(props.get(SELLER_PASSWORD));
    }

    @Override
    public AuthorizationData saleByQR(SaleData saleData, BankQRProcessingCallback callback) throws BankException {
        RequestResponseContainer response = api.createInvoice(saleData.getAmount().intValue(), saleData.getCurrencyCode(),
                createMessage());
        CreateInvoiceRs createInvoiceRs = (CreateInvoiceRs) response.getData();
        String orderId = createInvoiceRs.getOrderId();
        String invoiceId = createInvoiceRs.getInvoiceId();
        LOG.debug("Invoice created with orderId {}, invoiceId {}", orderId, invoiceId);

        LocalDateTime startDate = LocalDateTime.now();
        callback.eventShowQRCode(config.getUrl().getQrPrefix() + invoiceId, getPaymentSystemLogoId(), saleData.getAmount(),
                startDate, startDate.plus(QR_EXPIRATION), PROVIDER);

        boolean operationInterrupted = waitWhileNoInfo(invoiceId, orderId, callback);
        callback.eventHideQRCode();
        if (operationInterrupted) {
            return cancelOperation(invoiceId, orderId, saleData);
        }

        StatusPaymentRs statusPaymentRs = waitWhileInProgress(invoiceId, orderId);

        return createAuthDataFrom(statusPaymentRs, saleData);
    }

    private String createMessage() {
        return String.format(ResBundleBankODengiQR.getString("INVOICE_DESCRIPTION"),
                properties.getCashNumber(), properties.getShopName(), properties.getShopAddress());
    }

    /**
     * @return {@code true} если операция была прервана кассиром
     */
    private boolean waitWhileNoInfo(String invoiceId, String orderId, BankQRProcessingCallback callback) throws BankException {
        Status status;
        do {
            if (callback.isStopped()) {
                return true;
            }
            waitingFor();
            StatusPaymentRs statusPaymentRs = (StatusPaymentRs) api.statusPayment(invoiceId, orderId).getData();
            status = statusPaymentRs.getStatus();
            // если статус не пришел, значит должны прийти payments (новый статус уже внутри) - покупатель отсканировал QR-код
        } while (!(status == null || statusIsApproved(status)));
        return false;
    }

    private StatusPaymentRs waitWhileInProgress(String invoiceId, String orderId) throws BankException {
        StatusPaymentRs statusPaymentRs;
        do {
            waitingFor();
            statusPaymentRs = (StatusPaymentRs) api.statusPayment(invoiceId, orderId).getData();
        } while (!statusIsApproved(statusPaymentRs));
        return statusPaymentRs;
    }

    private void waitingFor() throws BankException {
        try {
            timeSupplier.sleep(500L);
        } catch (InterruptedException e) {
            LOG.info("Interrupt bank QR processing!");
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleBankODengiQR.getString("OPERATION_INTERRUPTED"));
        }
    }

    private boolean statusIsApproved(StatusPaymentRs statusPaymentRs) throws BankException {
        if (statusPaymentRs.getPayments() == null || statusPaymentRs.getPayments().isEmpty()) {
            LOG.error("Invalid response without expected non empty field: payments");
            throw new BankCommunicationException(ResBundleBankODengiQR.getString("BANK_COMMUNICATION_EXCEPTION"));
        }
        Status status = statusPaymentRs.getPayments().get(0).getStatus();
        if (status == null) {
            LOG.error("Payment status is NULL");
            throw new BankCommunicationException(ResBundleBankODengiQR.getString("BANK_COMMUNICATION_EXCEPTION"));
        }
        return statusIsApproved(status);
    }

    private boolean statusIsApproved(Status status) throws BankException {
        switch (status) {
            case CANCELED:
                throw new BankException(ResBundleBankODengiQR.getString("PAYMENT_CANCELED"));
            case APPROVED:
                return true;
            case PROCESSING:
                return false;
            case UNKNOWN:
            default:
                LOG.debug("Unknown status in response");
                // могут прислать фигню типа "status": "9", по договоренности всё неизвестное считаем "processing"
                return false;
        }
    }

    private AuthorizationData cancelOperation(String invoiceId, String orderId, SaleData saleData) throws BankException {
        Data response = api.invoiceCancel(invoiceId).getData();
        if (response.getError() != null) {
            if (Integer.parseInt(response.getError()) == INVOICE_ALREADY_PAID_ERROR_CODE) {
                // пытаемся отменить оплату, но счет уже оплачен - запросим статус и заполним AuthorisationData
                StatusPaymentRs statusPaymentRs = (StatusPaymentRs) api.statusPayment(invoiceId, orderId).getData();
                return createAuthDataFrom(statusPaymentRs, saleData);
            }
            LOG.warn("Invoice cancel failed for invoice id {}. Error: {}, {}", invoiceId, response.getError(), response.getDesc());
            throw new BankException(String.format(ResBundleBankODengiQR.getString("CANCEL_ERROR"), response.getDesc()));
        }
        InvoiceCancelRs cancelRs = (InvoiceCancelRs) response;
        if (!cancelRs.isSuccess()) {
            LOG.warn("Invoice cancel failed for invoice id {}", invoiceId);
            throw new BankException(String.format(ResBundleBankODengiQR.getString("CANCEL_ERROR"), response.getDesc()));
        }
        // счет успешно отменен - сообщаем об этом
        throw new BankInterruptedException();
    }

    private AuthorizationData createAuthDataFrom(StatusPaymentRs statusPaymentRs, SaleData saleData) {
        Payment payment = statusPaymentRs.getPayments().get(0);

        AuthorizationData authData = new AuthorizationData();
        authData.setOperationType(BankOperationType.SALE);
        // в ответе на status нам возвращают сумму оплаты и сумму оплаты с комиссией, но сейчас это не используется
        authData.setAmount(saleData.getAmount());
        authData.setStatus(payment.getStatus() == Status.APPROVED);
        authData.setDate(DateConverters.toDate(payment.getDatePay()));
        authData.setRefNumber(payment.getTransactionId());
        Map<String, String> extData = authData.getExtendedData();
        extData.put(BankQRPlugin.MERCHANT_ID_PROP_NAME, config.getSellerID());
        extData.put(BankQRPlugin.ORDER_ID_PROP_NAME, payment.getOrderId());
        return authData;
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_ODENGI;
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return BankPaymentType.ONLY_QR_SUPPORTED;
    }

    public void setUrl(String url) {
        try {
            config.setUrl(ODengiURL.valueOf(url));
        } catch (IllegalArgumentException e) {
            LOG.warn("Incorrect value for property 'url': {}. Allowed values: {}. 'PRODUCTION' will be used",
                    url, StringUtils.join(ODengiURL.values(), ", "));
            config.setUrl(ODengiURL.PRODUCTION);
        }
    }

    public void setSellerId(String sellerId) {
        if (StringUtils.isBlank(config.getSellerID()) && StringUtils.isNotBlank(sellerId)) {
            config.setSellerID(sellerId);
        }
    }

    public void setSellerPassword(String sellerPassword) {
        if (StringUtils.isBlank(config.getSellerPassword()) && StringUtils.isNotBlank(sellerPassword)) {
            config.setSellerPassword(sellerPassword);
        }
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
    public AuthorizationData refundByQR(RefundData refundData) {
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
}
