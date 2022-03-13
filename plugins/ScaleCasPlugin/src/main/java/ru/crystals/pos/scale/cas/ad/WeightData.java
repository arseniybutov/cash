package ru.crystals.pos.scale.cas.ad;

public class WeightData {
    private String weight;
    private String unit;
    private boolean stable;
    private boolean overload;
    private boolean negativeWeight;

    @Override
    public String toString() {
        return new StringBuilder()
        .append("weight =").append(weight)
        .append(" | unit = ").append(unit)
        .append(" | stable = ").append(stable)
        .append(" | overload = ").append(overload)
        .append(" | negativeWeight = ").append(negativeWeight).toString();
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public boolean isNegativeWeight() {
        return negativeWeight;
    }

    public void setNegativeWeight(boolean negativeWeight) {
        this.negativeWeight = negativeWeight;
    }

    public boolean isOverload() {
        return overload;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
