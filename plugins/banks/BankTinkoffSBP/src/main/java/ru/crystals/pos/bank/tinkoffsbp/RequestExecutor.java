package ru.crystals.pos.bank.tinkoffsbp;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import ru.crystals.pos.bank.commonsbpprovider.RequestInterceptor;
import ru.crystals.pos.bank.commonsbpprovider.SBPProviderConfig;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RequestExecutor {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private SBPProviderConfig config;

    public RequestExecutor(SBPProviderConfig config) {
        this.config = config;

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        httpRequestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(5));
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
        restTemplate.setInterceptors(Collections.singletonList(new RequestInterceptor()));
    }

    public <T, R> T executeRequest(String requestPath, HttpMethod httpMethod, HttpEntity<R> httpEntity, Class<T> responseType,
                                   Object... variables) {
        ResponseEntity<T> response = restTemplate.exchange(config.getUrl() + requestPath, httpMethod, httpEntity, responseType, variables);
        return response.getBody();
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

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
