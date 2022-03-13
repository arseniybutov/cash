package ru.crystals.pos.emsr.emulator;

import org.apache.commons.lang.StringUtils;

import javax.swing.SwingUtilities;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MSREmulatorClient {
    public static final String PARAM_NAME_GUI = "gui";
    public static final String PARAM_NAME_IP = "ip";
    public static final String PARAM_NAME_PORT = "port";
    public static final String PARAM_NAME_TRACK = "track";
    public static final String PARAM_PREFIX = "--";

    private static final int RETURN_CODE_ERROR = -1;

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        Map<String, String> parsedArguments = parseArguments(args);

        if (parsedArguments.containsKey(PARAM_NAME_GUI)) {
            System.out.println("Starting in gui mode");
            startInGuiMode(parsedArguments.get(PARAM_NAME_IP),
                    parsedArguments.containsKey(PARAM_NAME_PORT) ? Integer.parseInt(parsedArguments.get(PARAM_NAME_PORT)) : MSREmulatorService.MSR_SERVER_PORT,
                    parsedArguments.get(PARAM_NAME_TRACK + "1"),
                    parsedArguments.get(PARAM_NAME_TRACK + "2"),
                    parsedArguments.get(PARAM_NAME_TRACK + "3"),
                    parsedArguments.get(PARAM_NAME_TRACK + "4"));
            return;
        }

        if (!parsedArguments.containsKey(PARAM_NAME_IP)) {
            System.err.println("Ip not specified");
            printHelp();
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        int port = MSREmulatorService.MSR_SERVER_PORT;
        if (parsedArguments.containsKey(PARAM_NAME_PORT)) {
            try {
                port = Integer.parseInt(parsedArguments.get(PARAM_NAME_PORT));
            } catch (NumberFormatException nfe) {
                System.err.println("Incorrect port number");
                System.exit(RETURN_CODE_ERROR);
                return;
            }
        }

        if (!parsedArguments.containsKey(PARAM_NAME_TRACK + "1") &&
                !parsedArguments.containsKey(PARAM_NAME_TRACK + "2") &&
                !parsedArguments.containsKey(PARAM_NAME_TRACK + "3") &&
                !parsedArguments.containsKey(PARAM_NAME_TRACK + "4")) {
            System.err.println("No track data provided.");
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        if (StringUtils.isBlank(parsedArguments.get(PARAM_NAME_TRACK + "1")) &&
                StringUtils.isBlank(parsedArguments.get(PARAM_NAME_TRACK + "2")) &&
                StringUtils.isBlank(parsedArguments.get(PARAM_NAME_TRACK + "3")) &&
                StringUtils.isBlank(parsedArguments.get(PARAM_NAME_TRACK + "4"))) {
            System.err.println("Empty track data provided.");
            System.exit(RETURN_CODE_ERROR);
            return;
        }
        try {
            MSREmulatorClient client = new MSREmulatorClient();
            client.sendMsr(parsedArguments.get(PARAM_NAME_IP),
                    port,
                    parsedArguments.get(PARAM_NAME_TRACK + "1"),
                    parsedArguments.get(PARAM_NAME_TRACK + "2"),
                    parsedArguments.get(PARAM_NAME_TRACK + "3"),
                    parsedArguments.get(PARAM_NAME_TRACK + "4"));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(RETURN_CODE_ERROR);
        }
    }

    public void sendMsr(String host, int port, String track1, String track2, String track3, String track4) throws Exception {
        byte[] track1Bytes = track1 == null ? new byte[0] : track1.getBytes(MSREmulatorService.CLIENT_ENCODING);
        byte[] track2Bytes = track2 == null ? new byte[0] : track2.getBytes(MSREmulatorService.CLIENT_ENCODING);
        byte[] track3Bytes = track3 == null ? new byte[0] : track3.getBytes(MSREmulatorService.CLIENT_ENCODING);
        byte[] track4Bytes = track4 == null ? new byte[0] : track4.getBytes(MSREmulatorService.CLIENT_ENCODING);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1 + 4 + track1Bytes.length + track2Bytes.length + track3Bytes.length + track4Bytes.length);

        byteStream.write(4);
        byteStream.write(track1Bytes.length);
        byteStream.write(track1Bytes);

        byteStream.write(track2Bytes.length);
        byteStream.write(track2Bytes);

        byteStream.write(track3Bytes.length);
        byteStream.write(track3Bytes);

        byteStream.write(track4Bytes.length);
        byteStream.write(track4Bytes);

        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(byteStream.toByteArray());
        }
    }

    public static Map<String, String> parseArguments(String[] arguments) {
        Map<String, String> parsedParamMap = new HashMap<>();
        for (int i = 0; i < arguments.length; ++i) {
            String arg = arguments[i];
            if (arg.equals(PARAM_PREFIX + PARAM_NAME_TRACK)) {
                int tr = 1;
                for (++i; i < arguments.length; ++i) {
                    String arg2 = arguments[i];
                    if (arg2.startsWith(PARAM_PREFIX)) {
                        --i;
                        break;
                    } else {
                        parsedParamMap.put(PARAM_NAME_TRACK + String.valueOf(tr), arg2);
                    }
                    if (tr == 4) {
                        break;
                    }
                    ++tr;
                }
                continue;
            }
            if (arg.equals(PARAM_PREFIX + PARAM_NAME_GUI)) {
                parsedParamMap.put(PARAM_NAME_GUI, "true");
                continue;
            }
            if (!arg.startsWith(PARAM_PREFIX)) {
                System.err.println("Invalid argument " + arg);
                return Collections.emptyMap();
            }
            if (i + 1 == arguments.length) {
                System.err.println("No value for argument " + arg + " provided");
                return Collections.emptyMap();
            }
            ++i;
            String value = arguments[i];
            parsedParamMap.put(arg.substring(2), value);
        }
        return parsedParamMap;
    }

    private static void startInGuiMode(String host, int port, String track1, String track2, String track3, String track4) {
        SwingUtilities.invokeLater(() -> {
            MSREmulatorClient client = new MSREmulatorClient();
            MSREmulatorClientGui clientGui = new MSREmulatorClientGui(client);
            clientGui.setHost(host);
            clientGui.setPort(port);
            clientGui.setTrack1(track1);
            clientGui.setTrack2(track2);
            clientGui.setTrack3(track3);
            clientGui.setTrack4(track4);

        });
    }

    private static void printHelp() {
        System.out.println("MSR emulator");
        System.out.println("Usage: java -jar externalMSR-emulator.jar --host <host> --port <port> --track <track1> <track2> <track3> <track4>");
        System.out.println("Arguments:");
        System.out.println("--host <host>\tHost address or IP where MSR emulator is located.");
        System.out.println("--port <port>\tPort to connect to the emulator service. This is an optional parameter, default value is " + MSREmulatorService.MSR_SERVER_PORT);
        System.out.println("--gui\t Optional parameter. If it set, MSR Client Emulator starts in GUI mode.");
        System.out.println("--track\tTrack which will be sent as MSR track data. You may provide from one to four tracks. If you wanna leave, say, one track empty, " +
                "just provide it as \"\"");
    }
}
