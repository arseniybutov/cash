package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание объект заголовка чека. Содержит реквизиты организации, а так же общую информацию о чеке.
 */
public class Header {
    /**
     * Штрихкод документа
     */
    @JsonProperty("barcode")
    private String barcode;
    /**
     * Адрес торговой точки. Если адрес торговой точки не доступен, то используется адрес организации.
     */
    @JsonProperty("address")
    private String address;
    /**
     * Наименование организации
     */
    @JsonProperty("company_name")
    private String companyName;
    /**
     * Дата/время проведения чека в формате ISO 8601 YYYY-MM-DDThh:mm:ss UTC
     */
    @JsonProperty("date_time")
    private String dateTime;
    /**
     * Внутренний номер документа
     */
    @JsonProperty("document_number")
    private Integer documentNumber;
    /**
     *  Фискальный признак документа
     */
    @JsonProperty("fp")
    private String fp;
    /**
     * ИИН/БИН организации
     */
    @JsonProperty("iin_bin")
    private String iinBin;
    /**
     * Флаг оффлайн документа
     */
    @JsonProperty("is_offline")
    private boolean isOffline;
    /**
     * Номер кассового места
     */
    @JsonProperty("kkm_pos")
    private Integer kkmPos;
    /**
     *  Регистрационный номер кассы
     */
    @JsonProperty("kkm_register_number")
    private String kkmRegisterNumber;
    /**
     * Серийный номер кассы
     */
    @JsonProperty("kkm_serial_number")
    private String kkmSerialNumber;
    /**
     * Имя оператора
     */
    @JsonProperty("operator_name")
    private String operatorName;
    /**
     * Номер смены
     */
    @JsonProperty("shift_number")
    private Integer shiftNumber;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(int documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFp() {
        return fp;
    }

    public void setFp(String fp) {
        this.fp = fp;
    }

    public String getIinBin() {
        return iinBin;
    }

    public void setIinBin(String iinBin) {
        this.iinBin = iinBin;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public Integer getKkmPos() {
        return kkmPos;
    }

    public void setKkmPos(int kkmPos) {
        this.kkmPos = kkmPos;
    }

    public String getKkmRegisterNumber() {
        return kkmRegisterNumber;
    }

    public void setKkmRegisterNumber(String kkmRegisterNumber) {
        this.kkmRegisterNumber = kkmRegisterNumber;
    }

    public String getKkmSerialNumber() {
        return kkmSerialNumber;
    }

    public void setKkmSerialNumber(String kkmSerialNumber) {
        this.kkmSerialNumber = kkmSerialNumber;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }
}
