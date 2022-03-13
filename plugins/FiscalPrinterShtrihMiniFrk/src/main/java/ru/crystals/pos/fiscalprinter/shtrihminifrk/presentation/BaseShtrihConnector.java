package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ShiftNonNullableCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.CsvBasedParametersReader;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ParametersReader;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihFieldType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihParameter;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.AnnulReceiptCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BeepCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BeginCorrectionReceiptCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CancelDocument;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CashInCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CashOutCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CloseNonFiscalDocumentCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CloseReceiptCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CloseReceiptCommandEx;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CloseReceiptV2CommandEx;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.ConfirmSetDateCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.ContinuePrintingCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CorrectionReceiptV2Command;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.CutCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.FNSendItemCodeCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetCashRegistryCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetDeviceParamsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetDeviceTypeCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetEklzDataCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetEklzStateOneCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFieldStructureCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFiscalMemorySums;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetFontPropertiesCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetLastDocInfoCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetOperationRegistryCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetRegNoCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetShiftTotalByNumberCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetShortStatusCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetStatusCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.GetTableStructureCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.LoadDataCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.LoadGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.OpenCashDrawerCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.OpenNonFiscalDocumentCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.OpenShiftCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.Print2DBarcodeCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintExtGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintLineCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintLineUsingFontCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintScaledGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintXReportCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintZReportCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.ReadTableCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.RegDiscountCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.RegMarginCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.RegOperationCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.RegReturnCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.RegSaleCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.SetDateCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.SetTimeCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.Shtrih2DBarcodeAlignment;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.Shtrih2DBarcodeType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.StopEklzSessionCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.TLVDataCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.WriteTableCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihInternalProcessingException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihResponseException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihResponseParseException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihTransport;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.Transport;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.fiscalprinter.utils.Alignment;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * "Базовая" реализация протокола обмена с ФР v. 1.12.
 * <p/>
 * Список сокращений, принятых в документе: <og>
 * <li>ФР - Фискальный Регистратор;
 * <li>ФП - Фискальная Плата;
 * <li>ВУ - Внешнее Устройство;
 * <li>МДЕ - Минимальная Денежная Единица, "копейка";
 * <li>LRC - Longitude Redundancy Check - контрольная сумма </og>
 *
 * @author aperevozchikov
 */
public class BaseShtrihConnector implements ShtrihConnector {
    protected static final Logger log = LoggerFactory.getLogger(BaseShtrihConnector.class);

    /**
     * Пароль оператора по умолчанию (4 байта)
     */
    private static final int DEFAULT_OPERATOR_PASSWORD = 30;

    /**
     * Разрешение графики всех ФП семейства "Штрих", точек на дюйм
     */
    private static final int DPI = 203;

    /**
     * Кодировка, в которой ВСЕ строки отправляются в ФР семейства "Штрих"
     */
    private static final Charset ENCODING = Charset.forName("windows-1251");

    /**
     * Пробел в кодировке {@link #ENCODING} - для заполнения "пустот"
     */
    private static final byte SPACE = 0x20;

    /**
     * Нулевой символ в кодировке {@link #ENCODING} - для заполнения "пустот" пустотой
     */
    private static final byte NULL_CHAR = 0x00;

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
     * Переворачивать байты при печати штрихкода
     */
    private boolean needRevertBytes = false;

    /**
     * Путь к файлу с инициализирующими настройками. В этом файле настройки, что будут записаны в устройство при старте кассы.
     * <p/>
     * Например, "lib/jpos/shtrih/shtrih-mptk.csv"
     */
    private String parametersFilePath;

    /**
     * Периодичность проверки статуса ФР. Используется при ожидании наступления желаемого состояния ФР. Например, состояния возможности продолжения
     * печати.
     */
    private long checkStatusInterval = 100;

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
     *
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
     * Пароль оператора. Всегда 4 байта.
     */
    private int password = DEFAULT_OPERATOR_PASSWORD;

    /**
     * Реализация "транспортного" уровня общения с ФР.
     */
    protected Transport transport;

    /**
     * "шапка", что должна быть распечатана в заголовке следующего документа по завершению печати текущего
     */
    private List<FontLine> header;

    /**
     * Характеристики шрифтов, что поддерживаются данным ФР
     */
    private Map<Byte, ShtrihFontProperties> fonts;

    private Integer imageFirstLine;
    private Integer imageLastLine;

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

    /**
     * Вернет свойства шрифта с указанным номером.
     *
     * @param fontNo
     *            номер шрифта, настройки/сворйства которого надо вернуть
     * @return <code>null</code>, если шрифта с таким номером вообще нет
     */
    private ShtrihFontProperties getFontProperties(byte fontNo) throws IOException, PortAdapterException, ShtrihException {
        ShtrihFontProperties result = null;

        log.trace("entering getFontProperties(byte). The argument is: fontNo [{}]", fontNo);

        if (fonts == null) {
            fonts = new HashMap<>();
        }
        if (fonts.get(fontNo) != null) {
            // уже вытягивали настройки этого шрифта - нового запроса не требуется
            result = fonts.get(fontNo);
            log.trace("leaving getFontProperties(byte). The result (IN-MEMORY) is: {}", result);
            return result;
        }

        // придется делать запрос к ФР
        GetFontPropertiesCommand cmd = new GetFontPropertiesCommand(fontNo, getPassword());
        byte[] response = execute(cmd);
        result = cmd.decodeResponse(response);
        if (result != null) {
            fonts.put(fontNo, result);
        }

        log.trace("leaving getFontProperties(byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество строк текста (нормальным шрифтом) между термоголовкой принтера и ножом. Т.е., это количество уже распечатанных строк, что
     * попадут в следующий чек, если отрезать чековую ленту прямо сейчас.
     * <p/>
     * NOTE: значения у каждой модели принтера свои - потому и protected.
     * <p/>
     * NOTE2: вообще, наверно, можно определить это расстояние считав данные из служебной таблицы (Таблица 10, ряд 1, поле 1 -
     * "Расстояние от головки до ножа").
     *
     * @return неотрицательное число
     */
    protected int getLinesBetweenThermoHeadAndKnife() {
        return 4;
    }

    /**
     * Вернет тип отрезки чека: <code>true</code> – полная, <code>false</code> – неполная.
     * <p/>
     * Implementation Note: вот эту настройку можно и из таблицы считать: например, для Штрих-М ПТК: таблица 1, строка 1, поле 7 - настройка отрезка
     * чека: 0 - нет отрезки, 1 - полная отрезка, 2 - не полная отрезка. Но пока (2016-01-19) просто всегда будем возвращать <code>false</code> -
     * частичную отрезку.
     *
     * @return <code>true</code>, если надо полностью отрезать чек.
     */
    protected boolean isCutOff() {
        return false;
    }

    /**
     * Отрезает чековую ленту.
     *
     * @param cutOff флаг признак: надо ли ПОЛНОСТЬЮ отрезать ленту
     */
    private void cut(boolean cutOff) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering cut(boolean). The argument is: cutOff [{}]", cutOff);
        executeAndThrowExceptionIfError(new CutCommand(cutOff, getPassword()));
        log.debug("leaving cut(boolean)");
    }

    /**
     * Флаг-признак: текущая модель ФР поддерживает команду "Расширенный запрос" (для запроса параметров модели)
     */
    private boolean supportsGetModelParamsCmd = true;

    /**
     * Параметры текущей модели ФР
     */
    private ShtrihModelParams modelParams = null;

    /**
     * Вернет параметры текущей модели ФР - если есть возможность получить/извлечь эту информацию
     *
     * @return {@code null}, если информацию о параметрах модели получить невозможно
     */
    private ShtrihModelParams getModelParams() {
        log.trace("entering getModelParams()");

        if (modelParams != null) {
            log.trace("leaving getModelParams(). The result is (IN-MEMORY): {}", modelParams);
            return modelParams;
        }
        if (!supportsGetModelParamsCmd) {
            log.trace("leaving getModelParams(). The command is NOT supported");
            return null;
        }

        // попробуем считать
        GetDeviceParamsCommand cmd = new GetDeviceParamsCommand(getPassword());
        try {
            byte[] response = executeAndThrowExceptionIfError(cmd);
            modelParams = cmd.decodeResponse(response);
        } catch (Throwable t) {
            log.error("it seems that the \"ext. request\" cmd (0xF7) is NOT supported", t);
            supportsGetModelParamsCmd = false;
        }

        log.trace("leaving getModelParams(). The result is: {}", modelParams);

        return modelParams;
    }

    /**
     * Допечатывает документ: печатает картинки и рекламный тескт в подвале текущего документа, отрезает чековую ленту, печатает текст и картинки в
     * "шапке" следующего документа.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    protected void printDocEnd() throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering printDocEnd()");

        // 0. допечатать те строки, что не попали на чековую ленту из-за того, что документ был открыт
        // 1. Распечатать 1ю картинку для "подвала"
        // 2. Распечатать основной текст подвала
        // 3. Распечатать 2ю картинку для "подвала"
        // 4. Распечатать дополнительный текст подвала
        // 5. Распечатать 3ю картинку для "подвала"

        // 6. Распечатать 2 (жестко) пустые строки нормальным шрифтом
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        printLine(emptyLine);
        printLine(emptyLine);

        // 7. отрезать чековую ленту & распечатать заголовок следующего документа
        printHeaderAndCut();

        // 8. Распечатать картинку следующего документа

        log.debug("leaving printDocEnd()");
    }

    /**
     * Допечатывает ОТЧЕТ: печатает картинки и рекламный тескт в подвале текущего ОТЧЕТА, отрезает чековую ленту, печатает текст и картинки в
     * "шапке" следующего ДОКУМЕНТА.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    protected void printReportEnd() throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering printReportEnd()");

        // 0. допечатать те строки, что не попали на чековую ленту из-за того, что документ был открыт
        // 1. Распечатать 1ю картинку для "подвала"
        // 2. Распечатать основной текст подвала
        // 3. Распечатать 2ю картинку для "подвала"
        // 4. Распечатать дополнительный текст подвала
        // 5. Распечатать 3ю картинку для "подвала"

        // 6. Распечатать 2 (жестко) пустые строки нормальным шрифтом
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        printLine(emptyLine);
        printLine(emptyLine);

        // 7. отрезать чековую ленту & распечатать заголовок следующего документа
        printHeaderAndCut();

        // 8. Распечатать картинку следующего документа

        log.debug("leaving printReportEnd()");
    }

    /**
     * Печатает "шапку" <b>следующего</b> документа и отрезает чековую ленту.
     */
    private void printHeaderAndCut() throws IOException, PortAdapterException, ShtrihException {
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        int linesBetweenThermoHeadAndKnife = getLinesBetweenThermoHeadAndKnife();
        List<FontLine> header = getHeader();

        // 1. распечатаем часть заголовка - так чтобы весь ТЕКУЩИЙ документ оказался ВЫШЕ ножа
        for (int i = 0; i < linesBetweenThermoHeadAndKnife; i++) {
            FontLine line;
            if (i < header.size()) {
                line = header.get(i);
            } else {
                line = emptyLine;
            }

            printLine(line);
        } // for i

        // 2. отрежем чековую ленту
        cut(isCutOff());

        // 3. допечатаем заголовок - если еще что осталось
        for (int i = linesBetweenThermoHeadAndKnife; i < header.size(); i++) {
            FontLine line = header.get(i);
            printLine(line);
        } // for i
    }

    /**
     * Вернет содержимое <b>ДЕНЕЖНОГО</b> регистра с указанным номером.
     *
     * @param registerNo
     *            номер денежного регистра (0..255), содержимое которого надо вернуть
     * @return значение регистра
     */
    protected long getCashRegister(byte registerNo) throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getCashRegister(byte)");

        GetCashRegistryCommand cmd = new GetCashRegistryCommand(registerNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        Long value = cmd.decodeResponse(response);
        if (value == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }
        result = value;

        log.trace("leaving getCashRegister(byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет номер регистра в списке <i>ДЕНЕЖНЫХ</i> регистров, что хранит накопление наличности в кассе [за смену], в "копейках" (в МДЕ -
     * Минимальных Денежных Единицах)
     *
     * @return номер регистра
     */
    protected byte getCashAccumulationRegistry() {
        // 241й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return (byte) 0xF1;
    }

    @Override
    public long getCashAccumulation() throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getCashAccumulation()");
        result = getCashRegister(getCashAccumulationRegistry());
        log.trace("leaving getCashAccumulation(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму продаж за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: продажа
     */
    protected long getSalesSum() throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getSalesSum()");

        // суммы продаж находятся в ДЕНЕЖНЫХ регистрах: 121 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы продаж по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            byte regNo = (byte) (121 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }

        log.trace("leaving getSalesSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму ВОЗВРАТОВ за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: "возврат продажи"
     */
    protected long getReturnsSum() throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getReturnsSum()");

        // суммы возвратов находятся в ДЕНЕЖНЫХ регистрах: 123 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы возвратов по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            byte regNo = (byte) (123 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }

        log.trace("leaving getReturnsSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков продаж в смене.
     *
     * @return количество торговых операций "Продажа" за смену
     */
    protected long getSalesCount() throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getSalesCount()");

        // в 144м операционном регистре
        result = getOperationRegistry((byte) 144);

        log.trace("leaving getSalesCount(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков возврата в смене.
     *
     * @return количество торговых операций "Возврат продажи" за смену
     */
    protected long getReturnsCount() throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getReturnsCount()");

        // в 146м операционном регистре
        result = getOperationRegistry((byte) 146);

        log.trace("leaving getReturnsCount(). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihShiftCounters getShiftCounters() throws IOException, PortAdapterException, ShtrihException {
        ShtrihShiftCounters result = new ShtrihShiftCounters();

        log.debug("entering getShiftCounters()");

        // 1. накопление наличности в кассе
        result.setCashSum(getCashAccumulation());

        // 2. сумма продаж
        result.setSumSale(getSalesSum());

        // 3. сумма возвратов
        result.setSumReturn(getReturnsSum());

        // 4. количество продаж
        result.setCountSale(getSalesCount());

        // 5. количество возвратов
        result.setCountReturn(getReturnsCount());

        log.debug("leaving getShiftCounters(). The result is: {}", result);

        return result;
    }


    // @formatter:off
    /**
     * вернет суммы из указанных данных отчета ЭКЛЗ, в МДЕ.
     * <p/>
     * NOTE: вернет жестко <code>4</code> элемента (именно в такой последовательности):
     * <ol>
     * <li>сумма продаж;
     * <li>сумма покупок;
     * <li>сумма возвратов продаж;
     * <li>сумма возвратов покупок.
     * </ol>
     * <p/>
     * Implementation Note:
     * <pre>
     *  Данные по смене примерно в таком виде:
     *      "ККМ 000000010420 ИНН 750901438305       "
     *      "ЭКЛЗ 1445973046                         "
     *      "ЗАКР.СМ. 0100 29/12/15 15:29 ОПЕРАТОР30 "
     *      "ПРОДАЖА                            *0.00"
     *      "ПОКУПКА                            *0.00"
     *      "ВОЗВР. ПРОДАЖИ                     *0.00"
     *      "ВОЗВР. ПОКУПКИ                     *0.00"
     *      "00001311 #016163                        "
     *- отсюда надо вытащить суммы продаж и возвратов - ищем звездочки ('*' == 0x2A) и парсим строки
      <pre>
     *
     * @param data
     *            данные отчета ЭКЛЗ в "сыром" виде - в виде массива байт
     * @return <code>null</code>, если не удастся распарсить суммы, ИЛИ если этих сумм будет не <code>4</code>.
     * @throws NullPointerException if the argument is <code>null</code>
     */
    // @formatter:off
    private List<Long> getSums(List<byte[]> data) {
        byte asterixMarker = 0x2A;
        List<Long> result = new ArrayList<>(4);
        for (byte[] line : data) {
            int asterixIndex;
            if ((asterixIndex = getFirstIndexOf(line, asterixMarker)) < 0) {
                continue;
            }
            byte[] sumData = Arrays.copyOfRange(line, asterixIndex + 1, line.length);
            String sumAsString = BaseCommand.getString(sumData);
            try {
                BigDecimal bd = new BigDecimal(sumAsString);
                bd = bd.multiply(new BigDecimal(100.0));
                long sum = bd.longValue();
                result.add(sum);
            } catch (Throwable t) {
                log.error("getSums(List): failed to parse \"{}\"", sumAsString);
                result = null;
                break;
            }
        } // for line
        if (result != null && result.size() != 4) {
            result = null;
        }

        return result;
    }

    @Override
    public ShtrihShiftCounters getShiftCounters(int shiftNo) throws IOException, PortAdapterException, ShtrihException {
        ShtrihShiftCounters result = null;

        log.debug("entering getShiftCounters(int). The argument is: shiftNo [{}]", shiftNo);

        List<byte[]> data = getShiftDataByNumber(shiftNo);
        if (data == null) {
            // данные по этой смене не найдены
            log.warn("leaving getShiftCounters(int): the shift #{} data was not found!", shiftNo);
            return null;
        }
        // Данные по смене получены. вытащим суммы:
        List<Long> sums = getSums(data);
        if (sums != null) {
            // есть валидные данные:
            result = new ShtrihShiftCounters();
            result.setSumSale(sums.get(0));
            result.setSumReturn(sums.get(2));
        }

        log.debug("leaving getShiftCounters(int). The result is: {}", result);

        return result;
    }

    /**
     * Вернет значение <i>ОПЕРАЦИОННОГО</i> регистра с указанным номером.
     *
     * @param registryNo
     *            номер регистра. значение которого надо вернуть
     * @return 2 байта: 00..65535
     */
    protected long getOperationRegistry(byte registryNo) throws IOException, PortAdapterException, ShtrihException {
        long result = 0;

        log.trace("entering getOperationRegistry()");

        GetOperationRegistryCommand cmd = new GetOperationRegistryCommand(registryNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        Integer value = cmd.decodeResponse(response);
        if (value == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }
        result = value;

        log.trace("leaving getOperationRegistry(). The result is: {}", result);

        return result;
    }


    /**
     * Вернет информацию о последнем документе из ФН по номеру ФД
     * @param docNumber номер документа
     * @return данные последнего закрытого документа в ФР
     * @throws IOException
     * @throws PortAdapterException
     * @throws ShtrihException
     */
    public FiscalDocumentData getLastDocInfo(long docNumber) throws IOException, PortAdapterException, ShtrihException {
        FiscalDocumentData result;

        log.trace("entering getOperationRegistry()");

        GetLastDocInfoCommand cmd = new GetLastDocInfoCommand(docNumber, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        FiscalDocumentData value = cmd.decodeResponse(response);
        result = value;

        log.trace("leaving getOperationRegistry(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество внесений за смену.
     *
     * @return номер регистра
     */
    protected byte getCashInCountRegistry() {
        // 153й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return (byte) 0x99;
    }

    @Override
    public long getCashInCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(getCashInCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество изъятий за смену (количество выплат денежных сумм за
     * смену).
     *
     * @return номер регистра
     */
    protected byte getCashOutCountRegistry() {
        // 154й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return (byte) 0x9A;
    }

    @Override
    public long getCashOutCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(getCashOutCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество отмененных документов.
     *
     * @return номер регистра
     */
    protected byte getAnnulCountRegistry() {
        // 157й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return (byte) 0x9D;
    }

    @Override
    public long getAnnulCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(getAnnulCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит СПНД (сквозной порядковый номер документа).
     *
     * @return номер регистра
     */
    protected byte getSpndRegistry() {
        // 152й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return (byte) 0x98;
    }

    @Override
    public long getSpnd() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(getSpndRegistry());
    }

    /**
     * Номер <i>ОПЕРАЦИОННОГО</i> регистра, хранящего номер последнего Z отчета.
     *
     * @return номер регистра
     */
    private byte getZReportCountRegistry() {
        // 159й регистр
        return (byte) 0x9F;
    }

    @Override
    public long getZReportCount() throws IOException, PortAdapterException, ShtrihException {
        return getOperationRegistry(getZReportCountRegistry());
    }

    @Override
    public void beep() throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering beep()");
        executeAndThrowExceptionIfError(new BeepCommand(getPassword()));
        log.trace("leaving beep()");
    }

    @Override
    public void annul() throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering annul()");

        // 1. сначала убедимся, что не идет печать какого-либо документа и что есть _открытый_ документ, что надо аннулировать
        ShtrihStateDescription state = waitForPrinting();
        if (ShtrihModeEnum.DOCUMENT_IS_OPEN.equals(state.getMode().getStateNumber())) {
            // 2. аннулируем открытый документ
            AnnulReceiptCommand cmd = new AnnulReceiptCommand(getPassword());
            executeAndThrowExceptionIfError(cmd);

            // 3. и распечатаем заголовок следующего документа
            printDocEnd();
        } else {
            // а вот нету открытого документа - нечего аннулировать
            log.trace("annul(): no one document is opened");
        }

        log.trace("leaving annul()");
    }

    @Override
    public void regSale(ShtrihPosition position) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regSale(ShtrihPosition). The argument is: position [{}]", position);

        if (position == null) {
            log.error("leaving regSale(ShtrihPosition): the argument is NULL!");
        }

        // отредактируем, если надо, номер отдела:
        if (position.getDeptNo() < 1 || position.getDeptNo() > getDeptCount()) {
            log.warn("regSale(ShtrihPosition): the \"deptNo\" of the argument was invalid ({})", position.getDeptNo());
            position.setDeptNo((byte) 1);
        }

        // исполним команду:
        RegSaleCommand cmd = new RegSaleCommand(position, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving regSale(ShtrihPosition)");
    }

    @Override
    public void regOperation(ShtrihOperation operation) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regOperation(ShtrihOperation). The argument is: position [{}]", operation);

        if (operation == null) {
            log.error("leaving regOperation(ShtrihOperation): the argument is NULL!");
        }

        // отредактируем, если надо, номер отдела:
        if (operation.getDepartment() < 1 || operation.getDepartment() > getDeptCount()) {
            log.warn("regOperation(ShtrihOperation): the \"deptNo\" of the argument was invalid ({})", operation.getDepartment());
            operation.setDepartment((byte) 1);
        }

        // исполним команду:
        RegOperationCommand cmd = new RegOperationCommand(operation, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving regOperation(ShtrihOperation)");
    }

    @Override
    public void regReturn(ShtrihPosition position) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regReturn(ShtrihPosition). The argument is: position [{}]", position);

        if (position == null) {
            log.error("leaving regReturn(ShtrihPosition): the argument is NULL!");
        }

        // отредактируем, если надо, номер отдела:
        if (position.getDeptNo() < 1 || position.getDeptNo() > getDeptCount()) {
            log.warn("regReturn(ShtrihPosition): the \"deptNo\" of the argument was invalid ({})", position.getDeptNo());
            position.setDeptNo((byte) 1);
        }

        // исполним команду:
        RegReturnCommand cmd = new RegReturnCommand(position, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving regReturn(ShtrihPosition)");
    }

    @Override
    public void regDiscount(ShtrihDiscount discount) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regDiscount(ShtrihDiscount). The argument is: discount [{}]", discount);

        if (discount == null) {
            log.error("leaving regDiscount(ShtrihDiscount): The argument is NULL");
        }

        RegDiscountCommand cmd = new RegDiscountCommand(discount, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving regDiscount(ShtrihDiscount)");
    }

    public void regMargin(ShtrihDiscount discount) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regMargin(ShtrihDiscount). The argument is: discount [{}]", discount);

        if (discount == null) {
            log.error("leaving regMargin(ShtrihDiscount): The argument is NULL");
        }

        RegMarginCommand cmd = new RegMarginCommand(discount, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving regMargin(ShtrihDiscount)");
    }

    @Override
    public void sendItemCode(ShtrihItemCode itemCode)  throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering sendItemCode(). The argument is: itemCode [{}]", itemCode);

        if (itemCode == null) {
            log.error("leaving sendItemCode(): The argument is NULL");
        }

        FNSendItemCodeCommand cmd = new FNSendItemCodeCommand(itemCode, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving sendItemCode()");
    }

    @Override
    public void closeReceipt(ShtrihReceiptTotal receiptTotal) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering closeReceipt(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotal);

        if (receiptTotal == null) {
            log.error("leaving closeReceipt(ShtrihReceiptTotal): the argument is NULL!");
        }

        // исполним команду:
        CloseReceiptCommand cmd = new CloseReceiptCommand(receiptTotal, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving closeReceipt(ShtrihReceiptTotal)");
    }

    @Override
    public void closeReceiptEx(ShtrihReceiptTotalEx receiptTotalEx) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering closeReceiptEx(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotalEx);

        if (receiptTotalEx == null) {
            log.error("leaving closeReceiptEx(ShtrihReceiptTotal): the argument is NULL!");
        }

        // исполним команду:
        CloseReceiptCommandEx cmd = new CloseReceiptCommandEx(receiptTotalEx, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving closeReceiptEx(ShtrihReceiptTotal)");
    }

    @Override
    public void closeReceiptV2Ex(ShtrihReceiptTotalV2Ex receiptTotalV2Ex) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering closeReceiptV2Ex(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotalV2Ex);

        if (receiptTotalV2Ex == null) {
            log.error("leaving closeReceiptV2Ex(ShtrihReceiptTotal): the argument is NULL!");
        }

        // исполним команду:
        CloseReceiptV2CommandEx cmd = new CloseReceiptV2CommandEx(receiptTotalV2Ex, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving closeReceiptV2Ex(ShtrihReceiptTotal)");
    }

    /**
     * Вернет номер таблицы, в которой хранятся запрограммированные названия заголовков (клише) и подвалов (рекламный текст) чеков.
     *
     * @return номер таблицы
     */
    protected byte getHeadersAndFootersTableNo() {
        return 4;
    }

    /**
     * Вернет количество строк рекламного текста, что хранится в таблице {@link #getHeadersAndFootersTableNo()}.
     *
     * @return 3 (для всех 5ти используемых моделей ФР "Штрих"(2016-01-27))
     */
    protected int getFooterLinesCount() {
        return 3;
    }

    /**
     * Вернет количество строк клише, поддерживаемое данной моделью ФР.
     *
     * @return не отрицательное число
     */
    private int getClicheLinesCount() throws IOException, PortAdapterException, ShtrihException {
        TableStructure tableStructure = getTableStructure(getHeadersAndFootersTableNo());
        return tableStructure.getRowsCount() - getFooterLinesCount();
    }

    /**
     * Вернет длину каждой линии текста клише.
     */
    private int getClicheLineLength() throws IOException, PortAdapterException, ShtrihException {
        FieldStructure fs = getFieldStructure(getHeadersAndFootersTableNo(), (byte) 1);
        return fs.getFieldWidth();
    }

    /**
     * Очистит клише (реквизиты/заголовок чека).
     *
     * @param clicheLinesCount
     *            количество строк клише, поддерживаемое данной моделью ФР
     * @param clicheLineLenght
     *            допустимая длина каждой строки клише
     */
    private void clearCliche(int clicheLinesCount, int clicheLineLenght) throws IOException, PortAdapterException, ShtrihException {
        byte[] empty = new byte[clicheLineLenght];
        for (int i = 0; i < clicheLinesCount; i++) {
            writeTable(getHeadersAndFootersTableNo(), getFooterLinesCount() + 1 + i, (byte) 1, empty);
        }
    }

    @Override
    public void setHeader(List<FontLine> header) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering setHeader(List). The argument is: header [{}]", header);

        // 0. сначала сохраним это в оперативке
        this.header = header;

        // 1. Здесь надо полностью очистить текст клише
        int clicheLinesCount = getClicheLinesCount();
        int clicheLineLenght = getClicheLineLength();
        if (clicheLinesCount <= 0 || clicheLineLenght <= 0) {
            // видимо, клише не поддерживается
            log.warn("leaving setHeader(List). Cliche is not supported [cliche lines: {}; cliche line lengths: {}]",
                clicheLinesCount, clicheLineLenght);
            return;
        }
        clearCliche(clicheLinesCount, clicheLineLenght);

        log.debug("setHeader(List) skip");

        // 2. и записать в клише то, что нам прислали, если настроена печать из юридического лица
        if (!isPrintLegalEntityHeader()) {
            List<byte[]> lines = new LinkedList<>();
            int count = 0;
            for (FontLine fl : header) {
                if (++count > clicheLinesCount) {
                    log.warn("text: {} will NOT be included in header 'cause there is no space left for it", fl);
                    continue;
                }
                if (fl != null && fl.getContent() != null) {
                    byte[] data = fl.getContent().isEmpty() ? new byte[]{SPACE} : fl.getContent().getBytes(ENCODING);
                    if (data.length > clicheLineLenght) {
                        data = Arrays.copyOfRange(data, 0, clicheLineLenght);
                    }
                    log.trace("\"{}\" was encoded into {}", fl.getContent(), PortAdapterUtils.arrayToString(data));
                    lines.add(data);
                }
            }
            for (int idx = 0; idx < lines.size(); idx++) {
                byte[] data = lines.get(idx);
                writeTable(getHeadersAndFootersTableNo(), idx + 1 + getFooterLinesCount(), (byte) 1, data);
            }
        }

        log.debug("leaving setHeader(List)");
    }

    /**
     * Вернет индекс 1го элемента из указанного массива, что равен указанному числу.
     *
     * @param data
     *            массив, в котором ведем поиск
     * @param marker
     *            значение, что ищем в массиве
     * @return отрицательное число, если указанное значение в массиве не нашлось
     */
    private int getFirstIndexOf(byte[] data, byte marker) {
        if (data == null || data.length == 0) {
            return -1;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] == marker) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Вернет "шапку", что будет печататься в заголовке <b>следующего</b> документа по завершению печати <b>текущего</b>.
     *
     * @return не <code>null</code> - в крайнем случае вернет пустую коллекцию, допускающую модификацию (добавление/удаление элементов)
     */
    protected List<FontLine> getHeader() throws IOException, PortAdapterException, ShtrihException {
        if (!isPrintLegalEntityHeader()) {
            return Collections.emptyList();
        }
        if (header == null) {
            // Придется читать из таблицы. Видимо, после открытия смены кассу перегрузили - из оперативки инфа о реквизитах пропала
            //  восстановим. Правда инфу о шрифте при этом потеряем (пока что 2016-01-27 - шрифт заголовка можно сохранять и считывать из другой таблицы)
            header = new LinkedList<>();

            int clicheLinesCount = getClicheLinesCount();
            for (int idx = 0; idx < clicheLinesCount; idx++) {
                byte[] data = readTable(getHeadersAndFootersTableNo(), idx + 1 + getFooterLinesCount(), (byte) 1);
                if (data == null || data.length == 0 || data[0] == 0x00) {
                    // осознанно запрограммированная часть клише закончилась
                    break;
                }
                // выкинем бессмысленные символы:
                int firstWrongByteIdx = getFirstIndexOf(data, (byte) 0x00);
                if (firstWrongByteIdx > 0) {
                    data = Arrays.copyOf(data, firstWrongByteIdx);
                }
                String line = new String(data, ENCODING);
                line = StringUtils.left(line, maxCharsInRow);
                log.info("{} converted into \"{}\"", PortAdapterUtils.arrayToString(data), line);
                FontLine fl = new FontLine(line, Font.NORMAL);
                header.add(fl);
            } // for idx
        }
        return header;
    }

    @Override
    public void openCashDrawer(byte cashDrawerNumber) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering openCashDrawer(byte). the argument is: cashDrawerNumber [{}]", cashDrawerNumber);
        executeAndThrowExceptionIfError(new OpenCashDrawerCommand(cashDrawerNumber, getPassword()));
        log.trace("leaving openCashDrawer(byte)");
    }

    @Override
    public ShtrihShortStateDescription getShortState() throws IOException, PortAdapterException, ShtrihException {
        ShtrihShortStateDescription result = null;

        log.trace("entering getShortState()");

        // 1. Исполним запрос
        GetShortStatusCommand cmd = new GetShortStatusCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getShortState(). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihStateDescription getState() throws IOException, PortAdapterException, ShtrihException {
        ShtrihStateDescription result = null;

        log.trace("entering getState()");

        // 1. считаем статус
        GetStatusCommand cmd = new GetStatusCommand(getPassword());
        byte[] response = transport.execute(cmd.getCommandAsByteArray(), cmd.getMaxResponseTime());

        // 2. получим объектное представление ответа
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден
            throw new ShtrihResponseParseException(String.format("the response [%s] is NOT valid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getState(). The result is: {}", result);

        return result;
    }

    @Override
    public FiscalMemorySums getFiscalMemorySums(boolean all) throws IOException, PortAdapterException, ShtrihException {
        FiscalMemorySums result = null;

        log.trace("entering getFiscalMemorySums(boolean). The argument is: all [{}]", all);

        GetFiscalMemorySums cmd = new GetFiscalMemorySums(all, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);

        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден
            throw new ShtrihResponseParseException(String.format("the response [%s] is NOT valid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getFiscalMemorySums(boolean). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihRegNum getRegNum() throws IOException, PortAdapterException, ShtrihException {
        ShtrihRegNum result = null;

        log.trace("entering getRegNum()");

        GetRegNoCommand cmd = new GetRegNoCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);

        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден
            throw new ShtrihResponseParseException(String.format("the response [%s] is NOT valid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getRegNum(). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihDeviceType getDeviceType() throws IOException, PortAdapterException, ShtrihException {
        ShtrihDeviceType result = null;

        log.trace("entering getDeviceType()");

        GetDeviceTypeCommand cmd = new GetDeviceTypeCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getDeviceType(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет строковое представление указанного массива байт в кодировке {@link #ENCODING}.
     *
     * @param data
     *            массив байт, что надо преобразовать в строку
     * @return не <code>null</code>
     */
    private String getString(byte[] data) {
        return new String(data, ENCODING);
    }

    /**
     * По факту обратный метод методу {@link #getString(byte[])}: указанную строку вернет в виде массива байт в кодировке {@link #ENCODING}.
     *
     * @param string
     *            строка, чье "байтовое" представление надо вернуть
     * @return не <code>null</code>
     */
    private byte[] getBytes(String string) {
        return string.getBytes(ENCODING);
    }

    @Override
    public void printBarcode(BarCode barcode, String label, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        long stopWatch = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("entering printBarcode(BarCode, String, ShtrihAlignment). " +
                "The arguments are: barcode [{}], label [{}], alignment [{}]", new Object[] {barcode, label, alignment});
        }

        if (!validate(barcode)) {
            log.error("leaving printBarcode(BarCode, String, ShtrihAlignment): the argument ({}) is INVALID!", barcode);
            throw new IllegalArgumentException("printBarcode(BarCode, String, ShtrihAlignment): the argument is INVALID!");
        }

        // 1. распечатаем ШК
        boolean printed = printBarcodeInner(barcode, alignment);

        // 2. И подпись под ШК, если надо
        if (printed && label != null) {
            // 2.1. получим выровнянную версию этой "подписи" ШК
            String alignedLine = getAlignedLine(label, alignment);

            // 2.2. и распечатаем как строку нормального текста
            printLine(new FontLine(alignedLine, Font.NORMAL));
        }

        log.debug("leaving printBarcode(BarCode, String, ShtrihAlignment). It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    /**
     * Вернет аргумент дополненный по краям (в зависимости от способа выравнивания) пробелами - до размера ширины чековой ленты (при печати нормальным
     * шрифтом).
     *
     * @param text
     *            текст, что надо "выровнять"
     * @param alignment
     *            способ выравнивания текста; <code>null</code> распознается как {@link ShtrihAlignment#CENTRE}
     * @return <code>null</code>, если аргумент == <code>null</code>; <em>обрезанную</em> (справа) версию аргумента, если данная строка уже и так шире
     *         ...ширины чековой ленты
     */
    private String getAlignedLine(String text, ShtrihAlignment alignment) {
        if (text == null) {
            return null;
        }

        int ribbonWidth = getMaxCharsInRow();
        if (ribbonWidth <= text.length()) {
            // надо вернуть урезанную версию аргумента
            return text.substring(0, ribbonWidth);
        }

        // выравниваем:
        if (alignment == null || ShtrihAlignment.CENTRE.equals(alignment)) {
            return StringUtils.center(text, ribbonWidth);
        } else if (ShtrihAlignment.RIGHT.equals(alignment)) {
            return StringUtils.leftPad(text, ribbonWidth);
        }
        return StringUtils.rightPad(text, ribbonWidth);
    }

    /**
     * Ожидает возможности начала печати. Бонусом вернет текущее состояние принтера.
     * <p/>
     * NOTE: этот метод потенциально может "зависнуть" - если аппаратная реализация неконсистентна.
     *
     * @return текущее состояние принтера
     * @throws ShtrihException
     *             если нет возможности начать печатать. например, если нету бумаги
     */
    protected ShtrihStateDescription waitForPrinting() throws IOException, PortAdapterException, ShtrihException {
        ShtrihStateDescription result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering waitForPrinting()");

        while (true) {
            result = getState();
            ShtrihSubState subMode = result.getSubState();
            ShtrihModeEnum mode = result.getMode().getStateNumber();

            // если нет бумаги - Exception
            if (ShtrihSubState.PAPER_ABSENT_ACTIVELY.equals(subMode) || ShtrihSubState.PAPER_ABSENT_PASSIVELY.equals(subMode)) {
                log.error("waitForPrinting(): paper is absent");
                throw new ShtrihResponseException(ShtrihResponseException.WRONG_SUBSTATE, (byte) 0, result);
            }

            // если есть бумага ...
            //  и при этом сейчас ничего не печатается, то можем печатать
            if (ShtrihSubState.PAPER_PRESENT.equals(subMode) && !ShtrihModeEnum.isPrinting(mode)) {
                // просто выйдем из цикла
                break;
            }

            // если ФР в состоянии "появилась бумага после ее активного отсутствия", то надо ФР сказать,
            //  что можем продолжить работу:
            if (ShtrihSubState.WAITING.equals(subMode)) {
                ContinuePrintingCommand cmd = new ContinuePrintingCommand(getPassword());
                byte[] response = executeOnce(cmd);
                // 2. выкинем Exception, если ответ невалиден
                throwExceptionIfError(cmd.getCommandCode(), response);
            }

            // подождем и считаем состояние ФР еще раз - в цикл
            try {
                Thread.sleep(getCheckStatusInterval());
            } catch (InterruptedException e) {
                log.error("waitForPrinting(): interrupted!", e);
            }
        }

        log.trace("leaving waitForPrinting(). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Разобъет указанную строку на кусочки длиной не более максимальной длины строоки указанным шрифторм.
     *
     * @param text текст, что надо разбить
     * @param fontNo номер шрифта. которым хотим этот текст печатать
     * @return не <code>null</code>; и размер будет как минимум <code>1</code> - как минимум содержащий сам <code>text</code>
     */
    private List<String> getLines(String text, byte fontNo) throws IOException, PortAdapterException, ShtrihException {
        List<String> result = null;

        if (StringUtils.isEmpty(text)) {
            // не надо шрифты анализировать
            return Collections.singletonList(text);
        }

        // получим настройки указанного шрифта
        ShtrihFontProperties fontProperties = getFontProperties(fontNo);
        if (fontProperties == null) {
            // какой-то левый шрифт хотят использовать при печати?
            log.error("getLines(String, byte): the font (#{}) is INVALID!", fontNo);
            return Collections.singletonList(text);
        }

        // узнаем сколько символов этим шрифтом может поместиться в строке
        int symbolsPerLine = 0;
        if (fontProperties.getSymbolWidth() != 0) {
            symbolsPerLine = fontProperties.getPrintableAreaWidth() / fontProperties.getSymbolWidth();
        }
        if (symbolsPerLine < 1) {
            log.error("getLines(String, byte): the font (#{}) is INVALID: {}", fontNo, fontProperties);
            return Collections.singletonList(text);
        }

        // а теперь просто разбиваем TEXT на строки длиной до symbolsPerLine
        result = new LinkedList<>();
        int beginIndex = 0;
        String nextLine = null;
        do {
            int endIndex = Math.min(beginIndex + symbolsPerLine, text.length());
            nextLine = text.substring(beginIndex, endIndex);
            if (StringUtils.isNotEmpty(nextLine)) {
                result.add(nextLine);
                beginIndex += nextLine.length();
            } else {
                break;
            }
        } while (StringUtils.isNotEmpty(nextLine));

        return result;
    }

    @Override
    public void printLine(FontLine line) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering printLine(FontLine). The argument is: line [{}]", line);

        if (line == null) {
            log.error("leaving printLine(FontLine): the argument ({}) is INVALID!", line);
            throw new IllegalArgumentException("printLine(FontLine): the argument is INVALID!");
        }

        // 1. выясним, каким шрифтом надо эту строку печатать:
        byte desirableFont = line.getConcreteFont() == null || line.getConcreteFont().intValue() == 0
                ? ((byte) getPrintStringFont()) : line.getConcreteFont().byteValue();
        log.trace("desirable font is: {}", desirableFont);

        // 1.1. И разобъем эту строку на кусочки (если строка слишком длинная):
        List<String> lines = getLines(line.getContent(), desirableFont);

        // 2. А теперь эти строчки просто распечатаем:
        for (String text : lines) {
            PrintLineUsingFontCommand cmd = new PrintLineUsingFontCommand(text, desirableFont, false, true, getPassword());
            executeAndThrowExceptionIfError(cmd);
        } // for text

        log.debug("leaving printLine(FontLine)");
    }

    @Override
    public void openNonFiscalDocument() throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering openNonFiscalDocument()");
        try {
            executeAndThrowExceptionIfError(new OpenNonFiscalDocumentCommand(getPassword()));
        } catch (ShtrihResponseException sre) {
            if (sre.getErrorCode() == COMMAND_NOT_SUPPORTED) {
                // норма
                log.trace("openNonFiscalDocument(): command not supported");
            } else {
                throw sre;
            }
        }
        log.trace("leaving openNonFiscalDocument()");
    }

    @Override
    public void closeNonFiscalDocument(AbstractDocument document) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering closeNonFiscalDocument()");

        // 1. сначала допечатаем документ
        // отезка отключается вместе с заголовком следующего документа (по аналогии с пиритом)
        if (document == null || document.getNextServiceDocument() == null || !document.getNextServiceDocument().isDisableCut()) {
            printDocEnd();
        }

        // 2. а потом закроем его
        try {
            executeAndThrowExceptionIfError(new CloseNonFiscalDocumentCommand(getPassword()));
        } catch (ShtrihResponseException sre) {
            if (sre.getErrorCode() == COMMAND_NOT_SUPPORTED) {
                // норма
                log.trace("closeNonFiscalDocument(): command not supported");
            } else {
                throw sre;
            }
        }
        log.trace("leaving closeNonFiscalDocument()");
    }

    @Override
    public void printXReport() throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering printXReport()");

        // 1. распечатаем X-отчет
        executeAndThrowExceptionIfError(new PrintXReportCommand(getPassword()));

        // 2. и шапку следующего документа
        printReportEnd();

        log.trace("leaving printXReport()");
    }

    @Override
    public void printZReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering printZReport()");

        // 1. дождемся когда принтер закончит предыдущую печать (если есть)
        ShtrihStateDescription state = waitForPrinting();

        // 2. и, если смена не открыта, сначала пробьем нулевой чек
        if (!ShtrihModeEnum.canCloseShift(state.getMode().getStateNumber())) {
            // надо пробить нулевой чек в 1м попавшемся отделе - тупо чтоб открыть смену
            // 2.1. регистрируем нулевую позицию:
            ShtrihPosition position = new ShtrihPosition("", 0L, 0L, (byte) 1);
            regSale(position);

            // 2.2. и закрываем этот нулевой чек
            ShtrihReceiptTotal receipt = new ShtrihReceiptTotal("", 0L);
            closeReceipt(receipt);
        }

        // 3. распечатаем Z-отчет
        PrintZReportCommand cmd = new PrintZReportCommand(getPassword());
        executeAndThrowExceptionIfError(cmd);

        // 4. и шапку следующего документа
        printReportEnd();

        log.trace("leaving printZReport()");
    }

    @Override
    public void printFNReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        // для регистраторов с ФН
    }

    @Override
    public void printLogo() throws IOException, PortAdapterException, ShtrihException {
        if (getImageFirstLine() != null && getImageLastLine() != null && getImageLastLine() > getImageFirstLine()) {
            executeAndThrowExceptionIfError(new PrintExtGraphicsCommand(getImageFirstLine(), getImageLastLine(), getPassword(), null));
        }
    }

    @Override
    public void regCashIn(long sum) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regCashIn(long). The argument is: sum [{}]", sum);

        // 0. операцию всегда выполняем от 1го кассира - только его имя регистрируется по техпроцессу.
        Integer pass = getCashierPassword((byte) 1);
        if (pass == null) {
            // пароль не удалось считать
            pass = getPassword();
        }

        // 1. зарегистрируем внесение
        CashInCommand cmd = new CashInCommand(sum, pass);
        byte[] response = executeAndThrowExceptionIfError(cmd);

        // 2. и печатаем шапку следующего документа
        printDocEnd();

        log.trace("leaving regCashIn(long). docNo is: {}", cmd.decodeResponse(response));
    }

    @Override
    public void regCashOut(long sum) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering regCashOut(long). The argument is: sum [{}]", sum);

        // 0. операцию всегда выполняем от 1го кассира - только его имя регистрируется по техпроцессу.
        Integer pass = getCashierPassword((byte) 1);
        if (pass == null) {
            // пароль не удалось считать
            pass = getPassword();
        }

        // 1. зарегистрируем внесение
        CashOutCommand cmd = new CashOutCommand(sum, pass);
        byte[] response = executeAndThrowExceptionIfError(cmd);

        // 2. и печатаем шапку следующего документа
        printDocEnd();

        log.trace("leaving regCashOut(long). docNo is: {}", cmd.decodeResponse(response));
    }

    @Override
    public void openShift(Cashier cashier) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering openShift()");
        executeAndThrowExceptionIfError(new OpenShiftCommand(getPassword()));
        log.trace("leaving openShift()");
    }

    /**
     * Выкинет {@link ShtrihResponseException}, если указанный ответ на указанную команду сигнализирует об ошибке.
     *
     * @param command команда, что выполняли - просто для логов
     * @param response ответ, полученный от ВУ
     * @throws ShtrihResponseException
     *             если ответ сигнализирует об ошибке
     */
    private void throwExceptionIfError(byte command, byte[] response) throws IOException, PortAdapterException, ShtrihResponseException {
        // Нас интересует только 4й байт: после STX, байта количества, и кода команды следует байт кода ошибки:
        byte errorCode = getErrorCode(response);
        if (ShtrihConnector.NO_ERROR != errorCode) {
            // есть ошибка
            log.error("leaving throwExceptionIfError(command: {}). the error code is: {}",
                PortAdapterUtils.toUnsignedByte(command), PortAdapterUtils.toUnsignedByte(errorCode));

            // вытащим состояние устройства - если можно:
            ShtrihStateDescription state = null;
            try {
                state = getState();
            } catch (Throwable t) {
                // ну не судьба. Видно, фискальнику совсем плохо - значит без состояния ошибку создадим
                log.warn("failed to get status", t);
            }

            throw new ShtrihResponseException(errorCode, command, state);
        }
    }

    /**
     * Исполнит указанную команду с указанными аргументами и выбросит <code>ShtrihException</code>, если получит ошибочный ответ от внешнего
     * устройства.
     * <p/>
     * По факту это пара вызовов: {@link #execute(BaseCommand)} + {@link #throwExceptionIfError(byte, byte[])}.
     *
     * @param command
     *            команда, что надо исполнить
     * @return ответ от внешнего устройства
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     */
    protected byte[] executeAndThrowExceptionIfError(BaseCommand<?> command) throws IOException, PortAdapterException, ShtrihException {
        byte[] result = execute(command);
        throwExceptionIfError(command.getCommandCode(), result);
        return result;
    }

    /**
     * Исполнит указанную команду с указанными аргументами.
     *
     * @param command
     *            команда, что надо исполнить
     * @return ответ от внешнего устройства
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     */
    private byte[] execute(BaseCommand<?> command) throws IOException, PortAdapterException, ShtrihException {
        byte[] result = null;

        // 1. Подготовим запрос
        byte[] request = command.getCommandAsByteArray();


        while (true) {
            // 2. выполним запрос
            result = transport.execute(request, command.getMaxResponseTime());

            // 3. и распарсим ответ
            byte errorCode = getErrorCode(result);
            switch(errorCode) {
                case NO_ERROR:
                    // 3.1. если ответ нормальный - просто выйдем: команда исполнена
                    return result;
                case PREVIOUS_PRINT_ORDER_IS_PROGRESS_ERROR:
                    // 3.2. если ошибка типа "идет печать предыдущей команды" - то ждем, потом повторим
                    waitForPrinting();
                    break;
                case CONTINUE_PRINTING_COMMAND_EXPECTED_ERROR:
                    // 3.3. если ошибка типа "Ожидание команды продолжения печати" - выполняем команду продолжения печати и ждем:
                    ContinuePrintingCommand cmd = new ContinuePrintingCommand(getPassword());
                    byte[] response = executeOnce(cmd);
                    throwExceptionIfError(cmd.getCommandCode(), response);
                    waitForPrinting();
                    break;
                default:
                    // 3.4. иначе тоже просто вернем ошибочный ответ - наверху разберутся
                    return result;
            } // switch
        }
    }

    /**
     * Исполнит указанную команду с указанными аргументами.
     * <p/>
     * Отличие от {@link #execute(BaseCommand)} состоит в том, что данная команда будет выполнена только один раз, и, если будет получена ошибка типа
     * "устройство пока занято - попробуйте повторить запрос по-позже", просто вернет эту ошибку - в отличие от {@link #execute(BaseCommand)} (что
     * будет ждать, и повторять запрос-команду до тех пор пока устройство не "освободится").
     *
     * @param command
     *            команда, что надо исполнить
     * @return ответ от внешнего устройства
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     */
    protected byte[] executeOnce(BaseCommand<?> command) throws IOException, PortAdapterException, ShtrihException {
        byte[] result = null;

        // 1. Подготовим запрос
        byte[] request = command.getCommandAsByteArray();

        // 2. выполним запрос
        result = transport.execute(request, command.getMaxResponseTime());

        return result;
    }

    /**
     * Вернет указанную строку текста в виде массива байт, недостающие до длинны байты будут заполнены пробелами.
     * <p/>
     * NOTE: аргументы не валидируются.
     *
     * @param text
     *            строка текста, что надо преобразовать в массив байт перед записью/печатью в ФР
     * @param length
     *            задает размер возвращаемого массива; если строка в результате преобразования в массив этого размера не поместится, то последние
     *            символы будут просто отброшены
     * @return представление строки-аргумента в виде массива байт в кодировке {@link #ENCODING}.
     */
    protected static byte[] getStringAsByteArray(String text, int length) {
        return getStringAsByteArray(text, length, SPACE);
    }

    /**
     * Вернет указанную строку текста в виде массива байт.
     * <p/>
     * NOTE: аргументы не валидируются.
     *
     * @param text
     *            строка текста, что надо преобразовать в массив байт перед записью/печатью в ФР
     * @param length
     *            задает размер возвращаемого массива; если строка в результате преобразования в массив этого размера не поместится, то последние
     *            символы будут просто отброшены
     * @param fillChar
     *            недостающие до длинны байты будут заполнены этим символом
     * @return представление строки-аргумента в виде массива байт в кодировке {@link #ENCODING}.
     */
    private static byte[] getStringAsByteArray(String text, int length, byte fillChar) {
        byte[] result = new byte[length];

        // 1. заполним всю строку символом fillChar
        Arrays.fill(result, fillChar);

        // 2. а теперь текстом:
        if (StringUtils.isEmpty(text)) {
            // аргумент пуст - вернем строку из пробелов
            return result;
        }
        byte[] textAsArray = text.getBytes(ENCODING);

        if (textAsArray.length > length) {
            if (log.isWarnEnabled()) {
                log.warn("The length of the string to print: \"{}\" is too big ({}) it will be trimmed to {} bytes",
                    new Object[] {text, textAsArray.length, length});
            }
        }
        System.arraycopy(textAsArray, 0, result, 0, Math.min(textAsArray.length, length));

        return result;
    }

    /**
     * Печатает указанную линию графической информации.
     * <p/>
     * NOTE: если аргументы невалидны, то ничего не будет сделано.
     *
     * @param data
     *            графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     *            <code>1</code> - черный
     * @param times
     *            Количество повторов (сколько линий подряд должны выглядеть точно так же на чеке)
     * @return <code>true</code>, если эта линия была распечатана; если линия не была распечатана (например, из-за того, что аргументы невалидны), то
     *         вернет <code>false</code>
     */
    private boolean printLine(byte[] data, int times) throws IOException, PortAdapterException, ShtrihException {
        if (log.isTraceEnabled()) {
            log.trace("entering printLine(byte[], int). The arguments are: data [{}], times [{}]", PortAdapterUtils.arrayToString(data), times);
        }

        // 1. валидация (косвенно) аргументов
        PrintLineCommand cmd;
        try {
            cmd = new PrintLineCommand(times, data, getPassword(), isNeedRevertBytes());
        } catch (Throwable t) {
            log.error(String.format("leaving printLine(byte[], int): at least one of the arguments is invalid: " +
                "either data [%s], or times [%s]", PortAdapterUtils.arrayToString(data), times), t);
            return false;
        }

        // 2. исполним запрос
        executeAndThrowExceptionIfError(cmd);

        // 3. и подождем пока аппаратура _реально_ исполнит эту команду
        exceptionFreeSleep(getPrintLineTime());

        log.trace("leaving printLine(byte[], int)");

        return true;
    }

    /**
     * Просто задержка на указанное время, в мс.
     *
     * @param duration задержка, в мс
     */
    private void exceptionFreeSleep(long duration) {
        if (duration <= 0) {
            return;
        }
        long timeThreshold = System.currentTimeMillis() + duration;
        long timeLeft = duration;
        while (timeLeft > 0) {
            try {
                Thread.sleep(timeLeft);
                // успех: никто "сон" не прервал
                break;
            } catch (InterruptedException e) {
                // и что делать?
                log.error("sleep interrupted!", e);
                timeLeft = timeThreshold - System.currentTimeMillis();
            }
        }
    }

    /**
     * Выполняет действия ДО печати ШК через [последователность] печати линийи.
     * <p/>
     * Просто некоторые ШК требуют распечатать пустую строку перед выполнением команды "Печать линии".
     */
    protected void doBeforePrintLinesBullshit() throws IOException, PortAdapterException, ShtrihException {
        // do nothing
    }

    /**
     * Выполняет действия после завершения печати ШК через [последователность] печати линийи.
     * <p/>
     * Просто некоторые ШК требуют после этого распечатать пустую строку - чтобы следующая строка была видна на чеке.
     */
    protected void doAfterPrintLinesBullshit() throws IOException, PortAdapterException, ShtrihException {
        // do nothing
    }

    /**
     * Флаг-признак означающий, что данная модель ФР поддерживает аппратную печать ШК (вернее команду печати 2D ШК - см. {@link Print2DBarcodeCommand})
     */
    private Boolean ableToPrint2DBarcodes;

    /**
     * Печатает 2D ШК аппаратно (через команду печати 2D ШК - см. {@link Print2DBarcodeCommand}).
     *
     * @param barcode ШК, что надо распечатать
     * @param alignment выравнивание этого ШК на чековой ленте
     * @return <code>true</code>, если ШК был распечатан; <code>false</code> - если нет (возможно, данная модель ФР просто не поддерживает команду печати 2D ШК)
     */
    private boolean print2DBarcode(BarCode barcode, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        boolean result = false;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering print2DBarcode(BarCode, ShtrihAlignment). The arguments are: barcode [{}], alignment [{}]", barcode, alignment);

        if (Boolean.FALSE.equals(ableToPrint2DBarcodes)) {
            // уже пробовали печатать 2D ШК и знаем, что данная модель ФР этот функционал не поддерживает
            log.trace("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (IN-MEMORY): FALSE");
            return false;
        }

        // 1. Определим тип этого ШК
        Shtrih2DBarcodeType type = getBarcodeType(barcode);
        if (type == null) {
            // это не знакомый нам 2D ШК - не будем печатать аппаратно
            log.trace("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (the barcode is not 2D): FALSE");
            return false;
        }
        log.trace("2D barcode type is: {}", type);

        // 2. Получим данные этого ШК:
        byte[] data = getBarcodeValue(barcode);
        if (data == null) {
            // это пустой ШК - не будем его печатать
            log.warn("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (the barcode value is EMPTY): FALSE");
            return false;
        }
        log.trace("barcode data is ({}): {}", data.length, PortAdapterUtils.arrayToString(data));

        // 3. загрузим данные
        boolean loadedSuccessfully = load2DBarcodeData(data);
        if (!loadedSuccessfully) {
            // данная модель ФР, видимо, не поддерживает эту команду
            log.warn("leaving print2DBarcode(BarCode, ShtrihAlignment): {}: this fiscal registry does not support 2D parcodes printing", this);
            ableToPrint2DBarcodes = false;
            return false;
        }

        // 4. и распечатаем
        Shtrih2DBarcodeAlignment align = convertAlignment(alignment);
        Print2DBarcodeCommand cmd = new Print2DBarcodeCommand(type, data.length, 0, getPrint2DParameters(type), align, getPassword());
        log.trace("printing 2D barcode command: {}", cmd);

        byte[] response = execute(cmd);
        byte errorCode = getErrorCode(response);
        if (ShtrihConnector.NO_ERROR != errorCode) {
            // ошибка! видимо, этот ФР не умеет печатать QR-коды
            log.warn("Command: {} failed. Error code: {}", cmd, PortAdapterUtils.toUnsignedByte(errorCode));
            ableToPrint2DBarcodes = false;
            result = false;
        } else {
            // этот ФР умеет! Похоже, реально 2D ШК распечатан
            result = true;
        }

        log.trace("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Вернет параметры для печати ШК указанного типа
     *
     * @param type тип ШК, для которого надо вернуть параметры
     * @return массив из 5ти (жестко) элементов-параметров
     */
    private byte[] getPrint2DParameters(Shtrih2DBarcodeType type) {
        byte[] result = new byte[5];

        if (Shtrih2DBarcodeType.QR.equals(type)) {
            // Version,0=auto
            result[0] = 0;
            // Dot size, 3-8
            int dotSize = getMaxScale();
            if (dotSize < 3) {
                dotSize = 3;
            }
            if (dotSize > 8) {
                dotSize = 8;
            }
            result[2] = (byte) dotSize;

            // Error correction level, 0-3
            result[4] = 1;
        }
        // остальные типы 2D ШК пока (2016-02-09) не знаем

        return result;
    }

    /**
     * Сконвертнет указанное выравнивание в другой формат - в формат, понятный команде {@link Print2DBarcodeCommand}.
     *
     * @param alignment выравнивание, что надо сконвертнуть
     * @return не <code>null</code> - в крайнем случае вернет {@link Shtrih2DBarcodeAlignment#CENTER}
     */
    private Shtrih2DBarcodeAlignment convertAlignment(ShtrihAlignment alignment) {
        Shtrih2DBarcodeAlignment result;

        if (ShtrihAlignment.LEFT.equals(alignment)) {
            result = Shtrih2DBarcodeAlignment.LEFT;
        } else if (ShtrihAlignment.RIGHT.equals(alignment)) {
            result = Shtrih2DBarcodeAlignment.RIGHT;
        } else {
            result = Shtrih2DBarcodeAlignment.CENTER;
        }

        return result;
    }

    /**
     * Загружает указанные данные в ФР
     *
     * @param data данные, что надо загрузить
     * @return <code>false</code>, если не удалось загрузить данные
     */
    private boolean load2DBarcodeData(byte[] data) throws IOException, PortAdapterException, ShtrihException {
        boolean result = true;

        int offset = 0;
        int dataChunkNo = 0;
        while (offset < data.length) {
            byte[] chunk = new byte[64];
            int availableDataLength = Math.min(chunk.length, data.length - offset);
            System.arraycopy(data, offset, chunk, 0, availableDataLength);

            // самма загрузка
            LoadDataCommand cmd = new LoadDataCommand((byte) 0, dataChunkNo, chunk, getPassword());
            byte[] response = execute(cmd);
            byte errorCode = getErrorCode(response);
            if (ShtrihConnector.NO_ERROR != errorCode) {
                // ошибка! видимо, этот ФР не умеет печатать QR-коды (скорей всего код ошибки == 0x37 (Команда не поддерживается в данной реализации ФР))
                log.warn("Command: {} failed. Error code: {}", cmd, PortAdapterUtils.toUnsignedByte(errorCode));
                result = false;
                break;
            }

            // следующая порция
            dataChunkNo++;
            offset += availableDataLength;
        }

        return result;
    }

    /**
     * Вернет {@link BarCode#getValue() значение} указанного ШК в виде массива байт.
     *
     * @param barcode ШК, содержимое/значение которого надо вернуть
     * @return <code>null</code>, если не удалось сконвертнуть значение указанного ШК в массив байт - по любой причине
     */
    private byte[] getBarcodeValue(BarCode barcode) {
        byte[] result = null;

        if (barcode == null || StringUtils.isEmpty(barcode.getValue())) {
            return null;
        }

        result = barcode.getValue().getBytes(BaseCommand.ENCODING);

        return result;
    }

    /**
     * Вернет тип указанного ШК.
     *
     * @param barcode ШК, чей тип надо вернуть
     * @return <code>null</code>, если аргумент не является 2D ШК, либо его тип не известен
     */
    private Shtrih2DBarcodeType getBarcodeType(BarCode barcode) {
        Shtrih2DBarcodeType result = null;

        if (barcode == null) {
            return null;
        }

        if (BarCodeType.QR.equals(barcode.getType())) {
            result = Shtrih2DBarcodeType.QR;
        }
        // другие типы 2D ШК пока (2016-02-09) не знаем

        return result;
    }

    /**
     * Флаг-признак: данная модель ФР поддерживает команду "Печать графической линии" (0xC5)
     */
    private boolean supportsPrintLineCmd = true;

    /**
     * Печать ШК. Вернет <code>true</code>, если ШК был распечатан. Иначе вернет <code>false</code>.
     */
    private boolean printBarcodeInner(BarCode barcode, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        boolean result = false;
        long stopWatch = System.currentTimeMillis();

        log.debug("entering printBarcodeInner(BarCode, ShtrihAlignment). The arguments are: barcode [{}], alignment [{}]", barcode, alignment);

        // 0. сначала попробуем распечатать как 2D ШК
        if (print2DBarcode(barcode, alignment)) {
            log.debug("leaving printBarcodeInner(BarCode, ShtrihAlignment): The barcode was printed as 2D one. It took {} [ms]",
                System.currentTimeMillis() - stopWatch);
            return true;
        }
        // как 2D распечатать не удалось - будем пробовать по-другому

        // 1. Получим картинку:
        BitMatrix matrix = GraphicsUtils.getBarcodeAsBitMatrix(barcode);
        if (matrix == null) {
            throw new ShtrihInternalProcessingException(String.format("Failed to convert barcode [%s] into a picture", barcode.getValue()));
        }
        log.debug("matrix [width: {}; height: {}]", matrix.getWidth(), matrix.getHeight());
        log.debug(logMatrix(matrix));

        // 2. Печатаем получившуюся картинку
        // 2.1. Сначала - через "печать линии"
        boolean printed = false;
        if ((matrix.getHeight() == 1 || !isHighQualityGraphics()) && supportsPrintLineCmd) {
            log.trace("printBarcodeInner: trying to print bar-code [{}] through \"print line\" commnads", barcode);

            // ПЕРЕД печатью ШК рекомендуется распечать пустую строку - чтоб строка перед ШК не задублировалалсь
            doBeforePrintLinesBullshit();

            // это одномерный ШК - можем печатать как "печать линии" (0xC5) либо печать "высококачественной" графики отключена
            try {
                result = printPictureAsLines(matrix, alignment);
                printed = result;
            } catch (ShtrihResponseException sre) {
                log.error("printBarcodeInner: it seems that this model does not support the 0xC5 command!", sre);
                // больше не будем пытаться печатать через "печать линии"
                supportsPrintLineCmd = false;
            }

            // после печати ШК рекомендуется распечать пустую строку - чтоб следующая строка не "обрезалась" по высоте
            //  (у некоторых фискальников верхняя часть символов терялась)
            doAfterPrintLinesBullshit();
        }

        // 2.2. если не получилось - через "печать графики":
        if (!printed) {
            // ЛИБО команда "печать линии" не поддерживается, ЛИБО
            // это 2D ШК (видимо, QR-код) и "многомерные" картинки хотим печатать с высоким качеством
            //  - надо печатать через печать графики (0xC1) или расширенной графики (0xC3)
            log.trace("printBarcodeInner: trying to print bar-code [{}] through \"print graphics\" commnads", barcode);
            result = printPictureAsGraphics(matrix, alignment);
        }

        log.debug("leaving printBarcodeInner(BarCode, ShtrihAlignment). It took {} [ms]", System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Вернет порядок битов (точек) в байте при печати линии (команда 0xC5).
     * <p/>
     * Не надо пытаться понять смысл этого метода: проблема у самого Штриха: печему-то у разных моделей порядок битов/точек в байтах разный:
     * например, для печати последовательности из 5 черных точек, а потом 3 белых у М-ПТК такой байт должен выглядеть: <code>0b11111000</code>, а у М-ФР-К: <code>0b00011111</code>.
     * <p/>
     * NOTE: данный метод вызовется (вернет захардкоженное значение для конкретной модели) только если команда "Расширенный запрос" (0xF7) не поддерживается.
     *
     * @return порядок битов/точек в байте.
     *
     * @see #isGraphicBitsInInverseOrder()
     *
     */
    protected boolean isBitsInInverseOrderWhilePrintingLine() {
        return true;
    }

    /**
     * Вернет порядок битов (точек) в байте при печати линии (команда 0xC5).
     *
     * @return
     */
    private boolean isGraphicBitsInInverseOrder() {
        ShtrihModelParams params = getModelParams();

        return params == null ? isBitsInInverseOrderWhilePrintingLine() : params.isBitsInInverseOrderWhilePrintingLine();
    }

    /**
     * Печатает указанную картинку через печать строк (через последовательность команд "Печать линии").
     *
     * @param matrix
     *            картинка, что надо распечатать
     * @param alignment
     *            [горизонтальное] выравнивание этой картинки на чековой ленте
     * @return <code>true</code>, если удалось распечатать хоть часть этой картинки
     */
    private boolean printPictureAsLines(BitMatrix matrix, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        boolean result = false;

        // При печати картинки как последовательности команд "Печать линии" надо установить ширину межстрочного интервала == 0
        //  чтоб минимизировать "просветы" между линиями

        // 1. Установим межстрочный интервал в 0
        byte desirableSpacing = (byte) 0;

        // узнаем какой межстрочный интервал там сейчас установлен:
        byte lineSpacing = readAndGetLineSpacing();
        log.trace("line spacing is: {}", lineSpacing);

        try {
            // 2. установим межстрочный интервал в правильное значение, если надо:
            if (lineSpacing != desirableSpacing) {
                // вообще, частая необходимость смены межстрочного интервала может ускорить износ оборудования
                log.warn("printPictureAsLines(BitMatrix, ShtrihAlignment): changing line spacing from {} to {}", lineSpacing, desirableSpacing);
                setAndWriteLineSpacing(desirableSpacing);
            }

            // 3. распечатаем саму картинку
            //  3.1. ширина полотна (чековой ленты)
            int ribbonWidth = getRibbonWidthInPx();
            if (matrix.getWidth() > ribbonWidth) {
                log.error("the barcode will NOT FIT 'cause the matrixes width: {} is MORE THAN ribbon width ({})", matrix.getWidth(), ribbonWidth);
                return false;
            }

            // во сколько раз эту картинку надо отмасштабировать, чтоб получилась максимально большое/читабельное изображение:
            int scale = getScale(ribbonWidth, matrix.getWidth());
            log.trace("the picture should be scaled by {} times for optimal readability", scale);
            if (scale < 1) {
                // картинку нельзя (запрещено по настройкам?) хотя бы в масштабе 1:1 нарисовать - значит не будем
                log.warn("printPictureAsLines(BitMatrix, ShtrihAlignment): the picture scale factor is non-positive ({}) " +
                    "so the picture (barcode?) will NOT be printed!", scale);
                return false;
            }

            // вот эта картинка по-строково:
            List<byte[]> pictureLines = GraphicsUtils.getPictureLines(matrix, getAlignment(alignment), scale, ribbonWidth, isGraphicBitsInInverseOrder());

            //  3.2. А теперь эту картинку по-строково распечатаем:
            //  сколько раз будем печатать каждую строчку:
            int repeats = scale;
            if (pictureLines.size() == 1) {
                // а это одномерный ШК - для "высоты" таких ШК есть своя настройка:
                repeats = getBarcodeHeightInPx();
                if (repeats < scale) {
                    repeats = scale;
                }
            }
            for (byte[] line : pictureLines) {
                // печать линии:
                boolean printed = printLine(line, repeats);
                if (printed) {
                    result = true;
                }
            } // for line
        } finally {
            // 4. что бы ни случилось - вернем межстрочный интервал (или хотя бы попытаемся)
            if (lineSpacing != desirableSpacing) {
                // вообще, частая необходимость смены межстрочного интервала может ускорить износ оборудования
                log.warn("printPictureAsLines(BitMatrix, ShtrihAlignment): changing line spacing BACK from {} to {}", desirableSpacing, lineSpacing);
                setAndWriteLineSpacing(lineSpacing);
            }
        }

        return result;
    }

    /**
     * Вернет <code>true</code>, если команда "печать расширенной графики" (0xC3) поддерживается данной моделью ФР.
     *
     * @return <code>false</code>, если команда "печать расширенной графики" (0xC3) не поддерживается
     */
    protected boolean isPrintExtGraphicsSupported() {
        return true;
    }

    /**
     * Вернет номер первой печатаемой линии в графике
     *
     * @return 0 или 1
     */
    private int getFirstLineNo() {
        return getModelParams() == null ? 0 : (getModelParams().getFirstImageLineNo() - 1);
    }

    /**
     * Флаг-признак: данная модель поддерживает команду "Печать графики с масштабированием" (0x4F)
     */
    private boolean supportsPrintScaledGraphicsCmd = true;

    /**
     * Печатает указанную картинку через команду "Печать графики с масштабированием" (0x4F)
     *
     * @param matrix
     *            картинка, что надо распечатать
     * @param alignment
     *            [горизонтальное] выравнивание этой картинки на чековой ленте
     * @return {@code true}, если удалось выполнить печать; если вернет {@code false} - значит картинка не была распечатана
     */
    private boolean printAsScaledGraphics(BitMatrix matrix, ShtrihAlignment alignment) {
        boolean result = false;

        if (matrix == null) {
            log.error("printAsScaledGraphics: the \"matrix\" arg is NULL");
            return false;
        }
        if (matrix.getWidth() > getPictureWidth()) {
            // не сможем распечатать картинкой
            log.error("printAsScaledGraphics: the picture is too \"wide\" ({})", matrix.getWidth());
            return false;
        }
        if (matrix.getHeight() <= 1) {
            // у команды 0x4F какой-то глюк: не может распечатать и отмасштабировать только одну линию
            log.trace("printAsScaledGraphics: the picture is 1D only");
            return false;
        }
        if (!supportsPrintScaledGraphicsCmd) {
            log.trace("leaving printAsScaledGraphics(BitMatrix, ShtrihAlignment): the 0x4F command is NOT supported");
            return false;
        }

        // максимально возможная ширина картинки/графики, в пикселях:
        int ribbonWidthInPx = getPictureWidth();

        // во сколько раз эту картинку надо отмасштабировать, чтоб получилась максимально большое/читабельное изображение:
        int scale = getScale(ribbonWidthInPx, matrix.getWidth());
        log.trace("printAsScaledGraphics: the picture should be scaled by {} times for optimal readability", scale);

        if (scale < 1) {
            log.error("printAsScaledGraphics: unable to print matrix 'cause the resulting scale is non-positive");
            return false;
        }

        // вот эта картинка по-строково:
        List<byte[]> pictureLines = GraphicsUtils.getPictureLines(matrix, getAlignment(alignment), scale, ribbonWidthInPx, false);

        // загрузим эту картинку в буфер принтера и печатаем:
        int maxLinesAtOnce = getMaxLoadGraphicsLines();
        int firstLineNo = 1; // жестко 1 - иначе не рыботает/ правильно было бы: getFirstLineNo()
        log.trace("printAsScaledGraphics: the first line no is: {}", firstLineNo);
        int lineNo = firstLineNo;
        log.trace("printAsScaledGraphics: about to load {} lines of graphics scaled by {} vertically {} lines at a time", pictureLines.size(), scale, maxLinesAtOnce);

        try {
            for (byte[] line : pictureLines) {
                BaseCommand<Object> cmdLoad = null;
                try {
                    cmdLoad = new LoadGraphicsCommand(lineNo, line, getPassword());
                } catch (Throwable t) {
                    // все же не удалось загрузить картинку
                    log.error("printAsScaledGraphics: failed to load picture", t);
                    return false;
                }
                executeAndThrowExceptionIfError(cmdLoad);
                lineNo++;

                // пора печатать, что уже загрузили?
                if (lineNo == maxLinesAtOnce) {
                    log.trace("printAsScaledGraphics: printing chunk of {} lines of graphics", lineNo);
                    BaseCommand<Object> cmdPrint = new PrintScaledGraphicsCommand(1, lineNo - 1, 1, scale, getPassword());
                    executeAndThrowExceptionIfError(cmdPrint);

                    // и начнем следующую итерацию - заполняем буфер с нуля
                    lineNo = firstLineNo;
                }
            } // for line

            // остался какой кусок рисунка, что надо до-печатать?
            if (lineNo != 0) {
                log.trace("printAsScaledGraphics: printing the last chunk of {} lines of graphics", lineNo);
                BaseCommand<Object> cmdPrint = new PrintScaledGraphicsCommand(1, lineNo - 1, 1, scale, getPassword());
                executeAndThrowExceptionIfError(cmdPrint);
            }

            result = true;
        } catch (Throwable t) {
            // значит, нельзя этой командой печатать
            log.trace("it seems that the 0x4F command is NOT supported", t);
            supportsPrintScaledGraphicsCmd = false;
        }

        return result;
    }

    /**
     * Печататет указанную картинку как ... картинку (через загрузку и печать графики)
     *
     * @param matrix
     *            картинка, что надо распечатать
     * @param alignment
     *            [горизонтальное] выравнивание этой картинки на чековой ленте
     * @return <code>false</code>, если эту картинку не удалось распечатать по любой причине
     */
    protected boolean printPictureAsGraphics(BitMatrix matrix, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        // если это 2D ШК - попробуем распечатать графику с масштабированием по горизонтали:
        if (printAsScaledGraphics(matrix, alignment)) {
            log.trace("printPictureAsGraphics: the picture was printed as a scaled graphics");
            return true;
        }

        // максимально возможная ширина картинки/графики, в пикселях:
        int ribbonWidthInPx = getPictureWidth();

        // во сколько раз эту картинку надо отмасштабировать, чтоб получилась максимально большое/читабельное изображение:
        int scale = getScale(ribbonWidthInPx, matrix.getWidth());
        log.trace("the picture should be scaled by {} times for optimal readability", scale);

        if (scale < 1) {
            log.error("printPictureAsGraphics: unable to print matrix 'cause the resulting scale is non-positive");
            return false;
        }

        // вот эта картинка по-строково:
        List<byte[]> pictureLines = GraphicsUtils.getPictureLines(matrix, getAlignment(alignment), scale, ribbonWidthInPx, false);

        // сколько раз надо повторить каждую линию
        int repeats = scale;
        if (pictureLines.size() == 1) {
            // а это одномерный ШК - для "высоты" таких ШК есть своя настройка:
            repeats = getBarcodeHeightInPx();
            if (repeats < scale) {
                repeats = scale;
            }
        }

        final int firstLineNo = 0; // getFirstLineNo
        return printPictureLines(pictureLines, repeats, firstLineNo);
    }

    protected boolean printPictureLines(List<byte[]> pictureLines, int scaleY, final int firstLineNo) throws IOException, PortAdapterException, ShtrihException {
        // загрузим эту картинку в буфер принтера и печатаем:
        int maxLinesAtOnce = getMaxLoadGraphicsLines();
        int lineNo = firstLineNo;
        log.trace("printPictureAsGraphics: about to load {} lines of graphics {} lines at a time", pictureLines.size() * scaleY, maxLinesAtOnce);
        for (byte[] line : pictureLines) {
            for (int i = 0; i < scaleY; i++) {
                BaseCommand<Object> cmdLoad;
                try {
                    cmdLoad = new LoadGraphicsCommand(lineNo, line, getPassword());
                } catch (Throwable t) {
                    // все же не удалось загрузить картинку
                    log.error("failed to load picture", t);
                    return false;
                }
                executeAndThrowExceptionIfError(cmdLoad);
                lineNo++;

                // дошли до конца буфера, печатаем и заполняем с firstLine
                if (lineNo == maxLinesAtOnce) {
                    log.trace("printPictureAsGraphics: printing chunk of {} lines of graphics", lineNo);
                    BaseCommand<Object> cmdPrint = new PrintGraphicsCommand(firstLineNo + 1, lineNo, getPassword());
                    executeAndThrowExceptionIfError(cmdPrint);
                    lineNo = firstLineNo;
                }
            } // scaleY
        } // for line

        // печать загруженных линий
        if (lineNo != firstLineNo) {
            log.trace("printPictureAsGraphics: printing the last chunk of {} lines of graphics", lineNo);
            BaseCommand<Object> cmdPrint = new PrintGraphicsCommand(firstLineNo + 1, lineNo, getPassword());
            executeAndThrowExceptionIfError(cmdPrint);
        }

        return true;
    }

    /**
     * Вернет указанное выравнивание в виде, понятном {@link GraphicsUtils}.
     *
     * @param alignment выравнивание, чье представление надо вернуть
     * @return не <code>null</code> - в крайнем случае вернет {@link Alignment#CENTER}
     */
    protected Alignment getAlignment(ShtrihAlignment alignment) {
        Alignment result;

        if (ShtrihAlignment.LEFT.equals(alignment)) {
            result = Alignment.LEFT;
        } else if (ShtrihAlignment.RIGHT.equals(alignment)) {
            result = Alignment.RIGHT;
        } else {
            result = Alignment.CENTER;
        }

        return result;
    }

    /**
     * Вернет ширину картинок в пикселях (с точки зрения работы с графикой).
     *
     * @return 320 == 40 байтов по 8 бит-пикселей
     */
    protected int getPictureWidth() {
        return 40 * 8;
    }

    /**
     * вернет коэффициент масштабирования картинки при печати ШК.
     *
     * @param ribbonWidth
     *            ширина чековой ленты, в пикселях
     * @param pictureWidth
     *            ширина картинки, в пикселях
     * @return <code>0</code>, если картинка не помещается на указанной ширине ленты
     */
    protected int getScale(int ribbonWidth, int pictureWidth) {
        int result = 0;

        if (pictureWidth <= 0) {
            return 0;
        }
        int maxScale = getMaxScale();
        if (maxScale == 0) {
            return 0;
        }

        result = ribbonWidth / pictureWidth;
        if (result > maxScale && maxScale > 0) {
            result = maxScale;
        }

        return result;
    }

    /**
     * вернет высоту одномерных ШК в пикселях
     *
     * @return количество точек
     */
    protected int getBarcodeHeightInPx() {
        int result = 0;
        result = (int) Math.floor(1.0 * getBarcodeHeight() * DPI / 25.4);
        return result;
    }

    /**
     * Ширина чековой ленты в пикселях
     */
    private Integer ribbonWidthInPx = null;

    /**
     * вернет ширину чековой ленты в пикселях.
     *
     * @return количество точек
     */
    private int getRibbonWidthInPx()  throws IOException, PortAdapterException, ShtrihException {
        if (ribbonWidthInPx != null) {
            // уже знаем ширину чековой ленты
            log.trace("getRibbonWidthInPx(): result is: {} (IN-MEMORY)", ribbonWidthInPx);
            return ribbonWidthInPx;
        }
        // придется определять

        // 1. тупо считаем ширину ленты в точках из свойств 1го шрифта (шрифт с таким номером всегда у всех есть):
        ShtrihFontProperties fontProperties = getFontProperties((byte) 1);
        if (fontProperties != null && fontProperties.getPrintableAreaWidth() > 0) {
            ribbonWidthInPx = fontProperties.getPrintableAreaWidth();
            log.trace("getRibbonWidthInPx(): result is: {} (THROUGH FONT)", ribbonWidthInPx);
            return ribbonWidthInPx;
        }

        // 2. на крйняк возьмем 40*8 - но это уже ошибка!
        ribbonWidthInPx = getPictureWidth();
        log.error("getRibbonWidthInPx(): result is: {} (HARD-CODE)", ribbonWidthInPx);
        return ribbonWidthInPx;
    }

    /**
     * Установит значение межстрочного интервала при печати в указанное количество <em>точек</em> (1 точка ~ 0.353 мм).
     *
     * @param newSpacing
     *            устанавливаемое значение межстрочного интервала, в точках
     * @return <code>false</code>, если не удалось выполнить операцию (например, если состояние чека не допускает выполнение этой операции (чек
     *         открыт))
     */
    private boolean setAndWriteLineSpacing(byte newSpacing) throws IOException, PortAdapterException {
        if (!isLineSpacingSupported()) {
            log.trace("setAndWriteLineSpacing({}) is not supported", newSpacing);
            return true;
        }
        log.trace("entering setAndWriteLineSpacing(byte). The argument is: {}", newSpacing);
        try {
            writeTable(ShtrihTables.TYPE_AND_MODE_TABLE, ShtrihTables.TYPE_AND_MODE_TABLE_SOLE_ROW, ShtrihTables.LINE_SPACING_FIELD_NO, new byte[] {newSpacing});
        } catch (ShtrihException se) {
            // не удалось выполнить операцию
            log.error("setAndWriteLineSpacing(byte) failed!", se);
            return false;
        }
        log.trace("leaving setAndWriteLineSpacing(byte)");
        return true;
    }

    /**
     * Считает из ФР и вернет межстрочный интервал при печати на чековой ленте.
     *
     * @return межстрочный интервал в <em>точках</em> (1 точка ~ 0.353 мм)
     */
    private byte readAndGetLineSpacing() throws IOException, PortAdapterException, ShtrihException {
        if (!isLineSpacingSupported()) {
            log.trace("readAndGetLineSpacing() is not supported");
            return 0;
        }

        log.trace("entering readAndGetLineSpacing()");

        byte[] response = readTable(ShtrihTables.TYPE_AND_MODE_TABLE, ShtrihTables.TYPE_AND_MODE_TABLE_SOLE_ROW, ShtrihTables.LINE_SPACING_FIELD_NO);
        byte result = response[0];

        log.trace("leaving readAndGetLineSpacing(). The result is: {}", result);

        return result;
    }

    /**
     * Чтение настроек ФР.
     *
     * @param tableNo
     *            номер таблицы, из которой считываем.
     * @param rowNo
     *            номер ряда, в которой находится значение интересующего нас поля
     * @param fieldNo
     *            сам номер поля, значение которого хотим считать
     * @return массив байт, описывающий значение этой настройки
     */
    private byte[] readTable(byte tableNo, int rowNo, byte fieldNo) throws IOException, PortAdapterException, ShtrihException {
        byte[] result = null;

        if (log.isTraceEnabled()) {
            log.trace("entering readTable(byte, byte, byte). the arguments are: tableNo [{}], rowNo [{}], fieldNo [{}]",
                new Object[] {tableNo, rowNo, fieldNo});
        }

        ReadTableCommand cmd = new ReadTableCommand(tableNo, rowNo, fieldNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);

        if (log.isTraceEnabled()) {
            log.trace("leaving readTable(byte, byte, byte). The result is: {}", PortAdapterUtils.arrayToString(result));
        }

        return result;
    }

    /**
     * Запись настроек ФР.
     *
     * @param tableNo
     *            номер таблицы, в которую записываем
     * @param rowNo
     *            номер строки в этой таблице, куда записываем
     * @param fieldNo
     *            непоследственно само поле (в этой строке) значение которого устанавливаем в указанное ...значение
     * @param value
     *            само новое значение настройки - его и записываем; если <code>null</code> - получите NPE
     */
    protected void writeTable(byte tableNo, int rowNo, byte fieldNo, byte[] value) throws IOException, PortAdapterException, ShtrihException {
        if (log.isTraceEnabled()) {
            log.trace("entering writeTable(byte, byte, byte, byte[]). the arguments are: tableNo [{}], rowNo [{}], fieldNo [{}], value [{}]",
                new Object[] {tableNo, rowNo, fieldNo, PortAdapterUtils.arrayToString(value)});
        }

        WriteTableCommand cmd = new WriteTableCommand(tableNo, rowNo, fieldNo, value, getPassword());
        executeAndThrowExceptionIfError(cmd);

        log.trace("leaving writeTable(byte, byte, byte, byte[])");
    }

    private String logMatrix(BitMatrix matrix) {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
            sb.append("\n");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Валидирует указанный ШК.
     *
     * @param barcode
     *            ШК, что надо отвалидировать
     * @return <code>false</code>, если этот ШК невалиден; иначе - вернет <code>true</code>
     */
    private boolean validate(BarCode barcode) {
        if (barcode == null) {
            log.error("validate(BarCode): the argument is NULL");
            return false;
        }
        if (barcode.getType() == null) {
            log.error("validate(BarCode): the type of the argument is NULL");
            return false;
        }
        if (StringUtils.isEmpty(barcode.getValue())) {
            log.error("validate(BarCode): the value of the argument is EMPTY");
            return false;
        }

        return true;
    }

    /**
     * Вернет код ошибки из указанного ответа от ВУ (Внешнего Устройства).
     * <p/>
     * NOTE: Данный следует вызывать только после того, как аргумент прошел валидацию - иначе будут всякие Exception: NPE, IndexOutOfBounds.
     *
     * @param response
     *            ответ, из которого надо извлечь код ошибки
     * @return код ошибки
     */
    private byte getErrorCode(byte[] response) {
        // Нас интересует только 4й байт: после STX, байта количества, и кода команды следует байт кода ошибки:
        return response[2] == (byte ) 0xFF ? response[4] : response[3];
    }

    @Override
    public void open() throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering open()");

        ShtrihTransport transportToBe = new ShtrihTransport();
        transportToBe.setByteWaitTime(getByteWaitTime());
        transportToBe.setPortName(getPortName());
        transportToBe.setBaudRate(getBaudRate());

        transport = transportToBe;
        transport.open();

        // надо аннулировать документ, если он открыт
        annul();

        // проинициализируем наше устройство:
        init();

        log.debug("leaving open()");
    }

    /**
     * Вернет данные отчета ЭКЛЗ (строки или фрагменты отчета) для смены с указанным номером.
     *
     * @param shiftNumber
     *            номер смены, данные отчета по которой надо вернуть
     * @return <code>null</code>, если не удалось получить данные по указанной смене
     */
    private List<byte[]> getShiftDataByNumber(int shiftNumber) throws IOException, PortAdapterException, ShtrihException {
        List<byte[]> result = null;

        log.debug("entering getShiftDataByNumber(int). The argument is: shiftNumber [{}]", shiftNumber);

        // 1. надо сказать ФР, чтобы он "нашел" указанную смену в ЭКЛЗ
        log.trace("moving the cursor to the shift #{} data position", shiftNumber);
        GetShiftTotalByNumberCommand startReadingCmd = new GetShiftTotalByNumberCommand(shiftNumber, getPassword());
        executeAndThrowExceptionIfError(startReadingCmd);

        // 2. считать все данные по этой смене из ЭКЛЗ
        log.trace("starting to read data of shift #{}", shiftNumber);
        GetEklzDataCommand getDataCmd = new GetEklzDataCommand(getPassword());
        result = new LinkedList<>();
        while (true) {
            try {
                byte[] response = executeAndThrowExceptionIfError(getDataCmd);
                byte[] payload = getDataCmd.decodeResponse(response);
                result.add(payload);
            } catch (Exception e) {
                // норма. возможно, просто более нет данных - код ошибки: 0xA9
                log.trace("no shift data to read left", e);
                break;
            }
        } // до тех пор, пока не получим ошибку "нет больше данных"

        // 3. сказать ФР, что мы закончили чтение данных по этой смене
        StopEklzSessionCommand stopReadingCmd = new StopEklzSessionCommand(getPassword());
        executeAndThrowExceptionIfError(stopReadingCmd);

        log.debug("leaving getShiftDataByNumber(int)");

        return result;
    }


    @Override
    public void setCashNumber(byte cashNum) throws IOException, PortAdapterException, ShtrihException {
        writeTable(ShtrihTables.TYPE_AND_MODE_TABLE, ShtrihTables.TYPE_AND_MODE_TABLE_SOLE_ROW, ShtrihTables.CASH_NUM_FIELD_NO, new byte[] {cashNum});
    }

    @Override
    public ShtrihEklzStateOne getEklzStateOne() throws IOException, PortAdapterException, ShtrihException {
        ShtrihEklzStateOne result = null;

        log.debug("entering getEklzStateOne()");

        // Исполним запрос
        GetEklzStateOneCommand cmd = new GetEklzStateOneCommand(getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        log.debug("leaving getEklzStateOne(). The result is: {}", result);

        return result;
    }

    @Override
    public String getFNNumber() throws IOException, PortAdapterException, ShtrihException{
        return null;
    }

    @Override
    public ShtrihFNStateOne getFNState() throws IOException, PortAdapterException, ShtrihException {
        return null;
    }

    @Override
    public ShtrihFiscalizationResult getLastFiscalizationResult() throws IOException, PortAdapterException, ShtrihException {
        return null;
    }

    @Override
    public void setClientData(String clientData) throws IOException, PortAdapterException, ShtrihException {
        // для ФН устройств
    }

    @Override
    public void setDateTime(Date dateTime) throws IOException, PortAdapterException, ShtrihException {
        if (log.isDebugEnabled()) {
            log.debug("entering setDateTime(Date). The argument is: {}", dateTime == null ? "(NULL)" : String.format("%1$tF %1$tT", dateTime));
        }

        if (dateTime == null) {
            log.error("setDateTime(Date): the argument is NULL!");
            return;
        }

        // 1. запрограммировать дату
        try {
            executeAndThrowExceptionIfError(new SetDateCommand(dateTime, getPassword()));
        } catch (ShtrihResponseException sre) {
            // если включен контроль времени и пытаемся на слишком большое количество дней откорректировать время, то
            if (ShtrihConnector.CONFIRM_DATE_ERROR == sre.getErrorCode()) {
                // подтвердить программирование даты
                // executeAndThrowExceptionIfError(new ConfirmSetDateCommand(dateTime, getSysAdminPassword()));
            } else {
                // какая-то другая ошибка - пробросим выше
                throw sre;
            }
        }

        // 2. подтвердить программирование даты - ВСЕГДА
        executeAndThrowExceptionIfError(new ConfirmSetDateCommand(dateTime, getPassword()));

        // 3. запрограммировать время
        executeAndThrowExceptionIfError(new SetTimeCommand(dateTime, getPassword()));

        log.debug("leaving setDateTime(Date)");
    }

    /**
     * Вернет структуру указанной таблицы.
     *
     * @param tableNo
     *            номер таблицы, чью структуру нало вернуть
     * @return не <code>null</code> - в крайнем случае будет exception
     */
    protected TableStructure getTableStructure(byte tableNo) throws IOException, PortAdapterException, ShtrihException {
        TableStructure result = null;

        log.trace("entering getTableStructure(byte). The argument is: tableNo [{}]", tableNo);

        // Исполним запрос
        GetTableStructureCommand cmd = new GetTableStructureCommand(tableNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getTableStructure(byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет структуру указанного поля.
     *
     * @param tableNo
     *            номер таблицы, структуру поля которого надо вернуть
     * @param fieldNo
     *            номер поля, структуру  которого надо вернуть
     * @return не <code>null</code> - в крайнем случае будет exception
     */
    protected FieldStructure getFieldStructure(byte tableNo, byte fieldNo) throws IOException, PortAdapterException, ShtrihException {
        FieldStructure result = null;

        log.trace("entering getFieldStructure(byte, byte). The arguments are: tableNo [{}], fieldNo [{}]", tableNo, fieldNo);

        // Исполним запрос
        GetFieldStructureCommand cmd = new GetFieldStructureCommand(tableNo, fieldNo, getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        result = cmd.decodeResponse(response);
        if (result == null) {
            // ответ все-таки не валиден!
            throw new ShtrihResponseParseException(String.format("the response [%s] is invalid", PortAdapterUtils.arrayToString(response)));
        }

        log.trace("leaving getFieldStructure(byte, byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет номер таблицы, в которой хранятся налоговый ставки у данной модели ФР.
     *
     * @return номер таблицы
     */
    protected byte getTaxRatesTableNo() {
        return 6;
    }

    /**
     * Вернет номер поля (в {@link #getTaxRatesTableNo() таблице налоговых ставок}), в котором хранится величина налога (в сотых долях процента).
     *
     * @return номер поля
     */
    protected byte getTaxRateValueFieldNo() {
        return 1;
    }

    /**
     * Вернет номер поля (в {@link #getTaxRatesTableNo() таблице налоговых ставок}), в котором хранится название налога.
     *
     * @return номер поля
     */
    protected byte getTaxNameFieldNo() {
        return 2;
    }

    @Override
    public LinkedHashMap<String, Long> getTaxes() throws IOException, PortAdapterException, ShtrihException {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();

        log.debug("entering getTaxes()");

        // 1. для начала узнаем сколько вообще налоговых ставок поддерживается:
        TableStructure tableStructure = getTableStructure(getTaxRatesTableNo());
        int rowsCount = tableStructure.getRowsCount();
        log.trace("tax rates table has {} rows", rowsCount);

        // 2. А теперь для всех налоговых ставок вытянем ... ставку и название
        for (int rowNo = 1; rowNo < rowsCount + 1; rowNo++) {
            // 2.1. ставка налога
            byte[] value = readTable(getTaxRatesTableNo(), rowNo, getTaxRateValueFieldNo());

            // 2.2. название налога
            byte[] name = readTable(getTaxRatesTableNo(), rowNo, getTaxNameFieldNo());

            // и поместим в результат
            result.put(getString(name), ShtrihUtils.getLong(ShtrihUtils.inverse(value)));
        } // for rowNo

        log.debug("leaving getTaxes(). the result is: {}", result);

        return result;
    }


    @Override
    public void setTaxes(Map<String, Long> taxes) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering setTaxes(Map). The argument is: taxes [{}]", taxes);

        if (taxes == null) {
            taxes = new LinkedHashMap<>();
        }

        // 1. узнаем структуру "налоговой" таблицы и полей ставки и названия:
        TableStructure tableStructure = getTableStructure(getTaxRatesTableNo());
        int taxesCount = tableStructure.getRowsCount();
        if (taxesCount < 1) {
            // видимо, этот фискальник вообще не поддерживает налогов? ничего не будем делать
            log.warn("leaving setTaxes(Map): this fiscal registry supports [{}] taxes", taxesCount);
            return;
        }
        log.trace("{} taxes are supported by this fiscal registry", taxesCount);

        FieldStructure taxRateFieldStructure = getFieldStructure(getTaxRatesTableNo(), getTaxRateValueFieldNo());
        if (taxRateFieldStructure.getFieldWidth() < 1) {
            // видимо, этот фискальник вообще не поддерживает налогов? ничего не будем делать
            log.warn("leaving setTaxes(Map): this fiscal registry supports [{}]-bytes-width tax rates", taxRateFieldStructure.getFieldWidth());
            return;
        }

        FieldStructure taxNameStructure = getFieldStructure(getTaxRatesTableNo(), getTaxNameFieldNo());
        if (taxNameStructure.getFieldWidth() < 1) {
            // видимо, этот фискальник вообще не поддерживает налогов? ничего не будем делать
            log.warn("leaving setTaxes(Map): this fiscal registry supports [{}]-length-long tax names", taxNameStructure.getFieldWidth());
            return;
        }

        // 2. на основе аргумента сформируем ЧТО реально будем записывать в таблицы:
        byte[][] taxRates = new byte[taxesCount][taxRateFieldStructure.getFieldWidth()];
        byte[][] taxNames = new byte[taxesCount][];

        for (int i = 0; i < taxesCount; i++) {
            Arrays.fill(taxRates[i], (byte) 0);
        }

        if (taxes.size() > taxesCount) {
            log.warn("setTaxes(Map): the arguments cardinality ({}) is greater than the amount of taxes supported ({}) " +
                "so the latter taxes (exceeding that amount) will be ignored", taxes.size(), taxesCount);
        }
        if (taxes.size() < taxesCount) {
            // вообще, нормально. Может, просто больше налоговых ставок не поддерживаются
            log.trace("setTaxes(Map): the arguments cardinality ({}) is less than the amount of taxes supported ({}) " +
                "so some taxes will be nullified", taxes.size(), taxesCount);
        }

        int idx = 0;
        for (Map.Entry<String, Long> entry : taxes.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();

            // от старшего к младшему:
            byte[] keyAsArray = ShtrihUtils.getLongAsByteArray(value);
            // инверсия: теперь от младшего к старшему:
            keyAsArray = ShtrihUtils.inverse(keyAsArray);
            // скопируем в результирующий массив:
            System.arraycopy(keyAsArray, 0, taxRates[idx], 0, Math.min(keyAsArray.length, taxRates[idx].length));

            taxNames[idx] = getStringAsByteArray(key, taxNameStructure.getFieldWidth(), NULL_CHAR);

            idx++;
            if (idx >= taxesCount) {
                // ну все - остальное не будем анализировать
                break;
            }
        } // for entry

        // 3. а теперь запишем что получилось
        for (int rowNum = 1; rowNum < taxesCount + 1; rowNum++) {
            // 3.1. запишем ставку налога
            writeTable(getTaxRatesTableNo(), rowNum, getTaxRateValueFieldNo(), taxRates[rowNum - 1]);

            // 3.2. и название налога
            writeTable(getTaxRatesTableNo(), rowNum, getTaxNameFieldNo(), taxNames[rowNum - 1]);
        } // for rowNum

        log.debug("leaving setTaxes(Map)");
    }

    /**
     * Вернет номер таблицы, в которой хранятся пароли кассиров и администраторов в данной модели ФР.
     *
     * @return номер таблицы
     */
    protected byte getCashiersTableNo() {
        return 2;
    }

    /**
     * Вернет номер поля (в {@link #getCashiersTableNo() таблице паролей кассиров и администраторов}), в котором хранится пароль кассира.
     *
     * @return номер поля
     */
    protected byte getCashierPasswordFieldNo() {
        return 1;
    }

    /**
     * Вернет номер поля (в {@link #getCashiersTableNo() таблице паролей кассиров и администраторов}), в котором хранятся реквизиты кассира.
     *
     * @return номер поля
     */
    protected byte getCashierNameFieldNo() {
        return 2;
    }

    @Override
    public void setCashierName(byte cashierNo, String cashierName) throws IOException, PortAdapterException, ShtrihException {
        log.debug("entering setCashierName(byte, String). The arguments are: cashierNo [{}], cashierName [{}]", cashierNo, cashierName);

        // 1. Сначала узнаем допустимое количество кассиров:
        TableStructure tableStructure = getTableStructure(getCashiersTableNo());
        int cashiersCount = tableStructure.getRowsCount();
        if (cashiersCount < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности "регистрации" кассиров
            log.warn("leaving setCashierName(byte, String): this fiscal registry supports [{}] cashiers", cashiersCount);
            return;
        }
        if (cashierNo < 1 || cashierNo > cashiersCount) {
            // аргумент невалиден
            log.warn("leaving setCashierName(byte, String): the \"cashierNo\" argument (== {}) is INVALID: more than {} or less than 1", cashierNo, cashiersCount);
            return;
        }

        // 2. аргументы валидны. Узнаем "ширину" поля имени кассира:
        FieldStructure cashierNameFieldStructure = getFieldStructure(getCashiersTableNo(), getCashierNameFieldNo());
        if (cashierNameFieldStructure.getFieldWidth() < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности "регистрации" кассиров
            log.warn("leaving setCashierName(byte, String): this fiscal registry supports [{}]-bytes-width cashier names", cashierNameFieldStructure.getFieldWidth());
            return;
        }

        // 3. и запишем в фискальник:
        byte[] value = getStringAsByteArray(cashierName, cashierNameFieldStructure.getFieldWidth());
        writeTable(getCashiersTableNo(), cashierNo, getCashierNameFieldNo(), value);

        log.debug("leaving setCashierName(byte, String)");
    }

    @Override
    public void sendCashierInnIfNeeded(String inn) throws PortAdapterException, IOException, ShtrihException {
        if (getVersionFFD() == 2 && StringUtils.isNotBlank(inn)) {
            sendTLVData(TLVDataCommand.Tags.CASHIER_INN, StringUtils.leftPad(inn, 12, "0"));
        }
    }

    /**
     * Считаем таблицу 17 "Региональные настройки" Поле 17 "Формат ФД"
     *
     * @return версия ФФД:
     * 0 - Формат ФД 1.0 Beta;
     * 1 - Формат ФД 1.0 New;
     * 2 - Формат ФД 1.05.
     */
    @Override
    public int getVersionFFD() {
        try {
            return (int) ShtrihUtils.getLong(ShtrihUtils.inverse(readTable(ShtrihTables.REGIONAL_SETTINGS_TABLE, (byte) 1, ShtrihTables.REGIONAL_SETTINGS_TABLE_FORMAT_FD_ROW)));
        } catch (IOException | PortAdapterException | ShtrihException e) {
            log.error("Can't read table data for get ffd version! Set default \"0\" - 1.0 Beta", e);
            return 0;
        }
    }

    /**
     * Вернет пароль кассира с указанным номером.
     *
     * @param cashierNo
     *            номер кассира (нумерация с 1), пароль которого надо вернуть
     * @return 4х байтное число - пароль кассира; вернет <code>null</code>, если аргумент является недопустимым: кассира с таким номером не существует
     */
    private Integer getCashierPassword(byte cashierNo) throws IOException, PortAdapterException, ShtrihException {
        Integer result = null;

        log.trace("entering getCashierPassword(byte). The argument is: cashierNo [{}]", cashierNo);

        // 1. Сначала узнаем допустимое количество кассиров:
        TableStructure tableStructure = getTableStructure(getCashiersTableNo());
        int cashiersCount = tableStructure.getRowsCount();
        if (cashiersCount < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности "регистрации" кассиров
            log.warn("leaving getCashierPassword(byte): this fiscal registry supports [{}] cashiers", cashiersCount);
            return null;
        }
        if (cashierNo < 1 || cashierNo > cashiersCount) {
            // аргумент невалиден
            log.warn("leaving getCashierPassword(byte): the \"cashierNo\" argument (== {}) is INVALID: more than {} or less than 1", cashierNo, cashiersCount);
            return null;
        }

        // 2. считаем пароль
        byte[] read = readTable(getCashiersTableNo(), cashierNo, getCashierPasswordFieldNo());
        byte[] pass = {0x00, 0x00, 0x00, 0x00};
        if (read != null && read.length != 0) {
            System.arraycopy(read, 0, pass, 0, Math.min(pass.length, read.length));
        }

        // 3. и сформируем ответ в принятом виде
        pass = ShtrihUtils.inverse(pass);
        result = new BigInteger(pass).intValue();

        log.trace("leaving getCashierPassword(byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет номер таблицы, в которой хранятся наименования отделов
     *
     * @return номер таблицы
     */
    protected byte getDeptNamesTableNo() {
        return 7;
    }

    /**
     * Вернет номер поля (в {@link #getDeptNamesTableNo() таблице названий отделов}), в котором хранятся "Запрограммированные названия секций".
     *
     * @return номер поля
     */
    protected byte getDeptNameFieldNo() {
        return 1;
    }

    /**
     * Хранит количество отделов, что может быть зарегистрировано в ФР
     */
    private Integer deptCount = null;
    private int getDeptCount() throws IOException, PortAdapterException, ShtrihException {
        if (deptCount != null) {
            return deptCount;
        }

        TableStructure tableStructure = getTableStructure(getDeptNamesTableNo());
        deptCount = tableStructure.getRowsCount();

        return deptCount;
    }

    @Override
    public void setDepartmentName(byte deptNo, String deptName) throws IOException, PortAdapterException, ShtrihException {
        log.trace("entering setDepartmentName(byte, String). The arguments are: deptNo [{}], deptName [{}]", deptNo, deptName);

        // 1. сначала узнаем количество отделов, поддерживаемых данной моделью ФР:
        int deptsCount = getDeptCount();
        if (deptsCount < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности редактирования названий отделов
            log.warn("leaving setDepartmentName(byte, String): this fiscal registry supports [{}] departments", deptsCount);
            return;
        }
        if (deptNo < 1 || deptNo > deptsCount) {
            // аргумент невалиден
            log.warn("leaving setDepartmentName(byte, String): the \"deptNo\" argument (== {}) is INVALID: more than {} or less than 1", deptNo, deptsCount);
            return;
        }

        // 2. аргументы валидны. Узнаем "ширину" поля названия отдела:
        FieldStructure deptNameFieldStructure = getFieldStructure(getDeptNamesTableNo(), getDeptNameFieldNo());
        if (deptNameFieldStructure.getFieldWidth() < 1) {
            // видимо, этот фискальник вообще не поддерживает возможности редактирования названий отделов
            log.warn("leaving setDepartmentName(byte, String): this fiscal registry supports [{}]-bytes-width dept names", deptNameFieldStructure.getFieldWidth());
            return;
        }

        // 3. и запишем в фискальник:
        byte[] value = new byte[deptNameFieldStructure.getFieldWidth()];
        String name = deptName == null ? " " : deptName;
        byte[] nameAsArray = getStringAsByteArray(name, Math.min(name.length(), deptNameFieldStructure.getFieldWidth()));
        System.arraycopy(nameAsArray, 0, value, 0, nameAsArray.length);
        writeTable(getDeptNamesTableNo(), deptNo, getDeptNameFieldNo(), value);

        log.trace("leaving setDepartmentName(byte, String)");
    }

    /**
     * Инициализация нашего устройства: запись в него настроек.
     */
    protected void init() throws IOException, PortAdapterException {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering init()");

        // 1. Считаем настройки:
        //  потом можно и другие форматы рассмотреть - и тогда ParametersReader
        //      надо будет получать через factory по имени файла (по его расширению)
        ParametersReader reader = new CsvBasedParametersReader(parametersFilePath);
        Collection<ShtrihParameter> parameters = reader.readParameters();
        log.trace("{} parameters were read", parameters.size());

        // 2. и запишем эти настройки
        for (ShtrihParameter sp : parameters) {
            writeParameter(sp);
        } // for sp

        log.trace("leaving init(). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    /**
     * Запишет указанный параметр/настройку.
     *
     * @param sp настройка, что надо записать в устройство
     */
    private void writeParameter(ShtrihParameter sp) {
        try {
            // 1. преобразуем значение настройки в массив байт:
            byte[] value = getValue(sp);

            // 2. и запишем в таблицу:
            writeTable((byte) sp.getTableNo(), sp.getRowNo(), (byte) sp.getFieldNo(), value);
        } catch (Throwable t) {
            // возможно, редактирование этой настройки просто запрещено (не редактируемая настройка) - нет оснований вываливаться по exception'у
            log.error(String.format("writeParameter(ShtrihParameter): failed to write parameter: %s", sp), t);
        }
    }

    /**
     * Вернет {@link ShtrihParameter#getValue() значение} указанной настройки в виде массива байт.
     * <p/>
     * NOTE: вализацию делать ДО вызова этого метода.
     *
     * @param parameter
     *            параметр, значение которого надо вернуть
     * @return массив байт длиной {@link ShtrihParameter#getFieldWidth()} содержащий значение настройки
     */
    private byte[] getValue(ShtrihParameter parameter) {
        byte[] result = new byte[parameter.getFieldWidth()];

        if (ShtrihFieldType.STRING.equals(parameter.getFieldType())) {
            // это строковый параметр
            // 1. Заполним весь массив пустыми(непечатаемыми) символами:
            Arrays.fill(result, (byte) 0x00);

            // 2. преобразуем строковый параметр в массив байт:
            byte[] value = getBytes(parameter.getValue());

            // 3. и запишем в результат:
            System.arraycopy(value, 0, result, 0, Math.min(value.length, result.length));
        } else {
            // это числовой параметр
            // 1. Преобразуем его в число:
            BigInteger value = new BigInteger(parameter.getValue());

            // 2. получим его "байтовое" представление (старшие быйты - вперед):
            byte[] valueAsArray = value.toByteArray();

            // 3. а теперь "перельем" это представление в "наш" формат (мл. байты - вперед):
            if (result.length < valueAsArray.length) {
                log.error("The actual \"width\" of the value of the parameter [{}] is greater than allowed: {} vs {}",
                    new Object[] {parameter, valueAsArray.length, result.length});
            }
            // все равно вернем - даже если придется урезать
            for (int i = 0; i < Math.min(result.length, valueAsArray.length); i++) {
                result[i] = valueAsArray[valueAsArray.length - 1 - i];
            } // for i
        }

        return result;
    }

    @Override
    public void close() {
        log.debug("entering close()");

        transport.close();

        log.debug("leaving close()");
    }

    @Override
    public String toString() {
        return String.format("base-shtrih-connector [post: %s; rate: %s]", getPortName(), getBaudRate());
    }

    // getters & setters

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

    public String getParametersFilePath() {
        return parametersFilePath;
    }

    public void setParametersFilePath(String parametersFilePath) {
        this.parametersFilePath = parametersFilePath;
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

    public int getMaxLoadGraphicsLines() {
        return maxLoadGraphicsLines;
    }

    public void setMaxLoadGraphicsLines(int maxLoadGraphicsLines) {
        this.maxLoadGraphicsLines = maxLoadGraphicsLines;
    }

    public long getCheckStatusInterval() {
        return checkStatusInterval < 0 ? 0 : checkStatusInterval;
    }

    public void setCheckStatusInterval(long checkStatusInterval) {
        this.checkStatusInterval = checkStatusInterval;
    }

    public int getPassword() {
        return password;
    }

    public byte[] getPasswordAsArray() {
        byte[] result = new byte[4];

        result[0] = (byte) password;
        result[1] = (byte) (password >>> 8 & 0xFF);
        result[2] = (byte) (password >>> 16 & 0xFF);
        result[3] = (byte) (password >>> 24 & 0xFF);

        return result;
    }

    public void setPassword(int password) {
        this.password = password;
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

    private <T> void sendTLVData(TLVDataCommand.Tags tag, T clientData) throws PortAdapterException, ShtrihException, IOException {
        if (clientData != null) {
            executeAndThrowExceptionIfError(new TLVDataCommand<>(tag, clientData, getPassword()));
        }
    }

    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws PortAdapterException, ShtrihException, IOException {
        // начать форм. чека коррекции
        executeAndThrowExceptionIfError(new BeginCorrectionReceiptCommand(getPassword()));

        // добавить TLV параметрами недостающие данные
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_REASON, correctionReceipt.getReason());
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_DOC_NUMBER, correctionReceipt.getReasonDocNumber());
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_DATE, correctionReceipt.getReasonDocDate());
        sendCashierInnIfNeeded(Optional.ofNullable(cashier).map(Cashier::getInn).orElse(null));

        // сформировать чек коррекции v2
        CorrectionReceiptV2Command cmd = new CorrectionReceiptV2Command(new ShtrihCorrectionReceiptV2(correctionReceipt), getPassword());
        byte[] response = executeAndThrowExceptionIfError(cmd);
        Long numFD = cmd.decodeResponse(response);
        return Optional.ofNullable(numFD);
    }

    @Override
    public void cancelDocument() {
        try {
            execute(new CancelDocument(getPassword()));
        } catch (Exception e) {
            // если пытались отменить документ, то уже беда и будет обработка
            log.error("Can't cancel document!");
        }
    }

    @Override
    public int getLastShiftNumber() throws ShtrihException {
        log.trace("entering getLastShiftNumber()");
        throw new ShtrihException("Method getLastShiftNumber is unsupported");
    }

    @Override
    public ShiftNonNullableCounters getShiftNonNullableCounters() throws ShtrihException {
        log.trace("entering getShiftNonNullableCounters()");
        throw new ShtrihException("Method getShiftNonNullableCounters is unsupported");
    }

    @Override
    public void addCounterpartyData(Check check) {
    }
}
