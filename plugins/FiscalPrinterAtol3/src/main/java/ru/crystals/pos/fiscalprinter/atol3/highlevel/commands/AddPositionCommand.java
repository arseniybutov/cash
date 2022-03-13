package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class AddPositionCommand extends CommandWithConfirmation {
    public static final int GOOD_NAME_MAX_LENGTH = 128;

    // <EBh>
    private static final int CODE = 0xEB;

    // <Флаги (1)
    private final int flags;

    // <Цена (7)>
    private final long price;

    // <Количество (5)>
    private final long amount;

    // <Стоимость позиции (7)>
    private final long cost;

    // <Налог Тип (1)>
    private final int taxType;

    // <Налог сумма (7)>
    private final long taxValue;

    // <Секция (1)>
    private final int section;

    // <Признак предмета расчета (1)>
    private final int reserved1 = 0; // Только для ФФД 1.05

    // <Признак способа расчета (1)>
    private final int reserved2 = 0; // Только для ФФД 1.05

    // <Знак скидки (1)>
    private final int discountSign;

    // <Информация о скидке (7)>
    private final long discount;

    // <Наименование товара (0..128)>
    private final String name;

    /**
     * Флаг отключения контроля наличности. Контролируем в кассе до фискализации в фискальнике
     * в этом необходимости нет. В данный момент фискальник выдает ошибку даже при возрате по безналу.
     */
    private static final int DONT_CHECK_CASH_FLAG= 0x2;

    /**
     * Неиспользуемые флаги.
     */
    private final static int UNUSED_FLAGS = 0xFC;

    public AddPositionCommand(int flags, long price, long amount, long cost, int taxType, long taxValue,
                              int section, long discount, String name) {
        super(CODE);
        flags = flags | DONT_CHECK_CASH_FLAG;
        this.flags = flags;
        
        if ((flags & UNUSED_FLAGS) != 0) {
            throw new IllegalArgumentException("Bits are not used and should be equal zero");
        }

        this.price = price;
        this.amount = amount;
        this.cost = cost;
        this.taxType = taxType;
        this.taxValue = taxValue;
        this.section = section;

        if (discount != 0) {
            if (cost != price * amount) {
                throw new IllegalArgumentException("Cost should be equal to price * amount while discount information is used");
            }
        }

        if (discount >= 0) {
            this.discountSign = 0;
            this.discount = discount;
        } else {
            this.discountSign = 1;
            this.discount = -discount;
        }

        // workaround: command drop two symbols
        this.name = "  " + name;
        if (name.length() > GOOD_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Position name is too long");
        }
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(encode(price, 7));
        stream.write(encode(amount, 5));
        stream.write(encode(cost, 7));
        stream.write(taxType);
        stream.write(encode(taxValue, 7));
        stream.write(encode(section, 1));
        stream.write(reserved1);
        stream.write(reserved2);
        stream.write(discountSign);
        stream.write(encode(discount, 7));
        stream.write(encode(name));
    }
}
