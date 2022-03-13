package ru.crystals.pos.bank.translink.api;

import org.apache.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.translink.ResBundleBankTranslink;
import ru.crystals.pos.bank.translink.api.dto.ExecutePosCmdRequest;
import ru.crystals.pos.bank.translink.api.dto.OpenPosRequest;
import ru.crystals.pos.bank.translink.api.dto.OpenPosResponse;
import ru.crystals.pos.bank.translink.api.dto.Result;
import ru.crystals.pos.bank.translink.api.dto.ResultCode;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class RequestSenderImpl implements RequestSender {

    private static final Logger log = LoggerFactory.getLogger(RequestSenderImpl.class);

    private String url;

    private final RestTemplate rt;
    private final TranslinkJsonConverter jsonConverter;
    private String licenseToken;
    private AtomicReference<String> accessToken = new AtomicReference<>();
    private String host = "localhost";
    private int port = 6678;
    private String version = "v102";

    public RequestSenderImpl() {
        url = makeApiUrl();

        jsonConverter = new TranslinkJsonConverter();
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
        httpRequestFactory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(30));

        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(httpRequestFactory);
        rt = new RestTemplate(factory);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(jsonConverter.getObjectMapper());
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
        rt.setMessageConverters(Collections.singletonList(converter));
        rt.setInterceptors(Collections.singletonList(new TranslinkRequestInterceptor()));
    }

    public void setVersion(String version) {
        this.version = version;
        url = makeApiUrl();
    }

    @Override
    public void setLicenseToken(String licenseToken) {
        this.licenseToken = licenseToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken.set(accessToken);
    }

    @Override
    public void setHost(String host) {
        this.host = host;
        url = makeApiUrl();
    }

    @Override
    public void setPort(int port) {
        this.port = port;
        url = makeApiUrl();
    }

    private String makeApiUrl() {
        return String.format("http://%s:%s/%s/{method}", host, port, version);
    }

    @Override
    public OpenPosResponse openPos() throws BankCommunicationException {
        final OpenPosRequest request = new OpenPosRequest(licenseToken, null, null, null);
        try {
            final ResponseEntity<OpenPosResponse> response = repeatOnNoResponse(() -> rt.postForEntity(url, request, OpenPosResponse.class, ApiMethod.openpos));
            checkResponse(response);
            return response.getBody();
        } catch (ResourceAccessException rae) {
            throw new BankCommunicationException(ResBundleBankTranslink.getForResultCode(ResultCode.CONNECTION_ERROR), rae);
        }
    }

    @Override
    public Result closePos() throws BankCommunicationException {
        if (accessToken.get() == null) {
            return new Result(ResultCode.NOT_INITILIAZED, null);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.get());
        ResponseEntity<Result> response;
        try {
            response = repeatOnNoResponse(() -> rt.postForEntity(url, new HttpEntity<>(headers), Result.class, ApiMethod.closepos));
        } catch (ResourceAccessException rae) {
            throw new BankCommunicationException(ResBundleBankTranslink.getForResultCode(ResultCode.CONNECTION_ERROR), rae);
        } catch (HttpClientErrorException.Unauthorized unauthorized) {
            accessToken.set(null);
            return new Result(ResultCode.NOT_INITILIAZED, null);
        }
        checkResponse(response);
        accessToken.set(null);
        return response.getBody();
    }

    @Override
    public Result sendCommand(Command cmd) throws BankCommunicationException {
        return sendCommand(cmd, null);
    }

    @Override
    public Result sendCommand(Command cmd, CommandParams params) throws BankCommunicationException {
        cmd.checkApplicableParams(params);
        final ExecutePosCmdRequest request = jsonConverter.prepareRequest(cmd, params);
        if (cmd.getResultClass() == Result.class) {
            final Result result = executePostCmd(request);
            Objects.requireNonNull(result);
            return result;
        }
        throw new IllegalArgumentException("Unsupported result class " + cmd.getResultClass());
    }

    @Override
    public BaseEvent getEvent() throws BankCommunicationException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.get());
        try {
            final ResponseEntity<BaseEvent> response = repeatOnNoResponse(() -> rt.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), BaseEvent.class, ApiMethod.getEvent));
            checkResponse(response);
            return response.getBody();
        } catch (ResourceAccessException rae) {
            throw new BankCommunicationException(ResBundleBankTranslink.getForResultCode(ResultCode.CONNECTION_ERROR), rae);
        }
    }

    private Result executePostCmd(ExecutePosCmdRequest req) throws BankCommunicationException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.get());
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Result> response;
        try {
            response = repeatOnNoResponse(() -> rt.postForEntity(url, new HttpEntity<>(req, headers), Result.class, ApiMethod.executeposcmd));
        } catch (ResourceAccessException rae) {
            throw new BankCommunicationException(ResBundleBankTranslink.getForResultCode(ResultCode.CONNECTION_ERROR), rae);
        } catch (HttpClientErrorException.Unauthorized unauthorized) {
            return new Result(ResultCode.NOT_INITILIAZED, null);
        }
        checkResponse(response);
        return response.getBody();
    }

    private <T> void checkResponse(ResponseEntity<T> response) {
        Objects.requireNonNull(response.getBody());
    }

    /**
     * Периодически xConnector возвращает пустые ответы
     */
    private <T> T repeatOnNoResponse(Supplier<T> requestCall) {
        int attempts = 5;
        while (true) {
            try {
                return requestCall.get();
            } catch (ResourceAccessException rae) {
                if (rae.getRootCause() instanceof NoHttpResponseException) {
                    attempts--;
                    log.warn("Empty response received (remain attempts: {}): {}", attempts, rae.getMessage());
                    if (attempts > 0) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new RuntimeException("Thread interrupted");
                        }
                        continue;
                    }
                }
                throw rae;
            }
        }
    }

    RestTemplate getRestTemplate() {
        return rt;
    }
}
