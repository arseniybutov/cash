package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

public class BinDecoder implements ValueDecoder<Long> {
    @Override
    public Long decode(byte[] data, int from, int length) {
        long value = 0;
        int to = from + length;
        int shift = (length - 1) * 8;
        for (int i = from; i < to; ++i, shift -= 8) {
            value |= data[i] << shift;
        }

        return value;
    }
}
