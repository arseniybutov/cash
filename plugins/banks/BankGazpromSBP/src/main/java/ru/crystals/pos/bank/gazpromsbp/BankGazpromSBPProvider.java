package ru.crystals.pos.bank.gazpromsbp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.commonsbpprovider.SBPProvider;
import ru.crystals.pos.bank.commonsbpprovider.SBPProviderConfig;
import ru.crystals.pos.bank.commonsbpprovider.StateOfRequest;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfFullAmountDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfPartAmountRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.QRInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.gazpromsbp.api.request.BlockQRRequest;
import ru.crystals.pos.bank.gazpromsbp.api.request.InitRefundRequest;
import ru.crystals.pos.bank.gazpromsbp.api.request.PaymentInfoRequest;
import ru.crystals.pos.bank.gazpromsbp.api.request.ProcessRefundRequest;
import ru.crystals.pos.bank.gazpromsbp.api.request.RefundInfoRequest;
import ru.crystals.pos.bank.gazpromsbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.gazpromsbp.api.response.BlockQRResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.InitRefundResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.ProcessRefundResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.RegistrationQRResponse;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BankGazpromSBPProvider implements SBPProvider {

    private static final Logger log = LoggerFactory.getLogger(BankGazpromSBPProvider.class);
    private static final String PROVIDER = "gazprom_sbp_sp";
    private static final String QR_EXPIRATION = "qrExpiration";
    private static final String PASSWORD = "password";
    private static final String LEGAL_ID = "legalId";
    private static final String BRAND_NAME = "brandName";
    private static final String SBP_MERCHANT_ID = "sbpMerchantId";
    private static final String ACCOUNT = "account";
    private HttpHeaders headers;
    //"000" - единственный код для ответа без ошибки
    private static final String SUCCESS_CODE = "000";


    private GazpromSBPConfig config;
    private RequestExecutor requestExecutor;

    public BankGazpromSBPProvider() {
        config = new GazpromSBPConfig();
        requestExecutor = new RequestExecutor(config);
    }

    @Override
    public void start() throws CashException {
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> props = coreConf.getProcessingProperties(PROVIDER);

        //отфильтровываем параметры с пустыми значениями
        props.values().removeIf(StringUtils::isEmpty);

        Set<String> propNames = new HashSet<>();
        propNames.add(PASSWORD);
        propNames.add(LEGAL_ID);
        propNames.add(BRAND_NAME);
        propNames.add(SBP_MERCHANT_ID);
        propNames.add(ACCOUNT);

        for (String propName : propNames) {
            if (props.get(propName) == null) {
                log.debug("One or more configuration fields are empty. Password: {}. Legal id: {}. Brand name: {}. SbpMerchant Id: {}. Account: {}.", props.get(PASSWORD),
                        props.get(LEGAL_ID), props.get(BRAND_NAME), props.get(SBP_MERCHANT_ID), props.get(ACCOUNT));
                throw new CashException(ResBundleBankGazpromSBP.getString("CONFIG_ERROR"));
            }
        }


        config.setSbpMerchantId(props.get(SBP_MERCHANT_ID));
        config.setPassword(props.get(PASSWORD));
        config.setLegalId(props.get(LEGAL_ID));
        config.setAccount(props.get(ACCOUNT));
        config.setBrandName(props.get(BRAND_NAME));
        config.setQrExpiration(Long.parseLong(props.getOrDefault(QR_EXPIRATION, "5")));
    }

    @Override
    public QRInfoResponseDTO registrationQR(RegistrationQRRequestDTO registrationQRRequestDTO) throws BankCommunicationException {
        RegistrationQRRequest registrationQRRequest = new RegistrationQRRequest(config.getAccount(), config.getSbpMerchantId(),
                Long.toString(registrationQRRequestDTO.getAmount()));

        RegistrationQRResponse response;
        try {
            response = requestExecutor.executeRequest("/merchant/qrc-data", HttpMethod.POST,
                    new HttpEntity<>(registrationQRRequest, getHeaders()), RegistrationQRResponse.class);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!SUCCESS_CODE.equals(response.getCode())) {
            throw new BankCommunicationException(response.getMessage());
        }

        log.debug("Request result: {}. QR-code id: {}. Payload is: {}", response.getCode(), response.getData().getQrcId(), response.getData().getPayload());
        return convertRegistrationQRResponseFromGazpromToDTO(response);
    }

    private QRInfoResponseDTO convertRegistrationQRResponseFromGazpromToDTO(RegistrationQRResponse response) {
        return new QRInfoResponseDTO(response.getData().getQrcId(), response.getData().getPayload(), response.getData().getStatus().getCommonStatus());
    }

    @Override
    public PaymentInfoResponseDTO getPaymentStatus(String qrId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        PaymentInfoRequest request = new PaymentInfoRequest(new String[]{qrId});
        PaymentInfoResponse response;
        PaymentInfoResponseDTO responseDTO;

        try {
            response = requestExecutor.executeRequest("/payment/v1/qrc-status", HttpMethod.PUT, new HttpEntity<>(request, getHeaders()), PaymentInfoResponse.class,
                    config, qrId);
            responseDTO = convertResponseFromOpenToDTO(response);
            stateOfRequest.requestExecutedSuccessfully(responseDTO);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!SUCCESS_CODE.equals(response.getCode())) {
            PaymentInfoResponseDTO errorInfoResponse = new PaymentInfoResponseDTO();
            errorInfoResponse.setStatus(Status.UNKNOWN);
            errorInfoResponse.setMessage(response.getMessage());
            return errorInfoResponse;
        }

        log.debug("Payment status is: {}", responseDTO.getStatus());

        return responseDTO;
    }

    private PaymentInfoResponseDTO convertResponseFromOpenToDTO(PaymentInfoResponse response) {
        PaymentInfoResponseDTO paymentInfoResponseDTO = new PaymentInfoResponseDTO();
        paymentInfoResponseDTO.setOperationId(response.getData().get(0).getTrxId());
        paymentInfoResponseDTO.setMessage(response.getMessage());
        paymentInfoResponseDTO.setStatus(response.getData().get(0).getStatus().getCommonStatus());
        return paymentInfoResponseDTO;
    }

    @Override
    public RefundInfoResponseDTO refund(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        String transactionRefundId = initRefund(refundDTO.getOperationId(), refundDTO.getAmount());
        return processRefund(transactionRefundId, refundDTO.getAmount(), refundDTO.getId());
    }

    @Override
    public RefundInfoResponseDTO refundPartOfAmount(RefundOfPartAmountRequestDTO refundDTO) throws BankCommunicationException {
        String transactionRefundId = initRefund(refundDTO.getOperationId(), refundDTO.getAmount());
        return processRefund(transactionRefundId, refundDTO.getAmount(), refundDTO.getId());
    }

    //подготовка возврата
    String initRefund(String operationId, long amount) throws BankCommunicationException {
        InitRefundRequest initRefundRequest = new InitRefundRequest(operationId, String.valueOf(amount), "RUB");
        InitRefundResponse response;

        try {
            response = requestExecutor.executeRequest("/merchant/transfer/return/prepare", HttpMethod.POST, new HttpEntity<>(initRefundRequest, getHeaders()),
                    InitRefundResponse.class, config);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!SUCCESS_CODE.equals(response.getCode())) {
            throw new BankCommunicationException(response.getMessage());
        }
        return response.getTransactionId();
    }


    private RefundInfoResponseDTO processRefund(String transactionRefundId, long refundAmount, String cashId) throws BankCommunicationException {
        ProcessRefundRequest request = new ProcessRefundRequest(transactionRefundId);
        ProcessRefundResponse response;

        try {
            response = requestExecutor.executeRequest("/merchant/transfer/return/confirm", HttpMethod.POST, new HttpEntity<>(request, getHeaders()),
                    ProcessRefundResponse.class,
                    config);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!SUCCESS_CODE.equals(response.getCode())) {
            return RefundInfoResponseDTO.builder()
                    .setStatus(Status.UNKNOWN)
                    .setId(cashId)
                    .setOperationTimestamp()
                    .build();
        }

        return RefundInfoResponseDTO.builder()
                .setOperationId(response.getTransactionId())
                .setStatus(mapStatusFromRefundInfoResponse(response.getCode()))
                .setAmount(refundAmount)
                .setId(cashId)
                .setOperationTimestamp()
                .build();
    }

    private Status mapStatusFromRefundInfoResponse(String code) {
        if (SUCCESS_CODE.equals(code)) {
            return Status.SUCCESS;
        } else {
            return Status.REJECTED;
        }
    }

    @Override
    public RefundInfoResponseDTO getRefundStatus(String refundId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        RefundInfoRequest request = new RefundInfoRequest(refundId);
        RefundInfoResponse response;
        try {
            response = requestExecutor.executeRequest("/operation/info", HttpMethod.POST, new HttpEntity<>(request, getHeaders()),
                    RefundInfoResponse.class, config);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!SUCCESS_CODE.equals(response.getCode())) {
            throw new BankCommunicationException(response.getMessage());
        }

        return RefundInfoResponseDTO.builder()
                .setOperationId(response.getTransactionId())
                .setStatus(response.getStatus().getCommonStatus())
                .setOperationTimestamp()
                .build();
    }

    @Override
    public RefundInfoResponseDTO cancelQRCode(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        BlockQRRequest request = new BlockQRRequest(refundDTO.getOperationId());
        BlockQRResponse response;

        try {
            response = requestExecutor.executeRequest("/merchant/qrc-block", HttpMethod.POST, new HttpEntity<>(request, getHeaders()),
                    BlockQRResponse.class, config);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankGazpromSBP.getString("CONNECTION_ERROR"), rce);
        }

        return RefundInfoResponseDTO.builder()
                .setOperationId(response.getTransactionId())
                .setOperationTimestamp()
                .build();
    }

    private HttpHeaders getHeaders() {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.set("login", config.getLegalId());
            headers.set("password", config.getPassword());
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_GAZPROM_SBP;
    }

    @Override
    public SBPProviderConfig getConfig() {
        return config;
    }


    @Override
    public String getProvider() {
        return PROVIDER;
    }

    void setConfig(GazpromSBPConfig config) {
        this.config = config;
    }

    void setRequestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

}
