package ru.crystals.pos.cash_glory;

import org.apache.commons.lang.exception.ExceptionUtils;
import ru.crystals.pos.cash_glory.handlers.EventHandler;
import ru.crystals.pos.cash_glory.handlers.HeartbeatHandler;
import ru.crystals.pos.cash_glory.handlers.SocketHandler;
import ru.crystals.pos.cash_glory.xml_interfaces.FactoryEvents;
import ru.crystals.pos.cash_machine.Constants;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class TCPEventsServer {

    public enum ReanimationReason {
        HEARTBEAT_NOT_STARTED(0, "Heartbeat not started"),
        HEARTBEAT_IS_LOST(1, "Heartbeat is lost"),
        CANT_OPEN_SOCKET(2, "Can't open socket");
        private String description;
        private int id;

        ReanimationReason(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public ActionEvent getActionEvent() {
            return new ActionEvent(this, id, description);
        }
    }

    public static final String TAG = "[TCPEventsServer]";

    private Action reanimateCallback;
    private final AtomicLong lastHeartbeatTime = new AtomicLong();
    private boolean alive = true;
    volatile boolean gloryConnected = false;
    private Timer timer;

    private SocketHandler socketHandler;
    private Thread socketHandlerThread;
    private Thread eventHandlerThread;

    public TCPEventsServer(int port, Action reanimateCallback) {
        Constants.LOG.info("{} Create EventServer {}", TAG, Thread.currentThread().getName());
        this.reanimateCallback = reanimateCallback;
        initHandlers(port);
        startPulsometer();
        waitForEvents();
    }

    private void initHandlers(int port) {
        BlockingQueue<WorkUnit> events = new LinkedBlockingQueue<>();
        socketHandler = new SocketHandler(port, events, lastHeartbeatTime, this::startReanimate);
        socketHandlerThread = new Thread(socketHandler, "SocketHandlerThread");
        eventHandlerThread = new Thread(new EventHandler(events), "EventHandlerThread");
    }

    private void closeSocket() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (socketHandler.getServer() != null) {
                Constants.LOG.debug("{} Try to close socket {}", TAG, Thread.currentThread().getName());
                socketHandlerThread.interrupt();
                socketHandlerThread.join();
                socketHandler.getServer().close();
                Constants.LOG.debug("{} Closed socket {}", TAG, Thread.currentThread().getName());
            }
            eventHandlerThread.interrupt();
            eventHandlerThread.join();
            Constants.LOG.debug("{} Stop event handler {}", TAG, Thread.currentThread().getName());
        } catch (Exception e) {
            Constants.LOG.error("{} Can't close socket! Throw:{}", TAG, ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Constants.LOG.error("{} Finalize", TAG);
        closeSocket();
    }

    private void startPulsometer() {
        Constants.LOG.debug("{} Wait for heartbeat {}", TAG, Thread.currentThread().getName());
        timer = new Timer();
        HeartbeatHandler heartbeatHandler = new HeartbeatHandler(this, this::startReanimate, lastHeartbeatTime);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    heartbeatHandler.actionPerformed(null);
                } catch (Exception e) {
                    Constants.LOG.error(e.getMessage(), e);
                }
            }
        }, 1000, 1000);
    }

    private void startReanimate(ReanimationReason heartbeatNotStarted) {
    	gloryConnected = false;
        if (reanimateCallback != null) {
            alive = false;
            closeSocket();
            reanimateCallback.actionPerformed(heartbeatNotStarted.getActionEvent());
        }
    }

    public void waitForEvents() {
        Constants.LOG.info("{} Start server. {}", TAG, Thread.currentThread().getName());
        socketHandlerThread.start();
        eventHandlerThread.start();
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isGloryConnected() {
    	return gloryConnected;
    }

    public void setGloryConnected() {
        gloryConnected = true;
    }

    /**
     * Обертка над данными, которые обрабатываются с помощью очереди
     */
    public static class WorkUnit {
        private FactoryEvents requestType;
        private StringBuilder content;

        public WorkUnit(FactoryEvents requestType, StringBuilder content) {
            this.requestType = requestType;
            this.content = content;
        }

        public FactoryEvents getRequestType() {
            return requestType;
        }

        public StringBuilder getContent() {
            return content;
        }
    }

}
