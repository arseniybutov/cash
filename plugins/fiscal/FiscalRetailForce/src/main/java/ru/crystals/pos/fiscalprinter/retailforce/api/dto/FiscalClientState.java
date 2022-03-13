package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FiscalClientState {

    @JsonProperty("notInitialized")
    NOTINITIALIZED,

    @JsonProperty("initialized")
    INITIALIZED,

    @JsonProperty("decommissioned")
    DECOMMISSIONED

}

