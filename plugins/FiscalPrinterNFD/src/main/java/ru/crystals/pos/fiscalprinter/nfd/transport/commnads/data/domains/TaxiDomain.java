package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains;

import java.math.BigDecimal;


public class TaxiDomain extends CommonDomain {

    /**
     * Номер машины.
     */
    private String carNumber;

    /**
     * Текущая плата.
     */
    private BigDecimal currentFee;

    /**
     * Признак заказа.
     */
    private boolean isOrder;

    public TaxiDomain(String carNumber, BigDecimal currentFee, boolean isOrder) {
        type = "emul:TaxiDomain";
        this.carNumber = carNumber;
        this.currentFee = currentFee;
        this.isOrder = isOrder;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public BigDecimal getCurrentFee() {
        return currentFee;
    }

    public void setCurrentFee(BigDecimal currentFee) {
        this.currentFee = currentFee;
    }

    public boolean isOrder() {
        return isOrder;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }


}
