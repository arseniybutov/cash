package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.PictureParams;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetPictureParamsCommand extends Command<PictureParams> {
    private static final int CODE = 0x90;

    private final int index;

    public GetPictureParamsCommand(int index) {
        super(CODE);
        this.index = index;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(index);
    }

    @Override
    public PictureParams parseResult(Response response) {
        return new PictureParams(response);
    }
}
