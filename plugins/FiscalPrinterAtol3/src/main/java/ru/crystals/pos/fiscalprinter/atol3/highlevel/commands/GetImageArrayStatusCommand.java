package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.ImageArrayStatus;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetImageArrayStatusCommand extends Command<ImageArrayStatus> {
    private static final int CODE = 0x8C;

    public GetImageArrayStatusCommand() {
        super(CODE);
    }

    @Override
    public ImageArrayStatus parseResult(Response response) {
        return new ImageArrayStatus(response);
    }
}
