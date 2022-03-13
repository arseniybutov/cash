package ru.crystals.pos.visualization.payments.bonuscard.view;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.styles.Style;

import javax.swing.JTextPane;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

public class BonusCardPaymentWriteOffConfirmForm extends
        AbstractPaymentForm<BonusCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                BonusCardPaymentController> {

    private JTextPane messageLabel;
    private CommonYesNoPanel commonYesNoPanel;

    public BonusCardPaymentWriteOffConfirmForm(XListener outerListener) {
        super(outerListener);
        messageLabel = new JTextPane();
        messageLabel.setEditable(false);
        Style.setactionStatusTextPaneStyle(messageLabel);
        messageLabel.setPreferredSize(new ScalableDimension(600, 185));

        this.setLayout(new FlowLayout());
        this.add(messageLabel);

        commonYesNoPanel = new CommonYesNoPanel(ResBundlePaymentBonusCard.getCommonString("KEY_CONFIRM"), ResBundlePaymentBonusCard.getString("CANCEL"));
        this.add(commonYesNoPanel);
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
    public boolean keyPressedNew(XKeyEvent e) {
        return e.getKeyCode() != KeyEvent.VK_ENTER;
    }


    public boolean isConfirmButtonPressed() {
        return commonYesNoPanel.isYes();
    }

    @Override
    public void showForm(BonusCardPaymentEntity payment, PaymentInfo info) {
        messageLabel.setText(String.format(ResBundlePaymentBonusCard.getString("AMOUNT_ALERT"),
                CurrencyUtil.formatSum(((BonusCardPaymentInfo) info).getAvailableBalance().movePointRight(2).longValue() - payment.getSumPay())));
    }
}
