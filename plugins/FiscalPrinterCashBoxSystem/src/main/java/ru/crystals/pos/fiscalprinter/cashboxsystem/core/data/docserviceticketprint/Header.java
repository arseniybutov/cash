package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docserviceticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание объекта заголовка чека внесения/изъятия наличных.
 */
public class Header {
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
     * ИИН/БИН организации
     */
    @JsonProperty("iin_bin")
    private String iinBin;
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
    /**
     * Номер документа
     */
    @JsonProperty("document_number")
    private Integer documentNumber;
    /**
     * Флаг оффлайн документа
     */
    @JsonProperty("is_offline")
    private Boolean isOffline;

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

    public String getIinBin() {
        return iinBin;
    }

    public void setIinBin(String iinBin) {
        this.iinBin = iinBin;
    }

    public Integer getKkmPos() {
        return kkmPos;
    }

    public void setKkmPos(Integer kkmPos) {
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

    public void setShiftNumber(Integer shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Integer documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getOffline() {
        return isOffline;
    }

    public void setOffline(Boolean offline) {
        isOffline = offline;
    }
}
