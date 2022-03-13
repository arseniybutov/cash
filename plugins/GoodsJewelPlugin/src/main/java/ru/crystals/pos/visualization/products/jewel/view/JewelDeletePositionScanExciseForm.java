package ru.crystals.pos.visualization.products.jewel.view;

import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.utils.ValidateExciseException;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonDeletePositionScanExciseForm;
import ru.crystals.pos.visualization.products.jewel.controller.JewelPluginController;

public class JewelDeletePositionScanExciseForm extends CommonDeletePositionScanExciseForm {

    public JewelDeletePositionScanExciseForm(XListener outerListener) {
        super(outerListener);
    }

    public JewelDeletePositionScanExciseForm(XListener outerListener, JewelPluginController controller) {
        this(outerListener);
        this.controller = controller;
    }

    @Override
    protected boolean exciseValidation(String barcode) {
        final MarkData markData;
        try {
            markData = getController().parseAndValidateOnDelete(barcode, position);
        } catch (ValidateExciseException e) {
            excisePanel.setMessage(e.getMessage());
            controller.startBeepError("Error validate mark");
            return false;
        }
        if (!markData.getRawMark().equals(position.getExciseToken())) {
            excisePanel.setMessage(ResBundleVisualization.getString("MARK_ANOTHER_PRODUCT"));
            controller.startBeepError("Mark belongs to another product");
            return false;
        }
        return true;
    }

    @Override
    public JewelPluginController getController() {
        return (JewelPluginController) super.getController();
    }
}
