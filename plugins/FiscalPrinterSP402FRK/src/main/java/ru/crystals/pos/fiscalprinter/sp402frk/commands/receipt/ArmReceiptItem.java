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

/**
 * Елемент позиции команды печати чека.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmReceiptItem {
    private static final int MAX_NAME_LENGTH = 128;

    @XmlAttribute(name = "n")
    private String name = "1059";
    @XmlAttribute(name = "t")
    private String type = "7";


    @XmlElement(name = "pa")
    private RequestElement quantity = new RequestElement("1023", KKTDataType.AMOUNT, "0.00");
    @XmlElement(name = "pa")
    private RequestElement itemName = new RequestElement("1030", KKTDataType.STRING, StringUtils.EMPTY);
    @XmlElement(name = "pa")
    private RequestElement price = new RequestElement("1079", KKTDataType.CURRENCY, "0.00");
    /**
     * Код товарной номенклатуры
     */
    @XmlElement(name = "pa")
    private RequestElement nomenclatureCode;
    /**
     * Единица измерения предмета расчета
     */
    @XmlElement(name = "pa")
    private RequestElement itemUnit;
    @XmlElement(name = "pa")
    private RequestElement taxType = new RequestElement("1199", KKTDataType.UINT, StringUtils.EMPTY);
    /**
     * Признак предмета расчета
     */
    @XmlElement(name = "pa")
    private RequestElement calcSubjectSign = new RequestElement("1212", KKTDataType.UINT, "1");
    /**
     * Признак способа расчета
     */
    @XmlElement(name = "pa")
    private RequestElement calcMethodSign = new RequestElement("1214", KKTDataType.UINT, "4");
    /**
     * Признак агента по предмету расчета
     */
    @XmlElement(name = "pa")
    private RequestElement agentCalcSign;
    /**
     * Данные агента
     */
    @XmlElement(name = "pa")
    private RequestElement agentData;
    /**
     * Данные поставщика
     */
    @XmlElement(name = "pa")
    private ArmItemProvider armItemProvider;
    /**
     * ИНН поставщика
     */
    @XmlElement(name = "pa")
    private RequestElement providerINN;

    @XmlElement(name = "pa")
    private RequestElement itemDiscounts;
    /**
     * Если параметр “свободный формат товарной позиции” включен выводится только этот текст
     */
    @XmlElement(name = "pa")
    private RequestElement itemText = new RequestElement("ItemText", KKTDataType.STRING, StringUtils.EMPTY);
    /**
     * Цена за единицу c учётом скидок/наценок (обязательно при печати свободной строкой)
     */
    @XmlElement(name = "pa")
    private RequestElement priceWithDiscount = new RequestElement("PriceWithDiscount", KKTDataType.CURRENCY, StringUtils.EMPTY);

    public String getItemName() {
        return (String) itemName.getValue();
    }

    /*
     * Обрезает itemName, при длинне больше 128 символов (ограничение ККТ)
     */
    public void setItemName(String itemName) {
        if (itemName.length() > MAX_NAME_LENGTH) {
            itemName = itemName.substring(0, MAX_NAME_LENGTH);
        }
        this.itemName.setValue(itemName);
    }

    public BigDecimal getPrice() {
        return price.getFloatValue();
    }

    public void setPrice(BigDecimal price) {
        this.price.setFloatValue(UtilsSP.roundBigDecimal(price, KKTDataType.CURRENCY));
    }

    public BigDecimal getQuantity() {
        return quantity.getFloatValue();
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity.setFloatValue(UtilsSP.roundBigDecimal(quantity, KKTDataType.AMOUNT));
    }

    public int getTaxType() {
        return taxType.getIntValue();
    }

    public void setTaxType(int taxType) {
        this.taxType.setIntValue(taxType);
    }

    public String getItemText() {
        return (String) itemText.getValue();
    }

    public void setItemText(String itemText) {
        this.itemText.setValue(itemText);
    }

    public void addItemText(String text) {
        this.itemText.setValue(itemText.getValue() + text + "\n");
    }

    public BigDecimal getPriceWithDiscount() {
        return priceWithDiscount.getFloatValue();
    }

    public void setPriceWithDiscount(BigDecimal priceWithDiscount) {
        this.priceWithDiscount.setFloatValue(UtilsSP.roundBigDecimal(priceWithDiscount, KKTDataType.CURRENCY));
    }

    public void setNomenclatureCode(String nomenclatureCodeData) {
        //Если в позиции есть код, то он будет передан.
        if (nomenclatureCodeData != null) {
            this.nomenclatureCode = new RequestElement("1162", KKTDataType.STRING, nomenclatureCodeData);
        }
    }

    public ArmItemProvider getArmItemProvider() {
        return armItemProvider;
    }

    public void setArmItemProvider(ArmItemProvider armItemProvider) {
        this.armItemProvider = armItemProvider;
    }

    public String getProviderINN() {
        return providerINN.getValue();
    }

    public void setProviderINN(String providerINN) {
        this.providerINN = new RequestElement("1226", KKTDataType.STRING, providerINN);
    }

    public int getAgentCalcSign() {
        return agentCalcSign.getIntValue();
    }

    public void setAgentCalcSign(int agentCalcSign) {
        this.agentCalcSign = new RequestElement("1222", KKTDataType.UINT, String.valueOf(agentCalcSign));
    }
}
