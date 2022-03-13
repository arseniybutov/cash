
package ru.crystals.pos.plugins.purchases.v1.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for client_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="client_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PRIVATE"/>
 *     &lt;enumeration value="JURISTIC_PERSON"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "client_Type")
@XmlEnum
public enum ClientType {


    /**
     * 
     *                         физическое лицо
     *                     
     * 
     */
    PRIVATE,

    /**
     * 
     *                         Юридическое лицо
     *                     
     * 
     */
    JURISTIC_PERSON;

    public String value() {
        return name();
    }

    public static ClientType fromValue(String v) {
        return valueOf(v);
    }

}
