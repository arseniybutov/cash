package ru.crystals.pos.scale.rbs;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.math.BigDecimal;
import java.util.Arrays;

public class RBSCS2011ScaleImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(RBSCS2011ScaleImpl.class);

    private static final long LAST_ANSWER_MAX_AGE = 5000L;

    private String port = "COM2";
    private int baudRate = 9600;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;

    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();

    private long lastWeightDate;

    @Override
    public void start() throws CashException {
        try {
            serialPortAdapter.setPort(port);
            serialPortAdapter.setBaudRate(baudRate);
            serialPortAdapter.setDataBits(dataBits);
            serialPortAdapter.setStopBits(stopBits);
            serialPortAdapter.setParity(parity);
            serialPortAdapter.setLogger(LOG).openPort();
            lastWeightDate = System.currentTimeMillis();
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
            //Весы постоянно посылают текущий "определенный" вес, вычитыывем данные с порта.
            byte[] byteData = serialPortAdapter.readBytes();
            int result = parseWeightData(byteData);
            LOG.debug("getWeight() The result is: {}", result);
            return result;
        } catch (ScaleException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error with scales connection: ", e);
            throw new ScaleException(ResBundleScaleRBS.getString("COMMUNICATION_ERROR"));
        }
    }


    @Override
    public Boolean moduleCheckState() {
        return serialPortAdapter.isConnected();
    }

    /**
     * Получение веса в граммах из пакета данных принятого с весов.
     *
     * @param weightData данные считанные с весов, если данные невалидные parseWeightData вернет 0
     * @return вес в граммах
     * @throws ScaleException исключение выбрасывается при отсутствии данных более 5 секунд
     */
    private int parseWeightData(byte[] weightData) throws ScaleException {

        int weightDataLength = 7; // 7 - максимальная длинна данных о весе
        int skipDataLength = 3;

        if (weightData.length < weightDataLength) {
            if ((System.currentTimeMillis() - lastWeightDate) > LAST_ANSWER_MAX_AGE) {
                throw new ScaleException(ResBundleScaleRBS.getString("COMMUNICATION_ERROR"));
            }
            return 0;
        }

        int weightBlockDelimiter = 0x3A;
        int endPacketMarker = 0x0A;
        int startIndex = -1;
        int endIndex = -1;
        for (int i = weightData.length - 1; i >= 0; i--) {
            if (weightData[i] == endPacketMarker) {
                endIndex = i;
            }
            if (weightData[i] == weightBlockDelimiter) {
                startIndex = i;
            }
            if (endIndex < startIndex) {
                endIndex = -1;
                startIndex = -1;
            }
            //найдены маркеры пакета с данными о весе
            if (startIndex > 0 && endIndex > 0) {
                int weightValue = getWeightValue(weightData, startIndex + 1, endIndex - skipDataLength);
                if (weightValue >= 0) {
                    lastWeightDate = System.currentTimeMillis();
                    return weightValue;
                }
                if ((System.currentTimeMillis() - lastWeightDate) > LAST_ANSWER_MAX_AGE) {
                    throw new ScaleException(ResBundleScaleRBS.getString("COMMUNICATION_ERROR"));
                }
            }
        }

        return 0;
    }

    /**
     * Пакет данных может быть в виде ��0.005kg
     * надо пропутсить неиспользуемые символы и спарсить данные веса
     */
    private int getWeightValue(byte[] weightData, int startIndex, int endIndex) {
        int skipDataLength = 0;
        int emptyValue = 0xA0;
        //узнаем сколько неиспользуемых символов пропустить
        if ((weightData[startIndex] & 0xFF) == emptyValue) {
            skipDataLength++;
        }
        if ((weightData[startIndex + 1] & 0xFF) == emptyValue) {
            skipDataLength++;
        }

        if (startIndex + skipDataLength < endIndex && weightData.length > endIndex) {
            byte[] range = Arrays.copyOfRange(weightData, startIndex + skipDataLength, endIndex);

            for (int i = 0; i < range.length; i++) {
                range[i] = convertDeviceByte(range[i]);
            }

            String weightValueStr = new String(range);
            try {
                LOG.debug("weight data raw: {}", weightValueStr);
                BigDecimal decimalValue = new BigDecimal(weightValueStr);
                BigDecimal grammMultiplier = new BigDecimal(1000);
                return decimalValue.multiply(grammMultiplier).intValue();
            } catch (Exception e) {
                LOG.error("Parse error {} {}", e, weightValueStr);
                return -1;
            }
        }
        return -1;
    }

    /**
     * Весы возвращают 1, 2, 4, 7, 8, 9, как 0xb1, 0xb2, 0xb4, 0xb7, 0xb8, 0xb9
     * Нужно конверировать эти символы
     */
    private byte convertDeviceByte(byte value) {
        if (value < 0) {
            return Integer.valueOf(value + 128).byteValue();
        }
        return value;
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
}
