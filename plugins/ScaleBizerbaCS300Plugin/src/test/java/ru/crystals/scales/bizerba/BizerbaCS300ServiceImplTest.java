package ru.crystals.scales.bizerba;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.Timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

//TODO: гавно в тестах разобраться
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class BizerbaCS300ServiceImplTest {
    private String port = "COM1";
    private String baudRate = "9600";
    private int dataBits = SerialPortAdapter.DATABITS_7;
    private String stopBits = "1";
    private String parity = "ODD";
    private static final byte EOT = 0x04;
    private static final byte STX = 0x02;
    private static final byte ESC = 0x1B;
    private static final byte ENQ = 0x05;
    private static final byte ETX = 0x03;
    private static final byte NAK = 0x15;
    private final byte[] getWeightCommand = new byte[]{ EOT, ENQ };
    private final byte[] getErrorCodeCommand = new byte[]{ EOT, STX, 0x30, 0x38, ETX }; //30 и 38 - номер соответствующей команды в ASCII (08)
    private final byte[] getWeightResponse =
        new byte[]{ STX, 0x30, 0x32, ESC, 0x33, ESC, 0x30, 0x30, 0x30, 0x36, 0x38, ESC, 0x30, 0x30, 0x30, 0x39, 0x38, 0x37, ETX };
    private final byte[] getErrorCodeResponse = new byte[]{ STX, 0x30, 0x39, ESC, 0x31, 0x31, ETX };
    private final byte[] nakResponse = new byte[]{ NAK };
    private final byte[] ackResponse = new byte[]{ 0x06 };
    @Mock
    private SerialPortAdapter portAdapter;
    @InjectMocks
    private BizerbaCS300ServiceImpl scales = spy(new BizerbaCS300ServiceImpl());

    @Test
    public void testStart() throws Exception {
        //when
        scales.start();
        scales.stop();

        //then
        verify(portAdapter).setPort(port);
        verify(portAdapter).setBaudRate(baudRate);
        verify(portAdapter).setDataBits(dataBits);
        verify(portAdapter).setStopBits(stopBits);
        verify(portAdapter).setParity(parity);
        verify(portAdapter).openPort();
    }

    @Test
    public void testTransmitUnitPrice() throws Exception {
        transmitUnitPrice(23456L);

        verify(scales).executePriceTransmission(new byte[]{ EOT, STX, (byte) 0x30, (byte) 0x31, ESC, 0x30, 0x32, 0x33, 0x34, 0x35, 0x36, ESC, ETX });
    }

    @Test
    public void testTransmitUnitPriceZero() throws Exception {
        transmitUnitPrice(0);
        assertEquals(0, scales.getWeightVariable());
    }

    private void transmitUnitPrice(long price) throws ScaleException {
        doNothing().when(scales).executePriceTransmission(any(byte[].class));

        //when
        scales.transmitUnitPrice(price);

        //then
        assertEquals(scales.getPrice(), price);
    }

    @Test
    public void testStop() throws Exception {
        scales.start();

        scales.stop();

        verify(portAdapter).close();
    }

    @Test
    public void testGetWeight() throws Exception {
        doReturn(true).when(scales).executeGetWeightCommand();

        scales.getWeight();

        verify(scales).executeGetWeightCommand();
    }

    @Test
    public void testGetWeightWhenNak() throws Exception {
        doReturn(false).when(scales).executeGetWeightCommand();

        scales.getWeight();

        verify(scales).executeGetWeightCommand();
        verify(scales).getErrorCode();
    }

    @Test
    public void testGetWeightWhenPriceReset() throws Exception {
        doReturn(ErrorCode.NO_UNIT_PRICE).when(scales).getErrorCode();
        doNothing().when(scales).transmitUnitPrice(anyLong());
        long price = 1;
        scales.setPrice(price);

        testGetWeightWhenNak();

        verify(scales).transmitUnitPrice(price);
    }

    @Test
    public void testGetWeightWhenScalesPassive() throws Exception {
        doReturn(ErrorCode.NO_MOTION_SINCE_LAST_OPERATION).when(scales).getErrorCode();

        testGetWeightWhenNak();

        verify(scales, never()).transmitUnitPrice(anyLong());
    }

    @Test
    public void testGetWeightWhenMinRange() throws Exception {
        doReturn(false).when(scales).executeGetWeightCommand();
        doReturn(ErrorCode.MIN_RANGE).when(scales).getErrorCode();

        scales.getWeight();

        assertEquals(0, scales.getWeightVariable());
    }

    @Test
    public void testGetWeighWithTransmissionOfUnitPrice() throws Exception {

        doNothing().when(scales).transmitUnitPrice(anyLong());
        doReturn(0).when(scales).getWeight();

        scales.getWeighWithTransmissionOfUnitPrice(25L);

        verify(scales).transmitUnitPrice(anyLong());
        verify(scales).getWeight();
    }

    @Test
    public void testGetWeighWithTransmissionOfUnitPriceFail() throws Exception {

        doNothing().when(scales).transmitUnitPrice(anyLong());
        doThrow(ScaleException.class).when(scales).getWeight();

        int w = scales.getWeighWithTransmissionOfUnitPrice(25L);

        verify(scales).transmitUnitPrice(anyLong());
        verify(scales).getWeight();
        assertEquals(0, w);
    }

    @Test
    public void testModuleCheckStateTrue() throws Exception {
        testModuleCheckState(true);
    }

    @Test
    public void testModuleCheckStateFalse() throws Exception {
        testModuleCheckState(false);
    }

    private void testModuleCheckState(Boolean active) throws Exception {

        doReturn(active).when(portAdapter).isConnected();

        assertEquals(scales.moduleCheckState(), active);
    }

    @Test
    public void testReadResponse() throws Exception {
        doReturn(2).when(portAdapter).getInputStreamBufferSize();
        doReturn(new byte[]{ EOT, ETX }).when(portAdapter).readBytes();
        doReturn(new int[]{ }).when(portAdapter).readAll();
        doNothing().when(portAdapter).write(any(byte[].class));

        scales.readResponse(new byte[]{ });
        verify(portAdapter).readAll();
        verify(portAdapter).write(any(byte[].class));
        verify(portAdapter).getInputStreamBufferSize();
        verify(portAdapter).readBytes();
    }

    @Test
    public void testReadResponseEmpty() throws Exception {
        doReturn(0).when(portAdapter).getInputStreamBufferSize();
        doReturn(new int[]{ }).when(portAdapter).readAll();
        doNothing().when(portAdapter).write(any(byte[].class));
        doReturn(false).doReturn(true).when(scales).isTimerExpired(any(Timer.class));

        scales.readResponse(new byte[]{ });

        verify(portAdapter).getInputStreamBufferSize();
    }

    @Test
    public void testExecuteGetWeightCommand() throws Exception {
        doReturn(getReadData(getWeightResponse)).when(scales).readResponse(getWeightCommand);

        boolean answer = scales.executeGetWeightCommand();

        assertEquals(68, scales.getWeightVariable());
        assertTrue(answer);
    }

    private StringBuilder getReadData(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append((char) aByte);
        }
        return sb;
    }

    @Test
    public void testExecuteGetWeightCommandNak() throws Exception {
        doReturn(getReadData(nakResponse)).when(scales).readResponse(getWeightCommand);

        boolean answer = scales.executeGetWeightCommand();

        assertFalse(answer);
        assertEquals(0, scales.getWeightVariable());
    }

    @Test(expected = ScaleException.class)
    public void testExecuteGetWeightCommandInvalid() throws Exception {
        doReturn(getReadData(new byte[]{ 1, 1, 1, 1 })).when(scales).readResponse(getWeightCommand);

        scales.executeGetWeightCommand();

        assertEquals(0, scales.getWeightVariable());
    }

    @Test(expected = ScaleException.class)
    public void testExecutePriceTransmission() throws Exception {
        doReturn(getReadData(nakResponse)).when(scales).readResponse(any(byte[].class));

        scales.executePriceTransmission(new byte[]{ });
    }

    @Test
    public void testParseErrorCode() throws Exception {
        doReturn(getReadData(getErrorCodeResponse)).when(scales).readResponse(getErrorCodeCommand);

        scales.getErrorCode();

        assertEquals(ErrorCode.NO_UNIT_PRICE, scales.getErrorCode());
    }

    @Test
    public void testParseErrorNak() throws Exception {
        doReturn(getReadData(nakResponse)).when(scales).readResponse(getErrorCodeCommand);

        scales.getErrorCode();

        assertEquals(ErrorCode.UNKNOWN, scales.getErrorCode());
    }
}
