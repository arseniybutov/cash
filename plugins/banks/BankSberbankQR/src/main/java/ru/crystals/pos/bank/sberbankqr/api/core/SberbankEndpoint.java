package ru.crystals.pos.bank.sberbankqr.api.core;

enum SberbankEndpoint {

    AUTH("/tokens/v2/oauth"),
    PAY("/qr/bscanc/v1/pay"),
    CREATE("/order/v1/creation"),
    STATUS("/order/v1/status"),
    CANCEL("/order/v1/cancel"),
    REVOKE("/order/v1/revocation");

    private final String path;

    SberbankEndpoint(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
