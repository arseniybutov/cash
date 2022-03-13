package ru.crystals.pos.fiscalprinter.pirit.vikiprint.models;

import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgent;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConfig;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.PiritModel;

import java.util.List;

/**
 * Базовый класс для всех VikiPrint
 */
//TODO вынести выше и использовать для всех пиритов
public abstract class AbstractVikiPrint {
    protected PiritConnector pc;
    protected PiritAgent pa;
    protected PiritConfig piritConfig;
    protected PiritModel piritModel = PiritModel.UNKNOWN;

    public void setPiritConfig(PiritConfig piritConfig) {
        this.piritConfig = piritConfig;
    }

    public void setPiritConnector(PiritConnector piritConnector) {
        this.pc = piritConnector;
    }

    public void setPiritAgent(PiritAgent piritAgent) {
        this.pa = piritAgent;
    }

    public String getEklzNum() throws FiscalPrinterException {
        return pa.getEklzNum();
    }

    public boolean isOfdDevice() {
        return piritModel.getCapabilities().isOfdDevice();
    }

    ;

    public String getINN() throws FiscalPrinterException {
        return pa.getINN();
    }

    public long getShiftNumber() throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    public long openShift(Cashier cashier) throws FiscalPrinterException {
        if (isOfdDevice()) {
            return pa.openShiftInFN(cashier);
        } else {
            return pa.getShiftNumber();
        }
    }

    public void printDocumentAfter(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {

    }

    public long getLastKpk() throws FiscalPrinterException {
        return pa.getLastKpk();
    }

    public boolean hasRegNum() {
        return false;
    }

    public void initFpCounters() {
    }

    public String getDeviceName() {
        return piritModel.getModelName();
    }

    public void setModel(PiritModel model) {
        this.piritModel = model;
    }

    public PiritModel getPiritModel() {
        return piritModel;
    }
}
