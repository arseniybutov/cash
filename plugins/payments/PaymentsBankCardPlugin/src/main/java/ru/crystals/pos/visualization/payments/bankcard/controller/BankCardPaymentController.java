package ru.crystals.pos.visualization.payments.bankcard.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashException;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.payments.BankCardPaymentTransactionEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPropertyEntity;
import ru.crystals.pos.payments.PaymentPropertyNameEntity;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.payments.PaymentsUtils;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentBankCard;
import ru.crystals.pos.visualization.payments.bankcard.integration.BaseCardPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentInfo;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentModel;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by agaydenger on 17.11.16.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BankCardPaymentController extends AbstractPaymentController<BaseCardPaymentPluginAdapter, BankCardPaymentModel> {

    private static final Logger LOG = LoggerFactory.getLogger(BankCardPaymentController.class);
    private final InternalCashPoolExecutor internalCashPoolExecutor;
    private boolean exception = false;

    @Autowired
    BankCardPaymentController(InternalCashPoolExecutor internalCashPoolExecutor) {
        this.internalCashPoolExecutor = internalCashPoolExecutor;
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        preProcessReturnCashOut();
        getModel().setState(BankCardPaymentState.PAYMENT);
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        cancelPurchase(payment);
    }

    private void preProcessReturnCashOut() {
        if (isRefund()) {
            ((BankCardPaymentInfo) getModel().getInfo()).setReturnCashOutAmount(null);

            PurchaseEntity salePurchase = Factory.getTechProcessImpl().getCheck().getSuperPurchase();

            if (CheckUtils.getRefundReceipt(salePurchase).isEmpty()) {
                PurchaseEntity purchaseWithCashOut = Factory.getInstance().getCheckService().searchExpenseReceiptWithCashOut(salePurchase);

                if (purchaseWithCashOut == null || CheckUtils.getRefundReceipt(purchaseWithCashOut).isEmpty()) {
                    return;
                }

                List<PaymentEntity> returnCashOutPayment = PaymentsUtils.getRefundedCashOutPayments(salePurchase);
                if (returnCashOutPayment.isEmpty()) {
                    ((BankCardPaymentInfo) getModel().getInfo()).setReturnCashOutAmount(purchaseWithCashOut.getCheckSumEnd());
                }
            }
        }
    }

    public void processOperation(BigDecimal doubleValue) {
        getModel().getPayment().setSumPay(CurrencyUtil.convertMoney(doubleValue));

        //проверим что есть выдача наличных и добавить ее в оплаты
        try {
            if (isRefund()) {
                processCashOutReturn(CurrencyUtil.convertMoney(doubleValue));
            } else {
                processCashOut(CurrencyUtil.convertMoney(doubleValue));
            }
        } catch (CashException ex) {
            onFaultActions(ex.getMessage());
            return;
        }

        Factory.getTechProcessImpl().getTechProcessEvents().eventAddPaymentStart(Factory.getTechProcessImpl().getCheck(), getModel().getPayment());

        if (getAdapter().getBankModule() != null) {
            ((BankCardPaymentModel) getModel()).getInfo().setMessageText(ResBundlePaymentBankCard.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
            getModel().setState(BankCardPaymentState.SHOW_WAIT);
            internalCashPoolExecutor.execute(() -> {
                BankCardPaymentEntity bcpe = (BankCardPaymentEntity) getModel().getPayment();
                Factory.getTechProcessImpl().getTechProcessEvents().eventAddCashlessPayment(Factory.getTechProcessImpl().getCheckWithNumber(), bcpe);
                if (isRefund()) {
                    processRefund(getAdapter().getBankModule(), bcpe);
                } else {
                    processSale(getAdapter().getBankModule());
                }
            });
        } else {
            onFaultActions(ResBundlePaymentBankCard.getString("ERROR_BANK_MODULE_DISCONNECTED"));
        }
    }

    private void processCashOut(long amount) throws CashException {
        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
        if (getAdapter().needCashOut(check)) {
            if (getAdapter().getBankModule().isPaymentWithCashOutPossible(getAdapter().getBankId())) {
                long amountCashOut = getAdapter().getCashOutAmount(check);

                if (amountCashOut >= amount) {
                    throw new CashException(ResBundlePaymentBankCard.getString("INVALID_ENTER_AMOUNT"));
                }

                if (amountCashOut > 0) {
                    PaymentPropertyEntity amountCashOutProperty = new PaymentPropertyEntity(getModel().getPayment(),
                            new PaymentPropertyNameEntity(BankPlugin.CASHOUT_AMOUNT), String.valueOf(amountCashOut));
                    getModel().getPayment().getProperties().add(amountCashOutProperty);
                }
            } else {
                throw new CashException(ResBundlePaymentBankCard.getString("CASH_OUT_UNAVAILABLE"));
            }
        }
    }

    private void processCashOutReturn(long amount) throws CashException {
        BankCardPaymentInfo info = (BankCardPaymentInfo) getModel().getInfo();
        if (info.getReturnCashOutAmount() != null) {
            if (info.getReturnCashOutAmount() >= amount) {
                throw new CashException(ResBundlePaymentBankCard.getString("INVALID_ENTER_AMOUNT"));
            }
            PaymentPropertyEntity returnAmountCashOutProperty = new PaymentPropertyEntity(getModel().getPayment(),
                    new PaymentPropertyNameEntity(BankPlugin.RETURN_CASHOUT_AMOUNT), String.valueOf(info.getReturnCashOutAmount()));
            getModel().getPayment().getProperties().add(returnAmountCashOutProperty);
        }
    }

    public void sendBankOperationResponse(BankDialog dialog, String response) {
        ((BankCardPaymentModel) getModel()).getInfo().setMessageText(ResBundlePaymentBankCard.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
        getModel().setState(BankCardPaymentState.SHOW_WAIT);
        getAdapter().getBankModule().sendDialogResponse(dialog.getDialogType(), response, getAdapter().getBankId());
    }


    /**
     * Выполнение прямой операции оплаты
     */
    private void processSale(Bank bank) {
        SaleData sd = new SaleData();
        sd.setAmount(getModel().getPayment().getSumPay());
        String amountCashOut = getModel().getPayment().getPluginPropertiesMap().get(BankPlugin.CASHOUT_AMOUNT);
        sd.setAmountCashOut(amountCashOut != null ? Long.valueOf(amountCashOut) : null);
        sd.setSurchargeAmount(Factory.getTechProcessImpl().getSurchargeValue());
        sd.setSuspensionCallback(Factory.getTechProcessImpl());
        sd.setCard(Factory.getInstance().getMainWindow().getCheckContainer().getBankCard());
        sd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        sd.setCashTransId(Factory.getTechProcessImpl().getTransNumber());
        sd.setBankId(getAdapter().getBankId());
        sd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckBySurchargeSum(sd.getAmount())));
        sd.setDiscountCardNumber(Factory.getTechProcessImpl().getCheck().getFirstInternalCardNumber());

        try {
            LOG.info("sale: amount = {}", sd.getAmount());
            completeDirectTransaction(bank.sale(sd));
        } catch (BankException be) {
            onFaultActions(be.getMessage());
        }
    }

    private String getInnFromCheckBySurchargeSum(Long amount) {
        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
        if (check.isPurchaseDivided()) {
            for (PurchaseEntity dividedPurchase : check.getDividedPurchases()) {
                Long subCheckSurcharge = Factory.getTechProcessImpl().getSurchargeValue(dividedPurchase);
                if (subCheckSurcharge.compareTo(0L) > 0 && subCheckSurcharge.compareTo(amount) >= 0) {
                    return dividedPurchase.getInn();
                }
            }
        }
        return check.getInn();
    }

    private String getInnFromCheckByPaidSum(Long amount) {
        PurchaseEntity check = Factory.getTechProcessImpl().getCheck();
        if (check.isPurchaseDivided()) {
            for (PurchaseEntity dividedPurchase : check.getDividedPurchases()) {
                for (PaymentEntity payment : dividedPurchase.getPayments()) {
                    if (BankCardPaymentEntity.class.getSimpleName().equals(payment.getPaymentType())
                            && payment.isSuccessProcessed() && ((BankCardPaymentEntity) payment).getAmount().equals(amount)) {
                        return dividedPurchase.getInn();
                    }
                }
            }
        }
        return check.getInn();
    }

    /**
     * Выполнение прямой операции возврата
     */
    private void processRefund(Bank bank, BankCardPaymentEntity bcpe) {
        ReversalData rd = new ReversalData();

        BankCard bc = new BankCard();
        bc.setCardNumber(StringUtils.trimToNull(bcpe.getCardNumber()));
        bc.setCardNumberHash(StringUtils.trimToNull(bcpe.getCardHash()));
        rd.setCard(bc);

        String amountCashOut = getModel().getPayment().getPluginPropertiesMap().get(BankPlugin.RETURN_CASHOUT_AMOUNT);
        rd.setAmountCashOut(amountCashOut != null ? Long.valueOf(amountCashOut) : null);
        rd.setAmount(getModel().getPayment().getSumPay());
        rd.setOriginalSaleTransactionAmount(bcpe.getAmount());
        rd.setAuthCode(bcpe.getAuthCode());
        rd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        rd.setCashTransId(bcpe.getCashTransId());
        rd.setRefNumber(bcpe.getRefNumber());
        rd.setTerminalId(bcpe.getTerminalId());
        rd.setHostTransId(bcpe.getHostTransId());
        rd.setMerchantId(bcpe.getMerchantId());
        rd.setOriginalSaleTransactionDate(bcpe.getDate());
        rd.setBankId(getAdapter().getBankId());
        rd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckByPaidSum(rd.getAmount())));
        rd.setOperationType(BankOperationType.REFUND);
        rd.setExtendedData(bcpe.getPropertiesAsMap());
        try {
            if (rd.getCashTransId() == null) {
                rd.setCashTransId(Factory.getTechProcessImpl().getTransNumber());
            }

            // Принимаем решение: делать "отмену оплаты" (приоритетнее) или "возврат оплаты":
            //  Если операцию проводим на той же кассе и в той же смене, когда делали покупку - попробуем сделать "отмену оплаты"
            //  (может превратиться в "возврат оплаты" - если делаем "частичный возврат",
            //      а банковский процессинг не допускает "частичной отмены оплаты" - см. ru.crystals.pos.bank.BankImpl.reversal(ReversalData))
            PurchaseEntity superPurchase = Factory.getTechProcessImpl().getCheck().getSuperPurchase();
            if (superPurchase != null && Factory.getTechProcessImpl().getShift().equals(superPurchase.getShift())) {
                LOG.info("reversal (current shift)");
                completeDirectTransaction(bank.reversal(rd));
            } else {
                //  а вот если возвращаем не в той смене (другой день, либо другая касса),
                //      либо делаем произвольный возврат (оригинального чека нет: superPurchase == null), то точно делаем "возврат оплаты" - без вариантов:
                LOG.info("refund ({})", superPurchase == null ? "arbitrary/position return" : "non current shift");
                completeDirectTransaction(bank.refund(rd));
            }

        } catch (BankException be) {
            onFaultActions(be.getMessage());
        }
    }

    /**
     * Обработка ответа по прямой операции
     */
    private void completeDirectTransaction(final AuthorizationData ad) {
        if (ad.isStatus()) {
            Factory.getTechProcessImpl().getTechProcessEvents().eventBankCardPaymentSuccess(ad);
            ((BankCardPaymentModel) getModel()).getInfo().setMessageText(ResBundlePaymentBankCard.getString("PRINTING_CHECK"));
            getModel().setState(BankCardPaymentState.SHOW_WAIT);
            updatePayment(ad);
            getAdapter().processPayment();
        } else {
            Factory.getTechProcessImpl().getTechProcessEvents().eventBankCardPaymentFailed(ad);
            processErrorSlips(ad);
            onFaultActions(ad.getMessage());
        }
    }

    /**
     * Действия, выполняемые при исключительной ситуации при прямых операциях
     */
    private void onFaultActions(String message) {
        // эта штука дальше используется только для дисплея покупателя, причем параметр isInterrupted игнорируется -
        // получается, что при ошибке на ДП выводится сообщение о добавлении оплаты
        if (StringUtils.isEmpty(message)) {
            message = ResBundlePaymentBankCard.getString("OPERATION_FAILED");
        }
        Factory.getTechProcessImpl().getTechProcessEvents().eventAddPaymentNotification(getModel().getPayment(), true, message);
        ((BankCardPaymentModel) getModel()).getInfo().setExceptionText(message);
        getModel().setState(BankCardPaymentState.ERROR);
    }

    private void processErrorSlips(AuthorizationData ad) {
        processErrorSlips(ad, null);
    }

    private void processErrorSlips(AuthorizationData ad, String faultMessage) {
        if (isNeedToPrintErrorSlip(ad)) {
            if (faultMessage != null) {
                // Добавление дополнительного сообщения к слипу (например, о том, что из-за  отказа не была выполнена отмена при аннулировании чека
                ad.getSlips().get(0).add(0, faultMessage);
            }
            printSlips(ad.getSlips());
        }
    }

    private boolean isNeedToPrintErrorSlip(AuthorizationData ad) {
        return !Factory.getInstance().getProperties().getPrintCashlessSlipInCheck() && ad != null &&
                !ad.isStatus() && ad.isPrintNegativeSlip() && ad.getSlips() != null && !ad.getSlips().isEmpty();
    }

    private void printSlips(List<List<String>> slips) {
        Factory.getTechProcessImpl().printSlips(slips, null);
    }

    public void updatePayment(AuthorizationData auth) {

        BankCardPaymentEntity bankCardPayment = (BankCardPaymentEntity) getModel().getPayment();
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();

        if (purchase != null) {
            bankCardPayment.setPurchase(purchase);
        }
        bankCardPayment.setBankid(getAdapter().getBankId());
        // Если был возврат выдачи, то в оплату он не должен попасть.
        // Сумма возврата выдачи была добавлена в оплату при возврате чека расхода с выдачей
        bankCardPayment.setSumPay(auth.getAmount() - PaymentsUtils.getReturnCashOutAmount(bankCardPayment));
        bankCardPayment.setAuthorizationData(auth);
        bankCardPayment.addTransactionData(auth);
    }

    /**
     * Отмена транзакции оплаты при аннулировании чека (непрямая транзакция)
     */
    private void cancelSaleTransaction(BankCardPaymentEntity bcpe) {
        LOG.info("Cancelling bank card payment");

        // Аннулирование чека оплаты из текущей смены (отмена транзакции)
        BankCard bc = new BankCard();
        bc.setCardNumber(bcpe.getCardNumber());
        bc.setCardNumberHash(bcpe.getCardHash());
        bc.setCardType(bcpe.getCardType());
        bc.setCardOperator(bcpe.getBankType());

        ReversalData rd = new ReversalData();
        rd.setCard(bc);
        rd.setAmount(bcpe.getAmount());
        rd.setOriginalSaleTransactionAmount(bcpe.getAmount());
        rd.setAuthCode(bcpe.getAuthCode());
        rd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        rd.setCashTransId(bcpe.getCashTransId());
        rd.setRefNumber(bcpe.getRefNumber());
        rd.setTerminalId(bcpe.getTerminalId());
        rd.setHostTransId(bcpe.getHostTransId());
        rd.setMerchantId(bcpe.getMerchantId());
        rd.setOriginalSaleTransactionDate(bcpe.getDate());
        rd.setBankId(bcpe.getBankid());
        rd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckByPaidSum(rd.getAmount())));
        rd.setOperationType(BankOperationType.REVERSAL);
        rd.setExtendedData(bcpe.getPropertiesAsMap());
        try {
            if (bcpe.getPurchase().getShift() == null) {
                LOG.info("Check is in current shift - Reversal will be processed");
                completeCancelSale(getAdapter().getBankModule().reversal(rd), bcpe);
            } else {
                // Аннулирование чека оплаты из другой смены (возврат)
                LOG.info("Check is not in current shift - Refund will be processed");
                rd.setAmount(bcpe.getSumPay());
                rd.setCashTransId(1L);

                completeCancelSale(getAdapter().getBankModule().refund(rd), bcpe);
            }
        } catch (BankException be) {
            LOG.error("cancelSaleTransaction", be);
            onFaultOnCancelSale();
        }
    }

    public void notExceptionalComplete() {
        exception = false;
    }

    public boolean isExceptionalComplete() {
        return exception;
    }

    private void onFaultOnCancelSale() {
        exception = true;
    }

    private void completeCancelSale(AuthorizationData ad, BankCardPaymentEntity bcpe) {
        if (ad.isStatus()) {
            bcpe.setAuthorizationData(ad);
            for (PaymentTransactionEntity transaction : bcpe.getTransactions()) {
                transaction.setNumShift(Factory.getTechProcessImpl().getShift().getNumShift());
                transaction.setShopIndex(Factory.getTechProcessImpl().getShift().getShopIndex());
            }
            makeAnnulledTransaction(ad, bcpe);
            notExceptionalComplete();
        } else {
            processErrorSlips(ad);
            LOG.error(ad.getMessage());
            onFaultOnCancelSale();
        }
    }

    /**
     * Создает транзакцию аннулирования. Привязывает ее к оплате и чеку.
     */
    private void makeAnnulledTransaction(AuthorizationData ad, BankCardPaymentEntity bcpe) {
        BankCardPaymentTransactionEntity cancelTransaction = new BankCardPaymentTransactionEntity(bcpe, ad);
        cancelTransaction.setDiscriminator(bcpe.getPaymentType());
        PurchaseEntity purchase = bcpe.getPurchase();
        cancelTransaction.setPurchase(purchase);
        cancelTransaction.setAnnulling(true);
        cancelTransaction.setNumShift(Factory.getTechProcessImpl().getShift().getNumShift());
        cancelTransaction.setShopIndex(Factory.getTechProcessImpl().getShift().getShopIndex());
        cancelTransaction.setCashNum(Factory.getTechProcessImpl().getShift().getCashNum());
        purchase.getTransactions().add(cancelTransaction);
        bcpe.getTransactions().add(cancelTransaction);
    }

    private void cancelPurchase(PaymentEntity cancelPayment) {
        if (getAdapter().getBankModule() == null) {
            LOG.error("There is no bank module, operation not complete");
            return;
        }
        if (Factory.getTechProcessImpl().getCheck().isSale()) {
            // Аннулирование чека оплаты
            cancelSaleTransaction((BankCardPaymentEntity) cancelPayment);
        }
    }

    public void closeBankDialog() {
        ((BankCardPaymentModel) getModel()).getInfo().setMessageText(ResBundlePaymentBankCard.getString("FOLLOW_INSTRUCTIONS_ON_TERMINAL"));
        getModel().setState(BankCardPaymentState.SHOW_WAIT);
        getAdapter().getBankModule().closeDialog(getAdapter().getBankId());
    }

}
