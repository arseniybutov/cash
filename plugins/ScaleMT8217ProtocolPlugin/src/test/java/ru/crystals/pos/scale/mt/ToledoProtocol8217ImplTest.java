package ru.crystals.pos.scale.mt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.Timer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by agaydenger on 15.02.17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ToledoProtocol8217ImplTest {
    private static final int READ_TIMEOUT = 220;
    private static final byte GET_WEIGHT_COMMAND = 0x57;
    private static final byte GET_STATUS_COMMAND = 0x5A;
    private static final byte END_PACKAGE_CODE = 0x0D;
    private static final byte STX = 0x02;
    private static final int STATUS_COMMAND_ANSWER_LENGTH = 2;

    @Mock
    private SerialPortAdapter serialPortAdapter;

    @Mock
    private Timer receiveAnswerTimer;

    @Spy
    private ToledoProtocol8217Impl service = new ToledoProtocol8217Impl();

    @Before
    public void beforeTest() {
        service.setSerialPortAdapter(serialPortAdapter);
        service.setReceiveAnswerTimer(receiveAnswerTimer);
        doReturn(serialPortAdapter).when(serialPortAdapter).setLogger(any(Logger.class));
    }

    @Test
    public void testStart() throws Exception {
        //given

        //when
        service.start();
        //then
        verify(serialPortAdapter).setLogger(any(Logger.class));
        verify(serialPortAdapter).openPort();
    }

    @Test
    public void testStartThrowsCashException() throws Exception {
        //given
        doThrow(new IOException()).when(serialPortAdapter).openPort();
        //when
        try {
            service.start();
            fail("Expected CashException but never throws");
        } catch (CashException e) {
            //then
            verify(serialPortAdapter).setLogger(any(Logger.class));
            verify(serialPortAdapter).openPort();
        }
    }

    @Test
    public void testStartThrowsCashException2() throws Exception {
        //given
        doThrow(new PortAdapterException("")).when(serialPortAdapter).openPort();
        //when
        try {
            service.start();
            fail("Expected CashException but never throws");
        } catch (CashException e) {
            //then
            verify(serialPortAdapter).setLogger(any(Logger.class));
            verify(serialPortAdapter).openPort();
        }
    }

    @Test
    public void testModuleCheckState() throws Exception {
        //given
        ArrayList<Byte> list = new ArrayList<>();
        doReturn(list).when(service).runCommand(GET_STATUS_COMMAND);
        //when
        Boolean result = service.moduleCheckState();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    public void testModuleCheckStateScaleExceptionIgnored() throws Exception {
        //given
        doThrow(new ScaleException()).when(service).runCommand(GET_STATUS_COMMAND);
        //when
        Boolean result = service.moduleCheckState();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    public void testModuleCheckStateReturnFalse() throws Exception {
        //given
        doThrow(new Exception()).when(service).runCommand(GET_STATUS_COMMAND);
        //when
        Boolean result = service.moduleCheckState();
        //then
        verify(service).runCommand(GET_STATUS_COMMAND);
        assertThat(result).isNotNull().isFalse();
    }

    @Test
    public void testRunCommand() throws Exception {
        //given
        doReturn(true).when(receiveAnswerTimer).isNotExpired();
        doReturn(true).when(serialPortAdapter).isDataAvailable();
        doReturn(1).doReturn(2).doReturn(3).doReturn(4).doReturn(5).doReturn(6).doReturn((int) END_PACKAGE_CODE).when(serialPortAdapter).read();
        //when
        List<Byte> result = service.runCommand(GET_WEIGHT_COMMAND);
        //then
        verify(serialPortAdapter).write(GET_WEIGHT_COMMAND);
        verify(receiveAnswerTimer).restart();
        verify(receiveAnswerTimer).isNotExpired();
        verify(serialPortAdapter).isDataAvailable();
        verify(serialPortAdapter, times(7)).read();
        verifyNoMoreInteractions(serialPortAdapter, receiveAnswerTimer);
        assertThat(result).isNotNull().isNotEmpty().containsOnly(Byte.valueOf("3"), Byte.valueOf("4"), Byte.valueOf("5"), Byte.valueOf("6"));
    }

    @Test
    public void testRunCommandNoEndByteReceived() throws Exception {
        //given
        doReturn(true).doReturn(false).when(receiveAnswerTimer).isNotExpired();
        doReturn(true).when(serialPortAdapter).isDataAvailable();
        doReturn(1).doReturn(2).doReturn(3).doReturn(4).doReturn(5).doReturn(6).doReturn(-1).when(serialPortAdapter).read();
        //when
        try {
            service.runCommand(GET_WEIGHT_COMMAND);
            fail("Expected ScaleException but never throws");
        } catch (ScaleException e) {
            //then
            verify(serialPortAdapter).write(GET_WEIGHT_COMMAND);
            verify(receiveAnswerTimer).restart();
            verify(receiveAnswerTimer, times(2)).isNotExpired();
            verify(serialPortAdapter).isDataAvailable();
            verify(serialPortAdapter, times(7)).read();
            verifyNoMoreInteractions(serialPortAdapter, receiveAnswerTimer);
        }
    }

    @Test
    public void testRunCommandNoStartByteReceived() throws Exception {
        //given
        doReturn(true).when(receiveAnswerTimer).isNotExpired();
        doReturn(true).when(serialPortAdapter).isDataAvailable();
        doReturn(1).doReturn(3).doReturn(4).doReturn(5).doReturn(6).doReturn((int) END_PACKAGE_CODE).when(serialPortAdapter).read();
        //when
        List<Byte> result = service.runCommand(GET_WEIGHT_COMMAND);
        //then
        verify(serialPortAdapter).write(GET_WEIGHT_COMMAND);
        verify(receiveAnswerTimer).restart();
        verify(receiveAnswerTimer).isNotExpired();
        verify(serialPortAdapter).isDataAvailable();
        verify(serialPortAdapter, times(6)).read();
        verifyNoMoreInteractions(serialPortAdapter, receiveAnswerTimer);
        assertThat(result).isNotNull().isEmpty();
    }
}