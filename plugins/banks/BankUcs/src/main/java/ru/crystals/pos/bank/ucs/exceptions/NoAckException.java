package ru.crystals.pos.bank.ucs.exceptions;

public class NoAckException extends ConnectorException {

    private static final long serialVersionUID = -6452931696356135060L;

    public NoAckException(String message) {
        super(message);
    }

}
