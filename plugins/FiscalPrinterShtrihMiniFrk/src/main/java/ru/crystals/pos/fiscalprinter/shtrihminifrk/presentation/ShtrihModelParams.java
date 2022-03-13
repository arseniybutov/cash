package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Описание параметров модели ФР.
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihModelParams {
    
    /**
     * Параметры модели (8 байт)
     */
    private long params;
    
    /**
     * Ширина печати шрифтом 1 (1 байт)
     */
    private byte firstFontWidth;
    
    /**
     * Ширина печати шрифтом 2 (1 байт)
     */
    private byte secondFontWidth;
    
    /**
     * Номер первой печатаемой линии в графике (1 байт)
     */
    private byte firstImageLineNo;
    
    /**
     * Количество цифр в ИНН (1 байт)
     */
    private byte tinWidth;
    
    /**
     * Количество цифр в РНМ (1 байт)
     */
    private byte regNoWidth;
    
    /**
     * Количество цифр в длинном РНМ (1 байт).
     * <p/>
     * если {@code 0} - длинный РНМ не поддерживается;
     */
    private byte longRegNoWidth;
    
    /**
     * Количество цифр в длинном заводском номере (1 байт)
     * <p/>
     * если {@code 0} - длинный заводской номер не поддерживается;
     */
    private byte longDeviceNoWidth;
    
    /**
     * Пароль налогового инспектора по умолчанию (4 байта)
     */
    private int defaultTaxCollectorPassword;
    
    /**
     * Пароль сист.админа по умолчанию (4 байта)
     */
    private int defaultAdminPassword;
    
    /**
     * Номер таблицы "BLUETOOTH БЕСПРОВОДНОЙ МОДУЛЬ" настроек Bluetooth (1 байт)
     * <p/>
     * если {@code 0} - таблица не поддерживается
     */
    private byte bluetoothSettingsTableNo;
    
    /**
     * Номер поля "НАЧИСЛЕНИЕ НАЛОГОВ" (1 байт)
     * <p/>
     * если {@code 0} - поле не поддерживается
     */
    private byte chargeTaxesFieldNo;
    
    /**
     * Максимальная длина команды (N/LEN16) (2 байта)
     */
    private int maxCmdLength; 
    
    /**
     * Ширина произвольной графической линии в байтах для печати одномерного штрих-кода (1 байт)
     */
    private byte graphicLineWidthInBytes;
    
    /**
     * Количество цифр в длинном ИНН (1 байт)
     * <p/>
     * если {@code 0} - длинный ИНН не поддерживается;
     */
    private byte longTinWidth;

    /**
     * Переворачивать байты при печати графики линией
     * <p/>
     */
    private boolean swapLineBytes;
    
    /**
     * остальные настройки
     */
    private byte[] other;

    @Override
    public String toString() {
        return String.format("model-params [(inverse bits in graphic: %s;" +
            "first-img-line-no: %s; tax-password: %s; admin-password: %s]", isBitsInInverseOrderWhilePrintingLine(),
            getFirstImageLineNo(), getDefaultTaxCollectorPassword(), getDefaultAdminPassword());
    }

    /**
     * Переворачивать байты при печати линии.
     * 
     * @return {@code true}, если биты в байтах надо передавать в обраном порядке при печати линии графики
     */
    public boolean isBitsInInverseOrderWhilePrintingLine() {
        return swapLineBytes;
    }
    // getters & setters
    
    public long getParams() {
        return params;
    }

    public void setParams(long params) {
        this.params = params;
    }

    public void setSwapLineBytes(boolean swapLineBytes) {
        this.swapLineBytes = swapLineBytes;
    }

    public byte getFirstFontWidth() {
        return firstFontWidth;
    }

    public void setFirstFontWidth(byte firstFontWidth) {
        this.firstFontWidth = firstFontWidth;
    }

    public byte getSecondFontWidth() {
        return secondFontWidth;
    }

    public void setSecondFontWidth(byte secondFontWidth) {
        this.secondFontWidth = secondFontWidth;
    }

    public byte getFirstImageLineNo() {
        return firstImageLineNo;
    }

    public void setFirstImageLineNo(byte firstImageLineNo) {
        this.firstImageLineNo = firstImageLineNo;
    }

    public byte getTinWidth() {
        return tinWidth;
    }

    public void setTinWidth(byte tinWidth) {
        this.tinWidth = tinWidth;
    }

    public byte getRegNoWidth() {
        return regNoWidth;
    }

    public void setRegNoWidth(byte regNoWidth) {
        this.regNoWidth = regNoWidth;
    }

    public byte getLongRegNoWidth() {
        return longRegNoWidth;
    }

    public void setLongRegNoWidth(byte longRegNoWidth) {
        this.longRegNoWidth = longRegNoWidth;
    }

    public byte getLongDeviceNoWidth() {
        return longDeviceNoWidth;
    }

    public void setLongDeviceNoWidth(byte longDeviceNoWidth) {
        this.longDeviceNoWidth = longDeviceNoWidth;
    }

    public int getDefaultTaxCollectorPassword() {
        return defaultTaxCollectorPassword;
    }

    public void setDefaultTaxCollectorPassword(int defaultTaxCollectorPassword) {
        this.defaultTaxCollectorPassword = defaultTaxCollectorPassword;
    }

    public int getDefaultAdminPassword() {
        return defaultAdminPassword;
    }

    public void setDefaultAdminPassword(int defaultAdminPassword) {
        this.defaultAdminPassword = defaultAdminPassword;
    }

    public byte getBluetoothSettingsTableNo() {
        return bluetoothSettingsTableNo;
    }

    public void setBluetoothSettingsTableNo(byte bluetoothSettingsTableNo) {
        this.bluetoothSettingsTableNo = bluetoothSettingsTableNo;
    }

    public byte getChargeTaxesFieldNo() {
        return chargeTaxesFieldNo;
    }

    public void setChargeTaxesFieldNo(byte chargeTaxesFieldNo) {
        this.chargeTaxesFieldNo = chargeTaxesFieldNo;
    }

    public int getMaxCmdLength() {
        return maxCmdLength;
    }

    public void setMaxCmdLength(int maxCmdLength) {
        this.maxCmdLength = maxCmdLength;
    }

    public byte getGraphicLineWidthInBytes() {
        return graphicLineWidthInBytes;
    }

    public void setGraphicLineWidthInBytes(byte graphicLineWidthInBytes) {
        this.graphicLineWidthInBytes = graphicLineWidthInBytes;
    }

    public byte getLongTinWidth() {
        return longTinWidth;
    }

    public void setLongTinWidth(byte longTinWidth) {
        this.longTinWidth = longTinWidth;
    }

    public byte[] getOther() {
        return other;
    }

    public void setOther(byte[] other) {
        this.other = other;
    }
}
