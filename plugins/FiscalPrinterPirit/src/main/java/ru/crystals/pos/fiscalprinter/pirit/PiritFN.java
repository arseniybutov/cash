package ru.crystals.pos.fiscalprinter.pirit;


import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.util.Optional;

@PrototypedComponent
public class PiritFN extends Pirit {

    @Override
    public boolean isOFDDevice() {
        return true;
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        long result = pa.openShiftInFN(cashier);
        taxes = getTaxes();
        return result;
    }

    @Override
    public synchronized Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_INC_TOTALS);
            final IncrescentTotal result = new IncrescentTotal();
            result.setSale(dp.getDoubleMoneyToLongValue(1));
            result.setReturn(dp.getDoubleMoneyToLongValue(2));
            result.setExpense(dp.getDoubleMoneyToLongValue(3));
            result.setReturnExpense(dp.getDoubleMoneyToLongValue(4));
            return Optional.of(result);
        } catch (Exception e) {
            if (e instanceof FiscalPrinterException) {
                throw (FiscalPrinterException) e;
            }
            LOG.error("getIncrescentTotal:", e);
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_READ_DATA"), e);
        }
    }

    @Override
    protected long getTargetMinimalFirmwareVersion(long currentVersion) {
        if (currentVersion < 200) {
            return MINIMAL_VERSION_WITH_FN1_SUPPORT;
        } else {
            return MINIMAL_VERSION_WITH_FN2_SUPPORT;
        }
    }

    @Override
    public FiscalPrinterInfo getFiscalPrinterInfo() throws FiscalPrinterException {
        return super.getFiscalPrinterInfoInner();
    }
}
