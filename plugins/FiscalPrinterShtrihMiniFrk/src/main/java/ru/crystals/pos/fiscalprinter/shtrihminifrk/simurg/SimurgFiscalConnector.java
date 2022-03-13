package ru.crystals.pos.fiscalprinter.shtrihminifrk.simurg;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.ExtraFiscalDocType;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ResBundleFiscalPrinterShtrihMiniFrk;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ShtrihErrorMsg;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihExceptionConvertor;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnector;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnectorFactory;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnectorProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDataStorage;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDeviceType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihModeEnum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShiftCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@PrototypedComponent
public class SimurgFiscalConnector implements FiscalConnector, Configurable<SimurgConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(SimurgFiscalConnector.class);

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .fiscalDocTypes(EnumSet.of(ExtraFiscalDocType.MONEY, ExtraFiscalDocType.X))
            .canReturnFullLastDocInfo(false)
            .build();

    private static final String DEVICE_ID = "simurg";
    //Две налоговые ставки в Узбекистане
    private static final float NDS_0 = 0.0f;
    //У НДС 15% записанном в таблице ФР так значение указанно
    private static final float NDS_15 = 150000.0f;

    /**
     * Список налогов из таблицы ФР, используется для индексации налогов в позиции
     */
    private ValueAddedTaxCollection kktTaxes;
    private SimurgCounterEmulator counterEmulator = new SimurgCounterEmulator();

    /**
     * Реализация протокола обмена с ФР семейства "Штрих"
     */
    protected ShtrihConnector shtrihConnector;
    protected ShtrihDataStorage regDataStorage;

    private SimurgConfig config;

    @Override
    public Class<SimurgConfig> getConfigClass() {
        return SimurgConfig.class;
    }

    @Override
    public void setConfig(SimurgConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        ShtrihConnectorProperties shtrihProperties = new ShtrihConnectorProperties();
        shtrihProperties.setPortName(config.getFiscalPort());
        shtrihProperties.setBaudRate(config.getFiscalBaudRate());
        shtrihProperties.setNeedRevertBytes(false);
        shtrihProperties.setMaxCharsInRow(config.getFiscalMaxCharRow());
        shtrihConnector = ShtrihConnectorFactory.createConnector(DEVICE_ID, shtrihProperties);
        try {
            shtrihConnector.open();
            ShtrihStateDescription deviceState = shtrihConnector.getState();
            ShtrihDeviceType deviceType = shtrihConnector.getDeviceType();

            regDataStorage = new ShtrihDataStorage();
            regDataStorage.setDataFromState(deviceState);

            kktTaxes = getSimurgTaxes();
            if (config.isMockCounters()) {
                counterEmulator.loadState();
            }

            LOG.trace("leaving init(). Device type is: {}; and the device state is: {}", deviceType, deviceState);
        } catch (Exception e) {
            logExceptionAndThrowIt("init()", e);
        }
    }

    @Override
    public void stop() {
        try {
            shtrihConnector.annul();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            LOG.error("stop() ", e);
        }
        shtrihConnector.close();
    }

    @Override
    public void tryRecoverOnError() throws FiscalPrinterException {
        stop();
        start();
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    @Override
    public String getINN() {
        try {
            if (regDataStorage.isDeviceINNEmpty()) {
                ShtrihStateDescription state = getState();
                regDataStorage.setDataFromState(state);
            }
        } catch (FiscalPrinterException ignored) {
            //ошибка залогирована в getState
        }
        return regDataStorage.getDeviceINN();
    }

    @Override
    public String getRegNum() {
        try {
            if (regDataStorage.isRegistrationNumEmpty()) {
                ShtrihStateDescription state = getState();
                regDataStorage.setRegistrationNum("" + state.getDeviceNo());
            }
        } catch (FiscalPrinterException ignored) {
            //ошибка залогирована в getState
        }
        return regDataStorage.getRegistrationNum();
    }

    @Override
    public String getFNNum() {
        return config.isMockCounters() ? "-mock-counters-" : "-no-";
    }

    @Override
    public String getFactoryNum() throws FiscalPrinterException {
        if (regDataStorage.isFactoryNumEmpty()) {
            ShtrihStateDescription state = getState();
            regDataStorage.setDataFromState(state);
        }
        return regDataStorage.getFactoryNum();
    }

    @Override
    public void openShift(Cashier cashier) throws FiscalPrinterException {
        setCashierName(cashier == null ? null : cashier.getName());
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        if (config.isMockCounters()) {
            return counterEmulator.getShiftNum();
        }
        ShtrihStateDescription state = getState();
        long lastClosedShiftNo = state.getLastClosedShiftNo();
        lastClosedShiftNo++;
        return lastClosedShiftNo;
    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
        ShtrihStateDescription state = getState();
        return state.getCurrentDocNo();
    }

    @Override
    public long getCashInCount() throws FiscalPrinterException {
        try {
            return shtrihConnector.getCashInCount();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getCountCashIn()", e);
        }
        return -1;
    }

    @Override
    public long getCashOutCount() throws FiscalPrinterException {
        try {
            return shtrihConnector.getCashOutCount();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getCountCashOut()", e);
        }
        return -1;
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        try {
            return shtrihConnector.getCashAccumulation();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getState()", e);
        }
        return -1;
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {

        ShtrihStateDescription state = getState();
        ShtrihModeEnum stateEnum = state.getMode().getStateNumber();
        return !ShtrihModeEnum.SHIFT_IS_CLOSED.equals(stateEnum);
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        ShtrihShiftCounters counters = null;
        try {
            counters = shtrihConnector.getShiftCounters();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getShiftCounters()", e);
        }

        if (counters == null) {
            throw new RuntimeException("Failed to get counters");
        }
        ShiftCounters result = new ShiftCounters();
        result.setSumCashEnd(counters.getCashSum());
        result.setSumSale(counters.getSumSale());
        result.setSumReturn(counters.getSumReturn());
        result.setCountSale(counters.getCountSale());
        result.setCountReturn(counters.getCountReturn());

        LOG.debug("getShiftCounters(). Result: {}", result);

        return result;
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterShtrihMiniFrk.getString("DEVICE_NAME_SIMURG");
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        setCashierName(check.getCashier().getName());

        try {
            long nds0Price = getNDS0PositionSum(check);
            long nds15Price = check.getCheckSumEnd() - nds0Price;

            // Регистрируем одну позицию по каждому типу налога
            if (nds0Price > 0) {
                addSinglePosition(config.getFiscalPositionNameNoNDS(), nds0Price, getTaxId(NDS_0), check);
            }
            if (nds15Price > 0) {
                addSinglePosition(config.getFiscalPositionName(), nds15Price, getTaxId(NDS_15), check);
            }

            ShtrihReceiptTotal receiptTotal = new ShtrihReceiptTotal();
            Map<Long, Payment> payments = getPaymentsGroupedByIndex(check.getPayments());
            if (payments.get(0L) != null) {
                receiptTotal.setCashSum(payments.get(0L).getSum());
            }
            if (payments.get(1L) != null) {
                receiptTotal.setSecondPaymentTypeSum(payments.get(1L).getSum());
            }
            receiptTotal.setText(StringUtils.EMPTY);
            shtrihConnector.closeReceipt(receiptTotal);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("processReceipt(List, Report)", e);
        }
    }

    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        setCashierName(report.getCashier().getName());

        try {
            if (report.isZReport()) {
                if (!report.isCopy()) {
                    shtrihConnector.printZReport(report.getCashier());
                    if (config.isMockCounters()) {
                        counterEmulator.incShiftNum();
                    }
                }
            } else if (report.isXReport()) {
                shtrihConnector.printXReport();
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        }
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        setCashierName(money.getCashier().getName());

        if (!InventoryOperationType.DECLARATION.equals(money.getOperationType())) {
            long total = money.getValue();
            if (InventoryOperationType.CASH_IN.equals(money.getOperationType())) {
                registerCashIn(total);
            } else {
                registerCashOut(total);
            }
        }
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        return getUZTaxes();
    }

    private ValueAddedTaxCollection getSimurgTaxes() {
        ValueAddedTaxCollection result = new ValueAddedTaxCollection();
        LOG.debug("entering getSimurgTaxes()");
        try {
            // 1. вытащим налоги
            Map<String, Long> taxes = shtrihConnector.getTaxes();
            // 2. а теперь сконвертном в этот формат с Float
            int idx = 0;
            for (Map.Entry<String, Long> entry : taxes.entrySet()) {
                float value = BigDecimal.valueOf(entry.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN).floatValue();
                result.addTax(new ValueAddedTax(idx, value, entry.getKey()));
                idx++;
            }
        } catch (ShtrihException | IOException | PortAdapterException e) {
            LOG.error("getTaxes() error", e);
        }
        LOG.debug("leaving getSimurgTaxes(). The result is: {}", result);
        return result;
    }

    private ValueAddedTaxCollection getUZTaxes() {
        ValueAddedTaxCollection taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(0, 15.0f, "НДС15", Collections.singletonMap(15.0f, "НДС15")));
        taxes.addTax(new ValueAddedTax(1, 0.0f, "НДС0", Collections.singletonMap(0.0f, "НДС0")));
        return taxes;
    }

    private byte getTaxId(float taxValue) throws FiscalPrinterException {
        ValueAddedTax tax = kktTaxes.lookupByValue((taxValue));
        if (tax == null) {
            throw new FiscalPrinterException("ERROR: Tax type not found!");
        }
        // в штрихе нумерация налогов начинается с 1
        return (byte) (tax.index + 1);
    }

    private long getNDS0PositionSum(Check check) {
        long positionSum = 0L;
        for (Goods position : check.getGoods()) {
            positionSum += position.getTax() == NDS_0 ? position.getEndPositionPrice() : 0L;
        }
        return positionSum;
    }

    private void addSinglePosition(String name, long price, byte taxId, Check check) throws IOException, PortAdapterException, ShtrihException {
        ShtrihPosition position = new ShtrihPosition(name, price, 1000, check.getDepart().byteValue());
        position.setTaxOne(taxId);

        if (CheckType.SALE.equals(check.getType())) {
            shtrihConnector.regSale(position);
        } else if (CheckType.RETURN.equals(check.getType())) {
            shtrihConnector.regReturn(position);
        }
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
    private void logExceptionAndThrowIt(String methodName, Exception e) throws FiscalPrinterException {
        LOG.error("{} failure", methodName, e);
        FiscalPrinterException fpe = ShtrihExceptionConvertor.convert(e);
        throw fpe == null ? new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("UNKNOWN_ERROR"), ShtrihErrorMsg.getErrorType()) : fpe;
    }

    /**
     * Вернет состояние фискальника.
     *
     * @return {@link ShtrihStateDescription [полное] состояние фискальника}; не вернет <code>null</code> - в крайнем случае будет выброшен exception
     * @throws FiscalPrinterException при воникновении ошибок инфо-обмена
     */
    private ShtrihStateDescription getState() throws FiscalPrinterException {
        try {
            return shtrihConnector.getState();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getState()", e);
        }
        throw new FiscalPrinterException("Failed to get printer state (null)");
    }

    /**
     * Вернет описание подключенного ФР.
     *
     * @return не <code>null</code>
     */
    private ShtrihDeviceType getDeviceType() throws FiscalPrinterException {
        try {
            return shtrihConnector.getDeviceType();
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("getDeviceType()", e);
        }
        throw new FiscalPrinterException("Failed to get device type");
    }

    /**
     * Вернет оплаты, примененные в указанном чеке, сгруппированные по типам отплат Симуга [0, 1].
     *
     * @param payments список оплаты
     * @return схлопнутые оплаты под ключами 0 - наличные, 1 - безналичные
     */
    public Map<Long, Payment> getPaymentsGroupedByIndex(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return Collections.emptyMap();
        }
        return payments.stream().collect(Collectors.toMap(this::getPaymentIndex, Function.identity(), this::sumPayment));
    }

    private Payment sumPayment(Payment a, Payment b) {
        final Payment payment = new Payment(a);
        payment.setSum(a.getSum() + b.getSum());
        return payment;
    }

    private long getPaymentIndex(Payment payment) {
        return payment.getIndexPayment() == PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex() ? 0 : 1;
    }

    private void setCashierName(String name) throws FiscalPrinterException {
        try {
            //При работе из ПО кассир всегда 30-й
            final byte cashierNo = 30;
            shtrihConnector.setCashierName(cashierNo, name);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("setCashierName(String)", e);
        }
    }

    /**
     * Регисртирует (в ФР) внесение на указанную сумму
     *
     * @param sum сумма внесения, в МДЕ
     */
    private void registerCashIn(long sum) throws FiscalPrinterException {
        try {
            shtrihConnector.regCashIn(sum);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("registerCashIn(long)", e);
        }
    }

    /**
     * Регисртирует (в ФР) изъятие указанной суммы
     *
     * @param sum сумма изъятия, в МДЕ
     */
    private void registerCashOut(long sum) throws FiscalPrinterException {
        try {
            shtrihConnector.regCashOut(sum);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("registerCashOut(long)", e);
        }
    }

}
