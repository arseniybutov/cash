package ru.crystals.pos.fiscalprinter.shtrihminifrk.simurg;

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
 * Эмулятор счетчика номера смены
 */
public class SimurgCounterEmulator {
    private static final Logger LOG = LoggerFactory.getLogger(SimurgCounterEmulator.class);

    private static final String SHIFT_NUM_PARAMETER = "ShiftNum";
    private static final String COUNTERS_STORAGE = "simurg_counters.properties";
    private static final String COUNTERS_FILE_PATH = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + COUNTERS_STORAGE;
    private static Properties properties;

    public SimurgCounterEmulator() {
        properties = new Properties();
    }

    public long getShiftNum() throws FiscalPrinterException {
        return getLongProperty(SHIFT_NUM_PARAMETER);
    }

    private void setShiftNum(Long shiftNum) {
        setLongProperty(SHIFT_NUM_PARAMETER, shiftNum);
    }

    public void incShiftNum() throws FiscalPrinterException {
        setLongProperty(SHIFT_NUM_PARAMETER, getShiftNum() + 1);
    }

    public void loadState() throws Exception {
        File file = new File(COUNTERS_FILE_PATH);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
            }
        } else {
            setInitialState();
            updateState();
        }
    }

    private void setInitialState() {
        setShiftNum(1L);
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
        if (p == null) {
            setInitialState();
            p = getStringProperty(name);
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
        return properties.getProperty(name);
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

}
