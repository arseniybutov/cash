package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Описание ограничений рассрочки
 */
public class InstallmentForm {

    /**
     * Минимальное количество платежей
     */
    private final int min;

    /**
     * Максимальное количество платежей
     */
    private final int max;

    /**
     * Список возможных значений количества платежей. Если не задан, то можно использовать любое число платежей
     * в диапазоне [{@link #min}..{@link #max}] (включительно)
     */
    private final List<Integer> listOfPossibleValues;

    @JsonCreator
    public InstallmentForm(@JsonProperty("min") int min,
                           @JsonProperty("max") int max,
                           @JsonProperty("listOfPossibleValues") List<Integer> listOfPossibleValues) {
        this.min = min;
        this.max = max;
        this.listOfPossibleValues = listOfPossibleValues == null ? Collections.emptyList() : listOfPossibleValues;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public List<Integer> getListOfPossibleValues() {
        return listOfPossibleValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallmentForm that = (InstallmentForm) o;
        return min == that.min &&
                max == that.max &&
                Objects.equals(listOfPossibleValues, that.listOfPossibleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, listOfPossibleValues);
    }

    @Override
    public String toString() {
        return "InstallmentForm{" +
                "min=" + min +
                ", max=" + max +
                ", listOfPossibleValues=" + listOfPossibleValues +
                '}';
    }
}
