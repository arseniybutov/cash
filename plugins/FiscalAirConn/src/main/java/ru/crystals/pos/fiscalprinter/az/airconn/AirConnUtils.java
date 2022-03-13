package ru.crystals.pos.fiscalprinter.az.airconn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AirConnUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AirConnUtils.class);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Возращает из строки формата "2000-01-01T20:15:00Z" UTC +0 строку с датой и временем в текущем часовом поясе
     *
     * @param isoTime строка в формате "2000-01-01T20:15:00Z" UTC +0
     * @return строка в формате "yyyy-MM-dd HH:mm:ss" или исходная строка при ошибке
     */
    public static String getDateTimeString(String isoTime) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoTime);
            zdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
            return TIME_FORMATTER.format(zdt);
        } catch (Exception e) {
            LOG.info("Time format canceled: {}", e.getMessage());
            return isoTime;
        }
    }

    /**
     * Метод для форматирования текста по краям строки
     *
     * @param testLeft  текст для левого края
     * @param textRight текст для правого края
     * @param len       длина итоговой строки
     * @return форматированная строка
     */
    public static String textSidesAlign(String testLeft, String textRight, int len) {
        int spaceSize = len - testLeft.length() - textRight.length();
        return spaceSize > 0 ? String.format("%s%" + spaceSize + "s%s", testLeft, "", textRight) : testLeft + textRight;
    }
}
