package ru.crystals.pos.bank.ucs.exceptions;

import java.io.IOException;

public class ConnectorException extends IOException {

    private static final long serialVersionUID = -4962463254843155929L;

    public ConnectorException(String message) {
        super(message);
    }

}
