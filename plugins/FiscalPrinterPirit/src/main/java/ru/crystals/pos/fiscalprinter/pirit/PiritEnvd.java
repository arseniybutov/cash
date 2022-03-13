package ru.crystals.pos.fiscalprinter.pirit;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.utils.ByteUtils;

import java.util.List;

@PrototypedComponent
public class PiritEnvd extends AbstractPirit {

    private FiscalPrinterData fiscalData;
    @Autowired
    private Properties properties;
    private static final String FAKE_REG_NUM = "-=no=-";

    @Override
    public void start() throws FiscalPrinterException {
        startInner();

        try {
            fiscalData = new FiscalPrinterData();
            fiscalData.loadState();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return null;
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return properties.getShopINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер последней закрытой

     */
    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    @Override
    protected void postProccesingPrintedDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            if (document instanceof Check) {
                Check check = (Check) document;
                if (!check.isCopy() && !(check instanceof FullCheckCopy) && !check.isAnnul()) {
                    incKPK();
                }
            } else if (document instanceof Report) {
                if (((Report) document).isZReport() && !((Report) document).isCopy()) {
                    incKPK();
                }
            }
        } catch (Exception e) {
            throwUnknownError(e);
        }
    }

    private void incKPK() throws FiscalPrinterException {
        fiscalData.incKPK();
        fiscalData.updateState();
    }

    @Override
    protected void checkStatusOfEklzAndFiscalMemory(StatusFP status, DataPacket statusDataPacket) throws Exception {
        int val = statusDataPacket.getIntegerSafe(1).orElse(0);
        if (ByteUtils.hasBit(val, 6)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_FREE_FISCAL_MEMORY"));
        } else if (ByteUtils.hasBit(val, 7)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_PASSWORD_FOR_ACCESS_TO_FISCAL_MEMORY"));
        }
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        try {
            return fiscalData.getKPK();
        } catch (Exception e) {
            throwUnknownError(e);
        }
        return 0L;
    }


    @Override
    public String getRegNum() throws FiscalPrinterException {
        if (getPiritConfig().isPiritK()) {
            return FAKE_REG_NUM;
        }
        return super.getRegNum();
    }
}
