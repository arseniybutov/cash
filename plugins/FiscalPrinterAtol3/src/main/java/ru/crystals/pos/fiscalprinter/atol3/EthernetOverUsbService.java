package ru.crystals.pos.fiscalprinter.atol3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

@Component
class EthernetOverUsbService {
    private static final Logger logger = LoggerFactory.getLogger(EthernetOverUsbService.class);
    private static final long CHECK_TIMEOUT = 1;

    private volatile boolean started;
    private final ReentrantLock lock = new ReentrantLock();

    private Process process;

    void start(Path path) throws FiscalPrinterException {
        if (started) {
            logger.debug("Already started");
            return;
        }
        if (!Files.isExecutable(path)) {
            throw new FiscalPrinterException(path + " has to be executable");
        }
        lock.lock();
        try {
            if (process != null && process.isAlive()) {
                started = true;
                return;
            }
            process = Runtime.getRuntime().exec(String.format("sudo %s -e", path));
            StreamReaderThread.execute(process.getErrorStream());
            StreamReaderThread.execute(process.getInputStream());

            if (process.waitFor(CHECK_TIMEOUT, TimeUnit.SECONDS)) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("SERVICE_UNEXPECTED_STOP_ERROR"));
            }
            started = true;
            logger.info("{} started", path);
        } catch (IOException | InterruptedException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("SERVICE_ERROR"));
        } finally {
            lock.unlock();
        }
    }

    void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private static class StreamReaderThread extends Thread {
        private final BufferedReader reader;

        StreamReaderThread(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream));
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (reader.readLine() != null) {
                    // just clear buffer
                }
            } catch (IOException e) {
                logger.error("Error while read service stream");
            }
        }

        static void execute(InputStream stream) {
            StreamReaderThread thread = new StreamReaderThread(stream);
            thread.start();
        }
    }
}
