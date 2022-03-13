package ru.crystals.pos.visualization.products.weight.model;

import ru.crystals.pos.visualization.commonplugin.model.AgeRestrictedProductPluginModel;

import java.math.BigDecimal;

/**
 * Модель для MVC весового товара.
 */
public class WeightProductPluginModel extends AgeRestrictedProductPluginModel {

    /**
     * Флаг подтверждения маленького веса.
     * true - кассир подтвердил добавление товара с весом меньше минимального.
     */
    private boolean lowWeightConfirmed;

    /**
     * Сохраненный вес.
     * Вес который был введен/отсканирован до вызова формы подтверждения продажи маленького веса.
     */
    private BigDecimal savedWeight;

    public boolean isLowWeightConfirmed() {
        return lowWeightConfirmed;
    }

    public void setLowWeightConfirmed(boolean lowWeightConfirmed) {
        this.lowWeightConfirmed = lowWeightConfirmed;
    }

    public BigDecimal getSavedWeight() {
        return savedWeight;
    }

    public void setSavedWeight(BigDecimal savedWeight) {
        this.savedWeight = savedWeight;
    }
}