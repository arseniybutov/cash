package ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions;

import java.io.IOException;
import java.io.OutputStream;

public class AbortAction extends Action {
    private static final int CODE = 0xC4;

    public AbortAction() {
        super(CODE);
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
    }
}
