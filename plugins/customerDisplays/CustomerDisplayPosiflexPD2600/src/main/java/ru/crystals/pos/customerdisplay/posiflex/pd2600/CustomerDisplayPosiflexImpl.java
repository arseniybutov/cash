package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.utils.HIDDeviceAdapter;
import ru.crystals.pos.utils.HIDDeviceAdapterObservable;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.Observer;

public class CustomerDisplayPosiflexImpl extends AbstractCustomerDisplayPosiflexImpl implements Observer {
    private String vendorId = "0D3A";
    private String productId = "0200";
    private HIDDeviceAdapterObservable adapter;
    private static final int MAX_CHAR_COUNT_PER_PACKET = 3;

    @Override
    public void executeCommand(byte[] buffer) throws CustomerDisplayPluginException {
        executeCommand(buffer, new byte[]{ESC});
    }

    @Override
    public void executeCommand(byte[] buffer, byte[] prefix) throws CustomerDisplayPluginException {
        if (isDeviceConnected) {
            try {
                ((HIDDeviceAdapterObservable) getAdapter()).write(buffer, MAX_CHAR_COUNT_PER_PACKET, prefix);
            } catch (Exception e) {
                throw new CustomerDisplayPluginException(e.getMessage());
            }
        } else {
            throw new CustomerDisplayPluginException("Device is not connected");
        }
    }

    @Override
    protected PortAdapter getAdapter() throws IOException, PortAdapterException {
        if (adapter == null) {
            try {
                adapter = new HIDDeviceAdapterObservable(new HIDDeviceAdapter()).setProductId(Integer.valueOf(productId, 16))
                        .setVendorId(Integer.valueOf(vendorId, 16)).setSerialNumber(null);
            } catch (NumberFormatException e) {
                throw new IOException("Wrong parameters in config file");
            }
            adapter.addObserver(this);
        }
        return adapter;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Изврат для тестов
     */

    protected void setAdapter(HIDDeviceAdapterObservable adapter) {
        this.adapter = adapter;
    }

}
