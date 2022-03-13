package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.verification.VerificationModeFactory;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.mstar.core.MstarUtils;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

public class MstarConfigTest {
    private static final int MAX_PAYMENT_COUNT = 16;
    private static final int MAX_REQUISITES_COUNT = 4;
    private static final int MAX_STRING_LENGTH = 40;
    MstarConnector pc;
    MstarConfig config;

    @Before
    public void setUp() {
        pc = mock(MstarConnector.class);
        config = new MstarConfig();
        config.setConnector(pc);
    }

    @Test
    public void verifyGetCashNumberReturnsCorrectResult() throws FiscalPrinterException {
        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.CASH_NUMBER, 1234L);

        long cashNumber = config.getCashNumber();

        assertEquals(1234L, cashNumber);
        verifyGetValue(dataPacket);
    }

    @Test
    public void verifyIsCashDrawerMoneyControlReturnsCorrectResult() throws Exception {

        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.CASH_DRAWER_MONEY_CONTROL, true);

        boolean isCashDrawerMoneyControl = config.isCashDrawerMoneyControl();

        assertEquals(false, isCashDrawerMoneyControl);
        verifyGetValue(dataPacket);
    }

    @Test
    public void verifySetPrintLogoSetValueIfItIsDifferent() throws Exception {
        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.PRINT_LOGO, false);

        config.setPrintLogo(true);

        verifyGetValue(dataPacket);
        verifySetValue(MstarConfigParameter.PRINT_LOGO, true);
    }

    @Test
    public void verifySetPrintLogoDoesNotSetValueIfItIsEqual() throws Exception {

        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.PRINT_LOGO, true);

        config.setPrintLogo(true);

        verifyGetValue(dataPacket);
        verifyNotSetValue(MstarConfigParameter.PRINT_LOGO, true);
    }

    @Test
    public void verifySetAutoWithdrawalSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.AUTO_WITHDRAWAL, false);

        config.setAutoWithdrawal(true);

        verifyGetValue(dataPacket);
        verifySetValue(MstarConfigParameter.AUTO_WITHDRAWAL, true);
    }

    @Test
    public void verifySetOpenCashDrawerByCashSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.CASH_DRAWER_OPEN_BY, true);

        config.setOpenCashDrawerByCash(true);

        verifyGetValue(dataPacket);
        verifySetValue(MstarConfigParameter.CASH_DRAWER_OPEN_BY, false);
    }

    @Test
    public void verifySetUseCashDrawerMoneyControlSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(MstarConfigParameter.CASH_DRAWER_MONEY_CONTROL, true);

        config.setUseCashDrawerMoneyControl(true);

        verifyGetValue(dataPacket);
        verifySetValue(MstarConfigParameter.CASH_DRAWER_MONEY_CONTROL, false);
    }

    @Test
    public void verifyGetPaymentsReturnsCorrectResults() throws Exception {
        List<PaymentType> expectedPayments = Arrays.asList(
                new PaymentType(0, "Наличные"),
                new PaymentType(1, "Банковская карта"),
                new PaymentType(3, "Бонусная карта"));

        when(pc.sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap(expectedPayments.get(0).getName()))
                .thenReturn(wrap(expectedPayments.get(1).getName()))
                .thenReturn(wrap(""))
                .thenReturn(wrap(expectedPayments.get(2).getName()))
                .thenReturn(wrap(""));

        List<PaymentType> actualPayments = config.getPayments();

        assertEquals(expectedPayments, actualPayments);

        for (int i = 0; i < MAX_PAYMENT_COUNT; i++) {
            verifySendPacketGetTable(MstarConfigParameter.PAYMENT_NAMES, i);
        }
    }

    @Test
    public void verifyGetRequisitesReturnsCorrectResults() throws Exception {
        List<String> expectedRequisites = Arrays.asList(
                "Кристалл Сервис",
                "ул. Профессора Попова");

        when(pc.sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap(expectedRequisites.get(0)))
                .thenReturn(wrap(expectedRequisites.get(1)))
                .thenReturn(wrap(""));

        List<String> actualRequisites = config.getRequisites();

        assertEquals(expectedRequisites, actualRequisites);

        for (int i = 0; i < MAX_REQUISITES_COUNT; i++) {
            verifySendPacketGetTable(MstarConfigParameter.REQUISITES, i);
        }
    }

    @Test
    public void verifySetRequisitesSetCorrectValues() throws Exception {
        Map<RequisiteType, List<String>> expectedRequisites = new LinkedHashMap<>();
        expectedRequisites.put(RequisiteType.SHOP_NAME, Arrays.asList(StringUtils.center("Кристалл Сервис", MAX_STRING_LENGTH)));
        expectedRequisites.put(RequisiteType.SHOP_ADDRESS, Arrays.asList("ул. Профессора Попова c длинным названием больше 40 символов"));
        config.setRequisites(expectedRequisites);

        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE,
                getDataBufferForSetting(MstarConfigParameter.REQUISITES, 0, expectedRequisites.get(RequisiteType.SHOP_NAME).get(0)));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.REQUISITES, 1, ""));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.REQUISITES, 2,
                StringUtils.left(expectedRequisites.get(RequisiteType.SHOP_ADDRESS).get(0), MAX_STRING_LENGTH)));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.REQUISITES, 3, ""));
    }

    @Test
    public void verifyGetTaxesReturnsCorrectResults() throws Exception {
        ValueAddedTaxCollection expectedTaxes = new ValueAddedTaxCollection();
        expectedTaxes.addTax(new ValueAddedTax(0, 18.0f, "НДС 18%"));
        expectedTaxes.addTax(new ValueAddedTax(1, 10.0f, "НДС 10%"));
        expectedTaxes.addTax(new ValueAddedTax(2, -18.0f, "НДС 18/118"));
        expectedTaxes.addTax(new ValueAddedTax(3, -10.0f, "НДС 10/110"));
        expectedTaxes.addTax(new ValueAddedTax(4, 0.0f, "НДС 0%"));
        expectedTaxes.addTax(new ValueAddedTax(5, -1.0f, ""));

        when(pc.sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap("НДС 18%"))
                .thenReturn(wrap("1800"))
                .thenReturn(wrap("НДС 10%"))
                .thenReturn(wrap("1000"))
                .thenReturn(wrap("НДС 18/118"))
                .thenReturn(wrap("1800"))
                .thenReturn(wrap("НДС 10/110"))
                .thenReturn(wrap("1000"))
                .thenReturn(wrap("НДС 0%"))
                .thenReturn(wrap("000"))
                .thenReturn(wrap(""))
                .thenReturn(wrap("000"));

        ValueAddedTaxCollection actualTaxes = config.getTaxes();

        assertEquals(expectedTaxes, actualTaxes);

        for (int i = 0; i < expectedTaxes.size(); i++) {
            verifySendPacketGetTable(MstarConfigParameter.TAX_NAMES, i);
            verifySendPacketGetTable(MstarConfigParameter.TAX_PERCENTS, i);
        }
    }

    @Test
    public void verifySetTaxesSetCorrectValues() throws Exception {
        ValueAddedTaxCollection expectedTaxes = new ValueAddedTaxCollection();
        expectedTaxes.addTax(new ValueAddedTax(0, 18.0f, "НДС"));
        expectedTaxes.addTax(new ValueAddedTax(1, 10.0f, "НДС2"));

        config.setTaxes(expectedTaxes);

        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_NAMES, 0, "НДС"));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_PERCENTS, 0, "1800"));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_NAMES, 1, "НДС2"));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_PERCENTS, 1, "1000"));

        for (int i = 2; i < expectedTaxes.size(); i++) {
            verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_NAMES, i, ""));
            verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(MstarConfigParameter.TAX_PERCENTS, i, ""));
        }
    }

    private void verifyGetValue(DataPacket dataPacket) throws FiscalPrinterException {
        verify(pc).sendRequest(MstarCommand.GET_CONFIGURATION_TABLE, dataPacket);
    }

    private DataPacket setGetValueResponse(MstarConfigParameter param, long responseValue) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putLongValue((long) param.getTableNumber());
        dp.putLongValue((long) param.getIndex());

        DataPacket result = new DataPacket();
        result.putLongValue(responseValue);
        when(pc.sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.eq(dp))).thenReturn(result);
        return dp;
    }

    private DataPacket setGetValueResponse(MstarConfigParameter param, boolean responseValue) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putLongValue((long) param.getTableNumber());
        dp.putLongValue((long) param.getIndex());

        DataPacket result = new DataPacket();
        result.putLongValue(MstarUtils.setBit(0L, param.getBitNum(), responseValue));
        when(pc.sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.eq(dp))).thenReturn(result);
        return dp;
    }


    private void verifyNotSetValue(MstarConfigParameter param, boolean value) throws FiscalPrinterException {
        DataPacket dpSet = new DataPacket();
        dpSet.putLongValue((long) param.getTableNumber());
        dpSet.putLongValue((long) param.getIndex());
        dpSet.putLongValue(MstarUtils.setBit(0L, param.getBitNum(), value));
        verify(pc, VerificationModeFactory.atLeast(0)).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, dpSet);
    }

    private void verifySetValue(MstarConfigParameter param, boolean value) throws FiscalPrinterException {
        DataPacket dpSet = new DataPacket();
        dpSet.putLongValue((long) param.getTableNumber());
        dpSet.putLongValue((long) param.getIndex());
        dpSet.putLongValue(MstarUtils.setBit(0L, param.getBitNum(), value));
        verify(pc).sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, dpSet);
    }

    private DataPacket wrap(String value) {
        DataPacket dp = new DataPacket();
        dp.putStringValue(value);
        return dp;
    }

    private void verifySendPacketGetTable(MstarConfigParameter parameter, int index) throws FiscalPrinterException {
        verify(pc).sendRequest(Matchers.eq(MstarCommand.GET_CONFIGURATION_TABLE), Matchers.eq(getDataBuffer(parameter, index)));
    }

    private DataPacket getDataBuffer(MstarConfigParameter param, long index) {
        DataPacket dp = new DataPacket();
        dp.putLongValue((long) param.getTableNumber());
        dp.putLongValue(index);
        return dp;
    }

    private DataPacket getDataBufferForSetting(MstarConfigParameter parameter, int index, String value) {
        DataPacket dp = new DataPacket();
        dp.putLongValue((long) parameter.getTableNumber());
        dp.putLongValue((long) index);
        dp.putStringValue(value);
        return dp;
    }
}
