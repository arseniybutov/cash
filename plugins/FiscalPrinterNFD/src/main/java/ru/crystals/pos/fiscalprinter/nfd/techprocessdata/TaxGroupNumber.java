package ru.crystals.pos.fiscalprinter.nfd.techprocessdata;

public enum TaxGroupNumber {

    NDS_12_GROUP(1, "12"),
    NDS_8_GROUP(2, "8"),
    NDS_0_GROUP(3, "0"),
    ;

    final int value;
    private final String percent;

    TaxGroupNumber(int value, String percent) {
        this.value = value;
        this.percent = percent;
    }

    public int getValue() {
        return value;
    }

    public String getPercent() {
        return percent;
    }
}
