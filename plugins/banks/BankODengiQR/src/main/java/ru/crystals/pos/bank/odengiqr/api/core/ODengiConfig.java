package ru.crystals.pos.bank.odengiqr.api.core;

public class ODengiConfig {

    private String sellerID;
    private String sellerPassword;
    private ODengiURL url = ODengiURL.PRODUCTION;

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public String getSellerPassword() {
        return sellerPassword;
    }

    public void setSellerPassword(String sellerPassword) {
        this.sellerPassword = sellerPassword;
    }

    public ODengiURL getUrl() {
        return url;
    }

    public void setUrl(ODengiURL url) {
        this.url = url;
    }
}
