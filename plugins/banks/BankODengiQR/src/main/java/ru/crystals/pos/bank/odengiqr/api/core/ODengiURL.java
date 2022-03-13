package ru.crystals.pos.bank.odengiqr.api.core;

public enum ODengiURL {

    TEST("https://mwalletsitold.dengi.o.kg/api/json/json.php", "https://mwalletsitold.dengi.o.kg/ru/invoice/?token="),

    PRODUCTION("https://api.dengi.o.kg/api/json/json.php", "https://api.dengi.o.kg/ru/invoice/?token=");

    /**
     * URL для запросов.
     */
    private final String url;
    /**
     * Данные для формирования QR-кода. В конец строки добавляется invoiceId.
     */
    private final String qrPrefix;

    ODengiURL(String url, String qrPrefix) {
        this.url = url;
        this.qrPrefix = qrPrefix;
    }

    public String getUrl() {
        return url;
    }

    public String getQrPrefix() {
        return qrPrefix;
    }
}
