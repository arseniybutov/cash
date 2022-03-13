package ru.crystals.pos.visualization.payments.bonuscard.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.component.BonusCardPaymentConfirmPaymentPanel;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class BonusCardPaymentConfirmForm extends
        AbstractPaymentForm<BonusCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, BonusCardPaymentConfirmPaymentPanel,
                BonusCardPaymentController> {

    public BonusCardPaymentConfirmForm(XListener outerListener) {
        super(outerListener);
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
        return new CommonPaymentHeaderPanel(ResBundlePaymentBonusCard.getString("BONUS_PAYMENT"), ResBundlePaymentBonusCard.getString("REFUND_BONUS_PAYMENT"));
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
    public BonusCardPaymentConfirmPaymentPanel createFooterPanel() {
        return new BonusCardPaymentConfirmPaymentPanel();
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (isCommaOrDigitOrBackspace.test(e)) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            BigDecimal amountFromPanel = footerPanel.getDoubleValue();
            return !((amountFromPanel.compareTo(BigDecimal.ZERO) > 0) && CurrencyUtil.checkPaymentRatio(BigDecimalConverter.convertMoney(amountFromPanel)));
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean() || getController().isWriteOffAllAmount()) {
                return false;
            } else {
                footerPanel.clear();
                return true;
            }
        }
        return false;
    }

    public BigDecimal getSumToPay() {
        return footerPanel.getDoubleValue();
    }

    @Override
    public void showForm(BonusCardPaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        if (controller.isRefund()) {
            BigDecimal value = (payment.getSumPay() != null && BigDecimalConverter.convertMoney(payment.getSumPay()).compareTo(info.getSurcharge()) < 0) ?
                    BigDecimalConverter.convertMoney(payment.getSumPay()) :
                    info.getSurcharge();
            footerPanel.setMaximumValue(value);
            footerPanel.setDoubleValue(value);
            footerPanel.setLabelText(ResBundleVisualization.getString("PC_SUMMA"));
        } else {
            footerPanel.setAvailableSummToPayment(CurrencyUtil.formatSum(((BonusCardPaymentInfo) info).getAvailableBalance()));
            BigDecimal presetValue = ((BonusCardPaymentInfo) info).getAvailableBalance().compareTo(info.getSurcharge()) > 0 ? info.getSurcharge() :
                    ((BonusCardPaymentInfo) info).getAvailableBalance();
            //Теперь округлим вниз, чтобы не вводить пользователя в заблуждение, ибо в РБ на счете может быть 9'020 но по факту списать в соответствии с минимальной
            // валютой можно 9'000
            presetValue = CurrencyUtil.roundDown(presetValue);
            footerPanel.setMaximumValue(presetValue);
            if (Boolean.FALSE.equals(payment.getPaymentSettings().getChangePaymentAmount())) {
                footerPanel.setMinimumValue(presetValue);
            }
            footerPanel.setDoubleValue(presetValue);
            footerPanel.setInputEnabled(!getController().isWriteOffAllAmount());
        }
    }
}
