package ru.crystals.pos.visualization.products.bonus;

import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductBonusPointsEntity;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.commonplugin.model.ProductPluginModel;
import ru.crystals.pos.visualization.products.CommonProductContainer;

import javax.swing.JPanel;

/**
 * Плагин для типа товара "Зачисление бонусных баллов"
 * это пустышка, т.к. товар не может продаваться/добавляться вручную,
 * а добавляется только программно, по результатам акций или других действий
 */
@SuppressWarnings("unused")
@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_BONUS_POINTS_ENTITY, mainEntity = ProductBonusPointsEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class BonusPointsProductContainer extends CommonProductContainer {
	private ProductBonusPointsEntity product = null;

    @Override
    protected ProductPluginModel getModel() {
        return null;
    }

    @Override
    protected AbstractProductController getController() {
        return null;
    }

    @Override
    public void setProduct(ProductEntity product) {
        this.product = (ProductBonusPointsEntity) product;
    }

    @Override
    public ProductEntity getProduct() {
        return product;
    }

    @Override
    public void clean() {
        product = null;
    }

    @Override
    public boolean isProductStateAllowed(ProductState state) {
        return false;
    }

    @Override
    public void reset() {
        // не нужен, нет визуализации
    }

    @Override
    public boolean ableToAddPosition() {
        return false;
    }

    @Override
    public void doPositionAdd() {
        // не нужен, нет визуализации
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public JPanel getVisualPanel() {
        return null;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return false;
    }

    @Override
    public void enter() {
        // не нужен, нет визуализации
    }

    @Override
    public void number(Byte num) {
        // не нужен, нет визуализации
    }
}
