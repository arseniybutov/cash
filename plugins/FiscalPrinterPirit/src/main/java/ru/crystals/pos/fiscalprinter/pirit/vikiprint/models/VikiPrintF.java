package ru.crystals.pos.fiscalprinter.pirit.vikiprint.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VikiPrintF extends AbstractVikiPrint {
    protected final Logger log = LoggerFactory.getLogger(VikiPrintF.class);


    @Override
    public boolean hasRegNum() {
        return true;
    }


}
