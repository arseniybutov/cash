package ru.crystals.pos.bank.ucs;

import org.apache.commons.lang.time.DateUtils;

public class Timeouts {

    /**
     * Таймаут ожидания получения ACK или NAK
     */
    private long ackTimeout = 3000;
    /**
     * • таймер Т1 (5 секунд) начинает отсчет времени после получения последовательности ENQ/ACK. До истечения времени должна начаться передача
     * данных, т.е. должна быть получена последовательность DLE/STX
     */
    private long dataStartReceiveTimeout = 5000;
    /**
     * • второй таймаут Т2 (200 миллисекунд) - ограничение на задержку во время передачи сообщения. Если получение данных приостанавливается на время
     * большее, чем таймаут Т2, то получатель отправляет символ NAK и ждет повторной отправки ему сообщения STX (см. Рис. 3).
     */
    private long maxTimeBetweenBytes = 200;

    private long whileSleepAckTimeout = 10;

    private long eotTimeout = 100;

    /**
     * Таймаут получения intial response от терминала
     */
    private long initialResponseTimeout = 10 * DateUtils.MILLIS_PER_SECOND;
    /**
     * Таймаут получения printline response от терминала
     */
    private long printLineResponseTimeout = 10 * DateUtils.MILLIS_PER_SECOND;
    /**
     * Таймаут получения остальных сообщений от терминала
     */
    private long commonResponseTimeout = 30 * DateUtils.MILLIS_PER_SECOND;

    public long getAckTimeout() {
        return ackTimeout;
    }

    public Timeouts setAckTimeout(long ackTimeout) {
        this.ackTimeout = ackTimeout;
        return this;
    }

    public long getWhileSleepAckTimeout() {
        return whileSleepAckTimeout;
    }

    public Timeouts setWhileSleepAckTimeout(long whileSleepAckTimeout) {
        this.whileSleepAckTimeout = whileSleepAckTimeout;
        return this;
    }

    public long getDataStartReceiveTimeout() {
        return dataStartReceiveTimeout;
    }

    public Timeouts setDataStartReceiveTimeout(long dataStartReceiveTimeout) {
        this.dataStartReceiveTimeout = dataStartReceiveTimeout;
        return this;
    }

    public long getMaxTimeBetweenBytes() {
        return maxTimeBetweenBytes;
    }

    public Timeouts setMaxTimeBetweenBytes(long maxTimeBetweenBytes) {
        this.maxTimeBetweenBytes = maxTimeBetweenBytes;
        return this;
    }

    public long getEOTTimeout() {
        return eotTimeout;
    }

    public Timeouts setEOTTimeout(long eotTimeout) {
        this.eotTimeout = eotTimeout;
        return this;
    }

    public long getCommonResponseTimeout() {
        return commonResponseTimeout;
    }

    public Timeouts setCommonResponseTimeout(long commonResponseTimeout) {
        this.commonResponseTimeout = commonResponseTimeout;
        return this;
    }

    public long getPrintLineResponseTimeout() {
        return printLineResponseTimeout;
    }

    public Timeouts setPrintLineResponseTimeout(long printLineResponseTimeout) {
        this.printLineResponseTimeout = printLineResponseTimeout;
        return this;
    }

    public long getInitialResponseTimeout() {
        return initialResponseTimeout;
    }

    public Timeouts setInitialResponseTimeout(long initialResponseTimeout) {
        this.initialResponseTimeout = initialResponseTimeout;
        return this;
    }

}
