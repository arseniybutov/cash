package ru.crystals.rxtxadapter;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

public class CommPortIdentifierWrapper {

    private final CommPortIdentifier cpi;

    public CommPortIdentifierWrapper(CommPortIdentifier cpi) {
        this.cpi = cpi;
    }

    public CommPort open(String portName, int i) throws PortInUseException {
        return cpi.open(portName, i);
    }

    public boolean isCurrentlyOwned() {
        return cpi.isCurrentlyOwned();
    }


}
