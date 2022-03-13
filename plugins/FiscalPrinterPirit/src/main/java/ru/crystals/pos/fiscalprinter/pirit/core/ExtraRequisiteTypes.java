package ru.crystals.pos.fiscalprinter.pirit.core;

/**
 * Список возможных данных для передачи в тег ФФД 1084 "дополнительный реквизит пользователя"
 */
public enum ExtraRequisiteTypes {
    CASH_NUMBER("CASH_NUMBER"),
    EMPTY("");

    /**
     * Ключ для строки из ресурсов с наименованием дополнительного реквизита
     */
    private String nameKey;

    ExtraRequisiteTypes(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }
}
