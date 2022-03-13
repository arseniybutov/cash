
package jp.co.glory.bruebox;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequireVerifyMixStackerInfosType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequireVerifyMixStackerInfosType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequireVerifyMixStacker" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyMixStackerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireVerifyMixStackerInfosType", propOrder = {
    "requireVerifyMixStacker"
})
public class RequireVerifyMixStackerInfosType {

    @XmlElement(name = "RequireVerifyMixStacker", namespace = "")
    protected List<RequireVerifyMixStackerType> requireVerifyMixStacker;

    /**
     * Gets the value of the requireVerifyMixStacker property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requireVerifyMixStacker property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequireVerifyMixStacker().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RequireVerifyMixStackerType }
     * 
     * 
     */
    public List<RequireVerifyMixStackerType> getRequireVerifyMixStacker() {
        if (requireVerifyMixStacker == null) {
            requireVerifyMixStacker = new ArrayList<RequireVerifyMixStackerType>();
        }
        return this.requireVerifyMixStacker;
    }

}
