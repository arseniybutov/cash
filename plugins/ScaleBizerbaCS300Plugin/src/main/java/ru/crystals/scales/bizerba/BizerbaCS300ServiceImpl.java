package ru.crystals.scales.bizerba;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.Timer;

public class BizerbaCS300ServiceImpl extends AbstractScalePluginImpl {
    private static final Logger logger = LoggerFactory.getLogger(BizerbaCS300ServiceImpl.class);
    private static final byte EOT = 0x04;
    private static final byte STX = 0x02;
    private static final byte ESC = 0x1B;
    private static final byte ENQ = 0x05;
    private static final byte ETX = 0x03;
    private static final byte NAK = 0x15;
    private static final byte ACK = 0x06;
    private final byte[] getWeightCommand = new byte[]{ EOT, ENQ };
    private final byte[] getErrorCodeCommand = new byte[]{ EOT, STX, 0x30, 0x38, ETX }; //30 и 38 - номер соответствующей команды в ASCII (08)
    private String port = "COM1";
    private String baudRate = "9600";
    private int dataBits = SerialPortAdapter.DATABITS_7;
    private String stopBits = "1";
    private String parity = "ODD";
    private SerialPortAdapter portAdapter = new SerialPortAdapter();
    private int weight = 0;
    private long price;
    private static final long MAX_TRANSMISSION_TIMEOUT = 200L;
    private static final long MIN_TRANSMISSION_TIMEOUT = 50L;
    private long transmissionTimeout = MIN_TRANSMISSION_TIMEOUT;

    @Override
    public void start() throws CashException {
        logger.debug("start()");
        try {
            portAdapter.setPort(port).setBaudRate(baudRate).setDataBits(dataBits).setStopBits(stopBits).setParity(parity);

            portAdapter.openPort();
        } catch (Exception e) {
            throw new CashException(e);
        }
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBaudRate(String baudRate) {
        this.baudRate = baudRate;
    }

    public String getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        if (dataBits > 4 && dataBits < 9) {
            this.dataBits = dataBits;
        } else {
            this.dataBits = 7;
        }
    }

    public void setStopBits(String stopBits) {
        this.stopBits = stopBits;
    }

    public void setParity(String parity) {
        this.parity = parity;
    }

    @Override
    public int getWeighWithTransmissionOfUnitPrice(Long price) {
        try {
            transmitUnitPrice(price);
            return getWeight();
        } catch (ScaleException e) {
            logger.error("Ошибка ответа от весов");
            return weight = 0;
        }
    }

    @Override
    public void transmitUnitPrice(Long price) throws ScaleException {
        if (price == 0) {
            weight = 0;
        }
        ByteBuffer command = ByteBuffer.allocate(13); //длина пакета передачи цены
        command.put(new byte[]{ EOT, STX, (byte) 0x30, (byte) 0x31, ESC }); //30 и 31 - номер соответствующей команды в ASCII (01)
        command.put(StringUtils.leftPad(Long.toString(price), 6, "0").getBytes());
        command.put(new byte[]{ ESC, ETX });
        executePriceTransmission(command.array());
        this.price = price;
    }

    @Override
    public int getWeight() throws ScaleException {
        ErrorCode errorCode = ErrorCode.OK;
        if (!executeGetWeightCommand()) {
            errorCode = getErrorCode();
        }
        if (errorCode == ErrorCode.NO_UNIT_PRICE && price != 0) {
            transmitUnitPrice(price);
        } else if (errorCode == ErrorCode.MIN_RANGE && price == 0) {
            weight = 0;
        } else if (errorCode != ErrorCode.OK && errorCode != ErrorCode.NO_MOTION_SINCE_LAST_OPERATION) {
            logger.warn(getErrorString(errorCode));
        }
        return weight;
    }


    synchronized StringBuilder readResponse(byte[] bytes) {
        Timer timer = new Timer(transmissionTimeout);
        StringBuilder readData = new StringBuilder();
        try {
            portAdapter.readAll();
            portAdapter.write(bytes);
            timer.start();
            while (!Thread.currentThread().isInterrupted() && !isTimerExpired(timer)) {
                if (portAdapter.getInputStreamBufferSize() > 0) {
                    for (byte b : portAdapter.readBytes()) {
                        readData.append((char) b);
                        if (b == ETX || b == NAK || b == ACK) {
                            return readData;
                        }
                    }
                    Thread.sleep(2);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    ErrorCode getErrorCode() throws ScaleException {
        StringBuilder readData = readResponse(getErrorCodeCommand);
        if (readData == null || readData.charAt(0) == NAK) {
            return ErrorCode.UNKNOWN;
        }
        if (readData.length() > 5) {
            return ErrorCode.getType(Integer.valueOf(readData.substring(4, 6)));
        }
        throw new ScaleException("Неверный формат пакета ошибки");
    }

    void executePriceTransmission(byte[] command) throws ScaleException {
        StringBuilder readData = readResponse(command);
        if (readData == null || readData.charAt(0) == NAK) {
            throw new ScaleException("Ошибка при посылке цены");
        }
    }

    boolean executeGetWeightCommand() throws ScaleException {
        StringBuilder readData = readResponse(getWeightCommand);
        if (readData == null) {
            throw new ScaleException("Отсутствие ответа при запросе веса");
        }
        if (readData.charAt(0) == NAK) {
            return false;
        } else if (readData.length() > 10) {
            weight = Integer.valueOf(readData.substring(6, 11));
            return true;
        }
        throw new ScaleException("Ошибка при запросе веса");
    }

    @Override
    public void stop() throws CashException {
        logger.debug("stop()");
        portAdapter.close();
    }

    private String getErrorString(ErrorCode errorCode) {
        return ResBundleScaleBizerbaCS300.getString(errorCode.name());
    }

    @Override
    public Boolean moduleCheckState() {
        return portAdapter.isConnected();
    }

    //для тестов

    long getPrice() {
        return price;
    }

    void setPrice(long price) {
        this.price = price;
    }

    int getWeightVariable() {
        return weight;
    }

    boolean isTimerExpired(Timer timer) {
        return timer == null || timer.isExpired();
    }

    public long getTransmissionTimeout() {
        return transmissionTimeout;
    }

    public void setTransmissionTimeout(long transmissionTimeout) {
        if (transmissionTimeout < MIN_TRANSMISSION_TIMEOUT) {
            this.transmissionTimeout = MIN_TRANSMISSION_TIMEOUT;
        } else if (transmissionTimeout > MAX_TRANSMISSION_TIMEOUT) {
            this.transmissionTimeout = MAX_TRANSMISSION_TIMEOUT;
        } else {
            this.transmissionTimeout = transmissionTimeout;
        }
    }
}

