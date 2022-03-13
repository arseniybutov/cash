package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests.BaseRequest;

import java.io.IOException;

public class DataSerializer {

    private static DataSerializer ourInstance;
    private ObjectMapper objectMapper;

    private DataSerializer() {
        objectMapper = new ObjectMapper();

        //Если поле пустое - игнорируем
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //Работаетаем по полям, не по Get методам
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        //Если объект пустой все равно формируем
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static DataSerializer getInstance() {
        if (ourInstance == null) {
            ourInstance = new DataSerializer();
        }
        return ourInstance;
    }

    /**
     * Сериализует объект запроса в json
     * @param request запрос для сериализации
     * @return сериализованная строка
     */
    public String serialize(BaseRequest request) throws IOException  {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
    }

    /**
     * Десериализует json в объект ответа
     * @param json строка для десериализации
     * @param responseClass класс объекта ответа
     * @return объект ответа от CBS
     */
    public <T> T deserialize(String json, Class<T> responseClass) throws IOException {
        return objectMapper.readValue(json, responseClass);
    }
}
