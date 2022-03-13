package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains;

import java.math.BigDecimal;

public class GasOilDomain extends CommonDomain {

    /***
     * Номер.
     * */
    private String correctionNumberFieldName = "correctionNumber";

    /***
     * Сумма.
     * */
    private String correctionSumFieldName = "correctionSum";

    /***
     * Номер карты.
     * */
    private String cardNumberFieldName = "cardNumber";

    public GasOilDomain(String correctionNumberFieldName, String correctionSumFieldName, String cardNumberFieldName) {
        type = "emul:GasOilDomain";
        this.correctionNumberFieldName = correctionNumberFieldName;
        this.correctionSumFieldName = correctionSumFieldName;
        this.cardNumberFieldName = cardNumberFieldName;
    }

    public String getCorrectionNumber() {
        return (String) getParam(correctionNumberFieldName);
    }

    public void setCorrectionNumber(String correctionNumber) {
        addParam(correctionNumberFieldName, correctionNumber);
    }

    public BigDecimal getCorrectionSum() {
        return (BigDecimal) getParam(correctionSumFieldName);
    }

    public void setCorrectionSum(BigDecimal correctionSum) {
        addParam(correctionSumFieldName, correctionSum);
    }

    public String getCardNumber() {
        return (String) getParam(cardNumberFieldName);
    }

    public void setCardNumber(String cardNumber) {
        addParam(cardNumberFieldName, cardNumber);
    }

}
