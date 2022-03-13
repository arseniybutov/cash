package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * настройки соединения/общения с ФР семейства "Штрих".
 *
 * @author aperevozchikov
 */
public class ShtrihConnectorProperties {
    /**
     * Название последовательно порта, через который ведется информационный обмен.
     */
    private String portName = "/dev/ttyS0";

    /**
     * Скорость обмена данными через этот последовательный порт, бод
     */
    private int baudRate = 9600;

    /**
     * Ip адресс для RNDIS соединения
     */
    private String ipAddress;

    /**
     * Порт для RNDIS соединения
     */
    private int tcpPort;

    /**
     * переворачивать байты при печати штрихкода
     */
    private boolean needRevertBytes;

    /**
     * Путь к файлу с инициализирующими настройками. В этом файле настройки, что будут записаны в устройство при старте кассы.
     * <p/>
     * Например, "lib/jpos/shtrih/shtrih-mptk.csv"
     */
    private String parametersFilePath;

    /**
     * Максимальное количество символов в строке при печати нормальным (обычным) шрифтом
     */
    private int maxCharsInRow = 48;

    /**
     * Номер шрифта ККТ Штрих используемый для печати нефискальных строк,
     * допустимые значения от 0 до 255
     */
    private int printStringFont = 1;

    /**
     * Высота одномерных (т.е., не 2D-, не QR-) ШК, в мм
     */
    private int barcodeHeight = 7;

    /**
     * Печатать графическую информацию (QR-коды, например) с высоким качеством (но ооооочень медленно)
     */
    private boolean highQualityGraphics = false;

    /**
     * Время, что требуется ФР для исполнения команды "Печать линии" (0xC5). После исполнения этой команды надо тупо ждать и не пытаться больше ничего
     * печатать - иначе линия будет распечатана не корректно. В милисекундах.
     */
    private long printLineTime = 200;

    /**
     * максимальное увеличение/масштабирование картинки при печати ШК; отрицательное значение распознается как отсутствие предела: ШК будет
     * максимально увеличен (до ширины чековой ленты)
     */
    private int maxScale = 12;

    /**
     * максимальное количество линий графики, что можно грузить в ФР по команде "загрузка графики" (0xC0)
     */
    private int maxLoadGraphicsLines = 200;

    /**
     * Номер строки в пикселях с которой печатать логотип
     */
    private Integer imageFirstLine;

    /**
     * Номер строки в пикселях по которую печатать логотип
     */
    private Integer imageLastLine;

    /**
     * Пароль оператора. Всегда 4 байта.
     */
    private int password = 30;

    /**
     * Доступна ли на модели установка значения межстрочного интервала
     */
    private boolean lineSpacingSupported = true;

    /**
     * Максимальное время ожидания каждого байта от устройства, в мс
     */
    protected long byteWaitTime = 50;

    /**
     * Флаг печати заголовка из юридического лица
     */
    private boolean printLegalEntityHeader = true;

    @Override
    public String toString() {
        return String.format("shtrih-connector-properties [port: %s; rate: %s; ipAddress: %s; tcpPort: %s; password: %s; parameters-file: \"%s\"]", getPortName(),
                getBaudRate(), getIpAddress(), getTcpPort(), getPassword(), getParametersFilePath());
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
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

    public int getMaxCharsInRow() {
        return maxCharsInRow;
    }

    public void setMaxCharsInRow(int maxCharsInRow) {
        this.maxCharsInRow = maxCharsInRow;
    }

    public void setPrintStringFont(int printStringFont) {
        this.printStringFont = printStringFont;
    }

    public int getPrintStringFont() {
        return printStringFont;
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

    public int getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
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

    public boolean isLineSpacingSupported() {
        return lineSpacingSupported;
    }

    public void setLineSpacingSupported(boolean lineSpacingSupported) {
        this.lineSpacingSupported = lineSpacingSupported;
    }

    public long getByteWaitTime() {
        return byteWaitTime;
    }

    public void setByteWaitTime(long byteWaitTime) {
        this.byteWaitTime = byteWaitTime;
    }

    public boolean isPrintLegalEntityHeader() {
        return printLegalEntityHeader;
    }

    public void setPrintLegalEntityHeader(boolean printLegalEntityHeader) {
        this.printLegalEntityHeader = printLegalEntityHeader;
    }
}
