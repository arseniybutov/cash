package ru.crystals.pos.fiscalprinter.mstar.core.connect.fn;

import java.text.SimpleDateFormat;
import java.util.Date;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

/**
 * Состояние фискального накопителя
 */
public class StateFN {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Состояние фазы жизни
     */
    private long lifeCycle;
    /**
     * Текущий документ
     */
    private long currentDoc;
    /**
     * Данные документа
     */
    private long documentData;
    /**
     * Состояние смены
     */
    private long shiftState;
    /**
     * Флаги предупреждения
     */
    private long flags;
    /**
     * Дата последнего документа
     */
    private Date lastDocDate;
    /**
     * Время последнего документа
     */
    private Date lastDocTime;
    /**
     * Номер фискального накопителя
     */
    private String numberFN;
    /**
     * Номер последнего фискального документа
     */
    private long lastFiscalDocNumber;
    /**
     * Версия ПО фискального накопителя
     */
    private String versionFirmwareFN;
    /**
     * Тип ПО фискального накопителя
     */
    private long typeFirmwareFN;
    /**
     * Срок действия фискального накопителя
     */
    private Date validityFN;
    /**
     * Оставшееся количество возможности сделать отчет о регистрации (перерегистрации) ККТ
     */
    private long remainingAmountToRegisration;
    /**
     * Количество уже сделанных отчётов о регистрации (перерегистрации) ККТ
     */
    private long alreadyMadeReportRegistration;


    public StateFN(DataPacket dp) throws FiscalPrinterException {
        try {
            lifeCycle = dp.getLongValue(0);
            currentDoc = dp.getLongValue(1);
            documentData = dp.getLongValue(2);
            shiftState = dp.getLongValue(3);
            flags = dp.getLongValue(4);
            lastDocDate = dp.getDateValue(5);
            lastDocTime = dp.getTimeValue(6);
            numberFN = dp.getStringValue(7);
            lastFiscalDocNumber = dp.getLongValue(8);
            versionFirmwareFN = dp.getStringValue(9);
            typeFirmwareFN = dp.getLongValue(10);
            validityFN = dp.getDateValue(11);
            remainingAmountToRegisration = dp.getLongValue(12);
            alreadyMadeReportRegistration = dp.getLongValue(13);
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public long getLifeCycle() {
        return lifeCycle;
    }

    public long getCurrentDoc() {
        return currentDoc;
    }

    public long getDocumentData() {
        return documentData;
    }

    public long getShiftState() {
        return shiftState;
    }

    public long getFlags() {
        return flags;
    }

    public Date getLastDocDate() {
        return lastDocDate;
    }

    public Date getLastDocTime() {
        return lastDocTime;
    }

    public String getNumberFN() {
        return numberFN;
    }

    public long getLastFiscalDocNumber() {
        return lastFiscalDocNumber;
    }

    public String getVersionFirmwareFN() {
        return versionFirmwareFN;
    }

    public long getTypeFirmwareFN() {
        return typeFirmwareFN;
    }

    public Date getValidityFN() {
        return validityFN;
    }

    public long getRemainingAmountToRegisration() {
        return remainingAmountToRegisration;
    }

    public long getAlreadyMadeReportRegistration() {
        return alreadyMadeReportRegistration;
    }

    @Override
    public String toString() {
        return "StateFN{" +
                "lifeCycle=" + lifeCycle +
                ", currentDoc=" + currentDoc +
                ", documentData=" + documentData +
                ", shiftState=" + shiftState +
                ", flags=" + flags +
                ", lastDocDate=" + dateFormat.format(lastDocDate) +
                ", lastDocTime=" + timeFormat.format(lastDocTime) +
                ", numberFN='" + numberFN + '\'' +
                ", lastFiscalDocNumber=" + lastFiscalDocNumber +
                ", versionFirmwareFN='" + versionFirmwareFN + '\'' +
                ", typeFirmwareFN=" + typeFirmwareFN +
                ", validityFN=" + dateFormat.format(validityFN) +
                ", remainingAmountToRegisration=" + remainingAmountToRegisration +
                ", alreadyMadeReportRegistration=" + alreadyMadeReportRegistration +
                '}';
    }
}
