package ru.crystals.pos.services.cyberplat.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ru.crystals.cash.services.cyberplat.numcapacity.NumcapacityDocument;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument;

public class Converter {

	private final String inputNumcapacityFile;
	private final String inputOperatorsFile;
	private final String outputNumcapacityFile;
	private final String outputOperatorsFile;

	private final String xmlIndent = "    ";

	public Converter(String serviceFolder, String numcapacityFile, String operatorsFile) {
		inputNumcapacityFile = serviceFolder + Constants.PATH_UPDATE + numcapacityFile;
		inputOperatorsFile = serviceFolder + Constants.PATH_UPDATE + operatorsFile;
		outputNumcapacityFile = serviceFolder + numcapacityFile;
		outputOperatorsFile = serviceFolder + operatorsFile;
	}

	public boolean isUpdatePossible() {
		return new File(inputNumcapacityFile).exists() && new File(inputOperatorsFile).exists();
	}

	public void updateDocuments() throws IOException, SAXException {
		NumcapacityDocument numcapacity = createOutputNumcapacity(getIdListFromInputOperators());
		numcapacity.save(new File(outputNumcapacityFile), getXmlOptions());

		OperatorsDocument operators = createOutputOperators(getIdListFromOutputNumcapacity());
		operators.save(new File(outputOperatorsFile), getXmlOptions());

		new File(inputNumcapacityFile).delete();
		new File(inputOperatorsFile).delete();
	}

	private XMLReader createParser() {
		return (XMLReader) new SAXParser();
	}

	private List<Integer> getIdListFromInputOperators() throws IOException, SAXException {
		XMLReader parser = createParser();
		IdListOperatorsHandler idListOperatorsHandler = new IdListOperatorsHandler();
		parser.setContentHandler(idListOperatorsHandler);
		parser.parse(inputOperatorsFile);
		return idListOperatorsHandler.getIdList();
	}

	private NumcapacityDocument createOutputNumcapacity(List<Integer> operatorsIdList) throws IOException, SAXException {
		XMLReader parser = createParser();
		NumcapacityCreatingHandler numcapacityHandler = new NumcapacityCreatingHandler(operatorsIdList);
		parser.setContentHandler(numcapacityHandler);
		parser.parse(inputNumcapacityFile);
		return numcapacityHandler.getNumcapacity();
	}

	private List<Integer> getIdListFromOutputNumcapacity() throws IOException, SAXException {
		XMLReader parser = createParser();
		IdListNumcapacityHandler idListNumcapacityHandler = new IdListNumcapacityHandler();
		parser.setContentHandler(idListNumcapacityHandler);
		parser.parse(outputNumcapacityFile);
		return idListNumcapacityHandler.getIdList();
	}

	private OperatorsDocument createOutputOperators(List<Integer> operatorsIdList) throws IOException, SAXException {
		XMLReader parser = createParser();
		OperatorsCreatingHandler operatorsCreatingHandler = new OperatorsCreatingHandler(operatorsIdList);
		parser.setContentHandler(operatorsCreatingHandler);
		parser.parse(inputOperatorsFile);
		return operatorsCreatingHandler.getOperators();
	}

	private XmlOptions getXmlOptions() {
		XmlOptions options = new XmlOptions();
		options.setSavePrettyPrint();
		options.setSavePrettyPrintIndent(xmlIndent.length());

		Map<String, String> suggestedPrefixes = new HashMap<String, String>();
		suggestedPrefixes.put("http://crystals.ru/cash/services/cyberplat/numcapacity", "");
		suggestedPrefixes.put("http://crystals.ru/cash/services/cyberplat/operators", "");
		options.setSaveSuggestedPrefixes(suggestedPrefixes);

		return options;
	}

}
