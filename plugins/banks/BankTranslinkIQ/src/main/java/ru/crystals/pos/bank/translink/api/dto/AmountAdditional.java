package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Дополнительная сумма
 */
public class AmountAdditional {
    /**
     * Тип, поддерживаются нижеперечисленные значения:
     * <ul>
     * <li>40 CahsBack</li>
     * <li>TP Чаевые</li>
     * <li>02 Остаток на картер</li>
     * <li>CA Сумма транзакции в другой валюте после конверсии</li>
     * </ul>
     */
    private final String type;
    /**
     * Числовой код валюты
     */
    private final String currencyCode;
    /**
     * Значение
     */
    private final long amount;

    @JsonCreator
    public AmountAdditional(@JsonProperty("type") String type,
                            @JsonProperty("currencyCode") String currencyCode,
                            @JsonProperty("amount") long amount) {
        this.type = type;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public long getAmount() {
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
        AmountAdditional that = (AmountAdditional) o;
        return amount == that.amount &&
                Objects.equals(type, that.type) &&
                Objects.equals(currencyCode, that.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, currencyCode, amount);
    }

    @Override
    public String toString() {
        return "TypeAmountAdditional{" +
                "type='" + type + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", amount=" + amount +
                '}';
    }
}
