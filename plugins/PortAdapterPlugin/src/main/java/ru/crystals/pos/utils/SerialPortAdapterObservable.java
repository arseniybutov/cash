package ru.crystals.pos.utils;

import java.io.File;
import java.io.IOException;

/**
 * Убивает кассу в момент вызова метода close предположительно проблема в библиотеке rxtx. Пока не разобрался deprecated.
 * <b>Внимание!</b>
 * Хоть и deprecated, можно использовать, если устройство работает как <b>UsbToSerial</b>.
 */
@Deprecated
public class SerialPortAdapterObservable extends AbstractPortAdapterObservable<SerialPortAdapterObservable,SerialPortAdapter> {
    private String settingsPortID;

    public SerialPortAdapterObservable(SerialPortAdapter deviceInstance) {
        super(deviceInstance);
        settingsPortID = deviceInstance.portID;
    }

    public void openPort() throws IOException, PortAdapterException {
        try {
            openPortFromObserver();
            notifyObservers(true);
        } catch (Exception e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            // обращение к библиотеке rxtx (поиск порта, попытка открыть)
            // вызывает потерю байта в данных других потоков, которые работают через rxtx  COP-652
           //  runDeviceObserver();
        }
    }
    protected void openPortFromObserver() throws IOException, PortAdapterException {
        String actualPort = getActualPortId(settingsPortID);
        device.setPort(actualPort != null ? actualPort : settingsPortID);
        device.openPort();
    }

    public boolean isCD() {
        return device.isCD();
    }

    public boolean isConnected() {
        return device.isConnected();
    }

    public SerialPortAdapterObservable setOpenTimeOut(int openTimeOut) {
        device.setOpenTimeOut(openTimeOut);
        return this;
    }

    public SerialPortAdapterObservable setSleepTime(int sleepTime) {
        device.setSleepTime(sleepTime);
        return this;
    }

    public SerialPortAdapterObservable setOwner(String owner) {
        device.setOwner(owner);
        return this;
    }

    protected String getActualPortId(String portId) {
        String resultPort = null;
        try {
            resultPort = new File(portId).getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Unable to get real port by symlink " + portId, e);
        }
        return resultPort;
    }

    public void setSettingsPortID(String portID) {
        this.settingsPortID = portID;
    }

    public SerialPortAdapterObservable setPort(String portID) {
        device.portID = portID;
        return this;
    }

    public SerialPortAdapterObservable setBaudRate(int baudRate) {
        device.baudRate = baudRate;
        return this;
    }

    public SerialPortAdapterObservable setDataBits(int dataBits) {
        device.dataBits = dataBits;
        return this;
    }

    public SerialPortAdapterObservable setStopBits(int stopBits) {
        device.stopBits = stopBits;
        return this;
    }

    public SerialPortAdapterObservable setParity(int parity) {
        device.parity = parity;
        return this;
    }

    public void setRTS(boolean b) {
        device.setRTS(b);
    }

    public String getOwner() {
        return device.owner;
    }

    public SerialPortAdapterObservable setParity(String parity) {
        device.setParity(parity);
        return this;
    }

    public SerialPortAdapterObservable setStopBits(String stopBits) {
        device.setStopBits(stopBits);
        return this;
    }

    public SerialPortAdapterObservable setBaudRate(String baudRate) {
        device.setBaudRate(baudRate);
        return this;
    }

    @Override
    public String getPort() {
        return settingsPortID;
    }
}
