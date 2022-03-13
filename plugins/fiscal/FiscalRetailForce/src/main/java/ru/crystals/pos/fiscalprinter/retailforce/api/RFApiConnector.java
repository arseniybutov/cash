package ru.crystals.pos.fiscalprinter.retailforce.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.crystals.pos.fiscalprinter.retailforce.RetailForceConfig;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.Document;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPayment;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentValidationError;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalClientStatus;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalResponse;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayDocumentResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RFApiConnector {

    private static final Logger log = LoggerFactory.getLogger(RFApiConnector.class);

    private static final String VAT_NUMBER = "[0] VatNumber";

    private final RestTemplate rtJson;
    private final RestTemplate rtText;
    private final RetailForceConfig config;
    private final ObjectMapper objectMapper;
    private final ParameterizedTypeReference<List<DocumentValidationError>> validationResponseType =
            new ParameterizedTypeReference<List<DocumentValidationError>>() {
            };
    private final ParameterizedTypeReference<List<DocumentPayment>> actualStockResponseType =
            new ParameterizedTypeReference<List<DocumentPayment>>() {
            };

    public RFApiConnector(final RetailForceConfig config) {
        this.config = config;

        objectMapper = new RFJsonConverter().getObjectMapper();

        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        httpRequestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(30));

        final ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(httpRequestFactory);
        rtJson = new RestTemplate(factory);
        rtText = new RestTemplate(factory);
        final RFRequestInterceptor logInterceptor = new RFRequestInterceptor();

        rtText.setMessageConverters(Collections.singletonList(new StringHttpMessageConverter(StandardCharsets.UTF_8)));
        rtText.setInterceptors(Collections.singletonList(logInterceptor));

        rtJson.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
        rtJson.setInterceptors(Collections.singletonList(logInterceptor));


    }

    RestTemplate getRtJson() {
        return rtJson;
    }

    RestTemplate getRtText() {
        return rtText;
    }

    /**
     * Получить версию Retail Force
     */
    public String getVersion() {
        return getString(url("information/version"));
    }

    /**
     * Получить id лицензии (нужен для запросас clientId)
     */
    public String getLicenseConsumerId() {
        final String url = urlTemplate("management/clients/licenseConsumerId")
                .queryParam("identification", config.getIdentification())
                .queryParam("type", VAT_NUMBER)
                .build().toUriString();
        return getString(url);
    }

    /**
     * Получить clientId по номеру магазина, кассы и лицензии
     */
    public String getClientId(final String licenseConsumerId) {
        final String url = urlTemplate("management/clients/id")
                .queryParam("licenseConsumerId", licenseConsumerId)
                .queryParam("storeNumber", config.getStoreNumber())
                .queryParam("terminalNumber", config.getTerminalNumber())
                .build().toUriString();
        return getString(url);
    }

    /**
     * Получить статус фискального клиента
     */
    public FiscalClientStatus getStatus(final String clientId) {
        final String url = urlTemplate("information/client/{clientId}/status")
                .uriVariables(Collections.singletonMap("clientId", clientId))
                .build().toUriString();
        return getData(url, FiscalClientStatus.class);
    }

    public List<DocumentPayment> getActualStock(final String clientId) {
        final String url = urlTemplate("closing/{clientId}/actualStock")
                .uriVariables(Collections.singletonMap("clientId", clientId))
                .build().toUriString();

        return getData(url, actualStockResponseType);
    }

    public EndOfDayDocumentResponse getEndOfDayDocument(final String clientId) {
        final String url = urlTemplate("closing/{clientId}/endofdayDocument")
                .uriVariables(Collections.singletonMap("clientId", clientId))
                .build().toUriString();

        return getData(url, EndOfDayDocumentResponse.class);
    }

    /**
     * Зарегистрировать фискального клиента
     */
    public String registerClient() {
        final String url = urlTemplate("management/clients/byCloud")
                .queryParam("type", VAT_NUMBER)
                .queryParam("identification", config.getIdentification())
                .queryParam("storeNumber", config.getStoreNumber())
                .queryParam("terminalNumber", config.getTerminalNumber())
                .queryParam("cloudApiKey", config.getCloudApiKey())
                .build().toUriString();
        final ResponseEntity<String> response = rtText.exchange(url, HttpMethod.PUT, makeSecretRequest(), String.class);
        return StringUtils.strip(response.getBody(), "\"");
    }

    /**
     * Проинициализировать фискального клиента, делается один раз. Необходимость можно проверить через {@link #getStatus(String)}
     */
    public void initialize(final Document document) {
        postDocument(document, "management/clients/initialize", FiscalResponse.class);
    }

    /**
     * Подключиться для возможности выполнения дальнейших команд формирования документов
     */
    public void connect(final String clientId) {
        final String url = urlTemplate("management/cloud/connect")
                .queryParam("cloudApiKey", config.getCloudApiKey())
                .queryParam("clientId", clientId)
                .build().toUriString();
        rtText.exchange(url, HttpMethod.POST, makeSecretRequest(), String.class);
    }

    /**
     * Проверить документ на корреткность
     */
    public List<DocumentValidationError> validate(final Document document) {
        return rtJson.exchange(url("transactions/validateDocument"), HttpMethod.POST, request(document), validationResponseType).getBody();
    }

    /**
     * Создать документ (первый этап регистрации)
     */
    public FiscalResponse create(final String clientId, final DocumentType documentType) {
        final String url = UriComponentsBuilder.fromHttpUrl(config.getUrl())
                .pathSegment("api", "v1", "transactions/createDocument")
                .queryParam("uniqueClientId", clientId)
                .queryParam("documentType", objectMapper.convertValue(documentType, String.class))
                .build().toUriString();
        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonHeaders());
        final ResponseEntity<FiscalResponse> response = rtJson.exchange(url, HttpMethod.PUT, requestEntity, FiscalResponse.class);
        return response.getBody();
    }

    /**
     * Зарегистрировать документ (второй этап регистрации)
     */
    public FiscalResponse storeDocument(final Document document) {
        try {
            return postDocument(document, "transactions/storeDocument", FiscalResponse.class);
        } catch (HttpClientErrorException.UnprocessableEntity e) {
            final List<DocumentValidationError> errors = validate(document);
            if (!errors.isEmpty()) {
                log.error("Validation errors: {}", errors);
            }
            throw new IllegalArgumentException("Invalid document to store: " + e.getMessage());
        }
    }

    /**
     * Аннулировать открытый документ (выполняется только после создания)
     */
    public FiscalResponse cancelDocument(final Document document) {
        return postDocument(document, "transactions/cancelDocument", FiscalResponse.class);
    }

    private <T> T postDocument(final Document doc, final String url, final Class<T> responseClass) {
        final ResponseEntity<T> response = rtJson.exchange(url(url), HttpMethod.POST, request(doc), responseClass);
        return response.getBody();
    }

    private HttpEntity<Document> request(final Document document) {
        return new HttpEntity<>(document, jsonHeaders());
    }

    private HttpHeaders jsonHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getString(final String url) {
        try {
            return getData(url, String.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private <T> T getData(final String url, final ParameterizedTypeReference<T> responseClass) {
        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonHeaders());
        final ResponseEntity<T> response = rtJson.exchange(url, HttpMethod.GET, requestEntity, responseClass);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return null;
        }
        throw new RuntimeException(response.getStatusCode().getReasonPhrase());
    }

    private <T> T getData(final String url, final Class<T> responseClass) {
        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonHeaders());
        final ResponseEntity<T> response = rtJson.exchange(url, HttpMethod.GET, requestEntity, responseClass);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return null;
        }
        throw new RuntimeException(response.getStatusCode().getReasonPhrase());
    }

    private HttpEntity<String> makeSecretRequest() {
        return new HttpEntity<>("'" + config.getCloudApiSecret() + "'", jsonHeaders());
    }

    private String url(final String method) {
        return UriComponentsBuilder.fromHttpUrl(config.getUrl())
                .pathSegment("api", "v1", method).build().toUriString();
    }

    private UriComponentsBuilder urlTemplate(final String method) {
        return UriComponentsBuilder.fromHttpUrl(config.getUrl())
                .pathSegment("api", "v1", method);
    }
}
