package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class DocumentPositionReference {

    @JsonUnwrapped
    private DocumentReference reference;

    @JsonProperty("positionNumber")
    private int positionNumber;

    public DocumentPositionReference() {
    }

    public DocumentPositionReference(DocumentReference reference, int positionNumber) {
        this.reference = reference;
        this.positionNumber = positionNumber;
    }

    public DocumentReference getReference() {
        return reference;
    }

    public void setReference(DocumentReference reference) {
        this.reference = reference;
    }

    public int getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(int positionNumber) {
        this.positionNumber = positionNumber;
    }
}

