package ru.crystals.pos.scale.cas.ad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.math.BigDecimal;

public class CasScalesServiceImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(CasScalesServiceImpl.class);
    private SerialPortAdapter portAdapter = new SerialPortAdapter();
    private static final int ENQ = 0X5;
    private static final int ACK = 0X6;
    private static final int DC1 = 0X11;
    private static final int EOT = 0X4;

    @Override
    public void start() throws CashException {
        try {
            portAdapter.openPort();
        } catch (Exception e) {
            log(e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() {
        portAdapter.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            int weight = 0;
            WeightData weightData = getWeightData();
            if ((weightData != null) && weightData.isStable() && !weightData.isNegativeWeight()) {
                weight = getWeightInGrams(weightData.getWeight());
            }
            return weight;
        } catch (Exception e) {
            throw new ScaleException(e.getMessage());
        }
    }

    private synchronized WeightData getWeightData() throws Exception {
        WeightData weightData = null;
        portAdapter.write(ENQ);
        int[] rawData = portAdapter.read(ACK);
        if (rawData[0] == ACK) {
            portAdapter.write(DC1);
            rawData = portAdapter.read(EOT);
            if (LOG.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (int i : rawData) {
                    sb.append(String.format("0x%02X", i & 0xFF)).append(" ");
                }
                LOG.debug(sb.toString());
            }
            weightData = Parser.parse(rawData);
            LOG.debug("getWeightData() result is {}", weightData);
            if (!weightData.getUnit().endsWith("g")) {
                LOG.error("Wrong unit of weight: " + weightData.getUnit());
                throw new Exception("Wrong unit of weight");
            }
        }
        return weightData;
    }

    private static int getWeightInGrams(String weight) {
        if (weight.contains(" ") && weight.contains(".")) {
            return new BigDecimal(weight.replaceAll(" ", "")).movePointRight(3).intValue();
        }
        String weightInGramms = weight.replaceAll("\\.", "");
        return Integer.parseInt(weightInGramms.trim());
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
        portAdapter.setPort(port);
    }

    public void setBaudRate(int baudRate) {
        portAdapter.setBaudRate(baudRate);
    }

    public void setDataBits(int dataBits) {
        portAdapter.setDataBits(dataBits);
    }

    public void setStopBits(int stopBits) {
        portAdapter.setStopBits(stopBits);
    }

    public void setParity(int parity) {
        portAdapter.setParity(parity);
    }

    public void setPortAdapter(SerialPortAdapter adapter) {
        portAdapter = adapter;
    }
}
