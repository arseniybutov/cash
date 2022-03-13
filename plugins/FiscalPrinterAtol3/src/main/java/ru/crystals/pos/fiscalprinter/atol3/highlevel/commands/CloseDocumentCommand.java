package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class CloseDocumentCommand extends CommandWithConfirmation {
    private static final int CODE = 0x4A;

    private final int flags;
    private final int type;
    private final long summa;

    public CloseDocumentCommand(int flags, int type, long summa) {
        super(CODE);
        this.flags = flags;
        this.type = type;
        this.summa = summa;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(type);
        stream.write(encode(summa, 5));
    }
}
