package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

/**
 * Данные команды печати чека коррекции.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmCorrection {
    @XmlAttribute(name = "n")
    private String name = "31";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Кассир
     */
    @XmlElement(name = "pa")
    private RequestElement userLoginName = new RequestElement("1021", KKTDataType.STRING, "");
    @XmlElement(name = "pa")
    private RequestElement cashSumm = new RequestElement("1031", KKTDataType.CURRENCY, "");
    /**
     * Признак расчета: 1 – приход; 3 – расход;
     */
    @XmlElement(name = "pa")
    private RequestElement receiptType = new RequestElement("1054", KKTDataType.UINT, "");
    @XmlElement(name = "pa")
    private RequestElement taxSystem = new RequestElement("1055", KKTDataType.UINT, "");
    @XmlElement(name = "pa")
    private RequestElement nonCashSumm = new RequestElement("1081", KKTDataType.CURRENCY, "0.00");
    /**
     * Сумма НДС чека
     */
    @XmlElement(name = "pa")
    private RequestElement tax18 = new RequestElement("1102", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement tax10 = new RequestElement("1103", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement tax0 = new RequestElement("1104", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement taxNon = new RequestElement("1105", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement tax118 = new RequestElement("1106", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement tax110 = new RequestElement("1107", KKTDataType.CURRENCY, "");
    @XmlElement(name = "pa")
    private RequestElement correctionType = new RequestElement("1073", KKTDataType.UINT, "");
    @XmlElement(name = "pa")
    private ArmCorrectionReason correctionReason;
    /**
     * ИНН кассира
     */
    @XmlElement(name = "pa")
    private RequestElement userINN = new RequestElement("1203", KKTDataType.STRING, "");
    /**
     * Сумма по чеку предоплатой
     */
    @XmlElement(name = "pa")
    private RequestElement prepaymentSumm = new RequestElement("1215", KKTDataType.CURRENCY, "0.00");
    /**
     * Сумма по чеку постоплатой
     */
    @XmlElement(name = "pa")
    private RequestElement postpaymentSumm = new RequestElement("1216", KKTDataType.CURRENCY, "0.00");
    /**
     * Сумма по чеку встречным предоставлением
     */
    @XmlElement(name = "pa")
    private RequestElement oncomingSumm = new RequestElement("1217", KKTDataType.CURRENCY, "0.00");

    public String getUserLoginName() {
        return (String) userLoginName.getValue();
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName.setValue(userLoginName);
    }

    public BigDecimal getCashSumm() {
        return cashSumm.getFloatValue();
    }

    public void setCashSumm(BigDecimal cashSumm) {
        this.cashSumm.setFloatValue(UtilsSP.roundBigDecimal(cashSumm, KKTDataType.CURRENCY));
    }

    public int getReceiptType() {
        return receiptType.getIntValue();
    }

    public void setReceiptType(int receiptType) {
        this.receiptType.setIntValue(receiptType);
    }

    public int getTaxSystem() {
        return taxSystem.getIntValue();
    }

    public void setTaxSystem(int taxSystem) {
        this.taxSystem.setIntValue(taxSystem);
    }

    public BigDecimal getNonCashSumm() {
        return nonCashSumm.getFloatValue();
    }

    public void setNonCashSumm(BigDecimal nonCashSumm) {
        this.nonCashSumm.setFloatValue(UtilsSP.roundBigDecimal(nonCashSumm, KKTDataType.CURRENCY));
    }

    public BigDecimal getTax18() {
        return tax18.getFloatValue();
    }

    public void setTax18(BigDecimal tax18) {
        this.tax18.setFloatValue(UtilsSP.roundBigDecimal(tax18, KKTDataType.CURRENCY));
    }

    public BigDecimal getTax10() {
        return tax10.getFloatValue();
    }

    public void setTax10(BigDecimal tax10) {
        this.tax10.setFloatValue(UtilsSP.roundBigDecimal(tax10, KKTDataType.CURRENCY));
    }

    public BigDecimal getTax0() {
        return tax0.getFloatValue();
    }

    public void setTax0(BigDecimal tax0) {
        this.tax0.setFloatValue(UtilsSP.roundBigDecimal(tax0, KKTDataType.CURRENCY));
    }

    public BigDecimal getTaxNon() {
        return taxNon.getFloatValue();
    }

    public void setTaxNon(BigDecimal taxNon) {
        this.taxNon.setFloatValue(UtilsSP.roundBigDecimal(taxNon, KKTDataType.CURRENCY));
    }

    public BigDecimal getTax118() {
        return tax118.getFloatValue();
    }

    public void setTax118(BigDecimal tax118) {
        this.tax118.setFloatValue(UtilsSP.roundBigDecimal(tax118, KKTDataType.CURRENCY));
    }

    public BigDecimal getTax110() {
        return tax110.getFloatValue();
    }

    public void setTax110(BigDecimal tax110) {
        this.tax110.setFloatValue(UtilsSP.roundBigDecimal(tax110, KKTDataType.CURRENCY));
    }

    public int getCorrectionType() {
        return correctionType.getIntValue();
    }

    public void setCorrectionType(int correctionType) {
        this.correctionType.setIntValue(correctionType);
    }

    public ArmCorrectionReason getCorrectionReason() {
        return correctionReason;
    }

    public void setCorrectionReason(ArmCorrectionReason correctionReason) {
        this.correctionReason = correctionReason;
    }

    public String getUserINN() {
        return (String) userINN.getValue();
    }

    public void setUserINN(String userINN) {
        this.userINN.setValue(userINN);
    }

    public BigDecimal getPrepaymentSumm() {
        return prepaymentSumm.getFloatValue();
    }

    public void setPrepaymentSumm(BigDecimal prepaymentSumm) {
        this.prepaymentSumm.setFloatValue(UtilsSP.roundBigDecimal(prepaymentSumm, KKTDataType.CURRENCY));
    }

    public BigDecimal getPostpaymentSumm() {
        return postpaymentSumm.getFloatValue();
    }

    public void setPostpaymentSumm(BigDecimal postpaymentSumm) {
        this.postpaymentSumm.setFloatValue(UtilsSP.roundBigDecimal(postpaymentSumm, KKTDataType.CURRENCY));
    }

    public BigDecimal getOncomingSumm() {
        return oncomingSumm.getFloatValue();
    }

    public void setOncomingSumm(BigDecimal oncomingSumm) {
        this.oncomingSumm.setFloatValue(UtilsSP.roundBigDecimal(oncomingSumm, KKTDataType.CURRENCY));
    }
}
