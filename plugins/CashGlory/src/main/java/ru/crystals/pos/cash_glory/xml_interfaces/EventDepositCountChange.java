package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.ArrayList;

import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

/**
 * The calculation data of payment is notified.
 * This event is notified of after receipt of money calculation completion.
 * 
 * @author p.tykvin
 * 
 */
public class EventDepositCountChange extends ArrayList<DenominationInterface> implements DepositCounterInterface {

	private static final long serialVersionUID = 1L;

	@Override
	public ArrayList<DenominationInterface> getDenominations() {
		return this;
	}

}
