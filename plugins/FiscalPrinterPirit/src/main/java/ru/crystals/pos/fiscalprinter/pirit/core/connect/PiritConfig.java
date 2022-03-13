package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Класс для работы с таблицей настроек ККТ
 */
public class PiritConfig {
    private static final Logger log = LoggerFactory.getLogger(PiritConfig.class);
    private static final int MAX_CASH_NUMBER = 9999;
    private static final int MAX_REQUISITES_COUNT = 4;
    private static final int MAX_SHOP_NAME_STRING_COUNT = 2;
    private static final int MAX_SHOP_ADDRESS_STRING_COUNT = 2;
    private static final int MAX_TAX_COUNT = 6;
    private static final int MAX_PAYMENTS_COUNT = 16;
    private static final int MAX_PAYMENT_NAME_LENGTH = 18;
    private static final int MAX_TAX_NAME_LENGTH = 18;
    /**
     * Мы искусственно ограничиваем максимальное значение, чтобы не передать в Пирит очень большое значение,
     * которое будет его вешать на обмена с ОИСМ при отсутствии связи
     */
    public static final int MAX_OISM_TIMEOUT_SEC = 10;

    private static final int MAX_REQUISITES_LENGTH_WIDE = 44;
    private static final int MAX_REQUISITES_LENGTH_SLIM = 40;
    private static final int MAX_CHAR_COUNT_IN_PRINTING_COMMAND_PIRITK = 72;
    private static final int MAX_CHAR_COUNT_IN_PRINTING_COMMAND_PIRIT = 56;
    private static final String NO_TAX = "БЕЗ НДС";
    //Чтобы не потерять эти настройки после восстановления от ошибок, а они задаются только в методе verifyDevice на старте кассы данные поля пока static
    private int maxCharCountInPrintCommand = MAX_CHAR_COUNT_IN_PRINTING_COMMAND_PIRIT;
    private int maxRequisitesLength = MAX_REQUISITES_LENGTH_SLIM;
    private PiritConnector pc;
    private boolean piritK = false;
    private boolean useWidePaper = true;

    /**
     * Установить PiritConnector
     *
     * @param pc Pirit connector
     */
    public void setConnector(PiritConnector pc) {
        this.pc = pc;
    }

    /**
     * Установить логический номер кассы
     *
     * @param cashNumber номер кассы от 1 до 9999
     */
    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        log.debug("Setting cash number to {}", cashNumber);
        final int toSet = (int) Math.min(Math.abs(cashNumber), MAX_CASH_NUMBER);
        final int currentCashNumber = getCashNumber();
        if (currentCashNumber == toSet) {
            return;
        }
        setIntValueTableSettings(PiritConfigParameter.CASH_NUMBER, toSet);
    }

    public int getCashNumber() throws FiscalPrinterException {
        return getIntValueTableSettings(PiritConfigParameter.CASH_NUMBER);
    }

    public void setOismTimeout(int timeoutSec) {
        if (timeoutSec <= 0 || timeoutSec > MAX_OISM_TIMEOUT_SEC) {
            log.warn("Invalid OISM timeout value: {} (expected to be in range 0..{} sec)", timeoutSec, MAX_OISM_TIMEOUT_SEC);
            return;
        }
        try {
            final int currentOismTimeout = getOismTimeout();
            if (currentOismTimeout == timeoutSec) {
                return;
            }
            log.debug("Setting OISM timeout to {} (will be activated on shift opening)", timeoutSec);
            setIntValueTableSettings(PiritConfigParameter.OISM_TIMEOUT, timeoutSec);
        } catch (FiscalPrinterException e) {
            log.warn("Unable to set OISM timeout value: {}", timeoutSec);
        }
    }

    public int getOismTimeout() throws FiscalPrinterException {
        return getIntValueTableSettings(PiritConfigParameter.OISM_TIMEOUT);
    }

    /**
     * Получить установленный номер дизайна (биты с 0 по 4)
     */
    public int getDesignNumber() throws FiscalPrinterException {
        final int value = getIntValueTableSettings(PiritConfigParameter.FULL_CHECK_PARAMS);
        return value & 0b0001_1111;
    }

    /**
     * Установить признак необходимости печати логотипа
     *
     * @param value true - печатать лого, false - не печатать лого
     */
    public void setPrintLogo(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.PRINT_LOGO, value);
    }

    /**
     * Установить признак отключения печати документов на чековой ленте
     *
     * @param value true - не печатать документы, false - печатать документы
     */
    public void setDisablePrinting(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.DISABLE_PRINTING, value);
    }

    /**
     * Получить признак необходимости контроля наличных со стороны ФР при изъятии
     *
     * @return true - Пирит контролирует сумму изъятия, false - не контролирует сумму изъятия (позволяет изымать больше, чем есть в кассе)
     */
    public boolean isCashDrawerMoneyControl() throws Exception {
        return makeInvertedValue(getBitParameter(PiritConfigParameter.CASH_DRAWER_MONEY_CONTROL));
    }

    /**
     * Установить признак необходимости контроля наличных со стороны ФР при изъятии
     *
     * @param useCashControl true - Пирит контролирует сумму изъятия, false - не контролирует сумму изъятия (позволяет изымать больше, чем есть в
     *                       кассе)
     */
    public void setUseCashDrawerMoneyControl(boolean useCashControl) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.CASH_DRAWER_MONEY_CONTROL, makeInvertedValue(useCashControl));
    }

    /**
     * Установить признак необходимости открытия денежного ящика по команде от кассы
     *
     * @param openByCash true - денежный ящик открывается по команде кассы, false - Пирит сам решает, когда открывать ящик
     */
    public void setOpenCashDrawerByCash(boolean openByCash) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.CASH_DRAWER_OPEN_BY, makeInvertedValue(openByCash));
    }

    /**
     * Установить признак необходимости нумерации документов в Пирите кассой
     *
     * @param checkNumerationByCash true - нумерация чеков осуществляется кассой, false - Пирит сам нумерует чеки
     */
    public void setCheckNumerationByCash(boolean checkNumerationByCash) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.CHECK_NUMERATION_BY_EXTERNAL_APP, checkNumerationByCash);
    }

    /**
     * Установить признак необходимости округления налогов только после ввода всех позиций и скидок
     *
     * @param value true - налоги округляются только после ввода всех позиций и скидок, false - налоги округляются в момент добавления каждого товара
     */
    public void setRoundTaxesAfterAllPositionsAndDiscounts(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.ROUND_TAXES_AFTER_ALL_POSITIONS_AND_DISCOUNTS, value);
    }

    /**
     * Установить признак необходимости автоматчиеской инкассация
     *
     * @param value true - Автоматическая инкассация включена.
     *              При этом перед печатью Z-отчета печатается чек инкассации на всю сумму наличных в денежном ящике
     *              , false - Автоматическая инкассация выключена(*)
     */
    public void setAutoWithdrawal(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.AUTO_WITHDRAWAL, value);
    }

    /**
     * Установить признак необходимости печати вертикальных полос на сервисных документах
     *
     * @param value true - печатать полосы на сервисных документах, false - не печатать
     */
    public void setPrintVerticalBarsOnServiceDoc(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.PRINT_VERTICAL_BARS_ON_SERVICE_DOC, value);
    }

    /**
     * Установить признак необходимости учета чеков, аннулированных при включении питания
     *
     * @param value true - Учитывать чеки, аннулированные при включении питания(*), false - Не учитывать чеки, аннулированные при включении питания
     */
    public void setTakeIntoAccountDocumentsCancelledOnRestart(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.TAKE_INTO_ACCOUNT_DOCUMENTS_CANCELED_ON_RESTART, makeInvertedValue(value));
    }

    /**
     * Установить признак необходимости учета чеков, аннулированных при включении питания
     *
     * @param value true - Нормальный режим печати, false - Печать с уменьшенным межстрочным интервалом, для экономии бумаги(*)
     */
    public void setUseSmallerLineHeightPrintMode(boolean value) throws FiscalPrinterException {
        setParameter(PiritConfigParameter.USE_SMALLER_LINE_HEIGHT_PRINT_MODE, value);
    }

    public void setBelarusRoundToValue(int value) throws FiscalPrinterException {
        setIntValueTableSettings(PiritConfigParameter.RB_ROUND_VALUE, value);
    }

    public void setTaxes(ValueAddedTaxCollection taxes) throws FiscalPrinterException {
        log.debug("Setting taxes");
        int i = 0;
        String taxName;
        String taxValue;
        for (ValueAddedTax tax : taxes) {
            if (StringUtils.isNotEmpty(tax.name) && i < MAX_TAX_COUNT) {
                taxName = StringUtils.left(tax.name, MAX_TAX_NAME_LENGTH);
                taxValue = Float.toString(tax.value);
                log.info("Setting taxes: {} = {}", taxName, taxValue);
                setStrValueTableSettings(PiritConfigParameter.TAX_NAMES, i, taxName);
                setStrValueTableSettings(PiritConfigParameter.TAX_PERCENTS, i++, taxValue);
            }
        }

        while (i < MAX_TAX_COUNT) {
            setStrValueTableSettings(PiritConfigParameter.TAX_NAMES, i, "");
            setStrValueTableSettings(PiritConfigParameter.TAX_PERCENTS, i++, "");
        }
    }

    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        log.info("Setting requisites: {}", requisites);
        int i = 0;

        for (int lineCount = 0; lineCount < MAX_SHOP_NAME_STRING_COUNT; lineCount++) {
            if (requisites.getOrDefault(RequisiteType.SHOP_NAME, new ArrayList<>()).isEmpty() ||
                    lineCount >= requisites.getOrDefault(RequisiteType.SHOP_NAME, new ArrayList<>()).size()) {
                setStrValueTableSettings(PiritConfigParameter.REQUISITES, i++, "");
            } else {
                setStrValueTableSettings(PiritConfigParameter.REQUISITES, i++,
                        StringUtils.center(StringUtils.substring(requisites.get(RequisiteType.SHOP_NAME).get(lineCount), 0, maxRequisitesLength), maxRequisitesLength));
            }
        }
        for (int lineCount = 0; lineCount < MAX_SHOP_ADDRESS_STRING_COUNT; lineCount++) {
            if (requisites.getOrDefault(RequisiteType.SHOP_ADDRESS, new ArrayList<>()).isEmpty() ||
                    lineCount >= requisites.getOrDefault(RequisiteType.SHOP_ADDRESS, new ArrayList<>()).size()) {
                setStrValueTableSettings(PiritConfigParameter.REQUISITES, i++, "");
            } else {
                setStrValueTableSettings(PiritConfigParameter.REQUISITES, i++,
                        StringUtils.center(StringUtils.substring(requisites.get(RequisiteType.SHOP_ADDRESS).get(lineCount), 0, maxRequisitesLength),
                                maxRequisitesLength));
            }
        }
    }

    /**
     * Загружает типы оплат в соответствии с ФФД 1.00
     */
    public void setPaymentsFFD100(List<PaymentType> payments) throws FiscalPrinterException {
        log.debug("Setting payments FFD 1.00");
        for (byte i = 0; i < MAX_PAYMENTS_COUNT; i++) {
            String paymentName = "";
            for (PaymentType payment : payments) {
                if (i == payment.getIndexPaymentFFD100()) {
                    paymentName = payment.getNameFFD100();
                    break;
                }
            }
            log.info("Setting payment with index {} to name = {}", i, paymentName);
            setStrValueTableSettings(PiritConfigParameter.PAYMENT_NAMES, i, StringUtils.left(paymentName, MAX_PAYMENT_NAME_LENGTH));
        }
    }

    public void setPayments(Collection<PaymentType> payments) throws FiscalPrinterException {
        log.debug("Setting payments");
        String paymentName;
        for (byte i = 0; i < MAX_PAYMENTS_COUNT; i++) {
            boolean find = false;
            for (PaymentType payment : payments) {
                if (i == payment.getIndexPayment()) {
                    paymentName = StringUtils.left(payment.getName(), MAX_PAYMENT_NAME_LENGTH);
                    log.info("Setting payment with index {} to name = {}", i, paymentName);
                    setStrValueTableSettings(PiritConfigParameter.PAYMENT_NAMES, i, paymentName);
                    find = true;
                    break;
                }
            }
            if (!find) {
                setStrValueTableSettings(PiritConfigParameter.PAYMENT_NAMES, i, "");
            }
        }
    }

    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        log.debug("Getting taxes");
        List<ValueAddedTax> taxesList = new ArrayList<>();
        for (byte i = 0; i < MAX_TAX_COUNT; i++) {
            String name = getStrValueTableSettings(PiritConfigParameter.TAX_NAMES, i);
            if (StringUtils.isEmpty(name)) {
                name = NO_TAX;
            }
            float value = Float.parseFloat(getStrValueTableSettings(PiritConfigParameter.TAX_PERCENTS, i));
            taxesList.add(new ValueAddedTax(i, value, name));
        }
        return new ValueAddedTaxCollection(taxesList);
    }

    public List<String> getRequisites() throws FiscalPrinterException {
        log.debug("Getting requisites");
        List<String> requisites = new ArrayList<>();
        for (byte i = 0; i < MAX_REQUISITES_COUNT; i++) {
            String str = getStrValueTableSettings(PiritConfigParameter.REQUISITES, i);
            if (StringUtils.isNotEmpty(str)) {
                requisites.add(str);
            }
        }
        return requisites;
    }

    public List<PaymentType> getPayments() throws FiscalPrinterException {
        log.debug("Getting payments");
        List<PaymentType> payments = new ArrayList<>();
        for (byte i = 0; i < MAX_PAYMENTS_COUNT; i++) {
            String str = getStrValueTableSettings(PiritConfigParameter.PAYMENT_NAMES, i);
            if (StringUtils.isNotEmpty(str)) {
                PaymentType pt = new PaymentType();
                pt.setIndexPayment(i);
                pt.setName(str);
                payments.add(pt);
            }
        }
        return payments;
    }

    /**
     * Получить максимальное количество символов для заданного атрибута шрифта (номер шрифта с атрибутами печати)
     */
    public Optional<Integer> getMaxCharCount(int fontAttribute) {
        try {
            final DataPacket dp = new DataPacket();
            dp.putIntValue(fontAttribute);
            final DataPacket result = pc.sendRequest(ExtendedCommand.GET_MAX_CHAR_FOR_FONT, dp);
            return result.getIntegerSafe(1);
        } catch (FiscalPrinterException e) {
            log.debug("Error on read max char count from device", e);
            return Optional.empty();
        }
    }

    /**
     * Получить значение типа {@code int} из конфигурационной таблицы ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @return значение элемента  {@code int}
     */
    private int getIntValueTableSettings(int tableNumber, int index) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putIntValue(tableNumber);
        dp.putIntValue(index);

        return pc.sendRequest(PiritCommand.GET_CONFIGURATION_TABLE, dp)
                .getIntegerSafe(0)
                .orElseGet(() -> {
                    log.error("Unable to parse value for table {} field {}", tableNumber, index);
                    return 0;
                });
    }

    private int getIntValueTableSettings(PiritConfigParameter param) throws FiscalPrinterException {
        return getIntValueTableSettings(param.getTableNumber(), param.getIndex());
    }

    private void setIntValueTableSettings(PiritConfigParameter param, int value) throws FiscalPrinterException {
        setIntValueTableSettings(param.getTableNumber(), param.getIndex(), value);
    }

    /**
     * Записать значение типа {@code int} в конфигурационную таблицу ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @param value       - записываемое значение {@code int}
     */
    private void setIntValueTableSettings(int tableNumber, int index, int value) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putIntValue(tableNumber);
        dp.putIntValue(index);
        dp.putIntValue(value);

        pc.sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, dp);
    }

    /**
     * Получить значение типа {@link String} из конфигурационной таблицы ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @return значение элемента {@link String}
     */
    private String getStrValueTableSettings(int tableNumber, int index) throws FiscalPrinterException {
        log.debug("Getting table {} index {} value...", tableNumber, index);
        String result = "";
        DataPacket dp = new DataPacket();
        dp.putIntValue(tableNumber);
        dp.putIntValue(index);

        dp = pc.sendRequest(PiritCommand.GET_CONFIGURATION_TABLE, dp);
        if (dp.getCountValue() > 0) {
            try {
                result = dp.getStringValue(0);
                log.debug("Table {} index {} value: {}", tableNumber, index, result);
            } catch (Exception e) {
                log.error("Unable to parse value for table {} index {}", tableNumber, index, e);
            }
        }

        return result;
    }

    private String getStrValueTableSettings(PiritConfigParameter param, int index) throws FiscalPrinterException {
        return getStrValueTableSettings(param.getTableNumber(), index);
    }

    /**
     * Записать значение типа {@link String} в конфигурационную таблицу ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @param value       - записываемое значение {@link String}
     */
    private void setStrValueTableSettings(int tableNumber, int index, String value) throws FiscalPrinterException {
        log.info("Setting table {} index {}: {}", tableNumber, index, value);
        DataPacket dp = new DataPacket();
        dp.putIntValue(tableNumber);
        dp.putIntValue(index);
        dp.putStringValue(value);

        pc.sendRequest(PiritCommand.SET_CONFIGURATION_TABLE, dp);
    }

    private void setStrValueTableSettings(PiritConfigParameter param, int index, String value) throws FiscalPrinterException {
        setStrValueTableSettings(param.getTableNumber(), index, value);
    }

    private boolean getBitParameter(PiritConfigParameter param) throws FiscalPrinterException {
        log.debug("Getting parameter {} value...", param.name());
        boolean result = ByteUtils.hasBit(getIntValueTableSettings(param.getTableNumber(), param.getIndex()), param.getBitNum());
        log.debug("Parameter {} value: {}", param.name(), result);
        return result;
    }

    private void setParameter(PiritConfigParameter param, boolean value) throws FiscalPrinterException {
        log.debug("Checking parameter {} is {}", param.name(), value);
        int fieldValue = getIntValueTableSettings(param.getTableNumber(), param.getIndex());
        if (ByteUtils.hasBit(fieldValue, param.getBitNum()) != value) {
            log.info("Setting parameter {} to {}", param.name(), value);
            fieldValue = ByteUtils.setBit(fieldValue, param.getBitNum(), value);
            setIntValueTableSettings(param.getTableNumber(), param.getIndex(), fieldValue);
        }
    }

    /**
     * Часть параметров Пирита имеют инвертированную логику, которая также скрыта в этом классе, чтобы не запутывать основной код
     * (только чтобы на это намекнуть и создан этот метод)
     */
    private boolean makeInvertedValue(boolean value) {
        return !value;
    }

    public boolean isPiritK() {
        return piritK;
    }

    public void setPiritK(boolean piritK) {
        this.piritK = piritK;
        if (piritK) {
            maxCharCountInPrintCommand = MAX_CHAR_COUNT_IN_PRINTING_COMMAND_PIRITK;
            maxRequisitesLength = MAX_REQUISITES_LENGTH_WIDE;
        } else {
            maxCharCountInPrintCommand = MAX_CHAR_COUNT_IN_PRINTING_COMMAND_PIRIT;
            maxRequisitesLength = MAX_REQUISITES_LENGTH_SLIM;
        }
    }

    public int getMaxCharCountInPrintCommand() {
        return maxCharCountInPrintCommand;
    }

    public int getMaxPaymentNameLength() {
        return MAX_PAYMENT_NAME_LENGTH;
    }

    public void useWidePaper(boolean useWidePaper) {
        this.useWidePaper = useWidePaper;
        if (!useWidePaper) {
            maxRequisitesLength = MAX_REQUISITES_LENGTH_SLIM;
        }
    }

    public boolean isUseWidePaper() {
        return useWidePaper;
    }
}
