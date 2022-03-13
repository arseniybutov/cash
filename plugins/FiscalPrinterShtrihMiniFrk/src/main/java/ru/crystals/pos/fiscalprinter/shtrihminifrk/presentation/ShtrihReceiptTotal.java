package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Total чека (так понимаю это просто сумма по {@link ShtrihPosition позициям} чека.)
 * 
 * @author aperevozchikov
 */
public class ShtrihReceiptTotal {

    /**
     * Сумма наличных, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long cashSum;
    
    /**
     * Сумма типа оплаты 2, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long secondPaymentTypeSum;
    
    /**
     * Сумма типа оплаты 3, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long thirdPaymentTypeSum;
    
    /**
     * Сумма типа оплаты 4, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long fourthPaymentTypeSum;
    
    /**
     * Количество сотых долей процента скидки.
     * <p/>
     * Скидка/Надбавка(в случае отрицательного значения) в % на чек от 0 до 99,99 % -9999…9999
     */
    private long discountPercent;

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
     * Некое описание этого чека
     */
    private String text;
    
    public ShtrihReceiptTotal() {
    }
    
    /**
     * Удобный конструктор.
     * 
     * @param text
     *            Некое описание этого чека
     * @param cashSum
     *            Сумма наличных оплат в этом чеке, в "копейках"
     */
    public ShtrihReceiptTotal(String text, long cashSum) {
        this.text = text;
        this.cashSum = cashSum;
    }
    
    @Override
    public String toString() {
        return String.format("receipt-total [text: \"%s\", cash: %s; pament2: %s; pament3: %s; pament4: %s]", 
            getTaxFour(), getCashSum(), getSecondPaymentTypeSum(), getThirdPaymentTypeSum(), getFourthPaymentTypeSum());
    }

    public long getCashSum() {
        return cashSum;
    }

    public void setCashSum(long cashSum) {
        this.cashSum = cashSum;
    }

    public long getSecondPaymentTypeSum() {
        return secondPaymentTypeSum;
    }

    public void setSecondPaymentTypeSum(long secondPaymentTypeSum) {
        this.secondPaymentTypeSum = secondPaymentTypeSum;
    }

    public long getThirdPaymentTypeSum() {
        return thirdPaymentTypeSum;
    }

    public void setThirdPaymentTypeSum(long thirdPaymentTypeSum) {
        this.thirdPaymentTypeSum = thirdPaymentTypeSum;
    }

    public long getFourthPaymentTypeSum() {
        return fourthPaymentTypeSum;
    }

    public void setFourthPaymentTypeSum(long fourthPaymentTypeSum) {
        this.fourthPaymentTypeSum = fourthPaymentTypeSum;
    }

    public long getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(long discountPercent) {
        this.discountPercent = discountPercent;
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