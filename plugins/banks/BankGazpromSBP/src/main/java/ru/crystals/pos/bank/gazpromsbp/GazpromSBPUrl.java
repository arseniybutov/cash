package ru.crystals.pos.bank.gazpromsbp;

public enum GazpromSBPUrl {
    PRODUCTION("https://sbp.gazprombank.ru:9443"),

    TEST("https://195.225.38.125:9443");

    private String url;

    GazpromSBPUrl(String url) {
        this.url = url;
    }

    public String getValue() {
        return url;
    }
}
