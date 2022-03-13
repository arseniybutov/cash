package ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions;

import java.io.IOException;
import java.io.OutputStream;

public class AckResponseAction extends Action {
    private static final int CODE = 0xC2;
    private final int tid;

    public AckResponseAction(int tid) {
        super(CODE);
        this.tid = tid;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(tid);
    }
}
