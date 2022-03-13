package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.utils.HIDDeviceAdapterObservable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PosiflexPD2600CustomerDisplayServiceImplTest {

    private static final int MAX_CHAR_COUNT_PER_PACKET = 3;
    private static final int ESC = 27;

    @Spy
    private CustomerDisplayPosiflexImpl service = new CustomerDisplayPosiflexImpl();
    @Mock
    private HIDDeviceAdapterObservable portAdapter;
    @Mock
    private HIDManager manager;
    @Mock
    private HIDDevice device;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOpen() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        service.open();
        verify(service).configureDisplay();
        verify(service).setCodeSet();
        verify(service).clearText();
        verify(service).setOverwriteMode();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedToCreateDevice() throws Exception {
        doThrow(IOException.class).when(service).getAdapter();
        service.open();
        verify(service, never()).configureDisplay();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedSetCodeSetthrows() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        doThrow(CustomerDisplayPluginException.class).when(service).setCodeSet();
        service.open();
        verify(service, never()).setOverwriteMode();
        verify(service, never()).clearText();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedSetOverrideMode() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        doThrow(CustomerDisplayPluginException.class).when(service).setOverwriteMode();
        service.open();
        verify(service).setCodeSet();
        verify(service, never()).clearText();
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testOpenFailedClearText() throws Exception {
        doReturn(portAdapter).when(service).getAdapter();
        doThrow(CustomerDisplayPluginException.class).when(service).clearText();
        service.open();
        verify(service).setCodeSet();
        verify(service).setOverwriteMode();
    }

    @Test
    public void testExecuteCommandWithOneArg() throws CustomerDisplayPluginException, IOException {
        service.setAdapter(portAdapter);
        service.setDeviceConnected(true);
        byte[] anyArray;
        service.executeCommand(anyArray = new byte[]{ anyByte() });
        verify(portAdapter).write(anyArray, MAX_CHAR_COUNT_PER_PACKET, new byte[]{ESC});
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testExecuteCommandWithOneArgDeviceIsNotConnected() throws CustomerDisplayPluginException, IOException {
        service.setDeviceConnected(false);
        service.executeCommand(new byte[]{ anyByte() });
        verify(portAdapter,never()).write((byte[]) any());
    }

    @Test()
    public void testExecuteCommandWithOneArgException() throws CustomerDisplayPluginException, IOException {
        service.setAdapter(portAdapter);
        service.setDeviceConnected(true);
        doThrow(IOException.class).when(portAdapter).write(new byte[]{ anyByte() });
        service.executeCommand(new byte[]{ anyByte() });
    }

    @Test
    public void testExecuteCommandWithThreeArg() throws CustomerDisplayPluginException, IOException {
        service.setAdapter(portAdapter);
        service.setDeviceConnected(true);
        service.executeCommand(new byte[]{ anyByte() }, new byte[]{ anyByte() });
        verify(portAdapter).write(new byte[]{ anyByte() }, anyInt(), new byte[]{ anyByte() });
    }

    @Test(expected=CustomerDisplayPluginException.class)
    public void testExecuteCommandWithThreeArgException() throws CustomerDisplayPluginException, IOException {
        service.setAdapter(portAdapter);
        service.setDeviceConnected(true);
        doThrow(IOException.class).when(portAdapter).write(new byte[]{ anyByte() }, anyInt(), new byte[]{ anyByte() });
        service.executeCommand(new byte[]{ anyByte() }, new byte[]{ anyByte() });
    }

    @Test(expected = CustomerDisplayPluginException.class)
    public void testExecuteCommandWithThreeArgAdapterIsNotSet() throws CustomerDisplayPluginException, IOException {
        service.setAdapter(portAdapter);
        service.setDeviceConnected(false);
        service.executeCommand(new byte[]{ anyByte() }, new byte[]{ anyByte() });
        verify(portAdapter, never()).write(new byte[]{ anyByte() }, anyInt(), new byte[]{ anyByte() });
    }

    @Test
    public void testUpdate() throws CustomerDisplayPluginException {
        service.setAdapter(portAdapter);
        service.update(portAdapter,true);
        verify(service).configureDisplay();
    }
    @Test
    public void testUpdateFalse() throws CustomerDisplayPluginException {
        service.setAdapter(portAdapter);
        service.update(portAdapter,false);
        verify(service,never()).configureDisplay();
    }
}
