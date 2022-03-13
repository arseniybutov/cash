package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Структура таблицы [с настройками ФР]
 * 
 * @author aperevozchikov
 *
 */
public class TableStructure {
    /**
     * название таблицы
     */
    private String tableName;
    
    /**
     * количество строк в этой таблице
     */
    private int rowsCount;
    
    /**
     * количество полей
     */
    private int fieldsCount;

    @Override
    public String toString() {
        return String.format("table-structure [name: \"%s\"; rows: %s; fields: %s]", 
            getTableName(), getRowsCount(), getFieldsCount());
    }
    
    // getters & setters
    
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public int getFieldsCount() {
        return fieldsCount;
    }

    public void setFieldsCount(int fieldsCount) {
        this.fieldsCount = fieldsCount;
    }
}