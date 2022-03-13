package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import java.util.Arrays;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class DeviceType extends Result {
    public final int protocol;
    public final int type;
    public final int model;
//        public final int mode2;
    public final byte[] version;
    public final String name;

    public DeviceType(Response response) {
        super(response.getData()[response.getDataOffset()]);

        byte[] data = response.getData();
        int index = response.getDataOffset() + 1;
        protocol = data[index]; index += 1;
        type = data[index]; index += 1;
        model = data[index]; index += 1;
        index += 2; // mode
        version = Arrays.copyOfRange(data, index, index + 5); index += 5; // version

        name = ValueDecoder.STRING.decode(data, index, data.length - index);
    }
}
