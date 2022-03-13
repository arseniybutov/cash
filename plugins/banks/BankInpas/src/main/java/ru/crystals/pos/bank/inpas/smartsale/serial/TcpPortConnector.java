package ru.crystals.pos.bank.inpas.smartsale.serial;

import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;

public class TcpPortConnector implements SimpleSerialPortConnector {

    private final TCPPortAdapter port;

    public TcpPortConnector(String terminalIp, int tcpPort) {
        port = new TCPPortAdapter();
        port.setTcpAddress(terminalIp);
        port.setTcpPort(tcpPort);
    }

    @Override
    public SimpleSerialPortConnector setPort(String portID) {
        return this;
    }

    @Override
    public SimpleSerialPortConnector setDataBits(int dataBits) {
        return this;
    }

    @Override
    public SimpleSerialPortConnector setParity(String parity) {
        return this;
    }

    @Override
    public SimpleSerialPortConnector setStopBits(String stopBits) {
        return this;
    }

    @Override
    public SimpleSerialPortConnector setBaudRate(String baudRate) {
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
