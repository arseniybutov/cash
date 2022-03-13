package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DEFAULT")
@XmlAccessorType(XmlAccessType.NONE)
public class DefaultEvents extends AbstractEvents {

	@XmlElement(name = "StatusChangeEvent")
	private void setEventStatusChange(EventStatusChange eventStatusChange) {
		notificator.fireEventStatusChange(eventStatusChange);
	}

	@XmlElement(name = "InventoryResponse")
	private InventoryResponse inventoryResponse;

	public void setInventoryResponse(InventoryResponse inventoryResponse) {
		notificator.fireInventoryResponse(inventoryResponse);
	}

	public InventoryResponse getInventoryResponse() {
		return inventoryResponse;
	}

}
