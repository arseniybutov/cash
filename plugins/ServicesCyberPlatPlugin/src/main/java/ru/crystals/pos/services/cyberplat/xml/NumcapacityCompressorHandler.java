package ru.crystals.pos.services.cyberplat.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class NumcapacityCompressorHandler extends DefaultHandler {

	private Map<Range, List<Integer>> rangeMap = new HashMap<Range, List<Integer>>();

	private Range range;

	private List<Integer> idList;

	private String currentElement = new String();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		currentElement = localName;
		if (localName.equals(Constants.RANGE)) {
			range = new Range();
			range.setFrom(Long.valueOf(attributes.getValue(Constants.FROM)));
			range.setTo(Long.valueOf(attributes.getValue(Constants.TO)));
			idList = new ArrayList<Integer>();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentElement = new String();
		if (localName.equals(Constants.RANGE)) {
			if (range != null && idList != null) {
				rangeMap.put(range, idList);
			}
			range = null;
			idList = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (range != null && idList != null) {
			String currentText = new String(ch, start, length);
			if (currentElement.equals(Constants.ID)) {
				idList.add(Integer.valueOf(currentText));
			}
		}
	}

	public Map<Range, List<Integer>> getRangeMap() {
		return rangeMap;
	}

}
