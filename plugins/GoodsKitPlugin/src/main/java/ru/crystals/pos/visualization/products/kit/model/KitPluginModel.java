package ru.crystals.pos.visualization.products.kit.model;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

import java.math.BigDecimal;
import java.util.List;

public class KitPluginModel extends CommonProductPluginModel {
    /**
     * Компоненты комплекта
     */
    private List<ProductEntity> components;

    /**
     * цена за один комплект
     */
    private BigDecimal price;

    public List<ProductEntity> getComponents() {
        return components;
    }

    public void setComponents(List<ProductEntity> components) {
        this.components = components;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
