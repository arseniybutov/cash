package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class Initialization extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "Initialization";

    private String rnm;

    private String serialNumber;

    private String taxPayer;

    private String bin;

    private String rnn;

    private String taxation;

    private String department;

    private String cashDeskCode;

    private String address;

    public String getRnm() {
        return rnm;
    }

    public void setRnm(String rnm) {
        this.rnm = rnm;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getTaxPayer() {
        return taxPayer;
    }

    public void setTaxPayer(String taxPayer) {
        this.taxPayer = taxPayer;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getRnn() {
        return rnn;
    }

    public void setRnn(String rnn) {
        this.rnn = rnn;
    }

    public String getTaxation() {
        return taxation;
    }

    public void setTaxation(String taxation) {
        this.taxation = taxation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCashDeskCode() {
        return cashDeskCode;
    }

    public void setCashDeskCode(String cashDeskCode) {
        this.cashDeskCode = cashDeskCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Initialization{" +
                "rnm='" + rnm + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", taxPayer='" + taxPayer + '\'' +
                ", bin='" + bin + '\'' +
                ", rnn='" + rnn + '\'' +
                ", taxation='" + taxation + '\'' +
                ", department='" + department + '\'' +
                ", cashDeskCode='" + cashDeskCode + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
