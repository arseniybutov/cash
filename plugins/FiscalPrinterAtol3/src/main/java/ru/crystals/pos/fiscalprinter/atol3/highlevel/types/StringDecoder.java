package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

import java.util.Arrays;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.commands.Command;

public class StringDecoder implements ValueDecoder<String> {
    @Override
    public String decode(byte[] data, int from, int length) {
        return new String(Arrays.copyOfRange(data, from, from + length), Command.CHARSET_CP866);
    }
}
