package ru.crystals.pos.fiscalprinter.mstar;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.DocumentType;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.cards.plastek.PlastekDocument;
import ru.crystals.pos.check.Base39Coder;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.AbstractFiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BankNote;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusCFTDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
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
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardInfoReport;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardReplaceReport;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;
import ru.crystals.pos.fiscalprinter.mstar.core.MstarUtils;
import ru.crystals.pos.fiscalprinter.mstar.core.ResBundleFiscalPrinterMstar;
import ru.crystals.pos.fiscalprinter.mstar.core.TaxSystem;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.MstarAgent;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.MstarConfig;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.MstarConnector;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.MstarErrorMsg;
import ru.crystals.pos.fiscalprinter.mstar.core.connect.PingStatus;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;
import ru.crystals.pos.utils.CheckUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PrototypedComponent
public class Mstar extends AbstractFiscalPrinterPlugin implements Configurable<MstarPluginConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(Mstar.class);

    private static final int CASHIER_NAME_MAX_LENGTH = 26;
    private static final int GOOD_NAME_MAX_LENGTH = 233;
    private static final int ITEM_MAX_LENGTH = 18;
    private static final int COUPON_BARCODE_LENGTH = 21;
    private static final long BARCODE_HEIGHT = 40;

    private static final int MAX_CHAR_ROW = 42;
    private static final int MAX_CHAR_ROW_SMALL_FOR_SERVICE_DOC = 54;
    private static final int MAX_CHAR_ROW_SMALL = 55;
    private static final int MAX_CHAR_ROW_DOUBLEWIDTH = 20;
    private static final int KKT_CHAR_IN_ROW = 48;
    private static final int MAX_DEPART_NUMBER = 15;

    private static final long PRICE_ORDER = 100;
    private static final String DEFAULT_GOODS_NAME = "-----";
    private static final String SPACE = " ";
    private static final long COUNT_ORDER = 1000;

    protected final MstarConfig mstarConfig = new MstarConfig();
    protected MstarConnector mstarConnector = new MstarConnector();
    private MstarAgent mstarAgent;
    private ValueAddedTaxCollection taxes;
    private long taxSystem = -1;
    private long firmwareId = 0;
    private MstarPluginConfig config;

    @Override
    public Class<MstarPluginConfig> getConfigClass() {
        return MstarPluginConfig.class;
    }

    @Override
    public void setConfig(MstarPluginConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        try {
            connect();
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_STATUS);
            validateCurrentFlagStatus(dp.getLongValue(0));
            validateDocumentStatus(dp.getLongValue(1));

            taxes = getTaxes();
            configureDevice();

            logStatusFN();

            taxSystem = TaxSystem.getByFlag(readTaxSystem()).ordinal();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            LOG.error("", e);
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Получить код системы налогооблажения
     *
     * @return числовой код системы налогооблажения
     * @see TaxSystem
     */
    private long readTaxSystem() throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        long lastReportRegistration = mstarAgent.getStatusFN().getAlreadyMadeReportRegistration();
        dp.putLongValue(lastReportRegistration);
        dp = mstarConnector.sendRequest(MstarCommand.GET_RESULT_REGISTRATION, dp);
        try {
            return dp.getLongValue(4);
        } catch (Exception e) {
            throw new FiscalPrinterException("Failed to get taxSystem", e);
        }
    }

    private void connect() throws FiscalPrinterOpenPortException {
        mstarConnector.setParams(config);
        try {
            mstarConnector.connect();
            PingStatus ps = mstarConnector.isMstarOnline();
            int c = 30;
            while (!ps.isOnline() && c > 0) {
                c--;
                ps = mstarConnector.isMstarOnline();
                Thread.sleep(1000);
            }
            if (!ps.isOnline()) {
                throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterMstar.getString("SERVICE_TIMEOUT"), CashErrorType.FATAL_ERROR);
            }
        } catch (Exception e) {
            LOG.warn("", e);
            throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterMstar.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
        }
        mstarAgent = new MstarAgent(mstarConnector);
        mstarConfig.setConnector(mstarConnector);
    }

    private void validateCurrentFlagStatus(long currentFlagsStatus) throws Exception {
        // Выключен флаг "Нефискальный режим"
        if (MstarUtils.getBit(currentFlagsStatus, 1)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("ERROR_NOT_FISCALIZED"), CashErrorType.FATAL_ERROR);
        }

        // Не была вызвана функция «Начало работы»
        boolean needStartCmd = MstarUtils.getBit(currentFlagsStatus, 0);
        if (needStartCmd) {
            DataPacket dp = new DataPacket();
            dp.putDateValue(new Date());
            dp.putTimeValue(new Date());

            try {
                mstarConnector.sendRequest(MstarCommand.START_WORK, dp);
            } catch (FiscalPrinterException fpe) {
                //Ошибка 0Ch возникает, в случае если передаваемая в команде ”Начало работы” дата меньше
                //даты последней фискальной операции
                if ((fpe.getErrorCode() != null) && (fpe.getErrorCode() == 0x0C)) {
                    throw fpe;
                }
            }
        }
    }

    private void validateDocumentStatus(long stateDocument) throws Exception {
        boolean openDoc = (stateDocument & 0x1F) != 0;
        if (openDoc) {
            annulCheck();
        }
    }

    private void logStatusFN() {
        try {
            LOG.info(mstarAgent.getStatusFN().toString());
            LOG.info(mstarAgent.getStatusOFD().toString());
        } catch (FiscalPrinterException ex) {
            LOG.error("Get log status error", ex);
        }
    }

    protected void configureDevice() throws FiscalPrinterException {
        mstarConfig.setAutoWithdrawal(false);
        mstarConfig.setCompressFont(false);
        //При значениях CharInRow в настройках ККТ отличных от 48 ломается формат документов
        mstarConfig.setCharInRow(KKT_CHAR_IN_ROW);
    }

    /**
     * Регистрационный номер ФН
     *
     * @return номер ФН
     */
    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return mstarAgent.getStatusFN().getNumberFN();
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return mstarAgent.getINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return mstarAgent.openShiftInFN(cashier);
    }

    /**
     * Запрос номера последнего фискального документа
     *
     * @return номер последнего фискального документа
     */
    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return mstarAgent.getLastKpk();
    }

    @Override
    public void stop() {
        mstarConnector.close();
    }

    /**
     * Запрос регистрационного номера ККТ
     *
     * @return регистрационный номер ККТ
     */
    @Override
    public String getRegNum() throws FiscalPrinterException {
        return mstarAgent.getRegistrationNumber();
    }

    @Override
    public String getVerBios() throws FiscalPrinterException {
        try {
            return String.valueOf(getFirmwareId());
        } catch (Exception ex) {
            throw new FiscalPrinterException("GET VER BIOS ERROR", MstarErrorMsg.getErrorType());
        }
    }

    /**
     * Получить идентификатор прошивки
     */
    private long getFirmwareId() throws FiscalPrinterException {
        if (firmwareId == 0) {
            DataPacket dp = new DataPacket();
            dp.putLongValue(2L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            try {
                firmwareId = dp.getLongValue(1);
            } catch (Exception e) {
                throw new FiscalPrinterException("Failed to get firmwareId", e);
            }
        }
        return firmwareId;
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        try {
            return mstarAgent.getShiftParameters().isShiftOpen();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public Date getDate() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_DATE);
            return mergeTimeToDate(dp.getDateValue(0), dp.getTimeValue(1));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setDate(Date date) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putDateValue(date);
            dp.putTimeValue(date);
            mstarConnector.sendRequest(MstarCommand.SET_DATE, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        try {
            mstarConnector.sendRequest(MstarCommand.OPEN_MONEY_DRAWER);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Не поддерживается ФР.
     *
     * @return всегда false
     */
    @Override
    public boolean isMoneyDrawerOpen() {
        return false;
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            ShiftCounters shiftCounters = new ShiftCounters();
            DataPacket dp = new DataPacket();

            /* Номер смены */
            dp.putLongValue(1L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            shiftCounters.setShiftNum(dp.getLongValue(1));

            /* Оплаты */
            dp.clear();
            dp.putLongValue(3L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            shiftCounters.setSumCashPurchase(dp.getDoubleMoneyToLongValue(1));

            long value = 0;
            for (byte i = 0; i < 4; i++) {
                value += dp.getDoubleMoneyToLongValue(2 + i);
            }
            shiftCounters.setSumCashlessPurchase(value);
            shiftCounters.setSumSale(shiftCounters.getSumCashPurchase() + shiftCounters.getSumCashlessPurchase());

            /* Возвраты */
            dp.clear();
            dp.putLongValue(4L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            shiftCounters.setSumCashReturn(dp.getDoubleMoneyToLongValue(1));

            value = 0;
            for (byte i = 0; i < 4; i++) {
                value += dp.getDoubleMoneyToLongValue(2 + i);
            }
            shiftCounters.setSumCashlessReturn(value);
            shiftCounters.setSumReturn(shiftCounters.getSumCashReturn() + shiftCounters.getSumCashlessReturn());

            /* Сумма наличных в денежном ящике */
            dp.clear();
            dp.putLongValue(7L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            shiftCounters.setSumCashEnd(dp.getDoubleMoneyToLongValue(1));

            /* Количество чеков по типам операций */
            dp.clear();
            dp.putLongValue(5L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);

            shiftCounters.setCountSale(dp.getDoubleToRoundLong(1));
            shiftCounters.setCountReturn(dp.getDoubleToRoundLong(2));
            shiftCounters.setCountCashIn(dp.getDoubleToRoundLong(6));
            shiftCounters.setCountCashOut(dp.getDoubleToRoundLong(7));

            dp.clear();
            dp.putLongValue(6L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            shiftCounters.setSumCashIn(dp.getDoubleMoneyToLongValue(2));
            shiftCounters.setSumCashOut(dp.getDoubleMoneyToLongValue(3));

            /* Количество оплат по чекам. Нет данных */
            shiftCounters.setCountCashPurchase(0L);
            shiftCounters.setCountCashlessPurchase(0L);
            shiftCounters.setCountCashReturn(0L);
            shiftCounters.setCountCashlessReturn(0L);

            return shiftCounters;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public Date getLastFiscalOperationDate() {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(5L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            return mergeTimeToDate(dp.getDateValue(1), dp.getTimeValue(2));
        } catch (Exception e) {
            LOG.error("Failed to get close shift date/time. Current value will be returned", e);
            return new Date();
        }
    }

    private Date mergeTimeToDate(Date date, Date time) {
        LocalDate newDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime newTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        newDate.isEqual(LocalDate.now());
        return Date.from(newDate.atTime(newTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public PrinterState getPrinterState() throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_PRINTER_STATE);

            PrinterState ps = new PrinterState();
            ps.setLongState(dp.getLongValue(0));
            if (MstarUtils.getBit(ps.getLongState(), 0)) {
                ps.addDescription(ResBundleFiscalPrinterMstar.getString("WARN_PRINTER_NOT_READY"));
            }
            if (MstarUtils.getBit(ps.getLongState(), 1)) {
                ps.addDescription(ResBundleFiscalPrinterMstar.getString("WARN_END_OF_PAPER"));
                ps.setState(PrinterState.State.END_PAPER);
            }
            if (MstarUtils.getBit(ps.getLongState(), 2)) {
                ps.addDescription(ResBundleFiscalPrinterMstar.getString("OPEN_PRINTER_COVER"));
                ps.setState(PrinterState.State.OPEN_COVER);
            }
            if (MstarUtils.getBit(ps.getLongState(), 3)) {
                ps.addDescription(ResBundleFiscalPrinterMstar.getString("ERROR_CUTTER_PRINTER"));
            }
            if (MstarUtils.getBit(ps.getLongState(), 7)) {
                ps.addDescription(ResBundleFiscalPrinterMstar.getString("NO_COMMUNICATION_WITH_PRINTER"));
            }
            return ps;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        try {
            openDocument(check, false);
            putGoods(check.getGoods(), false);
            mstarConnector.sendRequest(MstarCommand.SUBTOTAL, false);
            putPayments(check.getPayments());
            if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                printDocumentNumberBarcode(check);
            }
            if (check.isAnnul()) {
                annulCheck();
            } else {
                if (check.getClientRequisites() != null) {
                    putCustomerAddress(check.getClientRequisites());
                }
                closeDocument(true);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    protected String getCashierName(Cashier cashier) {
        return StringUtils.left(cashier.getNullSafeName(), CASHIER_NAME_MAX_LENGTH);
    }

    private Long getDocDepart(Long depart) {
        return Optional.ofNullable(depart)
                .filter(dep -> dep >= 1L)
                .filter(dep -> dep <= MAX_DEPART_NUMBER)
                .orElse(1L);
    }

    private void printDocumentNumberBarcode(Check check) throws Exception {
        BarCode documentBarcode = PluginUtils.getDocumentBarcode(check);
        documentBarcode.setTextPosition(TextPosition.NONE_TEXT);
        documentBarcode.setHeight(BARCODE_HEIGHT);
        printBarCode(documentBarcode, false);
        FontLine barcodeLabel = new FontLine(StringUtils.center(documentBarcode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
        printLine(barcodeLabel);
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {
        try {
            openDocument(money, false);
            long totalSum = 0;
            if (money.getOperationType() == InventoryOperationType.CASH_IN) {
                for (BankNote bankNote : money.getBankNotes()) {
                    totalSum += bankNote.getValue();
                }
            } else {
                for (BankNote bankNote : money.getBankNotes()) {
                    totalSum += bankNote.getValue() * bankNote.getCount();
                }
                if (money.getSumCoins() != null) {
                    totalSum += money.getSumCoins();
                }
            }
            DataPacket dp = new DataPacket();
            dp.putStringValue(ResBundleFiscalPrinterMstar.getString("PD_SUM_CASH_IN_OUT") + money.getCurrency());
            dp.putDoubleValue((double) totalSum / PRICE_ORDER);
            mstarConnector.sendRequest(MstarCommand.ADD_MONEY_IN_OUT, dp, false);

            closeDocument(true);

        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        try {
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_STATUS);
            long stateDocument = dp.getLongValue(1);
            boolean openDoc = ((stateDocument & 0x1F) != 0);

            // Не была вызвана функция «Начало работы»*
            if ((dp.getLongValue(0) & 1) != 0) {
                dp = new DataPacket();
                dp.putDateValue(new Date());
                dp.putTimeValue(new Date());
                mstarConnector.sendRequest(MstarCommand.START_WORK, dp);
            }
            if (openDoc) {
                annulCheck();
            }

            dp.clear();
            dp.putLongValue(DocumentType.SERVICE_DOCUMENT.getValue());
            dp.putLongValue(getDocDepart(serviceDocument.getDepart()));
            dp.putStringValue(getCashierName(serviceDocument.getCashier()));
            // Параметр «Номер документа» не задаётся и передаётся пустым значением
            dp.putLongValue(null);

            mstarConnector.sendRequest(MstarCommand.OPEN_DOCUMENT, dp);

            for (Row row : serviceDocument.getRows()) {
                if (row instanceof Text) {
                    printText((Text) row, getMaxCharRowForServiceDocument(((Text) row).getSize()));
                } else if (row instanceof BarCode) {
                    printBarCode((BarCode) row, false);
                    printText(new Text(""));
                }
            }

            closeDocument(true);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private int getMaxCharRowForServiceDocument(TextSize font) {
        switch (font) {
            case SMALL:
                return MAX_CHAR_ROW_SMALL_FOR_SERVICE_DOC;
            case DOUBLE_WIDTH:
                return MAX_CHAR_ROW_DOUBLEWIDTH;
            case FULL_DOUBLE:
            case DOUBLE_HEIGHT:
            case NORMAL:
            default:
                return MAX_CHAR_ROW;
        }
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(report.getCashier()));

            mstarConnector.sendRequest(MstarCommand.PRINT_X_REPORT, dp, false);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(report.getCashier()));
            mstarConnector.sendRequest(MstarCommand.PRINT_Z_REPORT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setPayments(List<PaymentType> payments) throws FiscalPrinterException {
        try {
            mstarConfig.setPaymentsFFD100(payments);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        try {
            mstarConfig.setRequisites(requisites);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        try {
            return mstarConfig.getTaxes();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setTaxes(ValueAddedTaxCollection taxes) throws FiscalPrinterException {
        try {
            mstarConfig.setTaxes(taxes);
            this.taxes = getTaxes();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        try {
            mstarConfig.setCashNumber(cashNumber);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printBarCode(BarCode barCode, boolean isAsync) throws Exception {
        DataPacket dp = new DataPacket();
        // Проблема с печатью штрихкода. Несмотря на то что в доках указана возможность использовать любой параметр, пока доступен только 2.
        // Из переписки с разработчиком ФР:
        // Похоже это связано с параметром "вывод текста".Пока рекомендую использовать настройку 2. В дальнейшем будет доступна и 0. Другие моделью не поддерживаются
        if (barCode.getType() == BarCodeType.Code39) {
            encodeToBase42AndPrintBarcode(barCode);
            return;
        }
        if (barCode.getType() == BarCodeType.QR) {
            printQR(barCode, isAsync);
            return;
        }
        //"вывод текста", только 2, см комент выше
        dp.putLongValue(2L);
        dp.putLongValue(barCode.getWidth());
        dp.putLongValue(barCode.getHeight());

        if (barCode.getType() == BarCodeType.Code39) {
            dp.putLongValue(4L);
        } else if (barCode.getType() == BarCodeType.EAN13) {
            dp.putLongValue(2L);
        } else if (barCode.getType() == BarCodeType.EAN8) {
            dp.putLongValue(3L);
        } else if (barCode.getType() == BarCodeType.UPCA) {
            dp.putLongValue(0L);
        } else if (barCode.getType() == BarCodeType.UPCE) {
            dp.putLongValue(1L);
        }
        dp.putStringValue(barCode.getValue());
        mstarConnector.sendRequest(MstarCommand.PRINT_BARCODE, dp, isAsync);
    }

    private void printQR(BarCode barCode, boolean isAsync) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        // Модель [0]
        dp.putLongValue(0L);
        // Размер точки [1, 8]
        dp.putLongValue(8L);
        // Уровень коррекции ошибок [0, 3]
        dp.putLongValue(2L);
        dp.putStringValue(barCode.getValue());
        mstarConnector.sendRequest(MstarCommand.PRINT_QR, dp, isAsync);
    }

    private void encodeToBase42AndPrintBarcode(BarCode barCode) throws Exception {
        DataPacket dp = new DataPacket();
        String code = StringUtils.trimToEmpty(barCode.getValue());
        if (code.length() == COUPON_BARCODE_LENGTH && Base39Coder.canCodeToBase39(code)) {
            try {
                barCode.setValue(Base39Coder.base39Encode(code));
                barCode.setBarcodeLabel(code);
            } catch (Exception e) {
                return;
            }
        }

        if (barCode.getTextPosition() == TextPosition.TOP_TEXT || barCode.getTextPosition() == TextPosition.TOP_AND_BOTTOM_TEXT) {
            FontLine barcodeLabel = new FontLine(StringUtils.center(barCode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
            printLine(barcodeLabel);
        }
        //"вывод текста", только 2, см комент выше методом
        dp.putLongValue(2L);
        dp.putLongValue(barCode.getWidth());
        dp.putLongValue(barCode.getHeight());
        dp.putLongValue(4L);
        dp.putStringValue(barCode.getValue());
        mstarConnector.sendRequest(MstarCommand.PRINT_BARCODE, dp, false);
        if (barCode.getTextPosition() == TextPosition.BOTTOM_TEXT || barCode.getTextPosition() == TextPosition.TOP_AND_BOTTOM_TEXT) {
            FontLine barcodeLabel = new FontLine(StringUtils.center(barCode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
            printLine(barcodeLabel);
        }
    }

    protected void putGoods(List<Goods> goods, boolean isAsyncMode) throws Exception {
        int posNum = 0;

        for (Goods good : goods) {
            posNum = putGood(good, posNum, isAsyncMode);
        }
    }

    protected int putGood(Goods good, int posNum, boolean isAsyncMode) throws Exception {
        DataPacket dp = new DataPacket();

        if (config.isPrintGoodsName()) {
            if (good.getName() == null) {
                dp.putStringValue(DEFAULT_GOODS_NAME);
            } else {
                // ограничение на длину названия товара
                if (good.getName().length() > GOOD_NAME_MAX_LENGTH) {
                    dp.putStringValue(good.getName().substring(0, GOOD_NAME_MAX_LENGTH));
                } else {
                    dp.putStringValue(good.getName());
                }
            }
        } else {
            dp.putStringValue("");
        }

        if (config.isPrintItem()) {
            // ограничение на длину артикула или штрихового кода товара/номера ТРК
            if (good.getItem().length() > ITEM_MAX_LENGTH) {
                dp.putStringValue(good.getItem().substring(0, ITEM_MAX_LENGTH));
            } else {
                dp.putStringValue(good.getItem());
            }
        } else {
            dp.putStringValue("");
        }

        dp.putDoubleValue((double) good.getQuant() / COUNT_ORDER);
        dp.putDoubleValue(CurrencyUtil.convertMoney(good.getEndPricePerUnit()).doubleValue());
        ValueAddedTax tax = taxes.lookupByValue(good.getTax());
        if (tax == null) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("ERROR_TAX_VALUE"), MstarErrorMsg.getErrorType());
        }
        dp.putLongValue(tax.index);

        if (config.isPrintPosNum()) {
            dp.putStringValue(String.format("%3d ", ++posNum));
        } else {
            dp.putStringValue("");
        }
        dp.putLongValue(0L);

        mstarConnector.sendRequest(MstarCommand.ADD_ITEM, dp, isAsyncMode);

        return posNum;
    }

    private void putPayments(List<Payment> payments) throws Exception {
        //группировка по типу оплат и сортировка что бы 0-тип оплаты(наличные) был последним
        for (Payment payment : CheckUtils.reduceAndSortPaymentsByIndexPaymentFDD(payments)) {
            DataPacket dp = new DataPacket();

            dp.putLongValue(payment.getIndexPaymentFDD100());
            //сумма всех оплат одного типа
            dp.putDoubleValue(CurrencyUtil.convertMoney(payment.getSum()).doubleValue());
            dp.putStringValue("");

            mstarConnector.sendRequest(MstarCommand.ADD_PAYMENT, dp, false);
        }
    }

    private void putCustomerAddress(String customerAddress) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putStringValue(customerAddress);

        mstarConnector.sendRequest(MstarCommand.SET_BUYER_EMAIL, dp);
    }

    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        switch (font) {
            case SMALL:
                return MAX_CHAR_ROW_SMALL;
            case DOUBLEWIDTH:
                return MAX_CHAR_ROW_DOUBLEWIDTH;
            case UNDERLINE:
            case DOUBLEHEIGHT:
            case NORMAL:
            default:
                return MAX_CHAR_ROW;
        }
    }

    @Override
    public int getPaymentLength() {
        return mstarConfig.getMaxPaymentNameLength();
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер следующей
     */
    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        long shiftNumber = mstarAgent.getShiftNumber();
        if (!isShiftOpen()) {
            shiftNumber++;
        }
        return shiftNumber;
    }

    /**
     * Метод возвращает значение сменного счетчика внесений
     */
    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        long result;
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(5L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            result = dp.getLongValue(6);
            LOG.info("CashIn = {}", result);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(5L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            long result = dp.getLongValue(7);
            LOG.info("CashOut = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCountAnnul() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(5L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_COUNTERS, dp);
            long result = dp.getLongValue(5);
            LOG.info("CountAnnul = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(7L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            long result = dp.getDoubleMoneyToLongValue(1);
            LOG.info("CashAmount = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        LOG.info("getStatus");
        StatusFP status = new StatusFP();
        try {
            // Запрос флагов статуса
            DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_STATUS);
            long val = dp.getLongValue(0);
            if (MstarUtils.getBit(val, 5)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("ARCHIVE_FN_CLOSED"));
            }
            if (!status.getDescriptions().isEmpty()) {
                status.setLongStatus(val);
                status.setStatus(StatusFP.Status.FATAL);
                return status;
            }

            // Запрос состояния печатающего устройства
            dp = mstarConnector.sendRequest(MstarCommand.GET_PRINTER_STATE);
            status.setLongStatus(dp.getLongValue(0));
            if (MstarUtils.getBit(status.getLongStatus(), 0)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("WARN_PRINTER_NOT_READY"));
            }
            if (MstarUtils.getBit(status.getLongStatus(), 2)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("OPEN_PRINTER_COVER"));
                status.setStatus(StatusFP.Status.OPEN_COVER);
            }
            if (MstarUtils.getBit(status.getLongStatus(), 1)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("WARN_END_OF_PAPER"));
                status.setStatus(StatusFP.Status.END_PAPER);
            }
            if (MstarUtils.getBit(status.getLongStatus(), 3)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("ERROR_CUTTER_PRINTER"));
            }
            if (MstarUtils.getBit(status.getLongStatus(), 7)) {
                status.addDescription(ResBundleFiscalPrinterMstar.getString("NO_COMMUNICATION_WITH_PRINTER"));
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
        return status;
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterMstar.getString("DEVICE_NAME");
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) throws FiscalPrinterException {
        LOG.info("Reconnect fiscal: {}", mstarAgent.getLoggedInn());
        stop();
        start();
    }

    /**
     * Метод для печати документа по шаблону с пост обработкой
     *
     * @param sectionList Шаблон документа
     * @param document    документ
     */
    @Override
    public void printDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        printDocumentByTemplate(sectionList, document);
    }

    private void printDocumentByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        StatusFP status = getStatus();
        if (status.getStatus() != StatusFP.Status.NORMAL) {
            throw new FiscalPrinterException(status.getDescriptions().get(status.getDescriptions().size() - 1), CashErrorType.FISCAL_ERROR);
        }

        if (document instanceof Check) {
            printCheckByTemplate(sectionList, (Check) document);
        } else if (document instanceof Report) {
            Report report = (Report) document;
            if (!report.isCopy()) {
                printReportByTemplate(sectionList, report);
            } else {
                printReportCopyByTemplate(sectionList, report);
            }
        } else if (document instanceof Money) {
            printMoneyByTemplate(sectionList, (Money) document);
        } else if (document instanceof BonusCFTDocument) {
            printBonusCFTReportByTemplate(sectionList, document);
        } else if (document instanceof DailyLogData) {
            printBankDailyReportByTemplate(sectionList, document);
        } else if (document instanceof PlastekDocument) {
            printPlastekAccrueBonusesReportByTemplate(sectionList, (PlastekDocument) document);
        } else if (document instanceof PresentCardInfoReport) {
            printPresentCardInfoByTemplate(sectionList, document);
        } else if (document instanceof PresentCardReplaceReport) {
            printPresentCardReplcaeByTemplate(sectionList, document);
        } else {
            //Сюда провалятся отчет и др.
            printServiceByTemplate(sectionList, document);
        }
    }

    private void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check.isAnnul()) {
            printAnnulCheckByTemplate(sectionList, check);
        } else if (check.isCopy() || check instanceof FullCheckCopy) {
            printCopyCheckByTemplate(sectionList, check);
        } else {
            try {
                for (DocumentSection section : sectionList) {
                    String sectionName = section.getName();
                    if ("logo".equals(sectionName)) {
                        printLogo();
                    } else if ("header".equals(sectionName)) {
                        openDocument(check, false);
                    } else if ("position".equals(sectionName) || "positionSectionWithGoodSets".equals(sectionName)) {
                        processPositionSection(check, section);
                    } else if ("payment".equals(sectionName)) {
                        putPayments(check.getPayments());
                    } else if ("fiscal".equals(sectionName)) {
                        if (check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                            printDocumentNumberBarcode(check);
                        }
                        if (check.getClientRequisites() != null) {
                            putCustomerAddress(check.getClientRequisites());
                        }
                        closeDocument(true);
                    } else {
                        printLinesList(section.getContent());
                    }
                }
            } catch (FiscalPrinterException fpe) {
                throw fpe;
            } catch (Exception e) {
                throw new FiscalPrinterException(e.getMessage(), e);
            }
        }
    }

    private void printBankDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(document, true);
                    printLinesList(section.getContent());
                } else if ("slip".equals(sectionName)) {
                    printLinesList(section.getContent());
                } else if ("cut".equals(sectionName)) {
                    closeDocument(true);
                } else if (!("footer".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printPlastekAccrueBonusesReportByTemplate(List<DocumentSection> sectionList, PlastekDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();

                if ("report".equals(sectionName)) {
                    openDocument(document, true);
                    printLinesList(section.getContent());
                } else if ("cut".equals(sectionName)) {
                    closeDocument(true);
                } else {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException e) {
            throw e;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printBonusCFTReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case "logo":
                        printLogo();
                        break;
                    case "operationList":
                        openDocument(document, true);
                        printLinesList(section.getContent());
                        break;
                    case "cut":
                        closeDocument(true);
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printServiceByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(document, true);
                } else if ("cut".equals(sectionName)) {
                    closeDocument(true);
                } else if (!("footer".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printPresentCardInfoByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(document, true);
                } else if ("cut".equals(sectionName)) {
                    closeDocument(true);
                } else if (!("footer".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printPresentCardReplcaeByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(document, true);
                } else if ("cut".equals(sectionName)) {
                    closeDocument(true);
                } else if (!("footer".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    if (money.getOperationType() == InventoryOperationType.DECLARATION) {
                        openDocument(money, true);
                    } else {
                        if (money.isInventoryDocument()) {
                            openDocument(money, true);
                        } else {
                            openDocument(money, false);
                        }
                    }
                } else if ("fiscal".equals(sectionName)) {
                    if (money.getOperationType() != InventoryOperationType.DECLARATION) {
                        fiscalMoneyDocument(money);
                    }
                    closeDocument(true);
                } else if (!("footer".equals(sectionName) || "cut".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(report, true);
                } else if ("fiscal".equals(sectionName)) {
                    closeDocument(true);
                    if (report.isZReport()) {
                        printZReport(report);
                    } else if (report.isXReport()) {
                        printXReport(report);
                    }
                } else if (!("footer".equals(sectionName) || "cut".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printReportCopyByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            if (report.isXReport()) {
                throw new UnsupportedOperationException("X-report's copy doesn't support");
            } else {
                for (DocumentSection section : sectionList) {
                    String sectionName = section.getName();
                    if ("logo".equals(sectionName)) {
                        printLogo();
                    } else if ("header".equals(sectionName)) {
                        openDocument(report, true);
                        printLinesList(section.getContent());
                    } else if ("fiscal".equals(sectionName)) {
                        closeDocument(true);
                    } else if ("footer".equals(sectionName)) {
                        //ничего
                    } else {
                        printLinesList(section.getContent());
                    }
                }
            }
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void processPositionSection(Check check, DocumentSection section) throws Exception {
        if (check.isCopy() || check.isAnnul() || check instanceof FullCheckCopy) {
            printLinesList(section.getContent());
            fiscalizeSum((double) check.getCheckSumEnd() / 100);
        } else {
            processPositionSectionOFD(check, section);
        }
    }

    private void processPositionSectionOFD(Check check, DocumentSection section) throws Exception {
        putGoods(check.getGoods(), false);
        printLinesList(section.getContent());
        mstarConnector.sendRequest(MstarCommand.SUBTOTAL, false);
    }

    private void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(check, true);
                    printLinesList(section.getContent());
                } else if ("fiscal".equals(sectionName)) {
                    printDocumentNumberBarcode(check);
                    closeDocument(true);
                } else if (!("footer".equals(sectionName) || "cut".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                String sectionName = section.getName();
                if ("logo".equals(sectionName)) {
                    printLogo();
                } else if ("header".equals(sectionName)) {
                    openDocument(check, false);
                } else if ("position".equals(sectionName)) {
                    printLinesList(section.getContent());
                } else if ("payment".equals(sectionName)) {
                    fiscalizeSum((double) check.getCheckSumEnd() / 100);
                    putPayments(check.getPayments());
                } else if ("fiscal".equals(sectionName)) {
                    annulCheck();
                } else if (!("total".equals(sectionName) || "footer".equals(sectionName) || "cut".equals(sectionName))) {
                    printLinesList(section.getContent());
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    public void printLogo() {
        // сейчас вообще не печатается на уровне аппарата
        // планируется добавить поддержку логотипа в будущем
    }

    private void annulCheck() throws Exception {
        mstarConnector.sendRequest(MstarCommand.CANCEL_DOCUMENT);
    }

    private void fiscalMoneyDocument(Money money) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putStringValue(money.getCurrency());
        dp.putDoubleValue((double) money.getValue() / PRICE_ORDER);
        mstarConnector.sendRequest(MstarCommand.ADD_MONEY_IN_OUT, dp, false);
    }

    private long getTaxSystem() {
        return taxSystem;
    }

    private void openDocument(FiscalDocument doc, boolean isServiceDoc) throws Exception {
        DataPacket dp = mstarConnector.sendRequest(MstarCommand.GET_STATUS);
        long stateDocument = dp.getLongValue(1);
        boolean openDoc = ((stateDocument & 0x1F) != 0);
        if ((dp.getLongValue(1) & 1) != 0) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("WARN_NEED_RESTART"), MstarErrorMsg.getErrorType());
        }
        if (openDoc) {
            annulCheck();
        }

        dp.clear();
        if (isServiceDoc) {
            dp.putLongValue(DocumentType.SERVICE_DOCUMENT.getValue());
        } else if (doc instanceof Check) {
            Check check = (Check) doc;
            long value = 0;
            if (check.getType() == CheckType.SALE) {
                value = DocumentType.SALE.getValue();
            } else if (check.getType() == CheckType.RETURN) {
                value = DocumentType.RETURN_SALE.getValue();
            }
            dp.putLongValue(value);
        } else if (doc instanceof Money) {
            Money money = (Money) doc;
            if (money.getOperationType() == InventoryOperationType.CASH_IN) {
                dp.putLongValue(DocumentType.CASH_IN.getValue());
            } else {
                validateCashOutMoney(money);
                dp.putLongValue(DocumentType.CASH_OUT.getValue());
            }
        } else {
            dp.putLongValue(DocumentType.SERVICE_DOCUMENT.getValue());
        }

        dp.putLongValue(getDocDepart(doc.getDepart()));
        dp.putStringValue(getCashierName(Optional.ofNullable(doc.getCashier()).orElse(new Cashier("", "", ""))));
        // Параметр «Номер документа» не задаётся и передаётся пустым значением
        dp.putLongValue(null);

        mstarConnector.sendRequest(MstarCommand.OPEN_DOCUMENT, dp, false);
    }

    private void validateCashOutMoney(Money money) throws Exception {
        if (money.getValue() > getCashAmount() && mstarConfig.isCashDrawerMoneyControl()) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("ERROR_SUM_GREATER_CASH_AMOUNT"), MstarErrorMsg.getErrorType());
        }
    }

    private void printText(Text text) throws Exception {
        printText(text, MAX_CHAR_ROW);
    }

    private void printText(Text text, int maxCharRow) throws Exception {
        if (text.getValue().length() > maxCharRow) {
            String totalText = text.getValue();
            while (totalText.length() > 0) {
                if (totalText.length() > maxCharRow) {
                    String row = totalText.substring(0, maxCharRow);
                    if ((row.lastIndexOf(SPACE) != row.length()) && (row.lastIndexOf(SPACE) > 0)) {
                        text.setValue(totalText.substring(0, row.lastIndexOf(SPACE)));
                        totalText = totalText.substring(text.getValue().length() + 1);
                    } else {
                        text.setValue(totalText.substring(0, maxCharRow));
                        totalText = totalText.substring(maxCharRow);
                    }
                } else {
                    text.setValue(totalText);
                    totalText = "";
                }
                printString(text.getValue(), getTextAttribute(text));
            }
        } else {
            printString(text.getValue(), getTextAttribute(text));
        }
    }

    private Long getTextAttribute(Text text) {
        switch (text.getSize()) {
            case SMALL:
                return 4L;
            case DOUBLE_HEIGHT:
                return 3L;
            case DOUBLE_WIDTH:
                return 2L;
            case NORMAL:
            default:
                return 0L;
        }
    }

    private void printLinesList(List<FontLine> stringList) throws Exception {
        for (FontLine str : stringList) {
            if (str != null) {
                printLine(str);
            }
        }
    }

    protected void printLine(FontLine line) throws Exception {
        String content = Optional.ofNullable(line).map(FontLine::getContent).orElse(StringUtils.EMPTY);
        content = content.length() > mstarConfig.getMaxCharCountInPrintCommand() ?
                content.substring(0, mstarConfig.getMaxCharCountInPrintCommand()) : content;
        printString(content, getFontAttribute(line));
    }

    private Long getFontAttribute(FontLine line) {
        switch (line.getFont()) {
            case SMALL:
                return 4L;
            case DOUBLEHEIGHT:
                return 3L;
            case DOUBLEWIDTH:
                return 2L;
            case UNDERLINE:
                // не поддерживается
            case NORMAL:
            default:
                return 0L;
        }
    }

    private void printString(String text, Long attribute) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putStringValue(text);
        dp.putLongValue(attribute);
        mstarConnector.sendRequest(MstarCommand.PRINT_STRING, dp, false);
    }

    private void fiscalizeSum(double sum) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putStringValue(ResBundleFiscalPrinterMstar.getString("TOTAL"));
        dp.putStringValue("");
        dp.putDoubleValue(1.0);
        dp.putDoubleValue(sum);
        mstarConnector.sendRequest(MstarCommand.ADD_ITEM, dp, false);

        mstarConnector.sendRequest(MstarCommand.SUBTOTAL, false);
    }

    private void closeDocument(boolean isNeedCut) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putLongValue(isNeedCut ? 0L : 1L);
        mstarConnector.sendRequest(MstarCommand.CLOSE_DOCUMENT, dp, false);
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(1L);
            dp = mstarConnector.sendRequest(MstarCommand.GET_INFO, dp);
            return dp.getStringValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isOFDDevice() {
        return true;
    }

    /**
     * Формирование отчёта о состоянии расчётов
     */
    @Override
    public void printFNReport(Cashier cashier) throws FiscalPrinterException {
        try {
            mstarConnector.sendRequest(MstarCommand.REPORT_STATUS_SETTLEMENTS);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFFDDevice() {
        return true;
    }
}
