package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.results.Result;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public abstract class Command<T extends Result> {
    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    public static final Charset CHARSET_CP866 = Charset.forName("cp866");
    private final byte[] code;

    public Command(int code) {
        this(new byte[] { (byte) code });
    }

    public Command(byte[] code) {
        logger.trace("{}", getClass());
        this.code = code;
    }

    public final void write(OutputStream stream) throws IOException {
        stream.write(code);
        writeData(stream);
    }

    void writeData(OutputStream stream) throws IOException {
        // empty
    }

    public T parseResult(Response response) {
        throw new UnsupportedOperationException();
    }

    static byte[] encode(String s) {
        return s.getBytes(CHARSET_CP866);
    }

    static byte[] encode(byte b) {
        return encode(b, 1);
    }

    static byte[] encode(long value, int size) {
        byte[] result = new byte[size];

        final long div = 100;
        for (int i = 0; i < size; ++i) {
            long pair = value % div;
            long d = pair / 10;
            long e = pair % 10;
            result[size - i - 1] = (byte) (d << 4 | e);
            value /= div;
        }

        return result;
    }
}
