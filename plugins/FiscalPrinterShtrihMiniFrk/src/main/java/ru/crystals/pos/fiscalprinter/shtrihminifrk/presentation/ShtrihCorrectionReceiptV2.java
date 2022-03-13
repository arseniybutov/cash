package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.util.Set;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.CorrectionReceiptPaymentsEntity;
import ru.crystals.pos.check.CorrectionReceiptTaxesEntity;
import ru.crystals.pos.check.correction.AccountSign;
import ru.crystals.pos.check.correction.CorrectionReceiptType;
import ru.crystals.pos.check.correction.TaxSystem;
import ru.crystals.pos.check.correction.TaxesTypes;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;

/**
 * Сформировать чек коррекции V2
 *
 * @author borisov
 */
public class ShtrihCorrectionReceiptV2 {

    /**
     * Тип коррекции 1 байт, диапазон: 0-1 (0 - "Самостоятельно", 1 - "По предписанию")
     */
    private byte correctionType;

    /**
     * Признак расчета: 1 байт, диапазон: 1;3 (1 - Коррекция прихода, 3 - Коррекция расхода)
     */
    private byte calculationSign;

    /**
     * Сумма всех оплат в чеке
     */
    private long sumAll;

    /**
     * Сумма Наличными - в "копейках"
     */
    private long sumCash;

    /**
     * Сумма Электронными - в "копейках"
     */
    private long sumElectron;

    /**
     * Сумма Предоплатой - в "копейках"
     */
    private long sumPrepayment;

    /**
     * Сумма Постоплатой - в "копейках"
     */
    private long sumPostpay;

    /**
     * Сумма Встречным представлением - в "копейках"
     */
    private long sumCounteroffer;

    /**
     * Сумма НДС 18% - в "копейках"
     */
    private long taxOne;

    /**
     * Сумма НДС 10% - в "копейках"
     */
    private long taxTwo;

    /**
     * Сумма расчета по ставке 0% - в "копейках"
     */
    private long taxThree;

    /**
     * Сумма расчета по чеку без НДС - в "копейках"
     */
    private long taxFour;

    /**
     * Сумма расчета по чеку 18/118 - в "копейках"
     */
    private long taxFive;

    /**
     * Сумма расчета по расч. ставке 10/110 - в "копейках"
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


    public ShtrihCorrectionReceiptV2() {
    }

    /**
     * Удобный конструктор.
     *
     * @param correctionReceipt Сущность описывающая чек коррекции
     */
    public ShtrihCorrectionReceiptV2(CorrectionReceiptEntity correctionReceipt) throws ShtrihException {
        setCorrectionType(correctionReceipt.getCorrectionType());
        setCalculationSign(correctionReceipt.getAccountSign());
        setPayment(correctionReceipt.getPayments());
        setTaxes(correctionReceipt.getTaxes());
        setTaxSystem(correctionReceipt.getTaxSystem());
    }

    private void setTaxSystem(TaxSystem taxSystem) throws ShtrihException {
        switch (taxSystem) {
            case COMMON:
                setTaxSystem((byte) 1);
                break;
            case SIMPLIFIED_INCOME:
                setTaxSystem((byte) 2);
                break;
            case SIMPLIFIED_INCOME_MINUS_EXPENSE:
                setTaxSystem((byte) 4);
                break;
            case UNIFIED:
                setTaxSystem((byte) 8);
                break;
            case UNIFIED_AGRICULTURAL:
                setTaxSystem((byte) 16);
                break;
            case PATENT:
                setTaxSystem((byte) 32);
                break;
            default:
                throw new ShtrihException("Unknown tax type!");
        }
    }

    private void setTaxes(Set<CorrectionReceiptTaxesEntity> taxes) throws ShtrihException {
        for (CorrectionReceiptTaxesEntity tax : taxes) {
            switch (TaxesTypes.getByName(tax.getCorrectionReceiptTaxesEntityPK().getTax().getName())) {
                case TAX_20:
                    setTaxOne(tax.getTaxSum());
                    break;
                case TAX_10:
                    setTaxTwo(tax.getTaxSum());
                    break;
                case TAX_20_120:
                    setTaxFive(tax.getTaxSum());
                    break;
                case TAX_10_110:
                    setTaxSix(tax.getTaxSum());
                    break;
                case TAX_0:
                    setTaxThree(tax.getTaxSum());
                    break;
                case TAX_NONDS:
                    setTaxFour(tax.getTaxSum());
                    break;
                default:
                    throw new ShtrihException("Unknown tax type!");
            }
        }
    }

    private void setPayment(Set<CorrectionReceiptPaymentsEntity> payments) throws ShtrihException {
        for (CorrectionReceiptPaymentsEntity payment : payments) {
            switch (payment.getCorrectionReceiptPaymentsEntityPK().getPaymentName()) {
                case CASH:
                    setSumCash(payment.getPaymentSum());
                    break;
                case ELECTRON:
                    setSumElectron(payment.getPaymentSum());
                    break;
                case PREPAYMENT:
                    setSumPrepayment(payment.getPaymentSum());
                    break;
                case POSTPAY:
                    setSumPostpay(payment.getPaymentSum());
                    break;
                case COUNTEROFFER:
                    setSumCounteroffer(payment.getPaymentSum());
                    break;
                default:
                    throw new ShtrihException("Unknown payment type!");
            }
        }
        setSumAll(getSumCash() + getSumElectron() + getSumPrepayment() + getSumPostpay() + getSumCounteroffer());
    }

    private void setCalculationSign(AccountSign accountSign) throws ShtrihException {
        switch (accountSign) {
            case RECEIPT:
                setCalculationSign((byte) 1);
                break;
            case SPENDING:
                setCalculationSign((byte) 3);
                break;
            default:
                throw new ShtrihException("Unknown account sign!");
        }
    }

    private void setCorrectionType(CorrectionReceiptType correctionType) throws ShtrihException {
        switch (correctionType) {
            case INDEPENDENTLY:
                setCorrectionType((byte) 0);
                break;
            case ORDER:
                setCorrectionType((byte) 1);
                break;
            default:
                throw new ShtrihException("Unknown correction type!");
        }
    }

    public byte getCorrectionType() {
        return correctionType;
    }

    public void setCorrectionType(byte correctionType) {
        this.correctionType = correctionType;
    }

    public byte getCalculationSign() {
        return calculationSign;
    }

    public void setCalculationSign(byte calculationSign) {
        this.calculationSign = calculationSign;
    }

    public long getSumAll() {
        return sumAll;
    }

    public void setSumAll(long sumAll) {
        this.sumAll = sumAll;
    }

    public long getSumCash() {
        return sumCash;
    }

    public void setSumCash(long sumCash) {
        this.sumCash = sumCash;
    }

    public long getSumElectron() {
        return sumElectron;
    }

    public void setSumElectron(long sumElectron) {
        this.sumElectron = sumElectron;
    }

    public long getSumPrepayment() {
        return sumPrepayment;
    }

    public void setSumPrepayment(long sumPrepayment) {
        this.sumPrepayment = sumPrepayment;
    }

    public long getSumPostpay() {
        return sumPostpay;
    }

    public void setSumPostpay(long sumPostpay) {
        this.sumPostpay = sumPostpay;
    }

    public long getSumCounteroffer() {
        return sumCounteroffer;
    }

    public void setSumCounteroffer(long sumCounteroffer) {
        this.sumCounteroffer = sumCounteroffer;
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

    @Override
    public String toString() {
        return String.format("correction-receipt [sumAll: \"%s\", sumCash: %s; sumElectron: %s; sumPostpay: %s; sumPrepayment: %s; sumCounteroffer: %s; correctionType: %s; calculationSign: %s]",
                sumAll, sumCash, sumElectron, sumPostpay, sumPrepayment, sumCounteroffer, correctionType, calculationSign);
    }
}