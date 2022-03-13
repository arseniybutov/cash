
package ru.crystals.pos.visualization.products.weight.tare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "tare")
public class Tare implements Comparable<Tare>{

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "weight", required = true)
    protected Long weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeight() {
        return BigDecimal.valueOf(weight,3);
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Tare o) {
        return weight.intValue() - o.getWeight().intValue();
    }

    @Override
    public String toString() {
        return name + " - " +
            weight +
            " гр";
    }
}
