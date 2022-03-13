package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Отмена/аннулирование ранее авторизованной карточной транзакции.  Данная операция возможна в течение банковского дня.
 */
public class VoidPartialCommand implements CommandParams {

    /**
     * Идентификатор операции, полученный в ответе команды AUTHORIZE
     */
    private final String operationId;

    private final long voidAmount;

    /**
     * Сумма void транзакции в валюте транзакции.
     */
    private final long originalAmount;

    /**
     * Сумма авторизированной транзакции в валюте транзакции
     */
    public VoidPartialCommand(String operationId, long voidAmount, long originalAmount) {
        this.operationId = operationId;
        this.voidAmount = voidAmount;
        this.originalAmount = originalAmount;
    }

    @JsonGetter("operationId")
    public String getOperationId() {
        return operationId;
    }

    @JsonGetter("voidAmount")
    public long getVoidAmount() {
        return voidAmount;
    }

    @JsonGetter("originalAmount")
    public long getOriginalAmount() {
        return originalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoidPartialCommand that = (VoidPartialCommand) o;
        return voidAmount == that.voidAmount &&
                originalAmount == that.originalAmount &&
                Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, voidAmount, originalAmount);
    }

    @Override
    public String toString() {
        return "VoidCommand{" +
                "operationId='" + operationId + '\'' +
                ", voidAmount=" + voidAmount +
                ", originalAmount=" + originalAmount +
                '}';
    }
}
