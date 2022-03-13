package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.cashboxsystem.ResBundleFiscalPrinterCBS;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.BaseRequest;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.MDEncoder;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.RSAEncoder;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpTransport {
    private static final Logger LOG = LoggerFactory.getLogger(HttpTransport.class);

    private HttpURLConnection connection;
    private BaseRequest request;

    private static final String CBS_SERVICE_URL = "http://localhost:46722";
    private static final int CONNECT_TIMEOUT = 4000;
    /**
     * По документации время ответа не более 16 секунд в онлайн режиме и не более 2 секунд в оффлайн режиме работы ПФ.
     */
    private static final int READ_TIMEOUT = 16000;

    public HttpTransport(BaseRequest request) throws FiscalPrinterCommunicationException {
        if (request == null) {
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterCBS.getString("INVALID_REQUEST"), CashErrorType.FISCAL_ERROR);
        }
        this.request = request;
        request.generateBody();
    }

    /**
     * Отправляет переданный в HttpTransport запрос в CBS
     * @return json с ответом CBS на запрос
     * @throws FiscalPrinterCommunicationException при ошибках в передачи/приеме данных
     */
    public String send() throws FiscalPrinterCommunicationException {
        try {
            signData();
            connect();
            try (OutputStream os = connection.getOutputStream()) {
                os.write(request.getBody().getBytes(StandardCharsets.UTF_8));
                os.flush();
                LOG.info("{} <-- {} Request : {}", request.getRequestMethod(), request.getTarget(), request.getBody());
            }
            return readFromResponseStream();
        } catch (Exception e) {
            LOG.error("send() error: ", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterCBS.getString("CONNECTION_ERROR"), CashErrorType.FISCAL_ERROR);
        } finally {
            disconnect();
        }
    }

    private void signData() throws IOException {
        if (request.getBody() == null) {
            LOG.warn("Request {} body is empty!", request.getTarget());
        }

        String dataToEncode = request.getTarget() + request.getBody();
        String encodedData = RSAEncoder.getInstance().encodeData(MDEncoder.digestData(dataToEncode, MDEncoder.ENCODE_ALGORITHM_MD5));

        RequestProperty eds = new RequestProperty(HeaderProperties.EDS, encodedData);
        request.addHttpProperty(eds);
    }

    private void connect() throws IOException {
        String fullUrl = CBS_SERVICE_URL + request.getTarget();
        connection = (HttpURLConnection) new URL(fullUrl).openConnection();
        connection.setRequestMethod(request.getRequestMethod());
        for (RequestProperty property : request.getHttpProperties()) {
            connection.setRequestProperty(property.getPropertyName(), property.getPropertyValue());
        }

        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        connection.connect();
    }

    private void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private String readFromResponseStream() throws IOException {
        try (InputStream responseStream = getResponseStream()) {
            if (responseStream == null) {
                throw new IOException(ResBundleFiscalPrinterCBS.getString("ERROR_NO_DATA"));
            }
            String response = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
            LOG.info("{} --> Response : {}", request.getRequestMethod(), response);
            return response;
        }
    }

    private InputStream getResponseStream() throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        } else {
            LOG.error("Response ERROR: {}", connection.getResponseCode());
            return connection.getErrorStream();
        }
    }

}
