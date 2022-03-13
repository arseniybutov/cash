package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ReportType;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.util.ArrayList;
import java.util.List;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

public class ReportDocument extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "ReportDocument";

    @JacksonXmlProperty(localName = "entries")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CommonDocumentEntry> data = new ArrayList();

    private ReportType type;

    public List<CommonDocumentEntry> getData() {
        return data;
    }

    public void setData(List<CommonDocumentEntry> data) {
        this.data = data;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public void addDataEntry(CommonDocumentEntry documentEntry) {
        data.add(documentEntry);
    }

    @Override
    public String toString() {
        return "ReportDocument{" +
                "data=" + data +
                '}';
    }
}