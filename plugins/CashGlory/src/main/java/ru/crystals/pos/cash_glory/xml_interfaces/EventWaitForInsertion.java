package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This event is notified when the cassette is unset when starting.
 * See DevicePosition ID table.
 * 
 * @author p.tykvin
 * 
 */
@XmlType
public class EventWaitForInsertion {

	@XmlElement(name = "DevicePositionId")
	private Integer devicePositionId;

}
