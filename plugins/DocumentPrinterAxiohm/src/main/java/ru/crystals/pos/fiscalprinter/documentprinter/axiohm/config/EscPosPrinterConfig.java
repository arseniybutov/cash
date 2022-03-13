package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class EscPosPrinterConfig {

    @JsonProperty("printerEncoding")
    private String printerEncoding;

    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private int baudRate = 9600;

    @JsonProperty("useUSB")
    private boolean useUsb = true;

    @JsonProperty("qrSize")
    private Integer qrSize;

    /**
     * Длинна промотки перед отрезкой в точках
     */
    @JsonProperty("feedLength")
    private Integer feedLength;

    /**
     * Использовать ли полную отрезку (по умолчанию частичная)
     */
    @JsonProperty("isFullCut")
    private boolean isFullCut;

    /**
     * Печатать ли лого в начале чека
     */
    @JsonProperty("useLogo")
    private boolean useLogo;

    @JsonProperty("printerTimeOut")
    private long printerTimeOut = 2500;

    @JsonProperty("printerStatusWaitTime")
    private long printerStatusWaitTime = TimeUnit.SECONDS.toMillis(12);

    @JsonProperty("clearOutPutTimeOut")
    private long clearOutPutTimeOut = TimeUnit.SECONDS.toMillis(30);

    /**
     * Максимальное число символов в строке для заданного шрифта
     */
    @JsonProperty("maxCharRowMap")
    private Map<Font, Integer> maxCharRowMap = Collections.emptyMap();

    /**
     * Набор команд, отличающихся для этого принтера от базовой реализации, с их представлением в виде набора байт
     */
    @JsonDeserialize(contentUsing = ByteSequenceDeserializer.ByteSequenceValueDeserializer.class)
    @JsonProperty("commands")
    private Map<PrinterCommandType, ByteSequence> commands = Collections.emptyMap();

    /**
     * Соответствие результатов выполнения команды {@link PrinterCommandType#STATUS} состояниям принтера
     * <p>
     * Результат команды сравнивает со значениями в мапе через побитовое И.
     * <p>
     * Таким образом в ключах мапы могут быть указаны как полные ответы, так и только значащие биты для определения статус, однако количество байт в ключах мапы
     * должно совпадать с количеством байт в ответе на команду {@link PrinterCommandType#STATUS} (количество байт в маппинге используется
     * как количество байт, которое нужно вычитать из порта после отправки запроса).
     */
    @JsonDeserialize(keyUsing = ByteSequenceDeserializer.ByteSequenceKeyDeserializer.class)
    @JsonProperty("printerStatusMap")
    private Map<ByteSequence, StatusFP.Status> printerStatusMap = Collections.emptyMap();

    /**
     * Соответствие состояния денежного ящика результату выполнения команды {@link PrinterCommandType#DRAWER_STATUS}.
     * <p>
     * Фактически мапится только одно состояние - закрыт (false) или открыт (true), а второе выводится из него через отрицание.
     * <p>
     * В значении мапы может быть указан как полный ответ, так и только значащие биты для определения статуса.
     */
    @JsonDeserialize(contentUsing = ByteSequenceDeserializer.ByteSequenceValueDeserializer.class)
    @JsonProperty("drawerStatusMap")
    private Map<Boolean, ByteSequence> drawerStatusMap = Collections.emptyMap();

    /**
     * Список замен одних символов на другие, например, если нужного символа нет в кодировке/шрифте принтера
     */
    @JsonProperty("missingSymbolsReplacement")
    private Map<Character, Character> missingSymbolsReplacement = Collections.emptyMap();

    public boolean isUseUsb() {
        return useUsb;
    }

    public void setUseUsb(boolean useUsb) {
        this.useUsb = useUsb;
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

    public Optional<Integer> getQrSize() {
        return Optional.ofNullable(qrSize);
    }

    public void setQrSize(Integer qrSize) {
        this.qrSize = qrSize;
    }

    public Integer getFeedLength() {
        return feedLength;
    }

    public void setFeedLength(Integer feedLength) {
        this.feedLength = feedLength;
    }

    public boolean isFullCut() {
        return isFullCut;
    }

    public void setFullCut(boolean fullCut) {
        isFullCut = fullCut;
    }

    public boolean isUseLogo() {
        return useLogo;
    }

    public void setUseLogo(boolean useLogo) {
        this.useLogo = useLogo;
    }

    public long getPrinterTimeOut() {
        return printerTimeOut;
    }

    public void setPrinterTimeOut(long printerTimeOut) {
        this.printerTimeOut = printerTimeOut;
    }

    public long getPrinterStatusWaitTime() {
        return printerStatusWaitTime;
    }

    public void setPrinterStatusWaitTime(long printerStatusWaitTime) {
        this.printerStatusWaitTime = printerStatusWaitTime;
    }

    public String getPrinterEncoding() {
        return printerEncoding;
    }

    public void setPrinterEncoding(String printerEncoding) {
        this.printerEncoding = printerEncoding;
    }

    public long getClearOutPutTimeOut() {
        return clearOutPutTimeOut;
    }

    public void setClearOutPutTimeOut(long clearOutPutTimeOut) {
        this.clearOutPutTimeOut = clearOutPutTimeOut;
    }

    public Map<Font, Integer> getMaxCharRowMap() {
        return maxCharRowMap;
    }

    public void setMaxCharRowMap(Map<Font, Integer> maxCharRowMap) {
        this.maxCharRowMap = maxCharRowMap;
    }

    public Map<PrinterCommandType, ByteSequence> getCommands() {
        return commands;
    }

    public void setCommands(Map<PrinterCommandType, ByteSequence> commands) {
        this.commands = commands;
    }

    public Map<ByteSequence, StatusFP.Status> getPrinterStatusMap() {
        return printerStatusMap;
    }

    public void setPrinterStatusMap(Map<ByteSequence, StatusFP.Status> printerStatusMap) {
        this.printerStatusMap = printerStatusMap;
    }

    public Map<Boolean, ByteSequence> getDrawerStatusMap() {
        return drawerStatusMap;
    }

    public void setDrawerStatusMap(Map<Boolean, ByteSequence> drawerStatusMap) {
        this.drawerStatusMap = drawerStatusMap;
    }

    public Map<Character, Character> getMissingSymbolsReplacement() {
        return missingSymbolsReplacement;
    }

    public void setMissingSymbolsReplacement(Map<Character, Character> missingSymbolsReplacement) {
        this.missingSymbolsReplacement = new LinkedHashMap<>(missingSymbolsReplacement);
    }

}
