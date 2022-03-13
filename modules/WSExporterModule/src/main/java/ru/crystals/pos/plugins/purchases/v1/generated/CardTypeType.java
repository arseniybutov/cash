
package ru.crystals.pos.plugins.purchases.v1.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for card-typeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="card-typeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="INTERNAL"/>
 *     &lt;enumeration value="EXTERNAL"/>
 *     &lt;enumeration value="PRESENT"/>
 *     &lt;enumeration value="BONUS"/>
 *     &lt;enumeration value="COUPON_CARD"/>
 *     &lt;enumeration value="COUPON_RECEIPT"/>
 *     &lt;enumeration value="COUPON_PROCESSING"/>
 *     &lt;enumeration value="COUPON_UNIQUE"/>
 *     &lt;enumeration value="UNKNOWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "card-typeType")
@XmlEnum
public enum CardTypeType {


    /**
     * 
     *                         внутренняя карта
     *                     
     * 
     */
    INTERNAL,

    /**
     * 
     *                         внешняя карта
     *                     
     * 
     */
    EXTERNAL,

    /**
     * 
     *                         подарочная карта
     *                     
     * 
     */
    PRESENT,

    /**
     * 
     *                         бонусная карта
     *                     
     * 
     */
    BONUS,

    /**
     * 
     *                         купон (вырезанный из газетки?)
     *                     
     * 
     */
    COUPON_CARD,

    /**
     * 
     *                         купон (распечатанный на чеке продажи?)
     *                     
     * 
     */
    COUPON_RECEIPT,

    /**
     * 
     *                         купон (возвратный)
     *                     
     * 
     */
    COUPON_PROCESSING,

    /**
     * 
     *                         купон (уникальный)
     *                     
     * 
     */
    COUPON_UNIQUE,

    /**
     * 
     *                         купон (уникальный)
     *                     
     * 
     */
    UNKNOWN;

    public String value() {
        return name();
    }

    public static CardTypeType fromValue(String v) {
        return valueOf(v);
    }

}
