package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import java.math.BigDecimal;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationTaxShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationTaxShiftAccumulation";

    /**
     * Номер налоговой группы.
     */
    private int taxGroupNumber;

    /**
     * Оборот по налогу.
     */
    private BigDecimal turnover;

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;

    public int getTaxGroupNumber() {
        return taxGroupNumber;
    }

    public void setTaxGroupNumber(int taxGroupNumber) {
        this.taxGroupNumber = taxGroupNumber;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public TradeOperationType getTradeOperationType() {
        return tradeOperationType;
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        this.tradeOperationType = tradeOperationType;
    }

    @Override
    public String toString() {
        return "TradeOperationTaxShiftAccumulation{" +
                "taxGroupNumber=" + taxGroupNumber +
                ", turnover=" + turnover +
                ", tradeOperationType=" + tradeOperationType +
                ", sum=" + sum +
                '}';
    }
}
