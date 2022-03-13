package ru.crystals.pos.visualization.payments.bankqr.integration;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SlipsContainer;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TransactionData;
import ru.crystals.pos.payments.BankQRPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPluginDisabledReason;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.service.BankQRPaymentPluginInitMark;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.payments.bankqr.ResBundlePaymentBankQR;
import ru.crystals.pos.visualization.payments.bankqr.controller.BankQRPaymentController;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentModel;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentState;
import ru.crystals.pos.visualization.payments.bankqr.view.BankQRPaymentView;
import ru.crystals.pos.visualization.payments.bankqr.view.BankQRReturnComponent;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;

import javax.swing.JPanel;

@PaymentCashPluginComponent(typeName = BankQRPaymentPluginAdapter.PAYMENT_TYPE, mainEntity = BankQRPaymentEntity.class)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BankQRPaymentPluginAdapter extends AbstractPaymentPluginAdapter implements PaymentPluginDisabledReason, BankEvent, BankQRPaymentPluginInitMark {
    public static final String PAYMENT_TYPE = PaymentsDiscriminators.BANK_QRPAYMENT_ENTITY;
    private Bank bankModule;
    private BankQRReturnComponent bankQRReturnComponent;
    private final BankQRPaymentController controller;
    private final BankQRPaymentModel model;
    private final BankQRPaymentView view;
    private TechProcessInterface techProcessInterface;

    @Autowired
    BankQRPaymentPluginAdapter(BankQRPaymentController controller) {
        this.controller = controller;
        model = new BankQRPaymentModel();
        view = new BankQRPaymentView();

        new JPanel().add(view);
        model.addModelListener(view);
        view.setController(controller);
        controller.setModel(model);
        controller.setAdapter(this);
    }

    @Autowired(required = false)
    void setBankModule(Bank bankModule) {
        this.bankModule = bankModule;
    }

    @Autowired
    void setTechProcessInterface(TechProcessInterface techProcessInterface) {
        this.techProcessInterface = techProcessInterface;
    }

    @Override
    protected BankQRPaymentController getController() {
        return controller;
    }

    @Override
    protected BankQRPaymentModel getModel() {
        return model;
    }

    @Override
    protected BankQRPaymentView getView() {
        return view;
    }

    private BankQRReturnComponent getBankQRReturnComponent() {
        if (bankQRReturnComponent == null) {
            bankQRReturnComponent = new BankQRReturnComponent();
        }
        return bankQRReturnComponent;
    }

    @Override
    public String getTitlePaymentType() {
        return getTitlePaymentType(false);
    }

    @Override
    public void setBankId(String bankId) {
        if (StringUtils.isNotBlank(bankId)) {
            super.setBankId(bankId);
        }
    }

    @Override
    public String getReturnPaymentString(PaymentEntity payment) {
        String title = getResourceRefundString();
        return createBankTitleMessage(title, ((BankQRPaymentEntity) payment).getBankid());
    }

    @Override
    public String getReturnPaymentString() {
        return getTitlePaymentType(true);
    }

    public Bank getBankModule() {
        return bankModule;
    }

    private String getTitlePaymentType(boolean isRefund) {
        String title;
        if (isRefund || isRefund()) {
            title = getResourceRefundString();
        } else if (isPositionsRefund()) {
            title = getResourceRefundString();
        } else {
            title = getResourcePaymentString();
        }
        return createBankTitleMessage(title, getBankId());
    }

    protected String getResourcePaymentString() {
        return ResBundlePaymentBankQR.getString("QR_PAYMENT");
    }

    protected String getResourceRefundString() {
        return ResBundlePaymentBankQR.getString("QR_PAYMENT_REFUND");
    }

    @Override
    public boolean isActivated() {
        return isActivated(getBankId());
    }

    @Override
    public boolean isActivated(String providerId) {
        return getBankModule() != null && getBankModule().isProviderAvailable(providerId, BankPaymentType.QR);
    }

    @Override
    public String getDisabledReason() {
        if (isPositionsRefund()) {
            return ResBundlePaymentBankQR.getString("QR_PAYMENT_REFUND_FORBIDDEN");
        }
        return null;
    }

    @Override
    public boolean canApplyOnArbitraryRefund() {
        return false;
    }

    @Override
    public void processPayment() {
        try {
            techProcessInterface.addPayment(getModel().getPayment());
        } catch (Exception e) {
            getFactory().showMessage(e.getMessage());
            setCallDone(true);
        }
    }

    @Override
    public void eventPINEntry() {
        //
    }

    @Override
    public void eventOnlineRequest() {
        //
    }

    @Override
    public void eventAuthorizationComplete(AuthorizationData auth) {
        if (auth.isStatus()) {
            getController().updatePayment(auth);
        }
    }

    @Override
    public void eventBankModuleOnline() {
        //
    }

    @Override
    public void eventBankModuleOffline() {
        //
    }

    @Override
    public void eventShowCustomPaymentProcessMessage(String message) {
        getModel().getInfo().setMessageText(message);
        getModel().setState(BankQRPaymentState.SHOW_WAIT);
    }

    @Override
    public void eventDailyLogComplete(BankPlugin plugin) {
        //
    }

    @Override
    public void showCustomProcessScreen(BankDialog dialog) {
        getModel().getInfo().setDialog(dialog);
        getModel().setState(BankQRPaymentState.DIALOG);
    }

    @Override
    public void dispatchCloseEvent(boolean isCloseByKey) {
        super.dispatchCloseEvent(isCloseByKey);
        getFactory().getMainWindow().getCheckContainer().setBankCard(null);
    }

    @Override
    public boolean isMoveCursorEnabled() {
        return getView().isMoveCursorAvailable();
    }

    @Override
    public void preparePrintCheck(Check check, PaymentTransactionEntity paymentTransactionEntity) {
        SlipsContainer sc = getSlipsContainer(check);
        sc.add(new TransactionData(paymentTransactionEntity));
    }

    @Override
    public void preparePrintCheck(Check check, PaymentEntity payment) {
        SlipsContainer sc = getSlipsContainer(check);

        for (PaymentTransactionEntity pte : payment.getTransactions()) {
            sc.add(new TransactionData(pte));
        }
    }

    private SlipsContainer getSlipsContainer(Check check) {
        SlipsContainer sc = check.getCheckSlipsContainer(PAYMENT_TYPE);
        if (sc == null) {
            sc = new SlipsContainer(ResBundlePaymentBankQR.getString("QR_PAYMENT"));
            check.setCheckSlipsContainer(PAYMENT_TYPE, sc);
        }
        return sc;
    }

    @Override
    public void cancelPurchase(PaymentEntity cancelPayment) {
        getBankQRReturnComponent().setController(getController());
        getBankQRReturnComponent().processCancelPayment(cancelPayment);
    }

    private String createBankTitleMessage(String title, String bankId) {
        return title + ", " + bankId;
    }
}
