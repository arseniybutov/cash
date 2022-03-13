package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

public class LongDecoder implements ValueDecoder<Long> {
    @Override
    public Long decode(byte[] data, int from, int length) {
        long value = 0;
        long mul = 1;
        int to = from + length;
        for (int i = to - 1; i >= from; --i) {
            long d = (data[i] >> 4) & 0xF;
            long e = data[i] & 0xF;
            value += (d * 10 + e) * mul;
            mul *= 100;
        }

        return value;
    }
}
