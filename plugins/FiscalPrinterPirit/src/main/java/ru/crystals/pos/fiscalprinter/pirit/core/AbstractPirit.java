package ru.crystals.pos.fiscalprinter.pirit.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.LongRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.image.context.fiscal.FiscalDevice;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.cards.plastek.PlastekDocument;
import ru.crystals.pos.check.Base39Coder;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.PluginUtils;
import ru.crystals.pos.fiscalprinter.PrinterClass;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
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
import ru.crystals.pos.fiscalprinter.datastruct.documents.FirmwareVersionUtils;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnStatus;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FullCheckCopy;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Image;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ImageData;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Margin;
import ru.crystals.pos.fiscalprinter.datastruct.documents.MarginType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextSize;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextStyle;
import ru.crystals.pos.fiscalprinter.datastruct.info.FnDocInfo;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardInfoReport;
import ru.crystals.pos.fiscalprinter.datastruct.presentcard.PresentCardReplaceReport;
import ru.crystals.pos.fiscalprinter.datastruct.state.PrinterState;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterNeedToRevalidateCodeMarks;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PingStatus;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgent;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConfig;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritErrorMsg;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.fn.StatusFN;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.fn.StatusOFD;
import ru.crystals.pos.fiscalprinter.pirit.core.font.FontConfiguration;
import ru.crystals.pos.fiscalprinter.pirit.core.font.PiritFontManager;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.rxtxadapter.ComPortUtil;
import ru.crystals.rxtxadapter.CommPortIdentifierSource;
import ru.crystals.utils.time.DateConverters;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ru.crystals.pos.utils.ByteUtils.hasBit;

public abstract class AbstractPirit implements FiscalPrinterPlugin, Configurable<PiritPluginConfig> {

    protected static final int GOOD_NAME_MAX_LENGTH = 56;
    protected static final int ITEM_MAX_LENGTH = 18;
    private static final int DISC_AND_MARGIN_NAME_MAX_LENGTH = 38;
    private static final long MAX_ASYNC_COMMAND_BUFFER_SIZE_PIRIT_II = 100;
    /**
     * Название модуля в sales_management_properties
     */
    private static final String MODULE_CONFIG = "FISCAL_CONFIG";

    /**
     * Версия прошивки Пирит ФР01К (15), в которой была исправлена ошибка изъятия большей суммы наличных,
     * чем есть в кассе (до исправления Пирит уходил в минус, а после стал обнулять)
     */
    private static final int PIRIT_VERSION_WITH_WITHDRAWAL_FIX = 15;

    // TODO: При дальнейшем рефакторинге вынести в отдельные класс
    private static final long PIRIT_PACKET_MODE = 16;
    private static final long REQUISITES_PRINTING_DEFERRED_MODE = 32L;
    private static final long NO_PRINTING_MODE = 128L;
    private static final long PIRIT_EXTENDED_MODE_NO_POSITION_PRINT = 64;
    private static final long PIRIT_SERVICE_DOCUMENT = 1;
    protected static final long PIRIT_CHECK_SALE = 2;
    protected static final long PIRIT_CHECK_RETURN = 3;
    private static final long PIRIT_MONEY_IN = 4;
    private static final long PIRIT_MONEY_OUT = 5;
    private static final long PIRIT_EXPENSE_RECEIPT = 6;
    private static final long PIRIT_EXPENSE_RECEIPT_RETURN = 7;
    private static final long GET_INFO_PAPER_SIZE_80_MM = 0L;
    public static final long GET_INFO_PAPER_SIZE_57_MM = 1L;
    private static final int MAX_LENGTH_CODE_39_BARCODE_ON_57_MM = 15;
    private static final int COUPON_BARCODE_LENGTH = 21;
    public static final String PIRIT_CODE_SET = "CP866";
    private static final long IMAGE_ALIGN_LEFT = 0;
    private static final long IMAGE_ALIGN_CENTER = 1;
    private static final long IMAGE_ALIGN_RIGHT = 2;
    private static final long IMAGE_ALIGN_DEFAULT = 3;

    private static final Set<FiscalDocumentType> CHECK_DOC_TYPES = EnumSet.of(
            FiscalDocumentType.REFUND,
            FiscalDocumentType.SALE,
            FiscalDocumentType.EXPENSE,
            FiscalDocumentType.EXPENSE_REFUND
    );

    public static final long MINIMAL_SUPPORTED_VERSION = 13;
    protected static final long MINIMAL_VERSION_WITH_FN1_SUPPORT = 153;
    protected static final long MINIMAL_VERSION_WITH_FN2_SUPPORT = 252;
    protected static final long MINIMAL_BYN_VERSION = 262;

    protected final static Logger LOG = LoggerFactory.getLogger(AbstractPirit.class);

    // для случаев с 2-мя фискальниками
    protected static Map<String, AbstractPirit> pcMap = new HashMap<>();

    protected PiritConnector pc = new PiritConnector();
    protected PiritAgent pa;
    public final PiritConfig piritConfig = new PiritConfig();
    protected ValueAddedTaxCollection taxes;

    protected final String defaultGoodsName = "-----";

    private int MAX_DEPART_NUMBER = 99;
    protected final long COUNT_ORDER = 1000;
    public final long PRICE_ORDER = 100;

    private static final int MIN_QR_SIZE_BYTES = 30;
    private static final int MAX_QR_SIZE_BYTES = 48;
    private static final long MAX_QR_WIDTH_PIRIT_1F = 4L;
    private static final long MAX_QR_WIDTH_PIRIT_2F = 8L;
    protected boolean firstStart = true;
    protected long taxSystem = -1;

    private static final LongRange PIRIT_1F_RANGE = new LongRange(150, 199);
    private static final LongRange PIRIT_2F_RANGE = new LongRange(550, 599);
    private static final LongRange PIRIT_VIKI_RANGE = new LongRange(650, 699);

    private static final String NOT_NEW_LINE_PIRIT_2F_PREFIX = "&";

    /**
     * Версия с которой стало возможным получение расширенной информации о версии прошивки (минорные версии)
     * Пирит 2Ф
     */
    private static final Long EXTENDED_VERSION_INFO_PIRIT_2F_START_VERSION = 565L;

    /**
     * Версия с которой стало возможным получение расширенной информации о версии прошивки (минорные версии)
     * Пирит 1Ф
     */
    private static final Long EXTENDED_VERSION_INFO_PIRIT_1F_START_VERSION = 165L;

    /**
     * Версия с которой стало возможна отправка данных о ЮЛ в ОФД.
     * Пирит 1Ф
     */
    private static final String JURISTIC_DATA_PIRIT_1F_START_VERSION = "165.0.6";

    /**
     * Версия с которой стало возможна отправка данных о ЮЛ в ОФД.
     * Пирит 2Ф
     */
    private static final String JURISTIC_DATA_PIRIT_2F_START_VERSION = "565.0.12";

    /**
     * Версия с которой стало возможным получение расширенной информации о версии прошивки (минорные версии)
     * ВикиПринт
     */
    private static final Long EXTENDED_VERSION_INFO_VIKI_START_VERSION = 665L;

    /**
     * Текущая версия прошивки
     */
    private String currentFirmwareVersion;

    /**
     * Текущий тип прошивки
     */
    private Long currentFirmwareType;

    /**
     * Максимальная длинна наименования клиента для Pirit1F
     */
    private int pirit1fMaxCustomerNameLength = 64;

    /**
     * Название настройки "PIRIT1F_MAX_CUSTOMER_NAME_LENGTH" в sales_management_properties
     */
    private static final String PIRIT1F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY = "fiscal.config.pirit1f.max.customer.name.length";

    private static final String OISM_TIMEOUT_PROPERTY = "fiscal.config.pirit.oism.timeout";

    /**
     * Максимальная длинна наименования клиента для Pirit2F
     */
    private int pirit2fMaxCustomerNameLength = 256;

    /**
     * Название настройки "PIRIT2F_MAX_CUSTOMER_NAME_LENGTH" в sales_management_properties
     */
    private static final String PIRIT2F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY = "fiscal.config.pirit2f.max.customer.name.length";


    /**
     * название настройки "needPrintKKTinfo" в sales_management_properties
     */
    private static final String PIRIT2F_NEED_PRINT_KKT_INFO_PROPERTY = "fiscal.config.pirit2f.need.print.kkt.info";

    private PropertiesManager propertiesManager;

    protected PiritPluginConfig config;
    protected PiritFontManager fontManager = new PiritFontManager();
    protected int oismTimeout = -1;

    protected enum TaxSystem {
        COMMON(1),
        SIMPLIFIED_INCOME(2),
        SIMPLIFIED_INCOME_MINUS_EXPENSE(4),
        UNIFIED(8),
        UNIFIED_AGRICULTURAL(16),
        PATENT(32);

        public final int flag;

        TaxSystem(int flag) {
            this.flag = flag;
        }
    }


    protected CustomerPluginProvider pluginProvider = new DefaultPluginProvider(this);

    private ImagePrintingRoutine imagePrintingRoutine;

    private final ComProxyService comProxyService = new ComProxyService();

    @Override
    public Class<PiritPluginConfig> getConfigClass() {
        return PiritPluginConfig.class;
    }

    @Override
    public void setConfig(PiritPluginConfig config) {
        this.config = config;
    }

    @PostConstruct
    void init() {
        propertiesManager.addListener(MODULE_CONFIG, null, this::propertiesChanged);
    }

    private void propertiesChanged(Map<String, String> params) {
        oismTimeout = propertiesManager.getIntProperty(MODULE_CONFIG, null, OISM_TIMEOUT_PROPERTY, -1);
    }

    @Override
    public abstract void start() throws FiscalPrinterException;

    @Autowired
    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    protected void startComProxy() throws FiscalPrinterException {
        if (useComProxy()) {
            comProxyService.configure(config);
            comProxyService.start(isOFDDevice());
        }
    }

    protected void startInner() throws FiscalPrinterException {
        startPluginProvider();
        startComProxy();
        PiritCommand.setCommandsReadTimeout(config.getCommandsReadTimeout());
        startPCMap();
        imagePrintingRoutine = new ImagePrintingRoutine(this);

        Map<String, String> fiscalSettings = propertiesManager.getByModulePlugin(MODULE_CONFIG, null);
        setFiscalSettings(fiscalSettings);
    }

    private void setFiscalSettings(Map<String, String> fiscalSettings) {
        propertiesChanged(fiscalSettings);
        if (fiscalSettings.containsKey(PIRIT1F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY)) {
            pirit1fMaxCustomerNameLength = Integer.parseInt(fiscalSettings.get(PIRIT1F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY));
        }
        if (fiscalSettings.containsKey(PIRIT2F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY)) {
            pirit2fMaxCustomerNameLength = Integer.parseInt(fiscalSettings.get(PIRIT2F_MAX_CUSTOMER_NAME_LENGTH_PROPERTY));
        }
        if (fiscalSettings.containsKey(PIRIT2F_NEED_PRINT_KKT_INFO_PROPERTY)) {
            config.setNeedPrintKKTInfo(Boolean.parseBoolean(fiscalSettings.get(PIRIT2F_NEED_PRINT_KKT_INFO_PROPERTY)));
        }

    }

    private void startPCMap() throws FiscalPrinterException {
        if (firstStart) {
            startPcInner();
            firstStart = false;
        } else {
            for (AbstractPirit ap : pcMap.values()) {
                try {
                    ap.startPcInner();
                } catch (FiscalPrinterException ex) {
                    LOG.error("", ex);
                }
            }
        }
    }

    private void startPcInner() throws FiscalPrinterException {
        try {
            if (config.getPort() == null) {
                LOG.warn("Port not defined");
                throw new FiscalPrinterConfigException(ResBundleFiscalPrinterPirit.getString("ERROR_CONFIG"), CashErrorType.FATAL_ERROR);
            }
            if (config.getBaudRate() == null) {
                LOG.warn("BaudRate not defined");
                throw new FiscalPrinterConfigException(ResBundleFiscalPrinterPirit.getString("ERROR_CONFIG"), CashErrorType.FATAL_ERROR);
            }

            initPiritConnector();

            try {
                pc.reconnect();

                PingStatus ps = pc.isPiritOnline();

                int c = 30;
                while (!ps.isOnline() && c > 0) {
                    c--;
                    ps = pc.isPiritOnline();
                    Thread.sleep(1000);
                }

                if (!ps.isOnline()) {
                    throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterPirit.getString("SERVICE_TIMEOUT"), CashErrorType.FATAL_ERROR);
                }
            } catch (Exception e) {
                LOG.warn("", e);
                throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinterPirit.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
            }

            pa = createPiritAgent(pc);

            piritConfig.setConnector(pc);

            PiritStatus piritStatus = new PiritStatus(pc.sendRequest(PiritCommand.GET_STATUS_INIT));

            if (piritStatus.needToStartWork()) {
                startWork();
                piritStatus = new PiritStatus(pc.sendRequest(PiritCommand.GET_STATUS_INIT));
            }

            forceCloseOrCancelDocument(piritStatus);

            taxes = getTaxes();
            piritConfig.setCheckNumerationByCash(true);
            if (isOFDDevice()) {
                piritConfig.setRoundTaxesAfterAllPositionsAndDiscounts(false);
            } else {
                piritConfig.setRoundTaxesAfterAllPositionsAndDiscounts(true);
            }
            piritConfig.setAutoWithdrawal(false);


            pcMap.put(getFactoryNum(), this);
            logStatusFN();

        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            LOG.error("", e);
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    protected void startWork() throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putDateAndTime(LocalDateTime.now());

        try {
            pc.sendRequest(PiritCommand.START_WORK, dp);
        } catch (FiscalPrinterException fpe) {
            if ((fpe.getErrorCode() != null) && (fpe.getErrorCode() == 0x0C)) {
                throw fpe;
            }
        }
    }

    protected void initPiritConnector() {
        pc.setParams(ComPortUtil.getRealSystemPortName(config.getPort()), config.getBaudRate());
    }

    private void startPluginProvider() {
        if (config.getPluginProvider() == null) {
            return;
        }
        try {
            Class serviceClass = Class.forName(config.getPluginProvider());
            pluginProvider = (CustomerPluginProvider) serviceClass.newInstance();
            pluginProvider.setBasePlugin(this);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void logStatusFN() {
        if (isOFDDevice() && this.getCountry() == PiritCountry.RU) {
            try {
                LOG.info(pa.getStatusFN().toString());
                LOG.info(pa.getStatusOFD().toString());
                LOG.info(pa.getVersionFN().toString());
            } catch (FiscalPrinterException ex) {
                LOG.error("Get log status error", ex);
            }
        }
    }

    @Override
    public void stop() {
        try {
            stopComProxy();
            stopPCMap();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void stopComProxy() throws FiscalPrinterException {
        if (!useComProxy()) {
            return;
        }
        if (isOFDDevice() && !pc.isPiritOnline().isOnline()) {
            comProxyService.stopService();
        }
    }

    private void stopPC() {
        pc.close();
    }

    private void stopPCMap() {
        for (AbstractPirit ap : pcMap.values()) {
            ap.stopPC();
        }
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_REG_NUM);
            return dp.getStringValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public String getVerBios() throws FiscalPrinterException {
        try {
            return getFirmware();
        } catch (Exception ex) {
            throw new FiscalPrinterException("GET VER BIOS ERROR", PiritErrorMsg.getErrorType());
        }
    }

    /**
     * Возвращает версию прошивки фискального регистратора
     *
     * @return версия прошивки фискального регистратора в формате X.Y.Z
     * @throws FiscalPrinterException если при запросе версии возникли ошибки
     * @see #getFirmwareLong()
     */
    private String getFirmware() throws FiscalPrinterException {
        if (currentFirmwareVersion == null) {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_FW_ID);
            try {
                currentFirmwareVersion = dp.getStringValue(1);

                //  если есть возможность, то подтянем и минорные версии
                if (canExtractExtendedVersionInfo()) {
                    currentFirmwareVersion += "." + dp.getStringValue(2);
                    currentFirmwareVersion += "." + dp.getStringValue(3);
                }
            } catch (Exception e) {
                throw new FiscalPrinterException("Failed to get firmwareId", e);
            }
        }

        return currentFirmwareVersion;
    }

    protected boolean canExtractExtendedVersionInfo() {
        Long version;
        Long currentMajorVersion = FirmwareVersionUtils.parseVersion(currentFirmwareVersion, FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);

        if (PIRIT_1F_RANGE.containsLong(currentMajorVersion)) {
            version = EXTENDED_VERSION_INFO_PIRIT_1F_START_VERSION;
        } else if (PIRIT_2F_RANGE.containsLong(currentMajorVersion)) {
            version = EXTENDED_VERSION_INFO_PIRIT_2F_START_VERSION;
        } else {
            version = EXTENDED_VERSION_INFO_VIKI_START_VERSION;
        }

        return FirmwareVersionUtils.compare(currentFirmwareVersion, String.valueOf(version)) >= 0;
    }

    /**
     * Проверяем можно ли отправлять данные о ЮЛ в ККТ.
     * В данный момент поддерживается только Пирит 1Ф и Пирит 2Ф.
     *
     * @return true - ККТ поддерживает работу с ЮЛ, false - ККТ не поддерживает работу с ЮЛ.
     */
    protected boolean canSendJuristicData() {
        Long currentMajorVersion = FirmwareVersionUtils.parseVersion(currentFirmwareVersion, FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
        String version;

        if (PIRIT_1F_RANGE.containsLong(currentMajorVersion)) {
            version = JURISTIC_DATA_PIRIT_1F_START_VERSION;
        } else if (PIRIT_2F_RANGE.containsLong(currentMajorVersion)) {
            version = JURISTIC_DATA_PIRIT_2F_START_VERSION;
        } else {
            return false;
        }

        return FirmwareVersionUtils.compare(currentFirmwareVersion, version) >= 0;
    }

    /**
     * Возвращает тип прошивки фискального регистратора
     *
     * @return тип прошивки фискального регистратора
     */
    protected long getFirmwareType() {
        if (currentFirmwareType == null) {
            try {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_FW_TYPE);
                currentFirmwareType = dp.getLongValue(1);
            } catch (Exception e) {
                currentFirmwareType = 0L;
                LOG.warn("Failed to get firmwareTypeId", e);
            }
        }
        return currentFirmwareType;
    }

    protected PiritStatus getPiritStatus() throws FiscalPrinterException {
        DataPacket dp = pc.sendRequest(PiritCommand.GET_STATUS);
        return new PiritStatus(dp);
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        try {
            if (getPiritStatus().isShiftOpened()) {
                return true;
            } else if (!isOFDDevice()) {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_OPERATION_COUNTER);
                String operCounter = dp.getStringValue(1);
                return Long.parseLong(operCounter.substring(operCounter.lastIndexOf(".") + 1)) > 1;
            }
            return false;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    protected long getLastDocNum() throws FiscalPrinterException {
        try {
            return pc.sendRequest(ExtendedCommand.GET_RECEIPT_DATA_LAST).getLongValue(4);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public Date getDate() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(PiritCommand.GET_DATE);
            return dp.getOptionalDateTimeValue(0, 1)
                    .map(DateConverters::toDate)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid date"));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setDate(Date date) throws FiscalPrinterException {
        final LocalDateTime localDateTime = DateConverters.toLocalDateTime(date);
        try {
            setDateInner(localDateTime);
        } catch (FiscalPrinterException fpe) {
            LOG.error("Try to set date second time", fpe);
            setDateInner(localDateTime);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void setDateInner(LocalDateTime date) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putDateAndTime(date);

            pc.sendRequest(PiritCommand.SET_DATE, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void beepError() {
        if (config.isUseBeep()) {
            beep(200);
            // Для Пирита-2
            // beep(500);
        }
    }

    @Override
    public void beepCriticalError() {
        if (config.isUseBeep()) {
            beep(50);
            sleep(170);
            beep(50);
            sleep(170);
            beep(400);
        }

        // Для Пирита-2
        // beep(300);
        // sleep(200);
        // beep(300);
        // sleep(200);
        // beep(1000);

    }

    private static void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beepConfirm() {
        if (config.isUseBeep()) {
            beep(50);
        }
    }

    /**
     * Возвращает версию прошивки фискального регистратора.
     *
     * @return версия прошивки фискального регистратора
     * @throws FiscalPrinterException если при запросе версии произошли ошибки
     * @see #getFirmware()
     */
    protected long getFirmwareLong() throws FiscalPrinterException {
        return FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
    }

    @Override
    public String getHardwareName() throws FiscalPrinterException {
        LOG.info("getHardwareName");
        try {
            long firmware = getFirmwareLong();
            LOG.info("Firmware = {}", firmware);
            if (firmware < MINIMAL_SUPPORTED_VERSION) {
                // добавляем версии
                String strError = ResBundleFiscalPrinterPirit.getString("ERROR_UNSUPPORTABLE_FIRMWARE_VERSION")
                        .replace("{1}", "(" + firmware + ")")
                        .replace("{2}", "(" + MINIMAL_SUPPORTED_VERSION + ")");
                throw new FiscalPrinterException(strError, CashErrorType.FATAL_ERROR);
            }

            if (PIRIT_1F_RANGE.containsLong(firmware)) {
                return ResBundleFiscalPrinterPirit.getString("PIRIT_1F");
            } else if (PIRIT_2F_RANGE.containsLong(firmware)) {
                return ResBundleFiscalPrinterPirit.getString("PIRIT_2F");
            } else if (PIRIT_VIKI_RANGE.containsLong(firmware)) {
                return ResBundleFiscalPrinterPirit.getString("VIKI_PRINT");
            } else if (firmware < 200) {
                return ResBundleFiscalPrinterPirit.getString("PIRIT_1_NAME");
            } else {
                return ResBundleFiscalPrinterPirit.getString("PIRIT_2_NAME");
            }

        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            return null;
        }
    }

    private void beep(int time) {
        try {
            DataPacket dp = new DataPacket(String.valueOf(time));
            pc.sendRequest(PiritCommand.BEEP, dp);
        } catch (FiscalPrinterException fpe) {
            LOG.trace("Error on beeping through Pirit");
        }
    }

    @Override
    public void openMoneyDrawer() throws FiscalPrinterException {
        try {
            pc.sendRequest(PiritCommand.OPEN_MONEY_DRAWER);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isMoneyDrawerOpen() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(PiritCommand.STATUS_MONEY_DRAWER);

            long lVal = dp.getLongValue(0);

            return lVal != 0;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            ShiftCounters sc = new ShiftCounters();
            DataPacket dp;

            /* Номер смены */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_SHIFT_NUMBER);
            sc.setShiftNum(dp.getLongValue(1));

            if (!getPiritStatus().isShiftOpened()) {
                sc.setShiftNum(sc.getShiftNum() - 1);
            }

            /* Оплаты */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_SALE_SUMS_BY_PAYMENT);
            sc.setSumCashPurchase(dp.getDoubleMoneyToLongValue(1));
            sc.setSumCashlessPurchase(getCashlessSumFromDP(dp));
            sc.setSumSale(sc.getSumCashPurchase() + sc.getSumCashlessPurchase());

            /* Возвраты */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_REFUND_SUMS_BY_PAYMENT);
            sc.setSumCashReturn(dp.getDoubleMoneyToLongValue(1));
            sc.setSumCashlessReturn(getCashlessSumFromDP(dp));
            sc.setSumReturn(sc.getSumCashReturn() + sc.getSumCashlessReturn());

            /* Количество чеков продажи */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_SALE_COUNTS_BY_PAYMENT);
            sc.setCountCashPurchase(dp.getLongValue(1));
            sc.setCountCashlessPurchase(getCashlessCountFromDP(dp));

            /* Количество чеков возврата */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_REFUND_COUNTS_BY_PAYMENT);
            sc.setCountCashReturn(dp.getLongValue(1));
            sc.setCountCashlessReturn(getCashlessCountFromDP(dp));

            /* Денег в кассе */
            dp = pc.sendRequest(ExtendedCommand.GET_INFO_CASH_AMOUNT);

            sc.setSumCashEnd(dp.getDoubleMoneyToLongValue(1));

            /* Количество чеков прихода, возврата прихода, внесения, изъятия */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);

            sc.setCountSale(dp.getDoubleToRoundLong(1));
            sc.setCountReturn(dp.getDoubleToRoundLong(2));
            // 3 - количество аннулирований (РБ)
            // 4 - количество отмененных чеков (не включает изъятия и сервисные документы) (РБ)
            sc.setCountCashIn(dp.getDoubleToRoundLong(5));
            sc.setCountCashOut(dp.getDoubleToRoundLong(6));

            /* Сумма внесений и изьятий */
            dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_SUMS_BY_TYPE);
            sc.setSumCashIn(dp.getDoubleMoneyToLongValue(3));
            sc.setSumCashOut(dp.getDoubleMoneyToLongValue(4));

            if (isOFDDevice() && this.getCountry() == PiritCountry.RU) {
                /* Расход */
                dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_EXPENSE_SUM_BY_PAYMENTS);
                sc.setSumCashExpenseReceipt(dp.getDoubleMoneyToLongValue(1));
                sc.setSumCashlessExpenseReceipt(getCashlessSumFromDP(dp));
                sc.setSumExpenseReceipt(sc.getSumCashExpenseReceipt() + sc.getSumCashlessExpenseReceipt());

                /* Возврат расхода */
                dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_RETURN_EXPENSE_SUM_BY_PAYMENTS);
                sc.setSumCashReturnExpenseReceipt(dp.getDoubleMoneyToLongValue(1));
                sc.setSumCashlessReturnExpenseReceipt(getCashlessSumFromDP(dp));
                sc.setSumReturnExpenseReceipt(sc.getSumCashReturnExpenseReceipt() + sc.getSumCashlessReturnExpenseReceipt());

                /* Количество чеков расхода и возврата расхода */
                dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_EXPENSE_COUNT_BY_TYPE);
                sc.setCountExpenseReceipt(dp.getLongValue(1));
                sc.setCountReturnExpenseReceipt(dp.getLongValue(2));

                // Данные по коррекциям
                dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_CORRECTION_DATA);
                sc.setCountCorrections(dp.getLongValue(1));
                // Суммы коррекций приходов наличных и безналичных
                sc.setSumCashCorrectionsReceipt(dp.getDoubleMoneyToLongValue(2));
                sc.setSumCashlessCorrectionsReceipt(dp.getDoubleMoneyToLongValue(3));
                // Присутствуют ли значения сумм коррекций расходов наличных и безналичных
                boolean isCorrectionsSpendingPresent = dp.getCountValue() > 5;
                sc.setSumCashCorrectionsSpending(isCorrectionsSpendingPresent ? dp.getDoubleMoneyToLongValue(4) : 0L);
                sc.setSumCashlessCorrectionSpending(isCorrectionsSpendingPresent ? dp.getDoubleMoneyToLongValue(5) : 0L);
            }

            return sc;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private long getCashlessSumFromDP(DataPacket dp) throws Exception {
        long value = 0;
        for (byte i = 0; i < 15; i++) {
            value += dp.getDoubleMoneyToLongValue(2 + i);
        }
        return value;
    }

    private long getCashlessCountFromDP(DataPacket dp) throws Exception {
        long value = 0;
        for (byte i = 0; i < 15; i++) {
            value += dp.getLongValue(2 + i);
        }
        return value;
    }


    @Override
    public Date getLastFiscalOperationDate() {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_LAST_FISCAL_DATE);
            return dp.getOptionalDateTimeValue(1, 2)
                    .map(DateConverters::toDate)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid date"));
        } catch (Exception e) {
            LOG.error("Failed to get close shift date/time. Current value will be returned", e);
            return new Date();
        }
    }

    @Override
    public void printReportFiscalMemoryByDate(Date startDate, Date endDate, String password, boolean isFullReport) throws FiscalPrinterException {

        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(isFullReport ? 1L : 0L);
            dp.putDateValue(DateConverters.toLocalDate(startDate));
            dp.putDateValue(DateConverters.toLocalDate(endDate));
            dp.putStringValue(password);

            pc.sendRequest(PiritCommand.PRINT_FISCAL_REPORT_BY_DATE, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void printReportFiscalMemoryByShiftID(long startShiftID, long endShiftID, String password, boolean isFullReport) throws FiscalPrinterException {

        try {
            DataPacket dp = new DataPacket();
            dp.putLongValue(isFullReport ? 1L : 0L);
            dp.putLongValue(startShiftID);
            dp.putLongValue(endShiftID);
            dp.putStringValue(password);

            pc.sendRequest(PiritCommand.PRINT_FISCAL_REPORT_BY_SHIFT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public PrinterState getPrinterState() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(PiritCommand.GET_PRINTER_STATE);

            PrinterState ps = new PrinterState();
            final int state = dp.getIntegerSafe(0).orElse(0);
            ps.setLongState(state);

            if (hasBit(state, 0)) {
                ps.addDescription(ResBundleFiscalPrinterPirit.getString("WARN_PRINTER_NOT_READY"));
            }

            if (hasBit(state, 1)) {
                ps.addDescription(ResBundleFiscalPrinterPirit.getString("WARN_END_OF_PAPER"));
                ps.setState(PrinterState.State.END_PAPER);
            }

            if (hasBit(state, 2)) {
                ps.addDescription(ResBundleFiscalPrinterPirit.getString("OPEN_PRINTER_COVER"));
                ps.setState(PrinterState.State.OPEN_COVER);
            }

            if (hasBit(state, 3)) {
                ps.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_CUTTER_PRINTER"));
            }

            if (hasBit(state, 7)) {
                ps.addDescription(ResBundleFiscalPrinterPirit.getString("NO_COMMUNICATION_WITH_PRINTER"));
            }

            return ps;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Метод проверяет состояние текущего документа в ККТ и выполняет необходимы действия
     *
     * 1. Если требуется команда "Начала работы" - выбрасывает соответствующий эксепшен
     * 2. Если документ открыт и в его флагах есть требование повторить отправку команды "Закрыт" - повторяем отправку команды "Закрыть"
     * 3. Если документ открыт, то аннулируем его
     *
     * @return {@code true} - если в результате проверки был обнаружен недозакрытый документ и он был закрыт,
     * {@code false} - если открытого документа не было или он был, но мы его аннулировали
     */
    private boolean checkStateBeforeOpenDoc() throws FiscalPrinterException {
        final PiritStatus piritStatus = getPiritStatus();
        if (piritStatus.needToStartWork()) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("WARN_NEED_RESTART"), PiritErrorMsg.getErrorType());
        }
        return forceCloseOrCancelDocument(piritStatus);
    }

    /**
     * Дозакрывает или аннулирует открытый в ККТ документ в зависимости от флагов документа
     * @param piritStatus результат запроса команды "Состояние ККТ" (00)
     * @return {@code true} - если в результате проверки был обнаружен недозакрытый документ и мы его дозакрыли,
     * {@code false} - если открытого документа не было или он был, но мы его аннулировали
     */
    private boolean forceCloseOrCancelDocument(PiritStatus piritStatus) throws FiscalPrinterException {
        if (piritStatus.docNeedToBeClosed()) {
            // Тут мы должны довериться Пириту и просто послать ему команду "Закрыть документ" (значение по умолчанию - с отрезкой)
            pc.sendRequest(PiritCommand.CLOSE_DOCUMENT);
            return true;
        }
        if (piritStatus.isDocOpened()) {
            annulCheck();
        }
        return false;
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        try {
            if (checkStateBeforeOpenDoc()) {
                return;
            }
            long docTypeCode = PIRIT_PACKET_MODE;
            if (check.getType() == CheckType.SALE) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT : PIRIT_CHECK_SALE;
            } else if (check.getType() == CheckType.RETURN) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT_RETURN : PIRIT_CHECK_RETURN;
            }
            DataPacket dp = new DataPacket();
            dp.putLongValue(docTypeCode);

            dp.putLongValue(getFiscalDocDepart(check));

            dp.putStringValue(getCashierName(check.getCashier()));
            dp.putLongValue(check.getCheckNumber());

            long taxSystem = getTaxSystem();
            if (taxSystem != -1) {
                dp.putLongValue(taxSystem);
            }

            pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);

            this.putGoods(check.getGoods(), true);

            pc.sendRequest(PiritCommand.SUBTOTAL, true);

            this.putDiscounts(check.getDiscs(), true);
            this.putMargin(check.getMargins(), true);
            this.putPayments(check.getPayments(), true);

            if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
                printDocumentNumberBarcode(check, true);
            }

            if (check.isAnnul()) {
                annulCheck();
            } else {
                closeCheck(check);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    protected void closeCheck(Check check) throws Exception {
        configureLogoPrinting(check);
        DataPacket closePacket = new DataPacket();
        if (check != null && check.getNextServiceDocument() != null && check.getNextServiceDocument().isDisableCut()) {
            closePacket.putLongValue(5L);
        }
        pc.sendRequest(PiritCommand.CLOSE_DOCUMENT, closePacket);
    }

    protected String getCashierName(Cashier cashier) {
        return pa.getCashierName(cashier);
    }

    protected Long getFiscalDocDepart(FiscalDocument doc) {
        if (doc.getDepart() < MAX_DEPART_NUMBER) {
            return doc.getDepart();
        } else {
            LOG.warn("departNumber=" + doc.getDepart());
            return 1L;
        }
    }

    @Override
    public boolean printCheckCopy(Check check) throws FiscalPrinterException {
        try {
            if (checkStateBeforeOpenDoc()) {
                return true;
            }

            long docTypeCode = PIRIT_PACKET_MODE;
            if (check.getType() == CheckType.SALE) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT : PIRIT_CHECK_SALE;
            } else if (check.getType() == CheckType.RETURN) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT_RETURN : PIRIT_CHECK_RETURN;
            }
            DataPacket dp = new DataPacket();
            dp.putLongValue(docTypeCode);

            dp.putLongValue(getFiscalDocDepart(check));
            dp.putStringValue(getCashierName(check.getCashier()));
            dp.putLongValue(check.getCheckNumber());
            dp.putLongValue(check.getCashNumber());
            dp.putDateAndTime(DateConverters.toLocalDateTime(check.getDateForPrint()));

            pc.sendRequest(PiritCommand.OPEN_CHECK_COPY, dp);

            this.putGoods(check.getGoods(), false);

            pc.sendRequest(PiritCommand.SUBTOTAL);

            this.putDiscounts(check.getDiscs(), false);
            this.putMargin(check.getMargins(), false);
            this.putPayments(check.getPayments(), false);

            if (check.getDiscountValueTotal() != null && check.getDiscountValueTotal() > 0L) {
                this.putText(
                        new Text(ResBundleFiscalPrinterPirit.getString("PD_DISCOUNT_SUM")
                                + CurrencyUtil.formatSum(check.getDiscountValueTotal()), TextSize.NORMAL,
                                TextStyle.NORMAL), false);
            }

            printDocumentNumberBarcode(check, false, false);

            closeDocument(true, null);
            return true;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    public void printDocumentNumberBarcode(Check check, boolean isFiscalDocument) throws Exception {
        printDocumentNumberBarcode(check, isFiscalDocument, true);
    }

    protected void printDocumentNumberBarcode(Check check, boolean isFiscalDocument, boolean isAsync) throws Exception {
        BarCode documentBarcode = PluginUtils.getDocumentBarcode(check);
        documentBarcode.setTextPosition(TextPosition.NONE_TEXT);
        documentBarcode.setHeight(config.getBarcodeHeight());
        putBarCode(documentBarcode, isFiscalDocument, isAsync);
        FontLine barcodeLabel = new FontLine(StringUtils.center(documentBarcode.getBarcodeLabel(), getMaxCharRow(Font.NORMAL, null)), Font.NORMAL);
        printLine(barcodeLabel, isFiscalDocument, isAsync);
    }

    /**
     * Печать загруженного изображения
     *
     * @param isFiscalDocument фискальная часть
     * @param imageNumber      номер изображения 1-15
     * @param align            выравнивание
     */
    void printStoredImage(boolean isFiscalDocument, long imageNumber, long align) throws Exception {
        DataPacket dp = new DataPacket();
        dp.putLongValue(align);
        dp.putLongValue(imageNumber);
        pc.sendRequest(PiritCommand.PRINT_GRAPHICS, dp);
        printLine(new FontLine(""), isFiscalDocument);
    }

    @Override
    public void printMoneyDocument(Money money) throws FiscalPrinterException {

        try {
            if (checkStateBeforeOpenDoc()) {
                return;
            }

            DataPacket dp = new DataPacket();
            if (money.getOperationType() == InventoryOperationType.CASH_IN) {
                dp.putLongValue(PIRIT_PACKET_MODE | PIRIT_MONEY_IN);
            } else {
                validateCashOutMoney(money);
                dp.putLongValue(PIRIT_PACKET_MODE | PIRIT_MONEY_OUT);
            }

            dp.putLongValue(getFiscalDocDepart(money));
            dp.putStringValue(getCashierName(money.getCashier()));
            dp.putLongValue(money.getCheckNumber());

            long taxSystem = getTaxSystem();
            if (taxSystem != -1) {
                dp.putLongValue(taxSystem);
            }

            pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);

            long totalSum = 0;

            if (money.getOperationType() == InventoryOperationType.CASH_IN) {
                for (BankNote bankNote : money.getBankNotes()) {
                    long value = bankNote.getValue();

                    StringBuilder row = new StringBuilder();

                    String str = money.getCurrency();
                    row.append(String.format("%10.10s", str));

                    str = bankNote.getValue() / PRICE_ORDER + "." + String.format("%02d", bankNote.getValue() % PRICE_ORDER);

                    row.append(String.format("%15.15s", str));

                    this.putRequisite(new Text(row.toString()), true);
                    totalSum += value;
                }
            } else {
                this.putRequisite(new Text(ResBundleFiscalPrinterPirit.getString("PD_CURRENCY_NAME") + money.getCurrency()), true);
                for (BankNote bankNote : money.getBankNotes()) {
                    long value = bankNote.getValue() * bankNote.getCount();

                    StringBuilder row = new StringBuilder();

                    String str = bankNote.getValue() / PRICE_ORDER + "." + String.format("%02d", bankNote.getValue() % PRICE_ORDER) + "x" + bankNote.getCount();

                    row.append(String.format("%25.25s", str));

                    str = " =" + value / PRICE_ORDER + "." + String.format("%02d", value % PRICE_ORDER);
                    row.append(String.format("%15.15s", str));

                    this.putRequisite(new Text(row.toString()), true);
                    totalSum += value;
                }

                if (money.getSumCoins() != null) {
                    StringBuilder row = new StringBuilder(String.format("%25.25s", ResBundleFiscalPrinterPirit.getString("PD_CASH_OUT_COINS")));
                    String str = " =" + money.getSumCoins() / PRICE_ORDER + "." + String.format("%02d", money.getSumCoins() % PRICE_ORDER);
                    row.append(String.format("%15.15s", str));

                    this.putRequisite(new Text(row.toString()), true);
                    totalSum += money.getSumCoins();
                }
            }

            dp.clear();
            dp.putStringValue(ResBundleFiscalPrinterPirit.getString("PD_SUM_CASH_IN_OUT") + money.getCurrency());
            dp.putDoubleValue((double) totalSum / PRICE_ORDER);
            pc.sendRequest(PiritCommand.ADD_MONEY_IN_OUT, dp, true);

            closeDocument(true, null);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void validateCashOutMoney(Money money) throws Exception {
        Long version = FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
        if (money.getValue() > getCashAmount() && (piritConfig.isCashDrawerMoneyControl() || version < PIRIT_VERSION_WITH_WITHDRAWAL_FIX)) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_SUM_GREATER_CASH_AMOUNT"), PiritErrorMsg.getErrorType());
        }
    }

    @Override
    public void printServiceDocument(SimpleServiceDocument serviceDocument) throws FiscalPrinterException {
        try {
            final PiritStatus piritStatus = getPiritStatus();
            if (piritStatus.needToStartWork()) {
                // По какой-то исторической несправедливости для сервисных документов, печатаемых через этот метод,
                // мы прям внутри метода отправляем команду "Начало работы", если это требуется
                startWork();
            }
            if (forceCloseOrCancelDocument(piritStatus)) {
                return;
            }

            long requisitesPrintingMode = 0L;
            if (serviceDocument.isDisableRequisites()) {
                piritConfig.setDisablePrinting(true);
                requisitesPrintingMode = REQUISITES_PRINTING_DEFERRED_MODE;
            }
            DataPacket dp = new DataPacket();
            dp.putLongValue(requisitesPrintingMode | PIRIT_PACKET_MODE | PIRIT_SERVICE_DOCUMENT);
            if (serviceDocument.getDepart() < MAX_DEPART_NUMBER) {
                dp.putLongValue(serviceDocument.getDepart());
            } else {
                dp.putLongValue(1L);
                LOG.warn("departNumber=" + serviceDocument.getDepart());
            }
            dp.putStringValue(getCashierName(serviceDocument.getCashier()));

            long taxSystem = getTaxSystem();
            if (taxSystem != -1) {
                dp.putLongValue(taxSystem);
            }

            pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);

            if (serviceDocument.isDisableRequisites()) {
                piritConfig.setDisablePrinting(false);
            }

            for (Row row : serviceDocument.getRows()) {
                if (row instanceof Text) {
                    this.putText((Text) row, true);
                } else if (row instanceof BarCode) {
                    this.putBarCode((BarCode) row, false);
                    this.putText(new Text(""), true);
                } else if (row instanceof Image) {
                    printImage((Image) row);
                }
            }

            closeDocument(true, serviceDocument);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printImage(Image row) throws Exception {
        long align = IMAGE_ALIGN_DEFAULT; // поумолчанию то которое было задано при загрузке
        switch (row.getAlign()) {
            case LEFT:
                align = IMAGE_ALIGN_LEFT;
                break;
            case CENTER:
                align = IMAGE_ALIGN_CENTER;
                break;
            case RIGHT:
                align = IMAGE_ALIGN_RIGHT;
                break;
            default:
                break;
        }
        switch (row.getSource()) {
            case STORED:
                printStoredImage(false, Long.parseLong(row.getValue()), align);
                break;
            case DATABASE:
            case FILE:
                if (row.getImageData() == null && row.getActualImage() == null) {
                    LOG.warn("Not specified image data for {} source", row.getSource());
                } else {
                    printImage(row, align);
                }
                break;
            default:
                LOG.warn("Requested to print image from {} source. But it is not supported yet", row.getSource());
                break;
        }
    }

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        try {
            DataPacket dp = new DataPacket();
            dp.putStringValue(getCashierName(report.getCashier()));
            pc.sendRequest(PiritCommand.PRINT_X_REPORT, dp);
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
            pc.sendRequest(PiritCommand.PRINT_Z_REPORT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        try {
            return piritConfig.getTaxes();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setPayments(List<PaymentType> payments) throws FiscalPrinterException {
        try {
            piritConfig.setPayments(payments);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        pluginProvider.setRequisites(requisites);
    }

    @Override
    public void setTaxes(ValueAddedTaxCollection taxes) throws FiscalPrinterException {
        try {
            piritConfig.setTaxes(taxes);
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
            piritConfig.setCashNumber(cashNumber);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Конфигурация печати лого
     *
     * @param doc текущий документ
     */
    private void configureLogoPrinting(AbstractDocument doc) {
        try {
            // печать лого необходимо конфигурировать только если данные о том должно ли печататься лого для текущего документа и для следующего не совпадают
            boolean logoEnabled = !(doc instanceof ServiceDocument && ((ServiceDocument) doc).isDisableLogo());
            boolean enableLogoForNext = doc == null || doc.getNextServiceDocument() == null || !doc.getNextServiceDocument().isDisableLogo();
            if (enableLogoForNext != logoEnabled) {
                piritConfig.setPrintLogo(enableLogoForNext);
            }
        } catch (Exception e) {
            LOG.warn("unable to configure logo printing");
        }
    }

    private void putRequisite(Text text, boolean isAsyncMode) throws Exception {

        long textProperties = fontManager.getTextAttributes(text);
        int maxCharRow = getMaxCharRow(text);

        if (text.getValue().length() > maxCharRow) {
            String totalText = text.getValue();
            while (totalText.length() > 0) {
                if (totalText.length() > maxCharRow) {
                    text.setValue(totalText.substring(0, maxCharRow));
                    totalText = totalText.substring(maxCharRow);
                } else {
                    text.setValue(totalText);
                    totalText = "";
                }

                DataPacket dp = new DataPacket();
                dp.putLongValue(0L);
                dp.putLongValue(textProperties);
                dp.putStringValue(text.getValue());

                sendRequest(PiritCommand.PRINT_REQUISITE, dp, isAsyncMode);
            }
        } else {
            DataPacket dp = new DataPacket();
            dp.putLongValue(0L);
            dp.putLongValue(textProperties);
            dp.putStringValue(text.getValue());

            sendRequest(PiritCommand.PRINT_REQUISITE, dp, isAsyncMode);
        }
    }

    private void encodeToBase42AndPrintBarcode(BarCode barCode, boolean isFiscal) throws Exception {
        String code = StringUtils.trimToEmpty(barCode.getValue());
        if (code.length() == COUPON_BARCODE_LENGTH && Base39Coder.canCodeToBase39(code)) {
            try {
                barCode.setValue(Base39Coder.base39Encode(code));
                barCode.setBarcodeLabel(code);
            } catch (Exception e) {
                return;
            }
        }

        printBarcodeWithLabel(barCode, isFiscal);
    }

    private void printBarcodeWithLabel(BarCode barCode, boolean isFiscal) throws Exception {
        if (barCode.getType() != BarCodeType.Code39 && barCode.getType() != BarCodeType.QR) {
            return;
        }

        DataPacket dp = new DataPacket();
        if (barCode.getBarcodeLabel() != null &&
                barCode.getTextPosition() == TextPosition.TOP_TEXT || barCode.getTextPosition() == TextPosition.TOP_AND_BOTTOM_TEXT) {
            FontLine barcodeLabel = new FontLine(StringUtils.center(barCode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
            printLine(barcodeLabel, isFiscal);
        }
        //Не печатаем текст ШК автоматом
        dp.putLongValue(0L);
        //
        dp.putLongValue(getBarCodeWidth(barCode));
        dp.putLongValue(barCode.getHeight());
        if (barCode.getType() == BarCodeType.Code39) {
            dp.putLongValue(4L);
        } else if (barCode.getType() == BarCodeType.QR) {
            dp.putLongValue(8L);
        }
        dp.putStringValue(barCode.getValue());
        sendRequest(PiritCommand.PRINT_BARCODE, dp, true);
        if (barCode.getBarcodeLabel() != null &&
                barCode.getTextPosition() == TextPosition.BOTTOM_TEXT || barCode.getTextPosition() == TextPosition.TOP_AND_BOTTOM_TEXT) {
            FontLine barcodeLabel = new FontLine(StringUtils.center(barCode.getBarcodeLabel(), getMaxCharRow()), Font.NORMAL);
            printLine(barcodeLabel, isFiscal);
        }
    }

    private long getBarCodeWidth(BarCode barCode) {
        if (BarCodeType.QR.equals(barCode.getType()) && barCode.isMaxQRCodeWidth()) {
            try {
                Long version = FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
                if (PIRIT_2F_RANGE.containsLong(version)) {
                    return MAX_QR_WIDTH_PIRIT_2F;
                } else if (PIRIT_1F_RANGE.containsLong(version)) {
                    return MAX_QR_WIDTH_PIRIT_1F;
                } else {
                    return barCode.getWidth();
                }
            } catch (FiscalPrinterException fpe) {
                return barCode.getWidth();
            }
        }
        return barCode.getWidth();
    }

    private void putBarCode(BarCode barcode, boolean isFicscal) throws Exception {
        putBarCode(barcode, isFicscal, true);
    }

    protected void putBarCode(BarCode barCode, boolean isFiscal, boolean isAsync) throws Exception {
        DataPacket dp = new DataPacket();

        if (barCode.getType() == BarCodeType.Code39 && StringUtils.trimToEmpty(barCode.getValue()).length() > MAX_LENGTH_CODE_39_BARCODE_ON_57_MM) {
            encodeToBase42AndPrintBarcode(barCode, isFiscal);
            return;
        } else if (barCode.isPrintBarcodeLabel() && barCode.getType() == BarCodeType.QR && !config.isPrintQRAsImage()) {
            printBarcodeWithLabel(barCode, isFiscal);
            return;
        }

        if (barCode.getTextPosition() == TextPosition.NONE_TEXT) {
            dp.putLongValue(0L);
        } else if (barCode.getTextPosition() == TextPosition.TOP_TEXT) {
            dp.putLongValue(1L);
        } else if (barCode.getTextPosition() == TextPosition.BOTTOM_TEXT) {
            dp.putLongValue(2L);
        } else if (barCode.getTextPosition() == TextPosition.TOP_AND_BOTTOM_TEXT) {
            dp.putLongValue(3L);
        }

        dp.putLongValue(getBarCodeWidth(barCode));
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
        } else if (barCode.getType() == BarCodeType.QR) {
            if (config.isPrintQRAsImage()) {
                printQRAsImage(barCode);
                return;
            }
            dp.putLongValue(8L);
        }
        dp.putStringValue(tailorBarcodeValueToMaxLength(barCode));
        sendRequest(PiritCommand.PRINT_BARCODE, dp, isAsync);
    }

    private void printQRAsImage(BarCode barCode) {
        GraphicsUtils.BitMapImage qrBitMap = GraphicsUtils.getQRBitMap(barCode.getBarcodeLabelOrText(), MIN_QR_SIZE_BYTES, MIN_QR_SIZE_BYTES, MAX_QR_SIZE_BYTES,
                MAX_QR_SIZE_BYTES, 0);
        printImageBase(qrBitMap.getImageWidth(), qrBitMap.getImageHeight(), IMAGE_ALIGN_CENTER, qrBitMap.getImageBytes());
    }

    /**
     * Печатает изображение.
     *
     * @param image строка с изображением, которою требуется напечатать
     * @param align выравнивание изображения. См. {@link #IMAGE_ALIGN_CENTER}, {@link #IMAGE_ALIGN_LEFT}, {@link #IMAGE_ALIGN_RIGHT}, {@link #IMAGE_ALIGN_DEFAULT}
     */
    private void printImage(Image image, long align) throws FiscalPrinterException {
        ImageData imageData = image.getImageData();
        if (image.getActualImage() != null) {
            if (imagePrintingRoutine == null) {
                LOG.warn("Unable to print image: No suitable image printing routine found");
                return;
            }
            imagePrintingRoutine.printImage(image.getActualImage(), align);
            return;
        }
        if (imageData != null) {
            printImageBase(imageData.getImageWidth(), imageData.getImageHeight(), align, imageData.getImageBytes());
            return;
        }
        LOG.warn("Unable to print {}: no image data provided", image);
    }

    /**
     * Печать изображения на чеке
     *
     * @param width  - ширина картинки
     * @param height - высота картинки
     * @param data   - графическое изображение
     */
    void printImageBase(long width, long height, long align, byte[] data) {
        try {
            DataPacket metaData = new DataPacket();
            metaData.putLongValue(width);
            metaData.putLongValue(height);
            metaData.putLongValue(align);
            pc.sendBinaryRequest(PiritCommand.PRINT_IMAGE_QR, metaData, data);
        } catch (FiscalPrinterException e) {
            LOG.error("Error printing image by 0x55 command", e);
        }
    }

    /**
     * Печать изображения на чеке в формате PNG
     *
     * @param width  - ширина картинки
     * @param height - высота картинки
     * @param align  - выравнивание
     * @param data   - графическое изображение
     */
    void printImagePNGBase(long width, long height, long align, byte[] data) {
        try {
            DataPacket metaData = new DataPacket();
            metaData.putLongValue((long) data.length);
            metaData.putLongValue(width);
            metaData.putLongValue(height);
            metaData.putLongValue(align);
            pc.sendBinaryRequest(PiritCommand.PRINT_IMAGE_PNG, metaData, data);
        } catch (FiscalPrinterException e) {
            LOG.error("Error printing PNG image by 0x67 command", e);
        }
    }

    protected String tailorBarcodeValueToMaxLength(BarCode barCode) {
        return barCode.getValue();
    }

    protected void putText(Text text, boolean isAsyncMode) throws Exception {
        final long textProperties = fontManager.getTextAttributes(text);
        final int maxCharRow = getMaxCharRow(text);

        String prefix = "";
        Long version = FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
        if (PIRIT_2F_RANGE.containsLong(version) && !text.isEndOfLine()) {
            prefix = NOT_NEW_LINE_PIRIT_2F_PREFIX;
        }
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

                DataPacket dp = new DataPacket();
                dp.putStringValue(prefix + text.getValue());
                dp.putLongValue(textProperties);

                sendRequest(PiritCommand.PRINT_STRING, dp, isAsyncMode);
            }
        } else {
            DataPacket dp = new DataPacket();
            dp.putStringValue(prefix + text.getValue());
            dp.putLongValue(textProperties);

            sendRequest(PiritCommand.PRINT_STRING, dp, isAsyncMode);
        }
    }

    protected void putDiscounts(List<Disc> discountList, boolean isAsyncMode) throws Exception {
        for (Disc discs : discountList) {
            DataPacket dp = new DataPacket();

            if (discs.getType() == DiscType.PERCENT) {
                dp.putLongValue(0L);
            } else {
                dp.putLongValue(1L);
            }

            // ограничение на длину названия скидки
            if (discs.getName().length() > DISC_AND_MARGIN_NAME_MAX_LENGTH) {
                dp.putStringValue(discs.getName().substring(0, DISC_AND_MARGIN_NAME_MAX_LENGTH));
            } else {
                dp.putStringValue(discs.getName());
            }
            dp.putDoubleValue((double) discs.getValue() / PRICE_ORDER);

            sendRequest(PiritCommand.ADD_DISCOUNT, dp, isAsyncMode);
        }
    }

    protected void putMargin(List<Margin> _margin, boolean isAsyncMode) throws Exception {
        for (Margin margin : _margin) {
            DataPacket dp = new DataPacket();

            if (margin.getType() == MarginType.PERCENT) {
                dp.putLongValue(0L);
            } else {
                dp.putLongValue(1L);
            }

            // ограничение на длину названия скидки
            if (margin.getName().length() > DISC_AND_MARGIN_NAME_MAX_LENGTH) {
                dp.putStringValue(margin.getName().substring(0, DISC_AND_MARGIN_NAME_MAX_LENGTH));
            } else {
                dp.putStringValue(margin.getName());
            }
            dp.putDoubleValue((double) margin.getValue() / PRICE_ORDER);

            sendRequest(PiritCommand.ADD_MARGIN, dp, isAsyncMode);
        }
    }

    public void putGoods(List<Goods> goods, boolean isAsyncMode) throws Exception {
        int posNum = 0;

        for (Goods good : goods) {
            DataPacket dp = new DataPacket();

            if (config.isPrintGoodsName()) {
                if (good.getName() == null) {
                    dp.putStringValue(defaultGoodsName);
                } else {
                    // ограничение на длину названия товара (см. док-цию команда 42)
                    if (good.getName().length() > config.getGoodNameMaxLength()) {
                        dp.putStringValue(good.getName().substring(0, config.getGoodNameMaxLength()));
                    } else {
                        dp.putStringValue(good.getName());
                    }
                }
            } else {
                dp.putStringValue("");
            }

            if (config.isPrintItem()) {
                // ограничение на длину итема (см. док-цию команда 42)
                if (good.getItem().length() > ITEM_MAX_LENGTH) {
                    dp.putStringValue(good.getItem().substring(0, ITEM_MAX_LENGTH));
                } else {
                    dp.putStringValue(good.getItem());
                }
            } else {
                dp.putStringValue("");
            }

            long calculatedEndPrice = good.getEndPricePerUnit();
            long calculatedDiscount = CurrencyUtil.getPositionSum(good.getEndPricePerUnit(), good.getQuant()) - good.getEndPositionPrice();
            dp.putDoubleValue((double) good.getQuant() / COUNT_ORDER);
            dp.putDoubleValue((double) calculatedEndPrice / PRICE_ORDER);

            ValueAddedTax tax = taxes.lookupByValue(good.getTax());
            if (tax == null) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_TAX_VALUE"), PiritErrorMsg.getErrorType());
            }
            dp.putLongValue(tax.index);

            if (config.isPrintPosNum()) {
                dp.putStringValue(String.format("%3d ", ++posNum));
            } else {
                dp.putStringValue("");
            }

            if (calculatedDiscount > 0) {
                dp.putLongValue(0L);
                dp.putLongValue(2L);
                dp.putStringValue("Скидка");
                dp.putDoubleValue((double) calculatedDiscount / PRICE_ORDER);
            } else if (calculatedDiscount < 0) {
                dp.putLongValue(0L);
                dp.putLongValue(4L);
                dp.putStringValue("Наценка");
                dp.putDoubleValue(((double) calculatedDiscount * -1) / PRICE_ORDER);
            }

            sendRequest(PiritCommand.ADD_ITEM, dp, isAsyncMode);

        }
    }

    public void putPayments(List<Payment> payments, boolean isAsyncMode) throws Exception {
        for (Payment payment : payments) {
            DataPacket dp = new DataPacket();

            dp.putLongValue(payment.getIndexPayment());
            dp.putDoubleValue((double) payment.getSum() / PRICE_ORDER);
            dp.putStringValue("");

            sendRequest(PiritCommand.ADD_PAYMENT, dp, isAsyncMode);
        }
    }

    public void processPaymentSection(List<Payment> payments, boolean isAsyncMode) throws Exception {
        pc.sendRequest(PiritCommand.SUBTOTAL, true);
        putPayments(payments, isAsyncMode);
    }

    public String getPort() {
        return ComPortUtil.getRealSystemPortName(config.getPort());
    }

    public void setPort(String port) {
        this.config.setPort(port);
    }

    @Override
    public int getMaxCharRow(Font font, Integer extendedFont) {
        if (extendedFont != null) {
            return fontManager.getMaxCharRow(extendedFont);
        }
        return fontManager.getMaxCharRow(font);
    }

    protected int getMaxCharRow(Text text) {
        return fontManager.getMaxCharRow(text);
    }

    @Override
    public int getPaymentLength() {
        return piritConfig.getMaxPaymentNameLength();
    }

    /**
     * Метод запрашивает версию прошивки Пирита и сравнивает с ожидаемой
     *
     * @throws FiscalPrinterException - если прошивка не соответствует ожидаемой
     */
    @Override
    public boolean verifyDevice() throws FiscalPrinterException {
        LOG.info("verifyDevice");
        try {
            long firmware = FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
            LOG.info("Firmware = {}", firmware);
            validateFirmware(getTargetMinimalFirmwareVersion(firmware), firmware);
            if (firmware < 200) {
                if (firmware == MINIMAL_SUPPORTED_VERSION) {
                    // Залипень для ru.crystals.lenta.fiscalprinter.test.LentaPiritTemplatesTest,
                    // чтобы не ловить проверку ДЯ на PiritConnector#normalizedPacket
                    // (13 версия настолько старая, что в реальной жизни ни у кого ее нет)
                    pc.setMaxAsyncCommandBuffer(Long.MAX_VALUE);
                }
                piritConfig.setPiritK(false);
                fontManager.configure(piritConfig, config, FontConfiguration.PIRIT_1F, 0);
            } else {
                piritConfig.setPiritK(true);
                piritConfig.useWidePaper(isUseWidePaper());

                if (firmware < 600) {
                    /* Ставим большой буфер только для Pirit K/2Ф
                       (Пирит 1Ф и википринты будут использовать меньший буфер по умолчанию) */
                    pc.setMaxAsyncCommandBuffer(MAX_ASYNC_COMMAND_BUFFER_SIZE_PIRIT_II);
                    configureFont2F(getDesignNumber());
                } else {
                    fontManager.configure(piritConfig, config, FontConfiguration.VIKIPRINT_80, 0);
                }
            }
            Optional.ofNullable(config.getMaxAsyncCommandBuffer()).ifPresent(pc::setMaxAsyncCommandBuffer);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private int getDesignNumber() {
        try {
            return piritConfig.getDesignNumber();
        } catch (FiscalPrinterException e) {
            LOG.debug("Unable to get design number", e);
        }
        return 0;
    }

    protected void configureFont2F(int designNumber) {
        fontManager.configure(piritConfig, config, piritConfig.isUseWidePaper() ? FontConfiguration.PIRIT_2F : FontConfiguration.PIRIT_2F_NARROW, designNumber);
    }

    protected long getTargetMinimalFirmwareVersion(long currentVersion) {
        return MINIMAL_SUPPORTED_VERSION;
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер
     * последней закрытой
     */
    @Override
    public abstract long getShiftNumber() throws FiscalPrinterException;

    @Override
    public long getCountCashIn() throws FiscalPrinterException {
        try {
            long result;
            if (isShiftOpen()) {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);
                result = dp.getLongValue(5);
            } else {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_LAST_REPORT);
                result = dp.getLongValue(10);
            }
            LOG.info("CashIn = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public long getCountCashOut() throws FiscalPrinterException {
        try {
            long result;
            if (isShiftOpen()) {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);
                result = dp.getLongValue(6);
            } else {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_LAST_REPORT);
                result = dp.getLongValue(12);
            }
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
        long result;
        try {
            if (isShiftOpen()) {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_DOC_COUNTS_BY_TYPE);
                result = dp.getLongValue(3);
            } else {
                DataPacket dp = pc.sendRequest(ExtendedCommand.GET_COUNTERS_LAST_REPORT);
                result = dp.getLongValue(8);
            }
            LOG.info("CountAnnul = {}", result);
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Метод возвращает количество денег в денежном ящике ККМ
     */
    @Override
    public long getCashAmount() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_CASH_AMOUNT);
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
            // fatal
            DataPacket dp = pc.sendRequest(PiritCommand.GET_STATUS);

            final int state = dp.getIntegerSafe(0).orElse(0);
            status.setLongStatus(state);

            if (hasBit(state, 0)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("INVALID_CHECKSUM_NVR"));
            }

            if (hasBit(state, 1)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("INCORRECT_CHECKSUM_CONFIGURATION"));
            }

            if (hasBit(state, 2)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_INTERFACE_FP"));
            }

            if (hasBit(state, 3)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("INVALID_CHECKSUM_FISCAL_MEMORY"));
            }

            if (hasBit(state, 4)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_WRITING_TO_FISCAL_MEMORY"));
            }

            if (hasBit(state, 5)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("FISCAL_MODULE_NOT_AUTHORIZED"));
            }

            if (hasBit(state, 6)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("FAILURE_EKLZ"));
            }

            if (hasBit(state, 7)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("DISCREPANCY_BETWEEN_FP_AND_EKLZ"));
            }

            checkStatusOfEklzAndFiscalMemory(status, dp);

            if (!status.getDescriptions().isEmpty()) {
                status.setStatus(StatusFP.Status.FATAL);
                return status;
            }

            // printer
            dp = pc.sendRequest(PiritCommand.GET_PRINTER_STATE);

            final int printerState = dp.getIntegerSafe(0).orElse(0);
            status.setLongStatus(printerState);

            if (hasBit(printerState, 0)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("WARN_PRINTER_NOT_READY"));
            }

            if (hasBit(printerState, 2)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("OPEN_PRINTER_COVER"));
                status.setStatus(StatusFP.Status.OPEN_COVER);
            }

            if (hasBit(printerState, 1)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("WARN_END_OF_PAPER"));
                status.setStatus(StatusFP.Status.END_PAPER);
            }

            if (hasBit(printerState, 3)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_CUTTER_PRINTER"));
            }

            if (hasBit(printerState, 7)) {
                status.addDescription(ResBundleFiscalPrinterPirit.getString("NO_COMMUNICATION_WITH_PRINTER"));
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
        return status;
    }

    protected void checkStatusOfEklzAndFiscalMemory(StatusFP status, DataPacket statusDataPacket) throws Exception {
        int val = statusDataPacket.getIntegerSafe(1).orElse(0);
        if (hasBit(val, 4) || hasBit(val, 5)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_STATE_EKLZ"));
        } else if (hasBit(val, 6)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_FREE_FISCAL_MEMORY"));
        } else if (hasBit(val, 7)) {
            status.addDescription(ResBundleFiscalPrinterPirit.getString("ERROR_PASSWORD_FOR_ACCESS_TO_FISCAL_MEMORY"));
        }
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterPirit.getString("DEVICE_NAME");
    }

    @Override
    public void postProcessNegativeScript(Exception ePrint) throws FiscalPrinterException {
        LOG.info("Reconnect fiscal: {}", pa.getLoggedInn());
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
        postProccesingPrintedDocument(sectionList, document);
    }

    /**
     * Метод для пост обработки документов после печати
     *
     * @param sectionList Шаблон документа
     * @param document    документ
     */
    protected void postProccesingPrintedDocument(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
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
            Money m = (Money) document;
            printMoneyByTemplate(sectionList, m);
        } else if (document instanceof DiscountsReport) {
            printServiceByTemplate(sectionList, document);
        } else if (document instanceof BonusCFTDocument) {
            printBonusCFTReportByTemplate(sectionList, document);
        } else if (document instanceof DailyLogData) {
            printBankDailyReportByTemplate(sectionList, document);
        } else if (document instanceof PlastekDocument) {
            printPlastekAccrueBonusesReportByTemplate(sectionList, (PlastekDocument) document);
        } else if (document instanceof PresentCardInfoReport) {
            printPresentCardInfoByTemplate(sectionList, document);
        } else if (document instanceof PresentCardReplaceReport) {
            printPresentCardReplaceByTemplate(sectionList, document);
        } else {
            //Сюда провалятся отчет по проданным товарам и кассирам для РБ.
            printServiceByTemplate(sectionList, document);
        }
    }

    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        if (check.isAnnul()) {
            printAnnulCheckByTemplate(sectionList, check);
        } else if (check.isCopy() || check instanceof FullCheckCopy) {
            printCopyCheckByTemplate(sectionList, check);
        } else {
            try {
                pluginProvider.printCheckByTemplate(sectionList, check);
            } catch (FiscalPrinterException e) {
                if (e.getCause() != null && e.getCause() instanceof FiscalPrinterCommunicationException) {
                    FiscalPrinterCommunicationException cause = (FiscalPrinterCommunicationException) e.getCause();
                    if (cause.getErrorCode() != null && (cause.getErrorCode() == 0x73 || cause.getErrorCode() == 0x7E)) {
                        throw new FiscalPrinterNeedToRevalidateCodeMarks(
                                ResBundleFiscalPrinterPirit.getString("MARKED_GOODS_SHOULD_BE_REVALIDATED"), PiritErrorMsg.getErrorType(), pa.getLoggedInn());
                    }
                }
                throw e;
            }
        }
    }

    private void printBankDailyReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        pluginProvider.printBankDailyReportByTemplate(sectionList, document);
    }

    private void printPlastekAccrueBonusesReportByTemplate(List<DocumentSection> sectionList, PlastekDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case SECTION_REPORT:
                        openServiceDocument(document);
                        printLinesList(section.getContent());
                        break;
                    case SECTION_CUT:
                        closeDocument(true, null);
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }
        } catch (FiscalPrinterException e) {
            // пусть оно наверх уходит
            throw e;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printBonusCFTReportByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                switch (section.getName()) {
                    case SECTION_LOGO:
                        printLogo();
                        break;
                    case SECTION_OPERATION_LIST:
                        openServiceDocument(document);
                        printLinesList(section.getContent());
                        break;
                    case SECTION_CUT:
                        closeDocument(true, null);
                        break;
                }
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    public void printServiceByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                printSection(section, document);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printSection(DocumentSection section, FiscalDocument document) throws Exception {
        switch (section.getName()) {
            case SECTION_LOGO:
                printLogo();
                break;
            case SECTION_HEADER:
                openServiceDocument(document);
                break;
            case SECTION_KKT_INFO:
                if (isNeedPrintKKTInfo()) {
                    printLinesList(section.getContent());
                }
                break;
            case SECTION_CUT:
                closeDocument(true, null);
                break;
            case SECTION_FOOTER:
                break;
            default:
                printLinesList(section.getContent());
                break;
        }
    }

    private void printPresentCardInfoByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                printSection(section, document);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printPresentCardReplaceByTemplate(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        try {
            for (DocumentSection section : sectionList) {
                printSection(section, document);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void printMoneyByTemplate(List<DocumentSection> sectionList, Money money) throws FiscalPrinterException {
        pluginProvider.printMoneyByTemplate(sectionList, money);
    }

    public void printReportByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        pluginProvider.printReportByTemplate(sectionList, report);
    }

    private void printReportCopyByTemplate(List<DocumentSection> sectionList, Report report) throws FiscalPrinterException {
        try {
            if (report.isXReport()) {
                throw new UnsupportedOperationException("X-report's copy doesn't support");
            } else {
                for (DocumentSection section : sectionList) {
                    switch (section.getName()) {
                        case SECTION_LOGO:
                            openServiceDocument(report);
                            printLogo();
                            break;
                        case SECTION_HEADER:
                            printLinesList(section.getContent());
                            break;
                        case SECTION_KKT_INFO:
                            if (isNeedPrintKKTInfo()) {
                                printLinesList(section.getContent());
                            }
                            break;
                        case SECTION_FISCAL:
                            closeDocument(true, null);
                            break;
                        case SECTION_FOOTER:
                            break;
                        default:
                            printLinesList(section.getContent());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    void processPositionSection(Check check, DocumentSection section) throws Exception {
        if (isOFDDevice() && getCountry().equals(PiritCountry.RU)) {
            if (check.isCopy() || check.isAnnul() || check instanceof FullCheckCopy) {
                printLinesListInCheck(section.getContent(), check);
                fiscalizeSum((double) check.getCheckSumEnd() / 100);
            } else {
                printLinesListInCheck(section.getContent(), check);
                putGoods(check.getGoods(), true);
            }
        } else {
            printLinesListInCheck(section.getContent(), check);
            fiscalizeSum((double) check.getCheckSumEnd() / 100);
        }
    }

    protected void printCopyCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        pluginProvider.printCopyCheckByTemplate(sectionList, check);
    }

    private void printAnnulCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        pluginProvider.printAnnulCheckByTemplate(sectionList, check);
    }

    public void printLogo() throws FiscalPrinterException {
    }

    public void annulCheck() throws FiscalPrinterException {
        pc.sendRequest(PiritCommand.CANCEL_DOCUMENT);
    }

    protected void fiscalMoneyDocument(Money money) throws Exception {
        pluginProvider.fiscalMoneyDocument(money);
    }

    public void sendRequest(PiritCommand command, boolean isAsyncMode) throws FiscalPrinterException {
        sendRequest(command, null, isAsyncMode);
    }

    public void sendRequest(PiritCommand command, DataPacket dp, boolean isAsyncMode) throws FiscalPrinterException {
        pc.sendRequest(command, dp, isAsyncMode);
    }

    private long getTaxSystem() {
        return taxSystem;
    }

    protected long readTaxSystem() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_TAX_WORK_MODE);
            return dp.getLongValue(1);
        } catch (Exception e) {
            throw new FiscalPrinterException("Failed to get taxSystem", e);
        }
    }

    public void openServiceDocument(FiscalDocument document) throws Exception {
        try {
            if (checkStateBeforeOpenDoc()) {
                return;
            }

            DataPacket dp = new DataPacket();

            dp.putLongValue(PIRIT_PACKET_MODE | PIRIT_SERVICE_DOCUMENT);
            dp.putLongValue(1L);
            dp.putStringValue(getCashierName(document.getCashier() == null ? new Cashier("", "", "") : document.getCashier()));

            long taxSystem = getTaxSystem();
            if (taxSystem != -1) {
                dp.putLongValue(taxSystem);
            }

            pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    public void openDocument(FiscalDocument doc) throws Exception {

        final PiritStatus piritStatus = getPiritStatus();

        if (piritStatus.needToStartWork()) {
            if (doc instanceof Check) {
                Check check = (Check) doc;
                if (!check.isAnnul() && check.hasMarksToBeCheckedInFp()) {
                    // Есть товары с марками, которые требуют проверки в ФР, нужно перепроверить их еще раз перед регистрацией чека.
                    // При аннулировании это не нужно делать, т.к. не будет происходить фискализация чека.
                    throw new FiscalPrinterNeedToRevalidateCodeMarks(
                            ResBundleFiscalPrinterPirit.getString("MARKED_GOODS_SHOULD_BE_REVALIDATED"), PiritErrorMsg.getErrorType(), pa.getLoggedInn());
                }
            }
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("WARN_NEED_RESTART"), PiritErrorMsg.getErrorType());
        }

        forceCloseOrCancelDocument(piritStatus);

        DataPacket dp = new DataPacket();

        long checkNumber = 0L;

        if (doc instanceof Check) {
            Check check = (Check) doc;

            long docTypeCode = PIRIT_PACKET_MODE;
            if (check.getType() == CheckType.SALE) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT : PIRIT_CHECK_SALE;
            } else if (check.getType() == CheckType.RETURN) {
                docTypeCode = CashOperation.EXPENSE.equals(check.getOperation()) ? PIRIT_EXPENSE_RECEIPT_RETURN : PIRIT_CHECK_RETURN;
            }
            long value = PIRIT_PACKET_MODE | docTypeCode;
            if (isOFDDevice() && getCountry().equals(PiritCountry.RU)) {
                if (check.isCopy() || check.isAnnul() || check instanceof FullCheckCopy) {
                    //Эти документы в ОФД не передаются так что менять ничего не будем
                } else {
                    value |= PIRIT_EXTENDED_MODE_NO_POSITION_PRINT;
                }
            }
            if (check.isDisablePrint()) {
                value |= NO_PRINTING_MODE;
            }
            dp.putLongValue(value);
            checkNumber = check.getCheckNumber();
        } else if (doc instanceof Money) {
            Money money = (Money) doc;
            if (money.getOperationType() == InventoryOperationType.CASH_IN) {
                dp.putLongValue(PIRIT_PACKET_MODE | PIRIT_MONEY_IN);
            } else {
                validateCashOutMoney(money);
                dp.putLongValue(PIRIT_PACKET_MODE | PIRIT_MONEY_OUT);
            }
            checkNumber = money.getCheckNumber();
        }

        dp.putLongValue(getFiscalDocDepart(doc));
        dp.putStringValue(getCashierName(doc.getCashier()));
        dp.putLongValue(checkNumber);

        long taxSystem = getTaxSystem();
        if (taxSystem != -1) {
            dp.putLongValue(taxSystem);
        }

        pc.sendRequest(PiritCommand.OPEN_DOCUMENT, dp);
    }

    public void printLinesList(List<FontLine> stringList) throws Exception {
        printLinesList(stringList, false);
    }

    public void printLinesListInDoc(List<FontLine> stringList) throws Exception {
        printLinesList(stringList, true);
    }

    public void printLinesListInCheck(List<FontLine> stringList, Check check) throws Exception {
        if (check == null || !check.isDisablePrint()) {
            printLinesListInDoc(stringList);
        }
    }

    public void printLinesList(List<FontLine> stringList, boolean isFiscal) throws Exception {
        for (FontLine str : stringList) {
            if (str != null) {
                if (str.getBarcode() != null) {
                    putBarCode(str.getBarcode(), isFiscal);
                    continue;
                }
                if (str.getImage() != null) {
                    printImage(str.getImage());
                    continue;
                }
                printLine(str, isFiscal);
            }
        }
    }

    public void printLine(FontLine line, boolean isFiscal) throws Exception {
        printLine(line, isFiscal, true);
    }

    protected void printLine(FontLine line, boolean isFiscal, boolean isAsync) throws Exception {
        PiritCommand cmd;
        DataPacket dp = new DataPacket();
        String content = line.getContent().length() > piritConfig.getMaxCharCountInPrintCommand() ?
                line.getContent().substring(0, piritConfig.getMaxCharCountInPrintCommand()) : line.getContent();

        long version = FirmwareVersionUtils.parseVersion(getFirmware(), FirmwareVersionUtils.VersionType.MAJOR_VERSION).orElse(0L);
        //пирит 1ф до 161 версии прошивки при печати 0 шрифтом обрезает строку после знака %, у других моделей такого нет
        if (version >= 150 && version < 161 && content.contains("%") && "normal".equalsIgnoreCase(line.getFont().name())) {
            content = content.replaceAll("%", "%%");
        }

        if (isFiscal) {
            cmd = PiritCommand.PRINT_REQUISITE;
            dp.putLongValue(0L);
            dp.putIntValue(fontManager.getFontAttribute(line));
            dp.putStringValue(content);
        } else {
            cmd = PiritCommand.PRINT_STRING;
            dp.putStringValue(content);
            dp.putIntValue(fontManager.getFontAttribute(line));
        }
        sendRequest(cmd, dp, isAsync);
    }

    private void fiscalizeSum(double sum) throws Exception {
        pluginProvider.fiscalizeSum(sum);
    }

    public void closeDocument(boolean cutFlag, AbstractDocument doc) throws Exception {
        cutFlag = cutFlag && (doc == null || doc.getNextServiceDocument() == null || !doc.getNextServiceDocument().isDisableCut());
        DataPacket dp = new DataPacket();
        if (doc instanceof Check) {
            Check check = (Check) doc;
            try {
                validateCheckSum(check.getCheckSumEnd());
            } catch (FiscalPrinterException fpe) {
                pc.sendRequest(PiritCommand.CANCEL_DOCUMENT);
                throw fpe;
            }
            if (!cutFlag) {
                dp.putLongValue(5L);
            } else {
                dp.putLongValue(0L);
            }

            putCloseDocumentData(check, dp);
        } else if (!cutFlag) {
            dp.putLongValue(1L);
        }

        // проверку необходимо ли печатать лого необходимо осуществлять при закрытии любого документа
        configureLogoPrinting(doc);

        pc.sendRequest(PiritCommand.CLOSE_DOCUMENT, dp);
    }

    /**
     * Устанавливает входные параметры команды завершить документ, зависящие от модели и версии прошивки ФР
     */
    protected void putCloseDocumentData(Check check, DataPacket dp) throws FiscalPrinterException {
        //В базовой реализации дополнительные входные параметры отсутствуют
    }

    /**
     * Метод обрезает наименование клиента если это необходимо.
     * Для Pirit1F максимальная длинна 64 символа, для Pirit2F 82 символа
     *
     * @param clientName наименование клиента
     * @return наименование
     */
    protected String cutClientNameIfNeeded(String clientName) throws FiscalPrinterException {
        if (clientName == null) {
            return null;
        }

        long firmwareVersion = getFirmwareLong();
        if (PIRIT_2F_RANGE.containsLong(firmwareVersion)) {
            return clientName.substring(0, Math.min(clientName.length(), pirit2fMaxCustomerNameLength));
        } else if (PIRIT_1F_RANGE.containsLong(firmwareVersion)) {
            return clientName.substring(0, Math.min(clientName.length(), pirit1fMaxCustomerNameLength));
        } else {
            return clientName;
        }
    }

    protected void throwUnknownError(Exception cause) throws FiscalPrinterException {
        LOG.error("", cause);
        throw new FiscalPrinterException(ResBundleFiscalPrinter.getString("UNKNOWN_ERROR"), CashErrorType.FISCAL_ERROR);
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        try {
            return pc.sendRequest(ExtendedCommand.GET_INFO_FACTORY_NUM).getStringValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    boolean isUseWidePaper() throws Exception {
        DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_PAPER_SIZE);
        return dp.getLongValue(1) == GET_INFO_PAPER_SIZE_80_MM;
    }

    public PiritConfig getPiritConfig() {
        return piritConfig;
    }

    public boolean isOFDDevice() {
        return false;
    }

    @Override
    public void printFNReport(Cashier cashier) throws FiscalPrinterException {
        if (isOFDDevice()) {
            pa.printCurrentFNReport(cashier);
        } else {
            throw new FiscalPrinterException(ResBundleFiscalPrinter.getString("ERROR_COMMAND_NOT_SUPPORTED"));
        }
    }

    private void validateFirmware(long minimalFirmwareVersion, long currentFirmwareVersion) throws FiscalPrinterException {
        if (currentFirmwareVersion < minimalFirmwareVersion) {
            String strError = ResBundleFiscalPrinterPirit.getString("ERROR_UNSUPPORTABLE_FIRMWARE_VERSION").replace("{1}", "(" + minimalFirmwareVersion + ")")
                    .replace("{2}", "(" + currentFirmwareVersion + ")");
            throw new FiscalPrinterException(strError, CashErrorType.FATAL_ERROR);
        }
    }

    public PiritCountry getCountry() {
        return PiritCountry.RU;
    }

    @Override
    public FiscalDocumentData getLastFiscalDocumentData() throws FiscalPrinterException {
        try {
            /*
                Поля в ответе по индексам:
                1 тип чека (для аннулиров. = 0)
                3 номер чека,
                5 сумма чека,
                8 строка ФП (фиск. признак),
                9 Номер ФД
            */
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_RECEIPT_DATA_LAST);
            final FiscalDocumentType docType = FiscalDocumentType.getTypeByCode(dp.getLongValue(1));
            if (!CHECK_DOC_TYPES.contains(docType)) {
                return null;
            }
            final long lastFD = getLastKpk();
            final long lastDocumentFD = dp.getLongValue(9);
            if (lastDocumentFD != lastFD) {
                LOG.warn("Last doc data has another FD number. Result will be discarded");
                return null;
            }

            FiscalDocumentData result = new FiscalDocumentData();
            result.setNumFD(lastFD);
            result.setType(docType);
            result.setSum(dp.getDoubleMoneyToLongValue(5));
            result.setOperationDate(getLastFiscalOperationDate());
            result.setFiscalSign(dp.getLongValue(8));
            result.setFnNumber(getEklzNum());
            result.setQrCode(PluginUtils.buildQRCode(result, null));
            return result;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Получить информацию о ФН
     */
    private FnInfo getFnInfo() throws FiscalPrinterException {
        FnInfo fnInfo = new FnInfo();
        fnInfo.setFnNumber(getEklzNum());
        fnInfo.setFnStatus(getFnStatus());
        fnInfo.setFnVersion(pa.getVersionFN().getVerFn());
        fnInfo.setLastFDNumber(String.valueOf(getLastKpk()));

        StatusOFD statusOFD = pa.getStatusOFD();
        fnInfo.setNotSentDocCount((int) statusOFD.getCountDocForOFD());
        if (statusOFD.getNumFirstNoSentDoc() != 0) {
            FnDocInfo firstNotSentDoc = new FnDocInfo();
            firstNotSentDoc.setNumber(statusOFD.getNumFirstNoSentDoc());
            if (statusOFD.getFirstNotSentDocDateTime() != null) {
                firstNotSentDoc.setDate(statusOFD.getFirstNotSentDocDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            fnInfo.setFirstNotSentDoc(firstNotSentDoc);
        }
        // Будет выпилено в SRTZ-401
        fnInfo.setLastNotSendedFDNumber(statusOFD.getNumFirstNoSentDoc());
        fnInfo.setFirstNotSendedFDDate(statusOFD.getDateFistNoSentDocOriginStr());
        fnInfo.setNotSendedFDCount(statusOFD.getCountDocForOFD());
        return fnInfo;
    }

    private FnStatus getFnStatus() throws FiscalPrinterException {
        StatusFN statusFN = pa.getStatusFN();
        return new FnStatus(statusFN.getStateFN(), statusFN.getStateCurrentDoc(), statusFN.getFlags());
    }

    /**
     * Получить сумму текущего открытого документа.
     *
     * @return Сумма чека в копейках
     */
    private long getCurrentCheckSum() throws FiscalPrinterException {
        try {
            return pc.sendRequest(ExtendedCommand.GET_RECEIPT_DATA_CURRENT).getDoubleMoneyToLongValue(1);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Сверяем сумму чека в ФР.
     *
     * @param sum - Сумма чека в копейках
     */
    private void validateCheckSum(long sum) throws FiscalPrinterException {
        if (sum != getCurrentCheckSum()) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("UNCORRECT_CHECK_SUM"), CashErrorType.FISCAL_ERROR);
        }
    }

    public PrinterClass getPrinterClass() {
        return PrinterClass.PIRIT;
    }

    @Override
    public FiscalDevice getImagePrintingType() throws FiscalPrinterException {
        long hardwareId = getFirmwareLong();
        if (PIRIT_1F_RANGE.containsLong(hardwareId)) {
            return FiscalDevice.PIRIT_1;
        } else if (PIRIT_2F_RANGE.containsLong(hardwareId)) {
            return FiscalDevice.PIRIT_2;
        }
        return null;
    }

    public void setCurrentFirmwareVersion(String currentFirmwareVersion) {
        this.currentFirmwareVersion = currentFirmwareVersion;
    }

    @Override
    public boolean isNeedPrintKKTInfo() throws FiscalPrinterException {
        if (!config.isNeedPrintKKTInfo()) {
            return false;
        }
        final long fw = getFirmwareLong();
        return PIRIT_2F_RANGE.containsLong(fw) || PIRIT_VIKI_RANGE.containsLong(fw);
    }

    /**
     * Добавление реквизитов чека (например, ОФД), вызывается до закрытия документа
     */
    public void putCheckRequisites(Check check) throws FiscalPrinterException {
        // Ничего не добавляем в базовой версии
    }

    protected boolean useComProxy() {
        return true;
    }

    public PiritPluginConfig getConfig() {
        return config;
    }

    protected FiscalPrinterInfo getFiscalPrinterInfoInner() throws FiscalPrinterException {
        FiscalPrinterInfo fiscalPrinterInfo = new FiscalPrinterInfo();
        fiscalPrinterInfo.setFirmware(getFirmware());
        fiscalPrinterInfo.setFnInfo(getFnInfo());
        fiscalPrinterInfo.setUrlOfd(pa.getUrlOFD());
        fiscalPrinterInfo.setServiceInfo(pa.getPrinterServiceInfo());
        fiscalPrinterInfo.setProxySoftware(comProxyService.getInfo());
        return fiscalPrinterInfo;
    }

    protected PiritAgent createPiritAgent(PiritConnector piritConnector) {
        return new PiritAgent(piritConnector);
    }

    public void setCommPortIdentifierSource(CommPortIdentifierSource commPortIdentifier) {
        pc.setCommPortIdentifierSource(commPortIdentifier);
    }

}
