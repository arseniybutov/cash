package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class DocumentPositionText {

    @JsonUnwrapped
    private DocumentPositionBase common;

    @JsonProperty("text")
    private String text;

    public DocumentPositionBase getCommon() {
        return common;
    }

    public void setCommon(final DocumentPositionBase common) {
        this.common = common;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}

