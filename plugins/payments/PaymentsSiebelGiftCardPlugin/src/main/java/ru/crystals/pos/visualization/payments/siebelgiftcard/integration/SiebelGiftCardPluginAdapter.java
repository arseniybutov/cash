package ru.crystals.pos.visualization.payments.siebelgiftcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.PaymentPluginDisabledReason;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.SiebelGiftCardPaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.siebelgiftcard.ResBundlePaymentSiebelGiftCard;
import ru.crystals.pos.visualization.payments.siebelgiftcard.controller.SiebelGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.siebelgiftcard.model.SiebelGiftCardPaymentModel;
import ru.crystals.pos.visualization.payments.siebelgiftcard.view.SiebelGiftCardPaymentView;

import javax.swing.JPanel;

/**
 * Адаптер плагина оплаты подарочными картами Siebel. Через него плагин общается с кассой.
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.SIEBEL_GIFT_CARD_PAYMENT_ENTITY, mainEntity = SiebelGiftCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
@ConditionalOnBean({SiebelService.class})
public class SiebelGiftCardPluginAdapter extends AbstractPaymentPluginAdapter implements PaymentPluginDisabledReason {
    private final SiebelGiftCardPaymentModel model;
    private final SiebelGiftCardPaymentView view;
    private final SiebelGiftCardPaymentController controller;
    private final SiebelService service;

    @Autowired
    SiebelGiftCardPluginAdapter(SiebelService service, SiebelGiftCardPaymentController controller) {
        this.controller = controller;
        model = new SiebelGiftCardPaymentModel();
        view = new SiebelGiftCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);

        this.service = service;
    }

    @Override
    protected SiebelGiftCardPaymentController getController() {
        return controller;
    }

    @Override
    protected SiebelGiftCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected SiebelGiftCardPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentSiebelGiftCard.getString("GIFTCARD_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentSiebelGiftCard.getString("GIFTCARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        if (service == null) {
            return false;
        }
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();
        return purchase != null && !purchase.isReturn() && purchase.getSuperPurchase() == null;
    }

    @Override
    public boolean canApplyOnArbitraryRefund() {
        return false;
    }

    @Override
    public String getDisabledReason() {
        if (isPositionsRefund()) {
            return ResBundlePaymentSiebelGiftCard.getString("GIFTCARD_REFUND_FORBIDDEN");
        }
        return null;
    }
}
