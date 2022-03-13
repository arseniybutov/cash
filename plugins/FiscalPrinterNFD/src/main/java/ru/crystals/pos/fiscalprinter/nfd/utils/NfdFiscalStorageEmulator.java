package ru.crystals.pos.fiscalprinter.nfd.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Эмулятор счетчиков и хранилище фискальных данных
 */
public class NfdFiscalStorageEmulator {

    private static final Logger LOG = LoggerFactory.getLogger(NfdFiscalStorageEmulator.class);

    private static final String RNM_PARAMETER = "RNM";
    private static final String SERIAL_NUMBER_PARAMETER = "serialNumber";
    private static final String TAX_PAYER_PARAMETER = "taxPayer";
    private static final String BIN_PARAMETER = "BIN";
    private static final String RNN_PARAMETER = "RNN";
    private static final String TAXATION_PARAMETER = "taxation";
    private static final String DEPARTMENT_PARAMETER = "department";
    private static final String CASH_DESK_CODE_PARAMETER = "cashDeskCode";
    private static final String ADDRESS_PARAMETER = "address";

    private static final String KPK_PARAMETER = "KPK";
    private static final String SPND_PARAMETER = "SPND";
    private static final String SHIFT_NUM_PARAMETER = "ShiftNum";
    private static final String ANNUL_COUNT_PARAMETER = "AnnulCount";
    private static final String COUNTERS_STORAGE = "nfd_counters.properties";
    private static final String COUNTERS_FILE_PATH = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + COUNTERS_STORAGE;
    private static Properties properties;

    public NfdFiscalStorageEmulator() {
        properties = new Properties();
    }

    // RNM
    public long getRNM() throws FiscalPrinterException {
        return getLongProperty(RNM_PARAMETER);
    }

    public void setRNM(long rnm) {
        setLongProperty(RNM_PARAMETER, rnm);
    }

    // serialNumber
    public String getSerialNumber() {
        return getStringProperty(SERIAL_NUMBER_PARAMETER);
    }

    public void setSerialNumber(String serialNumber) {
        setStringProperty(SERIAL_NUMBER_PARAMETER, serialNumber);
    }

    // taxPayer
    public String getTaxPayer() {
        return getStringProperty(TAX_PAYER_PARAMETER);
    }

    public void setTaxPayer(String taxPayer) {
        setStringProperty(TAX_PAYER_PARAMETER, taxPayer);
    }

    // BIN
    public long getBIN() throws FiscalPrinterException {
        return getLongProperty(BIN_PARAMETER);
    }

    public void setBIN(long bin) {
        setLongProperty(BIN_PARAMETER, bin);
    }

    // RNN
    public long getRNN() throws FiscalPrinterException {
        return getLongProperty(RNN_PARAMETER);
    }

    public void setRNN(long rnn) {
        setLongProperty(RNN_PARAMETER, rnn);
    }

    // taxation
    public String getTaxation() {
        return getStringProperty(TAXATION_PARAMETER);
    }

    public void setTaxation(String taxation) {
        setStringProperty(TAXATION_PARAMETER, taxation);
    }

    // department
    public String getDepartment() {
        return getStringProperty(DEPARTMENT_PARAMETER);
    }

    public void setDepartment(String department) {
        setStringProperty(DEPARTMENT_PARAMETER, department);
    }

    // cashDeskCode
    public String getCashDeskCode() {
        return getStringProperty(CASH_DESK_CODE_PARAMETER);
    }

    public void setCashDeskCode(String cashDeskCode) {
        setStringProperty(CASH_DESK_CODE_PARAMETER, cashDeskCode);
    }

    // address
    public String getAddress() throws FiscalPrinterException {
        return getStringProperty(ADDRESS_PARAMETER);
    }

    public void setAddress(String address) {
        setStringProperty(ADDRESS_PARAMETER, address);
    }

    // KPK
    public long getKPK() throws FiscalPrinterException {
        return getLongProperty(KPK_PARAMETER);
    }

    private void setKPK(long kpk) {
        setLongProperty(KPK_PARAMETER, kpk);
    }

    public void incKPK() throws FiscalPrinterException {
        setLongProperty(KPK_PARAMETER, getKPK() + 1);
    }

    // SPND
    public long getSPND() throws FiscalPrinterException {
        return getLongProperty(SPND_PARAMETER);
    }

    private void setSPND(long docNum) {
        setLongProperty(SPND_PARAMETER, docNum);
    }

    public void incSPND() throws FiscalPrinterException {
        setLongProperty(SPND_PARAMETER, getSPND() + 1);
    }

    // ShiftNum
    public Long getShiftNum() throws FiscalPrinterException {
        return getLongProperty(SHIFT_NUM_PARAMETER);
    }

    private void setShiftNum(Long shiftNum) {
        setLongProperty(SHIFT_NUM_PARAMETER, shiftNum);
    }

    // Annul count
    public long getAnnulCount() throws FiscalPrinterException {
        return getLongProperty(ANNUL_COUNT_PARAMETER);
    }

    private void setAnnulCount(long count) {
        setLongProperty(ANNUL_COUNT_PARAMETER, count);
    }

    public void incAnnulCount() throws FiscalPrinterException {
        setLongProperty(ANNUL_COUNT_PARAMETER, getAnnulCount() + 1);
    }

    public void loadState() throws FiscalPrinterException {
        File file = new File(COUNTERS_FILE_PATH);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
            } catch (IOException e) {
                throw new FiscalPrinterException("NfdFiscalStorageEmulator error load state", e);
            }
        } else {
            setInitialState();
            updateState();
        }
    }

    private void setInitialState() {
        setAnnulCount(0L);
        setShiftNum(1L);
        setSPND(1L);
        setKPK(1L);
    }

    public void updateState() throws FiscalPrinterException {
        try (OutputStream out = new FileOutputStream(COUNTERS_FILE_PATH)) {
            properties.store(out, "Fiscal printer state");
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    private long getLongProperty(String name) throws FiscalPrinterException {
        String p = getStringProperty(name);
        if (p == null || "".equals(p)) {
            p = "0";
        }
        try {
            return Long.parseLong(p);
        } catch (Exception e) {
            throw new FiscalPrinterException(String.format("Uncorrect value of property: file - %s %n property name - %s %n" +
                    "property value - %s, instead of long value.%n", COUNTERS_FILE_PATH, name, p));
        }
    }

    private void setLongProperty(String name, long value) {
        setStringProperty(name, Long.toString(value));
    }

    private String getStringProperty(String name) {
        String p = properties.getProperty(name);
        if (p == null) {
            p = "";
        }
        return p;
    }

    private void setStringProperty(String name, String value) {
        LOG.debug("update Property: {} value: {}", name, value);
        properties.setProperty(name, value);
        try {
            updateState();
        } catch (FiscalPrinterException e) {
            LOG.error("update Property Error: ", e);
        }
    }

    @Override
    public String toString() {
        return "NfdFiscalStorageEmulator:" + properties.toString();
    }
}
