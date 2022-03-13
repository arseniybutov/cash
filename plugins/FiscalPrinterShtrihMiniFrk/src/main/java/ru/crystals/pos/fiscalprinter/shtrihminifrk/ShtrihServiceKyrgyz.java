package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.ExtraFiscalDocType;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnectorProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDiscount;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihPosition;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihReceiptTotal;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Реализация плагина ФР ШТРИХ для Киргизии
 */
@PrototypedComponent
public class ShtrihServiceKyrgyz extends ShtrihMiniFrkFiscalPrinterServiceImpl implements Configurable<BaseShtrihConfig> {

    /**
     * Для нумерации документов используем СПНД, поэтому учитываем внесения/изъятия и X отчет
     */
    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .fiscalDocTypes(EnumSet.of(ExtraFiscalDocType.MONEY, ExtraFiscalDocType.X))
            .canRefundByCashOnly(true)
            .canReturnFullLastDocInfo(false)
            .build();

    @Override
    public Class<BaseShtrihConfig> getConfigClass() {
        return BaseShtrihConfig.class;
    }

    @Override
    public void setConfig(BaseShtrihConfig config) {
        super.setConfig(config);
    }

    @Override
    protected ShtrihConnectorProperties initConnectorProperties() {
        ShtrihConnectorProperties properties = super.initConnectorProperties();
        properties.setLineSpacingSupported(false);
        properties.setByteWaitTime(500);
        return properties;
    }

    @Override
    public boolean verifyDevice() throws FiscalPrinterException {
        String actual = getHardwareName();
        LOG.debug("verifyDevice(): actual device name: {}", actual);
        return true;
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        try {
            setCashierName(cashier == null ? null : cashier.getName());
            getConnector().openShift(cashier);
            getTaxes();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("openShift(Cashier)", e);
        }
        return getShiftNumber();
    }

    @Override
    public ValueAddedTaxCollection getTaxes() throws FiscalPrinterException {
        if (taxes != null) {
            return taxes;
        }
        taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(0, 12.0f, "НДС12", Collections.singletonMap(12.0f, "НДС12")));
        taxes.addTax(new ValueAddedTax(1, 0.0f, "НДС0", Collections.singletonMap(0.0f, "НДС0")));
        return taxes;
    }

    @Override
    public void setTaxes(ValueAddedTaxCollection taxesCollection) throws FiscalPrinterException {
        //ФР работает без указания налога
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        LOG.trace("entering getINN()");
        if (regDataStorage.isDeviceINNEmpty()) {
            ShtrihStateDescription state = getState();
            regDataStorage.setDataFromState(state);
        }

        String inn = regDataStorage.getDeviceINN();
        if (inn == null) {
            return null;
        }
        //В Киргизии длина ИНН 14 сиволов, обробатываем в плагине, т.к. в Properties заложена обработка только до 12-ти
        inn = Properties.fillInnByZero(inn, 14 - inn.length());
        LOG.trace("leaving getINN(). The result is: {}", inn);
        return inn;
    }

    @Override
    public void printLogo() throws FiscalPrinterException {
        try {
            getConnector().printLogo();
        } catch (IOException | PortAdapterException | ShtrihException e) {
            logExceptionAndThrowIt("printLogo()", e);
        }
    }

    @Override
    public void printCheck(Check check) throws FiscalPrinterException {
        throw new FiscalPrinterException("Unsupported operation");
    }

    @Override
    protected void printCheckByTemplate(List<DocumentSection> sectionList, Check check) throws FiscalPrinterException {
        LOG.debug("entering printCheckByTemplate(List, Check). The arguments are: check [{}]", check);

        setCashierName(check.getCashier().getName());
        setDepartNumber(check.getDepart().intValue());

        Map<Integer, Long> payments = getPayments(check);
        if (payments.get(1) != null && CheckType.RETURN.equals(check.getType())) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("CASHLESS_RETURN_UNSUPPORTED"));
        }

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
                        break;
                    case FiscalPrinterPlugin.SECTION_CUT:
                        break;
                    default:
                        printLinesList(section.getContent());
                        break;
                }
            }

            //Добавляем в ФР одну общую позицию без названия для печати по кассовому шаблону
            addSingleEmptyPosition(check);

            ShtrihReceiptTotal receiptTotal = new ShtrihReceiptTotal();
            receiptTotal.setText("");

            if (payments.get(0) != null) {
                receiptTotal.setCashSum(payments.get(0));
            }
            if (payments.get(1) != null) {
                receiptTotal.setSecondPaymentTypeSum(payments.get(1));
            }

            closeReceipt(receiptTotal);
            closeNonFiscalDocument(check);
        } catch (ShtrihException | IOException | PortAdapterException e) {
            logExceptionAndThrowIt("printReportByTemplate(List, Report)", e);
        }

        LOG.debug("leaving printCheckByTemplate(List, Check)");
    }

    /**
     * Добавление в ФР одной позиции без названия с ценой равной сумме чека
     */
    private void addSingleEmptyPosition(Check check) throws FiscalPrinterException, PortAdapterException, ShtrihException, IOException {
        ShtrihPosition position = new ShtrihPosition(StringUtils.EMPTY, check.getCheckSumEnd(), 1000, check.getDepart().byteValue());
        position.setTaxOne(getTaxId(0));
        if (CheckType.SALE.equals(check.getType())) {
            getConnector().regSale(position);
        } else if (CheckType.RETURN.equals(check.getType())) {
            getConnector().regReturn(position);
        }
    }

    private void putGoods(Check check) throws PortAdapterException, ShtrihException, IOException, FiscalPrinterException {
        for (Goods pos : check.getGoods()) {
            if (CheckType.SALE.equals(check.getType())) {
                getConnector().regSale(makePosition(pos));
            } else if (CheckType.RETURN.equals(check.getType())) {
                getConnector().regReturn(makePosition(pos));
            }
            ShtrihDiscount shtrihDiscount = makeDiscount(pos);
            if (shtrihDiscount.getSum() > 0) {
                getConnector().regDiscount(shtrihDiscount);
            } else if (shtrihDiscount.getSum() < 0) {
                shtrihDiscount.setSum(shtrihDiscount.getSum() * -1);
                getConnector().regMargin(shtrihDiscount);
            }
        }
    }

    /**
     * Формирование позици: Код ТНВЭД (разделитель ;) Штрих код (разделитель 0x0A) Наименование товара
     * Пример: 1234567890;487061000x0AНаименование товара
     */
    private String getKGPositionName(Goods goods) {
        final char separator1 = ';';
        final char separator2 = 0x0A;
        return goods.getItem() != null ? goods.getMarkCode() : "" + separator1 + goods.getBarcode() + separator2 + goods.getName();
    }

    private ShtrihPosition makePosition(Goods goods) throws FiscalPrinterException {
        String namePos = getKGPositionName(goods);
        ShtrihPosition shtrihPosition;
        shtrihPosition = new ShtrihPosition(namePos, goods.getEndPricePerUnit(), goods.getQuant(), goods.getDepartNumber().byteValue());
        shtrihPosition.setTaxOne(getTaxId(goods.getTax()));
        return shtrihPosition;
    }

    ShtrihDiscount makeDiscount(Goods pos) {
        return new ShtrihDiscount(CurrencyUtil.getPositionSum(pos.getEndPricePerUnit(), pos.getQuant()) - pos.getEndPositionPrice(), "");
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        long result;

        LOG.trace("entering getLastKpk()");
        result = getState().getCurrentDocNo();
        LOG.trace("leaving getLastKpk(). The result is: {}", result);

        return result;
    }

    @Override
    protected byte getTaxId(float taxValue) throws FiscalPrinterException {
        //В ФР Киргизии не указывается налоговая группа
        return 0;
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    @Override
    protected String getCashierName(Cashier cashier) {
        return cashier.getName();
    }
}
