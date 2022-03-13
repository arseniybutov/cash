
package jp.co.glory.bruebox;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequireVerifyCollectionContainerInfosType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequireVerifyCollectionContainerInfosType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequireVerifyCollectionContainer" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyCollectionContainerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireVerifyCollectionContainerInfosType", propOrder = {
    "requireVerifyCollectionContainer"
})
public class RequireVerifyCollectionContainerInfosType {

    @XmlElement(name = "RequireVerifyCollectionContainer", namespace = "")
    protected List<RequireVerifyCollectionContainerType> requireVerifyCollectionContainer;

    /**
     * Gets the value of the requireVerifyCollectionContainer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requireVerifyCollectionContainer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequireVerifyCollectionContainer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RequireVerifyCollectionContainerType }
     * 
     * 
     */
    public List<RequireVerifyCollectionContainerType> getRequireVerifyCollectionContainer() {
        if (requireVerifyCollectionContainer == null) {
            requireVerifyCollectionContainer = new ArrayList<RequireVerifyCollectionContainerType>();
        }
        return this.requireVerifyCollectionContainer;
    }

}
