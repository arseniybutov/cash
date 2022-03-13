package ru.crystals.comportemulator.interfaces;

import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author dalex
 */
public class RequestPacketUtils {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RequestPacketUtils.class);

    public static final String DATE_FORMAT_PREFIX = "<|DATE:";
    public static final String NUMERICAL_FORMAT_PREFIX = "<|NUMERICAL:";
    public static final String SUFFIX = "|>";

    /**
     * Проверка соответствия текста маске
     *
     * @param thatData - либо текст, либо текст содержащий маски
     * @param thisData - либо текст, либо текст содержащий маски
     * @return результат проверки
     */
    public static boolean testEqualsDataWithTags(String thatData, String thisData) {
        String maskData = thisData;
        //определяем где текст с масками
        if (thatData.contains(DATE_FORMAT_PREFIX) || thatData.contains(thisData)) {
            maskData = thatData;
            thatData = thisData;
        }

        //проверка даты по маске (маска стандартная для Java)
        int indexDate = maskData.indexOf(DATE_FORMAT_PREFIX);
        int finishIndex;
        String dataMask = null;
        String dataString;
        while (indexDate > -1) {
            try {
                finishIndex = maskData.indexOf(SUFFIX, indexDate);
                dataMask = maskData.substring(indexDate + DATE_FORMAT_PREFIX.length(), finishIndex);
                if (thatData.length() < indexDate || thatData.length() < (indexDate + dataMask.length())) {
                    return false;
                }
                dataString = thatData.substring(indexDate, indexDate + dataMask.length());

                //выравнивание данных в строках
                maskData = maskData.replace(DATE_FORMAT_PREFIX + dataMask + SUFFIX, dataMask);
                thatData = thatData.replace(dataString, dataMask);
                //просто проверка что там есть дата нужного формата
                SimpleDateFormat sdf = new SimpleDateFormat(dataMask);
                sdf.parse(dataString);
                indexDate = maskData.indexOf(DATE_FORMAT_PREFIX, indexDate);
            } catch (ParseException ex) {
                LOG.error("Error parse date: " + dataMask, ex);
                return false;
            } catch (Exception ex) {
                LOG.error("Unknown error : ", ex);
                return false;
            }
        }

        //проверка числа по маске (маска стандартная для Java)
        indexDate = maskData.indexOf(NUMERICAL_FORMAT_PREFIX);
        while (indexDate > -1) {
            finishIndex = maskData.indexOf(SUFFIX, indexDate);
            dataMask = maskData.substring(indexDate + NUMERICAL_FORMAT_PREFIX.length(), finishIndex);
            dataString = thatData.substring(indexDate, indexDate + dataMask.length());

            //выравнивание данных в строках
            maskData = maskData.replace(NUMERICAL_FORMAT_PREFIX + dataMask + SUFFIX, dataMask);
            thatData = thatData.replace(dataString, dataMask);

            indexDate = maskData.indexOf(NUMERICAL_FORMAT_PREFIX, indexDate);
        }
        return maskData.equals(thatData);
    }
}
