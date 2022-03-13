package ru.crystals.pos.bank.zvt.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncodingUtilsTest {

    @Test
    public void extractMessageTest() {
        String decodedAdditionalText = "Cancellation approved/AS-Proc-Code = 00 906 00\n" +
                "Capt.-Ref.= 0005";

        final String message = EncodingUtils.extractMessage(decodedAdditionalText);

        assertEquals("Cancellation approved", message);
    }
}