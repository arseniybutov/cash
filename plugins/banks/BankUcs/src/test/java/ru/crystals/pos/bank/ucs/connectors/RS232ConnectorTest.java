package ru.crystals.pos.bank.ucs.connectors;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.Terminal;
import ru.crystals.pos.bank.ucs.Timeouts;
import ru.crystals.pos.bank.ucs.exceptions.CorruptDataException;
import ru.crystals.pos.bank.ucs.messages.requests.LoginRequest;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RS232ConnectorTest {
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;
    private static final int ACK = 0x06;
    private static final int ENQ = 0x05;
    private static final int DLE = 0x10;
    private static final int NAK = 0x15;
    @Mock
    SerialPortAdapter port;
    InOrder inOrder;
    @InjectMocks
    @Spy
    RS232Connector connector = new RS232Connector();
    private static final long TEST_ACK_TIMEOUT = 200;
    private static final long TEST_WHILE_SLEEP_ACK_TIMEOUT = 0;
    private static final long TEST_DATA_START_RECEIVE_TIMEOUT = 100;
    private static final long TEST_MAX_TIME_BETWEEN_BYTES_TIMEOUT = 50;
    private static final long TEST_EOT_TIMEOUT = 20;
    private static final String ANY_MESSAGE = "любое сообщение123";
    private static final String VALID_MESSAGE = "31012345678908АБВГДЕЯЮ";
    private static final String VALID_5M_MESSAGE = "5M012345678908АБВГДЕЯЮ";
    private static final String VALID_32_MESSAGE = "32012345678908АБВГДЕЯЮ";
    private static final char CR = (char) (0x0A);

    @Before
    public void setUp() throws Exception {
        connector.setPortAdapter(port).setTimeouts(
                new Timeouts().setAckTimeout(TEST_ACK_TIMEOUT).setDataStartReceiveTimeout(TEST_DATA_START_RECEIVE_TIMEOUT)
                        .setMaxTimeBetweenBytes(TEST_MAX_TIME_BETWEEN_BYTES_TIMEOUT).setWhileSleepAckTimeout(TEST_WHILE_SLEEP_ACK_TIMEOUT)
                        .setEOTTimeout(TEST_EOT_TIMEOUT));
        inOrder = inOrder(port);
    }

    @Test
    public void shouldWriteEOTOnFinishRequest() throws Exception {
        connector.finishRequest();

        verify(port).write(EOT);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteACKOnWriteACK() throws Exception {
        connector.writeACK();

        verify(port).write(ACK);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteNAKOnWriteNAK() throws Exception {
        connector.writeNAK();

        verify(port).write(NAK);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForEOT() throws Exception {
        doReturn(EOT).when(port).read();

        connector.waitForEOT();

        verify(port).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForEOTMaxTime() throws Exception {
        doAnswer(returnValueInTimeout(-1, TEST_EOT_TIMEOUT + 10)).when(port).read();

        connector.waitForEOT();

        verify(port).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteDLESTXOnWriteHead() throws Exception {
        connector.writeHead();

        InOrder io = inOrder(port);
        io.verify(port).write(DLE);
        io.verify(port).write(STX);
        io.verifyNoMoreInteractions();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteLRCOnWriteLRC() throws Exception {
        connector.writeLRC(ANY_MESSAGE);

        verify(port).write(calculateLrc(ANY_MESSAGE));
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteDLEETXOnWriteTail() throws Exception {
        connector.writeTail();

        InOrder io = inOrder(port);
        io.verify(port).write(DLE);
        io.verify(port).write(ETX);
        io.verifyNoMoreInteractions();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteMessageOnWriteMessage() throws Exception {
        connector.writeMessage(ANY_MESSAGE);

        verify(port).write(ANY_MESSAGE);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteENQOnWriteENQ() throws Exception {
        connector.writeENQ();

        verify(port).write(ENQ);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldTrueIfACKOnWaitForACK() throws Exception {
        doReturn(ACK).when(port).read();

        boolean ackStatus = connector.waitForACK();

        assertThat(ackStatus).isTrue();
        verify(port).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldFalseIfNAKOnWaitForACK() throws Exception {
        doReturn(NAK).when(port).read();

        boolean ackStatus = connector.waitForACK();

        assertThat(ackStatus).isFalse();
        verify(port).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldFalseIfNoAnswerOnWaitForACK() throws Exception {
        Terminal.doAnswer(returnValueInTimeout(-1, TEST_ACK_TIMEOUT + 10)).when(port).read();

        boolean ackStatus = connector.waitForACK();

        assertThat(ackStatus).isFalse();
        verify(port).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWriteENQAndReturnTrueOnStartRequestIfReadACK() throws Exception {
        doReturn(true).when(connector).waitForACK();

        boolean startRequestResponse = connector.startSession();

        assertThat(startRequestResponse).isTrue();
        InOrder io = inOrder(connector);
        io.verify(connector).writeENQ();
        io.verify(connector).waitForACK();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRepeatWriteENQAndFinishRequestIfNoAckForThreeTimes() throws Exception {
        // given
        doReturn(false).when(connector).waitForACK();

        // when
        boolean startRequestResponse = connector.startSession();

        // then
        assertThat(startRequestResponse).isFalse();
        InOrder io = inOrder(connector);
        io.verify(connector).writeENQ();
        io.verify(connector).waitForACK();
        io.verify(connector).writeENQ();
        io.verify(connector).waitForACK();
        io.verify(connector).writeENQ();
        io.verify(connector).waitForACK();
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSendMessageWithHeadTailLRCAndRead() throws Exception {
        connector.sendMessage(ANY_MESSAGE);

        InOrder io = inOrder(connector);
        io.verify(connector).sendMessage(ANY_MESSAGE);
        io.verify(connector).writeHead();
        io.verify(connector).writeMessage(ANY_MESSAGE);
        io.verify(connector).writeTail();
        io.verify(connector).writeLRC(ANY_MESSAGE);
        // io.verify(connector).calculateLrc(ANY_MESSAGE);
        // io.verifyNoMoreInteractions();
        verify(connector).setPortAdapter(any(SerialPortAdapter.class));
        verify(connector).setTimeouts(any(Timeouts.class));
        verify(connector).calculateLrc(ANY_MESSAGE);
        verify(connector).calculateLrc(ANY_MESSAGE.getBytes());
        verifyNoMoreInteractions(connector);
    }

    @Test
    public void shouldSendMessageWaitForACKAndFinishRequestOnSendRequest() throws Exception {
        doReturn(true).when(connector).waitForACK();
        LoginRequest loginRequest = new LoginRequest();
        connector.sendRequest(loginRequest);

        InOrder io = inOrder(connector);
        io.verify(connector).sendMessage(loginRequest.toString());
        io.verify(connector).waitForACK();
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRepeatSendMessageIfWaitForACKFalseOnSendRequest() throws Exception {
        doReturn(false).doReturn(true).when(connector).waitForACK();
        LoginRequest loginRequest = new LoginRequest();
        connector.sendRequest(loginRequest);

        InOrder io = inOrder(connector);
        io.verify(connector).sendMessage(loginRequest.toString());
        io.verify(connector).waitForACK();
        io.verify(connector).sendMessage(loginRequest.toString());
        io.verify(connector).waitForACK();
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRepeatSendMessageMaxTimesAndFininshRequestIfNoAckOnSendRequest() throws Exception {
        doReturn(false).when(connector).waitForACK();
        LoginRequest loginRequest = new LoginRequest();

        try {
            connector.sendRequest(loginRequest);
            fail("No exception when no ACK received for 3 times");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BankException.class);
            assertThat(e.getMessage()).isEqualTo("No ACK received for 3 times");
        }

        InOrder io = inOrder(connector);
        for (int i = 0; i < 3; i++) {
            io.verify(connector).sendMessage(loginRequest.toString());
            io.verify(connector).waitForACK();
        }
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldWaitForENQAndSendAckAndReturnTrue() throws Exception {
        doAnswer(returnValueInTimeout(-1, 100)).doReturn(ENQ).when(port).read();

        boolean result = connector.waitForENQ(100 + 100);

        assertThat(result).isTrue();
        InOrder io = inOrder(port);
        io.verify(port, VerificationModeFactory.times(2)).read();
        io.verify(port).write(ACK);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldWaitForENQAndReturnFalseIfTimeoutIsExpired() throws Exception {
        doReturn(-1).doAnswer(returnValueInTimeout(-1, 50)).when(port).read();

        boolean result = connector.waitForENQ(50);

        assertThat(result).isFalse();
        verify(port, VerificationModeFactory.times(2)).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForDLESTXAndReturnTrue() throws Exception {
        doReturn(DLE).doReturn(STX).when(port).read();

        boolean result = connector.waitForDLESTX();

        assertThat(result).isTrue();
        verify(port, VerificationModeFactory.times(2)).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForDLESTXAndReturnFalseIfDataStartTimeoutExpired() throws Exception {
        long dataStartReceiveTimeout = connector.getTimeouts().getDataStartReceiveTimeout();
        doReturn(-1).doAnswer(returnValueInTimeout(-1, dataStartReceiveTimeout + 10)).doReturn(DLE).doReturn(STX).when(port).read();

        boolean result = connector.waitForDLESTX();

        assertThat(result).isFalse();
        verify(port, VerificationModeFactory.times(2)).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForDLESTXAndReturnFalseIfNoSTXReceivedAfterDLE() throws Exception {
        doReturn(DLE).doReturn(0).when(port).read();

        boolean result = connector.waitForDLESTX();

        assertThat(result).isFalse();
        verify(port, VerificationModeFactory.times(2)).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitForDLESTXAndReturnFalseIfTimeBetweenBytesTimeoutExpired() throws Exception {
        long maxTimeBetweenBytesTimeout = connector.getTimeouts().getMaxTimeBetweenBytes();
        doReturn(DLE).doAnswer(returnValueInTimeout(-1, maxTimeBetweenBytesTimeout + 10)).doReturn(STX).when(port).read();

        boolean result = connector.waitForDLESTX();

        assertThat(result).isFalse();
        verify(port, VerificationModeFactory.atLeast(2)).read();
        verifyNoMoreInteractions(port);
    }

    @Test
    public void shouldWaitENQWaitDLESTXReadAnswerAndSendACKonWaitAndReadResponse() throws Exception {
        doReturn(true).when(connector).waitForENQ(100);
        doReturn(true).when(connector).waitForDLESTX();
        doReturn(ANY_MESSAGE).when(connector).readAnswer();
        doNothing().when(connector).waitForEOT();

        String result = connector.waitAndReadResponse(100);

        assertThat(result).isEqualTo(ANY_MESSAGE);

        InOrder io = inOrder(connector);
        io.verify(connector).waitForENQ(100);
        io.verify(connector).waitForDLESTX();
        io.verify(connector).readAnswer();
        io.verify(connector).writeACK();
        io.verify(connector).waitForEOT();
        io.verifyNoMoreInteractions();
        verify(connector, VerificationModeFactory.atMost(1)).waitForENQ(100);
        verify(connector, VerificationModeFactory.atMost(1)).waitForDLESTX();
        verify(connector, VerificationModeFactory.atMost(1)).readAnswer();
        verify(connector, VerificationModeFactory.atMost(1)).waitForEOT();
        verify(connector, VerificationModeFactory.atMost(0)).writeNAK();
    }

    @Test
    public void shouldWaitENQAndThrowExceptionIfNoENQonWaitAndReadResponse() throws Exception {
        doReturn(false).when(connector).waitForENQ(100);

        try {
            connector.waitAndReadResponse(100);
            fail("No expected exception");
        } catch (Exception e) {
        }

        InOrder io = inOrder(connector, port);
        io.verify(connector).waitForENQ(100);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRepeatWaitForDLESTXIfNoDLESTXReceived() throws Exception {
        doReturn(true).when(connector).waitForENQ(100);
        doReturn(false).doReturn(true).when(connector).waitForDLESTX();
        doReturn(ANY_MESSAGE).when(connector).readAnswer();
        doNothing().when(connector).waitForEOT();

        String result = connector.waitAndReadResponse(100);

        assertThat(result).isEqualTo(ANY_MESSAGE);

        InOrder io = inOrder(connector, port);
        io.verify(connector).waitForENQ(100);
        io.verify(connector, VerificationModeFactory.times(2)).waitForDLESTX();
        io.verify(connector).readAnswer();
        io.verify(connector).writeACK();
        io.verify(connector).waitForEOT();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldRepeatWaitForDLESTXForMaxTimesIfNoDLESTXReceived() throws Exception {
        doReturn(true).when(connector).waitForENQ(100);
        doReturn(false).when(connector).waitForDLESTX();
        doNothing().when(connector).finishRequest();

        try {
            connector.waitAndReadResponse(100);
            fail("No expected exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BankException.class);
        }

        InOrder io = inOrder(connector, port);
        io.verify(connector).waitForENQ(100);
        io.verify(connector, VerificationModeFactory.times(3)).waitForDLESTX();
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSendNakAndRepeatWaitForDLESTXIfReadAnswerFailed() throws Exception {
        doReturn(true).when(connector).waitForENQ(100);
        doReturn(true).when(connector).waitForDLESTX();
        doNothing().when(connector).waitForEOT();
        doThrow(CorruptDataException.class).doReturn(ANY_MESSAGE).when(connector).readAnswer();

        String result = connector.waitAndReadResponse(100);

        assertThat(result).isEqualTo(ANY_MESSAGE);

        InOrder io = inOrder(connector);
        io.verify(connector).waitForENQ(100);
        io.verify(connector).waitForDLESTX();
        io.verify(connector).readAnswer();
        io.verify(connector).writeNAK();
        io.verify(connector).waitForDLESTX();
        io.verify(connector).readAnswer();
        io.verify(connector).writeACK();
        io.verify(connector).waitForEOT();
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldSendNakAndRepeatWaitForDLESTXForMaxTimesIfReadAnswerFailed() throws Exception {
        doReturn(true).when(connector).waitForENQ(100);
        doReturn(true).when(connector).waitForDLESTX();
        doThrow(CorruptDataException.class).when(connector).readAnswer();
        doNothing().when(connector).finishRequest();

        try {
            connector.waitAndReadResponse(100);
            fail("No expected exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BankCommunicationException.class);
        }

        InOrder io = inOrder(connector);
        io.verify(connector).waitForENQ(100);
        for (int i = 0; i < 3; i++) {
            io.verify(connector).waitForDLESTX();
            io.verify(connector).readAnswer();
            io.verify(connector).writeNAK();
        }
        io.verify(connector).finishRequest();
        io.verifyNoMoreInteractions();
    }

    private int validMessageIndex = 0;

    private Answer<Byte> getValidMessageByteToByte(final String message, final int lrc) {
        return new Answer<Byte>() {
            @Override
            public Byte answer(InvocationOnMock invocation) {
                byte[] data = message.getBytes(Charset.forName("cp1251"));
                Byte result = null;
                if (validMessageIndex < data.length) {
                    result = data[validMessageIndex];
                } else {
                    if (validMessageIndex == data.length) {
                        result = DLE;
                    } else if (validMessageIndex == data.length + 1) {
                        result = ETX;
                    } else {
                        result = (byte) lrc;
                    }
                }
                validMessageIndex++;
                return result;
            }
        };
    }

    private Answer<Byte> getValidMessageByteToByte(final String message) {
        return new Answer<Byte>() {
            @Override
            public Byte answer(InvocationOnMock invocation) {
                byte[] data = message.getBytes(Charset.forName("cp1251"));
                Byte result = null;
                if (validMessageIndex < data.length) {
                    result = data[validMessageIndex];
                } else {
                    if (validMessageIndex == data.length) {
                        result = DLE;
                    } else if (validMessageIndex == data.length + 1) {
                        result = ETX;
                    } else {
                        result = (byte) calculateLrc(new String(data));
                    }
                }
                validMessageIndex++;
                return result;
            }
        };
    }

    @Test
    public void shouldReturnReadDataIfCheckSumAndLengthIsValidOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        int checkSum = calculateLrc(cp1251Bytes.toByteArray());
        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(true).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        doAnswer(getValidMessageByteToByte(VALID_MESSAGE)).when(port).read();

        // when
        String responseMessage = connector.readAnswer();

        // then
        assertThat(responseMessage).isEqualTo(VALID_MESSAGE);
        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReturnReadDataIfCheckSumEqualsToETXOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(true).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), ETX);
        doAnswer(getValidMessageByteToByte(VALID_MESSAGE, ETX)).when(port).read();

        // when
        String responseMessage = connector.readAnswer();

        // then
        assertThat(responseMessage).isEqualTo(VALID_MESSAGE);
        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), ETX);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReturnReadDataIfCheckSumEqualsToDLEOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(true).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), DLE);
        doAnswer(getValidMessageByteToByte(VALID_MESSAGE, DLE)).when(port).read();

        // when
        String responseMessage = connector.readAnswer();

        // then
        assertThat(responseMessage).isEqualTo(VALID_MESSAGE);
        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), DLE);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldThrowExceptionIfCheckSumIsInvalidOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        int checkSum = calculateLrc(cp1251Bytes.toByteArray());

        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(false).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        doAnswer(getValidMessageByteToByte(VALID_MESSAGE)).when(port).read();

        try {
            connector.readAnswer();
            fail("No expected exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CorruptDataException.class);
        }

        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldThrowExceptionAndNoCheckSumVerificationIfLengthIsInvalidOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");

        doReturn(false).when(connector).checkLengthIsCorrect(resultMessage);
        doAnswer(getValidMessageByteToByte(VALID_MESSAGE)).when(port).read();

        try {
            connector.readAnswer();
            fail("No expected exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CorruptDataException.class);
        }

        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReturnReadDataIfLengthIsValidButChecksumIsInvalidFor5MMessageOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_5M_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        int checkSum = calculateLrc(cp1251Bytes.toByteArray());
        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(false).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        doAnswer(getValidMessageByteToByte(VALID_5M_MESSAGE)).when(port).read();

        // when
        String responseMessage = connector.readAnswer();

        // then
        assertThat(responseMessage).isEqualTo(VALID_5M_MESSAGE);
        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_5M_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldReturnReadDataIfLengthIsValidButChecksumIsInvalidFor32MessageOnReadAnswer() throws Exception {
        ByteArrayOutputStream cp1251Bytes = new ByteArrayOutputStream();
        cp1251Bytes.write(VALID_32_MESSAGE.getBytes("cp1251"));
        String resultMessage = cp1251Bytes.toString("cp1251");
        int checkSum = calculateLrc(cp1251Bytes.toByteArray());

        doReturn(true).when(connector).checkLengthIsCorrect(resultMessage);
        doReturn(false).when(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        doAnswer(getValidMessageByteToByte(VALID_32_MESSAGE)).when(port).read();

        // when
        String responseMessage = connector.readAnswer();

        // then
        assertThat(responseMessage).isEqualTo(VALID_32_MESSAGE);
        InOrder io = inOrder(port, connector);
        io.verify(port, VerificationModeFactory.times(VALID_32_MESSAGE.length() + 3)).read();
        io.verify(connector).checkLengthIsCorrect(resultMessage);
        io.verify(connector).checkSumIsValid(cp1251Bytes.toByteArray(), checkSum);
        io.verifyNoMoreInteractions();
    }

    @Test
    public void shouldCorrectCheckSumCalculation() throws Exception {
        Object[][] data = new Object[][] { { "5M001999984510ПРИHИМАЮ emv.cfg", 0xE6 }, { "5M001999984513ПРИHИМАЮ cert00.p12", 0x8A },
                { "320019999845E70|==============================|" + CR + "|          ИНКАССАЦИЯ          |" + CR +
                        "|** 07/11/2013        12:49  **|" + CR +
                        "|                              |" + CR +
                        "|\"ТЕСТ EFTPOS\"                 |" + CR +
                        "|ЛЮБАЯ УЛИЦА 1                 |" + CR +
                        "|ПИТЕР                         |", 0xA5 } };

        for (Object[] dataCase : data) {
            String utf8 = (String) dataCase[0];
            System.out.println(utf8.length() - 14);
            assertEquals("неправильно вычислена контрольная сумма для сообщения " + utf8, dataCase[1],
                    connector.calculateLrc(utf8.getBytes("cp1251")));
            // connector.calculateLrc(((String) dataCase[0]).getBytes("cp1251")));
        }
    }

    @Test
    public void shouldReturnTrueIfLengthIsValidOnLengthVerification() throws Exception {
        String[][] data = new String[][] { { "31123456789000", "пакет без данных" }, { "311234567890011", "длина 1" },
                { "3112345678900A" + StringUtils.rightPad("Z", 0x0A, "X"), "длина 0A" },
                { "5X00199998451EE3Превышен таймаут ввода карты", "длина 1E" },
                { "311234567890FF" + StringUtils.rightPad("Z", 0xFF, "X"), "максимальная длина FF" } };
        //
        // 09.11 19:38:32 DEBUG [Bank] <- STX
        // 09.11 19:38:32 DEBUG [Bank] <- 5X00199998451EE3Превышен таймаут ввода карты
        // 09.11 19:38:32 DEBUG [Bank] <- DLE
        for (String[] dataCase : data) {
            assertTrue("ошибочный FALSE для ситуации, когда " + dataCase[1], connector.checkLengthIsCorrect(dataCase[0]));
        }
    }

    @Test
    public void shouldReturnFalseIfCheckSumIsInvalidOnCheckSumVerification() throws Exception {
        String validMessage = "31123456789011";

        boolean result = connector.checkSumIsValid(validMessage.getBytes(), calculateLrc(validMessage) + 1);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnFalseIfLengthIsInvalidOnLengthVerification() throws Exception {

        String[][] data = new String[][] { { "3112345678900111", "фактическая длина (2) больше заявленной (1)" },
                { "311234567890001", "фактическая длина (1) больше заявленной (0)" },
                { "31123456789001", "фактическая длина (0) меньше заявленной (1)" },
                { "311234567890021", "фактическая длина (1) меньше заявленной (2)" }, { "3112345678900", "меньше минимальной длины" },
                { "311234567890GG", "значение длины не является hex-числом" },
                { "311234567890FF" + StringUtils.rightPad("Z", 0xFF + 1, "X"), "максимальная длина FF, но данных больше на 1 байт" },
                { "0", "меньше минимальной длины" }, { "", "пустое сообщение" }, { null, "пустое сообщение" }, };

        for (String[] dataCase : data) {
            assertFalse("ошибочный TRUE для ситуации, когда " + dataCase[1], connector.checkLengthIsCorrect(dataCase[0]));
        }
    }

    protected int calculateLrc(String expectedTerminalId) {
        if (expectedTerminalId == null) {
            return 0;
        }
        return calculateLrc(expectedTerminalId.getBytes());
    }

    protected int calculateLrc(byte[] bytes) {
        int lrc = 0;
        for (byte b : bytes) {
            lrc ^= b & 0xFF;
        }
        return lrc;
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
}
