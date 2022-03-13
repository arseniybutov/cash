package ru.crystals.pos.bank.ucs.exceptions;

public class DataTimeoutException extends ConnectorException {

    private static final long serialVersionUID = 7234973983397365978L;

    public DataTimeoutException(String message) {
        super(message);
    }

}
