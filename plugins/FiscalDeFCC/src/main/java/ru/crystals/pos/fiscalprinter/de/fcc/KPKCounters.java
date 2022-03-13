package ru.crystals.pos.fiscalprinter.de.fcc;

import ru.crystals.bundles.BundleManager;
import ru.crystals.json.DefaultJsonParser;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.property.Properties;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author dalex
 */
public class KPKCounters {

    private static final String COUNTERS_PARAM_NAME = "kpk_counters";
    private static final String TRANSACTION_ID_PARAM_NAME = "fcc_transaction_id";

    private String jsonModuleName = "fiscal.printer.de.fcc";

    private PropertiesManager propertiesManager;
    private Properties properties;
    private KPKCountersVO countersVO;
    private DefaultJsonParser jsonParser = new DefaultJsonParser(true, false, "yyyy-MM-dd'T'HH:mm:sss");

    public KPKCounters() {
    }

    public String getJsonModuleName() {
        return jsonModuleName;
    }

    public void setJsonModuleName(String jsonModuleName) {
        this.jsonModuleName = jsonModuleName;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    public long getKPK() throws FiscalPrinterException {
        return countersVO.getKpk();
    }

    public void incKPK() throws FiscalPrinterException {
        countersVO.setKpk(countersVO.getKpk() + 1);
        saveState();
    }

    public long getSPND() throws FiscalPrinterException {
        return countersVO.getSpnd();
    }

    public void incSPND() throws FiscalPrinterException {
        countersVO.setSpnd(countersVO.getSpnd() + 1);
        saveState();
    }

    public long getShiftNum() throws FiscalPrinterException {
        return notNullValue(countersVO.getShiftCounters().getShiftNum());
    }

    public boolean isShiftOpen() {
        return countersVO.isShiftOpen();
    }

    public void setShiftOpen(boolean shiftOpen) throws FiscalPrinterException {
        if (shiftOpen == countersVO.isShiftOpen()) {
            return;
        }

        if (shiftOpen) {
            ShiftCounters counters = countersVO.getShiftCounters();
            if (counters == null) {
                counters = new ShiftCounters();
            }
            long shiftNum = notNullValue(counters.getShiftNum());
            long sumCashEnd = notNullValue(counters.getSumCashEnd());
            counters = new ShiftCounters();
            counters.setShiftNum(shiftNum + 1);
            countersVO.setShiftCounters(counters);
            counters.setSumCashEnd(sumCashEnd);
        }
        countersVO.setShiftOpen(shiftOpen);
        saveState();
    }

    private PropertiesManager getPropertiesManager() {
        if (propertiesManager == null) {
            propertiesManager = BundleManager.get(PropertiesManager.class);
        }
        return propertiesManager;
    }

    public void loadState() throws FiscalPrinterException {
        String kpkJson = getPropertiesManager().getProperty(jsonModuleName, null, COUNTERS_PARAM_NAME, null);
        countersVO = null;
        if (kpkJson != null) {
            try {
                countersVO = jsonParser.readValue(kpkJson, KPKCountersVO.class);
            } catch (Exception e) {
                throw new FiscalPrinterException("", e);
            }
        }
        boolean needSave = false;
        if (countersVO == null) {
            countersVO = new KPKCountersVO();
            countersVO.setKpk(1);
            countersVO.setShiftOpen(false);
            countersVO.setSpnd(1);
            needSave = true;
        }
        if (countersVO.getShiftCounters() == null) {
            countersVO.setShiftCounters(new ShiftCounters());
            countersVO.getShiftCounters().setSumCashEnd(0L);
            needSave = true;
        }
        if (needSave) {
            saveState();
        }
    }

    private void saveState() throws FiscalPrinterException {
        try {
            String kpkJson = jsonParser.writeValue(countersVO);
            getPropertiesManager().setObjectProperty(jsonModuleName, null, COUNTERS_PARAM_NAME, kpkJson, "DE Fiscal cloud kpk counters");
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    public long getCashInCount() {
        return notNullValue(countersVO.getShiftCounters().getCountCashIn());
    }

    public long getCashOutCount() {
        return notNullValue(countersVO.getShiftCounters().getCountCashOut());
    }

    ShiftCounters getShiftCountersClone() throws FiscalPrinterException {
        try {
            String ShiftCounters = jsonParser.writeValue(countersVO.getShiftCounters());
            ShiftCounters clone = jsonParser.readValue(ShiftCounters, ShiftCounters.class);
            return clone;
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    long getCashAmount() {
        return notNullValue(countersVO.getShiftCounters().getSumCashEnd());
    }

    private long notNullValue(Long value) {
        return value == null ? 0 : value;
    }

    void incReturn(long cashPayment, long cashlessPayment, long change) throws FiscalPrinterException {
        long checkSum = cashPayment + cashlessPayment - change;
        countersVO.getShiftCounters().setCountReturn(notNullValue(countersVO.getShiftCounters().getCountReturn()) + 1);
        countersVO.getShiftCounters().setSumReturn(notNullValue(countersVO.getShiftCounters().getSumReturn()) + checkSum);
        if (cashPayment > 0) {
            countersVO.getShiftCounters().setCountCashReturn(notNullValue(countersVO.getShiftCounters().getCountCashReturn()) + 1);
            countersVO.getShiftCounters().setSumCashEnd(notNullValue(countersVO.getShiftCounters().getSumCashEnd()) - cashPayment + change);
        }
        if (cashlessPayment > 0) {
            countersVO.getShiftCounters().setCountCashlessReturn(notNullValue(countersVO.getShiftCounters().getCountCashlessReturn()) + 1);
        }
        incSPND();
    }

    void incSale(long cashPayment, long cashlessPayment, long change) throws FiscalPrinterException {
        long checkSum = cashPayment + cashlessPayment - change;
        countersVO.getShiftCounters().setCountSale(notNullValue(countersVO.getShiftCounters().getCountSale()) + 1);
        countersVO.getShiftCounters().setSumSale(notNullValue(countersVO.getShiftCounters().getSumSale()) + checkSum);
        if (cashPayment > 0) {
            countersVO.getShiftCounters().setCountCashPurchase(notNullValue(countersVO.getShiftCounters().getCountCashPurchase()) + 1);
            countersVO.getShiftCounters().setSumCashEnd(notNullValue(countersVO.getShiftCounters().getSumCashEnd()) + cashPayment - change);
        }
        if (cashlessPayment > 0) {
            countersVO.getShiftCounters().setCountCashlessPurchase(notNullValue(countersVO.getShiftCounters().getCountCashlessPurchase()) + 1);
        }
        incSPND();
    }

    void incCashIn(long sum) throws FiscalPrinterException {
        countersVO.getShiftCounters().setCountCashIn(notNullValue(countersVO.getShiftCounters().getCountCashIn()) + 1);
        countersVO.getShiftCounters().setSumCashIn(notNullValue(countersVO.getShiftCounters().getSumCashIn()) + sum);
        countersVO.getShiftCounters().setSumCashEnd(notNullValue(countersVO.getShiftCounters().getSumCashEnd()) + sum);
        incSPND();
    }

    void incCashOut(long sum) throws FiscalPrinterException {
        countersVO.getShiftCounters().setCountCashOut(notNullValue(countersVO.getShiftCounters().getCountCashOut()) + 1);
        countersVO.getShiftCounters().setSumCashOut(notNullValue(countersVO.getShiftCounters().getSumCashOut()) + sum);
        countersVO.getShiftCounters().setSumCashEnd(notNullValue(countersVO.getShiftCounters().getSumCashEnd()) - sum);
        incSPND();
    }

    synchronized String getTransactionID() {
        String uuid = UUID.randomUUID().toString();
        long v = (Long) getPropertiesManager().getProperty(jsonModuleName, null, TRANSACTION_ID_PARAM_NAME, Long.TYPE, 1L);
        v++;
        getPropertiesManager().setObjectProperty(jsonModuleName, null, TRANSACTION_ID_PARAM_NAME, v, "DE Fcc transaction counter");
        return uuid + "." + properties.getShopIndex() + "." + properties.getCashNumber() + "." + countersVO.getShiftCounters().getShiftNum() + "." + v;
    }
}
