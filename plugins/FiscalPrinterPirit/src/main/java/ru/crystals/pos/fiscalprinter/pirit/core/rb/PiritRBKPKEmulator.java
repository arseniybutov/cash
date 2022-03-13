package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.*;
import java.util.Properties;

public class PiritRBKPKEmulator {
    private static final String KPK_PARAMETER = "KPK";
    private static final String START_Z_PARAMETER = "START_Z";
    private static final String PIRIT_COUNTERS_STORAGE = "pirit_counters.properties";
    private static final String COUNTERS_FILE_PATH = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + PIRIT_COUNTERS_STORAGE;
    private static Properties properties;

    public PiritRBKPKEmulator() {
        properties = new Properties();
    }

    public long getKPK() throws FiscalPrinterException {
        return getLongProperty(KPK_PARAMETER);
    }

    private void setKPK(long kpk) {
        setLongProperty(KPK_PARAMETER, kpk);
    }

    public boolean getStartZ(){
        return Boolean.valueOf(getStringProperty(START_Z_PARAMETER));
    }
    
    public void setStartZ(boolean start) {
        setStringProperty(START_Z_PARAMETER, Boolean.toString(start));
    }
    
    public long incKPKAndGet() throws FiscalPrinterException {
        long kpk = getKPK() + 1;
        setLongProperty(KPK_PARAMETER, kpk);
        return kpk;
    }

    public void loadState() throws Exception {
        File file = new File(COUNTERS_FILE_PATH);
        if (file.exists()) {
            InputStream is = new FileInputStream(file);
            properties.load(is);
            is.close();
        } else {
            setInitialState();
            updateState();
        }
    }

    private void setInitialState() {
        properties.setProperty(KPK_PARAMETER, "0");
        properties.setProperty(START_Z_PARAMETER, "false");
    }

    public void updateState() throws FiscalPrinterException {
        try {
            OutputStream out = new FileOutputStream(COUNTERS_FILE_PATH);
            properties.store(out, "Fiscal printer state");
            out.close();
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
            return new Long(p).longValue();
        } catch (Exception e) {
            throw new FiscalPrinterException("Uncorrect value of property: " + "file - " + COUNTERS_FILE_PATH + "/n" + "property name - " + name + "/n" +
                    "property value - " + p + ", instead of long value./n");
        }
    }

    private void setLongProperty(String name, long value) {
        setStringProperty(name, Long.toString(value));
    }

    private String getStringProperty(String name) {
        return properties.getProperty(name);
    }

    private void setStringProperty(String name, String value) {
        properties.setProperty(name, value);
        try {
            updateState();
        } catch (FiscalPrinterException e) {
            e.printStackTrace();
        }
    }

}
