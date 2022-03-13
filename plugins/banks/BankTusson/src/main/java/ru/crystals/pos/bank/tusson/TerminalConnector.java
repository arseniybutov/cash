package ru.crystals.pos.bank.tusson;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.tusson.protocol.Request;
import ru.crystals.pos.bank.tusson.protocol.Response;
import ru.crystals.pos.bank.tusson.protocol.ResponseStatus;
import ru.crystals.pos.utils.PortAdapterUtils;
import ru.crystals.pos.utils.Timer;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Отвечает за коммуникацию с терминалом и разбор ответов
 */
public class TerminalConnector {
    private static final Logger LOG = LoggerFactory.getLogger(TerminalConnector.class);
    private static final String LOCAL_ADDRESS = "0.0.0.0";
    private static final long DEFAULT_OPERATION_TIMEOUT_SECONDS = 90;
    private static final long TRY_TO_GET_ANSWER_COUNT = 6;
    private static final long TRY_TO_GET_ANSWER_TIME_OUT_SECONDS = 1;
    private static final long DATA_READING_TIMEOUT_MILLIS = 50;

    private static AtomicInteger packetNumber = new AtomicInteger(1);

    private int port = 5757;
    private String address;
    private long operationTimeout = DEFAULT_OPERATION_TIMEOUT_SECONDS;


    public TerminalConnector(String address, int port, long operationTimeout) {
        this.port = port;
        this.address = address;
        this.operationTimeout = Math.max(operationTimeout, DEFAULT_OPERATION_TIMEOUT_SECONDS);
    }

    public TerminalConnector(String address) {
        this.address = address;
    }

    public TerminalConnector() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Вызывает операции на PinPad. Нужно быть уверенным что до вызова данного метода запущен поток слушателя слипов
     *
     * @param parentRequest Запрос к терминалу
     * @return ответ от терминала
     * @throws BankCommunicationException Ошибка связи, либо однозначный ответ не получен в установленное время
     */
    public synchronized Response processOperation(Request parentRequest) throws BankCommunicationException {
        Response response = new Response();
        parentRequest.setPacketNumber(packetNumber.incrementAndGet());
        DatagramChannel channel = null;
        try {
            channel = openChanel();
            response = sendRequest(parentRequest, channel);
            Timer operationTimer = createTimer(operationTimeout * DateUtils.MILLIS_PER_SECOND);
            while (operationTimer.isNotExpired() && (response.getStatus() == ResponseStatus.IN_PROGRESS || response.getStatus() == ResponseStatus.BUSY)) {
                try {
                    response = sendRequest(parentRequest.getSimpleStatusPingRequest(packetNumber.incrementAndGet()), channel);
                } catch (BankCommunicationException ex) {
                    LOG.warn("No response from terminal", ex);
                }
            }
        } catch (BankCommunicationException e) {
            LOG.error("No response from terminal!", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Exception while trying to communicate with terminal", e);
            throw new BankCommunicationException(ResBundleBankTusson.getString("COMMUNICATION_ERROR"), e);
        } finally {
            close(channel);
        }
        return response;
    }

    DatagramChannel openChanel() throws Exception {
        DatagramChannel chanel = DatagramChannel.open();
        chanel.configureBlocking(false);
        chanel.socket().bind(new InetSocketAddress(LOCAL_ADDRESS, port));
        chanel.connect(new InetSocketAddress(address, port));
        return chanel;
    }

    Response sendRequest(Request request, DatagramChannel channel) throws Exception {
        int countOfOperation = 0;
        while (countOfOperation < TRY_TO_GET_ANSWER_COUNT) {
            ByteBuffer send = request.toByteBuffer();
            channel.send(send, new InetSocketAddress(address, port));
            Timer receiveReplyTimer = createTimer(TRY_TO_GET_ANSWER_TIME_OUT_SECONDS * DateUtils.MILLIS_PER_SECOND);
            while (receiveReplyTimer.isNotExpired()) {
                ByteBuffer buffer = ByteBuffer.allocate(Response.MAX_RESPONSE_LENGTH);
                int bytesRead = channel.read(buffer);
                if (bytesRead > 0) {
                    byte[] bytesFromBuffer = getBytesFromBuffer(buffer, bytesRead);
                    LOG.debug("---> " + PortAdapterUtils.arrayToString(bytesFromBuffer));
                    Response response = new Response(bytesFromBuffer);
                    LOG.debug("Response status is " + response.getStatus().name());
                    if (response.getPacketNumber() == packetNumber.get()) {
                        return response;
                    } else {
                        LOG.warn("Wrong Packet received! Expected " + packetNumber.get() + " but received " + response.getPacketNumber());
                        //Пришел ответ на предыдущую команду? Его можно игнорить. Читаем до победного
                    }
                } else {
                    Thread.sleep(DATA_READING_TIMEOUT_MILLIS);
                }
            }
            countOfOperation++;
            request = request.getSimpleStatusPingRequest(packetNumber.incrementAndGet());
        }
        throw new BankCommunicationException(ResBundleBankTusson.getString("ERROR_NO_ANSWER_FROM_TERMINAL"));
    }

    void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                LOG.warn("Failed to close resource!");
            }
        }
    }

    byte[] getBytesFromBuffer(ByteBuffer source, int realBufferSize) {
        byte[] result = new byte[realBufferSize];
        source.flip();
        source.get(result, 0, realBufferSize);
        return result;
    }

    Timer createTimer(long timeoutMillis) {
        return new Timer(timeoutMillis);
    }


}
