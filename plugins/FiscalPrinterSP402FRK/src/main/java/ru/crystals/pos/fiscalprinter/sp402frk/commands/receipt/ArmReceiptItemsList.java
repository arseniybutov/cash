package ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Контейнер элементов позиций, нужен для корректного формирования XML команды печати чека
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ArmReceiptItemsList {
    @XmlAttribute(name = "n")
    private String name = "1059";
    @XmlAttribute(name = "t")
    private String type = "6";

    @XmlElement(name = "pa")
    private List<ArmReceiptItem> items = new ArrayList<>();

    public List<ArmReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ArmReceiptItem> items) {
        this.items = items;
    }

    public void addItem(ArmReceiptItem item) {
        if (this.items != null) {
            this.items.add(item);
        }
    }
}
