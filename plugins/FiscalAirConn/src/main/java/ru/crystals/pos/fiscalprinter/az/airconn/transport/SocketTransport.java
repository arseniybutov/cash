package ru.crystals.pos.fiscalprinter.az.airconn.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.util.concurrent.locks.ReentrantLock;

public class SocketTransport implements AirConnTransport {
    private static final Logger LOG = LoggerFactory.getLogger(SocketTransport.class);

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * IP и порт для подключения к службе AirConn
     */
    private static final String TOKEN_ADDRESS_PREFIX = "tcp://";
    private String tokenAddress;
    private ZContext zContext;
    private ZMQ.Socket reqSocket;

    @Override
    public void init(String tokenAddress) {
        this.tokenAddress = TOKEN_ADDRESS_PREFIX + tokenAddress;
        zContext = new ZContext();
        LOG.info("Socket context management created");
    }

    /**
     * Освобождает ресурсы ZMQ, обязательно вызывать на остановке плагина
     */
    @Override
    public void close() {
        // When you exit your application you must destroy the context using ZMQ.Context.close()
        zContext.close();
        LOG.info("Socket context management closed");
    }

    @Override
    public void connect() throws FiscalPrinterCommunicationException {
        if (zContext == null) {
            throw new FiscalPrinterCommunicationException("Missing socket context management class");
        }
        LOG.info("Connecting socket to: {}", tokenAddress);
        reqSocket = zContext.createSocket(SocketType.REQ);
        int socketTimeOut = 5000;
        reqSocket.setReceiveTimeOut(socketTimeOut);
        if (!reqSocket.connect(tokenAddress)) {
            throw new FiscalPrinterCommunicationException("Socket was not connected");
        }
    }

    @Override
    public void disconnect() {
        if (reqSocket != null) {
            reqSocket.disconnect(tokenAddress);
            reqSocket.close();
            LOG.info("Socket disconnected from: {}", tokenAddress);
        }
    }

    @Override
    public <C extends BaseCommand> C executeCommand(C command) throws FiscalPrinterException {
        LOG.debug("executing command({})", command.getOperationId());
        String response = send(command.serializeRequest());
        command.deserializeResponse(response);
        command.checkForApiError();
        LOG.debug("execute end");
        return command;
    }

    /**
     * Отправка на AirConn сериализованной команды через сокет ZMQ
     *
     * @param jsonCommand сереализованная в json команда
     * @return ответ от AirConn
     * @throws FiscalPrinterCommunicationException при ошибках в работе сокета, отсутствии связи с AirConn
     */
    private String send(String jsonCommand) throws FiscalPrinterCommunicationException {
        if (reqSocket == null) {
            throw new FiscalPrinterCommunicationException("Command not queued on the ZMQ.Socket (null)");
        }
        lock.lock();
        LOG.debug("send command : {}", jsonCommand);
        try {
            if (reqSocket.send(jsonCommand)) {
                //Блокирующий метод ожидания данных
                String jsonReply = reqSocket.recvStr();
                if (jsonReply == null) {
                    throw new FiscalPrinterCommunicationException("Command timeout Error");
                }
                LOG.debug("command result: {}", jsonReply);
                return jsonReply;
            }
        } finally {
            lock.unlock();
        }
        throw new FiscalPrinterCommunicationException("Command not queued on the ZMQ.Socket");
    }
}

