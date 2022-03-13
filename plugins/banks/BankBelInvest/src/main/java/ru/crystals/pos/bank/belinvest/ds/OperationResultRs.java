package ru.crystals.pos.bank.belinvest.ds;

import ru.crystals.pos.bank.belinvest.ds.adapters.DateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

/**
 * Created by Tatarinov Eduard on 17.11.16.
 */
@XmlRootElement(name = "OperationResultRs")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationResultRs {
    /**
     * код возврата (0 - успех )
     */
    @XmlElement(name = "ResultCode")
    private Long resultCode;
    /**
     * описание результата
     */
    @XmlElement(name = "ResultText")
    private String resultText;
    /**
     * чек
     */
    @XmlElement(name = "ChequeText")
    private String chequeText;
    @XmlElement(name = "ChequeCount")
    private Long chequeCount;
    /**
     * оригинальный код при операциях оплаты
     */
    @XmlElement(name = "OriginalCode")
    private String originalCode;
    @XmlElement(name = "Totals")
    private Totals totals;
    @XmlElement(name = "CardNo")
    private String cardNo;
    @XmlElement(name = "TerminalId")
    private String terminalId;
    /**
     * формат YYYY-MM-DDTHH:NN:SS
     */
    @XmlElement(name = "DateTime")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private LocalDateTime dateTime;

    public Long getResultCode() {
        return resultCode;
    }

    public void setResultCode(Long resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public String getChequeText() {
        return chequeText;
    }

    public void setChequeText(String chequeText) {
        this.chequeText = chequeText;
    }

    public Long getChequeCount() {
        return chequeCount;
    }

    public void setChequeCount(Long chequeCount) {
        this.chequeCount = chequeCount;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public Totals getTotals() {
        return totals;
    }

    public void setTotals(Totals totals) {
        this.totals = totals;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "OperationResultRs{" +
                "resultCode=" + resultCode +
                ", resultText='" + resultText + '\'' +
                ", chequeText='" + chequeText + '\'' +
                ", chequeCount=" + chequeCount +
                ", originalCode='" + originalCode + '\'' +
                ", totals=" + totals +
                ", cardNo='" + cardNo + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
