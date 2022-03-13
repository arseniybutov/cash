package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

public class CancelDocumentCommand extends CommandWithConfirmation {
    private static final int CODE = 0x59;

    public CancelDocumentCommand() {
        super(CODE);
    }
}
