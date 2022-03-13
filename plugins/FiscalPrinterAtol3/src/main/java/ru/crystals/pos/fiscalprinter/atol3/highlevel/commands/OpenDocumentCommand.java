package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class OpenDocumentCommand extends CommandWithConfirmation {
    private static final int CODE = 0x92;

    private final int flags;
    private final int type;

    public OpenDocumentCommand(int flags, int type) {
        super(CODE);
        this.flags = flags;
        this.type = type;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(type);
    }
}
