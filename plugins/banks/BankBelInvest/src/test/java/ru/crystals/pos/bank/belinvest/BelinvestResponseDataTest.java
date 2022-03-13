package ru.crystals.pos.bank.belinvest;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tatarinov Eduard on 23.11.16.
 */
public class BelinvestResponseDataTest extends Assert {

    private List<String> successSale = Arrays.asList(
            "<OperationResultRs>",
            "<ResultCode>0</ResultCode>",
            "<ResultText>ВЫПОЛНЕНО УСПЕШНО</ResultText>",
            "<TerminalId>S0739001</TerminalId>",
            "<ChequeText>",
            "Строка1",
            "</ChequeText>",
            "<ChequeCount>2</ChequeCount>",
            "<OriginalCode>5</OriginalCode>",
            "<CardNo>************3805</CardNo>",
            "<DateTime>2013-03-20T23:41:16</DateTime>",
            "</OperationResultRs>");

    private List<String> successDayLog = Arrays.asList(
            "<OperationResultRs>",
            "<ResultCode>0</ResultCode>",
            "<ResultText>ВЫПОЛНЕНО УСПЕШНО</ResultText>",
            "<TerminalId>S0739001</TerminalId>",
            "<ChequeText>",
            "Строка1",
            "Строка2",
            "Строка3",
            "</ChequeText>",
            "<ChequeCount>1</ChequeCount>",
            "<DateTime>2013-03-20T23:41:16</DateTime>",
            "<Totals>",
            "<SaleAmt>38600</SaleAmt>",
            "<SaleCount>8</SaleCount>",
            "<ReversalAmt>8200</ReversalAmt>",
            "<ReversalCount>3</ReversalCount>",
            "<ReturnAmt>5000</ReturnAmt>",
            "<ReturnCount>1</ReturnCount>",
            "<TotalAmt>25400</TotalAmt>",
            "<TotalCount>12</TotalCount>",
            "</Totals>",
            "</OperationResultRs>");


    private BelinvestResponseData responseData = new BelinvestResponseData();

    @Test
    public void parseTest() {
        BelinvestResponseData brd = responseData.parseResponseFile(successSale);

        assertTrue(brd.isSuccessful());
        assertEquals("0", brd.getResponseCode());
        assertEquals("5", brd.getAuthCode());
        assertEquals(2, (long) brd.getCountSlip());
        assertEquals(2, brd.getSlip().size());
        assertEquals(LocalDateTime.parse("2013-03-20T23:41:16"), brd.getDate());
    }

    @Test
    public void saleResponseWithOtherElementOrder() throws Exception {
        final Path path = Paths.get(this.getClass().getClassLoader().getResource("saleResponse.xml").toURI());
        final List<String> input = Files.readAllLines(path);

        final BelinvestResponseData response = responseData.parseResponseFile(input);

        assertTrue(response.isSuccessful());
        assertEquals("0", response.getResponseCode());
        assertEquals("11", response.getAuthCode());
        assertEquals(2, (long) response.getCountSlip());
        assertEquals("S5000S66", response.getTerminalId());
        assertEquals(2, response.getSlip().size());
        assertEquals(LocalDateTime.parse("2020-09-25T12:13:28"), response.getDate());

        final List<String> expectedSlip = Arrays.asList("             Visa",
                "Магазин \"Пчёлка\" №3",
                "г.Минск, ул.Ленина, №1",
                "тел. 323-45-67",
                "УНП: 111111111",
                "ТЕРМИНАЛ: S5000S66",
                "     КАРТ-ЧЕК N 088/0011",
                "            ОПЛАТА",
                "25.09.2020               12:13",
                "XXXXXXXXXXXX6830",
                "",
                "СУММА                 0.50 BYN",
                "КОД АВТ.: 121330  026912000445",
                "",
                "           КОД: 000",
                "      ВЫПОЛНЕНО УСПЕШНО",
                "",
                "ОПЕРАЦИЯ БЕЗ ПИН-КОДА",
                "",
                "           ___________________",
                "           (ПОДПИСЬ ДЕРЖАТЕЛЯ)",
                ""
        );
        assertEquals(Arrays.asList(expectedSlip, expectedSlip), response.getSlip());
    }

    @Test
    public void parseDayLog() {
        BelinvestResponseData brd = responseData.parseResponseFile(successDayLog);

        assertTrue(brd.isSuccessful());
        assertEquals("0", brd.getResponseCode());
        assertNull(brd.getAuthCode());
        assertEquals(1, (long) brd.getCountSlip());
        assertEquals(1, brd.getSlip().size());
        assertEquals(LocalDateTime.parse("2013-03-20T23:41:16"), brd.getDate());
        //Totals
        assertEquals(25400, (long) brd.getOperationResultRs().getTotals().getTotalAmt());
        assertEquals(12, (long) brd.getOperationResultRs().getTotals().getTotalCount());
    }
}