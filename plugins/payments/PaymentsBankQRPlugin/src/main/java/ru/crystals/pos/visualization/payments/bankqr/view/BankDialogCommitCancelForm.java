package ru.crystals.pos.visualization.payments.bankqr.view;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BankQRPaymentEntity;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.payments.bankqr.controller.BankQRPaymentController;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.JTextPane;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

public class BankDialogCommitCancelForm extends
        AbstractPaymentForm<BankQRPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                BankQRPaymentController> {

    private final JTextPane messageLabel;
    private final CommonYesNoPanel commonYesNoPanel;
    private BankDialog dialog;

    public BankDialogCommitCancelForm(XListener outerListener) {
        super(outerListener);
        messageLabel = new JTextPane();
        messageLabel.setEditable(false);
        Style.setactionStatusTextPaneStyle(messageLabel);
        messageLabel.setPreferredSize(new ScalableDimension(600, 185));

        this.setLayout(new FlowLayout());
        this.add(messageLabel);

        commonYesNoPanel = new CommonYesNoPanel(ResBundleVisualization.getString("BUTTON_NO"), ResBundleVisualization.getString("BUTTON_YES"));
        this.add(commonYesNoPanel);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return null;
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        return null;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return null;
    }

    @Override
    public void showForm(BankQRPaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        dialog = ((BankQRPaymentInfo) info).getDialog();
        messageLabel.setText(dialog.getMessage());
        if (dialog.getButtons() != null && dialog.getButtons().size() > 1) {
            commonYesNoPanel.setYesButtonCaption(dialog.getButtons().get(1));
            commonYesNoPanel.setNoButtonCaption(dialog.getButtons().get(0));
        } else {
            commonYesNoPanel.setYesButtonCaption(ResBundleVisualization.getString("BUTTON_YES"));
            commonYesNoPanel.setNoButtonCaption(ResBundleVisualization.getString("BUTTON_NO"));
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (commonYesNoPanel.isYes()) {
                getController().sendBankOperationResponse(dialog, "");
            } else {
                getController().closeBankDialog();
            }
        }
        return true;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return true;
    }

}
