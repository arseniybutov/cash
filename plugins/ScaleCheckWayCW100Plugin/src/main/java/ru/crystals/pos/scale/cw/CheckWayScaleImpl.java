package ru.crystals.pos.scale.cw;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckWayScaleImpl extends AbstractScalePluginImpl {
    private static final Logger LOG = LoggerFactory.getLogger(CheckWayScaleImpl.class);

    private static final int STX = 0x02;
    private static final byte[] GET_WEIGHT = { STX, 0x0D, 0x0A };
    private static final long READ_TIMEOUT = 100L;
    /**
     * Паттерн строки формата: Gyyy.xxx
     * Где G - режим весов, yyy - килограммы, xxx - граммы.
     * Паттерн выдает последннее совпадение в строке.
     */
    private static final String WEIGHT_DATA_PATTERN = ".*(?<prefix>.)(?<kg>\\d{3})[.](?<gram>\\d{3})";
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
            //Весы постоянно посылают текущий "оределенный" вес, но для случаев, когда данные с порта
            //уже были считаны нужен запрос GET_WEIGHT с ожиданием данных на порту.
            serialPortAdapter.write(GET_WEIGHT);
            //Ожидание данных на порту не выдает ошибок при их отсутствии,
            //отсутствие данных может означать что весы еще "стабилизируются"
            serialPortAdapter.waitForAnyData(READ_TIMEOUT);
            String data = new String(serialPortAdapter.readBytes());
            LOG.debug("weight data: {}", data);
            int result = parseWeightData(data);
            LOG.debug("getWeight() The result is: {}", result);
            return result;
        } catch (ScaleException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error with scales connection: ", e);
            throw new ScaleException(ResBundleScaleCheckWay.getString("COMMUNICATION_ERROR"));
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return serialPortAdapter.isConnected();
    }

    /**
     * Получение веса в граммах из пакета данных принятого с весов.
     * @param weightData данные считанные с весов, если строка пустая parseWeightData вернет 0
     * @return вес в граммах
     * @throws ScaleException исключение выбрасывается при отсутствии данных более 5 секунд
     */
    private int parseWeightData(String weightData) throws ScaleException {
        Pattern pattern = Pattern.compile(WEIGHT_DATA_PATTERN);
        Matcher matcher = pattern.matcher(weightData);
        if(matcher.find()) {
            lastWeightDate = System.currentTimeMillis();
            return Integer.parseInt(matcher.group("gram")) + (Integer.parseInt(matcher.group("kg")) * 1000);
        }
        if ((System.currentTimeMillis() - lastWeightDate) > LAST_ANSWER_MAX_AGE) {
            throw new ScaleException(ResBundleScaleCheckWay.getString("COMMUNICATION_ERROR"));
        }
        return 0;
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
