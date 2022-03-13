package ru.crystals.pos.bank.bpc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.pos.utils.Timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BPCConnectorTest {
    private static final String PS_PROCESS = "ps";
    private static final String PS_TEST = "ps test";
    public static final int TIMEOUT = 120000;
    @Mock
    private TCPPortAdapter portAdapter;
    @Mock
    private BufferedReader input;
    @Mock
    private Process proc;
    @Mock
    private Timer timer;
    @Spy @InjectMocks
    private BPCConnector connector = new BPCConnector();

    @Before
    public void setUp() throws IOException, PortAdapterException, InterruptedException {
        doReturn(true).when(timer).isNotExpired();
        doReturn(null).when(timer).restart(anyLong());
        doNothing().when(connector).waitFor(anyLong());
        doNothing().when(portAdapter).openPort();
        doNothing().when(portAdapter).write((byte[]) any());
        doReturn(1).doReturn(5).doReturn(5).when(portAdapter).getInputStreamBufferSize();
    }

    @Test
    public void testConnectOnWindows() throws BankCommunicationException {

        //given
        doReturn(true).when(connector).isOsWindows();

        //when
        connector.connect();

        //then
        verify(portAdapter).setLogger(any(Logger.class));
        verify(portAdapter).setTcpAddress(anyString());
        verify(portAdapter).setTcpPort(anyInt());
        verify(connector, never()).checkProcessIsStarted(anyString());
    }

    @Test
    public void testConnectOnLinuxProcessIsStarted() throws BankCommunicationException {

        //given
        doReturn(false).when(connector).isOsWindows();
        doReturn(true).when(connector).checkProcessIsStarted(anyString());

        //when
        connector.connect();

        //then
        verify(portAdapter).setLogger(any(Logger.class));
        verify(portAdapter).setTcpAddress(anyString());
        verify(portAdapter).setTcpPort(anyInt());
        verify(connector, times(2)).checkProcessIsStarted(anyString());
        verify(connector).startProcessWithTimeout(anyString());
    }

    @Test
    public void testConnectOnLinuxProcessIsNotStarted() throws BankCommunicationException {

        //given
        doReturn(false).doReturn(true).when(connector).checkProcessIsStarted(anyString());
        doReturn(false).when(connector).isOsWindows();
        doNothing().when(connector).startProcessWithTimeout(anyString());

        //when
        connector.connect();

        //then
        verify(portAdapter).setLogger(any(Logger.class));
        verify(portAdapter).setTcpAddress(anyString());
        verify(portAdapter).setTcpPort(anyInt());
        verify(connector, times(2)).checkProcessIsStarted(anyString());
        verify(connector).startProcessWithTimeout(anyString());
        verify(connector).getFullPathToProcessingExecutable();
    }

    @Test(expected = BankCommunicationException.class)
    public void testConnectFailedOnStartProcess() throws BankCommunicationException {

        //given
        doReturn(false).when(connector).isOsWindows();
        doReturn(false).when(connector).checkProcessIsStarted(anyString());
        doNothing().when(connector).startProcessWithTimeout(anyString());

        //when
        connector.connect();

        //then
        verify(portAdapter).setLogger(any(Logger.class));
        verify(portAdapter).setTcpAddress(anyString());
        verify(portAdapter).setTcpPort(anyInt());
        verify(connector).checkProcessIsStarted(anyString());
        verify(connector).startProcessWithTimeout(anyString());
        verify(connector, times(2)).getFullPathToProcessingExecutable();
    }

    @Test
    public void testCheckProcessStarted() throws IOException {
        //given
        doReturn(input).when(connector).getInputStreamFromProcess(PS_PROCESS);
        doReturn(PS_TEST).when(input).readLine();

        //when
        boolean result = connector.checkProcessIsStarted(PS_PROCESS);

        //then
        verify(input).close();
        assertThat(result).isTrue();
    }

    @Test
    public void testCheckProcessNotStarted() throws IOException {
        //given
        doReturn(input).when(connector).getInputStreamFromProcess(PS_PROCESS);
        doReturn(null).when(input).readLine();

        //when
        boolean result = connector.checkProcessIsStarted(PS_PROCESS);

        //then
        verify(input).close();
        assertThat(result).isFalse();
    }

    @Test
    public void testCheckProcessNotStartedExceptionWhileGettingStream() throws IOException {
        //given
        doThrow(new IOException()).when(connector).getInputStreamFromProcess(PS_PROCESS);

        //when
        boolean result = connector.checkProcessIsStarted(PS_PROCESS);

        //then
        verify(input, never()).close();
        assertThat(result).isFalse();
    }

    @Test
    public void testMakeTransaction() throws Exception {
        //given
        doReturn(1).when(portAdapter).read((byte[]) any());
        doReturn(new HashMap<Integer, DataByte>()).when(connector).parse((byte[]) any());

        //when
        connector.makeTransaction(new Request());

        //then
        verify(connector).parse((byte[]) any());
        verify(portAdapter, times(2)).getInputStreamBufferSize();
        verify(portAdapter).close();
    }

    @Test
    public void testMakeTransactionWithReadDelay() throws Exception {
        //given
        doReturn(0).doReturn(5).doReturn(5).when(portAdapter).getInputStreamBufferSize();
        doReturn(1).when(portAdapter).read((byte[]) any());
        doReturn(new HashMap<Integer, DataByte>()).when(connector).parse((byte[]) any());

        //when
        connector.makeTransaction(new Request());

        //then
        verify(connector).parse((byte[]) any());
        verify(portAdapter, times(3)).getInputStreamBufferSize();
        verify(portAdapter).close();
    }

    @Test(expected = BankException.class)
    public void testMakeTransactionThrowsException() throws Exception {
        //given
        doThrow(new IOException()).when(portAdapter).read((byte[]) any());

        //when
        connector.makeTransaction(new Request());

        //then
        verify(connector, never()).parse((byte[]) any());
        verify(portAdapter, times(2)).getInputStreamBufferSize();
        verify(portAdapter).close();
    }

    @Test
    public void testMakeTransactionTimerExpired() throws Exception {
        //given
        doReturn(false).when(timer).isNotExpired();

        //when
        Map<Integer, DataByte> result = connector.makeTransaction(new Request());

        //then
        verify(portAdapter).openPort();
        verify(portAdapter).write((byte[]) any());
        verify(timer).restart(TIMEOUT);
        verify(connector, never()).parse((byte[]) any());
        verify(portAdapter, never()).getInputStreamBufferSize();
        verify(portAdapter).close();
        assertThat(result).isEmpty();
    }
}
