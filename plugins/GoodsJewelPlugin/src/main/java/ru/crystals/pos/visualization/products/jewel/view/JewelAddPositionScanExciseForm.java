package ru.crystals.pos.visualization.products.jewel.view;

import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonAddPositionScanExciseForm;
import ru.crystals.pos.visualization.products.jewel.controller.JewelPluginController;

public class JewelAddPositionScanExciseForm extends CommonAddPositionScanExciseForm {

    public JewelAddPositionScanExciseForm(XListener outerListener) {
        super(outerListener);
    }

    public JewelAddPositionScanExciseForm(XListener outerListener, JewelPluginController controller) {
        this(outerListener);
        this.controller = controller;
    }

    @Override
    protected String getMessageForScanMark() {
        if (getController().canSkipScanMarkForCurrentPosition()) {
            return ResBundleVisualization.getString("SCAN_MARK_OR_CANCEL");
        }

        return ResBundleVisualization.getString("SCAN_MARK");
    }

    @Override
    protected boolean exciseValidation(String barcode) {
        showWaitComponent();
        try {
            final MarkData markData = getController().parseAndValidateOnAdd(barcode, position, getController().isRefund());
            setValidExcise(true);
            position.fillByMarkData(markData);
            getController().setExcise(markData);
        } catch (Exception ex) {
            footerPanel.setMessage(ex.getMessage());
            controller.startBeepError("Error validate mark");
        }

        hideWaitComponent();
        return isValidExcise();
    }

    @Override
    public JewelPluginController getController() {
        return (JewelPluginController) super.getController();
    }

    @Override
    public void clear() {
        setValidExcise(false);
    }
}
