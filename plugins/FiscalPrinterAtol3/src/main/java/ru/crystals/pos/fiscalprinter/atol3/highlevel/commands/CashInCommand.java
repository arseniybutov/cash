package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class CashInCommand extends CommandWithConfirmation {
    private static final int CODE = 0x49;

    private final int flags;
    private final long summa;

    public CashInCommand(int flags, long summa) {
        super(CODE);
        this.flags = flags;
        this.summa = summa;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(encode(summa, 5));
    }
}
