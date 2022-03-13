package ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions;

import java.io.IOException;
import java.io.OutputStream;

public class ReqTaskStatusAction extends Action {
    private static final int CODE = 0xC3;
    private final int tid;

    public ReqTaskStatusAction(int tid) {
        super(CODE);
        this.tid = tid;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(tid);
    }
}
