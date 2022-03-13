package ru.crystals.pos.fiscalprinter.atol.universal.connector;

/**
 * Настройки ККТ, которые можно записать по номеру ячейки.
 * Номера ячеек могут отличаться в зависимости от модели, либо отсутствовать в некоторых моделях.
 * См. <a href="http://integration.atol.ru/api/?java#kkt_params_list">в документации</a>
 */
enum AtolSetting {

    /**
     * Открывать ДЯ при закрытии чека
     * 0 - Нет
     * 1 - Да
     */
    OPEN_DRAWER_ON_CHECK_CLOSE(9),
    /**
     * Отрезать чек после завершения документа
     * 0 - Не отрезать
     * 1 - Не полностью
     * 2 - Полностью
     */
    CUT_AFTER_DOCUMENT(66),
    /**
     * Запретить отрезку чеков
     * 0 - Нет
     * 1 - Да
     */
    FORBID_CUT_AFTER_CHECK(67),
    /**
     * Запретить отрезку отчетов
     * 0 - Нет
     * 1 - Да
     */
    FORBID_CUT_AFTER_REPORT(68),
    /**
     * Отрезать ЧЛ после печати клише командой «Печать клише»
     * 0 - Нет
     * 1 - Да
     */
    CUT_AFTER_CLICHE(69);

    /**
     * Номер ячейки для настройки в ФР
     */
    private final int num;

    AtolSetting(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
