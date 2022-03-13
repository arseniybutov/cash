package jp.co.glory.bruebox;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.AbstractDenomination;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

/**
 * <p>
 * Java class for DenominationType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DenominationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Piece"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Status"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.glory.co.jp/bruebox.xsd}DenominationAttribGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DenominationType", propOrder = {"piece", "status"})
public class DenominationType extends AbstractDenomination {

    @XmlElement(name = "Piece", required = true)
    protected BigInteger piece;
    @XmlElement(name = "Status", required = true)
    protected BigInteger status;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected String cc;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger fv;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger rev;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger devid;

    /**
     * Gets the value of the piece property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getPiece() {
        return piece;
    }

    /**
     * Sets the value of the piece property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setPiece(BigInteger value) {
        this.piece = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setStatus(BigInteger value) {
        this.status = value;
    }

    /**
     * Gets the value of the cc property.
     * 
     * @return possible object is {@link String }
     */
    public String getCc() {
        return cc;
    }

    /**
     * Sets the value of the cc property.
     * 
     * @param value
     *            allowed object is {@link String }
     */
    public void setCc(String value) {
        this.cc = value;
    }

    /**
     * Gets the value of the fv property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getFv() {
        return fv;
    }

    /**
     * Sets the value of the fv property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setFv(BigInteger value) {
        this.fv = value;
    }

    /**
     * Gets the value of the rev property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getRev() {
        return rev;
    }

    /**
     * Sets the value of the rev property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setRev(BigInteger value) {
        this.rev = value;
    }

    /**
     * Gets the value of the devid property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getDevid() {
        return devid;
    }

    /**
     * Sets the value of the devid property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setDevid(BigInteger value) {
        this.devid = value;
    }

    //end of factory class

    @Override
    public long getValueInf() {
        return fv.longValue();
    }

    @Override
    public long getPieceInf() {
        return piece.longValue();
    }

    @Override
    public String getCurrencyInf() {
        return cc;
    }

    @Override
    public void setPieceInf(long piece) {
        this.piece = BigInteger.valueOf(piece);
    }

    @Override
    public int getDevidInf() {
        return devid.intValue();
    }

    @Override
    public int getStatusInf() {
        return status.intValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fv == null) ? 0 : fv.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DenominationType other = (DenominationType) obj;
        if (fv == null) {
            if (other.fv != null)
                return false;
        } else if (!fv.equals(other.fv))
            return false;
        return true;
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
