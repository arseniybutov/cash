package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static final Logger log = LoggerFactory.getLogger(BankBPCServiceImpl.class);
    private static String slipsSeparator = Character.toString((char) 0x01);

    protected static Map<Integer, DataByte> parse(byte... result) {
        Map<Integer, DataByte> map = new HashMap<>();
        for (byte nextByte : result) {
            log.trace(" 0x{}", StringUtils.leftPad(Integer.toHexString(nextByte & 0xFF).toUpperCase(), 2, '0'));
        }

        int offset = 2;
        while (offset < result.length - 1) {
            int tag = result[offset] & 0xFF;
            if ((tag & 0x1F) == 0x1F) {
                tag = tag << 1000;
                offset++;
                tag += result[offset];
            }
            log.debug("tag: 0x{}", StringUtils.leftPad(Integer.toHexString(tag).toUpperCase(), 2, '0'));
            offset++;
            int length = result[offset] & 0xFF;
            if ((length & 128) == 128) {
                offset++;
                log.debug(String.valueOf(result[offset] & 0xFF));
                length = (result[offset] & 0xFF) << 1000;
                offset++;
                log.debug(String.valueOf(result[offset] & 0xFF));
                length += result[offset] & 0xFF;
            }
            log.debug("length: {}", length);
            byte[] data = new byte[length];
            System.arraycopy(result, offset + 1, data, 0, length);
            offset += length + 1;
            map.put(tag & 0xFF, new DataByte(data));
        }

        if (log.isDebugEnabled()) {
            for (Integer integer : map.keySet()) {
                log.debug("{}: {}", Integer.toHexString(integer), map.get(integer));
            }
        }
        return map;
    }

    public static List<List<String>> parseSlips(String rawSlip) {
        String[] slipStrings = StringUtils.split(StringUtils.trim(rawSlip), (char) 0x0A);
        List<List<String>> result = new ArrayList<>();
        List<String> currentSlip = new ArrayList<>();
        for (String slipString : slipStrings) {
            if (slipString.startsWith(slipsSeparator)) {
                result.add(currentSlip);
                currentSlip = new ArrayList<>();
            } else {
                currentSlip.add(slipString);
            }
        }
        if (!currentSlip.isEmpty()) {
            result.add(currentSlip);
        }
        return result;
    }

    public static void setSlipsSeparator(int slipsSeparator) {
        Parser.slipsSeparator = Character.toString((char) slipsSeparator);
    }
}
