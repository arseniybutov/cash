package ru.crystals.pos.bank.raiffeisensbp;

public enum RaiffeisenSBPURL {

    PRODUCTION("https://e-commerce.raiffeisen.ru/api/sbp/v1", "https://e-commerce.raiffeisen.ru/api/sbp/v2"),

    TEST("https://test.ecom.raiffeisen.ru/api/sbp/v1", "https://test.ecom.raiffeisen.ru/api/sbp/v2");

    private final String urlV1;
    private final String urlV2;

    RaiffeisenSBPURL(String urlV1, String urlV2) {
        this.urlV1 = urlV1;
        this.urlV2 = urlV2;
    }

    public String getUrlV1() {
        return urlV1;
    }

    public String getUrlV2() {
        return urlV2;
    }
}
