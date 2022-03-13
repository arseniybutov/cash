package ru.crystals.pos.bank.raiffeisensbp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RefundRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.response.CancelQrResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.ResponseStatusCode;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Система быстрых платежей
 */
public class FastPaymentSystem {

    private static final Logger log = LoggerFactory.getLogger(FastPaymentSystem.class);

    private final RestTemplate restTemplate;
    private final SBPConfig sbpConfig;
    private final ObjectMapper objectMapper;

    public FastPaymentSystem(SBPConfig sbpConfig) {
        this.sbpConfig = sbpConfig;
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        httpRequestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(30));
        objectMapper = new ObjectMapper()
                .registerModule(getTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(httpRequestFactory);
        restTemplate = new RestTemplate(factory);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
        restTemplate.setMessageConverters(Collections.singletonList(converter));
        restTemplate.setInterceptors(Collections.singletonList(new SBPRequestInterceptor()));
    }

    /**
     * Регистрация QR-кода
     *
     * @param registrationRequest запрос на регистрацию
     * @return информация по регистрации
     */
    public QRInfoResponse registrationQR(RegistrationQRRequest registrationRequest) throws BankCommunicationException {
        log.debug("Request for registration QR-code. Order id: {}", registrationRequest.getOrder());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        QRInfoResponse response;
        try {
            response = executeRequest("/qr/register", HttpMethod.POST, new HttpEntity<>(registrationRequest, headers), QRInfoResponse.class);
            log.debug("Request result: {}. QR-code id: {}", response.getCode(), response.getQrId());
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return response;
    }

    /**
     * Получение данных по зарегистрированному ранее QR-коду
     *
     * @param qrId Идентификатор зарегистрированного QRС в СБП
     * @return статус по QR-коду
     */
    public QRInfoResponse checkQR(String qrId) {
        log.debug("Request for checking QR-code by QR-code id: {}", qrId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sbpConfig.getSecretKey());
        QRInfoResponse response = executeRequest("/qr/{qrId}/info", HttpMethod.GET, new HttpEntity<>(headers), QRInfoResponse.class, qrId);
        log.debug("Request result: {}", response.getCode());
        return response;
    }

    /**
     * Получение информации по платежу
     *
     * @param qrId Идентификатор зарегистрированного QRС в СБП
     * @return статус по платежу
     */
    public PaymentInfoResponse checkPaymentStatus(String qrId) throws BankCommunicationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sbpConfig.getSecretKey());
        PaymentInfoResponse response;
        try {
            response = executeRequest("/qr/{qrId}/payment-info", HttpMethod.GET, new HttpEntity<>(headers), PaymentInfoResponse.class, qrId);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return response;
    }

    /**
     * Оформление возврата по платежу
     *
     * @param refundRequest запрос на возврат
     * @return информация по запросу на возврат
     */
    public RefundInfoResponse refund(RefundRequest refundRequest) throws BankCommunicationException {
        log.debug("Request for refund payment. Order id: {}. Refund id: {}", refundRequest.getOrder(), refundRequest.getRefundId());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sbpConfig.getSecretKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        RefundInfoResponse response;
        try {
            response = executeRequest("/refund", HttpMethod.POST, new HttpEntity<>(refundRequest, headers), RefundInfoResponse.class);
            log.debug("refund result: {}", response);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return response;
    }

    /**
     * Получение информации по возврату
     *
     * @param refundId Уникальный идентификатор запроса на возврат
     * @return информацию по возврату
     */
    public RefundInfoResponse checkRefundStatus(String refundId) throws BankCommunicationException {
        log.debug("Request for checking refund status. Refund id: {}", refundId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sbpConfig.getSecretKey());
        RefundInfoResponse response;
        try {
            response = executeRequest("/refund/{refundId}", HttpMethod.GET, new HttpEntity<>(headers), RefundInfoResponse.class, refundId);
            log.debug("checkRefundStatus result: {}", response);
        } catch (Exception rce) {
            throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"), rce);
        }
        return response;
    }

    /**
     * @param <Q> request DTO type
     * @param <S> response DTO type
     */
    protected <Q, S> S executeRequest(String requestPath, HttpMethod httpMethod, HttpEntity<Q> httpEntity, Class<S> responseType, Object... variables) {
        ResponseEntity<S> response = restTemplate.exchange(sbpConfig.getUrl().getUrlV1() + requestPath, httpMethod, httpEntity, responseType, variables);
        return response.getBody();
    }

    public void cancelQr(String qrId) throws BankCommunicationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sbpConfig.getSecretKey());
        try {
            restTemplate.exchange(sbpConfig.getUrl().getUrlV2() + "/qrs/" + qrId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        } catch (HttpClientErrorException e) {
            checkExceptionMessage(e);
        }
    }

    /**
     * Пробуем отменить QR, но что-то пошло не так (получили {@link HttpClientErrorException}), возможно 3 варианта:
     * 1. Если код ошибки говорит, что нельзя сменить статус EXPIRED на CANCELLED, то нас это устраивает, считаем что QR отменен
     * 2. Если получили ошибку в запросе, при этом код ошибки не про некорректный статус - бросаем exception
     * 3. Пришло что-то странное, что не смогло распарситься в {@link CancelQrResponse} - бросаем exception
     */
    private void checkExceptionMessage(HttpClientErrorException e) throws BankCommunicationException {
        try {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                CancelQrResponse response = objectMapper.readValue(e.getResponseBodyAsString(), CancelQrResponse.class);
                if (response.getCode() != ResponseStatusCode.WRONG_QR) {
                    throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"));
                }
            }
        } catch (IOException jsonParseException) {
            throw new BankCommunicationException(ResBundleBankRaiffeisenSBP.getString("CONNECTION_ERROR"));
        }
    }

    private SimpleModule getTimeModule() {
        return new JavaTimeModule()
                .addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
                    @Override
                    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value));
                    }
                }).addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
                    @Override
                    public OffsetDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
                        return OffsetDateTime.parse(p.getText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                });
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
