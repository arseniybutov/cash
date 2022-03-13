package ru.crystals.pos.utils;

import java.io.IOException;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeminders.hidapi.ClassPathLibraryLoader;

public class HIDDeviceAdapterObservable extends AbstractPortAdapterObservable<HIDDeviceAdapterObservable,HIDDeviceAdapter> {

    static {
        ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    public HIDDeviceAdapterObservable(HIDDeviceAdapter deviceInstance) {
        super(deviceInstance);
    }


    public void write(byte[] buffer) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(buffer);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        } else {
            throw new IOException("Init device first");
        }
    }

    public void write(byte[] buffer, int packetLength, byte[] prefix) throws IOException {
        if (isDeviceAvailable) {
            try {
                device.write(buffer, packetLength, prefix);
            } catch (IOException e) {
                LOG.warn("Device is not available", e);
                notifyObservers(false);
                runDeviceObserver();
            }
        } else {
            throw new IOException("Init device first");
        }
    }

    protected void openPortFromObserver() throws IOException, PortAdapterException {
        device.openPort();
    }

    public int getVendorId() {
        return device.getVendorId();
    }

    public HIDDeviceAdapterObservable setVendorId(int vendorId) {
        device.setVendorId(vendorId);
        return this;
    }

    public int getProductId() {
        return device.getProductId();
    }

    public HIDDeviceAdapterObservable setProductId(int productId) {
        device.setProductId(productId);
        return this;
    }

    /**
     * методы для тестов
     */
    protected void setDeviceAvailable(boolean deviceAvailable) {
        isDeviceAvailable = deviceAvailable;
    }

    protected boolean isDeviceAvailable() {
        return isDeviceAvailable;
    }

    public String getSerialNumber() {
        return device.getSerialNumber();
    }

    public HIDDeviceAdapterObservable setSerialNumber(String serialNumber) {
        device.setSerialNumber(serialNumber);
        return this;
    }
}
