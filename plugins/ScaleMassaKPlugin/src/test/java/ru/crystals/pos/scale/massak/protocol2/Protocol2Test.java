package ru.crystals.pos.scale.massak.protocol2;

import java.io.IOException;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;

@RunWith(MockitoJUnitRunner.class)
public class Protocol2Test {
    private static final byte GET_WEIGHT_COMMAND = 0x45;
    private static final byte GET_STATUS_COMMAND = 0x48;
    private static final int PACKET_LENGTH = 2;
    private static final long READ_TIMEOUT = 200;
    private static final int STOP_BITS = 2;
    private static final String PARITY_EVEN = "EVEN";
    private static final int BAUD_RATE = 4800;
    private static final byte[] CORRECT_STATUS_GRAM = new byte[]{(byte) 128,0x00};
    private static final byte[] CORRECT_STATUS_TENTH_GRAM = new byte[]{(byte) 128,0x01};
    private static final byte[] INCORRECT_STATUS_GRAM_ALL_WRONG = new byte[]{(byte) 112,0x00};
    private static final byte TEST_20 = (byte) 20;
    private static final String TEST_20_BINARY = "00010100";
    private static final int TEST_WEIGHT_GRAM = 20;
    private static final int TEST_WEIGHT_TENTH_GRAM = 200;

    @Mock
    private SerialPortAdapter serialPortAdapter;

    @Mock
    private Status status;

    @Spy
    @InjectMocks
    private MassaKProtocol2Impl service = new MassaKProtocol2Impl();

    @Before
    public void beforeTest(){
        doReturn(serialPortAdapter).when(serialPortAdapter).setBaudRate(anyInt());
        doReturn(serialPortAdapter).when(serialPortAdapter).setStopBits(anyInt());
        doReturn(serialPortAdapter).when(serialPortAdapter).setParity(anyString());
        doReturn(serialPortAdapter).when(serialPortAdapter).setLogger(any(Logger.class));
    }

    @Test
    public void testStart() throws Exception {

        //when
        service.start();
        //then
        verify(serialPortAdapter).setBaudRate(BAUD_RATE);
        verify(serialPortAdapter).setStopBits(STOP_BITS);
        verify(serialPortAdapter).setParity(PARITY_EVEN);
        verify(serialPortAdapter).setLogger(any(Logger.class));
        verify(serialPortAdapter).openPort();
    }

    @Test
     public void testStartThrowsIOException() throws Exception {
        //given
        doThrow(new IOException()).when(serialPortAdapter).openPort();

        //when
        try {
            service.start();
            fail("Expected CashException but never throws");
        } catch (CashException e) {

        }
        //then
        verify(serialPortAdapter).setBaudRate(BAUD_RATE);
        verify(serialPortAdapter).setStopBits(STOP_BITS);
        verify(serialPortAdapter).setParity(PARITY_EVEN);
        verify(serialPortAdapter).setLogger(any(Logger.class));
        verify(serialPortAdapter).openPort();
    }

    @Test
    public void testStartThrowsPortAdapterException() throws Exception {
        //given
        doThrow(new PortAdapterException("")).when(serialPortAdapter).openPort();

        //when
        try {
            service.start();
            fail("Expected CashException but never throws");
        } catch (CashException e) {

        }
        //then
        verify(serialPortAdapter).setBaudRate(BAUD_RATE);
        verify(serialPortAdapter).setStopBits(STOP_BITS);
        verify(serialPortAdapter).setParity(PARITY_EVEN);
        verify(serialPortAdapter).setLogger(any(Logger.class));
        verify(serialPortAdapter).openPort();
    }

    @Test
    public void testSetPort() throws Exception {

        //when
        service.setPort("ANY");
        //then
        verify(serialPortAdapter).setPort("ANY");
    }

    @Test
    public void testSetBaudRate() throws Exception {

        //when
        service.setBaudRate(BAUD_RATE);
        //then
        verify(serialPortAdapter).setBaudRate(BAUD_RATE);
    }

    @Test
    public void testSetDataBits() throws Exception {

        //when
        service.setDataBits(BAUD_RATE);
        //then
        verify(serialPortAdapter).setDataBits(BAUD_RATE);
    }

    @Test
    public void testSetStopBits() throws Exception {

        //when
        service.setStopBits(BAUD_RATE);
        //then
        verify(serialPortAdapter).setStopBits(BAUD_RATE);
    }

    @Test
    public void testSetParity() throws Exception {

        //when
        service.setParity(BAUD_RATE);
        //then
        verify(serialPortAdapter).setParity(BAUD_RATE);
    }

    @Test
    public void testModuleCheckState() throws Exception {
        //given

        doReturn(new Status(new byte[]{0, 0})).when(service).getStatus();

        //when
        boolean result = service.moduleCheckState();
        //then
        assertTrue(result);
        verify(service).getStatus();
    }

    @Test
    public void testModuleCheckStateReturnFalse() throws Exception {
        //given

        doThrow(new ScaleException()).when(service).getStatus();

        //when
        boolean result = service.moduleCheckState();
        //then
        assertFalse(result);
        verify(service).getStatus();
    }

    @Test
    public void testGetStatusGram() throws Exception {
        //given

        doReturn(CORRECT_STATUS_GRAM).when(service).runCommand(anyByte());

        //when
        Status status = service.getStatus();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertTrue(status.isWeightStable());
        assertFalse(status.isLowWeight());
        assertFalse(status.isTareOnScale());
        assertEquals(status.getMeasure(), Measure.GRAM);
    }

    @Test
    public void testGetStatusTenthGram() throws Exception {
        //given

        doReturn(CORRECT_STATUS_TENTH_GRAM).when(service).runCommand(anyByte());

        //when
        Status status = service.getStatus();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertTrue(status.isWeightStable());
        assertFalse(status.isLowWeight());
        assertFalse(status.isTareOnScale());
        assertEquals(status.getMeasure(), Measure.TENTH_OF_GRAM);
    }

    @Test
    public void testGetStatusGramAllWrong() throws Exception {
        //given

        doReturn(INCORRECT_STATUS_GRAM_ALL_WRONG).when(service).runCommand(anyByte());

        //when
        Status status = service.getStatus();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertFalse(status.isWeightStable());
        assertTrue(status.isLowWeight());
        assertTrue(status.isTareOnScale());
        assertEquals(status.getMeasure(), Measure.GRAM);
    }

    @Test
    public void testGetStatusThrowsException() throws Exception {
        //given

        doThrow(new ScaleException("")).when(service).runCommand(anyByte());

        //when
        try {
            service.getStatus();
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
    }

    @Test
    public void testRunCommand() throws Exception {
        //given

        doReturn(CORRECT_STATUS_GRAM).when(serialPortAdapter).readBytes();

        //when

        byte[] result = service.runCommand(GET_STATUS_COMMAND);
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter).readBytes();
        assertThat(result).isSameAs(CORRECT_STATUS_GRAM);
    }

    @Test
    public void testRunCommandWriteThrowsIOException() throws Exception {
        //given
        doThrow(new IOException()).when(serialPortAdapter).write(anyByte());

        //when
        try {
            service.runCommand(GET_STATUS_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter, never()).readBytes();
    }

    @Test
    public void testRunCommandReadThrowsIOException() throws Exception {
        //given
        doThrow(new IOException()).when(serialPortAdapter).readBytes();

        //when
        try {
            service.runCommand(GET_STATUS_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter).readBytes();
    }

    @Test
    public void testRunCommandReadNullBuffer() throws Exception {
        //given
        doReturn(null).when(serialPortAdapter).readBytes();

        //when
        try {
            service.runCommand(GET_STATUS_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter).readBytes();
    }

    @Test
    public void testRunCommandWrongAnswerSize() throws Exception {
        //given
        doReturn(new byte[]{0,1,2}).when(serialPortAdapter).readBytes();

        //when
        try {
            service.runCommand(GET_STATUS_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter).readBytes();
    }

    @Test
    public void testRunCommandWrongAnswerSize2() throws Exception {
        //given
        doReturn(new byte[]{0}).when(serialPortAdapter).readBytes();

        //when
        try {
            service.runCommand(GET_STATUS_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {

        }
        //then
        verify(serialPortAdapter).write(GET_STATUS_COMMAND);
        verify(serialPortAdapter).readBytes();
    }

    @Test
    public void testToBinaryString() throws Exception {
        //when
        String result = service.toBinaryString(TEST_20);
        //then
        assertEquals(TEST_20_BINARY, result);
    }

    @Test
    public void testParseWeight() throws Exception {
        //given

        //when
        int result = service.parseWeight(new byte[]{TEST_20, 0});
        //then
        verify(service,times(2)).toBinaryString(anyByte());
        verify(service).toBinaryString(TEST_20);
        verify(service).toBinaryString((byte) 0);
        assertTrue(result == TEST_20);
    }

    @Test
    public void testParseWeightNegative() throws Exception {
        //given

        //when
        int result = service.parseWeight(new byte[]{TEST_20, -10});
        //then
        verify(service,never()).toBinaryString(anyByte());
        assertTrue(result == 0);
    }

    @Test
    public void testGetWeightGram() throws Exception {
        //given
        doReturn(status).when(service).getStatus();
        doReturn(false).when(status).isLowWeight();
        doReturn(true).when(status).isWeightStable();
        doReturn(CORRECT_STATUS_GRAM).when(service).runCommand(anyByte());
        doReturn(TEST_WEIGHT_GRAM).when(service).parseWeight(any(byte[].class));
        doReturn(Measure.GRAM).when(status).getMeasure();
        //when
        int result = service.getWeight();
        //then
        verify(service).getStatus();
        verify(status).isLowWeight();
        verify(status).isWeightStable();
        verify(service).runCommand(GET_WEIGHT_COMMAND);
        verify(service).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, times(2)).getMeasure();
        assertThat(result).isSameAs(TEST_WEIGHT_GRAM);
    }

    @Test
    public void testGetWeightTenthGram() throws Exception {
        //given
        doReturn(status).when(service).getStatus();
        doReturn(false).when(status).isLowWeight();
        doReturn(true).when(status).isWeightStable();
        doReturn(CORRECT_STATUS_GRAM).when(service).runCommand(anyByte());
        doReturn(TEST_WEIGHT_TENTH_GRAM).when(service).parseWeight(any(byte[].class));
        doReturn(Measure.TENTH_OF_GRAM).when(status).getMeasure();
        //when
        int result = service.getWeight();
        //then
        verify(service).getStatus();
        verify(status).isLowWeight();
        verify(status).isWeightStable();
        verify(service).runCommand(GET_WEIGHT_COMMAND);
        verify(service).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, times(2)).getMeasure();
        assertThat(result).isSameAs(TEST_WEIGHT_GRAM);
    }

    @Test
    public void testGetWeightLow() throws Exception {
        //given
        doReturn(status).when(service).getStatus();
        doReturn(true).when(status).isLowWeight();
        //when
        int result = service.getWeight();
        //then
        verify(service).getStatus();
        verify(status).isLowWeight();
        verify(status, never()).isWeightStable();
        verify(service, never()).runCommand(GET_WEIGHT_COMMAND);
        verify(service, never()).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, never()).getMeasure();
        assertThat(result).isSameAs(0);
    }

    @Test
    public void testGetWeightNotStable() throws Exception {
        //given
        doReturn(status).when(service).getStatus();
        doReturn(false).when(status).isLowWeight();
        doReturn(false).when(status).isWeightStable();
        //when
        int result = service.getWeight();
        //then
        verify(service).getStatus();
        verify(status).isLowWeight();
        verify(status).isWeightStable();
        verify(service, never()).runCommand(GET_WEIGHT_COMMAND);
        verify(service, never()).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, never()).getMeasure();
        assertThat(result).isSameAs(0);
    }

    @Test
    public void testGetWeightGetStatusThrowsException() throws Exception {
        //given
        doThrow(new Exception()).when(service).getStatus();
        //when
        try {
            service.getWeight();
            fail("Expected ScalesException but never throws");
        }catch(ScaleException e){

        }
        //then
        verify(service).getStatus();
        verify(status, never()).isLowWeight();
        verify(status, never()).isWeightStable();
        verify(service, never()).runCommand(GET_WEIGHT_COMMAND);
        verify(service, never()).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, never()).getMeasure();
    }

    @Test
    public void testGetWeightRunCommandThrowsException() throws Exception {
        //given
        doReturn(status).when(service).getStatus();
        doReturn(false).when(status).isLowWeight();
        doReturn(true).when(status).isWeightStable();
        doThrow(new ScaleException("")).when(service).runCommand(anyByte());
        //when
        try {
            service.getWeight();
            fail("Expected ScaleException but never throws");
        } catch(ScaleException e){

        }
        //then
        verify(service).getStatus();
        verify(status).isLowWeight();
        verify(status).isWeightStable();
        verify(service).runCommand(GET_WEIGHT_COMMAND);
        verify(service, never()).parseWeight(CORRECT_STATUS_GRAM);
        verify(status, never()).getMeasure();
    }

}
