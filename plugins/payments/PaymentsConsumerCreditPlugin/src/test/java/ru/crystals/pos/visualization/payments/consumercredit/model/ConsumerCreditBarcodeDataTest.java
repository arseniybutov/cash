package ru.crystals.pos.visualization.payments.consumercredit.model;


import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class ConsumerCreditBarcodeDataTest {

    private static final String BARCODE = "742VTB#123213KB-12#87165-A324#0.00#1000.00#0#yOLg7e7iIMji4O0gyOLg7e7i6Pc=";

    @Test
    public void testCorrectBarcode() {
        ConsumerCreditBarcodeData consumerCreditBarcodeData = ConsumerCreditBarcodeData.getEntity(BARCODE);
        Assert.assertEquals("", "742VTB", consumerCreditBarcodeData.getBankCode());
        Assert.assertEquals("123213KB-12", consumerCreditBarcodeData.getProductCode());
        Assert.assertEquals("87165-A324", consumerCreditBarcodeData.getContractNumber());
        Assert.assertEquals(100000L, consumerCreditBarcodeData.getCreditBaseSum());
        Assert.assertEquals(100000L, consumerCreditBarcodeData.getCreditSum());
        Assert.assertEquals("Иванов Иван Иванович", consumerCreditBarcodeData.getFIO());
    }

    @Test
    public void incorrectBarcode() {
        ConsumerCreditBarcodeData excess = ConsumerCreditBarcodeData.getEntity(BARCODE + "#excessElement");
        ConsumerCreditBarcodeData missing = ConsumerCreditBarcodeData.getEntity(BARCODE.substring(7));
        Assert.assertNull(excess);
        Assert.assertNull(missing);
    }

    @Test
    public void incorrectBankCode() {
        ConsumerCreditBarcodeData consumerCreditBarcodeData = ConsumerCreditBarcodeData.getEntity("toManyCharsAtBankCode" + BARCODE);
        Assert.assertNull(consumerCreditBarcodeData);
    }

    @Test
    public void emptyStringScanned() {
        ConsumerCreditBarcodeData dataNull = ConsumerCreditBarcodeData.getEntity(null);
        ConsumerCreditBarcodeData dataEmpty = ConsumerCreditBarcodeData.getEntity(StringUtils.EMPTY);
        Assert.assertNull(dataNull);
        Assert.assertNull(dataEmpty);
    }
}