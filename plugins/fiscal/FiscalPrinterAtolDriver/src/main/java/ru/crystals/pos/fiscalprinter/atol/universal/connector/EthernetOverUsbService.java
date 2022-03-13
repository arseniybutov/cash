package ru.crystals.pos.fiscalprinter.atol.universal.connector;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.atol.universal.ResBundleFiscalPrinterAtolUniversal;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class EthernetOverUsbService {
    private static final Logger logger = LoggerFactory.getLogger(EthernetOverUsbService.class);

    private static final Path SERVICE_PATH = Paths.get("/home/tc/storage/3rd-party/atol/EthOverUsb.sh");

    public static void start() throws FiscalPrinterException {
        if (SystemUtils.IS_OS_WINDOWS) {
            return;
        }
        try {
            Process process = Runtime.getRuntime().exec(String.format("sudo %s -e", SERVICE_PATH));
            if (process.waitFor(2, TimeUnit.SECONDS)) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("SERVICE_UNEXPECTED_STOP_ERROR"));
            }
            logger.info("{} started", SERVICE_PATH);
        } catch (IOException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("SERVICE_ERROR"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("SERVICE_ERROR"));
        }
    }
}
