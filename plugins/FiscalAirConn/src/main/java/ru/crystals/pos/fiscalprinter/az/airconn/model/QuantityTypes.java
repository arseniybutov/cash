package ru.crystals.pos.fiscalprinter.az.airconn.model;

/**
 * Единицы измерения количества товара
 */
public enum QuantityTypes {

    PIECE(0),
    WEIGHT(1),
    LITER(2),
    METER(3),
    METER_SQUARE(4),
    METER_CUBIC(5);

    private int typeId;

    QuantityTypes(int id) {
        typeId = id;
    }

    public int getId() {
        return typeId;
    }
}
