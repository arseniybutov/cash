package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Запрос на возврат по платежу
 */
public class RefundPartOfAmountRequest {

    /**
     * "ReferenceId” – то же, что и “OperationId"
     */
    @JsonProperty("referenceId")
    private final String referenceId;

    /**
     * Уникальный идентификатор операции. Обязательно заполнять данное поле в POST запросах при использовании сервиса в составе Merchant API
     */
    @JsonProperty("id")
    private String id;

    /**
     * Информация о возвращаемой сумме для возврата части суммы
     */
    @JsonProperty("amount")
    private Amount amount;

    /**
     * Конструктор для возврата части суммы
     */
    public RefundPartOfAmountRequest(String referenceId, String id, Amount amount) {
        this.referenceId = referenceId;
        this.id = id;
        this.amount = amount;
    }

    @JsonGetter
    public String getReferenceId() {
        return referenceId;
    }

    @JsonGetter
    public String getId() {
        return id;
    }

    @JsonGetter
    public Amount getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefundPartOfAmountRequest that = (RefundPartOfAmountRequest) o;
        return Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(id, that.id) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceId, id);
    }

    @Override
    public String toString() {
        return "RefundPartOfAmountRequest{" +
                "referenceId='" + referenceId + '\'' +
                ", id='" + id + '\'' +
                ", amount=" + amount +
                '}';
    }
}
