package ru.crystals.pos.bank.sberbank;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

public class SberbankServiceImplTest {

    private static final Long AMOUNT = 1000L;
    private static final String CARD_TYPE_AUTO = "0";
    private static final String DEFAULT_TRACK2 = "0";
    private static final String WITHOUT_CARD = "QSELECT";
    private static final String SALE = "1";
    private static final String REFUND = "3";
    private static final String REVERSAL = "8";
    private static final String OPERATION_STATUS = "52";
    private static final String FIRST_DEPART = "00001";
    private static final String SECOND_DEPART = "00002";
    private static final String TRACK2 = "123456789";
    private static final String RRN = "987654321";

    private SberbankServiceImpl service = new SberbankServiceImpl();

    @Test
    public void sale() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        SaleData saleData = new SaleData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);
    }

    @Test
    public void saleWithDepart() {
        service.setFirstFiscalPrinterDepartment(FIRST_DEPART);
        service.setSecondFiscalPrinterDepartment(SECOND_DEPART);

        SaleData saleData = new SaleData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, "/d=00001", null), result);

        saleData.setFirstFiscalPrinter(false);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, "/d=00002", null), result);

    }

    @Test
    public void saleWithWrongDepart() {
        service.setFirstFiscalPrinterDepartment(FIRST_DEPART);
        service.setSecondFiscalPrinterDepartment(SECOND_DEPART);

        SaleData saleData = new SaleData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(null);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        saleData.setFirstFiscalPrinter(true);
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(SECOND_DEPART);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        saleData.setFirstFiscalPrinter(true);
        service.setFirstFiscalPrinterDepartment(FIRST_DEPART);
        service.setSecondFiscalPrinterDepartment(null);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        saleData.setFirstFiscalPrinter(false);
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(SECOND_DEPART);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        saleData.setFirstFiscalPrinter(false);
        service.setFirstFiscalPrinterDepartment(FIRST_DEPART);
        service.setSecondFiscalPrinterDepartment(null);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);
    }

    @Test
    public void saleWithCard() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        BankCard bankCard = new BankCard();

        SaleData saleData = new SaleData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(bankCard);
        saleData.setFirstFiscalPrinter(true);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        bankCard.setTrack2(TRACK2);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, "/t;123456789?", null, null), result);
    }

    @Test
    public void refund() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        RefundData saleData = new RefundData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);
    }

    @Test
    public void refundWithRRN() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        RefundData saleData = new RefundData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);
        saleData.setRefNumber(StringUtils.EMPTY);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        saleData.setRefNumber(RRN);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, RRN, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, RRN, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, RRN, null, null), result);
    }

    @Test
    public void reversal() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        ReversalData saleData = new ReversalData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);
    }

    @Test
    public void reversalWithRRN() {
        service.setFirstFiscalPrinterDepartment(null);
        service.setSecondFiscalPrinterDepartment(null);

        ReversalData saleData = new ReversalData();
        saleData.setAmount(AMOUNT);
        saleData.setCard(null);
        saleData.setFirstFiscalPrinter(true);
        saleData.setRefNumber("");

        List<String> result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2,  null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, null, null, null), result);

        saleData.setRefNumber(RRN);

        result = service.prepareExecutableParameters(saleData, BankOperationType.SALE);
        checkResult(Arrays.asList(SALE, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2,  null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REFUND);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, RRN, null, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSAL);
        checkResult(Arrays.asList(REVERSAL, AMOUNT.toString(), CARD_TYPE_AUTO, WITHOUT_CARD, RRN, null), result);

        result = service.prepareExecutableParameters(saleData, BankOperationType.REVERSE_LAST);
        checkResult(Arrays.asList(REFUND, AMOUNT.toString(), CARD_TYPE_AUTO, DEFAULT_TRACK2, RRN, null, null), result);
    }

    /**
     * Тест на проверку присутствия параметра для отмены оплаты по банку
     */
    @Test
    public void cancelByBank() {
        List<String> result = service.prepareExecutableParameters(new SaleData(), BankOperationType.CANCEL_AT_BANK);
        checkResult(Collections.singletonList("8"), result);
    }

    @Test
    public void testPrepareOperationStatusRequest() {
        service.setNeedGenerateRequestId(false);

        List<String> result = service.prepareOperationStatusRequest("123");
        checkResult(Arrays.asList(OPERATION_STATUS, "123", null), result);

        service.setNeedGenerateRequestId(true);

        result = service.prepareOperationStatusRequest("123");
        Assert.assertEquals("Неверное количество параметров!", 3, result.size());
        Assert.assertEquals(OPERATION_STATUS, result.get(0));
        Assert.assertEquals("123", result.get(1));
        Assert.assertTrue(result.get(2).matches("/q=[\\dA-Fa-f]{8}"));
    }

    @Test
    public void testCheckResponseDataAndStatusFailed() {
        String lastRequestId = "c82cd50f";
        String lastRequestIdUpperCase = "c82cd50f".toUpperCase();

        // ничего не пришло - плохо
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(null, false, lastRequestId));
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(null, true, lastRequestId));

        // пришло, но без response code - плохо
        SberbankResponseData responseData = Mockito.spy(new SberbankResponseData());
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, false, lastRequestId));
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, true, lastRequestId));

        // пришло с плохим response code - плохо
        when(responseData.getResponseCode()).thenReturn("2004");
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, false, lastRequestId));
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, true, lastRequestId));

        // нормальный response code, но пустой request ID
        when(responseData.getResponseCode()).thenReturn("0");
        when(responseData.getRequestId()).thenReturn(SberbankServiceImpl.EMPTY_REQUEST_ID);
        // пустой request ID недопустим - плохо
        Assert.assertTrue(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, false, lastRequestId));
        // пустой request ID допустим - ок
        Assert.assertFalse(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, true, lastRequestId));

        // нормальный request ID - ок
        when(responseData.getRequestId()).thenReturn(lastRequestIdUpperCase);
        Assert.assertFalse(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, false, lastRequestId));
        Assert.assertFalse(SberbankServiceImpl.checkResponseDataAndStatusFailed(responseData, true, lastRequestId));
    }

    private void checkResult(List<String> expected, List<String> actual) {
        Assert.assertNotNull("Пустой результат!", actual);
        Assert.assertEquals("Неверное количество параметров!", expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}