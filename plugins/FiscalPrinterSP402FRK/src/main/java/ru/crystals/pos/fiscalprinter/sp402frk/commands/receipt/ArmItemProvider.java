package ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Данные поставщика
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmItemProvider {

    @XmlAttribute(name = "n")
    private String name = "1224";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Наименование поставщика
     */
    @XmlElement(name = "pa")
    private RequestElement providerName = new RequestElement("1225", KKTDataType.STRING, StringUtils.EMPTY);

    /**
     * Телефон поставщика
     */
    @XmlElement(name = "pa")
    private RequestElement providerPhone = new RequestElement("1171", KKTDataType.STRING, StringUtils.EMPTY);

    public String getProviderName() {
        return providerName.getValue();
    }

    public void setProviderName(String providerName) {
        this.providerName.setValue(providerName);
    }

    public String getProviderPhone() {
        return providerPhone.getValue();
    }

    public void setProviderPhone(String providerPhone) {
        this.providerPhone.setValue(providerPhone);
    }

}
