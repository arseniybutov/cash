package ru.crystals.pos.fiscalprinter.az.airconn.model;

import ru.crystals.pos.barcodeprocessing.processors.ProductProcessor;

/**
 * тип кода товара, влияет на трактование значения в поле “itemCode”
 */
public enum ItemCodeType {
    /**
     * 0 - plain text - произвольное значение
     */
    PLAIN_TEXT(0),
    /**
     * 1 - EAN8 - штрихкод EAN8
     */
    EAN8(1),
    /**
     * 2 - EAN13 - штрихкод EAN13
     */
    EAN13(2),
    /**
     * 3 - service - код услуги
     */
    SERVICE(3),
    /**
     * 4 - prepayment - покупка подарочного сертификата, бонусной карты и т.д.
     */
    PREPAYMENT(4),
    /**
     * 5 - credit - погашение кредита
     */
    CREDIT(5);

    private final int typeId;

    ItemCodeType(int id) {
        this.typeId = id;
    }

    public int getId() {
        return typeId;
    }

    public static ItemCodeType getItemCodeTypeByBarcode(String barcode) {
        if (ProductProcessor.isEAN13correct(barcode)) {
            return EAN13;
        }
        if (ProductProcessor.isEAN8correct(barcode)) {
            return EAN8;
        }
        return PLAIN_TEXT;
    }
}
