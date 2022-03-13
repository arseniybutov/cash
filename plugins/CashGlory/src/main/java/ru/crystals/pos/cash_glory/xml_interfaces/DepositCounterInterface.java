package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.ArrayList;

import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

public interface DepositCounterInterface {
	public ArrayList<DenominationInterface> getDenominations();
}
