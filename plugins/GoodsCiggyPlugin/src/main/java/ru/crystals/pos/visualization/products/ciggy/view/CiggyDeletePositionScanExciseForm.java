package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.utils.ValidateExciseException;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonDeletePositionScanExciseForm;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;

public class CiggyDeletePositionScanExciseForm extends CommonDeletePositionScanExciseForm<ProductCiggyEntity, PositionEntity> {

    public CiggyDeletePositionScanExciseForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    protected boolean exciseValidation(String barcode) {
        PositionEntity position = controller.getModel().getPosition();
        final MarkData markData;
        try {
            markData = getController().parseAndValidateOnDelete(barcode, position);
        } catch (ValidateExciseException e) {
            excisePanel.setMessage(e.getMessage());
            controller.startBeepError("Error validate mark");
            return false;
        }
        final PositionEntity positionEntity = getController().filterPositionWithMarkInCurrentPurchase(markData.getRawMark()).orElse(null);
        if (positionEntity == null) {
            excisePanel.setMessage(ResBundleVisualization.getString("MARK_NOT_FOUND"));
            controller.startBeepError("Mark not found");
            return false;
        }
        positionEntity.fillByMarkData(markData);
        controller.getModel().setPosition(positionEntity);
        return true;
    }

    @Override
    public CiggyPluginController getController() {
        return (CiggyPluginController) super.getController();
    }
}
