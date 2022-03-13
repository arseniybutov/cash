package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

/**
 * Команда инициирует процедуру закрытия финансового дня на POS.
 */
public class CloseDayCommand implements CommandParams {
    /**
     * Идентификатор кассира.
     */
    private final String operatorId;

    /**
     * Имя, фамилия кассира.
     */
    private final String operatorName;

    public CloseDayCommand(String operatorId, String operatorName) {
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
        CloseDayCommand that = (CloseDayCommand) o;
        return Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(operatorName, that.operatorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operatorId, operatorName);
    }

    @Override
    public String toString() {
        return "CloseDayCommand{" +
                "operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
