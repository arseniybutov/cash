package ru.crystals.pos.bank.ucs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.exceptions.LastLineAttributeNotReceivedException;
import ru.crystals.pos.bank.ucs.exceptions.RequiresLoginFirstException;
import ru.crystals.pos.bank.ucs.messages.requests.FinalizeDayTotalsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.GetTransactionDetailsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.LoginRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.responses.InitialErrorResponse;
import ru.crystals.pos.bank.ucs.messages.responses.Response;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;
import ru.crystals.pos.bank.ucs.serviceoperations.UCSServiceOperation;

import java.util.ArrayList;
import java.util.List;

public class RequestManager {
    private static Logger log = LoggerFactory.getLogger(RequestManager.class);
    private String terminalID;
    private EftposConnector connector;
    private TerminalDelay terminalDelay;
    private ResponseHandler responseHandler = new ResponseHandler();
    private TerminalMessageListener messageListener;
    private Timeouts timeouts = new Timeouts();

    public RequestManager(TerminalDelay terminalDelay) {
        this.terminalDelay = terminalDelay;
        responseHandler.setConnector(connector);
        responseHandler.setTerminalMessageListener(messageListener);
        responseHandler.setTerminalDelay(terminalDelay);
    }

    public void login() throws BankException {
        try {
            connector.openSession();
            LoginRequest loginRequest = new LoginRequest(terminalID);
            waitForTerminalReadyWithMessage(ResBundleBankUcs.getString("REGISTRATION_IS_IN_PROGRESS") + ", " + ResBundleBankUcs.getString("WAIT_FOR"),
                    "");
            messageListener.showCustomMessage(ResBundleBankUcs.getString("REGISTRATION_IS_IN_PROGRESS"));
            connector.sendRequest(loginRequest);
            Response response = responseHandler.waitForAnyResponse(timeouts.getCommonResponseTimeout());
            if (terminalID == null) {
                terminalID = response.getTerminalId();
            }
            connector.setTerminalId(terminalID);
            if (response.getType() == ResponseType.INITIAL_ERROR_RESPONSE) {
                terminalDelay.unsuccessful();
                throw new BankConfigException(((InitialErrorResponse) response).getCombinedMessage());
            }
            if (response.getType() == ResponseType.LOGIN_RESPONSE) {
                terminalDelay.successfulLogin();
            }
        } finally {
            connector.closeSession();
        }
    }

    void sendRequest(Request request) throws BankException {
        checkTerminalIsReady();
        messageListener.showCustomMessage(ResBundleBankUcs.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
        connector.sendRequest(request);
    }

    private void checkTerminalIsReady() {
        waitForTerminalReadyWithMessage(ResBundleBankUcs.getString("WAITING_FOR_TERMINAL_READY"),
                ResBundleBankUcs.getString("WAITING_FOR_TERMINAL_READY_IN") + " ");
    }

    private void waitForTerminalReadyWithMessage(String line1, String line2) {
        if (!terminalDelay.isAvailable()) {
            log.debug("Waiting for terminal ready in {}", terminalDelay.getRemainTimeToAvailable());
            if (terminalDelay.getRemainTimeToAvailable() > 2000) {
                int seconds;
                while (!terminalDelay.isAvailable() && (seconds = (int) (terminalDelay.getRemainTimeToAvailable() / 1000)) > 0) {
                    messageListener.showCustomMessage(line1 + "\n" + line2 + seconds + " " +
                            ResBundleBankUcs.getString("WAITING_FOR_TERMINAL_READY_SEC"));
                    waitForTimeout(1000);
                }
            } else {
                messageListener.showCustomMessage(line1);
            }
            terminalDelay.waitForTerminal();
        }
    }

    private void waitForTimeout(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTerminalMessageListener(TerminalMessageListener messageListener) {
        this.messageListener = messageListener;
        responseHandler.setTerminalMessageListener(messageListener);
    }

    public void setConnector(EftposConnector connector) {
        this.connector = connector;
        responseHandler.setConnector(connector);
    }

    public DailyLogData dailyLog() throws BankException {
        try {
            connector.openSession();
            FinalizeDayTotalsRequest request = new FinalizeDayTotalsRequest();

            sendRequest(request);
            return responseHandler.getDailyLog();
        } finally {
            connector.closeSession();
        }
    }

    public AuthorizationData makeTransaction(Request request) throws BankException {
        try {
            connector.openSession();
            AuthorizationData authorizationData;
            sendRequest(request);
            try {
                authorizationData = getAuthorizationDataWithSlips();
            } catch (RequiresLoginFirstException ignored) {
                log.info("Operation requires login first");
                connector.closeSession();
                login();
                connector.openSession();
                sendRequest(request);
                authorizationData = getAuthorizationDataWithSlips();
            }
            messageListener.showCustomMessage(ResBundleBankUcs.getString("PRINTING_RECEIPT"));
            return authorizationData;
        } finally {
            connector.closeSession();
        }
    }

    private AuthorizationData getAuthorizationDataWithSlips() throws BankException {
        try {
            return responseHandler.getAuthorizationData();
        } catch (LastLineAttributeNotReceivedException e) {
            terminalDelay.successful();
            log.debug("Error during receiving slips");
            log.debug("Receiving slips copy");
            return tryGetSlipsCopy(e.getAuthorizationData());
        }
    }

    private AuthorizationData tryGetSlipsCopy(AuthorizationData authorizationData) {
        AuthorizationData newAuthorizationData = null;
        String firstWarningLine = ResBundleBankUcs.getString("RECEIVED_SLIP_IS_INCOMPLETE");
        List<String> slip = new ArrayList<>();
        String emptyWarningLine = "";
        slip.add(emptyWarningLine);
        slip.add(firstWarningLine);
        String thirdWarningLine = ResBundleBankUcs.getString("SLIP_DUPLICATE_HAS_PERFORMED");

        try {
            connector.openSession();
            sendRequest(new GetTransactionDetailsRequest(authorizationData.getRefNumber()));
            newAuthorizationData = responseHandler.getAuthorizationData();
        } catch (LastLineAttributeNotReceivedException e) {
            newAuthorizationData = e.getAuthorizationData();
        } catch (BankException e) {
            log.debug("Unable to get slips copy", e);
            thirdWarningLine = ResBundleBankUcs.getString("REQUEST_SLIP_COPY");
        } finally {
            connector.closeSession();
        }
        slip.add(thirdWarningLine);
        slip.add(emptyWarningLine);

        if (authorizationData.getSlips() == null || authorizationData.getSlips().isEmpty()) {
            List<List<String>> slips = new ArrayList<>();
            slips.add(slip);
            authorizationData.setSlips(slips);
        } else {
            authorizationData.getSlips().get(authorizationData.getSlips().size() - 1).addAll(slip);
        }
        if (newAuthorizationData != null && newAuthorizationData.getSlips() != null) {
            if (!authorizationData.getSlips().isEmpty()) {
                authorizationData.getSlips().get(authorizationData.getSlips().size() - 1).addAll(newAuthorizationData.getSlips().get(0));
            }
            if (authorizationData.getSlips().size() == 1) {
                authorizationData.getSlips().addAll(newAuthorizationData.getSlips());
            }
        }
        return authorizationData;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public List<String> processServiceOperation(UCSServiceOperation operation) throws BankException {
        log.debug("Executing service operation {}", operation.getCommandTitle());
        Request request = operation.createRequest();
        List<String> result;
        if (operation.hasInitialResponse()) {
            AuthorizationData authorizationData = makeTransaction(request);
            result = authorizationData.getSlips().get(0);
        } else {
            try {
                connector.openSession();
                sendRequest(request);
                result = responseHandler.readSlips().get(0);
            } finally {
                connector.closeSession();
            }
        }
        return result;
    }
}