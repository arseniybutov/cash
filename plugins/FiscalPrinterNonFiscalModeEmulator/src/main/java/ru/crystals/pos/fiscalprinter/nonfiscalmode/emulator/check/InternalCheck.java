package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check;

import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.MarginType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InternalCheck {
	private final String CHECK_EMULATOR_FILE = "check.emulator.xml";
	private final String checkFile = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + CHECK_EMULATOR_FILE;
	private Document checkXML;

    private CashOperation operation;
	public CheckType type = null;
	long shiftNumber = 0;
	long number = 0;
	Date date = Calendar.getInstance().getTime();

	List<InternalGoods> goods = new ArrayList<>();
	List<InternalDisc> discs = new ArrayList<>();
	List<InternalMargin> margins = new ArrayList<>();
	List<InternalPayments> payments = new ArrayList<>();
	private CheckState state = CheckState.OPEN;

	public InternalCheck() throws Exception {
		File file = new File(checkFile);
		if (file.exists()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			checkXML = dBuilder.parse(file);
			if (checkXML != null && checkXML.getDocumentElement() != null)
				checkXML.getDocumentElement().normalize();
		} else {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			checkXML = dBuilder.newDocument();
		}

		fromXML(checkXML);
	}

	private void fromXML(Document checkXML) {
		NodeList nList = checkXML.getElementsByTagName("head");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				type = CheckType.valueOf(eElement.getAttribute("type"));
				state = CheckState.valueOf(eElement.getAttribute("state"));
				shiftNumber = NumberUtils.toLong(eElement.getAttribute("shiftNumber"), 0);
				number = NumberUtils.toLong(eElement.getAttribute("number"), 0);
				break;
			}
		}

		nList = checkXML.getElementsByTagName("position");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				InternalGoods pos = new InternalGoods();
				pos.goodsName = eElement.getAttribute("goodsName");
				pos.posNumber = NumberUtils.toLong(eElement.getAttribute("posNumber"), 0);
				pos.departNumber = NumberUtils.toLong(eElement.getAttribute("departNumber"), 1);
				pos.quantity = NumberUtils.toLong(eElement.getAttribute("quantity"), 0);
				pos.price = NumberUtils.toLong(eElement.getAttribute("price"), 0);
				pos.sum = (pos.quantity * pos.price) / 1000;
				goods.add(pos);

				NodeList aList = eElement.getElementsByTagName("posDiscount");
				for (int r = 0; r < aList.getLength(); r++) {
					Node aNode = aList.item(r);
					if (aNode.getNodeType() == Node.ELEMENT_NODE) {
						Element aElement = (Element) aNode;

						InternalDisc disc = new InternalDisc();
						String typeStr = aElement.getAttribute("type");
						if (typeStr!= null && !typeStr.isEmpty())
							disc.type = DiscType.valueOf(aElement.getAttribute("type"));
						disc.name = aElement.getAttribute("name");
						disc.value = NumberUtils.toLong(aElement.getAttribute("value"), 0);
						goods.add(pos);
					}
				}
				aList = eElement.getElementsByTagName("posMargin");
				for (int r = 0; r < aList.getLength(); r++) {
					Node aNode = aList.item(r);
					if (aNode.getNodeType() == Node.ELEMENT_NODE) {
						Element aElement = (Element) aNode;

						InternalMargin margin = new InternalMargin();
						String typeStr = aElement.getAttribute("type");
						if (typeStr!= null && !typeStr.isEmpty())
							margin.type = MarginType.valueOf(aElement.getAttribute("type"));
						margin.name = aElement.getAttribute("name");
						margin.value = NumberUtils.toLong(aElement.getAttribute("value"), 0);
						goods.add(pos);
					}
				}

			}
		}

		NodeList aList = checkXML.getElementsByTagName("discount");
		for (int r = 0; r < aList.getLength(); r++) {
			Node aNode = aList.item(r);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				Element aElement = (Element) aNode;

				InternalDisc disc = new InternalDisc();
				String typeStr = aElement.getAttribute("type");
				if (typeStr!= null && !typeStr.isEmpty())
					disc.type = DiscType.valueOf(typeStr);
				disc.name = aElement.getAttribute("name");
				disc.value = NumberUtils.toLong(aElement.getAttribute("value"), 0);
				discs.add(disc);
			}
		}
		aList = checkXML.getElementsByTagName("margin");
		for (int r = 0; r < aList.getLength(); r++) {
			Node aNode = aList.item(r);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				Element aElement = (Element) aNode;

				InternalMargin margin = new InternalMargin();
				margin.type = MarginType.valueOf(aElement.getAttribute("type"));
				String typeStr = aElement.getAttribute("type");
				if (typeStr!= null && !typeStr.isEmpty())
					margin.name = aElement.getAttribute("name");
				margin.value = NumberUtils.toLong(aElement.getAttribute("value"), 0);
				margins.add(margin);
			}
		}

		nList = checkXML.getElementsByTagName("payment");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				InternalPayments pay = new InternalPayments();
				pay.index = NumberUtils.toLong(eElement.getAttribute("index"), 0);
				pay.value = NumberUtils.toLong(eElement.getAttribute("value"), 0);
				System.out.println("payment: " + pay);
				payments.add(pay);
			}
		}

	}

	private void store() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		checkXML = dBuilder.newDocument();

		Element rootElement = checkXML.createElement("check");
		checkXML.appendChild(rootElement);

		Element head = checkXML.createElement("head");
		if (type != null) {
			head.setAttribute("type", type.toString());
		}
		if (state != null) {
            head.setAttribute("state", state.toString());
        }
        if (operation != null) {
            head.setAttribute("operation", operation.name());
        }
		head.setAttribute("shiftNumber", String.valueOf(shiftNumber));
		head.setAttribute("number", String.valueOf(number));
		head.setAttribute("date", String.valueOf(date));
		rootElement.appendChild(head);

		for (InternalGoods pos : goods) {
			Element node = checkXML.createElement("position");
			node.setAttribute("goodsName", pos.goodsName);
			node.setAttribute("posNumber", String.valueOf(pos.posNumber));
			node.setAttribute("departNumber", String.valueOf(pos.departNumber));
			node.setAttribute("quantity", String.valueOf(pos.quantity));
			node.setAttribute("price", String.valueOf(pos.price));
			head.appendChild(node);
			for (InternalDisc disc : pos.discs) {
				Element aNode = checkXML.createElement("posDiscount");
				aNode.setAttribute("name", disc.name);
				aNode.setAttribute("type", disc.type.toString());
				aNode.setAttribute("value", String.valueOf(disc.value));
				node.appendChild(aNode);
			}
			for (InternalMargin margin : pos.margins) {
				Element aNode = checkXML.createElement("posMargin");
				aNode.setAttribute("name", margin.name);
				aNode.setAttribute("type", margin.type.toString());
				aNode.setAttribute("value", String.valueOf(margin.value));
				node.appendChild(aNode);
			}
		}

		for (InternalDisc disc : discs) {
			Element aNode = checkXML.createElement("discount");
			aNode.setAttribute("name", disc.name);
			aNode.setAttribute("type", disc.type.toString());
			aNode.setAttribute("value", String.valueOf(disc.value));
			head.appendChild(aNode);
		}
		for (InternalMargin margin : margins) {
			Element aNode = checkXML.createElement("margin");
			aNode.setAttribute("name", margin.name);
			aNode.setAttribute("type", margin.type.toString());
			aNode.setAttribute("value", String.valueOf(margin.value));
			head.appendChild(aNode);
		}

		for (InternalPayments pay : payments) {
			Element node = checkXML.createElement("payment");
			node.setAttribute("index", String.valueOf(pay.index));
			node.setAttribute("value", String.valueOf(pay.value));
			head.appendChild(node);
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(checkXML);
		StreamResult result = new StreamResult(new File(checkFile));
		transformer.transform(source, result);
	}

	public void clear() throws Exception {
		File file = new File(checkFile);
		if (file.exists()) {
			file.delete();
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		checkXML = dBuilder.newDocument();

		type = null;
		shiftNumber = 0;
		number = 0;
		date = null;
		state = CheckState.CLOSED;

		goods = new ArrayList<>();
		discs = new ArrayList<>();
		margins = new ArrayList<>();
		payments = new ArrayList<>();
	}

	public boolean isOpen() {
		return type != null;
	}

	public void setType(CheckType type) throws Exception {
		if (this.type == type) {
            return;
        }
		this.type = type;
		store();
	}

	public void setShiftNumber(Long shiftNum) throws Exception {
		if (this.shiftNumber == shiftNum) {
            return;
        }
		this.shiftNumber = shiftNum;
		store();
	}

	public void addGoods(InternalGoods pos) throws Exception {
		goods.add(pos);
		store();
	}

	public void addPayment(InternalPayments pay) throws Exception {
		long checkSum = getCheckSum();
		long cashlessSum = getCashlessSum();

		if (!isCashPresent() && pay.index!=0 && checkSum < cashlessSum + pay.value) {
            throw new FiscalPrinterException("Сумма оплат больше суммы чека (суммы чека = " + checkSum + " , оплаты = " + (cashlessSum + pay.value) + ")");
        }
		payments.add(pay);
		store();
	}

	private boolean isCashPresent() {
		for (InternalPayments pay : payments) {
			if (pay.index == 0 && pay.value > 0) {
                return true;
            }
		}
		return false;
	}

	public long getCheckSum() {
		long result = 0;
		for (InternalGoods pos : goods) {
			result += (pos.quantity * pos.price) / 1000;
			for (InternalDisc disc : pos.discs) {
				result -= disc.value;
			}
			for (InternalMargin margin : pos.margins) {
				result += margin.value;
			}
		}
		for (InternalDisc disc : discs) {
			result -= disc.value;
		}
		for (InternalMargin margin : margins) {
			result += margin.value;
		}
		return result;
	}

	public long getCashSum() {
		return getCashSum(true);
	}

	public long getCashSum(boolean backInclude) {
		long result = 0;
		long cashlessSum = 0;
		long checkSum = getCheckSum();
		for (InternalPayments pay : payments) {
			if (pay.index == 0) {
                result += pay.value;
            } else if(pay.index < 13) {
                cashlessSum += pay.value;
            }
		}
		if (backInclude && cashlessSum + result > checkSum) {
            result = checkSum - cashlessSum;
        }
		return result;
	}

	public long getCashlessSum() {
		long result = 0;
		for (InternalPayments pay : payments) {
			if (pay.index > 0 && pay.index < 13) {
                result += pay.value;
            }
		}
		return result;
	}

	private long getPaymentSumByIndex(long index) {
		long result = 0;
		for(InternalPayments pay : payments) {
			if(pay.index == index) {
				result += pay.value;
			}
		}
		return result;
	}

	public long getPrePaymentSum() {
		return getPaymentSumByIndex(13);
	}

	public long getPostPaymentSum() {
		return getPaymentSumByIndex(14);
	}

	public long getOtherPaySum() {
		return getPaymentSumByIndex(15);
	}

	public void addDiscount(boolean positional, InternalDisc disc) throws Exception {
		if (positional) {
			if (goods.size() == 0) {
                throw new FiscalPrinterException("Отсутствует позиция чека");
            }
			goods.get(goods.size() - 1).discs.add(disc);
		} else {
			discs.add(disc);
		}
		store();
	}

	public void addMargin(boolean positional, InternalMargin margin) throws Exception {
		if (positional) {
			if (goods.isEmpty()) {
                throw new FiscalPrinterException("Отсутствует позиция чека");
            }
			goods.get(goods.size() - 1).margins.add(margin);
		} else {
			margins.add(margin);
		}
		store();
	}

	public Date getDate() {
		return date == null ? Calendar.getInstance().getTime() : date;
	}

	public CheckState getState() {
		return state;
	}

	public void setState(CheckState state) throws Exception {
		if (this.state == state) {
            return;
        }
		this.state = state;
		store();
	}

    public CashOperation getOperation() {
        return operation;
    }

    public void setOperation(CashOperation operation) throws Exception {
        if (this.operation == operation) {
            return;
        }
        this.operation = operation;
        store();
    }
}
