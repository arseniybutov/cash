package ru.crystals.pos.visualization.payments.extpayment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.crystals.api.adapters.ExtPaymentController;
import ru.crystals.api.loader.payments.PluginDescription;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.api.plugin.PaymentPlugin;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.PurchaseExtDataKey;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SlipsContainer;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TransactionData;
import ru.crystals.pos.listeners.XBarcodeListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XKeyListenerInt;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.listeners.XMSRListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPropertyEntity;
import ru.crystals.pos.payments.PaymentPropertyNameEntity;
import ru.crystals.pos.payments.PaymentTransactionEntity;
import ru.crystals.pos.payments.service.SetAPIPaymentPluginInitMark;
import ru.crystals.pos.spi.plugin.payment.InvalidPaymentException;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.ui.forms.UIFormsAdapter;
import ru.crystals.pos.spi.ui.forms.XOperationType;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.components.Container;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentOperationListener;

import javax.swing.JPanel;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Плагин-Адаптер для внешних оплат.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ExtPaymentContainer extends PaymentContainer implements PaymentCallback, XListener, SetAPIPaymentPluginInitMark {

    private static final Logger LOG = LoggerFactory.getLogger("API");

    private final ExtPaymentComponent guiPanel = new ExtPaymentComponent();
    private ExtPaymentController extPaymentController;
    private PluginDescription paymentPluginDescription;

    private PaymentPlugin extPaymentPlugin;

    private CheckService checkService;
    private TechProcessInterface tp;
    private InternalCashPoolExecutor threadPool;

    private XOperationType operationType;
    private ExtPaymentRequest extPaymentRequest;
    private ExtRefundRequest extRefundRequest;
    private ExtCancelRequest extCancelRequest;

    private final UIFormsAdapter uiAdapter = new UIFormsAdapter(guiPanel);

    private Container previousVisualContainer = null;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public ExtPaymentContainer() {
        // Должен иметь конструктор без аргументов.
    }

    @Autowired
    void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }

    @Autowired
    void setTp(TechProcessInterface tp) {
        this.tp = tp;
    }

    @Autowired
    void setThreadPool(InternalCashPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void setPaymentId(String paymentId) {
        super.setPaymentId(paymentId);
        if (isStarted.compareAndSet(false, true)) {
            startContainer();
        }
    }

    private void startContainer() {
        this.extPaymentController = (ExtPaymentController) tp.getPaymentType(this.paymentId);
        this.paymentPluginDescription = this.extPaymentController.getExternalPluginDescription();
        this.extPaymentPlugin = (PaymentPlugin) this.paymentPluginDescription.getPlugin();
        this.extPaymentRequest = new ExtPaymentRequest(tp, this);
        this.extRefundRequest = new ExtRefundRequest(tp, this);
        this.extCancelRequest = new ExtCancelRequest(tp, this);
        this.paymentPluginDescription.setUiForms(uiAdapter);
        // Так и должно быть, необходим для ловли событий ввода.
        XListenerAdapter adapter = new XListenerAdapter(guiPanel, this); // NOSONAR
    }

    @Override
    public void setPayment(PaymentEntity payment, PurchaseEntity purchase) {
        super.setPayment(payment, purchase);
        if (isRefund() || isPositionsRefund()) {
            operationType = XOperationType.RETURN;
        } else {
            operationType = XOperationType.SALE;
        }
        uiAdapter.setxOperationType(operationType);

        try {
            if (operationType == XOperationType.SALE) {
                extPaymentRequest.getReceipt();
                uiAdapter.setMaxSum(extPaymentRequest.getReceipt().getSurchargeSum());
                // start plugin IoC :
                LOG.info("{}.doPayment receipt.getSurchargeSum()={} ", extPaymentPlugin.getClass().getName(), extPaymentRequest.getReceipt().getSurchargeSum());
                // Сейчас мы находимся в Event Dispatching Thread (EDT) свинга.
                // Поэтому важно вызвать метод плагина именно в отдельном треде, потому что
                // если плагин попытается отобразить UI, затем приступит к выполнению длительной операции,
                // UI будет заблокирован и может не показаться. Так будет потому что тред встанет на выполнении операции
                // и не будет качать очередь событий.
                threadPool.execute(() -> extPaymentPlugin.doPayment(extPaymentRequest));
            } else if (operationType == XOperationType.RETURN) {

                BigDecimal sumToRefund;
                // В случае возврата purchase - это чек болванка содержащий информацию о том что еще можно вернуть
                if (purchase != null) {
                    extRefundRequest.setOriginalPurchase(purchase.getSet5OriginalCheck() != null
                            ? purchase.getSet5OriginalCheck() : purchase.getSuperPurchase());
                    sumToRefund = calculateSumToRefund(paymentEntity);
                } else {
                    sumToRefund = CurrencyUtil.convertMoney(tp.getSurchargeValue());
                }
                extRefundRequest.setPaymentEntity(paymentEntity);
                extRefundRequest.setSumToRefund(sumToRefund);
                uiAdapter.setMaxSum(sumToRefund);
                // start plugin IoC :
                LOG.info("{}.doRefund receipt.getSumToRefund()={} ", extPaymentPlugin.getClass().getName(), extRefundRequest.getSumToRefund());
                threadPool.execute(() -> extPaymentPlugin.doRefund(extRefundRequest));
            }
        } catch (Exception e) {
            LOG.error(operationType + " purchase fail", e);
            paymentNotCompleted();
        }
    }

    private BigDecimal calculateSumToRefund(PaymentEntity payment) {
        Long refundSum = tp.getPaymentRefundAvailable(payment);
        Long surch;
        if (getCurrency() != null && !getCurrency().equals(tp.getCurrency())) {
            surch = Factory.getTechProcessImpl().getSurchargeValue(getCurrency());
        } else {
            surch = Factory.getTechProcessImpl().getSurchargeValue(Factory.getTechProcessImpl().getCurrency());
        }
        if (surch > getSurcharge()) {
            surch = getSurcharge();
        }
        if (refundSum > surch) {
            refundSum = surch;
        }
        return CurrencyUtil.convertMoney(refundSum);
    }

    @Override
    public void cancelPurchase(PaymentEntity cancelPayment) {
        previousVisualContainer = Factory.getInstance().getMainWindow().getCheckContainer().getCurrentContainer();
        Factory.getInstance().getMainWindow().getCheckContainer().setCurrentContainer(this);
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setVisible(true);
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setCancelPurchase();
        operationType = XOperationType.CANCEL;
        try {
            extCancelRequest.setPaymentEntity(cancelPayment);
            LOG.info("{}.doPaymentCancel sum={}", extPaymentPlugin.getClass().getName(), extCancelRequest.getPayment().getSum());
            threadPool.execute(() -> extPaymentPlugin.doPaymentCancel(extCancelRequest));
        } catch (Exception e) {
            LOG.error("{}.doPaymentCancel ", extPaymentPlugin.getClass().getName(), e);
            paymentNotCompleted();
        } finally {
            Factory.getInstance().getMainWindow().getCheckContainer().setCurrentContainer(previousVisualContainer);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setVisible(false);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().resetCheckMode();
            previousVisualContainer = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param paymentToCancel оплата, которую требуется аннулировать.
     * @param callback        каллбек, который будет дернут после отработки метода
     * @implNote данная реализация метода никогда не вызывает {@link PaymentOperationListener#onFailed(PaymentEntity)},
     * ошибка и отмена оба приводят к вызову {@link PaymentOperationListener#onCancelled(PaymentEntity)}.
     */
    @Override
    public void cancelPurchaseAsync(PaymentEntity paymentToCancel, PaymentOperationListener callback) {
        this.paymentEntity = paymentToCancel;
        extCancelRequest.setPaymentEntity(paymentToCancel);
        previousVisualContainer = Factory.getInstance().getMainWindow().getCheckContainer().getCurrentContainer();
        Factory.getInstance().getMainWindow().getCheckContainer().setCurrentContainer(this);
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setVisible(true);
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setCancelPurchase();
        operationType = XOperationType.CANCEL;
        try {
            ExtCancelRequestAsyncProxy paymentCancellationRequestProxy = new ExtCancelRequestAsyncProxy(tp, this, callback);
            paymentCancellationRequestProxy.setPaymentEntity(paymentToCancel);
            LOG.info("{}.doPaymentCancel sum={}", extPaymentPlugin.getClass().getName(), extCancelRequest.getPayment().getSum());
            extPaymentPlugin.doPaymentCancel(paymentCancellationRequestProxy);
        } catch (Exception e) {
            LOG.error(operationType + " purchase fail", e);
            paymentNotCompleted();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return эта реализация контейнера всегда возвращает true.
     */
    @Override
    public boolean hasCancellationGui() {
        return true;
    }

    @Override
    public void paymentCompleted(Payment payment) {
        LOG.info("paymentCompleted '{}' operation : {}", operationType, payment);
        Factory.getTechProcessImpl().saveCheckExtendedAttributes(payment.getExtendedReceiptAttributes());
        switch (operationType) {
            case SALE:
                paymentSaleCompleted(payment);
                break;
            case RETURN:
                paymentReturnCompleted(payment);
                break;
            case CANCEL:
                paymentCancelCompleted(payment);
                break;
            default:
                LOG.error("Unknown receipt operation \"{}\" received", operationType);
                break;
        }
    }

    public void paymentCancelCompleted(Payment payment) {
        if (previousVisualContainer != null) {
            Factory.getInstance().getMainWindow().getCheckContainer().setCurrentContainer(previousVisualContainer);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setVisible(false);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().resetCheckMode();
            previousVisualContainer = null;
        }

        PaymentTransactionEntity paymentTransaction = new PaymentTransactionEntity(paymentEntity, true);
        paymentEntity.getTransactions().add(paymentTransaction);
        addSlipsToTransaction(paymentTransaction, payment);
        updatePaymentProperties(paymentEntity, payment);

        try {
            reset();
            Factory.getTechProcessImpl().addPaymentTransaction(paymentTransaction);
        } catch (Exception e) {
            LOG.error("Error on addPaymentTransaction: ", e);
            Factory.getInstance().showMessage(e.getMessage());
            setCallDone(true);
        }
    }

    public void paymentReturnCompleted(Payment payment) {
        paymentCompletedInner(payment);
    }

    public void paymentSaleCompleted(Payment payment) {
        paymentCompletedInner(payment);
    }

    private void addSlipsToTransaction(PaymentTransactionEntity target, Payment payment) {
        // getSlip внутри возвращает первый слип из списка, поэтому нельзя комбинировать setSlip и getSlips().add(...)
        // В конце-концоы, getSlip и setSlip депрекейтнуты, прекратите использовать их!
        for (String slip : payment.getSlips()) {
            target.addSlip(slip);
        }
    }

    private void updatePaymentProperties(PaymentEntity paymentEntity, Payment payment) {
        for (PaymentPropertyEntity property : paymentEntity.getProperties()) {
            String newValue = payment.getData(property.getKey().getName());
            if (newValue != null) {
                property.setValue(newValue);
            }
        }
    }

    private void paymentCompletedInner(Payment payment) {
        PaymentEntity newPayment = new PaymentEntity();
        newPayment.setSumPay(CurrencyUtil.convertMoney(payment.getSum()));
        newPayment.setPaymentType(paymentId);
        newPayment.setPaymentSettings(extPaymentController);
        newPayment.setDateCreate(new Date());

        PaymentTransactionEntity paymentTransaction = new PaymentTransactionEntity(newPayment, false);
        newPayment.getTransactions().add(paymentTransaction);

        addSlipsToTransaction(paymentTransaction, payment);

        if (operationType == XOperationType.RETURN) {
            newPayment.setOriginalPaymentNumber(getPayment().getNumber());
        }

        for (Entry<String, String> p : payment.getData().entrySet()) {
            PaymentPropertyNameEntity paymentPropertyName = checkService.getPropertyEntityByName(p.getKey());
            newPayment.getProperties().add(new PaymentPropertyEntity(newPayment, paymentPropertyName, p.getValue()));
        }

        if (newPayment.getSumPay() > 0 && CurrencyUtil.checkPaymentRatio(newPayment.getSumPay())) {
            try {
                reset();

                // CORE-248: для не-нальной оплаты событие в призму надо отправлять в явном виде:
                Factory.getTechProcessImpl().getTechProcessEvents().eventAddCashlessPayment(Factory.getTechProcessImpl().getCheckWithNumber(), newPayment);

                Factory.getTechProcessImpl().addPayment(newPayment);
            } catch (Exception e) {
                LOG.error("Error on addPayment: ", e);
                Factory.getInstance().showMessage(e.getMessage());
                setCallDone(true);
            }
        } else {
            Factory.getTechProcessImpl().error("Cannot process payment due to failed restrictions check");
        }
    }

    @Override
    public void paymentNotCompleted(Payment payment) {
        LOG.info("paymentNotCompleted '{}' operation : {}", operationType, payment);
        Factory.getTechProcessImpl().saveCheckExtendedAttributes(payment.getExtendedReceiptAttributes());
        if (operationType == XOperationType.RETURN && payment.isRefundPaymentAsCash()) {
            PurchaseEntity purchase = tp.getCheck();
            PurchaseEntity superPurchase = purchase.getSet5OriginalCheck() != null ? purchase.getSet5OriginalCheck() : purchase.getSuperPurchase();
            superPurchase.addExtData(PurchaseExtDataKey.REFUND_EXTERNAL_PAYMENT_AS_CASH.toString(), Boolean.TRUE.toString());
            Factory.getTechProcessImpl().updatePurchase(superPurchase);
        }
        if (previousVisualContainer != null) {
            Factory.getInstance().getMainWindow().getCheckContainer().setCurrentContainer(previousVisualContainer);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().setVisible(false);
            Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().getVisualPanel().resetCheckMode();
            previousVisualContainer = null;
        }
        dispatchCloseEvent(false);
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return guiPanel;
    }

    @Override
    public JPanel getVisualPanel() {
        return guiPanel;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (guiPanel.getCurrentComponent() instanceof XListener) {
            return ((XListener) guiPanel.getCurrentComponent()).keyPressedNew(e);
        }
        if (guiPanel.getCurrentComponent() instanceof XKeyListenerInt) {
            return ((XKeyListenerInt) guiPanel.getCurrentComponent()).keyPressedNew(e);
        }
        return false;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (guiPanel.getCurrentComponent() instanceof XListener) {
            return ((XListener) guiPanel.getCurrentComponent()).barcodeScanned(barcode);
        }
        if (guiPanel.getCurrentComponent() instanceof XBarcodeListener) {
            return ((XBarcodeListener) guiPanel.getCurrentComponent()).barcodeScanned(barcode);
        }
        return false;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (guiPanel.getCurrentComponent() instanceof XListener) {
            return ((XListener) guiPanel.getCurrentComponent()).eventMSR(track1, track2, track3, track4);
        }
        if (guiPanel.getCurrentComponent() instanceof XMSRListener) {
            return ((XMSRListener) guiPanel.getCurrentComponent()).eventMSR(track1, track2, track3, track4);
        }
        return false;
    }

    private String getPaymentTitle() {
        return paymentPluginDescription.getResourceBundle().getString(paymentPluginDescription.getExtPlugin().getLocaleKey());
    }

    @Override
    public boolean isActivated() {
        return extPaymentPlugin.isAvailable();
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return false;
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
        SlipsContainer sc = check.getCheckSlipsContainer(paymentPluginDescription.getTypeName());
        if (sc == null) {
            sc = new SlipsContainer(getPaymentTitle());
            check.setCheckSlipsContainer(paymentPluginDescription.getTypeName(), sc);
        }
        return sc;
    }

    @Override
    public String getPaymentString() {
        return getPaymentTitle();
    }

    @Override
    public boolean isChangeAvailable() {
        log("isChangeAvailable ");
        return false;
    }

    @Override
    public void setPaymentFields() {
        //
    }

    @Override
    public String getChargeName() {
        return getPaymentTitle();
    }

    @Override
    public String getPaymentType() {
        return getPaymentTitle();
    }

    @Override
    public String getTitlePaymentType() {
        return getPaymentTitle();
    }

    @Override
    public String getReturnPaymentString() {
        return getPaymentTitle();
    }

    @Override
    public String getPaymentTypeName() {
        return getPaymentTitle();
    }

    @Override
    public boolean isVisualPanelCreated() {
        return true;
    }

    @Override
    public void number(Byte num) {
        //
    }

    @Override
    public void enter() {
        //
    }

    @Override
    public void reset() {
        //
    }

    @Override
    public long getSum() {
        log("getSum ");
        return 0;
    }

    @Override
    public void setSum(long sum) {
        log("setSum " + sum);
    }

    private void log(String text) {
        LOG.info("CONTAINER LOG: {}", text);
    }

    /**
     * Прокся для {@link ExtCancelRequest}, которая при дергании её каллбека умеет сливать данные стороннему слушателю.
     */
    private class ExtCancelRequestAsyncProxy extends ExtCancelRequest {
        private PaymentOperationListener operationListener;

        /**
         * Конструктор класса. Создаёт новый экземпляр класса {@link ExtCancelRequestAsyncProxy}.
         *
         * @param tp                техпроцесс
         * @param paymentCallback   настоящий каллбек, который бы был не у проксированного варианта объекта
         * @param operationListener слушатель, которому нужно сливать данные при дергании каллбека.
         */
        public ExtCancelRequestAsyncProxy(TechProcessInterface tp, PaymentCallback paymentCallback, PaymentOperationListener operationListener) {
            super(tp, paymentCallback);
            this.operationListener = operationListener;
        }

        @Override
        public PaymentCallback getPaymentCallback() {

            return new PaymentCallback() {
                @Override
                public void paymentCompleted(Payment payment) throws InvalidPaymentException {
                    paymentCallback.paymentCompleted(payment);
                    operationListener.onSucceeded(paymentEntity);
                }

                @Override
                public void paymentNotCompleted(Payment payment) {
                    paymentCallback.paymentNotCompleted(payment);
                    operationListener.onCancelled(paymentEntity);
                }
            };
        }
    }

    @Override
    public Class getPaymentContainerClass() {
        return ExtPaymentContainer.class;
    }
}
