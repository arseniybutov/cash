package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeDocumentType;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeEntity;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapeException;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapePayment;
import ru.crystals.pos.fiscalprinter.controltape.ControlTapePosition;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;

/**
 *
 * @author Tatarinov Eduard
 */
public class PiritReportDataTest {

    private static final int PIRIT_CHECK_SALE = 2;
    private static final int PIRIT_CHECK_RETURN = 3;
    private static final int PIRIT_MONEY_IN = 4;
    private static final int PIRIT_MONEY_OUT = 5;
    private static final String CASHIER = "Администратор";
    private static final String INN = "1234567890";
    private static final String REG_NUMBER = "1234567890";
    private static final long DOC_NUM = 32L;
    private static final long SHIFT_NUM = 12L;
    private static final long DOC_SUMM = 34600L;
    private static final long GOODS_QUANT = 10000L;
    private static final String GOODS_ITEM = "00001";
    private static final String GOODS_NAME = "Хлеб малиновый сладкий";
    private static final String CASH_PAYMENT = "НАЛИЧНЫЕ";
    private static final String CASH_IN = "ВНЕСЕНИЕ";
    private static final String CASH_OUT = "ИЗЪЯТИЕ";
    private static final String DISCOUNT_NAME = "Округление копеек";
    private static final long DISCOUNT_SUMM = 4600L;
    private static final long DIVIDER_SUMM = 100L;
    private static final long DIVIDER_QUANT = 10L;

    private static final int U16 = 2;
    private static final int U32 = 4;
    private static final int U64 = 8;
    private static final int DATE_BYTE_COUNT = 7;
    private static final int UI_BYTE_COUNT = 12;
    private static final int SECTION_OPEN_SIZE = 49;
    private static final int SECTION_CLOSE_SIZE = 30;
    private static final int SECTION_ITEM_SIZE = 118;
    private static final int SECTION_TOTAL_SIZE = 12;
    private static final int SECTION_CASHINOUT_SIZE = 56;
    private static final int SECTION_PAYMENT_SIZE = 75;
    private static final int SECTION_DISCOUNT_SIZE = 61;

    public PiritReportDataTest() {

    }

    @Test
    public void testGetCheckSale() throws UnsupportedEncodingException, ControlTapeException {
        byte[] data = new byte[345];
        int index = 0;
        System.arraycopy(createOpenSection(PIRIT_CHECK_SALE), 0, data, index, SECTION_OPEN_SIZE);
        System.arraycopy(createItemSection(getGoods()), 0, data, index += SECTION_OPEN_SIZE, SECTION_ITEM_SIZE);
        System.arraycopy(createDiscountSection((byte) 0x06, false, false, DISCOUNT_NAME, 0, DISCOUNT_SUMM), 0, data, index += SECTION_ITEM_SIZE, SECTION_DISCOUNT_SIZE);
        System.arraycopy(createTotalSection((byte) 0x0a, 0x02, DOC_SUMM), 0, data, index += SECTION_DISCOUNT_SIZE, SECTION_TOTAL_SIZE);
        System.arraycopy(createPaymentSection(CASH_PAYMENT, DOC_SUMM), 0, data, index += SECTION_TOTAL_SIZE, SECTION_PAYMENT_SIZE);
        System.arraycopy(createCloseSection((byte)0x02, DOC_SUMM), 0, data, index + SECTION_PAYMENT_SIZE, SECTION_CLOSE_SIZE);

        ControlTapeEntity cte = PiritReportData.parseReport(data, INN, REG_NUMBER, new ArrayList<PaymentType>(), true);

        Assert.assertEquals(cte.getNumberDocument(), Long.valueOf(DOC_NUM));
        Assert.assertEquals(cte.getShiftNum(), Long.valueOf(SHIFT_NUM));
        Assert.assertEquals(cte.getDocumentType(), ControlTapeDocumentType.SALE);
        Assert.assertEquals(CASHIER, cte.getCashier().getName().trim());
        Assert.assertEquals(cte.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertNotNull(cte.getSknoUi());

        Assert.assertEquals(1, cte.getPayments().size());
        ControlTapePayment payment = cte.getPayments().get(0);
        Assert.assertEquals(payment.getSumma(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(CASH_PAYMENT, payment.getNamePayment().trim());

        Assert.assertEquals(1, cte.getPositions().size());
        ControlTapePosition position = cte.getPositions().get(0);
        Assert.assertEquals(GOODS_NAME, position.getNameGoods().trim());
        Assert.assertEquals(position.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(position.getPrice(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(position.getAmount(), Long.valueOf( GOODS_QUANT / DIVIDER_QUANT));
    }

        @Test
    public void testGetCheckReturn() throws UnsupportedEncodingException, ControlTapeException {
        byte[] data = new byte[345];
        int index = 0;
        System.arraycopy(createOpenSection(PIRIT_CHECK_RETURN), 0, data, index, SECTION_OPEN_SIZE);
        System.arraycopy(createItemSection(getGoods()), 0, data, index += SECTION_OPEN_SIZE, SECTION_ITEM_SIZE);
        System.arraycopy(createDiscountSection((byte) 0x06, false, false, DISCOUNT_NAME, 0, DISCOUNT_SUMM), 0, data, index += SECTION_ITEM_SIZE, SECTION_DISCOUNT_SIZE);
        System.arraycopy(createTotalSection((byte) 0x0a, 0x02, DOC_SUMM), 0, data, index += SECTION_DISCOUNT_SIZE, SECTION_TOTAL_SIZE);
        System.arraycopy(createPaymentSection(CASH_PAYMENT, DOC_SUMM), 0, data, index += SECTION_TOTAL_SIZE, SECTION_PAYMENT_SIZE);
        System.arraycopy(createCloseSection((byte)0x02, DOC_SUMM), 0, data, index + SECTION_PAYMENT_SIZE, SECTION_CLOSE_SIZE);

        ControlTapeEntity cte = PiritReportData.parseReport(data, INN, REG_NUMBER, new ArrayList<PaymentType>(), true);

        Assert.assertEquals(cte.getNumberDocument(), Long.valueOf(DOC_NUM));
        Assert.assertEquals(cte.getShiftNum(), Long.valueOf(SHIFT_NUM));
        Assert.assertEquals(cte.getDocumentType(), ControlTapeDocumentType.RETURN);
        Assert.assertEquals(CASHIER, cte.getCashier().getName().trim());
        Assert.assertEquals(cte.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertNotNull(cte.getSknoUi());

        Assert.assertEquals(1, cte.getPayments().size());
        ControlTapePayment payment = cte.getPayments().get(0);
        Assert.assertEquals(payment.getSumma(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(CASH_PAYMENT, payment.getNamePayment().trim());

        Assert.assertEquals(1, cte.getPositions().size());
        ControlTapePosition position = cte.getPositions().get(0);
        Assert.assertEquals(GOODS_NAME, position.getNameGoods().trim());
        Assert.assertEquals(position.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(position.getPrice(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertEquals(position.getAmount(), Long.valueOf( GOODS_QUANT / DIVIDER_QUANT));
    }

    @Test
    public void testGetCashIn() throws UnsupportedEncodingException, ControlTapeException {
        byte[] data = new byte[210];
        int index = 0;
        System.arraycopy(createOpenSection(PIRIT_MONEY_IN), 0, data, index, SECTION_OPEN_SIZE);
        System.arraycopy(createCashInOutSection(true, DOC_SUMM), 0, data, index += SECTION_OPEN_SIZE, SECTION_CASHINOUT_SIZE);
        System.arraycopy(createPaymentSection(CASH_PAYMENT, DOC_SUMM), 0, data, index += SECTION_CASHINOUT_SIZE, SECTION_PAYMENT_SIZE);
        System.arraycopy(createCloseSection((byte) 0x02, DOC_SUMM), 0, data, index + SECTION_PAYMENT_SIZE, SECTION_CLOSE_SIZE);

        ControlTapeEntity cte = PiritReportData.parseReport(data, INN, REG_NUMBER, new ArrayList<PaymentType>(), true);

        Assert.assertEquals(cte.getNumberDocument(), Long.valueOf(DOC_NUM));
        Assert.assertEquals(cte.getShiftNum(), Long.valueOf(SHIFT_NUM));
        Assert.assertEquals(cte.getDocumentType(), ControlTapeDocumentType.IN_CASH);
        Assert.assertEquals(CASHIER, cte.getCashier().getName().trim());
        Assert.assertEquals(cte.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertNotNull(cte.getSknoUi());

    }

    @Test
    public void testGetCashOut() throws UnsupportedEncodingException, ControlTapeException {
        byte[] data = new byte[210];
        int index = 0;
        System.arraycopy(createOpenSection(PIRIT_MONEY_OUT), 0, data, index, SECTION_OPEN_SIZE);
        System.arraycopy(createCashInOutSection(true, DOC_SUMM), 0, data, index += SECTION_OPEN_SIZE, SECTION_CASHINOUT_SIZE);
        System.arraycopy(createPaymentSection(CASH_PAYMENT, DOC_SUMM), 0, data, index += SECTION_CASHINOUT_SIZE, SECTION_PAYMENT_SIZE);
        System.arraycopy(createCloseSection((byte) 0x02, DOC_SUMM), 0, data, index + SECTION_PAYMENT_SIZE, SECTION_CLOSE_SIZE);

        ControlTapeEntity cte = PiritReportData.parseReport(data, INN, REG_NUMBER, new ArrayList<PaymentType>(), true);

        Assert.assertEquals(cte.getNumberDocument(), Long.valueOf(DOC_NUM));
        Assert.assertEquals(cte.getShiftNum(), Long.valueOf(SHIFT_NUM));
        Assert.assertEquals(cte.getDocumentType(), ControlTapeDocumentType.OUT_CASH);
        Assert.assertEquals(CASHIER, cte.getCashier().getName().trim());
        Assert.assertEquals(cte.getSumm(), Long.valueOf(DOC_SUMM / DIVIDER_SUMM));
        Assert.assertNotNull(cte.getSknoUi());
    }

    private byte[] createOpenSection(int typeDoc) throws UnsupportedEncodingException {
        byte[] result = new byte[SECTION_OPEN_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = 0x01;
        result[index++] = (byte) typeDoc;
        System.arraycopy(getDataAsByteArray(1, U16), 0, result, index, U16);
        System.arraycopy(getDataAsByteArray(1, U16), 0, result, index += U16, U16);
        System.arraycopy(getDataAsByteArray(32, U32), 0, result, index += U16, U32);
        System.arraycopy(getDataAsByteArray(12, U16), 0, result, index += U32, U16);
        System.arraycopy(getDataAsByteArray(12, U32), 0, result, index += U16, U32);
        System.arraycopy(getDate(LocalDateTime.now()), 0, result, index += U32, DATE_BYTE_COUNT);
        System.arraycopy(getString(CASHIER, 24), 0, result, index + DATE_BYTE_COUNT, 24);
        result[SECTION_OPEN_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] createCloseSection(byte idSection, long summ) {
        byte[] result = new byte[SECTION_CLOSE_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = idSection;
        System.arraycopy(getDataAsByteArray(summ, U64), 0, result, index, U64);
        System.arraycopy(getDate(LocalDateTime.now()), 0, result, index += U64, DATE_BYTE_COUNT);
        System.arraycopy(getRandomByte(UI_BYTE_COUNT), 0, result, index + DATE_BYTE_COUNT, UI_BYTE_COUNT);
        result[SECTION_CLOSE_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] createItemSection(Goods goods) throws UnsupportedEncodingException {
        byte[] result = new byte[SECTION_ITEM_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = 0x05;
        result[index++] = 0x00;
        System.arraycopy(getDataAsByteArray(goods.getEndPricePerUnit(), U64), 0, result, index, U64);
        System.arraycopy(getDataAsByteArray(goods.getQuant(), U64), 0, result, index += U64, U64);
        System.arraycopy(getDataAsByteArray(goods.getEndPositionPrice(), U64), 0, result, index += U64, U64);
        System.arraycopy(new byte[5] , 0, result, index += U64, 5);
        System.arraycopy(getString(String.valueOf(goods.getPositionNum()), 5), 0, result, index += 5, 5);
        System.arraycopy(getString(goods.getItem(), 19), 0, result, index += 5, 19);
        System.arraycopy(getString(goods.getName(), 57), 0, result, index += 19, 57);
        System.arraycopy(getDataAsByteArray(1, U16), 0, result, index += 57, U16);
        System.arraycopy(getDataAsByteArray(1, U16), 0, result, index + U16, U16);
        result[SECTION_ITEM_SIZE -1] = calcCRC(result);
        return result;
    }

    private byte[] createTotalSection(byte idSection, int docType, long summ) {
       byte[] result = new byte[SECTION_TOTAL_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = idSection;
        result[index++] = (byte) docType;
        System.arraycopy(getDataAsByteArray(summ, U64), 0, result, index, U64);
        result[SECTION_TOTAL_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] createDiscountSection(byte idSection, boolean onCheck, boolean isPercentType, String name, long percent, long summ) throws UnsupportedEncodingException {
      byte[] result = new byte[SECTION_DISCOUNT_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = idSection;
        result[index++] = (byte) (onCheck ? 0x00 : 0x01);
        result[index++] = (byte) (isPercentType ? 0x00 : 0x01);
        result[index++] = 0x00;
        System.arraycopy(getString(DISCOUNT_NAME, 39), 0, result, index, 39);
        System.arraycopy(getDataAsByteArray(percent, U64), 0, result, index += 39, U64);
        System.arraycopy(getDataAsByteArray(summ, U64), 0, result, index + U64, U64);
        result[SECTION_DISCOUNT_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] createCashInOutSection(boolean cashIn, long summ) throws UnsupportedEncodingException {
        byte[] result = new byte[SECTION_CASHINOUT_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = 0x0C;
        System.arraycopy(getString(cashIn ? CASH_IN : CASH_OUT , 45), 0, result, index, 45);
        System.arraycopy(getDataAsByteArray(summ, U64), 0, result, index + 45, U64);
        result[SECTION_CASHINOUT_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] createPaymentSection(String typeName, long summ) throws UnsupportedEncodingException {
        byte[] result = new byte[SECTION_PAYMENT_SIZE];
        int index = 0;
        result[index++] = 0x01;
        result[index++] = 0x08;
        System.arraycopy(new byte[45], 0, result, index, 45);
        System.arraycopy(getString(typeName, 19), 0, result, index += 45, 19);
        System.arraycopy(getDataAsByteArray(summ, U64), 0, result, index + 19, U64);
        result[SECTION_PAYMENT_SIZE - 1] = calcCRC(result);
        return result;
    }

    private byte[] getRandomByte(int count) {
        byte[] result = new byte[count];
        new Random().nextBytes(result);
        return result;
    }

    private byte calcCRC(byte[] data) {
        byte result = 0;
        for (int i = 0; i < data.length; i++) {
            result += data[i];
        }
        return (byte) (result + 0x35);
    }

    private byte[] getDate(LocalDateTime date) {
        byte[] result = new byte[7];
        result[0] = (byte) date.getMonthValue();
        result[1] = (byte) date.getDayOfMonth();
        result[2] = (byte) (date.getYear() & 0xFF);
        result[3] = (byte) ((date.getYear() >> 8) & 0xFF);
        result[4] = (byte) date.getHour();
        result[5] = (byte) date.getMinute();
        result[6] = (byte) date.getSecond();
        return result;
    }

    private byte[] getDataAsByteArray(long data, int byteCount) {
        byte[] result = new byte[byteCount];
        int shift = 0;
        for (int i = 0; i < byteCount; i++) {
            result[i] = (byte) ((data >> shift) & 0xFF);
            shift += 8;
        }
        return result;
    }

    private byte[] getString(String data, int count) throws UnsupportedEncodingException {
        byte[] result = new byte[count];
        int stringSubCount = count - 2;
        if (data.length() > stringSubCount) {
            data = data.substring(0, stringSubCount);
        }
        System.arraycopy(data.getBytes(AbstractPirit.PIRIT_CODE_SET), 0, result, 0, data.length());
        result[result.length - 1] = 0x00;
        return result;
    }

    private Goods getGoods(){
        Goods result = new Goods();
        result.setPositionNum(1L);
        result.setItem(GOODS_ITEM);
        result.setName(GOODS_NAME);
        result.setQuant(GOODS_QUANT);
        result.setEndPricePerUnit(DOC_SUMM);
        result.setEndPositionPrice(DOC_SUMM);
        return result;
    }

}
