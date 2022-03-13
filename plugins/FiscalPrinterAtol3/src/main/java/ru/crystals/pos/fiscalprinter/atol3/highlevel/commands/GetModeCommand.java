package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Mode;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetModeCommand extends Command<Mode> {
    private static final int CODE = 0x45;

    public GetModeCommand() {
        super(CODE);
    }

    @Override
    public Mode parseResult(Response response) {
        return new Mode(response);
    }
}
