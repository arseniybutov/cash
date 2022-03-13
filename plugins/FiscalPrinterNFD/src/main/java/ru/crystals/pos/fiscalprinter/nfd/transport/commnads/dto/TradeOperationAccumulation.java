package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.tradeoperationaccumulation.CommonTradeOperationAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.util.ArrayList;
import java.util.List;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class TradeOperationAccumulation extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "TradeOperationAccumulation";

    @JacksonXmlProperty(localName = "accumulations")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CommonTradeOperationAccumulation> accumulations = new ArrayList();

    public List<CommonTradeOperationAccumulation> getAccumulations() {
        return accumulations;
    }

    public void setAccumulations(List<CommonTradeOperationAccumulation> accumulations) {
        this.accumulations = accumulations;
    }

    @Override
    public String toString() {
        return "TradeOperationAccumulation{" +
                "accumulations=" + accumulations +
                '}';
    }
}