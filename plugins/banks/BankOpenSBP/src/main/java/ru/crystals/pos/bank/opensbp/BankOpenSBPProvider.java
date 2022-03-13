package ru.crystals.pos.bank.opensbp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
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
import ru.crystals.pos.bank.opensbp.api.request.Amount;
import ru.crystals.pos.bank.opensbp.api.request.PaymentInfoRequest;
import ru.crystals.pos.bank.opensbp.api.request.QrcType;
import ru.crystals.pos.bank.opensbp.api.request.RefundPartOfAmountRequest;
import ru.crystals.pos.bank.opensbp.api.request.RefundRequest;
import ru.crystals.pos.bank.opensbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.opensbp.api.request.TemplateVersion;
import ru.crystals.pos.bank.opensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.opensbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.opensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.opensbp.api.response.ResponseWithHttpStatus;
import ru.crystals.pos.bank.opensbp.api.response.TokenResponse;
import ru.crystals.pos.bank.opensbp.api.status.PaymentOperationStatus;
import ru.crystals.pos.bank.opensbp.api.status.RefundStatus;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.utils.time.Timer;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Плагин реализации Системы Быстрых Платежей для банка Открытие
 */
public class BankOpenSBPProvider implements SBPProvider {

    private static final Logger log = LoggerFactory.getLogger(BankOpenSBPProvider.class);
    private static final String PROVIDER = "open_sbp_sp";
    private static final String SECRET_KEY = "secretKey";
    private static final String ACCOUNT = "account";
    private static final String MERCHANT_ID = "sbpMerchantId";
    private static final String QR_EXPIRATION = "qrExpiration";
    private static final String DELAY = "delayInSeconds";
    private static final String MAX_NUMBER_OF_RETRIES = "numberOfRetries";

    private Timer tokenTimer;
    private final Duration tokenDuration = Duration.ofSeconds(3600);
    private String token;

    private OpenSBPConfig config;
    private RequestExecutor requestExecutor;

    public BankOpenSBPProvider() {
        config = new OpenSBPConfig();
        requestExecutor = new RequestExecutor(config);
    }

    @Override
    public void start() throws CashException {
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> props = coreConf.getProcessingProperties(PROVIDER);
        tokenTimer = Timer.of(Duration.ZERO);
        if (props.isEmpty()) {
            return;
        }
        if (props.get(MERCHANT_ID) == null || props.get(ACCOUNT) == null || props.get(SECRET_KEY) == null) {
            log.debug("One or more configuration fields are empty. MerchantId: {}. Account: {}. Secret key: {}", props.get(MERCHANT_ID), props.get(ACCOUNT),
                    props.get(SECRET_KEY));
            throw new CashException(ResBundleBankOpenSBP.getString("CONFIG_ERROR"));
        }
        config.setSecretKey(props.get(SECRET_KEY));
        config.setAccount(props.get(ACCOUNT));
        config.setSbpMerchantId(props.get(MERCHANT_ID));
        config.setDelayInSeconds(props.getOrDefault(DELAY, "5"));
        config.setMaxNumberOfRetries(props.getOrDefault(MAX_NUMBER_OF_RETRIES, "5"));
        config.setQrExpiration(Long.parseLong(props.getOrDefault(QR_EXPIRATION, "5")));

    }


    @Override
    public QRInfoResponseDTO registrationQR(RegistrationQRRequestDTO registrationQRRequestDto) throws BankCommunicationException {

        RegistrationQRRequest registrationQRRequest = RegistrationQRRequest.builder(new RegistrationQRRequest(registrationQRRequestDto))
                .setTemplateVersion(TemplateVersion.TEMPLATE_VERSION_1)
                .setQrcType(QrcType.QR_DYNAMIC)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        QRInfoResponseDTO responseDTO;

        log.debug("Request for registration QR-code. Order id: {}", registrationQRRequestDto.getOrderId());

        try {
            ResponseWithHttpStatus<QRInfoResponse> responseWithHttpStatus = requestExecutor.executeRequestWithHttpStatus("/api/merchant/v1/qrc-data/", HttpMethod.POST,
                    new HttpEntity<>(registrationQRRequest, headers), QRInfoResponse.class);
            QRInfoResponse response = responseWithHttpStatus.getBody();
            response.setHttpStatus(responseWithHttpStatus.getStatus());
            log.debug("Request result: {}. QR-code id: {}", response.getHttpStatus(), response.getQrId());
            responseDTO = convertQRInfoResponseFromOpenToDTO(response);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return responseDTO;
    }

    @Override
    public PaymentInfoResponseDTO getPaymentStatus(String qrId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        PaymentInfoRequest request = new PaymentInfoRequest(new String[]{qrId});
        PaymentInfoResponse response;
        PaymentInfoResponseDTO responseDTO;

        if (!stateOfRequest.isCanExecuteRequest()) {
            return stateOfRequest.getCachedResponse();
        }

        try {
            response = requestExecutor.executeRequest("/api/merchant/v1/qrc-status", HttpMethod.PUT, new HttpEntity<>(request, headers), PaymentInfoResponse.class,
                    config, qrId);
            responseDTO = convertResponseFromOpenToDTO(response);
            stateOfRequest.requestExecutedSuccessfully(responseDTO);
        } catch (RestClientException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                log.debug("Timeout exception in status request. Retries left: {}", stateOfRequest.getCounterOfRetries());
                return stateOfRequest.requestExecutedWithException();
            } else {
                throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), e);
            }
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }

        if (!stateOfRequest.isRetriesLeft()) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("STATUS_TIMEOUT_ERROR"));
        }

        stateOfRequest.refreshCounterOfRetries();
        log.debug("Payment status is: {}", responseDTO.getStatus());

        return responseDTO;
    }

    @Override
    public RefundInfoResponseDTO refund(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        log.debug("Request for refund payment. Refund id: {}. Operation id: {}", refundDTO.getId(), refundDTO.getOperationId());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        RefundInfoResponse response;
        RefundRequest refundRequest = new RefundRequest(refundDTO.getOperationId());
        try {
            response = requestExecutor.executeRequest("/api/merchant/v1/operations/return", HttpMethod.POST, new HttpEntity<>(refundRequest, headers),
                    RefundInfoResponse.class);
            log.debug("refund result: {}", response.getStatus());
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return convertRefundResponseFromOpenToDTO(response);
    }

    @Override
    public RefundInfoResponseDTO getRefundStatus(String refundId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        log.debug("Request for checking refund status. Refund id: {}", refundId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        RefundInfoResponse response;

        if (!stateOfRequest.isCanExecuteRequest()) {
            return stateOfRequest.getCachedRefundResponse();
        }

        try {
            response = requestExecutor.executeRequest("/api/merchant/v1/operations/return/{refundId}", HttpMethod.GET, new HttpEntity<>(headers),
                    RefundInfoResponse.class, refundId);
            log.debug("checkRefundStatus result: {}", response);
            stateOfRequest.requestRefundExecutedSuccessfully(convertRefundResponseFromOpenToDTO(response));
        } catch (RestClientException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                log.debug("Timeout exception in status request. Retries left: {}", stateOfRequest.getCounterOfRetries());
                return stateOfRequest.requestRefundExecutedWithException();
            } else {
                throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), e);
            }
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }

        if (!stateOfRequest.isRetriesLeft()) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("STATUS_TIMEOUT_ERROR"));
        }

        stateOfRequest.refreshCounterOfRetries();

        return convertRefundResponseFromOpenToDTO(response);
    }

    @Override
    public RefundInfoResponseDTO refundPartOfAmount(RefundOfPartAmountRequestDTO refundRequestDTO) throws BankCommunicationException {

        log.debug("Request for partial refund payment. Refund id: {}", refundRequestDTO.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        RefundInfoResponse response;
        RefundPartOfAmountRequest refundRequest = new RefundPartOfAmountRequest(refundRequestDTO.getOperationId(), refundRequestDTO.getId(),
                new Amount(refundRequestDTO.getAmount(), "RUB"));
        try {
            response = requestExecutor.executeRequest("/api/merchant/v1/operations/return", HttpMethod.POST, new HttpEntity<>(refundRequest, headers),
                    RefundInfoResponse.class);
            log.debug("refund result: {}", response);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return convertRefundResponseFromOpenToDTO(response);
    }

    private RefundInfoResponseDTO convertRefundResponseFromOpenToDTO(RefundInfoResponse response) {
        return RefundInfoResponseDTO.builder()
                //Для запроса о статусе возврата нужен обычный id, а не operation id
                .setOperationId(response.getId())
                .setStatus(mapStatusFromRefundInfoResponse(response.getStatus().getId()))
                .setAmount(response.getAmount().getAmount())
                .setId(response.getId())
                .setOperationTimestamp()
                .build();
    }

    String getToken() throws BankCommunicationException {
        if (tokenTimer.isNotExpired()) {
            return token;
        }
        log.debug("Request for getting OAuth token");
        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", config.getAccount());
        form.add("password", config.getSecretKey());
        form.add("scope", "read");
        form.add("client_id", "security-client");

        TokenResponse response;
        try {
            response = requestExecutor.executeRequestFormData("/am/ipslegals/connect/token", HttpMethod.POST, new HttpEntity<>(form, headers),
                    TokenResponse.class);
            log.debug("token result: {}", response.getAccessToken());
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankOpenSBP.getString("CONNECTION_ERROR"), rce);
        }
        token = response.getAccessToken();
        tokenTimer.restart(tokenDuration);
        return token;
    }

    private Status mapStatusFromRefundInfoResponse(RefundStatus id) {
        switch (id) {
            case PROCESSING:
                return Status.PROCESSING;
            case REJECTED:
                return Status.REJECTED;
            case COMPLETED:
                return Status.SUCCESS;
            default:
                return Status.UNKNOWN;
        }
    }

    private QRInfoResponseDTO convertQRInfoResponseFromOpenToDTO(QRInfoResponse qrInfoResponse) {
        return new QRInfoResponseDTO(qrInfoResponse.getQrId(), qrInfoResponse.getPayload(), qrInfoResponse.getHttpStatus());
    }

    private PaymentInfoResponseDTO convertResponseFromOpenToDTO(PaymentInfoResponse response) {
        PaymentInfoResponseDTO paymentInfoResponseDTO = new PaymentInfoResponseDTO();
        paymentInfoResponseDTO.setOperationId(String.valueOf(response.getData().get(0).getOperationId()));
        paymentInfoResponseDTO.setMessage(response.getData().get(0).getMessage());
        if (Objects.nonNull(response.getData().get(0).getOperationTimestamp())) {
            paymentInfoResponseDTO.setOperationTimestamp(response.getData().get(0).getOperationTimestamp());
        }
        PaymentOperationStatus openResponseStatus = response.getData().get(0).getStatus();
        paymentInfoResponseDTO.setStatus(mapStatusFromPaymentInfoResponse(openResponseStatus));
        return paymentInfoResponseDTO;
    }

    private Status mapStatusFromPaymentInfoResponse(PaymentOperationStatus status) {
        switch (status) {
            case ACTC:
            case RCVD:
                return Status.PROCESSING;
            case NTST:
                return Status.NOT_STARTED;
            case RJCT:
                return Status.REJECTED;
            case ACWP:
                return Status.SUCCESS;
            default:
                return Status.UNKNOWN;
        }
    }


    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_OPEN_SBP;
    }

    @Override
    public SBPProviderConfig getConfig() {
        return config;
    }

    void setConfig(OpenSBPConfig config) {
        this.config = config;
    }

    void setRequestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }
}
