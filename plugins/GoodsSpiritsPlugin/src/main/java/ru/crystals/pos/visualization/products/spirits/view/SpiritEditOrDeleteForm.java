package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;

/**
 * Форма "Редактирование позиции. Редактировать или Удалить?"
 */
public class SpiritEditOrDeleteForm extends CommonEditOrDeleteForm {


    public SpiritEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        this.setName(SpiritEditOrDeleteForm.class.getName());
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);
        // тут идет редактирование позиции - продукт вытягиваем из позиции
        headerPanel.setHeaderInfo(position.getProduct());
        unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        summPanel.updateSumm(BigDecimalConverter.convertMoney(position.calculateStartPositionSum()));
        footerPanel.reset();
    }

    public boolean isEdit() {
        return !footerPanel.isYes();
    }
}
