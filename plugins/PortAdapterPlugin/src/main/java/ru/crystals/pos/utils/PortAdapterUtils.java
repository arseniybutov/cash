package ru.crystals.pos.utils;

public abstract class PortAdapterUtils {

    public static String arrayToString(byte[] b) {
        if (b == null) {
            return "(NULL)";
        }
        StringBuilder result = new StringBuilder();
        result.append("[");
        if (b != null && b.length > 0) {
            for (int i = 0; i < b.length - 1; i++) {
                result.append(toUnsignedByte(b[i])).append(", ");
            }
            result.append(toUnsignedByte(b[b.length - 1]));
        } else {
            result.append("null");
        }
        result.append("]");
        return result.toString();
    }

    /**
     * Возвращает строку, в которой перечислен указанный диапазон элементов массива в шестнадцатеричном формате.
     * @param arr массив, строковое представление элементов которого в шестнадцатеричном формате требуется получить.
     * @param start начальный индекс, с которого начинается вывод элементов.
     * @param len число выводимых элементов. Не должно превышать размер массива, быть меньше start.
     * @return строка с разделёнными пробелом элементами массива в шестнадцатеричном формате.
     */
    public static String arrayToHexString(byte[] arr, int start, int len) {
        StringBuilder sb = new StringBuilder();
        for(int i = start; i < len; ++i) {
            sb.append(String.format("%02X ", arr[i]));
        }
        return sb.toString();
    }

    public static String arrayToString(int[] b) {
        if (b == null) {
            return "(NULL)";
        }
        StringBuilder result = new StringBuilder();
        result.append("[");
        if (b != null && b.length > 0) {
            for (int i = 0; i < b.length - 1; i++) {
                result.append(toUnsignedByte(b[i])).append(", ");
            }
            result.append(toUnsignedByte(b[b.length - 1]));
        } else {
            result.append("null");
        }
        result.append("]");
        return result.toString();
    }

    public static String toUnsignedByte(byte b) {
        return String.format("0x%02X", b & 0xFF);
    }

    public static String toUnsignedByte(int b) {
        return String.format("0x%02X", b & 0xFF);
    }
}
