package ru.crystals.pos.bank.zvt.protocol;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class TransactionDataParser {

    public Map<TransactionField, String> parse(String rawData) {

        final EnumMap<TransactionField, String> result = new EnumMap<>(TransactionField.class);

        int offset = 0;
        while (offset < rawData.length()) {
            final String fieldCode = rawData.substring(offset, offset + 2);
            TransactionField fieldType = TransactionField.getByCode(fieldCode);
            Objects.requireNonNull(fieldType);
            offset += 2;
            if (fieldType.getLength() != 0) {
                final String value = rawData.substring(offset, offset + fieldType.getLength() * 2);
                result.put(fieldType, value);
                offset += fieldType.getLength() * 2;
                continue;
            }
            if (fieldType.getDataType() == FieldDataType.LLVAR) {
                final int length = Integer.parseInt(rawData.substring(offset, offset + 2 * 2).replace("F", ""));
                offset += 2 * 2;
                final String value = rawData.substring(offset, offset + length * 2);
                result.put(fieldType, value);
                offset += length * 2;
                continue;

            }
            if (fieldType.getDataType() == FieldDataType.LLLVAR) {
                final int length = Integer.parseInt(rawData.substring(offset, offset + 3 * 2).replace("F", ""));
                offset += 3 * 2;
                final String value = rawData.substring(offset, offset + length * 2);
                result.put(fieldType, value);
                offset += length * 2;
                continue;
            }
            throw new IllegalStateException();

        }
        return result;
    }
}
