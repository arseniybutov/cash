package ru.crystals.pos.scale.bizerba.ecoasia;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.crystals.pos.CashException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.test.exceptions.ExceptionResult;

public class BizebraEcoAsiaScalesServiceImplTest {

    // Возвращает вес в 0.082 кг
    private static final int[] CORRECT_RESPONSE_00_082 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x20, 0x30, 0x2E, 0x30, 0x38, 0x32, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] CORRECT_RESPONSE_00_406 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x20, 0x30, 0x2E, 0x34, 0x30, 0x36, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] CORRECT_RESPONSE_05_317 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x20, 0x35, 0x2E, 0x33, 0x31, 0x37, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] CORRECT_RESPONSE_14_983 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x31, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS1 = new int[]{0x55, 0x53, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x31, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS2 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x31, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B,
            0x67, 0x20, 0x0B, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS3 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x31, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B, 0x67,
            0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS4 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x29, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS5 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x30, 0x34, 0x2E, 0x39, 0x38, 0x29, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS6 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x30, 0x3A, 0x2E, 0x39, 0x38, 0x35, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS7 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x30, 0x33, 0x2E, 0x39, 0x38, 0x3A, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ANS8 = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x20, 0x20, 0x30, 0x33, 0x2E, 0x39, 0x38, 0x3A, 0x6B,
            0x67, 0x20, 0x0D, 0x0A, 0x0A};
    private static final int[] CORRECT_RESPONSE_MINUS = new int[]{0x53, 0x54, 0x2C, 0x47, 0x53, 0x2C, 0x2D, 0x20, 0x31, 0x32, 0x2E, 0x34, 0x34, 0x34, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] CORRECT_RESPONSE_TARA = new int[]{0x53, 0x54, 0x2C, 0x4E, 0x54, 0x2C, 0x20, 0x20, 0x31, 0x34, 0x2E, 0x39, 0x38, 0x33, 0x6B,
            0x67, 0x20, 0x0D, 0x0A};
    private static final int[] NEGATIVE_RESPONSE_ZERO = new int[0];

    private BizebraEcoAsiaScalesServiceImpl service = new BizebraEcoAsiaScalesServiceImpl();
    @Mock
    private SerialPortAdapter portAdapter;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
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
    public void testGetWeightLess100gramms() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_00_082);
        assertEquals(82, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test
    public void testGetWeightLess1000gramms() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_00_406);
        assertEquals(406, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test
    public void testGetWeightLess10000gramms() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_05_317);
        assertEquals(5317, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test
    public void testGetWeightLess15000gramms() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_14_983);
        assertEquals(14983, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test
    public void testGetWeightNegativeAns1() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS1);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns2() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS2);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns3() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS3);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionZero() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ZERO);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns4() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS4);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns5() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS5);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns6() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS6);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns7() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS7);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test(expected = CashException.class)
    public void getWeightWeightDataThrowsExceptionAns8() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS8);
        service.getWeight();
        fail("Исключение не было выброшено");
    }

    @Test
    public void getWeightWeightDataThrowsExceptionAns8A() throws Exception {
        when(portAdapter.readAll()).thenReturn(NEGATIVE_RESPONSE_ANS8);

        ExceptionResult<ScaleException> exceptionResult = new ExceptionResult<>();
        exceptionRule.expect(ScaleException.class);
        exceptionRule.expectMessage("Неверная длина пакета");
        service.getWeight();
    }

    @Test
    public void getWeightWeightDataWithMinus() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_MINUS);
        assertEquals(0, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }

    @Test(expected = CashException.class)
    public void testGetWeightWhenWeightDataReturnsNull() throws Exception {
        assertEquals(0, service.getWeight());
        fail("Исключение не было выброшено");
    }

    @Test
    public void testGetWeightWithTara() throws Exception {
        when(portAdapter.readAll()).thenReturn(CORRECT_RESPONSE_TARA);
        assertEquals(14983, service.getWeight());
        verify(portAdapter, times(1)).readAll();
    }
}
