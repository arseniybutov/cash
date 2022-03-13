package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

public class PrintKKTRegistrationSummaryCommand extends CommandWithConfirmation {
    private static final int CODE = 0xA8;

    public PrintKKTRegistrationSummaryCommand() {
        super(CODE);
    }
}
