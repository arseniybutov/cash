package ru.crystals.pos.fiscalprinter.atol3.transport;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

class Receiver implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final InputStream stream;
    private final Thread thread = new Thread(this);
    private final MailBox mailBox = new MailBox();

    private volatile boolean stopped = true;

    Receiver(InputStream stream) {
        this.stream = stream;
    }

    void start() {
        thread.setDaemon(true);
        thread.start();
    }

    // need to stop before last packet received
    void stop() {
        stopped = true;
    }

    public void run() {
        stopped = false;

        try {
            nextFrame:
            while (!stopped) {
                if (read() != ControlSymbol.STX.code) {
                    logger.trace("<----------------------- Received not STX");
                    continue;
                }

                int length = readLength();
                if (length < 0 || length > 0x7E7F) {
                    logger.trace("<----------------------- Incorrect length: {}", String.format("0x%02X", length));
                    continue;
                }

                int id = read();
                if (id != ControlSymbol.ASYNC_REPLY.code && (id < 0 || id > 0xDF)) {
                    logger.trace("<----------------------- Incorrect id: {}", String.format("0x%02X", id));
                    continue;
                }

                CRC8 crc8 = new CRC8();
                crc8.add(id);

                byte[] frame = new byte[length];

                for (int i = 0; i < length; ++i) {
                    int data = readWithByteStuffing();
                    if (data == -1) {
                        logger.trace("<----------------------- Incorrect combination ESC + X");
                        continue nextFrame;
                    }

                    frame[i] = (byte) data;
                    crc8.add(data);
                }

                int crc = readWithByteStuffing();
                int expectedCrc = crc8.get();

                if (crc != expectedCrc) {
                    logger.trace("<----------------------- Incorrect CRC8");
                    continue;
                }

                addToMailBox(id, frame);
            }
        } catch (IOException e) {
            logger.warn("Exception while read stream from fiscal printer: {}", e);
        }
    }

    private int readWithByteStuffing() throws IOException {
        int data = read();
        if (data == ControlSymbol.ESC.code) {
            data = read();
            if (data == ControlSymbol.TSTX.code) {
                return ControlSymbol.STX.code;
            } else if (data == ControlSymbol.TESC.code) {
                return ControlSymbol.ESC.code;
            } else {
                return -1;
            }
        }

        return data;
    }

    Response receive(Predicate<Response> predicate, long timeout) throws InterruptedException {
        return mailBox.get(predicate, timeout);
    }

    void clearPendingAsyncError() {
        mailBox.clearPendingAsyncError();
    }

    private void addToMailBox(int id, byte[] data) {
        logger.trace("<==== id = {} {}", String.format("%02X", id), Packet.bufferToString(data));
        mailBox.add(Response.create(id, data));
    }

    private int readLength() throws IOException {
        int low = read();
        int high = read();
        return (high << 7) | low;
    }

    private int read() throws IOException {
        int result = stream.read();

        logger.trace("<----------------------- {}", String.format("%02X", result));

        if (result == -1) {
            throw new EOFException();
        }
        return result;
    }

    void close() throws IOException {
        stream.close();
    }
}
