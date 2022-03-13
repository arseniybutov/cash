package ru.crystals.pos.bank.ucs;

import org.apache.commons.lang.StringUtils;

public class TestUtils {

    public static String convertIntToHexString(int i) {
        return StringUtils.leftPad(Integer.toHexString(i), 2, '0');
    }

    public static String prepareMessage(String commandAndTerminal) {
        return prepareMessage(commandAndTerminal, null);
    }

    public static String prepareMessage(String commandAndTerminal, String data) {
        String resultData = StringUtils.defaultString(data, "");
        return commandAndTerminal + convertIntToHexString(resultData.length()) + data;
    }

}
