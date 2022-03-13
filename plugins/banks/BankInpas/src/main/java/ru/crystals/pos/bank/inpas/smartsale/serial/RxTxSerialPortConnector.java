package ru.crystals.pos.bank.inpas.smartsale.serial;

import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

public class RxTxSerialPortConnector implements SimpleSerialPortConnector {

    public static final String ID = "rxtx";

    private final SerialPortAdapter port;

    public RxTxSerialPortConnector() {
        port = new SerialPortAdapter();
    }

    @Override
    public RxTxSerialPortConnector setPort(String portID) {
        port.setPort(portID);
        return this;
    }

    @Override
    public RxTxSerialPortConnector setDataBits(int dataBits) {
        port.setDataBits(dataBits);
        return this;
    }

    @Override
    public RxTxSerialPortConnector setStopBits(String stopBits) {
        port.setStopBits(stopBits);
        return this;
    }

    @Override
    public RxTxSerialPortConnector setBaudRate(String baudRate) {
        port.setBaudRate(baudRate);
        return this;
    }

    @Override
    public RxTxSerialPortConnector setParity(String parity) {
        port.setParity(parity);
        return this;
    }

    @Override
    public void openPort() throws IOException, PortAdapterException {
        port.openPort();
    }

    @Override
    public void write(byte[] b) throws IOException {
        port.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        port.write(b);
    }

    @Override
    public int read() throws IOException {
        return port.read();
    }

    @Override
    public int getInputStreamBufferSize() throws IOException {
        return port.getInputStreamBufferSize();
    }

    @Override
    public void close() {
        port.close();
    }

}
