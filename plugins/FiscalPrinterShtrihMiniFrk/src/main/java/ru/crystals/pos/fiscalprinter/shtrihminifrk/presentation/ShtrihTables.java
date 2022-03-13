package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Описание таблиц настроек ФР семейства "Штрих"
 *
 * @author aperevozchikov
 */
public class ShtrihTables {

    /**
     * Номер таблицы, в которой хранятся настройки "Типа и режима кассы"
     */
    public static final byte TYPE_AND_MODE_TABLE = (byte) 1;

    /**
     * Номер единственной строки в таблице "Типы и режимы кассы"
     */
    public static final int TYPE_AND_MODE_TABLE_SOLE_ROW = 1;

    /**
     * Номер поля в единственной строке таблицы "Типы и режимы кассы", в котором хранится номер ККМ в магазине
     */
    public static final byte CASH_NUM_FIELD_NO = (byte) 1;

    /**
     * Номер поля в единственной строке таблицы "Типы и режимы кассы", в котором хранится размер межстрочного интервала (при печати на чековой ленте)
     */
    public static final byte LINE_SPACING_FIELD_NO = (byte) 29;

    /**
     * Номер таблицы, в которой хранятся "настройки шрифтов"
     */
    public static final byte FONTS_TABLE = (byte) 8;

    /**
     * Номер единственной строки в таблице "Настройки шрифтов"
     */
    public static final int FONTS_TABLE_SOLE_ROW = 1;

    /**
     * Номер поля в единственной строке таблицы "Настройки шрифтов", в котором хранится значение шрифта для "Теста в чеке"
     */
    public static final byte GENERAL_TEXT_FONT_FIELD_NO = (byte) 1;

    /**
     * Номер таблицы "Региональные настройки"
     */
    public static final byte REGIONAL_SETTINGS_TABLE = (byte) 17;

    /**
     * Номер строки "Формат ФД" в таблице "Региональные настройки"
     */
    public static final byte REGIONAL_SETTINGS_TABLE_FORMAT_FD_ROW = (byte) 17;

    /**
     * Номер поля "Не печатать документ (RUS)" в таблице "Региональные настройки"
     *
     * Возможные значения: 0 - печатать, 1 - не печатать
     */
    public static final byte REGIONAL_SETTINGS_TABLE_RUS_DO_NOT_PRINT_DOC = (byte) 7;

    /**
     * Номер таблицы "Пароли кассиров и администраторов"
     */
    public static final byte CASHIER_AND_ADMINS_PASSWORDS = (byte) 2;
}