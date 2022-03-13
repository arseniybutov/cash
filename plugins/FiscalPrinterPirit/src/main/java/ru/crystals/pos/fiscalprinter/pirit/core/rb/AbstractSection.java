package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.FiscalPrinterImpl;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;

import java.io.UnsupportedEncodingException;

public abstract class AbstractSection {
    protected static final Logger LOG = LoggerFactory.getLogger(FiscalPrinterImpl.class);
    private static final String EMPTY_STRING_CHAR = "\u0000";

    public AbstractSection(byte[] data) {

    }

    protected static int getByte(byte num) {
        if (num < 0) return (num + 256);
        return num;
    }

    protected static long getLong8Bytes(byte[] dataM, int offset) {        
        return getLong8Bytes(dataM, offset, 100L);
    }
    
    protected static long getLong8Bytes(byte[] dataM, int offset, long divider) {
        long value = 0;
        for (int i = 7; i >= 0; i--) {
            value *= 256;
            value += getByte(dataM[offset + i]);
        }
        return value / divider;
    }

    protected static String getStringBytes(byte[] dataM, int offset, int len) {
        String value = null;
        try {
            value = new String(dataM, offset, len, AbstractPirit.PIRIT_CODE_SET).replaceAll(EMPTY_STRING_CHAR, " ");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    public abstract int getSize();
}
