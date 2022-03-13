package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ExciseValidationErrorType;
import ru.crystals.pos.egais.excise.validation.ExciseValidationResult;
import ru.crystals.pos.egais.excise.validation.ds.ProductType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.utils.Swing;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

/**
 * Created by m.smirnov on 16.09.2016.
 */
public class SpiritExciseValidationForm extends CommonForm<ProductEntity, PositionEntity> {
    private final JLabel messageLabel = new JLabel();
    private final CommonYesNoPanel yesNoPanel = new CommonYesNoPanel(ResBundleVisualization.getString("BUTTON_NO"), ResBundleVisualization.getString(
            "SCAN_EXCISE_ACCEPT"));
    private final ExciseValidation egaisExciseCheckValidation;

    public SpiritExciseValidationForm(ExciseValidation egaisExciseCheckValidation, XListener outerListener) {
        super(outerListener);
        this.egaisExciseCheckValidation = egaisExciseCheckValidation;
        setLayout(new BorderLayout());
        messageLabel.setFont(MyriadFont.getItalic(33F));
        messageLabel.setForeground(ru.crystals.pos.visualization.styles.Color.secondTitleForeGround);
        messageLabel.setBorder(new EmptyBorder(0, Scale.getX(16), 0, 0));
        messageLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        add(messageLabel, BorderLayout.CENTER);
        add(yesNoPanel, BorderLayout.SOUTH);
    }

    public void setMessage(ExciseValidationResult exciseValidationResult) {
        switch (egaisExciseCheckValidation.getCurrentExciseValidationType(ProductType.SPIRITS.name())) {
            case SetRetail: {
                Swing.wrapLabelTextUsingSeparators(this.messageLabel, exciseValidationResult.operationMessage);
                yesNoPanel.setVisible(controller.isRefund() && exciseValidationResult.errorType == ExciseValidationErrorType.EXCISE_IS_NOT_EXIST);
                break;
            }
            default: {
                Swing.wrapLabelTextUsingSeparators(this.messageLabel, exciseValidationResult.operationMessage);
                yesNoPanel.setVisible(false);
                break;
            }
        }
    }

    public boolean isYes() {
        //yes и no мен¤ютс¤ местами, т.к. yes должен быть справа!
        return !yesNoPanel.isYes();
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            yesNoPanel.selectNo();
            return false;
        } else {
            return e.getKeyCode() != KeyEvent.VK_ENTER || !yesNoPanel.isVisible();
        }
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public void clear() {
        //
    }

}
