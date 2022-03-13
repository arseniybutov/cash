package ru.crystals.pos.bank.ucs.connectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.ucs.messages.requests.LoginRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TCPConnectorTest {

    TCPPortAdapter port = mock(TCPPortAdapter.class);
    InOrder inOrder;
    @InjectMocks
    @Spy
    TCPConnector connector = new TCPConnector();
    private static final String ANY_MESSAGE = "любое сообщение123";
    private static final String VALID_MESSAGE = "31012345678908АБВГДЕЯЮ";
    private static final String VALID_5M_MESSAGE = "5M012345678908АБВГДЕЯЮ";
    private static final String VALID_32_MESSAGE = "32012345678908АБВГДЕЯЮ";
    private static final char CR = (char) (0x0A);
    private static final String TERMINAL_CHARSET = "cp1251";

    @Before
    public void setUp() throws Exception {
        port = mock(TCPPortAdapter.class);
        connector.setPortAdapter(port);
        // .setTcpPort(40001).setTerminalIP("172.16.5.43");
        inOrder = inOrder(port);
    }

    @Test
    public void shouldWriteMessageOnWriteMessage() throws Exception {
        connector.writeMessage(ANY_MESSAGE);

        verify(port).write(ANY_MESSAGE);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteMessageOnSendRequest() throws Exception {
        Request request = new LoginRequest();

        connector.sendRequest(request);

        verify(connector).setPortAdapter(any(PortAdapter.class));
        verify(connector).sendRequest(request);
        verify(connector).writeMessage(request.toString());
        verifyNoMoreInteractions(connector);
    }

    @Test
    public void shouldReadResponse() throws Exception {
        doReturn(VALID_MESSAGE).when(connector).readMessage();

        String result = connector.waitAndReadResponse(1000L);

        assertThat(result).isEqualTo(VALID_MESSAGE);
        verify(connector).readMessage();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForTimeoutAndReadResponse() throws Exception {
        doReturn(null).doReturn(VALID_MESSAGE).when(connector).readMessage();

        String result = connector.waitAndReadResponse(1000L);

        assertThat(result).isEqualTo(VALID_MESSAGE);
        verify(connector, VerificationModeFactory.times(2)).readMessage();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForMaxTimeoutAndReadResponse() throws Exception {
        long timeoutOnResponse = 200;
        doReturn(null).doAnswer(returnValueInTimeout(null, timeoutOnResponse)).doReturn(VALID_MESSAGE).when(connector).readMessage();

        String result = connector.waitAndReadResponse(timeoutOnResponse + 100);

        assertThat(result).isEqualTo(VALID_MESSAGE);
        verify(connector, VerificationModeFactory.times(3)).readMessage();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForMaxTimeoutAndReturnNullIfNoAnswer() throws Exception {
        long timeoutOnResponse = 200;
        doReturn(null).doAnswer(returnValueInTimeout(null, timeoutOnResponse)).doReturn(VALID_MESSAGE).when(connector).readMessage();

        try {
            String result = connector.waitAndReadResponse(timeoutOnResponse - 100);
            fail("No expected exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BankCommunicationException.class);
        }
        verify(connector, VerificationModeFactory.times(2)).readMessage();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldReadOnlyOneMessageFromSocket() throws Exception {
        String messageFromSocket = VALID_MESSAGE + VALID_32_MESSAGE;
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(messageFromSocket.getBytes("cp1251"));
        doReturn(1).when(port).getInputStreamBufferSize();
        doAnswer(getValidMessageByteToByte(messageFromSocket)).when(port).read();

        // when
        String responseMessage = connector.readMessage();

        // then
        assertThat(responseMessage).isEqualTo(VALID_MESSAGE);
        verify(port, VerificationModeFactory.times(VALID_MESSAGE.length())).getInputStreamBufferSize();
        verify(port, VerificationModeFactory.times(VALID_MESSAGE.length())).read();
    }

    // TODO + тест на то, что при неполном получении сообщения выполняем соответствующие действия

    @Test
    public void shouldCloseConnectionOnEndSession() throws Exception {
        connector.endSession();

        verify(port).close();
        verifyNoMoreInteractions(port);
    }

    private Answer<Integer> returnValueInTimeout(final Integer value, final long timeout) {
        return new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                }
                return value;
            }
        };
    }
    int validMessageIndex = 0;
    private Answer<Byte> getValidMessageByteToByte(final String message) {
        return new Answer<Byte>() {
            @Override
            public Byte answer(InvocationOnMock invocation) {
                byte[] data = message.getBytes(Charset.forName("cp1251"));
                Byte result = null;
                if (validMessageIndex < data.length) {
                    result = data[validMessageIndex];
                }
                validMessageIndex++;
                return result;
            }
        };
    }

//    private Answer<String> returnValueInTimeout(final String value, final long timeout) {
//        return new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) {
//                try {
//                    Thread.sleep(timeout);
//                } catch (InterruptedException e) {
//                }
//                return value;
//            }
//        };
//    }
}
