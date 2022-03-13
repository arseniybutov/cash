package ru.crystals.pos.visualization.products.setapi.goods;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import java.math.BigDecimal;

/**
 * Форма удаления плагинного товара из чека.
 *
 * @author aperevozchikov
 */
public class SetApiGoodsEditOrDeleteForm extends CommonEditOrDeleteForm {
    private static final long serialVersionUID = 1L;

    public SetApiGoodsEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        SetApiGoodsEditOrDeleteForm.this.setName(SetApiGoodsEditOrDeleteForm.class.getName());
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);
        footerPanel.reset();
        // Товары плагинов Set API - особый зверь, у которого может не быть справочника цен.
        // А если цена есть, она может быть некорректной в описывающей товар структуре, поскольку
        // может быть неизвестна до создания позиции.
        // От безысходности, прибегнем к магии здесь и вручную проставим первую цену как начальную стоимость единицы товара в позиции,
        // благо сюда мы можем попасть только до расчета скидок.
        headerPanel.setFirstPrice(position.getPriceStart() == null ? 0 : position.getPriceStart());
        footerPanel.setNoEnabled(false); // Редактирование позиций товаров плагинов Set API не поддерживается.
    }

    @Override
    public CommonYesNoPanel createQuantityPanel() {
        return new CommonYesNoPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public AbstractProductUnitPriceComponent createUnitPanel() {
        return new CommonProductUnitPricePanel(false);
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return null;
    }

    @Override
    public BigDecimal getPrice() {
        return null;
    }

    @Override
    public void clear() {
    }
}