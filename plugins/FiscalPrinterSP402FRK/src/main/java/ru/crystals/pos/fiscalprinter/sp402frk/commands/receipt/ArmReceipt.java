package ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;

/**
 * Данные команды печати чека, содержит все реквизиты документа, сереализуется и отправляется в качестве
 * данных пакета запроса.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmReceipt {
    @XmlAttribute(name = "n")
    private String name = "3";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Адрес оператора перевода
     */
    @XmlElement(name = "pa")
    /**
     * Не инициализарованные элементы - не обязательны и в чек не добавляютстся
     */
    private RequestElement operAddr;
    @XmlElement(name = "pa")
    private RequestElement customerAddress = new RequestElement("1008", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * ИНН оператора перевода
     */
    @XmlElement(name = "pa")
    private RequestElement operINN;
    /**
     * Кассир
     */
    @XmlElement(name = "pa")
    private RequestElement userLoginName = new RequestElement("1021", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * Наименование оператора перевода
     */
    @XmlElement(name = "pa")
    private RequestElement operName;
    @XmlElement(name = "pa")
    private RequestElement cashSumm = new RequestElement("1031", KKTDataType.CURRENCY, "0.00");
    /**
     * Операция платежного агента
     */
    @XmlElement(name = "pa")
    private RequestElement agentOper;
    @XmlElement(name = "pa")
    private RequestElement receiptType = new RequestElement("1054", KKTDataType.UINT, StringUtils.EMPTY);
    @XmlElement(name = "pa")
    private RequestElement taxSystem = new RequestElement("1055", KKTDataType.UINT, StringUtils.EMPTY);
    /**
     * Признак агента
     */
    @XmlElement(name = "pa")
    private RequestElement agentSign;

    /**
     * Телефон поставщика
     */
    @XmlElement(name = "pa")
    private RequestElement providerPhone;

    @XmlElement(name = "pa")
    private ArmReceiptItemsList items = new ArmReceiptItemsList();
    /**
     * Телефон платежного агента
     */
    @XmlElement(name = "pa")
    private RequestElement agentPhone;
    /**
     * Телефон оператора по приему платежей
     */
    @XmlElement(name = "pa")
    private RequestElement payOperPhone;
    /**
     * Телефон оператора перевода
     */
    @XmlElement(name = "pa")
    private RequestElement operPhone;
    @XmlElement(name = "pa")
    private RequestElement nonCashSumm = new RequestElement("1081", KKTDataType.CURRENCY, "0.00");

    /**
     * ИНН кассира
     */
    @XmlElement(name = "pa")
    private RequestElement userINN = new RequestElement("1203", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * Признак предмета расчета
     */
    @XmlElement(name = "pa")
    private RequestElement calcSubjectSign;
    /**
     * Признак способа расчета
     */
    @XmlElement(name = "pa")
    private RequestElement calcMethodSign;
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
    /**
     * Не выполнять печать при заполненном поле CustomerAddress
     */
    @XmlElement(name = "pa")
    private RequestElement doNotPrint;
    @XmlElement(name = "pa")
    private RequestElement footer = new RequestElement("Footer", KKTDataType.STRING, StringUtils.EMPTY);
    @XmlElement(name = "pa")
    private RequestElement headerText = new RequestElement("HeaderText", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * Номер POS-терминала
     */
    @XmlElement(name = "pa")
    private RequestElement posNum = new RequestElement("PosNum", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * Номер чека в кассовой системе
     */
    @XmlElement(name = "pa")
    private RequestElement posReceiptNum;
    /**
     * Номер смены в кассовой системе
     */
    @XmlElement(name = "pa")
    private RequestElement posShiftNum;
    /**
     * Опциональный текстовый блок после чека
     */
    @XmlElement(name = "pa")
    private RequestElement textAfterDoc;

    /**
     * Сумма текущего чека
     */
    private BigDecimal checkSumm = BigDecimal.valueOf(0);

    public String getUserLoginName() {
        return (String) userLoginName.getValue();
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName.setValue(userLoginName);
    }

    public String getUserINN() {
        return (String) userINN.getValue();
    }

    public void setUserINN(String userINN) {
        this.userINN.setValue(userINN);
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

    public String getHeaderText() {
        return (String) headerText.getValue();
    }

    public void setHeaderText(String headerText) {
        this.headerText.setValue(headerText);
    }

    public void addHeaderText(String text) {
        this.headerText.setValue(headerText.getValue() + text + "\n");
    }

    public List<ArmReceiptItem> getItems() {
        return items.getItems();
    }

    public void setItems(List<ArmReceiptItem> items) {
        this.items.setItems(items);
    }

    public void addItem(ArmReceiptItem item) {
        items.addItem(item);
    }

    public String getCustomerAddress() {
        return customerAddress.getValue();
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress.setValue(customerAddress);
    }

    public BigDecimal getCashSumm() {
        return cashSumm.getFloatValue();
    }

    public void setCashSumm(BigDecimal cashSumm) {
        this.cashSumm.setFloatValue(UtilsSP.roundBigDecimal(cashSumm, KKTDataType.CURRENCY));
    }

    public BigDecimal getNonCashSumm() {
        return nonCashSumm.getFloatValue();
    }

    public void setNonCashSumm(BigDecimal nonCashSumm) {
        this.nonCashSumm.setFloatValue(UtilsSP.roundBigDecimal(nonCashSumm, KKTDataType.CURRENCY));
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

    public String getFooter() {
        return footer.getValue();
    }

    public void setFooter(String footer) {
        this.footer.setValue(footer);
    }

    public void addFooterText(String text) {
        this.footer.setValue(footer.getValue() + text + "\n");
    }

    public void setPosNum(String posNum) {
        this.posNum.setValue(posNum);
    }

    public BigDecimal getCheckSumm() {
        return checkSumm;
    }

    public void setCheckSumm(BigDecimal checkSumm) {
        this.checkSumm = checkSumm;
    }

    public int getAgentSign() {
        return agentSign.getIntValue();
    }

    public void setAgentSign(int agentSign) {
        this.agentSign = new RequestElement("1057", KKTDataType.UINT, String.valueOf(agentSign));
    }

    public String getProviderPhone() {
        return providerPhone.getValue();
    }

    public void setProviderPhone(String providerPhone) {
        this.providerPhone = new RequestElement("1171", KKTDataType.STRING, providerPhone);
    }
}
