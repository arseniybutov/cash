package ru.crystals.pos.fiscalprinter.pirit.core.connect.fn;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

/**
 * Формат ответа на команду {@link ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand#GET_EKLZ_INFO_FW}
 *
 * @author Tatarinov Eduard
 */
public class VersionFN {

    /**
     * Версия прошивки ФН
     */
    private String verFn;
    /**
     * Версия ФР (0 - отладочный ФН, 1 - серийный ФН)
     */
    private String flagFn;
    /**
     * Версия ФФД (1209)
     */
    private String verFfd;
    /**
     * Версия ККТ (1188)
     */
    private String verKkt;
    /**
     * Версия ФФД ККТ (1189)
     */
    private String verFfdKkt;
    /**
     * Зарегистрированная версия ФФД ФН
     */
    private String regVerFfdFn;
    /**
     * Максимальная версия ФФД ФН (1190)
     */
    private String maxVerFfdFn;

    public VersionFN(DataPacket dp) throws FiscalPrinterException {
        try {
            if (dp.getLongValue(0) == 14L) {
                verFn = dp.getStringValue(1);
                flagFn = dp.getStringValue(2);
                verFfd = dp.getStringValueNull(3);
                verKkt = dp.getStringValueNull(4);
                verFfdKkt = dp.getStringValueNull(5);
                regVerFfdFn = dp.getStringValueNull(6);
                maxVerFfdFn = dp.getStringValueNull(7);
            }
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public String getVerFn() {
        return verFn;
    }

    public String getFlagFn() {
        return flagFn;
    }

    public String getVerFfd() {
        return verFfd;
    }

    public String getVerKkt() {
        return verKkt;
    }

    public String getVerFfdKkt() {
        return verFfdKkt;
    }

    public String getRegVerFfdFn() {
        return regVerFfdFn;
    }

    public String getMaxVerFfdFn() {
        return maxVerFfdFn;
    }

    @Override
    public String toString() {
        return "VersionFN{" +
                "verFn='" + verFn + '\'' +
                ", flagFn='" + flagFn + '\'' +
                ", verFfd='" + verFfd + '\'' +
                ", verKkt='" + verKkt + '\'' +
                ", verFfdKkt='" + verFfdKkt + '\'' +
                ", regVerFfdFn='" + regVerFfdFn + '\'' +
                ", maxVerFfdFn='" + maxVerFfdFn + '\'' +
                '}';
    }
}
