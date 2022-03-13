package ru.crystals.pos.scale.mt;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapterNoConnectionException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

public class ToledoTigerImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ToledoTigerImpl.class);

    private static final long READ_TIMEOUT = 500L;
    private static final long LAST_ANSWER_MAX_AGE = 5000L;

    private static final byte GET_WEIGHT_COMMAND = 0x03;
    private static final long GET_WEIGHT_COMMAND_ANSWER_LENGTH = 18L;

    private String port = "COM2";
    private int baudRate = 4800;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_EVEN;

    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();

    private Answer lastAnswer;

    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.setPort(port);
            serialPortAdapter.setBaudRate(baudRate);
            serialPortAdapter.setDataBits(dataBits);
            serialPortAdapter.setStopBits(stopBits);
            serialPortAdapter.setParity(parity);
            serialPortAdapter.setLogger(LOG).openPort();
        } catch (Exception e) {
            LOG.error("Failed to open port", e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() throws CashException {
        serialPortAdapter.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            return getWeightSimple();
        } catch (ScaleException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Problem with communication with scales. Trying get last value...", e);
            if (lastAnswer != null && (lastAnswer.getLastWeightDate() - System.currentTimeMillis()) < LAST_ANSWER_MAX_AGE) {
                LOG.error("Return last value!");
                return lastAnswer.getWeightInt();
            }
            LOG.error("Have no last value!");
            throw new ScaleException(ResBundleScaleMTTiger.getString("COMMUNICATION_ERROR"));

        }
    }

    private int getWeightSimple() throws ScaleException, IOException, PortAdapterNoConnectionException {
        long startTime = System.currentTimeMillis();
        serialPortAdapter.write(GET_WEIGHT_COMMAND);
        String data = new String(serialPortAdapter.readBytes(READ_TIMEOUT, 220L));
        lastAnswer = new Answer(data);
        LOG.info("-------------------------->>> getWeight TIMER - " + (startTime - System.currentTimeMillis()));
        return lastAnswer.getWeightInt();
    }

    @Override
    public Boolean moduleCheckState() {
        return serialPortAdapter.isConnected();
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    private class Answer {
        private String weight;
        private String pricePerUnit;
        private String priceTotal;
        private long lastWeightDate;

        Answer(String answer) throws ScaleException {
            if (answer == null || answer.length() != GET_WEIGHT_COMMAND_ANSWER_LENGTH) {
                throw new ScaleException("Wrong answer from Scales!");
            }
            StringBuffer reverseAnswer = new StringBuffer(answer).reverse();
            this.weight = reverseAnswer.substring(12, 18);
            this.pricePerUnit = reverseAnswer.substring(6, 12);
            this.priceTotal = reverseAnswer.substring(0, 6);
            lastWeightDate = System.currentTimeMillis();
        }

        public String getWeight() {
            return weight;
        }

        public int getWeightInt() throws ScaleException {
            try {
                return Integer.parseInt(weight);
            } catch (NumberFormatException e) {
                throw new ScaleException("Wrong answer from Scales!");
            }
        }

        public String getPricePerUnit() {
            return pricePerUnit;
        }

        public String getPriceTotal() {
            return priceTotal;
        }

        public long getLastWeightDate() {
            return lastWeightDate;
        }
    }
}
