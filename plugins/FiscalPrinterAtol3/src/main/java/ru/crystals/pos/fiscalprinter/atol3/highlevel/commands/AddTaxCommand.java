package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class AddTaxCommand extends Command {
    private static final int CODE = 0xB8;

    private final int flags;
    private final boolean positional;
    private final int index;
    private final long summa;

    public AddTaxCommand(int flags, boolean positional, int index, long summa) {
        super(CODE);
        this.flags = flags;
        this.positional = positional;
        this.index = index;
        this.summa = summa;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(positional ? 1 : 0);
        stream.write(index);
        stream.write(encode(summa, 5));
    }
}
