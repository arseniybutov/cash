package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class OpenShiftCommand extends CommandWithConfirmation {
    private static final int CODE = 0x9A;

    private final int flags;
    private final String text;

    public OpenShiftCommand(int flags, String text) {
        super(CODE);
        this.flags = flags;
        this.text = text;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(encode(text));
    }
}
