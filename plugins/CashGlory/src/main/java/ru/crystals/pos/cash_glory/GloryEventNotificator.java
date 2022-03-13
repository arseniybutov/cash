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

public enum GloryEventNotificator implements GloryEventInterface {

    INSTANCE;

    private GloryEventsListener listener;

    @Override
    public void fireEventStatusChange(EventStatusChange eventStatusChange) {
        listener.eventStatusChange(eventStatusChange);
    }

    @Override
    public void fireEventExist(EventExist eventExist) {
        listener.eventExist(eventExist);
    }

    @Override
    public void fireEventDepositCountChange(DepositCounterInterface eventDepositCountChange) {
        listener.eventDepositCountChange(eventDepositCountChange);
    }

    @Override
    public void fireEventDepositCountMonitor(DepositCounterInterface eventDepositCountMonitor) {
        listener.eventDepositCountMonitor(eventDepositCountMonitor);
    }

    @Override
    public void fireEventBanknoteOpened(EventOpened eventOpened) {
        listener.eventBanknoteOpened(eventOpened);
    }

    @Override
    public void fireEventBanknoteClosed(EventClosed eventClosed) {
        listener.eventBanknoteClosed(eventClosed);
    }

    @Override
    public void fireEventError(ErrorEventInterface eventError) {
        listener.eventError(eventError);
    }

    @Override
    public void fireEventWaitForBanknoteCassetteInsertion(EventWaitForInsertion eventWaitForInsertion) {
        listener.eventWaitForBanknoteCassetteInsertion(eventWaitForInsertion);
    }

    @Override
    public void fireInventoryResponse(InventoryResponse inventoryResponse) {
        listener.eventInventoryResponse(inventoryResponse);
    }

    @Override
    public void fireEventBanknoteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval) {
        listener.eventBanknoteCassetteInventoryOnRemoval(cassetteInventoryOnRemoval);
    }

    @Override
    public void fireEventBanknoteCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion) {
        listener.eventBanknoteCassetteInventoryOnInsertion(eventCassetteInventoryOnInsertion);
    }

    @Override
    public void setListener(GloryEventsListener listener) {
        this.listener = listener;
    }

    @Override
    public void fireEventCoinCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval) {
        listener.eventCoinCassetteInventoryOnRemoval(cassetteInventoryOnRemoval);
    }

    @Override
    public void fireEventCoinCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion) {
        listener.eventCoinCassetteInventoryOnInsertion(eventCassetteInventoryOnInsertion);
    }

    @Override
    public void fireEventCoinOpened(EventOpened eventOpened) {
        listener.eventCoinOpened(eventOpened);
    }

    @Override
    public void fireEventCoinClosed(EventClosed eventClosed) {
        listener.eventCoinClosed(eventClosed);
    }

    @Override
    public void fireEventWaitForCoinCassetteInsertion(EventWaitForInsertion eventWaitForInsertion) {
        listener.eventWaitForCoinCassetteInsertion(eventWaitForInsertion);
    }
}
