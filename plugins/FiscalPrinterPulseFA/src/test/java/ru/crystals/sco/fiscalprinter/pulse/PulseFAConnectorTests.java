package ru.crystals.sco.fiscalprinter.pulse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PulseFAConnector.class)
public class PulseFAConnectorTests {

    private final static String IP = "128.0.0.1";
    private final static int PORT = 123;

    private PulseFAConnector connector;

    @Mock
    private Socket socket;
    @Mock
    private InputStream is;
    @Mock
    private OutputStream os;
    @Mock
    private InetAddress inetAddress;

    @Before
    public void beforePulseFAConnectorTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(Socket.class).withAnyArguments().thenReturn(socket);
        Mockito.doReturn(is).when(socket).getInputStream();
        Mockito.doReturn(os).when(socket).getOutputStream();
        connector = new PulseFAConnector(IP, PORT);
        PowerMockito.mockStatic(InetAddress.class);
        PowerMockito.when(InetAddress.getByName(IP)).thenReturn(inetAddress);
    }

    @Test
    public void testReconnect() throws Exception {
        connector.reconnect();

        Mockito.verify(socket, Mockito.times(1)).getOutputStream();
        Mockito.verify(socket, Mockito.times(1)).getInputStream();
        PowerMockito.verifyNew(Socket.class, Mockito.times(1)).withArguments(IP, PORT);
        Mockito.doReturn(true).when(socket).isConnected();

        connector.reconnect();

        Mockito.verify(socket, Mockito.times(2)).getOutputStream();
        Mockito.verify(socket, Mockito.times(2)).getInputStream();
        PowerMockito.verifyNew(Socket.class, Mockito.times(2)).withArguments(IP, PORT);
        Mockito.verify(socket, Mockito.times(1)).close();
        Assert.assertTrue("InputStream должен был подмениться", is == getFieldSuper("is", connector));
        Assert.assertTrue("OutputStream должен был подмениться", os == getFieldSuper("os", connector));
    }

    @Test
    public void testClose() throws FiscalPrinterException, IOException {
        connector.reconnect();
        Mockito.verify(is, Mockito.times(0)).close();
        Mockito.verify(os, Mockito.times(0)).close();
        Mockito.verify(socket, Mockito.times(0)).close();

        connector.close();

        Mockito.verify(is, Mockito.times(2)).close();
        Mockito.verify(os, Mockito.times(2)).close();
        Mockito.verify(socket, Mockito.times(1)).close();
    }

    @Test
    public void testIsOnline() throws IOException, FiscalPrinterException {
        mockIsOnline(true, true);
        Assert.assertFalse(connector.isPiritOnline().isOnline());
        connector.reconnect();
        Assert.assertTrue(connector.isPiritOnline().isOnline());
        mockIsOnline(true, false);
        Assert.assertFalse(connector.isPiritOnline().isOnline());
        mockIsOnline(false, true);
        Assert.assertFalse(connector.isPiritOnline().isOnline());
        mockIsOnline(true, true);
        Mockito.doThrow(new IOException()).when(inetAddress).isReachable(1000);
        Assert.assertFalse(connector.isPiritOnline().isOnline());
    }

    private void mockIsOnline(boolean socketOnline, boolean hostReacheble) throws IOException {
        Mockito.doReturn(socketOnline).when(socket).isConnected();
        Mockito.doReturn(hostReacheble).when(inetAddress).isReachable(1000);
    }

    private Object getFieldSuper(String fieldName, Object src) throws IllegalAccessException {
        Field field = null;
        Class clazz = src.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (Exception e) {}
            if (field == null) {
                clazz = clazz.getSuperclass();
            } else {
                break;
            }
        }
        field.setAccessible(true);
        return field.get(src);
    }
}
