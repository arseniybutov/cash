package ru.crystals.pos.scale.shtrih.slim200;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class ShtrihSlim200ScaleServiceImplTest {
    private static final int NAK = 0X15;
    private static final int[] CORRECT_STATE_RESPONSE = new int[]{NAK};
    private static final int[] WRONG_STATE_RESPONSE = new int[]{0};
    // Возвращает вес в 0.312 кг
    private static final int[] CORRECT_RESPONSE = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D};
    // Возвращает вес в 382 г и т.д.
    private static final int[] CORRECT_RESPONSE_382 = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x7E, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5B};
    private static final int[] CORRECT_RESPONSE_190 = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0xBE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x9A};
    private static final int[] CORRECT_RESPONSE_200 = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0xC8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xEC};
    private static final int[] CHANNEL_INFO = new int[]{NAK, 0x06, 0x02, 0x0B, /*команда*/ 0xE8, 0x00, 0x15, 0x00, 0x00, 0x00, /*НПВ*/ 0x00, 0x00, /*НмПВ 200*/ 0xC8, 0x00, 0x00, 0x3E};
    private static final int[] NEGATIVE_WEIGHT_RESPONSE = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x14, 0x00, 0x70, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0xAA};
    private static final int[] WEIGHT_NOT_STABLE_RESPONSE = new int[]{NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x04, 0x00, 0xC4, 0x8, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF9};
    private static final int[] CORRECT_RESPONSE_ANS1 = new int[]{NAK, NAK, NAK, 0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D};
    private static final int[] CORRECT_RESPONSE_ANS2 = new int[]{0x06, 0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D};
    private static final int[] CORRECT_RESPONSE_ANS3 = new int[]{0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D};
    private static final int[] NEGATIVE_CRC_RESPONSE = new int[]{0x02, 0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1F};
    private static final int[] NEGATIVE_RESPONSE_ANS1 = new int[]{0x0B, 0x3A, 0x00, 0x15, 0x00, 0x38, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D};
    @Spy
    private ShtrihSlim200ScaleServiceImpl service = new ShtrihSlim200ScaleServiceImpl();
    @Mock
    private SerialPortAdapter portAdapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service.setSerialPortAdapter(portAdapter);
        when(portAdapter.isConnected()).thenReturn(true);
        service.setReadTimeout(1);
    }

    @Test
    public void testStart() throws Exception {
        service.start();
        verify(portAdapter).openPort();
    }

    @Test(expected = CashException.class)
    public void testStartWithException() throws Exception {
        doThrow(Exception.class).when(portAdapter).openPort();
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
            // ok
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
    public void testModuleCheckStateOk() throws Exception {
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE);
        assertTrue(service.moduleCheckState());
        verify(portAdapter).read(anyInt());
        verify(portAdapter).readAll();
    }

    @Test
    public void testModuleCheckStateNullWeightData() throws Exception {
        when(portAdapter.read(NAK)).thenReturn(WRONG_STATE_RESPONSE);
        assertFalse(service.moduleCheckState());
        verify(portAdapter).read(anyInt());
    }

    @Test
    public void testModuleCheckStateExceptionInWeightData() throws Exception {
        when(portAdapter.readAll()).thenThrow(Exception.class);
        assertFalse(service.moduleCheckState());
    }

    @Test
    public void testGetWeight() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMinimalWeight(40);
        assertEquals(312, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test
    public void testGetWeightWithAnswer1() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_ANS1);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMinimalWeight(40);
        assertEquals(312, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test
    public void testGetWeightWithAnswer2() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_ANS2);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMinimalWeight(40);
        assertEquals(312, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test
    public void testGetWeightWithAnswer3() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_ANS3);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMinimalWeight(40);
        assertEquals(312, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test(expected = ScaleException.class)
    public void testGetWeightWithNegativeCRC() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_CRC_RESPONSE);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.getWeight();
    }

    @Test(expected = ScaleException.class)
    public void testGetWeightWithAnswerWithoutSTX() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS1);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.getWeight();
    }

    @Test
    public void testGetWeightWithAnswer3WithMinimalWeight() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_ANS3);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMinimalWeight(400);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test
    public void testGetWeightWhenWeightDataReturnsNull() throws Exception {
        when(portAdapter.read(NAK)).thenReturn(WRONG_STATE_RESPONSE);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(2)).read(anyInt());
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsException() throws Exception {
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test
    public void getWeightWithsNotStableWeight() throws Exception {
        when(portAdapter.readAll()).thenReturn(WEIGHT_NOT_STABLE_RESPONSE);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    @Test
    public void getWeightWithNegativeWeight() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_WEIGHT_RESPONSE);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(3)).read(anyInt());
        verify(portAdapter, times(3)).readAll();
    }

    public Collection<Object[]> multipliers() {
        return Arrays.asList(new Object[][] {
                {"0.01", 3},
                {"0.1", 38},
                {"1", 382},
                {"10", 3820},
                {"100", 38200},
                // multiplier 1 для некорректных значений
                {"aaa", 382},
                {"0.0", 382},
                {"0", 382},
                {null, 382},
                // любой разделитель
                {"0,1", 38}
        });
    }

    @Test
    @Parameters(method = "multipliers")
    public void testGetWeightWithMultiplier(String multiplier, Integer expected) throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_382);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMultiplier(multiplier);
        assertNotNull(expected);
        assertEquals(expected.intValue(), service.getWeight());
    }

    @Test
    public void testMinimalWeightFromConfigZeroWithMultiplier() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_190);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMultiplier("0.1");
        service.setMinimalWeight(20);
        // реальный минимальный вес = 20, поэтому 19 г -> 0 г
        assertEquals(0, service.getWeight());
        assertEquals(200, service.getTotalMinimalWeight());
    }

    @Test
    public void testMinimalWeightFromConfigNotZeroWithMultiplier() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_200);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMultiplier("0.1");
        service.setMinimalWeight(20);
        // реальный минимальный вес = 20, поэтому 19 г -> 0 г
        assertEquals(20, service.getWeight());
        assertEquals(200, service.getTotalMinimalWeight());
    }

    @Test
    public void testMinimalWeightFromScaleZeroWithMultiplier() throws Exception {
        when(portAdapter.readAll())
                .thenReturn(CORRECT_RESPONSE_190)
                // минимальный вес в ответе весов = 200
                .thenReturn(CHANNEL_INFO)
                .thenReturn(CORRECT_RESPONSE_190);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMultiplier("0.1");
        // реальный минимальный вес = 20, поэтому 19 г -> 0 г
        assertEquals(0, service.getWeight());
        assertEquals(200, service.getTotalMinimalWeight());
    }

    @Test
    public void testMinimalWeightFromScaleNotZeroWithMultiplier() throws Exception {
        when(portAdapter.readAll())
                .thenReturn(CORRECT_RESPONSE_200)
                // минимальный вес в ответе весов = 200
                .thenReturn(CHANNEL_INFO)
                .thenReturn(CORRECT_RESPONSE_200);
        when(portAdapter.read(NAK)).thenReturn(CORRECT_STATE_RESPONSE);
        service.setMultiplier("0.1");
        assertEquals(20, service.getWeight());
        assertEquals(200, service.getTotalMinimalWeight());
    }
}
