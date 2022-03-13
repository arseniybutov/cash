package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.PaymentType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationPaymentShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationPaymentShiftAccumulation";

    /**
     * Тип оплаты.
     */
    private PaymentType type;

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;


    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
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
        return "TradeOperationPaymentShiftAccumulation{" +
                "type=" + type +
                ", tradeOperationType=" + tradeOperationType +
                ", sum=" + sum +
                '}';
    }
}
