package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

import ru.crystals.pos.annotation.PrototypedComponent;

/**
 * Created by Tatarinov Eduard on 28.12.16.
 */
@PrototypedComponent
public class VikiPrintFN extends VikiPrint {

    @Override
    public boolean isOFDDevice() {
        return true;
    }

}
