
package jp.co.glory.bruebox;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequireVerifyMixStackerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequireVerifyMixStackerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyInfoAttribGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireVerifyMixStackerType")
public class RequireVerifyMixStackerType {

    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd", required = true)
    protected BigInteger devid;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd", required = true)
    protected BigInteger val;

    /**
     * Gets the value of the devid property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDevid() {
        return devid;
    }

    /**
     * Sets the value of the devid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDevid(BigInteger value) {
        this.devid = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVal(BigInteger value) {
        this.val = value;
    }

}
