package ru.crystals.pos.nfc.acr122;

import java.io.IOException;
import java.util.List;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для взаимодействия со считывателем бесконтактных карт ACR122UReader
 *
 * Created by v.osipov on 08.11.2017.
 */
public class ACR122UReader {
    private static final Logger logger = LoggerFactory.getLogger(NfcReaderACR122UPlugin.class);

    private static final String SMART_CARD_LIBRARY_PROPERTY = "sun.security.smartcardio.library";
    private static final String DEFAULT_SMART_CARD_LIBRARY_LOCATION = "/usr/local/lib/libpcsclite.so.1";

    private CardTerminal terminal;
    private boolean silent;
    private boolean initialized;

    /**
     * Пакет для запроса UID
     */
    private static final byte[] GET_DATA_PACKET = {
            (byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x04
    };

    /**
     * Пакет для включения сигнала
     */
    private static final byte[] TURN_ON_BUZZER_PACKET = {
            (byte) 0xFF, 0x00, 0x52, (byte) 0xFF, 0x00
    };

    /**
     * Пакет для выключения сигнала
     */
    private static final byte[] TURN_OFF_BUZZER_PACKET = {
            (byte) 0xFF, 0x00, 0x52, 0x00, 0x00
    };


    public void open(boolean silent) throws CardException {
        this.silent = silent;
        try {
            Runtime.getRuntime().exec("sudo pcscd");
            System.setProperty(SMART_CARD_LIBRARY_PROPERTY, DEFAULT_SMART_CARD_LIBRARY_LOCATION);
            Thread.sleep(500);
        } catch (IOException e) {
            logger.error("Failed to start smart-card daemon");
        } catch (InterruptedException e) {
        }
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        logger.info("NFC readers: " + terminals);
        if (CollectionUtils.isEmpty(terminals)) {
            throw new CardException("Failed to find NFC reader");
        }
        terminal = terminals.get(0);
    }

    public byte[] poll() {
        byte[] result = null;
        try {
            if (terminal.waitForCardPresent(0L)) {
                Card card = terminal.connect("*");
                CardChannel channel = card.getBasicChannel();
                ResponseAPDU r = channel.transmit(new CommandAPDU(GET_DATA_PACKET));
                result = r.getData();
                if (!initialized) {
                    channel.transmit(new CommandAPDU(silent ? TURN_OFF_BUZZER_PACKET : TURN_ON_BUZZER_PACKET));
                    initialized = true;
                }
                card.disconnect(false);
            }
        } catch (CardException e) {
            logger.warn("Exception while reading smart-card: {}", e.getMessage());
        }
        return result;
    }


    public static void main(String[] args) throws CardException {
        ACR122UReader reader = new ACR122UReader();
        reader.open(true);
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1100);
            } catch(Exception tex) {
            }

            byte[] res = reader.poll();
            if (res != null) {
                System.out.println(Hex.encodeHexString(res));
            }
        }
    }

}
