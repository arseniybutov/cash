package ru.crystals.pos.utils;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HIDDeviceAdapterTest {
    @Spy
    private HIDDeviceAdapter adapter;
    @Mock
    private HIDDevice device;
    @Mock
    private HIDManager manager;
    private static final byte[] SHORT_MESSAGE = new byte[]{ 0x01 };
    private static final byte[] SHORT_MESSAGE_COMPLETE = new byte[]{ 0x00, 0x01 };
    private static final byte[] LONG_MESSAGE = new byte[]{ 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
    private static final byte[] LONG_MESSAGE_PREFIX = new byte[]{ 0x0a, 0x0b };

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void writeWithOneArg() throws IOException {
        adapter.setDevice(device);
        adapter.write(SHORT_MESSAGE);
        verify(device).write(SHORT_MESSAGE_COMPLETE);
    }

    @Test
    public void writeWithOneEmptyArg() throws IOException {
        adapter.setDevice(device);
        adapter.write(new byte[]{ });
        verify(device).write(new byte[]{ 0x00 });
    }

    @Test(expected = IOException.class)
    public void writeWhenDeviceIsNotSet() throws IOException {
        adapter.write(new byte[]{ });
        verify(device, never()).write((byte[]) any());
    }

    @Test
    public void writeWithThreeArg() throws IOException {
        adapter.setDevice(device);
        adapter.write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
        verify(device).write(new byte[]{ 0x00, 0x0a, 0x0b, 0x01, 0x02, 0x03 });
        verify(device).write(new byte[]{ 0x00, 0x0a, 0x0b, 0x04, 0x05, 0x06 });
        verify(device).write(new byte[]{ 0x00, 0x0a, 0x0b, 0x07, 0x08, 0x09 });
        verify(device, times(3)).write((byte[]) any());
    }

    @Test
    public void writeWithThreeEmptyArg() throws IOException {
        adapter.setDevice(device);
        adapter.write(new byte[]{ }, 3, LONG_MESSAGE_PREFIX);
        verify(device, never()).write((byte[]) any());
    }

    @Test(expected = IOException.class)
    public void writeThreeWhenDeviceIsNotSet() throws IOException {
        adapter.write(LONG_MESSAGE, 3, LONG_MESSAGE_PREFIX);
        verify(device, never()).write((byte[]) any());
    }

    @Test(expected = IOException.class)
    public void writeThreeWithWrongPrefix() throws IOException {
        adapter.setDevice(device);
        adapter.write(LONG_MESSAGE, -1, LONG_MESSAGE_PREFIX);
        verify(device, never()).write((byte[]) any());
    }

    @Test
    public void testClose() throws IOException {
        adapter.setDevice(device);
        adapter.close();
        verify(device).close();
    }
    @Test
    public void testCloseDeviceIsNull() throws IOException {
        adapter.close();
        verify(device,never()).close();
    }
    @Test
    public void testCloseThrowException() throws IOException {
        adapter.setDevice(device);
        doThrow(IOException.class).when(device).close();
        adapter.close();
    }
}
