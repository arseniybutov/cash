package ru.crystals.pos.fiscalprinter.atol3.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Transmitter {
    private static final Logger logger = LoggerFactory.getLogger(Transmitter.class);
    private final OutputStream stream;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    Transmitter(OutputStream stream) {
        this.stream = stream;
    }

    void transmit(Packet packet) throws IOException {
        write(ControlSymbol.STX);

        write(packet.getLen() & 0x7F);
        write(packet.getLen() >> 7);
        write(packet.getId());
        writeWithByteStuffing(packet.getData());
        writeWithByteStuffing(packet.getCrc());

        flush();
    }

    void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
        buffer.close();
    }

    private void flush() throws IOException {
        logger.trace("======> {} {}", Thread.currentThread(), Packet.bufferToString(buffer.toByteArray()));

        stream.write(buffer.toByteArray());
        stream.flush();
        buffer.reset();
    }

    private void write(ControlSymbol symbol) {
        buffer.write(symbol.code);
    }

    private void write(int b) {
        buffer.write(b);
    }

    private void writeWithByteStuffing(int b) {
        b &= 0xFF;
        if (b == ControlSymbol.STX.code) {
            write(ControlSymbol.ESC);
            write(ControlSymbol.TSTX);
        } else if (b == ControlSymbol.ESC.code) {
            write(ControlSymbol.ESC);
            write(ControlSymbol.TESC);
        } else {
            write(b);
        }
    }

    private void writeWithByteStuffing(byte[] data) {
        for (byte b : data) {
            writeWithByteStuffing(b);
        }
    }
}
