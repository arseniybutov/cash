package ru.crystals.pos.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SerialProxyService {

    private static final Logger logger = LoggerFactory.getLogger(SerialProxyService.class);

    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String SP_SH = "sp.sh";
    private static final String SERIALPROXY = "serialproxy";
    private final Logger log;
    private final String virtualPort1;

    private final String virtualPort2;

    private static final Path SP_DIRECTORY = Paths.get("/home/tc/.serialproxy");
    private static final Path SERIALPROXY_DEST = SP_DIRECTORY.resolve(SERIALPROXY);
    private static final Path SP_SH_DEST = SP_DIRECTORY.resolve(SP_SH);

    private boolean extracted;

    public SerialProxyService(Logger log, String virtualPort1, String virtualPort2) {
        this.log = log;
        this.virtualPort1 = virtualPort1;
        this.virtualPort2 = virtualPort2;
    }

    private static synchronized void extractLibs() throws IOException {
        if (!Files.exists(SP_DIRECTORY)) {
            Files.createDirectory(SP_DIRECTORY);
        }
        extract("/linux/sp.sh", SP_SH_DEST);
        extract("/linux/serialproxy", SERIALPROXY_DEST);
    }

    private static void extract(String src, Path dest) throws IOException {
        if (Files.exists(dest)) {
            return;
        }
        try (final InputStream res = SerialProxyService.class.getResourceAsStream(src)) {
            if (res == null) {
                logger.error("No resource {} in class's jar {}", src, SerialProxyService.class);
                return;
            }
            Files.copy(res, dest, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(dest, PosixFilePermissions.fromString("rwxrwxrw-"));
        }
    }

    public void start(String realPort, String baudRate) throws PortAdapterException {
        if (!extracted) {
            try {
                extractLibs();
            } catch (IOException e) {
                log.error("Error on extract libs", e);
            }
            extracted = true;
        }

        controlService(new String[]{"sudo", "/home/tc/.serialproxy/sp.sh", START_CMD, realPort, virtualPort1, virtualPort2, baudRate});
    }

    public void stop() throws PortAdapterException {
        controlService(new String[]{"sudo", "/home/tc/.serialproxy/sp.sh", STOP_CMD, virtualPort1});
    }

    public String getVirtualPort2() {
        return virtualPort2;
    }

    private void controlService(final String[] commandForRun) throws PortAdapterException {
        log.debug("serialproxy service: {} ", argsToString(commandForRun));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> task = executor.submit(() -> new ProcessBuilder(commandForRun)
                    .directory(new File("/home/tc/.serialproxy/"))
                    .start().waitFor());
            final Integer result = task.get(5, TimeUnit.SECONDS);
            log.debug("serialproxy service: {} (complete with code {})", argsToString(commandForRun), result);
        } catch (Exception e) {
            log.error("Error on start serialproxy service {}", Arrays.toString(commandForRun), e);
            throw new PortAdapterException("Device is not connected!");
        }
    }

    private String argsToString(final String[] commandForRun) {
        if (log.isDebugEnabled()) {
            return Arrays.toString(commandForRun);
        }
        return "";
    }

}