package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintPictureCommand extends CommandWithConfirmation {
    private static final int CODE = 0x8D;

    private final int printer;
    private final int number;
    private final int offset;

    public PrintPictureCommand(int printer, int number, int offset) {
        super(CODE);
        this.printer = printer;
        this.number = number;
        this.offset = offset;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(printer);
        stream.write(number);
        stream.write(offset >> 8);
        stream.write(offset & 0xFF);
    }
}
