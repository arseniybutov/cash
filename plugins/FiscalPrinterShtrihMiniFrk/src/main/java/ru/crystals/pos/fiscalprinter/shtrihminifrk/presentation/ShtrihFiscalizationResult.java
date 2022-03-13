package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.util.Date;

/**
 * Данные итогов фискализации (перерегистрации).
 */
public class ShtrihFiscalizationResult {

    /**
     * Дата и время фискализации
     */
    private Date fiscalizationDate;

    /**
     * ИНН (Taxpayer Identification Number).
     */
    private long tin;

    /**
     * регистрационный номер ФР;
     */
    private String regNo;

    /**
     * Код налогообложения
     */
    private byte taxId;

    /**
     * Режим работы: Шифрование, Автономный режим, Автоматический режим, Применение в сфере услуг,
     * Режим БСО, Применение в Интернет
     */
    private byte workMode;
    
    // @formatter:off
    @Override
    public String toString() {
        return String.format("shtrih-fiscalization-result[tin: %s; regNo: %s; fiscalization-date: %s; taxId: %s; workMode: %s;]", getTin(),
                getRegNum(),getFiscalizationDate() == null ? "(NULL)" : String.format("%tF", getFiscalizationDate()), getTaxId(), getWorkMode());
    }
    // @formatter:on
    
    // getters & setters

    public Date getFiscalizationDate() {
        return fiscalizationDate;
    }

    public void setFiscalizationDate(Date fiscalizationDate) {
        this.fiscalizationDate = fiscalizationDate;
    }

    public long getTin() {
        return tin;
    }

    public void setTin(long tin) {
        this.tin = tin;
    }

    public String getRegNum() {
        return regNo;
    }

    public void setRegNum(String regNo) {
        this.regNo = regNo;
    }

    public byte getTaxId() {
        return taxId;
    }

    public void setTaxId(byte taxId) {
        this.taxId = taxId;
    }

    public byte getWorkMode() {
        return workMode;
    }

    public void setWorkMode(byte workMode) {
        this.workMode = workMode;
    }
}
