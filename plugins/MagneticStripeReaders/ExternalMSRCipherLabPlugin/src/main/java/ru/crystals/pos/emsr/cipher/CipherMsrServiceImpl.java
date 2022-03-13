package ru.crystals.pos.emsr.cipher;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.emsr.NoKeyboardMSRPlugin;
import ru.crystals.pos.emsr.ResBundleExternalMSR;
import ru.crystals.pos.emsr.exception.ExternalMSRException;
import ru.crystals.pos.keyboard.MsrProcessor;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.simple.SerialPortConfiguration;
import ru.crystals.pos.utils.simple.SimpleSerialPortAdapter;

import java.util.ArrayList;
import java.util.List;

public class CipherMsrServiceImpl extends NoKeyboardMSRPlugin implements SerialPortEventListener, Configurable<CipherMsrConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CipherMsrServiceImpl.class);

    /**
     * Возвращается в конце строки с дорожками.
     */
    private static final byte CR = 0x0D;

    private final SimpleSerialPortAdapter portAdapter = new SimpleSerialPortAdapter(LOG);
    private final List<Integer> msrBuffer = new ArrayList<>();
    private final MsrProcessor msrProcessor = new MsrProcessor(new CipherMsrSentinels());

    private MSREvent msrListener;
    private CipherMsrConfig config;

    @Override
    public void start() throws CashException {
        msrListener = BundleManager.get(MSREvent.class, "keyboard");
        portAdapter.setConfiguration(SerialPortConfiguration.builder().port(config.getPort()).build());
        try {
            portAdapter.openPort();
            portAdapter.addEventListener(this);
        } catch (PortAdapterException e) {
            throw new ExternalMSRException(ResBundleExternalMSR.getString("ERROR_MODULE_IS_NOT_STARTED"), e);
        }
    }

    @Override
    public void stop() {
        portAdapter.close();
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        byte[] bytes = portAdapter.readBytes();
        if (bytes == null) {
            // Иногда в потоке не оказывается байт, хотя метод уже был вызван.
            // Байты не потерялись, просто прочитаем их в следующий раз.
            return;
        }
        for (byte b : bytes) {
            if (b == CR) {
                endOfBarcode();
                return;
            }
            msrBuffer.add((int) b);
        }
    }

    private void endOfBarcode() {
        String[] tracks = msrProcessor.getTracks(msrBuffer);
        msrListener.eventMSR(tracks[0], tracks[1], tracks[2], tracks[3]);
        msrBuffer.clear();
    }

    @NonNull
    @Override
    public Class<CipherMsrConfig> getConfigClass() {
        return CipherMsrConfig.class;
    }

    @Override
    public void setConfig(@NonNull CipherMsrConfig config) {
        this.config = config;
    }
}
