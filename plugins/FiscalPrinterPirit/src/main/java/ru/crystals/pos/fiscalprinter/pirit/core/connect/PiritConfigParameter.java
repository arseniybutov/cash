package ru.crystals.pos.fiscalprinter.pirit.core.connect;

/**
 * Параметры таблицы настроек ККТ Пирита
 */
public enum PiritConfigParameter {

    /**
     * Реквизиты
     * 0..1 Наименование организации (массив из 2-х строк)
     * 2..3 Адрес организации (массив из 2-х строк)
     */
    REQUISITES(30),
    /**
     * Названия типов платежей (массив из 16-х строк)
     */
    PAYMENT_NAMES(32),
    /**
     * Название ставки налога (массив из 6-х строк)
     */
    TAX_NAMES(40),
    /**
     * Процент ставки налога (массив из 6 дробных чисел)
     */
    TAX_PERCENTS(41),
    /**
     * Таймаут обмена с ОИСМ (при проверке марок)
     */
    OISM_TIMEOUT(204, 0),
    /**
     * Логический номер ККТ (число 1..9999)
     */
    CASH_NUMBER(10, 0),

    /**
     * РБ: Разряд до которого выполняется округление
     */
    RB_ROUND_VALUE(7, 0),
    /**
     * 0 Не печатать графический логотип(*)
     * 1 Печатать графический логотип
     * (логотип должен быть предварительно загружен в ПУ)
     */
    PRINT_LOGO(1, 0, 2),
    /**
     * 0 Включен контроль наличных в денежном ящике. (*)
     * 1 Контроль наличных в денежном ящике отключен.  При этом сумма в денежном ящике может быть отрицательной
     */
    CASH_DRAWER_MONEY_CONTROL(5, 0, 0),
    /**
     * 0 Денежный ящик открывает внешняя программа  (*)
     * 1 Денежный ящик открывает ККТ при работе с наличными
     */
    CASH_DRAWER_OPEN_BY(4, 0, 0),
    /**
     * 0 Автоматическая инкассация выключена(*)
     * 1 Автоматическая инкассация включена.
     * При этом перед печатью Z-отчета печатается чек инкассации на всю сумму наличных в денежном ящике
     */
    AUTO_WITHDRAWAL(5, 0, 2),
    /**
     * 0 Округлять сумму налога после каждой позиции (*)
     * 1 Округлять сумму налога только после ввода всех позиций и скидок
     */
    ROUND_TAXES_AFTER_ALL_POSITIONS_AND_DISCOUNTS(6, 0, 3),
    /**
     * 0 Нумерация чеков ККТ (*)
     * 1 Нумерация чеков внешней программой
     */
    CHECK_NUMERATION_BY_EXTERNAL_APP(2, 0, 7),
    /**
     * 0..4 бит
     * Номер дизайна чека:
     * 0 - обычный, 1 - расширенный, 2 - экономный, 3 - нано, 4 - расширенный для бумаги 57мм, экономный для бумаги 57мм,
     * 16 и выше - дополнительные загружаемые дизайны
     * 6 бит 0 - печатать наличные в ДЯ на чеках внесения/инкассации, 1 - не печатать наличные в ДЯ на чеках внесения/инкассации
     * 7 бит {@link #CHECK_NUMERATION_BY_EXTERNAL_APP}
     */
    FULL_CHECK_PARAMS(2, 0),
    /**
     * 0 Нормальный режим печати
     * 1 Печать с уменьшенным межстрочным интервалом, для экономии бумаги(*)
     */
    USE_SMALLER_LINE_HEIGHT_PRINT_MODE(1, 0, 0),
    /**
     * 0 Не печатать вертикальные полосы на сервисных документах(*)
     * 1 Печатать вертикальные полосы на сервисных документах
     */
    PRINT_VERTICAL_BARS_ON_SERVICE_DOC(1, 0, 3),
    /**
     * 0 Учитывать чеки, аннулированные при включении питания(*)
     * 1 Не учитывать чеки, аннулированные при включении питания
     */
    TAKE_INTO_ACCOUNT_DOCUMENTS_CANCELED_ON_RESTART(5, 0, 1),
    /**
     * 0 Печатать документы на чековой ленте(*)
     * 1 Не печатать документы на чековой ленте
     */
    DISABLE_PRINTING(1, 0, 7);

    private int tableNumber;
    private int index;
    private int bitNum;

    PiritConfigParameter(int tableNumber, int index, int bitNum) {
        this.tableNumber = tableNumber;
        this.index = index;
        this.bitNum = bitNum;
    }

    PiritConfigParameter(int tableNumber, int index) {
        this.tableNumber = tableNumber;
        this.index = index;
    }

    PiritConfigParameter(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getIndex() {
        return index;
    }

    public int getBitNum() {
        return bitNum;
    }
}
