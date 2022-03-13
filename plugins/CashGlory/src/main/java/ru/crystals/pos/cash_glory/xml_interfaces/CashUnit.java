package ru.crystals.pos.cash_glory.xml_interfaces;

import ru.crystals.pos.cash_machine.entities.AbstractCashUnit;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class CashUnit extends AbstractCashUnit {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "unitno")
    private Integer unitno;

    @XmlAttribute
    private Integer st;

    @XmlAttribute
    private Integer nf;

    @XmlAttribute
    private Integer ne;

    @XmlAttribute
    private Integer max;

    @XmlElements({@XmlElement(name = "Denomination", type = Denomination.class)})
    private List< ? extends DenominationInterface> denomintaions;

    public Integer getUnitno() {
        return unitno;
    }

    public void setUnitno(Integer unitno) {
        this.unitno = unitno;
    }

    @Override
    public int getMaxInf() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    @Override
    public int getNearFullInf() {
        return nf;
    }

    @Override
    public int getNearEmptyInf() {
        return ne;
    }

    @Override
    public List< ? extends DenominationInterface> getDenominationsInf() {
        return denomintaions;
    }

    @Override
    public String getCurrencyInf() {
        if (getDenominationsInf() != null && getDenominationsInf().size() == 1) {
            return getDenominationsInf().get(0).getCurrencyInf();
        }
        return null;
    }

    @Override
    public Long getValueInf() {
        if (getDenominationsInf() != null && getDenominationsInf().size() == 1) {
            return getDenominationsInf().get(0).getValueInf();
        }
        return null;
    }

    @Override
    public boolean isCassete() {
        if (isBanknote()) {
            return unitno == 4057 || unitno == 4058 || unitno == 4059 || unitno == 4060;
        } else {
            return unitno == 4084 || unitno == 4165;
        }
    }

    @Override
    public void setMaxInf(int maxInf) {
        this.max = maxInf;
    }

    @Override
    public void setNearEmptyInf(int nearEmptyInf) {
        this.ne = nearEmptyInf;
    }

    @Override
    public void setNearFullInf(int nearFullInf) {
        this.nf = nearFullInf;
    }

    @Override
    public Integer getUnitnoInf() {
        return unitno;
    }

    @Override
    public void setUnitnoInf(Integer unitnoInf) {
        this.unitno = unitnoInf;
    }

    @Override
    public void setDenominationsInf(List< ? extends DenominationInterface> denominationsInf) {
        this.denomintaions = denominationsInf;
    }


}
