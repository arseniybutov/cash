
package ru.crystals.pos.visualization.products.weight.tare;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "tare"
})
@XmlRootElement(name = "tares")
public class Tares {

    private List<Tare> tare = new ArrayList<>();

    public List<Tare> getTare() {
        return tare;
    }
}
