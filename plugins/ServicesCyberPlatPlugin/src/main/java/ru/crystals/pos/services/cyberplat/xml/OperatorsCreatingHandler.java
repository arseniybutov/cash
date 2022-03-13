package ru.crystals.pos.services.cyberplat.xml;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Processor;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Processor.Request;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Receipts;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Receipts.Parameter;

public class OperatorsCreatingHandler extends DefaultHandler {

	private List<Integer> operatorsIdList;

	private OperatorsDocument operators;

	private Operator operator;

	private Processor processor;

	private Request request;

	private Receipts receipts;

	private String currentElement = new String();

	public OperatorsCreatingHandler(List<Integer> operatorsIdList) {
		this.operatorsIdList = operatorsIdList;
		operators = OperatorsDocument.Factory.newInstance();
		operators.addNewOperators();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		currentElement = localName;
		if (localName.equals(Constants.OPERATOR)) {
			Integer id = Integer.valueOf(attributes.getValue(Constants.ID));
			if (operatorsIdList.contains(id)) {
				operator = operators.getOperators().addNewOperator();
				operator.setId(id);
			}
		} else if (localName.equals(Constants.PROCESSOR) && (operator != null)) {
			processor = operator.addNewProcessor();
		} else if (localName.equals(Constants.REQUEST) && (processor != null)) {
			request = processor.addNewRequest();
			request.setName(attributes.getValue(Constants.NAME));
		} else if (localName.equals(Constants.RECEIPTS) && (operator != null)) {
			receipts = operator.addNewReceipts();
		} else if (localName.equals(Constants.PARAMETER) && (receipts != null)) {
			Parameter parameter = receipts.addNewParameter();
			parameter.setName(attributes.getValue(Constants.NAME));
			parameter.setValue(attributes.getValue(Constants.VALUE));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentElement = new String();
		if (localName.equals(Constants.OPERATOR)) {
			operator = null;
		} else if (localName.equals(Constants.PROCESSOR)) {
			processor = null;
		} else if (localName.equals(Constants.REQUEST)) {
			request = null;
		} else if (localName.equals(Constants.RECEIPTS)) {
			receipts = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (operator != null) {
			String currentText = new String(ch, start, length);
			if (currentElement.equals(Constants.NAME)) {
				if ((operator.getName() == null) || operator.getName().isEmpty()) {
					operator.setName(currentText);
				}
			} else if (currentElement.equals(Constants.URL)) {
				if (request.getUrl() == null) {
					request.setUrl("");
				}
				request.setUrl(request.getUrl() + currentText);
			}
		}
	}

	public OperatorsDocument getOperators() {
		return operators;
	}

}
