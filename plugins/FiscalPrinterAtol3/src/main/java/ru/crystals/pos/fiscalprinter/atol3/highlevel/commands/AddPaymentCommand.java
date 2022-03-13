package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.AddPaymentResult;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class AddPaymentCommand extends Command<AddPaymentResult> {
    private static final int CODE = 0x99;

    // <Флаги (1)>
    private final int flags;

    // <Тип оплаты (1)>
    private final int type;

    // <Сумма (5)>
    private final long summa;

    public AddPaymentCommand(int flags, int type, long summa) {
        super(CODE);
        this.flags = flags;
        this.type = type;
        this.summa = summa;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(type);
        stream.write(encode(summa, 5));
    }

    @Override
    public AddPaymentResult parseResult(Response response) {
        return new AddPaymentResult(response);
    }
}
