package ru.crystals.pos.services.cyberplat.xml;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import ru.crystals.cash.services.cyberplat.numcapacity.NumcapacityDocument;
import ru.crystals.cash.services.cyberplat.numcapacity.NumcapacityDocument.Numcapacity.Range;

public class NumcapacityCreatingHandler extends DefaultHandler {

	private List<Integer> operatorsIdList;

	private NumcapacityDocument numcapacity;

	private Range range;

	private String currentElement = new String();

	public NumcapacityCreatingHandler(List<Integer> operatorsIdList) {
		this.operatorsIdList = operatorsIdList;
		numcapacity = NumcapacityDocument.Factory.newInstance();
		numcapacity.addNewNumcapacity();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		currentElement = localName;
		if (localName.equals(Constants.RANGE)) {
			range = numcapacity.getNumcapacity().addNewRange();
			range.setFrom(Long.valueOf(attributes.getValue(Constants.FROM)));
			range.setTo(Long.valueOf(attributes.getValue(Constants.TO)));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentElement = new String();
		if (localName.equals(Constants.RANGE)) {
			range = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (range != null) {
			String currentText = new String(ch, start, length);
			if (currentElement.equals(Constants.ID)) {
				Integer id = Integer.valueOf(currentText);
				if (operatorsIdList.contains(id)) {
					range.addId(Integer.valueOf(currentText));
				}
			} else if (currentElement.equals(Constants.NAME)) {
				range.setName(currentText);
			} else if (currentElement.equals(Constants.COMPANY)) {
				range.setCompany(currentText);
			} else if (currentElement.equals(Constants.REGION)) {
				range.setRegion(currentText);
			}
		}
	}

	public NumcapacityDocument getNumcapacity() {
		return numcapacity;
	}

}
