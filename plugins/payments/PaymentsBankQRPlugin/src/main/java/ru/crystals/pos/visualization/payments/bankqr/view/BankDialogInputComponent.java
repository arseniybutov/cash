package ru.crystals.pos.visualization.payments.bankqr.view;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BankQRPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

public class BankDialogInputComponent extends AbstractPaymentForm<BankQRPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel,
        CommonPaymentToPayPanel, CommonProductInputPanel, BankQRPaymentController> {

    private final CommonProductInputPanel inputPanel;
    private final JTextPane messageLabel;
    private BankDialog dialog;

    public BankDialogInputComponent(XListener outerListener) {
        super(outerListener);
        messageLabel = new JTextPane();
        messageLabel.setEditable(false);
        Style.setactionStatusTextPaneStyle(messageLabel);
        messageLabel.setPreferredSize(new ScalableDimension(600, 160));
        inputPanel = new CommonProductInputPanel(CommonProductInputPanel.InputType.NUMBER_WITH_LEADING_ZEROES_ALLOWED);
        this.setLayout(new FlowLayout());
        this.add(messageLabel);
        this.add(inputPanel, BorderLayout.NORTH);
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return true;
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
        inputPanel.clear();
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            getController().sendBankOperationResponse(dialog, inputPanel.getTextValue());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (inputPanel.isClean()) {
                getController().closeBankDialog();
            } else {
                inputPanel.clear();
            }
        } else {
            inputPanel.keyPressed(e);
        }
        return true;
    }

}
