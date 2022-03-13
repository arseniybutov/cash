package ru.crystals.pos.bank.odengiqr.api.dto;

public enum ODengiCommand {

    CREATE_INVOICE("createInvoice"),
    STATUS_PAYMENT("statusPayment"),
    INVOICE_CANCEL("invoiceCancel");

    private final String command;

    ODengiCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
