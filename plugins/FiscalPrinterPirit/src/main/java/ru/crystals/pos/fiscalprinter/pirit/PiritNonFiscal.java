package ru.crystals.pos.fiscalprinter.pirit;

import org.apache.commons.lang.StringUtils;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.property.Properties;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@PrototypedComponent
public class PiritNonFiscal extends AbstractPirit {

    private FiscalPrinterData fiscalData;
    private Properties properties;
    private String eklzNum;
    private String regNum;


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

    /**
     * Генерация рег. номера ФН
     * Статичный рег. номер используется для того что бы не было
     * аварийного закрытия смен поле каждой перезагрузки кассы
     *
     * @return строка из 16 нулей
     */
    @Override
    public String getEklzNum() {
        if (eklzNum == null) {
            eklzNum = getZeroNum(16);
        }
        return eklzNum;
    }

    /**
     * Генерация рег. номера ККТ
     *
     * @return строка из 16 нулей
     */
    @Override
    public String getRegNum() {
        if (regNum == null) {
            regNum = getZeroNum(16);
        }
        return regNum;
    }

    private String getZeroNum(int numLength) {
        return StringUtils.leftPad("", numLength, "0");
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = BundleManager.get(Properties.class);
        }
        return properties;
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return getProperties().getShopINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    protected void postProccesingPrintedDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        if ((document instanceof Report) && ((Report) document).isZReport()) {
            fiscalData.incShiftNum();
        }
    }

    @Override
    public void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            openServiceDocument(report);
            for (DocumentSection section : sectionList) {
                if (section.getName().equals("logo")) {
                    printLogo();
                } else {
                    printLinesList(section.getContent());
                }
            }
            closeDocument(true, null);
            if (report.isZReport()) {
                printZReport(report);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void checkStatusOfEklzAndFiscalMemory(StatusFP status, DataPacket statusDataPacket) throws Exception {

    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return super.getLastDocNum();
    }

    @Override
    public Date getEKLZActivizationDate() throws FiscalPrinterException {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return c.getTime();
    }

}
