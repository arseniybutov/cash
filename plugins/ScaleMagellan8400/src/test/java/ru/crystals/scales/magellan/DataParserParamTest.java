package ru.crystals.scales.magellan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static ru.crystals.scales.magellan.DeviceResponse.barcode;
import static ru.crystals.scales.magellan.DeviceResponse.weight;
import static ru.crystals.scales.magellan.DeviceResponse.weightError;

@RunWith(Parameterized.class)
public class DataParserParamTest {

    private static DataParser parser = new DataParser();

    final String message;
    final String rawData;
    final DeviceResponse expectedResponse;

    public DataParserParamTest(final String message, final String rawData, final DeviceResponse expectedResponse) {
        this.message = message;
        this.rawData = rawData;
        this.expectedResponse = expectedResponse;
    }

    @Parameterized.Parameters(name = "rawData: {0}, expected: {1}")
    public static Iterable<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{
                // Полные ШК
                {"ШК EAN-13 (F)", "S08F9900000521431", barcode("9900000521431")},
                {"ШК EAN-8 (FF)", "S08FF46218636", barcode("46218636")},
                {"ШК 2/5 (B1)", "S08B11234567890", barcode("1234567890")},
                {"ШК 2/5 (B2)", "S08B21234567892", barcode("1234567892")},
                {"ШК Code-128 (B3)", "S08B30105000190075501", barcode("0105000190075501")},
                {"ШК Datamatrix (Dm)", "S08Dm12345ABCDEF", barcode("12345ABCDEF")},
                {"ШК QR (QR)", "S08QR12345ABCDEF", barcode("12345ABCDEF")},
                {"ШК GS1 (]e0)", "S08]e00109900000521431", barcode("0109900000521431")},
                {"Неизвестный формат ШК (односимвольный префикс)", "S08X9900000521431", barcode("9900000521431")},

                // ШК с минимальной длиной
                {"Минимальный ШК EAN-13 (F)", "S08F9", barcode("9")},
                {"Минимальный ШК EAN-8 (FF)", "S08FF4", barcode("4")},
                {"Минимальный ШК 2/5 (B1)", "S08B13", barcode("3")},
                {"Минимальный ШК 2/5 (B2)", "S08B24", barcode("4")},
                {"Минимальный ШК Code-128 (B3)", "S08B30", barcode("0")},
                {"Минимальный ШК Datamatrix (Dm)", "S08Dm1", barcode("1")},
                {"Минимальный ШК QR (QR)", "S08QR1", barcode("1")},
                {"Минимальный ШК GS1 (]e0)", "S08]e01", barcode("1")},
                {"Минимальный неизвестный формат ШК (односимвольный префикс)", "S08X9", barcode("9")},

                // Битые ШК (известные префиксы, но без данных)
                {"Битый ШК EAN-13 (F)", "S08F", null},
                {"Битый ШК EAN-8 (FF)", "S08FF", null},
                {"Битый ШК 2/5 (B1)", "S08B1", null},
                {"Битый ШК 2/5 (B2)", "S08B2", null},
                {"Битый ШК Code-128 (B3)", "S08B3", null},
                {"Битый ШК Datamatrix (Dm)", "S08Dm", null},
                {"Битый ШК QR (QR)", "S08QR", null},
                {"Битый ШК GS1 (]e0)", "S08]e0", null},
                {"Битый неизвестный формат ШК (односимвольный префикс)", "S08X", null},
                // Битые пакеты (без префиксов)
                {"Неполноценный пакет ШК считаем отсутствием ШК (SRTZ-170)", "S0", null},
                {"Неполноценный пакет ШК считаем отсутствием ШК (SRTZ-170)", "S08F", null},

                // Получение веса
                {"Вес 98765 г", "S14498765", weight(98765)},
                {"Вес 195 г", "S14400195", weight(195)},
                {"Вес 4 г", "S14400004", weight(4)},

                // Ошибки при получении веса
                {"Код ошибки ВЕСЫ НЕ ГОТОВЫ", "S140", weightError(DeviceError.SCALES_NOT_READY)},
                {"Код ошибки ВЕС СТАБИЛИЗИРУЮТСЯ", "S141", weightError(DeviceError.SCALES_STABILIZING)},
                {"Код ошибки ВЕСЫ ПЕРЕГРУЖЕНЫ", "S142", weightError(DeviceError.SCALES_OVERLOAD)},
                {"Код ошибки НУЛЕВОЙ ВЕС", "S143", weightError(DeviceError.ZERO_WEIGHT)},
                {"Код ошибки ОТРИЦАТЕЛЬНЫЙ ВЕС", "S145", weightError(DeviceError.NEGATIVE_WEIGHT)},
                {"Неизвестный код ошибки 9", "S149", weightError(DeviceError.UNKNOWN_ERROR)},
                {"Неполноценный пакет веса считаем нулевым весом", "S14", weight(0)},
                {"Неполноценный пакет веса считаем нулевым весом", "S1", weight(0)},
        });
    }

    @Test
    public void parseTest() {
        assertEquals(message, expectedResponse, parser.parseData(rawData));
    }
}