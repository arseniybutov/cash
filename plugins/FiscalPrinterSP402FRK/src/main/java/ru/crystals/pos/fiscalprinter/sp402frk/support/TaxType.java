package ru.crystals.pos.fiscalprinter.sp402frk.support;


/**
 * Id налогов в ККТ id аналогичны но больше на 1
 */
public enum TaxType {

    NDS20(0, 20.0f),
    NDS10(1, 10.0f),
    NDS_20_120(2, -20.0f),
    NDS_10_110(3, -10.0f),
    NDS0(4, 0.0f),
    NO_NDS(5, -1.0f);

    private int ndsId;
    private float ndsValue;

    TaxType(int id, float value) {
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
