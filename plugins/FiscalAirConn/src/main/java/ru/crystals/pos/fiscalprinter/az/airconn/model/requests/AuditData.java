package ru.crystals.pos.fiscalprinter.az.airconn.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditData {
    @JsonProperty("access_token")
    String accessToken;
    String author;
    String status = "SUCCESS";
    String details = "Verification completed successfully.";

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
