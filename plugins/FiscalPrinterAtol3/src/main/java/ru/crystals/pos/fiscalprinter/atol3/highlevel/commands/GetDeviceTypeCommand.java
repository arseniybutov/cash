package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.DeviceType;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class GetDeviceTypeCommand extends Command<DeviceType> {
    private static final int CODE = 0xA5;

    public GetDeviceTypeCommand() {
        super(CODE);
    }

    @Override
    public DeviceType parseResult(Response response) {
        return new DeviceType(response);
    }
}
