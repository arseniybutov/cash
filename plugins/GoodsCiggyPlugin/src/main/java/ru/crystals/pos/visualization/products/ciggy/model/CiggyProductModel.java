package ru.crystals.pos.visualization.products.ciggy.model;

import ru.crystals.pos.visualization.commonplugin.model.CommonMarkedProductPluginModel;
import ru.crystals.pos.visualization.products.ProductContainer;

public class CiggyProductModel extends CommonMarkedProductPluginModel {
    private ProductContainer.ProductState prevState;
    private String prevMessage;
    private boolean confirmedProductionDate;

    public ProductContainer.ProductState getPrevState() {
        return prevState;
    }

    public void setPrevState(ProductContainer.ProductState prevState) {
        this.prevState = prevState;
    }

    public String getPrevMessage() {
        return prevMessage;
    }

    public void setPrevMessage(String prevMessage) {
        this.prevMessage = prevMessage;
    }

    public boolean isConfirmedProductionDate() {
        return confirmedProductionDate;
    }

    public void setConfirmedProductionDate(boolean confirmedProductionDate) {
        this.confirmedProductionDate = confirmedProductionDate;
    }
}
