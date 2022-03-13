package ru.crystals.pos.bank.zvt.protocol;

public class ZVTPacket {

    private byte[] controlField;
    private int length;
    private byte[] data;

    public ZVTPacket(byte[] controlField, int length, byte[] data) {
        this.controlField = controlField;
        this.length = length;
        this.data = data;
    }

    public byte[] getControlField() {
        return controlField;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : controlField) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        sb.append(" (").append(length).append("): ");
        for (byte b : data) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
}
