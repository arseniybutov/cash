package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.SerialPortAdapterObservable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SerialServiceImplTest {

    @Mock
    private SerialPortAdapter portAdapter;

    private SerialPortAdapterObservable serialPortAdapterObservable;

    @InjectMocks @Spy
    private CustomerDisplayPosiflexSerialImpl service;

    @Test
    public void testSetPortDev() throws Exception {
        String port = "/dev/usbPOSIFLEX0";
        serialPortAdapterObservable = mock(SerialPortAdapterObservable.class);
        service.setSerialPortAdapterObservable(serialPortAdapterObservable);
        doNothing().when(service).createPortAdapterObservable();

        service.setPort(port);

        verify(serialPortAdapterObservable).addObserver(service);
        verify(serialPortAdapterObservable).setSettingsPortID(port);
        verify(portAdapter).setPort(new File(port).getCanonicalPath());
    }

    @Test
    public void testSetPort() throws Exception {
        String port = "COM1";
        serialPortAdapterObservable = mock(SerialPortAdapterObservable.class);
        service.setSerialPortAdapterObservable(serialPortAdapterObservable);

        service.setPort(port);

        verify(service, never()).createPortAdapterObservable();
        verify(serialPortAdapterObservable, never()).addObserver(service);
        verify(serialPortAdapterObservable, never()).setSettingsPortID(port);
        verify(portAdapter).setPort(port);
    }

    @Test
    public void testGetAdapter() throws Exception {
        service.setPort("COM1");

        PortAdapter adapter = service.getAdapter();

        assertEquals(portAdapter, adapter);
    }

    @Test
    public void testGetAdapterDev() throws Exception {
        String port = "/dev/usbPOSIFLEX0";
        service.setPort(port);

        PortAdapter adapter = service.getAdapter();

        assertTrue(adapter instanceof SerialPortAdapterObservable);
    }

    @Test
    public void testExecuteCommandWithOneArg() throws CustomerDisplayPluginException, IOException {
        service.setDeviceConnected(true);
        byte[] anyArray;

        service.executeCommand(anyArray = new byte[]{ anyByte() });

        verify(portAdapter).write(anyArray);
    }
}
