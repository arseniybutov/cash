package ru.crystals.pos.bank.odengiqr.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Общий класс для специфичных данных запросов
 */
public class Data {

    /**
     * Код ошибки
     */
    @JsonProperty("error")
    private String error;
    /**
     * Описание ошибки
     */
    @JsonProperty("desc")
    private String desc;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
