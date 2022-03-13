package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.IncrescentTotal;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.SetExchangeParamCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDataStorage;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDeviceType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihItemCode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihOperation;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotalV2Ex;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShiftCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.transport.ShtrihTransport;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.DocumentUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Реализация для устройств с ФН через драйвер https://github.com/shtrih-m/fr_drv_ng
 */
@PrototypedComponent
public class ShtrihServiceDriver extends BaseShtrihServiceFN implements Configurable<ShtrihConfiguration> {
    private static final int[] BAUD_RATES = new int[]{2400, 4800, 9600, 19200, 38400, 57600, 115200};

    private static final Map<Integer, Integer> BAUD_RATE_INDEXES_BY_RATE = IntStream.range(0, BAUD_RATES.length).boxed()
            .collect(Collectors.toMap(index -> BAUD_RATES[index], Function.identity()));

    private static final Map<CashOperation, Map<CheckType, Byte>> DOC_TYPES = ImmutableMap.of(
            CashOperation.INCOME, ImmutableMap.of(
                    CheckType.SALE, (byte) 0x01,
                    CheckType.RETURN, (byte) 0x02
            ),
            CashOperation.EXPENSE, ImmutableMap.of(
                    CheckType.SALE, (byte) 0x03,
                    CheckType.RETURN, (byte) 0x04
            )
    );

    private static final String MODULE_NAME = "FISCAL_PRINTER";

    /**
     * Параметр для определения источника получения шрифтов для фискальника
     */
    private static final String PROPERTY_PRINT_WITH_TEMPLATE_FONTS = "print.with.template.fonts";

    private ShtrihConfiguration config;

    @Autowired
    private Properties properties;

    @Autowired
    private PropertiesManager propertiesManager;

    @Override
    public Class<ShtrihConfiguration> getConfigClass() {
        return ShtrihConfiguration.class;
    }

    @Override
    public void setConfig(ShtrihConfiguration config) {
        this.config = config;
        super.setConfig(config);
    }

    @Override
    public void start() throws FiscalPrinterException {
        LOG.trace("entering start()");

        config.setPrintLegalEntityHeader(properties.isPrintLegalEntityHeader());
        separator = getSeparator();

        if (config.getJposName() == null) {
            FiscalPrinterConfigException e = new FiscalPrinterConfigException(
                    ResBundleFiscalPrinterShtrihMiniFrk.getString("ERROR_CONFIG"),
                    CashErrorType.FATAL_ERROR
            );
            LOG.error("start() failure", e);
            throw e;
        }

        tryToConnectDirectly();

        ShtrihDriverConnector driverConnector = new ShtrihDriverConnector(config);
        driverConnector.setUseFontsFromTemplate(propertiesManager.getBooleanProperty(MODULE_NAME, null, PROPERTY_PRINT_WITH_TEMPLATE_FONTS, false));
        ShtrihStateDescription deviceState = null;
        ShtrihDeviceType deviceType = null;
        connector = driverConnector;
        try {
            connector.open();
            // 1. получить состояние устройства
            deviceState = driverConnector.getState();
            // 2. сохраняем регистационные данные ККТ
            regDataStorage = new ShtrihDataStorage();
            regDataStorage.setDataFromState(deviceState);

            // 3. получить тип устройства 0xFC
            deviceType = connector.getDeviceType();
        } catch (Exception e) {
            logExceptionAndThrowIt("start()", e);
        }
        taxes = getTaxes();
        try {
            connector.updateVersionFFD();
        } catch (ShtrihException e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("FAILED_READ_FFD_VERSION"), CashErrorType.FATAL_ERROR, e);
        }
        LOG.trace("leaving start(). Device type is: {}; and the device state is: {}", deviceType, deviceState);
    }

    /**
     * Костыль для оживления драйвера с некоторыми Штрихами и платами расширения (без этого на старте системы не удается подключитсья к ККТ)
     */
    private void tryToConnectDirectly() {
        if (!config.isComConnection() || !config.isTestSerialPortUsingDirectCommand()) {
            return;
        }
        try (ShtrihTransport temporaryTransport = new ShtrihTransport()) {
            LOG.debug("Try to test direct connection...");
            temporaryTransport.setByteWaitTime(400);
            if (SystemUtils.IS_OS_WINDOWS) {
                temporaryTransport.setPortName(config.getSerialPort());
            } else {
                temporaryTransport.setPortName("/dev/" + config.getSerialPort());
            }
            final int serialPortBaudRate = config.getSerialPortBaudRate();
            temporaryTransport.setBaudRate(serialPortBaudRate);
            temporaryTransport.open();
            LOG.debug("Direct connection established!");
            final int password = 30;
            final byte[] result = temporaryTransport.execute(new SetExchangeParamCommand(password,
                    BAUD_RATE_INDEXES_BY_RATE.get(serialPortBaudRate)).getCommandAsByteArray(), 1000L);
            LOG.debug("Baud rate is set {}", Arrays.toString(result));
        } catch (Exception e) {
            LOG.debug("Failed to connect.", e);
        }
    }

    /**
     * Метод запрашивает тип устройства
     *
     * @throws FiscalPrinterException - если устройство не семейства ШТРИХ
     */
    @Override
    public boolean verifyDevice() throws FiscalPrinterException {
        LOG.debug("entering verifyDevice()");
        String actual = getHardwareName();
        LOG.debug("verifyDevice(): actual device name: {}", actual);
        LOG.debug("leaving verifyDevice(). The result is: true");
        return true;
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        LOG.debug("entering setRequisites(List). The argument is: requisites [{}]", requisites);
        if (config.isHeaderFromCash()) {
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
        } else {
            LOG.debug("skip setRequisites(List) setHeaderFromCash = {}", config.isHeaderFromCash());
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
                // Значения нд хранятся по разному в ФР
                float value;
                if (entry.getValue() > 100) {
                    value = (float) 1.0 * entry.getValue() / 100;
                } else {
                    value = (float) 1.0 * entry.getValue();
                }
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
    public synchronized Optional<IncrescentTotal> getIncTotal() throws FiscalPrinterException {
        try {
            IncrescentTotal result = new IncrescentTotal();
            // Нарастающие итоги Штрих актуализирует только после печати Z
            ShiftNonNullableCounters nullableCounters = connector.getShiftNonNullableCounters();
            result.setSale(nullableCounters.getSumNonNullableSales());
            result.setReturn(nullableCounters.getSumNonNullableReturnSales());
            result.setExpense(nullableCounters.getSumNonNullablePurchases());
            result.setReturnExpense(nullableCounters.getSumNonNullableReturnPurchases());
            // Поэтому мы дополняем их сменными итогами (для случая, когда смена еще не закрыта)
            final ShtrihShiftCounters shiftCounters = connector.getShiftCounters();
            result.addSale(shiftCounters.getSumSale());
            result.addExpense(shiftCounters.getSumExpense());
            result.addReturn(shiftCounters.getSumReturn());
            result.addReturnExpense(shiftCounters.getSumReturnExpense());
            return Optional.of(result);
        } catch (Exception e) {
            logExceptionAndThrowIt("getIncTotal()", e);
            return Optional.empty();
        }
    }

    private ShtrihOperation makeOperation(Goods goods) throws FiscalPrinterException {
        // добавлены слеши к наименованию позиции для отмены печати позиций внутренним шаблоном ФР, залипуха штриха.
        String namePos = "//" + goods.getName();
        int maxCharRow;
        maxCharRow = getMaxCharRow();
        byte department = goods.getDepartNumber() != null ? goods.getDepartNumber().byteValue() : 0;
        ShtrihOperation shtrihOperation = new ShtrihOperation(goods, namePos.length() < maxCharRow ? namePos : namePos.substring(0, maxCharRow),
                goods.getEndPricePerUnit(), goods.getQuant(), department);

        shtrihOperation.setQuantity(goods.getQuant()); //драйвер штриха принимает по 1 шт
        shtrihOperation.setTaxOne(getTaxId(goods.getTax()));
        // предмет расчета - товар
        shtrihOperation.setPaymentItemSing(formTag1212(goods));
        // признак способа расчета
        shtrihOperation.setPaymentTypeSing(goods.getCalculationMethod().byteValue());
        return shtrihOperation;
    }

    private byte formTag1212(Goods goods) {
        return goods.getCalculationSubject() != null ? goods.getCalculationSubject().byteValue() : 0x01;
    }

    /**
     * КТН передается в виде xxxxYYYYserial
     * где x - код товара, y - GTIN переведенный в hex, SERIAL - серийный номер
     *
     * @param good товарная позиция
     * @return ShtrihItemCode с данными КТН
     */
    @Override
    protected ShtrihItemCode getCodeMark(Goods good) {
        String markCodeStr = good.getMarkCode();
        long gtin = Long.parseLong(good.getMarkEan());
        String serialData = good.getSerialNumber();
        if (good.getMarkMrp() != null) {
            serialData += good.getMarkMrp();
        }
        return new ShtrihItemCode(markCodeStr, gtin, serialData);
    }

    @Override
    public void printLogo() {
        // Не печатаем логотип по секции logo через драйвер - вместо этого печатаем препринт в конце документа
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        LOG.debug("entering printCheck(Check)");

        // 1. как обычно записываем кассира и номер отдела в фискальник перед фискализацией документа:
        setCashierName(getCashierName(check.getCashier()));
        setDepartNumber(check.getDepart().intValue());

        // 2. печать заголовка
        printSubHeader(check.getCashNumber(), check.getCheckNumber(), check.getShiftId(), check.getCashier());
        printLine(new FontLine(getSeparator(), Font.NORMAL));

        // 3. Печать товаров со скидками и налогами, если они есть
        printGoods(check);

        // for product
        printLine(new FontLine(getSeparator(), Font.NORMAL));

        long checkDiscValueTotal;
        if (getDiscValue(check.getDiscs()) != 0) {
            checkDiscValueTotal = getDiscValue(check.getDiscs());
            if (!isOFDDevice()) {
                ShtrihDiscount discount = new ShtrihDiscount(checkDiscValueTotal, ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TITLE"));
                regDiscount(discount);
            }
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
            line = DocumentUtils.makeHeader(ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT_TEXT"), getMaxCharRow(), SPACE_SYMBOL);
            printLine(new FontLine(line, Font.NORMAL));

            line = ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT") +
                    formatRight(BigDecimal.valueOf(checkDiscValueTotal / 100.0).setScale(2, RoundingMode.HALF_UP).toString(),
                            getMaxCharRow() / 2 - ResBundleFiscalPrinterShtrihMiniFrk.getString("DISCOUNT").length());
            printLine(new FontLine(line, Font.DOUBLEWIDTH));
        }

        // Пропуск строки
        printLine(new FontLine(SPACE, Font.NORMAL));

        if (check.isCopy() || check.getPrintDocumentSettings().isNeedPrintBarcode()) {
            printCheckBarcode(check);
        }

        if (check.getClientRequisites() != null) {
            try {
                getConnector().setClientData(check.getClientRequisites());
            } catch (IOException | PortAdapterException | ShtrihException e) {
                LOG.error("Error sent client data: {}", e.getMessage());
            }
        }

        if (check.isAnnul()) {
            annulDocument();
        } else {
            Map<Integer, Long> payments = getPayments(check);

            // закрыть/зарегистрировать чек
            ShtrihReceiptTotalV2Ex receiptTotal = makeReceiptTotalV2(check, payments);

            try {
                getConnector().closeReceiptV2Ex(receiptTotal);
            } catch (ShtrihException | IOException | PortAdapterException e) {
                logExceptionAndThrowIt("closeReceiptV2Ex(ShtrihReceiptTotal)", e);
            }

            // Осталось распечатать шапку следующего документа и отрезать чековую ленту
            closeNonFiscalDocument(check);
        }
        LOG.debug("leaving printCheck(Check)");
    }

    // Печать товаров со скидками и налогами, если они есть
    private void printGoods(Check check) throws FiscalPrinterException {
        for (Goods product : check.getGoods()) {
            // 3.1. регистрация позиции
            String positionName = formatLeft(product.getItem(), ITEM_PLACE) + formatLeft(product.getName(), getMaxCharRow() - ITEM_PLACE);
            positionName = StringUtils.left(positionName, getMaxCharRow());
            ShtrihOperation operation = makeOperation(product);
            operation.setStringForPrinting(positionName);

            ShtrihDiscount shtrihDiscount = makeDiscount(product);
            if (shtrihDiscount.getSum() != 0) {
                // если округление не совпало точно, то укажем сумму
                operation.setSumm(product.getEndPositionPrice());
            }

            // Проставим тип чека. При нарушении маппинга будет NPE
            operation.setCheckType(DOC_TYPES.get(check.getOperation()).get(check.getType()));

            try {
                if (StringUtils.isNotEmpty(product.getExcise())) {
                    putCodingMark(product);
                    operation.setAddItemCode(true);
                } else {
                    operation.setAddItemCode(false);
                }
                getConnector().regOperation(operation);
            } catch (IOException | PortAdapterException | ShtrihException e) {
                logExceptionAndThrowIt("Operation", e);
            }
        }
    }

    @Override
    protected void putGoods(Check check) throws PortAdapterException, ShtrihException, IOException, FiscalPrinterException {
        for (Goods pos : check.getGoods()) {
            ShtrihOperation operation = makeOperation(pos);

            // Проставим тип чека. При нарушении маппинга будет NPE
            operation.setCheckType(DOC_TYPES.get(check.getOperation()).get(check.getType()));

            ShtrihDiscount shtrihDiscount = makeDiscount(pos);
            if (shtrihDiscount.getSum() != 0) {
                // если округление не совпало точно, то укажем сумму
                operation.setSumm(pos.getEndPositionPrice());
            }
            if (StringUtils.isNotEmpty(pos.getExcise())) {
                putCodingMark(pos);
                operation.setAddItemCode(true);
            } else {
                operation.setAddItemCode(false);
            }
            getConnector().regOperation(operation);
        }
    }

    @Override
    public String getEklzNum() {
        LOG.trace("entering getEklzNum() ");
        // SRTZ-811 не кешируем номер ФН, т.к. забывают перезапускать кассу при замене ФН
        String fnNumber = null;
        try {
            // Старый плагин возвращал 16 символов (дополнял пробелами).
            // В базе хранится как строка поэтому надо тоже дополнять
            fnNumber = String.format("%-" + 16 + "s", getConnector().getFNNumber());
        } catch (IOException | PortAdapterException | ShtrihException e) {
            LOG.error(e.getMessage());
        }
        LOG.trace("leaving getEklzNum(). The result is: {}", fnNumber);
        return fnNumber;
    }

    @Override
    public FiscalPrinterInfo getFiscalPrinterInfo() throws FiscalPrinterException {
        try {
            return connector.getFiscalPrinterInfo();
        } catch (Exception e) {
            logExceptionAndThrowIt("getFiscalPrinterInfo", e);
            return null;
        }
    }

    @Override
    public Optional<FiscalMarkValidationResult> validateMarkCode(PositionEntity position, MarkData markData, boolean isSale) throws FiscalPrinterException {
        try {
            return Optional.ofNullable(connector.validateMarkCode(position, markData, isSale));
        } catch (Exception e) {
            logExceptionAndThrowIt("validateMarkCode", e);
            return Optional.empty();
        }
    }
}
