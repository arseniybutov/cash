package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport;

public class RequestProperty {
    private String propertyName;
    private String propertyValue;

    public RequestProperty(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
