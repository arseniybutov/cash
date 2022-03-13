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


public interface GloryEventInterface {

    void fireEventStatusChange(EventStatusChange eventStatusChange);

    void fireEventExist(EventExist eventExist);

    void fireEventDepositCountChange(DepositCounterInterface eventDepositCountChange);

    void fireEventDepositCountMonitor(DepositCounterInterface eventDepositCountMonitor);

    void fireEventBanknoteOpened(EventOpened eventOpened);

    void fireEventBanknoteClosed(EventClosed eventClosed);

    void fireEventError(ErrorEventInterface eventError);

    void fireEventWaitForBanknoteCassetteInsertion(EventWaitForInsertion eventWaitForInsertion);

    void fireInventoryResponse(InventoryResponse inventoryResponse);

    void fireEventBanknoteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval);

    void fireEventBanknoteCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion);

    void setListener(GloryEventsListener listener);

    void fireEventCoinCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval);

    void fireEventCoinCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion);

    void fireEventCoinOpened(EventOpened eventOpened);

    void fireEventCoinClosed(EventClosed eventClosed);

    void fireEventWaitForCoinCassetteInsertion(EventWaitForInsertion eventWaitForInsertion);

}
