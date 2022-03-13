package ru.crystals.pos.bank.odengiqr.api.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.client.RestTemplate;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.odengiqr.ResBundleBankODengiQR;
import ru.crystals.pos.bank.odengiqr.TimeSupplier;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;
import ru.crystals.pos.bank.odengiqr.api.dto.ODengiCommand;
import ru.crystals.pos.bank.odengiqr.api.dto.RequestResponseContainer;
import ru.crystals.pos.bank.odengiqr.api.dto.request.cancel.InvoiceCancelRq;
import ru.crystals.pos.bank.odengiqr.api.dto.request.create.CreateInvoiceRq;
import ru.crystals.pos.bank.odengiqr.api.dto.request.create.Views;
import ru.crystals.pos.bank.odengiqr.api.dto.request.status.StatusPaymentRq;
import ru.crystals.utils.time.StopTimer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ODengiApi {

    private static final Logger LOG = LoggerFactory.getLogger(ODengiApi.class);
    /**
     * Алгоритм вычисления поля hash для запросов
     */
    private static final String HASH_ALGORITHM = "HmacMD5";
    /**
     * Версия API ОДенег
     */
    private static final int VERSION = 1005;
    /**
     * Язык интерфейса (не участвует в наших запросах, но поле обязательное)
     */
    private static final String LANG = "ru";
    /**
     * Пока только для поля Payment#datePay
     */
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .toFormatter();
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .registerModule(getTimeModule());
    private final RestTemplate restTemplate;

    private final ODengiConfig config;
    private final TimeSupplier timeSupplier;

    public ODengiApi(ODengiConfig config) {
        this.config = config;
        this.restTemplate = restTemplate();
        this.timeSupplier = new TimeSupplier();
    }

    private static SimpleModule getTimeModule() {
        return new JavaTimeModule()
                .addSerializer(Instant.class, new JsonSerializer<Instant>() {
                    @Override
                    public void serialize(Instant value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString(Long.toString(value.toEpochMilli()));
                    }
                })
                .addDeserializer(Instant.class, new JsonDeserializer<Instant>() {
                    @Override
                    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        return Instant.ofEpochMilli(Long.parseLong(p.getText()));
                    }
                })
                .addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        return LocalDateTime.parse(p.getText(), LOCAL_DATE_TIME_FORMATTER);
                    }
                });
    }

    private static RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
        httpRequestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));
        BufferingClientHttpRequestFactory buffering = new BufferingClientHttpRequestFactory(httpRequestFactory);
        RestTemplate restTemplate = new RestTemplate(buffering);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(MAPPER);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        restTemplate.setMessageConverters(Collections.singletonList(converter));
        restTemplate.setInterceptors(Collections.singletonList(new ODengiQrLoggingInterceptor()));
        return restTemplate;
    }

    public RequestResponseContainer createInvoice(int amount, String currencyCode, String message) throws BankCommunicationException {
        Instant now = timeSupplier.now();

        CreateInvoiceRq createInvoiceRq = new CreateInvoiceRq();
        createInvoiceRq.setAmount(amount);
        createInvoiceRq.setOrderId(generateOrderId());
        createInvoiceRq.setCurrency(currencyCode);
        createInvoiceRq.setDesc(message);

        RequestResponseContainer request = new RequestResponseContainer();
        request.setData(createInvoiceRq);

        return request(request, ODengiCommand.CREATE_INVOICE, now);
    }

    public RequestResponseContainer statusPayment(String invoiceId, String orderId) throws BankCommunicationException {
        StatusPaymentRq statusPaymentRq = new StatusPaymentRq();
        statusPaymentRq.setInvoiceId(invoiceId);
        statusPaymentRq.setOrderId(orderId);
        return request(new RequestResponseContainer(statusPaymentRq), ODengiCommand.STATUS_PAYMENT);
    }

    public RequestResponseContainer invoiceCancel(String invoiceId) throws BankCommunicationException {
        InvoiceCancelRq invoiceCancelRq = new InvoiceCancelRq();
        invoiceCancelRq.setInvoiceId(invoiceId);
        return request(new RequestResponseContainer(invoiceCancelRq), ODengiCommand.INVOICE_CANCEL);
    }

    private RequestResponseContainer request(RequestResponseContainer requestResponseContainer,
                                             ODengiCommand command) throws BankCommunicationException {
        return request(requestResponseContainer, command, timeSupplier.now());
    }

    private RequestResponseContainer request(RequestResponseContainer request, ODengiCommand command, Instant now) throws BankCommunicationException {
        RequestResponseContainer response;
        try {
            request.setVersion(VERSION);
            request.setLang(LANG);
            request.setCommand(command.getCommand());
            request.setSellerID(config.getSellerID());
            request.setMkTime(now);
            setHash(request);

            MappingJacksonValue jacksonValue = new MappingJacksonValue(request);
            jacksonValue.setSerializationView(Views.WithHash.class);
            response = restTemplate.postForObject(config.getUrl().getUrl(),
                    new HttpEntity<>(jacksonValue), RequestResponseContainer.class);
        } catch (Exception e) {
            throw new BankCommunicationException(ResBundleBankODengiQR.getString("BANK_COMMUNICATION_EXCEPTION"), e);
        }
        validateResponse(response);
        return response;
    }

    private static void validateResponse(RequestResponseContainer response) throws BankCommunicationException {
        if (response == null) {
            throw new BankCommunicationException(ResBundleBankODengiQR.getString("BANK_COMMUNICATION_EXCEPTION"));
        }
        Data data = response.getData();
        if (data.getError() != null) {
            LOG.error("Response with error {}: {}", data.getError(), data.getDesc());
            throw new BankCommunicationException(data.getDesc());
        }
    }

    /**
     * Сгенерировать и проставить order_id в запрос (уникальный номер заказа в магазине)
     */
    private static String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Добавляет поле hash к запросу
     * ВАЖНО для вычисления hash использовать тот же mapper, который будет использован в дальнейшем в запросах
     */
    void setHash(RequestResponseContainer request) throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {
        // сериализуем без поля hash
        String requestWithoutHash = MAPPER.writerWithView(Views.WithoutHash.class).writeValueAsString(request);
        LOG.trace("request without hash: {}", requestWithoutHash);

        // вычисляем hash по сериализованному объекту
        StopTimer timer = new StopTimer();
        SecretKeySpec key = new SecretKeySpec(config.getSellerPassword().getBytes(), HASH_ALGORITHM);
        Mac mac = Mac.getInstance(HASH_ALGORITHM);
        mac.init(key);
        byte[] bytes = mac.doFinal(requestWithoutHash.getBytes(StandardCharsets.UTF_8));
        String hash = DatatypeConverter.printHexBinary(bytes).toLowerCase();
        LOG.trace("hash {}, calculated in {}", hash, timer);

        request.setHash(hash);
    }
}
