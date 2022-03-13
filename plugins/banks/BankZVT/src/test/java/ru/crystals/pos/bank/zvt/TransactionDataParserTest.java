package ru.crystals.pos.bank.zvt;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.bank.zvt.protocol.TransactionDataParser;
import ru.crystals.pos.bank.zvt.protocol.TransactionField;

import java.util.EnumMap;
import java.util.Map;

public class TransactionDataParserTest {

    @Test
    public void parseTest() {
        Map<TransactionField, String> expected = new EnumMap<>(TransactionField.class);
        expected.put(TransactionField.RESULT_CODE, "00");
        expected.put(TransactionField.AMOUNT, "000000000069");
        expected.put(TransactionField.TRACE, "000086");
        expected.put(TransactionField.ORIGINAL_TRACE, "000085");
        expected.put(TransactionField.TIME, "154844");
        expected.put(TransactionField.DATE, "0302");
        expected.put(TransactionField.EXPIRATION_DATE, "3012");
        expected.put(TransactionField.CC_PAYMENT_TYPE, "60");
        expected.put(TransactionField.PAN, "4149011500000147");
        expected.put(TransactionField.TERMINAL_ID, "69299844");
        expected.put(TransactionField.AID, "3733303237350000");
        expected.put(TransactionField.RECEIPT_NO, "0016");
        expected.put(TransactionField.CARD_TYPE, "0A");
        expected.put(TransactionField.VU_NUMBER, "202020202020313537373737373736");
        expected.put(TransactionField.ADDITIONAL_TEXT,
                "43616E63656C6C6174696F6E20617070726F7665642F41532D50726F632D436F6465203D203030203930362030300A436170742E2D5265662E3D20303030350A");
        expected.put(TransactionField.TURNOVER_NO, "000016");
        expected.put(TransactionField.CARD_NAME, "56697361");

        String raw = "2700" +
                "04000000000069" +
                "0B000086" +
                "37000085" +
                "0C154844" +
                "0D0302" +
                "2969299844" +
                "22F0F84149011500000147" +
                "870016" +
                "3B3733303237350000" +
                "1960" +
                "0E3012" +
                "8A0A" +
                "8BF0F456697361" +
                "2A202020202020313537373737373736" +
                "3CF0F6F443616E63656C6C6174696F6E20617070726F7665642F41532D50726F632D436F6465203D203030203930362030300A436170742E2D5265662E3D20303030350A" +
                "88000016";

        final Map<TransactionField, String> parsed = new TransactionDataParser().parse(raw);
        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void parseFailedTxnTest() {
        Map<TransactionField, String> expected = new EnumMap<>(TransactionField.class);
        expected.put(TransactionField.RESULT_CODE, "38");
        expected.put(TransactionField.AMOUNT, "000000000069");
        expected.put(TransactionField.TRACE, "000087");
        expected.put(TransactionField.TIME, "081622");
        expected.put(TransactionField.DATE, "0303");
        expected.put(TransactionField.EXPIRATION_DATE, "2206");
        expected.put(TransactionField.SEQ_NO, "0001");
        expected.put(TransactionField.CC_PAYMENT_TYPE, "60");
        expected.put(TransactionField.PAN, "5536913809070044");
        expected.put(TransactionField.TERMINAL_ID, "69299844");
        expected.put(TransactionField.RECEIPT_NO, "0017");
        expected.put(TransactionField.CARD_TYPE, "06");
        expected.put(TransactionField.VU_NUMBER, "202020202020313537373737373736");
        expected.put(TransactionField.ADDITIONAL_TEXT,
                "496E76616C696420436172642F41532D50726F632D436F6465203D203030203037352030300A436170742E2D5265662E3D20303030320A");
        expected.put(TransactionField.TURNOVER_NO, "000017");
        expected.put(TransactionField.CARD_NAME, "4D6173746572632E");

        String raw = "2738040000000000690B0000870C0816220D0303296929984422F0F8553691380907004417000187" +
                "001719600E22068A068BF0F84D6173746572632E2A2020202020203135373737373737363CF0F5F5496E76616C696420436172642F41" +
                "532D50726F632D436F6465203D203030203037352030300A436170742E2D5265662E3D20303030320A88000017";

        final Map<TransactionField, String> parsed = new TransactionDataParser().parse(raw);
        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testDataDailyLogData() {
        String raw = "2700" +
                "04000000000000" +
                "60F0F5F30017001600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "0C150801" +
                "0D0303";

        Map<TransactionField, String> expected = new EnumMap<>(TransactionField.class);
        expected.put(TransactionField.RESULT_CODE, "00");
        expected.put(TransactionField.AMOUNT, "000000000000");
        expected.put(TransactionField.TIME, "150801");
        expected.put(TransactionField.DATE, "0303");
        expected.put(TransactionField.SINGLE_AMOUNTS, "0017001600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        testParser(raw, expected);
    }

    @Test
    public void testRegistrationData() {
        String raw = "2969299844490978";

        Map<TransactionField, String> expected = new EnumMap<>(TransactionField.class);
        expected.put(TransactionField.TERMINAL_ID, "69299844");
        expected.put(TransactionField.CURRENCY_CODE, "0978");
        testParser(raw, expected);
    }

    private void testParser(String raw, Map<TransactionField, String> expected) {
        final Map<TransactionField, String> parsed = new TransactionDataParser().parse(raw);
        Assert.assertEquals(expected, parsed);
    }
}