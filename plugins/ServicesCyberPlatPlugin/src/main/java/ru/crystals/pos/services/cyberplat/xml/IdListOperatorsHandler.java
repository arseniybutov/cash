package ru.crystals.pos.services.cyberplat.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class IdListOperatorsHandler extends DefaultHandler {

	private List<Integer> idList = new ArrayList<Integer>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (localName.equals(Constants.OPERATOR)) {
			idList.add(Integer.valueOf(attributes.getValue(Constants.ID)));
		}
	}

	public List<Integer> getIdList() {
		return idList;
	}

}
