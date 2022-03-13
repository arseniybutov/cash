package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.tradeoperationaccumulation;

import java.math.BigDecimal;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TaxTradeOperationAccumulation extends CommonTradeOperationAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TaxTradeOperationAccumulation";

    /**
     * Номер налоговой группы.
     */
    private int taxGroupNumber;

    /**
     * Оборот по налогу.
     */
    private BigDecimal turnover;

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

    @Override
    public String toString() {
        return "TaxTradeOperationAccumulation{" +
                "taxGroupNumber=" + taxGroupNumber +
                ", turnover=" + turnover +
                ", sum=" + sum +
                ", count=" + count +
                '}';
    }
}
