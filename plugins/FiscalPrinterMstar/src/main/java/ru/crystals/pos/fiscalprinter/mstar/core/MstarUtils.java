package ru.crystals.pos.fiscalprinter.mstar.core;

public class MstarUtils {

    public static String upceToUpcaBarCodeConverter(String input) {
        if (input.length() != 8)
            return "";
        input = input.substring(1, 7);
        String manufacturerCode;
        String productCode;
        char lastDigit = input.charAt(5);

        switch (lastDigit) {
            case '0':
            case '1':
            case '2':
                manufacturerCode = input.substring(0, 2) + input.substring(5, 6) + "00";
                productCode = "00" + input.substring(2, 5);
                break;
            case '3':
                manufacturerCode = input.substring(0, 3) + "00";
                productCode = "000" + input.substring(3, 5);
                break;
            case '4':
                manufacturerCode = input.substring(0, 4) + "0";
                productCode = "0000" + input.substring(4, 5);
                break;
            default:
                manufacturerCode = input.substring(0, 5);
                productCode = "0000" + input.substring(5, 6);
                break;
        }

        return "0" + manufacturerCode + productCode;
    }

    public static boolean getBit(long number, int bitNum) {
        return (number & (1 << bitNum)) > 0;
    }

    public static long setBit(long number, int bitNum, boolean bitValue) {
        if (!bitValue) {
            return number & (~(1 << bitNum));
        } else {
            return number | (1 << bitNum);
        }
    }
}
