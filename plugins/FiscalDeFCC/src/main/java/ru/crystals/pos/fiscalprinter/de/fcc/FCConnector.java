package ru.crystals.pos.fiscalprinter.de.fcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.configurator.core.Configurable;
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
import ru.crystals.pos.fiscalprinter.datastruct.documents.LongExtended;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.property.Properties;
import ru.crystals.set10dto.TaxVO;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Используется в Германии
 *
 * @author dalex
 */
@PrototypedComponent
public class FCConnector implements FiscalConnector, Configurable<FCConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(FCConnector.class);
    static final String PURCHASE_SET10_TRANSACTION_ID = "PurchaseFCC_SET10TransactionID";
    static final String PURCHASE_FCC_TRANSACTION_ID = "PurchaseFCC_TransactionID";

    private final KPKCounters counters = new KPKCounters();
    private FCCImpl fcc;
    private List<TaxVO> taxes;
    private ValueAddedTaxCollection taxesOld;
    private FCConfig config;
    @Autowired
    private Properties properties;

    @Override
    public Class<FCConfig> getConfigClass() {
        return FCConfig.class;
    }

    @Override
    public void setConfig(FCConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        fcc = new FCCImpl(counters);
        counters.loadState();
        counters.setProperties(properties);
        fillTaxes();
        try {
            if (config.getDeviceSerial() == null) {
                config.setDeviceSerial(getLocalMacAddress());
            }
            fcc.setConfig(config);
            fcc.connect();
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
                if (network == null) {
                    network = ints.nextElement();
                }
                if (!network.isLoopback()) {
                    break;
                }
            }
            if (network != null && network.getHardwareAddress() != null) {
                byte[] mac = network.getHardwareAddress();
                for (byte i : mac) {
                    strMac.append(Integer.toHexString((int) i));
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
        fcc.setTaxes(taxes);
    }

    @Override
    public String getINN() {
        return config.getUniqueClientId();
    }

    @Override
    public String getRegNum() {
        return config.getRegistrationToken();
    }

    @Override
    public String getFactoryNum() {
        return "1.0.1";
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        return counters.getShiftNum();
    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
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
        return "DE FCC";
    }

    @Override
    public void processNonFiscal(AbstractDocument document) throws FiscalPrinterException {
        counters.incSPND();
    }

    @Override
    public void processCopyDocument(FiscalDocument document) throws FiscalPrinterException {
        counters.incSPND();
        try {
            fcc.updatePurchase(document);
        } catch (Exception ex) {
            LOG.error("Cannot update purchase data", ex);
        }
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
                fcc.fiscalize(check);
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

        check.getMap().put("taxpayerid", config.getUniqueClientId());
        check.getMap().put("registrationToken", config.getRegistrationToken());
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
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleFCC.getString("FISCALIZATION_FAIL"), e);
        }
    }

    private void updateReportParams(Report report) throws FiscalPrinterException {
        ReportCounters repCounters = report.getReportCounters();
        report.getMap().put("totalcountcheck", new LongExtended(repCounters.getCountSale() + repCounters.getCountReturn()));
        report.getMap().put("notsendedtofd", new LongExtended(0L));
        report.getMap().put("notsendedtofdtime", "00.00.00");
        report.getMap().put("taxpayerid", getINN());
        report.getMap().put("shiftfdcount", new LongExtended(counters.getSPND()));
        report.getMap().put("cashserialnumber", config.getDeviceSerial());
        report.getMap().put("eklz", config.getDeviceSerial());
        report.getMap().put("registration_token", config.getRegistrationToken());
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
        if (money.getOperationType() == InventoryOperationType.CASH_IN) {
            counters.incCashIn(money.getValue());
        } else if (money.getOperationType() == InventoryOperationType.CASH_OUT) {
            counters.incCashOut(money.getValue());
        }
        updateMoneyParams(money);
    }

    private void updateMoneyParams(Money report) {
        report.getMap().put("taxpayerid", getINN());
        report.getMap().put("eklz", config.getDeviceSerial());
        report.getMap().put("cashserialnumber", config.getDeviceSerial());
        report.getMap().put("registration_token", config.getRegistrationToken());
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
