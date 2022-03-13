package ru.crystals.pos.bank.translink.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.UncheckedIOException;

public class TranslinkJsonConverterTest {

    private static ObjectMapper om;

    static TranslinkJsonConverter converter;

    /**
     * Writer для нормализации результатов при сравнение JSON
     */
    static ObjectWriter writer;

    @BeforeClass
    public static void setUp() {
        converter = new TranslinkJsonConverter();
        om = converter.getObjectMapper();
        writer = om.writerWithDefaultPrettyPrinter();
    }

    <T> T readAsObject(String fileName, Class<T> valueType) {
        try {
            return om.readValue(this.getClass().getResource("/api/" + fileName + ".json"), valueType);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    String readAsString(String fileName) {
        try {
            return writer.writeValueAsString(om.readTree(this.getClass().getResource("/api/" + fileName + ".json")));
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

}