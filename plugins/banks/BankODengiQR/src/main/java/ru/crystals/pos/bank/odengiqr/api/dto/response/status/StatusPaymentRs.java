package ru.crystals.pos.bank.odengiqr.api.dto.response.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

import java.util.List;

public class StatusPaymentRs extends Data {

    /**
     * Статус транзакции (возвращается только если счет еще никто не пытался оплатить)
     * (processing – в процессе оплаты; canceled – закончилось время жизни счета date_life)
     */
    @JsonProperty("status")
    private Status status;

    /**
     * Массив всех попыток оплатить счет
     */
    @JsonProperty("payments")
    private List<Payment> payments;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
