package ru.crystals.pos.visualization.payments.supra.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;
import ru.crystals.pos.visualization.payments.supra.controller.SupraCardPaymentController;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentInfo;
import ru.crystals.utils.UnboxingUtils;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class SupraCardPaymentEnterSumForm extends SupraCardPaymentBaseForm {
    public SupraCardPaymentEnterSumForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentSupraCard.getString("ENTER_SUM"));
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
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.SUMM);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_COMMA
                || e.getKeyChar() == KeyEvent.VK_COMMA
                || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar())) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                return false;
            } else {
                footerPanel.clear();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    public BigDecimal getSumToPay() {
        return footerPanel.getDoubleValue();
    }

    /***
     * При отображении это формы необходимо показать количество бонусов, которое можно списать
     * Это количество должно быть не больше суммы чека и количество доступных бонусов
     */
    @Override
    public void showForm(PaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        SupraCardPaymentInfo supraInfo = (SupraCardPaymentInfo) info;

        long availableBonuses = UnboxingUtils.valueOf(supraInfo.getAmount());
        long surcharge = BigDecimalConverter.convertMoney(info.getSurcharge());
        BigDecimal presetValue = CurrencyUtil.convertMoney(Math.min(surcharge, availableBonuses));
        //Теперь округлим вниз, чтобы не вводить пользователя в заблуждение, ибо в РБ на счете может быть 9'020 но по факту списать в соответствии с минимальной
        // валютой можно 9'000
        presetValue = CurrencyUtil.roundDown(presetValue);
        footerPanel.setMaximumValue(presetValue);
        footerPanel.setDoubleValue(presetValue);
    }

    public void setController(PaymentController controller) {
        this.controller = (SupraCardPaymentController) controller;
    }

    public SupraCardPaymentController getController() {
        return controller;
    }

}
