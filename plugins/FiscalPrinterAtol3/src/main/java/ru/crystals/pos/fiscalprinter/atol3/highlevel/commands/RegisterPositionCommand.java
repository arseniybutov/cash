package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

// Команда предназначена для совместимости и может быть выполнена только при ФФД 1.0!
// Режим совместимости должен быть запрограммирован в Т2Р1П109 (значение 1).
public class RegisterPositionCommand extends CommandWithConfirmation {
    public static final int GOOD_NAME_MAX_LENGTH = 64;

    // <E6h>
    private static final int CODE = 0xE6;

    // <Флаги (1)>
    private final int flags;

    // <Наименование товара (64)>
    private final String name;

    // <Цена (6)>
    private final long price;

    // <Количество (5)>
    private final long amount;

    // <Тип (1)>
    private final int type;

    // <Знак (1)>
    private final int sign;

    // <Размер (6)>
    private final long discount;

    // <Налог (1)>
    private final int tax;

    // <Секция (1)>
    private final int section;

    // <Резерв (17)>
    private final byte[] reserved = new byte[17];

    public RegisterPositionCommand(int flags, String name, long price, long amount, int type, long discount, int tax, int section) {
        super(CODE);
        this.flags = flags;
        if (name.length() > GOOD_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Position name is too long");
        }
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.type = type;
        if (discount >= 0) {
            this.sign = 0;
            this.discount = discount;
        } else {
            this.sign = 1;
            this.discount = -discount;
        }
        this.tax = tax;
        this.section = section;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(encode(name));
        for (int i = name.length(); i < GOOD_NAME_MAX_LENGTH; ++i) {
            stream.write(0);
        }
        stream.write(encode(price, 6));
        stream.write(encode(amount, 5));
        stream.write(type);
        stream.write(sign);
        stream.write(encode(discount, 6));
        stream.write(tax);
        stream.write(encode(section, 1));
        stream.write(reserved);
    }
}
