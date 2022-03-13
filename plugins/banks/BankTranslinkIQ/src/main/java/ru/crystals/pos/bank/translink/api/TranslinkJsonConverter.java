package ru.crystals.pos.bank.translink.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import ru.crystals.pos.bank.translink.api.dto.ExecutePosCmdHeader;
import ru.crystals.pos.bank.translink.api.dto.ExecutePosCmdRequest;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TranslinkJsonConverter {

    private final ObjectMapper objectMapper;

    public TranslinkJsonConverter() {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter)))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String serialize(Command command, CommandParams params) {
        return serialize(prepareRequest(command, params));
    }

    public String serialize(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String serialize(Command command, CommandParams params, ObjectWriter objectWriter) {
        return serialize(prepareRequest(command, params), objectWriter);
    }

    String serialize(Object request, ObjectWriter objectWriter) {
        try {
            return objectWriter.writeValueAsString(request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ExecutePosCmdRequest prepareRequest(Command command, CommandParams params) {
        return new ExecutePosCmdRequest(new ExecutePosCmdHeader(command), params);
    }

    public String serializeRequest(CommandParams params) {
        final Command command = Command.getByParamClass(params.getClass());
        Objects.requireNonNull(command, () -> "Command not found for params class " + params.getClass());
        return serialize(command, params);
    }
}
