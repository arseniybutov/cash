package ru.crystals.pos.utils;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class HIDDeviceAdapter extends AbstractPortAdapter<HIDDeviceAdapter> {
    private HIDDevice device;
    private static final int START_BYTE = 0x00;
    private int vendorId;
    private int productId;
    private String serialNumber;

    static {
        ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    public void write(byte[] buffer) throws IOException {
        if (device != null) {
            byte[] message = new byte[buffer.length + 1];
            message[0] = START_BYTE;
            System.arraycopy(buffer, 0, message, 1, buffer.length);
            device.write(message);
        } else {
            throw new IOException("Init device first");
        }
    }

    public void write(byte[] buffer, int packetLength, byte[] prefix) throws IOException {
        if (device != null) {
            if (packetLength > 0) {
                byte[] realPrefix = new byte[prefix.length + 1];
                realPrefix[0] = START_BYTE;
                System.arraycopy(prefix, 0, realPrefix, 1, prefix.length);
                for (int i = 0; i < buffer.length; i += packetLength) {
                    byte[] mes = Arrays.copyOfRange(buffer, i, i + packetLength > buffer.length ? buffer.length : i + packetLength);
                    byte[] message = new byte[realPrefix.length + mes.length];
                    System.arraycopy(realPrefix, 0, message, 0, realPrefix.length);
                    System.arraycopy(mes, 0, message, realPrefix.length, mes.length);
                    device.write(message);
                }
            } else {
                throw new IOException("Message part must be contains at least 1 character! Please, set correct packetLength argument");
            }
        } else {
            throw new IOException("Init device first");
        }
    }

    public void close() {
        if (device != null) {
            try {
                device.close();
            } catch (IOException e) {

            }
        }
    }

    public void openPort() throws IOException, PortAdapterException {
        if ((device = HIDManager.getInstance().openById(vendorId, productId, serialNumber)) == null) {
            throw new PortAdapterException("Device is not available");
        }
    }

    protected void setDevice(HIDDevice device) {
        this.device = device;
    }

    public int getVendorId() {
        return vendorId;
    }

    public HIDDeviceAdapter setVendorId(int vendorId) {
        this.vendorId = vendorId;
        return this;
    }

    public int getProductId() {
        return productId;
    }

    public HIDDeviceAdapter setProductId(int productId) {
        this.productId = productId;
        return this;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public HIDDeviceAdapter setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }
}
