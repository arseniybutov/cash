package ru.crystals.pos.bank.demir.response;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResponseTest {

    @Test
    public void testParseResponse() {
        String okResponse = "82004900010100000000010000000141343000                        PS0000350000230000094***********3265   " +
                "210204201400100000000003305100P1035200624560080008000A0000000031010                  " +
                "A2C04852D024F25AVISA                            013УСПЕШНАЯ ТРАНЗАК                        ";
        Response response = Response.parse(okResponse);
        assertTrue(response.isOk());
        assertEquals("103520062456", response.getRRN());
        assertEquals(LocalDateTime.parse("2021-02-04T20:14:00"), response.getDateTime());
    }
}