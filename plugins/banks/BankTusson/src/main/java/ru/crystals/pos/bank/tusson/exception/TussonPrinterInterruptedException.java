package ru.crystals.pos.bank.tusson.exception;

public class TussonPrinterInterruptedException extends TussonPrinterException {
    public TussonPrinterInterruptedException(String message) {
        super(message);
    }

    public TussonPrinterInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
