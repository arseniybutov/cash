package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.FiscalSummary;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetFiscalSummary extends Command<FiscalSummary> {
    private static final byte[] CODE = new byte[] { (byte) 0xA4, 0x43 };

    public GetFiscalSummary() {
        super(CODE);
    }

    @Override
    public FiscalSummary parseResult(Response response) {
        return new FiscalSummary(response);
    }
}
