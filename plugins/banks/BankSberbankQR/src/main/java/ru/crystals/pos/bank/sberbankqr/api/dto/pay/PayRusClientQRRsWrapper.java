package ru.crystals.pos.bank.sberbankqr.api.dto.pay;


import com.fasterxml.jackson.annotation.JsonProperty;

public class PayRusClientQRRsWrapper {

    @JsonProperty("PayRusClientQRRs")
    private PayRusClientQRRs payRusClientQRRs;

    public PayRusClientQRRs getPayRusClientQRRs() {
        return payRusClientQRRs;
    }

    public void setPayRusClientQRRs(PayRusClientQRRs payRusClientQRRs) {
        this.payRusClientQRRs = payRusClientQRRs;
    }
}
