package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.verification.VerificationModeFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.utils.ByteUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PiritConfigTest {
    private static final int MAX_PAYMENT_COUNT = 16;
    private static final int MAX_REQUISITES_COUNT = 4;
    private static final int MAX_STRING_LENGTH = 40;
    PiritConnector pc;
    PiritConfig config;

    @Before
    public void setUp() {
        pc = mock(PiritConnector.class);
        config = new PiritConfig();
        config.setConnector(pc);
    }

    @Test
    public void verifyGetCashNumberReturnsCorrectResult() throws FiscalPrinterException {
        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.CASH_NUMBER, 1234);

        int cashNumber = config.getCashNumber();

        assertEquals(1234, cashNumber);
        verifyGetValue(dataPacket);
    }

    @Test
    public void verifyIsCashDrawerMoneyControlReturnsCorrectResult() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.CASH_DRAWER_MONEY_CONTROL, true);

        boolean isCashDrawerMoneyControl = config.isCashDrawerMoneyControl();

        assertFalse(isCashDrawerMoneyControl);
        verifyGetValue(dataPacket);
    }

    @Test
    public void verifySetPrintLogoSetValueIfItIsDifferent() throws Exception {
        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.PRINT_LOGO, false);

        config.setPrintLogo(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.PRINT_LOGO, true);
    }

    @Test
    public void verifySetPrintLogoDoesNotSetValueIfItIsEqual() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.PRINT_LOGO, true);

        config.setPrintLogo(true);

        verifyGetValue(dataPacket);
        verifyNotSetValue(PiritConfigParameter.PRINT_LOGO, true);
    }

    @Test
    public void verifySetAutoWithdrawalSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.AUTO_WITHDRAWAL, false);

        config.setAutoWithdrawal(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.AUTO_WITHDRAWAL, true);
    }

    @Test
    public void verifySetCheckNumerationByCashSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.CHECK_NUMERATION_BY_EXTERNAL_APP, false);

        config.setCheckNumerationByCash(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.CHECK_NUMERATION_BY_EXTERNAL_APP, true);
    }

    @Test
    public void verifySetOpenCashDrawerByCashSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.CASH_DRAWER_OPEN_BY, true);

        config.setOpenCashDrawerByCash(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.CASH_DRAWER_OPEN_BY, false);
    }

    @Test
    public void verifySetPrintVerticalBarsOnServiceDocSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.PRINT_VERTICAL_BARS_ON_SERVICE_DOC, false);

        config.setPrintVerticalBarsOnServiceDoc(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.PRINT_VERTICAL_BARS_ON_SERVICE_DOC, true);
    }

    @Test
    public void verifySetTakeIntoAccountDocumentsCancelledOnRestartSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.TAKE_INTO_ACCOUNT_DOCUMENTS_CANCELED_ON_RESTART, true);

        config.setTakeIntoAccountDocumentsCancelledOnRestart(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.TAKE_INTO_ACCOUNT_DOCUMENTS_CANCELED_ON_RESTART, false);
    }

    @Test
    public void verifySetUseSmallerLineHeightPrintModeSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.USE_SMALLER_LINE_HEIGHT_PRINT_MODE, false);

        config.setUseSmallerLineHeightPrintMode(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.USE_SMALLER_LINE_HEIGHT_PRINT_MODE, true);
    }

    @Test
    public void verifySetUseCashDrawerMoneyControlSetValue() throws Exception {

        DataPacket dataPacket = setGetValueResponse(PiritConfigParameter.CASH_DRAWER_MONEY_CONTROL, true);

        config.setUseCashDrawerMoneyControl(true);

        verifyGetValue(dataPacket);
        verifySetValue(PiritConfigParameter.CASH_DRAWER_MONEY_CONTROL, false);
    }

    @Test
    public void verifyGetPaymentsReturnsCorrectResults() throws Exception {
        List<PaymentType> expectedPayments = Arrays.asList(
                new PaymentType(0, "Наличные"),
                new PaymentType(1, "Банковская карта"),
                new PaymentType(3, "Бонусная карта"));

        when(pc.sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap(expectedPayments.get(0).getName()))
                .thenReturn(wrap(expectedPayments.get(1).getName()))
                .thenReturn(wrap(""))
                .thenReturn(wrap(expectedPayments.get(2).getName()))
                .thenReturn(wrap(""));

        List<PaymentType> actualPayments = config.getPayments();

        assertEquals(expectedPayments, actualPayments);

        for (int i = 0; i < MAX_PAYMENT_COUNT; i++) {
            verifySendPacketGetTable(PiritConfigParameter.PAYMENT_NAMES, i);
        }
    }

    @Test
    public void verifySetPaymentsSetCorrectValues() throws Exception {
        List<PaymentType> expectedPayments = Arrays.asList(
                new PaymentType(0, "Наличные"),
                new PaymentType(1, "Банковская карта"),
                new PaymentType(3, "Бонусная карта"));

        config.setPayments(expectedPayments);

        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE,
                getDataBufferForSetting(PiritConfigParameter.PAYMENT_NAMES, 0, expectedPayments.get(0).getName()));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE,
                getDataBufferForSetting(PiritConfigParameter.PAYMENT_NAMES, 1, expectedPayments.get(1).getName()));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.PAYMENT_NAMES, 2, ""));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE,
                getDataBufferForSetting(PiritConfigParameter.PAYMENT_NAMES, 3, expectedPayments.get(2).getName()));

        for (int i = 4; i < MAX_PAYMENT_COUNT; i++) {
            verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.PAYMENT_NAMES, 2, ""));
        }
    }

    @Test
    public void verifyGetRequisitesReturnsCorrectResults() throws Exception {
        List<String> expectedRequisites = Arrays.asList(
                "Кристалл Сервис",
                "ул. Профессора Попова");

        when(pc.sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap(expectedRequisites.get(0)))
                .thenReturn(wrap(expectedRequisites.get(1)))
                .thenReturn(wrap(""));

        List<String> actualRequisites = config.getRequisites();

        assertEquals(expectedRequisites, actualRequisites);

        for (int i = 0; i < MAX_REQUISITES_COUNT; i++) {
            verifySendPacketGetTable(PiritConfigParameter.REQUISITES, i);
        }
    }

    @Test
    public void verifySetRequisitesSetCorrectValues() throws Exception {
        Map<RequisiteType, List<String>> expectedRequisites = new LinkedHashMap<>();
        expectedRequisites.put(RequisiteType.SHOP_NAME, Collections.singletonList(StringUtils.center("Кристалл Сервис", MAX_STRING_LENGTH)));
        expectedRequisites.put(RequisiteType.SHOP_ADDRESS, Collections.singletonList("ул. Профессора Попова c длинным названием больше 40 символов"));
        config.setRequisites(expectedRequisites);

        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE,
                getDataBufferForSetting(PiritConfigParameter.REQUISITES, 0, expectedRequisites.get(RequisiteType.SHOP_NAME).get(0)));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.REQUISITES, 1, ""));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.REQUISITES, 2,
                StringUtils.left(expectedRequisites.get(RequisiteType.SHOP_ADDRESS).get(0), MAX_STRING_LENGTH)));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.REQUISITES, 3, ""));
    }

    @Test
    public void verifyGetTaxesReturnsCorrectResults() throws Exception {
        ValueAddedTaxCollection expectedTaxes = new ValueAddedTaxCollection();
        expectedTaxes.addTax(new ValueAddedTax(0, 18.0f, "НДС 18%"));
        expectedTaxes.addTax(new ValueAddedTax(1, 10.0f, "НДС 10%"));
        expectedTaxes.addTax(new ValueAddedTax(2, 0.0f, "НДС 0%"));
        expectedTaxes.addTax(new ValueAddedTax(3, -1.0f, ""));
        expectedTaxes.addTax(new ValueAddedTax(4, -18.0f, "НДС 18/118"));
        expectedTaxes.addTax(new ValueAddedTax(5, -10.0f, "НДС 10/110"));

        when(pc.sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.any(DataPacket.class)))
                .thenReturn(wrap("НДС 18%"))
                .thenReturn(wrap("18.0"))
                .thenReturn(wrap("НДС 10%"))
                .thenReturn(wrap("10.0"))
                .thenReturn(wrap("НДС 0%"))
                .thenReturn(wrap("0"))
                .thenReturn(wrap(""))
                .thenReturn(wrap("0"))
                .thenReturn(wrap("НДС 18/118"))
                .thenReturn(wrap("18.0"))
                .thenReturn(wrap("НДС 10/110"))
                .thenReturn(wrap("10.0"));

        ValueAddedTaxCollection actualTaxes = config.getTaxes();

        assertEquals(expectedTaxes, actualTaxes);

        for (int i = 0; i < expectedTaxes.size(); i++) {
            verifySendPacketGetTable(PiritConfigParameter.TAX_NAMES, i);
            verifySendPacketGetTable(PiritConfigParameter.TAX_PERCENTS, i);
        }
    }

    @Test
    public void verifySetTaxesSetCorrectValues() throws Exception {
        ValueAddedTaxCollection expectedTaxes = new ValueAddedTaxCollection();
        expectedTaxes.addTax(new ValueAddedTax(0, 18.0f, "НДС"));
        expectedTaxes.addTax(new ValueAddedTax(1, 10.0f, "НДС2"));

        config.setTaxes(expectedTaxes);

        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_NAMES, 0, "НДС"));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_PERCENTS, 0, "18.0"));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_NAMES, 1, "НДС2"));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_PERCENTS, 1, "10.0"));

        for (int i = 2; i < expectedTaxes.size(); i++) {
            verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_NAMES, i, ""));
            verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, getDataBufferForSetting(PiritConfigParameter.TAX_PERCENTS, i, ""));
        }
    }

    private void verifyGetValue(DataPacket dataPacket) throws FiscalPrinterException {
        verify(pc).sendRequest(PiritCommand.GET_CONFIGURATION_TABLE, dataPacket);
    }

    private DataPacket setGetValueResponse(PiritConfigParameter param, long responseValue) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putIntValue(param.getTableNumber());
        dp.putIntValue(param.getIndex());

        DataPacket result = new DataPacket();
        result.putLongValue(responseValue);
        when(pc.sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.eq(dp))).thenReturn(result);
        return dp;
    }

    private DataPacket setGetValueResponse(PiritConfigParameter param, boolean responseValue) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putIntValue(param.getTableNumber());
        dp.putIntValue(param.getIndex());

        DataPacket result = new DataPacket();
        result.putIntValue(ByteUtils.setBit(0, param.getBitNum(), responseValue));
        when(pc.sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.eq(dp))).thenReturn(result);
        return dp;
    }


    private void verifyNotSetValue(PiritConfigParameter param, boolean value) throws FiscalPrinterException {
        DataPacket dpSet = new DataPacket();
        dpSet.putIntValue(param.getTableNumber());
        dpSet.putIntValue(param.getIndex());
        dpSet.putIntValue(ByteUtils.setBit(0, param.getBitNum(), value));
        verify(pc, VerificationModeFactory.atLeast(0)).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, dpSet);
    }

    private void verifySetValue(PiritConfigParameter param, boolean value) throws FiscalPrinterException {
        DataPacket dpSet = new DataPacket();
        dpSet.putIntValue(param.getTableNumber());
        dpSet.putIntValue(param.getIndex());
        dpSet.putIntValue(ByteUtils.setBit(0, param.getBitNum(), value));
        verify(pc).sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, dpSet);
    }

    private DataPacket wrap(String value) {
        DataPacket dp = new DataPacket();
        dp.putStringValue(value);
        return dp;
    }

    private void verifySendPacketGetTable(PiritConfigParameter parameter, int index) throws FiscalPrinterException {
        verify(pc).sendRequest(Matchers.eq(PiritCommand.GET_CONFIGURATION_TABLE), Matchers.eq(getDataBuffer(parameter, index)));
    }

    private DataPacket getDataBuffer(PiritConfigParameter param, int index) {
        DataPacket dp = new DataPacket();
        dp.putIntValue(param.getTableNumber());
        dp.putIntValue(index);
        return dp;
    }

    private DataPacket getDataBufferForSetting(PiritConfigParameter parameter, int index, String value) {
        DataPacket dp = new DataPacket();
        dp.putIntValue(parameter.getTableNumber());
        dp.putIntValue(index);
        dp.putStringValue(value);
        return dp;
    }
}
