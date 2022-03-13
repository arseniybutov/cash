package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class Version extends Result {

    public final long version;
    public final long subVersion;
    public final long build;

    public Version(Response response) {
        byte[] data = response.getData();
        int index = response.getDataOffset() + 1;
        subVersion = ValueDecoder.LONG.decode(data, index, 1); index += 1;
        version = ValueDecoder.LONG.decode(data, index, 1); index += 1;
        build = ValueDecoder.LONG.decode(data, index, 4);
    }


    public String getFormattedVersion() {
        return String.format("%d.%d.%d", version, subVersion, build);
    }
}
