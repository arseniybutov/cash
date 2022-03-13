package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

import java.nio.charset.Charset;
import java.util.Arrays;

public class AsciiStringDecoder implements ValueDecoder<String> {
    private static final Charset CHARSET_ASCII = Charset.forName("US-ASCII");

    @Override
    public String decode(byte[] data, int from, int length) {
        return new String(Arrays.copyOfRange(data, from, from + length), CHARSET_ASCII);
    }
}
