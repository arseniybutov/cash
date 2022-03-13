package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class DocumentReference {
    @JsonProperty("referenceType")
    private ReferenceType referenceType;

    @JsonProperty("storeNumber")
    private String storeNumber;

    @JsonProperty("terminalNumber")
    private String terminalNumber;

    @JsonProperty("documentType")
    private DocumentType documentType;

    @JsonProperty("documentNumber")
    private String documentNumber;

    @JsonProperty("fiscalDocumentNumber")
    private int fiscalDocumentNumber;

    @JsonProperty("documentGuid")
    private String documentGuid;

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("documentBookDate")
    private OffsetDateTime documentBookDate;

    public DocumentReference() {
    }

    DocumentReference(ReferenceType referenceType,
                      String storeNumber,
                      String terminalNumber,
                      DocumentType documentType,
                      String documentNumber,
                      int fiscalDocumentNumber,
                      String documentGuid,
                      String documentId,
                      OffsetDateTime documentBookDate) {
        this.referenceType = referenceType;
        this.storeNumber = storeNumber;
        this.terminalNumber = terminalNumber;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.fiscalDocumentNumber = fiscalDocumentNumber;
        this.documentGuid = documentGuid;
        this.documentId = documentId;
        this.documentBookDate = documentBookDate;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(final ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(final String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(final String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(final DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public int getFiscalDocumentNumber() {
        return fiscalDocumentNumber;
    }

    public void setFiscalDocumentNumber(final int fiscalDocumentNumber) {
        this.fiscalDocumentNumber = fiscalDocumentNumber;
    }

    public String getDocumentGuid() {
        return documentGuid;
    }

    public void setDocumentGuid(final String documentGuid) {
        this.documentGuid = documentGuid;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public OffsetDateTime getDocumentBookDate() {
        return documentBookDate;
    }

    public void setDocumentBookDate(final OffsetDateTime documentBookDate) {
        this.documentBookDate = documentBookDate;
    }

    public static DocumentReferenceBuilder builder() {
        return new DocumentReferenceBuilder();
    }

    public static class DocumentReferenceBuilder {
        private ReferenceType referenceType;
        private String storeNumber;
        private String terminalNumber;
        private DocumentType documentType;
        private String documentNumber;
        private int fiscalDocumentNumber;
        private String documentGuid;
        private String documentId;
        private OffsetDateTime documentBookDate;

        DocumentReferenceBuilder() {
        }

        public DocumentReferenceBuilder referenceType(ReferenceType referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public DocumentReferenceBuilder storeNumber(String storeNumber) {
            this.storeNumber = storeNumber;
            return this;
        }

        public DocumentReferenceBuilder terminalNumber(String terminalNumber) {
            this.terminalNumber = terminalNumber;
            return this;
        }

        public DocumentReferenceBuilder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public DocumentReferenceBuilder documentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
            return this;
        }

        public DocumentReferenceBuilder fiscalDocumentNumber(int fiscalDocumentNumber) {
            this.fiscalDocumentNumber = fiscalDocumentNumber;
            return this;
        }

        public DocumentReferenceBuilder documentGuid(String documentGuid) {
            this.documentGuid = documentGuid;
            return this;
        }

        public DocumentReferenceBuilder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public DocumentReferenceBuilder documentBookDate(OffsetDateTime documentBookDate) {
            this.documentBookDate = documentBookDate;
            return this;
        }

        public DocumentReference build() {
            return new DocumentReference(referenceType,
                    storeNumber,
                    terminalNumber,
                    documentType,
                    documentNumber,
                    fiscalDocumentNumber,
                    documentGuid,
                    documentId,
                    documentBookDate);
        }
    }
}

