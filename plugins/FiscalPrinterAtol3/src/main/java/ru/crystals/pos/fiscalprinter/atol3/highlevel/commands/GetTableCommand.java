package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Value;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetTableCommand extends Command<Value> {
    private static final int CODE = 0x46;
    private final int num;
    private final int row;
    private final int column;

    public GetTableCommand(int num, int row, int column) {
        super(CODE);
        this.num = num;
        this.row = row;
        this.column = column;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(num);
        stream.write(row >> 8);
        stream.write(row & 0xFF);
        stream.write(column);
    }

    @Override
    public Value parseResult(Response response) {
        return new Value(response);
    }
}
