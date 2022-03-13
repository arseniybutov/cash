package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HeartBeatEvent")
@XmlAccessorType(XmlAccessType.NONE)
public class HeartBeatEvents {

	@XmlElement(name = "SerialNo")
	private String serialNo = "123";

	public String getSerialNo() {
		return serialNo;
	}

}
