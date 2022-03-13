package ru.crystals.scales.magellan;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DataParserTest {

    private static DataParser parser = new DataParser();

    @Test
    public void parseTest() {
        Assert.assertEquals(Arrays.asList(
                DeviceResponse.weight(98765),
                DeviceResponse.barcode("9900000521431")
        ), parser.tryParseData("S14498765" + (char) 0x0D +
                "S08F9900000521431" + (char) 0x0D));
    }
}