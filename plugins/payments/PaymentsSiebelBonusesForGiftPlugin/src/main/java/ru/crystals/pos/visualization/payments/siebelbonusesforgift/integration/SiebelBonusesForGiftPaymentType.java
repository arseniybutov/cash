package ru.crystals.pos.visualization.payments.siebelbonusesforgift.integration;

import ru.crystals.pos.PaymentPluginComponent;
import ru.crystals.pos.PluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.payments.PaymentsDiscriminators;

/**
 * Заглушечный тип оплаты "Бонусы Siebel на подарок".
 *
 * @since 10.2.84.0
 */
@PaymentPluginComponent(
        value = PaymentsDiscriminators.SIEBEL_BONUSES_FOR_GIFT_PAYMENT_ENTITY,
        typeName = PaymentsDiscriminators.SIEBEL_BONUSES_FOR_GIFT_PAYMENT_ENTITY,
        config = "config/plugins/payments-siebelBonusesForGift-config.xml")
@PluginQualifier(PluginType.PAYMENTS)
@ConditionalOnBean({SiebelService.class})
public class SiebelBonusesForGiftPaymentType extends PaymentType {

    static final String PAYMENT_TYPE = PaymentsDiscriminators.SIEBEL_BONUSES_FOR_GIFT_PAYMENT_ENTITY;

    @Override
    public String getPaymentType() {
        return PAYMENT_TYPE;
    }
}
