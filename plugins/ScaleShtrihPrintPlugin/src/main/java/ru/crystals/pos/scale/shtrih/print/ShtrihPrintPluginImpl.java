package ru.crystals.pos.scale.shtrih.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.Scale;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapterUtils;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by agaydenger on 31.10.15.
 */
public class ShtrihPrintPluginImpl implements Scale {

    private static final Logger LOG = LoggerFactory.getLogger(ShtrihPrintPluginImpl.class);
    private static final byte STX = 0x02;
    private static final byte ENQ = 0x05;
    private static final byte NAK = 0x15;
    private static final byte ACK = 0x06;
    private static final byte WEIGHT_COMMAND = 0x38;
    private static final byte[] GET_WEIGHT_PACKET = new byte[]{STX, 0x05, 0x38, 0x30, 0x30, 0x33, 0x30, 0x3E};
    private static final byte[] SET_PRICE_PACKET = new byte[]{STX, 0x09, 0x33, 0x30, 0x30, 0x33, 0x30};
    private static final int OPERATION_TIMEOUT = 300;

    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    private int minimalWeight;

    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.setOpenTimeOut(OPERATION_TIMEOUT);
            serialPortAdapter.openPort();
            sendStartJob();
        } catch (Exception e) {
            LOG.error("Failed to connect to device");
            throw new CashException(e);
        }
    }

    @Override
    public synchronized int getWeight() throws ScaleException {
        try {
            serialPortAdapter.readBytes();
            serialPortAdapter.write(GET_WEIGHT_PACKET);
            Thread.sleep(50);
            byte[] readBytes = serialPortAdapter.readBytes();
            serialPortAdapter.write(ACK);
            return parseWeight(readBytes);
        } catch (Exception e) {
            LOG.error("", e);
            throw new ScaleException("");
        }
    }

    private int parseWeight(byte[] bytes) throws ScaleException {
        if (bytes != null && bytes.length > 0) {
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == STX) {
                    if (i + 1 > bytes.length) {
                        throw new ScaleException("Wrong packet!");
                    }
                    int answerLength = bytes[i + 1];
                    if (answerLength == 4) {
                        if (bytes[i + 2] == WEIGHT_COMMAND) {
                            return ByteBuffer.wrap(Arrays.copyOfRange(bytes, i + 4, i + 6)).order(ByteOrder.LITTLE_ENDIAN).getShort();
                        } else {
                            throw new ScaleException("Unexpected answer");
                        }
                    } else if (answerLength == 3) {
                        if (bytes[i + 2] == WEIGHT_COMMAND) {
                            throw new ScaleException("Get weight command failed. Exception code is " + bytes[i + 3]);
                        } else {
                            throw new ScaleException("Unexpected answer");
                        }
                    } else {
                        LOG.error("WRONG packet " + PortAdapterUtils.arrayToString(bytes));
                        throw new ScaleException("Wrong answer ");
                    }
                }
            }
        }
        throw new ScaleException("No answer from scales");
    }

    @Override
    public Boolean moduleCheckState() {
        return serialPortAdapter.isConnected();
    }

    @Override
    public synchronized int getWeighWithTransmissionOfUnitPrice(Long price) throws ScaleException {
        transmitUnitPrice(price);
        return getWeight();
    }

    @Override
    public synchronized void transmitUnitPrice(Long price) throws ScaleException {
        ByteBuffer command = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        command.put(SET_PRICE_PACKET);
        command.putInt(price.intValue());
        command.put(calcCRC(command.array(), 1, 11));
        try {
            //Очистим буфер
            serialPortAdapter.readBytes();
            serialPortAdapter.write(command.array());
            Thread.sleep(50);
            //Без разницы что получили в ответ тупо отошлем подтверждение
            serialPortAdapter.write(ACK);
        } catch (Exception e) {
            LOG.error("Failed to transmit price", e);
            throw new ScaleException("Failed to transmit price");
        }
    }

    @Override
    public void stop() throws CashException {
        //DO NOTHING
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

    public void setSerialPortAdapter(SerialPortAdapter adapter) {
        serialPortAdapter = adapter;
    }

    public int getMinimalWeight() {
        return minimalWeight;
    }

    public void setMinimalWeight(int minimalWeight) {
        this.minimalWeight = minimalWeight;
    }

    public boolean isDeviceAvailable() {
        return serialPortAdapter.isConnected();
    }

    public String getPort() {
        return serialPortAdapter.getPort();
    }

    private byte calcCRC(byte[] bytes, int startIndex, int endIndex) throws ScaleException {
        if (bytes != null && bytes.length >= endIndex && startIndex > 0 && endIndex > startIndex) {
            byte CRC = 0x00;
            for (int i = startIndex; i < endIndex; i++) {
                CRC = (byte) (CRC ^ bytes[i]);
            }
            return CRC;
        } else {
            throw new ScaleException("Error calc CRC");
        }
    }

    private void sendStartJob() throws Exception {
        serialPortAdapter.write(ENQ);
        Thread.sleep(50);
        serialPortAdapter.readBytes();
    }
}
