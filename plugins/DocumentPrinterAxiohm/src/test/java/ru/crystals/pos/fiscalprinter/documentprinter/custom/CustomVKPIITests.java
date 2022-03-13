package ru.crystals.pos.fiscalprinter.documentprinter.custom;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.fiscalprinter.Connector;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.EscPosPrinterConfig;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.utils.Timer;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static org.testng.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CustomVKPIITests {

    @InjectMocks
    CustomVKPII customVKPII = new CustomVKPII();
    @Mock
    Connector connector;
    @Mock
    Timer clearOutPutTimer;

    private final EscPosPrinterConfig config = new EscPosPrinterConfig();

    @Before
    public void beforeCustomVKPIITest() {
        config.setUseUsb(true);
        customVKPII.setConfig(config);
        setInternalState(customVKPII, "connector", connector);
    }

    @Test
    public void testGetMaxCharRow() {
        assertEquals(customVKPII.getMaxCharRow(Font.DOUBLEHEIGHT), 42);
        assertEquals(customVKPII.getMaxCharRow(Font.NORMAL), 42);
        assertEquals(customVKPII.getMaxCharRow(Font.UNDERLINE), 42);
        assertEquals(customVKPII.getMaxCharRow(Font.DOUBLEWIDTH), 20);
        assertEquals(customVKPII.getMaxCharRow(Font.SMALL), 54);
    }

    @Test
    public void testSkipAndCut() throws FiscalPrinterException {
        //when
        customVKPII.skipAndCut();
        //then
        InOrder connectorInOrder = inOrder(connector);
        connectorInOrder.verify(connector).sendData(new byte[]{0x1B, 0x69});
        connectorInOrder.verify(connector).sendData(new byte[]{0x1D, 0x65, 0x3, 0xC});
        verify(connector, times(2)).sendData(any(byte[].class));
    }

    @Test
    public void testOpenDocumentTimeOutNotExpired() throws FiscalPrinterException, InterruptedException {
        //given
        doReturn(new byte[]{8}).when(connector).readData(1);
        doReturn(true).when(clearOutPutTimer).isNotExpired();
        //when
        AtomicReference<Exception> exception = new AtomicReference<>();
        Thread openThread = new Thread(() -> {
            try {
                customVKPII.openDocument();
            } catch (FiscalPrinterException e) {
                exception.set(e);
            }
        });
        openThread.start();
        //then
        verify(connector, timeout(500).atLeast(3)).sendData(new byte[]{0x1D, 0x65, 0x6});
        verify(connector, timeout(200).atLeast(3)).readData(1);
        doReturn(new byte[]{0}).when(connector).readData(1);
        openThread.join(500);
        Assert.assertFalse(openThread.isAlive());
        Assert.assertNull(exception.get());
        verify(connector, never()).sendData(new byte[]{0x1D, 0x65, 0x2});
    }

    @Test
    public void testOpenDocumentTimeOutExpired() throws FiscalPrinterException, InterruptedException {
        //given
        doReturn(new byte[]{8}).when(connector).readData(1);
        doReturn(true).when(clearOutPutTimer).isNotExpired();
        //when
        AtomicReference<Exception> exception = new AtomicReference<>();
        Thread openThread = new Thread(() -> {
            try {
                customVKPII.openDocument();
            } catch (FiscalPrinterException e) {
                exception.set(e);
            }
        });
        openThread.start();
        //then
        verify(connector, timeout(500).atLeast(3)).sendData(new byte[]{0x1D, 0x65, 0x6});
        verify(connector, timeout(200).atLeast(3)).readData(1);
        doReturn(false).when(clearOutPutTimer).isNotExpired();
        openThread.join(500);
        Assert.assertFalse(openThread.isAlive());
        Assert.assertNull(exception.get());
        verify(connector).sendData(new byte[]{0x1D, 0x65, 0x2});
    }

    @Test
    public void testOpenDocumentClear() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0}).when(connector).readData(1);
        //when
        customVKPII.openDocument();
        //then
        verify(connector, times(2)).sendData(new byte[]{0x1D, 0x65, 0x6});
        verify(connector, times(2)).readData(1);
        verify(connector, never()).sendData(new byte[]{0x1D, 0x65, 0x2});
        verify(clearOutPutTimer, never()).isNotExpired();
        verify(connector, times(2)).sendData(any(byte[].class));
    }

    @Test
    public void testCloseDocument() throws FiscalPrinterException {
        //given
        mockNormalStatusResponse();

        long timeout = 121;
        config.setClearOutPutTimeOut(timeout);
        //when
        customVKPII.closeDocument();
        //then
        verify(clearOutPutTimer).restart(timeout);
    }

    @Test
    public void testGetDeviceName() {
        Assert.assertEquals("Custom VKP80II", customVKPII.getDeviceName());
    }

    @Test
    public void testGetStatusNormal() throws FiscalPrinterException {
        //given
        mockNormalStatusResponse();
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.NORMAL, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(0));
        Assert.assertEquals(0, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    private void mockNormalStatusResponse() throws FiscalPrinterException {
        doReturn(new byte[]{0x10, 0x0F, 0, 0, 0, 0}).when(connector).readData(6);
    }

    @Test
    public void testGetStatusCoverOpen() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0x10, 0x0F, 0, 3, 0, 0}).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.OPEN_COVER, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(1));
        Assert.assertEquals(ResBundleDocPrinterAxiohm.getString("PRINTER_COVER_OPENED"), status.getDescriptions().get(0));
        Assert.assertEquals(844424930131968L, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testGetStatusPaperEnd() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0x10, 0x0F, 1, 0, 0, 0}).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.END_PAPER, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(1));
        Assert.assertEquals(ResBundleDocPrinterAxiohm.getString("PRINTER_END_OF_PAPER"), status.getDescriptions().get(0));
        Assert.assertEquals(72057594037927936L, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testGetStatusFatalError() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0x10, 0x0F, 0, 0, 1, 0}).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.FATAL, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(1));
        Assert.assertEquals(ResBundleDocPrinterAxiohm.getString("PRINTER_FATAL_ERROR"), status.getDescriptions().get(0));
        Assert.assertEquals(1099511627776L, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testGetStatusFatalError2() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0x10, 0x0F, 0, 0, 0, 1}).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.FATAL, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(1));
        Assert.assertEquals(ResBundleDocPrinterAxiohm.getString("PRINTER_FATAL_ERROR"), status.getDescriptions().get(0));
        Assert.assertEquals(4294967296L, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testGetStatusReadError() throws FiscalPrinterException {
        //given
        doThrow(new FiscalPrinterException()).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.NORMAL, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(0));
        Assert.assertEquals(0, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testGetStatusWrongControlBytes() throws FiscalPrinterException {
        //given
        doReturn(new byte[]{0x11, 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}).when(connector).readData(6);
        //when
        StatusFP status = customVKPII.getStatus();
        //then
        Assert.assertEquals(StatusFP.Status.NORMAL, status.getStatus());
        assertThat(status.getDescriptions(), Matchers.hasSize(0));
        Assert.assertEquals(0, status.getLongStatus());
        InOrder conOrder = inOrder(connector);
        conOrder.verify(connector).sendData(new byte[]{0x10, 0x4, 20});
        conOrder.verify(connector).readData(6);
    }

    @Test
    public void testPrintQRData() throws FiscalPrinterException {
        //given
        byte[] cmd = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x31};
        //when
        customVKPII.printQRCodeCommand();
        //then
        Mockito.verify(connector).sendData(Mockito.any(byte[].class));
        Mockito.verify(connector).sendData(cmd);
    }

    @Test
    public void testLoadQRData() throws FiscalPrinterException {
        //given
        byte[] cmd = {0x1D, 0x28, 0x6B, 0x06, 0x00, 0x31, 0x50, 0x31, 0x31, 0x32, 0x33};
        //when
        customVKPII.loadQRData("123");
        //then
        Mockito.verify(connector).sendData(Mockito.any(byte[].class));
        Mockito.verify(connector).sendData(cmd);
    }

    @Test
    public void testLoadQRDataBigLength() throws FiscalPrinterException {
        //given
        String qr = RandomStringUtils.random(600);
        byte[] cmd = {0x1D, 0x28, 0x6B, 91, 0x02, 0x31, 0x50, 0x31};
        cmd = ArrayUtils.addAll(cmd, qr.getBytes());
        //when
        customVKPII.loadQRData(qr);
        //then
        Mockito.verify(connector).sendData(Mockito.any(byte[].class));
        Mockito.verify(connector).sendData(cmd);
    }
}
