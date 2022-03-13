package ru.crystals.pos.cash_glory.handlers;

import org.apache.commons.lang.exception.ExceptionUtils;
import ru.crystals.pos.cash_glory.TCPEventsServer;
import ru.crystals.pos.cash_glory.xml_interfaces.DefaultEvents;
import ru.crystals.pos.cash_glory.xml_interfaces.DepositCounterInterface;
import ru.crystals.pos.cash_glory.xml_interfaces.FactoryEvents;
import ru.crystals.pos.cash_glory.xml_interfaces.GlyCashierEvents;
import ru.crystals.pos.cash_machine.Constants;

import javax.xml.bind.JAXBException;
import java.util.concurrent.BlockingQueue;

/**
 * Читает из очереди ивенты.
 * Обрабатывает {@link FactoryEvents#GLYCASHIER} и прочие
 */
public class EventHandler implements Runnable {

    private BlockingQueue<TCPEventsServer.WorkUnit> events;

    public EventHandler(BlockingQueue<TCPEventsServer.WorkUnit> events) {
        this.events = events;
    }

    @Override
    public void run() {
        TCPEventsServer.WorkUnit workUnit = null;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Constants.LOG.debug("{} Taking event from queue. thread: {}", TCPEventsServer.TAG, Thread.currentThread().getName());
                workUnit = events.take();
                Constants.LOG.debug("{} Took event from queue. Size now: {}, thread: {}", TCPEventsServer.TAG, events.size(), Thread.currentThread().getName());
                switch (workUnit.getRequestType()) {
                    case GLYCASHIER:
                        parseGlyCashierEvent(workUnit);
                        break;
                    default:
                        parseOtherEvent(workUnit);
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Constants.LOG.debug("{} Shutdown event handler, thread: {}", TCPEventsServer.TAG, Thread.currentThread().getName());
            } catch (JAXBException e) {
                Constants.LOG.error("{} String response: {}. Stacktrace: {}", TCPEventsServer.TAG, workUnit.getContent(), ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    private void parseGlyCashierEvent(TCPEventsServer.WorkUnit workUnit) throws JAXBException {
        Constants.LOG.debug("{} Parsing XML GLYCASHIER: {}, thread: {}", TCPEventsServer.TAG, workUnit.getContent().toString(), Thread.currentThread().getName());
        GlyCashierEvents gcEvent = (GlyCashierEvents) FactoryEvents.GLYCASHIER.unmarshal(workUnit.getContent());
        DepositCounterInterface eventDepositCountChange = gcEvent.getEventDepositCountChange();
        DepositCounterInterface eventDepositCountMonitor = gcEvent.getEventDepositCountMonitor();
        if (eventDepositCountChange != null && !eventDepositCountChange.getDenominations().isEmpty()) {
            gcEvent.setEventDepositCountChange(eventDepositCountChange);
        } else if (eventDepositCountMonitor != null && !eventDepositCountMonitor.getDenominations().isEmpty()) {
            gcEvent.setEventDepositCountMonitor(eventDepositCountMonitor);
        }
    }

    private void parseOtherEvent(TCPEventsServer.WorkUnit workUnit) throws JAXBException {
        Constants.LOG.debug("{} Parsing XML default: {}, thread: {}", TCPEventsServer.TAG, workUnit.getContent().toString(), Thread.currentThread().getName());
        workUnit.getContent().insert(0, "<DEFAULT>").append("</DEFAULT>");
        DefaultEvents dEvents = (DefaultEvents) FactoryEvents.DEFAULT.unmarshal(workUnit.getContent());
        if (dEvents.getInventoryResponse() != null) {
            dEvents.setInventoryResponse(dEvents.getInventoryResponse());
        }
    }

}
