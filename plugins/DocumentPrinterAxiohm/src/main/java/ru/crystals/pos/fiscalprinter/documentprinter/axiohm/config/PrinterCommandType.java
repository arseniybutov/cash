package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Команды принтеров Esc/P, которые могут переопределяться в разных реализациях (в том числе через конф. файл)
 */
public enum PrinterCommandType {

    /**
     * Установка таблицы символов, с которой будет работать принтер
     */
    @JsonProperty("INIT_CHARSET")
    INIT_CHARSET,

    /**
     * Частичная отрезка
     */
    @JsonProperty("CUT")
    CUT,

    /**
     * Полная отрезка (если не определена для принтера, то вместо полной будет выполняться частичная)
     */
    @JsonProperty("FULL_CUT")
    FULL_CUT,

    /**
     * Запрос состоянии принтера
     */
    @JsonProperty("STATUS")
    STATUS,

    /**
     * Запрос состояния денежного ящика
     */
    @JsonProperty("DRAWER_STATUS")
    DRAWER_STATUS,

    /**
     * Промотка бумаги на произвольное количество строк
     */
    @JsonProperty("FEED")
    FEED,

    @JsonEnumDefaultValue
    UNKNOWN
}
