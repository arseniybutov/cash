package ru.crystals.pos.fiscalprinter.atol3.transport;

class CRC8 {
    private static final int CRC_INIT_VALUE = 0xFF;
    private static final int CRC_POLYNOM = 0x31;

    void add(byte[] data) {
        for (byte b : data) {
            add(b);
        }
    }

    private int crc = CRC_INIT_VALUE;

    void add(int data) {
        crc ^= data;

        for (int i = 0; i < 8; i++) {
            if ((crc & 0x80) != 0) {
                crc = (crc << 1) ^ CRC_POLYNOM;
            } else {
                crc <<= 1;
            }
        }
    }

    int get() {
        return crc & 0xFF;
    }
}
