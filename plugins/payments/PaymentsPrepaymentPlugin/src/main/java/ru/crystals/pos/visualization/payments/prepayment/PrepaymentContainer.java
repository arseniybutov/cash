package ru.crystals.pos.visualization.payments.prepayment;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.PaymentCashPluginComponent;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPlugin;
import ru.crystals.pos.payments.PaymentsDiscriminators;
import ru.crystals.pos.payments.PrepaymentEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.payments.PaymentComponent;
import ru.crystals.pos.visualization.payments.PaymentContainer;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldInterface;
import ru.crystals.utils.UnboxingUtils;

import java.math.BigDecimal;

/**
 * Грязный коннтролер, манипулирует {@link PrepaymentComponent}
 */
@PaymentCashPluginComponent(typeName = PaymentsDiscriminators.PREPAYMENT_ENTITY, mainEntity = PrepaymentEntity.class)
@CashPluginQualifier(PluginType.PAYMENTS)
public class PrepaymentContainer extends PaymentContainer implements PaymentPlugin, DotEventListener {
    private final InternalCashPoolExecutor threadPool;
    private PrepaymentComponent visualPanel;

    @Autowired
    PrepaymentContainer(InternalCashPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void setPaymentId(String paymentId) {
        super.setPaymentId(paymentId);
    }

    @Override
    public void setPaymentFields() {
        //
    }

    @Override
    public String getPaymentType() {
        if (isRefund() || isPositionsRefund()) {
            return ResBundlePrepayment.getString("REFUND_PREPAYMENT");
        } else {
            return ResBundlePrepayment.getString("PREPAYMENT");
        }
    }

    @Override
    public String getChargeName() {
        if (isRefund() || isPositionsRefund()) {
            return ResBundleVisualization.getString("REFUND_DISPLAY");
        } else {
            return ResBundleVisualization.getString("CHECK_AMOUNT");
        }
    }

    @Override
    public String getTitlePaymentType() {
        return getPaymentType();
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String getPaymentString() {
        return ResBundlePrepayment.getString("PREPAYMENT");
    }

    @Override
    public String getReturnPaymentString() {
        return ResBundlePrepayment.getString("REFUND_PREPAYMENT");
    }

    @Override
    public String getPaymentTypeName() {
        return ResBundlePrepayment.getString("PREPAYMENT");
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public PaymentComponent getPaymentComponent() {
        return getVisualPanel();
    }

    @Override
    public PrepaymentComponent getVisualPanel() {
        if (visualPanel == null) {
            visualPanel = new PrepaymentComponent();
        }
        return visualPanel;
    }

    @Override
    public void number(Byte num) {
        if (getVisualPanel().getPaymentInputPanel().isVisible()) {
            getInputField().addChar(num.toString().charAt(0));
        }
    }

    @Override
    public void dot() {
        if (getVisualPanel().getPaymentInputPanel().isVisible()) {
            getInputField().dot();
        }
    }

    @Override
    public void esc() {
        if (getVisualPanel().getPaymentInputPanel().isVisible()) {
            dispatchCloseEvent(true);
        }
    }

    private boolean isSumAvailable() {
        return getSum() <= getSurcharge();
    }

    @Override
    public void setPayment(PaymentEntity payment, PurchaseEntity purchase) {
        super.setPayment(payment, purchase);
        PurchaseEntity pe = purchase != null ? purchase : Factory.getTechProcessImpl().getCheck();
        if (pe != null && CollectionUtils.isEmpty(pe.getPayments()) && pe.getPrePayment() != null
                && PrepaymentEntity.class.getSimpleName().equalsIgnoreCase(pe.getPrePayment().getPaymentType())
                && UnboxingUtils.valueOf(pe.getPrePayment().getSumPay()) != 0) {
            // автоввод предоплаченной суммы
            getVisualPanel().showSpinner();
            threadPool.execute(() -> {
                setSum(pe.getPrePayment().getSumPay());
                processPayment();
            });
        } else {
            getVisualPanel().showPaymentForm();
        }
    }

    @Override
    public void enter() {
        if (!getVisualPanel().getPaymentInputPanel().isVisible()) {
            return;
        }
        if (!isSumAvailable()) {
            showAmountOfPaymentExceededMessage();
            setSum(getSurcharge());
        } else {
            processPayment();
        }
    }

    @Override
    public void reset() {
        setChange(false);
        setReset(true);
        resetPanels();

        if (isRefund()) {
            getVisualPanel().getPaymentInputPanel()
                    .setPresetMaxValue(BigDecimalConverter.convertMoney(Math.min(getSurcharge(), getPayment().getSumPay())));
        } else {
            getVisualPanel().getPaymentInputPanel().setMaxValue(null);
        }
    }

    @Override
    public long getSum() {
        return BigDecimalConverter.convertMoney(getInputField().getValue());
    }

    @Override
    public void setSum(long sum) {
        getInputField().setValue(BigDecimalConverter.convertMoney(sum));
    }

    @Override
    public boolean isPaymentAlwaysAvailable() {
        return true;
    }

    @Override
    public boolean isChangeAvailable() {
        return false;
    }

    private InputFieldInterface<BigDecimal> getInputField() {
        return getVisualPanel().getInputField();
    }

    private void showAmountOfPaymentExceededMessage() {
        String message = ResBundlePrepayment.getString("AMOUNT_OF_PAYMENT_EXCEEDED_MESSAGE");
        CheckContainer cc = Factory.getInstance().getMainWindow().getCheckContainer();
        cc.showMessage(message);
        cc.setState(CheckState.SHOW_MESSAGE);
    }
}
