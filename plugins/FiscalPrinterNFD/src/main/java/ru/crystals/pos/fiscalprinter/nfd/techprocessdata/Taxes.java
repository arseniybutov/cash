package ru.crystals.pos.fiscalprinter.nfd.techprocessdata;

/**
 * Список налогов используемых плагином
 */
public enum Taxes {

    NDS12(0, 12.0f),
    NO_NDS(2, -1.0f),
    NDS8(1, 8.0f),
    NDS0(3, 0.0f);

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
