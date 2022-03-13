package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos;

public class PosApiException extends RuntimeException {

    private Integer code;

    public PosApiException() {
    }

    public PosApiException(String message) {
        super(message);
    }

    public PosApiException(String message, int code) {
        super(message);
        this.code = code;
    }

    public PosApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public PosApiException(Throwable cause) {
        super(cause);
    }

    public Integer getCode() {
        return code;
    }
}
