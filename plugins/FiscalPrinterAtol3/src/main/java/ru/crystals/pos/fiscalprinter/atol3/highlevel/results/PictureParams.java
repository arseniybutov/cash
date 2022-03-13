package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class PictureParams extends Result {
    // 55h
    public final int width;
    public final int height;
    public final int status;

    public PictureParams(Response response) {
        super(response);

        byte[] data = response.getData();
        int index = response.getDataOffset() + 2;
        width = data[index]; index += 1;
        height = (data[index] << 8) | data[index + 1]; index += 2;
        status = data[index];
    }
}
