package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Confirmation;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public abstract class CommandWithConfirmation extends Command<Confirmation> {
    public CommandWithConfirmation(int code) {
        super(code);
    }

    @Override
    public Confirmation parseResult(Response response) {
        return new Confirmation(response);
    }
}
