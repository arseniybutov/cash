package ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.Command;

public class AddTaskAction extends Action {
    private static final int CODE = 0xC1;
    private final int id;
    private final int flags;
    private final byte[] password;
    private final Command command;

    public AddTaskAction(int id, int flags, byte[] password, Command command) {
        super(CODE);
        this.id = id;
        this.flags = flags;
        this.password = password;
        this.command = command;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(id);
        stream.write(password);
        command.write(stream);
    }
}
