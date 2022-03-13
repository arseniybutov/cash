package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.tradeoperationaccumulation;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ModifierType;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class ModifierTradeOperationAccumulation extends CommonTradeOperationAccumulation {

    public static final String TYPE_NAME = DTO_PREFIX + "ModifierTradeOperationAccumulation";

    /**
     * Тип модификатора
     */
    private ModifierType type;

    public ModifierType getType() {
        return type;
    }

    public void setType(ModifierType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ModifierTradeOperationAccumulation{" +
                "type=" + type +
                ", sum=" + sum +
                ", count=" + count +
                '}';
    }
}
