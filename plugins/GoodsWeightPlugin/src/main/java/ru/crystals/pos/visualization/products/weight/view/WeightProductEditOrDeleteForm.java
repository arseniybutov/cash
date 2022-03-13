package ru.crystals.pos.visualization.products.weight.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;

/**
 * Стандартная форма выбора "Удалить" - "Редактировать"
 */
public class WeightProductEditOrDeleteForm extends CommonEditOrDeleteForm {


    public WeightProductEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        WeightProductEditOrDeleteForm.this.setName("ru.crystals.pos.visualization.products.weight.WeightProductEditOrDeleteForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);

        footerPanel.reset();
        footerPanel.setNoEnabled(false);
    }
}
