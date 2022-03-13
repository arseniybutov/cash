package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.tradeoperationaccumulation;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class SectionTradeOperationAccumulation extends CommonTradeOperationAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "SectionTradeOperationAccumulation";

    /**
     * Номер секции.
     */
    private int sectionNumber;

    public int getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    @Override
    public String toString() {
        return "SectionTradeOperationAccumulation{" +
                "sectionNumber=" + sectionNumber +
                ", sum=" + sum +
                ", count=" + count +
                '}';
    }
}
