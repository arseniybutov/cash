package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationError {
    @JsonProperty("errorLevel")
    private ErrorLevel errorLevel;

    @JsonProperty("errorText")
    private String errorText;

    @JsonProperty("errorSource")
    private String errorSource;


    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(final ErrorLevel errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(final String errorText) {
        this.errorText = errorText;
    }

    public String getErrorSource() {
        return errorSource;
    }

    public void setErrorSource(final String errorSource) {
        this.errorSource = errorSource;
    }
}

