package ru.crystals.pos.bank.tinkoffsbp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
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
import ru.crystals.pos.bank.tinkoffsbp.api.request.FullRefundRequest;
import ru.crystals.pos.bank.tinkoffsbp.api.request.InitRequest;
import ru.crystals.pos.bank.tinkoffsbp.api.request.PartOfAmountRequest;
import ru.crystals.pos.bank.tinkoffsbp.api.request.QRInfoRequest;
import ru.crystals.pos.bank.tinkoffsbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.tinkoffsbp.api.request.Views;
import ru.crystals.pos.bank.tinkoffsbp.api.response.InitResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.RefundResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.RegistrationQRResponse;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.configurator.core.CoreConfigurator;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Objects;

public class BankTinkoffSBPProvider implements SBPProvider {

    private static final Logger log = LoggerFactory.getLogger(BankTinkoffSBPProvider.class);
    private static final String PROVIDER = "tinkoff_sbp_sp";
    private static final String QR_EXPIRATION = "qrExpiration";
    private static final String MAX_NUMBER_OF_RETRIES = "numberOfRetries";
    private static final String PASSWORD = "password";
    private static final String TERMINAL_KEY = "terminalKey";
    private static final String SUCCESS_ERROR_CODE = "0";

    private TinkoffSBPConfig config;
    private RequestExecutor requestExecutor;
    private final ObjectMapper mapper;

    public BankTinkoffSBPProvider() {
        config = new TinkoffSBPConfig();
        requestExecutor = new RequestExecutor(config);
        mapper = requestExecutor.getObjectMapper();
    }

    @Override
    public void start() throws CashException {
        CoreConfigurator coreConf = BundleManager.get(CoreConfigurator.class);
        final Map<String, String> props = coreConf.getProcessingProperties(PROVIDER);
        if (props.isEmpty()) {
            return;
        }
        if (props.get(PASSWORD) == null || props.get(TERMINAL_KEY) == null) {
            log.debug("One or more configuration fields are empty. Password: {}. Terminal key: {}", props.get(PASSWORD),
                    props.get(TERMINAL_KEY));
            throw new CashException(ResBundleBankTinkoffSBP.getString("CONFIG_ERROR"));
        }

        config.setMaxNumberOfRetries(props.getOrDefault(MAX_NUMBER_OF_RETRIES, "5"));
        config.setQrExpiration(Long.parseLong(props.getOrDefault(QR_EXPIRATION, "5")));
        config.setSbpMerchantId(props.get(TERMINAL_KEY));
        config.setPassword(props.get(PASSWORD));
        config.setTerminalKey(props.get(TERMINAL_KEY));
    }

    @Override
    public QRInfoResponseDTO registrationQR(RegistrationQRRequestDTO registrationQRRequestDTO) throws BankCommunicationException {
        InitResponse initResponse = initPaymentSession(registrationQRRequestDTO);
        if (!initResponse.isSuccess()) {
            throw new BankCommunicationException(initResponse.getDetails());
        } else if ("REJECTED".equals(initResponse.getStatus())) {
            log.error("Initialization of payment session is rejected. Order id is {}", initResponse.getOrderId());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(initResponse.getOrderId(), ResBundleBankTinkoffSBP.getString("INITIALIZATION_REJECTED")));
        }

        RegistrationQRRequest registrationQRRequest = new RegistrationQRRequest(initResponse.getTerminalKey(), Long.toString(initResponse.getPaymentId()));
        try {
            registrationQRRequest.setToken(config.getPassword(), registrationQRRequest, mapper);
        } catch (IOException e) {
            log.error("Error while generating token. Payment id is {}", registrationQRRequest.getPaymentId());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(registrationQRRequest.getPaymentId(), ResBundleBankTinkoffSBP.getString("STATUS_UNKNOWN")));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        QRInfoResponseDTO responseDTO;
        RegistrationQRResponse response;

        MappingJacksonValue jacksonValue = new MappingJacksonValue(registrationQRRequest);
        jacksonValue.setSerializationView(Views.WithToken.class);

        try {
            response = requestExecutor.executeRequest("/v2/GetQr", HttpMethod.POST,
                    new HttpEntity<>(jacksonValue, headers), RegistrationQRResponse.class);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!response.isSuccess()) {
            throw new BankCommunicationException(response.getMessage() + response.getDetails());
        }
        log.debug("Request result: {}. QR-code id: {}. Payload is: {}", response.getErrorCode(), response.getPaymentId(), response.getData());
        responseDTO = convertRegistrationQRResponseFromTinkoffToDTO(response);
        return responseDTO;

    }


    private QRInfoResponseDTO convertRegistrationQRResponseFromTinkoffToDTO(RegistrationQRResponse response) {
        Status status;
        if (response.getErrorCode().equals(SUCCESS_ERROR_CODE)) {
            status = Status.SUCCESS;
        } else {
            status = Status.UNKNOWN;
        }
        return new QRInfoResponseDTO(response.getPaymentId(), response.getData(), status);
    }

    InitResponse initPaymentSession(RegistrationQRRequestDTO registrationQRRequestDTO) throws BankCommunicationException {
        String amount = Long.toString(registrationQRRequestDTO.getAmount());
        String orderId = registrationQRRequestDTO.getOrderId();
        InitRequest initRequest;
        MappingJacksonValue jacksonValue;
        if (Objects.nonNull(registrationQRRequestDTO.getDiscountCardNumber())) {
            String cardId = registrationQRRequestDTO.getDiscountCardNumber();
            initRequest = new InitRequest(config.getTerminalKey(), amount, orderId, cardId);
            jacksonValue = new MappingJacksonValue(initRequest);
            jacksonValue.setSerializationView(Views.WithCard.class);
        } else {
            initRequest = new InitRequest(config.getTerminalKey(), amount, orderId);
            jacksonValue = new MappingJacksonValue(initRequest);
            jacksonValue.setSerializationView(Views.WithoutCard.class);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        InitResponse response;

        try {
            response = requestExecutor.executeRequest("/v2/Init/", HttpMethod.POST, new HttpEntity<>(jacksonValue, headers), InitResponse.class,
                    config);
            log.debug("Init for payment. OrderId is {}", response.getOrderId());
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("CONNECTION_ERROR"), rce);
        }

        return response;

    }


    @Override
    public PaymentInfoResponseDTO getPaymentStatus(String qrId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        QRInfoRequest request = new QRInfoRequest(config.getTerminalKey(), qrId);
        try {
            request.setToken(config.getPassword(), request, mapper);
        } catch (IOException e) {
            log.error("Error while generating token. Payment id is {}", request.getPaymentId());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(request.getPaymentId(), ResBundleBankTinkoffSBP.getString("STATUS_UNKNOWN")));
        }

        QRInfoResponse response;
        PaymentInfoResponseDTO responseDTO;

        MappingJacksonValue jacksonValue = new MappingJacksonValue(request);
        jacksonValue.setSerializationView(Views.WithToken.class);
        try {
            response = requestExecutor.executeRequest("/v2/GetState", HttpMethod.POST, new HttpEntity<>(jacksonValue, headers), QRInfoResponse.class,
                    config, qrId);
        } catch (RestClientException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                log.debug("Timeout exception in status request. Retries left: {}", stateOfRequest.getCounterOfRetries());
                return stateOfRequest.requestExecutedWithException();
            } else {
                throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("CONNECTION_ERROR"), e);
            }
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!response.isSuccess()) {
            throw new BankCommunicationException(response.getDetails());
        }
        responseDTO = convertQRInfoResponseFromTinkoffToDTO(response);
        stateOfRequest.requestExecutedSuccessfully(responseDTO);

        if (!stateOfRequest.isRetriesLeft()) {
            throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("STATUS_TIMEOUT_ERROR"));
        }

        return responseDTO;
    }

    private PaymentInfoResponseDTO convertQRInfoResponseFromTinkoffToDTO(QRInfoResponse response) {
        PaymentInfoResponseDTO responseDTO = new PaymentInfoResponseDTO();
        responseDTO.setOperationId(Long.toString(response.getPaymentId()));
        responseDTO.setMessage(response.getErrorCode());
        responseDTO.setStatus(response.getStatus().getCommonStatus());
        return responseDTO;
    }


    @Override
    public RefundInfoResponseDTO refund(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        log.debug("Request for refund payment. Refund id: {}", refundDTO.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        FullRefundRequest refundRequest = new FullRefundRequest(config.getTerminalKey(), refundDTO.getOperationId());
        try {
            refundRequest.setToken(config.getPassword(), refundRequest, mapper);
        } catch (IOException e) {
            log.error("Error while generating token. Payment id is {}", refundRequest.getPaymentId());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(refundRequest.getPaymentId(), ResBundleBankTinkoffSBP.getString("STATUS_UNKNOWN")));
        }
        MappingJacksonValue jacksonValue = new MappingJacksonValue(refundRequest);
        return executeRefundRequest(jacksonValue, headers);
    }

    private RefundInfoResponseDTO convertRefundResponseFromTinkoffToDTO(RefundResponse response) {
        return RefundInfoResponseDTO.builder()
                .setOperationId(Long.toString(response.getPaymentId()))
                .setStatus(response.getStatus().getCommonStatus())
                .setAmount(response.getOriginalAmount() - response.getNewAmount())
                .setId(response.getOrderId())
                .setOperationTimestamp()
                .build();

    }

    @Override
    public RefundInfoResponseDTO refundPartOfAmount(RefundOfPartAmountRequestDTO refundDTO) throws BankCommunicationException {
        log.debug("Request for refund payment. Refund id: {}", refundDTO.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        PartOfAmountRequest partOfAmountRequest = new PartOfAmountRequest(config.getTerminalKey(), refundDTO.getOperationId(), Long.toString(refundDTO.getAmount()));
        try {
            partOfAmountRequest.setToken(config.getPassword(), partOfAmountRequest, mapper);
        } catch (IOException e) {
            log.error("Error while generating token. Payment id is {}", partOfAmountRequest.getPaymentId());
            throw new BankCommunicationException(StringUtils.defaultIfBlank(partOfAmountRequest.getPaymentId(), ResBundleBankTinkoffSBP.getString("STATUS_UNKNOWN")));
        }
        MappingJacksonValue jacksonValue = new MappingJacksonValue(partOfAmountRequest);
        return executeRefundRequest(jacksonValue, headers);
    }

    private RefundInfoResponseDTO executeRefundRequest(MappingJacksonValue jacksonValue, HttpHeaders headers) throws BankCommunicationException {
        RefundResponse response;
        jacksonValue.setSerializationView(Views.WithToken.class);
        try {
            response = requestExecutor.executeRequest("/v2/Cancel", HttpMethod.POST, new HttpEntity<>(jacksonValue, headers),
                    RefundResponse.class);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankTinkoffSBP.getString("CONNECTION_ERROR"), rce);
        }
        if (!response.isSuccess()) {
            throw new BankCommunicationException(response.getMessage() + response.getDetails());
        }
        log.debug("refund result: {}", response);
        return convertRefundResponseFromTinkoffToDTO(response);
    }


    @Override
    public RefundInfoResponseDTO getRefundStatus(String refundId, StateOfRequest stateOfRequest) throws BankCommunicationException {
        PaymentInfoResponseDTO info = getPaymentStatus(refundId, stateOfRequest);
        return RefundInfoResponseDTO.builder()
                .setOperationId(info.getOperationId())
                .setStatus(info.getStatus())
                .build();
    }

    /**
     * Отменяет платежную сессию Тинькоффа
     */
    @Override
    public RefundInfoResponseDTO cancelQRCode(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        return refund(refundDTO);
    }


    @Override
    public PictureId getPaymentSystemLogoId() {
        return PictureId.QR_PAY_TINKOFF_SBP;
    }

    @Override
    public SBPProviderConfig getConfig() {
        return config;
    }

    void setConfig(TinkoffSBPConfig config) {
        this.config = config;
    }

    void setRequestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    public String getProvider() {
        return PROVIDER;
    }
}
