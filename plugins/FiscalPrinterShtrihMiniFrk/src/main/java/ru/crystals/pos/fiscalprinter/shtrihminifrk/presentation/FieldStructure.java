package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihFieldType;

/**
 * Описание структуры поля [в таблице настроек ФР].
 * 
 * @author aperevozchikov
 */
public class FieldStructure {

    /**
     * название поля
     */
    private String fieldName;

    /**
     * Тип поля
     */
    private ShtrihFieldType fieldType;

    /**
     * размер поля, в байтах/символах
     */
    private int fieldWidth;

    /**
     * Минимально допустимое значение этого поля; для {@link ShtrihFieldType#STRING строковых} типов игнорируется
     */
    private long minValue;

    /**
     * Максимально допустимое значение этого поля; для {@link ShtrihFieldType#STRING строковых} типов игнорируется
     */
    private long maxValue;

    @Override
    public String toString() {
        return String.format("field-struct [name: \"%s\"; type: %s; width: %s; min: %s; max: %s]", 
            getFieldName(), getFieldType(), getFieldWidth(), getMinValue(), getMaxValue());
    }
    
    // getters & setters
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public ShtrihFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(ShtrihFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
}