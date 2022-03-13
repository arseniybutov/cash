package ru.crystals.pos.visualization.payments.siebelbonuscard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.annotation.ConditionalOnBean;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.SiebelBonusCardPaymentEntity;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.siebelbonuscard.ResBundlePaymentSiebelBonusCard;
import ru.crystals.pos.visualization.payments.siebelbonuscard.controller.SiebelBonusCardPaymentController;
import ru.crystals.pos.visualization.payments.siebelbonuscard.model.SiebelBonusCardPaymentModel;
import ru.crystals.pos.visualization.payments.siebelbonuscard.view.SiebelBonusCardPaymentView;

import javax.swing.JPanel;
import java.util.Optional;

/**
 * Тут мы пишем что угодно - лишь бы обеспечивалась совместимость плагина с существующей моделью кассы Т.е. весь "странный" код(говнокод) должен быть
 * тут
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.SIEBEL_BONUS_CARD_PAYMENT_ENTITY, mainEntity = SiebelBonusCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
@ConditionalOnBean({SiebelService.class})
public class SiebelBonusCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter {
    private final SiebelBonusCardPaymentModel model;
    private final SiebelBonusCardPaymentView view;
    private final SiebelBonusCardPaymentController controller;
    private final SiebelService service;
    private final Properties properties;

    @Autowired
    SiebelBonusCardPaymentPluginAdapter(Properties properties, SiebelService service, SiebelBonusCardPaymentController controller) {
        this.controller = controller;
        model = new SiebelBonusCardPaymentModel();
        view = new SiebelBonusCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);

        this.service = service;
        this.properties = properties;
    }

    @Override
    protected SiebelBonusCardPaymentController getController() {
        return controller;
    }

    @Override
    protected SiebelBonusCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected SiebelBonusCardPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentSiebelBonusCard.getString("SIEBEL_BONUSCARD_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentSiebelBonusCard.getString("SIEBEL_BONUSCARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        //Если включеная настрока запрета смешанных типов оплат, то этот плагин становится выключенным по умолчанию
        //(https://crystals.atlassian.net/browse/SRTB-1081)
        if (properties.isMixedPaymentProhibited()) {
            return false;
        }
        // Тип оплат для чека продажи всегда недоступен - необходимо использовать бонусы как скидка
        return service != null && Optional.ofNullable(Factory.getTechProcessImpl().getCheck())
                .map(p -> p.isReturn() && p.getSuperPurchase() != null).orElse(false);
    }
}
