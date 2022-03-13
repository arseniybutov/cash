package ru.crystals.pos.barcodescanner.emulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.HardwareModule;

public class BarcodeScannerEmulatorPlugin implements HardwareModule {
    private static final Logger logger = LoggerFactory.getLogger(BarcodeScannerEmulatorPlugin.class);
    public static final int LISTENING_PORT = 23221;
    public static final String CLIENT_ENCODING = "utf-8";
    private ServerSocket listeningSocket;
    private Thread socketListenerThread;
    private volatile boolean isRunning;

    @Override
    public void start() {
        logger.info("Barcode Emulator Plugin starting...");
        try {
            listeningSocket = new ServerSocket(LISTENING_PORT);
        } catch(IOException ioex) {
            logger.error("Failed to start barcode emulator service", ioex);
            return;
        }
        socketListenerThread = new Thread(()->{
            logger.info("MSR emulator service started.");
            while (isRunning) {
                try(Socket clientSocket = listeningSocket.accept()) {
                    logger.info("Client {} connected", clientSocket.getInetAddress().getHostAddress());
                    doClientSocketRoutine(clientSocket);
                    logger.info("Closing client socket {}", clientSocket.getInetAddress().getHostAddress());
                } catch(IOException ioex) {
                    logger.error("IOError", ioex);
                }
            }
        });
        isRunning = true;
        socketListenerThread.start();
        logger.info("Barcode Emulator Plugin started.");
    }

    private void doClientSocketRoutine(Socket client) throws IOException {
        int packageLength = client.getInputStream().read();
        logger.info("Package length {}", packageLength);
        if(packageLength <= 0) {
            logger.error("Invalid package length {}", packageLength);
            return;
        }
        byte[] data = new byte[packageLength];
        int bytesRead = client.getInputStream().read(data);
        logger.info("{} bytes read", bytesRead);
        String barcode = new String(data, CLIENT_ENCODING);
        logger.info("Received barcode \"{}\" from client {}", barcode, client.getInetAddress().getHostAddress());
        CashEventSource.getInstance().barcodeScanned(barcode);
    }

    @Override
    public void stop() {
        logger.info("Barcode Emulator Plugin stopping...");
        if(socketListenerThread == null) {
            return;
        }
        isRunning = false;
        try {
            listeningSocket.close();
        } catch(IOException ioex) {
            logger.error("Failed to gracefully close server socket", ioex);
        }
        listeningSocket = null;
        socketListenerThread = null;
        logger.info("Barcode Emulator Plugin stopped.");
    }
}
