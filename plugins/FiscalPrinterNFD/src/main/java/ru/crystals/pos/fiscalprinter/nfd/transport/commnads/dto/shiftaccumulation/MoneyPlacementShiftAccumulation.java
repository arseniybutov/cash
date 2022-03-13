package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.shiftaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.MoneyPlacementType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class MoneyPlacementShiftAccumulation extends CommonShiftAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "MoneyPlacementShiftAccumulation";

    /**
     * Тип операции.
     */
    private MoneyPlacementType type;

    /**
     * Количество.
     */
    private Long count = 0L;

    public MoneyPlacementType getType() {
        return type;
    }

    public void setType(MoneyPlacementType type) {
        this.type = type;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MoneyPlacementShiftAccumulation{" +
                "type=" + type +
                ", count=" + count +
                ", sum=" + sum +
                '}';
    }
}
