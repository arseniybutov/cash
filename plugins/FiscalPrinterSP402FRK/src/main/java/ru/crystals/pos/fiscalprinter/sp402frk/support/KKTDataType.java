package ru.crystals.pos.fiscalprinter.sp402frk.support;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class KKTDataType {
    /**
     * Строка в кодировке UTF-8
     */
    public static final String STRING = "1";
    /**
     * Денежная сумма - целое, с фиксированной точкой. Точность – 2 знака после точки.
     */
    public static final String CURRENCY = "2";
    /**
     * Количество/вес: целое, с фиксированной точкой. Точность – 3 знака после точки.
     */
    public static final String AMOUNT = "3";
    /**
     * Беззнаковое целое: 4 байта, порядок байт Little Endian
     */
    public static final String UINT = "4";
    /**
     * Дата и время в формате: yyyy-mm-dd hh:mm:ss
     */
    public static final String SP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME = "5";
    /**
     * Массив однотипных элементов с одинаковым наименованием, которое совпадает с наименованием данного элемента.
     */
    public static final String ARRAY = "6";
    /**
     * Составной тип произвольной структуры, может включать произвольное количество элементов.
     */
    public static final String STRUCT = "7";
}
