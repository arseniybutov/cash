package ru.crystals.pos.scale.bizerba.ecoasia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

public class BizebraEcoAsiaScalesServiceImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BizebraEcoAsiaScalesServiceImpl.class);
    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    private static long minimalTimeOut = 3000; // Минимальный TimeOut между запросами на весы.
    private static int readTimeout = 100;
    private String weightMessage = "eco000000";

    private boolean isDeviceAvailable = true;
    private long curTime = 0;

    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.openPort();
        } catch (Exception e) {
            log(e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() {
        serialPortAdapter.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            int weight = 0;

            WeightData weightData = getWeightData();
            LOG.info("getWeight() result is {}", weightData);

            if ((weightData != null) && (weightData.isScalesIsStable())) {
                // Если вес зафиксирован, то получаем вес
                weight = weightData.getWeight();
            }
            return Math.max(weight, 0);
        } catch (Exception e) {
            throw new ScaleException(e.getMessage());
        }
    }

    private boolean validateStatus() throws Exception {
        if (isDeviceAvailable) {
            if (System.currentTimeMillis() - curTime > minimalTimeOut) {
                curTime = System.currentTimeMillis();
                return true;
            }
        }
        throw new Exception("Device is not connected!");
    }

    private synchronized WeightData getWeightData() throws Exception {
        WeightData weightData = null;
        if (validateStatus()) {
            serialPortAdapter.write(weightMessage.getBytes());
            Thread.sleep(readTimeout);
            int[] buffer = serialPortAdapter.readAll();
            LOG.debug("Read bytes: " + Parser.intArray2String(buffer));
            weightData = Parser.parseWeight(buffer);
        }
        return weightData;
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
        String resultPort = port;
        serialPortAdapter.setPort(resultPort);
    }

    public void setBaudRate(int baudRate) {
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

    public void setReadTimeout(int readTimeout) {
        BizebraEcoAsiaScalesServiceImpl.readTimeout = readTimeout;
    }

    public String getWeightMessage() {
        return weightMessage;
    }

    public void setWeightMessage(String weightMessage) {
        this.weightMessage = weightMessage;
    }
}
