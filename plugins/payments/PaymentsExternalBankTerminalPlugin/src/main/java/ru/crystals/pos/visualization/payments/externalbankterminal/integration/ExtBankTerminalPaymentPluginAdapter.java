package ru.crystals.pos.visualization.payments.externalbankterminal.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.payments.ExternalBankTerminalPaymentEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.externalbankterminal.ResBundlePaymentExternalBankTerminal;
import ru.crystals.pos.visualization.payments.externalbankterminal.forms.ExtBankTerminalPaymentView;

import javax.swing.JPanel;

@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.EXTERNAL_BANK_TERMINAL_PAYMENT_ENTITY, mainEntity = ExternalBankTerminalPaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class ExtBankTerminalPaymentPluginAdapter extends AbstractPaymentPluginAdapter {
    private InternalCashPoolExecutor threadPool;
    private final ExtBankTerminalPaymentModel model;
    private final ExtBankTerminalPaymentView view;
    private final ExtBankTerminalPaymentController controller;
    private final InternalCashPoolExecutor internalCashPoolExecutor;

    @Autowired
    ExtBankTerminalPaymentPluginAdapter(InternalCashPoolExecutor internalCashPoolExecutor) {
        this.controller = new ExtBankTerminalPaymentController();
        model = new ExtBankTerminalPaymentModel();
        view = new ExtBankTerminalPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);

        this.internalCashPoolExecutor = internalCashPoolExecutor;
    }

    @Override
    protected ExtBankTerminalPaymentController getController() {
        return controller;
    }

    @Override
    protected ExtBankTerminalPaymentModel getModel() {
        return model;
    }

    @Override
    protected ExtBankTerminalPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        return ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL");
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    public InternalCashPoolExecutor getThreadPool() {
        return internalCashPoolExecutor;
    }
}
