package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The note or the coin of the device device is notified to exist.
 * See DevicePosition ID table.
 * 
 * @author p.tykvin
 * 
 */
@XmlType
public class EventExist {

	@XmlElement(name = "DevicePositionId")
	private Integer devicePositionId;

}