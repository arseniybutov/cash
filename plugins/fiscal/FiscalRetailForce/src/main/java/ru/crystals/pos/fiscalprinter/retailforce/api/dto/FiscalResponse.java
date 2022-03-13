package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class FiscalResponse {
    @JsonProperty("fiscalCountry")
    private FiscalCountry fiscalCountry;

    @JsonProperty("fiscalisationDocumentNumber")
    private int fiscalisationDocumentNumber;

    @JsonProperty("fiscalisationDocumentRevision")
    private int fiscalisationDocumentRevision;

    @JsonProperty("fiscalDocumentStartTime")
    private Long fiscalDocumentStartTime;

    @JsonProperty("errorDescription")
    private String errorDescription;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("userMessage")
    private String userMessage;

    @JsonProperty("requestTime")
    private OffsetDateTime requestTime;

    @JsonProperty("requestCompletionTime")
    private OffsetDateTime requestCompletionTime;

    @JsonProperty("AdditionalFields")
    private FiscalResponseAdditionalFields additionalFields;

    public FiscalResponse() {
    }

    FiscalResponse(FiscalCountry fiscalCountry,
                   int fiscalisationDocumentNumber,
                   int fiscalisationDocumentRevision,
                   Long fiscalDocumentStartTime,
                   String errorDescription,
                   String signature,
                   String userMessage,
                   OffsetDateTime requestTime,
                   OffsetDateTime requestCompletionTime,
                   FiscalResponseAdditionalFields additionalFields) {
        this.fiscalCountry = fiscalCountry;
        this.fiscalisationDocumentNumber = fiscalisationDocumentNumber;
        this.fiscalisationDocumentRevision = fiscalisationDocumentRevision;
        this.fiscalDocumentStartTime = fiscalDocumentStartTime;
        this.errorDescription = errorDescription;
        this.signature = signature;
        this.userMessage = userMessage;
        this.requestTime = requestTime;
        this.requestCompletionTime = requestCompletionTime;
        this.additionalFields = additionalFields;
    }

    public FiscalCountry getFiscalCountry() {
        return fiscalCountry;
    }

    public void setFiscalCountry(final FiscalCountry fiscalCountry) {
        this.fiscalCountry = fiscalCountry;
    }

    public int getFiscalisationDocumentNumber() {
        return fiscalisationDocumentNumber;
    }

    public void setFiscalisationDocumentNumber(final int fiscalisationDocumentNumber) {
        this.fiscalisationDocumentNumber = fiscalisationDocumentNumber;
    }

    public int getFiscalisationDocumentRevision() {
        return fiscalisationDocumentRevision;
    }

    public void setFiscalisationDocumentRevision(final int fiscalisationDocumentRevision) {
        this.fiscalisationDocumentRevision = fiscalisationDocumentRevision;
    }

    public Long getFiscalDocumentStartTime() {
        return fiscalDocumentStartTime;
    }

    public void setFiscalDocumentStartTime(final Long fiscalDocumentStartTime) {
        this.fiscalDocumentStartTime = fiscalDocumentStartTime;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(final String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(final String userMessage) {
        this.userMessage = userMessage;
    }

    public OffsetDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(final OffsetDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public OffsetDateTime getRequestCompletionTime() {
        return requestCompletionTime;
    }

    public void setRequestCompletionTime(final OffsetDateTime requestCompletionTime) {
        this.requestCompletionTime = requestCompletionTime;
    }

    public FiscalResponseAdditionalFields getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(final FiscalResponseAdditionalFields additionalFields) {
        this.additionalFields = additionalFields;
    }

    public static FiscalResponseBuilder builder() {
        return new FiscalResponseBuilder();
    }

    public static class FiscalResponseBuilder {
        private FiscalCountry fiscalCountry;
        private int fiscalisationDocumentNumber;
        private int fiscalisationDocumentRevision;
        private Long fiscalDocumentStartTime;
        private String errorDescription;
        private String signature;
        private String userMessage;
        private OffsetDateTime requestTime;
        private OffsetDateTime requestCompletionTime;
        private FiscalResponseAdditionalFields additionalFields;

        FiscalResponseBuilder() {
        }

        public FiscalResponseBuilder fiscalCountry(FiscalCountry fiscalCountry) {
            this.fiscalCountry = fiscalCountry;
            return this;
        }

        public FiscalResponseBuilder fiscalisationDocumentNumber(int fiscalisationDocumentNumber) {
            this.fiscalisationDocumentNumber = fiscalisationDocumentNumber;
            return this;
        }

        public FiscalResponseBuilder fiscalisationDocumentRevision(int fiscalisationDocumentRevision) {
            this.fiscalisationDocumentRevision = fiscalisationDocumentRevision;
            return this;
        }

        public FiscalResponseBuilder fiscalDocumentStartTime(Long fiscalDocumentStartTime) {
            this.fiscalDocumentStartTime = fiscalDocumentStartTime;
            return this;
        }

        public FiscalResponseBuilder errorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }

        public FiscalResponseBuilder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public FiscalResponseBuilder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public FiscalResponseBuilder requestTime(OffsetDateTime requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public FiscalResponseBuilder requestCompletionTime(OffsetDateTime requestCompletionTime) {
            this.requestCompletionTime = requestCompletionTime;
            return this;
        }

        public FiscalResponseBuilder additionalFields(FiscalResponseAdditionalFields additionalFields) {
            this.additionalFields = additionalFields;
            return this;
        }

        public FiscalResponse build() {
            return new FiscalResponse(fiscalCountry,
                    fiscalisationDocumentNumber,
                    fiscalisationDocumentRevision,
                    fiscalDocumentStartTime,
                    errorDescription,
                    signature,
                    userMessage,
                    requestTime,
                    requestCompletionTime,
                    additionalFields);
        }
    }
}

