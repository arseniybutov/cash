package ru.crystals.pos.bank.emulator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationDataProperties {
    @JsonProperty("status")
    private boolean status = true;

    @JsonProperty("message")
    private String message = "УСПЕШНО";

    @JsonProperty("resultCode")
    private long resultCode = 333L;

    @JsonProperty("responseCode")
    private String responseCode = "555";

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean value) {
        status = value;
    }

    public void setMessage(String value) {
        message = value;
    }

    public String getMessage() {
        return message;
    }

    public void setResultCode(long value) {
        resultCode = value;
    }

    public long getResultCode() {
        return resultCode;
    }

    public void setResponseCode(String value) {
        responseCode = value;
    }

    public String getResponseCode() {
        return responseCode;
    }
}
