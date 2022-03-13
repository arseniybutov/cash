package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationSectionShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationSectionShiftAccumulation";

    /**
     * Номер секции.
     */
    private Integer sectionNumber;

    /**
     * Тип торговой операции.
     */
    private TradeOperationType tradeOperationType;

    /**
     * Количество.
     */
    private Long count;

    public Integer getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(Integer sectionNumber) {
        this.sectionNumber = sectionNumber;
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
        return "TradeOperationSectionShiftAccumulation{" +
                "sectionNumber=" + sectionNumber +
                ", tradeOperationType=" + tradeOperationType +
                ", count=" + count +
                ", sum=" + sum +
                '}';
    }
}
