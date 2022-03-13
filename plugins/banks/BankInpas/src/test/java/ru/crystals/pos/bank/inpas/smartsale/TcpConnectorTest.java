package ru.crystals.pos.bank.inpas.smartsale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TcpConnectorTest {
    @Mock
    TCPPortAdapter port;

    @Spy
    @InjectMocks
    private TCPConnector tcpConnector;

    public static final long EXECUTION_STATUS_SUCCESS = 0L;
    public static final long EXECUTION_STATUS_NO_DATA = 1L;
    public static final long EXECUTION_STATUS_FAIL = 2L;
    public static final byte COMMAND_MODE_TO_TERMINAL = 1;
    public static final byte COMMAND_MODE_TO_HOST = 0;
    public static final byte COMMAND_MODE_OPEN_CONNECTION = 1;
    public static final long COMMAND_MODE2_SERVER_CONNECTION = 16L;
    public static final long COMMAND_MODE_2_SEND_DATA = 17L;

    @Test
    public void testProcessServerConnectionConnect() throws Exception {
        FieldCollection fc = new FieldCollection();
        fc.setTcpParams(("214.25.135.63;7050" + (char) 0).getBytes());
        doReturn(true).when(tcpConnector).startSession();
        doNothing().when(tcpConnector).setConnectionParams(any(InetSocketAddress.class));

        testProcessServerConnection(fc, (byte) 1);

        verify(tcpConnector).setConnectionParams(new InetSocketAddress("214.25.135.63", 7050));
        verify(tcpConnector).startSession();
    }

    @Test
    public void testProcessServerConnectionDisconnect() throws Exception {
        doNothing().when(tcpConnector).close();

        testProcessServerConnection(new FieldCollection(), (byte) 0);

        verify(tcpConnector).close();
    }

    private void testProcessServerConnection(FieldCollection fc, byte commandMode) throws Exception {
        tcpConnector.setDataToFill(new FieldCollection());
        fc.setCommandMode(commandMode);
        fc.setCommandMode2(COMMAND_MODE2_SERVER_CONNECTION);

        tcpConnector.processServerConnection(fc);

        assertThat(tcpConnector.getDataToFill().getCommandExecutionStatus()).isEqualTo(EXECUTION_STATUS_SUCCESS);
    }

    @Test
    public void testFillData16() throws Exception {
        FieldCollection fieldCollection = new FieldCollection();
        fieldCollection.setCommandMode2(COMMAND_MODE2_SERVER_CONNECTION);
        doNothing().when(tcpConnector).processServerConnection(fieldCollection);
        FieldCollection data = new FieldCollection();

        tcpConnector.fillData(fieldCollection, data);

        verify(tcpConnector).processServerConnection(fieldCollection);
        assertThat(data).isEqualTo(tcpConnector.getDataToFill());
    }

    @Test
    public void testFillData17() throws Exception {
        FieldCollection fieldCollection = new FieldCollection();
        fieldCollection.setCommandMode2(COMMAND_MODE_2_SEND_DATA);
        doNothing().when(tcpConnector).processDataSend(fieldCollection);
        FieldCollection data = new FieldCollection();

        tcpConnector.fillData(fieldCollection, data);

        verify(tcpConnector).processDataSend(fieldCollection);
        assertThat(data).isEqualTo(tcpConnector.getDataToFill());
        assertThat(tcpConnector.getDataToFill().getCommandMode2()).isEqualTo(COMMAND_MODE_2_SEND_DATA);
    }

    @Test(expected = BankException.class)
    public void testFillDataException() throws Exception {
        FieldCollection fieldCollection = new FieldCollection();
        fieldCollection.setCommandMode2(COMMAND_MODE_2_SEND_DATA);
        doThrow(new IOException()).when(tcpConnector).processDataSend(fieldCollection);

        tcpConnector.fillData(fieldCollection, new FieldCollection());

        verify(tcpConnector).close();
    }

    @Test
    public void testProcessDataSendToTerminalNoAnswer() throws Exception {
        FieldCollection dataToFill = mock(FieldCollection.class);
        tcpConnector.setDataToFill(dataToFill);
        FieldCollection fc = new FieldCollection();
        fc.setCommandMode(COMMAND_MODE_TO_TERMINAL);

        tcpConnector.processDataSend(fc);

        verify(dataToFill).setCommandExecutionStatus(EXECUTION_STATUS_NO_DATA);
    }

    @Test
    public void testProcessDataSendToTerminal() throws Exception {
        FieldCollection dataToFill = mock(FieldCollection.class);
        tcpConnector.setDataToFill(dataToFill);
        FieldCollection fc = new FieldCollection();
        fc.setCommandMode(COMMAND_MODE_TO_TERMINAL);
        byte[] answer = new byte[]{ 1 };
        doNothing().when(tcpConnector).sendBytes(any(byte[].class));
        tcpConnector.setAnswer(answer);

        tcpConnector.processDataSend(fc);

        verify(dataToFill).setCommandExecutionStatus(EXECUTION_STATUS_SUCCESS);
        verify(dataToFill).setTcpParams(answer);
    }

    @Test
    public void testProcessDataSendToHost() throws Exception {
        FieldCollection dataToFill = mock(FieldCollection.class);
        tcpConnector.setDataToFill(dataToFill);
        FieldCollection fc = new FieldCollection();
        fc.setCommandMode(COMMAND_MODE_TO_HOST);
        fc.setTcpParams(new byte[]{ 1 });
        doNothing().when(tcpConnector).sendBytes(fc.getTcpParams());

        tcpConnector.processDataSend(fc);

        verify(dataToFill).setCommandExecutionStatus(EXECUTION_STATUS_SUCCESS);
        verify(tcpConnector).sendBytes(fc.getTcpParams());
        verify(tcpConnector).setWaiting(true);
    }

    @Test
    public void testListener() throws Exception {
        tcpConnector.setWaiting(true);
        byte[] answer = new byte[]{ 1 };
        doReturn(answer).when(tcpConnector).readPacket();

        doAnswer(new Answer() {
            @Override public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                tcpConnector.getFuture().cancel(true);
                return null;
            }
        }).when(tcpConnector).readPacket();

        tcpConnector.startSession();
    }
}
