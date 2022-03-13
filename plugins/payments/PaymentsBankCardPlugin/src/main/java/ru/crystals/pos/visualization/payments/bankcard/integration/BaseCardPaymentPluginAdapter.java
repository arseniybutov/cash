package ru.crystals.pos.visualization.payments.bankcard.integration;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SlipsContainer;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TransactionData;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPluginDisabledReason;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentBankCard;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentModel;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentState;
import ru.crystals.pos.visualization.payments.bankcard.view.BankCardPaymentView;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentPluginAdapter;

import javax.swing.JPanel;

/**
 * Created by agaydenger on 17.11.16.
 */
public abstract class BaseCardPaymentPluginAdapter extends AbstractPaymentPluginAdapter implements PaymentPluginDisabledReason, BankEvent {
    private Bank bankModule;
    private final BankCardPaymentModel model;
    private final BankCardPaymentView view;
    private final BankCardPaymentController controller;

    protected BaseCardPaymentPluginAdapter(BankCardPaymentController controller) {
        this.controller = controller;
        model = new BankCardPaymentModel();
        view = new BankCardPaymentView();

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

    @Override
    protected BankCardPaymentController getController() {
        return controller;
    }

    @Override
    protected BankCardPaymentModel getModel() {
        return model;
    }

    @Override
    protected BankCardPaymentView getView() {
        return view;
    }

    @Override
    public String getTitlePaymentType() {
        return getTitlePaymentType(getPayment(), false);
    }

    @Override
    public void setBankId(String bankId) {
        if (StringUtils.isNotBlank(bankId)) {
            super.setBankId(bankId);
        }
    }

    @Override
    public String getReturnPaymentString() {
        return getTitlePaymentType(getPayment(), true);
    }

    public Bank getBankModule() {
        return bankModule;
    }

    private String getTitlePaymentType(PaymentEntity payment, boolean isRefund) {
        String title;
        if (isRefund || isRefund()) {
            title = getResourceRefundString();
            if (payment instanceof BankCardPaymentEntity) {
                String cardNumber = ((BankCardPaymentEntity) payment).getCardNumber();
                cardNumber = cardNumber == null ? "" : cardNumber;

                if (cardNumber.length() > 4) {
                    cardNumber = "..." + cardNumber.substring(cardNumber.length() - 4);
                }
                return title + ' ' + cardNumber + getBankNameTitle(((BankCardPaymentEntity) payment).getBankid());
            }
        } else if (isPositionsRefund()) {
            title = getResourceRefundString();
        } else {
            title = isPositionsRefund() ? getResourceRefundString() : getResourcePaymentString();
        }
        return title + getBankNameTitle(getBankId());
    }

    protected abstract String getResourcePaymentString();

    public abstract String getBankNameTitle(String bankid);

    protected abstract String getResourceRefundString();

    @Override
    public void processPayment() {
        try {
            Factory.getTechProcessImpl().addPayment(getModel().getPayment());
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
        getModel().setState(BankCardPaymentState.SHOW_WAIT);
    }

    @Override
    public void eventDailyLogComplete(BankPlugin plugin) {
        //
    }

    @Override
    public void showCustomProcessScreen(BankDialog dialog) {
        getModel().getInfo().setDialog(dialog);
        getModel().setState(BankCardPaymentState.DIALOG);
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
        SlipsContainer sc = check.getCheckSlipsContainer(PaymentsDiscriminators.BANK_CARD_PAYMENT_ENTITY);
        if (sc == null) {
            sc = new SlipsContainer(ResBundlePaymentBankCard.getString("BANK_PAYMENT_LABEL"));
            check.setCheckSlipsContainer(PaymentsDiscriminators.BANK_CARD_PAYMENT_ENTITY, sc);
        }
        return sc;
    }

    public boolean needCashOut(PurchaseEntity check) {
        return CheckUtils.isCashOutReceipt(check);
    }

    public long getCashOutAmount(PurchaseEntity check) {
        long result = 0;
        if (needCashOut(check)) {
            result =  check.getDividedPurchases().stream()
                    .filter(p -> p.isExpense() && p.getPayments().isEmpty())
                    .findFirst()
                    .map(PurchaseEntity::getCheckSumEnd)
                    .orElse(0L);
        }
        return result;
    }
}
