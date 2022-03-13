package ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary;

/**
 * Настройка устройства "Штрих".
 * <p/>
 * Настройка представлена в виде "координат" (таблица, строка, поле) этой настройки, ее названия, и значение (с "размером" и типом значения).
 * 
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihParameter {
    /**
     * Номер таблицы, в которой эта настройка хранится
     */
    private int tableNo;
    
    /**
     * Номер строки в этой таблице, в которой хранится настройка
     */
    private int rowNo;
    
    /**
     * Номер поля (в этой строке) в котором и хранится значение настройки
     */
    private int fieldNo;
    
    /**
     * размер поля, в байтах/символах
     */
    private int fieldWidth;
    
    /**
     * тип этого поля
     */
    private ShtrihFieldType fieldType;
    
    /**
     * Минимально допустимое значение этого поля; для {@link ShtrihFieldType#STRING строковых} типов игнорируется
     */
    private long minValue;
    
    /**
     * Максимально допустимое значение этого поля; для {@link ShtrihFieldType#STRING строковых} типов игнорируется
     */
    private long maxValue;
    
    /**
     * название этой настройки
     */
    private String name;
    
    /**
     * значение этой настройки
     */
    private String value;

    @Override
    public String toString() {
        return String.format("shtrih-parameter [table-no: %s; row-no: %s; field-no: %s; width: %s; type: %s; value: %s; name: %s]", 
            getTableNo(), getRowNo(), getFieldNo(), getFieldWidth(), getFieldType(), getValue(), getName());
    }
    
    // getters & setters
    
    public int getTableNo() {
        return tableNo;
    }

    public void setTableNo(int tableNo) {
        this.tableNo = tableNo;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public int getFieldNo() {
        return fieldNo;
    }

    public void setFieldNo(int fieldNo) {
        this.fieldNo = fieldNo;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    public ShtrihFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(ShtrihFieldType fieldType) {
        this.fieldType = fieldType;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}