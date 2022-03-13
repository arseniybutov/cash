package ru.crystals.pos.fiscalprinter.nfd;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NFDConfig {

    @JsonProperty("ofdUri")
    private String ofdUri;

    @JsonProperty("nfdUri")
    private String nfdUri;

    @JsonProperty("useRounding")
    private boolean useRounding;

    @JsonProperty("token")
    private String token;

    public String getNfdUri() {
        return nfdUri;
    }

    public void setNfdUri(String nfdUri) {
        this.nfdUri = nfdUri;
    }

    public boolean isUseRounding() {
        return useRounding;
    }

    public void setUseRounding(boolean useRounding) {
        this.useRounding = useRounding;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
