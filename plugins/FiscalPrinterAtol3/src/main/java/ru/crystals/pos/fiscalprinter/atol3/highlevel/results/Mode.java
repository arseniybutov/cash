package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class Mode extends Result {
    // 55h
    public final int mode;
    public final int flags;

    public Mode(Response response) {
        super(0);
        int index = response.getDataOffset() + 1;
        byte[] data = response.getData();

        mode = data[index]; index += 1;
        flags = data[index];
    }
}
