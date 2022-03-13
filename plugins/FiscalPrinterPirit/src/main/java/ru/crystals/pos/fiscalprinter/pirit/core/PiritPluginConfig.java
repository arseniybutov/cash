package ru.crystals.pos.fiscalprinter.pirit.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.Map;

public class PiritPluginConfig {

    /**
     * Клиентский плагин
     */
    @JsonProperty("pluginProvider")
    private String pluginProvider;

    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private String baudRate = "57600";

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("tcpPort")
    private int tcpPort;

    /**
     * Путь к конфигу компрокси. Используется не напрямую плагином, а при конфигурации плагина через UI (вытаскиваем оттуда физический порт)
     */
    @JsonProperty("serviceIni")
    private String serviceIni = "/home/tc/storage/comproxy/ComProxy.ini";
    @JsonProperty("runServiceTimeout")
    private Long runServiceTimeout;
    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("servicePath")
    private String servicePath;

    @JsonProperty("printPosNum")
    private boolean printPosNum = true;
    @JsonProperty("printGoodsName")
    private boolean printGoodsName = true;
    @JsonProperty("printItem")
    private boolean printItem;
    @JsonProperty("useBeep")
    private boolean useBeep;
    @JsonProperty("goodNameMaxLength")
    private int goodNameMaxLength = AbstractPirit.GOOD_NAME_MAX_LENGTH;
    @JsonProperty("barcodeHeight")
    private long barcodeHeight = 40;
    @JsonProperty("printStoredImage")
    private boolean printStoredImage;
    @JsonProperty("printQRAsImage")
    private boolean printQRAsImage;
    /**
     * Настройка печатать информацию о ккт(РН, ЗН, ФН, ИНН)
     */
    @JsonProperty("needPrintKKTInfo")
    private boolean needPrintKKTInfo;

    /**
     * Переопределение Read timeout команды
     * Ключ - номер команды, значение - read timeout в миллисекундах
     */
    @JsonDeserialize(keyUsing = HexIntegerDeserializer.class)
    @JsonProperty("commandsReadTimeout")
    private Map<Integer, Long> commandsReadTimeout = Collections.emptyMap();

    /**
     * Количество отправленных асинхронных команд, после которого нужно дождаться завершения обработки
     * По умолчанию для Пирит 2Ф используется {@link AbstractPirit#MAX_ASYNC_COMMAND_BUFFER_SIZE_PIRIT_II}, а для остальных (1Ф и Википринты) 20 команд.
     */
    @JsonProperty("maxAsyncCommandBuffer")
    private Long maxAsyncCommandBuffer;

    @JsonProperty("maxCharRowMap")
    private Map<Integer, Integer> maxCharRowMap = Collections.emptyMap();

    @JsonProperty("oismTimeout")
    private Integer oismTimeout;

    public String getPluginProvider() {
        return pluginProvider;
    }

    public void setPluginProvider(String pluginProvider) {
        this.pluginProvider = pluginProvider;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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

    public String getServiceIni() {
        return serviceIni;
    }

    public void setServiceIni(String serviceIni) {
        this.serviceIni = serviceIni;
    }

    public Long getRunServiceTimeout() {
        return runServiceTimeout;
    }

    public void setRunServiceTimeout(Long runServiceTimeout) {
        this.runServiceTimeout = runServiceTimeout;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(String baudRate) {
        this.baudRate = baudRate;
    }

    public boolean isPrintPosNum() {
        return printPosNum;
    }

    public void setPrintPosNum(boolean printPosNum) {
        this.printPosNum = printPosNum;
    }

    public boolean isPrintGoodsName() {
        return printGoodsName;
    }

    public void setPrintGoodsName(boolean printGoodsName) {
        this.printGoodsName = printGoodsName;
    }

    public boolean isPrintItem() {
        return printItem;
    }

    public void setPrintItem(boolean printItem) {
        this.printItem = printItem;
    }

    public boolean isUseBeep() {
        return useBeep;
    }

    public void setUseBeep(boolean useBeep) {
        this.useBeep = useBeep;
    }

    public int getGoodNameMaxLength() {
        return goodNameMaxLength;
    }

    public void setGoodNameMaxLength(int goodNameMaxLength) {
        this.goodNameMaxLength = goodNameMaxLength;
    }

    public long getBarcodeHeight() {
        return barcodeHeight;
    }

    public void setBarcodeHeight(long barcodeHeight) {
        this.barcodeHeight = barcodeHeight;
    }

    public boolean isPrintStoredImage() {
        return printStoredImage;
    }

    public void setPrintStoredImage(boolean printStoredImage) {
        this.printStoredImage = printStoredImage;
    }

    public boolean isPrintQRAsImage() {
        return printQRAsImage;
    }

    public void setPrintQRAsImage(boolean printQRAsImage) {
        this.printQRAsImage = printQRAsImage;
    }

    public Map<Integer, Long> getCommandsReadTimeout() {
        return commandsReadTimeout;
    }

    public void setCommandsReadTimeout(Map<Integer, Long> commandsReadTimeout) {
        this.commandsReadTimeout = commandsReadTimeout;
    }

    public Long getMaxAsyncCommandBuffer() {
        return maxAsyncCommandBuffer;
    }

    public void setMaxAsyncCommandBuffer(Long maxAsyncCommandBuffer) {
        this.maxAsyncCommandBuffer = maxAsyncCommandBuffer;
    }

    public boolean isNeedPrintKKTInfo() {
        return needPrintKKTInfo;
    }

    public void setNeedPrintKKTInfo(boolean needPrintKKTInfo) {
        this.needPrintKKTInfo = needPrintKKTInfo;
    }

    public Map<Integer, Integer> getMaxCharRowMap() {
        return maxCharRowMap;
    }

    public void setMaxCharRowMap(Map<Integer, Integer> maxCharRowMap) {
        this.maxCharRowMap = maxCharRowMap;
    }

    public Integer getOismTimeout() {
        return oismTimeout;
    }

    public void setOismTimeout(Integer oismTimeout) {
        this.oismTimeout = oismTimeout;
    }
}
