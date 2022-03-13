package ru.crystals.pos.bank.inpas.smartsale.serial;

import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;

public interface SimpleSerialPortConnector {

    SimpleSerialPortConnector setPort(String portID);

    SimpleSerialPortConnector setDataBits(int dataBits);

    SimpleSerialPortConnector setParity(String parity);

    SimpleSerialPortConnector setStopBits(String stopBits);

    SimpleSerialPortConnector setBaudRate(String baudRate);

    void openPort() throws IOException, PortAdapterException;

    void close();

    void write(byte[] b) throws IOException;

    void write(int b) throws IOException;

    int read() throws IOException;

    int getInputStreamBufferSize() throws IOException;

}
