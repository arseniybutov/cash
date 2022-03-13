package ru.crystals.pos.fiscalprinter.de.emulator;

import ru.crystals.json.DefaultJsonParser;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class KPKCounters {

    private static final String COUNTERS_STORAGE = "fiscal_emulator_counters.properties";
    private static final String COUNTERS_FILE_PATH = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + COUNTERS_STORAGE;

    private KPKCountersVO countersVO;
    private DefaultJsonParser jsonParser = new DefaultJsonParser(true, false, "yyyy-MM-dd'T'HH:mm:sss");

    public long getKPK() {
        return countersVO.getKpk();
    }

    public void incKPK() throws FiscalPrinterException {
        countersVO.setKpk(countersVO.getKpk() + 1);
        saveState();
    }

    public long getSPND() {
        return countersVO.getSpnd();
    }

    public void incSPND() throws FiscalPrinterException {
        countersVO.setSpnd(countersVO.getSpnd() + 1);
        saveState();
    }

    public long getShiftNum() {
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
            long shiftNum = notNullValue(counters.getShiftNum(), 1L);
            long sumCashEnd = notNullValue(counters.getSumCashEnd());
            counters = new ShiftCounters();
            counters.setShiftNum(shiftNum);
            countersVO.setShiftCounters(counters);
            counters.setSumCashEnd(sumCashEnd);
        } else {
            long shiftNum = countersVO.getShiftCounters().getShiftNum() + 1;
            countersVO.getShiftCounters().setShiftNum(shiftNum);
        }
        countersVO.setShiftOpen(shiftOpen);
        saveState();
    }

    public void loadState() throws FiscalPrinterException {
        try {
            countersVO = null;
            File file = new File(COUNTERS_FILE_PATH);
            if (file.exists()) {
                try (FileInputStream is = new FileInputStream(file)) {
                    String kpkJson = getFileContent(is);
                    countersVO = jsonParser.readValue(kpkJson, KPKCountersVO.class);
                }
            }
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
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
            countersVO.getShiftCounters().setShiftNum(1L);
            needSave = true;
        }
        if (needSave) {
            saveState();
        }
    }

    private static String getFileContent(FileInputStream fis) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    private void saveState() throws FiscalPrinterException {
        try (PrintStream ps = new PrintStream(new FileOutputStream(COUNTERS_FILE_PATH))) {
            String kpkJson = jsonParser.writeValue(countersVO);
            ps.print(kpkJson);
            ps.flush();
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
            String shiftCounters = jsonParser.writeValue(countersVO.getShiftCounters());
            ShiftCounters clone = jsonParser.readValue(shiftCounters, ShiftCounters.class);
            return clone;
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    long getCashAmount() {
        return notNullValue(countersVO.getShiftCounters().getSumCashEnd());
    }

    private long notNullValue(Long value) {
        return notNullValue(value, 0L);
    }

    private long notNullValue(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
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

}
