package ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions;

import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Action {
    private static final Logger logger = LoggerFactory.getLogger(Action.class);
    private final int code;

    Action(int code) {
        logger.trace("{}", getClass());
        this.code = code;
    }

    public final void write(OutputStream stream) throws IOException {
        stream.write(code);
        writeData(stream);
    }

    abstract void writeData(OutputStream stream) throws IOException;
}
