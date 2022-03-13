package ru.crystals.pos.fiscalprinter.sp402frk.device;

/**
 * SP402FR-k Parameters
 */

public class Device {
    /**
     * Наименование устройства;
     */
    protected String name = "SP402FR-K";
    /**
     * ширина строки
     */
    protected int maxTextLength = 40;
    /**
     * длина реквизитов и т.д.
     */
    protected int maxSettingsLength = 40;
    /**
     * Максимальное количество байт в строке печати картинки
     */
    protected int maxPrintImageBytes;

    protected int clicheCount = 4;
    /**
     * режим КЛ
     */
    protected boolean isKLMode = false;
    /**
     * отрезка
     */
    protected boolean isCutAllowed = true;
    /**
     * поддержка печати изображений
     */
    protected boolean isImageAllowed = true;

    protected int maxCode39Length = 43;
    /**
     * сохраняется ли количество аннулирований
     */
    protected boolean isAnnulCounted = false;
    /**
     * Количество типов оплат
     */
    protected int paymentCount = 5;
    /**
     * пропуск между верхним краем чека и клише
     */
    protected boolean lineAfterCliche = false;

    protected boolean selfCut = true;
    /**
     * Количество налоговsых ставок
     */
    protected int taxCount = 6;
    /**
     * Печать скидок/надбавок в Z- и X- отчетах и при регистрации
     */
    protected boolean printDiscountsInReports = false;
    /**
     * Делается ли после открытия смены запись на чеке об этом
     */
    protected boolean isShiftOpenNoted = true;
    /**
     * Делается ли после установки запись на чеке об этом
     */
    protected boolean isDateSetNoted = false;
    /**
     * Поддержка печати QR кодов
     */
    protected boolean QRCodeSupport = true;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the maxTextLength property.
     */
    public int getMaxTextLength() {
        return maxTextLength;
    }

    /**
     * Sets the value of the maxTextLength property.
     */
    public void setMaxTextLength(int value) {
        this.maxTextLength = value;
    }

    /**
     * Gets the value of the maxSettingsLength property.
     */
    public int getMaxSettingsLength() {
        return maxSettingsLength;
    }

    /**
     * Sets the value of the maxSettingsLength property.
     */
    public void setMaxSettingsLength(int value) {
        this.maxSettingsLength = value;
    }

    /**
     * Gets the value of the maxPrintImageBytes property.
     */
    public int getMaxPrintImageBytes() {
        return maxPrintImageBytes;
    }

    /**
     * Sets the value of the maxPrintImageBytes property.
     */
    public void setMaxPrintImageBytes(int value) {
        this.maxPrintImageBytes = value;
    }

    /**
     * Gets the value of the clicheCount property.
     */
    public int getClicheCount() {
        return clicheCount;
    }

    /**
     * Sets the value of the clicheCount property.
     */
    public void setClicheCount(int value) {
        this.clicheCount = value;
    }

    /**
     * Gets the value of the isKLMode property.
     */
    public boolean isKLMode() {
        return isKLMode;
    }

    /**
     * Sets the value of the isKLMode property.
     */
    public void setKLMode(boolean value) {
        this.isKLMode = value;
    }

    /**
     * Gets the value of the isCutAllowed property.
     */
    public boolean isCutAllowed() {
        return isCutAllowed;
    }

    /**
     * Sets the value of the isCutAllowed property.
     */
    public void setIsCutAllowed(boolean value) {
        this.isCutAllowed = value;
    }

    /**
     * Gets the value of the isImageAllowed property.
     */
    public boolean isImageAllowed() {
        return isImageAllowed;
    }

    /**
     * Sets the value of the isImageAllowed property.
     */
    public void setIsImageAllowed(boolean value) {
        this.isImageAllowed = value;
    }

    /**
     * Gets the value of the maxCode39Length property.
     */
    public int getMaxCode39Length() {
        return maxCode39Length;
    }

    /**
     * Sets the value of the maxCode39Length property.
     */
    public void setMaxCode39Length(int value) {
        this.maxCode39Length = value;
    }

    /**
     * Gets the value of the isAnnulCounted property.
     */
    public boolean isAnnulCounted() {
        return isAnnulCounted;
    }

    /**
     * Sets the value of the isAnnulCounted property.
     */
    public void setIsAnnulCounted(boolean value) {
        this.isAnnulCounted = value;
    }

    /**
     * Gets the value of the paymentCount property.
     */
    public int getPaymentCount() {
        return paymentCount;
    }

    /**
     * Sets the value of the paymentCount property.
     */
    public void setPaymentCount(int value) {
        this.paymentCount = value;
    }

    /**
     * Gets the value of the lineAfterCliche property.
     */
    public boolean isLineAfterCliche() {
        return lineAfterCliche;
    }

    /**
     * Sets the value of the lineAfterCliche property.
     */
    public void setLineAfterCliche(boolean value) {
        this.lineAfterCliche = value;
    }

    /**
     * Sets the value of the selfCut property.
     */
    public void setSelfCut(boolean value) {
        this.selfCut = value;
    }

    /**
     * Gets the value of the taxCount property.
     */
    public int getTaxCount() {
        return taxCount;
    }

    /**
     * Sets the value of the taxCount property.
     */
    public void setTaxCount(int value) {
        this.taxCount = value;
    }

    /**
     * Gets the value of the printDiscountsInReports property.
     */
    public boolean isPrintDiscountsInReports() {
        return printDiscountsInReports;
    }

    /**
     * Sets the value of the printDiscountsInReports property.
     */
    public void setPrintDiscountsInReports(boolean value) {
        this.printDiscountsInReports = value;
    }

    public boolean isShiftOpenNoted() {
        return isShiftOpenNoted;
    }

    public void setShiftOpenNoted(boolean isShiftOpenNoted) {
        this.isShiftOpenNoted = isShiftOpenNoted;
    }

    public boolean isDateSetNoted() {
        return isDateSetNoted;
    }

    public void setDateSetNoted(boolean isDateSetNoted) {
        this.isDateSetNoted = isDateSetNoted;
    }

    public boolean isQRCodeSupport() {
        return QRCodeSupport;
    }

    public void setQRCodeSupport(boolean QRCodeSupport) {
        this.QRCodeSupport = QRCodeSupport;
    }
}
