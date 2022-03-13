package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

public class OpenDrawerCommand extends CommandWithConfirmation {
    private static final int CODE = 0x80;

    public OpenDrawerCommand() {
        super(CODE);
    }
}
