package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

public class SetTimeCommand extends Command {
    private static final int CODE = 0x4B;

    private final Date time;

    public SetTimeCommand(Date time) {
        super(CODE);
        this.time = time;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        stream.write(encode(calendar.get(Calendar.HOUR_OF_DAY), 1));
        stream.write(encode(calendar.get(Calendar.MINUTE), 1));
        stream.write(encode(calendar.get(Calendar.SECOND), 1));
    }
}
