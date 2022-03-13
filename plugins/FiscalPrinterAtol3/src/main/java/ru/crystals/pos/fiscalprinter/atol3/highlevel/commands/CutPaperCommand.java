package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class CutPaperCommand extends Command {
    private static final int CODE = 0x75;
    private final boolean full;

    public CutPaperCommand(boolean full) {
        super(CODE);
        this.full = full;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(full ? 0 : 1);
    }
}
