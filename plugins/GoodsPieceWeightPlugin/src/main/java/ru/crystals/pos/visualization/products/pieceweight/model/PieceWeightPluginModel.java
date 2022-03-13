package ru.crystals.pos.visualization.products.pieceweight.model;

import ru.crystals.pos.visualization.commonplugin.model.AgeRestrictedProductPluginModel;

/**
 * Created by alexey on 17.07.15.
 */
public class PieceWeightPluginModel extends AgeRestrictedProductPluginModel {
    //Минимальное количество, разрешенное к продаже в штуках
    private Long minQuantity;

    public PieceWeightPluginModel(Long minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Long getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Long minQuantity) {
        this.minQuantity = minQuantity;
    }

}
