package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintBarcodeContinueCommand extends CommandWithConfirmation {
    private static final int CODE = 0xC2;

    private final boolean toBeContinued;
    private final String text;

    public PrintBarcodeContinueCommand(boolean toBeContinued, String text) {
        super(CODE);
        this.toBeContinued = toBeContinued;
        this.text = text;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(toBeContinued ? 0x80 : 0);
        stream.write(encode(text));
    }
}
