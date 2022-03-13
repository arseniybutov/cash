package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuantityUnit {
    @JsonProperty("id")
    private String id;

    public QuantityUnit(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

