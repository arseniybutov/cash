package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;

/**
 * Расширенное закрытие чека (аналогично {@link ShtrihReceiptTotal Total(обычному закрытию)}, но с бОльшим числом параметров.
 * В частности, больше типов оплат, необходимо для ФФД 1.00+)
 *
 * @author borisov
 */
public class ShtrihReceiptTotalV2Ex {

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
     * Округление до рубля в копейках (1 байт)
     * Это количество коппек на которое будет уменьшена итоговая сумма,
     * данная функция полезна, если магазин не использует 50 копеечные монеты,
     * тогда при итоге чека 101.58 сделать округление на 58 коппек и сумма в чеке будет 101.00. Значение от 0 до 99.
     */
    private byte roundingSumm;

    /**
     * Сумма налога 1 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     * <p>
     * В режиме начисления налогов 0 ( 1 Таблица) касса рассчитывает налоги
     * самостоятельно исходя из проведенных в документе операций и налоги
     * переданные в команде игнорируются. В режиме начисления налогов 1
     * налоги должны быть обязательно переданы из верхнего ПО.
     */
    private long taxOne;

    /**
     * Сумма налога 2 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long taxTwo;

    /**
     * Сумма налога 3 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long taxThree;

    /**
     * Сумма налога 4 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long taxFour;

    /**
     * Сумма налога 5 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long taxFive;

    /**
     * Сумма налога 6 (5 байт) Значение налога 0…9999999999, в МДЕ (Минимальных Денежных Единицах) - в "копейках"
     */
    private long taxSix;

    /**
     * Код системы налогообложения. Битовое поле (1 байт)
     * <p>
     * 000001 Основная
     * 000010 Упрощенная система налогообложения доход
     * 000100 Упрощенная система налогообложения доход минус расход
     * 001000 Единый налог на вмененный доход
     * 010000 Единый сельскохозяйственный налог
     * 100000 Патентная система налогообложения
     */
    private byte taxSystem;

    /**
     * Чек
     */
    private final Check check;
    /**
     * Некое описание этого чека
     */
    private String text;

    /**
     * Удобный конструктор.
     *
     * @param text    Некое описание этого чека
     * @param cashSum Сумма наличных оплат в этом чеке, в "копейках"
     */
    public ShtrihReceiptTotalV2Ex(Check check, String text, long cashSum) {
        this.check = check;
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

    public byte getRoundingSumm() {
        return roundingSumm;
    }

    public void setRoundingSumm(byte roundingSumm) {
        this.roundingSumm = roundingSumm;
    }

    public long getTaxOne() {
        return taxOne;
    }

    public void setTaxOne(long taxOne) {
        this.taxOne = taxOne;
    }

    public long getTaxTwo() {
        return taxTwo;
    }

    public void setTaxTwo(long taxTwo) {
        this.taxTwo = taxTwo;
    }

    public long getTaxThree() {
        return taxThree;
    }

    public void setTaxThree(long taxThree) {
        this.taxThree = taxThree;
    }

    public long getTaxFour() {
        return taxFour;
    }

    public void setTaxFour(long taxFour) {
        this.taxFour = taxFour;
    }

    public long getTaxFive() {
        return taxFive;
    }

    public void setTaxFive(long taxFive) {
        this.taxFive = taxFive;
    }

    public long getTaxSix() {
        return taxSix;
    }

    public void setTaxSix(long taxSix) {
        this.taxSix = taxSix;
    }

    public byte getTaxSystem() {
        return taxSystem;
    }

    public void setTaxSystem(byte taxSystem) {
        this.taxSystem = taxSystem;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Check getCheck() {
        return check;
    }
}