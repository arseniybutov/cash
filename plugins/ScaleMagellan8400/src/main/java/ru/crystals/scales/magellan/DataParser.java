package ru.crystals.scales.magellan;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataParser {
    /**
     * Формат ответа с ШК: S08(?<prefix>.{1,3})(?<barcode>.+), поэтому минимальная длина 5
     */
    private static final int MIN_BARCODE_DATA_LENGTH = 5;
    private static final char SCANNER_DEVICE = '0';
    private static final char SCALE_DEVICE = '1';
    private static final int BARCODE_PREFIX_START_INDEX = 3;
    private static final String ETX = String.valueOf((char) 0x0D);
    private static final String[] TWO_DIGITS_BARCODE_PREFIXES = new String[]{"FF", "B1", "B2", "B3", "Dm", "QR"};
    private static final String GS1_PREFIX = "]e0";

    public List<DeviceResponse> tryParseData(String readString) {
        if (readString == null || readString.isEmpty()) {
            return Collections.emptyList();
        }
        if (readString.contains(ETX)) {
            return Arrays.stream(readString.split(ETX))
                    .map(this::parseData)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        final DeviceResponse result = parseData(readString);
        if (result == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(result);
    }

    DeviceResponse parseData(String data) {
        if (data.length() <= 1) {
            return null;
        }
        if (data.charAt(1) == SCANNER_DEVICE) {
            return parseBarcode(data);
        } else if (data.charAt(1) == SCALE_DEVICE) {
            return parseWeight(data);
        }
        return null;
    }

    private DeviceResponse parseBarcode(String data) {
        if (data.length() < MIN_BARCODE_DATA_LENGTH) {
            return null;
        }
        if (data.startsWith(GS1_PREFIX, BARCODE_PREFIX_START_INDEX)) {
            if (data.length() > 6) {
                return DeviceResponse.barcode(data.substring(6));
            }
            return null;
        }
        if (startsWithAny(data, TWO_DIGITS_BARCODE_PREFIXES, BARCODE_PREFIX_START_INDEX)) {
            if (data.length() > 5) {
                return DeviceResponse.barcode(data.substring(5));
            }
            return null;
        }
        return DeviceResponse.barcode(data.substring(4));
    }

    private boolean startsWithAny(String barcodeWithPrefix, String[] prefixes, int offset) {
        for (String prefix : prefixes) {
            if (barcodeWithPrefix.startsWith(prefix, offset)) {
                return true;
            }
        }
        return false;
    }

    private DeviceResponse parseWeight(String data) {
        if (data.length() >= 8) {
            try {
                return DeviceResponse.weight(Integer.parseInt(data.substring(4)));
            } catch (NumberFormatException nfe) {
                return DeviceResponse.weight(0);
            }
        }
        if (data.length() > 3) {
            return DeviceResponse.weightError(DeviceError.getByErrorCode(data.charAt(3)));
        }
        // Невалидный пакет с ошибкой считаем разовым сбоем и просто отвечаем, что вес нулевой
        return DeviceResponse.weight(0);
    }
}
