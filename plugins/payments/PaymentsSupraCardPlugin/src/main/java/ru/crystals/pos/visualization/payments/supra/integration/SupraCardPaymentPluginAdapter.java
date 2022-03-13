package ru.crystals.pos.visualization.payments.supra.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnModule;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.SupraPaymentEntity;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;
import ru.crystals.pos.visualization.payments.supra.controller.SupraCardPaymentController;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentModel;
import ru.crystals.pos.visualization.payments.supra.view.SupraCardPaymentView;
import ru.crystals.supra.SupraBridge;

import javax.swing.JPanel;

/**
 * Тут мы пишем что угодно - лишь бы обеспечивалась совместимость плагина с существующей моделью кассы Т.е. весь "странный" код(говнокод) должен быть
 * тут
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.SUPRA_PAYMENT_ENTITY, mainEntity = SupraPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
@ConditionalOnModule(Constants.SUPRA_CARD_PROCESSING_NAME)
public class SupraCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter {
    private final SupraBridge supraBridge;
    private final SupraCardPaymentModel model;
    private final SupraCardPaymentView view;
    private final SupraCardPaymentController controller;

    @Autowired
    SupraCardPaymentPluginAdapter(SupraBridge supraBridge, SupraCardPaymentController controller) {
        this.supraBridge = supraBridge;
        this.controller = controller;
        model = new SupraCardPaymentModel();
        view = new SupraCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Override
    protected SupraCardPaymentController getController() {
        return controller;
    }

    @Override
    protected SupraCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected SupraCardPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentSupraCard.getString("SUPRA_CARD_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentSupraCard.getString("SUPRA_CARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        return supraBridge != null;
    }
}
