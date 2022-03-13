package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Произвольные параметры
 */
public class Params {

    /**
     * «Служебное» назначения платежа: не передается в НСПК, не демонстрируется покупателю после сканирования QR,
     * опционально к заполнению, в случае заполнения будет отображаться в банковской выписке по расчетному счету торговца в Назначении платежа.
     * Максимальная длина - 140 символов.
     */
    @JsonProperty("paymentData")
    private String paymentData;

    @JsonCreator
    public Params(@JsonProperty("paymentData") String paymentData) {
        this.paymentData = paymentData;
    }

    public String getPaymentData() {
        return paymentData;
    }
}
