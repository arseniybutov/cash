package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.AbstractCash;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Cash extends AbstractCash {

    @XmlAttribute
    private Integer type;

    @XmlElements({@XmlElement(name = "Denomination", type = Denomination.class)})
    private List<DenominationInterface> denomintaions;

    @Override
    public String toString() {
        double sum = 0;
        for (DenominationInterface d : denomintaions) {
            sum += d.getAmountInf();
        }
        return String.valueOf(sum / 100D);
    }

    @Override
    public int getTypeInf() {
        return type;
    }

    @Override
    public List< ? extends DenominationInterface> getDenomintaions() {
        return denomintaions;
    }

    @Override
    public void addDenomination(DenominationInterface denomintion) {
        denomintaions.add(denomintion);
    }

    @Override
    public boolean isDispensable() {
        return (getTypeInf() == 4);
    }

}
