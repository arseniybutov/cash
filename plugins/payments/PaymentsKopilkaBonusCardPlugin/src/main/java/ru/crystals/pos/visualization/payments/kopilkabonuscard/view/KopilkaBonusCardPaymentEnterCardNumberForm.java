package ru.crystals.pos.visualization.payments.kopilkabonuscard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.ResBundlePaymentKopilkaBonusCard;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.controller.KopilkaBonusCardPaymentController;

import java.awt.event.KeyEvent;

/**
 * Форма ввода номера карты Копилка, доступен только ввод по MSR (NFC)
 */
public class KopilkaBonusCardPaymentEnterCardNumberForm extends
        AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
                CommonProductInputPanel, KopilkaBonusCardPaymentController> {
    KopilkaBonusCardPaymentEnterCardNumberForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_BONUSCARD_PAYMENT"),
                ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_BONUSCARD_PAYMENT"));
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        return new CommonPaymentPaidPanel();
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return new CommonPaymentToPayPanel();
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.CARD_NUMBER);
    }

    /**
     * Enter и Escape могут пробрасываться наружу (если вернуть false)
     * остальные нажатия обрабатываются внутри формы
     */
    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.getTextValue() == null || footerPanel.getTextValue().isEmpty()) {
                if (controller != null) {
                    controller.beepError("KopilkaBonusCardPayment: empty card number field, cannot process info");
                }
                return true;
            }
            return false;
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
            if (controller != null) {
                controller.beepError("KopilkaBonusCardPayment: empty card number field, cannot process info");
            }
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                return false;
            } else {
                footerPanel.reset();
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

}
