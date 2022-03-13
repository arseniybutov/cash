package ru.crystals.pos.bank.tusson.exception;

public class TussonPrinterException extends RuntimeException {

    public TussonPrinterException(String message) {
        super(message);
    }

    public TussonPrinterException(String message, Throwable cause) {
        super(message, cause);
    }
}
