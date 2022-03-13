package ru.crystals.pos.emsr.emulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.CashException;
import ru.crystals.pos.emsr.ExternalMSRPluginImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Эмулятор считывателя карт с магнитной полосой.
 * Работает по клиент-серверному принципу. Эмулятор представляет собой сервер, который запускается вместе с кассой, когда последняя подгрузит плагины MSR,
 * и ожидает клиента, который передаёт серверу информацию о дорожках карты. После чего эмулятор зажигает кассовое событие "Прокатана карта с магнитной полосой".
 */
public class MSREmulatorService extends ExternalMSRPluginImpl {
    private static final Logger logger = LoggerFactory.getLogger(MSREmulatorService.class);
    /**
     * Максимальная длина дорожки в байтах.
     */
    public static final int MAX_TRACK_LENGTH = 255;

    /**
     * Название имплементации эмулятора для конфига.
     */
    private static final String PROVIDER = "emulator";
    /**
     * Порт, на котором работает эмулятор MSR.
     * {@link ru.crystals.pos.emsr.ExternalMSR} не поддерживает настройки своих плагинов, поэтому зашито жестко.
     */
    public static final int MSR_SERVER_PORT = 23220;
    /**
     * Кодировка текста, в которой передаются дорожки.
     */
    public static final String CLIENT_ENCODING = "utf-8";

    private static final int MAX_TRACKS = 4;

    private List<Integer> cardPrefix1 = new ArrayList<>();
    private List<Integer> cardSuffix1 = new ArrayList<>();
    private List<Integer> cardPrefix2 = new ArrayList<>();
    private List<Integer> cardSuffix2 = new ArrayList<>();
    private List<Integer> cardPrefix3 = new ArrayList<>();
    private List<Integer> cardSuffix3 = new ArrayList<>();

    private ServerSocket listeningSocket;
    private Thread socketListenerThread;
    private volatile boolean isRunning;

    public MSREmulatorService() {
        super(PROVIDER);
        // Взято у ru.crystals.pos.emsr.posiflex.ServiceImpl.
        // У данной реализации класса не несёт смысловой нагрузки, просто заглушка,
        // чтобы ничего не поломалось.
        this.cardPrefix1.add(37);
        this.cardPrefix2.add(59);
        this.cardSuffix1.add(63);
        this.cardSuffix2.add(63);
    }

    /**
     * Запускает эмулятор MSR.
     *
     * @throws CashException если запустить эмулятор не удалось.
     */
    @Override
    public void start() throws CashException {
        logger.info("Starting MSR emulator service...");
        super.start();
        try {
            listeningSocket = new ServerSocket(MSR_SERVER_PORT);
        } catch (IOException ioex) {
            logger.error("Failed to start MSR emulator service", ioex);
            return;
        }
        socketListenerThread = new Thread(() -> {
            logger.info("MSR emulator service started.");
            while (isRunning) {
                try (Socket clientSocket = listeningSocket.accept()) {
                    logger.info("Client {} connected", clientSocket.getInetAddress().getHostAddress());
                    doClientSocketRoutine(clientSocket);
                    logger.info("Closing client socket {}", clientSocket.getInetAddress().getHostAddress());
                } catch (IOException ioex) {
                    logger.error("IOError", ioex);
                }
            }
        });
        isRunning = true;
        socketListenerThread.start();
    }

    private void doClientSocketRoutine(Socket client) throws IOException {
        int numTracks;
        List<byte[]> tracks = new ArrayList<>(MAX_TRACKS);
        numTracks = client.getInputStream().read();
        if (numTracks > MAX_TRACKS) {
            logger.error("Num tracks cannot be greater than {} ({} provided).", MAX_TRACKS, numTracks);
            return;
        }
        for (int i = 0; i < numTracks; ++i) {
            byte trackLength = (byte) client.getInputStream().read();
            trackLength &= 0xFF;
            byte[] track = new byte[trackLength];
            if (client.getInputStream().read(track) <= 0) {
                logger.error("Failed to read track {}", i + 1);
            }
            tracks.add(track);
        }
        if (tracks.isEmpty()) {
            logger.warn("No track data provided.");
            return;
        }

        CashEventSource.getInstance().scannedMSR(
                new String(tracks.get(0), CLIENT_ENCODING),
                tracks.size() > 1 ? new String(tracks.get(1), CLIENT_ENCODING) : null,
                tracks.size() > 2 ? new String(tracks.get(2), CLIENT_ENCODING) : null,
                tracks.size() > 3 ? new String(tracks.get(3), CLIENT_ENCODING) : null
        );
    }

    @Override
    public void stop() throws CashException {
        logger.info("Stopping MSR emulator service...");
        super.stop();
        if (socketListenerThread == null) {
            return;
        }
        isRunning = false;
        try {
            listeningSocket.close();
        } catch (IOException ioex) {
            logger.error("Failed to gracefully close server socket", ioex);
        }
        listeningSocket = null;
        socketListenerThread = null;
        logger.info("MSR emulator service stopped.");
    }

    @Override
    public List<Integer> getCardPrefix1() {
        return this.cardPrefix1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        return this.cardSuffix1;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        return this.cardPrefix2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        return this.cardSuffix2;
    }

    @Override
    public List<Integer> getCardPrefix3() {
        return cardPrefix3;
    }

    @Override
    public List<Integer> getCardSuffix3() {
        return cardSuffix3;
    }

    @Override
    public String[] getTracks(List<Integer> scanCodeList) {
        logger.warn("An attempt to call a not implemented method getTracks(List<Integer>) registered.");
        return new String[0];
    }
}
