package ru.crystals.pos.utils.simple;

import org.slf4j.Logger;
import ru.crystals.pos.utils.PortAdapterException;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SimpleSerialPortAdapterObservable extends Observable implements SimplePortAdapter {

    private static final long DEVICE_SEARCH_PERIOD = TimeUnit.SECONDS.toMillis(3);

    private final Logger log;

    private final SimplePortAdapter device;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean isDeviceAvailable;

    private volatile ScheduledFuture<?> observerFuture;

    public SimpleSerialPortAdapterObservable(SimplePortAdapter device, Logger log) {
        this.device = device;
        this.log = log;
    }

    @Override
    public void setConfiguration(SerialPortConfiguration configuration) {
        device.setConfiguration(configuration);
    }

    @Override
    public void openPort() {
        try {
            openPortFromObserver();
            notifyObservers(true);
        } catch (Exception e) {
            log.warn("Device is not available", e);
            notifyObservers(false);
            runDeviceObserver();
        }
    }

    @Override
    public void close() {
        device.close();
    }

    @Override
    public void write(byte[] b) throws PortAdapterException {
        if (isDeviceAvailable) {
            try {
                device.write(b);
            } catch (PortAdapterException e) {
                log.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        }
    }

    @Override
    public void notifyObservers(Object obj) {
        setChanged();
        isDeviceAvailable = (Boolean) obj;
        super.notifyObservers(obj);
    }

    private void openPortFromObserver() throws PortAdapterException {
        device.openPort();
    }

    private void runDeviceObserver() {
        scheduleObserverIfNotYetScheduled();
    }

    private void tryOpenPort() {
        synchronized (this) {
            observerFuture = null;
            if (!isDeviceAvailable) {
                try {
                    openPortFromObserver();
                    isDeviceAvailable = true;
                } catch (Exception e) {
                    scheduleObserverIfNotYetScheduled();
                }
            }
            notifyObservers(true);
        }
    }

    private void scheduleObserverIfNotYetScheduled() {
        if (observerFuture == null) {
            observerFuture = scheduleObserver();
        }
    }

    private ScheduledFuture<?> scheduleObserver() {
        return executorService.schedule(this::tryOpenPort, SimpleSerialPortAdapterObservable.DEVICE_SEARCH_PERIOD, TimeUnit.MILLISECONDS);
    }
}
