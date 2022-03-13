package ru.crystals.pos.bank.sberbankqr.api.core;

enum SberbankApiScope {

    PAY("https://api.sberbank.ru/qr/order.pay"),
    CREATE("https://api.sberbank.ru/order.create"),
    STATUS("https://api.sberbank.ru/order.status"),
    CANCEL("https://api.sberbank.ru/order.cancel"),
    REVOKE("https://api.sberbank.ru/order.revoke");

    private final String scope;

    SberbankApiScope(String scope) {
        this.scope = scope;
    }

    public String getValue() {
        return scope;
    }
}
