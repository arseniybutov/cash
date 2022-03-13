package ru.crystals.pos.visualization.products.piece.view;

import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonAddPositionScanExciseForm;
import ru.crystals.pos.visualization.products.piece.controller.PiecePluginController;

public class PieceAddPositionScanExciseForm extends CommonAddPositionScanExciseForm {

    public PieceAddPositionScanExciseForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    protected boolean exciseValidation(String barcode) {
        showWaitComponent();
        try {
            final MarkData markData = getController().parseAndValidateOnAdd(barcode, controller.getModel().getPosition(), getController().isRefund());
            setValidExcise(true);
            position.fillByMarkData(markData);
            getController().setExcise(markData);
            if (!controller.checkFiscalMarkValidationResults(position, true)) {
                setValidExcise(false);
                throw new Exception(ResBundleVisualization.getString("MARK_REJECTED_BY_FISCAL_VALIDATION"));
            }
        } catch (Exception ex) {
            footerPanel.setMessage(ex.getMessage());
            controller.startBeepError("Error validate mark");
        }

        hideWaitComponent();
        return isValidExcise();
    }

    @Override
    public PiecePluginController getController() {
        return (PiecePluginController) super.getController();
    }

    @Override
    public void clear() {
        setValidExcise(false);
    }

    @Override
    protected String getMessageForScanMark() {
        if (getController().getModel().isCanSkipScanMarkForm()) {
            return ResBundleVisualization.getString("SCAN_MARK_OR_CANCEL");
        }

        return ResBundleVisualization.getString("SCAN_MARK");
    }
}
