package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.math.BigDecimal;

/**
 * Базовый элемент данных запроса, хранит один параметр XML: имя, тип, значение
 */
@XmlAccessorType(XmlAccessType.NONE)
public class RequestElement {

    @XmlAttribute(name = "n")
    private String name;
    @XmlAttribute(name = "t")
    private String type;
    @XmlValue
    private String value;

    public RequestElement(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BigDecimal getFloatValue() {
        return new BigDecimal(value);
    }

    public void setFloatValue(BigDecimal value) {
        this.value = value.toString();
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }

    public void setIntValue(int value) {
        this.value = Integer.toString(value);
    }
}
