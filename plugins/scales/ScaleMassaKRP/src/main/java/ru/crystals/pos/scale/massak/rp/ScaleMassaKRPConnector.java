package ru.crystals.pos.scale.massak.rp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.scale.ResBundleScale;
import ru.crystals.pos.scale.exception.ScaleConnectionException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;

public class ScaleMassaKRPConnector {

    private static final Logger log = LoggerFactory.getLogger(ScaleMassaKRPConnector.class);

    private final ScaleMassaKRPConfig config;


    public ScaleMassaKRPConnector(ScaleMassaKRPConfig config) {
        this.config = config;
    }

    private TCPPortAdapter open() throws IOException {
        TCPPortAdapter portAdapter = new TCPPortAdapter();
        portAdapter.setTcpPort(config.getTcpPort());
        portAdapter.setTcpAddress(config.getIp());
        log.debug("Connecting to scale {}:{}...", portAdapter.getTcpAddress(), portAdapter.getTcpPort());
        portAdapter.openPort();
        return portAdapter;
    }

    public ScaleTCPSession newSession() throws ScaleConnectionException {
        try {
            final TCPPortAdapter tcp = open();
            return new ScaleTCPSession(tcp);
        } catch (IOException e) {
            log.error("Unable to connect to scale {}:{}...", config.getIp(), config.getTcpPort());
            throw new ScaleConnectionException(ResBundleScale.getString("TERMINAL_COMMUNICATION_ERROR"), e);
        }
    }
}
