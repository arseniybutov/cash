package ru.crystals.pos.bank.opensbp;

public enum OpenSBPURL {
    PRODUCTION("https://c2b-sbp.openbank.ru"),

    TEST("https://test-c2b-sbp.openbank.ru");

    private String url;

    OpenSBPURL(String url) {
        this.url = url;
    }

    public String getValue() {
        return url;
    }
}
