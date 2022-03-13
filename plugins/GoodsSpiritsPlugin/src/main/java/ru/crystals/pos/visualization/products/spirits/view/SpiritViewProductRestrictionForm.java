package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.awt.BorderLayout;

public class SpiritViewProductRestrictionForm extends SpiritViewProductForm {
    protected SpiritWarnPanel warnPanel = new SpiritWarnPanel();

    public SpiritViewProductRestrictionForm(XListener outerListener) {
        super(outerListener);
        add(warnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionEntity position) {
        super.showForm(product, position);
        String restriction = ((SpiritProductModel) getModel()).getRestrictionMessage();
        warnPanel.setWarningText(restriction);
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return null;
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return null;
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return null;
    }
}