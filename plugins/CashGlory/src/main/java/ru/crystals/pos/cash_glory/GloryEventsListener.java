package ru.crystals.pos.cash_glory;

import ru.crystals.pos.cash_glory.xml_interfaces.DepositCounterInterface;
import ru.crystals.pos.cash_glory.xml_interfaces.EventCassetteInventoryOnInsertion;
import ru.crystals.pos.cash_glory.xml_interfaces.EventCassetteInventoryOnRemoval;
import ru.crystals.pos.cash_glory.xml_interfaces.EventClosed;
import ru.crystals.pos.cash_glory.xml_interfaces.EventExist;
import ru.crystals.pos.cash_glory.xml_interfaces.EventOpened;
import ru.crystals.pos.cash_glory.xml_interfaces.EventStatusChange;
import ru.crystals.pos.cash_glory.xml_interfaces.EventWaitForInsertion;
import ru.crystals.pos.cash_glory.xml_interfaces.InventoryResponse;
import ru.crystals.pos.cash_machine.ErrorEventInterface;

public interface GloryEventsListener {

    void eventStatusChange(EventStatusChange e);

    void eventBanknoteOpened(EventOpened eventOpened);

    void eventExist(EventExist eventExist);

    void eventInventoryResponse(InventoryResponse inventoryResponse);

    void eventDepositCountChange(DepositCounterInterface eventDepositCountChange);

    void eventDepositCountMonitor(DepositCounterInterface eventDepositCountMonitor);

    void eventBanknoteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval);

    void eventBanknoteCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion);

    void eventBanknoteClosed(EventClosed eventClosed);

    void eventError(ErrorEventInterface eventError);

    void eventWaitForBanknoteCassetteInsertion(EventWaitForInsertion eventWaitForInsertion);

    void eventCoinCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval);

    void eventCoinCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion);

    void eventCoinClosed(EventClosed eventClosed);

    void eventCoinOpened(EventOpened eventOpened);

    void eventWaitForCoinCassetteInsertion(EventWaitForInsertion eventWaitForInsertion);
}
