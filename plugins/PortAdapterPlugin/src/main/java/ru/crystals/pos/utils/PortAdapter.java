package ru.crystals.pos.utils;

import java.io.IOException;

import org.slf4j.Logger;

import gnu.io.SerialPort;

/**
 * Created with IntelliJ IDEA. User: agaydenger Date: 05.09.13 Time: 17:45 To change this template use File | Settings | File Templates.
 */
public interface PortAdapter {
    int DATABITS_7 = SerialPort.DATABITS_7;
    int DATABITS_5 = SerialPort.DATABITS_5;
    int DATABITS_6 = SerialPort.DATABITS_6;
    int DATABITS_8 = SerialPort.DATABITS_8;

    void openPort() throws IOException, PortAdapterException;

    void write(byte[] b) throws IOException;

    int read(byte[] b) throws IOException;

    void close();

    int read() throws IOException;

    byte[] readBytes() throws IOException;

    int[] readAll() throws Exception;

    void write(int enq) throws IOException;

    int[] read(int nak) throws Exception;

    <T extends PortAdapter> T setLogger(Logger logger);

    int getInputStreamBufferSize() throws IOException;

    int read(byte[] b, int off, int len) throws IOException;

    void write(String message) throws IOException;

    void write(String message, String charSet) throws IOException;

    String readAll(String charset) throws IOException;

    String getPort();
}
