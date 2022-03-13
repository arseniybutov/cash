package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

// Команда предназначена для совместимости и может быть выполнена только при ФФД 1.0!
// Режим совместимости должен быть запрограммирован в Т2Р1П109 (значение 1).
public class AddReturnPositionCommand extends CommandWithConfirmation {
    private static final int CODE = 0x57;

    private final int flags;
    private final long price;
    private final long amount;

    public AddReturnPositionCommand(int flags, long price, long amount) {
        super(CODE);
        this.flags = flags;
        this.price = price;
        this.amount = amount;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(encode(price, 5));
        stream.write(encode(amount, 5));
    }
}
