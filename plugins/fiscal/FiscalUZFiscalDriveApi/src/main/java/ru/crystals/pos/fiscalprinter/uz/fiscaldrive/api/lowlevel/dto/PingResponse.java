package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PingResponse {

    @JsonProperty("Message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PingResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
