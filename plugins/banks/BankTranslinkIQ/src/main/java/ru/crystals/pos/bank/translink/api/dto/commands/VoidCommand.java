package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Отмена/аннулирование ранее авторизованной карточной транзакции.  Данная операция возможна в течение банковского дня.
 */
public class VoidCommand implements CommandParams {

    /**
     * Идентификатор операции, полученный в ответе команды AUTHORIZE
     */
    private final String operationId;

    public VoidCommand(String operationId) {
        this.operationId = operationId;
    }

    @JsonGetter("operationId")
    public String getOperationId() {
        return operationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoidCommand that = (VoidCommand) o;
        return Objects.equals(operationId, that.operationId);
    }

    @Override
    public String toString() {
        return "VoidCommand{" +
                "operationId='" + operationId + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
}
