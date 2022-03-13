package ru.crystals.pos.bank.ucs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.exceptions.LastLineAttributeNotReceivedException;
import ru.crystals.pos.bank.ucs.exceptions.RequiresLoginFirstException;
import ru.crystals.pos.bank.ucs.messages.requests.GetTransactionDetailsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.LoginRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.responses.InitialErrorResponse;
import ru.crystals.pos.bank.ucs.messages.responses.LoginResponse;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;
import ru.crystals.pos.bank.ucs.serviceoperations.UCSServiceOperation;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestManagerTest {
    public static final String EXPECTED_TERMINAL_ID = "1234567891";
    public static final String DEFAULT_TERMINAL_ID = "0000000000";
    @Mock
    private EftposConnector connector;
    @Mock
    private TerminalMessageListener messageListener;
    @Mock
    private TerminalDelay terminalDelay;
    @Mock
    private ResponseHandler responseHandler;
    @Mock
    private LoginResponse loginResponse;
    @Mock
    private UCSServiceOperation operation;
    @Mock
    private Request request;
    @Spy @InjectMocks
    private RequestManager requestManager = new RequestManager(new TerminalDelay());

    @Before
    public void setUp() throws Exception {
        when(loginResponse.getType()).thenReturn(ResponseType.LOGIN_RESPONSE);
        when(loginResponse.getTerminalId()).thenReturn(EXPECTED_TERMINAL_ID);
    }

    @Test
    public void shouldWaitForTerminalAndMakeLogin() throws Exception {

        // given
        when(responseHandler.waitForAnyResponse(anyLong())).thenReturn(loginResponse);

        when(terminalDelay.isAvailable()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);

        requestManager.setTerminalID(EXPECTED_TERMINAL_ID);

        doNothing().when(terminalDelay).successfulLogin();

        // when
        requestManager.login();
        String terminalIdFromTerminal = requestManager.getTerminalID();

        // then
        verifyMessagesOrder(new LoginRequest(EXPECTED_TERMINAL_ID));
        assertThat(terminalIdFromTerminal).isNotNull();
        assertThat(terminalIdFromTerminal).isEqualTo(EXPECTED_TERMINAL_ID);
    }

    private void verifyMessagesOrder(Request request) throws CashException {
        InOrder inOrder = inOrder(connector);
        inOrder.verify(connector).openSession();
        inOrder.verify(connector).sendRequest(request);
        inOrder.verify(connector).setTerminalId(EXPECTED_TERMINAL_ID);
        inOrder.verify(connector).closeSession();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testMakeTransaction() throws Exception {

        //given
        AuthorizationData authorizationData = new AuthorizationData();
        when(responseHandler.getAuthorizationData()).thenReturn(authorizationData);
        Request request = mock(Request.class);

        //when
        AuthorizationData resultAuthorizationData = requestManager.makeTransaction(request);

        //then
        assertThat(resultAuthorizationData).isEqualTo(authorizationData);
        verify(responseHandler).getAuthorizationData();
    }

    @Test
    public void testMakeTransactionWithRequiresLoginFirstResponse() throws Exception {

        //given
        AuthorizationData authorizationData = new AuthorizationData();
        when(responseHandler.getAuthorizationData()).thenThrow(new RequiresLoginFirstException()).thenReturn(authorizationData);

        when(responseHandler.waitForAnyResponse(anyLong())).thenReturn(loginResponse);

        when(terminalDelay.isAvailable()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);

        requestManager.setTerminalID(EXPECTED_TERMINAL_ID);

        Request request = mock(Request.class);

        //when
        AuthorizationData resultAuthorizationData = requestManager.makeTransaction(request);

        //then
        assertThat(resultAuthorizationData).isEqualTo(authorizationData);
        verify(responseHandler, times(2)).getAuthorizationData();
    }

    @Test
    public void testMakeTransactionWithoutLastLineInSlip() throws BankException {

        //given
        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setRefNumber("12345");
        ArrayList<List<String>> slips = new ArrayList<>();
        List<String> slip = new ArrayList<>();
        slips.add(slip);
        authorizationData.setSlips(slips);
        when(responseHandler.getAuthorizationData()).thenThrow(new LastLineAttributeNotReceivedException(authorizationData));

        Request request = mock(Request.class);

        //when
        AuthorizationData resultAuthorizationData = requestManager.makeTransaction(request);

        //then
        assertThat(resultAuthorizationData).isEqualTo(authorizationData);
        verify(responseHandler, times(2)).getAuthorizationData();
        verify(connector).sendRequest(new GetTransactionDetailsRequest(authorizationData.getRefNumber()));
    }

    @Test
    public void shouldMakeLoginWithDefaultTerminalId() throws Exception {

        // given
        when(terminalDelay.isAvailable()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(terminalDelay.getRemainTimeToAvailable()).thenReturn(3000L);

        when(responseHandler.waitForAnyResponse(anyLong())).thenReturn(loginResponse);

        // when
        requestManager.login();
        String terminalIdFromTerminal = requestManager.getTerminalID();

        // then
        verify(terminalDelay).successfulLogin();
        verifyMessagesOrder(new LoginRequest(null));
        assertThat(terminalIdFromTerminal).isEqualTo(EXPECTED_TERMINAL_ID);
    }

    @Test
    public void canNotLoginRequestError() throws Exception {

        //given
        InitialErrorResponse initialErrorResponse = mock(InitialErrorResponse.class);
        when(initialErrorResponse.getType()).thenReturn(ResponseType.INITIAL_ERROR_RESPONSE);
        when(initialErrorResponse.getCombinedMessage()).thenReturn("ERROR PARSING PREVIOUS REQUEST (01)");
        when(initialErrorResponse.getTerminalId()).thenReturn(EXPECTED_TERMINAL_ID);

        when(responseHandler.waitForAnyResponse(anyLong())).thenReturn(initialErrorResponse);

        //when
        BankException caughtException = null;
        try {
            requestManager.login();
        } catch (BankException e) {
            caughtException = e;
        }

        //then
        assert caughtException != null;
        assertThat(caughtException.getMessage()).isEqualTo("ERROR PARSING PREVIOUS REQUEST (01)");
        verifyMessagesOrder(new LoginRequest(null));
    }

    @Test
    public void testDailyLog() throws Exception {

        //given
        DailyLogData dailyLogData = new DailyLogData();
        when(responseHandler.getDailyLog()).thenReturn(dailyLogData);

        //when
        DailyLogData resultDailyLogData = requestManager.dailyLog();

        //then
        assertThat(resultDailyLogData).isEqualTo(dailyLogData);
        verify(responseHandler).getDailyLog();
    }

    @Test
    public void testProcessServiceOperation() throws BankException {

        // given
        when(operation.createRequest()).thenReturn(request);
        when(operation.hasInitialResponse()).thenReturn(true);

        List<List<String>> slips = new ArrayList<>();
        List<String> slip = new ArrayList<>();
        slip.add("Test");
        slips.add(slip);

        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setSlips(slips);
        doReturn(authorizationData).when(requestManager).makeTransaction(request);

        // when
        List<String> document = requestManager.processServiceOperation(operation);

        // then
        assertThat(document.containsAll(slip)).isTrue();
    }
}
