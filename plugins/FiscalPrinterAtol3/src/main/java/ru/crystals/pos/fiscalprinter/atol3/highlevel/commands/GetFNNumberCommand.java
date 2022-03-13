package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Value;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetFNNumberCommand extends Command<Value> {
    private static final byte[] CODE = new byte[] { (byte) 0xA4, 0x31 };

    public GetFNNumberCommand() {
        super(CODE);
    }

    @Override
    public Value parseResult(Response response) {
        return new Value(response);
    }
}
