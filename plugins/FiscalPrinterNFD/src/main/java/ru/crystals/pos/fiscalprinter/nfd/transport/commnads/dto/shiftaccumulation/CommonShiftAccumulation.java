package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashSumShiftAccumulation.class, name = CashSumShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = MoneyPlacementShiftAccumulation.class, name = MoneyPlacementShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = RevenueShiftAccumulation.class, name = RevenueShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationModifierShiftAccumulation.class, name = TradeOperationModifierShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationNonNullableShiftAccumulation.class, name = TradeOperationNonNullableShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationPaymentShiftAccumulation.class, name = TradeOperationPaymentShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationSectionShiftAccumulation.class, name = TradeOperationSectionShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationSectionTotalShiftAccumulation.class, name = TradeOperationSectionTotalShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationTaxShiftAccumulation.class, name = TradeOperationTaxShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationTotalShiftAccumulation.class, name = TradeOperationTotalShiftAccumulation.TYPE_NAME),
})
public class CommonShiftAccumulation extends BaseResultObject {

    /**
     * Сумма накоплений.
     */
    protected BigDecimal sum = new BigDecimal(0);

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getSumForSetX() {
        return sum.multiply(new BigDecimal("100"));
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "ShiftAccumulation{" +
                "sum=" + sum +
                '}';
    }
}
