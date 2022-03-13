package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Расширенное закрытие чека (аналогично {@link ShtrihReceiptTotal Total(обычному закрытию)}, но с бОльшим числом параметров.
 *
 * @author borisov
 */
public class ShtrihReceiptTotalEx {

    /**
     * Сумма наличных, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * в ОФД - НАЛИЧНЫЕ
     */
    private long summ1;

    /**
     * Сумма типа оплаты 2, в МДЕ (Минимальных Денежных Единицах) - в "копейках".
     * в ОФД - ЭЛЕКТРОННЫМИ
     */
    private long summ2;

    /**
     * Сумма типа оплаты 3, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * в ОФД - ЭЛЕКТРОННЫМИ
     */
    private long summ3;

    /**
     * Сумма типа оплаты 4, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * в ОФД - ЭЛЕКТРОННЫМИ
     */
    private long summ4;

    /**
     * Сумма типа оплаты 5, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ5;

    /**
     * Сумма типа оплаты 6, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ6;

    /**
     * Сумма типа оплаты 7, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ7;

    /**
     * Сумма типа оплаты 8, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ8;

    /**
     * Сумма типа оплаты 9, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ9;

    /**
     * Сумма типа оплаты 10, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ10;

    /**
     * Сумма типа оплаты 11, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ11;

    /**
     * Сумма типа оплаты 12, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ12;

    /**
     * Сумма типа оплаты 13, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * РАСШИРЕННАЯ ОПЛАТА
     */
    private long summ13;

    /**
     * Сумма типа оплаты 14, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * для указания суммы предварительной оплаты (аванс)
     */
    private long summ14;

    /**
     * Сумма типа оплаты 15, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * для суммы последующей оплаты (кредит)
     */
    private long summ15;

    /**
     * Сумма типа оплаты 16, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * для иной формы оплаты (оплата встречным предоставлением)
     */
    private long summ16;

    /**
     * Количество сотых долей процента скидки.
     * <p>
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

    public ShtrihReceiptTotalEx() {
    }

    /**
     * Удобный конструктор.
     *
     * @param text    Некое описание этого чека
     * @param cashSum Сумма наличных оплат в этом чеке, в "копейках"
     */
    public ShtrihReceiptTotalEx(String text, long cashSum) {
        this.text = text;
        this.summ1 = cashSum;
    }

    @Override
    public String toString() {
        return String.format("receipt-total [text: \"%s\", cash: %s; pament2: %s; pament3: %s; pament4: %s; pament14: %s; pament15: %s; pament16: %s]",
                getTaxFour(), getSumm1(), getSumm2(), getSumm3(), getSumm4(), getSumm14(), getSumm15(), getSumm16());
    }

    public long getSumm1() {
        return summ1;
    }

    public void setSumm1(long summ1) {
        this.summ1 = summ1;
    }

    public long getSumm2() {
        return summ2;
    }

    public void setSumm2(long summ2) {
        this.summ2 = summ2;
    }

    public long getSumm3() {
        return summ3;
    }

    public void setSumm3(long summ3) {
        this.summ3 = summ3;
    }

    public long getSumm4() {
        return summ4;
    }

    public void setSumm4(long summ4) {
        this.summ4 = summ4;
    }

    public long getSumm5() {
        return summ5;
    }

    public void setSumm5(long summ5) {
        this.summ5 = summ5;
    }

    public long getSumm6() {
        return summ6;
    }

    public void setSumm6(long summ6) {
        this.summ6 = summ6;
    }

    public long getSumm7() {
        return summ7;
    }

    public void setSumm7(long summ7) {
        this.summ7 = summ7;
    }

    public long getSumm8() {
        return summ8;
    }

    public void setSumm8(long summ8) {
        this.summ8 = summ8;
    }

    public long getSumm9() {
        return summ9;
    }

    public void setSumm9(long summ9) {
        this.summ9 = summ9;
    }

    public long getSumm10() {
        return summ10;
    }

    public void setSumm10(long summ10) {
        this.summ10 = summ10;
    }

    public long getSumm11() {
        return summ11;
    }

    public void setSumm11(long summ11) {
        this.summ11 = summ11;
    }

    public long getSumm12() {
        return summ12;
    }

    public void setSumm12(long summ12) {
        this.summ12 = summ12;
    }

    public long getSumm13() {
        return summ13;
    }

    public void setSumm13(long summ13) {
        this.summ13 = summ13;
    }

    public long getSumm14() {
        return summ14;
    }

    public void setSumm14(long summ14) {
        this.summ14 = summ14;
    }

    public long getSumm15() {
        return summ15;
    }

    public void setSumm15(long summ15) {
        this.summ15 = summ15;
    }

    public long getSumm16() {
        return summ16;
    }

    public void setSumm16(long summ16) {
        this.summ16 = summ16;
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