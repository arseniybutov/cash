package ru.crystals.pos.fiscalprinter.transport;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author dalex
 */
public class DataPacketTest {

    @Test
    public void getLongValueFromDoubleString() throws Exception {
        DataPacket sut = new DataPacket(String.join(DataPacket.FS, "", "0", "1000", "1001.0", "1003.01", "1004."));

        assertEquals(0L, sut.getDoubleMoneyToLongValue(0));
        assertEquals(0L, sut.getDoubleMoneyToLongValue(1));
        assertEquals(100000L, sut.getDoubleMoneyToLongValue(2));
        assertEquals(100100L, sut.getDoubleMoneyToLongValue(3));
        assertEquals(100301L, sut.getDoubleMoneyToLongValue(4));
        assertEquals(100400L, sut.getDoubleMoneyToLongValue(5));
    }

    @Test
    public void getDoubleValueFromDoubleString() throws Exception {
        DataPacket sut = new DataPacket(String.join(DataPacket.FS, "0.01", "1.1", "2", "3.0", "4.", ""));

        assertEquals(0L, sut.getDoubleToRoundLong(0));
        assertEquals(1L, sut.getDoubleToRoundLong(1));
        assertEquals(2L, sut.getDoubleToRoundLong(2));
        assertEquals(3L, sut.getDoubleToRoundLong(3));
        assertEquals(4L, sut.getDoubleToRoundLong(4));
        assertEquals(0L, sut.getDoubleToRoundLong(5));
    }

    @Test
    public void testHasNotNullValuesWhenEmpty() {
        DataPacket dp = new DataPacket();
        assertFalse(dp.hasNonNullValues());
    }

    @Test
    public void testOnlyNullValues() {
        DataPacket dp = new DataPacket();
        dp.putStringValue(null);
        dp.putStringValue(null);
        dp.putStringValue(null);
        assertFalse(dp.hasNonNullValues());
    }

    @Test
    public void testHasNotNullValues() {
        DataPacket dp = new DataPacket();
        dp.putStringValue(null);
        dp.putStringValue(null);
        dp.putStringValue("123");
        assertTrue(dp.hasNonNullValues());
    }

    @Test
    public void testWithSpaces() {
        DataPacket dp = new DataPacket();
        dp.putStringValue(null);
        dp.putStringValue(" ");
        assertTrue(dp.hasNonNullValues());
    }
}
