package ru.crystals.pos.scale.cas.ad;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import ru.crystals.pos.CashException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CasScalesServiceImplTest {
    private String port = "";
    private static final int[] CORRECT_ACK_RESPONSE = new int[]{0x6, 0x23};
    private static final int[] WRONG_ACK_RESPONSE = new int[]{0x2, 0x23};
    private static final int ACK = 0X6;
    private static final int EOT = 0X4;
    //Возвращает вес в 001234kg (1,234 кг)
    private static final int[] CORRECT_EOT_RESPONSE = new int[]{0x6, 0x23, 0x53, 0x30, 0x30, 0x30, 0x31, 0x32, 0x33, 0x34, 0x6b, 0x67};
    //Возвращает вес в '  0.76kg' (760 гр)
    private static final int[] CORRECT_EOT_RESPONSE_SPACES = new int[]{0x01, 0x02, 0x53, 0x20, 0x20, 0x20, 0x30, 0x2E, 0x37, 0x36, 0x6B, 0x67};
    private static final int[] WRONG_WEIGHT_EOT_RESPONSE = new int[]{0x6, 0x23, 0x53, 0x30, 0x30, 0x30, 0x31, 0x32, 0x33, 0x34, 0x6b, 0x68};
    private static final int[] NOT_STABLE_DATA_EOT_RESPONSE = new int[]{0x6, 0x23, 0x00, 0x2d, 0x30, 0x30, 0x31, 0x32, 0x33, 0x34, 0x6b, 0x67};
    @Spy
    private CasScalesServiceImpl service = new CasScalesServiceImpl();
    @Mock
    private SerialPortAdapter portAdapter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service.setPortAdapter(portAdapter);
        portAdapter.setPort(port);
        doReturn(true).when(portAdapter).isConnected();
    }

    @Test
    public void testStart() throws Exception {
        service.start();
        verify(portAdapter).openPort();
    }

    @Test
    public void testStartIOException() throws Exception {
        doThrow(new IOException()).when(portAdapter).openPort();
        try {
            service.start();
            fail("Исключение не было выброшено");
        } catch (CashException e) {
        }
        verify(portAdapter).openPort();
    }

    @Test
    public void testStop() throws Exception {
        service.start();
        verify(portAdapter).openPort();
        service.stop();
        verify(portAdapter).close();
    }

    @Test
    public void moduleCheckState() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(CORRECT_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertTrue(service.moduleCheckState());
        verify(portAdapter, times(2)).read(anyInt());
    }

    @Test
    public void muduleCheckStateNullWeightData() throws Exception {
        doReturn(WRONG_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(CORRECT_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertFalse(service.moduleCheckState());
        verify(portAdapter).read(anyInt());
    }

    @Test
    public void muduleCheckStateExceptionInWeightData() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(WRONG_WEIGHT_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertFalse(service.moduleCheckState());
    }

    @Test
    public void getWeight() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(CORRECT_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertEquals(1234, service.getWeight());
        verify(portAdapter, times(2)).read(anyInt());
    }

    @Test
    public void getWeightSpaces() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(CORRECT_EOT_RESPONSE_SPACES).when(portAdapter).read(EOT);
        assertEquals(760, service.getWeight());
        verify(portAdapter, times(2)).read(anyInt());
    }

    @Test
    public void getWeightWeightDataReturnsNull() throws Exception {
        doReturn(WRONG_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(CORRECT_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertEquals(0, service.getWeight());
        verify(portAdapter).read(anyInt());
    }

    @Test
    public void getWeightWeightDataThrowsException() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(WRONG_WEIGHT_EOT_RESPONSE).when(portAdapter).read(EOT);
        try {
            service.getWeight();
            fail("Исключение не было выброшено");
        } catch (CashException e) {
        }
    }

    @Test
    public void getWeightWeightDataIsNotStable() throws Exception {
        doReturn(CORRECT_ACK_RESPONSE).when(portAdapter).read(ACK);
        doReturn(NOT_STABLE_DATA_EOT_RESPONSE).when(portAdapter).read(EOT);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(2)).read(anyInt());
    }
}
