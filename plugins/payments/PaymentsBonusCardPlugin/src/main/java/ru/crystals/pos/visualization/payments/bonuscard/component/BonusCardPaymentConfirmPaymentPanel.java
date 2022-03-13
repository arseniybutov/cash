package ru.crystals.pos.visualization.payments.bonuscard.component;

import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;


public class BonusCardPaymentConfirmPaymentPanel extends CommonProductInputPanel {

    public BonusCardPaymentConfirmPaymentPanel(InputType type, String welcome, String label, int length) {
        super(type, welcome, label, length);
    }

    public BonusCardPaymentConfirmPaymentPanel(InputType type, String welcome, String label) {
        super(type, welcome, label);
    }

    public BonusCardPaymentConfirmPaymentPanel(InputType type) {
        super(type);
    }

    public BonusCardPaymentConfirmPaymentPanel() {
        super(CommonProductInputPanel.InputType.SUMM);
    }

    public void setAvailableSummToPayment(String sum) {
        setLabelText(ResBundlePaymentBonusCard.getString("SUMMA_TO_PAY") + " (" + ResBundlePaymentBonusCard.getString("NOT_MORE") + " " + sum + " )");
    }

}
