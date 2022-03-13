package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Последовательность байт команды или результата выполнения команды
 */
public class ByteSequence {

    private final byte[] command;

    public ByteSequence(byte[] command) {
        this.command = command;
    }

    public static ByteSequence of(int... bytes) {
        final byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) bytes[i];
        }
        return new ByteSequence(result);
    }

    public byte[] getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByteSequence that = (ByteSequence) o;
        return Arrays.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(command);
    }

    public static boolean bitAnd(byte[] result, byte[] expected) {
        if (result.length == 1) {
            return (result[0] & expected[0]) == expected[0];
        }
        final BitSet bitSet = BitSet.valueOf(result);
        final BitSet set = BitSet.valueOf(expected);
        bitSet.and(set);
        return bitSet.equals(set);
    }

    public boolean matchesToResult(byte[] result) {
        return bitAnd(result, this.command);
    }
}
