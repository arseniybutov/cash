package ru.crystals.pos.visualization.products.mobilepay;

import ru.crystals.pos.visualization.Factory;

public class SummaHelper {

    private Double summa = 0.0;
    private boolean dot = false;
    private Double pow = 1D;
    private Factory factory;
    private Double precision;

    protected Double getMaxValue() {
        return getFactory().getMaxPayment() * 1D;
    }

    public SummaHelper(Factory factory, Double precision) {
        this.setFactory(factory);
        this.precision = precision;
    }

    public void setSumma(Double summa) {
        this.summa = summa;
    }

    public Double getSumma() {
        return summa;
    }

    public void setDot(boolean dot) {
        this.dot = dot;
    }

    public boolean isDot() {
        return dot;
    }

    public void setPow(Double pow) {
        if (pow < precision) {
            this.pow = 0.0;
        } else {
            this.pow = pow;
        }
    }

    public Double getPow() {
        return pow;
    }

    public void number(Byte num) {
        Double x = getSumma();
        if (isDot()) {
            setPow(getPow() / 10);
            x = x + num * getPow();
        } else {
            x = x * 10 + num;
        }
        if (x <= getMaxValue()) {
            setSumma(x);
        }
    }

    public void dot() {
        setDot(true);
    }

    public void reset() {
        setDot(false);
        setSumma(0.0);
        setPow(1D);
    }

    public long getLongSummaX100() {
        return Math.round(summa * 100);
    }

    public long getLongSumma() {
        return Math.round(summa);
    }

    public boolean isSummaAvailable() {
        return summa != 0;
    }

    public boolean isMoneySummaAvailable() {
        return summa >= 0;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public Factory getFactory() {
        return factory;
    }
}
