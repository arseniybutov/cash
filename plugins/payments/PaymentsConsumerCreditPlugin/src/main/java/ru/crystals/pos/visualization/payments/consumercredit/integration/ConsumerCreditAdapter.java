package ru.crystals.pos.visualization.payments.consumercredit.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.keyboard.Keyboard;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.model.ConsumerCreditModel;
import ru.crystals.pos.visualization.payments.consumercredit.view.ConsumerCreditView;

import javax.swing.JPanel;

/**
 * Адаптер плагина. Через него плагин общается с кассой
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.CONSUMER_CREDIT_PAYMENT_ENTITY, mainEntity = ConsumerCreditPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class ConsumerCreditAdapter extends AbstractPaymentPluginAdapter {
    private static final String CONSUMER_CREDIT = PaymentsDiscriminators.CONSUMER_CREDIT_PAYMENT_ENTITY;
    private final ConsumerCreditModel model;
    private final ConsumerCreditView view;
    private final ConsumerCreditController controller;
    private Properties properties;
    private Keyboard keyboard;

    public ConsumerCreditAdapter() {
        this.controller = new ConsumerCreditController();
        model = new ConsumerCreditModel();
        view = new ConsumerCreditView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Override
    protected ConsumerCreditController getController() {
        return controller;
    }

    @Override
    protected ConsumerCreditModel getModel() {
        return model;
    }

    @Override
    protected ConsumerCreditView getView() {
        return view;
    }

    @Autowired
    void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    void setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentConsumerCredit.getString("CONSUMER_CREDIT_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        //Если включеная настрока запрета смешанных типов оплат, то этот плагин становится выключенным по умолчанию
        //(https://crystals.atlassian.net/browse/SRTB-1081)
        if (properties.isMixedPaymentProhibited()) {
            return false;
        }

        return !getCashPaymentType().getBankProducts().isEmpty();
    }

    public PaymentType getCashPaymentType() {
        return getFactory().getTechProcessImpl().getPaymentTypes().get(CONSUMER_CREDIT);
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentConsumerCredit.getString("CONSUMER_CREDIT_PAYMENT");
    }

    @Override
    public boolean isMoveCursorEnabled() {
        return false;
    }

    public boolean isShowOnscreenKeyboard() {
        return keyboard == null || !keyboard.getProviderName().toLowerCase().contains("qwerty");
    }
}
