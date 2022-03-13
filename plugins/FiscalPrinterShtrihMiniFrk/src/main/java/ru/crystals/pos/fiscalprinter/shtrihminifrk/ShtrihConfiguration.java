package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.TimeUnit;

public class ShtrihConfiguration extends BaseShtrihConfig {

    /**
     * Нулевая ширина линии штрихкода в точках
     */
    protected static final int NONE_WIDTH_BARCODE_LINE = 0;

    /**
     * Единичная ширина линии штрихкода в точках
     */
    protected static final int ONE_WIDTH_BARCODE_LINE = 1;

    /**
     * Частичная отрезка чека
     */
    @JsonProperty("partialCutReceipt")
    private boolean partialCutReceipt = true;

    /**
     * Установка заголовка чека в таблицу настроек из кассы
     */
    @JsonProperty("headerFromCash")
    private boolean headerFromCash = true;

    /**
     * Печать штрих-кода как QR - не все модели могут печатать штрих код линиями.
     **/
    @JsonProperty("printQrBarCode")
    private boolean printQrBarCode;

    /**
     * СOM-порт соединения (без префикса /dev/)
     */
    @JsonProperty("serialPort")
    private String serialPort = "ttyS1";

    /**
     * Скорость подключения по COM
     */
    @JsonProperty("serialPortBaudRate")
    private int serialPortBaudRate = 115200;

    /**
     * Тип соединения с ККТ (COM - true, TCP - false)
     */
    @JsonProperty("comConnection")
    private boolean comConnection;

    /**
     * Основной таймаут для установки в драйвере (работает как таймаут подключения и ответа на команды) (мс)
     */
    @JsonProperty("timeout")
    private long timeout = TimeUnit.SECONDS.toMillis(10);

    /**
     * Максимальное время, которое пытаемся подключиться к ККТ на старте кассы (фактически мы делаем несколько попыток подключения, равных {@link #timeout} и считаем,
     * пока это время не истечет) (мс)
     */
    @JsonProperty("maxConnectionTimeout")
    private long maxConnectionTimeout = timeout * 6;

    /**
     * Максимальное время переподключения к ККТ при возникновении ошибки ввода вывода в процессе работы (мс)
     */
    @JsonProperty("maxReconnectionTimeout")
    private long maxReconnectionTimeout = timeout * 2;

    /**
     * Использовать костыль для оживления драйвера с некоторыми Штрихами и платами расширения (без этого на старте системы не удается подключитсья к ККТ)
     */
    @JsonProperty("testSerialPortUsingDirectCommand")
    private boolean testSerialPortUsingDirectCommand = true;

    /**
     * Режим переворачивания байта (влияет на методы драйвера PrintBarcodeLine, PrintLine)
     * 0: Переворачивать
     * 1: Не переворачивать
     * 2: Использовать свойство драйвера LineSwapBytes
     * 3: Использовать настройки модели
     */
    @JsonProperty("swapBytesMode")
    private int swapBytesMode = 3;

    /**
     * Ширина линий штрихкода в точках
     */
    @JsonProperty("barcodeLineWidth")
    private int barcodeLineWidth = NONE_WIDTH_BARCODE_LINE;

    /**
     * Флаг печати заголовка из юридического лица
     */
    @JsonProperty("printLegalEntityHeader")
    private boolean printLegalEntityHeader = true;

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        if (serialPort != null) {
            if (serialPort.contains("/")) {
                this.serialPort = serialPort.substring(serialPort.lastIndexOf('/') + 1);
            } else {
                this.serialPort = serialPort;
            }
        }
    }

    public int getSerialPortBaudRate() {
        return serialPortBaudRate;
    }

    public void setSerialPortBaudRate(int serialPortBaudRate) {
        this.serialPortBaudRate = serialPortBaudRate;
    }

    public boolean isComConnection() {
        return comConnection;
    }

    public void setComConnection(boolean comConnection) {
        this.comConnection = comConnection;
    }

    public long getMaxConnectionTimeout() {
        return maxConnectionTimeout;
    }

    public void setMaxConnectionTimeout(long maxConnectionTimeout) {
        this.maxConnectionTimeout = maxConnectionTimeout;
    }

    public long getMaxReconnectionTimeout() {
        return maxReconnectionTimeout;
    }

    public void setMaxReconnectionTimeout(long maxReconnectionTimeout) {
        this.maxReconnectionTimeout = maxReconnectionTimeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isTestSerialPortUsingDirectCommand() {
        return testSerialPortUsingDirectCommand;
    }

    public void setTestSerialPortUsingDirectCommand(boolean testSerialPortUsingDirectCommand) {
        this.testSerialPortUsingDirectCommand = testSerialPortUsingDirectCommand;
    }

    public boolean isPartialCutReceipt() {
        return partialCutReceipt;
    }

    public void setPartialCutReceipt(boolean partialCutReceipt) {
        this.partialCutReceipt = partialCutReceipt;
    }

    public boolean isHeaderFromCash() {
        return headerFromCash;
    }

    public void setHeaderFromCash(boolean headerFromCash) {
        this.headerFromCash = headerFromCash;
    }

    public boolean isPrintQrBarCode() {
        return printQrBarCode;
    }

    public void setPrintQrBarCode(boolean printQrBarCode) {
        this.printQrBarCode = printQrBarCode;
    }

    public int getSwapBytesMode() {
        return swapBytesMode;
    }

    public void setSwapBytesMode(int swapBytesMode) {
        this.swapBytesMode = swapBytesMode;
    }

    public int getBarcodeLineWidth() {
        return barcodeLineWidth;
    }

    public void setBarcodeLineWidth(int barcodeLineWidth) {
        this.barcodeLineWidth = barcodeLineWidth;
    }

    public boolean isPrintLegalEntityHeader() {
        return printLegalEntityHeader;
    }

    public void setPrintLegalEntityHeader(boolean printLegalEntityHeader) {
        this.printLegalEntityHeader = printLegalEntityHeader;
    }

}
