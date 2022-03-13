package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Status;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetStatusCommand extends Command<Status> {
    private static final int CODE = 0x3F;

    public GetStatusCommand() {
        super(CODE);
    }

    @Override
    public Status parseResult(Response response) {
        return new Status(response);
    }
}
