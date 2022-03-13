package ru.crystals.pos.scale.massak.protocol2;

public enum Measure {
    GRAM(1), TENTH_OF_GRAM(10);
    private int measureDivider;

    Measure(int measureDivider) {
        this.measureDivider = measureDivider;
    }

    public int getMeasureDivider() {
        return measureDivider;
    }
}
