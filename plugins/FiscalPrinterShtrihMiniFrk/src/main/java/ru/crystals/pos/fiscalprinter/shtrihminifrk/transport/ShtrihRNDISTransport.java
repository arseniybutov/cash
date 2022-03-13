package ru.crystals.pos.fiscalprinter.shtrihminifrk.transport;

import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;

/**
 * реализация {@link Transport} для фискальных регистраторов семейства "Штрих".
 * <p>
 * NOTE: данная реализация подразумевает, что используется способ сваязи типа "полу-дуплекс" (запрос-ответ) - т.е., мы не ожидаем ответа от внешнего
 * устройства, если мы его не спрашивали: если нам пихают какие-то данные, перед тем. как мы собираемся что-то спросить - ответим ACK'ом - чтоб это
 * <em>подчиненное</em> устройство просто заткнулось и проигнорируем ответ.
 */
public class ShtrihRNDISTransport extends AbstractTransport {

    /**
     * Ip адресс для RNDIS соединения
     */
    private String ipAddress;

    /**
     * Порт для RNDIS соединения
     */
    private int tcpPort;

    @Override
    public void open() throws IOException, PortAdapterException {
        log.trace("entering open()");

        adapter = new TCPPortAdapter();
        getAdapter().setTcpAddress(getIpAddress());
        getAdapter().setTcpPort(getTcpPort());

        log.trace("about to open adapter: {}", adapter);

        adapter.openPort();

        log.trace("leaving open()");
    }

    @Override
    protected void waitEndMessage() throws IOException {
        // Реализация необходима только для RNDIS транспорта, попытка побороть SRTB-2857
        if (adapter.waitForAnyData(byteWaitTime)) {
            adapter.readBytes();
        }
    }
    
    @Override
    public String toString() {
        return String.format("shtrih-transport[ipAddress: %s, tcpPort: %s]", ipAddress, tcpPort);
    }

    public TCPPortAdapter getAdapter() {
        return (TCPPortAdapter) adapter;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }
}