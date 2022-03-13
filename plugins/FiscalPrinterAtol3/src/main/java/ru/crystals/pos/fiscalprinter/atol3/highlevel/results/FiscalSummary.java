package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class FiscalSummary extends Result {
    // 55h
    // public final Date dateTime;
    public final String inn;
    // TODO: add more

    public FiscalSummary(Response response) {
        super(response);

        byte[] data = response.getData();
        int index = response.getDataOffset() + 2;
        index += 5; // dateTime
        inn = ValueDecoder.ASCII_STRING.decode(data, index, 12);
    }
}
