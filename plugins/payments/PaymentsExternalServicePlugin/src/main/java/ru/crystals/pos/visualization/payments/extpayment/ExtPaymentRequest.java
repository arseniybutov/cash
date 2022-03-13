package ru.crystals.pos.visualization.payments.extpayment;

import ru.crystals.api.commons.ReceiptPurchaseEntityWrapper;
import ru.crystals.pos.spi.plugin.payment.PaymentCallback;
import ru.crystals.pos.spi.plugin.payment.PaymentRequest;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.techprocess.TechProcessInterface;

/**
 * @author dalex
 */
public class ExtPaymentRequest implements PaymentRequest {

    private TechProcessInterface tp;
    private PaymentCallback paymentCallback;

    public ExtPaymentRequest(TechProcessInterface tp, PaymentCallback paymentCallback) {
        this.tp = tp;
        this.paymentCallback = paymentCallback;
    }

    @Override
    public Receipt getReceipt() {
        ReceiptPurchaseEntityWrapper w = new ReceiptPurchaseEntityWrapper(tp.getCheck());
        Long number = tp.getCheckWithNumber().getNumber();
        w.setNumber(number == null ? -1 : number.intValue());
        w.setShift(tp.getShift());
        return w;
    }

    @Override
    public PaymentCallback getPaymentCallback() {
        return paymentCallback;
    }
}
