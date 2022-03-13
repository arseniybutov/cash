package ru.crystals.pos.cash_glory.xml_interfaces;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GlyCashierEvent")
@XmlAccessorType(XmlAccessType.NONE)
public class GlyCashierEvents extends AbstractEvents {

    private static final int BANKNOTE_DEVICE_ID = 1;
    private static final int COIN_DEVICE_ID = 2;
    private int deviceId;
    private CountDownLatch l = new CountDownLatch(1);

    public GlyCashierEvents() {
        //зачем? Thread.currentThread().setName("EVENT THREAD");
    }

    @XmlAttribute
    public void setDevid(String devid) {
        deviceId = Integer.valueOf(devid);
        l.countDown();
    }

    @XmlAttribute
    public void setUser(String user) {

    }

    @XmlElement
    public void setEventCassetteInventoryOnRemoval(EventCassetteInventoryOnRemoval eventCassetteInventoryOnRemoval) {
        if (deviceId == BANKNOTE_DEVICE_ID) {
            notificator.fireEventBanknoteCassetteInventoryOnRemoval(eventCassetteInventoryOnRemoval);
        } else if (deviceId == COIN_DEVICE_ID) {
            notificator.fireEventCoinCassetteInventoryOnRemoval(eventCassetteInventoryOnRemoval);
        }
    }

    @XmlElement
    public void setEventExist(EventExist eventExist) {
        notificator.fireEventExist(eventExist);
    }

    @XmlElement
    public void setEventCassetteInventoryOnInsertion(EventCassetteInventoryOnInsertion eventCassetteInventoryOnInsertion) {
        if (deviceId == BANKNOTE_DEVICE_ID) {
            notificator.fireEventBanknoteCassetteInventoryOnInsertion(eventCassetteInventoryOnInsertion);
        } else if (deviceId == COIN_DEVICE_ID) {
            notificator.fireEventCoinCassetteInventoryOnInsertion(eventCassetteInventoryOnInsertion);
        }
    }

    @XmlElementWrapper(name = "eventDepositCountChange")
    @XmlElement(name = "Denomination", type = Denomination.class)
    private EventDepositCountChange eventDepositCountChange;

    @XmlElementWrapper(name = "eventDepositCountMonitor")
    @XmlElement(name = "Denomination", type = Denomination.class)
    private EventDepositCountMonitor eventDepositCountMonitor;

    public void setEventDepositCountChange(DepositCounterInterface eventDepositCountChange) {
        notificator.fireEventDepositCountChange(eventDepositCountChange);
    }

    public void setEventDepositCountMonitor(DepositCounterInterface eventDepositCountMonitor) {
        notificator.fireEventDepositCountMonitor(eventDepositCountMonitor);
    }

    @XmlElement
    public void setEventError(final EventError eventError) {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    l.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                eventError.setDeviceId(deviceId);
                notificator.fireEventError(eventError);
            }
        });
    }

    @XmlElement
    public void setEventOpened(EventOpened eventOpened) {
        if (deviceId == BANKNOTE_DEVICE_ID) {
            notificator.fireEventBanknoteOpened(eventOpened);
        } else if (deviceId == COIN_DEVICE_ID) {
            notificator.fireEventCoinOpened(eventOpened);
        }
    }

    @XmlElement
    public void setEventClosed(EventClosed eventClosed) {
        if (deviceId == BANKNOTE_DEVICE_ID) {
            notificator.fireEventBanknoteClosed(eventClosed);
        } else if (deviceId == COIN_DEVICE_ID) {
            notificator.fireEventCoinClosed(eventClosed);
        }
    }

    @XmlElement
    public void setEventWaitForInsertion(EventWaitForInsertion eventWaitForInsertion) {
        if (deviceId == BANKNOTE_DEVICE_ID) {
            notificator.fireEventWaitForBanknoteCassetteInsertion(eventWaitForInsertion);
        } else if (deviceId == COIN_DEVICE_ID) {
            notificator.fireEventWaitForCoinCassetteInsertion(eventWaitForInsertion);
        }
    }

    public DepositCounterInterface getEventDepositCountChange() {
        return eventDepositCountChange;
    }

    public DepositCounterInterface getEventDepositCountMonitor() {
        return eventDepositCountMonitor;
    }

}
