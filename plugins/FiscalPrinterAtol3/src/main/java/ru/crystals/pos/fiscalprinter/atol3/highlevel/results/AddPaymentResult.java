package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class AddPaymentResult extends Result {
    // <55h>
    // <Остаток (5)>
    public final long remain;

    // <Сдача (5)>
    public final long summa;

    public AddPaymentResult(Response response) {
        super(response);

        int index = response.getDataOffset() + 2;
        byte[] data = response.getData();

        remain = ValueDecoder.LONG.decode(data, index, 5); index += 5;
        summa = ValueDecoder.LONG.decode(data, index, 5);
    }
}
