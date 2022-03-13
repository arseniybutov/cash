package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class AddAdjustmentCommand extends CommandWithConfirmation {
    private static final int CODE = 0x43;

    // <Флаги (1)>
    private final int flags;

    // <Область (1)>
    private final int area;

    // <Тип (1)>
    private final int type;

    // <Знак (1)>
    private final int sign;

    // <Размер (X)>
    private final long value;

    public AddAdjustmentCommand(int flags, int area, int type, int sign, long value) {
        super(CODE);
        this.flags = flags;
        this.area = area;
        this.type = type;
        this.sign = sign;
        this.value = value;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(area);
        stream.write(type);
        stream.write(sign);
        stream.write(encode(value, 5));
    }
}
