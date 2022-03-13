package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.lang.StringUtils;

public class SetTableCommand extends Command {
    private static final int STRING_LENGTH = 64;
    private static final int CODE = 0x50;
    private final int num;
    private final int row;
    private final int column;
    private final byte[] value;

    public SetTableCommand(int num, int row, int column, String value) {
        this(num, row, column, encode(StringUtils.rightPad(value, STRING_LENGTH)));
    }

    public SetTableCommand(int num, int row, int column, byte value) {
        this(num, row, column, encode(value));
    }

    public SetTableCommand(int num, int row, int column, float value, int size) {
        this(num, row, column, Float.valueOf(value * 100).longValue(), size);
    }

    public SetTableCommand(int num, int row, int column, long value, int size) {
        this(num, row, column, encode(value, size));
    }

    private SetTableCommand(int num, int row, int column, byte[] value) {
        super(CODE);
        this.num = num;
        this.row = row;
        this.column = column;
        this.value = value;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(num);
        stream.write(row >> 8);
        stream.write(row & 0xFF);
        stream.write(column);
        stream.write(value);
    }
}
