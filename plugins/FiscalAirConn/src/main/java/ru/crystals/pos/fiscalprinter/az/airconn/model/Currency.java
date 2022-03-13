package ru.crystals.pos.fiscalprinter.az.airconn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Currency {

    @JsonProperty("currency")
    private String currencyName;

    private Long saleCount;
    private BigDecimal saleSum;
    private BigDecimal saleCashSum;
    private BigDecimal saleCashlessSum;
    private BigDecimal salePrepaymentSum;
    private BigDecimal saleCreditSum;
    private BigDecimal saleBonusSum;
    private List<VatAmount> saleVatAmounts = new ArrayList<>();
    private Long depositCount;
    private BigDecimal depositSum;
    private Long withdrawCount;
    private BigDecimal withdrawSum;
    private Long moneyBackCount;
    private BigDecimal moneyBackSum;
    private BigDecimal moneyBackCashSum;
    private BigDecimal moneyBackCashlessSum;
    private BigDecimal moneyBackPrepaymentSum;
    private BigDecimal moneyBackCreditSum;
    private BigDecimal moneyBackBonusSum;
    private List<VatAmount> moneyBackVatAmounts = new ArrayList<>();
    private Long rollbackCount;
    private BigDecimal rollbackSum;
    private BigDecimal rollbackCashSum;
    private BigDecimal rollbackCashlessSum;
    private BigDecimal rollbackPrepaymentSum;
    private BigDecimal rollbackCreditSum;
    private BigDecimal rollbackBonusSum;
    private List<VatAmount> rollbackVatAmounts = new ArrayList<>();
    private Long correctionCount;
    private BigDecimal correctionSum;
    private BigDecimal correctionCashSum;
    private BigDecimal correctionCashlessSum;
    private BigDecimal correctionPrepaymentSum;
    private BigDecimal correctionCreditSum;
    private BigDecimal correctionBonusSum;
    private List<VatAmount> correctionVatAmounts = new ArrayList<>();

    public String getCurrency() {
        return currencyName;
    }

    public void setCurrency(String currency) {
        this.currencyName = currency;
    }

    public Long getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Long saleCount) {
        this.saleCount = saleCount;
    }

    public BigDecimal getSaleSum() {
        return saleSum;
    }

    public void setSaleSum(BigDecimal saleSum) {
        this.saleSum = saleSum;
    }

    public BigDecimal getSaleCashSum() {
        return saleCashSum;
    }

    public void setSaleCashSum(BigDecimal saleCashSum) {
        this.saleCashSum = saleCashSum;
    }

    public BigDecimal getSaleCashlessSum() {
        return saleCashlessSum;
    }

    public void setSaleCashlessSum(BigDecimal saleCashlessSum) {
        this.saleCashlessSum = saleCashlessSum;
    }

    public BigDecimal getSalePrepaymentSum() {
        return salePrepaymentSum;
    }

    public void setSalePrepaymentSum(BigDecimal salePrepaymentSum) {
        this.salePrepaymentSum = salePrepaymentSum;
    }

    public BigDecimal getSaleCreditSum() {
        return saleCreditSum;
    }

    public void setSaleCreditSum(BigDecimal saleCreditSum) {
        this.saleCreditSum = saleCreditSum;
    }

    public BigDecimal getSaleBonusSum() {
        return saleBonusSum;
    }

    public void setSaleBonusSum(BigDecimal saleBonusSum) {
        this.saleBonusSum = saleBonusSum;
    }

    public List<VatAmount> getSaleVatAmounts() {
        return saleVatAmounts;
    }

    public void setSaleVatAmounts(List<VatAmount> saleVatAmounts) {
        this.saleVatAmounts = saleVatAmounts;
    }

    public Long getDepositCount() {
        return depositCount;
    }

    public void setDepositCount(Long depositCount) {
        this.depositCount = depositCount;
    }

    public BigDecimal getDepositSum() {
        return depositSum;
    }

    public void setDepositSum(BigDecimal depositSum) {
        this.depositSum = depositSum;
    }

    public Long getWithdrawCount() {
        return withdrawCount;
    }

    public void setWithdrawCount(Long withdrawCount) {
        this.withdrawCount = withdrawCount;
    }

    public BigDecimal getWithdrawSum() {
        return withdrawSum;
    }

    public void setWithdrawSum(BigDecimal withdrawSum) {
        this.withdrawSum = withdrawSum;
    }

    public Long getMoneyBackCount() {
        return moneyBackCount;
    }

    public void setMoneyBackCount(Long moneyBackCount) {
        this.moneyBackCount = moneyBackCount;
    }

    public BigDecimal getMoneyBackSum() {
        return moneyBackSum;
    }

    public void setMoneyBackSum(BigDecimal moneyBackSum) {
        this.moneyBackSum = moneyBackSum;
    }

    public BigDecimal getMoneyBackCashSum() {
        return moneyBackCashSum;
    }

    public void setMoneyBackCashSum(BigDecimal moneyBackCashSum) {
        this.moneyBackCashSum = moneyBackCashSum;
    }

    public BigDecimal getMoneyBackCashlessSum() {
        return moneyBackCashlessSum;
    }

    public void setMoneyBackCashlessSum(BigDecimal moneyBackCashlessSum) {
        this.moneyBackCashlessSum = moneyBackCashlessSum;
    }

    public BigDecimal getMoneyBackPrepaymentSum() {
        return moneyBackPrepaymentSum;
    }

    public void setMoneyBackPrepaymentSum(BigDecimal moneyBackPrepaymentSum) {
        this.moneyBackPrepaymentSum = moneyBackPrepaymentSum;
    }

    public BigDecimal getMoneyBackCreditSum() {
        return moneyBackCreditSum;
    }

    public void setMoneyBackCreditSum(BigDecimal moneyBackCreditSum) {
        this.moneyBackCreditSum = moneyBackCreditSum;
    }

    public BigDecimal getMoneyBackBonusSum() {
        return moneyBackBonusSum;
    }

    public void setMoneyBackBonusSum(BigDecimal moneyBackBonusSum) {
        this.moneyBackBonusSum = moneyBackBonusSum;
    }

    public List<VatAmount> getMoneyBackVatAmounts() {
        return moneyBackVatAmounts;
    }

    public void setMoneyBackVatAmounts(List<VatAmount> moneyBackVatAmounts) {
        this.moneyBackVatAmounts = moneyBackVatAmounts;
    }

    public Long getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Long rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public BigDecimal getRollbackSum() {
        return rollbackSum;
    }

    public void setRollbackSum(BigDecimal rollbackSum) {
        this.rollbackSum = rollbackSum;
    }

    public BigDecimal getRollbackCashSum() {
        return rollbackCashSum;
    }

    public void setRollbackCashSum(BigDecimal rollbackCashSum) {
        this.rollbackCashSum = rollbackCashSum;
    }

    public BigDecimal getRollbackCashlessSum() {
        return rollbackCashlessSum;
    }

    public void setRollbackCashlessSum(BigDecimal rollbackCashlessSum) {
        this.rollbackCashlessSum = rollbackCashlessSum;
    }

    public BigDecimal getRollbackPrepaymentSum() {
        return rollbackPrepaymentSum;
    }

    public void setRollbackPrepaymentSum(BigDecimal rollbackPrepaymentSum) {
        this.rollbackPrepaymentSum = rollbackPrepaymentSum;
    }

    public BigDecimal getRollbackCreditSum() {
        return rollbackCreditSum;
    }

    public void setRollbackCreditSum(BigDecimal rollbackCreditSum) {
        this.rollbackCreditSum = rollbackCreditSum;
    }

    public BigDecimal getRollbackBonusSum() {
        return rollbackBonusSum;
    }

    public void setRollbackBonusSum(BigDecimal rollbackBonusSum) {
        this.rollbackBonusSum = rollbackBonusSum;
    }

    public List<VatAmount> getRollbackVatAmounts() {
        return rollbackVatAmounts;
    }

    public void setRollbackVatAmounts(List<VatAmount> rollbackVatAmounts) {
        this.rollbackVatAmounts = rollbackVatAmounts;
    }

    public Long getCorrectionCount() {
        return correctionCount;
    }

    public void setCorrectionCount(Long correctionCount) {
        this.correctionCount = correctionCount;
    }

    public BigDecimal getCorrectionSum() {
        return correctionSum;
    }

    public void setCorrectionSum(BigDecimal correctionSum) {
        this.correctionSum = correctionSum;
    }

    public BigDecimal getCorrectionCashSum() {
        return correctionCashSum;
    }

    public void setCorrectionCashSum(BigDecimal correctionCashSum) {
        this.correctionCashSum = correctionCashSum;
    }

    public BigDecimal getCorrectionCashlessSum() {
        return correctionCashlessSum;
    }

    public void setCorrectionCashlessSum(BigDecimal correctionCashlessSum) {
        this.correctionCashlessSum = correctionCashlessSum;
    }

    public BigDecimal getCorrectionPrepaymentSum() {
        return correctionPrepaymentSum;
    }

    public void setCorrectionPrepaymentSum(BigDecimal correctionPrepaymentSum) {
        this.correctionPrepaymentSum = correctionPrepaymentSum;
    }

    public BigDecimal getCorrectionCreditSum() {
        return correctionCreditSum;
    }

    public void setCorrectionCreditSum(BigDecimal correctionCreditSum) {
        this.correctionCreditSum = correctionCreditSum;
    }

    public BigDecimal getCorrectionBonusSum() {
        return correctionBonusSum;
    }

    public void setCorrectionBonusSum(BigDecimal correctionBonusSum) {
        this.correctionBonusSum = correctionBonusSum;
    }

    public List<VatAmount> getCorrectionVatAmounts() {
        return correctionVatAmounts;
    }

    public void setCorrectionVatAmounts(List<VatAmount> correctionVatAmounts) {
        this.correctionVatAmounts = correctionVatAmounts;
    }
}
