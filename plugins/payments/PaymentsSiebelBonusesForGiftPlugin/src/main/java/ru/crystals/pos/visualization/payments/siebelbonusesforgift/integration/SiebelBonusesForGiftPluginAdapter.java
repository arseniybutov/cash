package ru.crystals.pos.visualization.payments.siebelbonusesforgift.integration;


import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.payments.PaymentPlugin;
import ru.crystals.pos.payments.SiebelBonusesForGiftPaymentEntity;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;

import javax.swing.JPanel;

/**
 * Этот тип оплат автоматизирован и его ручное введение не предусмотрнено.
 * Адаптер не имплементирован.
 */
@PaymentCashPluginComponent(typeName = SiebelBonusesForGiftPaymentType.PAYMENT_TYPE, mainEntity = SiebelBonusesForGiftPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
@ConditionalOnBean({SiebelService.class})
public class SiebelBonusesForGiftPluginAdapter extends PaymentContainer implements PaymentPlugin {

    @Override
    public boolean isActivated() {
        return false;
    }

    @Override
    public String getPaymentString() {
        return null;
    }

    @Override
    public void setPaymentFields() {
        //
    }

    @Override
    public String getPaymentType() {
        return null;
    }

    @Override
    public String getChargeName() {
        return null;
    }

    @Override
    public String getTitlePaymentType() {
        return null;
    }

    @Override
    public String getReturnPaymentString() {
        return null;
    }

    @Override
    public String getPaymentTypeName() {
        return null;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return false;
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return null;
    }

    @Override
    public JPanel getVisualPanel() {
        return null;
    }

    @Override
    public void number(Byte num) {
        //
    }

    @Override
    public void enter() {
        //
    }

    @Override
    public void reset() {
        //
    }

    @Override
    public long getSum() {
        return 0;
    }

    @Override
    public void setSum(long sum) {
        //
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return false;
    }

    @Override
    public boolean isChangeAvailable() {
        return false;
    }
}