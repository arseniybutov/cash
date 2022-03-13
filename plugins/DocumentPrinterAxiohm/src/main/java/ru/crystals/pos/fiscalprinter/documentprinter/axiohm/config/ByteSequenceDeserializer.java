package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;

public class ByteSequenceDeserializer {

    public static class ByteSequenceKeyDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            try {
                return ByteSequenceDeserializer.deserialize(key);
            } catch (Exception e) {
                return (byte[]) ctxt.handleWeirdKey(ByteSequence.class, key,
                        "Failed to deserialize %s: (%s) %s",
                        ByteSequence.class.getName(), e.getClass().getName(), e.getMessage());
            }
        }
    }

    public static class ByteSequenceValueDeserializer extends JsonDeserializer<ByteSequence> {

        @Override
        public ByteSequence deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return ByteSequenceDeserializer.deserialize(p.getText());
            } catch (Exception e) {
                return (ByteSequence) ctxt.handleWeirdStringValue(ByteSequence.class, p.getText(),
                        "Failed to deserialize %s: (%s) %s",
                        ByteSequence.class.getName(), e.getClass().getName(), e.getMessage());
            }
        }
    }

    private static ByteSequence deserialize(String value) throws DecoderException {
        if (value == null) {
            return null;
        }
        return new ByteSequence(Hex.decodeHex(value
                .replace(" ", "")
                .replace(",", "")
                .toCharArray()));
    }
}
