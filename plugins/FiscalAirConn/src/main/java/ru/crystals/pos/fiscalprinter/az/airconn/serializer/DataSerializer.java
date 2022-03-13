package ru.crystals.pos.fiscalprinter.az.airconn.serializer;

import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.BaseRequest;
import ru.crystals.util.JsonMappers;

import java.io.IOException;
import java.util.Map;

public class DataSerializer {

    /**
     * Сериализует объект запроса в json
     *
     * @param request запрос {@link BaseRequest} для сериализации
     * @return сериализованная строка
     */
    public static String serialize(BaseRequest request) throws IOException {
        return JsonMappers.getDefaultMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request);
    }

    /**
     * Десериализует json в объект
     *
     * @param json          строка для десериализации
     * @param responseClass класс объекта
     * @return десериализованный объект
     */
    public static <RS> RS deserialize(String json, Class<RS> responseClass) throws IOException {
        return JsonMappers.getDefaultMapper().readValue(json, responseClass);
    }

    public static <D> D convertData(Map<String, Object> data, Class<D> responseDataClass) throws IOException {
        return JsonMappers.getDefaultMapper().convertValue(data, responseDataClass);
    }
}
