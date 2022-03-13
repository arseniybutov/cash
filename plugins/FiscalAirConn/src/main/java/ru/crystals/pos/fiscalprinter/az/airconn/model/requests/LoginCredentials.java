package ru.crystals.pos.fiscalprinter.az.airconn.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginCredentials {
    String pin;
    String role;
    @JsonProperty("cashregister_factory_number")
    String cashRegisterFactoryNumber;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCashRegisterFactoryNumber() {
        return cashRegisterFactoryNumber;
    }

    public void setCashRegisterFactoryNumber(String cashregisterFactoryNumber) {
        this.cashRegisterFactoryNumber = cashregisterFactoryNumber;
    }
}
