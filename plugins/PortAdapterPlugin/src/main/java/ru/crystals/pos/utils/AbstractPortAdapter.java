package ru.crystals.pos.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA. User: a.gaydenger Date: 30.10.13 Time: 12:11 To change this template use File | Settings | File Templates.
 */
public abstract class AbstractPortAdapter <T extends PortAdapter> implements PortAdapter{

    protected Logger LOG = LoggerFactory.getLogger(PortAdapter.class);

    @Override
    public abstract void openPort() throws IOException, PortAdapterException;

    @Override
    public void write(byte[] b) throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public int read(byte[] b) throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public abstract void close();

    @Override
    public int read() throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public byte[] readBytes() throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public int[] readAll() throws Exception{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void write(int enq) throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public int[] read(int nak) throws Exception{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public T setLogger(Logger logger){
        this.LOG=logger;
        return (T) this;
    }

    @Override
    public int getInputStreamBufferSize() throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void write(String message) throws IOException{
        // TODO добавить метод с кодировкой
        write(message.getBytes());
    }

    @Override
    public void write(String message, String charSet) throws IOException{
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public String readAll(String charset) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public boolean waitForAnyData(long waitTimeout) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public byte[] readBytes(long waitTimeout, Long byteWaitTime) throws IOException, PortAdapterNoConnectionException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public String getPort() {
        return null;
    }
}
