package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание итогов чека. Содержит информацию по всем итогам, налогам, видам оплат и доп информацию от ОФД.
 */
public class Footer {
    /**
     * Итоги чека
     */
    @JsonProperty("amounts")
    private Amounts amounts;
    /**
     * Информация, полученная от ОФД, которая должна быть напечатана после итогов.
     */
    @JsonProperty("ofd_info")
    private String ofdInfo;
    /**
     * Информация, полученная от ОФД, которая должна быть напечатана после итогов.
     */
    @JsonProperty("qr_code")
    private String qrCode;
    /**
     * Массив информации об оплате чека
     */
    @JsonProperty("payments")
    private List<FooterPayment> payments = new ArrayList<>();
    /**
     * Информация о налогах
     */
    @JsonProperty("taxes")
    private List<FooterTax> taxes = new ArrayList<>();

    public Amounts getAmounts() {
        return amounts;
    }

    public void setAmounts(Amounts amounts) {
        this.amounts = amounts;
    }

    public String getOfdInfo() {
        return ofdInfo;
    }

    public void setOfdInfo(String ofdInfo) {
        this.ofdInfo = ofdInfo;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<FooterPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<FooterPayment> payments) {
        this.payments = payments;
    }

    public List<FooterTax> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<FooterTax> taxes) {
        this.taxes = taxes;
    }
}
