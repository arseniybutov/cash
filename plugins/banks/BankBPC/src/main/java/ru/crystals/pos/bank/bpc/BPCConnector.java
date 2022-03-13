package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.pos.utils.Timer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

public class BPCConnector {
    private static final Logger log = LoggerFactory.getLogger(BankBPCServiceImpl.class);
    private static final int TRANSACTION_TIMEOUT = 120000;
    public static final String EXECUTABLE_FILE_NAME = "ppServer";
    private static final int PPSERVER_START_TIMEOUT = 500;
    private String fullPathToProcessingFolder;
    private String processingCatalog = "banks/bpc/";
    private TCPPortAdapter portAdapter = new TCPPortAdapter();
    private String tcpAddress = "localhost";
    private int tcpPort = 16500;
    private Timer transactionTimer = new Timer("Transaction timer");

    public void connect() throws BankCommunicationException {
        portAdapter.setLogger(log);
        portAdapter.setTcpAddress(tcpAddress);
        portAdapter.setTcpPort(tcpPort);
        if (isOsWindows()) {
            log.error("ppServer is not supported on Windows");
            return;
        }
        if (checkProcessIsStarted(EXECUTABLE_FILE_NAME)) {
            killProcessWithTimeout();
        }
        startProcessWithTimeout(getFullPathToProcessingExecutable());
        Timer timer = new Timer(PPSERVER_START_TIMEOUT);
        timer.start();
        while (!checkProcessIsStarted(EXECUTABLE_FILE_NAME)) {
            if (timer.isExpired()) {
                if (checkProcessIsStarted(EXECUTABLE_FILE_NAME)) {
                    log.info("ppServer is started");
                    return;
                } else {
                    throw new BankCommunicationException(ResBundleBankBPC.getString("UNABLE_TO_START_PPSERVER"));
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    String getFullPathToProcessingExecutable() {
        return getFullPathToProcessingFolder() + '/' + "ppServer";
    }

    private String getFullPathToProcessingFolder() {
        if (fullPathToProcessingFolder == null) {
            fullPathToProcessingFolder = StringUtils.stripEnd(System.getProperty("user.dir").replace('\\', '/'), "/") + "/" +
                    StringUtils.strip(processingCatalog.replace('\\', '/'), "/") + "/" +
                    (isOsWindows() ? "windows" : "linux");
        }
        return fullPathToProcessingFolder;
    }

    void killProcessWithTimeout() {
        log.info("Kill executable: {}", EXECUTABLE_FILE_NAME);
        try {
            Runtime.getRuntime().exec("killall -9 " + EXECUTABLE_FILE_NAME).waitFor();
        } catch (Exception e) {
            log.info("", e);
        }
    }

    void startProcessWithTimeout(final String commandString) {
        log.info("Start executable: {}", commandString);
        Runnable target = () -> {
            try {
                Runtime.getRuntime().exec(commandString, null, new File(getFullPathToProcessingFolder())).waitFor();
                log.debug("Executable is finished");
            } catch (Exception e) {
                log.info("", e);
            }
        };
        Executors.newSingleThreadExecutor().submit(target);
    }

    boolean checkProcessIsStarted(String processString) {
        try {
            BufferedReader input = getInputStreamFromProcess("ps");
            String line;
            while ((line = input.readLine()) != null) {
                if (StringUtils.contains(line, processString)) {
                    input.close();
                    return true;
                }
            }
            input.close();
        } catch (IOException ioe) {
            log.warn("Unable to check if ppServer is started", ioe);
        }
        return false;
    }

    public void setProcessingCatalog(String processingCatalog) {
        this.processingCatalog = processingCatalog;
    }

    protected Map<Integer, DataByte> makeTransaction(Request request) throws BankException {
        try {
            log.debug("ppServer address: {}", portAdapter.getTcpAddress());
            portAdapter.openPort();
            portAdapter.write(request.toBytes());
            transactionTimer.restart(TRANSACTION_TIMEOUT);
            while (!Thread.interrupted() && transactionTimer.isNotExpired()) {
                waitFor(1000);
                if (portAdapter.getInputStreamBufferSize() > 0) {
                    byte[] result = new byte[portAdapter.getInputStreamBufferSize()];
                    portAdapter.read(result);
                    return parse(result);
                }
            }
        } catch (Exception e) {
            log.error("", e);
            throw new BankException(ResBundleBankBPC.getString("AUTHORIZATION_ERROR"));
        } finally {
            portAdapter.close();
        }
        return Collections.emptyMap();
    }

    void waitFor(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    Map<Integer, DataByte> parse(byte[] result) {
        return Parser.parse(result);
    }

    public void setTcpAddress(String tcpAddress) {
        this.tcpAddress = tcpAddress;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    boolean isOsWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    BufferedReader getInputStreamFromProcess(String processName) throws IOException {
        return new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(processName).getInputStream()));
    }
}
