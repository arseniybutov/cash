package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

public class ZReportCommand extends CommandWithConfirmation {
    private static final int CODE = 0x5A;

    public ZReportCommand() {
        super(CODE);
    }
}
