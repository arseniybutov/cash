package ru.crystals.pos.fiscalprinter.shtrihminifrk.transport;

import gnu.io.SerialPort;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

/**
 * реализация {@link Transport} для фискальных регистраторов семейства "Штрих".
 * <p/>
 * NOTE: данная реализация подразумевает, что используется способ сваязи типа "полу-дуплекс" (запрос-ответ) - т.е., мы не ожидаем ответа от внешнего
 * устройства, если мы его не спрашивали: если нам пихают какие-то данные, перед тем. как мы собираемся что-то спросить - ответим ACK'ом - чтоб это
 * <em>подчиненное</em> устройство просто заткнулось и проигнорируем ответ.
 *
 * @author aperevozchikov
 */
public class ShtrihTransport extends AbstractTransport implements AutoCloseable {

    /**
     * Название последовательно порта, через который ведется информационный обмен.
     */
    private String portName = "/dev/ttyS0";

    /**
     * Скорость обмена данными через этот последовательный порт, бод
     */
    private int baudRate = 9600;

    /**
     * количество бит данных в одном байте
     */
    private int dataBits = SerialPort.DATABITS_8;

    /**
     * количество стоповых бит при передаче одного байта
     */
    private int stopBits = SerialPort.STOPBITS_1;

    /**
     * бит паритета - добавлять (чтоб "1" в "байте" всегда было четное. либо нечетное количество) или нет (без паритета - по уму)
     */
    private int parity = SerialPort.PARITY_NONE;

    @Override
    public void open() throws IOException, PortAdapterException {
        log.trace("entering open()");

        adapter = new SerialPortAdapter();
        getAdapter().setPort(portName);
        getAdapter().setBaudRate(baudRate);
        getAdapter().setDataBits(dataBits);
        getAdapter().setParity(parity);
        getAdapter().setStopBits(stopBits);

        log.trace("about to open adapter: {}", adapter);

        adapter.openPort();

        log.trace("leaving open()");
    }

    @Override
    public String toString() {
        return String.format("shtrih-transport[port: %s, rate: %s]", portName, baudRate);
    }

    // getters & setters

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public SerialPortAdapter getAdapter() {
        return (SerialPortAdapter) adapter;
    }

    public void setAdapter(SerialPortAdapter adapter) {
        this.adapter = adapter;
    }

    public long getMinEnqResponseTime() {
        return minEnqResponseTime;
    }

    public void setMinEnqResponseTime(long minEnqResponseTime) {
        this.minEnqResponseTime = minEnqResponseTime;
    }

    public int getDataSendAttempts() {
        return dataSendAttempts;
    }

    public void setDataSendAttempts(int dataSendAttempts) {
        this.dataSendAttempts = dataSendAttempts;
    }

    public int getDataReadAttempts() {
        return dataReadAttempts;
    }

    public void setDataReadAttempts(int dataReadAttempts) {
        this.dataReadAttempts = dataReadAttempts;
    }
}