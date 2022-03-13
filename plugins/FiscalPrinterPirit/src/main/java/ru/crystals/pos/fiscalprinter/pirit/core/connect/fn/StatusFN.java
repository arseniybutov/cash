package ru.crystals.pos.fiscalprinter.pirit.core.connect.fn;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

/**
 *
 * @author Tatarinov Eduard
 */
public class StatusFN {

    private long stateFN;
    private long stateCurrentDoc;
    private long flags;
    
    public StatusFN(DataPacket dp) throws FiscalPrinterException {
        try {
            if (dp.getLongValue(0) == 2L) {
                stateFN = dp.getLongValue(1);
                stateCurrentDoc = dp.getLongValue(2);
                flags = dp.getLongValue(3);
            }
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public long getStateFN() {
        return stateFN;
    }

    public long getStateCurrentDoc() {
        return stateCurrentDoc;
    }

    public long getFlags() {
        return flags;
    }
    
    @Override
    public String toString() {
        return "StatusFN{ " + "stateFN = " + stateFN + ", stateCurrentDoc = " + stateCurrentDoc + ", flags = " + flags + " }";
    }
    
    
    
}
