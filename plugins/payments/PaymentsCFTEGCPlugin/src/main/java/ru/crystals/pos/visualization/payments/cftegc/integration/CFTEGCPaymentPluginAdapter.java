package ru.crystals.pos.visualization.payments.cftegc.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.cards.cft.CFTBridge;
import ru.crystals.pos.check.CheckStatus;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.model.cft.CFTEGCModel;
import ru.crystals.pos.payments.CFTEGCPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPluginDisabledReason;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.visualization.payments.cftegc.ResBundlePaymentCFTEGC;
import ru.crystals.pos.visualization.payments.cftegc.controller.CFTEGCPaymentController;
import ru.crystals.pos.visualization.payments.cftegc.model.CFTEGCPaymentModel;
import ru.crystals.pos.visualization.payments.cftegc.view.CFTEGCPaymentView;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;

import javax.annotation.PostConstruct;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Плагин оплаты по ЭПС ЦФТ
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.CFTEGCPAYMENT_ENTITY, mainEntity = CFTEGCPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class CFTEGCPaymentPluginAdapter extends AbstractPaymentPluginAdapter implements PaymentPluginDisabledReason {
    @Autowired(required = false)
    @Qualifier(Constants.CFTType.Names.CFT_EGC_PROCESSING_NAME)
    private CFTBridge cft;

    @Autowired
    private Properties properties;
    private final CFTEGCPaymentModel model;
    private final CFTEGCPaymentView view;
    private final CFTEGCPaymentController controller;
    private CFTEGCModel cftegcModel;

    public CFTEGCPaymentPluginAdapter() {
        this.controller = new CFTEGCPaymentController();
        model = new CFTEGCPaymentModel();
        view = new CFTEGCPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Override
    protected CFTEGCPaymentController getController() {
        return controller;
    }

    @Override
    protected CFTEGCPaymentModel getModel() {
        return model;
    }

    @Override
    protected CFTEGCPaymentView getView() {
        return view;
    }

    @PostConstruct
    private void localInit(){
        cftegcModel = new CFTEGCModel(cft);
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentCFTEGC.getString("CFT_GIFTCARD_PAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentCFTEGC.getString("CFT_GIFTCARD_PAYMENT");
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
        if (purchase.isSale() && purchase.getCheckStatus() != CheckStatus.Cancelled) {
            List<CFTEGCPaymentEntity> giftEGCPayments = new ArrayList<>();
            for (PaymentEntity p : purchase.getPayments()) {
                if (p instanceof CFTEGCPaymentEntity) {
                    giftEGCPayments.add((CFTEGCPaymentEntity) p);
                }
            }

            if (!giftEGCPayments.isEmpty()) {
                SimpleServiceDocument slip = new SimpleServiceDocument();
                slip.setPromo(true);
                List<String> rows = new ArrayList<>();
                rows.add(ResBundlePaymentCFTEGC.getString("CFT_GIFTCARD_PAYMENT"));
                rows.add("------------------------------------------");
                for (CFTEGCPaymentEntity p : giftEGCPayments) {
                    rows.add(p.getActivationSlip());
                }
                rows.add("------------------------------------------");
                slip.addText(rows);
                serviceDocuments.add(slip);
            }
        }
    }

    @Override
    public boolean canApplyOnArbitraryRefund() {
        return false;
    }

    @Override
    public String getDisabledReason() {
        if (isPositionsRefund()) {
            return ResBundlePaymentCFTEGC.getString("CFT_EGC_REFUND_FORBIDDEN");
        }
        return null;
    }

    public CFTEGCModel getCFTEGCModel() {
        return cftegcModel;
    }
}
