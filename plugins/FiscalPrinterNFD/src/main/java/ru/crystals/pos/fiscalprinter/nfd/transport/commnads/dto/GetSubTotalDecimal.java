package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.math.BigDecimal;

public class GetSubTotalDecimal extends BaseResultObject {

    public static final String TYPE_NAME = "xs:decimal";

    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "GetSubTotalDecimal{" +
                "value=" + value +
                '}';
    }
}
