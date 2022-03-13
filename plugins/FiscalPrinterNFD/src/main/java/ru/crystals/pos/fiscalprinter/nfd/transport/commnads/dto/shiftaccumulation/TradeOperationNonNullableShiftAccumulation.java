package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NonNullableType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationNonNullableShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationNonNullableShiftAccumulation";

    /**
     * Тип модификатора.
     */
    private NonNullableType type;

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;


    public NonNullableType getType() {
        return type;
    }

    public void setType(NonNullableType type) {
        this.type = type;
    }

    public TradeOperationType getTradeOperationType() {
        return tradeOperationType;
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        this.tradeOperationType = tradeOperationType;
    }

    @Override
    public String toString() {
        return "TradeOperationNonNullableShiftAccumulation{" +
                "type=" + type +
                ", tradeOperationType=" + tradeOperationType +
                ", sum=" + sum +
                '}';
    }
}
