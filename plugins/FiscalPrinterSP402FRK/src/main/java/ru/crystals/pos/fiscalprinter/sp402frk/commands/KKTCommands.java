package ru.crystals.pos.fiscalprinter.sp402frk.commands;

/**
 * Содержит номера команд ФР, данные с параметрами этих команд сириализируются из классов
 * находящихся в данном package
 */
public class KKTCommands {
    public static final int GET_STATUS = 2;
    public static final int SHIFT_OPEN = 4;
    public static final int PRINT_Z_REPORT = 6;
    public static final int PRINT_RECEIPT = 8;
    public static final int CORRECTION = 10;
    public static final int PRINT_FN_REPORT = 12;
    public static final int GET_DATE = 22;
    public static final int SET_DATE = 24;
    public static final int PRINT_NONFISCAL = 30;
    public static final int GET_REGISTRATION_PARAMS = 50;
    public static final int GET_SETTINGS = 54;
    public static final int SET_SETTINGS = 56;
    public static final int GET_COUNTERS = 40;
    public static final int OPEN_CASH_DRAWER = 36;
    public static final int GET_DRAWER_STATUS = 52;
    public static final int CASH_IN_OUT = 38;
    public static final int PRINT_X_REPORT = 46;
    public static final int GET_FN_SHORT_STATUS = 44;

    public static final int GET_KKT_INFO = 58;

    /**
     * Команды, не меняющие состояние ККТ и запрашиваемые параметры которых не меняются с течением времени.
     * Используется для разрешения частых перезапросов счетчиков на стороне плагина.
     * ALERT: При использовании команды для запроса состояния обмена с ОФД, смены > 24ч, и т.д. необходимо убрать ее
     * из этого списка.
     */
    public static final int[] NO_REPEAT_COMMANDS = {GET_STATUS, GET_COUNTERS, GET_FN_SHORT_STATUS};

    /**
     * Команды, ответные параметры которых не используются или будут перезапрошены отдельной командой в случае отсутсвия.
     * Используестя для условия  - можно ли обойтись ручной проверкой кодов ошибок в случае ошибок десериализации.
     */
    public static final int[] NO_RESPONSE_COMMANDS = {SHIFT_OPEN, PRINT_Z_REPORT, PRINT_X_REPORT, PRINT_RECEIPT,
            CORRECTION, PRINT_FN_REPORT, SET_DATE, PRINT_NONFISCAL, SET_SETTINGS, OPEN_CASH_DRAWER, CASH_IN_OUT};
}
