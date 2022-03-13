package ru.crystals.pos.visualization.payments.cftgiftcard.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.cards.cft.CFTBridge;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.model.cft.CFTGiftCardsModel;
import ru.crystals.pos.payments.CFTGiftCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPluginDisabledReason;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.payments.cftgiftcard.ResBundlePaymentCftGiftCard;
import ru.crystals.pos.visualization.payments.cftgiftcard.controller.CFTGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.cftgiftcard.model.CFTGiftCardPaymentModel;
import ru.crystals.pos.visualization.payments.cftgiftcard.view.CFTGiftCardPaymentView;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;

import javax.annotation.PostConstruct;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Тут мы пишем что угодно - лишь бы обеспечивалась совместимость плагина с
 * существующей моделью кассы Т.е. весь "странный" код(говнокод) должен быть тут
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.CFTGIFT_CARD_PAYMENT_ENTITY, mainEntity = CFTGiftCardPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class CFTGiftCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter implements PaymentPluginDisabledReason {

    @Autowired(required = false)
    @Qualifier(Constants.CFTType.Names.CFT_GIFTCARDS_PROCESSING_NAME)
    private CFTBridge cft;

    @Autowired
    private Properties properties;
    private final CFTGiftCardPaymentModel model;
    private final CFTGiftCardPaymentView view;
    private final CFTGiftCardPaymentController controller;
    private CFTGiftCardsModel cftGiftCardsModel;

    public CFTGiftCardPaymentPluginAdapter() {
        this.controller = new CFTGiftCardPaymentController();
        model = new CFTGiftCardPaymentModel();
        view = new CFTGiftCardPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Override
    protected CFTGiftCardPaymentController getController() {
        return controller;
    }

    @Override
    protected CFTGiftCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected CFTGiftCardPaymentView getView() {
        return view;
    }

    @PostConstruct
    private void localInit() {
        cftGiftCardsModel = new CFTGiftCardsModel(cft);
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_PAYMENT");
    }

    @Override
    public boolean isActivated() {
        //Если включеная настрока запрета смешанных типов оплат, то этот плагин становится выключенным по умолчанию
        // (https://crystals.atlassian.net/browse/SRTB-1081)
        if (properties.isMixedPaymentProhibited()) {
            return false;
        }
        return cft != null;
    }

    @Override
    public void preparePrintCheck(List<ServiceDocument> serviceDocuments, PurchaseEntity purchase) {
        List<CFTGiftCardPaymentEntity> giftCardPayments = new ArrayList<>();
        for (PaymentEntity p : purchase.getPayments()) {
            if (p instanceof CFTGiftCardPaymentEntity) {
                giftCardPayments.add((CFTGiftCardPaymentEntity) p);
            }
        }

        if (!giftCardPayments.isEmpty()) {
            SimpleServiceDocument slip = new SimpleServiceDocument();
            slip.setPromo(true);
            List<String> rows = new ArrayList<>();
            for (CFTGiftCardPaymentEntity p : giftCardPayments) {
                if (p.getActivationSlip() != null) {
                    rows.add(p.getActivationSlip());
                }
                if (p.getDeactivationSlip() != null) {
                    rows.add(p.getDeactivationSlip());
                }
            }
            slip.addText(rows);
            serviceDocuments.add(slip);
        }
    }

    @Override
    public boolean canApplyOnArbitraryRefund() {
        return false;
    }

    @Override
    public String getDisabledReason() {
        if (isPositionsRefund()) {
            return ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_REFUND_FORBIDDEN");
        }
        return null;
    }

    public CFTGiftCardsModel getCFTGiftModel() {
        return cftGiftCardsModel;
    }
}
