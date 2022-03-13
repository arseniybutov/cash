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
 * Плагин для модуля NFC-считывателя для работы с NFC-считывателем ACR122S.
 * С точки зрения результатов работы, плагин прикидывается считывателем магнитных карт и возвращает
 * считанный UID NFC карты во второй дорожке магнитной карты.
 * @see ACR122SReader
 */
public class NfcReaderACR122SPlugin implements NFCPlugin {
    /**
     * Этот параметр определяет порт, на котором висит железка.
     */
    private static final String PROPERTY_NAME_PORT = "port";
    /**
     * Этот параметр определяет бодрейт, которым с железкой нужно разговаривать.
     */
    private static final String PROPERTY_NAME_BAUD_RATE = "baudRate";

    private static final String PROPERTY_VALUE_PORT_DEFAULT = "/dev/ttyUSB0";
    private static final int PROPERTY_VALUE_BAUD_RATE = 9600;
    private static final boolean PROPERTY_VALUE_SILENT_DEFAULT = false;
    private static final int PROPERTY_VALUE_PULL_DELAY_MS = 1900;

    private String port = PROPERTY_VALUE_PORT_DEFAULT;
    private int baudRate = PROPERTY_VALUE_BAUD_RATE;
    private boolean silent = PROPERTY_VALUE_SILENT_DEFAULT;
    private int pollDelayMs = PROPERTY_VALUE_PULL_DELAY_MS;
    private ResultFormat resultFormat = ResultFormat.DECIMAL;
    private ACR122SReader reader;
    private ExecutorService poller;
    private Logger logger;
    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link NfcReaderACR122SPlugin}.
     * Всякий плагин ридера NFC-меток ДОЛЖЕН иметь конструктор без аргументов.
     */
    public NfcReaderACR122SPlugin() {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("{} constructor called.", this.getClass().getName());
        reader = new ACR122SReader();
        poller = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init(ModuleConfigDocument.ModuleConfig config) {
        for(PropertyDocument.Property property : config.getPropertyArray()) {
            if(PROPERTY_NAME_PORT.equals(property.getKey())) {
                port = property.getValue();
            }
            if(PROPERTY_NAME_BAUD_RATE.equals(property.getKey())) {
                baudRate = Integer.parseInt(property.getValue());
            }
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

    public void start() throws CashException {
        logger.info("Starting {}...", this.getClass().getName());
        try {
            reader.open(this.port, this.baudRate);
        } catch(Exception ex) {
            logger.error("Failed to open NFC reader", ex);
            return;
        }
        poller.execute(new NfcReaderPoller());
        logger.info("{} started.", this.getClass().getName());
    }

    public void stop() throws CashException {
        logger.info("Stopping {}...", this.getClass().getName());
        poller.shutdown();
        logger.info("{} stopped.", this.getClass().getName());
    }

    class NfcReaderPoller implements Runnable {
        public void run() {
            byte[] prev = null;
            boolean process;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if(pollDelayMs > 0) {
                        try { Thread.sleep(pollDelayMs); } catch(Exception tex) { logger.error("Suddenly awake", tex);}
                    }
                    byte[] rec = reader.poll();
                    if( rec == null) {
                        prev = null;
                        continue;
                    }
                    process = prev == null;
                    if(prev != null && prev.length == rec.length) {
                        for(int i = 0; i < prev.length; ++i) {
                            if(rec[i] != prev[i]) {
                                process = true;
                                break;
                            }
                        }
                    }
                    prev = rec;
                    if(!process) {
                        continue;
                    }
                    if(!silent) {
                        reader.beep();
                    }

                    String strUid;
                    switch (resultFormat) {
                        case HEX:
                            strUid = Hex.encodeHexString(rec);
                            break;
                        case DECIMAL:
                        default:
                            strUid = NfcUIDConverter.convert(rec);
                            break;
                    }

                    CashEventSource.getInstance().scannedMSR("", strUid, "", "");


                } catch(Exception ex) {
                    logger.error("Failed to poll an NFC tag", ex);
                }
            }
            reader.close();
        }
    }

}
