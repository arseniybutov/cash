package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Описание секции
 */
public class Section {

    /**
     * Наименование секции
     */
    @JsonProperty("name")
    private String name;
    /**
     * Код секции, должен быть >= 0
     */
    @JsonProperty("code")
    private Long code;
    @JsonProperty("tax")
    private Tax tax;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }
}
