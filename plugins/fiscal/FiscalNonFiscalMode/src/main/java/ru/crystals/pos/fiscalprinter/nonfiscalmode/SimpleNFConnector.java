package ru.crystals.pos.fiscalprinter.nonfiscalmode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.property.Properties;

@PrototypedComponent
public class SimpleNFConnector implements FiscalConnector {

    private static final Logger log = LoggerFactory.getLogger(SimpleNFConnector.class);
    private final Properties properties;


    private FiscalPrinterData fiscalData;

    @Autowired
    public SimpleNFConnector(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void start() {
        fiscalData = new FiscalPrinterData();
        try {
            fiscalData.loadState();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void openShift(Cashier cashier) throws FiscalPrinterException {
        fiscalData.resetShiftCounters();
        fiscalData.setShiftOpen();
        fiscalData.updateState();

    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    public long getLastFiscalDocId() throws FiscalPrinterException {
        return fiscalData.getKPK();
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        return fiscalData.getCashAmount();
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        return fiscalData.isShiftOpen();
    }

    @Override
    public String getDeviceName() {
        return null;
    }

    @Override
    public String getRegNum() throws FiscalPrinterException {
        if (fiscalData.getRegNum() == null) {
            fiscalData.setRegNum(String.format("NFM.%s.%s.0.%s", properties.getShopIndex(),
                    properties.getCashNumber(),
                    System.currentTimeMillis()));
            fiscalData.updateState();
        }
        return fiscalData.getRegNum();
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        return fiscalData.getShiftCounters();
    }

    @Override
    public long getCashInCount() throws FiscalPrinterException {
        return fiscalData.getCountCashIn();
    }

    @Override
    public long getCashOutCount() throws FiscalPrinterException {
        return fiscalData.getCountCashOut();
    }

    @Override
    public long getAnnulCount() throws FiscalPrinterException {
        return fiscalData.getCountAnnul();
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        fiscalData.incKPK();
        if (check.getType() == CheckType.SALE) {
            long paymentsSumm = 0;
            long cashSumm = 0;
            for (Payment p : check.getPayments()) {
                paymentsSumm += p.getSum();
                if (p.getIndexPayment() == PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex()) {
                    cashSumm += p.getSum();
                }
            }
            long surch = paymentsSumm - check.getCheckSumEnd();

            fiscalData.incCashAmount(cashSumm - surch);
            fiscalData.incSumSale(paymentsSumm - surch);
            fiscalData.incCountSale();
            fiscalData.updateState();
        } else if (check.getType() == CheckType.RETURN) {

            long cashSumm = 0;
            long paymentsSumm = 0;
            for (Payment p : check.getPayments()) {
                paymentsSumm += p.getSum();
                if (p.getIndexPayment() == PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex()) {
                    cashSumm += p.getSum();
                }
            }
            fiscalData.decCashAmount(cashSumm);
            fiscalData.incSumReturn(paymentsSumm);
            fiscalData.incCountReturn();
            fiscalData.updateState();
        }
    }

    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        if (report.isZReport()) {
            fiscalData.incShiftNum();
            fiscalData.incKPK();
            fiscalData.setShiftClose();
            fiscalData.updateState();
        }
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        if (money.getOperationType().equals(InventoryOperationType.CASH_IN)) {
            fiscalData.incCountCashIn();
            fiscalData.incCashAmount(money.getValue());
        } else if (money.getOperationType().equals(InventoryOperationType.CASH_OUT)) {
            fiscalData.incCountCashOut();
            fiscalData.decCashAmount(money.getValue());
        }
        fiscalData.updateState();
    }

    @Override
    public void registerAnnulCheck(Check check) throws FiscalPrinterException {
        fiscalData.incCountAnnul();
        fiscalData.updateState();
    }

    @Override
    public String getFactoryNum() {
        return "EMPTY";
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        return null;
    }
}
