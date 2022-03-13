package ru.crystals.pos.cash_glory.xml_interfaces;

import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import ru.crystals.pos.cash_machine.Constants;

public enum FactoryEvents {

	HEARTBEAT("</HeartBeatEvent>", new Class[] { HeartBeatEvents.class }),
	GLYCASHIER("</GlyCashierEvent>", new Class[] {
			GlyCashierEvents.class,
			Cash.class,
			Denomination.class,
			EventCassetteInventoryOnRemoval.class,
			EventCassetteInventoryOnInsertion.class,
			EventClosed.class,
			EventDepositCountChange.class,
			EventDepositCountMonitor.class,
			EventError.class,
			EventExist.class,
			EventOpened.class,
			EventStatusChange.class,
 EventWaitForInsertion.class
	}),
	DEFAULT("</DEFAULT>", new Class[] { DefaultEvents.class, EventStatusChange.class, InventoryResponse.class, Cash.class, CashUnits.class, CashUnit.class });

	private Class<?>[] classes;
	private JAXBContext jc;
	private Unmarshaller um;
	private String type;

	static public FactoryEvents getType(String pType) {
		for (FactoryEvents t : FactoryEvents.values()) {
			if (pType.equals(t.getType())) {
				return t;
			}
		}
		return DEFAULT;
	}

	private FactoryEvents(String type, Class<?>[] classes) {
		this.classes = classes;
		this.type = type;
		init();
	}

	private void init() {
		try {
			jc = JAXBContext.newInstance(classes);
			um = jc.createUnmarshaller();
		} catch (JAXBException e) {
            Constants.LOG.error("create unmarshaller: ", e);
		}
	}

	public Object unmarshal(StringBuilder sb) throws JAXBException {
		StringReader reader = new StringReader(sb.toString());
        return um.unmarshal(reader);
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return type;
	}

}
