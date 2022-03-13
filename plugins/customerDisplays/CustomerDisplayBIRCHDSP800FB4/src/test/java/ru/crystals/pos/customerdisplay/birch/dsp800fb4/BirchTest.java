package ru.crystals.pos.customerdisplay.birch.dsp800fb4;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BirchTest {
    @Spy
    private CustomerDisplayBirchPluginImpl service = new CustomerDisplayBirchPluginImpl();
    @Mock
    private SerialPortAdapter portAdapter;
    private static final String testMessage = "Привет!";
    private static final byte[] testMessageBytesCP866 = new byte[]{ -113, -32, -88, -94, -91, -30, 33 };

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOpen() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        service.open();
        verify(service).configureDisplay();
        verify(service).setDisplayOn();
        verify(service).setCharacterSet();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedToCreateDevice() throws Exception {
        doThrow(CustomerDisplayPluginException.class).when(service).getAdapter();
        service.open();
        verify(service, never()).configureDisplay();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedSetCodeSetthrows() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        doThrow(CustomerDisplayPluginException.class).when(service).setCharacterSet();
        service.open();
        verify(service).configureDisplay();
        verify(service).setDisplayOn();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedSetDisplayOnthrows() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        doThrow(CustomerDisplayPluginException.class).when(service).setDisplayOn();
        service.open();
        verify(service).configureDisplay();
        verify(service, never()).setDisplayOn();
    }

    @Test
    public void getPortAdapterPortWasSetted() throws CustomerDisplayPluginException, IOException, PortAdapterException {
        service.setAdapter(portAdapter);
        service.getAdapter();
        verify(portAdapter, never()).openPort();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void getPortAdapter() throws CustomerDisplayPluginException, IOException, PortAdapterException {
        service.getAdapter();
        verify(portAdapter).openPort();
    }

    @Test
    public void testExecuteCommandCP866Encoding() throws IOException, CustomerDisplayPluginException {
        service.setAdapter(portAdapter);
        service.setEncoding("cp866");
        service.executeCommand(testMessage);
        verify(portAdapter).write(testMessageBytesCP866);
    }

    @Test
    public void testDisplayTextAt() throws CustomerDisplayPluginException {
        service.setAdapter(portAdapter);
        service.displayTextAt(0,0,testMessage);
    }
}
