package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import jpos.FiscalPrinterConst;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.check.DocumentNumber;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.AbstractFiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ReportCounters;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.WorkPlace;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusCFTDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Disc;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextStyle;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihExceptionConvertor;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.FiscalMemorySums;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihAlignment;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnector;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnectorFactory;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnectorProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDataStorage;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDeviceType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihEklzStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihModeEnum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShiftCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShortStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.DocumentUtils;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Общая реализация для всех Штрихов
 */
public class ShtrihMiniFrkFiscalPrinterServiceImpl extends AbstractFiscalPrinterPlugin implements FiscalPrinterConst, StatusUpdateListener, ErrorListener {

    /**
     * Видимо, какая-то устаревшая версия BIOS ФР. Требует каких-то "костыльных" действий по техпроцессу.
     */
    private static final String OBSOLETE_BIOS_VERSION = "A.4";

    protected static final int ITEM_PLACE = 6;
    protected static final String SPACE = " ";
    protected static final char SPACE_SYMBOL = ' ';
    /**
     * формат даты QR-кода Штриха - с двумя нулями
     */
    protected static final String QR_CODE_DATE_PATTERN = "yyyyMMdd'T'HHmm'00'";
    private static final String MULTIPLICATION = "x";
    private static final String EQUALS_SIGN = "=";
    private static final String INDENT = "         ";
    private static final String INDENT_3 = "       ";
    private static final String INDENT_2 = "      ";
    private static final String INDENT_4 = "     ";
    public static final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    private static final float NDS_18 = 18.0f;
    private static final float NDS_20 = 20.0f;
    private static final float NDS_18_118 = -18.0f;
    private static final float NDS_20_120 = -20.0f;

    private BaseShtrihConfig config;
    @Autowired
    private Properties cashProperties;
    /**
     * [текущий] Номер отдела, в котором пробиваются документы
     */
    private byte deptNo = 1;

    private String deviceName;
    protected ShtrihDataStorage regDataStorage;

    protected String separator;
    private final char DASH = '-';

    /**
     * Реализация протокола обмена с ФР семейства "Штрих"
     */
    protected ShtrihConnector connector;

    /**
     * Реальные налоги из ФР
     */
    protected ValueAddedTaxCollection taxes;

    /**
     * Хранит версию ПО подключенного в данным момент ФР
     */
    private String biosVersion = null;

    protected void setConfig(BaseShtrihConfig config) {
        this.config = config;
    }

    @Override
    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public void start() throws FiscalPrinterException {
        LOG.trace("entering start()");

        separator = getSeparator();

        if (config.getJposName() == null) {
            FiscalPrinterConfigException e = new FiscalPrinterConfigException(
                    ResBundleFiscalPrinterShtrihMiniFrk.getString("ERROR_CONFIG"),
                    CashErrorType.FATAL_ERROR
            );
            LOG.error("start() failure", e);
            throw e;
        }

        connector = ShtrihConnectorFactory.createConnector(config.getJposName(), initConnectorProperties());
        ShtrihStateDescription deviceState = null;
        ShtrihDeviceType deviceType = null;
        try {
            connector.open();
            // 1. получить состояние устройства
            deviceState = connector.getState();
            // 2. сохраняем регистационные данные ККТ
            regDataStorage = new ShtrihDataStorage();
            regDataStorage.setDataFromState(deviceState);

            // 3. получить тип устройства 0xFC
            deviceType = connector.getDeviceType();
        } catch (Exception e) {
            logExceptionAndThrowIt("start()", e);
        }

        LOG.trace("leaving start(). Device type is: {}; and the device state is: {}", deviceType, deviceState);
    }

    protected ShtrihConnectorProperties initConnectorProperties() {
        ShtrihConnectorProperties properties = new ShtrihConnectorProperties();
        properties.setPortName(config.getPort());
        properties.setBaudRate(config.getBaudRate());
        properties.setIpAddress(config.getIpAddress());
        properties.setTcpPort(config.getTcpPort());
        properties.setNeedRevertBytes(config.isNeedRevertBytes());
        properties.setParametersFilePath(config.getParametersFilePath());
        properties.setMaxCharsInRow(config.getMaxCharRow());
        properties.setPrintStringFont(config.getPrintStringFont());
        properties.setBarcodeHeight(config.getBarcodeHeight());
        properties.setHighQualityGraphics(config.isHighQualityGraphics());
        properties.setPrintLineTime(config.getPrintLineTime());
        properties.setMaxScale(config.getMaxBarcodeScaleFactor());
        properties.setMaxLoadGraphicsLines(config.getMaxLoadGraphicsLines());
        properties.setImageFirstLine(config.getImageFirstLine());
        properties.setImageLastLine(config.getImageLastLine());
        properties.setPrintLegalEntityHeader(cashProperties.isPrintLegalEntityHeader());
        return properties;
    }

    /**
     * Вернет описание подключенного ФР.
     *
     * @return не <code>null</code>
     */
    private ShtrihDeviceType getDeviceType() throws FiscalPrinterException {
        try {
            return connector.getDeviceType();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getDeviceType()", e);
        }
        throw new FiscalPrinterException("Failed to get device type");
    }

    protected String getSeparator() {
        StringBuilder separatorBuilder = new StringBuilder();
        for (int i = 0; i < config.getMaxCharRow(); i++) {
            separatorBuilder.append(DASH);
        }
        return separatorBuilder.toString();
    }

    private void logInfo(String msg) {
        LOG.info(msg);
    }

    @Override
    public void stop() throws FiscalPrinterException {
        LOG.trace("entering stop()");

        // 1. аннулировать открытый документ:
        try {
            connector.annul();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("postProcessNegativeScript(Exception)", e);
        }

        // и, наконец, закрыть коннекцию
        connector.close();

        LOG.trace("leaving stop()");
    }

    public void annulDocument() throws FiscalPrinterException {
        LOG.info("entering annulDocument()");

        // 1. аннулировать текущий документ. если надо
        try {
            connector.annul();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("postProcessNegativeScript(Exception)", e);
        }

        LOG.info("leaving annulDocument()");
    }

    @Override
    public Date getDate() throws FiscalPrinterException {
        LOG.debug("entering getDate()");
        Date result;
        ShtrihStateDescription state = getState();
        result = state.getCurrentTime();
        if (LOG.isDebugEnabled()) {
            LOG.debug("leaving getDate(). The result is: {}", String.format("%1$tF %1$tT", result));
        }
        return result;
    }

    /**
     * Вернет состояние фискальника.
     *
     * @return {@link ShtrihStateDescription [полное] состояние фискальника}; не вернет <code>null</code> - в крайнем случае будет выброшен exception
     * @throws FiscalPrinterException при воникновении ошибок инфо-обмена
     */
    protected ShtrihStateDescription getState() throws FiscalPrinterException {
        try {
            return connector.getState();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getState()", e);
        }
        throw new FiscalPrinterException("Failed to get printer state (null)");
    }

    /**
     * Вернет краткую информацию о состоянии фискальника.
     *
     * @return {@link ShtrihShortStateDescription [краткое] состояние фискальника}; не вернет <code>null</code> - в крайнем случае будет выброшен exception
     * @throws FiscalPrinterException при воникновении ошибок инфо-обмена
     */
    private ShtrihShortStateDescription getShortState() throws FiscalPrinterException {
        try {
            return connector.getShortState();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getShortState()", e);
        }
        throw new FiscalPrinterException("Failed to get short printer state (null)");
    }

    /**
     * Логгирует указанную ошибку, возникшую при работе с фискальником, конвертит ее в {@link FiscalPrinterException понятный техпроцессу вид} и
     * выбрасывает.
     *
     * @param methodName название метода, при обработке которого получили этот exception
     * @param e          само исключение, что словили
     * @throws FiscalPrinterException всегда выбрасывается. В крайнем случае будет выброшен {@link FiscalPrinterException} с сообщегнием о том, что произошла неизвестная
     *                                ошибка
     */
    protected void logExceptionAndThrowIt(String methodName, Exception e) throws FiscalPrinterException {
        LOG.error(methodName + " failure", e);
        FiscalPrinterException fpe = ShtrihExceptionConvertor.convert(e);
        throw fpe == null ? new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("UNKNOWN_ERROR"), ShtrihErrorMsg.getErrorType()) : fpe;
    }

    protected FiscalPrinterException logExceptionAndMakeFPE(String methodName, Exception e)  {
        LOG.error("{} failure", methodName, e);
        FiscalPrinterException fpe = ShtrihExceptionConvertor.convert(e);
        if (fpe != null) {
            return fpe;
        }
        return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("UNKNOWN_ERROR"), ShtrihErrorMsg.getErrorType());
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        LOG.trace("entering getEklzNum()");

        String result = "";
        ShtrihEklzStateOne eklzState = getEklzStateOne();
        result += eklzState.getEklzNum();

        LOG.trace("leaving getEklzNum(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет {@link ShtrihEklzStateOne состояние по коду 1 ЭКЛЗ} подключенного ФР.
     *
     * @return не <code>null</code> - в крайнем случае будет выброшен Exception
     * @throws FiscalPrinterException при воникновении ошибок инфо-обмена
     */
    private ShtrihEklzStateOne getEklzStateOne() throws FiscalPrinterException {
        try {
            return connector.getEklzStateOne();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getEklzStateOne()", e);
        }
        throw new FiscalPrinterException("Failed to get EKLZ state");
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        long result;

        LOG.trace("entering getLastKpk()");

        // номер КПК хранится в состоянии самого ФР
        result = getState().getCurrentDocNo();

        LOG.trace("leaving getLastKpk(). The result is: {}", result);

        return result;
    }

    @Override
    public PrinterState getPrinterState() throws FiscalPrinterException {
        PrinterState result = new PrinterState();

        LOG.debug("entering getPrinterState()");

        ShtrihShortStateDescription ss = getShortState();
        ShtrihFlags flags = ss.getFlags();
        if (flags == null) {
            LOG.error("leaving getPrinterState(): the shtrih flags is NULL!");
            return result;
        }
        if (flags.isCaseCover()) {
            result.addDescription("Открыта крышка принтера");
            result.setState(PrinterState.State.OPEN_COVER);
            result.setLongState(1L);
        }
        // отсутствие бумаги более приоритено?
        if (!flags.isRibbonSensor()) {
            result.addDescription("В принтере нет бумаги");
            result.setState(PrinterState.State.END_PAPER);
            result.setLongState(1L);
        }

        LOG.debug("leaving getPrinterState(). The result is: {}", result);

        return result;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * NOTE: Возвращает заводской номер ФР. Чтоб получить Рег. номер
     * нужно использовать getRegNumCorrect() из класса ShtrihServiceFN100
     * <p/>
     */
    @Override
    public String getRegNum() throws FiscalPrinterException {
        LOG.debug("entering getRegNum()");

        if (regDataStorage.isRegistrationNumEmpty()) {
            ShtrihStateDescription state = getState();
            regDataStorage.setRegistrationNum("" + state.getDeviceNo());
        }

        LOG.debug("leaving getRegNum(). The result is: {}", regDataStorage.getRegistrationNum());

        return regDataStorage.getRegistrationNum();
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        LOG.debug("entering getFactoryNum()");

        if (regDataStorage.isFactoryNumEmpty()) {
            ShtrihStateDescription state = getState();
            regDataStorage.setDataFromState(state);
        }

        LOG.debug("leaving getFactoryNum(). The result is: {}", regDataStorage.getFactoryNum());

        return regDataStorage.getFactoryNum();
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        ShiftCounters result;

        LOG.debug("entering getShiftCounters()");

        ShtrihShiftCounters counters = null;
        try {
            counters = connector.getShiftCounters();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getShiftCounters()", e);
        }

        if (counters == null) {
            throw new RuntimeException("Failed to get counters");
        }
        result = new ShiftCounters();
        result.setSumCashEnd(counters.getCashSum());
        result.setSumSale(counters.getSumSale());
        result.setSumReturn(counters.getSumReturn());
        result.setSumExpenseReceipt(counters.getSumExpense());
        result.setSumReturnExpenseReceipt(counters.getSumReturnExpense());
        result.setCountSale(counters.getCountSale());
        result.setCountReturn(counters.getCountReturn());
        result.setCountExpenseReceipt(counters.getCountExpense());
        result.setCountReturnExpenseReceipt(counters.getCountReturnExpense());

        LOG.debug("leaving getShiftCounters(). The result is: {}", result);

        return result;
    }

    /**
     * Метод возвращает номер последней закрытой смены + 1. ВСЕГДА.
     */
    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        long result;

        LOG.debug("entering getShiftNumber()");

        ShtrihStateDescription state = getState();
        result = state.getLastClosedShiftNo();

        // ВСЕГДА возвращаем номер посленей закрытой смены + 1:
        result++;

        LOG.debug("leaving getShiftNumber(). The result is: {}", result);

        return result;
    }

    @Override
    public String getVerBios() throws FiscalPrinterException {
        String result;

        boolean foundInMemory = false;
        if (biosVersion != null) {
            foundInMemory = true;
        } else {
            ShtrihStateDescription state = getState();
            // Версия прошивки не изменяется, для ее идентификации используется дата
            SimpleDateFormat dateFormat = new SimpleDateFormat(" dd.MM.yyyy");
            String softwareDateStr = dateFormat.format(state.getSoftwareReleaseDate());
            biosVersion = state.getSoftwareVersion() + softwareDateStr;
        }
        result = biosVersion;

        LOG.debug("BIOS version ({}) is: \"{}\"", foundInMemory ? "in-memory" : "read", result);

        return result;
    }

    @Override
    public boolean isMoneyDrawerOpen() throws FiscalPrinterException {
        boolean result = false;

        LOG.debug("entering isMoneyDrawerOpen()");

        ShtrihShortStateDescription ss = getShortState();
        if (ss.getFlags() != null && ss.getFlags().isCashDrawer()) {
            result = true;
        }

        LOG.debug("leaving isMoneyDrawerOpen(). The result is: {}", result);

        return result;
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        boolean result;

        LOG.trace("entering isShiftOpen()");

        ShtrihStateDescription state = getState();
        ShtrihModeEnum stateEnum = state.getMode().getStateNumber();

        // смену считаем открытой, если она не закрыта
        result = !ShtrihModeEnum.SHIFT_IS_CLOSED.equals(stateEnum);

        LOG.trace("leaving isShiftOpen(). The result is: {}", result);

        return result;
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        LOG.info("entering openMoneyDrawer()");

        // всегда открываем 0й денежный ящик
        try {
            connector.openCashDrawer((byte) 0);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("openMoneyDrawer()", e);
        }

        LOG.info("leaving openMoneyDrawer()");
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        long result;

        LOG.debug("entering openShift(Cashier). the argument is: cashier [{}]", cashier);

        // 1. запишем в ФР имя кассира, открывшего смену:
        setCashierName(cashier == null ? null : getCashierName(cashier));

        // 2. и вернем номер [типа] текущей смены:
        result = getShiftNumber();

        LOG.debug("leaving openShift(Cashier). the result is: {}", result);

        return result;
    }

    protected void setCashierName(String name) throws FiscalPrinterException {
        LOG.debug("entering setCashierName(String). The argument is: name [{}]", name);

        // всегда редактируем только 1го кассира:
        try {
            connector.setCashierName((byte) 1, name);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("setCashierName(String)", e);
        }

        LOG.debug("leaving setCashierName(String)");
    }

    /**
     * Регистрация указанной позиции ПРОДАЖИ в ФР.
     *
     * @param position позиция продажи, что надо зарегистрировать
     */
    protected void regSalePosition(ShtrihPosition position) throws FiscalPrinterException {
        try {
            connector.regSale(position);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("regSalePosition(ShtrihPosition)", e);
        }
    }

    /**
     * Регистрация указанной позиции ВОЗВРАТА в ФР.
     *
     * @param position позиция возврата, что надо зарегистрировать
     */
    protected void regRetPosition(ShtrihPosition position) throws FiscalPrinterException {
        try {
            connector.regReturn(position);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("regRetPosition(ShtrihPosition)", e);
        }
    }

    /**
     * Регистрация указанной скидки в ФР.
     *
     * @param discount скидка, что надо зарегистрировать
     */
    protected void regDiscount(ShtrihDiscount discount) throws FiscalPrinterException {
        try {
            connector.regDiscount(discount);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("regDiscount(ShtrihDiscount)", e);
        }
    }

    /**
     * Регистрация указанной наценки в ФР.
     *
     * @param discount скидка, что надо зарегистрировать
     */
    protected void regMargin(ShtrihDiscount discount) throws FiscalPrinterException {
        try {
            connector.regMargin(discount);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("regDiscount(ShtrihDiscount)", e);
        }
    }

    /**
     * Регистрация указанного чека в ФР.
     *
     * @param receiptTotal итог чека, что надо зарегистрировать
     */
    protected void closeReceipt(ShtrihReceiptTotal receiptTotal) throws FiscalPrinterException {
        try {
            connector.closeReceipt(receiptTotal);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("closeReceipt(ShtrihReceiptTotal)", e);
        }
    }

    /**
     * Распечататет шапку следующего документа и отрежет чековую ленту
     *
     * @param document текущий документ
     * @throws FiscalPrinterException
     */
    protected void closeNonFiscalDocument(AbstractDocument document) throws FiscalPrinterException {
        try {
            connector.closeNonFiscalDocument(document);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("closeNonFiscalDocument(AbstractDocument)", e);
        }
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {

        LOG.debug("entering printCheck(Check)");

        // 1. как обычно записываем кассира и номер отдела в фискальник перед фискализацией документа:
        setCashierName(getCashierName(check.getCashier()));
        setDepartNumber(check.getDepart().intValue());

        // 2. печать заголовка
        printSubHeader(check.getCashNumber(), check.getCheckNumber(), check.getShiftId(), check.getCashier());
        printLine(new FontLine(separator, Font.NORMAL));

        // 3. Печать товаров со скидками и налогами, если они есть
        long subTotal = 0;
        final int maxCharRow = config.getMaxCharRow();
        for (Goods product : check.getGoods()) {
            // 3.1. регистрация позиции
            String positionName = formatLeft(product.getItem(), ITEM_PLACE) + formatLeft(product.getName(), maxCharRow - ITEM_PLACE);
            positionName = StringUtils.left(positionName, maxCharRow);
            ShtrihPosition position = new ShtrihPosition(positionName, product.getStartPricePerUnit(),
                    product.getQuant(), check.getDepart().byteValue());
            position.setTaxOne(getTaxId(product.getTax()));
            if (CheckType.RETURN.equals(check.getType())) {
                // это позиция из чека ВОЗВРАТА - регистрируем возврат:
                regRetPosition(position);
            } else {
                // иначе считаем, что это чек ПРОДАЖИ
                regSalePosition(position);
            }

            // 3.2. регистрация скидки на эту позицию
            long discValue = 0;
            if (product.getDiscs() != null) {
                discValue = this.getDiscValue(product.getDiscs());
                if (discValue != 0) {
                    ShtrihDiscount discount = new ShtrihDiscount(discValue, "");
                    regDiscount(discount);
                }
            }

            long sum = (Math.round((product.getStartPricePerUnit() * product.getQuant() / 1000.0))) - discValue;
            subTotal += sum;
        } // for product
        printLine(new FontLine(separator, Font.NORMAL));

        // 4. Печать подитога
        printSubTotal(subTotal);

        long checkDiscValueTotal;
        if (getDiscValue(check.getDiscs()) != 0) {
            checkDiscValueTotal = getDiscValue(check.getDiscs());
            ShtrihDiscount discount = new ShtrihDiscount(checkDiscValueTotal, ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TITLE"));
            regDiscount(discount);
        }

        checkDiscValueTotal = getDiscValue(check.getDiscs());
        if (check.getGoods() != null) {
            for (Goods goods : check.getGoods()) {
                checkDiscValueTotal += getDiscValue(goods.getDiscs());
            }
        }

        // Печать "ИТОГО СКИДКА НА ЧЕК СОСТАВИЛА"
        if ((checkDiscValueTotal == 0) && (check.getDiscountValueTotal() != null)) {
            checkDiscValueTotal = check.getDiscountValueTotal();
        }
        if (checkDiscValueTotal > 0) {
            String line;
            line = DocumentUtils.makeHeader(ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TEXT"), maxCharRow, SPACE_SYMBOL);
            printLine(new FontLine(line, Font.NORMAL));

            line = ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT") +
                    formatRight(BigDecimal.valueOf(checkDiscValueTotal / 100.0).setScale(2, RoundingMode.HALF_UP).toString(),
                            maxCharRow / 2 - ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT").length());
            printLine(new FontLine(line, Font.DOUBLEWIDTH));
        }

        printLine(new FontLine(SPACE, Font.NORMAL));
        String line = DocumentUtils.makeHeader(ResBundleFiscalPrinterShtrihMiniFrk.getString("GRATITUDE"), maxCharRow, SPACE_SYMBOL);
        printLine(new FontLine(line, Font.NORMAL));

        line = DocumentUtils.makeHeader(ResBundleFiscalPrinterShtrihMiniFrk.getString("OPEN_TIME"), maxCharRow, SPACE_SYMBOL);
        printLine(new FontLine(line, Font.NORMAL));

        // Пропуск строки
        printLine(new FontLine(SPACE, Font.NORMAL));

        if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
            printCheckBarcode(check);
        }

        if (check.isAnnul()) {
            annulDocument();
        } else {
            Map<Integer, Long> payments = getPayments(check);

            // закрыть/зарегистрировать чек
            long totalSum = check.getCheckSumEnd();
            ShtrihReceiptTotal receiptTotal = new ShtrihReceiptTotal("", totalSum);
            // оплаты по типам:
            // нал
            if (payments.get(0) != null) {
                receiptTotal.setCashSum(payments.get(0));
            }
            // безнал
            if (payments.get(1) != null) {
                receiptTotal.setSecondPaymentTypeSum(payments.get(1));
            }
            // налоги и скидки в ФР пишем по нулям

            closeReceipt(receiptTotal);

            // Осталось распечатать шапку следующего документа и отрезать чековую ленту
            closeNonFiscalDocument(check);
        }

        LOG.debug("leaving printCheck(Check)");
    }

    /**
     * Печать "Подитога".
     *
     * @param subTotal сумма подитога
     */
    protected void printSubTotal(long subTotal) throws FiscalPrinterException {
        // По сути здесь надо:
        // 1. распечатать строку да строки с "ПОДИТОГ" - пока (2015-01-25) не надо и не настраивается

        // 2. распечатать саму строку с текстом "ПОДИТОГ"
        //  2.1. само слово "ПОДИТОГ"
        String subTotalText = ResBundleFiscalPrinterShtrihMiniFrk.getString("shtrih.subtotal.text");

        // 2.2. сумма в виде строки
        String sumText = "=" + CurrencyUtil.formatSum(subTotal);

        // 2.3. теперь слово "Подитог" выравниваем по левому краю, а сумму - по правому:
        //  Печатаем нормальным шрифтом. т.е., количество символов в строке == maxCharRow
        int maxCharRow = config.getMaxCharRow();
        if (maxCharRow < subTotalText.length() + sumText.length()) {
            // текст не помещается - не будем печатать
            LOG.error("Subtotal text cannot be printed 'cause total length of \"{}\" and \"{}\" is greater than the line length ({})",
                    subTotalText, sumText, maxCharRow);
            return;
        }
        int spacesCount = maxCharRow - subTotalText.length() - sumText.length();
        String spaceText = StringUtils.repeat(" ", spacesCount);
        String finalText = subTotalText + spaceText + sumText;

        try {
            connector.printLine(new FontLine(finalText, Font.NORMAL));
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getState()", e);
        }

        // 3. строку после "ПОДИТОГ" - пока (2015-01-25) не надо и не настраивается
    }

    protected void setDepartNumber(int number) throws FiscalPrinterException {
        LOG.debug("entering setDepartNumber(int). The argument is: number [{}]", number);

        // 1. поддерживаются только номера отделов с 1..16
        byte validNumber = (byte) number;
        if (number > 16 || number < 1) {
            // какой-то левый номер отдела прислали. значит, будет 1й отдел
            validNumber = 1;
        }

        // 2. сохраним в оперативке текущий номер отдела:
        deptNo = validNumber;

        // 3. если у нас отсталая версия BIOS и прислали левый номер отдела, то надо в ФР название 1го отдела изменить
        if (validNumber != number && OBSOLETE_BIOS_VERSION.equalsIgnoreCase(getVerBios())) {
            LOG.info("setting name of dept #1 to \"{}\"", validNumber);
            try {
                connector.setDepartmentName(validNumber, "" + validNumber);
            } catch (ShtrihException | IOException | PortAdapterException e) {
                logExceptionAndThrowIt("setDepartNumber(int)", e);
            }
        }
        LOG.debug("leaving setDepartNumber(int)");
    }

    protected byte getTaxId(float taxValue) throws FiscalPrinterException {
        ValueAddedTax tax = getTax(taxValue);
        if (tax == null) {
            throw new FiscalPrinterException("ERROR: Tax type not found!");
        }
        return (byte) (tax.index + 1); // в штрихе нумерация налогов начинается с 1
    }

    private ValueAddedTax getTax(float taxValue) {
        ValueAddedTax tax = taxes.lookupByValue(taxValue);
        // Сделано на переходный период (НДС 20%), когда возможно ФР не перепрошит и в нем старые налоги, пробуем найти индекс по ним.
        if (tax == null) {
            if (taxValue == NDS_20) {
                tax = taxes.lookupByValue(NDS_18);
            } else if (taxValue == NDS_20_120) {
                tax = taxes.lookupByValue(NDS_18_118);
            }
        }
        return tax;
    }

    protected void printSubHeader(Long posId, Long checkNumber, Long shiftId, Cashier cashier) throws NumberFormatException, FiscalPrinterException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("entering printSubHeader(Long, Long, Long, Cashier). " +
                            "The arguments are: posId [{}], checkNumber [{}], shiftId [{}], cashier [{}]",
                    posId, checkNumber, shiftId, cashier);
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat df1 = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        if (posId == null) {
            posId = 0L;
        }
        if (checkNumber == null) {
            checkNumber = 0L;
        }
        if (shiftId == null) {
            shiftId = 0L;
        }
        if (cashier == null) {
            cashier = new Cashier();
        }
        if (cashier.getName() == null) {
            cashier.setName("Robot");
        }
        if (cashier.getTabNum() == null) {
            cashier.setTabNum("0");
        }
        int maxCharRow = config.getMaxCharRow();
        String line = ResBundleFiscalPrinterShtrihMiniFrk.getString("CASH_NUMBER_2") + String.format("%03d", posId) +
                getTab(maxCharRow - (ResBundleFiscalPrinterShtrihMiniFrk.getString("CASH_NUMBER_2").length() + 3 +
                        ResBundleFiscalPrinterShtrihMiniFrk.getString("RECEIPT_NUMBER").length() + 8)) +
                ResBundleFiscalPrinterShtrihMiniFrk.getString("RECEIPT_NUMBER") + String.format("%8s", checkNumber);
        printLine(new FontLine(line, Font.NORMAL));

        line = ResBundleFiscalPrinterShtrihMiniFrk.getString("SHIFT_ID") + String.format("%8s", shiftId) +
                getTab(maxCharRow - (ResBundleFiscalPrinterShtrihMiniFrk.getString("SHIFT_ID").length() + 8 +
                        ResBundleFiscalPrinterShtrihMiniFrk.getString("DATE").length() + 10)) +
                ResBundleFiscalPrinterShtrihMiniFrk.getString("DATE") + df.format(date);
        printLine(new FontLine(line, Font.NORMAL));

        line = ResBundleFiscalPrinterShtrihMiniFrk.getString("REGISTER_NUMBER") + String.format("%016d", Long.parseLong(getRegNum())) +
                getTab(maxCharRow - (ResBundleFiscalPrinterShtrihMiniFrk.getString("REGISTER_NUMBER").length() + 16 +
                        ResBundleFiscalPrinterShtrihMiniFrk.getString("TIME").length() + 8)) +
                ResBundleFiscalPrinterShtrihMiniFrk.getString("TIME") + String.format("%8s", df1.format(date));
        printLine(new FontLine(line, Font.NORMAL));

        line = ResBundleFiscalPrinterShtrihMiniFrk.getString("CASHIER") +
                formatLeft(getCashierName(cashier), maxCharRow - ResBundleFiscalPrinterShtrihMiniFrk.getString("CASHIER").length());
        printLine(new FontLine(line, Font.NORMAL));

        LOG.debug("leaving printSubHeader(Long, Long, Long, Cashier)");
    }

    private String getTab(int width) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < width; j++) {
            sb.append(SPACE);
        }
        return sb.toString();
    }

    protected long getDiscValue(List<Disc> discs) throws FiscalPrinterException {
        long discValue = 0;
        if (discs != null) {
            List<Disc> goodsDiscs = this.convertDisc(discs);
            List<DiscType> goodsDiscTypes = this.getGoodsDiscTypes(goodsDiscs);
            // СКИДКА ПРОЦЕНТНАЯ
            if (goodsDiscTypes.contains(DiscType.PERCENT)) {
                throw new FiscalPrinterException("ErrorDiscountTypeValue");
            }
            // СКИДКА СУММОВАЯ
            if (goodsDiscTypes.contains(DiscType.SUMMA)) {
                for (Disc disc : discs) {
                    if (disc.getType() == DiscType.SUMMA) {
                        discValue += disc.getValue();
                    }
                }
            }
        }
        return discValue;
    }

    private void printBarcode(BarCode barcode) {
        LOG.debug("entering printBarcode(BarCode). The arguments are: barcode [{}]", barcode);

        try {
            connector.printBarcode(barcode, barcode == null ? null : barcode.getBarcodeLabel(), ShtrihAlignment.CENTRE);
        } catch (Throwable t) {
            LOG.error(String.format("printBarcode(BarCode) FAILED. The arguments were: barcode [%s]", barcode), t);
        }

        LOG.debug("leaving printBarcode(BarCode)");
    }

    // только с двумя скидками
    private List<Disc> convertDisc(List<Disc> discounts) {
        List<DiscType> types = new ArrayList<>();
        types.add(DiscType.PERCENT);
        types.add(DiscType.SUMMA);

        List<Disc> discs = new ArrayList<>();

        for (DiscType type : types) {
            long discValue = 0;
            for (Disc disc : discounts) {
                if (type == disc.getType()) {
                    discValue += disc.getValue();
                }
            }
            if (discValue != 0) {
                Disc discObject = new Disc();
                discObject.setType(type);
                discObject.setValue(discValue);

                discs.add(discObject);
            }
        }
        return discs;
    }

    private List<DiscType> getGoodsDiscTypes(List<Disc> discounts) {
        List<DiscType> types = new ArrayList<>();

        for (Disc disc : discounts) {
            types.add(disc.getType());
        }

        return types;
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        LOG.debug("entering printMoneyDocument(Money). The arguments are: money [{}]", money);

        setCashierName(getCashierName(money.getCashier()));
        setDepartNumber(money.getDepart().intValue());
        printSubHeader(money.getCashNumber(), money.getCheckNumber(), getShiftNumber(), money.getCashier());
        printLine(new FontLine(separator, Font.NORMAL));

        //  общая сумма внесения/изъятия
        //  значение именно этой переменной будет в конечном итоге передано в фискальный регистратор
        long total = money.getValue();

        //  далее только печатаем необходимые строки шаблона внесения/изъятия и передаем соответствующее значение в ФР
        //  все расчеты уже выполнены
        if (InventoryOperationType.CASH_IN.equals(money.getOperationType())) {
            String totalInCurrencyRow = INDENT_2 + money.getCurrency() + INDENT_3 + CurrencyUtil.convertMoney(total);
            printLine(new FontLine(totalInCurrencyRow, Font.NORMAL));

            String totalTitle = INDENT + ResBundleFiscalPrinterShtrihMiniFrk.getString("SUMMA") + money.getCurrency() +
                    formatRight(EQUALS_SIGN + CurrencyUtil.convertMoney(total), 17);
            printLine(new FontLine(totalTitle, Font.NORMAL));
            printLine(new FontLine(separator, Font.NORMAL));
            printLine(new FontLine(ResBundleFiscalPrinterShtrihMiniFrk.getString("TOTAL_CASH_IN_2") + CurrencyUtil.convertMoney(total), Font.NORMAL));

            //  регистрируем внесение в ФР
            registerCashIn(total);
        } else if (InventoryOperationType.CASH_OUT.equals(money.getOperationType())) {
            String currencyRow = ResBundleFiscalPrinterShtrihMiniFrk.getString("CURRENCY") + money.getCurrency();
            printLine(new FontLine(currencyRow, Font.NORMAL));

            //  печатаем расшифровку по банкнотам
            for (BankNote bn : money.getBankNotes()) {
                long value = bn.getValue();
                long count = bn.getCount();
                long sum = value * count;

                String bankNoteRow = INDENT + formatLeft(CurrencyUtil.convertMoney(value) + MULTIPLICATION + bn.getCount().doubleValue(), 15) + EQUALS_SIGN +
                        CurrencyUtil.convertMoney(sum);
                printLine(new FontLine(bankNoteRow, Font.NORMAL));
            }

            //  печатаем сумму сумму монет
            if (money.getSumCoins() != null) {
                String coinsRow = INDENT + formatLeft(ResBundleFiscalPrinterShtrihMiniFrk.getString("COINS"), 15) + EQUALS_SIGN + CurrencyUtil.convertMoney(money
                        .getSumCoins());
                printLine(new FontLine(coinsRow, Font.NORMAL));
            }

            String totalTitleRow = INDENT_4 + ResBundleFiscalPrinterShtrihMiniFrk.getString("SUMMA") + money.getCurrency() + INDENT + EQUALS_SIGN + CurrencyUtil
                    .convertMoney(total);
            printLine(new FontLine(totalTitleRow, Font.NORMAL));
            printLine(new FontLine(separator, Font.NORMAL));
            printLine(new FontLine(ResBundleFiscalPrinterShtrihMiniFrk.getString("TOTAL_CASH_OUT_2") + CurrencyUtil.convertMoney(total), Font.NORMAL));

            //  регистрируем изъятие в ФР
            registerCashOut(total);
        }

        LOG.debug("leaving printMoneyDocument(Money)");

    }

    protected String formatRight(String text, int length) {
        return StringUtils.leftPad(StringUtils.right(text, length), length);
    }

    protected String formatLeft(String text, int length) {
        return StringUtils.rightPad(StringUtils.left(text, length), length);
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        LOG.debug("entering printServiceDocument(SimpleServiceDocument, String). " +
                "The arguments are: serviceDocument [{}]", serviceDocument);

        // 1. как обычно записываем кассира и номер отдела в фискальник перед печатью документа:
        setCashierName(getCashierName(serviceDocument.getCashier()));
        setDepartNumber(serviceDocument.getDepart().intValue());

        printLine(new FontLine(separator, Font.NORMAL));

        for (Row row : serviceDocument.getRows()) {
            if (row instanceof BarCode) {
                printBarcode((BarCode) row);
            } else if (row instanceof Text) {
                printRow((Text) row);
            }
        }

        // ради обрезания чековой ленты и печати заголовка следующего документа
        closeNonFiscalDocument(serviceDocument);
        LOG.debug("leaving printServiceDocument(SimpleServiceDocument, String)");
    }

    private void printRow(Text text) throws FiscalPrinterException {
        LOG.trace("entering printRow(Row). The argument is: tetx [{}]", text);

        Integer concreteFont = text.getConcreteFont();
        if (concreteFont == null) {
            if (text.getSize() == TextSize.FULL_DOUBLE) {
                concreteFont = 2;
            } else if (text.getStyle() == TextStyle.ITALIC) {
                concreteFont = 6;
            } else if (text.getSize() == TextSize.SMALL) {
                concreteFont = 3;
            } else if (text.getStyle() == TextStyle.BOLD) {
                concreteFont = 4;
            }
        }
        int maxCharRow = config.getMaxCharRow();
        if (text.getValue().length() > maxCharRow) {
            String totalText = text.getValue();
            while (totalText.length() > 0) {
                if (totalText.length() > maxCharRow) {
                    String row = totalText.substring(0, maxCharRow);
                    if ((row.lastIndexOf(" ") != row.length()) && (row.lastIndexOf(" ") > 0)) {
                        text.setValue(totalText.substring(0, row.lastIndexOf(" ")));
                        totalText = totalText.substring(text.getValue().length() + 1);
                    } else {
                        text.setValue(totalText.substring(0, maxCharRow));
                        totalText = totalText.substring(maxCharRow);
                    }
                } else {
                    text.setValue(totalText);
                    totalText = "";
                }
                printLine(new FontLine(text.getValue(), Font.NORMAL, concreteFont));
            }
        } else {
            printLine(new FontLine(text.getValue(), Font.NORMAL, concreteFont));
        }

        LOG.trace("leaving printRow(Row)");
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        LOG.info("entering printXReport(Report, String). The arguments are: report [{}]", report);

        // 0. вытащим данные из аргумента
        Long documentNumber = report.getDocumentNumber();
        WorkPlace wp = report.getWorkPlace();
        ReportCounters rc = report.getReportCounters();
        Cashier cashier = report.getCashier();

        if (documentNumber == null) {
            documentNumber = 0L;
        }
        if (wp == null) {
            wp = new WorkPlace();
        }
        if (rc == null) {
            rc = new ReportCounters();
        }
        if (cashier == null) {
            cashier = new Cashier();
        }

        // 1. запишем имя кассира в фискальник. Не понятно для чего: на дефолной форме имя кассира не фигурирует
        //  Но сделаем как было до доработок (2016-01-18):
        setCashierName(report.getCashier() == null ? null : getCashierName(report.getCashier()));

        // 2. печатаем заголовок - обычным шрифтом
        printLine(new FontLine(DocumentUtils.makeHeader("ОТЧЕТ ПО КАССЕ (Отчет Х)", config.getMaxCharRow(), '*'), Font.NORMAL));
        printSubHeader(wp.getPosId(), documentNumber, rc.getShiftNum(), cashier);

        // 3. счетчики
        printLine(new FontLine(SPACE, Font.NORMAL));
        printLine(new FontLine(SPACE, Font.NORMAL));

        // 4. наконец печатаем отчет по шаблону, зашитому в фискальнике
        try {
            connector.printXReport();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printXReport(Report, String)", e);
        }

        LOG.info("leaving printXReport(Report, String)");
    }

    /**
     * Просто вызывает команду ФР "печать Z-отчета"
     *
     * @param cashier
     */
    private void printZReport(Cashier cashier) throws FiscalPrinterException {
        try {
            connector.printZReport(cashier);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printZReport()", e);
        }
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        LOG.debug("entering printZReport(Report). The arguments are: report [{}]", report);

        Long documentNumber = report.getDocumentNumber();
        WorkPlace wp = report.getWorkPlace();
        ReportCounters rc = report.getReportCounters();
        Cashier cashier = report.getCashier();

        if (documentNumber == null) {
            documentNumber = 0L;
        }
        if (wp == null) {
            wp = new WorkPlace();
        }
        if (rc == null) {
            rc = new ReportCounters();
        }
        if (cashier == null) {
            cashier = new Cashier();
        }

        printLine(new FontLine(DocumentUtils.makeHeader("ОТЧЕТ ПО КАССЕ (Отчет Z)", config.getMaxCharRow(), '*'), Font.NORMAL));
        printSubHeader(wp.getPosId(), documentNumber, rc.getShiftNum(), cashier);

        // Пропуск строки
        FontLine empty = new FontLine(SPACE, Font.NORMAL);
        printLine(empty);
        printLine(empty);
        printLine(empty);

        // печать Z-отчета самим ФР
        printZReport(report.getCashier());

        LOG.debug("leaving printZReport(Report)");
    }

    public long getIncreaseSale() throws FiscalPrinterException {
        long result = 0L;

        LOG.trace("entering getIncreaseSale()");

        try {
            // запрашивается сумма записей за все время:
            FiscalMemorySums fms = connector.getFiscalMemorySums(true);
            result = fms.getSalesSum().longValue();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getIncreaseSale()", e);
        }

        LOG.trace("leaving getIncreaseSale(). The result is: {}", result);

        return result;
    }

    @Override
    public void setDate(Date date) throws FiscalPrinterException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("entering setDate(Date). The argument is: {}", date == null ? "(NULL)" : String.format("%1$tF %1$tT", date));
        }

        try {
            connector.setDateTime(date);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("setDate(Date)", e);
        }

        LOG.debug("leaving setDate(Date)");
    }

    @Override
    public void statusUpdateOccurred(StatusUpdateEvent event) {
        logInfo("StatusUpdateEvent: " + event.getStatus());
    }

    @Override
    public void errorOccurred(ErrorEvent event) {
        logInfo("ErrorEvent: " + event.getErrorCode() + "/" + event.getErrorCodeExtended());
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        LOG.debug("entering setRequisites(List). The argument is: requisites [{}]", requisites);
        List<String> cliche = requisites.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<FontLine> header = new LinkedList<>();
        if (!cliche.isEmpty()) {
            // заголовок будем печатать нормальным шрифтом
            for (String req : cliche) {
                FontLine line = new FontLine(DocumentUtils.makeHeader(req == null ? "" : req, config.getMaxCharRow(), ' '), Font.NORMAL);
                header.add(line);
            }
        }

        try {
            connector.setHeader(header);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getShortState()", e);
        }

        LOG.debug("leaving setRequisites(List)");
    }

    @Override
    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        ValueAddedTaxCollection result = new ValueAddedTaxCollection();

        LOG.debug("entering getTaxes()");

        try {
            // 1. вытащим налоги
            Map<String, Long> taxes = connector.getTaxes();
            // 2. а теперь сконвертном в этот формат с Float
            int idx = 0;
            for (Map.Entry<String, Long> entry : taxes.entrySet()) {
                float value = (float) 1.0 * entry.getValue() / 100;
                result.addTax(new ValueAddedTax(idx, value, entry.getKey()));
                idx++;
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getTaxes()", e);
        }

        LOG.debug("leaving getTaxes(). The result is: {}", result);

        return result;
    }

    @Override
    public void setTaxes(ValueAddedTaxCollection taxesCollection) throws FiscalPrinterException {
        LOG.debug("entering setTaxes(Map). The argument is: taxes [{}]", taxesCollection);
        this.taxes = getTaxes();
        if (taxesCollection == null || taxesCollection.equals(taxes)) {
            LOG.debug("leaving setTaxes(Map): Taxes in KKT are equal to cash configuration (or cash configuration invalid)");
            return;
        }
        LOG.debug("Taxes in KKT [{}] are not equal to cash configuration and will be set", taxes);
        LinkedHashMap<String, Long> argInRightFormat = new LinkedHashMap<>();
        for (ValueAddedTax tax : taxesCollection) {
            argInRightFormat.put(tax.name, Math.round((double) tax.value * 100));
        }

        // 2. а теперь запишем в ФР
        try {
            connector.setTaxes(argInRightFormat);
        } catch (ShtrihException e) {
            LOG.error("setTaxes(Map): Unable to set taxes (probably it's new firmware with read only tax configuration tables {}", e.getMessage());
            return;
        } catch (IOException | PortAdapterException e) {
            logExceptionAndThrowIt("setTaxes(Map)", e);
        }

        // 3. засинхронизируем налоги в плагине с налогами в ФР
        this.taxes = getTaxes();
        LOG.debug("leaving setTaxes(Map)");
    }

    @Override
    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        LOG.trace("entering setCashNumber(Long). The argument is: {}", cashNumber);
        try {
            long currentNumber = getState().getNumber();
            long toSet = Math.min(cashNumber, 99);
            if (currentNumber == toSet) {
                return;
            }
            connector.setCashNumber((byte) toSet);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("setCashNumber(Long)", e);
        }
        LOG.trace("leaving setCashNumber(Long)");
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        LOG.trace("entering getINN()");
        if (regDataStorage.isDeviceINNEmpty()) {
            ShtrihStateDescription state = getState();
            regDataStorage.setDataFromState(state);
        }

        LOG.trace("leaving getINN(). The result is: {}", regDataStorage.getDeviceINN());

        return regDataStorage.getDeviceINN();
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        return config.getMaxCharRow();
    }

    @Override
    public int getPaymentLength() {
        return 18;
    }


    /**
     * Метод запрашивает тип устройства и сравнивает с ожидаемым
     *
     * @throws FiscalPrinterException - если устройство не семейства ШТРИХ
     */
    @Override
    public boolean verifyDevice() throws FiscalPrinterException {
        boolean result;
        LOG.debug("entering verifyDevice()");
        String expected = getDeviceName();
        String actual = getHardwareName();
        LOG.debug("verifyDevice(): expected device name: \"{}\"; actual device name: \"{}\"", expected, actual);
        result = actual.equals(expected);
        // Если не совпало имя, возможно на одном плагине подключено несколько моделей ФР
        if (!result) {
            LOG.debug("verifyDevice(): unexpected device name, try additionally...");
            for (String additionallyName : config.getJposNameAdditionally()) {
                expected = ResBundleFiscalPrinterShtrihMiniFrk.getString("DEVICE_NAME_" + additionallyName.replace('-', '_').toUpperCase());
                result = actual.equals(expected);
                if (result) {
                    deviceName = expected;
                    break;
                }
            }
        }
        LOG.debug("leaving verifyDevice(). The result is: {}", result);
        return result;
    }

    /**
     * Метод возвращает значение общего счетчика внесений
     */
    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        long result = 0;

        LOG.trace("entering getCountCashIn()");

        try {
            result = connector.getCashInCount();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getCountCashIn()", e);
        }

        LOG.trace("leaving getCountCashIn(). The result is: {}", result);

        return result;
    }

    /**
     * Метод возвращает значение общего счетчика изъятий
     */
    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        long result = 0;

        LOG.trace("entering getCountCashOut()");

        try {
            result = connector.getCashOutCount();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getCountCashOut()", e);
        }

        LOG.trace("leaving getCountCashOut(). The result is: {}", result);

        return result;
    }

    /**
     * Метод возвращает значение общего счетчика аннулирований
     */
    @Override
    public long getCountAnnul() throws FiscalPrinterException {
        long result = 0;

        LOG.trace("entering getCountAnnul()");

        try {
            result = connector.getAnnulCount();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getCountAnnul()", e);
        }

        LOG.trace("leaving getCountAnnul(). The result is: {}", result);

        return result;
    }

    /**
     * Метод возвращает количество денег в денежном ящике ККМ
     */
    @Override
    public long getCashAmount() throws FiscalPrinterException {
        long result = 0;

        LOG.debug("entering getCashAmount()");

        try {
            result = connector.getCashAccumulation();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getState()", e);
        }

        LOG.debug("leaving getCashAmount(). The result is: {}", result);

        return result;
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        StatusFP result = new StatusFP();

        LOG.debug("entering getStatus()");

        ShtrihShortStateDescription shortState = getShortState();

        if (shortState == null) {
            throw new FiscalPrinterException("Failed to get short state");
        }

        // ошибка фискальной памяти
        if (shortState.getFiscalBoardErrorCode() != 0 && (!isOFDDevice())) {
            result.setLongStatus(shortState.getFiscalBoardErrorCode());
            result.addDescription("Ошибка фискальной памяти");
        }

        // ошибка ЭКЛЗ
        if (shortState.getEklzErrorCode() != 0 && (!isOFDDevice())) {
            result.setLongStatus(shortState.getEklzErrorCode());
            result.addDescription("Ошибка ЭКЛЗ");
        }

        if (!result.getDescriptions().isEmpty()) {
            result.setStatus(StatusFP.Status.FATAL);

            LOG.debug("leaving getStatus(). The result is: {}", result);

            return result;
        }

        // printer
        ShtrihStateDescription state = getState();
        ShtrihFlags flags = state.getFlags();
        if (flags != null) {
            if (!flags.isRibbonSensor()) {
                result.addDescription("В принтере нет бумаги");
                result.setStatus(StatusFP.Status.END_PAPER);
                result.setLongStatus(1L);
            } else if (flags.isCaseCover()) {
                result.addDescription("Открыта крышка принтера");
                result.setStatus(StatusFP.Status.OPEN_COVER);
                result.setLongStatus(1L);
            }
        }

        LOG.debug("leaving getStatus(). The result is: {}", result);

        return result;
    }

    public void printLogo() throws FiscalPrinterException {
    }

    @Override
    public String getDeviceName() {
        if (deviceName == null) {
            deviceName = ResBundleFiscalPrinterShtrihMiniFrk.getString("DEVICE_NAME_" + config.getJposName().replace('-', '_').toUpperCase());
        }
        return deviceName;
    }

    @Override
    public String getHardwareName() throws FiscalPrinterException {
        return getDeviceType().getName().trim();
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) throws FiscalPrinterException {
        // здесь надо просто аннулировать документ, если он открыт

        LOG.info("entering postProcessNegativeScript(Exception)");
        try {
            regDataStorage.clearSavedData();
            connector.annul();
        } catch (SocketException e) {
            //попробуем переподключиться, вероятно отключилось питание или кабель
            try {
                connector.close();
                connector.open();
            } catch (ShtrihException | IOException | PortAdapterException ex) {
                logExceptionAndThrowIt("postProcessNegativeScript(Exception)", e);
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("postProcessNegativeScript(Exception)", e);
        }

        LOG.info("leaving postProcessNegativeScript(Exception)");
    }

    @Override
    public void printDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        if (document instanceof Check) {
            Check check = (Check) document;
            if (check.isAnnul()) {
                printAnnulCheckByTemplate(sectionList, check);
            } else if (check.isCopy() || check instanceof FullCheckCopy) {
                printCopyCheckByTemplate(sectionList, check);
            } else {
                printCheckByTemplate(sectionList, check);
            }
        } else if (document instanceof Report) {
            printReportByTemplate(sectionList, (Report) document);
        } else if (document instanceof Money) {
            printMoneyByTemplate(sectionList, (Money) document);
        } else if (document instanceof DiscountsReport) {
            printServiceByTemplate(sectionList, document);
        } else if (document instanceof DailyLogData) {
            printDailyReportByTemplate(sectionList, document);
        } else if (document instanceof BonusCFTDocument) {
            printBonusCFTReportByTemplate(sectionList, document);
        } else {
            printServiceByTemplate(sectionList, document);
        }
    }

    protected void printBonusCFTReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_OPERATION_LIST:
                        setCashierName(document.getCashier().getName());
                        setDepartNumber(document.getDepart().intValue());
                        connector.openNonFiscalDocument();
                        printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        closeNonFiscalDocument(document);
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        break;
                    default:
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        LOG.debug("entering printDailyReportByTemplate(List, FiscalDocument). The arguments are: document [{}]", document);

        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        setCashierName(getCashierName(document.getCashier()));
                        setDepartNumber(document.getDepart().intValue());
                        connector.openNonFiscalDocument();
                        printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        closeNonFiscalDocument(document);
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printDailyReportByTemplate(List, FiscalDocument)", e);
        }

        LOG.debug("leaving printDailyReportByTemplate(List, FiscalDocument)");
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        LOG.debug("entering printMoneyByTemplate(List, Money). The arguments are: money [{}]", money);

        if (InventoryOperationType.DECLARATION.equals(money.getOperationType())) {
            printServiceByTemplate(sectionList, money);
        } else {
            // сумма внесения/изъятия:
            long total = 0L;
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        setCashierName(getCashierName(money.getCashier()));
                        setDepartNumber(money.getDepart().intValue());
                        printLinesList(section.getContent());
                        break;
                    case "total":
                        total = money.getValue();
                        printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        // осталось зарегистрировать внесение/изъятие
                        if (InventoryOperationType.CASH_IN.equals(money.getOperationType())) {
                            registerCashIn(total);
                        } else {
                            registerCashOut(total);
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
        }

        LOG.debug("leaving printMoneyByTemplate(List, Money)");
    }

    /**
     * Регисртирует (в ФР) внесение на указанную сумму
     *
     * @param sum сумма внесения, в МДЕ
     */
    protected void registerCashIn(long sum) throws FiscalPrinterException {
        try {
            connector.regCashIn(sum);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("registerCashIn(long)", e);
        }
    }

    /**
     * Регисртирует (в ФР) изъятие указанной суммы
     *
     * @param sum сумма изъятия, в МДЕ
     */
    protected void registerCashOut(long sum) throws FiscalPrinterException {
        try {
            connector.regCashOut(sum);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("registerCashOut(long)", e);
        }
    }

    private void printServiceByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        LOG.debug("entering printServiceByTemplate(List, FiscalDocument). The arguments are: document [{}]", document);

        try {
            boolean closeNonFiscal = false;
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        setCashierName(document.getCashier() == null ? "" : getCashierName(document.getCashier()));
                        setDepartNumber(document.getDepart() == null ? 0 : document.getDepart().intValue());
                        connector.openNonFiscalDocument();
                        printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        closeNonFiscal = true;
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
            if (closeNonFiscal) {
                closeNonFiscalDocument(document);
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        }

        LOG.debug("leaving printServiceByTemplate(List, FiscalDocument)");
    }

    private void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        LOG.info("entering printReportByTemplate(List, Report). The arguments are: report [{}]", report);

        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_HEADER:
                        setCashierName(getCashierName(report.getCashier()));
                        setDepartNumber((report.getDepart() == null) ? 1 : report.getDepart().intValue());
                        connector.openNonFiscalDocument();
                        printLinesList(section.getContent());
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        connector.printLine(new FontLine(SPACE, Font.NORMAL));
                        if (report.isZReport()) {
                            closeNonFiscalDocument(null);

                            if (!report.isCopy()) {
                                printAdditionalInfo(report);
                                connector.printZReport(report.getCashier());
                            }
                        } else if (report.isXReport()) {
                            closeNonFiscalDocument(null);
                            printAdditionalInfo(report);
                            connector.printXReport();
                        }
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                    case FiscalPrinterPlugin.SECTION_FOOTER:
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        }

        LOG.info("leaving printReportByTemplate(List, Report)");
    }

    /**
     * Метод печатает дополнительные строки в чек.
     * Вынесен отдельно, поскольку могут быть добавлены в него же другие строки.
     *
     * @throws FiscalPrinterException
     */
    private void printAdditionalInfo(Report report) throws FiscalPrinterException {
        printLine(new FontLine(ResBundleFiscalPrinterShtrihMiniFrk.getString("SHTRIH_CASH_NUMBER") + report.getCashNumber()));
    }

    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        LOG.debug("entering printCheckByTemplate(List, Check). The arguments are: check [{}]", check);

        setCashierName(getCashierName(check.getCashier()));
        setDepartNumber(check.getDepart().intValue());

        Map<Integer, Long> payments = getPayments(check);
        ShtrihPosition position = new ShtrihPosition();
        position.setPrice(check.getCheckSumEnd());
        position.setQuantity(1000);
        position.setDeptNo(deptNo);
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                switch (sectionName) {
                    case FiscalPrinterPlugin.SECTION_LOGO:
                        printLogo();
                        break;
                    case FiscalPrinterPlugin.SECTION_FISCAL:
                        if (check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                            printCheckBarcode(check);
                            printLine(new FontLine(SPACE, Font.NORMAL));
                        }
                        // Регистрируем одну позицию на всю сумму чека
                        if (CheckType.SALE.equals(check.getType())) {
                            connector.regSale(position);
                        } else if (CheckType.RETURN.equals(check.getType())) {
                            connector.regReturn(position);
                        }
                        // распечатать подытог
                        printSubTotal(check.getCheckSumEnd());
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
            // закрыть/зарегистрировать чек
            ShtrihReceiptTotal receiptTotal = new ShtrihReceiptTotal();
            receiptTotal.setText("");
            // оплаты по типам:
            // нал
            if (payments.get(0) != null) {
                receiptTotal.setCashSum(payments.get(0));
            }
            // безнал
            if (payments.get(1) != null) {
                receiptTotal.setSecondPaymentTypeSum(payments.get(1));
            }
            // налоги и скидки в ФР пишем по нулям

            connector.closeReceipt(receiptTotal);
            closeNonFiscalDocument(check);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        }

        LOG.debug("leaving printCheckByTemplate(List, Check)");
    }

    @Override
    public void printBarcodeBlock(BarCode barcode) {
        connector.printBarcodeBlock(barcode);
    }

    /**
     * Вернет оплаты, примененные в указанном чеке, сгруппированные по типам [отплат].
     *
     * @param check чек, оплаты из которого надо вернуть
     * @return код типа оплаты с точки зрения ФР (0..4) - в качестве ключа, сумма оплат этого типа (в МДЕ) - в качестве значения; не вернет
     * <code>null</code> - как минимум одна оплата в коллекции будет (если не было оплат вообще - вернет. что была оплата налом на 0 копеек)
     */
    protected Map<Integer, Long> getPayments(Check check) {
        Map<Integer, Long> result = new HashMap<>();

        if (check == null) {
            LOG.error("getPayments(Check): the argument is NULL!");
            return result;
        }
        long cashSum = 0L;
        long nonCashSum = 0L;
        if (CollectionUtils.isEmpty(check.getPayments())) {
            LOG.warn("getPayments(Check): the argument has no payments!");
        } else {
            for (Payment p : check.getPayments()) {
                if (p == null) {
                    LOG.error("getPayments(Check): NULL paiment was detected!");
                    continue;
                }

                if (p.getIndexPayment() == PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex()) {
                    // это нал
                    cashSum += p.getSum();
                } else {
                    // иначе - безнал
                    nonCashSum += p.getSum();
                }
            } // for p
        }

        // теперь: оплата налом - это 0й тип, а безналом - 1й
        if (nonCashSum != 0) {
            result.put(1, nonCashSum);
        }
        if (cashSum != 0 || result.isEmpty()) {
            result.put(0, cashSum);
        }

        return result;
    }

    private void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        LOG.debug("entering printCopyCheckByTemplate(List, Check). The arguments are: check [{}]", check);

        for (DocumentSection section : sectionList) {
            String sectionName = section.getName();
            switch (sectionName) {
                case FiscalPrinterPlugin.SECTION_LOGO:
                    printLogo();
                    break;
                case FiscalPrinterPlugin.SECTION_HEADER:
                    setCashierName(getCashierName(check.getCashier()));
                    setDepartNumber(check.getDepart().intValue());
                    printLinesList(section.getContent());
                    break;
                case FiscalPrinterPlugin.SECTION_FISCAL:
                    printCheckBarcode(check);
                    break;
                case FiscalPrinterPlugin.SECTION_CUT:
                    closeNonFiscalDocument(check);
                    break;
                default:
                    printLinesList(section.getContent());
                    break;
            }
        }

        LOG.debug("leaving printCopyCheckByTemplate(List, Check)");
    }

    protected void printCheckBarcode(Check check) {
        DocumentNumber barcode = PluginUtils.getDocumentNumberBarcode(check);
        printBarcode(barcode.getBarcode(), barcode.getReadableBarcodeLabel());
    }

    /**
     * Просто печатает укаазанный ШК в кодировке {@link BarCodeType#Code39}.
     * <p/>
     * Картинка-ШК будет выровняна по центру.
     *
     * @param barcode сам код ШК. что надо распечатать
     * @param label   подпись под ШК
     */
    private void printBarcode(String barcode, String label) {
        LOG.debug("entering printBarcode(String, String). The arguments are: barcode [{}], label [{}]", barcode, label);

        if (StringUtils.isEmpty(barcode)) {
            LOG.debug("leaving printBarcode(String, String). The \"barcode\" is EMPTY!");
            return;
        }
        try {
            BarCode bc = new BarCode(barcode);
            bc.setType(BarCodeType.Code39);
            connector.printBarcode(bc, label, ShtrihAlignment.CENTRE);
        } catch (Throwable t) {
            LOG.error(String.format("printBarcode(String, String) FAILED. The arguments were: barcode [%s], label [%s]", barcode, label), t);
        }

        LOG.debug("leaving printBarcode(String, String)");
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        LOG.debug("entering printAnnulCheckByTemplate(List, Check). The arguments are: check [{}]", check);

        for (DocumentSection section : sectionList) {
            String sectionName = section.getName();
            switch (sectionName) {
                case FiscalPrinterPlugin.SECTION_LOGO:
                    printLogo();
                    break;
                case FiscalPrinterPlugin.SECTION_HEADER:
                    setCashierName(getCashierName(check.getCashier()));
                    setDepartNumber(check.getDepart().intValue());
                    printLinesList(section.getContent());
                    break;
                case FiscalPrinterPlugin.SECTION_FISCAL:
                    break;
                case FiscalPrinterPlugin.SECTION_CUT:
                    printLine(new FontLine("", Font.NORMAL));
                    closeNonFiscalDocument(check);
                    break;
                case FiscalPrinterPlugin.SECTION_POSITION:
                default:
                    printLinesList(section.getContent());
                    break;
            }
        }

        LOG.debug("leaving printAnnulCheckByTemplate(List, Check)");
    }

    protected void printLinesList(List<FontLine> stringList) throws FiscalPrinterException {
        for (FontLine str : stringList) {
            printLine(str);
        }
    }

    protected void printLine(FontLine line) throws FiscalPrinterException {
        LOG.trace("entering printLine(FontLine): the argument is: line [{}]", line);

        try {
            if (line != null && line.getBarcode() != null) {
                printBarcode(line.getBarcode());
                return;
            }
            connector.printLine(line);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printLine(FontLine)", e);
        }

        LOG.trace("leaving printLine(FontLine)");
    }

    @Override
    public boolean isOFDDevice() {
        return config.isOfdDevice();
    }

    public ShtrihConnector getConnector() {
        return connector;
    }

    protected String getCashierName(Cashier cashier) {
        return cashier.getCashierStringForOFDTag1021();
    }
}
