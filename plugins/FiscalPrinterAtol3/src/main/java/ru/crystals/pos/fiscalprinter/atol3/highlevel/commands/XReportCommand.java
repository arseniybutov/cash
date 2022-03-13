package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class XReportCommand extends CommandWithConfirmation {
    private static final int CODE = 0x67;

    private final int type;

    public XReportCommand(int type) {
        super(CODE);
        this.type = type;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(encode(type, 1));
    }
}
