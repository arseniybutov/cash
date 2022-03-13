package ru.crystals.pos.fiscalprinter.mstar.core.connect;

/**
 * Параметры таблицы настроек ККТ Mstar
 */
public enum MstarConfigParameter {

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
     * Признаки соответствия типов платежей (массив из
     * 16-ти строк для каждого
     * типа платежа из настройки
     * №32 - см. выше)
     */
    PAYMENT_TYPE(33),
    /**
     * Название ставки налога (массив из 6-х строк)
     */
    TAX_NAMES(40),
    /**
     * Процент ставки налога (массив из 6 дробных чисел)
     */
    TAX_PERCENTS(41),
    /**
     * Логический номер ККМ (число 1..9999)
     */
    CASH_NUMBER(10, 0),
    /**
     * 0	Не печатать графический логотип(*)
     * 1	Печатать графический логотип
     * (логотип должен быть предварительно загружен в ПУ)
     */
    PRINT_LOGO(1, 0, 2),
    /**
     * 0	Не сжимать шрифт(*)
     * 1	Сжать шрифт
     */
    COMPRESS_FONT(1, 0, 3),
    /**
     * 0	Включен контроль наличных в денежном ящике. (*)
     * 1	Контроль наличных в денежном ящике отключен.  При этом сумма в денежном ящике может быть отрицательной
     */
    CASH_DRAWER_MONEY_CONTROL(5, 0, 0),
    /**
     * 0	Денежный ящик открывает внешняя программа  (*)
     * 1	Денежный ящик открывает ККТ при работе с наличными
     */
    CASH_DRAWER_OPEN_BY(4, 0, 0),
    /**
     * 0	Автоматическая инкассация выключена(*)
     * 1	Автоматическая инкассация включена.
     * При этом перед печатью Z-отчета печатается чек инкассации на всю сумму наличных в денежном ящике
     */
    AUTO_WITHDRAWAL(5, 0, 2),
    /**
     * Количество символов в строке (Целое число 40.. 64)
     */
    CHAR_IN_ROW(86, 0);

    private int tableNumber;
    private int index;
    private int bitNum;

    MstarConfigParameter(int tableNumber, int index, int bitNum) {
        this.tableNumber = tableNumber;
        this.index = index;
        this.bitNum = bitNum;
    }

    MstarConfigParameter(int tableNumber, int index) {
        this.tableNumber = tableNumber;
        this.index = index;
    }

    MstarConfigParameter(int tableNumber) {
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
