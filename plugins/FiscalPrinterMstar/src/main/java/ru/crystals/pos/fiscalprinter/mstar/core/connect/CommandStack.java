package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.MstarRequestPacket;
import ru.crystals.comportemulator.mstar.MstarResponsePacket;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandStack {
    private static final Logger log = LoggerFactory.getLogger(MstarConnector.class);

    private static final int QUEUE_SIZE = 208;

    private int stackSize;
    private BlockingQueue<MstarRequestPacket> stack = new LinkedBlockingQueue<>();
    private Map<Integer, RequestResponse> responsesOtherThreadId = new ConcurrentHashMap<>();

    public CommandStack() {
        this(QUEUE_SIZE);
    }

    public CommandStack(int queueSize) {
        stackSize = queueSize;
    }

    public void markRequestAsLost(int packetId) {
        RequestResponse rr = responsesOtherThreadId.get(packetId);
        if (rr != null) {
            rr.getRequestPacket().setLost();
        }
    }

    public int getStackSize() {
        return stack.size();
    }

    public List<MstarRequestPacket> getStackBody() {
        return Arrays.asList(stack.toArray(new MstarRequestPacket[0]));
    }

    protected long getCurrentThreadID() {
        return Thread.currentThread().getId();
    }

    public void addRequestPacket(MstarRequestPacket packet) throws FiscalPrinterException {
        try {
            packet.setThreadID(getCurrentThreadID());
            stack.put(packet);
            RequestResponse requestResponse = new RequestResponse(packet);
            responsesOtherThreadId.put((int) packet.getPacketId(), requestResponse);
            if (stack.size() > stackSize) {
                stack.take();
            }
        } catch (InterruptedException e) {
            throw new FiscalPrinterException("", e);
        }
    }

    public MstarResponsePacket getResponsePacketBeforeRead(int packetId) throws FiscalPrinterCommunicationException {
        RequestResponse requestResponsePair = responsesOtherThreadId.get(packetId);
        if (requestResponsePair != null) {
            if (requestResponsePair.getRequestPacket().isLost()) {
                log.error("Skip lost response: " + requestResponsePair.getResponsePacket());
            } else {
                MstarResponsePacket response = requestResponsePair.getResponsePacket();
                if (response != null) {
                    responsesOtherThreadId.remove((int) response.getPacketId());
                    validateAnswer(requestResponsePair.getRequestPacket(), response);
                    return response;
                }
            }
        }
        return null;
    }

    public MstarResponsePacket getResponsePacket(MstarResponsePacket responsePacket, int requestPacketId) throws FiscalPrinterException {
        RequestResponse requestResponsePair = responsesOtherThreadId.get((int) responsePacket.getPacketId());
        if (requestResponsePair != null && requestResponsePair.getRequestPacket() != null && requestResponsePair.getRequestPacket().isLost()) {// ответ для этой команды уже никто не ждет - timeout
            requestResponsePair.setResponsePacket(responsePacket);
            log.error("Skip lost response: " + responsePacket);
            return null;
        }

        requestResponsePair = responsesOtherThreadId.get(requestPacketId);
        if (requestResponsePair != null) {// положительный сценарий
            responsesOtherThreadId.remove(requestPacketId);
        } else {// возможно ответ другого потока
            RequestResponse oldErrorRequestPacket = responsesOtherThreadId.get((int) responsePacket.getPacketId());
            if (oldErrorRequestPacket != null) {
                if (oldErrorRequestPacket.getRequestPacket().getThreadID() != getCurrentThreadID()) {// да - другой поток
                    oldErrorRequestPacket.setResponsePacket(responsePacket);// отдадим его
                    responsePacket = null;
                }//else  отдадим его в ответе
            } else {// если прилетел пакет который никто не спрашивал - выкенем его, пойдем опрашивать дальше
                responsePacket = null;
            }
        }

        // и наконец если есть нужный пакет - проверим его
        if (responsePacket != null && requestResponsePair != null) {
            validateAnswer(requestResponsePair.getRequestPacket(), responsePacket);
        }
        return responsePacket;
    }

    private boolean validateAnswer(MstarRequestPacket requestPacket, MstarResponsePacket responsePacket) throws FiscalPrinterCommunicationException {
        int errorCode = responsePacket.getErrorCode();
        int expectedCMD = requestPacket.getCommandID().getCode();
        int receivedCMD = responsePacket.getCommandID().getCode();
        int expectedPacketId = requestPacket.getPacketId();
        int receivedId = responsePacket.getPacketId();

        if (responsePacket.getErrorCode() != 0) {
            logCommunicationExceptionAndThrow(
                    new FiscalPrinterCommunicationException(MstarErrorMsg.getErrorMessage(errorCode), MstarErrorMsg.getErrorType(errorCode), (long) errorCode),
                    "Error received!", errorCode, expectedCMD, receivedCMD, expectedPacketId, receivedId);
        }

        return requestPacket.getCommandID().equals(responsePacket.getCommandID()) && requestPacket.getPacketId() == responsePacket.getPacketId();
    }

    private void log(String logExceptionMessage, int errorCode, int expectedCMD, int receivedCMD, int expectedPacketId, int receivedId) {
        log.error("=============================================================");
        log.error(logExceptionMessage);
        log.error("Error code={}, error message={}", String.format("%02X", errorCode), MstarErrorMsg.getErrorMessage(errorCode));
        log.error("Code Cmd: expectedCMD={} receivedCMD={}", String.format("%02X", expectedCMD), String.format("%02X", receivedCMD));
        log.error("Packet Id: sendId={} receivedId={}", String.format("%02X", expectedPacketId), String.format("%02X", receivedId));
        log.error("=============================================================");
    }

    private void logCommunicationExceptionAndThrow(FiscalPrinterCommunicationException exception, String logExceptionMessage,
                                                   int errorCode, int expectedCMD, int receivedCMD, int expectedPacketId, int receivedId)
            throws FiscalPrinterCommunicationException {
        log(logExceptionMessage, errorCode, expectedCMD, receivedCMD, expectedPacketId, receivedId);
        throw exception;
    }

    class RequestResponse {

        private MstarRequestPacket requestPacket;
        private MstarResponsePacket responsePacket;

        public RequestResponse(MstarRequestPacket requestPacket) {
            this.requestPacket = requestPacket;
        }

        public MstarRequestPacket getRequestPacket() {
            return requestPacket;
        }

        public MstarResponsePacket getResponsePacket() {
            return responsePacket;
        }

        public void setResponsePacket(MstarResponsePacket responsePacket) {
            this.responsePacket = responsePacket;
        }
    }

}
