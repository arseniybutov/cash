package ru.crystals.pos.scale.shtrih.slim200;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.SerialProxyService;

import java.io.File;
import java.io.IOException;

public class ShtrihSlim200ScaleServiceImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ShtrihSlim200ScaleServiceImpl.class);
    private static final String DEV_TTY_S_801 = "/dev/ttyS801";
    private static final String DEV_TTY_S_802 = "/dev/ttyS802";
    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    private static final int STX = 0x02;
    private static final int ENQ = 0x05;
    private static final int NAK = 0x15;
    private static final byte[] GET_WEIGHT = {STX, 0x05, 0x3A, 0x30, 0x30, 0x33, 0x30, 0x3C};
    private static final byte[] GET_CONFIG = {STX, 0x02, (byte) 0xE8, 0x00, (byte) 0xEA};
    private static final byte[] GET_CHANNEL = {STX, 0x01, (byte) 0xEA, (byte) 0xEB};
    private static int readTimeout = 100;

    // считывается из конфига, если в конфиге
    // нет, то из весов.
    private int minimalWeight = -1;
    private int totalMinimalWeight = -1;
    private int minimalWeightFromScale = -1;
    private double multiplier = 1.0;

    private SerialProxyService serialProxyService;
    private boolean useSerialProxy;
    private int baudRate;
    private String port;

    @Override
    public void start() throws CashException {
        try {
            if (useSerialProxy && serialProxyService == null) {
                serialProxyService = new SerialProxyService(LOG, DEV_TTY_S_801, DEV_TTY_S_802);
            }
            startSerialProxy();
            getAvailableAdapter().openPort();
        } catch (Exception e) {
            log(e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() {
        getAvailableAdapter().close();
        stopSerialProxy();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {

            if ((totalMinimalWeight < 0) || (minimalWeightFromScale < 0)) {
                minimalWeightFromScale = getSettingsCurrentChannel();
                LOG.debug("minimalWeight from CONFIG : " + minimalWeight);
                LOG.debug("minimalWeight from SCALES : " + minimalWeightFromScale);
                // делим на multiplier, чтобы сравнивать веса, которые реально приходят с весов: вес товара и минимальный вес
                totalMinimalWeight = (minimalWeight < 0) ? minimalWeightFromScale : (int) (minimalWeight / multiplier);
                if (totalMinimalWeight < 0) {
                    // не пришло с кассы и нет в конфиге - дефолтное значение
                    totalMinimalWeight = (int) (40 / multiplier);
                }
                LOG.debug("TOTAL minimalWeight       : " + totalMinimalWeight);
            }

            WeightData weightData = getWeightData();
            LOG.info("getWeight() result is {}", weightData);
            return (int) (multiplier * parseWeightData(weightData, totalMinimalWeight));
        } catch (Exception e) {
            tryReconnect();
            throw new ScaleException(e.getMessage());
        }
    }

    private void tryReconnect() {
        getAvailableAdapter().close();
        stopSerialProxy();
        startSerialProxy();
        try {
            getAvailableAdapter().openPort();
        } catch (Exception e) {
            LOG.error("Error on reopenPort {}", e.getMessage());
        }
    }

    private void stopSerialProxy() {
        if (!useSerialProxy || serialProxyService == null) {
            return;
        }
        try {
            serialProxyService.stop();
        } catch (Exception e) {
            LOG.error("Error on stop serialproxy {}", e.getMessage());
        }
    }

    private void startSerialProxy() {
        if (!useSerialProxy || serialProxyService == null) {
            return;
        }
        try {
            serialProxyService.start(port, String.valueOf(baudRate));
        } catch (Exception e) {
            LOG.error("Error on start serialproxy {}", e.getMessage());
        }
    }

    protected boolean validateStatus() throws Exception {
        getAvailableAdapter().write(ENQ);
        return getAvailableAdapter().read(NAK)[0] == NAK;
    }

    private synchronized WeightData getWeightData() throws Exception {
        try {
            WeightData weightData = null;
            if (validateStatus()) {
                getAvailableAdapter().write(GET_WEIGHT);
                Thread.sleep(readTimeout);
                int[] buffer = getAvailableAdapter().readAll();
                LOG.debug("Read bytes: " + Parser.intArray2String(buffer));
                weightData = Parser.parseWeight(buffer);
            }
            return weightData;
        } catch (Exception e) {
            throw e;
        }
    }

    protected synchronized int parseWeightData(WeightData weightData, int totalMinimalWeight) {
        int weight = 0;
        if ((weightData != null) && (weightData.getErrorCode() == 0) && !weightData.isOverloadScales()) {
            if (!weightData.isZeroOnStartError() && !weightData.isMeasureError() && !weightData.isLittleWeight() && !weightData.isNoAnswerADP()) {
                // Если нет перегрузки и нет ошибок, то
                /*
                 * правильный код с точки зрения логики, так как вес
                 * минимальный гарантированный вес не должен считываться из
                 * конфига, а должен браться непосредственно из самих весов
                 * if (weightData.isScalesIsStable() && weightData.isWeightIsFixed()) {
                 */
                if (weightData.isScalesIsStable() && ((weightData.getTaraWeight() + weightData.getWeight()) >= totalMinimalWeight)) {
                    // Если вес зафиксирован, то получаем вес
                    weight = weightData.getWeight();
                } else if (weightData.isScalesIsStable() && weightData.isTarePresent()) {
                    // Если весы стабилизированы и присутствует тара, но вес
                    // меньше minimalWeight (вес не зафиксирован)
                    if ((weightData.getTaraWeight() + weightData.getWeight()) >= totalMinimalWeight) {
                        // Если сумма веса тары и веса товара >= НмПВ, то
                        // получаем вес
                        weight = weightData.getWeight();
                    }
                }
            }
        }
        return Math.max(weight, 0);
    }

    private synchronized int getNumberCurrentChannel() throws Exception {
        int channel = 0;
        if (validateStatus()) {
            getAvailableAdapter().write(GET_CHANNEL);
            Thread.sleep(readTimeout);
            int[] buffer = getAvailableAdapter().readAll();
            LOG.debug("getNumberCurrentChannel Read bytes: " + Parser.intArray2String(buffer));
            channel = Parser.parseChannel(buffer);
        }
        return channel;
    }

    private synchronized int getSettingsCurrentChannel() throws Exception {
        int channel = 0;
        if (validateStatus()) {
            GET_CONFIG[3] = (byte) getNumberCurrentChannel();
            GET_CONFIG[4] = (byte) Parser.calcCRC(GET_CONFIG, 1, GET_CONFIG[1] + 1);
            getAvailableAdapter().write(GET_CONFIG);
            Thread.sleep(readTimeout);
            int[] buffer = getAvailableAdapter().readAll();
            LOG.debug("getSettingsCurrentChannel Read bytes: " + Parser.intArray2String(buffer));
            channel = Parser.parseChannelSettings(buffer);
        }
        return channel;
    }

    @Override
    public Boolean moduleCheckState() {
        try {
            return getWeightData() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void log(Exception e) {
        LOG.error("", e);
    }

    public void setPort(String port) {
        setPort(port, true);
    }

    protected void setPort(String port, boolean verifyObservable) {
        serialPortAdapter.setPort(port);
        this.port = port;
        if (StringUtils.startsWith(port, "/dev/")) {
            try {
                final String resultPort = new File(port).getCanonicalPath();
                useSerialProxy = true;
                serialPortAdapter.setPort(DEV_TTY_S_802);
                this.port = resultPort;
            } catch (IOException e) {
                LOG.warn("Unable to get real port by symlink " + port, e);
            }
        }
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        serialPortAdapter.setBaudRate(baudRate);
    }

    public void setDataBits(int dataBits) {
        serialPortAdapter.setDataBits(dataBits);
    }

    public void setStopBits(int stopBits) {
        serialPortAdapter.setStopBits(stopBits);
    }

    public void setParity(int parity) {
        serialPortAdapter.setParity(parity);
    }

    public void setSerialPortAdapter(SerialPortAdapter adapter) {
        serialPortAdapter = adapter;
    }

    public int getMinimalWeight() {
        return minimalWeight;
    }

    public void setMinimalWeight(int minimalWeight) {
        this.minimalWeight = minimalWeight;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = parseMultiplier(multiplier);
    }

    private double parseMultiplier(String multiplier) {
        try {
            multiplier = multiplier.replace(",", ".");
            double doubleMultiplier = Double.parseDouble(multiplier);
            // если multiplier = 0, используем дефолтное значение 1
            return Double.compare(doubleMultiplier, 0) == 0 ? 1 : doubleMultiplier;
        } catch (Exception e) {
            return 1;
        }
    }

    public void setReadTimeout(int readTimeout) {
        ShtrihSlim200ScaleServiceImpl.readTimeout = readTimeout;
    }

    private PortAdapter getAvailableAdapter() {
        return serialPortAdapter;
    }

    public String getPort() {
        return getAvailableAdapter().getPort();
    }

    int getTotalMinimalWeight() {
        return totalMinimalWeight;
    }
}