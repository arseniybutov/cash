package ru.crystals.pos.fiscalprinter.az.airconn.commands;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.az.airconn.ResBundleFiscalAirConn;
import ru.crystals.pos.fiscalprinter.az.airconn.model.AirConnError;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.BaseRequest;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.az.airconn.serializer.DataSerializer;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.IOException;

/**
 * Абстарктный базовый класс формирования комады. Хранит, сериализует/десериализует данные.
 *
 * @param <P>  Тип объекта параметров запроса
 * @param <RD> Тип данных ожидаемых в ответе {@link BaseResponse} на команду
 */
public abstract class BaseCommand<P, RD> {

    private BaseRequest request;
    private BaseResponse response;
    private P parameters;
    private RD responseData;

    /**
     * Тип возвращаемых в ответе на команду данных
     * @return ожидаемый в ответе class обекта
     */
    public abstract Class<RD> getResponseDataClass();

    protected BaseRequest getRequest() {
        if (request == null) {
            request = new BaseRequest();
        }
        return request;
    }

    protected P getParameters() {
        return parameters;
    }

    protected void setParameters(P parameters) {
        this.parameters = parameters;
    }

    public String getOperationId() {
        return getRequest().getOperationId();
    }

    /**
     * Сериализует данные запроса
     *
     * @return запрос в Json формате
     * @throws Exception при ошибках сериализации
     */
    public String serializeRequest() throws FiscalPrinterCommunicationException {
        try {
            saveRequestParameters();
            return DataSerializer.serialize(getRequest());
        } catch (IOException e) {
            throw new FiscalPrinterCommunicationException(ResBundleFiscalAirConn.getString("ERROR_SERIALIZE"), CashErrorType.FISCAL_ERROR);
        }
    }

    /**
     * Десериализует и сохраняет данные ответа
     *
     * @param jsonResponse ответ на команду в Json формате
     */
    public void deserializeResponse(String jsonResponse) throws FiscalPrinterCommunicationException {
        try {
            response = DataSerializer.deserialize(jsonResponse, BaseResponse.class);
            if (response.getData() != null) {
                responseData = DataSerializer.convertData(response.getData(), getResponseDataClass());
            }
        } catch (IOException e) {
            throw new FiscalPrinterCommunicationException(ResBundleFiscalAirConn.getString("ERROR_DESERIALIZE"), CashErrorType.FISCAL_ERROR);
        }
    }

    public RD getResponseData() {
        return responseData;
    }

    public void checkForApiError() throws FiscalPrinterException {
        if (response != null) {
            AirConnError error = AirConnError.getErrorByCode(response.getCode());
            if (error != AirConnError.SUCCESS) {
                throw new FiscalPrinterException(response.getMessage(), CashErrorType.FISCAL_ERROR);
            }
        }
    }

    /**
     * Метод для сохранения объекта параметоров в Request
     */
    private void saveRequestParameters() {
        getRequest().setParameters(parameters);
    }
}
