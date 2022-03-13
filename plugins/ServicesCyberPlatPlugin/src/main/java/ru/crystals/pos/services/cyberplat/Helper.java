package ru.crystals.pos.services.cyberplat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator;
import ru.crystals.pos.services.cyberplat.xml.Constants;
import ru.crystals.pos.services.cyberplat.xml.NumcapacityCompressorHandler;
import ru.crystals.pos.services.cyberplat.xml.Range;

public class Helper {

	/** Генерация номера сессии */
	public String generateSessionId() {
		String sessionId = "SR10I" + Long.valueOf(Calendar.getInstance().getTimeInMillis()).toString();
		if (sessionId.length() > 20) {
			sessionId = sessionId.substring(0, 20);
		}
		return sessionId;
	}

	public Response parseResponse(String responseString) throws IOException {
		Properties properties = new Properties();
		properties.load(new StringReader(responseString));

		Response response = new Response();
		response.setDate(properties.getProperty("DATE"));
		response.setSessionId(properties.getProperty("SESSION"));
		if (properties.getProperty("RESULT").equals("0")) {
			response.setResult(true);
		}
		response.setErrorCode(Integer.valueOf(properties.getProperty("ERROR")));
		response.setErrorMessage(properties.getProperty("ERRMSG"));

		return response;
	}

	public String toStringFormat(Long money) {
		String stringMoney = Long.valueOf(money).toString();
		int length = stringMoney.length();
		stringMoney = stringMoney.substring(0, length - 2) + "." + stringMoney.substring(length - 2);
		return stringMoney;
	}

	public URLs getURLs(Operator operator) {
		URLs urls = new URLs();
		for (ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Processor.Request request : operator.getProcessor()
				.getRequestArray()) {
			if (request.getName().equals(Constants.CHECK)) {
				urls.setCheckURL(request.getUrl());
			} else if (request.getName().equals(Constants.PAYMENT)) {
				urls.setPaymentURL(request.getUrl());
			} else if (request.getName().equals(Constants.STATUS)) {
				urls.setStatusURL(request.getUrl());
			}
		}
		return urls;
	}

	public Map<Range, List<Integer>> getRangeMap(String numcapacityFile) throws IOException, SAXException {
		XMLReader parser = new SAXParser();
		NumcapacityCompressorHandler numcapacityCompressorHandler = new NumcapacityCompressorHandler();
		parser.setContentHandler(numcapacityCompressorHandler);
		parser.parse(numcapacityFile);
		return numcapacityCompressorHandler.getRangeMap();
	}

	public List<Operator> getFitOperators(Map<Range, List<Integer>> rangeMap, OperatorsDocument operators, String accountNumber) {
		List<Operator> fitOperators = new ArrayList<Operator>();
		List<Integer> idList = getIdList(rangeMap, accountNumber);
		if (idList != null) {
			for (Operator operator : operators.getOperators().getOperatorArray()) {
				if (idList.contains(operator.getId())) {
					fitOperators.add(operator);
				}
			}
		}
		return fitOperators;
	}

	private List<Integer> getIdList(Map<Range, List<Integer>> rangeMap, String accountNumber) {
		long number = Long.valueOf(accountNumber);
		Range range = null;
		for (Range testRange : rangeMap.keySet()) {
			if (number >= testRange.getFrom() && number <= testRange.getTo()) {
				range = testRange;
				break;
			}
		}
		return rangeMap.get(range);
	}

}
