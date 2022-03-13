package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class EventCassetteInventoryOnInsertion {

    @XmlElement(name = "SerialNo")
    protected String serialNo;

    @XmlElements({@XmlElement(name = "Cash", type = Cash.class),})
    private List<Cash> cash;

    public List< ? extends DenominationInterface> getDenominations() {
        return cash.get(0).getDenomintaions();
    }

}
