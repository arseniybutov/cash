package ru.crystals.pos.scale.massak.rp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.utils.time.Timer;

import java.io.IOException;
import java.time.Duration;

public class ScaleTCPSession implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ScaleTCPSession.class);

    private static final byte[] EMPTY_RESPONSE = new byte[0];

    private final TCPPortAdapter port;

    private final Timer readTimer = Timer.of(Duration.ofSeconds(5));

    public ScaleTCPSession(TCPPortAdapter port) {
        log.debug("Connected to scale {}:{}", port.getTcpAddress(), port.getTcpPort());
        this.port = port;
    }

    @Override
    public void close() {
        port.close();
        log.debug("Disconnected from scale {}:{}", port.getTcpAddress(), port.getTcpPort());
    }

    public byte[] sendAndRead(byte[] request) throws IOException {
        port.readBytes();
        port.write(request);
        readTimer.restart();
        while (readTimer.isNotExpired() && !Thread.currentThread().isInterrupted()) {
            final byte[] answer = port.readBytes();
            if (answer.length > 0) {
                // Поскольку у нас все-таки TCP, то тут закладываемся на то, что ответ приходит весь целиком
                // и если уж что-то вычитали, то это полный ответ. По тем же соображениям забиваем на CRC.
                return answer;
            }
        }
        return EMPTY_RESPONSE;
    }
}
