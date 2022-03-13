package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.mark.MarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkValidationStatus;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.fiscalprinter.FiscalMarkValidationUtil;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdditionalInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AgentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.info.FnDocInfo;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.CsvBasedParametersReader;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ParametersReader;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihFieldType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihParameter;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.Print2DBarcodeCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.Shtrih2DBarcodeType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.TLVDataCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihInternalProcessingException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihResponseException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.FieldStructure;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.FiscalMemorySums;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihAlignment;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnector;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihCorrectionReceiptV2;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDeviceType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihEklzStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFNStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFiscalizationResult;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFontProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihItemCode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihMode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihModeEnum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihOperation;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalEx;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalV2Ex;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihRegNum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShiftCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShortStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihSubState;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihTables;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.TableStructure;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.Transport;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihDataTableProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.SystemInfo;
import ru.crystals.pos.fiscalprinter.utils.Alignment;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;
import ru.crystals.pos.utils.PortAdapterUtils;
import ru.crystals.utils.time.DateConverters;
import ru.crystals.utils.time.StopTimer;
import ru.crystals.utils.time.Timer;
import ru.shtrih_m.fr_drv_ng.classic_interface.classic_interface;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * "Базовая" реализация протокола обмена с ФР v. 1.12.
 * <p/>
 * Список сокращений, принятых в документе: <og>
 * <li>ФР - Фискальный Регистратор;
 * <li>ФП - Фискальная Плата;
 * <li>ВУ - Внешнее Устройство;
 * <li>МДЕ - Минимальная Денежная Единица, "копейка";
 * <li>LRC - Longitude Redundancy Check - контрольная сумма </og>
 * <p>
 * Документация
 * https://github.com/shtrih-m/fr_drv_ng/wiki/Methods
 * https://github.com/shtrih-m/fr_drv_ng/wiki/Properties
 */
public class ShtrihDriverConnector implements ShtrihConnector {

    protected static final Logger log = LoggerFactory.getLogger(ShtrihDriverConnector.class);
    /**
     * Тип данных тега - Byte
     */
    private static final int TAG_TYPE_BYTE = 0;
    /**
     * Тип данных тега - String
     */
    private static final int TAG_TYPE_STRING = 7;

    /**
     * Код ошибки драйвера, означающий потерю соединения драйвера с ККТ
     */
    private static final int DRV_IO_ERROR = -1;

    /**
     * Пароль сис.администратора по умолчанию
     */
    private static final int DEFAULT_SYS_ADM_PASSWORD = 30;

    /**
     * Пароль администратора по умолчанию
     */
    private static final int DEFAULT_ADM_PASSWORD = 30;

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
     * Высота символов по умолчанию, если не смогли считать.
     */
    private static int charHeightDefault = 25;

    /**
     * Файл netcfg с настройкой для RNDIS
     */
    private static final String ETH_CONFIG_FILE = "/opt/netcfg";

    /**
     * Директория с библиотеками дравера для WIN_X32
     */
    private static final String WIN_X32_DIR = File.separator + "win_x32";

    /**
     * Директория с библиотеками дравера для WIN_X64
     */
    private static final String WIN_X64_DIR = File.separator + "win_x64";

    /**
     * Ширина узкой ленты в коливестве символов
     */
    private static final int SLIM_TAPE = 36;

    /**
     * Разделитель в "сырой" марке
     */
    private static final String GS = "\u001D";

    /**
     * Периодичность проверки статуса ФР. Используется при ожидании наступления желаемого состояния ФР. Например, состояния возможности продолжения
     * печати.
     */
    private long checkStatusInterval = 100;
    /**
     * Таймаут ожидания смены статуса "Идет печать" на ""
     */
    private Duration timeOutWaitPrint = Duration.ofSeconds(1);

    /**
     * Пароль оператора. Всегда 4 байта.
     */
    private int password = DEFAULT_SYS_ADM_PASSWORD;

    /**
     * Реализация "транспортного" уровня общения с ФР.
     */
    protected Transport transport;

    /**
     * "шапка", что должна быть распечатана в заголовке следующего документа по завершению печати текущего
     */
    private List<FontLine> header;

    /**
     * Интерфейс нативного драйвера штриха
     */
    private classic_interface ci;

    /**
     * Флаг-признак означающий, что данная модель ФР поддерживает аппратную печать ШК (вернее команду печати 2D ШК - см. {@link Print2DBarcodeCommand})
     */
    private Boolean ableToPrint2DBarcodes;

    /**
     * Характеристики шрифтов, что поддерживаются данным ФР
     */
    private Map<Byte, ShtrihFontProperties> fonts;

    /**
     * Флаг-признак: данная модель ФР поддерживает команду "Печать графической линии" (0xC5)
     */
    private boolean supportsPrintLineCmd;

    /**
     * Флаг-признак: данная модель поддерживает команду "Печать графики с масштабированием" (0x4F)
     */
    private boolean supportsPrintScaledGraphicsCmd;

    /**
     * Хранит количество отделов, что может быть зарегистрировано в ФР
     */
    private Integer deptCount;

    /**
     * Текущий пользователь (кассир, админ, сис.админ) - номер по таблице в ФР
     */
    private int currentUserNo = DEFAULT_SYS_ADM_PASSWORD;


    private ShtrihDataTableProperties shtrihDataTableProperties;

    private boolean useEncoding = false;

    /**
     * Сюда должны уезжать все новые параметры конфига
     */
    private ShtrihConfiguration config;

    private int ffdVersion;

    /**
     * Использовать штрифты заданные в шаблонах чеков
     */
    private boolean useFontsFromTemplate = false;

    public ShtrihDriverConnector(ShtrihConfiguration config) {
        this.config = config;
        supportsPrintScaledGraphicsCmd = true;
        supportsPrintLineCmd = true;
        ableToPrint2DBarcodes = true;
    }

    public int getCurrentUserNo() {
        return currentUserNo;
    }

    public void setCurrentUserNo(int currentUserNo) {
        this.currentUserNo = currentUserNo;
    }

    public static int getDefaultAdmPassword() {
        return DEFAULT_ADM_PASSWORD;
    }

    /**
     * Вернет свойства шрифта с указанным номером.
     *
     * @param fontNo номер шрифта, настройки/сворйства которого надо вернуть
     * @return <code>null</code>, если шрифта с таким номером вообще нет
     */
    private ShtrihFontProperties getFontProperties(byte fontNo) throws ShtrihException {
        ShtrihFontProperties result;

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
        ci.Set_FontType(fontNo);
        ci.GetFontMetrics();
        throwExceptionIfError(ci);
        result = new ShtrihFontProperties();
        result.setFontNumber(fontNo);
        result.setFontsCount(ci.Get_FontCount());
        result.setSymbolHeight(ci.Get_CharHeight());
        result.setSymbolWidth(ci.Get_CharWidth());
        result.setPrintableAreaWidth(ci.Get_PrintWidth());
        fonts.put(fontNo, result);

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
    protected int getLinesBetweenThermoHeadAndKnife(int charHeight) throws ShtrihException {
        if (charHeight <= 0) {
            charHeight = charHeightDefault;
        } else {
            charHeightDefault = charHeight;
        }
        return getSpaceBetweenKnifeHead() / charHeight;
    }

    private int getSkippedLinesCount(int spaceSize, int charHeight) {
        if (charHeight <= 0) {
            charHeight = charHeightDefault;
        } else {
            charHeightDefault = charHeight;
        }
        return spaceSize / charHeight;
    }

    private int getSpaceBetweenKnifeHead() throws ShtrihException {
        String strVal = new String(readTable((byte) 10, 1, (byte) 1), ENCODING);
        return Integer.parseInt(strVal);
    }

    /**
     * Отрезает чековую ленту.
     */
    private void cut() throws ShtrihException {
        log.debug("entering cut()");
        ci.Set_CutType(config.isPartialCutReceipt());
        ci.Set_FeedAfterCut(false);
        ci.CutCheck();
        throwExceptionIfError(ci);
        log.debug("leaving cut()");
    }

    /**
     * Допечатывает документ: печатает картинки и рекламный тескт в подвале текущего документа, отрезает чековую ленту, печатает текст и картинки в
     * "шапке" следующего документа.
     *
     * @throws ShtrihException при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *                         какого типа ошибки могут возникнуть
     */
    private void printDocEnd() throws ShtrihException {
        log.debug("entering printDocEnd()");

        // 1. Распечатать 2 (жестко) пустые строки нормальным шрифтом
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        printLine(emptyLine);
        printLine(emptyLine);

        // 2. отрезать чековую ленту & распечатать заголовок следующего документа
        printHeaderAndCut();
        waitForPrinting();

        log.debug("leaving printDocEnd()");
    }

    /**
     * Допечатывает ОТЧЕТ: печатает картинки и рекламный тескт в подвале текущего ОТЧЕТА, отрезает чековую ленту, печатает текст и картинки в
     * "шапке" следующего ДОКУМЕНТА.
     *
     * @throws ShtrihException при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *                         какого типа ошибки могут возникнуть
     */
    protected void printReportEnd() throws ShtrihException {
        log.debug("entering printReportEnd()");

        // 1. Распечатать 2 (жестко) пустые строки нормальным шрифтом
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        printLine(emptyLine);
        printLine(emptyLine);

        // 2. отрезать чековую ленту & распечатать заголовок следующего документа
        printHeaderAndCut();
        waitForPrinting();

        log.debug("leaving printReportEnd()");
    }

    /**
     * Печатает "шапку" <b>следующего</b> документа и отрезает чековую ленту.
     */
    private void printHeaderAndCut() throws ShtrihException {
        log.debug("entering printHeaderAndCut()");

        List<FontLine> checkHeader = getHeader();
        byte headerFontSize = checkHeader.stream().findFirst().map(this::getFontSizeForLine).orElseGet(() -> getFontSize(Font.NORMAL));
        int headerCharHeight = getFontProperties(headerFontSize).getSymbolHeight();

        int linesBetweenThermoHeadAndKnife = getLinesBetweenThermoHeadAndKnife(headerCharHeight);
        int spaceBetweenKnifeHead = getSpaceBetweenKnifeHead();
        final Integer imageFirstLine = config.getImageFirstLine();
        final Integer imageLastLine = config.getImageLastLine();
        int logoSize = imageLastLine - imageFirstLine;

        // проверяем помещается ли в препринт заголовок вместе с логотипом
        if (logoSize <= 0 || logoSize + checkHeader.size() * charHeightDefault <= spaceBetweenKnifeHead) {
            // 1. печатаем лого
            printLogo();
            // 2. печатаем заголовок
            printHeader(checkHeader, linesBetweenThermoHeadAndKnife - getSkippedLinesCount(logoSize, headerCharHeight));
            // 3. отрежем чековую ленту
            cut();
        } else {
            int printDelta = imageLastLine / 10; // при отрезке чека происходит смещение, которое нужно учесть
            // 1. печатаем часть лого на препринт
            log.debug("spaceBetweenKnifeHead = {}", spaceBetweenKnifeHead);
            int currentLastLine = imageLastLine;
            // если картинка укладывается в расстояние до ножа, то печатаем всю
            if (imageFirstLine + spaceBetweenKnifeHead - printDelta < imageLastLine) {
                printLogoBySize(imageFirstLine, imageFirstLine + spaceBetweenKnifeHead - printDelta);
                currentLastLine = imageFirstLine + spaceBetweenKnifeHead - printDelta;
            } else {
                printLogoBySize(imageFirstLine, currentLastLine);
            }
            // 2. отрежем чековую ленту
            cut();
            // 3. печатаем оставшуюся часть лого
            printLogoBySize(currentLastLine, imageLastLine);
            // 3. печатаем заголовок
            printHeader(checkHeader, linesBetweenThermoHeadAndKnife - getSkippedLinesCount(logoSize, headerCharHeight));
        }

        // проркучиваем ленту если надо, чтобы не отрезать на лого или тексте
        for (int i = linesBetweenThermoHeadAndKnife; i < checkHeader.size(); i++) {
            FontLine line = checkHeader.get(i);
            printLine(line);
        }

        log.debug("leaving printHeaderAndCut()");
    }

    private void printHeader(List<FontLine> checkHeader, int linesBetweenThermoHeadAndKnife) throws ShtrihException {
        log.debug("header: {} ", checkHeader);
        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        // 1. распечатаем часть заголовка - так чтобы весь ТЕКУЩИЙ документ оказался ВЫШЕ ножа

        int emptyLines = linesBetweenThermoHeadAndKnife - checkHeader.size();

        for (int j = 0; j < emptyLines; j++) {
            printLine(emptyLine);
        }

        for (int i = 0; i < linesBetweenThermoHeadAndKnife; i++) {
            FontLine line;
            if (i < checkHeader.size()) {
                line = checkHeader.get(i);
                printLine(line);
            }
        }

        if (linesBetweenThermoHeadAndKnife <= 0) {
            for (FontLine line : getHeader()) {
                printLine(line);
            }
        }
    }

    /**
     * Вернет содержимое <b>ДЕНЕЖНОГО</b> регистра с указанным номером.
     *
     * @param registerNo номер денежного регистра (0..255), содержимое которого надо вернуть
     * @return значение регистра
     */
    private long getCashRegister(int registerNo) throws ShtrihException {
        log.info("entering getCashRegister({})", registerNo);
        ci.Set_RegisterNumber(registerNo);
        ci.GetCashReg();
        throwExceptionIfError(ci);
        long result = ci.Get_ContentsOfCashRegister();

        log.info("leaving getCashRegister({}). The result is: {}", registerNo, result);

        return result;
    }

    /**
     * Вернет номер регистра в списке <i>ДЕНЕЖНЫХ</i> регистров, что хранит накопление наличности в кассе [за смену], в "копейках" (в МДЕ -
     * Минимальных Денежных Единицах)
     *
     * @return номер регистра
     */
    protected int getCashAccumulationRegistry() {
        // 241й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return 241;
    }

    @Override
    public long getCashAccumulation() throws ShtrihException {
        log.info("entering getCashAccumulation()");

        long result = getCashRegister(getCashAccumulationRegistry());

        log.info("leaving getCashAccumulation(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму продаж за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: продажа
     */
    protected long getSalesSum() throws ShtrihException {
        long result = 0;

        log.info("entering getSalesSum()");

        // суммы продаж находятся в ДЕНЕЖНЫХ регистрах: 121 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы продаж по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            int regNo = (121 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }
        log.info("leaving getSalesSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму ВОЗВРАТОВ за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: "возврат продажи"
     */
    protected long getReturnsSum() throws ShtrihException {
        long result = 0;

        log.info("entering getReturnsSum()");

        // суммы возвратов находятся в ДЕНЕЖНЫХ регистрах: 123 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы возвратов по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            int regNo = (123 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }

        log.info("leaving getReturnsSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму РАСХОДА за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: "расход"
     */
    protected long getExpenseSum() throws ShtrihException {
        long result = 0;

        log.info("entering getExpenseSum()");

        // суммы расходов находятся в ДЕНЕЖНЫХ регистрах: 122 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы расходов по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            int regNo = (122 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }

        log.info("leaving getExpenseSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет сумму ВОЗВРАТОВ РАСХОДА за смену, в МДЕ.
     *
     * @return сумма накоплений за смену по торговой операции: "возврат расхода"
     */
    protected long getReturnExpenseSum() throws ShtrihException {
        long result = 0;

        log.info("entering getReturnExpenseSum()");

        // суммы возвратов расхода находятся в ДЕНЕЖНЫХ регистрах: 124 + 4 * (X-1) (где X - это номер отдела: 1..16)
        //  т.е., надо просто просуммировать ... суммы возвратов расхода по 16ти отделам за смену
        //  NOTE: метод protected - на случай. если у какой модели эти суммы будут храниться в других регистрах
        for (int deptNo = 1; deptNo < 17; deptNo++) {
            int regNo = (124 + 4 * (deptNo - 1));
            result += getCashRegister(regNo);
        }

        log.info("leaving getReturnExpenseSum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков продаж в смене.
     *
     * @return количество торговых операций "Продажа" за смену
     */
    protected long getSalesCount() throws ShtrihException {
        log.info("entering getSalesCount()");

        // в 144м операционном регистре
        long result = getOperationRegistry(144);

        log.info("leaving getSalesCount(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков возврата в смене.
     *
     * @return количество торговых операций "Возврат продажи" за смену
     */
    protected long getReturnsCount() throws ShtrihException {
        log.info("entering getReturnsCount()");

        // в 146м операционном регистре
        long result = getOperationRegistry(146);

        log.info("leaving getReturnsCount(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков расхода в смене.
     *
     * @return количество торговых операций "Расход" за смену
     */
    private long getExpenseCount() throws ShtrihException {
        log.info("entering getExpenseCount()");

        // в 145м операционном регистре
        long result = getOperationRegistry(145);

        log.info("leaving getExpenseCount(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет количество чеков возврата расхода в смене.
     *
     * @return количество торговых операций "Возврат расхода" за смену
     */
    private long getReturnExpenseCount() throws ShtrihException {
        log.info("entering getReturnExpenseCount()");

        // в 147м операционном регистре
        long result = getOperationRegistry(147);

        log.info("leaving getReturnExpenseCount(). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihShiftCounters getShiftCounters() throws ShtrihException {
        ShtrihShiftCounters result = new ShtrihShiftCounters();

        log.debug("entering getShiftCounters()");

        // накопление наличности в кассе
        result.setCashSum(getCashAccumulation());

        // сумма продаж
        result.setSumSale(getSalesSum());

        // сумма возвратов
        result.setSumReturn(getReturnsSum());

        // сумма расхода
        result.setSumExpense(getExpenseSum());

        // сумма возвратов расхода
        result.setSumReturnExpense(getReturnExpenseSum());

        // количество продаж
        result.setCountSale(getSalesCount());

        // количество возвратов
        result.setCountReturn(getReturnsCount());

        // количество расходов
        result.setCountExpense(getExpenseCount());

        // количество возвратов расхода
        result.setCountReturnExpense(getReturnExpenseCount());

        log.debug("leaving getShiftCounters(). The result is: {}", result);

        return result;
    }


    @Override
    public ShtrihShiftCounters getShiftCounters(int shiftNo) throws ShtrihException {
        throw new ShtrihException("Metgod getShiftCounters is unsupported");
    }

    /**
     * Вернет значение <i>ОПЕРАЦИОННОГО</i> регистра с указанным номером.
     *
     * @param registryNo номер регистра. значение которого надо вернуть
     * @return 2 байта: 00..65535
     */
    private long getOperationRegistry(int registryNo) throws ShtrihException {
        log.info("entering getOperationRegistry()");

        ci.Set_RegisterNumber(registryNo);
        ci.GetOperationReg();
        throwExceptionIfError(ci);
        long result = ci.Get_ContentsOfOperationRegister();

        log.info("leaving getOperationRegistry(). The result is: {}", result);

        return result;
    }


    /**
     * Вернет информацию о последнем документе из ФН по номеру ФД
     *
     * @param docNumber номер документа
     * @return данные последнего закрытого документа в ФР
     * @throws ShtrihException
     */
    public FiscalDocumentData getLastDocInfo(long docNumber) throws ShtrihException {
        log.info("entering getLastDocInfo()");

        ci.Set_RegisterNumber((int) docNumber);
        ci.FNFindDocument();
        throwExceptionIfError(ci);

        /*
        Get_DocumentType:
            1 – Отчёт о регистрации
            2 – Отчёт об открытии смены
            3 – Кассовый чек
            4 – БСО
            5 – Отчёт о закрытии смены
            6 – Отчёт о закрытии фискального накопителя
            7 – Подтверждение оператора
            11 – Отчет об изменении параметров регистрации
            21 – Отчет о состоянии расчетов
            31 – Кассовый чек коррекции
            41 – Бланк строгой отчетности коррекции
        */
        final int docType = ci.Get_DocumentType();

        FiscalDocumentData result = new FiscalDocumentData();
        if (docType == 3) {
            result.setSum(ci.Get_Summ1());
            result.setType(ci.Get_OperationType() == 1 ? FiscalDocumentType.SALE : FiscalDocumentType.REFUND);
        } else {
            result.setType(FiscalDocumentType.UNKNOWN);
        }
        // Номер ФД возвращается в ci.Get_DocumentNumber(), но мы его также передавали в запросе, чтобы получить всю эту инфу
        result.setNumFD(docNumber);
        result.setFiscalSign(ci.Get_FiscalSign());
        result.setOperationDate(ci.Get_Date());

        log.info("leaving getLastDocInfo(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество внесений за смену.
     *
     * @return номер регистра
     */
    protected int getCashInCountRegistry() {
        // 153й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return 153;
    }

    @Override
    public long getCashInCount() throws ShtrihException {
        return getOperationRegistry(getCashInCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество изъятий за смену (количество выплат денежных сумм за
     * смену).
     *
     * @return номер регистра
     */
    protected int getCashOutCountRegistry() {
        // 154й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return 154;
    }

    @Override
    public long getCashOutCount() throws ShtrihException {
        return getOperationRegistry(getCashOutCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит количество отмененных документов.
     *
     * @return номер регистра
     */
    protected int getAnnulCountRegistry() {
        // 157й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return 157;
    }

    @Override
    public long getAnnulCount() throws ShtrihException {
        return getOperationRegistry(getAnnulCountRegistry());
    }

    /**
     * Вернет номер регистра в списке <i>ОПЕРАЦИОННЫХ</i> регистров, что хранит СПНД (сквозной порядковый номер документа).
     *
     * @return номер регистра
     */
    protected int getSpndRegistry() {
        // 152й регистр. Наверно, у всех моделей этот номер такой. Но на всякий этот метод protected
        return 152;
    }

    @Override
    public long getSpnd() throws ShtrihException {
        return getOperationRegistry(getSpndRegistry());
    }

    /**
     * Номер <i>ОПЕРАЦИОННОГО</i> регистра, хранящего номер последнего Z отчета.
     *
     * @return номер регистра
     */
    private int getZReportCountRegistry() {
        // 159й регистр
        return 159;
    }

    @Override
    public long getZReportCount() throws ShtrihException {
        return getOperationRegistry(getZReportCountRegistry());
    }

    @Override
    public void beep() throws ShtrihException {
        log.info("entering beep()");
        ci.Beep();
        throwExceptionIfError(ci);
        log.info("leaving beep()");
    }

    @Override
    public void annul() throws ShtrihException {
        log.info("entering annul()");

        // 1. сначала убедимся, что не идет печать какого-либо документа и что есть _открытый_ документ, что надо аннулировать
        waitForPrinting();
        ci.GetECRStatus();
        throwExceptionIfError(ci);

        if (ShtrihModeEnum.WAITING_FOR_DATE_CONFIRMATION.getCode() == ci.Get_ECRMode()) {
            Date date = ci.Get_Date();
            ci.Set_Date(date);
            ci.ConfirmDate();
            throwExceptionIfError(ci);
        }
        ci.GetECRStatus();
        throwExceptionIfError(ci);

        if (ShtrihModeEnum.DOCUMENT_IS_OPEN.getCode() == ci.Get_ECRMode()) {

            // 2. аннулируем открытый документ
            ci.CancelCheck();
            throwExceptionIfError(ci);

            // 3. и распечатаем заголовок следующего документа
            printDocEnd();
        } else {
            // а вот нету открытого документа - нечего аннулировать
            log.info("annul(): no one document is opened");
        }

        log.info("leaving annul()");
    }

    @Override
    public void regSale(ShtrihPosition position) throws ShtrihException {
        log.info("entering regSale(ShtrihPosition). The argument is: position [{}]", position);

        if (position == null) {
            log.error("leaving regSale(ShtrihPosition): the argument is NULL!");
            throw new ShtrihException("Method regSale - position is null");
        }

        // отредактируем, если надо, номер отдела:
        if (position.getDeptNo() < 1 || position.getDeptNo() > getDeptCount()) {
            log.warn("regSale(ShtrihPosition): the \"deptNo\" of the argument was invalid ({})", position.getDeptNo());
            position.setDeptNo((byte) 1);
        }

        // исполним команду:
        ci.Set_Quantity(position.getQuantity());
        ci.Set_Price(position.getPrice());
        ci.Set_Department(position.getDeptNo());
        ci.Set_Tax1(position.getTaxOne());
        ci.Set_Tax2(position.getTaxTwo());
        ci.Set_Tax3(position.getTaxThree());
        ci.Set_Tax4(position.getTaxFour());
        ci.Set_StringForPrinting(position.getName());
        ci.Sale();
        throwExceptionIfError(ci);

        log.info("leaving regSale(ShtrihPosition)");
    }

    @Override
    public void regOperation(ShtrihOperation operation) throws ShtrihException {
        log.info("entering regOperation(ShtrihOperation). The argument is: position [{}]", operation);

        if (operation == null) {
            log.error("leaving regOperation(ShtrihOperation): the argument is NULL!");
            throw new ShtrihException("Method regOperation - operation is null");
        }

        // отредактируем, если надо, номер отдела:
        if (operation.getDepartment() < 1 || operation.getDepartment() > getDeptCount()) {
            log.warn("regOperation(ShtrihOperation): the \"deptNo\" of the argument was invalid ({})", operation.getDepartment());
            operation.setDepartment((byte) 1);
        }

        if (ShtrihModeEnum.SHIFT_IS_CLOSED.getCode() == ci.Get_ECRMode()) {
            openShift(null);
        }
        // исполним команду:
        throwExceptionIfError(ci);
        ci.Set_Password(getCurrentUserNo());
        ci.Set_CheckType(operation.getCheckType());
        double q = operation.getQuantity() / 1000d;
        ci.Set_Quantity(q);
        ci.Set_Price(operation.getPrice());
        ci.Set_Summ1Enabled(false);
        if (operation.getSumm() == null) {
            long price = new Double(operation.getPrice() * q).longValue();
            ci.Set_Summ1(price);
        } else {
            ci.Set_Summ1(operation.getSumm());
        }
        if (operation.getTaxValue() != null) {
            ci.Set_TaxValue(operation.getTaxValue());
            ci.Set_TaxValueEnabled(true);
        } else {
            ci.Set_TaxValueEnabled(false);
        }
        ci.Set_Tax1(operation.getTaxOne());
        ci.Set_Department(operation.getDepartment());
        ci.Set_PaymentTypeSign(operation.getPaymentTypeSing());
        ci.Set_PaymentItemSign(operation.getPaymentItemSing());
        ci.Set_StringForPrinting(operation.getStringForPrinting());
        ci.FNOperation();
        throwExceptionIfError(ci);
        ci.Set_Password(getSysAdminPassword());

        putAdditionalPositionInfo(operation.getGoods().getAdditionalInfo());

        if (operation.isAddItemCode()) {
            if (getVersionFFD() == FFD_1_2) {
                registerMarkCode(operation.getGoods());
            } else {
                ci.Set_Password(getCurrentUserNo());
                ci.FNSendItemCodeData();
                throwExceptionIfError(ci);
                ci.Set_Password(getSysAdminPassword());
            }
        }

        log.info("leaving (ShtrihOperation)");
    }

    private void registerMarkCode(Goods position) throws ShtrihException {
        MarkValidationResult validationResult = position.getMarkValidationResult();
        if (validationResult == null) {
            throw new IllegalStateException("Unexpected missing mark code check result for FFD 1.2 device");
        }
        if (validationResult.getStatus() == MarkValidationStatus.NOT_SUPPORTED_KKT) {
            log.warn("Not supported validation status on register position");
            return;
        }
        final FiscalMarkValidationResult fiscalResult = validationResult.getFiscalResult();
        if (fiscalResult == null) {
            throw new IllegalStateException("Unexpected missing mark code check result for FFD 1.2 device");
        }
        ci.Set_BarCode((String) fiscalResult.getInput().get(FiscalMarkValidationUtil.MARK_KEY));
        ci.FNSendItemBarcode();
        throwExceptionIfError(ci);
    }

    @Override
    public void addCounterpartyData(Check check) throws ShtrihException {
        if (getVersionFFD() != FFD_1_2
                || areAllNull(check.getClientName(), check.getClientINN(), check.getClientAddress())) {
            return;
        }
        openParentTag(1256);
        addInnerTag(1227, check.getClientName());
        addInnerTag(1228, check.getClientINN());
        addInnerTag(1254, check.getClientAddress());
        closeParentTag();
    }

    @Override
    public void regReturn(ShtrihPosition position) throws ShtrihException {
        log.info("entering regReturn(ShtrihPosition). The argument is: position [{}]", position);

        if (position == null) {
            log.error("leaving regReturn(ShtrihPosition): the argument is NULL!");
            throw new ShtrihException("Method regReturn - position is null");
        }

        // отредактируем, если надо, номер отдела:
        if (position.getDeptNo() < 1 || position.getDeptNo() > getDeptCount()) {
            log.warn("regReturn(ShtrihPosition): the \"deptNo\" of the argument was invalid ({})", position.getDeptNo());
            position.setDeptNo((byte) 1);
        }

        // исполним команду:
        ci.Set_Quantity(position.getQuantity());
        ci.Set_Price(position.getPrice());
        ci.Set_Department(position.getDeptNo());
        ci.Set_Tax1(position.getTaxOne());
        ci.Set_Tax2(position.getTaxTwo());
        ci.Set_Tax3(position.getTaxThree());
        ci.Set_Tax4(position.getTaxFour());
        ci.Set_StringForPrinting(position.getName());
        ci.ReturnSale();
        throwExceptionIfError(ci);

        log.info("leaving regReturn(ShtrihPosition)");
    }

    @Override
    public void regDiscount(ShtrihDiscount discount) throws ShtrihException {
        log.info("entering regDiscount(ShtrihDiscount). The argument is: discount [{}]", discount);

        if (discount == null) {
            log.error("leaving regDiscount(ShtrihDiscount): The argument is NULL");
            throw new ShtrihException("Method regDiscount - discount is null");
        }

        ci.Set_Summ1(discount.getSum());
        ci.Set_Tax1(discount.getTaxOne());
        ci.Set_Tax2(discount.getTaxTwo());
        ci.Set_Tax3(discount.getTaxThree());
        ci.Set_Tax4(discount.getTaxFour());
        ci.Set_StringForPrinting(discount.getText());
        ci.Discount();
        throwExceptionIfError(ci);

        log.info("leaving regDiscount(ShtrihDiscount)");
    }

    public void regMargin(ShtrihDiscount discount) throws ShtrihException {
        log.info("entering regMargin(ShtrihDiscount). The argument is: discount [{}]", discount);

        if (discount == null) {
            log.error("leaving regMargin(ShtrihDiscount): The argument is NULL");
            throw new ShtrihException("Method regMargin - discount is null");
        }

        ci.Set_Summ1(discount.getSum());
        ci.Set_Tax1(discount.getTaxOne());
        ci.Set_Tax2(discount.getTaxTwo());
        ci.Set_Tax3(discount.getTaxThree());
        ci.Set_Tax4(discount.getTaxFour());
        ci.Set_StringForPrinting(discount.getText());
        ci.Charge();
        throwExceptionIfError(ci);

        log.info("leaving regMargin(ShtrihDiscount)");
    }

    @Override
    public void sendItemCode(ShtrihItemCode itemCode) {
        log.info("entering sendItemCode(). The argument is: itemCode [{}]", itemCode);

        if (itemCode == null) {
            log.error("leaving sendItemCode(): The argument is NULL");
            return;
        }

        ci.Set_MarkingType(itemCode.getIntMarking());
        ci.Set_GTIN(itemCode.getStrGtin());
        ci.Set_SerialNumber(itemCode.getStrSerialData());

        log.info("leaving sendItemCode()");
    }

    @Override
    public void closeReceipt(ShtrihReceiptTotal receiptTotal) throws ShtrihException {
        log.info("entering closeReceipt(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotal);

        if (receiptTotal == null) {
            log.error("leaving closeReceipt(ShtrihReceiptTotal): the argument is NULL!");
            throw new ShtrihException("Method closeReceipt - receiptTotal is null");
        }
        ci.Set_Password(getCurrentUserNo());
        ci.Set_Summ1(receiptTotal.getCashSum());
        ci.Set_Summ2(receiptTotal.getSecondPaymentTypeSum());
        ci.Set_Summ3(receiptTotal.getThirdPaymentTypeSum());
        ci.Set_Summ4(receiptTotal.getFourthPaymentTypeSum());
        ci.Set_DiscountOnCheck(receiptTotal.getDiscountPercent());
        ci.Set_Tax1(receiptTotal.getTaxOne());
        ci.Set_Tax2(receiptTotal.getTaxTwo());
        ci.Set_Tax3(receiptTotal.getTaxThree());
        ci.Set_Tax4(receiptTotal.getTaxFour());
        ci.Set_StringForPrinting(receiptTotal.getText());
        ci.EndDocument();
        throwExceptionIfError(ci);
        ci.Set_Password(getSysAdminPassword());

        log.info("leaving closeReceipt(ShtrihReceiptTotal)");
    }

    @Override
    public void closeReceiptEx(ShtrihReceiptTotalEx receiptTotalEx) throws ShtrihException {
        log.info("entering closeReceiptEx(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotalEx);

        if (receiptTotalEx == null) {
            log.error("leaving closeReceiptEx(ShtrihReceiptTotal): the argument is NULL!");
            throw new ShtrihException("Method closeReceiptEx - receiptTotalEx is null");
        }
        ci.Set_Password(getCurrentUserNo());
        ci.Set_Summ1(receiptTotalEx.getSumm1());
        ci.Set_Summ2(receiptTotalEx.getSumm2());
        ci.Set_Summ3(receiptTotalEx.getSumm3());
        ci.Set_Summ4(receiptTotalEx.getSumm4());
        ci.Set_Summ5(receiptTotalEx.getSumm5());
        ci.Set_Summ6(receiptTotalEx.getSumm6());
        ci.Set_Summ7(receiptTotalEx.getSumm7());
        ci.Set_Summ8(receiptTotalEx.getSumm8());
        ci.Set_Summ9(receiptTotalEx.getSumm9());
        ci.Set_Summ10(receiptTotalEx.getSumm10());
        ci.Set_Summ11(receiptTotalEx.getSumm11());
        ci.Set_Summ12(receiptTotalEx.getSumm12());
        ci.Set_Summ13(receiptTotalEx.getSumm13());
        ci.Set_Summ14(receiptTotalEx.getSumm14());
        ci.Set_Summ15(receiptTotalEx.getSumm15());
        ci.Set_Summ16(receiptTotalEx.getSumm16());
        ci.Set_DiscountOnCheck(receiptTotalEx.getDiscountPercent());
        ci.Set_Tax1(receiptTotalEx.getTaxOne());
        ci.Set_Tax2(receiptTotalEx.getTaxTwo());
        ci.Set_Tax3(receiptTotalEx.getTaxThree());
        ci.Set_Tax4(receiptTotalEx.getTaxFour());
        ci.Set_StringForPrinting(receiptTotalEx.getText());

        ci.FNCloseCheckEx();
        throwExceptionIfError(ci);
        ci.WaitForPrinting();
        ci.Set_Password(getSysAdminPassword());
        waitPrintWithCheckState();
        throwExceptionIfError(ci);

        log.info("leaving closeReceiptEx(ShtrihReceiptTotal)");
    }

    @Override
    public void closeReceiptV2Ex(ShtrihReceiptTotalV2Ex receiptTotalV2Ex) throws ShtrihException {
        log.info("entering closeReceiptV2Ex(ShtrihReceiptTotal). The argument is: receiptTotal [{}]", receiptTotalV2Ex);

        if (receiptTotalV2Ex == null) {
            log.error("leaving closeReceiptV2Ex(ShtrihReceiptTotal): the argument is NULL!");
            throw new ShtrihException("Method closeReceiptV2Ex - receiptTotalV2Ex is null");
        }
        ci.Set_Password(getCurrentUserNo());
        ci.Set_Summ1(receiptTotalV2Ex.getSumm1());
        ci.Set_Summ2(receiptTotalV2Ex.getSumm2());
        ci.Set_Summ3(receiptTotalV2Ex.getSumm3());
        ci.Set_Summ4(receiptTotalV2Ex.getSumm4());
        ci.Set_Summ5(receiptTotalV2Ex.getSumm5());
        ci.Set_Summ6(receiptTotalV2Ex.getSumm6());
        ci.Set_Summ7(receiptTotalV2Ex.getSumm7());
        ci.Set_Summ8(receiptTotalV2Ex.getSumm8());
        ci.Set_Summ9(receiptTotalV2Ex.getSumm9());
        ci.Set_Summ10(receiptTotalV2Ex.getSumm10());
        ci.Set_Summ11(receiptTotalV2Ex.getSumm11());
        ci.Set_Summ12(receiptTotalV2Ex.getSumm12());
        ci.Set_Summ13(receiptTotalV2Ex.getSumm13());
        ci.Set_Summ14(receiptTotalV2Ex.getSumm14());
        ci.Set_Summ15(receiptTotalV2Ex.getSumm15());
        ci.Set_Summ16(receiptTotalV2Ex.getSumm16());
        ci.Set_DiscountOnCheck(0.0d);
        ci.Set_Tax1((int) receiptTotalV2Ex.getTaxOne());
        ci.Set_Tax2((int) receiptTotalV2Ex.getTaxTwo());
        ci.Set_Tax3((int) receiptTotalV2Ex.getTaxThree());
        ci.Set_Tax4((int) receiptTotalV2Ex.getTaxFour());
        ci.Set_StringForPrinting(receiptTotalV2Ex.getText());

        if (getVersionFFD() != FFD_1_2) {
            putCheckAgentSign(receiptTotalV2Ex.getCheck());
        }

        ci.FNCloseCheckEx();
        throwExceptionIfError(ci);
        ci.WaitForPrinting();
        ci.Set_Password(getSysAdminPassword());
        waitPrintWithCheckState();
        throwExceptionIfError(ci);

        log.info("leaving closeReceiptV2Ex(ShtrihReceiptTotal)");
    }

    /**
     * Добавление реквизита "Признак агента" (1057) в чек
     */
    private void putCheckAgentSign(Check check) throws ShtrihException {
        final AgentType singleAgentType = check.getSingleAgentType();
        if (singleAgentType == null) {
            return;
        }
        log.debug("Purchase has all positions with same agent type {}", singleAgentType);
        sendTag(1057, (byte) singleAgentType.getBitMask());
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
    private int getClicheLinesCount() throws ShtrihException {
        TableStructure tableStructure = getTableStructure(getHeadersAndFootersTableNo());
        return tableStructure.getRowsCount() - getFooterLinesCount();
    }

    /**
     * Вернет длину каждой линии текста клише.
     */
    private int getClicheLineLength() throws ShtrihException {
        FieldStructure fs = getFieldStructure(getHeadersAndFootersTableNo(), (byte) 1);
        return fs.getFieldWidth();
    }

    /**
     * Очистит клише (реквизиты/заголовок чека).
     *
     * @param clicheLinesCount количество строк клише, поддерживаемое данной моделью ФР
     * @param clicheLineLenght допустимая длина каждой строки клише
     */
    private void clearCliche(int clicheLinesCount, int clicheLineLenght) throws ShtrihException {
        log.debug("entering clearCliche(clicheLinesCount, clicheLineLenght). The argument is: clicheLinesCount [{}], clicheLinesCount [{}]]", clicheLinesCount,
                clicheLineLenght);
        byte[] empty = new byte[clicheLineLenght];
        for (int i = 0; i < clicheLinesCount; i++) {
            writeTable(getHeadersAndFootersTableNo(), getFooterLinesCount() + 1 + i, (byte) 1, empty);
        }
        log.debug("entering leaving");
    }

    @Override
    public void setHeader(List<FontLine> header) throws ShtrihException {
        log.info("entering setHeader(List). The argument is: header [{}]", header);

        // 1. сначала сохраним это в оперативке
        this.header = header;

        // 2. Здесь надо полностью очистить текст клише
        int clicheLinesCount = getClicheLinesCount();
        int clicheLineLenght = getClicheLineLength();
        if (clicheLinesCount <= 0 || clicheLineLenght <= 0) {
            // видимо, клише не поддерживается
            log.warn("leaving setHeader(List). Cliche is not supported [cliche lines: {}; cliche line lengths: {}]",
                    clicheLinesCount, clicheLineLenght);
            return;
        }
        clearCliche(clicheLinesCount, clicheLineLenght);

        // 3. и записать в клише то, что нам прислали
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
                log.info("\"{}\" was encoded into {}", fl.getContent(), PortAdapterUtils.arrayToString(data));
                lines.add(data);
            }
        }
        for (int idx = 0; idx < lines.size(); idx++) {
            byte[] data = lines.get(idx);
            writeTable(getHeadersAndFootersTableNo(), idx + 1 + getFooterLinesCount(), (byte) 1, data);
        }
        // добавляем 1 к getFooterLinesCount() чтобы отоброжение заголовка в файле соответсвовало расположение заголовка в таблице штриха
        shtrihDataTableProperties.setHeader(header, 1 + getFooterLinesCount(), 1 + getFooterLinesCount() + header.size());
        log.info("leaving setHeader(List)");
    }

    /**
     * Вернет индекс 1го элемента из указанного массива, что равен указанному числу.
     *
     * @param data   массив, в котором ведем поиск
     * @param marker значение, что ищем в массиве
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
    protected List<FontLine> getHeader() throws ShtrihException {
        log.info("entering getHeader()");
        if (!config.isPrintLegalEntityHeader()) {
            return Collections.emptyList();
        }
        if (header == null) {
            // Придется читать из таблицы. Видимо, после открытия смены кассу перегрузили - из оперативки инфа о реквизитах пропала
            //  восстановим. Правда инфу о шрифте при этом потеряем (пока что 2016-01-27 - шрифт заголовка можно сохранять и считывать из другой таблицы)
            header = new LinkedList<>();

            int clicheLinesCount = getClicheLinesCount();
            for (int idx = 0; idx < clicheLinesCount; idx++) {
                byte[] data = readTable(getHeadersAndFootersTableNo(), idx + 1 + getFooterLinesCount(), (byte) 1);
                if (data.length == 0 || data[0] == 0x00) {
                    // осознанно запрограммированная часть клише закончилась
                    break;
                }
                // выкинем бессмысленные символы:
                int firstWrongByteIdx = getFirstIndexOf(data, (byte) 0x00);
                if (firstWrongByteIdx > 0) {
                    data = Arrays.copyOf(data, firstWrongByteIdx);
                }
                String line = new String(data, ENCODING);
                line = StringUtils.left(line, config.getMaxCharRow());
                log.info("{} converted into \"{}\"", PortAdapterUtils.arrayToString(data), line);
                FontLine fl = new FontLine(line, Font.NORMAL);
                header.add(fl);
            } // for idx
        }

        if (header.isEmpty()) {
            log.info("Header from fiscal printer is empty, try to load it from shtrihDataTableProperties");
            header = shtrihDataTableProperties.getHeader(1 + getFooterLinesCount(), getClicheLinesCount());
        }
        log.info("leaving getHeader - header value : {}", header);
        return header;
    }

    @Override
    public void openCashDrawer(byte cashDrawerNumber) throws ShtrihException {
        log.info("entering openCashDrawer(byte). the argument is: cashDrawerNumber [{}]", cashDrawerNumber);

        ci.Set_DrawerNumber(cashDrawerNumber);
        ci.OpenDrawer();
        throwExceptionIfError(ci);

        log.info("leaving openCashDrawer(byte)");
    }

    @Override
    public ShtrihShortStateDescription getShortState() throws ShtrihException {
        log.info("entering getShortState()");

        // 1. Исполним запрос
        ci.GetShortECRStatus();
        throwExceptionIfError(ci);
        ShtrihShortStateDescription result = new ShtrihShortStateDescription();
        result.setFlags(new ShtrihFlags((short) ci.Get_ECRFlags()));
        result.setState(new ShtrihMode((byte) ci.Get_ECRMode()));
        result.setSubState(ShtrihSubState.getByCode((byte) ci.Get_ECRAdvancedMode()));
        result.setOperationsCount(ci.Get_QuantityOfOperations());
        result.setUpsSupplyVoltage((int) ci.Get_BatteryVoltage());
        result.setMainSupplyVoltage((int) ci.Get_PowerSourceVoltage());
        result.setEklzErrorCode((byte) ci.Get_EKLZResultCode());
        result.setFiscalBoardErrorCode((byte) ci.Get_FMResultCode());

        log.info("leaving getShortState(). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihStateDescription getState() throws ShtrihException {
        log.info("entering getState()");

        // 1. считаем статус
        ci.GetECRStatus();
        throwExceptionIfError(ci);

        ShtrihStateDescription result = new ShtrihStateDescription();
        result.setSoftwareVersion(ci.Get_ECRSoftVersion());
        result.setSoftwareBuild(ci.Get_ECRBuild());
        result.setSoftwareReleaseDate(ci.Get_ECRSoftDate());
        result.setNumber((byte) ci.Get_LogicalNumber());
        result.setCurrentDocNo(ci.Get_OpenDocumentNumber());
        result.setFlags(new ShtrihFlags((short) ci.Get_ECRFlags()));
        result.setMode(new ShtrihMode((byte) ci.Get_ECRMode()));
        result.setSubState(ShtrihSubState.getByCode((byte) ci.Get_ECRAdvancedMode()));
        result.setPort((byte) ci.Get_PortNumber());
        result.setFiscalBoardSoftwareVersion(ci.Get_FMSoftVersion());
        result.setFiscalBoardSoftwareBuild(ci.Get_FMBuild());
        result.setFiscalBoardSoftwareReleaseDate(ci.Get_FMSoftDate());
        Date date = ci.Get_Date();
        date.setTime(ci.Get_Time().getTime());
        result.setCurrentTime(date);
        result.setDeviceNo(Long.parseLong(getSerialNumberFromTable()));
        result.setLastClosedShiftNo(ci.Get_SessionNumber());
        result.setFreeFiscalRecords(ci.Get_FreeRecordInFM());
        result.setFiscalizedCount((byte) ci.Get_RegistrationNumber());
        result.setFreeFiscalRecords((byte) ci.Get_FreeRegistration());
        result.setTin(Long.parseLong(ci.Get_INN()));
        throwExceptionIfError(ci);
        log.info("leaving getState(). The result is: {}", result);

        return result;
    }

    @Override
    public FiscalMemorySums getFiscalMemorySums(boolean all) throws ShtrihException {
        log.info("entering getFiscalMemorySums(boolean). The argument is: all [{}]", all);
        ci.Set_TypeOfSumOfEntriesFM(!all);
        ci.GetFMRecordsSum();
        throwExceptionIfError(ci);
        FiscalMemorySums result = new FiscalMemorySums();
        result.setSalesSum(BigInteger.valueOf(ci.Get_Summ1()));
        result.setPurchasesSum(ci.Get_Summ2());
        result.setSalesReturnsSum(ci.Get_Summ3());
        result.setPurchasesReturnsSum(ci.Get_Summ4());

        log.info("leaving getFiscalMemorySums(boolean). The result is: {}", result);

        return result;
    }

    @Override
    public ShtrihRegNum getRegNum() throws ShtrihException {
        log.info("entering getRegNum()");

        ShtrihRegNum result = new ShtrihRegNum();
        result.setDeviceNo(Long.parseLong(getSerialNumberFromTable()));
        result.setRegNo(Long.parseLong(getRNMFromTable()));

        log.info("leaving getRegNum(). The result is: {}", result);
        return result;
    }

    @Override
    public ShtrihDeviceType getDeviceType() throws ShtrihException {
        log.info("entering getDeviceType()");

        ci.GetDeviceMetrics();
        throwExceptionIfError(ci);

        ShtrihDeviceType result = new ShtrihDeviceType();
        result.setProtocolVersion(ci.Get_UMajorProtocolVersion());
        result.setProtocolSubVersion(ci.Get_UMinorProtocolVersion());
        result.setTypeId(ci.Get_UMajorType());
        result.setSubTypeId(ci.Get_UMinorType());
        result.setDeviceId(ci.Get_UModel());
        result.setLanguage(ci.Get_UCodePage());
        result.setName(ci.Get_UDescription());

        log.info("leaving getDeviceType(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет строковое представление указанного массива байт в кодировке {@link #ENCODING}.
     *
     * @param data массив байт, что надо преобразовать в строку
     * @return не <code>null</code>
     */
    private String getString(byte[] data) {
        return new String(data, ENCODING);
    }

    /**
     * По факту обратный метод методу {@link #getString(byte[])}: указанную строку вернет в виде массива байт в кодировке {@link #ENCODING}.
     *
     * @param string строка, чье "байтовое" представление надо вернуть
     * @return не <code>null</code>
     */
    private byte[] getBytes(String string) {
        return string.getBytes(ENCODING);
    }

    @Override
    public void printBarcode(BarCode barcode, String label, ShtrihAlignment alignment) throws ShtrihException {
        StopTimer stopWatch = new StopTimer();

        if (log.isDebugEnabled()) {
            log.debug("entering printBarcode(BarCode, String, ShtrihAlignment). " +
                    "The arguments are: barcode [{}], label [{}], alignment [{}]", barcode, label, alignment);
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

        log.debug("leaving printBarcode(BarCode, String, ShtrihAlignment). It took {}", stopWatch);
    }

    /**
     * Вернет аргумент дополненный по краям (в зависимости от способа выравнивания) пробелами - до размера ширины чековой ленты (при печати нормальным
     * шрифтом).
     *
     * @param text      текст, что надо "выровнять"
     * @param alignment способ выравнивания текста; <code>null</code> распознается как {@link ShtrihAlignment#CENTRE}
     * @return <code>null</code>, если аргумент == <code>null</code>; <em>обрезанную</em> (справа) версию аргумента, если данная строка уже и так шире
     * ...ширины чековой ленты
     */
    private String getAlignedLine(String text, ShtrihAlignment alignment) {
        if (text == null) {
            return null;
        }

        int ribbonWidth = config.getMaxCharRow();
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
     * @throws ShtrihException если нет возможности начать печатать. например, если нету бумаги
     */

    protected boolean validateShiftOpenTime() throws ShtrihException {
        log.info("entering validateShiftOpenTime()");
        boolean result;
        getState();
        throwExceptionIfError(ci);

        ci.GetShortECRStatus();
        throwExceptionIfError(ci);
        ShtrihModeEnum mode = ShtrihModeEnum.values()[ci.Get_ECRMode()];

        if (ShtrihModeEnum.SHIFT_IS_OPEN_FOR_MORE_THAN_24H.getCode() == mode.getCode()) {
            log.error("validateShiftOpenTime(): SHIFT_IS_OPEN_FOR_MORE_THAN_24H ");
            result = false;
        } else {
            result = true;
        }
        log.info("leaving validateShiftOpenTime()");
        return result;
    }

    protected ShtrihStateDescription waitForPrinting() throws ShtrihException {
        ShtrihStateDescription result;
        StopTimer stopWatch = new StopTimer();

        log.info("entering waitForPrinting()");

        while (true) {

            ci.WaitForPrinting();
            result = getState();
            ci.GetShortECRStatus();
            ShtrihModeEnum mode = ShtrihModeEnum.values()[ci.Get_ECRMode()];

            // если нет бумаги - Exception
            if (ShtrihSubState.PAPER_ABSENT_ACTIVELY.getCode() == result.getSubState().getCode() | ShtrihSubState.PAPER_ABSENT_PASSIVELY.getCode() == result.getSubState().getCode()) {
                log.error("waitForPrinting(): paper is absent");
                throw new ShtrihResponseException(ShtrihResponseException.WRONG_SUBSTATE, (byte) 0, result);
            }

            // если есть бумага ...
            //  и при этом сейчас ничего не печатается, то можем печатать
            if (ShtrihSubState.PAPER_PRESENT.getCode() == result.getSubState().getCode() && !ShtrihModeEnum.isPrinting(mode)) {
                // просто выйдем из цикла
                break;
            }

            // если ФР в состоянии "появилась бумага после ее активного отсутствия", то надо ФР сказать,
            //  что можем продолжить работу:
            if (ShtrihSubState.WAITING.getCode() == result.getSubState().getCode()) {
                ci.ContinuePrint();
                throwExceptionIfError(ci);
            }

            if (ShtrihModeEnum.CLEAN_UP_ALLOWED.equals(mode)) {
                throw new ShtrihException("Incorrect work mode : " + ShtrihModeEnum.CLEAN_UP_ALLOWED);
            }

            // подождем и считаем состояние ФР еще раз - в цикл
            try {
                Thread.sleep(checkStatusInterval);
            } catch (InterruptedException e) {
                log.error("waitForPrinting(): interrupted!", e);
                Thread.currentThread().interrupt();
            }
        }
        log.info("leaving waitForPrinting(). The result is: {}; it took {}", result, stopWatch);
        return result;
    }

    private void waitTimeInterval(long timeInterval) {
        // подождем и считаем состояние ФР еще раз - в цикл
        try {
            Thread.sleep(timeInterval);
        } catch (InterruptedException e) {
            log.error("waitForPrinting(): interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Разобъет указанную строку на кусочки длиной не более максимальной длины строоки указанным шрифторм.
     *
     * @param text   текст, что надо разбить
     * @param fontNo номер шрифта. которым хотим этот текст печатать
     * @return не <code>null</code>; и размер будет как минимум <code>1</code> - как минимум содержащий сам <code>text</code>
     */
    private List<String> getLines(String text, byte fontNo) throws ShtrihException {
        if (StringUtils.isEmpty(text)) {
            // не надо шрифты анализировать
            return Collections.singletonList(text);
        }

        // получим настройки указанного шрифта
        ShtrihFontProperties fontProperties = getFontProperties(fontNo);
        if (fontProperties == null) {
            // какой-то левый шрифт хотят использовать при печати?
            return Collections.singletonList(text);
        }

        // узнаем сколько символов этим шрифтом может поместиться в строке
        int symbolsPerLine = 0;
        if (fontProperties.getSymbolWidth() != 0) {
            symbolsPerLine = fontProperties.getPrintableAreaWidth() / fontProperties.getSymbolWidth();
        }
        if (symbolsPerLine < 1) {
            return Collections.singletonList(text);
        }

        // а теперь просто разбиваем TEXT на строки длиной до symbolsPerLine
        List<String> result = new LinkedList<>();
        int beginIndex = 0;
        String nextLine;
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
    public void printLine(FontLine line) throws ShtrihException {
        log.debug("entering printLine(FontLine). The argument is: line [{}]", line);

        if (line == null) {
            log.error("leaving printLine(FontLine): the argument ({}) is INVALID!", line);
            throw new IllegalArgumentException("printLine(FontLine): the argument is INVALID!");
        }

        // 1. выясним, каким шрифтом надо эту строку печатать:
        byte desirableFont = getFontSizeForLine(line);
        log.trace("desirable font is: {}", desirableFont);

        // 2. И разобъем эту строку на кусочки (если строка слишком длинная):
        List<String> lines = getLines(line.getContent(), desirableFont);

        // 3. А теперь эти строчки просто распечатаем:
        for (String text : lines) {
            ci.Set_UseJournalRibbon(false);
            ci.Set_UseReceiptRibbon(true);
            ci.Set_StringForPrinting(convertTextForPrint(text));
            ci.Set_FontType(desirableFont);
            printLineWithCheckState();
            throwExceptionIfError(ci);
            log.debug("print line: {}", text);
        }
        log.debug("leaving printLine(FontLine)");
    }

    protected byte getFontSizeForLine(FontLine line) {
        if (!isUseFontsFromTemplate()) {
            return (byte) config.getPrintStringFont();
        }
        if (line.getFont() == null || Font.NORMAL.equals(line.getFont())) {
            return line.getConcreteFont() == null || line.getConcreteFont() == 0
                    ? (byte) config.getPrintStringFont() : line.getConcreteFont().byteValue();
        }
        return getFontSize(line.getFont());
    }

    /**
     * Преобразует строку в байты и обратно при заданной кодировке.
     * Требуется в случае, если в строке, которую нужно напечатать, есть символы, которые штрих не распечатает
     * по причине их отсутствия в кодировке штриха.
     * В результате будет выдана строка с замененными необычными символами на что-то, что присутствует в кодировке.
     *
     * @param text текст с инородными символами
     * @return текст с замененными инородными символами
     */
    protected String convertTextForPrint(String text) {
        return StringUtils.isNotEmpty(text) ? new String(getBytes(text), ENCODING) : text;
    }

    @Override
    public void openNonFiscalDocument() throws ShtrihException {
        log.info("entering openNonFiscalDocument()");
        throwExceptionIfError(ci);
        log.info("leaving openNonFiscalDocument()");
    }

    @Override
    public void closeNonFiscalDocument(AbstractDocument document) throws ShtrihException {
        log.trace("entering closeNonFiscalDocument()");

        // 1. допечатаем документ
        // отезка отключается вместе с заголовком следующего документа (по аналогии с пиритом)
        if (document == null || document.getNextServiceDocument() == null || !document.getNextServiceDocument().isDisableCut()) {
            printDocEnd();
        }

        throwExceptionIfError(ci);

        log.info("leaving closeNonFiscalDocument()");
    }


    @Override
    public void printXReport() throws ShtrihException {
        log.info("entering printXReport()");
        // 1. распечатаем X-отчет
        ci.PrintReportWithoutCleaning();
        waitForPrinting();
        throwExceptionIfError(ci);
        // 2. и шапку следующего документа
        printReportEnd();

        log.info("leaving printXReport()");
    }

    @Override
    public void printZReport(Cashier cashier) throws ShtrihException {
        log.info("entering printZReport()");

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

        setCashierName((byte) getDefaultAdmPassword(), cashier.getLastnameAndInitials());

        // 3. закрываем смену
        ci.Set_Password(getDefaultAdmPassword());
        ci.PrintReportWithCleaning();
        throwExceptionIfError(ci);
        ci.Set_Password(getSysAdminPassword());
        waitForPrinting();
        throwExceptionIfError(ci);
        // 4. и шапку следующего документа
        printReportEnd();

        log.info("leaving printZReport()");
    }

    @Override
    public void printFNReport(Cashier cashier) throws ShtrihException {
        log.info("entering printFNReport(cashier). The argument is: cashier [{}]", cashier);

        FontLine emptyLine = new FontLine(" ", Font.NORMAL);
        ci.FNBeginCalculationStateReport();
        throwExceptionIfError(ci);
        ci.FNBuildCalculationStateReport();
        waitForPrinting();
        throwExceptionIfError(ci);

        printLine(emptyLine);

        printHeaderAndCut();

        log.info("leaving printFNReport(cashier)");
    }

    @Override
    public void regCashIn(long sum) throws ShtrihException {
        log.info("entering regCashIn(long). The argument is: sum [{}]", sum);

        ci.Set_Summ1(sum);
        ci.CashIncome();
        waitPrintWithCheckState();
        throwExceptionIfError(ci);

        printDocEnd();

        log.info("leaving regCashIn()}");
    }

    @Override
    public void regCashOut(long sum) throws ShtrihException {
        log.info("entering regCashOut(long). The argument is: sum [{}]", sum);

        ci.Set_Summ1(sum);
        ci.CashOutcome();
        waitPrintWithCheckState();
        throwExceptionIfError(ci);

        printDocEnd();

        log.info("leaving regCashOut");
    }

    @Override
    public void openShift(Cashier cashier) throws ShtrihException {
        log.info("entering openShift() cashier:{}", cashier);
        getState();
        if (cashier != null) {
            setCashierName((byte) getDefaultAdmPassword(), cashier.getLastnameAndInitials());
        }
        ci.Set_Password(getDefaultAdmPassword());
        ci.FNOpenSession();
        throwExceptionIfError(ci);
        ci.Set_Password(getCurrentUserNo());
        waitForPrinting();
        throwExceptionIfError(ci);
        getState();
        printDocEnd();
        log.info("leaving openShift()");
    }


    private void throwExceptionIfError(classic_interface ci) throws ShtrihException {
        throwExceptionIfError(ci, true, null);
    }

    private void throwExceptionIfError(classic_interface ci, Supplier<Integer> operation) throws ShtrihException {
        throwExceptionIfError(ci, true, operation);
    }

    /**
     * Выкинет {@link ShtrihResponseException}, если указанный ответ на указанную команду сигнализирует об ошибке.
     *
     * @param ci инстанс драйвера
     * @throws ShtrihResponseException если ответ сигнализирует об ошибке
     */
    private void throwExceptionIfError(classic_interface ci, boolean isTryReconnection, Supplier<Integer> operation) throws ShtrihException {
        int result = ci.Get_ResultCode();
        if (result == NO_ERROR) {
            return;
        }
        final String errorMessage = ci.Get_ResultCodeDescription();
        if (result == DRV_IO_ERROR) {
            log.warn("Reconnecting on DRV_IO_ERROR (-1)");
            if (isTryReconnection) {
                tryReconnect(true, Duration.ofMillis(config.getMaxReconnectionTimeout()));
                if (operation != null) {
                    operation.get();
                    throwExceptionIfError(ci, false, null);
                    return;
                }
            }
        }
        log.error("leaving throwExceptionIfError(error code is: {}, message {}", result, errorMessage);
        throw new ShtrihException(errorMessage);
    }

    private void tryReconnect(boolean closeConnection, Duration maxReconnectionTimeout) throws ShtrihException {
        final String baseIOErrorMessage = ci.Get_ResultCodeDescription();
        final Timer reconnectionTimer = Timer.of(maxReconnectionTimeout);
        while (reconnectionTimer.isNotExpired()) {
            if (reconnect(closeConnection)) {
                log.debug("Reconnected in {}", reconnectionTimer.getElapsedTimeAsString());
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.debug("Reconnecting interrupted after waiting for {}", reconnectionTimer.getElapsedTimeAsString());
                Thread.currentThread().interrupt();
                throw new ShtrihException(baseIOErrorMessage);
            }
        }
        log.debug("Reconnecting failed after waiting for {}", reconnectionTimer.getElapsedTimeAsString());
        throw new ShtrihException(baseIOErrorMessage);
    }

    private boolean reconnect(boolean close) {
        try {
            if (close) {
                close();
            }
            checkResult(ci.Connect());
        } catch (ShtrihException e) {
            log.error("Error on Connect(): {}, {}", ci.Get_ResultCodeDescription(), e.getMessage());
            return false;
        }
        try {
            checkResult(ci.GetECRStatus());
        } catch (ShtrihException e) {
            log.error("Error on get status after reconnect: {}, {}", ci.Get_ResultCodeDescription(), e.getMessage());
            return false;
        }
        return true;
    }

    private void printLineWithCheckState() throws ShtrihException {
        if (ShtrihConnector.NO_ERROR == ci.Get_ResultCode()) {
            ci.PrintStringWithFont();
        } else {
            waitForPrinting();
            ci.PrintStringWithFont();
        }
        waitPrintWithCheckState();
    }

    private void waitPrintWithCheckState() throws ShtrihException {
        Timer timer = Timer.of(timeOutWaitPrint);
        while (ShtrihConnector.PREVIOUS_PRINT_ORDER_IS_PROGRESS_ERROR == ci.Get_ResultCode()) {
            if (timer.isNotExpired()) {
                waitForPrinting();
            }
            waitTimeInterval(checkStatusInterval);
        }
    }


    /**
     * Вернет указанную строку текста в виде массива байт, недостающие до длинны байты будут заполнены пробелами.
     * <p/>
     * NOTE: аргументы не валидируются.
     *
     * @param text   строка текста, что надо преобразовать в массив байт перед записью/печатью в ФР
     * @param length задает размер возвращаемого массива; если строка в результате преобразования в массив этого размера не поместится, то последние
     *               символы будут просто отброшены
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
     * @param text     строка текста, что надо преобразовать в массив байт перед записью/печатью в ФР
     * @param length   задает размер возвращаемого массива; если строка в результате преобразования в массив этого размера не поместится, то последние
     *                 символы будут просто отброшены
     * @param fillChar недостающие до длинны байты будут заполнены этим символом
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
                        text, textAsArray.length, length);
            }
        }
        System.arraycopy(textAsArray, 0, result, 0, Math.min(textAsArray.length, length));

        return result;
    }

    /**
     * Печатает 2D ШК аппаратно (через команду печати 2D ШК - см. {@link Print2DBarcodeCommand}).
     *
     * @param barcode   ШК, что надо распечатать
     * @param alignment выравнивание этого ШК на чековой ленте
     * @return <code>true</code>, если ШК был распечатан; <code>false</code> - если нет (возможно, данная модель ФР просто не поддерживает команду печати 2D ШК)
     */
    private boolean print2DBarcode(BarCode barcode, ShtrihAlignment alignment) {
        boolean result;
        StopTimer stopWatch = new StopTimer();

        log.info("entering print2DBarcode(BarCode, ShtrihAlignment). The arguments are: barcode [{}], alignment [{}]", barcode, alignment);

        if (Boolean.FALSE.equals(ableToPrint2DBarcodes)) {
            // уже пробовали печатать 2D ШК и знаем, что данная модель ФР этот функционал не поддерживает
            log.info("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (IN-MEMORY): FALSE");
            return false;
        }

        // 1. Определим тип этого ШК
        Shtrih2DBarcodeType type = getBarcodeType(barcode);
        if (type == null) {
            // это не знакомый нам 2D ШК - не будем печатать аппаратно
            log.info("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (the barcode is not 2D): FALSE");
            return false;
        }
        log.info("2D barcode type is: {}", type);

        // 2. Получим данные этого ШК:
        byte[] data = getBarcodeValue(barcode);
        if (data == null) {
            // это пустой ШК - не будем его печатать
            log.warn("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is (the barcode value is EMPTY): FALSE");
            return false;
        }
        log.info("barcode data is ({}): {}", data.length, PortAdapterUtils.arrayToString(data));

        ci.Set_BarCode(barcode.getValue());
        ci.Set_BarcodeType(Shtrih2DBarcodeType.QR.equals(type) ? 3 : 0);  // Просетим тип QR кода
        ci.Set_BarcodeStartBlockNumber(0);
        ci.Set_BarcodeParameter1(getPrint2DParameters(type)[0]);
        ci.Set_BarcodeParameter3(getPrint2DParameters(type)[2]);
        ci.Set_BarcodeParameter5(getPrint2DParameters(type)[4]);
        ci.Set_BarcodeAlignment(classic_interface.TBarcodeAlignment.swigToEnum(convertAlignmentForBarCode(alignment)));

        log.info("printing 2D barcode");
        if (ci.LoadAndPrint2DBarcode() != 0) {
            int resultCode = ci.Get_ResultCode();
            String description = ci.Get_ResultCodeDescription();
            // ошибка! видимо, этот ФР не умеет печатать QR-коды
            log.warn("LoadAndPrint2DBarcode failed! ErrCode = {} description {}", resultCode, description);
            ableToPrint2DBarcodes = false;
            result = false;
        } else {
            // этот ФР умеет! Похоже, реально 2D ШК распечатан
            result = true;
        }
        log.info("leaving print2DBarcode(BarCode, ShtrihAlignment). The result is: {}; it took {}", result, stopWatch);

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
            int dotSize = config.getMaxBarcodeScaleFactor();
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

    private int convertAlignmentForBarCode(ShtrihAlignment alignment) {
        int result;
        if (ShtrihAlignment.LEFT.equals(alignment)) {
            result = 1;
        } else if (ShtrihAlignment.RIGHT.equals(alignment)) {
            result = 2;
        } else {
            result = 0;
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
        if (barcode == null || StringUtils.isEmpty(barcode.getValue())) {
            return null;
        }
        return barcode.getValue().getBytes(BaseCommand.ENCODING);
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
     * Печать ШК. Вернет <code>true</code>, если ШК был распечатан. Иначе вернет <code>false</code>.
     */
    private boolean printBarcodeInner(BarCode barcode, ShtrihAlignment alignment) throws ShtrihException {
        boolean result = false;
        StopTimer stopWatch = new StopTimer();

        log.debug("entering printBarcodeInner(BarCode, ShtrihAlignment). The arguments are: barcode [{}], alignment [{}]", barcode, alignment);

        // 0. сначала попробуем распечатать как 2D ШК
        if (print2DBarcode(barcode, alignment)) {
            log.debug("leaving printBarcodeInner(BarCode, ShtrihAlignment): The barcode was printed as 2D one. It took {}", stopWatch);
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

        // 2. Печатаем получившуюся картинку через "печать линии" если это одномерный ШК
        if (matrix.getHeight() == 1) {
            result = printBarcodeWithLines(barcode, alignment);
        }

        log.debug("leaving printBarcodeInner(BarCode, ShtrihAlignment). It took {}", stopWatch);
        return result;
    }

    private boolean printBarcodeWithLines(BarCode barcode, ShtrihAlignment alignment) throws ShtrihException {
        if (!supportsPrintLineCmd) {
            return false;
        }
        log.trace("printBarcodeInner: trying to print bar-code [{}] through \"print line\" commnads", barcode);
        try {
            ci.Set_BarCode(barcode.getValue());
            if (!config.isPrintQrBarCode()) {
                // 0 = Code128A
                ci.Set_BarcodeType(0);
                ci.Set_BarcodeAlignment(classic_interface.TBarcodeAlignment.swigToEnum(convertAlignmentForBarCode(alignment)));
                ci.Set_LineNumber((int) barcode.getHeight());
                ci.Set_BarWidth(getEffectiveBarcodeLineWidth(barcode));
                ci.Set_PrintBarcodeText(0);
                ci.Set_SwapBytesMode(config.getSwapBytesMode());

                ci.PrintBarcodeLine();
            } else {
                // 3 = QR код
                ci.Set_BarcodeType(3);
                ci.Set_BarcodeAlignment(classic_interface.TBarcodeAlignment.baCenter);
                ci.Set_BarcodeStartBlockNumber(0);
                ci.Set_BarcodeParameter1(0);
                ci.Set_BarcodeParameter2(0);
                ci.Set_BarcodeParameter3(3);
                ci.Set_BarcodeParameter4(0);
                ci.Set_BarcodeParameter5(0);

                ci.LoadAndPrint2DBarcode();
            }
            waitForPrinting();
            throwExceptionIfError(ci);
            return true;
        } catch (ShtrihResponseException sre) {
            if (sre.getErrorCode() != COMMAND_NOT_SUPPORTED) {
                throw sre;
            }
            log.error("printBarcodeInner: it seems that this model does not support print barcode command!", sre);
            // больше не будем пытаться печатать через "печать линии", т.к. команда не поддерживается
            supportsPrintLineCmd = false;
            return false;
        }
    }

    private int getEffectiveBarcodeLineWidth(BarCode barcode) {
        if (config.getMaxCharRow() == SLIM_TAPE && config.getBarcodeLineWidth() == ShtrihConfiguration.NONE_WIDTH_BARCODE_LINE) {
            return ShtrihConfiguration.ONE_WIDTH_BARCODE_LINE;
        }
        if (config.getBarcodeLineWidth() > ShtrihConfiguration.NONE_WIDTH_BARCODE_LINE) {
            return config.getBarcodeLineWidth();
        }
        return (int) barcode.getWidth();
    }


    /**
     * Печатает указанную картинку через команду "Печать графики с масштабированием" (0x4F)
     *
     * @param matrix    картинка, что надо распечатать
     * @param alignment [горизонтальное] выравнивание этой картинки на чековой ленте
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
            log.info("printAsScaledGraphics: the picture is 1D only");
            return false;
        }
        if (!supportsPrintScaledGraphicsCmd) {
            log.info("leaving printAsScaledGraphics(BitMatrix, ShtrihAlignment): the 0x4F command is NOT supported");
            return false;
        }

        // максимально возможная ширина картинки/графики, в пикселях:
        int currentRibbonWidthInPx = getPictureWidth();

        // во сколько раз эту картинку надо отмасштабировать, чтоб получилась максимально большое/читабельное изображение:
        int scale = getScale(currentRibbonWidthInPx, matrix.getWidth());
        log.info("printAsScaledGraphics: the picture should be scaled by {} times for optimal readability", scale);

        if (scale < 1) {
            log.error("printAsScaledGraphics: unable to print matrix 'cause the resulting scale is non-positive");
            return false;
        }

        // вот эта картинка по-строково:
        List<byte[]> pictureLines = GraphicsUtils.getPictureLines(matrix, getAlignment(alignment), scale, currentRibbonWidthInPx, false);

        // загрузим эту картинку в буфер принтера и печатаем:
        int maxLinesAtOnce = config.getMaxLoadGraphicsLines();
        int firstLineNo = 1; // жестко 1 - иначе не рыботает/ правильно было бы: getFirstLineNo()
        log.info("printAsScaledGraphics: the first line no is: {}", firstLineNo);
        int lineNo = firstLineNo;
        log.info("printAsScaledGraphics: about to load {} lines of graphics scaled by {} vertically {} lines at a time", pictureLines.size(), scale, maxLinesAtOnce);

        try {
            for (byte[] line : pictureLines) {
                ci.Set_LineNumber(lineNo);
                ci.Set_LineData(new String(line));
                try {
                    throwExceptionIfError(ci);
                } catch (ShtrihException t) {
                    // все же не удалось загрузить картинку
                    log.error("printAsScaledGraphics: failed to load picture", t);
                    return false;
                }
                lineNo++;
                // пора печатать, что уже загрузили?
                if (lineNo == maxLinesAtOnce) {
                    log.info("printAsScaledGraphics: printing chunk of {} lines of graphics", lineNo);
                    ci.Set_FirstLineNumber(1);
                    ci.Set_LastLineNumber(lineNo - 1);
                    ci.Draw();
                    throwExceptionIfError(ci);
                    // и начнем следующую итерацию - заполняем буфер с нуля
                    lineNo = firstLineNo;
                }
            } // for line

            // остался какой кусок рисунка, что надо до-печатать?
            if (lineNo != 0) {
                log.info("printAsScaledGraphics: printing the last chunk of {} lines of graphics", lineNo);
                ci.Set_FirstLineNumber(1);
                ci.Set_LastLineNumber(lineNo - 1);
                ci.Draw();
                throwExceptionIfError(ci);
            }

            result = true;
        } catch (ShtrihException t) {
            // значит, нельзя этой командой печатать
            log.info("it seems that the 0x4F command is NOT supported", t);
            supportsPrintScaledGraphicsCmd = false;
        }

        return result;
    }

    /**
     * Вернет указанное выравнивание в виде, понятном {@link GraphicsUtils}.
     *
     * @param alignment выравнивание, чье представление надо вернуть
     * @return не <code>null</code> - в крайнем случае вернет {@link Alignment#CENTER}
     */
    private Alignment getAlignment(ShtrihAlignment alignment) {
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
    private int getPictureWidth() {
        return 40 * 8;
    }

    /**
     * вернет коэффициент масштабирования картинки при печати ШК.
     *
     * @param ribbonWidth  ширина чековой ленты, в пикселях
     * @param pictureWidth ширина картинки, в пикселях
     * @return <code>0</code>, если картинка не помещается на указанной ширине ленты
     */
    private int getScale(int ribbonWidth, int pictureWidth) {
        int result;

        if (pictureWidth <= 0) {
            return 0;
        }
        int maxScale = config.getMaxBarcodeScaleFactor();
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
        int result;
        result = (int) Math.floor(1.0 * config.getBarcodeHeight() * DPI / 25.4);
        return result;
    }

    /**
     * Чтение настроек ФР.
     *
     * @param tableNo номер таблицы, из которой считываем.
     * @param rowNo   номер ряда, в которой находится значение интересующего нас поля
     * @param fieldNo сам номер поля, значение которого хотим считать
     * @return массив байт, описывающий значение этой настройки
     */
    private byte[] readTable(byte tableNo, int rowNo, byte fieldNo) throws ShtrihException {
        byte[] result;

        log.info("entering readTable(byte, byte, byte). the arguments are: tableNo [{}], rowNo [{}], fieldNo [{}]", tableNo, rowNo, fieldNo);
        ci.Set_TableNumber(tableNo);
        ci.Set_RowNumber(rowNo);
        ci.Set_FieldNumber(fieldNo);
        ci.ReadTable();
        throwExceptionIfError(ci);

        FieldStructure fieldStructure = getFieldStructure(tableNo, fieldNo);
        if (fieldStructure.getFieldType().equals(ShtrihFieldType.NUMBER)) {
            result = String.valueOf(ci.Get_ValueOfFieldInteger()).getBytes();
        } else {
            result = ci.Get_ValueOfFieldString().getBytes(ENCODING);
        }

        log.info("leaving readTable(byte, byte, byte). The result is: {}", PortAdapterUtils.arrayToString(result));
        return result;
    }

    /**
     * Запись настроек ФР.
     *
     * @param tableNo номер таблицы, в которую записываем
     * @param rowNo   номер строки в этой таблице, куда записываем
     * @param fieldNo непоследственно само поле (в этой строке) значение которого устанавливаем в указанное ...значение
     * @param value   само новое значение настройки - его и записываем; если <code>null</code> - получите NPE
     */
    protected void writeTable(byte tableNo, int rowNo, byte fieldNo, byte[] value) throws ShtrihException {
        log.info("entering writeTable(byte, byte, byte, byte[]). the arguments are: tableNo [{}], rowNo [{}], fieldNo [{}], value [{}]",
                tableNo, rowNo, fieldNo, PortAdapterUtils.arrayToString(value));

        throwExceptionIfError(ci);
        String strValue;

        if (useEncoding) {
            strValue = new String(value, ENCODING);
        } else {
            strValue = new String(value);
        }

        ci.Set_TableNumber(tableNo);
        ci.Set_RowNumber(rowNo);
        ci.Set_FieldNumber(fieldNo);

        FieldStructure fieldStructure = getFieldStructure(tableNo, fieldNo);

        log.info("write {} value to table", strValue);
        if (fieldStructure.getFieldType().equals(ShtrihFieldType.NUMBER)) {
            ci.Set_ValueOfFieldInteger(Integer.parseInt(strValue));
        } else {
            ci.Set_ValueOfFieldString(strValue);
        }
        ci.WriteTable();
        throwExceptionIfError(ci);

        log.info("leaving writeTable(byte, byte, byte, byte[])");
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
     * @param barcode ШК, что надо отвалидировать
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
     * Загрузка библиотек от вендора. библиотеки - fr_drv_ng_linux_i686
     */
    private static void loadShtrihLibs() {

        SystemInfo systemInfo = new SystemInfo();

        String homePath = System.getProperty("user.dir");

        String dllDir = "";

        if (!systemInfo.isLinux()) {
            if (systemInfo.isIs64BitOS()) {
                dllDir = WIN_X64_DIR;
            } else {
                dllDir = WIN_X32_DIR;
            }
        }

        String shtrihLibPath = new StringBuilder()
                .append(homePath)
                .append(File.separator)
                .append("lib")
                .append(File.separator)
                .append("shtrih")
                .append(dllDir).toString();

        log.info("driver libs path : {}", shtrihLibPath);

        Set<String> libs = new HashSet<>();
        libs.add(resolve(homePath, ""));
        List<String> dirsList = loadLibsCP(homePath, "lib" + File.separator + "shtrih");

        StringBuilder classPath = new StringBuilder();
        for (String dir : dirsList) {
            classPath.append(dir);
            classPath.append(File.pathSeparator);
        }

        StringBuilder libPath = new StringBuilder();
        libPath.append(System.getProperty("java.library.path"));
        classPath.append(System.getProperty("java.class.path"));

        if (!libPath.toString().endsWith(File.pathSeparator)) {
            libPath.append(File.pathSeparator);
        }

        libPath.append(shtrihLibPath);
        System.setProperty("java.class.path", classPath.toString());
        System.setProperty("java.library.path", libPath.toString());

        try {
            Field sys = ClassLoader.class.getDeclaredField("sys_paths");
            sys.setAccessible(true);
            String[] oldLibs = (String[]) sys.get(null);
            libs.add(libPath.toString());
            Collections.addAll(libs, oldLibs);
            String[] libsStrings = libs.toArray(new String[0]);
            sys.set(null, libsStrings);

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<?> clazz = URLClassLoader.class;

            Method method = clazz.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            for (String dir : dirsList) {
                method.invoke(classLoader, new File(dir).toURI().toURL());
            }

        } catch (Exception e) {
            log.error("Could not set sys_path" + e);
        }

        try {
            if (!SystemUtils.IS_OS_WINDOWS) {
                System.loadLibrary("one_s_fr_drv_ng");
            }
            System.loadLibrary("classic_fr_drv_ng");

        } catch (UnsatisfiedLinkError e) {
            log.error("Native code library failed to load.\n" + e);
        }
    }

    private static String resolve(String path, String pathToResolve) {
        if (path.endsWith(File.separator)) {
            return path + pathToResolve;
        } else {
            return path + File.separator + pathToResolve;
        }
    }

    private static List<String> loadLibsCP(String homepath, String... paths) {
        List<String> result = new ArrayList<>();
        for (String p : paths) {
            File dirPath = new File(resolve(homepath, p));
            final File[] files = dirPath.listFiles();
            if (files == null) {
                return result;
            }
            for (File file : files) {
                if (file.getName().endsWith(".jar")) {
                    result.add(file.getAbsolutePath());
                }
            }
        }
        return result;
    }

    //обновляет файл netcfg с настройкой для RNDIS
    private void updateNetCfg() {
        Charset charset = StandardCharsets.UTF_8;
        Path path = Paths.get(ETH_CONFIG_FILE);
        try {
            if (Files.exists(path)) {
                Files.write(path, new String(Files.readAllBytes(path), charset).
                        replace("SHTRIHRNDISSTART=\"0\"", "SHTRIHRNDISSTART=\"1\"").getBytes(charset));
            } else {
                log.error("File not found");
            }
        } catch (IOException exc) {
            log.error("Error during loading the file ", exc);
        }
    }

    /**
     * enq_mode=1 - Обязательная посылка enq после каждой команды
     */
    private String makeDeviceUrl(boolean serialConnection) {
        final String commonPart = String.format("timeout=%d&protocol=v1&enq_mode=1", config.getTimeout());
        if (serialConnection) {
            return String.format("serial://%s?baudrate=%s&%s", config.getSerialPort(), config.getSerialPortBaudRate(), commonPart);
        }
        return String.format("tcp://%s:%d?%s", config.getIpAddress(), config.getTcpPort(), commonPart);
    }

    @Override
    public void open() throws ShtrihException {
        log.debug("entering open()");

        final String deviceUrl = makeDeviceUrl(config.isComConnection());

        updateNetCfg();

        loadShtrihLibs();
        if (ci == null) {
            ci = new classic_interface();
        }

        ci.Set_SCPassword(getSysAdminPassword()); //Пароль ЦТО, нужен для установки нового пароля ЦТО + можно позже
        //воспользоваться для записи служебных таблиц(им необходим пароль ЦТО)
        ci.Set_SysAdminPassword(getSysAdminPassword()); //Пароль сист. администратора
        ci.Set_Password(getSysAdminPassword()); //Пароль кассира(может совпадать с паролем администратора)
        //Включаем обмен с ОФД средствами драйвера
        ci.Set_AutoEoD(true);
        log.info("DEVICE URL: {}", deviceUrl);
        ci.Set_ConnectionURI(deviceUrl);
        log.warn("Reconnecting on start");
        tryReconnect(false, Duration.ofMillis(config.getMaxConnectionTimeout()));

        shtrihDataTableProperties = new ShtrihDataTableProperties();
        shtrihDataTableProperties.loadState();

        // надо аннулировать документ, если он открыт
        if (validateShiftOpenTime()) {
            // надо аннулировать документ, если он открыт
            try {
                annul();
            } catch (ShtrihException e) {
                log.error("Error of annul document", e);
            }
        }

        // проинициализируем наше устройство:
        init();
        log.debug("leaving open()");
    }

    private void checkResult(int ret) throws ShtrihException {
        if (ret != ShtrihConnector.NO_ERROR) {
            throw new ShtrihException("error, bad return code: " + ret);
        }
    }

    @Override
    public void setCashNumber(byte cashNum) throws ShtrihException {
        writeTable(ShtrihTables.TYPE_AND_MODE_TABLE, ShtrihTables.TYPE_AND_MODE_TABLE_SOLE_ROW, ShtrihTables.CASH_NUM_FIELD_NO, String.valueOf(cashNum).getBytes());
    }

    @Override
    public void disableDocumentPrinting(boolean disabled) throws ShtrihException {
        writeTable(ShtrihTables.REGIONAL_SETTINGS_TABLE, 1, ShtrihTables.REGIONAL_SETTINGS_TABLE_RUS_DO_NOT_PRINT_DOC, String.valueOf(disabled ? 1 : 0).getBytes());
    }

    @Override
    public ShtrihEklzStateOne getEklzStateOne() throws ShtrihException {

        log.debug("entering getEklzStateOne()");

        throw new ShtrihException("Method getEklzActivationResultReport is not supported");

    }

    @Override
    public String getFNNumber() throws ShtrihException {
        log.debug("entering getFNNumber()");
        ci.FNGetStatus();
        throwExceptionIfError(ci, () -> ci.FNGetStatus());
        String result = String.valueOf(ci.Get_SerialNumber());

        log.debug("leaving getFNNumber()");

        return result;
    }

    @Override
    public ShtrihFNStateOne getFNState() throws ShtrihException {
        log.debug("entering getFNState()");
        ci.FNGetStatus();
        throwExceptionIfError(ci, () -> ci.FNGetStatus());
        ShtrihFNStateOne result = new ShtrihFNStateOne();
        result.setFnNum(ci.Get_SerialNumber());
        result.setLastFdNum(ci.Get_DocumentNumber());
        result.setShiftOpen(ci.Get_FNSessionState() > 0);

        log.debug("leaving getFNState()");

        return result;
    }

    @Override
    public ShtrihFiscalizationResult getLastFiscalizationResult() throws ShtrihException {
        log.debug("entering getLastFiscalizationResult()");
        ci.FNGetFiscalizationResult();
        throwExceptionIfError(ci, () -> ci.FNGetFiscalizationResult());
        ShtrihFiscalizationResult result = new ShtrihFiscalizationResult();
        result.setFiscalizationDate(ci.Get_Date());
        result.setTin(Long.parseLong(ci.Get_INN()));
        result.setRegNum(ci.Get_KKTRegistrationNumber());
        result.setWorkMode((byte) ci.Get_WorkMode());
        result.setTaxId((byte) ci.Get_TaxType());

        log.debug("leaving getLastFiscalizationResult()");

        return result;
    }

    @Override
    public void setClientData(String clientData) {
        log.debug("entering setClientData() clientData = {} ", clientData);
        try {
            String effectiveNumber = prepareClientData(clientData);
            if (!Objects.equals(effectiveNumber, clientData)) {
                log.debug("clientData converted from {} to {}", clientData, effectiveNumber);
            }
            ci.Set_CustomerEmail(effectiveNumber);
            ci.FNSendCustomerEmail();
            throwExceptionIfError(ci);
        } catch (Exception e) {
            log.error("Error setClientData()", e);
        }

        log.debug("leaving setClientData()");
    }

    private String prepareClientData(String clientData) {
        if (clientData != null && clientData.startsWith("7") && clientData.length() == 11 && NumberUtils.isDigits(clientData)) {
            return "+" + clientData;
        }
        return clientData;
    }

    @Override
    public void setDateTime(Date dateTime) throws ShtrihException {
        if (log.isDebugEnabled()) {
            log.debug("entering setDateTime(Date). The argument is: {}", dateTime == null ? "(NULL)" : String.format("%1$tF %1$tT", dateTime));
        }

        if (dateTime == null) {
            log.error("setDateTime(Date): the argument is NULL!");
            return;
        }

        // 1. запрограммировать время
        ci.Set_Time(dateTime);
        ci.SetTime();
        throwExceptionIfError(ci);

        // 2. запрограммировать дату
        ci.Set_Date(dateTime);
        ci.SetDate();
        ci.ConfirmDate();
        throwExceptionIfError(ci);

        log.debug("leaving setDateTime(Date)");
    }

    /**
     * Вернет структуру указанной таблицы.
     *
     * @param tableNo номер таблицы, чью структуру нало вернуть
     * @return не <code>null</code> - в крайнем случае будет exception
     */
    protected TableStructure getTableStructure(byte tableNo) throws ShtrihException {
        log.info("entering getTableStructure(byte). The argument is: tableNo [{}]", tableNo);

        // Исполним запрос
        ci.Set_TableNumber(tableNo);
        ci.GetTableStruct();
        throwExceptionIfError(ci);
        TableStructure result = new TableStructure();
        result.setTableName(ci.Get_TableName());
        result.setRowsCount(ci.Get_RowNumber());
        result.setFieldsCount(ci.Get_FieldNumber());

        log.info("leaving getTableStructure(byte). The result is: {}", result);

        return result;
    }

    /**
     * Вернет структуру указанного поля.
     *
     * @param tableNo номер таблицы, структуру поля которого надо вернуть
     * @param fieldNo номер поля, структуру  которого надо вернуть
     * @return не <code>null</code> - в крайнем случае будет exception
     */
    protected FieldStructure getFieldStructure(byte tableNo, byte fieldNo) throws ShtrihException {
        log.info("entering getFieldStructure(byte, byte). The arguments are: tableNo [{}], fieldNo [{}]", tableNo, fieldNo);

        // Исполним запрос
        ci.Set_TableNumber(tableNo);
        ci.Set_FieldNumber(fieldNo);
        ci.GetFieldStruct();
        throwExceptionIfError(ci);
        FieldStructure result = new FieldStructure();
        result.setFieldName(ci.Get_FieldName());
        result.setFieldType(ShtrihFieldType.getTypeByCode(ci.Get_FieldType() ? 1 : 0));
        result.setFieldWidth(ci.Get_FieldSize());
        result.setMaxValue(ci.Get_MAXValueOfField());
        result.setMinValue(ci.Get_MINValueOfField());

        log.info("leaving getFieldStructure(byte, byte). The result is: {}", result);

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
    public LinkedHashMap<String, Long> getTaxes() throws ShtrihException {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();

        log.debug("entering getTaxes()");

        // 1. для начала узнаем сколько вообще налоговых ставок поддерживается:
        TableStructure tableStructure = getTableStructure(getTaxRatesTableNo());
        int rowsCount = tableStructure.getRowsCount();
        log.info("tax rates table has {} rows", rowsCount);

        // 2. А теперь для всех налоговых ставок вытянем ... ставку и название
        for (int rowNo = 1; rowNo < rowsCount + 1; rowNo++) {
            // 2.1. ставка налога
            byte[] value = readTable(getTaxRatesTableNo(), rowNo, getTaxRateValueFieldNo());

            // 2.2. название налога
            byte[] name = readTable(getTaxRatesTableNo(), rowNo, getTaxNameFieldNo());

            // и поместим в результат
            result.put(getString(name), Long.valueOf(new String(value)));
        } // for rowNo

        log.debug("leaving getTaxes(). the result is: {}", result);

        return result;
    }


    @Override
    public void setTaxes(Map<String, Long> taxes) throws ShtrihException {
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
        log.info("{} taxes are supported by this fiscal registry", taxesCount);

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

        for (int i = 0; i < taxesCount; i++) {
            Arrays.fill(taxRates[i], (byte) 0);
        }

        if (taxes.size() > taxesCount) {
            log.warn("setTaxes(Map): the arguments cardinality ({}) is greater than the amount of taxes supported ({}) " +
                    "so the latter taxes (exceeding that amount) will be ignored", taxes.size(), taxesCount);
        }
        if (taxes.size() < taxesCount) {
            // вообще, нормально. Может, просто больше налоговых ставок не поддерживаются
            log.info("setTaxes(Map): the arguments cardinality ({}) is less than the amount of taxes supported ({}) " +
                    "so some taxes will be nullified", taxes.size(), taxesCount);
        }

        int rowNum = 1;
        for (Map.Entry<String, Long> entry : taxes.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();

            // 3.1. запишем ставку налога
            writeTable(getTaxRatesTableNo(), rowNum, getTaxRateValueFieldNo(), String.valueOf(value).getBytes());

            // 3.2. и название налога
            writeTable(getTaxRatesTableNo(), rowNum, getTaxNameFieldNo(), key.getBytes());

            rowNum++;

        }

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
    public void setCashierName(byte cashierNo, String cashierName) throws ShtrihException {
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
        useEncoding = true;
        writeTable(getCashiersTableNo(), DEFAULT_ADM_PASSWORD, getCashierNameFieldNo(), value);
        setCurrentUserNo(DEFAULT_ADM_PASSWORD);
        useEncoding = false;
        log.debug("leaving setCashierName(byte, String)");
    }

    @Override
    public void sendCashierInnIfNeeded(String inn) throws ShtrihException {
        if (getVersionFFD() == 2 && StringUtils.isNotBlank(inn)) {
            sendTLVData(TLVDataCommand.Tags.CASHIER_INN.getCode(), formatInn(inn));
        }
    }

    private String formatInn(String inn) {
        if (StringUtils.isBlank(inn)) {
            return inn;
        }
        return StringUtils.rightPad(inn, 12, ' ');
    }

    /**
     * Считаем таблицу 17 "Региональные настройки" Поле 17 "Формат ФД"
     *
     * @return версия ФФД:
     * 0 - Формат ФД 1.0 Beta
     * 1 - Формат ФД 1.0 New
     * 2 - Формат ФД 1.05
     * 52 - Формат ФД 1.2
     */
    @Override
    public int getVersionFFD() {
        return ffdVersion;
    }

    public void updateVersionFFD() throws ShtrihException {
        ffdVersion = (int) ShtrihUtils.getLong(ShtrihUtils.inverse(readTable(ShtrihTables.REGIONAL_SETTINGS_TABLE, (byte) 1,
                ShtrihTables.REGIONAL_SETTINGS_TABLE_FORMAT_FD_ROW)));
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

    private int getDeptCount() throws ShtrihException {
        if (deptCount != null) {
            return deptCount;
        }

        TableStructure tableStructure = getTableStructure(getDeptNamesTableNo());
        deptCount = tableStructure.getRowsCount();

        return deptCount;
    }

    @Override
    public void setDepartmentName(byte deptNo, String deptName) throws ShtrihException {
        log.info("entering setDepartmentName(byte, String). The arguments are: deptNo [{}], deptName [{}]", deptNo, deptName);

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

        log.info("leaving setDepartmentName(byte, String)");
    }

    /**
     * Инициализация нашего устройства: запись в него настроек.
     */
    protected void init() {
        StopTimer stopWatch = new StopTimer();

        log.info("entering init()");

        // 1. Считаем настройки:
        //  потом можно и другие форматы рассмотреть - и тогда ParametersReader
        //      надо будет получать через factory по имени файла (по его расширению)
        ParametersReader reader = new CsvBasedParametersReader(config.getParametersFilePath());
        Collection<ShtrihParameter> parameters = reader.readParameters();
        log.info("{} parameters were read", parameters.size());

        // 2. и запишем эти настройки
        for (ShtrihParameter sp : parameters) {
            useEncoding = true;
            writeParameter(sp);
            useEncoding = false;
        } // for sp


        try {
            updateSysAdminPassword();
        } catch (ShtrihException e) {
            log.error("Can not update sysAdminPassword", e);
        }

        try {
            fixWrongCashierPassword();
        } catch (ShtrihException e) {
            log.error("Can not fixWrongCashierPassword", e);
        }

        try {
            disableDocumentPrinting(false);
        } catch (ShtrihException e) {
            log.error("Can not disableDocumentPrinting", e);
        }
        log.info("leaving init(). it took {}", stopWatch);
    }

    private void updateSysAdminPassword() throws ShtrihException {
        log.info("entering updateSysAdminPassword()");
        ci.Set_SysAdminPassword(password);
        byte[] passwordData = readTable(ShtrihTables.CASHIER_AND_ADMINS_PASSWORDS, (byte) 30, (byte) 1);
        password = Integer.parseInt(new String(passwordData, ENCODING));
        log.info("leaving updateSysAdminPassword() password={}", password);
    }

    private void fixWrongCashierPassword() throws ShtrihException {
        log.info("entering fixWrongCashierPassword()");
        for (int i = 1; i <= 28; i++) {
            byte[] passwordData = readTable(ShtrihTables.CASHIER_AND_ADMINS_PASSWORDS, (byte) i, (byte) 1);
            int cashierPassword = Integer.parseInt(new String(passwordData, ENCODING));
            if (cashierPassword == currentUserNo) {
                cashierPassword = i;
                log.info("write cashierPassword {} for cashier {}", cashierPassword, i);
                writeTable(ShtrihTables.CASHIER_AND_ADMINS_PASSWORDS, (byte) i, (byte) 1, String.valueOf(cashierPassword).getBytes(ENCODING));
            }
        }
        log.info("leaving fixWrongCashierPassword()");

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
        } catch (ShtrihException t) {
            // возможно, редактирование этой настройки просто запрещено (не редактируемая настройка) - нет оснований вываливаться по exception'у
            log.error(String.format("writeParameter(ShtrihParameter): failed to write parameter: %s", sp), t);
        }
    }

    private String getSerialNumberFromTable() throws ShtrihException {
        return new String(readTable((byte) 18, 1, (byte) 1), ENCODING);
    }

    private String getRNMFromTable() throws ShtrihException {
        return new String(readTable((byte) 18, 1, (byte) 3), ENCODING);
    }

    /**
     * Вернет {@link ShtrihParameter#getValue() значение} указанной настройки в виде массива байт.
     * <p/>
     * NOTE: вализацию делать ДО вызова этого метода.
     *
     * @param parameter параметр, значение которого надо вернуть
     * @return массив байт длиной {@link ShtrihParameter#getFieldWidth()} содержащий значение настройки
     */
    private byte[] getValue(ShtrihParameter parameter) {
        byte[] result = new byte[parameter.getFieldWidth()];

        if (ShtrihFieldType.STRING.equals(parameter.getFieldType())) {
            // 1. преобразуем строковый параметр в массив байт:
            result = getBytes(parameter.getValue());
        } else {
            // это числовой параметр
            // 1. Преобразуем его в число:
            BigInteger value = new BigInteger(parameter.getValue());

            // 2. получим его "байтовое" представление (старшие быйты - вперед):
            byte[] valueAsArray = value.toByteArray();

            // 3. а теперь "перельем" это представление в "наш" формат (мл. байты - вперед):
            if (result.length < valueAsArray.length) {
                log.error("The actual \"width\" of the value of the parameter [{}] is greater than allowed: {} vs {}",
                        parameter, valueAsArray.length, result.length);
            }
            // все равно вернем - даже если придется урезать
            for (int i = 0; i < Math.min(result.length, valueAsArray.length); i++) {
                result[i] = valueAsArray[valueAsArray.length - 1 - i];
            } // for i

            result = String.valueOf(value).getBytes();
        }

        return result;
    }

    @Override
    public void close() {
        log.debug("entering close()");

        if (ci != null) {
            ci.Disconnect();
        }

        log.debug("leaving close()");
    }

    public int getSysAdminPassword() {
        return password;
    }

    private void sendTLVData(byte[] tag, String clientData) throws ShtrihException {
        if (clientData != null) {
            byte[] clientByteData = clientData.getBytes();
            byte[] bytesData = new byte[4 + clientByteData.length];
            int dataLenght = Integer.parseInt(Integer.toString(clientByteData.length), 16);
            byte[] bytesLenght = getRevertHexArray(dataLenght);
            System.arraycopy(tag, 0, bytesData, 0, 2);
            System.arraycopy(bytesLenght, 0, bytesData, 2, 2);
            System.arraycopy(clientByteData, 0, bytesData, 4, clientByteData.length);
            ci.Set_TLVData(bytesData);
            ci.FNSendTLV();
            throwExceptionIfError(ci);
        }
    }

    private byte[] getRevertHexArray(int hexData) {
        byte[] result = new byte[2];
        String data = String.valueOf(hexData);
        if (data.length() > 2) {
            result[0] = Byte.parseByte(data.substring(data.length() - 2));
            result[1] = Byte.parseByte(data.substring(0, data.length() - 2));
        } else {
            result[0] = Byte.parseByte(data);
            result[1] = (byte) 0;
        }
        return result;
    }

    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws ShtrihException {
        // начать форм. чека коррекции
        ci.FNBeginCorrectionReceipt();
        throwExceptionIfError(ci);
        // добавить TLV параметрами недостающие данные
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_REASON.getCode(), correctionReceipt.getReason());
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_DOC_NUMBER.getCode(), correctionReceipt.getReasonDocNumber());
        sendTLVData(TLVDataCommand.Tags.CORRECTION_RECEIPT_DATE.getCode(), correctionReceipt.getReasonDocDate().toString());
        sendCashierInnIfNeeded(Optional.ofNullable(cashier).map(Cashier::getInn).orElse(null));

        ShtrihCorrectionReceiptV2 shtrihCorrectionReceiptV2 = new ShtrihCorrectionReceiptV2(correctionReceipt);
        ci.Set_Summ1(shtrihCorrectionReceiptV2.getSumAll());
        ci.Set_CheckType(shtrihCorrectionReceiptV2.getCorrectionType());
        ci.FNBuildCorrectionReceipt();
        throwExceptionIfError(ci);
        long numFD = ci.Get_DocumentNumber();
        return Optional.of(numFD);
    }

    @Override
    public void cancelDocument() {
        try {
            ci.FNCancelDocument();
        } catch (Exception e) {
            // если пытались отменить документ, то уже беда и будет обработка
            log.error("Can't cancel document!");
        }
    }

    @Override
    public int getLastShiftNumber() throws ShtrihException {
        log.info("entering getLastShiftNumber()");
        int result;
        ci.GetRangeDatesAndSessions();
        throwExceptionIfError(ci);
        result = ci.Get_LastSessionNumber();
        log.info("leave getLastShiftNumber() result={}", result);
        return result;
    }

    @Override
    public ShiftNonNullableCounters getShiftNonNullableCounters() throws ShtrihException {
        log.info("entering getShiftNonNullableCounters()");
        ShiftNonNullableCounters result = new ShiftNonNullableCounters();
        // Необнуляемые суммы обновляются после снятия Z (поэтому в момент закрытия смены они содержат данные на начало смены)
        ci.FNGetNonClearableSumm();
        throwExceptionIfError(ci);
        result.setSumNonNullableSales(ci.Get_Summ1());
        //Драйвер путает сумму расхода и сумма возврата прихода, в документации указано наоборот.
        //При обновлении драйвера обратить внимание исправлено ли это.
        result.setSumNonNullablePurchases(ci.Get_Summ3());
        result.setSumNonNullableReturnSales(ci.Get_Summ2());
        result.setSumNonNullableReturnPurchases(ci.Get_Summ4());
        log.info("leave getShiftNonNullableCounters() result= {}", result);
        return result;
    }

    @Override
    public void printBarcodeBlock(BarCode barcode) {
        try {
            FontLine emptyLine = new FontLine("  ", Font.NORMAL);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
            String alignedLine = getAlignedLine(dateFormat.format(new Date()), ShtrihAlignment.CENTRE);
            FontLine fontLine = new FontLine(alignedLine, Font.NORMAL);
            printLine(fontLine);
            printLine(emptyLine);

            printEan13Barcode(barcode);

            // Печатаем пустые строки, чтобы цифры под ШК не отрезались от самого ШК
            printLine(emptyLine);
            printLine(emptyLine);

            printHeaderAndCut();
            throwExceptionIfError(ci);
        } catch (ShtrihException sre) {
            log.error("printBarcodeInner error!", sre);
        }
    }

    @Override
    public FiscalPrinterInfo getFiscalPrinterInfo() throws ShtrihException {
        ci.FNGetInfoExchangeStatus();
        throwExceptionIfError(ci);
        final FnInfo fnInfo = new FnInfo();
        final int notSentDocCount = ci.Get_MessageCount();
        fnInfo.setNotSentDocCount(notSentDocCount);
        if (notSentDocCount > 0) {
            final FnDocInfo firstNotSentDoc = new FnDocInfo();
            fnInfo.setFirstNotSentDoc(firstNotSentDoc);
            firstNotSentDoc.setNumber(ci.Get_DocumentNumber());
            try {
                final LocalTime time = DateConverters.toLocalTime(ci.Get_Time());
                final LocalDate date = DateConverters.toLocalDate(ci.Get_Date());
                if (time != null && date != null) {
                    firstNotSentDoc.setDate(time.atDate(date).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            } catch (Exception e) {
                log.debug("Error on parsing last doc dates", e);
            }
        }
        log.debug("FN info: {}", fnInfo);
        final FiscalPrinterInfo info = new FiscalPrinterInfo();
        info.setFnInfo(fnInfo);
        return info;
    }

    @Override
    public FiscalMarkValidationResult validateMarkCode(PositionEntity position, MarkData markData, boolean isSale)
            throws ShtrihException {
        if (getVersionFFD() != FFD_1_2) {
            return null;
        }
        String concatenatedMark = markData.getParser().concatMark(markData, GS);
        Map<String, Object> input = ImmutableMap.of(FiscalMarkValidationUtil.MARK_KEY, concatenatedMark);
        ci.Set_Password(getCurrentUserNo());
        ci.Set_BarCode(concatenatedMark);
        ci.Set_ItemStatus(FiscalMarkValidationUtil.formTag2003(position, isSale));
        ci.Set_CheckItemMode(0);
        ci.Set_TLVDataHex("");
        ci.FNCheckItemBarcode();
        throwExceptionIfError(ci);

        FiscalMarkValidationUtil.logFnCheckReason(log, ci.Get_CheckItemLocalError());

        int oismStatus = ci.Get_KMServerErrorCode();
        if (oismStatus == -1) {
            int tag2004 = ci.Get_CheckItemLocalResult();
            return FiscalMarkValidationResult.formForTag(input, tag2004);
        }
        int tag2106 = ci.Get_KMServerCheckingStatus();
        byte[] tlv = ci.Get_TLVData();
        Map<Integer, Long> parsedTlv = ShtrihUtils.parseTlv(tlv);

        ci.FNAcceptMarkingCode();
        throwExceptionIfError(ci);

        return new FiscalMarkValidationResult(input, tag2106, parsedTlv.get(2005).intValue(), parsedTlv.get(2105).intValue(),
                parsedTlv.get(2109).intValue());
    }

    private void printEan13Barcode(BarCode barcode) {
        ci.Set_BarCode(barcode.getValue());
        ci.PrintBarCode();
    }

    /**
     * Печать графики как картинки с шириной 512 px (картинку необходимо предварительно загрузить
     * через "Тест Драйвера" как "Загрузка графики в Буфер 512"
     * Сейчас используется команда драйвера штриха DrawEx() которая по доке печатае изображение,
     * но почему то она печатает картинку из буфера графика 512
     */
    public void printLogo() throws ShtrihException {
        printLogoBySize(config.getImageFirstLine(), config.getImageLastLine());
    }

    private void printLogoBySize(int imageFirstLine, int imageLastLine) throws ShtrihException {
        log.trace("entering printLogo({}, {})", imageFirstLine, imageLastLine);
        ci.Set_FirstLineNumber(imageFirstLine);
        ci.Set_LastLineNumber(imageLastLine);
        ci.DrawEx();
        throwExceptionIfError(ci);
        log.trace("leave printLogo()");
    }

    /**
     * Добавление дополнительных тегов к предмету расчета (позиции)
     */
    private void putAdditionalPositionInfo(AdditionalInfo info) throws ShtrihException {
        if (info == null) {
            return;
        }
        log.debug("Adding additional info: {}", info);
        final AgentType agentType = info.getAgentType();
        if (agentType != null) {
            sendTagOperation(1222, (byte) agentType.getBitMask());
        }
        if (StringUtils.isNotBlank(info.getDebitorINN())) {
            sendTagOperation(1226, formatInn(info.getDebitorINN()));
        }
        addDebitorInfo(info);
        addPaymentAgentInfo(info);
    }

    /**
     * Добавление тега "Данные поставщика" (1224) и его вложенных тегов
     */
    private void addDebitorInfo(AdditionalInfo info) throws ShtrihException {
        if (areAllNull(info.getDebitorPhone(), info.getDebitorName())) {
            return;
        }
        openParentTag(1224);
        addInnerTag(1225, info.getDebitorName());
        addInnerTag(1171, info.getDebitorPhone());
        closeParentTagOperation();
    }

    /**
     * Добавление тега "Данные (платежного) агента" (1223) и его вложенных тегов
     */
    private void addPaymentAgentInfo(AdditionalInfo info) throws ShtrihException {
        if (areAllNull(info.getTransferOperatorAddress(),
                info.getTransferOperatorInn(),
                info.getTransferOperatorName(),
                info.getTransferOperatorPhone(),
                info.getPaymentAgentOperation(),
                info.getPaymentAgentPhone(),
                info.getReceivePaymentsAgentPhone())) {
            return;
        }
        openParentTag(1223);
        addInnerTag(1005, info.getTransferOperatorAddress());
        addInnerTag(1016, formatInn(info.getTransferOperatorInn()));
        addInnerTag(1026, info.getTransferOperatorName());
        addInnerTag(1075, info.getTransferOperatorPhone());
        addInnerTag(1044, info.getPaymentAgentOperation());
        addInnerTag(1073, info.getPaymentAgentPhone());
        addInnerTag(1074, info.getReceivePaymentsAgentPhone());
        closeParentTagOperation();
    }

    /**
     * Открыть тег-контейнер
     */
    private void openParentTag(int tag) throws ShtrihException {
        log.trace("Adding container tag {}", tag);
        ci.Set_TagNumber(tag);
        ci.FNBeginSTLVTag();
        throwExceptionIfError(ci);
    }

    /**
     * Закрыть тег-контейнер и добавить его к операции (например, к добавлению позиции)
     */
    private void closeParentTagOperation() throws ShtrihException {
        ci.FNSendSTLVTagOperation();
        throwExceptionIfError(ci);
        log.trace("Adding container tag finished");
    }

    /**
     * Закрыть тег-контейнер - добавление в чек
     */
    private void closeParentTag() throws ShtrihException {
        ci.FNSendSTLVTag();
        throwExceptionIfError(ci);
        log.trace("Adding container tag for check finished");
    }

    /**
     * Добавить вложенный тег со строковым значением, если значение не пустое
     * <p>
     * Можно вызывать только между вызовами {@link #openParentTag(int)} и {@link #closeParentTagOperation()}}
     */
    private void addInnerTag(int tag, String value) throws ShtrihException {
        if (StringUtils.isNotBlank(value)) {
            log.trace("Adding inner tag {} with value {}", tag, value);
            ci.Set_TagNumber(tag);
            ci.Set_TagValueStr(value.trim());
            ci.Set_TagType(TAG_TYPE_STRING);
            ci.FNAddTag();
            throwExceptionIfError(ci);
        }
    }

    /**
     * Добавить тег с байтовым значением к чеку
     */
    private void sendTag(int tag, byte value) throws ShtrihException {
        log.trace("Adding tag {} with value {}", tag, value);
        ci.Set_TagNumber(tag);
        ci.Set_TagValueInt(BigInteger.valueOf(value));
        ci.Set_TagType(TAG_TYPE_BYTE);
        ci.FNSendTag();
        throwExceptionIfError(ci);
    }

    /**
     * Добавить тег со строковым значением к операции (например, к добавлению позиции)
     */
    private void sendTagOperation(int tag, String value) throws ShtrihException {
        log.trace("Adding operation tag {} with value {}", tag, value);
        ci.Set_TagNumber(tag);
        ci.Set_TagValueStr(value);
        ci.Set_TagType(TAG_TYPE_STRING);
        ci.FNSendTagOperation();
        throwExceptionIfError(ci);
    }

    /**
     * Добавить тег с байтовым значением к операции (например, к добавлению позиции)
     */
    private void sendTagOperation(int tag, byte value) throws ShtrihException {
        log.trace("Adding operation tag {} with value {}", tag, value);
        ci.Set_TagNumber(tag);
        ci.Set_TagValueInt(BigInteger.valueOf(value));
        ci.Set_TagType(TAG_TYPE_BYTE);
        ci.FNSendTagOperation();
        throwExceptionIfError(ci);
    }

    /**
     * Проверяет, что все переданные параметры пустые
     */
    private boolean areAllNull(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return false;
            }
        }
        return true;
    }

    public boolean isUseFontsFromTemplate() {
        return useFontsFromTemplate;
    }

    public void setUseFontsFromTemplate(boolean useFontsFromTemplate) {
        this.useFontsFromTemplate = useFontsFromTemplate;
    }
}
