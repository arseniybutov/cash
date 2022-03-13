package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ExciseValidationType;
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
 * Created by m.smirnov on 22.09.2016.
 */
public class SpiritConnectionWarningForm extends CommonForm<ProductEntity, PositionEntity> {
    private final JLabel messageLabel = new JLabel();
    private final CommonYesNoPanel yesNoPanel = new CommonYesNoPanel(ResBundleVisualization.getString("REPEAT_PENDING"), ResBundleVisualization.getString(
            "SCAN_EXCISE_ACCEPT"
    ));
    private final ExciseValidation egaisExciseCheckValidation;

    public SpiritConnectionWarningForm(ExciseValidation egaisExciseCheckValidation, XListener outerListener) {
        super(outerListener);
        this.egaisExciseCheckValidation = egaisExciseCheckValidation;
        setLayout(new BorderLayout());
        messageLabel.setFont(MyriadFont.getItalic(33F));
        messageLabel.setForeground(ru.crystals.pos.visualization.styles.Color.secondTitleForeGround);
        messageLabel.setBorder(new EmptyBorder(0, Scale.getX(16), 0, 0));
        messageLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        add(messageLabel, BorderLayout.CENTER);
        add(yesNoPanel, BorderLayout.SOUTH);
        switch (egaisExciseCheckValidation.getCurrentExciseValidationType(ProductType.SPIRITS.name())) {
            case SetRetail: {
                yesNoPanel.setVisible(true);
                break;
            }
            default: {
                yesNoPanel.setVisible(false);
                break;
            }
        }


    }

    public void setMessage(String message) {
        switch (egaisExciseCheckValidation.getCurrentExciseValidationType(ProductType.SPIRITS.name())) {
            case SetRetail: {
                yesNoPanel.setVisible(true);
                break;
            }
            default: {
                yesNoPanel.setVisible(false);
                break;
            }
        }
        Swing.wrapLabelTextUsingSeparators(this.messageLabel, message);
    }

    public boolean isYes() {
        //yes и no меняются местами, т.к. yes должен быть справа!
        return !yesNoPanel.isYes();
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            yesNoPanel.selectNo();
            return false;
        } else {
            return e.getKeyCode() != KeyEvent.VK_ENTER;
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

    public ExciseValidationType getFormType() {
        return egaisExciseCheckValidation.getCurrentExciseValidationType(ProductType.SPIRITS.name());
    }
}
