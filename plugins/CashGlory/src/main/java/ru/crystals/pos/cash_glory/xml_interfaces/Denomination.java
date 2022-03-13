package ru.crystals.pos.cash_glory.xml_interfaces;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import jp.co.glory.bruebox.DenominationType;
import ru.crystals.pos.cash_machine.entities.AbstractDenomination;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Denomination extends AbstractDenomination {

    @XmlAttribute(name = "cc")
    private String currency;

    @XmlAttribute(name = "fv")
    private Long value; //в копейках

    @XmlAttribute(name = "rev")
    private Integer rev;

    @XmlAttribute(name = "devid")
    private Integer devid;

    @XmlElement(name = "Piece")
    private Long piece; // количество

    @XmlElement(name = "Status")
    private Integer status;

    @XmlElement(name = "Category")
    private String category;

    @Override
    public String toString() {
        return String.format("%d %s x %d%n", value, currency, piece);
    }

    @Override
    public long getValueInf() {
        return value;
    }

    @Override
    public long getPieceInf() {
        return piece;
    }

    @Override
    public String getCurrencyInf() {
        return currency;
    }

    @Override
    public void setPieceInf(long piece) {
        this.piece = piece;
    }

    @Override
    public int getDevidInf() {
        return devid;
    }

    @Override
    public int getStatusInf() {
        return status;
    }

    @Override
    public DenominationInterface getClone() {
        DenominationType denomination = new DenominationType();
        denomination.setCc(this.getCurrencyInf());
        denomination.setDevid(BigInteger.valueOf(this.getDevidInf()));
        denomination.setPieceInf(this.getPieceInf());
        denomination.setStatus(BigInteger.valueOf(this.getStatusInf()));
        denomination.setFv(BigInteger.valueOf(this.getValueInf()));
        return denomination;
    }

}
