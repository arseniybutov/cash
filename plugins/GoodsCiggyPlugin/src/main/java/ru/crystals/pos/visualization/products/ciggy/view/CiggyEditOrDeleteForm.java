package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;

/**
 * Форма "Редактирование позиции. Редактировать или Удалить?"
 */
public class CiggyEditOrDeleteForm extends CommonEditOrDeleteForm {


    public CiggyEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        this.setName(CiggyEditOrDeleteForm.class.getName());
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);
        // тут идет редактирование позиции - продукт вытягиваем из позиции
        headerPanel.setHeaderInfo(position.getProduct());
        if(position != null){
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        }
        summPanel.updateSumm(BigDecimalConverter.convertMoney(position.calculateStartPositionSum()));
        footerPanel.reset();
    }

    public boolean isEdit() {
        return !footerPanel.isYes();
    }
}
