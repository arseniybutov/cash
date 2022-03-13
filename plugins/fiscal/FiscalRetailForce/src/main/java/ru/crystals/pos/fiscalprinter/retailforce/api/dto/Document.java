package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public class Document {

    @JsonProperty("uniqueClientId")
    private String uniqueClientId;

    @JsonProperty("documentGuid")
    private String documentGuid;

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("createDate")
    private OffsetDateTime createDate;

    @JsonProperty("bookDate")
    private OffsetDateTime bookDate;

    @JsonProperty("documentNumber")
    private String documentNumber;

    @JsonProperty("cancellationDocument")
    private boolean cancellationDocument;

    @JsonProperty("documentReference")
    private DocumentReference documentReference;

    @JsonProperty("isTraining")
    private boolean training;

    @JsonProperty("documentType")
    private DocumentType documentType;

    @JsonProperty("documentTypeCaption")
    private String documentTypeCaption;

    @JsonProperty("user")
    private User user;

    @JsonProperty("allocationGroups")
    private List<String> allocationGroups;

    @JsonProperty("fiscalResponse")
    private FiscalResponse fiscalResponse;

    @JsonProperty("fiscalDocumentNumber")
    private int fiscalDocumentNumber;

    @JsonProperty("fiscalDocumentRevision")
    private int fiscalDocumentRevision;

    @JsonProperty("fiscalDocumentStartTime")
    private Long fiscalDocumentStartTime;

    @JsonProperty("positions")
    private List<DocumentPosition> positions;

    @JsonProperty("payments")
    private List<DocumentPayment> payments;

    public Document() {
    }

    private Document(String uniqueClientId,
                     String documentGuid,
                     String documentId,
                     OffsetDateTime createDate,
                     OffsetDateTime bookDate,
                     String documentNumber,
                     boolean cancellationDocument,
                     DocumentReference documentReference,
                     boolean isTraining,
                     DocumentType documentType,
                     String documentTypeCaption,
                     User user,
                     List<String> allocationGroups,
                     FiscalResponse fiscalResponse,
                     int fiscalDocumentNumber,
                     int fiscalDocumentRevision,
                     Long fiscalDocumentStartTime,
                     List<DocumentPosition> positions,
                     List<DocumentPayment> payments) {
        this.uniqueClientId = uniqueClientId;
        this.documentGuid = documentGuid;
        this.documentId = documentId;
        this.createDate = createDate;
        this.bookDate = bookDate;
        this.documentNumber = documentNumber;
        this.cancellationDocument = cancellationDocument;
        this.documentReference = documentReference;
        this.training = isTraining;
        this.documentType = documentType;
        this.documentTypeCaption = documentTypeCaption;
        this.user = user;
        this.allocationGroups = allocationGroups;
        this.fiscalResponse = fiscalResponse;
        this.fiscalDocumentNumber = fiscalDocumentNumber;
        this.fiscalDocumentRevision = fiscalDocumentRevision;
        this.fiscalDocumentStartTime = fiscalDocumentStartTime;
        this.positions = positions;
        this.payments = payments;
    }

    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }

    public String getUniqueClientId() {
        return uniqueClientId;
    }

    public String getDocumentGuid() {
        return documentGuid;
    }

    public String getDocumentId() {
        return documentId;
    }

    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    public OffsetDateTime getBookDate() {
        return bookDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public boolean isCancellationDocument() {
        return cancellationDocument;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public boolean isTraining() {
        return training;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentTypeCaption() {
        return documentTypeCaption;
    }

    public User getUser() {
        return user;
    }

    public List<String> getAllocationGroups() {
        return allocationGroups;
    }

    public FiscalResponse getFiscalResponse() {
        return fiscalResponse;
    }

    public int getFiscalDocumentNumber() {
        return fiscalDocumentNumber;
    }

    public int getFiscalDocumentRevision() {
        return fiscalDocumentRevision;
    }

    public Long getFiscalDocumentStartTime() {
        return fiscalDocumentStartTime;
    }

    public List<DocumentPosition> getPositions() {
        return positions;
    }

    public List<DocumentPayment> getPayments() {
        return payments;
    }

    public void setUniqueClientId(String uniqueClientId) {
        this.uniqueClientId = uniqueClientId;
    }

    public void setDocumentGuid(String documentGuid) {
        this.documentGuid = documentGuid;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCreateDate(OffsetDateTime createDate) {
        this.createDate = createDate;
    }

    public void setBookDate(OffsetDateTime bookDate) {
        this.bookDate = bookDate;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setCancellationDocument(boolean cancellationDocument) {
        this.cancellationDocument = cancellationDocument;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public void setTraining(boolean isTraining) {
        this.training = isTraining;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public void setDocumentTypeCaption(String documentTypeCaption) {
        this.documentTypeCaption = documentTypeCaption;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAllocationGroups(List<String> allocationGroups) {
        this.allocationGroups = allocationGroups;
    }

    public void setFiscalResponse(FiscalResponse fiscalResponse) {
        this.fiscalResponse = fiscalResponse;
    }

    public void setFiscalDocumentNumber(int fiscalDocumentNumber) {
        this.fiscalDocumentNumber = fiscalDocumentNumber;
    }

    public void setFiscalDocumentRevision(int fiscalDocumentRevision) {
        this.fiscalDocumentRevision = fiscalDocumentRevision;
    }

    public void setFiscalDocumentStartTime(Long fiscalDocumentStartTime) {
        this.fiscalDocumentStartTime = fiscalDocumentStartTime;
    }

    public void setPositions(List<DocumentPosition> positions) {
        this.positions = positions;
    }

    public void setPayments(List<DocumentPayment> payments) {
        this.payments = payments;
    }

    public static class DocumentBuilder {
        private String uniqueClientId;
        private String documentGuid;
        private String documentId;
        private OffsetDateTime createDate;
        private OffsetDateTime bookDate;
        private String documentNumber;
        private boolean cancellationDocument;
        private DocumentReference documentReference;
        private boolean isTraining;
        private DocumentType documentType;
        private String documentTypeCaption;
        private User user;
        private List<String> allocationGroups;
        private FiscalResponse fiscalResponse;
        private int fiscalDocumentNumber;
        private int fiscalDocumentRevision;
        private Long fiscalDocumentStartTime;
        private List<DocumentPosition> positions;
        private List<DocumentPayment> payments;

        DocumentBuilder() {
        }

        public DocumentBuilder uniqueClientId(String uniqueClientId) {
            this.uniqueClientId = uniqueClientId;
            return this;
        }

        public DocumentBuilder documentGuid(String documentGuid) {
            this.documentGuid = documentGuid;
            return this;
        }

        public DocumentBuilder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public DocumentBuilder createDate(OffsetDateTime createDate) {
            this.createDate = createDate;
            return this;
        }

        public DocumentBuilder bookDate(OffsetDateTime bookDate) {
            this.bookDate = bookDate;
            return this;
        }

        public DocumentBuilder documentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
            return this;
        }

        public DocumentBuilder cancellationDocument(boolean cancellationDocument) {
            this.cancellationDocument = cancellationDocument;
            return this;
        }

        public DocumentBuilder documentReference(DocumentReference documentReference) {
            this.documentReference = documentReference;
            return this;
        }

        public DocumentBuilder isTraining(boolean isTraining) {
            this.isTraining = isTraining;
            return this;
        }

        public DocumentBuilder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public DocumentBuilder documentTypeCaption(String documentTypeCaption) {
            this.documentTypeCaption = documentTypeCaption;
            return this;
        }

        public DocumentBuilder user(User user) {
            this.user = user;
            return this;
        }

        public DocumentBuilder allocationGroups(List<String> allocationGroups) {
            this.allocationGroups = allocationGroups;
            return this;
        }

        public DocumentBuilder fiscalResponse(FiscalResponse fiscalResponse) {
            this.fiscalResponse = fiscalResponse;
            return this;
        }

        public DocumentBuilder fiscalDocumentNumber(int fiscalDocumentNumber) {
            this.fiscalDocumentNumber = fiscalDocumentNumber;
            return this;
        }

        public DocumentBuilder fiscalDocumentRevision(int fiscalDocumentRevision) {
            this.fiscalDocumentRevision = fiscalDocumentRevision;
            return this;
        }

        public DocumentBuilder fiscalDocumentStartTime(Long fiscalDocumentStartTime) {
            this.fiscalDocumentStartTime = fiscalDocumentStartTime;
            return this;
        }

        public DocumentBuilder positions(List<DocumentPosition> positions) {
            this.positions = positions;
            return this;
        }

        public DocumentBuilder payments(List<DocumentPayment> payments) {
            this.payments = payments;
            return this;
        }

        public Document build() {
            return new Document(uniqueClientId,
                    documentGuid,
                    documentId,
                    createDate,
                    bookDate,
                    documentNumber,
                    cancellationDocument,
                    documentReference,
                    isTraining,
                    documentType,
                    documentTypeCaption,
                    user,
                    allocationGroups,
                    fiscalResponse,
                    fiscalDocumentNumber,
                    fiscalDocumentRevision,
                    fiscalDocumentStartTime,
                    positions,
                    payments);
        }
    }
}
