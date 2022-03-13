package ru.crystals.pos.cash_glory;

import jp.co.glory.bruebox.CashType;
import jp.co.glory.bruebox.DenominationType;
import jp.co.glory.bruebox.StatusResponseType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import ru.crystals.pos.cash_glory.constants.DeviceType;
import ru.crystals.pos.cash_machine.CashEventsNotificator;
import ru.crystals.pos.cash_machine.CashMachinePluginInterface;
import ru.crystals.pos.cash_machine.CashMachineStateListener;
import ru.crystals.pos.cash_machine.Constants;
import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.StatusChange;
import ru.crystals.pos.cash_machine.callbacks.StatusChangeCallback;
import ru.crystals.pos.cash_machine.entities.interfaces.CashInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.InventoryResponseInterface;
import ru.crystals.pos.cash_machine.exceptions.CashMachineChangeShortageException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineErrorException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineNeedToResolveErrorException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineOfflineException;
import ru.crystals.pos.cash_machine.exceptions.CashMachineUnsupportedOperationException;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class GloryPlugin extends StatusChangeCallback implements CashMachinePluginInterface {

    private static final String TAG = "[GloryPlugin] ";
    private String ipAddr;
    private CashGloryFacadeInterface facade;
    private int eventPort = 55565;

    private StatusChange status = null;
    //private GloryConverter converter = new GloryConverter();
    private CashEventsNotificator cashEventsNotificator;
    private CashMachineStateListener cashMachineStateListener;
    private CashMachineErrorException error;
    private TCPEventsServer tcpServer;
    private boolean online = true; //мыслим позитивно

    private ChangeValidatorInterface changeValidator = new ChangeValidatorInterface() {
        @Override
        public boolean isChangeAvailable(Long amount) throws CashMachineException {
            if (tcpServer == null || !tcpServer.isAlive()) {
                throw new CashMachineOfflineException();
            }
            InventoryResponseInterface inventory = getInventory();
            CashInterface cash = inventory.getDispensableCash();
            List<? extends DenominationInterface> denominations = cash.getDenomintaions();
            long value = denominations.get(0).getValueInf();
            for (DenominationInterface item : denominations) {
                if (item.getValueInf() < value) {
                    value = item.getValueInf();
                }
            }
            return amount % value == 0;
        }
    };

    {
        name = "GloryPlugin";
    }

    private AbstractAction reanimateCallback = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(final ActionEvent e) {
            Executors.newCachedThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String reason = e == null ? "" : e.getActionCommand();
                        Constants.LOG.error("{}*REANIMATE* Get reanimating request by reason \"{}\". Start reanimating.", TAG, reason);
                        Constants.LOG.error("{}*REANIMATE* Remake facade...", TAG);
                        if (facade == null) {
                            facade = new CashGloryFacade(ipAddr, eventPort);
                        } else {
                            online = false;
                            if (cashMachineStateListener != null) {
                                cashMachineStateListener.cashMachineOffline();
                            }
                        }
                        if (facade != null && facade.isAlive()) {
                            Constants.LOG.error("{}*REANIMATE* Remake eventServer...", TAG);
                            initEventServer();
                        } else {
                            throw new Exception("Cannot init connection cause facade is null or not alive");
                        }
                        facade.close();
                        online = true;
                        if (cashMachineStateListener != null) {
                            cashMachineStateListener.cashMachineOnline();
                        }
                    } catch (Exception e1) {
                        Constants.LOG.error("{}*REANIMATE* Reanimation failure. Retry in 5 sec... error: {}", TAG, e1.getMessage());
                        Constants.LOG.error("{}*REANIMATE* facade is exist: {}, tcpServer is exist: {}", TAG, facade != null, tcpServer != null);
                        Constants.LOG.error("{}*REANIMATE* cash machine is online: {}", TAG, online);
                        status = null;
                        if (tcpServer == null) {
                            facade = null;
                        }
                        try {
                            Thread.sleep(5000);
                            actionPerformed(e);
                        } catch (InterruptedException e) {
                            Constants.LOG.error("{}{}", TAG, ExceptionUtils.getFullStackTrace(e));
                        }
                    }
                }

            });

        }

    };

    @Override
    public void cashRequest() throws CashMachineNeedToResolveErrorException {
        Constants.LOG.info("{} method cashRequest() Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        if (error != null) {
            throw new CashMachineNeedToResolveErrorException();
        }
        Response result = facade.cashIn();
        switch (result) {
            case EXCLUSIVE_ERROR:
                cashEventsNotificator.fireException(new CashMachineUnsupportedOperationException());
                waitIdle();
                cashRequest();
            case SUCCESS:
                toWaitInsertionState();
            default:
                break;

        }
    }

    @Override
    public void stopCashIn() {
        Constants.LOG.info("{} method stopCashIn() Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        toWaitInsertionState();
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                facade.cashEnd();
            }
        });
    }

    @Override
    public void cancelCashIn() throws CashMachineUnsupportedOperationException {
        Constants.LOG.info("{} method cancelCashIn() Currentthread.name() = {}", TAG, Thread.currentThread().getName());
    	if(!tcpServer.isGloryConnected()) {
    		throw new CashMachineUnsupportedOperationException();
    	}
        Response result = facade.cancelCashIn();
        if (result == Response.EXCLUSIVE_ERROR) {
            throw new CashMachineUnsupportedOperationException();
        }
    }

    @Override
    public void reset(boolean needWaitForRequest) throws CashMachineErrorException, CashMachineUnsupportedOperationException {
        Constants.LOG.info("{} method reset({}) Currentthread.name() = {}", TAG, needWaitForRequest, Thread.currentThread().getName());
        error = null;
        Response result = facade.reset();
        if (result != Response.SUCCESS) {
            if (result == Response.DEVICE_ERROR) {
                waitError();
                throw error;
            }
            throw new CashMachineUnsupportedOperationException();
        }
        int s = 0;
        Constants.LOG.info("{} method reset: Wait for 8 or 1 Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        while (s != 8 && s != 1) {
            s = getStatus().getStatus();
            Thread.yield();
        }
        Constants.LOG.info("{} method reset: Wait for 8 or 1: READY. Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        error = null;
        s = 0;
        if (needWaitForRequest) {
            Constants.LOG.info("{} method reset: Wait for 2 or 3 or 4 or error Currentthread.name() = {}", TAG, Thread.currentThread().getName());
            while (s != 2 && s != 3 && s != 4 && error == null) {
                s = getStatus().getStatus();
                Thread.yield();
            }
            Constants.LOG.info("{} method reset: Wait for 2 or 3 or 4 or error: READY Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        }
        if (error != null) {
            throw error;
        }
        if (needWaitForRequest) {
            facade.cancelCashRequest();
        }
        waitIdle();
    }

    @Override
    public void resetControl() throws CashMachineException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            CloseableHttpResponse execute = httpClient.execute(new HttpGet("http://"+ ipAddr + "/cgi-bin/reboot.cgi"));
            //Выставляем статус -1, перед перезагрузкой, дабы потом была возможность отследить изменение оного и понять, что глори включился
            eventStatusChange(new StatusChange(-1, -1L));
            Constants.LOG.info(execute.getEntity().getContent().toString());
        } catch (IOException e) {
            Constants.LOG.error(e.getMessage(), e);
            throw new CashMachineException(e.getMessage());
        }
    }

    @Override
    public void cashOut(final long amount) throws CashMachineException {
        Constants.LOG.info("{} method cashOut({}) Currentthread.name() = {}", TAG, amount, Thread.currentThread().getName());
        Response result = facade.cashOut(amount);
        processCashOutResult(result);
    }

    @Override
    public void cashOut(List<? extends DenominationInterface> denominations) throws CashMachineException {
        Constants.LOG.info("{} method cashOut({}) Currentthread.name() = {}", TAG, denominations, Thread.currentThread().getName());
        CashType cashType = new CashType();
        denominations.stream().map(facade::convToDenomintionType).forEach(cashType::addDenomination);
        Response result = facade.cashOut(cashType);
        processCashOutResult(result);
    }

    private void processCashOutResult(Response result) throws CashMachineErrorException, CashMachineUnsupportedOperationException {
        switch (result) {
            case DEVICE_ERROR:
                waitError();
                throw error;
            case SUCCESS:
                break;
            default:
                throw new CashMachineUnsupportedOperationException();
        }
    }

    public void waitError() {
        Constants.LOG.info("{} method waitError(): Wait for error Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        while (error == null) {
            Thread.yield();
        }
        Constants.LOG.info("{} method waitError(): Wait for error: READY Currentthread.name() = {}", TAG, Thread.currentThread().getName());
    }

    @Override
    public void cashRequestSuccess(final long requaredAmount) throws CashMachineException {
        Constants.LOG.info("{} method cashRequestSuccess({}) Currentthread.name() = {}", TAG, requaredAmount, Thread.currentThread().getName());
        Response response = facade.cashRequest(requaredAmount);
        if (response == Response.CHANGE_SHORTAGE) {
            throw new CashMachineChangeShortageException();
        }
    }

    private void initEventServer() throws Exception {
        if (facade != null) {
            tcpServer = new TCPEventsServer(eventPort, reanimateCallback);
        }
    }

    @Override
    public void start() {
        Constants.LOG.info("{} method start() Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        try {
            if (!StringUtils.isEmpty(ipAddr)) {
                facade = new CashGloryFacade(ipAddr, eventPort);
                if (facade != null && facade.isAlive()) {
                    initEventServer();
                } else {
                    reanimateCallback.actionPerformed(null);
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("{} method start(): {}", TAG, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public void setCashEventsNotificator(CashEventsNotificator notificator) {
        cashEventsNotificator = notificator;
        cashEventsNotificator.setStatusChangeCallback(this);
        GloryEventsListener eventsAdapter = new GloryEventsImpl(cashEventsNotificator);
        GloryEventNotificator.INSTANCE.setListener(eventsAdapter);
    }

    private void toWaitInsertionState() {
        Constants.LOG.info("{} method waitInsertion(): Wait for 3 Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        while (getStatus().getStatus() != 3) {
            Thread.yield();
        }
        Constants.LOG.info("{} method waitInsertion(): Wait for 3: ready Currentthread.name() = {}", TAG, Thread.currentThread().getName());
    }

    /**
     * проверить состояние и если и так уже Idle, то не начинаем ожидание
     * @throws InterruptedException
     */
    @Override
    public void waitIdle() {
        Constants.LOG.info("{} method waitIdle(): Wait for 1 Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        while (getStatus().getStatus() != 1) {
            Thread.yield();
        }
        Constants.LOG.info("{} method waitIdle(): Wait for 1: READY Currentthread.name() = {}", TAG, Thread.currentThread().getName());
    }

    public String getIp() {
        return ipAddr;
    }

    public void setIp(String ip) {
        this.ipAddr = ip;
    }

    public CashGloryFacadeInterface getFacade() {
        return facade;
    }

    public void setFacade(CashGloryFacadeInterface facade) {
        this.facade = facade;
    }

    public int getEventPort() {
        return eventPort;
    }

    public void setEventPort(int eventPort) {
        this.eventPort = eventPort;
    }

    @Override
    public StatusChange getStatus() {
        if (status == null) {
            StatusResponseType s = facade.getStatus();
            status = new StatusChange(s.getStatus().getCode().intValue(), 0L);
        }
        return status;
    }

    @Override
    public void eventStatusChange(StatusChange status) {
        Constants.LOG.info("{} method eventStatusChange(StatusChange) CURRENT STATUS IS {} Currentthread.name() = {}", TAG, status.getStatus(),
            Thread.currentThread().getName());
        this.status = status;
        StateValidator.setStatus(status);
    }

    @Override
    public InventoryResponseInterface getInventory() {
        while (getStatus().getStatus() == 19) {
            Thread.yield();
        }
        InventoryResponseInterface inventory = facade.inventory();
        while (inventory == null || inventory.getResponse() != Response.SUCCESS) {
            Constants.LOG.info("{} Inventory is null or not SUCCESS. Repeat Currentthread.name() = {}", TAG, Thread.currentThread().getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            inventory = facade.inventory();
        }
        return inventory;
    }

    @Override
    public void collect(final List<? extends DenominationInterface> denominations, final boolean collectMix, final boolean toExit) throws Exception {
        Constants.LOG.info("{} method collect(any-args), Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        CashType cash = new CashType();
        List<DenominationType> list = new ArrayList<DenominationType>();
        for (DenominationInterface d : denominations) {
            list.add(facade.convToDenomintionType(d));
        }
        cash.setDenomination(list);
        Response result = facade.collect(cash, collectMix, toExit);
        if (result == Response.EXCLUSIVE_ERROR) {
            throw new CashMachineUnsupportedOperationException();
        }
    }

    @Override
    public void cashIn() throws CashMachineUnsupportedOperationException {
        Constants.LOG.info("{} cashIn(), Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        Response result = facade.cashIn();
        if (result == Response.EXCLUSIVE_ERROR) {
            throw new CashMachineUnsupportedOperationException();
        }
        toWaitInsertionState();
    }

    @Override
    public void eventError(CashMachineErrorException e) {
        Constants.LOG.info("{} method eventError(CashMachineErrorException), Currentthread.name() = {}", TAG, Thread.currentThread().getName());
        this.error = e;
    }

    @Override
    public boolean isChangeAvailable(Long amount) throws CashMachineException {
        return changeValidator.isChangeAvailable(amount);
    }

    public ChangeValidatorInterface getChangeValidator() {
        return changeValidator;
    }

    public void setChangeValidator(ChangeValidatorInterface changeValidator) {
        this.changeValidator = changeValidator;
    }

    @Override
    public CashMachineStateListener getCashMachineStateListener() {
        return cashMachineStateListener;
    }

    @Override
    public void setCashMachineStateListener(CashMachineStateListener cashMachineStateListener) {
        this.cashMachineStateListener = cashMachineStateListener;
    }

    @Override
    public boolean isOnline(){
        boolean localOnline = online;
        Constants.LOG.debug("'Online' var value is: {}", localOnline);
        TCPEventsServer localTCPServer = tcpServer;
        boolean tcpServerAvailable = localTCPServer != null;
        Constants.LOG.debug("Server is available: {}", tcpServerAvailable);
        CashGloryFacadeInterface localFacade = facade;
        boolean cashGloryAvailable = localFacade != null && localFacade.isAccessibleByIP(300);
        Constants.LOG.debug("Accessible by IP: {}", cashGloryAvailable);
        return localOnline && tcpServerAvailable && cashGloryAvailable;
    }

    @Override
    public void lockBanknoteCassette() throws CashMachineUnsupportedOperationException {
        lockCassette(DeviceType.RBW);
    }

    @Override
    public void unlockBanknoteCassette() throws CashMachineUnsupportedOperationException {
        unlockCassette(DeviceType.RBW);
    }

    @Override
    public void lockCoinCassette() throws CashMachineUnsupportedOperationException {
        lockCassette(DeviceType.RCW);
    }

    @Override
    public void unlockCoinCassette() throws CashMachineUnsupportedOperationException {
        unlockCassette(DeviceType.RCW);
    }

    @Override
    public void removeBanknoteCassette() {
        removeCassette(DeviceType.RBW);
    }

    @Override
    public void cancelBanknoteCassetteRemove() {
        cancelCassetteRemove(DeviceType.RBW);
    }

    @Override
    public void removeCoinCassette() {
        removeCassette(DeviceType.RCW);
    }

    @Override
    public void cancelCoinCassetteRemove() {
        cancelCassetteRemove(DeviceType.RCW);
    }

    private void unlockCassette(DeviceType deviceType) throws CashMachineUnsupportedOperationException {
        Response response = facade.unlock(deviceType);
        if (response == Response.EXCLUSIVE_ERROR) {
            throw new CashMachineUnsupportedOperationException();
        }
    }

    private void lockCassette(DeviceType deviceType) throws CashMachineUnsupportedOperationException {
        Response response = facade.lock(deviceType);
        if (response == Response.EXCLUSIVE_ERROR) {
            throw new CashMachineUnsupportedOperationException();
        }
    }

    private void removeCassette(DeviceType deviceType) {
        Constants.LOG.info("{} method removeCassette() {}, Currentthread.name() = {}", TAG, deviceType, Thread.currentThread().getName());
        facade.unlock(deviceType);
        Constants.LOG.info("{} method removeCassette(): {} Wait for 18 , Currentthread.name() = {}", TAG, deviceType, Thread.currentThread().getName());
        while (getStatus().getStatus() != 18) {
            Thread.yield();
        }
        Constants.LOG.info("{} method removeCassette(): {} Wait for 18: READY, Currentthread.name() = {}", TAG, deviceType, Thread.currentThread().getName());
    }

    private void cancelCassetteRemove(DeviceType deviceType) {
        Constants.LOG.info("{} method cancelCassetteRemove() {}, Currentthread.name() = {}", TAG, deviceType, Thread.currentThread().getName());
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Constants.LOG.info("{} method cancelCassetteRemove {}, Currentthread.name() = {}", TAG, deviceType, Thread.currentThread().getName());
                    facade.lock(deviceType);
                } catch (Exception e) {
                    Constants.LOG.error("{} method cancelCassetteRemove: {} {}, Currentthread.name() = {}", TAG, deviceType, ExceptionUtils.getFullStackTrace(e), Thread.currentThread().getName());
                }
            }
        });
    }

}
