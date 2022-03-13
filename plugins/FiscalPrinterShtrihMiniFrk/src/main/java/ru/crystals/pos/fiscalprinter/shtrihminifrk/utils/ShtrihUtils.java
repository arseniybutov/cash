package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Разные мелкие утильные методы при работе с ФР семейства "Штрих".
 *
 * @author aperevozchikov
 */
public abstract class ShtrihUtils {

    /**
     * Подсчитывает контрольную сумму указанной последователности байт.
     * <p/>
     * Implementation Note: данный метод следует вызывать только после того, как аргумент уже был отвалидирован: что он не пуст и имеет правильную
     * длину - иначе будут Exception'ы.
     *
     * @param data
     *            последовательность данных, контрольную сумму которого надо подсчитать
     * @return контрольную сумму
     */
    public static byte calcLrc(byte[] data) {
        // Longitude Redundancy Check: побитовый XOR всех
        byte lrc = data[0];
        for (int idx = 1; idx < data.length; idx++) {
            lrc ^= data[idx];
        }
        return lrc;
    }

    /**
     * Просто вернет указанный массив байт в обратном порядке: т.е., последний байт исходного массива будет 1м в результирующем/возвращаемом.
     *
     * @param data
     *            массив, что надо "инвертнуть"
     * @return <code>null</code>, если аргумент <code>null</code>; вернет ТОТ ЖЕ САМЫЙ МАССИВ (т.е., аргумент будет испорчен)
     */
    public static byte[] inverse(byte[] data) {
        if (data == null) {
            return null;
        }

        for (int i = 0; i < data.length / 2; i++) {
            int to = data.length - i - 1;
            byte temp = data[to];
            data[to] = data[i];
            data[i] = temp;
        }

        return data;
    }

    /**
     * вернет <code>int</code>'овое представление указанных 2х байтов.
     *
     * @param low
     *            младший байт
     * @param hi
     *            старший байт
     * @return не <code>null</code>; не отрицательное число
     */
    public static int getInt(byte low, byte hi) {
        return new BigInteger(new byte[] {0, hi, low}).intValue();
    }

    /**
     * Вернет <code>long</code>'овое представление указанной последовательности байт (от старшего к младшему).
     *
     * @param bytes
     *            последовательность байт, от старшего к младшему, чье long'овое представление надо вернуть
     * @return не-отрицательное число
     */
    public static long getLong(byte[] bytes) {
        // нужен 0 в первом байте - иначе можем получить отрицательное число
        byte[] arg = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, arg, 1, bytes.length);
        arg[0] = 0;
        return new BigInteger(arg).longValue();
    }

    /**
     * По факту операция, обратная {@link #getLong(byte[])}: вернет указанное число в виде массива байт (от старшего к младшему).
     *
     * @param data
     *            число, чье представление в виде массива байт надо вернуть
     * @return массив из 8ми байт
     */
    public static byte[] getLongAsByteArray(long data) {
        byte[] result = new byte[8];

        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (data >>> (8 * (7 - i)));
        }

        return result;
    }

    /**
     * Переводим hex данные из строкогого предстовления в массив байтов.
     * Пример: "0005" -> {0x00, 0x05}
     * @param hexStr строка с hex
     * @return byte[]
     */
    public static byte[] hexStringDataToByteArray(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * По факту изменит знак (умножит на <code>-1</code>) указанного числа и вернет результат.
     *
     * @param number
     *            число (от старшего байта к младшему), что надо умножить на <code>-1</code>
     * @return результат умножения на <code>-1</code> (от старшего байта к младшему); вернет то же количество байт, что было в аргументе
     */
    public static byte[] changeSign(byte[] number) {
        byte[] result;

        if (number == null || number.length == 0) {
            // неликвид
            return number;
        }

        // 1. надо инвертнуть все биты
        result = new byte[number.length];
        for (int i = 0; i < number.length; i++) {
            result[i] = (byte) (number[i] ^ 0xFF);
        }

        // 2. надо прибавить 1 к младшему байту и перенос пробрасывать к старшим
        int idx = result.length - 1;
        while (++result[idx] == 0 && idx > 0) {
            idx--;
        }

        return result;
    }

    /**
     * Вернет указанную дату.
     *
     * @param day
     *            день месяца: 1..31
     * @param month
     *            номер месяца: 1..12
     * @param year
     *            номер года - после 2000: 0..99
     * @param hour
     *            час в течении дня
     * @param minute
     *            минута
     * @param second
     *            секунда
     * @return не <code>null</code>
     */
    public static Date getDate(byte day, byte month, byte year, byte hour, byte minute, byte second) {
        Calendar result = Calendar.getInstance();

        result.set(Calendar.MILLISECOND, 0);

        result.set(Calendar.DAY_OF_MONTH, day);
        // месяц нумеруется с 1, а не по-нормальному с 0
        result.set(Calendar.MONTH, month - 1);
        result.set(Calendar.YEAR, 2000 + year);

        result.set(Calendar.HOUR_OF_DAY, hour);
        result.set(Calendar.MINUTE, minute);
        result.set(Calendar.SECOND, second);

        return result.getTime();
    }

    /**
     * По факту выполняет операцию, обратную {@link #getDate(byte, byte, byte, byte, byte, byte)}: вернет указанную дату в виде массива байт.
     *
     * @param dateTime дата-время, чье представление в виде массива байт надо вернуть; если <code>null<code> - получите NPE
     * @return массив из 6ти элементов:
     * <ol>
     * <li> 0й байт - день месяца: 1..31;
     * <li> 1й байт - номер месяца: 1..12;
     * <li> 2й байт - последние 2 цифры года: 00..99;
     * <li> 3й байт - час в течении дня: 00..23;
     * <li> 4й байт - минута: 00..59;
     * <li> 5й байт - секунда: 00..59.
     * </ol>
     */
    public static byte[] getDateTime(Date dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);

        byte[] result = new byte[6];
        result[0] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        result[1] = (byte) (cal.get(Calendar.MONTH) + 1); // у нас нумерация месяцев нестандартная
        result[2] = (byte) (cal.get(Calendar.YEAR) % 100);
        result[3] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        result[4] = (byte) cal.get(Calendar.MINUTE);
        result[5] = (byte) cal.get(Calendar.SECOND);

        return result;
    }

    /**
     * Вернет указанную дату.
     *
     * @param day
     *            день месяца: 1..31
     * @param month
     *            номер месяца: 1..12
     * @param year
     *            номер года - после 2000: 0..99
     * @return не <code>null</code>
     */
    public static Date getDate(byte day, byte month, byte year) {
        Calendar result = Calendar.getInstance();

        result.set(Calendar.MILLISECOND, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.HOUR_OF_DAY, 0);

        result.set(Calendar.DAY_OF_MONTH, day);
        // месяц нумеруется с 1, а не по-нормальному с 0
        result.set(Calendar.MONTH, month - 1);
        result.set(Calendar.YEAR, 2000 + year);

        return result.getTime();
    }

    /**
     * Разбирает TLV List Штриха на теги
     * @param tlvBytes байты TLV
     * @return теги и их значения
     */
    public static Map<Integer, Long> parseTlv(byte[] tlvBytes) {
        Map<Integer, Long> tags = new HashMap<>();
        int index = 0;
        while (index < tlvBytes.length) {
            // первые два байта - номер тега (формально это не совсем так, но для наших случаев подходит)
            int tag = getInt(tlvBytes[index], tlvBytes[index + 1]);
            // следующие два байта - длина значения тега
            int length = getInt(tlvBytes[index + 2], tlvBytes[index + 3]);
            byte[] valueBytes = new byte[length];
            System.arraycopy(tlvBytes, index + 4, valueBytes, 0, length);
            long value = getLong(inverse(valueBytes));
            tags.put(tag, value);
            index += 4 + length;
        }
        return tags;
    }
}
