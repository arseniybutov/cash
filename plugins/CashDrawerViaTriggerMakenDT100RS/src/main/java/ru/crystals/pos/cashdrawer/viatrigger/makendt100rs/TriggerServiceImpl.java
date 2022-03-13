package ru.crystals.pos.cashdrawer.viatrigger.makendt100rs;

import java.io.IOException;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.cashdrawer.CashDrawer;
import ru.crystals.pos.cashdrawer.exception.CashDrawerException;
import ru.crystals.pos.utils.SerialPortAdapter;

/**
 * Плагин для работы с триггером денежного ящика Maken DT100-RS.
 * <p>
 * Триггер позволяет подключить к кассе денежный ящик при отсутствии фискального регистратора
 * и специального порта для ящика на системном блоке.
 * <p>
 * Триггер Maken DT100-RS включается в COM-порт (управляющий интерфейс) и USB (питание) системного блока, в триггер подключается денежный ящик.
 * <p>
 * Чтобы открыть ящик в COM-порт необходимо записать произвольные данные.
 * Состояние ящика определяется по наличию сигнала CD (Carrier Detected): ящик открыт, если сигнал есть.
 * <p>
 * Из-за примитивности интерфейса устройства не существует механизма проверки его подключенности к системе
 * (как и не существует механизма проверки подключенности ящика к триггеру).
 * <p>
 * Также отмечено, что при подключении триггера через переходник (gender changer), становится недоступным
 * состояние сигнала CD (Carrier Detected), соответственно, ящик открывается, но всегда говорит, что он закрыт
 */
public class TriggerServiceImpl implements CashDrawer {
    private static final Logger LOG = LoggerFactory.getLogger(TriggerServiceImpl.class);

    private static final String ANY_DATA_AS_OPEN_MESSAGE = "OPEN";
    /*
     * Параметр скорости работы с устройством не прописан в XML по умолчанию,
     * чтобы избежать диалога выбора скорости при настройке устройства в
     * конфигураторе (предполагается, что работать на иной скорости вряд ли кому-нибудь понадобится),
     * тем не менее может быть сконфигурирован при необходимости
     */
    private int baudRate = 9600;
    private String port;
    private SerialPortAdapter serialPort;
    /*
     * Пока в кассовой системе некорректно обрабатываются эксепшены, выброшенные
     * методами интерфейса CashDrawer (SRTECH-168), сделано тихое уведомление об ошибках (см. #throwSilentException()).
     */
    private boolean isSilentErrorHandling = true;

    public TriggerServiceImpl() {
        serialPort = new SerialPortAdapter();
    }

    @Override
    public void start() throws CashException {
        try {
            setPortParameters();
            serialPort.openPort();
        } catch (Exception e) {
            throwSilentException(new CashDrawerException("Unable to open trigger port", e));
        }
    }

    @Override
    public void stop() throws CashException {
        serialPort.close();
    }

    @Override
    public boolean openDrawer(CashDrawerOpenMode openMode) throws CashException {
        if (!serialPort.isConnected()) {
            throwSilentException(new CashDrawerException("Cash drawer has not been opened due to unavailable device"));
            return false;
        }
        try {
            serialPort.write(ANY_DATA_AS_OPEN_MESSAGE.getBytes());
            LOG.info("Cash drawer has been opened");
            return true;
        } catch (IOException e) {
            throwSilentException(new CashDrawerException("Cash drawer has not been opened due to unavailable device", e));
            return false;
        }
    }

    @Override
    public boolean isOpenDrawer() {
        if (serialPort.isConnected()) {
            boolean result = serialPort.isCD();
            LOG.trace("isOpenDrawer = " + result);
            return result;
        }
        LOG.trace("isOpenDrawer = false (null port)");
        return false;
    }

    protected void setPortParameters() {
        serialPort.setPort(port)
            .setOpenTimeOut(2000)
            .setBaudRate(baudRate)
            .setDataBits(SerialPort.DATABITS_8)
            .setStopBits(SerialPort.STOPBITS_1)
            .setParity(SerialPort.PARITY_NONE);
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public void setSilentErrorHandling(boolean isSilentErrorHandling) {
        this.isSilentErrorHandling = isSilentErrorHandling;
    }

    public void setPortAdapter(SerialPortAdapter adapter) {
        serialPort = adapter;
    }

    private void throwSilentException(CashDrawerException e) throws CashDrawerException {
        LOG.error("", e);
        if (!isSilentErrorHandling) {
            throw e;
        }
    }
}
