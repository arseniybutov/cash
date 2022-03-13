package ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.BusinessTransactionType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPosition;

import java.math.BigDecimal;
import java.util.Objects;

public class EndOfDayPosition implements DocumentPosition {

    @JsonProperty("type")
    private EndOfDayPositionType type;

    @JsonProperty("businessTransactionType")
    private BusinessTransactionType businessTransactionType;

    @JsonProperty("grossValue")
    private BigDecimal grossValue;

    public EndOfDayPosition() {
    }

    public EndOfDayPositionType getType() {
        return type;
    }

    public void setType(EndOfDayPositionType type) {
        this.type = type;
    }

    public BusinessTransactionType getBusinessTransactionType() {
        return businessTransactionType;
    }

    public void setBusinessTransactionType(BusinessTransactionType businessTransactionType) {
        this.businessTransactionType = businessTransactionType;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndOfDayPosition that = (EndOfDayPosition) o;

        if (type != that.type) {
            return false;
        }
        if (businessTransactionType != that.businessTransactionType) {
            return false;
        }
        return grossValue != null ? grossValue.compareTo(that.grossValue) == 0 : that.grossValue == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, businessTransactionType, grossValue);
    }

    @Override
    public String toString() {
        return "EndOfDayPosition{" +
                "type=" + type +
                ", businessTransactionType=" + businessTransactionType +
                ", grossValue=" + grossValue +
                '}';
    }


}

