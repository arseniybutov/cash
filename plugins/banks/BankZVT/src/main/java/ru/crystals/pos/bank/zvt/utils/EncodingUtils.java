package ru.crystals.pos.bank.zvt.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

public class EncodingUtils {

    public static String encodeToBCD(long value, int length) {
        return encodeToBCD(Long.toString(value), length, '0');
    }

    public static String encodeToBCD(String value, int length) {
        return encodeToBCD(value, length, '0');
    }

    public static String encodeToBCD(String value, int length, char c) {
        return StringUtils.leftPad(value, length, c);
    }

    public static Long decodeBCDToLong(String value) {
        return Long.parseLong(value);
    }

    public static String fromByte(byte b) {
        return fromBytes(new byte[]{b});
    }

    public static String fromBytes(byte[] bb) {
        return new String(Hex.encodeHex(bb, false));
    }

    public static String decodeHexAscii(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new String(Hex.decodeHex(value.toCharArray()), StandardCharsets.ISO_8859_1);
        } catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static byte[] hexToBytes(String plainRequest) {
        try {
            return Hex.decodeHex(plainRequest.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String extractMessage(String additionalText) {
        return StringUtils.trimToNull(StringUtils.substringBefore(additionalText, "/"));
    }
}
