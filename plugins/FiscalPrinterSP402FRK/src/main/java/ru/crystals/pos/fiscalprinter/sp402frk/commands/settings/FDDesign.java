package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class FDDesign {
    @XmlAttribute(name = "n")
    private String name = "FDDesign";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Перенос наименований товарных позиций
     * 0 – перенос
     * 1 – обрезка
     */
    @XmlElement(name = "pa")
    private RequestElement itemCutMode;
    /**
     * Печать QR кода с текстом справа (см. 4.8.8.1.5)
     * 0 – QR код справа
     * 1 – QR код по центру
     */
    @XmlElement(name = "pa")
    private RequestElement printQRWithText;
    /**
     * Свободная печать товарной позиции (см. 4.8.8.1.2)
     * 0 – формирует ККТ
     * 1 – формирует кассовое ПО
     */
    @XmlElement(name = "pa")
    private RequestElement useItemFreeString = new RequestElement("UseItemFreeString", KKTDataType.STRING, "0");
    /**
     * Блочная форма вывода НДС
     */
    @XmlElement(name = "pa")
    private RequestElement ndsInBlocks;
    /**
     * Буквенная кодировка НДС
     */
    @XmlElement(name = "pa")
    private RequestElement ndsLetterCode;

    public String getItemCutMode() {
        return itemCutMode.getValue();
    }

    public void setItemCutMode(String itemCutMode) {
        this.itemCutMode.setValue(itemCutMode);
    }

    public String getUseItemFreeString() {
        return useItemFreeString.getValue();
    }

    public void setUseItemFreeString(String useItemFreeString) {
        this.useItemFreeString.setValue(useItemFreeString);
    }
}
