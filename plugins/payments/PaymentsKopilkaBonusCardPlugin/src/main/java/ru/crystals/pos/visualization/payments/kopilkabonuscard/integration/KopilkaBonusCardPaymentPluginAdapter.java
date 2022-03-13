package ru.crystals.pos.visualization.payments.kopilkabonuscard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.kopilka.KopilkaService;
import ru.crystals.pos.payments.KopilkaPaymentEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.ResBundlePaymentKopilkaBonusCard;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.controller.KopilkaBonusCardPaymentController;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.model.KopilkaBonusCardPaymentModel;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.view.KopilkaBonusCardPaymentView;

import javax.swing.JPanel;

@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.KOPILKA_PAYMENT_ENTITY, mainEntity = KopilkaPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
@ConditionalOnBean({KopilkaService.class})
public class KopilkaBonusCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter {
    private final KopilkaBonusCardPaymentModel model;
    private final KopilkaBonusCardPaymentView view;
    private final KopilkaBonusCardPaymentController controller;
    private Properties properties;

    @Autowired
    public KopilkaBonusCardPaymentPluginAdapter(KopilkaBonusCardPaymentController controller) {
        this.controller = controller;
        model = new KopilkaBonusCardPaymentModel();
        view = new KopilkaBonusCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Override
    protected KopilkaBonusCardPaymentController getController() {
        return controller;
    }

    @Override
    protected KopilkaBonusCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected KopilkaBonusCardPaymentView getView() {
        return view;
    }

    @Autowired
    void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_BONUSCARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        if (properties.isMixedPaymentProhibited()) {
            return false;
        }
        return true;
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_BONUSCARD_PAYMENT");
    }
}
