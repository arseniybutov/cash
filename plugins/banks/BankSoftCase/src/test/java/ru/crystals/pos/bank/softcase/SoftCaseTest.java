package ru.crystals.pos.bank.softcase;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.BankTypeEnum;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.softcase.utils.SoftCaseDocumentTemplate;
import ru.crystals.pos.bank.softcase.utils.SoftCaseMessage;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SoftCaseTest {
    private final static String charset = "cp866";
    private final static String templatePathPrefix = "";
    private final static String saleTemplatePath = templatePathPrefix + "templates/softcase-sale.xml";

    @Spy
    private BankSoftCaseServiceImpl service = new BankSoftCaseServiceImpl();
    private List<String> PAYPASS = Collections.singletonList("                                   PayPass");
    private List<String> WITH_PIN = Collections.singletonList("              ОДОБРЕНО С ПИН              ");
    private List<String> ALL = Arrays.asList("Владелец карты               N43/TEST CARD",
            "            Подпись покупателя            ");
    private static final String SALE_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><mess><kkm>1</kkm><type>0200000000</type><amount>10</amount><crc" +
                    ">786d3e58537d3b47b74f3eeb14cc0dc0</crc></mess>";
    private static final String SALE_RESPONSE_WITHOUT_PIN = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131022171200</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329513086715</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD  </applabel>" +
            "<aid>A0000000032010</aid>" +
            "<trancert>8B34D7E69ABDAD63</trancert>" +
            "<pem>052</pem>" +
            "<hash>8BF846DD9AB2979F0A11BE5F23C32327675D5814</hash>" +
            "<cardid>00</cardid>" +
            "<crc>5696882094e66faf724338331d77b974</crc>" +
            "</mess>";
    private static final String SALE_RESPONSE_WITH_PIN = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131022171200</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329513086715</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО С ПИН</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD  </applabel>" +
            "<aid>A0000000032010</aid>" +
            "<trancert>8B34D7E69ABDAD63</trancert>" +
            "<pem>051</pem>" +
            "<hash>8BF846DD9AB2979F0A11BE5F23C32327675D5814</hash>" +
            "<cardid>00</cardid>" +
            "<crc>5696882094e66faf724338331d77b974</crc>" +
            "</mess>";
    private static final String SALE_RESPONSE_WITH_MAGNETIC_STRIP = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131022171200</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329513086715</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО С ПИН</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD  </applabel>" +
            "<aid></aid>" +
            "<trancert>8B34D7E69ABDAD63</trancert>" +
            "<pem>021</pem>" +
            "<hash>8BF846DD9AB2979F0A11BE5F23C32327675D5814</hash>" +
            "<cardid>00</cardid>" +
            "<crc>5696882094e66faf724338331d77b974</crc>" +
            "</mess>";
    private static final String SALE_RESPONSE_WITH_PIN_FAILED = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>912</code>" +
            "<type>0210000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131022171200</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329513086715</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>\u008E\u0092\u008A\u008B\u008E\u008D\u0085\u008D\u008E</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD  </applabel>" +
            "<aid>A0000000032010</aid>" +
            "<trancert>8B34D7E69ABDAD63</trancert>" +
            "<pem>051</pem>" +
            "<hash>8BF846DD9AB2979F0A11BE5F23C32327675D5814</hash>" +
            "<cardid>00</cardid>" +
            "<crc>5696882094e66faf724338331d77b974</crc>" +
            "</mess>";

    private static final String SALE_RESPONSE_PAYPASS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131022120000</tdt>" +
            "<expdt></expdt>" +
            "<rrn>470872520957</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО</resp>" +
            "<cardholder></cardholder>" +
            "<applabel>MASTERCARD  </applabel>" +
            "<aid>A0000000041010</aid>" +
            "<trancert>E2D65E3A54901F3F</trancert>" +
            "<pem>072</pem>" +
            "<hash>3C587049EAF38C7B0A34A953AE7628EF56B1FF6C</hash>" +
            "<cardid>00</cardid>" +
            "<crc>deb9f5343b9534c546244a421a385e7d</crc>" +
            "</mess>";
    private static final String REFUND_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><mess><kkm>1</kkm><type>0200200000</type><amount>10</amount><trace>7</trace><crc" +
                    ">64bef499e05305c3ec02491d16e5d7e8</crc></mess>";
    private static final String REFUND_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210200000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131023113300</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329659580939</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD      </applabel>" +
            "<aid>A0000000041010</aid>" +
            "<trancert>FFC49583DAB3BB5E</trancert>" +
            "<pem>052</pem>" +
            "<hash>3C587049EAF38C7B0A34A953AE7628EF56B1FF6C</hash>" +
            "<cardid>00</cardid>" +
            "<crc>7886c907510ccbcd4fe71e3299e901ab</crc>" +
            "</mess>";
    private static final String REVERSAL_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><mess><kkm>1</kkm><type>0400000000</type><amount>10</amount><trace>7</trace><crc" +
                    ">d07f5f477bd298c2ac5ba1ac17c3e7e7</crc></mess>";
    private static final String REVERSAL_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0410000000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000010</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>000000015</trace>" +
            "<tdt>131023115900</tdt>" +
            "<expdt></expdt>" +
            "<rrn>933320118937</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО С ПИН</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD      </applabel>" +
            "<aid>A0000000041010</aid>" +
            "<trancert>E8B796321B202C21</trancert>" +
            "<pem>051</pem>" +
            "<hash>3C587049EAF38C7B0A34A953AE7628EF56B1FF6C</hash>" +
            "<cardid>00</cardid>" +
            "<crc>900632e13d597d0ddbf32517b56a6d6b</crc>" +
            "</mess>";
    private static final String HALF_REVERSAL_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><mess><kkm>1</kkm><type>0200200000</type><amount>5</amount><trace>7</trace><crc" +
                    ">d4ca584f6b49eebfd2366e58fa33914d</crc></mess>";
    private static final String HALF_REVERSAL_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<mess>" +
            "<code>0</code>" +
            "<type>0210200000</type>" +
            "<card>4627XXXXXXXX0439</card>" +
            "<amount>000000000005</amount>" +
            "<kkm>1</kkm>" +
            "<cardtype>UNKNOWN</cardtype>" +
            "<track3></track3>" +
            "<trace>0000000015</trace>" +
            "<tdt>131023113300</tdt>" +
            "<expdt></expdt>" +
            "<rrn>329659580939</rrn>" +
            "<auth>025761</auth>" +
            "<termid>10000001</termid>" +
            "<resp>ОДОБРЕНО</resp>" +
            "<cardholder>N43/TEST CARD             </cardholder>" +
            "<applabel>MASTERCARD      </applabel>" +
            "<aid>A0000000041010</aid>" +
            "<trancert>FFC49583DAB3BB5E</trancert>" +
            "<pem>052</pem>" +
            "<hash>3C587049EAF38C7B0A34A953AE7628EF56B1FF6C</hash>" +
            "<cardid>00</cardid>" +
            "<crc>7886c907510ccbcd4fe71e3299e901ab</crc>" +
            "</mess>";
    @Mock
    private TCPPortAdapter portAdapter;
    private SoftCaseDocumentTemplate saleTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(getSaleTemplate()).when(service).getTemplate(anyString());
        service.setTerminalID("1");
        service.setUseCashNumberAsTerminalID(false);
        service.setCharset(charset);
    }

    @Test
    public void testStart() throws CashException {
        correctStart();
    }

    @Test
    public void testSalePayPass() throws IOException, CashException {
        correctStart();
        initInputOutput(SALE_REQUEST, SALE_RESPONSE_PAYPASS);
        AuthorizationData result = service.sale(getSaleData(10L));
        validateAuthorizationData(result, PAYPASS, ALL, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО");
    }

    @Test
    public void testSaleWithoutPin() throws IOException, CashException {
        correctStart();
        initInputOutput(SALE_REQUEST, SALE_RESPONSE_WITHOUT_PIN);
        AuthorizationData result = service.sale(getSaleData(10L));
        validateAuthorizationData(result, ALL, PAYPASS, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО");
    }

    @Test
    public void testSaleWithPin() throws IOException, CashException {
        correctStart();
        initInputOutput(SALE_REQUEST, SALE_RESPONSE_WITH_PIN);
        AuthorizationData result = service.sale(getSaleData(10L));
        validateAuthorizationData(result, WITH_PIN, PAYPASS, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО С ПИН");
        validateAuthorizationData(result, WITH_PIN, ALL, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО С ПИН");
    }

    @Test
    public void testSaleWithMagneticStrip() throws IOException, CashException {
        correctStart();
        initInputOutput(SALE_REQUEST, SALE_RESPONSE_WITH_MAGNETIC_STRIP);
        AuthorizationData result = service.sale(getSaleData(10L));
        System.out.println(StringUtils.join(result.getSlips().get(0).toArray(), '\n'));
        validateAuthorizationData(result, WITH_PIN, PAYPASS, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО С ПИН");
        validateAuthorizationData(result, WITH_PIN, ALL, BankOperationType.SALE, SoftCaseMessage.PAY_ANSWER_TYPE, "ОДОБРЕНО С ПИН");

    }

    @Test
    public void testRefund() throws IOException, CashException {
        correctStart();
        initInputOutput(REFUND_REQUEST, REFUND_RESPONSE);
        AuthorizationData result = service.refund(getRefundData());
        validateAuthorizationData(result, ALL, PAYPASS, BankOperationType.REFUND, SoftCaseMessage.REFUND_ANSWER_TYPE, "ОДОБРЕНО");
    }

    @Test
    public void testReversal() throws IOException, CashException {
        correctStart();
        initInputOutput(REVERSAL_REQUEST, REVERSAL_RESPONSE);
        AuthorizationData result = service.reversal(getReversalData());
        validateAuthorizationData(result, WITH_PIN, ALL, BankOperationType.REVERSAL, SoftCaseMessage.REVERSAL_ANSWER_TYPE, "ОДОБРЕНО С ПИН");
        validateAuthorizationData(result, WITH_PIN, PAYPASS, BankOperationType.REVERSAL, SoftCaseMessage.REVERSAL_ANSWER_TYPE, "ОДОБРЕНО С ПИН");
    }

    @Test
    public void testHalfReversal() throws IOException, CashException {
        correctStart();
        initInputOutput(HALF_REVERSAL_REQUEST, HALF_REVERSAL_RESPONSE);
        AuthorizationData result = service.reversal(getHalfReversalData());
        validateAuthorizationData(result, ALL, PAYPASS, BankOperationType.REFUND, SoftCaseMessage.REFUND_ANSWER_TYPE, "ОДОБРЕНО");
    }

    @Test
    public void testFailedSale() throws IOException, CashException {
        boolean isPrintNegative = true;
        service.setPrintNegativeSlip(isPrintNegative);
        correctStart();
        initInputOutput(HALF_REVERSAL_REQUEST, SALE_RESPONSE_WITH_PIN_FAILED);
        AuthorizationData result;
        boolean isCatched = false;
        try {
            result = service.reversal(getHalfReversalData());
        } catch (BankException e) {
            result = e.getAuthorizationData();
            Assert.assertTrue(!result.isStatus());
            Assert.assertEquals(isPrintNegative, result.isPrintNegativeSlip());
            isCatched = true;
        }
        Assert.assertTrue(isCatched);
    }

    private void correctStart() throws CashException {
        service.setPortAdapter(portAdapter);
        service.setTerminalTcpPort(anyInt());
        service.setTerminalIp(anyString());
        Mockito.doNothing().when(service).initSocket();
        service.start();
    }

    private SaleData getSaleData(Long amount) {
        SaleData result = new SaleData();
        result.setAmount(amount);
        return result;
    }

    private ReversalData getReversalData() {
        ReversalData result = new ReversalData();
        result.setAmount(10L);
        result.setOriginalSaleTransactionAmount(10L);
        result.setRefNumber("7");
        return result;
    }

    private ReversalData getHalfReversalData() {
        ReversalData result = new ReversalData();
        result.setAmount(5L);
        result.setOriginalSaleTransactionAmount(10L);
        result.setRefNumber("7");
        return result;
    }

    private RefundData getRefundData() {
        RefundData result = new RefundData();
        result.setAmount(10L);
        result.setRefNumber("7");
        return result;
    }

    private SoftCaseDocumentTemplate getSaleTemplate() throws IOException {

        saleTemplate = SoftCaseDocumentTemplate.getInstance(saleTemplatePath);
        FiscalPrinterPlugin emulator = mock(FiscalPrinterPlugin.class);
        when(emulator.getMaxCharRow()).thenReturn(42);
        when(emulator.getMaxCharRow(any(Font.class), anyInt())).thenReturn(42);
        saleTemplate.setPlugin(emulator);
        return saleTemplate;
    }

    private void validateAuthorizationData(AuthorizationData data, List<String> contains, List<String> doesNotContains,
                                           BankOperationType operationType, String answerType, String statusOperation) {
        System.out.println(StringUtils.join(data.getSlips().get(0).toArray(), '\n'));
        Assert.assertEquals(operationType, data.getOperationType());
        Assert.assertEquals("15", data.getRefNumber());
        Assert.assertEquals("025761", data.getAuthCode());
        Assert.assertEquals(Long.valueOf(answerType), data.getOperationCode());
        Assert.assertEquals("00", data.getResponseCode());
        Assert.assertTrue(data.isStatus());
        Assert.assertEquals(statusOperation, data.getMessage());
        Assert.assertFalse(data.isPrintNegativeSlip());
        Assert.assertFalse(data.isTransactionCancelled());
        Assert.assertEquals(BankTypeEnum.UNKNOWN, data.getCard().getCardOperator());
        Assert.assertEquals("MASTERCARD", data.getCard().getCardType().trim());
        Assert.assertEquals(2, data.getSlips().size());
        Assert.assertTrue(data.getSlips().get(0).containsAll(contains));
        Assert.assertFalse(data.getSlips().get(0).containsAll(doesNotContains));
    }

    private void initInputOutput(String request, String response) throws IOException {
        Mockito.doNothing().when(portAdapter).write(request.getBytes());
        doReturn(response).when(portAdapter).readAll(charset);
    }
}
