package ru.crystals.pos.bank.belinvest.ds;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Tatarinov Eduard on 17.11.16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"cashId", "scenary", "operationType", "amount", "currency", "originalCode", "detailReport"})
public class OperationDataRq {
    @XmlElement(name = "CashId")
    private String cashId;
    @XmlElement(name = "Scenary")
    private String scenary;
    @XmlElement(name = "OperationType")
    private String operationType;
    @XmlElement(name = "Amount")
    private Long amount;
    @XmlElement(name = "Currency")
    private String currency;
    @XmlElement(name = "OriginalCode")
    private Integer originalCode;
    @XmlElement(name = "DetailReport")
    private String detailReport;

    public String getCashId() {
        return cashId;
    }

    public void setCashId(String cashId) {
        this.cashId = cashId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(Integer originalCode) {
        this.originalCode = originalCode;
    }

    public String getDetailReport() {
        return detailReport;
    }

    public void setDetailReport(String detailReport) {
        this.detailReport = detailReport;
    }

    public String getScenary() {
        return scenary;
    }

    public void setScenary(String scenary) {
        this.scenary = scenary;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
