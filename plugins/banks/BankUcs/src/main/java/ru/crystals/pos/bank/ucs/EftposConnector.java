package ru.crystals.pos.bank.ucs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.TerminalConfiguration.TerminalConnectionType;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.connectors.Connector;
import ru.crystals.pos.bank.ucs.connectors.RS232Connector;
import ru.crystals.pos.bank.ucs.connectors.TCPConnector;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.requests.RequestType;

public class EftposConnector {
    private static Logger log = LoggerFactory.getLogger(EftposConnector.class);
    private Timeouts timeouts = new Timeouts();
    private String terminalId;
    private Connector connector;
    private TerminalConfiguration terminalConfiguration;

    public EftposConnector setConnector(Connector connector) {
        this.connector = connector;
        return this;
    }

    public String waitAndReadResponse(long timeout) throws BankException {
        try {
            return connector.waitAndReadResponse(timeout);
        } catch (BankException e) {
            log.error("", e);
            throw new BankCommunicationException(ResBundleBankUcs.getString("TERMINAL_TIMEOUT"));
        }
    }

    public void sendRequest(Request request) throws BankException {
        if (!connector.startSession()) {
            throw new BankCommunicationException(ResBundleBankUcs.getString("TERMINAL_TIMEOUT"));
        }
        if (request.getType() != RequestType.LOGIN) {
            request.setTerminalId(terminalId);
        }
        log.info("Sending request:\n{}", request.toLoggableString());
        connector.sendRequest(request);
    }

    public void init() throws BankException {
        if (terminalConfiguration.getConnectionType() == TerminalConnectionType.TCP) {
            connector = new TCPConnector();
        } else {
            connector = new RS232Connector();
        }
        connector.setTerminalConfiguration(terminalConfiguration);
        try {
            connector.init();
        } catch (BankException e) {
            throw new BankException(ResBundleBankUcs.getString("TERMINAL_TIMEOUT"), e);
        }
    }

    public void close() {
        connector.close();
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public EftposConnector setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public void setTerminalConfiguration(TerminalConfiguration terminalConfiguration) {
        this.terminalConfiguration = terminalConfiguration;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public void openSession() throws BankException {
        connector.openSession();
    }

    public void closeSession() {
        connector.closeSession();
    }
}
