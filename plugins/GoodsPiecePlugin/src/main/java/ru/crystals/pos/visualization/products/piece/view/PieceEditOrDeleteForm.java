package ru.crystals.pos.visualization.products.piece.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;

public class PieceEditOrDeleteForm extends CommonEditOrDeleteForm {


    public PieceEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        PieceEditOrDeleteForm.this.setName("ru.crystals.pos.visualization.products.piece.PieceEditOrDeleteForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);

        footerPanel.reset();
    }

    public boolean isEdit() {
        return !footerPanel.isYes();
    }
}
