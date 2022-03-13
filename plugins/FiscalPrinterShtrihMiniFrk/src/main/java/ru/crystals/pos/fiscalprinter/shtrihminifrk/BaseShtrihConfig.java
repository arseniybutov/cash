package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class BaseShtrihConfig {

    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private int baudRate = 9600;

    /**
     * Ip адресс для RNDIS соединения
     */
    @JsonProperty("ipAddress")
    private String ipAddress;

    /**
     * Порт для RNDIS соединения
     */
    @JsonProperty("tcpPort")
    private int tcpPort;

    /**
     * переворачивать байты при печати штрихкода
     */
    @JsonProperty("needRevertBytes")
    private boolean needRevertBytes;

    /**
     * Номер шрифта ККТ Штрих используемый для печати нефискальных строк,
     * допустимые значения от 0 до 255
     */
    @JsonProperty("printStringFont")
    private int printStringFont = 1;

    /**
     * Время, что требуется ФР для исполнения команды "Печать линии" (0xC5). После исполнения этой команды надо тупо ждать и не пытаться больше ничего
     * печатать - иначе линия будет распечатана не корректно. В милисекундах.
     */
    @JsonProperty("printLineTime")
    private long printLineTime = 200;

    /**
     * Номер строки в пикселях с которой печатать логотип
     */
    @JsonProperty("imageFirstLine")
    private Integer imageFirstLine;

    /**
     * Номер строки в пикселях по которую печатать логотип
     */
    @JsonProperty("imageLastLine")
    private Integer imageLastLine;

    /**
     * Высота одномерных (т.е., не 2D-, не QR-) ШК, в мм
     */
    @JsonProperty("barcodeHeight")
    private int barcodeHeight = 7;

    /**
     * Печатать графическую информацию (QR-коды, например) с высоким качеством (но ооооочень медленно)
     */
    @JsonProperty("highQualityGraphics")
    private boolean highQualityGraphics;

    /**
     * максимальное увеличение/масштабирование картинки при печати ШК; отрицательное значение распознается как отсутствие предела: ШК будет
     * максимально увеличен (до ширины чековой ленты)
     */
    @JsonProperty("maxBarcodeScaleFactor")
    private int maxBarcodeScaleFactor = 12;

    /**
     * Путь к файлу с инициализирующими настройками. В этом файле настройки, что будут записаны в устройство при старте кассы.
     * <p/>
     * Например, "lib/jpos/shtrih/shtrih-mptk.csv"
     */
    @JsonProperty("parametersFilePath")
    private String parametersFilePath;

    /**
     * максимальное количество линий графики, что можно грузить в ФР по команде "загрузка графики" (0xC0)
     */
    @JsonProperty("maxLoadGraphicsLines")
    private int maxLoadGraphicsLines = 200;

    @JsonProperty("ofdDevice")
    private boolean ofdDevice;

    @JsonProperty("jposName")
    private String jposName;

    @JsonProperty("jposNameAdditionally")
    private List<String> jposNameAdditionally = Collections.emptyList();

    @JsonProperty("maxCharRow")
    private int maxCharRow = 36;

    public int getMaxCharRow() {
        return maxCharRow;
    }

    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }


    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
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

    public boolean isNeedRevertBytes() {
        return needRevertBytes;
    }

    public void setNeedRevertBytes(boolean needRevertBytes) {
        this.needRevertBytes = needRevertBytes;
    }

    public Integer getImageFirstLine() {
        return imageFirstLine;
    }

    public void setImageFirstLine(Integer imageFirstLine) {
        this.imageFirstLine = imageFirstLine;
    }

    public Integer getImageLastLine() {
        return imageLastLine;
    }

    public void setImageLastLine(Integer imageLastLine) {
        this.imageLastLine = imageLastLine;
    }

    public int getBarcodeHeight() {
        return barcodeHeight;
    }

    public void setBarcodeHeight(int barcodeHeight) {
        this.barcodeHeight = barcodeHeight;
    }

    public boolean isHighQualityGraphics() {
        return highQualityGraphics;
    }

    public void setHighQualityGraphics(boolean highQualityGraphics) {
        this.highQualityGraphics = highQualityGraphics;
    }

    public long getPrintLineTime() {
        return printLineTime;
    }

    public void setPrintLineTime(long printLineTime) {
        this.printLineTime = printLineTime;
    }

    public int getMaxBarcodeScaleFactor() {
        return maxBarcodeScaleFactor;
    }

    public void setMaxBarcodeScaleFactor(int maxBarcodeScaleFactor) {
        this.maxBarcodeScaleFactor = maxBarcodeScaleFactor;
    }

    public String getParametersFilePath() {
        return parametersFilePath;
    }

    public void setParametersFilePath(String parametersFilePath) {
        this.parametersFilePath = parametersFilePath;
    }

    public int getMaxLoadGraphicsLines() {
        return maxLoadGraphicsLines;
    }

    public void setMaxLoadGraphicsLines(int maxLoadGraphicsLines) {
        this.maxLoadGraphicsLines = maxLoadGraphicsLines;
    }

    public boolean isOfdDevice() {
        return ofdDevice;
    }

    public void setOfdDevice(boolean ofdDevice) {
        this.ofdDevice = ofdDevice;
    }

    public String getJposName() {
        return jposName;
    }

    public void setJposName(String jposName) {
        this.jposName = jposName;
    }

    public List<String> getJposNameAdditionally() {
        return jposNameAdditionally;
    }

    public void setJposNameAdditionally(List<String> jposNameAdditionally) {
        this.jposNameAdditionally = jposNameAdditionally;
    }

    public int getPrintStringFont() {
        return printStringFont;
    }

    public void setPrintStringFont(int printStringFont) {
        this.printStringFont = printStringFont;
    }
}
