package ru.crystals.pos.cash_glory.handlers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import ru.crystals.pos.cash_glory.TCPEventsServer;
import ru.crystals.pos.cash_glory.xml_interfaces.FactoryEvents;
import ru.crystals.pos.cash_machine.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Читает по сокету ивенты, парсит.
 * {@link FactoryEvents#GLYCASHIER} добавляет в очередь
 * {@link FactoryEvents#HEARTBEAT} обрабатывает сам
 */
public class SocketHandler implements Runnable {

    private int port = 55565;
    private ServerSocket server = null;
    private BlockingQueue<TCPEventsServer.WorkUnit> events;
    private final AtomicLong lastHeartbeatTime;
    private Consumer<TCPEventsServer.ReanimationReason> reactionOfServerClosed;

    public SocketHandler(int port,
                         BlockingQueue<TCPEventsServer.WorkUnit> events,
                         AtomicLong lastHeartbeatTime,
                         Consumer<TCPEventsServer.ReanimationReason> reactionOfServerClosed) {
        this.port = port;
        this.events = events;
        this.lastHeartbeatTime = lastHeartbeatTime;
        this.reactionOfServerClosed = reactionOfServerClosed;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.server = serverSocket;
            if (serverSocket.isClosed()) {
                Constants.LOG.error("{} Can't open socket!", TCPEventsServer.TAG);
                reactionOfServerClosed.accept(TCPEventsServer.ReanimationReason.CANT_OPEN_SOCKET);
                return;
            }
            Constants.LOG.info("{} Wait for connect {}", TCPEventsServer.TAG, Thread.currentThread().getName());
            try (Socket serviceSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()))) {
                Constants.LOG.info("{} Glory connected to port {} {}", TCPEventsServer.TAG, port, Thread.currentThread().getName());
                StringBuilder responseXmlContent = null;
                String responseLine;
                String responseEnd = null;
                while (!Thread.currentThread().isInterrupted()) {
                    responseLine = reader.readLine();
                    if (responseLine == null || Thread.currentThread().isInterrupted()) {
                        Constants.LOG.debug("{} This cycle is break. Is response null: {}; Is thread interrupted: {}",
                                responseLine == null, Thread.currentThread().isInterrupted(), TCPEventsServer.TAG);
                        continue;
                    }
                    responseLine = responseLine.trim();
                    if (responseLine.startsWith("</BbxEventRequest>") && !StringUtils.equals(responseLine, "</BbxEventRequest>")) {
                        String eventType = responseLine.split("<BbxEventRequest><")[1];
                        responseXmlContent = new StringBuilder();
                        responseXmlContent.append("<").append(eventType);

                        String[] eventTypeParam = eventType.split(" ");
                        if (eventTypeParam.length > 1) {
                            eventType = eventTypeParam[0] + ">";
                        }
                        responseEnd = "</" + eventType;
                    } else if (responseXmlContent != null) {
                        if (responseLine.equals(responseEnd)) {
                            responseXmlContent.append(responseLine);
                            parseResponse(responseXmlContent, responseEnd);
                            responseXmlContent = null;
                        } else {
                            responseXmlContent.append(responseLine);
                        }
                    }
                }
                Constants.LOG.debug("{} Old server was closed. Close server socket {}", TCPEventsServer.TAG, Thread.currentThread().getName());
            }
        } catch (Exception e) {
            Constants.LOG.error("{} {}", TCPEventsServer.TAG, ExceptionUtils.getFullStackTrace(e));
        }
    }

    private void parseResponse(StringBuilder responseXmlContent, String responseEnd) {
        FactoryEvents request = FactoryEvents.getType(responseEnd);
        switch (request) {
            case HEARTBEAT:
                lastHeartbeatTime.set(System.currentTimeMillis());
                Constants.LOG.debug("{} Parsing XML HEARTBEAT, thread: {}, {}", TCPEventsServer.TAG, Thread.currentThread().getName());
                break;
            default:
                events.offer(new TCPEventsServer.WorkUnit(request, responseXmlContent));
                break;
        }
    }

    public ServerSocket getServer() {
        return server;
    }

}
