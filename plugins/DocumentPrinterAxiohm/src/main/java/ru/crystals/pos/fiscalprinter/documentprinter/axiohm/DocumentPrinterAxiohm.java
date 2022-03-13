package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.Connector;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.CommandConfiguration;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DrawerStatusCommand;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.EscPosPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.SimpleCommand;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialProxyService;
import ru.crystals.utils.time.DateConverters;
import ru.crystals.utils.time.Timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@PrototypedComponent
public class DocumentPrinterAxiohm implements ReceiptPrinter, Configurable<EscPosPrinterConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentPrinterAxiohm.class);

    private static final String DEFAULT_PAGE_CODE = "cp866";
    private static final int DEFAULT_NORMAL_MAX_CHAR = 44;

    protected static final Path LOGO_BMP_FILE = Paths.get("images/fiscalPrinterLogo.bmp");

    protected static final byte ESC = 0x1B;
    protected static final byte GS = 0x1D;
    protected static final byte US = 0x1F;
    protected static final byte ETX = 0x03;
    protected static final byte SYN = 0x16;

    private static final String DEV_TTY_S_803 = "/dev/ttyS803";
    private static final String DEV_TTY_S_804 = "/dev/ttyS804";
    /**
     * Текст заголовка для предпечати
     */
    private List<String> headerLines;

    protected Charset encoding;

    protected EscPosPrinterConfig config;

    protected final Connector connector = new Connector();

    private CommandConfiguration commandConfig;

    private SerialProxyService serialProxyService;
    protected boolean useSerialProxy;
    /**
     * Флаг состояния печати, по котором возвращаем закешированный статус ДЯ вместо прямого запроса к принтеру
     */
    private final AtomicBoolean printingMode = new AtomicBoolean();
    private final AtomicBoolean lastCashDrawerState = new AtomicBoolean();

    @Override
    public Class<EscPosPrinterConfig> getConfigClass() {
        return EscPosPrinterConfig.class;
    }

    @Override
    public void setConfig(EscPosPrinterConfig config) {
        this.config = config;
        final DefaultPrinterConfig defaultConfig = prepareDefaultConfig();
        customizeDefaultConfig(defaultConfig);
        commandConfig = new CommandConfiguration(config, defaultConfig);
        encoding = commandConfig.getEncoding();
    }

    private DefaultPrinterConfig prepareDefaultConfig() {
        final DefaultPrinterConfig defaultConfig = new DefaultPrinterConfig();
        defaultConfig.setPrinterEncoding(DEFAULT_PAGE_CODE);
        defaultConfig.setFeedLength(22);
        defaultConfig.setMaxCharRowMap(ImmutableMap.of(
                Font.NORMAL, 44,
                Font.SMALL, 56,
                Font.DOUBLEWIDTH, 22
        ));

        defaultConfig.setPrinterStatusMap(ImmutableMap.of(
                ByteSequence.of(0x01), StatusFP.Status.END_PAPER,
                ByteSequence.of(0x04), StatusFP.Status.END_PAPER,
                ByteSequence.of(0x02), StatusFP.Status.OPEN_COVER
        ));

        defaultConfig.setDrawerStatusMap(ImmutableMap.of(
                false, ByteSequence.of(0b0000_00011)
        ));

        // команды и подстановки в мутабельной мапе, потому что для них есть смысл в общих значениях
        // по умолчанию для разных принтеров - могут быть дополнены внутри реализаций customizeDefaultConfig
        // (статусы и шрифты если отличаются, то полностью и должны быть заданы для каждого принтера свои)

        final Map<PrinterCommandType, ByteSequence> commands = new HashMap<>();
        commands.put(PrinterCommandType.FULL_CUT, ByteSequence.of(0x19));
        commands.put(PrinterCommandType.CUT, ByteSequence.of(0x1A));
        commands.put(PrinterCommandType.STATUS, ByteSequence.of(0x1D, 0x72, 0x31));
        commands.put(PrinterCommandType.DRAWER_STATUS, ByteSequence.of(0x1D, 0x72, 0x32));
        commands.put(PrinterCommandType.FEED, ByteSequence.of(0x15));
        commands.put(PrinterCommandType.INIT_CHARSET, ByteSequence.of(0x1B, 0x52, 0x07));
        defaultConfig.setCommands(commands);

        final Map<Character, Character> substitution = new HashMap<>();
        substitution.put('«', '"');
        substitution.put('»', '"');
        defaultConfig.setMissingSymbolsReplacement(substitution);
        return defaultConfig;
    }

    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        // другие реализации принтеров могут поменять значения по умолчанию
    }

    @Override
    public void open() throws FiscalPrinterException {
        if (useSerialProxy) {
            if (serialProxyService == null) {
                serialProxyService = new SerialProxyService(LOG, DEV_TTY_S_803, DEV_TTY_S_804);
            }
            try {
                serialProxyService.start(config.getPort(), String.valueOf(config.getBaudRate()));
            } catch (PortAdapterException e) {
                LOG.error("Error on start serialproxy", e);
            }
            connector.open(DEV_TTY_S_804, config.getBaudRate(), config.getPrinterTimeOut());
        } else {
            connector.open(config.getPort(), config.getBaudRate(), config.getPrinterTimeOut());
        }
        setCharSettings();
    }

    @Override
    public void close() throws FiscalPrinterException {
        connector.close();
        if (useSerialProxy) {
            try {
                serialProxyService.stop();
            } catch (PortAdapterException e) {
                LOG.error("Error on stop serialproxy", e);
            }
        }
    }

    protected void setCharSettings() throws FiscalPrinterException {
        sendCommand(commandConfig.getInitCharsetCommand());
    }

    @Override
    public void printLine(String text) throws FiscalPrinterException {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            buf.write(getTextBytes(commandConfig.replaceMissingSymbols(text)));
            buf.write(0x0A);
            sendDataWaitingDSR(buf.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void openCashDrawer() throws FiscalPrinterException {
        byte[] pulse = new byte[]{0x1B, 0x70, 0x0, 0x14, 0x14};
        sendDataWaitingDSR(pulse);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOG.error("openCashDrawer ", ie);
        }
        setNewCashDrawerState(isCashDrawerOpenInner());
    }

    private void setNewCashDrawerState(boolean status) {
        if (lastCashDrawerState.compareAndSet(!status, status)) {
            LOG.debug("Last cash drawer state set to {}", status);
        }
    }

    @Override
    public boolean isCashDrawerOpen() throws FiscalPrinterException {
        if (printingMode.get()) {
            final boolean result = lastCashDrawerState.get();
            LOG.debug("Using last cash drawer state ({}) while printing", result);
            return result;
        }
        final boolean resultState = isCashDrawerOpenInner();
        setNewCashDrawerState(resultState);
        return resultState;
    }

    private boolean isCashDrawerOpenInner() throws FiscalPrinterException {
        final DrawerStatusCommand drawerStatusCommand = commandConfig.getDrawerStatusCommand();
        sendDataWaitingDSR(drawerStatusCommand.getCommand());
        byte[] result = connector.readData(drawerStatusCommand.getResponseLength());
        return drawerStatusCommand.parseResult(result);
    }

    @Override
    public void openDocument() throws FiscalPrinterException {
        setPrintingMode(true);
    }

    @Override
    public void closeDocument() throws FiscalPrinterException {
        // Получаем статус принтера после печати документа, тем самым дожидаемся окончания печати, не начиная засылать в принтер
        // другие команды (например, запрос статуса ДЯ) - принтер ответит на запрос статуса как только станет доступен после печати.
        // На самом деле, при печати очень длинных документов таймаута получения статуса может не хватить,
        // но если бесконечно увеличивать таймаут, то касса будет надолго зависать при отвалившемся принтере.
        // Нормальным решением в будущем будет использовать значение таймаута в зависимости от длины документа.
        final StatusFP status = getStatus();
        if (status.getStatus() != StatusFP.Status.NORMAL) {
            LOG.debug("Printer state on close document: {}", status);
        }
    }

    @Override
    public void onFinishDocumentPrinting() {
        setPrintingMode(false);
    }

    private void setPrintingMode(boolean active) {
        if (printingMode.compareAndSet(!active, active)) {
            LOG.debug("isPrinting = {}", active);
        } else {
            LOG.trace("isPrinting == {}", active);
        }
    }

    @Override
    public void printBarcode(BarCode barcode) throws FiscalPrinterException {
        int maxBarcodeLength = 21;
        String line = barcode.getValue();

        setTextAlign(1);
        if (barcode.getType() == BarCodeType.QR) {
            printQRByPrinter(barcode);
        } else if (line.length() <= maxBarcodeLength) {


            try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                // Ширина
                buf.write(0x1D);
                buf.write(0x77);
                buf.write((int) barcode.getWidth());
                // Высота
                buf.write(0x1D);
                buf.write(0x68);
                buf.write((int) barcode.getHeight());
                // Положение текста
                buf.write(0x1D);
                buf.write(0x48);
                //0 - Не печатается; 2 - Снизу штрих кода
                buf.write(0);
                // Печать баркода
                buf.write(0x1D);
                buf.write(0x6B);
                buf.write(69);
                final byte[] textBytes = line.getBytes();
                buf.write(textBytes.length);
                buf.write(textBytes);
                // Конец
                buf.write(0x00);
                sendDataWaitingDSR(buf.toByteArray());
                printLine(StringUtils.center(barcode.getBarcodeLabel(), getMaxCharRow(Font.NORMAL)));
            } catch (IOException ie) {
                throw new UncheckedIOException(ie);
            }
        } else {
            printLine(line);
        }
        setTextAlign(0);
    }

    @Override
    public void setFont(Font font) throws FiscalPrinterException {
        byte[] cmd = {0x1B, 0x21, getFontAttribute(font)};
        sendDataWaitingDSR(cmd);
    }

    private byte getFontAttribute(Font font) {
        switch (font) {
            case DOUBLEHEIGHT:
                return 16;
            case DOUBLEWIDTH:
                return 32;
            case SMALL:
                return 1;
            case UNDERLINE:
                return (byte) 128;
            case NORMAL:
            default:
                return 0;
        }
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        feed();
        if (config.isUseLogo()) {
            // печать лого на принтере происходит при вызове команды cut, а заголовок должен быть напечатан после этого
            cut();
            printHeaders();
        } else {
            //Препринт заголовка для экономии чековой ленты
            printHeaders();
            cut();
        }
    }

    /**
     * Промотка чековой ленты
     */
    protected void feed() throws FiscalPrinterException {
        sendCommand(commandConfig.getFeedCommand());
    }

    protected void printHeaders() throws FiscalPrinterException {
        if (headerLines == null) {
            return;
        }
        setFont(Font.NORMAL);
        for (String line : headerLines) {
            printLine(line);
        }
    }

    protected void cut() throws FiscalPrinterException {
        sendCommand(config.isFullCut() ? commandConfig.getFullCutCommand() : commandConfig.getCutCommand());
    }

    public void setHeaderLines(List<String> headerLines) {
        this.headerLines = headerLines;
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_PIRIT");
    }

    /**
     * Печать QR-кода средствами принтера.
     */
    private void printQRByPrinter(BarCode code) {
        try {
            setQRCodeSizeCommand(code);
            loadQRData(code.getValue());
            printQRCodeCommand();
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }

    protected void setQRCodeSizeCommand(BarCode code) {
        setQRCodeSizeCommand(code.getWidth() > 2 ? (int) code.getWidth() : config.getQrSize().orElse(5));
    }

    /**
     * Команда "Установка размера модуля QR-кода". Команда устанавливает размер
     * модуля (минимального элемента штрих-кода) в n точек.
     */
    protected void setQRCodeSizeCommand(int size) {
        // 1 <= size <= 16,  по умолчанию = 3
        if (size == 0) {
            size = 3;
        } else if (size < 1) {
            size = 1;
        } else if (size > 16) {
            size = 16;
        }
        try {
            byte[] cmd = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, (byte) size};
            sendDataWaitingDSR(cmd);
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }

    /**
     * Команда "Загрузка данных QR-кода". Команда загружает k байт QR-кода в
     * принтер.
     */
    protected void loadQRData(String code) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            buf.write(0x1D);
            buf.write(0x28);
            buf.write(0x6B);
            //qL
            buf.write(code.length() + 3);
            //qH
            buf.write(0x00);
            buf.write(0x31);
            buf.write(0x50);
            buf.write(0x30);
            //f1..fk
            buf.write(code.getBytes());

            sendDataWaitingDSR(buf.toByteArray());
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }

    /**
     * Команда "Печать QR-кода". Команда печатает предварительно загруженный
     * QR-код в соответствии с уже установленными размерами модуля и уровнем
     * коррекции ошибок.
     */
    protected void printQRCodeCommand() {
        try {
            byte[] cmd = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30};
            sendDataWaitingDSR(cmd);
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        byte[] result;
        if (config.isUseUsb()) {
            result = getPrinterStateWithWait();
        } else {
            result = getPrinterState();

        }

        StatusFP status = new StatusFP();
        if (result.length == 1) {
            status.setLongStatus(result[0]);
        }
        setPrinterStatus(status, result);
        return status;
    }

    private void setPrinterStatus(StatusFP status, byte[] result) {
        final StatusFP.Status resultStatus = commandConfig.getStatusCommand().parseResult(result);
        status.setStatus(resultStatus);
        if (resultStatus == StatusFP.Status.END_PAPER) {
            status.addDescription(ResBundleDocPrinterAxiohm.getString("PRINTER_END_OF_PAPER"));
        }
        if (resultStatus == StatusFP.Status.OPEN_COVER) {
            status.addDescription(ResBundleDocPrinterAxiohm.getString("PRINTER_COVER_OPENED"));
        }
    }

    /**
     * Возвращает text.getBytes() в указанной в pageCode кодировке,
     * метод создан для дополнительной обработки строк в сабклассах.
     *
     * @param text - текст на печать
     * @return Массив байт текста в установенной кодировке
     */
    protected byte[] getTextBytes(String text) {
        return text.getBytes(encoding);
    }

    protected byte[] getPrinterState() throws FiscalPrinterException {
        sendDataWaitingDSR(commandConfig.getStatusCommand().getCommand());
        return connector.readData(commandConfig.getStatusCommand().getResponseLength());
    }

    protected byte[] getPrinterStateWithWait() throws FiscalPrinterException {
        final Timer printerTimeout = Timer.of(Duration.ofMillis(config.getPrinterTimeOut()));
        final Timer overallTimeOut = Timer.of(Duration.ofMillis(config.getPrinterStatusWaitTime()));
        while (!Thread.currentThread().isInterrupted()) {
            try {
                printerTimeout.restart();
                return getPrinterState();
            } catch (FiscalPrinterException e) {
                if (!printerTimeout.isExpired()) {
                    // Если ошибка возникла до истечения этого таймера, то считаем, что у нас принтер отвалился и нет смысла пытаться ждать дольше
                    LOG.error("getPrinterState", e);
                    throw e;
                }
                if (overallTimeOut.isExpired()) {
                    LOG.error("getPrinterStateWithWait after {} of waiting", overallTimeOut.getElapsedTimeAsString(), e);
                    break;
                }
            }
        }
        throw new FiscalPrinterException(ResBundleFiscalPrinter.getString("ERROR_READ_DATA_TIMEOUT"), CashErrorType.FISCAL_ERROR,
                FiscalPrinterExceptionType.TIMEOUT_EXPIRED);
    }

    /**
     * Выравнивает весь текст, в заданное положение
     *
     * @param align 0 - по левому краю, 1 - по центру, 2 - по правому краю
     */
    protected void setTextAlign(int align) {
        try {
            byte[] cmd = {0x1B, 0x61, (byte) align};
            connector.sendData(cmd);
        } catch (Exception e) {
            LOG.error("Cannot set new text alignment", e);
        }
    }

    @Override
    public int getMaxCharRow(Font font) {
        final Integer size = config.getMaxCharRowMap().get(font);
        if (size != null) {
            return size;
        }
        final Integer normalFont = config.getMaxCharRowMap().getOrDefault(Font.NORMAL, DEFAULT_NORMAL_MAX_CHAR);
        if (font == Font.DOUBLEWIDTH) {
            return normalFont / 2;
        }
        return normalFont;
    }

    protected void replaceCharacters(Set<CharacterReplacement> replacements) throws Exception {
        for (CharacterReplacement characterReplacement : replacements) {
            replaceCharacter(characterReplacement);
        }
    }

    /**
     * Записываем в принтер символ {@link CharacterReplacement#getSymbol()} вместо имеющегося {@link CharacterReplacement#getPlace()}
     * Заменяемый символ передаем на принтер с учетом выбранной кодировки.
     */
    private void replaceCharacter(CharacterReplacement replacement) throws Exception {
        byte characterAsByte = replacement.getPlace().getBytes(encoding)[0];
        // 3 - количество байт в столбце (24 точки / 8 = 3 байта)
        // заменяем одну букву, поэтому передаем ее в качестве начала и конца диапазона
        connector.sendData(ESC, '&', 3, characterAsByte, characterAsByte, replacement.getColNum());
        connector.sendData(readAndTransform(replacement.getFileName(), replacement.getColNum()));
    }

    /**
     * Подменяем символы в переданной строке на те, которые были предварительно заменены в принтере.
     *
     * @param text         строка, в которой нужно заменить символы
     * @param replacements набор замен
     */
    protected String replaceSymbols(String text, Set<CharacterReplacement> replacements) {
        for (CharacterReplacement replacement : replacements) {
            text = text.replace(replacement.getSymbol(), replacement.getPlace());
        }
        return text;
    }

    /**
     * Данный метод преобразует "изображение" из файла в байты, подходящие для передачи на принтер.
     * <p>
     * Высота символа для wincor 24 точки. Соответственно каждая колонка кодируется 3 байтами.
     * Байты изображения должны передаваться на принтер по колонкам: 3 байта из первой колонки сверху вниз и т.д.
     * Итоговое количество байт будет (3 * colNum)
     *
     * @param resourceFileName имя файла из resources
     * @param colNum           количество точек "изображения" по горизонтали
     * @return последовательность байт символа
     */
    byte[] readAndTransform(String resourceFileName, int colNum) throws Exception {
        byte[][] bytes = readFromFile(resourceFileName, colNum);
        byte[] result = new byte[3 * colNum];
        for (int i = 0; i < colNum; i++) {
            for (int j = 0; j < 3; j++) {
                byte byteResult = 0;
                for (int k = 0; k < 8; k++) {
                    byteResult <<= 1;
                    byteResult += bytes[k + 8 * j][i];
                }
                result[3 * i + j] = byteResult;
            }
        }
        return result;
    }

    /**
     * Вспомогательный метод для считывания файла в двумерный массив из 0 и 1
     * В качестве 1 в файле используется *
     */
    private byte[][] readFromFile(String resourceFileName, int colNum) throws Exception {
        List<String> lines = Files.readAllLines(getResourcePath(resourceFileName));
        byte[][] bytes = new byte[24][colNum];
        for (int i = 0; i < 24 && i < lines.size(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < line.length(); j++) {
                bytes[i][j] = line.charAt(j) == '*' ? (byte) 1 : (byte) 0;
            }
        }
        return bytes;
    }

    /**
     * Загрузка логотипа в принтер.
     *
     * @param num номер логотипа в flash памяти принтера
     */
    protected void downloadLogoBmp(int num) throws FiscalPrinterException {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(LOGO_BMP_FILE);
        } catch (IOException e) {
            throw new FiscalPrinterException("Error while downloading logo to printer from file " + LOGO_BMP_FILE);
        }

        connector.sendData(GS, '#', num);
        connector.sendData(ESC);
        connector.sendData(bytes);
    }

    protected boolean needToReloadLogo(PrinterProperties properties) {
        LocalDateTime currentModifiedTime = getLogoFileLastModified();
        return currentModifiedTime.isAfter(properties.getLogoFileLastModified());
    }

    protected LocalDateTime getLogoFileLastModified() {
        return DateConverters.toLocalDateTime(LOGO_BMP_FILE.toFile().lastModified());
    }

    private Path getResourcePath(String path) {
        try {
            URL resource = getClass().getClassLoader().getResource(path);
            URI uri = resource.toURI();

            if (resource.getProtocol().startsWith("file")) {
                return Paths.get(uri);
            }
            try {
                return FileSystems.getFileSystem(uri).getPath(path);
            } catch (FileSystemNotFoundException fsnfe) {
                return FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(path);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void sendDataWaitingDSR(byte[] data) throws FiscalPrinterException {
        if (config.isUseUsb()) {
            connector.sendData(data);
        } else {
            connector.sendDataWaitingDSR(data);
        }
    }

    private void sendCommand(SimpleCommand command) throws FiscalPrinterException {
        sendDataWaitingDSR(command.getCommand());
    }

}
