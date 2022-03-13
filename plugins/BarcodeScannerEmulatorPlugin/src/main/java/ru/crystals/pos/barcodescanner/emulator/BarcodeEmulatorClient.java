package ru.crystals.pos.barcodescanner.emulator;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class BarcodeEmulatorClient {

    public static final String PARAM_NAME_GUI = "gui";
    public static final String PARAM_NAME_IP = "host";
    public static final String PARAM_NAME_PORT = "port";
    public static final String PARAM_NAME_BARCODE = "barcode";
    public static final String PARAM_PREFIX = "--";

    private static final int RETURN_CODE_ERROR = -1;

    public static void main(String[] args) {
        if(args.length == 0) {
            printHelp();
            return;
        }

        Map<String, String> parsedArguments = parseArguments(args);

        if(parsedArguments.containsKey(PARAM_NAME_GUI)) {
            System.out.println("Starting in gui mode");
            startInGuiMode(parsedArguments.get(PARAM_NAME_IP),
                    parsedArguments.containsKey(PARAM_NAME_PORT) ? Integer.parseInt(parsedArguments.get(PARAM_NAME_PORT)) : BarcodeScannerEmulatorPlugin.LISTENING_PORT,
                    parsedArguments.get(PARAM_NAME_BARCODE));
            return;
        }

        if(!parsedArguments.containsKey(PARAM_NAME_IP)) {
            System.err.println("Ip not specified");
            printHelp();
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        int port = BarcodeScannerEmulatorPlugin.LISTENING_PORT;
        if(parsedArguments.containsKey(PARAM_NAME_PORT)) {
            try {
                port = Integer.parseInt(parsedArguments.get(PARAM_NAME_PORT));
            } catch (NumberFormatException nfe) {
                System.err.println("Incorrect port number");
                System.exit(RETURN_CODE_ERROR);
                return;
            }
        }

        if(!parsedArguments.containsKey(PARAM_NAME_BARCODE)) {
            System.err.println("No barcode data provided.");
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        String barcode = parsedArguments.get(PARAM_NAME_BARCODE);
        if(barcode == null || "".equals(barcode.trim())) {
            System.err.println("Empty barcode data provided.");
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        try {
            BarcodeEmulatorClient client = new BarcodeEmulatorClient();
            client.scanBarcode(parsedArguments.get(PARAM_NAME_IP), port, barcode);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(RETURN_CODE_ERROR);
        }
    }

    public static Map<String, String> parseArguments(String[] arguments) {
        Map<String, String> parsedParamMap = new HashMap<>();
        for(int i = 0; i < arguments.length; ++i) {
            String arg = arguments[i];
            if(arg.equals(PARAM_PREFIX + PARAM_NAME_GUI)) {
                parsedParamMap.put(PARAM_NAME_GUI, "true");
                continue;
            }
            if(!arg.startsWith(PARAM_PREFIX)) {
                System.err.println("Invalid argument " + arg);
                return Collections.emptyMap();
            }
            if(i + 1 == arguments.length) {
                System.err.println("No value for argument " + arg + " provided");
                return Collections.emptyMap();
            }
            ++i;
            String value = arguments[i];
            parsedParamMap.put(arg.substring(2), value);
        }
        return parsedParamMap;
    }

    private static void startInGuiMode(String host, int port, String barcode) {
        SwingUtilities.invokeLater(()->{
            BarcodeEmulatorClient client = new BarcodeEmulatorClient();
            BarcodeEmulatorClientGui clientGui = new BarcodeEmulatorClientGui(client);
            clientGui.setHost(host);
            clientGui.setPort(port);
            clientGui.setBarcode(barcode);
        });
    }

    private static void printHelp() {
        System.out.println("Barcode emulator");
        System.out.println("Usage: java -jar barcodeScanner-emulator.jar --host <host> --port <port> --barcode <barcode>");
        System.out.println("Arguments:");
        System.out.println("--host <host>\tHost address or IP where barcode emulator is located.");
        System.out.println("--port <port>\tPort to connect to the emulator service. This is an optional parameter, default value is " + BarcodeScannerEmulatorPlugin.LISTENING_PORT);
        System.out.println("--gui\t Optional parameter. If it set, Barcode Emulator Client starts in GUI mode.");
        System.out.println("--barcode\tBarcode which will be sent as barcode data.");
    }

    public void scanBarcode(String host, int port, String barcode) throws Exception {
        byte[] barcodeBytes = barcode.getBytes(BarcodeScannerEmulatorPlugin.CLIENT_ENCODING);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1 + barcodeBytes.length);

        byteStream.write(barcodeBytes.length);
        byteStream.write(barcodeBytes);

        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(byteStream.toByteArray());
        }
    }
}
