package ru.crystals.pos.bank.ucs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.exceptions.LastLineAttributeNotReceivedException;
import ru.crystals.pos.bank.ucs.exceptions.NoPreviousTransactionWithSuchRefNumberException;
import ru.crystals.pos.bank.ucs.exceptions.RequiresLoginFirstException;
import ru.crystals.pos.bank.ucs.messages.responses.AuthorizationResponse;
import ru.crystals.pos.bank.ucs.messages.responses.ConsoleMessageResponse;
import ru.crystals.pos.bank.ucs.messages.responses.InitialErrorResponse;
import ru.crystals.pos.bank.ucs.messages.responses.PrintLineResponse;
import ru.crystals.pos.bank.ucs.messages.responses.Response;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseFactory;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;

import java.util.ArrayList;
import java.util.List;

public class ResponseHandler {
    private Timeouts timeouts = new Timeouts();
    private EftposConnector connector;
    private TerminalDelay terminalDelay;
    private TerminalMessageListener terminalMessageListener;
    private static Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    public Response waitForPrintLineResponse() throws BankException {
        return waitForAnyResponse(timeouts.getInitialResponseTimeout());
    }

    public Response waitForInitialResponse() throws BankException {
        return waitForAnyResponse(timeouts.getInitialResponseTimeout());
    }

    public Response waitForFinalResponse() throws BankException {
        return waitForAnyResponse(timeouts.getCommonResponseTimeout());
    }

    public Response waitForAnyResponse(long timeOut) throws BankException {
        String responseString = connector.waitAndReadResponse(timeOut);
        Response response = ResponseFactory.parse(responseString);
        log.info("Received response:\n{}", response.toLoggableString());
        return response;
    }

    public AuthorizationData getAuthorizationData() throws BankException {
        receiveInitialResponse();
        return processResponse();
    }

    private void receiveInitialResponse() throws BankException {
        Response response;
        response = waitForInitialResponse();
        switch (response.getType()) {
            case PRINT_LINE:
            case INITIAL_OK_RESPONSE:
                break;
            case INITIAL_REQUIRES_LOGIN_FIRST_RESPONSE:
                terminalDelay.unsuccessful();
                throw new RequiresLoginFirstException();
            case INITIAL_NO_PREVIOUS_TRANSACTION_WITH_SUCH_REF:
                terminalDelay.unsuccessful();
                throw new NoPreviousTransactionWithSuchRefNumberException();
            case INITIAL_ERROR_RESPONSE:
                String combinedMessage = ((InitialErrorResponse) response).getCombinedMessage();
                terminalDelay.unsuccessful();
                throw new BankAuthorizationException(combinedMessage);
            default:
                throw new BankAuthorizationException(ResBundleBankUcs.getString("TERMINAL_COMMUNICATION_ERROR"));
        }
    }

    private AuthorizationData processResponse() throws BankException {
        AuthorizationData authorizationData = null;
        Response response;
        do {
            response = waitForFinalResponse();

            switch (response.getType()) {
                case AUTHORIZATION_RESPONSE:
                    log.debug("Authorization response received");
                    authorizationData = ((AuthorizationResponse) response).getAuthorizationData();
                    if (!authorizationData.isStatus()) {
                        log.debug("Received response has error status {}", authorizationData.getMessage());
                        terminalDelay.unsuccessful();
                    }
                    log.debug("Receiving bank slip");
                    terminalMessageListener.showCustomMessage(ResBundleBankUcs.getString("RECEIVING_BANK_SLIP"));
                    Slips slips = readSlips();
                    authorizationData.setSlips(slips.getSlips());
                    if (!slips.areFull()) {
                        throw new LastLineAttributeNotReceivedException(authorizationData);
                    }
                    terminalDelay.successful();
                    break;
                case CONSOLE_MESSAGE:
                    terminalMessageListener.showCustomMessageFromTerminal(((ConsoleMessageResponse) response).getMessage());
                    log.info("Message from terminal: {}", ((ConsoleMessageResponse) response).getMessage());
                    break;
                case HOLD:
                case PIN_ENTRY_REQUIRED:
                case ONLINE_AUTHORISATION_REQUIRED:
                    break;
                case INITIAL_ERROR_RESPONSE:
                    terminalDelay.unsuccessful();
                    throw new BankAuthorizationException(((InitialErrorResponse) response).getCombinedMessage());
                case UNKNOWN:
                default:
                    log.warn("Unexpected response: {}", response);
                    break;
            }
        } while (response.getType() != ResponseType.AUTHORIZATION_RESPONSE);
        return authorizationData;
    }

    public DailyLogData getDailyLog() throws BankException {
        DailyLogData dailyLogData = null;
        Response response;
        do {
            response = waitForFinalResponse();
            switch (response.getType()) {
                case FINALIZE_DAY_TOTALS_RESPONSE:
                    dailyLogData = new DailyLogData();
                    Slips slips = readSlips();
                    dailyLogData.setSlip(slips.get(0));
                    terminalDelay.successfulDailyLog();
                    break;
                case INITIAL_ERROR_RESPONSE:
                    terminalDelay.unsuccessful();
                    throw new BankAuthorizationException(((InitialErrorResponse) response).getCombinedMessage());
                case CONSOLE_MESSAGE:
                    log.info("Message from terminal: {}", ((ConsoleMessageResponse) response).getMessage());
                    break;
                default:
                    log.warn("Unexpected response: {}", response);
                    break;
            }
        } while (response.getType() != ResponseType.FINALIZE_DAY_TOTALS_RESPONSE);
        return dailyLogData;
    }

    Slips readSlips() {
        Slips slips = new Slips();
        List<List<String>> result = new ArrayList<>();
        List<String> slip = new ArrayList<>();
        try {
            while (!Thread.interrupted()) {
                Response response = waitForPrintLineResponse();
                ResponseType type = response.getType();
                if (type == ResponseType.PRINT_LINE) {
                    PrintLineResponse printLineResponse = (PrintLineResponse) response;
                    if (printLineResponse.isSlipDelimiter()) {
                        result.add(slip);
                        slip = new ArrayList<>();
                    } else {
                        slip.addAll(printLineResponse.getTextLines());
                    }
                    if (printLineResponse.isLastLine()) {
                        break;
                    }
                } else if (type == ResponseType.HOLD) {
                    continue;
                } else {
                    log.warn("Unexpected response: {}", response);
                }
            }
        } catch (BankException e) {
            slips.setFull(false);
            log.debug("Error on reading print lines", e);
        }
        result.add(slip);
        slips.addSlips(result);
        return slips;
    }

    public void setConnector(EftposConnector connector) {
        this.connector = connector;
    }

    public void setTerminalDelay(TerminalDelay terminalDelay) {
        this.terminalDelay = terminalDelay;
    }

    public void setTerminalMessageListener(TerminalMessageListener terminalMessage) {
        this.terminalMessageListener = terminalMessage;
    }
}
