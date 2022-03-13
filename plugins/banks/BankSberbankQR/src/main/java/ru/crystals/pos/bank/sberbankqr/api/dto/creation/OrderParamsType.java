package ru.crystals.pos.bank.sberbankqr.api.dto.creation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderParamsType {

    /**
     * Наименование товарной позиции
     */
    @JsonProperty("position_name")
    private String positionName;

    /**
     * Кол-во шт.товарной позиции
     */
    @JsonProperty("position_count")
    private int positionCount;

    /**
     * Сумма товарной позиции в минимильных единицах Валюты (копейках)
     */
    @JsonProperty("position_sum")
    private int positionSum;

    /**
     * Описание товарной позиции
     */
    @JsonProperty("position_description")
    private String positionDescription;

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }

    public int getPositionSum() {
        return positionSum;
    }

    public void setPositionSum(int positionSum) {
        this.positionSum = positionSum;
    }

    public String getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }
}
