package ru.crystals.pos.scale.digi.ds980;

import jpos.JposException;
import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.config.simple.SimpleEntry;
import jpos.loader.JposServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.HardwareCOMPortConfig;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;

public class DigiDs980ScalesServiceImpl extends AbstractScalePluginImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DigiDs980ScalesServiceImpl.class);

    private static final String PROVIDER_NAME = "digi_ds980";

    private HardwareCOMPortConfig config = new HardwareCOMPortConfig();

    private jpos.Scale scale;

    private static final String JPOS_ERROR_TEXT = "JavaPOS driver error";

    @Override
    public void start() {
        try {
            configureJpos();

            if (scale == null) {
                scale = new jpos.Scale();
            }

            scale.open(PROVIDER_NAME);
            scale.claim(1000);

            if (!scale.getClaimed()) {
                throw new Exception("Scales is not claimed");
            }

            scale.setDeviceEnabled(true);
        } catch (Exception e) {
            log(e);
        }
    }

    private void configureJpos() {
        JposEntry jposEntry = new SimpleEntry();

        jposEntry.addProperty(JposEntry.LOGICAL_NAME_PROP_NAME, PROVIDER_NAME);
        jposEntry.addProperty(JposEntry.SI_FACTORY_CLASS_PROP_NAME, "ru.crystals.jpos.services.scale.DigiScaleSvc112Factory");

        jposEntry.addProperty("port", config.getPort());
        jposEntry.addProperty("baudRate", config.getBaudRate());

        JposEntryRegistry registry = JposServiceLoader.getManager().getEntryRegistry();
        registry.addJposEntry(jposEntry);
    }

    @Override
    public void stop() throws CashException {
        try {
            if (scale != null) {
                scale.setDeviceEnabled(false);
                if (scale.getClaimed()) {
                    scale.release();
                }
                scale.close();
            }
        } catch (JposException e) {
            log(e);
            throw new ScaleException(JPOS_ERROR_TEXT);
        }
    }

    @Override
    public int getWeight() throws ScaleException {
        int weight;
        try {
            weight = getWeightSynchronically();
        } catch (JposException e) {
            throw new ScaleException(JPOS_ERROR_TEXT);
        }
        return weight;
    }

    @Override
    public Boolean moduleCheckState() {
        try {
            getWeightSynchronically();
            return true;
        } catch (JposException e) {
            return false;
        }
    }

    private synchronized int getWeightSynchronically() throws JposException {
        int[] data = new int[1];
        scale.readWeight(data, 1000);
        return data[0];
    }

    private void log(Exception e) {
        LOG.error("", e);
    }

    public HardwareCOMPortConfig getConfig() {
        return config;
    }

    public void setConfig(HardwareCOMPortConfig config) {
        this.config = config;
    }

    public String getPort() {
        return config.getPort();
    }

    public int getBaudRate() {
        return config.getBaudRate();
    }

    public int getDataBits() {
        return config.getDataBits();
    }

    public int getStopBits() {
        return config.getStopBits();
    }

    public void setPort(String port) {
        config.setPort(port);
    }

    public void setBaudRate(int baudRate) {
        config.setBaudRate(baudRate);
    }

    public void setDataBits(int dataBits) {
        config.setDataBits(dataBits);
    }

    public void setStopBits(int stopBits) {
        config.setStopBits(stopBits);
    }

    public int getParity() {
        return config.getParity();
    }

    public void setParity(int parity) {
        config.setParity(parity);
    }

}
