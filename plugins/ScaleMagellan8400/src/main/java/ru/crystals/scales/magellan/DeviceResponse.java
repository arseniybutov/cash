package ru.crystals.scales.magellan;

import java.util.Objects;

public class DeviceResponse {

    final DeviceError error;
    final int weight;
    final String barcode;

    private DeviceResponse(DeviceError error, String barcode, int weight) {
        this.error = error;
        this.barcode = barcode;
        this.weight = weight;
    }

    public static DeviceResponse barcode(String barcode) {
        return new DeviceResponse(DeviceError.OK, barcode, 0);
    }

    public static DeviceResponse weight(int weight) {
        return new DeviceResponse(DeviceError.OK, null, weight);
    }

    public static DeviceResponse weightError(DeviceError error) {
        return new DeviceResponse(error, null, 0);
    }

    public boolean isBarcode() {
        return barcode != null;
    }

    public boolean isWeight() {
        return !isBarcode();
    }

    public DeviceError getError() {
        return error;
    }

    public int getWeigth() {
        return weight;
    }

    public String getBarcode() {
        return barcode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeviceResponse that = (DeviceResponse) o;
        return weight == that.weight &&
                error == that.error &&
                Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, weight, barcode);
    }

    @Override
    public String toString() {
        return "DeviceResponse{" +
                "error=" + error +
                ", weight=" + weight +
                ", barcode='" + barcode + '\'' +
                '}';
    }
}
