package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание реквизитов организации. Эти данные касса получает от ОФД.
 */
public class DocHeader {
    /**
     * Наименование организации
     */
    @JsonProperty("company_name")
    private String companyName;
    /**
     * Адрес торговой точки, если не указан, то адрес организации
     */
    @JsonProperty("address")
    private String address;
    /**
     * ИИН/БИН организации
     */
    @JsonProperty("iin_bin")
    private String iinBin;
    /**
     * Серийный номер кассы
     */
    @JsonProperty("kkm_serial_number")
    private String kkmSerialNumber;
    /**
     * Регистрационный номер кассы
     */
    @JsonProperty("kkm_register_number")
    private String kkmRegisterNumber;
    /**
     * Дата/время проведения чека в формате ISO 8601 YYYY-MM-DDThh:mm:ss UTC
     */
    @JsonProperty("date_time")
    private String dateTime;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIinBin() {
        return iinBin;
    }

    public void setIinBin(String iinBin) {
        this.iinBin = iinBin;
    }

    public String getKkmSerialNumber() {
        return kkmSerialNumber;
    }

    public void setKkmSerialNumber(String kkmSerialNumber) {
        this.kkmSerialNumber = kkmSerialNumber;
    }

    public String getKkmRegisterNumber() {
        return kkmRegisterNumber;
    }

    public void setKkmRegisterNumber(String kkmRegisterNumber) {
        this.kkmRegisterNumber = kkmRegisterNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
