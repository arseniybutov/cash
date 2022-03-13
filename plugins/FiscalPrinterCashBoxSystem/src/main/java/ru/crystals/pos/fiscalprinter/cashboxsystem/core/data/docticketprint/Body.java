package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание объект тела чека. Содержит поля чека.
 */
public class Body {

    /**
     * Наименование операции
     */
    @JsonProperty("operation_name")
    private String operationName;
    /**
     * Массив позиций чека
     */
    @JsonProperty("items")
    private List<Item> items = new ArrayList<>();

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
