package ru.crystals.pos.fiscalprinter.cashboxsystem.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UtilsCBS {

    private static final Logger LOG = LoggerFactory.getLogger(UtilsCBS.class);

    private static final String CBS_TIME_DELIMITER = "T";
    private static final String CBS_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Возращает из строки формата "2000-01-01T20:15:00" подстроку с временем
     * @param isoTime строка в формате "2000-01-01T20:15:00"
     * @return строка с временем или исходная строка если не найден разделитель
     */
    public static String getTimeString(String isoTime) {
        int timePos = isoTime.indexOf(CBS_TIME_DELIMITER);
        return timePos > 0 ? isoTime.substring(timePos + 1) : isoTime;
    }

    /**
     * Возращает из строки формата "2000-01-01T20:15:00Z" UTC +0 строку с датой и временем в текущем часовом поясе
     * @param isoTime строка в формате "2000-01-01T20:15:00Z" UTC +0
     * @return строка в формате "yyyy-MM-dd HH:mm:ss" или исходная строка при ошибке
     */
    public static String getDateTimeString(String isoTime) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoTime);
            zdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern(CBS_TIME_FORMAT).format(zdt);
        } catch (Exception e) {
            LOG.info("CBS time format canceled: {}", e.getMessage());
            return isoTime;
        }
    }

    /**
     * Метод для форматирования текста по краям строки
     * @param testLeft текст для левого края
     * @param textRight текст для правого края
     * @param len длина итоговой строки
     * @return форматированная строка
     */
    public static String textSidesAlign(String testLeft, String textRight, int len) {
        int spaceSize = len - testLeft.length() - textRight.length();
        return spaceSize > 0 ? String.format("%s%" + spaceSize + "s%s", testLeft, "", textRight) : testLeft + textRight;
    }
}
