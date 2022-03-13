package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.mstar.core.MstarUtils;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

/**
 * Класс для работы с таблицей настроек ККТ
 */
public class MstarConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MstarConfig.class);

    private static final long MAX_CASH_NUMBER = 9999;
    private static final int MAX_REQUISITES_COUNT = 4;
    private static final int MAX_SHOP_NAME_STRING_COUNT = 2;
    private static final int MAX_SHOP_ADDRESS_STRING_COUNT = 2;
    private static final int MAX_TAX_COUNT = 6;
    private static final int MAX_PAYMENTS_COUNT = 16;
    private static final int MAX_PAYMENT_NAME_LENGTH = 18;
    private static final int MAX_TAX_NAME_LENGTH = 18;

    private static final int MAX_REQUISITES_LENGTH_SLIM = 40;
    private static final int MAX_CHAR_COUNT_IN_PRINTING_COMMAND = 56;
    private static final int MAX_CHAR_IN_ROW = 64;
    private static final int MIN_CHAR_IN_ROW = 40;

    private MstarConnector mstarConnector;

    /**
     * Установить MstarConnector
     *
     * @param mstarConnector Mstar connector
     */
    public void setConnector(MstarConnector mstarConnector) {
        this.mstarConnector = mstarConnector;
    }

    public long getCashNumber() throws FiscalPrinterException {
        LOG.debug("Getting cash number");
        return Long.parseLong(getStrValueTableSettings(MstarConfigParameter.CASH_NUMBER));
    }

    /**
     * Установить логический номер кассы
     *
     * @param cashNumber номер кассы от 1 до 9999
     */
    public void setCashNumber(long cashNumber) throws FiscalPrinterException {
        LOG.debug("Setting cash number to {}", cashNumber);
        final long toSet = Math.min(Math.abs(cashNumber), MAX_CASH_NUMBER);
        final long currentCashNumber = getCashNumber();
        if (currentCashNumber == toSet) {
            return;
        }
        setStrValueTableSettings(MstarConfigParameter.CASH_NUMBER, Long.toString(toSet));
    }
    /**
     * Поулчить признак необходимости печати логотипа
     *
     * @return true - печатать лого, false - не печатать лого
     */
    public boolean isPrintLogo() throws FiscalPrinterException {
        return getBitParameter(MstarConfigParameter.PRINT_LOGO);
    }

    /**
     * Установить признак необходимости печати логотипа
     *
     * @param value true - печатать лого, false - не печатать лого
     */
    public void setPrintLogo(boolean value) throws FiscalPrinterException {
        setParameter(MstarConfigParameter.PRINT_LOGO, value);
    }

    /**
     * Установить признак сжатия шрифта
     *
     * @param value true - сжать шрифт, false - не сжимать шрифт
     */
    public void setCompressFont(boolean value) throws FiscalPrinterException {
        setParameter(MstarConfigParameter.COMPRESS_FONT, value);
    }

    /**
     * Получить признак необходимости контроля наличных со стороны ФР при изъятии
     *
     * @return true - контролирует сумму изъятия, false - не контролирует сумму изъятия (позволяет изымать больше, чем есть в кассе)
     */
    public boolean isCashDrawerMoneyControl() throws Exception {
        return makeInvertedValue(getBitParameter(MstarConfigParameter.CASH_DRAWER_MONEY_CONTROL));
    }

    /**
     * Установить признак необходимости контроля наличных со стороны ФР при изъятии
     *
     * @param useCashControl true - ФР контролирует сумму изъятия, false - не контролирует сумму изъятия (позволяет изымать больше, чем есть в
     *                       кассе)
     */
    public void setUseCashDrawerMoneyControl(boolean useCashControl) throws FiscalPrinterException {
        setParameter(MstarConfigParameter.CASH_DRAWER_MONEY_CONTROL, makeInvertedValue(useCashControl));
    }

    /**
     * Установить признак необходимости открытия денежного ящика по команде от кассы
     *
     * @param openByCash true - денежный ящик открывается по команде кассы, false - ФР сам решает, когда открывать ящик
     */
    public void setOpenCashDrawerByCash(boolean openByCash) throws FiscalPrinterException {
        setParameter(MstarConfigParameter.CASH_DRAWER_OPEN_BY, makeInvertedValue(openByCash));
    }

    /**
     * Установить признак необходимости автоматчиеской инкассация
     *
     * @param value true - Автоматическая инкассация включена.
     *              При этом перед печатью Z-отчета печатается чек инкассации на всю сумму наличных в денежном ящике
     *              , false - Автоматическая инкассация выключена(*)
     */
    public void setAutoWithdrawal(boolean value) throws FiscalPrinterException {
        setParameter(MstarConfigParameter.AUTO_WITHDRAWAL, value);
    }

    /**
     * Установить количество символов в строке
     *
     * @param charInRow количество символов от 40 до 64
     */
    public void setCharInRow(int charInRow) throws FiscalPrinterException {
        LOG.info("Setting char in row to {}", charInRow);
        setStrValueTableSettings(MstarConfigParameter.CHAR_IN_ROW,
                Integer.toString(Math.min(Math.max(charInRow, MIN_CHAR_IN_ROW), MAX_CHAR_IN_ROW))
        );
    }

    /**
     * Загружает типы оплат в соответствии с ФФД 1.00
     */
    public void setPaymentsFFD100(List<PaymentType> payments) throws FiscalPrinterException {
        LOG.debug("Setting payments FFD 1.00");
        for (byte i = 0; i < MAX_PAYMENTS_COUNT; i++) {
            String paymentName = "";
            long attribute = PaymentTableAttribute.NOT_SPECIFIED.getTableValue();
            for (PaymentType payment : payments) {
                if (i == payment.getIndexPaymentFFD100()) {
                    paymentName = payment.getNameFFD100();
                    attribute = PaymentTableAttribute.getTableByStandard(payment.getIndexPaymentFFD100());
                    break;
                }
            }
            LOG.info("Setting payment with index {} to name = {}", i, paymentName);
            setStrValueTableSettings(MstarConfigParameter.PAYMENT_NAMES, i, StringUtils.left(paymentName, MAX_PAYMENT_NAME_LENGTH));
            setIntValueTableSettings(MstarConfigParameter.PAYMENT_TYPE.getTableNumber(), i, attribute);
        }
    }

    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        LOG.debug("Getting taxes");
        List<ValueAddedTax> taxesList = new ArrayList<>();
        for (byte i = 0; i < MAX_TAX_COUNT; i++) {
            String name = getStrValueTableSettings(MstarConfigParameter.TAX_NAMES, i);
            String taxPercent = StringUtils.leftPad(getStrValueTableSettings(MstarConfigParameter.TAX_PERCENTS, i), 4, "0");
            float value = Float.parseFloat(taxPercent.substring(0, taxPercent.length() - 2) + "." + taxPercent.substring(taxPercent.length() - 2));
            taxesList.add(new ValueAddedTax(i, value, name));
        }
        return new ValueAddedTaxCollection(taxesList);
    }

    public void setTaxes(ValueAddedTaxCollection taxes) throws FiscalPrinterException {
        LOG.debug("Setting taxes");
        String taxName;
        String taxValue;
        for (ValueAddedTax tax : taxes) {
            if (StringUtils.isNotEmpty(tax.name)) {
                taxName = StringUtils.left(tax.name, MAX_TAX_NAME_LENGTH);
                taxValue = String.format("%.2f", tax.value).replaceAll("[,.]", "");
                LOG.info("Setting taxes: {} = {}", taxName, taxValue);
                // сетим налоги в строгом порядке. См. коментарий к enum Taxes
                Taxes taxType = Taxes.findByInternalValue(tax.valueInProduct.keySet());
                setStrValueTableSettings(MstarConfigParameter.TAX_NAMES, taxType.ordinal(), taxName);
                setStrValueTableSettings(MstarConfigParameter.TAX_PERCENTS, taxType.ordinal(), taxValue);
            }
        }
    }

    public List<String> getRequisites() throws FiscalPrinterException {
        LOG.debug("Getting requisites");
        List<String> requisites = new ArrayList<>();
        for (byte i = 0; i < MAX_REQUISITES_COUNT; i++) {
            String str = getStrValueTableSettings(MstarConfigParameter.REQUISITES, i);
            if (StringUtils.isNotEmpty(str)) {
                requisites.add(str);
            }
        }
        return requisites;
    }

    public void setRequisites(Map<RequisiteType, List<String>> requisites) throws FiscalPrinterException {
        LOG.info("Setting requisites: {}", requisites);
        int i = 0;

        for (int lineCount = 0; lineCount < MAX_SHOP_NAME_STRING_COUNT; lineCount++) {
            if (requisites.getOrDefault(RequisiteType.SHOP_NAME, new ArrayList<>()).isEmpty() ||
                    lineCount >= requisites.getOrDefault(RequisiteType.SHOP_NAME, new ArrayList<>()).size()) {
                setStrValueTableSettings(MstarConfigParameter.REQUISITES, i++, "");
            } else {
                setStrValueTableSettings(MstarConfigParameter.REQUISITES, i++, StringUtils.center(StringUtils.substring(requisites.get(RequisiteType.SHOP_NAME).get(lineCount), 0, MAX_REQUISITES_LENGTH_SLIM), MAX_REQUISITES_LENGTH_SLIM));
            }
        }
        for (int lineCount = 0; lineCount < MAX_SHOP_ADDRESS_STRING_COUNT; lineCount++) {
            if (requisites.getOrDefault(RequisiteType.SHOP_ADDRESS, new ArrayList<>()).isEmpty() ||
                    lineCount >= requisites.getOrDefault(RequisiteType.SHOP_ADDRESS, new ArrayList<>()).size()) {
                setStrValueTableSettings(MstarConfigParameter.REQUISITES, i++, "");
            } else {
                setStrValueTableSettings(MstarConfigParameter.REQUISITES, i++, StringUtils.center(StringUtils.substring(requisites.get(RequisiteType.SHOP_ADDRESS).get(lineCount), 0, MAX_REQUISITES_LENGTH_SLIM), MAX_REQUISITES_LENGTH_SLIM));
            }
        }

        while (i < MAX_REQUISITES_COUNT) {
            setStrValueTableSettings(MstarConfigParameter.REQUISITES, i++, "");
        }
    }

    public List<PaymentType> getPayments() throws FiscalPrinterException {
        LOG.debug("Getting payments");
        List<PaymentType> payments = new ArrayList<>();
        for (byte i = 0; i < MAX_PAYMENTS_COUNT; i++) {
            String str = getStrValueTableSettings(MstarConfigParameter.PAYMENT_NAMES, i);
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
     * Получить значение типа {@link long} из конфигурационной таблицы ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @return значение элемента {@link long}
     */
    private long getIntValueTableSettings(long tableNumber, long index) throws FiscalPrinterException {
        long result = 0;
        DataPacket dp = new DataPacket();
        dp.putLongValue(tableNumber);
        dp.putLongValue(index);

        dp = mstarConnector.sendRequest(MstarCommand.GET_CONFIGURATION_TABLE, dp);
        if (dp.getCountValue() > 0) {
            try {
                result = dp.getLongValue(0);
            } catch (Exception e) {
                LOG.error("Unable to parse value for table {} field {}", tableNumber, index, e);
            }
        }

        return result;
    }

    /**
     * Записать значение типа {@link long} в конфигурационную таблицу ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @param value       - записываемое значение {@link long}
     */
    private void setIntValueTableSettings(long tableNumber, long index, long value) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putLongValue(tableNumber);
        dp.putLongValue(index);
        dp.putLongValue(value);

        mstarConnector.sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, dp);
    }

    /**
     * Получить значение типа {@link String} из конфигурационной таблицы ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @return значение элемента {@link String}
     */
    private String getStrValueTableSettings(long tableNumber, long index) throws FiscalPrinterException {
        LOG.debug("Getting table {} index {} value...", tableNumber, index);
        String result = "";
        DataPacket dp = new DataPacket();
        dp.putLongValue(tableNumber);
        dp.putLongValue(index);

        dp = mstarConnector.sendRequest(MstarCommand.GET_CONFIGURATION_TABLE, dp);
        if (dp.getCountValue() > 0) {
            try {
                result = dp.getStringValue(0);
                LOG.debug("Table {} index {} value: {}", tableNumber, index, result);
            } catch (Exception e) {
                LOG.error("Unable to parse value for table {} index {}", tableNumber, index, e);
            }
        }

        return result;
    }

    private String getStrValueTableSettings(MstarConfigParameter param, long index) throws FiscalPrinterException {
        return getStrValueTableSettings(param.getTableNumber(), index);
    }

    private String getStrValueTableSettings(MstarConfigParameter param) throws FiscalPrinterException {
        return getStrValueTableSettings(param, param.getIndex());
    }

    /**
     * Записать значение типа {@link String} в конфигурационную таблицу ФР
     *
     * @param tableNumber - номер таблицы
     * @param index       - индекс элемента (если есть), иначе 0
     * @param value       - записываемое значение {@link String}
     */
    private void setStrValueTableSettings(long tableNumber, long index, String value) throws FiscalPrinterException {
        LOG.info("Setting table {} index {}: {}", tableNumber, index, value);
        DataPacket dp = new DataPacket();
        dp.putLongValue(tableNumber);
        dp.putLongValue(index);
        dp.putStringValue(value);

        mstarConnector.sendRequest(MstarCommand.SET_CONFIGURATION_TABLE, dp);
    }

    private void setStrValueTableSettings(MstarConfigParameter param, long index, String value) throws FiscalPrinterException {
        setStrValueTableSettings(param.getTableNumber(), index, value);
    }

    private void setStrValueTableSettings(MstarConfigParameter param, String value) throws FiscalPrinterException {
        setStrValueTableSettings(param.getTableNumber(), param.getIndex(), value);
    }

    private boolean getBitParameter(MstarConfigParameter param) throws FiscalPrinterException {
        LOG.debug("Getting parameter {} value...", param.name());
        boolean result = MstarUtils.getBit(getIntValueTableSettings(param.getTableNumber(), param.getIndex()), param.getBitNum());
        LOG.debug("Parameter {} value: {}", param.name(), result);
        return result;
    }

    private void setParameter(MstarConfigParameter param, boolean value) throws FiscalPrinterException {
        LOG.debug("Checking parameter {} is {}", param.name(), value);
        long fieldValue = getIntValueTableSettings(param.getTableNumber(), param.getIndex());
        if (MstarUtils.getBit(fieldValue, param.getBitNum()) != value) {
            LOG.info("Setting parameter {} to {}", param.name(), value);
            fieldValue = MstarUtils.setBit(fieldValue, param.getBitNum(), value);
            setIntValueTableSettings(param.getTableNumber(), param.getIndex(), fieldValue);
        }
    }

    /**
     * Часть параметров имеют инвертированную логику, которая также скрыта в этом классе, чтобы не запутывать основной код
     * (только чтобы на это намекнуть и создан этот метод)
     */
    private boolean makeInvertedValue(boolean value) {
        return !value;
    }

    public int getMaxCharCountInPrintCommand() {
        return MAX_CHAR_COUNT_IN_PRINTING_COMMAND;
    }

    public int getMaxPaymentNameLength() {
        return MAX_PAYMENT_NAME_LENGTH;
    }
}
