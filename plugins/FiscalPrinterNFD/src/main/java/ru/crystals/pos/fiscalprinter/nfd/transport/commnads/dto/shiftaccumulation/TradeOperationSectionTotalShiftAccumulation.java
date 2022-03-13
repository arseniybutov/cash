package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationSectionTotalShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationSectionTotalShiftAccumulation";

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;

    public TradeOperationType getTradeOperationType() {
        return tradeOperationType;
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        this.tradeOperationType = tradeOperationType;
    }

    @Override
    public String toString() {
        return "TradeOperationSectionTotalShiftAccumulation{" +
                "tradeOperationType=" + tradeOperationType +
                ", sum=" + sum +
                '}';
    }
}
