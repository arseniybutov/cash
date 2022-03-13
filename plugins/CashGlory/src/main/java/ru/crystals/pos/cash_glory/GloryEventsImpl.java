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
import ru.crystals.pos.cash_machine.CashEventsNotificator;
import ru.crystals.pos.cash_machine.Constants;
import ru.crystals.pos.cash_machine.ErrorEventInterface;
import ru.crystals.pos.cash_machine.StatusChange;

public class GloryEventsImpl implements GloryEventsListener {

    private CashEventsNotificator notificator;

    public GloryEventsImpl(CashEventsNotificator notificator) {
        this.notificator = notificator;
    }

    @Override
    public void eventStatusChange(EventStatusChange e) {
        StatusChange status = new StatusChange(e.getStatus(), e.getAmount(), e.getError(), e.getRecoveryURL());
        notificator.fireStatusChange(status);
        Constants.LOG.info("[GloryEventsImpl] eventStatusChange(EventStatusChange) STATUS: " + e.getStatus());
        switch (e.getStatus()) {
            case 3:
                Constants.LOG.info("[GloryEventsImpl] STATUS: 3 - WaitingInsertionCash");
                notificator.fireWaitingInsertionCash(status);
                break;
            case 6:
                Constants.LOG.info("[GloryEventsImpl] STATUS: 6 - Wait Removal");
                Constants.LOG.info("[GloryEventsImpl] Counting: {}", status.getAmount());
                notificator.fireCounting(status);
                break;
            case 4:
                Constants.LOG.info("[GloryEventsImpl] STATUS: 4 - Counting");
                Constants.LOG.info("[GloryEventsImpl] Counting: {}", status.getAmount());
                notificator.fireCounting(status);
                break;
            case 5:
                Constants.LOG.info("[GloryEventsImpl] STATUS: 5 - Dispensing");
                //                if (status.getAmount() == 0) {
                //                    notificator.fireException(new CashMachineException(ResBundleCashMachine.getString("CHANGE_SHORTAGE")));
                //                }
                break;
            default:
                break;
        }
    }

    @Override
    public void eventBanknoteOpened(EventOpened eventOpened) {
        Constants.LOG.info("[GloryEventsImpl] eventBanknoteOpened(EventOpened), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireBanknoteOpened();
    }

    @Override
    public void eventExist(EventExist eventExist) {
        Constants.LOG.info("[GloryEventsImpl] eventExist(EventExist), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireExist();
    }

    @Override
    public void eventInventoryResponse(InventoryResponse inventoryResponse) {
        //not used
    }

    @Override
    public void eventDepositCountChange(DepositCounterInterface eventDepositCountChange) {
        Constants.LOG.info("[GloryEventsImpl] eventDepositCountChange(DepositCounterInterface), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireCountChangeMonitor(eventDepositCountChange.getDenominations());
    }

    @Override
    public void eventDepositCountMonitor(DepositCounterInterface eventDepositCountMonitor) {
        Constants.LOG.info("[GloryEventsImpl] eventDepositCountMonitor(DepositCounterInterface), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireCountChangeMonitor(eventDepositCountMonitor.getDenominations());
    }

    @Override
    public void eventBanknoteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval) {
        Constants.LOG.info("[GloryEventsImpl] eventBanknoteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval), Currentthread.name() = {}",
            Thread.currentThread().getName());
        notificator.fireBanknoteCassetteRemove();
    }

    @Override
    public void eventBanknoteCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion) {
        Constants.LOG.info("[GloryEventsImpl] eventBanknoteCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion), Currentthread.name() = {}",
            Thread.currentThread().getName());
        boolean empty = eventCassetteInventoryOnInsertion.getDenominations() == null;
        notificator.fireBanknoteCassetteReturn(empty);
    }

    @Override
    public void eventBanknoteClosed(EventClosed eventClosed) {
        Constants.LOG.info("[GloryEventsImpl] eventBanknoteClosed(EventClosed), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireBanknoteClosed();
    }

    @Override
    public void eventError(ErrorEventInterface eventError) {
        Constants.LOG.info("[GloryEventsImpl] eventError(ErrorEventInterface), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireError(eventError);
    }

    @Override
    public void eventWaitForBanknoteCassetteInsertion(EventWaitForInsertion eventWaitForInsertion) {
        Constants.LOG.info("[GloryEventsImpl] eventWaitForBanknoteCassetteInsertion(EventWaitForInsertion), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireWaitForBanknoteCassetteInsertion();
    }

    @Override
    public void eventCoinCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval cassetteInventoryOnRemoval) {
        Constants.LOG.info("[GloryEventsImpl] eventCoinCassetteCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval), Currentthread.name() = {}",
                Thread.currentThread().getName());
        notificator.fireCoinCassetteRemove();
    }

    @Override
    public void eventCoinCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion) {
        Constants.LOG.info("[GloryEventsImpl] eventCoinCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion), Currentthread.name() = {}",
                Thread.currentThread().getName());
        boolean empty = eventCassetteInventoryOnInsertion.getDenominations() == null;
        notificator.fireCoinCassetteReturn(empty);
    }

    @Override
    public void eventCoinOpened(EventOpened eventOpened) {
        Constants.LOG.info("[GloryEventsImpl] eventCoinOpened(EventOpened), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireCoinOpened();
    }

    @Override
    public void eventCoinClosed(EventClosed eventClosed) {
        Constants.LOG.info("[GloryEventsImpl] eventCoinClosed(EventClosed), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireCoinClosed();
    }

    @Override
    public void eventWaitForCoinCassetteInsertion(EventWaitForInsertion eventWaitForInsertion) {
        Constants.LOG.info("[GloryEventsImpl] eventWaitForCoinCassetteInsertion(EventWaitForInsertion), Currentthread.name() = {}", Thread.currentThread().getName());
        notificator.fireWaitForCoinCassetteInsertion();
    }
}
