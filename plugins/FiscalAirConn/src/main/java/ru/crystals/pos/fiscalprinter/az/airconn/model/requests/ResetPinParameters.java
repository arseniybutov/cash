package ru.crystals.pos.fiscalprinter.az.airconn.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPinParameters {
    String puk;
    String role;
    @JsonProperty("new_pin")
    String newPin;

    public String getPuk() {
        return puk;
    }

    public void setPuk(String puk) {
        this.puk = puk;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNewPin() {
        return newPin;
    }

    public void setNewPin(String newPin) {
        this.newPin = newPin;
    }
}
