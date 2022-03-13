package ru.crystals.pos.bank.sberbankqr.api.core;

public enum SberbankQrUrl {

    PRODUCTION("https://api.sberbank.ru/ru/prod", "https://api.sberbank.ru:8443/prod"),

    TEST("https://dev.api.sberbank.ru/ru/prod", "https://uat.api.sberbank.ru:8443/prod");

    private final String url;
    private final String urlPay;

    SberbankQrUrl(String url, String urlPay) {
        this.url = url;
        this.urlPay = urlPay;
    }

    public String getValue() {
        return url;
    }

    public String getValuePay() {
        return urlPay;
    }
}
