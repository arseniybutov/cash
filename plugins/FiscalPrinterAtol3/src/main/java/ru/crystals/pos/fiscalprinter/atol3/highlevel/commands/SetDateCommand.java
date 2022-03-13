package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

public class SetDateCommand extends Command {
    private static final int CODE = 0x64;

    private final Date date;

    public SetDateCommand(Date date) {
        super(CODE);
        this.date = date;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        stream.write(encode(calendar.get(Calendar.DAY_OF_MONTH), 1));
        stream.write(encode(calendar.get(Calendar.MONTH) + 1, 1));
        stream.write(encode(calendar.get(Calendar.YEAR), 1));
    }
}
