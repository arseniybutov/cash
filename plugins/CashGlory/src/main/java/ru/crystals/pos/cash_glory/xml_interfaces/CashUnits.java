package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitsInterface;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class CashUnits implements CashUnitsInterface {

    @XmlAttribute
    private Integer devid;

    @XmlElements({
            @XmlElement(name = "CashUnit", type = CashUnit.class)
    })
    private List<? extends CashUnitInterface> allCashUnits;

    @Override
    public Integer getDevidInf() {
        return devid;
    }

    private List<? extends CashUnitInterface> cashUnits = null;

    @Override
    public List<? extends CashUnitInterface> getCashUnits() {
        if (cashUnits == null) {
            ArrayList<CashUnitInterface> list = new ArrayList<>();
            for (CashUnitInterface c : allCashUnits) {
                c.setBanknote(isBanknotes());
                if (!c.isCassete()) {
                    list.add(c);
                }
            }
            cashUnits = list;
        }
        return cashUnits;
    }

    @Override
    public boolean isBanknotes() {
        return getDevidInf() == 1;
    }

    @Override
    public boolean isCoins() {
        return getDevidInf() == 2;
    }

    @Override
    public List<? extends CashUnitInterface> getAllCashUnits() {
        return allCashUnits;
    }

}
