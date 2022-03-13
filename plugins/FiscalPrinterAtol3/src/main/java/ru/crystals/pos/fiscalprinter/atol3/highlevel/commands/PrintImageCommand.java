package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintImageCommand extends CommandWithConfirmation {
    private static final int CODE = 0x8E;

    private final int printer;
    private final long height;
    private final int offset;
    private final byte[] raster;

    public PrintImageCommand(int printer, long height, int offset, byte[] raster) {
        super(CODE);
        if ((printer & 0xFE) != 0) {
            throw new IllegalArgumentException("Only bit #0 should be used");
        }

        this.printer = printer;
        this.height = height;
        this.offset = offset;
        this.raster = raster;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(printer);
        stream.write((int)(height >> 8));
        stream.write((int)(height & 0xFF));
        stream.write(offset >> 8);
        stream.write(offset & 0xFF);

        for (byte b : raster) {
            stream.write(b ^ 255);
        }
    }
}
