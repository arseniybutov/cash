package ru.crystals.pos.service;

/**
 * @author s.pavlikhin
 */
public class TestExternalServiceSettings implements ExtServiceSettings {

    private int propertyA;
    private String propertyB;

    public int getPropertyA() {
        return propertyA;
    }

    public void setPropertyA(int propertyA) {
        this.propertyA = propertyA;
    }

    public String getPropertyB() {
        return propertyB;
    }

    public void setPropertyB(String propertyB) {
        this.propertyB = propertyB;
    }
}
