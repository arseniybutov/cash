package ru.crystals.pos.scale.mt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by agaydenger on 12.02.2017.
 */
public class ToledoProtocol8217Impl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ToledoProtocol8217Impl.class);
    private static final int READ_TIMEOUT = 220;
    private static final byte GET_WEIGHT_COMMAND = 0x57;
    private static final byte GET_STATUS_COMMAND = 0x5A;
    private static final byte END_PACKAGE_CODE = 0x0D;
    private static final byte STX = 0x02;
    private static final int STATUS_COMMAND_ANSWER_LENGTH = 2;
    private Timer receiveAnswerTimer = new Timer(READ_TIMEOUT);
    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    private Map <Integer, Boolean> triggerLoging = new HashMap<Integer, Boolean>();


    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.setLogger(LOG).openPort();
        } catch (Exception e) {
            LOG.error("Failed to open port", e);
            throw new CashException(e);
        }
    }

    List<Byte> runCommand(byte command) throws Exception {
        serialPortAdapter.write(command);
        boolean startPacketByteReceived = false;
        List<Byte> result = new ArrayList<>();
        receiveAnswerTimer.restart();
        while (receiveAnswerTimer.isNotExpired()) {
            if (serialPortAdapter.isDataAvailable()) {
                int read = -1;
                while ((read = serialPortAdapter.read()) != -1) {
                    if (read == STX) {
                        startPacketByteReceived = true;
                    } else if (read == END_PACKAGE_CODE) {
                        return result;
                    } else if (startPacketByteReceived) {
                        result.add((byte) read);
                    }
                }
            }
            Thread.sleep(30);
        }
        throw new ScaleException("Wrong answer from Scales!");
    }

    void checkStatuses(byte statusByte) throws ScaleException {
        int trigger = 1;
        try {
            checkStatus(statusByte, trigger, "Scale in motion");
            trigger = 4;
            checkStatus(statusByte, trigger, "Scale under zero");
        } catch (ScaleException e) {
            // чтобы не мусорить в логах
            if(triggerLoging.get(trigger) == null){
                triggerLoging.put(trigger, Boolean.TRUE);
                LOG.error(e.getMessage());
            }
        }
        checkStatus(statusByte, 2, "Scale overload");
    }

    void checkStatus(byte statusByte, int target, String exceptionText) throws ScaleException {
        if ((statusByte & target) == target) {
            throw new ScaleException(exceptionText);
        }
    }

    @Override
    public void stop() throws CashException {

    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            List<Byte> answer = runCommand(GET_WEIGHT_COMMAND);
            if (answer.size() == STATUS_COMMAND_ANSWER_LENGTH) {
                checkStatuses(answer.get(1));
            } else if (answer.size() > STATUS_COMMAND_ANSWER_LENGTH) {
                triggerLoging.clear();
                return parseWeight(answer);
            }
            return 0;
        } catch (ScaleException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Problem with communication with scales.", e);
            throw new ScaleException("COMMUNICATION_ERROR");
        }
    }

    int parseWeight(List<Byte> answer) {
        StringBuilder resultBuilder = new StringBuilder();
        int result = 0;
        for (Byte aByte : answer) {
            if (Character.isDigit(aByte)) {
                resultBuilder.append(Character.getNumericValue(aByte));
            }
        }
        try {
            LOG.trace("Weight is " + resultBuilder.toString());
            result = Integer.parseInt(resultBuilder.toString());
        } catch (Exception ignore) {
            LOG.warn("Ignore this", ignore);
        }
        return result;
    }

    @Override
    public Boolean moduleCheckState() {
        boolean result = true;
        try {
            runCommand(GET_STATUS_COMMAND);
        } catch (ScaleException ignore) {
            //DO NOTHING
        } catch (Exception e) {
            LOG.warn("Module check state failed", e);
            result = false;
        }
        return result;
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

    void setSerialPortAdapter(SerialPortAdapter serialPortAdapter) {
        this.serialPortAdapter = serialPortAdapter;
    }

    void setReceiveAnswerTimer(Timer receiveAnswerTimer) {
        this.receiveAnswerTimer = receiveAnswerTimer;
    }
}
