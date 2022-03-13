package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.tradeoperationaccumulation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ModifierTradeOperationAccumulation.class, name = ModifierTradeOperationAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = SectionTradeOperationAccumulation.class, name = SectionTradeOperationAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TaxTradeOperationAccumulation.class, name = TaxTradeOperationAccumulation.TYPE_NAME),
})
public class CommonTradeOperationAccumulation extends BaseResultObject {

    /**
     * Сумма накоплений.
     */
    protected BigDecimal sum;

    /**
     * Количество.
     */
    protected Long count;

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "TradeOperationAccumulation{" +
                "sum=" + sum +
                ", count=" + count +
                '}';
    }
}
