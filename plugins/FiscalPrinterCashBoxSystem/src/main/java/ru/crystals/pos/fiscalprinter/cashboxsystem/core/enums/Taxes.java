package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список налогов используемых плагином, в CBS ограничения на процент налога нет. Использумый в Казахстане налог - 12%
 */
public enum Taxes {

    NDS12(0, 12.0f),
    NO_NDS(1, -1.0f);

    private int ndsId;
    private float ndsValue;

    Taxes(int id, float value) {
        ndsId = id;
        ndsValue = value;
    }

    public int getId() {
        return ndsId;
    }

    public float getValue() {
        return ndsValue;
    }
}
