package ru.crystals.pos.fiscalprinter.atol.universal.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.atol.drivers10.fptr.Fptr;
import ru.atol.drivers10.fptr.IFptr;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.catalog.MarkType;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.fiscalprinter.Ffd;
import ru.crystals.pos.fiscalprinter.FfdVersion;
import ru.crystals.pos.fiscalprinter.FiscalMarkValidationUtil;
import ru.crystals.pos.fiscalprinter.atol.universal.AtolUniversalConfig;
import ru.crystals.pos.fiscalprinter.atol.universal.ResBundleFiscalPrinterAtolUniversal;
import ru.crystals.pos.fiscalprinter.atol.universal.json.OverallTotalsDTO;
import ru.crystals.pos.fiscalprinter.atol.universal.json.ShiftTotalsDTO;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdditionalInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AgentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterNeedToRevalidateCodeMarks;
import ru.crystals.pos.utils.Timer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class AtolUniversalConnector {

    private static final Logger LOG = LoggerFactory.getLogger(AtolUniversalConnector.class);

    private static final Map<Float, Integer> TAXES = new ImmutableMap.Builder<Float, Integer>()
            .put(20.0f, IFptr.LIBFPTR_TAX_VAT20)
            .put(10.0f, IFptr.LIBFPTR_TAX_VAT10)
            .put(0.0f, IFptr.LIBFPTR_TAX_VAT0)
            .put(-1.0f, IFptr.LIBFPTR_TAX_NO)
            .put(-20.0f, IFptr.LIBFPTR_TAX_VAT120)
            .put(-10.0f, IFptr.LIBFPTR_TAX_VAT110)
            // для исключительных случаев
            .put(18.0f, IFptr.LIBFPTR_TAX_VAT18)
            .put(-18.0f, IFptr.LIBFPTR_TAX_VAT118)
            .build();
    private static final Map<Long, Integer> FFD_TO_ATOL_PAYMENT_TYPE = ImmutableMap.of(
            0L, IFptr.LIBFPTR_PT_CASH,
            1L, IFptr.LIBFPTR_PT_ELECTRONICALLY,
            13L, IFptr.LIBFPTR_PT_PREPAID,
            14L, IFptr.LIBFPTR_PT_CREDIT,
            15L, IFptr.LIBFPTR_PT_OTHER
    );

    private static final Map<CashOperation, Map<CheckType, Integer>> CHECK_TYPE_TO_FP_TYPE = ImmutableMap.of(
            CashOperation.INCOME, ImmutableMap.of(
                    CheckType.SALE, IFptr.LIBFPTR_RT_SELL,
                    CheckType.RETURN, IFptr.LIBFPTR_RT_SELL_RETURN
            ),
            CashOperation.EXPENSE, ImmutableMap.of(
                    CheckType.SALE, IFptr.LIBFPTR_RT_BUY,
                    CheckType.RETURN, IFptr.LIBFPTR_RT_BUY_RETURN
            )
    );

    private static final Map<MarkType, Integer> MARK_TYPE_TO_NOMENCLATURE_TYPE = ImmutableMap.of(
            MarkType.TOBACCO, IFptr.LIBFPTR_NT_TOBACCO,
            MarkType.FOOTWEAR, IFptr.LIBFPTR_NT_SHOES
    );

    private static final Map<Integer, FfdVersion> FFD_VERSION_BY_CODE = ImmutableMap.of(
            IFptr.LIBFPTR_FFD_1_0, FfdVersion.FFD_1_0,
            IFptr.LIBFPTR_FFD_1_0_5, FfdVersion.FFD_1_05,
            IFptr.LIBFPTR_FFD_1_1, FfdVersion.FFD_1_1,
            IFptr.LIBFPTR_FFD_1_2, FfdVersion.FFD_1_2
    );

    private static final long VALIDATION_STEP_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(500);
    private static final long VALIDATION_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    private static final String GS = "\u001D";

    private final AtolUniversalConfig config;

    private IFptr fptr;

    /**
     * {@code true} если версия 5.X
     */
    private boolean isNewPlatform;

    private FfdVersion ffdVersion;

    public AtolUniversalConnector(AtolUniversalConfig config) {
        this.config = config;
    }

    public void start() throws FiscalPrinterException {
        LOG.debug("provider start...");

        driverLogSettings();

        EthernetOverUsbService.start();

        Path libPath = Paths.get("").resolve("lib").resolve("atol");
        if (SystemUtils.IS_OS_WINDOWS) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                libPath = libPath.resolve("win_x64");
            } else {
                libPath = libPath.resolve("win_x32");
            }
        }
        fptr = configureFptr(libPath.toAbsolutePath().toString());

        if (fptr.open() != 0) {
            LOG.error("Error on open port with given settings (port {}, baudRate {}): {} ({})", config.getPort(), config.getBaudRate(),
                    fptr.errorDescription(), fptr.errorCode());
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }

        // на случай перезагрузки, т.к. не вызывается метод stop(), все равно закроем fptr
        Runtime.getRuntime().addShutdownHook(new Thread(() -> fptr.close()));

        isNewPlatform = isNewPlatform();
        ffdVersion = getFfdVersion();
        initSettings();

        LOG.debug("provider started: {}", fptr.isOpened());
    }

    /**
     * Создаем и конфигурирем экземпляр драйвера
     * <p>
     * При работе с двумя ФР обязательно задание id, формируем его из имени порта, исключив предварительно запрещенные символы.
     * <p>
     * Возможные значения port в конфиге:
     * <ul>
     * <li>пустое значение (только при подключении одного устройства) - автоматическое определение при подключении по USB</li>
     * <li>USB3-1 или 3-1 - путь к USB-порту устройства, значение можно вычислить с помощью команды {@code lsusb -t}:
     * первое число - номер Bus, второе - вложенный номер порта. Изменяется при подключении устройства в другой USB-порт.</li>
     * <li>другое значение расмматривается как COM-порт, включая симлинки для USB (/dev/usbSV2912P0005 - симлинк для Product/Vendor id, /dev/usbATOLSN00202720 -
     * симлинк для Атола 77Ф с серийным номером)</li>
     * </ul>
     * <p>
     * Для подключения по USB рекомендуется использовать симлинки
     */
    private IFptr configureFptr(String libraryPath) throws FiscalPrinterException {
        final String port = config.getPort();
        if (port == null) {
            LOG.debug("Trying to connect to device using driver auto detection");
            return new Fptr(libraryPath);
        }
        final IFptr fptr = new Fptr(normalizePortAsId(port), libraryPath);
        fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_MODEL, String.valueOf(IFptr.LIBFPTR_MODEL_ATOL_AUTO));
        fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_BAUDRATE, config.getBaudRate());
        if (port.startsWith("USB") || port.matches("^\\d+-\\d+$")) {
            String usbPath = StringUtils.removeStart(port, "USB");
            LOG.debug("Trying to connect to device using USB path ({})", usbPath);
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, String.valueOf(IFptr.LIBFPTR_PORT_USB));
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_USB_DEVICE_PATH, usbPath);
        } else {
            LOG.debug("Trying to connect to device using serial port ({})", port);
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_PORT, String.valueOf(IFptr.LIBFPTR_PORT_COM));
            fptr.setSingleSetting(IFptr.LIBFPTR_SETTING_COM_FILE, port);
        }
        if (fptr.applySingleSettings() != 0) {
            LOG.error("Error on apply given settings (port {}, baudRate {}): {} ({})", port, config.getBaudRate(), fptr.errorDescription(), fptr.errorCode());
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        return fptr;
    }

    /**
     * ID экземпляра не может содержать некоторые символы
     */
    private String normalizePortAsId(String port) {
        return port
                .replace(".", "d")
                .replace(":", "c")
                .replace("/dev/", "");
    }

    private boolean isNewPlatform() {
        fptr.readModelFlags();
        return fptr.getParamBool(IFptr.LIBFPTR_PARAM_NEW_PLATFORM);
    }

    public FfdVersion getFfdVersion() {
        if (ffdVersion == null) {
            fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_FFD_VERSIONS);
            fptr.fnQueryData();
            int ffdResult = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_FFD_VERSION);
            ffdVersion = FFD_VERSION_BY_CODE.getOrDefault(ffdResult, FfdVersion.UNKNOWN);
        }
        return ffdVersion;
    }

    /**
     * Создаем директорию для логов драйвера, настройки и пути прописаны в crystal-cash/lib/log.properties
     * <p>
     * Для настройки логов при локальном запуске нужно:
     * 1. Добавить переменную среды DTO10_LOG_CONFIG_FILE, в которой указать путь до файла log.properties
     * (папка lib/atol в дистрибутиве, если там нет, можно скопировать из исходников)
     * 2. В файле log.properties заменить пути к файлам .../crystal-cash/logs/atol/xxx.log на соответствующие пути до папки logs/atol
     */
    private static void driverLogSettings() {
        try {
            Path logDirectoryPath = Paths.get("").resolve("logs").resolve("atol");
            if (!Files.exists(logDirectoryPath)) {
                Files.createDirectories(logDirectoryPath);
            }
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    private void initSettings() {
        // --------------- отрезка ----------
        // отрезать чек после завершения документа
        writeSetting(AtolSetting.CUT_AFTER_DOCUMENT, 1);
        // не запрещать отрезку чеков
        writeSetting(AtolSetting.FORBID_CUT_AFTER_CHECK, 0);
        // не запрещать отрезку отчетов
        writeSetting(AtolSetting.FORBID_CUT_AFTER_REPORT, 0);
        // отрезать ЧЛ после печати клише командой "Печать клише"
        writeSetting(AtolSetting.CUT_AFTER_CLICHE, 1);

        // ---------------- ДЯ ----------------
        // не открывать ДЯ при закрытии чека
        writeSetting(AtolSetting.OPEN_DRAWER_ON_CHECK_CLOSE, 0);
    }

    /**
     * Запись значения пользовательского параметра в память ФР по номеру ячейки.
     *
     * @param setting ячейка (номер)
     * @param value   значение
     */
    private void writeSetting(AtolSetting setting, long value) {
        fptr.setParam(IFptr.LIBFPTR_PARAM_SETTING_ID, setting.getNum());
        fptr.setParam(IFptr.LIBFPTR_PARAM_SETTING_VALUE, value);
        fptr.writeDeviceSetting();
        fptr.commitSettings();
    }

    public void stop() {
        LOG.debug("provider stop...");
        fptr.close();
        LOG.debug("provider stopped");
    }

    public synchronized String getRegNum() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_REG_INFO);
        fptr.fnQueryData();
        return fptr.getParamString(Ffd.TAG_1037);
    }

    public synchronized String getINN() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_REG_INFO);
        fptr.fnQueryData();
        return StringUtils.trim(fptr.getParamString(Ffd.TAG_1018));
    }

    public synchronized String getEklzNum() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_FN_INFO);
        fptr.fnQueryData();
        return fptr.getParamString(IFptr.LIBFPTR_PARAM_SERIAL_NUMBER);
    }

    public synchronized long getShiftNumber() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_SHIFT_STATE);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_SHIFT_NUMBER);
    }

    public synchronized boolean isShiftOpen() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_SHIFT_STATE);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_SHIFT_STATE) != IFptr.LIBFPTR_SS_CLOSED;
    }

    public synchronized long getLastDocNum() {
        try {
            fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_LAST_DOCUMENT);
            fptr.fnQueryData();
            return fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENT_NUMBER);
        } catch (Exception e) {
            LOG.warn("getLastDocNum parsing error", e);
            return 0L;
        }
    }

    public synchronized long getCountCashIn() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_CASHIN_COUNT);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENTS_COUNT);
    }

    public synchronized long getCountCashOut() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_CASHOUT_COUNT);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENTS_COUNT);
    }

    public synchronized Long getCountAnnul() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_CANCELLATION_COUNT_ALL);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENTS_COUNT);
    }

    public synchronized Long getCashAmount() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_CASH_SUM);
        fptr.queryData();
        return BigDecimalConverter.convertMoneyToLong(fptr.getParamDouble(IFptr.LIBFPTR_PARAM_SUM));
    }

    public synchronized FiscalDocumentData getLastFiscalDocumentData(long numFD) {
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_DOCUMENT_BY_NUMBER);
        fptr.setParam(IFptr.LIBFPTR_PARAM_DOCUMENT_NUMBER, numFD);
        fptr.fnQueryData();
        FiscalDocumentData fiscalDocumentData = new FiscalDocumentData();
        fiscalDocumentData.setNumFD(fptr.getParamInt(IFptr.LIBFPTR_PARAM_DOCUMENT_NUMBER));
        String fs = fptr.getParamString(IFptr.LIBFPTR_PARAM_FISCAL_SIGN);
        if (StringUtils.isNotBlank(fs)) {
            // иначе NPE, если вызывается метод сразу после включения ФР (потеря питания, например)
            fiscalDocumentData.setFiscalSign(Long.parseLong(fs));
        }
        fiscalDocumentData.setOperationDate(fptr.getParamDateTime(IFptr.LIBFPTR_PARAM_DATE_TIME));
        FiscalDocumentType fdType = getFiscalDocumentType(fptr.getParamInt(IFptr.LIBFPTR_PARAM_FN_DOCUMENT_TYPE));
        fiscalDocumentData.setType(fdType);
        if (fdType == FiscalDocumentType.UNKNOWN) {
            return fiscalDocumentData;
        }
        // нашли чек - заполняем остальные данные
        fiscalDocumentData.setSum(BigDecimalConverter.convertMoneyToLong(fptr.getParamDouble(Ffd.TAG_1020)));
        return fiscalDocumentData;
    }

    private FiscalDocumentType getFiscalDocumentType(long frDocumentType) {
        if (frDocumentType != IFptr.LIBFPTR_FN_DOC_RECEIPT) {
            return FiscalDocumentType.UNKNOWN;
        }
        switch ((int) fptr.getParamInt(Ffd.TAG_1054)) {
            case 1:
                return FiscalDocumentType.SALE;
            case 2:
                return FiscalDocumentType.REFUND;
            case 3:
                return FiscalDocumentType.EXPENSE;
            case 4:
                return FiscalDocumentType.EXPENSE_REFUND;
            default:
                return FiscalDocumentType.UNKNOWN;
        }
    }

    public synchronized OverallTotalsDTO.OverallTotals getOverallTotals() throws FiscalPrinterException {
        return processJsonCommand(JsonCommand.OVERALL_TOTALS, OverallTotalsDTO.class).getOverallTotals();
    }

    public synchronized ShiftTotalsDTO.ShiftTotals getShiftTotals() throws FiscalPrinterException {
        return processJsonCommand(JsonCommand.SHIFT_TOTALS, ShiftTotalsDTO.class).getShiftTotals();
    }

    private <T> T processJsonCommand(JsonCommand jsonCommand, Class<T> returnType) throws FiscalPrinterException {
        fptr.setParam(IFptr.LIBFPTR_PARAM_JSON_DATA, "{\"type\": \"" + jsonCommand.getCommand() + "\"}");
        fptr.processJson();
        String response = fptr.getParamString(IFptr.LIBFPTR_PARAM_JSON_DATA);
        if (response != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response, returnType);
            } catch (IOException e) {
                throw new FiscalPrinterException("error while parsing json from fiscal printer", e);
            }
        } else {
            throw new FiscalPrinterException("error while process json by fiscal printer");
        }
    }

    public synchronized int getMaxCharRow() {
        // получение количества символов в зависимости от шрифта поддерживается только для версий 5.X,
        // поэтому берем значение из настроек
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_RECEIPT_LINE_LENGTH);
        if (fptr.queryData() < 0) {
            LOG.error("Error on get max char row: {} ({})", fptr.errorDescription(), fptr.errorCode());
            return 42;
        }
        return (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_RECEIPT_LINE_LENGTH);
    }

    public synchronized String getFactoryNum() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_SERIAL_NUMBER);
        fptr.queryData();
        return fptr.getParamString(IFptr.LIBFPTR_PARAM_SERIAL_NUMBER);
    }

    public synchronized void setDate(Date date) {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATE_TIME, date);
        fptr.writeDateTime();
    }

    public synchronized Date getDate() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_DATE_TIME);
        fptr.queryData();
        return fptr.getParamDateTime(IFptr.LIBFPTR_PARAM_DATE_TIME);
    }

    public synchronized String getVerBios() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_UNIT_VERSION);
        fptr.setParam(IFptr.LIBFPTR_PARAM_UNIT_TYPE, IFptr.LIBFPTR_UT_FIRMWARE);
        fptr.queryData();
        return fptr.getParamString(IFptr.LIBFPTR_PARAM_UNIT_VERSION);
    }

    private void checkAndContinue() throws FiscalPrinterException {
        if (fptr.checkDocumentClosed() < 0) {
            // Не удалось проверить состояние документа. Вывести пользователю текст ошибки
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        if (!fptr.getParamBool(IFptr.LIBFPTR_PARAM_DOCUMENT_CLOSED)) {
            // если закрытие открытого документа провалилось, но
            // 81 - код ошибки "Чек закрыт - операция невозможна" - в данном случае все ок
            if (fptr.cancelReceipt() < 0 && fptr.errorCode() == IFptr.LIBFPTR_ERROR_DENIED_IN_CLOSED_RECEIPT) {
                return;
            }
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("DOCUMENT_NOT_CLOSED"),
                    CashErrorType.FISCAL_ERROR);
        }
        continuePrint();
    }

    public synchronized void annulCheckIfNotClosed() throws FiscalPrinterException {
        if (isDocOpen() && fptr.cancelReceipt() != 0) {
            if (fptr.errorCode() == IFptr.LIBFPTR_ERROR_CLOSE_RECEIPT_INTERRUPTED) {
                LOG.warn("Error {} - trying to continuePrint", fptr.errorCode());
                continuePrint();
            } else {
                throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
            }
        }
    }

    private void continuePrint() throws FiscalPrinterException {
        if (fptr.getParamBool(IFptr.LIBFPTR_PARAM_DOCUMENT_PRINTED)) {
            return;
        }
        // Можно сразу вызвать метод допечатывания документа
        LOG.debug("Trying to continue printing");
        if (fptr.continuePrint() < 0) {
            // Если не удалось допечатать документ - показать пользователю ошибку
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
    }

    private boolean isDocOpen() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_RECEIPT_STATE);
        fptr.queryData();
        return fptr.getParamInt(IFptr.LIBFPTR_PARAM_RECEIPT_TYPE) != IFptr.LIBFPTR_RT_CLOSED;
    }

    public synchronized long openShift(Cashier cashier) throws FiscalPrinterException {
        updateMarkCodeKeys();
        fillCashierData(cashier);
        if (fptr.openShift() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        checkAndContinue();
        return getShiftNumber();
    }

    /**
     * Обновление ключей проверки КМ в ФН
     */
    private void updateMarkCodeKeys() {
        LOG.trace("Mark code keys update starts");
        long start = System.currentTimeMillis();
        fptr.updateFnmKeys();
        LOG.trace("Keys updated in: {} ms", System.currentTimeMillis() - start);
    }

    public synchronized void fiscalMoneyDocument(Money money) throws FiscalPrinterException {
        if (money.getValue() == 0) {
            LOG.warn("do not perform cash in/out because of zero sum");
            return;
        }
        switch (money.getOperationType()) {
            case CASH_IN:
                fptr.setParam(IFptr.LIBFPTR_PARAM_SUM, BigDecimalConverter.convertMoneyToDouble(money.getValue()));
                if (fptr.cashIncome() != 0) {
                    throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
                }
                break;
            case CASH_OUT:
                fptr.setParam(IFptr.LIBFPTR_PARAM_SUM, BigDecimalConverter.convertMoneyToDouble(money.getValue()));
                if (fptr.cashOutcome() != 0) {
                    throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
                }
                break;
            default:
                // не ожидаем никаких других типов операций здесь
                break;
        }
    }

    public synchronized void putText(Text text) throws FiscalPrinterException {
        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, text.getValue());
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_LEFT);
        if (text.getConcreteFont() != null) {
            fptr.setParam(IFptr.LIBFPTR_PARAM_FONT, text.getConcreteFont());
        }
        fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_WIDTH, text.getSize() == TextSize.DOUBLE_WIDTH);
        fptr.setParam(IFptr.LIBFPTR_PARAM_FONT_DOUBLE_HEIGHT, text.getSize() == TextSize.DOUBLE_HEIGHT);
        if (fptr.printText() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
    }

    private TaxMode getTaxMode() {
        // получаем количество регистраций
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_LAST_REGISTRATION);
        fptr.fnQueryData();
        long registrationsCount = fptr.getParamInt(IFptr.LIBFPTR_PARAM_REGISTRATIONS_COUNT);

        // запрашиваем налоги из данных последней регистрации
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_REGISTRATION_TLV);
        fptr.setParam(IFptr.LIBFPTR_PARAM_REGISTRATION_NUMBER, registrationsCount);
        fptr.setParam(IFptr.LIBFPTR_PARAM_TAG_NUMBER, Ffd.TAG_1062);
        fptr.fnQueryData();

        return TaxMode.getFirstByMask(fptr.getParamInt(IFptr.LIBFPTR_PARAM_TAG_VALUE));
    }

    private void fillCashierData(Cashier cashier) {
        fptr.setParam(Ffd.TAG_1021, cashier.getCashierStringForOFDTag1021());
        if (cashier.getInn() != null) {
            fptr.setParam(Ffd.TAG_1203, cashier.getInn());
        }
        fptr.operatorLogin();
    }

    public synchronized void openDocument(FiscalDocument document) throws FiscalPrinterException {
        TaxMode taxMode = getTaxMode();
        annulCheckIfNotClosed();
        if (document instanceof Check) {
            Check check = (Check) document;
            openDocument(check, taxMode);
        }
    }

    private void openDocument(Check check, TaxMode taxMode) throws FiscalPrinterException {
        fillCashierData(check.getCashier());
        fillClientData(check);
        fptr.setParam(IFptr.LIBFPTR_PARAM_RECEIPT_TYPE, CHECK_TYPE_TO_FP_TYPE.get(check.getOperation()).get(check.getType()));
        fptr.setParam(Ffd.TAG_1055, taxMode.getMask());
        if (fptr.openReceipt() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
    }

    private void fillClientData(Check check) {
        if (ffdVersion.isBefore_1_2()) {
            // по требованиям передаем эти теги только для версий ФФД начиная с 1.2,
            // хотя, если верить документации, возможность передачи есть и на более старых версиях
            return;
        }
        // атол без ошибок принимает null для этих тегов, формирует пустой составной тег и не включает его в чек
        fptr.setParam(Ffd.TAG_1227, check.getClientName());
        fptr.setParam(Ffd.TAG_1228, check.getClientINN());
        fptr.setParam(Ffd.TAG_1254, check.getClientAddress());
        fptr.utilFormTlv();
        byte[] tag1256 = fptr.getParamByteArray(IFptr.LIBFPTR_PARAM_TAG_VALUE);
        fptr.setParam(Ffd.TAG_1256, tag1256);
    }

    public synchronized void closeDocument() throws FiscalPrinterException {
        if (fptr.closeReceipt() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        checkAndContinue();
    }

    public synchronized void openServiceDocument() throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheckIfNotClosed();
        }
        openNonfiscalDocument();
    }

    public synchronized void putGoods(List<Goods> positions, Check check) throws FiscalPrinterException {
        for (Goods position : positions) {
            putPaymentAgent(position);
            if (isNotBlank(position.getExcise())) {
                if (ffdVersion.isSince_1_2()) {
                    registerMarkCodeData(position);
                } else {
                    sendMarkCode(position);
                }
            }
            putSingleGoodsParams(position);

            if (position.getCalculationMethod() != null) {
                fptr.setParam(Ffd.TAG_1214, position.getCalculationMethod());
            }
            Optional.ofNullable(position.getCalculationSubject()).ifPresent(calcSubject -> fptr.setParam(Ffd.TAG_1212, calcSubject));

            if (ffdVersion.isSince_1_2()) {
                fptr.setParam(Ffd.TAG_2108, FiscalMarkValidationUtil.formTag2108(position));
            }

            if (fptr.registration() != 0) {
                analyzeRegistrationError(position, check);
            }
        }
    }

    private void analyzeRegistrationError(Goods position, Check check) throws FiscalPrinterException {
        int errorCode = fptr.errorCode();
        String errorDesc = fptr.errorDescription();
        if (errorCode == 420) {
            // 420 = В реквизите 2007 содержится КМ, который ранее не проверялся в ФН
            throw new FiscalPrinterNeedToRevalidateCodeMarks(
                    ResBundleFiscalPrinterAtolUniversal.getString("MARKED_GOODS_SHOULD_BE_REVALIDATED"),
                    CashErrorType.FISCAL_ERROR,
                    getINN());
        } else if (errorCode == 117 && ffdVersion.isSince_1_2() && isNotBlank(position.getExcise())) {
            // 117 = Неверное состояние ФН
            fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_MARKING_MODE_STATUS);
            fptr.fnQueryData();
            int countFnMarks = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_MARK_CHECKING_COUNT);
            int countCashMarks = (int) check.getGoods().stream().filter(good -> StringUtils.isNotBlank(good.getExcise())).count();
            if (countFnMarks < countCashMarks) {
                throw new FiscalPrinterNeedToRevalidateCodeMarks(
                        ResBundleFiscalPrinterAtolUniversal.getString("MARKED_GOODS_SHOULD_BE_REVALIDATED"),
                        CashErrorType.FISCAL_ERROR,
                        getINN());
            }
        }
        throw new FiscalPrinterException(errorDesc, CashErrorType.FISCAL_ERROR, errorCode);
    }

    private void registerMarkCodeData(Goods position) {
        FiscalMarkValidationResult validationResult = position.getMarkValidationResult().getFiscalResult();
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE, (String) validationResult.getInput().get(FiscalMarkValidationUtil.MARK_KEY));
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE_STATUS, (int) validationResult.getInput().get(FiscalMarkValidationUtil.TAG_2003_KEY));
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_PROCESSING_MODE, 0);
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE_ONLINE_VALIDATION_RESULT, validationResult.getTag2106());
    }

    public synchronized void putBarCode(BarCode barcode) {
        fptr.setParam(IFptr.LIBFPTR_PARAM_BARCODE, barcode.getValue());
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        if (barcode.getType() == BarCodeType.QR) {
            fptr.setParam(IFptr.LIBFPTR_PARAM_BARCODE_TYPE, IFptr.LIBFPTR_BT_QR);
            fptr.setParam(IFptr.LIBFPTR_PARAM_SCALE, config.getQrCodeScaleFactor());
        } else {
            fptr.setParam(IFptr.LIBFPTR_PARAM_BARCODE_TYPE, IFptr.LIBFPTR_BT_CODE_128);
        }
        if (fptr.printBarcode() != 0) {
            LOG.warn("Error occurred while printing barcode. Description: {}, error: {}, error code: {}", fptr.errorDescription(),
                    CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        fptr.setParam(IFptr.LIBFPTR_PARAM_TEXT, barcode.getBarcodeLabel());
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.printText();
    }

    public synchronized void closeDocument(Check check) throws FiscalPrinterException {
        LOG.debug("closeDocument()");
        if (isDocOpen()) {
            closeDocument();

            if (check != null && !check.isCopy()) {
                check.setDateForPrint(getLastCheckDateTime());
            }
        }
    }

    private Date getLastCheckDateTime() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_FN_DATA_TYPE, IFptr.LIBFPTR_FNDT_LAST_RECEIPT);
        fptr.fnQueryData();
        return fptr.getParamDateTime(IFptr.LIBFPTR_PARAM_DATE_TIME);
    }

    public synchronized void printXReport(Cashier cashier) throws FiscalPrinterException {
        fillCashierData(cashier);
        fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_X);
        if (fptr.report() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
        checkAndContinue();
    }

    public synchronized void printZReport(Cashier cashier) throws FiscalPrinterException {
        fillCashierData(cashier);
        fptr.setParam(IFptr.LIBFPTR_PARAM_REPORT_TYPE, IFptr.LIBFPTR_RT_CLOSE_SHIFT);
        if (fptr.report() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR_REBOOT, fptr.errorCode());
        }
        checkAndContinue();
    }

    public synchronized void openMoneyDrawer() throws FiscalPrinterException {
        if (fptr.openDrawer() != 0) {
            throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
        }
    }

    public synchronized boolean isMoneyDrawerOpen() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_SHORT_STATUS);
        fptr.queryData();
        boolean frResponse = fptr.getParamBool(IFptr.LIBFPTR_PARAM_CASHDRAWER_OPENED);
        return config.isInvertDrawerState() != frResponse;
    }

    public synchronized StatusFP getStatus() {
        StatusFP result = new StatusFP();
        result.setStatus(StatusFP.Status.NORMAL);
        fptr.setParam(IFptr.LIBFPTR_PARAM_DATA_TYPE, IFptr.LIBFPTR_DT_SHORT_STATUS);
        if (fptr.queryData() != 0) {
            // оставляем статус NORMAL, иначе FiscalPrinterImpl сам подставит сообщение про фатальную ошибку
            result.getDescriptions().add(ResBundleFiscalPrinterAtolUniversal.getString("NO_COMMUNICATION_WITH_PRINTER"));
            return result;
        }

        if (fptr.getParamBool(IFptr.LIBFPTR_PARAM_COVER_OPENED)) {
            result.setStatus(StatusFP.Status.OPEN_COVER);
            result.getDescriptions().add(ResBundleFiscalPrinterAtolUniversal.getString("OPEN_PRINTER_COVER"));
        } else if (!fptr.getParamBool(IFptr.LIBFPTR_PARAM_RECEIPT_PAPER_PRESENT)) {
            result.setStatus(StatusFP.Status.END_PAPER);
            result.getDescriptions().add(ResBundleFiscalPrinterAtolUniversal.getString("WARN_END_OF_PAPER"));
        }

        return result;
    }

    public synchronized void printLogo() {
        fptr.setParam(IFptr.LIBFPTR_PARAM_PICTURE_NUMBER, 1);
        fptr.setParam(IFptr.LIBFPTR_PARAM_ALIGNMENT, IFptr.LIBFPTR_ALIGNMENT_CENTER);
        fptr.printPictureByNumber();
    }

    public synchronized void sendMarkCode(Goods position) throws FiscalPrinterException {
        if (isNewPlatform) {
            StringBuilder sb = new StringBuilder();
            sb.append("01")
                    .append(MarkData.transformToGtin(position.getMarkEan()))
                    .append("21")
                    .append(position.getSerialNumber())
                    .append("\u001d");
            if (isNotBlank(position.getMarkMrp())) {
                sb.append("8005")
                        .append(position.getMarkMrp())
                        .append("\u001d");
            }
            byte[] excise = sb.toString().getBytes();
            fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE, excise);
            fptr.parseMarkingCode();
            byte[] tag1162 = fptr.getParamByteArray(Ffd.TAG_1162);
            fptr.setParam(Ffd.TAG_1162, tag1162);
        } else {
            Integer nomenclatureType = MARK_TYPE_TO_NOMENCLATURE_TYPE.get(position.getMarkType());
            if (nomenclatureType == null) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterAtolUniversal.getString("UNSUPPORTED_MARK_CODE_TYPE"),
                        CashErrorType.FISCAL_ERROR_REBOOT);
            }
            fptr.setParam(IFptr.LIBFPTR_PARAM_NOMENCLATURE_TYPE, nomenclatureType);
            fptr.setParam(IFptr.LIBFPTR_PARAM_GTIN, MarkData.transformToGtin(position.getMarkEan()));
            if (isNotBlank(position.getMarkMrp())) {
                fptr.setParam(IFptr.LIBFPTR_PARAM_SERIAL_NUMBER, position.getSerialNumber() + position.getMarkMrp());
            } else {
                fptr.setParam(IFptr.LIBFPTR_PARAM_SERIAL_NUMBER, position.getSerialNumber());
            }
            fptr.utilFormNomenclature();
            byte[] tag1162 = fptr.getParamByteArray(IFptr.LIBFPTR_PARAM_TAG_VALUE);
            fptr.setParam(Ffd.TAG_1162, tag1162);
        }
    }

    private void putSingleGoodsParams(Goods position) {
        fptr.setParam(IFptr.LIBFPTR_PARAM_COMMODITY_NAME, position.getName());
        fptr.setParam(IFptr.LIBFPTR_PARAM_PRICE, BigDecimalConverter.convertMoneyToDouble(position.getEndPricePerUnit()));
        fptr.setParam(IFptr.LIBFPTR_PARAM_QUANTITY, BigDecimalConverter.convertQuantity(position.getQuant()).doubleValue());
        fptr.setParam(IFptr.LIBFPTR_PARAM_TAX_TYPE, TAXES.get(position.getTax()));
    }

    public synchronized void putPayments(List<Payment> payments) throws FiscalPrinterException {
        for (Payment pay : payments) {
            fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_TYPE, FFD_TO_ATOL_PAYMENT_TYPE.get(pay.getIndexPaymentFDD100()));
            fptr.setParam(IFptr.LIBFPTR_PARAM_PAYMENT_SUM, BigDecimalConverter.convertMoneyToDouble(pay.getSum()));
            if (fptr.payment() != 0) {
                throw new FiscalPrinterException(fptr.errorDescription(), CashErrorType.FISCAL_ERROR, fptr.errorCode());
            }
        }
    }

    public synchronized void putGoodsOnAnnulationCancel(List<Goods> positions) {
        for (Goods position : positions) {
            putSingleGoodsParams(position);
            if (fptr.registration() != 0) {
                LOG.warn("Error on put position (annulation): {} ({})", fptr.errorDescription(), fptr.errorCode());
            }
        }
    }

    public synchronized void putCheckAgentInfo(Check check) {
        // для версий ФФД начиная с 1.2 передаем только в позиции
        if (ffdVersion.isBefore_1_2()) {
            AgentType checkAgentType = check.getSingleAgentType();
            if (checkAgentType != null) {
                fptr.setParam(Ffd.TAG_1057, checkAgentType.getBitMask());
            }
        }
    }

    private void putPaymentAgent(Goods position) {
        AdditionalInfo info = position.getAdditionalInfo();
        if (info != null) {
            // формируем составной тег 1224 из 1171 и 1225
            fptr.setParam(Ffd.TAG_1171, info.getDebitorPhone());
            fptr.setParam(Ffd.TAG_1225, info.getDebitorName());
            fptr.utilFormTlv();
            byte[] supplierInfo = fptr.getParamByteArray(IFptr.LIBFPTR_PARAM_TAG_VALUE);
            fptr.setParam(Ffd.TAG_1224, supplierInfo);

            // и только потом передаем одиночные теги
            fptr.setParam(Ffd.TAG_1222, info.getAgentType().getBitMask());
            fptr.setParam(Ffd.TAG_1226, info.getDebitorINN());
        }
    }

    public synchronized void openNonfiscalDocument() {
        fptr.beginNonfiscalDocument();
    }

    public synchronized void closeNonfiscalDocument() {
        // метод fptr.cut() работает только для версий 3.X, поэтому режем рекомендованным в документации способом
        fptr.setParam(IFptr.LIBFPTR_PARAM_PRINT_FOOTER, false);
        fptr.endNonfiscalDocument();
    }

    public Optional<FiscalMarkValidationResult> validateMarkCode(PositionEntity position,
                                                                 MarkData markData,
                                                                 boolean isSale) throws FiscalPrinterException {
        if (ffdVersion.isBefore_1_2()) {
            return Optional.empty();
        }
        String concatenatedMark = markData.getParser().concatMark(markData, GS);
        LOG.debug("Concatenated mark: {}", concatenatedMark);
        int tag2108 = FiscalMarkValidationUtil.formTag2108(position);
        int tag2003 = FiscalMarkValidationUtil.formTag2003(tag2108, isSale);

        Map<String, Object> input = ImmutableMap.of(
                FiscalMarkValidationUtil.MARK_KEY, concatenatedMark,
                FiscalMarkValidationUtil.TAG_2003_KEY, tag2003,
                FiscalMarkValidationUtil.TAG_2108_KEY, tag2108
        );

        // отменим предыдущую процедуру валидации, чтобы наверняка вернуть ФР в рабочее состояние
        fptr.cancelMarkingCodeValidation();
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE_TYPE, IFptr.LIBFPTR_MCT12_AUTO);
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE, concatenatedMark);
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_CODE_STATUS, tag2003);
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_WAIT_FOR_VALIDATION_RESULT, true);
        if (tag2108 > 0) {
            // в соответствии с таблицей 127 теги 1023 и 2108 передаются в запросе о коде маркировки, когда тег 2003 принимает значения 2 или 4,
            // а это возможно только если товар мерный (за исключением случая, когда статус товара не меняется, чего у нас нет)
            fptr.setParam(IFptr.LIBFPTR_PARAM_MEASUREMENT_UNIT, tag2108);
            fptr.setParam(IFptr.LIBFPTR_PARAM_QUANTITY, 1.000);
        }
        fptr.setParam(IFptr.LIBFPTR_PARAM_MARKING_PROCESSING_MODE, 0);
        // запрос в ФР и ОИСМ, в случае успешного завершения команды - КМ проверен в ФР, но еще неизвестно про ОИСМ
        if (fptr.beginMarkingCodeValidation() != 0) {
            String errorDesc = fptr.errorDescription();
            fptr.cancelMarkingCodeValidation();
            throw new FiscalPrinterException(errorDesc);
        }
        // результаты fptr.beginMarkingCodeValidation() - это результат локальной проверки (тег 2004) и ошибка локальной проверки
        int tag2004 = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_MARKING_CODE_VALIDATION_RESULT);
        LOG.debug("Tag 2004={}", tag2004);
        int fnCheckReason = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_MARKING_CODE_OFFLINE_VALIDATION_ERROR);
        FiscalMarkValidationUtil.logFnCheckReason(LOG, fnCheckReason);

        // подождем, пока в атоле появятся результаты проверки марки в ОИСМ
        waitForValidationResult();
        // получим ошибку онлайн проверки: в ответ получим HTTP статус, или 0, если все хорошо.
        int onlineValidationError = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_MARKING_CODE_ONLINE_VALIDATION_ERROR);
        LOG.debug("Atol online validation error: {}", onlineValidationError);
        if (onlineValidationError != 0) {
            // ФР не достучался до ОИСМ - примем результат валидации и вернем результат с тегом 2004
            fptr.acceptMarkingCode();
            return Optional.of(new FiscalMarkValidationResult(input, tag2004, null, null, null));
        }
        int tag2106 = (int) fptr.getParamInt(IFptr.LIBFPTR_PARAM_MARKING_CODE_ONLINE_VALIDATION_RESULT);

        Set<String> availableTagNames = getAvailableTagNames();
        Integer tag2005 = getTagByNumber(availableTagNames, Ffd.TAG_2005);
        Integer tag2105 = getTagByNumber(availableTagNames, Ffd.TAG_2105);
        Integer tag2109 = getTagByNumber(availableTagNames, Ffd.TAG_2109);

        // подтверждаем добавление в чек кода маркировки,
        // при этом заканчивается процедура проверки (не нужно вызывать fptr.cancelMarkingCodeValidation())
        fptr.acceptMarkingCode();

        return Optional.of(new FiscalMarkValidationResult(input, tag2106, tag2005, tag2105, tag2109));
    }

    /**
     * Ожидание ответа драйвера о результатах проверки в ОИСМ
     */
    private void waitForValidationResult() throws FiscalPrinterException {
        LOG.trace("Start waiting mark code validation in driver for {} ms", VALIDATION_TIMEOUT);
        Timer validationTimer = new Timer(VALIDATION_TIMEOUT);
        // метод позволяет получить данные в условии цикла while
        fptr.getMarkingCodeValidationStatus();
        // запрашиваем статус раз в какое-то время (VALIDATION_STEP_TIMEOUT), пока не истечет общий таймаут
        while (!fptr.getParamBool(IFptr.LIBFPTR_PARAM_MARKING_CODE_VALIDATION_READY)) {
            if (validationTimer.isExpired()) {
                LOG.trace("Stop waiting mark code validation by timeout");
                fptr.cancelMarkingCodeValidation();
                // такое состояние может возникнуть, если в настройках самого ФР поставлены таймауты >= 3 секунд,
                // или если используется прошивка ниже 5.8.6 версии.
                throw new FiscalPrinterException("OISM timeout expired in Atol!");
            }
            try {
                LOG.trace("sleeping on mark code validation {} ms", VALIDATION_STEP_TIMEOUT);
                Thread.sleep(VALIDATION_STEP_TIMEOUT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FiscalPrinterException("Atol waiting for validation result was interrupted", e);
            }
            fptr.getMarkingCodeValidationStatus();
        }
        LOG.trace("Received validation response");
    }

    private Set<String> getAvailableTagNames() {
        String[] availableTags = fptr.getParamString(IFptr.LIBFPTR_PARAM_TLV_LIST).split(";");
        return new HashSet<>(Arrays.asList(availableTags));
    }

    private Integer getTagByNumber(Set<String> tagNames, int tagNum) {
        return tagNames.contains(String.valueOf(tagNum)) ? (int) fptr.getParamInt(tagNum) : null;
    }

    public void clearBeforeMarkRevalidation() throws FiscalPrinterException {
        // аннулируем чек, иначе следующий метод падает с ошибкой
        annulCheckIfNotClosed();
        fptr.clearMarkingCodeValidationResult();
    }
}
