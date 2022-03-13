package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.MoneyPlacementType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.util.ArrayList;
import java.util.List;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class MoneyPlacementDocument extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "MoneyPlacementDocument";

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entries")
    private List<CommonDocumentEntry> data = new ArrayList();

    private MoneyPlacementType type;

    public List<CommonDocumentEntry> getData() {
        return data;
    }

    public void setData(List<CommonDocumentEntry> data) {
        this.data = data;
    }

    public MoneyPlacementType getType() {
        return type;
    }

    public void setType(MoneyPlacementType type) {
        this.type = type;
    }

    public void addDataEntry(CommonDocumentEntry documentEntry) {
        data.add(documentEntry);
    }

    @Override
    public String toString() {
        return "MoneyPlacementDocument{" +
                "data=" + data +
                '}';
    }
}