package ru.crystals.pos.bank.ucs.exceptions;

public class CorruptDataException extends ConnectorException {

    private static final long serialVersionUID = -8583664177422668420L;

    public CorruptDataException(String message) {
        super(message);
    }

}
