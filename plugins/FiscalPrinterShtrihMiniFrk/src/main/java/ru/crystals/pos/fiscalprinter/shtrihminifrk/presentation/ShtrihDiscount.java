package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Представление скидки с точки зрения Штриха.
 * 
 * @author aperevozchikov
 */
public class ShtrihDiscount {

    /**
     * сумма скидки. в МДЕ (минимальных денежных единицах)
     */
    private long sum;

    /**
     * Налог 1 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxOne;

    /**
     * Налог 2 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxTwo;

    /**
     * Налог 3 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxThree;

    /**
     * Налог 4 (1 байт) «0» – нет, «1»…«4» – налоговая группа
     */
    private byte taxFour;

    /**
     * Текст - описание этой скидки
     */
    private String text;

    /**
     * Удобный конструктор.
     * 
     * @param sum
     *            сумма скидки. в "копейках"
     * @param text
     *            описание скидкиЩ
     */
    public ShtrihDiscount(long sum, String text) {
        this.sum = sum;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("shtrih-discount [sum: %s; desc: \"%s\"]", getSum(), getText());
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public byte getTaxOne() {
        return taxOne;
    }

    public void setTaxOne(byte taxOne) {
        this.taxOne = taxOne;
    }

    public byte getTaxTwo() {
        return taxTwo;
    }

    public void setTaxTwo(byte taxTwo) {
        this.taxTwo = taxTwo;
    }

    public byte getTaxThree() {
        return taxThree;
    }

    public void setTaxThree(byte taxThree) {
        this.taxThree = taxThree;
    }

    public byte getTaxFour() {
        return taxFour;
    }

    public void setTaxFour(byte taxFour) {
        this.taxFour = taxFour;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
