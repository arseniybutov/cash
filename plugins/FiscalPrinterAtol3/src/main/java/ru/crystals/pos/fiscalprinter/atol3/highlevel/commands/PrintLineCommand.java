package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintLineCommand extends CommandWithConfirmation {
    private static final int CODE = 0x4C;
    private final String line;

    public PrintLineCommand(String line) {
        super(CODE);
        this.line = line;
    }

    @Override
    public void writeData(OutputStream stream) throws IOException {
        stream.write(encode(line));
    }
}
