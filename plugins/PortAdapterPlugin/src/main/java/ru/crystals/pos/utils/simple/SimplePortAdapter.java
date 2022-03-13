package ru.crystals.pos.utils.simple;

import ru.crystals.pos.utils.PortAdapterException;

public interface SimplePortAdapter {

    void setConfiguration(SerialPortConfiguration configuration);

    void openPort() throws PortAdapterException;

    void close();

    void write(byte[] b) throws PortAdapterException;

}
