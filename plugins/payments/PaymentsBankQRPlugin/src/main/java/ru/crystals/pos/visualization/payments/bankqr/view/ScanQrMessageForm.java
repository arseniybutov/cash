package ru.crystals.pos.visualization.payments.bankqr.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.math.BigDecimal;

public class ScanQrMessageForm extends CommonProductForm {

    protected JLabel messageLabel;
    private CommonMessageForm.ExitState exitState = CommonMessageForm.ExitState.TO_LAST;

    public enum ExitState {
        TO_EXIT, TO_LAST
    }

    public ScanQrMessageForm(XListener outerListener) {
        super(outerListener);

        messageLabel = new JLabel("");
        messageLabel.setFont(MyriadFont.getItalic(30F));
        messageLabel.setForeground(Color.darkGreyLabel);
        messageLabel.setPreferredSize(new ScaleDimension(600, 33));
        messageLabel.setPreferredSize(new ScalableDimension(600, 260));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        this.add(messageLabel);
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        return !isVisible();
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return !isVisible();
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return !isVisible();
    }

    @Override
    public JPanel createHeaderPanel() {
        return null;
    }

    @Override
    public JPanel createQuantityPanel() {
        return null;
    }

    @Override
    public JPanel createSummPanel() {
        return null;
    }

    @Override
    public JPanel createUnitPanel() {
        return null;
    }

    @Override
    public BigDecimal getQuantity() {
        return null;
    }

    @Override
    public BigDecimal getPrice() {
        return null;
    }

    @Override
    public void clear() {

    }

    public CommonMessageForm.ExitState getExitState() {
        return exitState;
    }

    public void setExitState(CommonMessageForm.ExitState exitState) {
        this.exitState = exitState;
    }

    public void setMessage(String text) {
        if (text != null) {
            text = text.replace("\n", "<br>");
            messageLabel.setText("<html><i>" + text + "</i></html>");
        }
    }
}
