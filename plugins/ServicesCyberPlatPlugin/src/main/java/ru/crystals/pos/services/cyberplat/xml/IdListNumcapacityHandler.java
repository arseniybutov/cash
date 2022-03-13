package ru.crystals.pos.services.cyberplat.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class IdListNumcapacityHandler extends DefaultHandler {

	private List<Integer> idList = new ArrayList<Integer>();

	private String currentElement = new String();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		currentElement = localName;
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentElement = new String();
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String currentText = new String(ch, start, length);
		if (currentElement.equals(Constants.ID)) {
			Integer id = Integer.valueOf(currentText);
			if (!idList.contains(id)) {
				idList.add(id);
			}
		}
	}

	public List<Integer> getIdList() {
		return idList;
	}

}
