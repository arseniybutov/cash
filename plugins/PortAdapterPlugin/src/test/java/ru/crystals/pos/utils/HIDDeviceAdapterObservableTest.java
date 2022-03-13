package ru.crystals.pos.utils;

import java.io.IOException;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
@RunWith(MockitoJUnitRunner.class)
public class HIDDeviceAdapterObservableTest {
    @Mock
    private HIDDeviceAdapter device;
    @Spy
    private HIDDeviceAdapterObservable adapter = new HIDDeviceAdapterObservable(device);
    private static final byte[] SHORT_MESSAGE = new byte[]{ 0x01 };
    private static final byte[] SHORT_MESSAGE_COMPLETE = new byte[]{ 0x00, 0x01 };
    private static final byte[] LONG_MESSAGE = new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
    private static final byte[] LONG_MESSAGE_PREFIX = new byte[]{ 0x0a, 0x0b };

    @Before
    public void setUp() throws Exception {
        adapter.setDevice(device);
        adapter.setDeviceAvailable(true);
    }

    @Test
    public void writeWithOneArg() throws IOException {
        adapter.write(SHORT_MESSAGE);
        verify(device).write(SHORT_MESSAGE);
    }

    @Test
    public void writeWithOneArgException() throws IOException {
        doThrow(IOException.class).when(device).write(SHORT_MESSAGE);
        adapter.write(SHORT_MESSAGE);
        verify(adapter).notifyObservers(false);
        verify(adapter).runDeviceObserver();
    }

    @Test(expected = IOException.class)
    public void writeWithOneArgDeviceIsNotAvailable() throws IOException {
        adapter.setDeviceAvailable(false);
        adapter.write(SHORT_MESSAGE);
    }

    @Test
    public void writeWithThreeArg() throws IOException {
        adapter.write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
        verify(device).write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
    }

    @Test
    public void writeWithThreeArgException() throws IOException {
        doThrow(IOException.class).when(device).write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
        adapter.write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
        verify(adapter).notifyObservers(false);
        verify(adapter).runDeviceObserver();
    }

    @Test(expected = IOException.class)
    public void writeWithThreeArgDeviceIsNotAvailable() throws IOException {
        adapter.setDeviceAvailable(false);
        adapter.write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
    }

    @Test
    public void testOpenPort() throws IOException, PortAdapterException {
        adapter.setDeviceAvailable(false);
        adapter.openPort();
        assertTrue(adapter.isDeviceAvailable());
    }

    @Test
    public void testRunObserver() throws IOException, PortAdapterException, InterruptedException {
        adapter.setDeviceListener(new Thread());
        doThrow(IOException.class).when(adapter).openPortFromObserver();
        adapter.setDeviceAvailable(false);
        adapter.runDeviceObserver();
        Thread.sleep(4000);
        assertFalse(adapter.isDeviceAvailable());
        verify(adapter, never()).notifyObservers(true);
        adapter.getDeviceListener().interrupt();
    }

    @Test
    public void testRunObserverSuccess() throws IOException, PortAdapterException, InterruptedException {
        adapter.setDeviceListener(new Thread());
        adapter.runDeviceObserver();
        Thread.sleep(4000);
        assertTrue(adapter.isDeviceAvailable());
        verify(adapter).notifyObservers(true);
    }
}
