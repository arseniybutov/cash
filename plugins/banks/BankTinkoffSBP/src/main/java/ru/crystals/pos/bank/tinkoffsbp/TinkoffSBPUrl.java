package ru.crystals.pos.bank.tinkoffsbp;

public enum TinkoffSBPUrl {

    PRODUCTION("https://securepay.tinkoff.ru"),

    TEST("https://rest-api-test.tinkoff.ru");

    private String url;

    TinkoffSBPUrl(String url) {
        this.url = url;
    }

    public String getValue() {
        return url;
    }
}
