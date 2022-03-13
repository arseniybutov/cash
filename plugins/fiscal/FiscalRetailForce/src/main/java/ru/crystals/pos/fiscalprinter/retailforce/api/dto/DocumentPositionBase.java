package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentPositionBase {
    @JsonProperty("positionNumber")
    private int positionNumber;

    @JsonProperty("positionReference")
    private DocumentPositionReference positionReference;

    @JsonProperty("cancellationPosition")
    private boolean cancellationPosition;

    @JsonProperty("Type")
    private DocumentPositionType type;

    public DocumentPositionBase() {
    }

    DocumentPositionBase(int positionNumber,
                         DocumentPositionReference positionReference,
                         boolean cancellationPosition,
                         DocumentPositionType type) {
        this.positionNumber = positionNumber;
        this.positionReference = positionReference;
        this.cancellationPosition = cancellationPosition;
        this.type = type;
    }

    public int getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(final int positionNumber) {
        this.positionNumber = positionNumber;
    }

    public DocumentPositionReference getPositionReference() {
        return positionReference;
    }

    public void setPositionReference(final DocumentPositionReference positionReference) {
        this.positionReference = positionReference;
    }

    public boolean isCancellationPosition() {
        return cancellationPosition;
    }

    public void setCancellationPosition(final boolean cancellationPosition) {
        this.cancellationPosition = cancellationPosition;
    }

    public DocumentPositionType getType() {
        return type;
    }

    public void setType(final DocumentPositionType type) {
        this.type = type;
    }

    public static DocumentPositionBaseBuilder builder() {
        return new DocumentPositionBaseBuilder();
    }

    public static class DocumentPositionBaseBuilder {
        private int positionNumber;
        private DocumentPositionReference positionReference;
        private boolean cancellationPosition;
        private DocumentPositionType type;

        DocumentPositionBaseBuilder() {
        }

        public DocumentPositionBaseBuilder positionNumber(int positionNumber) {
            this.positionNumber = positionNumber;
            return this;
        }

        public DocumentPositionBaseBuilder positionReference(DocumentPositionReference positionReference) {
            this.positionReference = positionReference;
            return this;
        }

        public DocumentPositionBaseBuilder cancellationPosition(boolean cancellationPosition) {
            this.cancellationPosition = cancellationPosition;
            return this;
        }

        public DocumentPositionBaseBuilder type(DocumentPositionType type) {
            this.type = type;
            return this;
        }

        public DocumentPositionBase build() {
            return new DocumentPositionBase(positionNumber, positionReference, cancellationPosition, type);
        }

    }
}

