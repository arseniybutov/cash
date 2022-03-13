package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.cashboxsystem.ResBundleFiscalPrinterCBS;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.RequestProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.HeaderProperties;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils.DataSerializer;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс http запроса. Содержит данные/методы для формирования заголовка и сериализации body.
 */
public abstract class BaseRequest {

    @JsonIgnore
    private static final String REQUEST_METHOD = "POST";
    @JsonIgnore
    private String body;
    @JsonIgnore
    private List<RequestProperty> httpProperties;

    public BaseRequest() {
        httpProperties = new ArrayList<>();
        httpProperties.add(new RequestProperty(HeaderProperties.CONTENT_TYPE, "application/json"));
        httpProperties.add(new RequestProperty(HeaderProperties.USER_AGENT, "cbs-client"));
    }

    public abstract String getTarget();

    /**
     * Возвращает тип ответа на запрос, BaseResponse используется если в ответе не ожидается данных от CBS
     * @return класс ответа на текущий запрос
     */
    public abstract <T> Class<T> getResponseClass();

    public String getRequestMethod() {
        return REQUEST_METHOD;
    }

    public String getBody() {
        return body;
    }

    public List<RequestProperty> getHttpProperties() {
        return httpProperties;
    }

    public void addHttpProperty(RequestProperty property){
        httpProperties.add(property);
    }

    /**
     * Сериализует поля запроса и сохраняет результат в {@link #body}
     */
    public void generateBody() throws FiscalPrinterCommunicationException {
        try {
            body = DataSerializer.getInstance().serialize(this);
        } catch (IOException e) {
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterCBS.getString("ERROR_SERIALIZE"), CashErrorType.FISCAL_ERROR);
        }
    }
}
