package ru.crystals.pos.fiscalprinter.az.airconn.model;

/**
 * Список налогов используемых плагином, в AirConn ограничения на процент налога нет.
 */
public enum Taxes {

    NDS18(0, 12.0f),
    NDS20(1, 20.0f),
    NO_NDS(2, -1.0f);

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
