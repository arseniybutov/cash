package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import ru.crystals.pos.fiscalprinter.atol3.StateMode;

public class SetModeCommand extends Command {
    private static final int PASSWORD_LENGTH = 4;
    private static final int CODE = 0x56;

    private final StateMode mode;
    private final String password;

    public SetModeCommand(StateMode mode, String password) {
        super(CODE);

        if (password.length() > PASSWORD_LENGTH) {
            throw new IllegalArgumentException();
        }

        this.mode = mode;
        this.password = password;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(mode.ordinal());
        stream.write(encode(Long.parseLong(password), PASSWORD_LENGTH));
    }
}
