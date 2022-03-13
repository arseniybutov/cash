package ru.crystals.pos.visualization.payments.bonuscard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.internalcards.InternalCards;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.cards.PluginCard;
import ru.crystals.pos.cards.informix.InformixService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentModel;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentState;
import ru.crystals.pos.visualization.payments.bonuscard.view.BonusCardPaymentView;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;

import javax.swing.JPanel;


@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.BONUS_CARD_PAYMENT_ENTITY, mainEntity = BonusCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class BonusCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter {
    private InternalCards service;
    private final BonusCardPaymentModel model;
    private final BonusCardPaymentView view;
    private final BonusCardPaymentController controller;
    private Properties properties;
    private InformixService informixService;

    public BonusCardPaymentPluginAdapter() {
        this.controller = new BonusCardPaymentController();
        model = new BonusCardPaymentModel();
        view = new BonusCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Autowired
    void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    void setInformixService(InformixService informixService) {
        this.informixService = informixService;
    }

    @Override
    protected BonusCardPaymentController getController() {
        return controller;
    }

    @Override
    protected BonusCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected BonusCardPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        if (isRefund()) {
            ResBundlePaymentBonusCard.getString("REFUND_BONUS_PAYMENT");
        }
        return ResBundlePaymentBonusCard.getString("BONUS_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        //Если включеная настрока запрета смешанных типов оплат, то этот плагин становится выключенным по умолчанию
        //(https://crystals.atlassian.net/browse/SRTB-1081)
        if (properties.isMixedPaymentProhibited()) {
            return false;
        }
        if (getPaymentService() instanceof InformixService) {
            PurchaseEntity currentCheck = Factory.getTechProcessImpl().getCheck();
            return informixService != null &&
                    currentCheck.getClientGUID() != null && !currentCheck.isReturn() || isRefund();
        }
        return true;
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentBonusCard.getString("RETURN_PAYMENT_BONUS_CARD");
    }

    private void initService() {
        for (PluginCard card : getFactory().getCards()) {

            if (card instanceof InternalCards) {
                service = (InternalCards) card;
                break;
            }
        }
    }

    @Override
    protected void doProcessPayment() {
        getTechProcessEvents().eventAddCashlessPayment(getFactory().getTechProcessImpl().getCheckWithNumber(), getPayment());
        super.doProcessPayment();
    }

    public InternalCards getPaymentService() {
        if (service == null) {
            initService();
        }
        return service;
    }

    @Override
    public boolean isMoveCursorEnabled() {
        return getModel().getState() != BonusCardPaymentState.CHOOSE_ACCOUNT;
    }
}
