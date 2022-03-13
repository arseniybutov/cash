package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.nio.charset.Charset;

/**
 * Представление КТН в Штрихе.
 */
public class ShtrihItemCode {

    /**
     * Маркировка товара, прим.:{0x00, 0x05} (2 - Изделия из меха, 3 - лекарственные препараты, 5 - Табачные изделия)
     */
    private byte[] byteMarking;

    private int intMarking;
    /**
     * GTIN - число, переводтися в HEX
     */
    private byte[] byteGtin;

    private String strGtin;
    /**
     * Cерийный номер
     */
    private byte[] byteSerialData;

    private String strSerialData;

    /**
     * Конструктор.
     *
     * @param marking    маркировка товара
     * @param gtin       GTIN
     * @param serialData серийный номер
     */
    public ShtrihItemCode(String marking, long gtin, String serialData) {
        this.intMarking = Integer.valueOf(marking, 16);
        this.strGtin = String.valueOf(gtin);
        this.strSerialData = serialData;

        this.byteMarking = ShtrihUtils.hexStringDataToByteArray(marking);
        String hexStrGtin = StringUtils.leftPad(Long.toHexString(gtin), 12, '0').toUpperCase();
        this.byteGtin = ShtrihUtils.hexStringDataToByteArray(hexStrGtin);
        this.byteSerialData = serialData.getBytes(Charset.forName("cp866"));
    }

    /**
     * Конструктор.
     *
     * @param marking    маркировка товара
     * @param gtin       GTIN
     * @param serialData серийный номер
     */
    public ShtrihItemCode(byte[] marking, byte[] gtin, byte[] serialData) {
        this.byteMarking = marking;
        this.byteGtin = gtin;
        this.byteSerialData = serialData;
    }


    @Override
    public String toString() {
        return String.format("shtrih-discount [marking: %s; gtin: %s; serialData: \"%s\"]", PortAdapterUtils.arrayToString(getMarking()),
                PortAdapterUtils.arrayToString(getGtin()), PortAdapterUtils.arrayToString(getSerialData()));
    }

    public byte[] getMarking() {
        return byteMarking;
    }

    public void setMarking(byte[] marking) {
        this.byteMarking = marking;
    }

    public byte[] getGtin() {
        return byteGtin;
    }

    public void setGtin(byte[] gtin) {
        this.byteGtin = gtin;
    }

    public byte[] getSerialData() {
        return byteSerialData;
    }

    public void setSerialData(byte[] serialData) {
        this.byteSerialData = serialData;
    }

    public int getIntMarking() {
        return intMarking;
    }

    public String getStrGtin() {
        return strGtin;
    }

    public String getStrSerialData() {
        return strSerialData;
    }
}
