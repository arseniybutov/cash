package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Value;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetRegisterCommand extends Command<Value> {

    private static final int CODE = 0x91;
    private final int num;
    private final int param1;
    private final int param2;

    public GetRegisterCommand(int num, int param1, int param2) {
        super(CODE);
        this.num = num;
        this.param1 = param1;
        this.param2 = param2;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(num);
        stream.write(param1);
        stream.write(param2);
    }

    @Override
    public Value parseResult(Response response) {
        return new Value(response);
    }
}
