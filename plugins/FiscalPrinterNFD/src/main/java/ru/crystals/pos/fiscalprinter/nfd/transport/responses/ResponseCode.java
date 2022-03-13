package ru.crystals.pos.fiscalprinter.nfd.transport.responses;

public enum ResponseCode {

    SUCCESS(0),
    VALIDATION_ERROR(800),
    OPERATION_ERROR(900);

    int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResponseCode valueOf(int value) {
        for (ResponseCode code : ResponseCode.values()) {
            if (code.getCode() == value) {
                return code;
            }
        }
        return null;
    }
}

