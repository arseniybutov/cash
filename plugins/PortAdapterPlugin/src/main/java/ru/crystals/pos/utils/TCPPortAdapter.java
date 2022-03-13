package ru.crystals.pos.utils;

import org.apache.commons.lang.time.DateUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

public class TCPPortAdapter extends AbstractPortAdapter<TCPPortAdapter> {
    private String tcpAddress;
    private Integer tcpPort;
    private int socketTimeOut = 10 * (int) DateUtils.MILLIS_PER_MINUTE;
    private int connectTimeOut = 3 * (int) DateUtils.MILLIS_PER_SECOND;
    private Socket socket;
    DataInputStream in;
    OutputStream out;
    private boolean isOpen = false;

    /**
     * Периодичность проверки появления данных в последовательном порту (при чтении "пакета"), в мс
     */
    private int sleepTime = 5;

    @Override
    public void openPort() throws IOException {
        socket = new Socket();
        socket.setSoTimeout(socketTimeOut);
        socket.connect(new InetSocketAddress(tcpAddress, tcpPort), connectTimeOut);
        in = new DataInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        isOpen = true;
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        out.flush();
    }

    @Override
    public void write(int enq) throws IOException {
        out.write(enq);
        out.flush();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public void close() {
        try {
            if (isOpen) {
                isOpen = false;
                out.close();
                in.close();
            }
        } catch (Exception e) {
            LOG.error("Failed to close streams", e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                LOG.error("Failed to close socket", e);
            }
        }
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public byte[] readBytes() throws IOException {
        byte[] result = new byte[in.available()];
        in.read(result, 0, result.length);
        return result;
    }

    @Override
    public String readAll(String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (getInputStreamBufferSize() > 0) {
            baos.write(read());
        }
        if (baos.size() == 0) {
            return null;
        }
        baos.close();
        return baos.toString(charset);
    }

    @Override
    public int getInputStreamBufferSize() throws IOException {
        return in.available();
    }

    public String getTcpAddress() {
        return tcpAddress;
    }

    public TCPPortAdapter setTcpAddress(String tcpAddress) {
        this.tcpAddress = tcpAddress;
        return this;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public TCPPortAdapter setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
        return this;
    }

    public int getSocketTimeOut() {
        return socketTimeOut;
    }

    public TCPPortAdapter setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
        return this;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public TCPPortAdapter setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    protected Socket getSocket() {
        return socket;
    }

    protected TCPPortAdapter setSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    /**
     * Ожидает появления данных в этом соединении и считывает их (данные).
     *
     * @param waitTimeout  максимальное время ожидания появления данных, в мс
     * @param byteWaitTime допустимое время ожидания получением очередного байта (уже после того, как примем начался), в мс;
     *                     <p>
     *                     <code>null</code> распознается как время передачи одного байта;
     *                     <p>
     *                     невалидное значение(не положительное) означает, что ожидания "появления" очередного байта не будет.
     * @return считанные данные; не <code>null</code>: если данных нет, то будет {@link PortAdapterNoConnectionException}
     * @throws IOException                      если возникли проблемы ввода/вывода
     * @throws PortAdapterNoConnectionException если не дождались отклика от внешнего устройства за <code>waitTimeout</code> мс (т.е.,
     *                                          на основании этого решили. что связи с внешним устройством [больше] нет)
     */
    public byte[] readBytes(long waitTimeout, Long byteWaitTime) throws IOException, PortAdapterNoConnectionException {
        byte[] result;

        // 1. ожидаем появления данных
        if (!waitForAnyData(waitTimeout)) {
            // так и не дождались отклика от устройства
            throw new PortAdapterNoConnectionException("no connection");
        }

        // 2. считываем
        result = readBytes();
        if (result == null) {
            // неожиданно! похоже, кто-то (другой поток?) еще читает из "нашего" порта
            LOG.error("seems, some other thread \"stole\" my data!");
            throw new PortAdapterNoConnectionException("no connection");
        }

        // 3. возможно, в порту еще что-то появится в этом же "кадре"?
        long byteWaitTimeout = Optional.ofNullable(byteWaitTime).orElse(0L);
        while (waitForAnyData(byteWaitTimeout)) {
            int lengthBefore = result.length;
            byte[] newChunk = readBytes();
            LOG.trace("{} \"extra\" bytes were read in addition to existing {} ones", newChunk.length, lengthBefore);

            result = Arrays.copyOf(result, lengthBefore + newChunk.length);
            System.arraycopy(newChunk, 0, result, lengthBefore, newChunk.length);
        }

        return result;
    }

    /**
     * Ожидает появления данных в порту.
     * <p>
     * Implementation Note: наличие данных в порту проверяется с периодичностью {@link #sleepTime} мс.
     *
     * @param waitTimeout время ожидания появления данных, в мс.
     * @return <code>false</code>, если данные в порту так и не появились за <code>waitTimeout</code> мс
     * @throws IOException
     */
    public boolean waitForAnyData(long waitTimeout) throws IOException {
        long timeLimit = System.currentTimeMillis() + waitTimeout;

        if (in.available() > 0) {
            // данные в порту уже есть - не надо ничего ждать
            return true;
        }
        if (waitTimeout <= 0) {
            return false;
        }
        do {
            long remaining = timeLimit - System.currentTimeMillis();
            if (remaining <= 0) {
                break;
            }
            try {
                Thread.sleep(Math.min(remaining, sleepTime));
            } catch (InterruptedException ie) {
                LOG.error("thread interrupted!", ie);
            }
            if (in.available() > 0) {
                return true;
            }
        } while (System.currentTimeMillis() < timeLimit);

        return false;
    }

    public byte[] readFully(int length) throws IOException {
        byte[] result = new byte[length];
        in.readFully(result, 0, length);
        return result;
    }
}
