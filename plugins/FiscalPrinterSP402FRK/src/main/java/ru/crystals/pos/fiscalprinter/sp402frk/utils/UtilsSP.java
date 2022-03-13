package ru.crystals.pos.fiscalprinter.sp402frk.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;

/**
 * Вспомогательные функции и константы
 *
 * @author Yanovsky
 */
public class UtilsSP {

    private static final int LONG_PRICE_PRECISION = 2;

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float roundFloat(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    /**
     * Округляение цен и количества для команд-запросов
     *
     * @param d
     * @param decimalPlace используется KKTDataType для определения количества знаков после запятой
     * @return
     */
    public static BigDecimal roundBigDecimal(BigDecimal d, String decimalPlace) {
        return d.setScale(Integer.parseInt(decimalPlace), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Переводим целочисленную цену в BigDecimal и делим на 100
     *
     * @param price
     * @return
     */
    public static BigDecimal longPriceToBigDecimal(long price) {
        BigDecimal fPrice = new BigDecimal(price);
        return fPrice.movePointLeft(LONG_PRICE_PRECISION);
    }

    public static Long floatPriceToLong(float price) {
        return (long) (price * 100);
    }

    public static long bigDecimalPriceToLong(BigDecimal price) {
        return price.movePointRight(LONG_PRICE_PRECISION).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    /**
     * Проверяем если число содержится в массиве
     *
     * @param array целочисленный массив
     * @param key   искомое число
     * @return true, если элемент содердится в массиве
     */
    public static boolean contains(final int[] array, final int key) {
        return Arrays.stream(array).anyMatch(i -> i == key);
    }

    public static String byteArray2Sring(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (byte b : bytes) {
            result.append(String.format("%02X", b & 0xFF)).append(" ");
        }
        return result.toString().trim() + "]";
    }

    /**
     * Перобразует дату в строку
     *
     * @param date {@link Date}
     * @return {@link String}
     */
    public static String date2String(Date date) {
        return SimpleDateFormat.getDateTimeInstance().format(date);
    }

    /**
     * Получить текущую даду
     *
     * @return форматированная "yyyy-MM-dd HH:mm:ss" строка с датой-временем
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(KKTDataType.SP_DATE_FORMAT);
        return dateTimeFormat.format(new Date());
    }

}
