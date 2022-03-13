package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationModifierShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationModifierShiftAccumulation";

    /**
     * Тип модификатора.
     */
    private ModifierType type;

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;

    /**
     * Количество модификаторов.
     */
    private Long count;

    public ModifierType getType() {
        return type;
    }

    public void setType(ModifierType type) {
        this.type = type;
    }

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
        return "TradeOperationModifierShiftAccumulation{" +
                "type=" + type +
                ", tradeOperationType=" + tradeOperationType +
                ", count=" + count +
                ", sum=" + sum +
                '}';
    }
}
