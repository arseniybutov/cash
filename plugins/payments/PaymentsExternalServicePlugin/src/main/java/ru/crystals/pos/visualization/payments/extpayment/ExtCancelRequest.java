package ru.crystals.pos.visualization.payments.extpayment;

import ru.crystals.api.commons.ReceiptPurchaseEntityWrapper;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.spi.plugin.payment.CancelRequest;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.techprocess.TechProcessInterface;

/**
 * @author dalex
 */
public class ExtCancelRequest implements CancelRequest {

    protected PaymentEntity paymentEntity;
    protected PaymentCallback paymentCallback;
    protected TechProcessInterface tp;
    protected Payment payment;

    public ExtCancelRequest(TechProcessInterface tp, PaymentCallback paymentCallback) {
        this.tp = tp;
        this.paymentCallback = paymentCallback;
    }

    public void setPaymentEntity(PaymentEntity paymentEntity) {
        this.paymentEntity = paymentEntity;
        payment = new Payment();
        payment.setSum(paymentEntity.getEndSumPayBigDecimal());
        payment.getData().putAll(paymentEntity.getPluginPropertiesMap());
    }

    @Override
    public Payment getPayment() {
        return payment;
    }

    @Override
    public PaymentCallback getPaymentCallback() {
        return paymentCallback;
    }

    @Override
    public Receipt getReceipt() {
        ReceiptPurchaseEntityWrapper w = new ReceiptPurchaseEntityWrapper(tp.getCheck());
        Long number = tp.getCheckWithNumber().getNumber();
        w.setNumber(number == null ? -1 : number.intValue());
        w.setShift(tp.getShift());
        return w;
    }
}
