package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This event is notified when the door is closed.
 * The “device position id” (“door id”) can have the following;
 * 1 collection door
 * 2 maintenance door
 * 
 * @author p.tykvin
 * 
 */
@XmlType
public class EventClosed {

	@XmlElement(name = "DevicePositionId")
	private Integer devicePositionId;

}
