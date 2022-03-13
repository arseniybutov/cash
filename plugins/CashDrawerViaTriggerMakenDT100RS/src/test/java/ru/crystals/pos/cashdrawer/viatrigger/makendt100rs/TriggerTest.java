package ru.crystals.pos.cashdrawer.viatrigger.makendt100rs;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ru.crystals.pos.CashException;
import ru.crystals.pos.cashdrawer.CashDrawer.CashDrawerOpenMode;
import ru.crystals.pos.utils.SerialPortAdapter;


public class TriggerTest {
    private static final String OPEN = "OPEN";
    private String port = "COM1";
    @Spy
    private TriggerServiceImpl service = new TriggerServiceImpl();
    @Mock
    private SerialPortAdapter serialPort;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        serialPort.setPort(port);
        service.setPortAdapter(serialPort);
        doReturn(true).when(serialPort).isConnected();
        doNothing().when(service).setPortParameters();
    }

    @Test
    public void testStart() throws Exception {
        service.start();
        verify(serialPort).openPort();
    }

    @Test
    public void testStartIOException() throws Exception {
        doThrow(new IOException()).when(serialPort).openPort();
        service.setSilentErrorHandling(false);
        try {
            service.start();
            fail("Исключение не было выброшено");
        } catch (CashException e) {
        }
        verify(serialPort).openPort();
    }

    @Test
    public void testStartIOExceptionSilentMode() throws Exception {
        doThrow(new IOException()).when(serialPort).openPort();
        service.setSilentErrorHandling(true);
        try {
            service.start();
        } catch (CashException e) {
            fail("Исключение не должно быть выброшено");
        }
        verify(serialPort).openPort();
    }

    @Test
    public void testStop() throws Exception {
        service.start();
        verify(serialPort).openPort();
        service.stop();
        verify(serialPort).close();
    }

    @Test
    public void testOpenDrawer() throws Exception {
        service.start();
        boolean result = service.openDrawer(CashDrawerOpenMode.AUTOMATIC);
        assertTrue(result);
        verify(serialPort).openPort();
        verify(serialPort).write(OPEN.getBytes());
    }

    @Test
    public void testOpenDrawerIOException() throws Exception {
        doThrow(new IOException()).when(serialPort).write(anyString().getBytes());
        service.start();
        boolean result = service.openDrawer(CashDrawerOpenMode.AUTOMATIC);
        assertFalse(result);
    }

    @Test
    public void testOpenDrawerIsNotConnected() throws Exception {
        service.setSilentErrorHandling(true);
        doThrow(new IOException()).when(serialPort).openPort();
        doReturn(false).when(serialPort).isConnected();
        service.start();
        assertFalse(service.openDrawer(CashDrawerOpenMode.AUTOMATIC));
        verify(serialPort, never()).write(OPEN.getBytes());
    }

    @Test
    public void testIsOpenDrawer() throws Exception {
        when(serialPort.isCD()).thenReturn(true);
        service.start();
        assertTrue(service.isOpenDrawer());
        verify(serialPort).isCD();
    }

    @Test
    public void testIsOpenDrawerFalse() throws Exception {
        when(serialPort.isCD()).thenReturn(false);
        service.start();
        assertFalse(service.isOpenDrawer());
        verify(serialPort).isCD();
    }

    @Test
    public void testIsOpenDrawerNull() throws Exception {
        service.setSilentErrorHandling(true);
        doThrow(new IOException()).when(serialPort).openPort();
        doReturn(false).when(serialPort).isConnected();
        service.start();
        assertFalse(service.isOpenDrawer());
        verify(serialPort, never()).isCD();
    }

    @Test
    public void testIsOpenDrawerIOException() throws Exception {
        service.setSilentErrorHandling(true);
        doThrow(new IOException()).when(serialPort).openPort();
        service.start();
        when(serialPort.isCD()).thenReturn(false);
        assertFalse(service.isOpenDrawer());
    }
}
