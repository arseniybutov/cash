package ru.crystals.pos.nfc.acr122;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cash.settings.ModuleConfigDocument;
import ru.crystals.cash.settings.PropertyDocument;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.CashException;
import ru.crystals.pos.nfc.NFCPlugin;
import ru.crystals.utils.NfcUIDConverter;

/**
 * Плагин для модуля NFC-считывателя для работы с NFC-считывателем ACR122U.
 * С точки зрения результатов работы, плагин прикидывается считывателем магнитных карт и возвращает
 * считанный UID NFC карты во второй дорожке магнитной карты.
 */
public class NfcReaderACR122UPlugin implements NFCPlugin {
    private static final Logger logger = LoggerFactory.getLogger(NfcReaderACR122UPlugin.class);

    private static final boolean PROPERTY_VALUE_SILENT_DEFAULT = false;
    private static final int PROPERTY_VALUE_PULL_DELAY_MS = 1900;

    private boolean silent = PROPERTY_VALUE_SILENT_DEFAULT;
    private int pollDelayMs = PROPERTY_VALUE_PULL_DELAY_MS;
    private ResultFormat resultFormat = ResultFormat.HEX;
    private ACR122UReader reader;
    private ExecutorService poller;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link NfcReaderACR122UPlugin}.
     * Всякий плагин ридера NFC-меток ДОЛЖЕН иметь конструктор без аргументов.
     */
    public NfcReaderACR122UPlugin() {
        reader = new ACR122UReader();
        poller = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init(ModuleConfigDocument.ModuleConfig config) {
        for(PropertyDocument.Property property : config.getPropertyArray()) {
            if(PROPERTY_NAME_SILENT.equals(property.getKey())) {
                silent = Boolean.parseBoolean(property.getValue());
            }
            if(PROPERTY_NAME_POLL_DELAY_MS.equals(property.getKey())) {
                pollDelayMs = Integer.parseInt(property.getValue());
            }
            if(PROPERTY_NAME_RESULT_FORMAT.equals(property.getKey())) {
                try {
                    resultFormat = ResultFormat.valueOf(property.getValue());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid resultFormat: {}. Using default", property.getValue());
                }
            }
        }
    }

    @Override
    public void start() throws CashException {
        logger.info("Starting {}...", this.getClass().getName());
        try {
            reader.open(silent);
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            return;
        }
        poller.execute(new NfcReaderPoller());
        logger.info("{} started.", this.getClass().getName());
    }

    @Override
    public void stop() throws CashException {
        logger.info("Stopping {}...", this.getClass().getName());
        poller.shutdown();
        logger.info("{} stopped.", this.getClass().getName());
    }

    class NfcReaderPoller implements Runnable {
        public void run() {

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if(pollDelayMs > 0) {
                        try {
                            Thread.sleep(pollDelayMs);
                        } catch(Exception tex) {
                        }
                    }

                    byte[] rec = reader.poll();

                    if (rec != null) {
                        String strUid;
                        switch (resultFormat) {
                            case DECIMAL:
                                strUid = NfcUIDConverter.convert(rec);
                                break;
                            case HEX:
                            default:
                                strUid = Hex.encodeHexString(rec);
                                break;
                        }

                        CashEventSource.getInstance().scannedMSR(null, strUid, null, null);
                    }
                } catch(Exception ex) {
                    logger.error("Failed to poll an NFC tag", ex);
                }
            }
        }
    }

}
