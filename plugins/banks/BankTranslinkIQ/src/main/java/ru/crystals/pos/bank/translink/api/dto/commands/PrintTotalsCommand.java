package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Команда инициирует создание отчета транзакций на POS. Если в терминале присутствует интегрированный принтер, печать отчета будет выполняться на принтере POS, в
 * противном случае будет инициировано событие ONPRINT, в котором будет передан текст отчёта.
 */
public class PrintTotalsCommand implements CommandParams {
    /**
     * Идентификатор кассира.
     */
    private final String operatorId;

    /**
     * Имя, фамилия кассира.
     */
    private final String operatorName;

    public PrintTotalsCommand(String operatorId, String operatorName) {
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }

    @JsonGetter("operatorId")
    public String getOperatorId() {
        return operatorId;
    }

    @JsonGetter("operatorName")
    public String getOperatorName() {
        return operatorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrintTotalsCommand that = (PrintTotalsCommand) o;
        return Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(operatorName, that.operatorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operatorId, operatorName);
    }

    @Override
    public String toString() {
        return "PrintTotalsCommand{" +
                "operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
