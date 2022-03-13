package ru.crystals.pos.bank.raiffeisensbp.api.request;


import com.fasterxml.jackson.annotation.JsonGetter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Запрос на возврат по платежу
 */
public class RefundRequest {

    /**
     * Сумма возврата в рублях
     */
    private BigDecimal amount;

    /**
     * Идентификатор заказа платежа в Райффайзенбанке, используется для возвратов по динамическому QR
     */
    private String order;

    /**
     * Уникальный идентификатор запроса на возврат
     */
    private String refundId;

    /**
     * Идентификатор операции платежа в Райффайзенбанке, используется для возвратов по статическому QR
     */
    private Long transactionId;

    /**
     * Дополнительные детали платежа
     */
    private String paymentDetails;

    public RefundRequest(BigDecimal amount, String order, String refundId, String paymentDetails) {
        this.amount = amount;
        this.order = order;
        this.refundId = refundId;
        this.paymentDetails = paymentDetails;
    }

    @JsonGetter
    public BigDecimal getAmount() {
        return amount;
    }

    @JsonGetter
    public String getOrder() {
        return order;
    }

    @JsonGetter
    public String getRefundId() {
        return refundId;
    }

    @JsonGetter
    public String getPaymentDetails() {
        return paymentDetails;
    }

    @JsonGetter
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefundRequest that = (RefundRequest) o;
        return amount.compareTo(that.amount) == 0 &&
                Objects.equals(order, that.order) &&
                Objects.equals(refundId, that.refundId) &&
                Objects.equals(paymentDetails, that.paymentDetails) &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, order, refundId, paymentDetails, transactionId);
    }

    @Override
    public String toString() {
        return "RefundRequest{" +
                "amount=" + amount +
                ", order='" + order + '\'' +
                ", refundId='" + refundId + '\'' +
                ", paymentDetails='" + paymentDetails + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }

}
