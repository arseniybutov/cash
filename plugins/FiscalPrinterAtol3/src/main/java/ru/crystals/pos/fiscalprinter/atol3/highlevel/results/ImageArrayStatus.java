package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class ImageArrayStatus extends Result {
    public final int freeBytes;
    public final int lastNumber;
    public final boolean lastClosed;

    public ImageArrayStatus(Response response) {
        super(response);

        byte[] data = response.getData();
        int index = response.getDataOffset() + 2;
        freeBytes = (data[index] << 8) + data[index + 1]; index += 2;
        lastNumber = data[index]; index += 1;
        lastClosed = (data[index] == 0);
    }
}
