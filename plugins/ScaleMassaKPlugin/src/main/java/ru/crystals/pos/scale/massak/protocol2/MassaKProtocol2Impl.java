package ru.crystals.pos.scale.massak.protocol2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

public class MassaKProtocol2Impl extends AbstractScalePluginImpl {

    private static final byte GET_WEIGHT_COMMAND = 0x45;
    private static final byte GET_STATUS_COMMAND = 0x48;
    private static final int PACKET_LENGTH = 2;
    private static final Logger LOG = LoggerFactory.getLogger(MassaKProtocol2Impl.class);
    private static final long READ_TIMEOUT = 100;
    private static final int STOP_BITS = 2;
    private static final String PARITY_EVEN = "EVEN";
    private static final int BAUD_RATE = 4800;

    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();

    @Override
    public int getWeight() throws ScaleException {
        int weight = 0;
        try {
            Status status = getStatus();
            if (!status.isLowWeight() && status.isWeightStable()) {
                weight = parseWeight(runCommand(GET_WEIGHT_COMMAND));
                LOG.debug("Received " + weight + status.getMeasure().name());
                weight /= status.getMeasure().getMeasureDivider();
            } else {
                LOG.debug("Wrong status received!");
            }
        } catch (Exception e) {
            LOG.error("Failed to get weight!", e);
            throw new ScaleException(e.getMessage());
        }
        return weight;
    }

    @Override
    public Boolean moduleCheckState() {
        try {
            getStatus();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    synchronized byte[] runCommand(byte command) throws ScaleException {
        byte[] answer = null;
        try {
            serialPortAdapter.write(command);
            Thread.sleep(READ_TIMEOUT);
            answer = serialPortAdapter.readBytes();
        } catch (Exception e) {
            LOG.error("", e);
            throw new ScaleException("Can't perform command " + command);
        }
        if (answer == null || answer.length != PACKET_LENGTH) {
            throw new ScaleException("Wrong answer from Scales!");
        }
        return answer;
    }

    Status getStatus() throws Exception {
        return new Status(runCommand(GET_STATUS_COMMAND));
    }

    int parseWeight(byte[] answer) {
        int weight = 0;
        if ((answer[1] & 128) != 128) {
            return Integer.parseInt(toBinaryString(answer[1]).substring(1) + toBinaryString(answer[0]), 2);
        }
        return weight;
    }

    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.setBaudRate(BAUD_RATE).setStopBits(STOP_BITS).setParity(PARITY_EVEN).setLogger(LOG).openPort();
        } catch (Exception e) {
            LOG.error("Failed to open port", e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() throws CashException {

    }

    String toBinaryString(byte b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i >= 0; --i) {
            sb.append(b >>> i & 1);
        }
        return sb.toString();
    }

    public void setPort(String port) {
        serialPortAdapter.setPort(port);
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

}
