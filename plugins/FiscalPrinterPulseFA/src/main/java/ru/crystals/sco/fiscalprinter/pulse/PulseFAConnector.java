package ru.crystals.sco.fiscalprinter.pulse;

import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PingStatus;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;

public class PulseFAConnector extends PiritConnector {

    private Socket socket;

    private String ip;

    private int port;

    private static final Logger log = LoggerFactory.getLogger(PulseFAConnector.class);

    public PulseFAConnector(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public synchronized void reconnect() throws FiscalPrinterException {
        try {
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
            socket = new Socket(ip, port);

            // создание потоков
            initStreams(socket.getInputStream(), socket.getOutputStream());
        } catch (Exception e) {
            log.error("", e);
            throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinter.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
        }
    }

    @Override
    public void close() {
        try {
            closeStreams();
        } catch (Exception e) {
            log.error("Error while close streams", e);
        }

        try {
            socket.close();
        } catch (Exception e) {
            log.error("Error while close streams", e);
        }
    }

    @Override
    public PingStatus isPiritOnline() {//TODO: дримкасс обещал скоро добавить команду 0x05
        try {
            if (socket != null && socket.isConnected() && InetAddress.getByName(ip).isReachable(1000)) {
                return new PingStatus(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new PingStatus(false);
    }
}
