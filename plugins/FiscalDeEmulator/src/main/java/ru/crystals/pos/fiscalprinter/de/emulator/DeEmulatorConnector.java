package ru.crystals.pos.fiscalprinter.de.emulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ReportCounters;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.LongExtended;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxInfo;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxes;
import ru.crystals.pos.property.Properties;
import ru.crystals.set10dto.TaxVO;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@PrototypedComponent
public class DeEmulatorConnector implements FiscalConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DeEmulatorConnector.class);

    private final KPKCounters counters = new KPKCounters();
    /**
     * There are five types of taxes in Germany
     */
    private static final String[] TAX_INDEXES = new String[]{"A", "B", "C", "D", "E", "F", "G"};
    private List<TaxVO> taxes;
    private ValueAddedTaxCollection taxesOld;
    private final Properties properties;
    private String deviceSerial;

    @Autowired
    public DeEmulatorConnector(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void start() throws FiscalPrinterException {
        counters.loadState();
        fillTaxes();
        try {
            deviceSerial = getLocalMacAddress();
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleFCC.getString("CONNECTION_FAIL"), e);
        }
    }

    private String getLocalMacAddress() {
        StringBuilder strMac = new StringBuilder();
        try {
            Enumeration<NetworkInterface> ints = NetworkInterface.getNetworkInterfaces();
            NetworkInterface network = null;
            while (ints.hasMoreElements()) {
                network = ints.nextElement();
                if (!network.isLoopback() && network.getHardwareAddress() != null) {
                    break;
                }
            }
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                for (byte i : mac) {
                    strMac.append(Integer.toHexString(i));
                }
            }
        } catch (Exception ex) {
            LOG.error("", ex);
        }
        return strMac.toString();
    }

    private void fillTaxes() {
        taxes = new ArrayList<>(5);
        taxes.add(new TaxVO(1, 1900L, "19%"));
        taxes.add(new TaxVO(2, 700L, "7%"));
        taxes.add(new TaxVO(3, 1070L, "10.7%"));
        taxes.add(new TaxVO(4, 550L, "5.5%"));
        taxes.add(new TaxVO(5, 0L, "0%"));
        taxes.add(new TaxVO(6, 1600L, "16%"));
        taxes.add(new TaxVO(7, 500L, "5%"));
    }

    @Override
    public String getRegNum() {
        return "E." + properties.getShopIndex() + "." + properties.getCashNumber();
    }

    @Override
    public String getFactoryNum() {
        return "1.0.0";
    }

    @Override
    public long getShiftNum() {
        return counters.getShiftNum();
    }

    @Override
    public long getLastFiscalDocId() {
        return counters.getKPK();
    }

    @Override
    public long getCashInCount() {
        return counters.getCashInCount();
    }

    @Override
    public long getCashOutCount() {
        return counters.getCashOutCount();
    }

    @Override
    public long getCashAmount() {
        return counters.getCashAmount();
    }

    @Override
    public boolean isShiftOpen() {
        return counters.isShiftOpen();
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        return counters.getShiftCountersClone();
    }

    @Override
    public String getDeviceName() {
        return "DE";
    }

    @Override
    public void processNonFiscal(AbstractDocument document) throws FiscalPrinterException {
        counters.incSPND();
    }

    @Override
    public void processCopyDocument(FiscalDocument document) throws FiscalPrinterException {
        counters.incSPND();
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        if (!check.isCopy()) {
            long cashPayment = 0;
            long cashlessPayment = 0;
            for (Payment p : check.getPayments()) {
                if ("CashPaymentEntity".equals(p.getPaymentType())) {
                    cashPayment += p.getSum();
                } else {
                    cashlessPayment += p.getSum();
                }
            }
            long change = 0;
            if (cashPayment > 0 && cashPayment + cashlessPayment > check.getCheckSumEnd()) {
                change = -(check.getCheckSumEnd() - (cashPayment + cashlessPayment));
            }

            if (!check.getCheckSumEnd().equals(cashPayment + cashlessPayment - change)) {
                LOG.error("Check sum fail: check sum = " + check.getCheckSumEnd() + ", calc check sum = " + (cashPayment + cashlessPayment - change));
                throw new FiscalPrinterException(ResBundleFCC.getString("PURCHASE_SUM_ERROR"));
            }

            try {
                addCheckTaxes(check);
                updatePurchaseInfo(check);
                if (check.getType() == CheckType.RETURN) {
                    counters.incReturn(cashPayment, cashlessPayment, change);
                } else {
                    counters.incSale(cashPayment, cashlessPayment, change);
                }
                counters.incKPK();
            } catch (Exception ex) {
                throw new FiscalPrinterException(ResBundleFCC.getString("FISCALIZATION_FAIL"), ex);
            }
        }
    }

    protected String addCheckTaxes(Check check) throws FiscalPrinterException {
        StringBuilder result = new StringBuilder();

        Map<String, Long> purchasesTaxes = new LinkedHashMap<>();
        for (TaxVO tax : taxes) {
            purchasesTaxes.put(tax.getCode(), 0L);
        }

        Map<String, PurchaseTaxInfo> purchaseTaxMap = new HashMap<>();
        List<PurchaseTaxInfo> taxesList = new ArrayList<>();
        final AtomicInteger taxIndex = new AtomicInteger(0);

        Long purchaseTax;
        PurchaseTaxInfo purchaseTaxInfo;
        for (Goods pos : check.getGoods()) {
            purchaseTaxInfo = purchaseTaxMap.computeIfAbsent(pos.getTaxName(), p -> {

                // it allows us collect taxes in order by taxIndex
                PurchaseTaxInfo pti = new PurchaseTaxInfo(TAX_INDEXES[taxIndex.getAndIncrement()], pos.getTaxName());
                taxesList.add(pti);
                return pti;
            });
            purchaseTaxInfo.add(pos.getTaxSum(), pos.getEndPositionPrice());
            pos.setTaxIndexName(purchaseTaxInfo.getTaxIndex());

            purchaseTax = purchasesTaxes.get(pos.getTaxName());
            if (purchaseTax == null) {
                LOG.error("Tax not found in tax table: " + pos.getTaxName());
                throw new FiscalPrinterException(ResBundleFCC.getString("ERROR_TAX_VALUE"));
            }
            purchaseTax += pos.getTaxSum();
            purchasesTaxes.put(pos.getTaxName(), purchaseTax);
        }

        PurchaseTaxes purchaseTaxes = new PurchaseTaxes();
        purchaseTaxes.setPurchaseTaxes(taxesList);
        check.getMap().put(PurchaseTaxes.DE_TAXES_FIELD, purchaseTaxes);

        String split = "";
        boolean refund = check.getType() == CheckType.RETURN;
        for (Map.Entry<String, Long> e : purchasesTaxes.entrySet()) {
            result.append(split);
            result.append(refund && e.getValue() > 0 ? "-" : "");
            result.append(CurrencyUtil.convertMoney(e.getValue()));
            split = "_";
        }

        return result.toString();
    }

    private void updatePurchaseInfo(Check check) {
        Map<String, Object> map = check.getMap();
        map.put("cashserialnumber", deviceSerial);
        map.put("timeformat", "unixTime");
    }

    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        try {
            if (report.isZReport()) {
                fiscalizeZReport();
            } else if (report.isXReport()) {
                fiscalizeXReport();
            } else {
                processNonFiscal(report);
            }
            updateReportParams(report);
            counters.getSPND();
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleFCC.getString("FISCALIZATION_FAIL"), e);
        }
    }

    private void updateReportParams(Report report) throws FiscalPrinterException {
        ReportCounters repCounters = report.getReportCounters();
        report.getMap().put("totalcountcheck", new LongExtended(repCounters.getCountSale() + repCounters.getCountReturn()));
        report.getMap().put("taxpayerid", properties.getShopKPP());
        report.getMap().put("shiftfdcount", new LongExtended(counters.getSPND()));
        report.getMap().put("eklz", deviceSerial);
    }

    private void fiscalizeZReport() throws FiscalPrinterException {
        counters.incKPK();
        counters.setShiftOpen(false);
    }

    private void fiscalizeXReport() throws FiscalPrinterException {
        counters.incSPND();
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        money.setBeforeCashOperationSum(counters.getCashAmount());
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            counters.incCashIn(money.getValue());
        } else if (money.getOperationType() == InventoryOperationType.CASH_OUT) {
            counters.incCashOut(money.getValue());
        }
        updateMoneyParams(money);
    }

    private void updateMoneyParams(Money report) {
        report.getMap().put("taxpayerid", properties.getShopKPP());
        report.getMap().put("cashserialnumber", deviceSerial);
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        if (taxesOld == null) {
            taxesOld = new ValueAddedTaxCollection();
            for (TaxVO t : taxes) {
                taxesOld.addTax(new ValueAddedTax(t.getIndex(), BigDecimalConverter.convertMoney(t.value).floatValue(), t.code));
            }
        }
        return taxesOld;
    }

    @Override
    public void openShift(Cashier cashier) throws FiscalPrinterException {
        counters.setShiftOpen(true);
    }
}
