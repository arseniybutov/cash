package ru.crystals.pos.fiscalprinter.pirit.core;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

public class HexIntegerDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        try {
            if (key.startsWith("0x")) {
                return Integer.parseInt(key.substring(2), 16);
            }
            return Integer.parseInt(key, 16);
        } catch (Exception e) {
            return ctxt.handleWeirdKey(Integer.class, key,
                    "Failed to deserialize %s: (%s) %s",
                    Integer.class.getName(), e.getClass().getName(), e.getMessage());
        }
    }
}