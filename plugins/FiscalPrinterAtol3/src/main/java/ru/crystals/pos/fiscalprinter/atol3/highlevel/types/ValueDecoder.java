package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

public interface ValueDecoder<T> {
    LongDecoder LONG = new LongDecoder();
    StringDecoder STRING = new StringDecoder();
    AtolStringDecoder ATOL_STRING = new AtolStringDecoder();
    AsciiStringDecoder ASCII_STRING = new AsciiStringDecoder();
    BinDecoder BIN = new BinDecoder();

    T decode(byte[] data, int from, int length);
}
