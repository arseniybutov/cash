package ru.crystals.pos.fiscalprinter.cashboxsystem.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CBSConfig {

    @JsonProperty("kkmId")
    private String kkmId;

    @JsonProperty("token")
    private String token;

    @JsonProperty("pseudoOffline")
    private boolean pseudoOffline;

    @JsonProperty("useRounding")
    private boolean useRounding;

    public String getKkmId() {
        return kkmId;
    }

    public void setKkmId(String kkmId) {
        this.kkmId = kkmId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isPseudoOffline() {
        return pseudoOffline;
    }

    public void setPseudoOffline(boolean pseudoOffline) {
        this.pseudoOffline = pseudoOffline;
    }

    public boolean isUseRounding() {
        return useRounding;
    }

    public void setUseRounding(boolean useRounding) {
        this.useRounding = useRounding;
    }
}
