package ru.crystals.pos.visualization.products.ciggy.view;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.catalog.exception.MinPriceException;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.utils.ValidateExciseException;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.check.QuestionForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonAddPositionScanExciseForm;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

public class CiggyAddPositionScanExciseForm extends CommonAddPositionScanExciseForm {

    /**
     * Цена позиции, которая будет получена из АМ, из блока МРЦ. Блок МРЦ может отсутствовать в АМ.
     */
    private BigDecimal defaultPrice = null;

    private QuestionForm questionForm;
    private AtomicBoolean questionLock = new AtomicBoolean();

    public CiggyAddPositionScanExciseForm(XListener outerListener) {
        super(outerListener);
        this.questionForm = new QuestionForm(
                new XListener() {
                    @Override
                    public boolean barcodeScanned(String barcode) {
                        return true;
                    }

                    @Override
                    public boolean keyPressedNew(XKeyEvent e) {
                        if (questionForm.isCommit()) {
                            getController().getModel().setConfirmedProductionDate(true);
                            setValidExcise(true);
                            footerPanel.setMessage(StringUtils.EMPTY);
                        } else {
                            getController().getModel().setConfirmedProductionDate(false);
                        }
                        CheckContainer.unlockComponent(questionLock);
                        return true;
                    }

                    @Override
                    public boolean eventMSR(String track1, String track2, String track3, String track4) {
                        return true;
                    }
                },
                StringUtils.EMPTY,
                ResBundleVisualization.getString("ACCEPT"),
                ResBundleVisualization.getString("CANCEL"));
    }

    @Override
    protected boolean exciseValidation(String barcode) {
        showWaitComponent();
        PositionEntity position = controller.getModel().getPosition();
        try {
            final MarkData markData = getController().parseAndValidateOnAdd(barcode, position, getController().isRefund());
            setValidExcise(true);
            position.fillByMarkData(markData);
            getController().setExcise(markData);
            if (getController().tryToAddMrpFromMark(getController().getProduct())) {
                getController().checkProductMinPriceRestrictions();
            }
            if (!controller.checkFiscalMarkValidationResults(position, true)) {
                throw new ValidateExciseException(ResBundleVisualization.getString("MARK_REJECTED_BY_FISCAL_VALIDATION"));
            }
        } catch (MinPriceException e) {
            if (!getController().getModel().isConfirmedProductionDate()) {
                setValidExcise(false);
                footerPanel.setMessage(e.getMessage());
                getController().getProduct().getProductConfig().processProductionDate(this::showQuestion);
                if (!isValidExcise()) {
                    controller.startBeepError("Error validate mark, price below SMP");
                }
            }
        } catch (ValidateExciseException e) {
            setValidExcise(false);
            footerPanel.setMessage(e.getMessage());
            controller.startBeepError("Error validate mark");
        }
        hideWaitComponent();
        return isValidExcise();
    }

    private void showQuestion(String message) {
        questionForm.setText(message);
        questionForm.selectNoButton();
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(questionForm, questionLock);
    }

    @Override
    public BigDecimal getPrice() {
        return defaultPrice;
    }

    @Override
    public void clear() {
        defaultPrice = null;
        setValidExcise(false);
    }

    @Override
    public CiggyPluginController getController() {
        return (CiggyPluginController) super.getController();
    }

    @Override
    protected String getMessageForScanMark() {
        if (getController().getModel().isCanSkipScanMarkForm()) {
            return ResBundleVisualization.getString("SCAN_MARK_OR_CANCEL");
        }

        return ResBundleVisualization.getString("SCAN_MARK");
    }
}