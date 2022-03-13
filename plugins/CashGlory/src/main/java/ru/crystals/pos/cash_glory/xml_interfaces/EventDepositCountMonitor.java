package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

/**
 * The calculation data of the payment completion is notified.
 * 
 * @author p.tykvin
 * 
 */
@XmlType
public class EventDepositCountMonitor extends ArrayList<DenominationInterface> implements DepositCounterInterface {

	private static final long serialVersionUID = 1L;

	@Override
	public ArrayList<DenominationInterface> getDenominations() {
		return this;
	}

}
