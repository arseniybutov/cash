package ru.crystals.pos.bank.inpas.smartsale;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class FieldCollectionTest {

    private FieldCollection fc;

    @Before
    public void init(){
        fc = new FieldCollection();
        fc.setAmount(1345L);
        fc.setAdditionalAmount(512L);
        fc.setAuthCode("123456");
        fc.setCardEntryMode((byte) 1);
        fc.setCashTransId(123456L);
        fc.setCurrencyCode("123456");
        fc.setHostTransId(123456L);
        fc.setMerchantId("123456");
        fc.setOperationCode(4L);
        fc.setPAN("123456");
    }

    @Test
    public void toArray() throws IOException {

        fc.setRefNumber("2");
        StringBuilder sb = new StringBuilder();
        for (byte b : fc.toArray()) {
            sb.append(b).append(" ");
        }
        assertEquals("0 4 0 49 51 52 53 1 3 0 53 49 50 4 1 0 48 8 1 0 49 10 6 0 49 50 51 52 53 54 13 6 0 49 50 51 52 53 54 14 1 0 50 25 1 0 52 26 6 0 49 50 51 52 53 54 ", sb.toString());
    }

    @Test
    public void toArrayWithoutRRN() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte b : fc.toArray()) {
            sb.append(b).append(" ");
        }
        assertEquals("0 4 0 49 51 52 53 1 3 0 53 49 50 4 1 0 48 8 1 0 49 10 6 0 49 50 51 52 53 54 13 6 0 49 50 51 52 53 54 25 1 0 52 26 6 0 49 50 51 52 53 54 ", sb.toString());
    }

}
