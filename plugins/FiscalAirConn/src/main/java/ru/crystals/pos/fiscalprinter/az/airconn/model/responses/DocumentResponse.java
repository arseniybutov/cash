package ru.crystals.pos.fiscalprinter.az.airconn.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentResponse {
    @JsonProperty("document_id")
    private String documentId;
    @JsonProperty("short_document_id")
    private String shortDocumentId;
    @JsonProperty("document_number")
    private Long documentNumber;
    @JsonProperty("shift_document_number")
    private Long shiftDocumentNumber;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getShortDocumentId() {
        return shortDocumentId;
    }

    public void setShortDocumentId(String shortDocumentId) {
        this.shortDocumentId = shortDocumentId;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getShiftDocumentNumber() {
        return shiftDocumentNumber;
    }

    public void setShiftDocumentNumber(Long shiftDocumentNumber) {
        this.shiftDocumentNumber = shiftDocumentNumber;
    }
}
