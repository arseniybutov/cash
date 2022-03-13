package ru.crystals.pos.fiscalprinter.pirit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgent;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author user
 */
@RunWith(MockitoJUnitRunner.class)
public class PiritRBSKNOTest {

    private static final String FIRST_NAME = "Иван";
    private static final String MIDDLE_NAME = "Иванович";
    private static final String LAST_NAME = "Иванов";
    private static final String CASHIER_NAME = FIRST_NAME + " " + MIDDLE_NAME + " " + LAST_NAME;

    private static final long SUPER_CHECK_SPND = 321L;
    private static final long CHECK_SPND = 123L;
    private static final long DOC_NUMBER = 567L;

    @Mock
    private PiritConnector pc;

    @Mock
    private PiritAgent pa;

    @Spy
    @InjectMocks
    private PiritRBSKNO provider;

    @Test
    public void printSKNOAnnulCheckOldVersionTest() throws FiscalPrinterException {
        DataPacket expectedRequest = prepareCancelCheckExpectedRequest();
        mockFirmware(ExtendedCommand.GET_INFO_FW_ID, 265L);

        verifyAnnulCheck(expectedRequest);
    }

    @Test
    public void printSKNOAnnulCheckTest() throws FiscalPrinterException {
        DataPacket expectedRequest = prepareCancelCheckExpectedRequest();
        expectedRequest.putDoubleValue(0.0);
        expectedRequest.putDoubleValue(0.0);
        expectedRequest.putLongValue(DOC_NUMBER);
        mockFirmware(ExtendedCommand.GET_INFO_FW_ID, 266L);
        mockFirmware(ExtendedCommand.GET_INFO_FW_TYPE, 35L);

        verifyAnnulCheck(expectedRequest);
    }

    private void verifyAnnulCheck(DataPacket expectedRequest) throws FiscalPrinterException {
        doReturn(CHECK_SPND).when(provider).getLastKpk();
        when(pa.getCashierName(any(Cashier.class))).thenReturn(CASHIER_NAME);
        when(pc.sendRequest(eq(PiritCommand.ANNUL_DOCUMENT), any(), eq(false))).thenReturn(null);

        provider.printSKNOAnnulCheck(getCheck());

        verify(pc).sendRequest(eq(PiritCommand.ANNUL_DOCUMENT), eq(expectedRequest), eq(false));
    }

    private DataPacket prepareCancelCheckExpectedRequest() {
        DataPacket dp = new DataPacket();
        dp.putLongValue(SUPER_CHECK_SPND);
        double[] payments = new double[16];
        payments[1] = 400.46;
        for (double p : payments) {
            dp.putDoubleValue(p);
        }
        dp.putStringValue(CASHIER_NAME);
        return dp;
    }

    private void mockFirmware(ExtendedCommand extendedCommand, long value) throws FiscalPrinterException {
        DataPacket resp = new DataPacket();
        resp.putStringValue("");
        resp.putLongValue(value);
        when(pc.sendRequest(eq(extendedCommand))).thenReturn(resp);
    }

    @Test
    public void printFiscalCopyShouldUseSpecialCommandTest() throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putLongValue(CHECK_SPND);

        Check fiscalCopy = new Check();
        fiscalCopy.setFiscalDocId(CHECK_SPND);
        fiscalCopy.setFiscalCopy(true);
        fiscalCopy.setCopy(true);

        provider.printCheckByTemplate(Collections.emptyList(), fiscalCopy);

        verify(pc).sendRequest(eq(PiritCommand.PRINT_COPY_DOCUMENT), eq(dp));
    }

    @Test
    public void printRegularCopyShouldNotUseSpecialCommandTest() throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putLongValue(CHECK_SPND);

        Check fiscalCopy = new Check();
        fiscalCopy.setFiscalDocId(CHECK_SPND);
        fiscalCopy.setFiscalCopy(false);
        fiscalCopy.setCopy(true);

        provider.printCheckByTemplate(Collections.emptyList(), fiscalCopy);

        verify(pc, never()).sendRequest(eq(PiritCommand.PRINT_COPY_DOCUMENT), any());
    }

    private Check getCheck() {
        Check superCheck = mock(Check.class);
        doReturn(SUPER_CHECK_SPND).when(superCheck).getFiscalDocId();

        Check result = new Check();
        result.setSuperCheck(superCheck);
        result.setPayments(getPayments());
        result.setCashier(new Cashier(FIRST_NAME, MIDDLE_NAME, LAST_NAME));
        result.setCheckNumber(DOC_NUMBER);
        return result;
    }

    private List<Payment> getPayments() {
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Payment pay = new Payment();
            pay.setSum(20023);
            pay.setIndexPayment(1);
            payments.add(pay);
        }
        return payments;
    }
}
