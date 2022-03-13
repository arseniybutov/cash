package ru.crystals.pos.fiscalprinter.nfd.transport.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.GetSubTotalDecimal;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.Initialization;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.MoneyPlacementDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.ReportDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.ShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.State;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.TradeOperationAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.TradeOperationDocument;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MoneyPlacementDocument.class, name = MoneyPlacementDocument.TYPE_NAME),
        @JsonSubTypes.Type(value = Initialization.class, name = Initialization.TYPE_NAME),
        @JsonSubTypes.Type(value = GetSubTotalDecimal.class, name = GetSubTotalDecimal.TYPE_NAME),
        @JsonSubTypes.Type(value = ReportDocument.class, name = ReportDocument.TYPE_NAME),
        @JsonSubTypes.Type(value = ShiftAccumulation.class, name = ShiftAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = State.class, name = State.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationAccumulation.class, name = TradeOperationAccumulation.TYPE_NAME),
        @JsonSubTypes.Type(value = TradeOperationDocument.class, name = TradeOperationDocument.TYPE_NAME),

})
public class BaseResultObject {
    public BaseResultObject() {
    }
}
