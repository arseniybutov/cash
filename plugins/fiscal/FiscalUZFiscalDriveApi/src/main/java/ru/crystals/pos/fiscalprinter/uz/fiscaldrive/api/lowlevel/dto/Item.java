package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

    @JsonProperty("Discount")
    private Long discount;
    @JsonProperty("Price")
    private Long price;
    @JsonProperty("Barcode")
    private String barcode;
    @JsonProperty("Amount")
    private Long amount;
    @JsonProperty("VAT")
    private Long vat;
    @JsonProperty("Name")
    private String Name;
    @JsonProperty("Other")
    private Long other;

    public Long getDiscount() {
        return discount;
    }

    public void setDiscount(Long discount) {
        this.discount = discount;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getVat() {
        return vat;
    }

    public void setVat(Long vat) {
        this.vat = vat;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Long getOther() {
        return other;
    }

    public void setOther(Long other) {
        this.other = other;
    }

    @Override
    public String toString() {
        return "Item{" +
                "discount=" + discount +
                ", price=" + price +
                ", barcode='" + barcode + '\'' +
                ", amount=" + amount +
                ", vAT=" + vat +
                ", Name='" + Name + '\'' +
                ", other=" + other +
                '}';
    }
}
