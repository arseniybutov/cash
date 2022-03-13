package ru.crystals.rxtxadapter;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

import java.util.Enumeration;

public class CommPortIdentifierSource {

    public CommPortIdentifierWrapper getPortIdentifier(String portName) throws NoSuchPortException {
        getPortIdentifiers();
        return new CommPortIdentifierWrapper(CommPortIdentifier.getPortIdentifier(portName));
    }

    public Enumeration getPortIdentifiers() {
        return CommPortIdentifier.getPortIdentifiers();
    }
}
