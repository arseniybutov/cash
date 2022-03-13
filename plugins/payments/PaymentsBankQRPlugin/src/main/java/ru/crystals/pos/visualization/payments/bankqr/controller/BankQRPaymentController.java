package ru.crystals.pos.visualization.payments.bankqr.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankInterruptedException;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.checkdisplay.CustomerMessage;
import ru.crystals.pos.checkdisplay.CustomerMessageContent;
import ru.crystals.pos.checkdisplay.CustomerMessageService;
import ru.crystals.pos.checkdisplay.GenericMessageGroup;
import ru.crystals.pos.checkdisplay.GenericMessageType;
import ru.crystals.pos.checkdisplay.MessageGroup;
import ru.crystals.pos.checkdisplay.MessageType;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.customerdisplay.CustomerDisplay;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.QRSaleDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.payments.BankCardPaymentTransactionEntity;
import ru.crystals.pos.payments.BankQRPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.payments.bankqr.ResBundlePaymentBankQR;
import ru.crystals.pos.visualization.payments.bankqr.integration.BankQRPaymentPluginAdapter;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentModel;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BankQRPaymentController extends AbstractPaymentController<BankQRPaymentPluginAdapter, BankQRPaymentModel> {

    private static final Logger log = LoggerFactory.getLogger(BankQRPaymentController.class);

    private final AtomicBoolean isBankOperationStopped = new AtomicBoolean();
    private final AtomicBoolean isOperationStopAvailable = new AtomicBoolean();

    private boolean exception = false;

    private final CustomerMessageService customerMessageService;
    private CustomerDisplay customerDisplay;
    private static final MessageGroup msgGroup = new GenericMessageGroup("payment");
    private static final MessageType qrMessageType = new GenericMessageType(msgGroup, PaymentsDiscriminators.BANK_QRPAYMENT_ENTITY);
    private final InternalCashPoolExecutor internalCashPoolExecutor;
    private final FiscalPrinter fiscalPrinter;

    @Autowired
    BankQRPaymentController(CustomerMessageService customerMessageService,
                            InternalCashPoolExecutor internalCashPoolExecutor,
                            FiscalPrinter fiscalPrinter) {
        this.customerMessageService = customerMessageService;
        this.internalCashPoolExecutor = internalCashPoolExecutor;
        this.fiscalPrinter = fiscalPrinter;
    }

    @Autowired(required = false)
    void setCustomerDisplay(CustomerDisplay customerDisplay) {
        this.customerDisplay = customerDisplay;
    }

    @Override
    public void processPayment(PaymentEntity payment) {
        getModel().setPayment(payment);
        setState(BankQRPaymentState.PAYMENT);
    }

    @Override
    public void processCancelPayment(PaymentEntity payment) {
        cancelPurchase(payment);
    }

    private void executeBank(Runnable r) {
        internalCashPoolExecutor.execute(r);
    }

    public void processOperation(BigDecimal doubleValue) {
        getModel().getPayment().setSumPay(CurrencyUtil.convertMoney(doubleValue));
        Factory.getTechProcessImpl().getTechProcessEvents().eventAddPaymentStart(Factory.getTechProcessImpl().getCheck(), getModel().getPayment());
        Bank bankModule = getAdapter().getBankModule();
        if (bankModule == null) {
            onFaultActions(ResBundlePaymentBankQR.getString("ERROR_BANK_MODULE_DISCONNECTED"));
            return;
        }
        if (isRefund()) {
            getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_WAITING_FOR_RESPONSE"));
            setState(BankQRPaymentState.SHOW_WAIT);
            executeBank(() -> {
                final BankQRPaymentEntity bcpe = eventAddCashlessPayment();
                processRefund(bankModule, bcpe);
            });
            return;
        }

        if (bankModule.canPayByCustomerQR(getAdapter().getBankId())) {
            setState(BankQRPaymentState.SCAN_CUSTOMER_QR);
            if (isShowQROnCheckDisplay()) {
                checkDisplayShowQR();
            }
            return;
        }

        getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_CUSTOMER_SCANNING_QR"));
        setState(BankQRPaymentState.SHOW_ABORT_WAIT);
        executeBank(() -> {
            eventAddCashlessPayment();
            processSale(bankModule);
        });
    }

    private BankQRPaymentEntity eventAddCashlessPayment() {
        BankQRPaymentEntity bcpe = (BankQRPaymentEntity) getModel().getPayment();
        Factory.getTechProcessImpl().getTechProcessEvents().eventAddCashlessPayment(Factory.getTechProcessImpl().getCheckWithNumber(), bcpe);
        return bcpe;
    }

    public void sendBankOperationResponse(BankDialog dialog, String response) {
        getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_CUSTOMER_SCANNING_QR"));
        setState(BankQRPaymentState.SHOW_WAIT);
        getAdapter().getBankModule().sendDialogResponse(dialog.getDialogType(), response, getAdapter().getBankId());
    }

    /**
     * Выполнение прямой операции оплаты
     */
    private void processSale(Bank bank) {
        SaleData sd = makeSaleData();

        final AtomicReference<String> fpeHolder = new AtomicReference<>();

        BankQRProcessingCallback qrProcessingCallback = new BankQRProcessingCallback() {
            @Override
            public boolean isStopped() {
                return isBankOperationStopped.get();
            }

            @Override
            public void eventShowQRCode(String qrCode,
                                        PictureId paymentSystemLogoId,
                                        Long amount,
                                        LocalDateTime dateBegin,
                                        LocalDateTime dateEnd,
                                        String bankProvider) {
                if (customerDisplay != null && customerDisplay.canShowQRCode()) {
                    customerDisplay.showQRCode(qrCode, bankProvider);
                } else if (isShowQROnCheckDisplay()) {
                    log.debug("Showing QR on check display");
                    checkDisplayScanQR(qrCode, paymentSystemLogoId, amount);
                } else {
                    log.debug("Printing QR on fiscal printer");
                    try {
                        QRSaleDocument qrSaleDocument = new QRSaleDocument(qrCode, sd.getAmount(), dateBegin, dateEnd);
                        qrSaleDocument.setDepart(Factory.getTechProcessImpl().getDepartNumber());
                        qrSaleDocument.setCashier(new Cashier(Factory.getTechProcessImpl().getCurrentUser()));
                        fiscalPrinter.printServiceDocument(qrSaleDocument);
                    } catch (FiscalPrinterException e) {
                        fpeHolder.set(ResBundlePaymentBankQR.getString("FISCAL_PRINTER_QR_ERROR") + ": " + e.getMessage());
                        setBankOperationStopped(true);
                        log.error("{}: {}", ResBundlePaymentBankQR.getString("FISCAL_PRINTER_QR_ERROR"), e.getMessage());
                    }
                }
            }

            @Override
            public void eventHideQRCode() {
                getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_WAITING_FOR_RESPONSE"));
                setState(BankQRPaymentState.SHOW_WAIT);
                onHideQR();
            }
        };

        try {
            setBankOperationStopped(false);
            log.info("sale: amount = {}", sd.getAmount());
            sd.setQRProcessingCallback(qrProcessingCallback);
            completeDirectTransaction(bank.sale(sd));
        } catch (BankInterruptedException be) {
            onFaultActions(fpeHolder.get(), ResBundlePaymentBankQR.getString("RESULT_OPERATION_ABORTED"));
        } catch (BankException be) {
            onFaultActions(fpeHolder.get(), be.getMessage());
        } finally {
            onHideQR();
        }
    }

    private void onFaultActions(String fpeMessage, String bankMessage) {
        if (fpeMessage != null) {
            onFaultActions(fpeMessage);
        } else {
            onFaultActions(bankMessage);
        }
    }

    private void onHideQR() {
        isOperationStopAvailable.set(false);
        customerMessageService.hideAll(qrMessageType);
        if (customerDisplay != null && customerDisplay.canShowQRCode()) {
            customerDisplay.hideQRCode();
        }
        checkDisplayHideAll();
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
                    if (BankQRPaymentEntity.class.getSimpleName().equals(payment.getPaymentType())
                            && payment.isSuccessProcessed() && ((BankQRPaymentEntity) payment).getAmount().equals(amount)) {
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
    private void processRefund(Bank bank, BankQRPaymentEntity bcpe) {
        ReversalData rd = new ReversalData();

        rd.setAmount(getModel().getPayment().getSumPay());
        rd.setOriginalSaleTransactionAmount(bcpe.getAmount());
        rd.setAuthCode(bcpe.getAuthCode());
        rd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        rd.setCashTransId(bcpe.getCashTransId());
        rd.setRefNumber(bcpe.getRefNumber());
        rd.setHostTransId(bcpe.getHostTransId());
        rd.setMerchantId(bcpe.getMerchantId());
        rd.setOriginalSaleTransactionDate(bcpe.getDate());
        rd.setBankId(getAdapter().getBankId());
        rd.setPaymentType(BankPaymentType.QR);
        rd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckByPaidSum(rd.getAmount())));
        rd.setOperationType(BankOperationType.REFUND);
        rd.setExtendedData(bcpe.getPropertiesAsMap());
        try {
            if (rd.getCashTransId() == null) {
                rd.setCashTransId(Factory.getTechProcessImpl().getTransNumber());
            }

            completeDirectTransaction(bank.refund(rd));

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
            getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("PRINTING_CHECK"));
            setState(BankQRPaymentState.SHOW_WAIT);
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
            message = ResBundlePaymentBankQR.getString("RESULT_OPERATION_FAILED");
        }
        Factory.getTechProcessImpl().getTechProcessEvents().eventAddPaymentNotification(getModel().getPayment(), true, message);
        getQRModel().getInfo().setExceptionText(message);
        setState(BankQRPaymentState.ERROR);
    }

    private void processErrorSlips(AuthorizationData ad) {
        if (isNeedToPrintErrorSlip(ad)) {
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
        BankQRPaymentEntity bankCardPayment = (BankQRPaymentEntity) getModel().getPayment();
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheck();

        if (purchase != null) {
            bankCardPayment.setPurchase(purchase);
        }
        bankCardPayment.setBankid(getAdapter().getBankId());
        bankCardPayment.setSumPay(auth.getAmount());
        bankCardPayment.setAuthorizationData(auth);
        bankCardPayment.addTransactionData(auth);
    }

    /**
     * Отмена транзакции оплаты при аннулировании чека (непрямая транзакция)
     */
    private void cancelSaleTransaction(BankQRPaymentEntity bcpe) {
        log.info("Cancelling bank qr payment");

        ReversalData rd = new ReversalData();
        rd.setAmount(bcpe.getAmount());
        rd.setOriginalSaleTransactionAmount(bcpe.getAmount());
        rd.setAuthCode(bcpe.getAuthCode());
        rd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        rd.setCashTransId(bcpe.getCashTransId());
        rd.setRefNumber(bcpe.getRefNumber());
        rd.setHostTransId(bcpe.getHostTransId());
        rd.setMerchantId(bcpe.getMerchantId());
        rd.setOriginalSaleTransactionDate(bcpe.getDate());
        rd.setBankId(bcpe.getBankid());
        rd.setPaymentType(BankPaymentType.QR);
        rd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckByPaidSum(rd.getAmount())));
        rd.setOperationType(BankOperationType.REVERSAL);
        rd.setExtendedData(bcpe.getPropertiesAsMap());
        try {
            rd.setAmount(bcpe.getSumPay());
            rd.setCashTransId(1L);
            completeCancelSale(getAdapter().getBankModule().refund(rd), bcpe);
        } catch (BankException be) {
            log.error("cancelSaleTransaction", be);
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


    private void completeCancelSale(AuthorizationData ad, BankQRPaymentEntity bcpe) {
        if (ad.isStatus()) {
            bcpe.setAuthorizationData(ad);
            for (PaymentTransactionEntity transaction : bcpe.getTransactions()) {
                transaction.setNumShift(Factory.getTechProcessImpl().getShift().getNumShift());
                transaction.setShopIndex(Factory.getTechProcessImpl().getShift().getShopIndex());
            }
            makeAnnulledTransaction(ad, bcpe);
        } else {
            processErrorSlips(ad);
            log.error(ad.getMessage());
            onFaultOnCancelSale();
        }
    }

    /**
     * Создает транзакцию аннулирования. Привязывает ее к оплате и чеку.
     */
    private void makeAnnulledTransaction(AuthorizationData ad, BankQRPaymentEntity bcpe) {
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
            log.error("There is no bank module, operation not complete");
            return;
        }
        if (Factory.getTechProcessImpl().getCheck().isSale()) {
            // Аннулирование чека оплаты
            cancelSaleTransaction((BankQRPaymentEntity) cancelPayment);
        }
    }

    public void closeBankDialog() {
        getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_CUSTOMER_SCANNING_QR"));
        setState(BankQRPaymentState.SHOW_WAIT);
        getAdapter().getBankModule().closeDialog(getAdapter().getBankId());
    }

    public void setBankOperationStopped(boolean isOperationStopped) {
        isBankOperationStopped.set(isOperationStopped);
    }

    public void processSaleByCustomerQR(String customerQR) {
        getQRModel().getInfo().setMessageText(ResBundlePaymentBankQR.getString("STATUS_WAITING_FOR_RESPONSE"));
        setState(BankQRPaymentState.SHOW_WAIT);
        executeBank(() -> {
            eventAddCashlessPayment();

            SaleData sd = makeSaleData();
            sd.setCustomerQR(customerQR);

            try {
                log.info("sale: amount = {}", sd.getAmount());
                completeDirectTransaction(getAdapter().getBankModule().sale(sd));
            } catch (BankException be) {
                onFaultActions(be.getMessage());
            } finally {
                checkDisplayHideAll();
            }
        });
    }

    private SaleData makeSaleData() {
        SaleData sd = new SaleData();
        sd.setAmount(getModel().getPayment().getSumPay());
        sd.setSurchargeAmount(Factory.getTechProcessImpl().getSurchargeValue());
        sd.setCurrencyCode(Factory.getTechProcessImpl().getCurrency().getId());
        sd.setCashTransId(Factory.getTechProcessImpl().getTransNumber());
        sd.setBankId(getAdapter().getBankId());
        sd.setPaymentType(BankPaymentType.QR);
        sd.setFirstFiscalPrinter(Factory.getTechProcessImpl().isFirstFiscalPrinter(getInnFromCheckBySurchargeSum(sd.getAmount())));
        sd.setDiscountCardNumber(Factory.getTechProcessImpl().getCheck().getFirstInternalCardNumber());
        return sd;
    }

    private BankQRPaymentModel getQRModel() {
        return (BankQRPaymentModel) getModel();
    }

    private void setState(BankQRPaymentState state) {
        getQRModel().setState(state);
    }

    private void checkDisplayShowQR() {
        customerMessageService.hideAllAndShow(new CustomerMessage(qrMessageType,
                CustomerMessageContent.newBuilder()
                        .withText(ResBundlePaymentBankQR.getString("SHOW_QR_TO_PAY"))
                        .withPicture(PictureId.ATTENTION)
                        .build(), Duration.ZERO));
    }

    private void checkDisplayScanQR(String qr, PictureId paymentSystemLogoId, long amount) {
        customerMessageService.hideAllAndShow(new CustomerMessage(qrMessageType,
                CustomerMessageContent.newBuilder()
                        .withText(ResBundlePaymentBankQR.getString("SCAN_QR_TO_PAY"))
                        .withSum(CurrencyUtil.formatSum(amount))
                        .withQR(qr)
                        .withPicture(paymentSystemLogoId)
                        .build(), Duration.ZERO));
    }

    public void checkDisplayHideAll() {
        if (isShowQROnCheckDisplay()) {
            customerMessageService.hideAll(msgGroup);
        }
    }

    private boolean isShowQROnCheckDisplay() {
        return customerMessageService.hasConfiguredListeners();
    }
}
