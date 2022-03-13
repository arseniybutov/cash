package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Информация об оплате чека из DocTicketPrint.Footer
 */
class FooterPayment {

    /**
     * Тип оплаты
     */
    @JsonProperty("payment_type")
    private String paymentType;
    /**
     * Сумма оплаты
     */
    @JsonProperty("sum")
    private CbsMoney sum;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public CbsMoney getSum() {
        return sum;
    }

    public void setSum(CbsMoney sum) {
        this.sum = sum;
    }
}
