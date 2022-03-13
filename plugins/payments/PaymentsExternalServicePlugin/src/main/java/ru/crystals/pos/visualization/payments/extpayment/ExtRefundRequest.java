package ru.crystals.pos.visualization.payments.extpayment;

import ru.crystals.api.commons.ReceiptPurchaseEntityWrapper;
import ru.crystals.pos.api.plugin.payment.Payment;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.plugin.payment.RefundRequest;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.techprocess.TechProcessInterface;

import java.math.BigDecimal;

/**
 * Запрос на выполнение возврата оплаты
 */
public class ExtRefundRequest implements RefundRequest {

    private TechProcessInterface tp;
    private PaymentCallback paymentCallback;

    /**
     * Оплата которую хотим вернуть
     */
    private PaymentEntity paymentEntity;

    /**
     * Оригинальный чек
     */
    private PurchaseEntity originalPurchase;
    private BigDecimal sumToRefund;

    public ExtRefundRequest(TechProcessInterface tp, PaymentCallback paymentCallback) {
        this.tp = tp;
        this.paymentCallback = paymentCallback;
    }

    public PurchaseEntity getOriginalPurchase() {
        return originalPurchase;
    }

    public void setOriginalPurchase(PurchaseEntity originalPurchase) {
        this.originalPurchase = originalPurchase;
    }

    public void setPaymentEntity(PaymentEntity paymentEntity) {
        this.paymentEntity = paymentEntity;
    }

    @Override
    public Receipt getOriginalReceipt() {
        return originalPurchase != null ? new ReceiptPurchaseEntityWrapper(originalPurchase) : null;
    }

    @Override
    public Payment getOriginalPayment() {
        Payment payment = new Payment();
        payment.setSum(paymentEntity.getEndSumPayBigDecimal());
        payment.getData().putAll(paymentEntity.getPluginPropertiesMap());
        return payment;
    }

    public void setSumToRefund(BigDecimal sumToRefund) {
        this.sumToRefund = sumToRefund;
    }

    @Override
    public BigDecimal getSumToRefund() {
        return sumToRefund;
    }

    @Override
    public PaymentCallback getPaymentCallback() {
        return paymentCallback;
    }

    @Override
    public Receipt getRefundReceipt() {
        ReceiptPurchaseEntityWrapper w = new ReceiptPurchaseEntityWrapper(tp.getCheck());
        Long number = tp.getCheckWithNumber().getNumber();
        w.setNumber(number == null ? -1 : number.intValue());
        w.setShift(tp.getShift());
        return w;
    }
}
