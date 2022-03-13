package ru.crystals.pos.barcodeprocessing.markdown;

import ru.crystals.pos.barcodeprocessing.processors.config.InputRestrictions;

public class MarkdownBarcode {

    private final String rawBarcode;
    private final Integer reasonCode;
    private final String ean13Barcode;
    private final String productItem;
    private final long price;
    private final InputRestrictions inputRestrictions;

    public MarkdownBarcode(String rawBarcode, Integer reasonCode, String ean13Barcode, String productItem, long price, InputRestrictions inputRestrictions) {
        this.rawBarcode = rawBarcode;
        this.reasonCode = reasonCode;
        this.ean13Barcode = ean13Barcode;
        this.productItem = productItem;
        this.price = price;
        this.inputRestrictions = inputRestrictions;
    }

    public Integer getReasonCode() {
        return reasonCode;
    }

    public String getEan13Barcode() {
        return ean13Barcode;
    }

    public String getProductItem() {
        return productItem;
    }

    public String getRawBarcode() {
        return rawBarcode;
    }

    public long getPrice() {
        return price;
    }

    public InputRestrictions getInputRestrictions() {
        return inputRestrictions;
    }

    @Override
    public String toString() {
        return "MarkDownBarcode{" +
                "rawBarcode='" + rawBarcode + '\'' +
                ", reasonCode=" + reasonCode +
                ", ean13Barcode='" + ean13Barcode + '\'' +
                ", productItem='" + productItem + '\'' +
                ", price=" + price +
                ", inputRestrictions=" + inputRestrictions +
                '}';
    }
}
