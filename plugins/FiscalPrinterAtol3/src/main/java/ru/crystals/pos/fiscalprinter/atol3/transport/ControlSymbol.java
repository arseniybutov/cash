package ru.crystals.pos.fiscalprinter.atol3.transport;

enum ControlSymbol {
    STX(0xFE),
    ESC(0xFD),
    TSTX(0xEE),
    TESC(0xED),
    ASYNC_REPLY(0xF0);

    int code;

    ControlSymbol(int code) {
        this.code = code;
    }
}
