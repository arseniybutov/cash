package ru.crystals.pos.visualization.products.spirits.model.ds;

import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;

/**
 * Данные полученные от сервера валидации
 * @author user
 */
public class ValidationData {

    private String alcocode;
    private String barcode;
    private Long amrc;

    public ValidationData() {
    }

    public ValidationData(String alcocode, String barcode, Long amrc) {
        this.alcocode = alcocode;
        this.barcode = barcode;
        this.amrc = amrc;
    }

    public ValidationData(ExciseValidationResult validationResult) {
        this(validationResult.getAlcocode(), validationResult.getBarcode(), validationResult.getAmrc());
    }

    public String getAlcocode() {
        return alcocode;
    }

    public void setAlcocode(String alcocode) {
        this.alcocode = alcocode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getAmrc() {
        return amrc;
    }

    public void setAmrc(Long amrc) {
        this.amrc = amrc;
    }

}
