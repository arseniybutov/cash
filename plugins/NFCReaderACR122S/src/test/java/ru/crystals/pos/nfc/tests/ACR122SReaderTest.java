package ru.crystals.pos.nfc.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.nfc.acr122.ACR122SReader;
import ru.crystals.utils.NfcUIDConverter;
import ru.crystals.pos.utils.SerialPortAdapter;

@RunWith(MockitoJUnitRunner.class)
public class ACR122SReaderTest {

    private static final int START_TRANSMIT_MARKER = 0x02;

    private static final int END_TRANSMIT_MARKER = 0x03;

    @Mock
    private SerialPortAdapter serialAdapter;

    @InjectMocks
    @Spy
    private ACR122SReader reader = new ACR122SReader();

    @Before
    public void before() throws IOException {
        doReturn(true).when(serialAdapter).waitForAnyData(Mockito.anyLong());
    }

    @Test
    public void pollNull() throws IOException {
        Queue<Integer> bytes = new LinkedList<>(Arrays.asList(
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 4, 0, 0, 0, 0, 0, 1, 0, 0,  0xD5, 0x33, 0x90, 0,  243, END_TRANSMIT_MARKER, -1,
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 5, 0, 0, 0, 0, 0, 1, 0, 0,  213, 75, 0,  144, 0,  138, END_TRANSMIT_MARKER, -1
        ));

        doAnswer(invocationOnMock -> bytes.poll()).when(serialAdapter).read();
        byte[] uid = reader.poll();

        assertNull(uid);
    }


    @Test
    public void poll01() throws IOException { //0782d74a
        Queue<Integer> bytes = new LinkedList<>(Arrays.asList(
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 4, 0, 0, 0, 0, 0, 1, 0, 0,  0xD5, 0x33, 0x90, 0,  243, END_TRANSMIT_MARKER, -1,
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 14, 0, 0, 0, 0, 0, 1, 0, 0,  213, 75, 1,
                1, 0, 4, 8, 4,
                7, 130, 215, 74,
                144, 0, 145, END_TRANSMIT_MARKER, -1
        ));

        doAnswer(invocationOnMock -> bytes.poll()).when(serialAdapter).read();
        byte[] uid = reader.poll();

        assertEquals("0782d74a", Hex.encodeHexString(uid));
        assertEquals("007130215074", NfcUIDConverter.convert(uid));
    }

    @Test
    public void poll02() throws IOException { //349949b1
        Queue<Integer> bytes = new LinkedList<>(Arrays.asList(
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 4, 0, 0, 0, 0, 0, 1, 0, 0,  0xD5, 0x33, 0x90, 0,  243, END_TRANSMIT_MARKER, -1,
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 14, 0, 0, 0, 0, 0, 1, 0, 0,  213, 75, 1,
                1, 0, 4, 8, 4,
                52, 153, 73, 177,
                144, 0, 220, END_TRANSMIT_MARKER, -1
        ));

        doAnswer(invocationOnMock -> bytes.poll()).when(serialAdapter).read();
        byte[] uid = reader.poll();

        assertEquals("349949b1", Hex.encodeHexString(uid));
        assertEquals("052153073177", NfcUIDConverter.convert(uid));
    }

    @Test
    public void poll03() throws IOException { //fb034a02
        Queue<Integer> bytes = new LinkedList<>(Arrays.asList(
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 4, 0, 0, 0, 0, 0, 1, 0, 0,  0xD5, 0x33, 0x90, 0,  243, END_TRANSMIT_MARKER, -1,
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 14, 0, 0, 0, 0, 0, 1, 0, 0,  213, 75, 1,
                1, 0, 4, 8, 4,
                251, 3, 74, 2,
                144, 0, 57, END_TRANSMIT_MARKER, -1
        ));

        doAnswer(invocationOnMock -> bytes.poll()).when(serialAdapter).read();
        byte[] uid = reader.poll();

        assertEquals("fb034a02", Hex.encodeHexString(uid));
        assertEquals("251003074002", NfcUIDConverter.convert(uid));
    }

    @Test
    public void poll04() throws IOException { //c703019a
        Queue<Integer> bytes = new LinkedList<>(Arrays.asList(
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 4, 0, 0, 0, 0, 0, 1, 0, 0,  0xD5, 0x33, 0x90, 0,  243, END_TRANSMIT_MARKER, -1,
                START_TRANSMIT_MARKER, 0, 0, END_TRANSMIT_MARKER,
                START_TRANSMIT_MARKER, 128, 14, 0, 0, 0, 0, 0, 1, 0, 0,  213, 75, 1,
                1, 0, 4, 8, 4,
                199, 3, 1, 154,
                144, 0, 214, END_TRANSMIT_MARKER, -1
        ));

        doAnswer(invocationOnMock -> bytes.poll()).when(serialAdapter).read();
        byte[] uid = reader.poll();
        
        assertEquals("c703019a", Hex.encodeHexString(uid));
        assertEquals("199003001154", NfcUIDConverter.convert(uid));
    }

}
