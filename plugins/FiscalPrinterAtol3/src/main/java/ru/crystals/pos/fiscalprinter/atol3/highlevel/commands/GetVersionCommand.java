package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Version;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

import java.io.IOException;
import java.io.OutputStream;

public class GetVersionCommand extends Command<Version> {

    private static final int CODE = 0x9D;

    private final int source;

    public GetVersionCommand(int source) {
        super(CODE);
        this.source = source;
    }

    @Override
    public Version parseResult(Response response) {
        return new Version(response);
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(source);
    }
}
