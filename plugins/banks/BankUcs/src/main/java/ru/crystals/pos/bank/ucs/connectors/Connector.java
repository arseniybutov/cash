package ru.crystals.pos.bank.ucs.connectors;

import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.utils.PortAdapter;

public interface Connector {
    default void init() throws BankException {

    }

    boolean startSession() throws BankException;

    void sendRequest(Request request) throws BankException;

    String waitAndReadResponse(long timeout) throws BankException;

    void endSession() throws BankException;

    void close();

    Connector setPortAdapter(PortAdapter portAdapter);

    Connector setTerminalConfiguration(TerminalConfiguration terminalConfiguration);

    void openSession() throws BankException;

    void closeSession();
}
