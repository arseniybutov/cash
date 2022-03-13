package ru.crystals.pos.bank.ucs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.exceptions.LastLineAttributeNotReceivedException;
import ru.crystals.pos.bank.ucs.exceptions.RequiresLoginFirstException;
import ru.crystals.pos.bank.ucs.messages.responses.AuthorizationResponse;
import ru.crystals.pos.bank.ucs.messages.responses.ConsoleMessageResponse;
import ru.crystals.pos.bank.ucs.messages.responses.InitialErrorResponse;
import ru.crystals.pos.bank.ucs.messages.responses.PrintLineResponse;
import ru.crystals.pos.bank.ucs.messages.responses.Response;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;
import ru.crystals.pos.bank.ucs.messages.responses.UnknownResponse;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseHandlerTest {
    @Mock
    private EftposConnector connector;
    @Mock
    private TerminalDelay terminalDelay;
    @Mock
    private TerminalMessageListener messageListener;
    @Mock
    private AuthorizationResponse authorizationResponse;
    @Mock
    private Response initialOkResponse;
    @Mock
    private AuthorizationData authorizationData;
    @Mock
    private Response response;
    @Mock
    private InitialErrorResponse initialErrorResponse;
    @Mock
    private PrintLineResponse printLineResponse;
    @Spy
    @InjectMocks
    private ResponseHandler responseHandler = new ResponseHandler();

    @Before
    public void setUp() throws Exception {
        when(printLineResponse.getType()).thenReturn(ResponseType.PRINT_LINE);
        when(initialOkResponse.getType()).thenReturn(ResponseType.INITIAL_OK_RESPONSE);
        when(authorizationResponse.getType()).thenReturn(ResponseType.AUTHORIZATION_RESPONSE);
        doNothing().when(terminalDelay).successful();
        doNothing().when(terminalDelay).unsuccessful();
        when(initialErrorResponse.getType()).thenReturn(ResponseType.INITIAL_ERROR_RESPONSE);
    }

    @Test
    public void testWaitForPrintLineResponse() throws Exception {

        // given
        when(printLineResponse.getType()).thenReturn(ResponseType.HOLD).thenReturn(ResponseType.PRINT_LINE);
        when(printLineResponse.isLastLine()).thenReturn(false).thenReturn(true);
        doReturn(printLineResponse).when(responseHandler).waitForAnyResponse(anyLong());

        // when
        Slips responses = responseHandler.readSlips();

        // then
        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    public void testWhenWaitForPrintLineResponseReceiveUnexpectedResponse() throws Exception {

        // given
        when(response.getType()).thenReturn(ResponseType.UNKNOWN);
        when(printLineResponse.isLastLine()).thenReturn(true);
        doReturn(response).doReturn(printLineResponse).when(responseHandler).waitForAnyResponse(anyLong());

        // when
        Slips responses = responseHandler.readSlips();

        // then
        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    public void testGetAuthorizationDataWithRequiresLoginFirstResponse() throws Exception {

        // given
        when(response.getType()).thenReturn(ResponseType.INITIAL_REQUIRES_LOGIN_FIRST_RESPONSE);
        doReturn(response).when(responseHandler).waitForAnyResponse(anyLong());

        // when
        try {
            responseHandler.getAuthorizationData();
            fail("No expected exception");
        } catch (RequiresLoginFirstException ignore) {

        }

        // then
        verify(terminalDelay).unsuccessful();
    }

    @Test
    public void testGetAuthorizationDataWithInitialOkResponseAndConsoleMessage() throws Exception {

        // given
        ConsoleMessageResponse consoleMessageResponse = mock(ConsoleMessageResponse.class);
        when(consoleMessageResponse.getType()).thenReturn(ResponseType.CONSOLE_MESSAGE);
        when(consoleMessageResponse.getMessage()).thenReturn("Console message");

        doReturn(initialOkResponse).doReturn(consoleMessageResponse).doReturn(authorizationResponse).when(responseHandler)
                .waitForAnyResponse(anyLong());

        when(authorizationData.isStatus()).thenReturn(true);
        when(authorizationResponse.getAuthorizationData()).thenReturn(authorizationData);

        Slips slips = new Slips();
        slips.setFull(true);
        doReturn(slips).when(responseHandler).readSlips();

        // when
        AuthorizationData resultAuthorizationData = responseHandler.getAuthorizationData();

        // then
        verify(terminalDelay).successful();
        assertThat(resultAuthorizationData.getSlips()).isEqualTo(slips.getSlips());
        verify(messageListener).showCustomMessageFromTerminal("Console message");
        verify(messageListener).showCustomMessage(ResBundleBankUcs.getString("RECEIVING_BANK_SLIP"));
    }

    @Test
    public void testGetAuthorizationDataWithSlipsWithoutLastLine() throws BankException {

        // given
        doReturn(initialOkResponse).doReturn(authorizationResponse).when(responseHandler)
                .waitForAnyResponse(anyLong());

        when(authorizationData.isStatus()).thenReturn(true);
        when(authorizationData.getRefNumber()).thenReturn("12345");
        when(authorizationResponse.getAuthorizationData()).thenReturn(authorizationData);

        Slips slips = new Slips();
        slips.setFull(false);
        doReturn(slips).when(responseHandler).readSlips();

        // when
        try {
            responseHandler.getAuthorizationData();
            fail("No expected exception");
        } catch (LastLineAttributeNotReceivedException e) {
            // then
            assertThat(e.getAuthorizationData().getSlips()).isEqualTo(slips.getSlips());
            verify(messageListener).showCustomMessage(ResBundleBankUcs.getString("RECEIVING_BANK_SLIP"));
        }
    }

    @Test
    public void testGetAuthorizationDataWithHold() throws Exception {
        // given
        Response holdResponse = mock(Response.class);
        when(holdResponse.getType()).thenReturn(ResponseType.HOLD);

        doReturn(initialOkResponse).doReturn(holdResponse).doReturn(authorizationResponse).when(responseHandler).waitForAnyResponse(anyLong());

        when(authorizationData.isStatus()).thenReturn(true);
        when(authorizationResponse.getAuthorizationData()).thenReturn(authorizationData);

        Slips slips = new Slips();
        slips.setFull(true);
        doReturn(slips).when(responseHandler).readSlips();


        // when
        AuthorizationData resultAuthorizationData = responseHandler.getAuthorizationData();

        // then
        verify(terminalDelay).successful();
        assertThat(resultAuthorizationData.getSlips()).isEqualTo(slips.getSlips());
        verify(messageListener).showCustomMessage(ResBundleBankUcs.getString("RECEIVING_BANK_SLIP"));
    }

    @Test
    public void testGetAuthorizationDataWithInitialErrorResponse() throws Exception {
        // given
        when(initialErrorResponse.getCombinedMessage()).thenReturn("combined message");

        doReturn(initialErrorResponse).doReturn(initialOkResponse).doReturn(authorizationResponse).when(responseHandler)
                .waitForAnyResponse(anyLong());

        // when
        try {
            responseHandler.getAuthorizationData();
            fail("No expected exception");
        } catch (BankAuthorizationException e) {
            // then
            assertThat(e.getMessage()).isEqualTo(initialErrorResponse.getCombinedMessage());
            verify(terminalDelay).unsuccessful();
        }

    }

    @Test
    public void testGetAuthorizationDataWrongTerminalStatus() throws Exception {
        // given
        doReturn(initialOkResponse).doReturn(authorizationResponse).when(responseHandler).waitForAnyResponse(anyLong());

        when(authorizationData.getMessage()).thenReturn("Test message");
        when(authorizationData.isStatus()).thenReturn(false);
        when(authorizationResponse.getAuthorizationData()).thenReturn(authorizationData);
        doReturn(new Slips()).when(responseHandler).readSlips();

        List<List<String>> slips = new ArrayList<>();

        // when
        AuthorizationData resultAuthorizationData = responseHandler.getAuthorizationData();

        // then
        verify(terminalDelay).unsuccessful();
        assertThat(resultAuthorizationData.getSlips()).isEqualTo(slips);
        verify(messageListener).showCustomMessage(ResBundleBankUcs.getString("RECEIVING_BANK_SLIP"));
        assertThat(resultAuthorizationData.getMessage()).isEqualTo(resultAuthorizationData.getMessage());
    }

    @Test
    public void testGetDailyLog() throws Exception {

        // given
        Response finalizeDayTotalsResponse = mock(Response.class);
        when(finalizeDayTotalsResponse.getType()).thenReturn(ResponseType.FINALIZE_DAY_TOTALS_RESPONSE);
        doReturn(finalizeDayTotalsResponse).when(responseHandler).waitForAnyResponse(anyLong());
        Slips slips = mock(Slips.class);
        doReturn(slips).when(responseHandler).readSlips();

        // when
        DailyLogData dailyLogData = responseHandler.getDailyLog();

        // then
        verify(terminalDelay).successfulDailyLog();
        assertThat(dailyLogData.getSlip()).isEqualTo(new ArrayList<String>());
    }

    @Test
    public void testGetDailyLogWithInitialErrorResponse() throws Exception {

        // given
        when(initialErrorResponse.getCombinedMessage()).thenReturn("combined message");

        doReturn(initialErrorResponse).when(responseHandler).waitForAnyResponse(anyLong());

        // when
        try {
            responseHandler.getDailyLog();
            fail("No expected exception");
        } catch (BankException e) {
            // then
            verify(terminalDelay).unsuccessful();
            assertThat(e.getMessage()).isEqualTo(initialErrorResponse.getCombinedMessage());
        }

    }

    @Test
    public void testGetAuthorizationDataWithUnexpectedMessage() throws Exception {

        // given
        UnknownResponse unknownResponse = mock(UnknownResponse.class);
        when(unknownResponse.getType()).thenReturn(ResponseType.UNKNOWN);
        doReturn(unknownResponse).when(responseHandler).waitForAnyResponse(anyLong());

        // when
        try {
            responseHandler.getAuthorizationData();
            fail("No expected exception");
        } catch (BankAuthorizationException e) {
            // then
            assertThat(e.getMessage()).isEqualTo(ResBundleBankUcs.getString("TERMINAL_COMMUNICATION_ERROR"));
        }
    }

    @Test
    public void testReadSlips() throws Exception {

        // given
        PrintLineResponse slipDelimiter = mock(PrintLineResponse.class);
        when(slipDelimiter.getType()).thenReturn(ResponseType.PRINT_LINE);
        when(slipDelimiter.isSlipDelimiter()).thenReturn(true);
        List<String> strings = new ArrayList<>();
        strings.add("test string 1");
        strings.add("test string 2");
        when(printLineResponse.getTextLines()).thenReturn(strings);
        when(printLineResponse.isLastLine()).thenReturn(false).thenReturn(true);
        doReturn(printLineResponse).doReturn(slipDelimiter).doReturn(printLineResponse).when(responseHandler).waitForPrintLineResponse();

        // when
        Slips slips = responseHandler.readSlips();

        // then
        assertThat(slips.size()).isEqualTo(2);
        assertThat(slips.get(0).get(0)).isEqualTo("test string 1");
        assertThat(slips.get(0).get(1)).isEqualTo("test string 2");
        assertThat(slips.get(1).get(0)).isEqualTo("test string 1");
        assertThat(slips.get(1).get(1)).isEqualTo("test string 2");
    }
}
