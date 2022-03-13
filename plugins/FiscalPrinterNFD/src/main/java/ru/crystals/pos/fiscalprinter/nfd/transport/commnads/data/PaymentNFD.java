package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;

import java.math.BigDecimal;

/**
 * Платежи.
 */
public class PaymentNFD {

    /**
     * Тип платежа.
     */
    private PaymentType paymentType;

    /**
     * Сумма платежа.
     */
    private BigDecimal sum;

    public PaymentNFD(PaymentType paymentType, BigDecimal sum) {
        this.paymentType = paymentType;
        this.sum = sum;
    }

    public PaymentNFD add(PaymentNFD other) {
        if (other.getPaymentType() != this.paymentType) {
            throw new IllegalArgumentException("Mismatch payment type");
        }
        return new PaymentNFD(paymentType, this.sum.add(other.getSum()));
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentNFD that = (PaymentNFD) o;

        if (paymentType != that.paymentType) {
            return false;
        }
        return sum != null ? sum.compareTo(that.sum) == 0 : that.sum == null;
    }

    @Override
    public int hashCode() {
        int result = paymentType != null ? paymentType.hashCode() : 0;
        result = 31 * result + (sum != null ? sum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PaymentNFD{" +
                "paymentType=" + paymentType +
                ", sum=" + sum +
                '}';
    }
}
