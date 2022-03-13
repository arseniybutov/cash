package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationTotalShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationTotalShiftAccumulation";

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;

    /**
     * Количество.
     */
    private Long count = 0L;

    public TradeOperationType getTradeOperationType() {
        return tradeOperationType;
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        this.tradeOperationType = tradeOperationType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "TradeOperationTotalShiftAccumulation{" +
                "tradeOperationType=" + tradeOperationType +
                ", count=" + count +
                ", sum=" + sum +
                '}';
    }
}
