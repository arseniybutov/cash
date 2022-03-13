package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringDocumentEntry.class, name = StringDocumentEntry.TYPE_NAME),
        @JsonSubTypes.Type(value = QrCodeDocumentEntry.class, name = QrCodeDocumentEntry.TYPE_NAME),
        @JsonSubTypes.Type(value = BarcodeDocumentEntry.class, name = BarcodeDocumentEntry.TYPE_NAME),
})
public class CommonDocumentEntry {
    /**
     * Данные
     */
    protected String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DocumentEntry{" +
                "data='" + data + '\'' +
                '}';
    }
}
