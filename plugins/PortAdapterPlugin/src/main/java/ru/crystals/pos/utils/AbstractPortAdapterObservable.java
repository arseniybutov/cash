package ru.crystals.pos.utils;

import java.io.IOException;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA. User: a.gaydenger Date: 30.10.13 Time: 12:17 To change this template use File | Settings | File Templates.
 */
public abstract class AbstractPortAdapterObservable<T extends PortAdapter, M extends AbstractPortAdapter> extends Observable implements PortAdapter {
    protected Logger LOG = LoggerFactory.getLogger(PortAdapter.class);
    protected M device;
    protected volatile boolean isDeviceAvailable = false;
    protected static final long DEVICE_SEARCH_TIMEOUT = 3000;
    private Thread deviceListener;

    public AbstractPortAdapterObservable(M deviceInstance) {
        this.device = deviceInstance;
    }

    @Override
    public void openPort() throws IOException, PortAdapterException {
        try {
            device.openPort();
            notifyObservers(true);
        } catch (Exception e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(b);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return device.read(b);
        } catch (IOException e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException("");
        }
    }

    @Override
    public void close() {
        device.close();
    }

    @Override
    public int read() throws IOException {
        try {
            return device.read();
        } catch (IOException e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException("");
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        try {
            return device.readBytes();
        } catch (IOException e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException("");
        }
    }

    @Override
    public int[] readAll() throws Exception {
        try {
            return device.readAll();
        } catch (IOException e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException("");
        }
    }

    @Override
    public void write(int enq) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(enq);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
    }

    @Override
    public int[] read(int nak) throws Exception {
        try {
            return device.read(nak);
        } catch (IOException e) {
            LOG.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException(e);
        }
    }

    @Override
    public T setLogger(Logger logger) {
        this.LOG = logger;
        device.setLogger(logger);
        return (T) this;
    }

    @Override
    public int getInputStreamBufferSize() throws IOException {
        if (isDeviceAvailable) {
            try {
                return device.getInputStreamBufferSize();
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
        throw new IOException("");
    }

    protected M getDevice() {
        return device;
    }

    protected void setDevice(M device) {
        this.device = device;
    }

    @Override
    public void notifyObservers(Object obj) {
        setChanged();
        isDeviceAvailable = (Boolean) obj;
        if (deviceListener != null && !deviceListener.isInterrupted() && !(Boolean) obj) {
            deviceListener.interrupt();
        }
        super.notifyObservers(obj);
    }

    protected abstract void openPortFromObserver() throws IOException, PortAdapterException;

    protected void runDeviceObserver() {
        deviceListener = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isDeviceAvailable) {
                    try {
                        openPortFromObserver();
                        isDeviceAvailable = true;
                    } catch (Exception e) {
                        try {
                            Thread.sleep(DEVICE_SEARCH_TIMEOUT);
                        } catch (Exception e1) {
                            // DO NOTHING
                        }
                    }
                }
                notifyObservers(true);
            }
        });
        deviceListener.start();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return device.read(b, off, len);
        } catch (IOException e) {
            notifyObservers(false);
            runDeviceObserver();
            throw new IOException(e);
        }
    }

    @Override
    public void write(String message) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(message);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
    }

    @Override
    public void write(String message, String charSet) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(message, charSet);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
    }

    protected Thread getDeviceListener() {
        return deviceListener;
    }

    protected void setDeviceListener(Thread deviceListener) {
        this.deviceListener = deviceListener;
    }

    @Override
    public String readAll(String charset) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public String getPort() {
        return device.getPort();
    }
}
