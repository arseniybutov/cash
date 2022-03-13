package ru.crystals.pos.bank.sberbankqr.api.core;

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
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.sberbankqr.api.dto.AccessTokenRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRq;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRq;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRq;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRsWrapper;
import ru.crystals.pos.bank.sberbankqr.api.dto.revocation.OrderRevocationQrRq;
import ru.crystals.pos.bank.sberbankqr.api.dto.revocation.OrderRevocationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRq;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SberbankApi {

    private static final Logger log = LoggerFactory.getLogger(SberbankApi.class);
    private static final DateTimeFormatter MOSCOW_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneId.of("Europe/Moscow"));
    private static final DateTimeFormatter ORDER_NUMBER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String CURRENCY = "643";
    private static final int STATUS_RETRIES_COUNT = 5;

    private final SberbankApiConfig config;
    private final RestTemplate restTemplate;
    private final TimeSupplier timeSupplier;
    private final RequestUidGenerator requestUidGenerator;

    public SberbankApi(SberbankApiConfig config) throws BankConfigException {
        checkConfig(config);
        this.config = config;
        this.restTemplate = restTemplate(config);
        this.timeSupplier = new TimeSupplier();
        this.requestUidGenerator = new RequestUidGenerator();
    }

    SberbankApi(SberbankApiConfig config, TimeSupplier timeSupplier, RequestUidGenerator requestUidGenerator) throws BankConfigException {
        this.config = config;
        this.restTemplate = restTemplate(config);
        this.timeSupplier = timeSupplier;
        this.requestUidGenerator = requestUidGenerator;
    }

    private static SSLConnectionSocketFactory createSSLConnectionSocketFactory(SberbankApiConfig config) throws BankConfigException {
        SSLConnectionSocketFactory sslConnectionSocketFactory;
        try (InputStream certificateInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(config.getCertificate()))) {
            char[] password = config.getCertificatePassword().toCharArray();
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(certificateInputStream, password);

            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.useProtocol("TLS");
            sslContextBuilder.loadKeyMaterial(clientStore, password);
            sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());

            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
            return sslConnectionSocketFactory;
        } catch (Exception e) {
            throw new BankConfigException(ResBundleBankSberbankQr.getString("CERTIFICATE_ERROR"), e);
        }
    }

    private static RestTemplate restTemplate(SberbankApiConfig config) throws BankConfigException {
        SSLConnectionSocketFactory sslConnectionSocketFactory = createSSLConnectionSocketFactory(config);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
        requestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));

        BufferingClientHttpRequestFactory buffering = new BufferingClientHttpRequestFactory(requestFactory);
        RestTemplate restTemplate = new RestTemplate(buffering);
        restTemplate.setInterceptors(Collections.singletonList(new SberbankQrLoggingInterceptor()));

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper());
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        restTemplate.setMessageConverters(Arrays.asList(converter, new AllEncompassingFormHttpMessageConverter()));
        return restTemplate;
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(getTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    private static SimpleModule getTimeModule() {
        return new JavaTimeModule()
                .addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
                    @Override
                    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        // отправляем в Сбербанк московское время - ZonedDateTime, но в формате как Instant
                        gen.writeString(MOSCOW_TIME_FORMAT.format(value));
                    }
                }).addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
                    @Override
                    public ZonedDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
                        return ZonedDateTime.parse(p.getText(), MOSCOW_TIME_FORMAT);
                    }
                });
    }

    String getAuthorizationToken(SberbankApiScope scope) throws BankCommunicationException {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("grant_type", "client_credentials");
        request.add("scope", scope.getValue());

        HttpHeaders headers = new HttpHeaders();
        headers.set("RqUID", requestUidGenerator.generateRqUID());
        headers.set("x-ibm-client-id", config.getClientId());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(config.getAuthToken());

        ResponseEntity<AccessTokenRs> response = restTemplate.postForEntity(buildUrl(SberbankEndpoint.AUTH, scope == SberbankApiScope.PAY),
                new HttpEntity<>(request, headers), AccessTokenRs.class);
        return response.getBody().getAccessToken();
    }

    /**
     * Запрос в Сбербанк на проведение операции оплаты.
     *
     * @param qrPayLoad информация из QR кода, считанного с мобильного устройства покупателя, в виде строки вида
     *                  "https://sberbank.ru/qr/?ClientIdQr=xxx&HashId=yyy&TimeStamp=1234&online"
     * @param amount    сумма операции в минимальных единицах валюты (копейках)
     */
    public PayRusClientQRRs.Status pay(String qrPayLoad, int amount) throws BankException {
        String token = getAuthorizationToken(SberbankApiScope.PAY);
        String rqUID = requestUidGenerator.generateRqUID();
        ZonedDateTime now = timeSupplier.currentMoscowTime();

        PayRusClientQRRq request = new PayRusClientQRRq();
        request.setRqUID(rqUID);
        request.setRqTm(now);
        request.setMemberId(config.getMemberId());
        request.setPartnerOrderNumber(generateUniqueOrderNumber(now));
        request.setTerminalId(config.getTerminalId());
        request.setIdQR(config.getIdQR());
        request.setPayLoad(qrPayLoad);
        request.setAmount(amount);
        request.setCurrency(CURRENCY);
        // TODO пока не работает без этого поля, хотя должно
        request.setOperationMessage("-");

        PayRusClientQRRsWrapper response = post(SberbankEndpoint.PAY, request, generateHeaders(rqUID, token), PayRusClientQRRsWrapper.class, true);
        return response.getPayRusClientQRRs().getStatus();
    }

    /**
     * Запрос на получение статуса и информации об операции с повторными попытками при ошибке таймаута
     *
     * @param orderId номер заказа в ППРБ.Карты (Сбербанк)
     */
    public ResponseEntity<OrderStatusRequestQrRs> statusWithRetries(String orderId) throws BankCommunicationException {
        ResponseEntity<OrderStatusRequestQrRs> response = null;
        int retries = STATUS_RETRIES_COUNT;
        while (response == null && retries > 0) {
            try {
                response = status(orderId);
            } catch (RestClientException e) {
                if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                    log.debug("Timeout exception in status request. Retries left: {}", retries);
                    retries--;
                } else {
                    throw e;
                }
            }
        }
        if (retries == 0) {
            throw new BankCommunicationException(ResBundleBankSberbankQr.getString("STATUS_TIMEOUT_ERROR"));
        }
        return response;
    }

    ResponseEntity<OrderStatusRequestQrRs> status(String orderId) throws BankCommunicationException {
        String token = getAuthorizationToken(SberbankApiScope.STATUS);
        String rqUID = requestUidGenerator.generateRqUID();

        OrderStatusRequestQrRq request = new OrderStatusRequestQrRq();
        request.setRqUID(rqUID);
        request.setRqTm(timeSupplier.currentMoscowTime());
        request.setOrderId(orderId);

        return restTemplate.postForEntity(buildUrl(SberbankEndpoint.STATUS, false),
                new HttpEntity<>(request, generateHeaders(rqUID, token)), OrderStatusRequestQrRs.class);
    }

    /**
     * Запрос на отмену/возврат финансовой операции.
     *
     * @param orderId            номер заказа в ППРБ.Карты (Сбербанк)
     * @param operationId        уникальный идентификатор операции в ППРБ.Карты (Сбербанк
     * @param authCode           код авторизации
     * @param cancelOperationSum сумма в минимальных единицах валюты (копейках), которую нужно отменить/возвратить
     * @param originalIdQR               idQR оригинальной транзакции
     */
    public OrderCancelQrRs.Status cancel(String orderId, String operationId, String authCode, int cancelOperationSum, String originalIdQR) throws BankCommunicationException {
        String token = getAuthorizationToken(SberbankApiScope.CANCEL);
        String rqUID = requestUidGenerator.generateRqUID();

        OrderCancelQrRq request = new OrderCancelQrRq();
        request.setRqUID(rqUID);
        request.setRqTm(timeSupplier.currentMoscowTime());
        request.setOrderId(orderId);
        request.setOperationId(operationId);
        request.setAuthCode(authCode);
        request.setIdQR(originalIdQR);
        request.setCancelOperationSum(cancelOperationSum);
        request.setOperationCurrency(CURRENCY);

        OrderCancelQrRs response = post(SberbankEndpoint.CANCEL, request, generateHeaders(rqUID, token), OrderCancelQrRs.class, false);
        return response.getStatus();
    }

    /**
     * Запрос на формирование заказа в АС Сбербанка.
     *
     * @param orderSum сумма заказа
     */
    public OrderCreationQrRs.Status creation(int orderSum) throws BankCommunicationException {
        String token = getAuthorizationToken(SberbankApiScope.CREATE);
        String rqUID = requestUidGenerator.generateRqUID();
        ZonedDateTime now = timeSupplier.currentMoscowTime();

        OrderCreationQrRq request = new OrderCreationQrRq();
        request.setRqUID(rqUID);
        request.setRqTm(now);
        request.setMemberId(config.getMemberId());
        request.setOrderNumber(generateUniqueOrderNumber(now));
        request.setOrderCreateDate(now);
        request.setIdQR(config.getIdQR());
        request.setOrderSum(orderSum);
        request.setCurrency(CURRENCY);

        OrderCreationQrRs response = post(SberbankEndpoint.CREATE, request, generateHeaders(rqUID, token), OrderCreationQrRs.class, false);
        return response.getStatus();
    }

    /**
     * Запрос на отмену заказа, по которому еще не была проведена финансовая операция
     *
     * @param orderId номер заказа
     */
    public OrderRevocationQrRs.Status revocation(String orderId) throws BankCommunicationException {
        String token = getAuthorizationToken(SberbankApiScope.REVOKE);
        String rqUID = requestUidGenerator.generateRqUID();

        OrderRevocationQrRq request = new OrderRevocationQrRq();
        request.setRqUID(rqUID);
        request.setRqTm(timeSupplier.currentMoscowTime());
        request.setOrderId(orderId);

        OrderRevocationQrRs response = post(SberbankEndpoint.REVOKE, request, generateHeaders(rqUID, token), OrderRevocationQrRs.class, false);
        return response.getStatus();
    }

    private String buildUrl(SberbankEndpoint endpoint, boolean forPay) {
        if (forPay) {
            return config.getUrl().getValuePay() + endpoint.getPath();
        } else {
            return config.getUrl().getValue() + endpoint.getPath();
        }
    }

    private HttpHeaders generateHeaders(String rqUID, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ibm-client-id", config.getClientId());
        headers.set("x-Introspect-RqUID", rqUID);
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <Q, S> S post(SberbankEndpoint endpoint, Q request, HttpHeaders headers, Class<S> responseClass, boolean forPay) throws BankCommunicationException {
        try {
            ResponseEntity<S> response = restTemplate.postForEntity(buildUrl(endpoint, forPay), new HttpEntity<>(request, headers), responseClass);
            final S body = response.getBody();
            if (body == null) {
                throw new BankCommunicationException(ResBundleBankSberbankQr.getString("BANK_COMMUNICATION_EXCEPTION"));
            }
            return body;
        } catch (RestClientException e) {
            throw new BankCommunicationException(ResBundleBankSberbankQr.getString("BANK_COMMUNICATION_EXCEPTION"), e);
        }
    }

    private String generateUniqueOrderNumber(ZonedDateTime now) {
        return config.getTerminalId() + ORDER_NUMBER_TIME_FORMATTER.format(now);
    }

    private static void checkConfig(SberbankApiConfig config) throws BankConfigException {
        Set<String> errors = new HashSet<>();
        checkFieldNotNull(errors, config.getClientId(), "clientId");
        checkFieldNotNull(errors, config.getClientSecret(), "clientSecret");
        checkFieldNotNull(errors, config.getCertificate(), "certificate");
        checkFieldNotNull(errors, config.getCertificatePassword(), "certificatePassword");
        checkFieldNotNull(errors, config.getMemberId(), "memberId");
        checkFieldNotNull(errors, config.getTerminalId(), "terminalId");
        checkFieldNotNull(errors, config.getIdQR(), "idQR");
        if (!errors.isEmpty()) {
            throw new BankConfigException("The following config parameters are required for Sberbank QR: " + StringUtils.join(errors, ", "));
        }
    }

    private static void checkFieldNotNull(Set<String> errors, String field, String fieldName) {
        if (StringUtils.isBlank(field)) {
            errors.add(fieldName);
        }
    }

    RestTemplate getRestTemplate() {
        return restTemplate;
    }

}
