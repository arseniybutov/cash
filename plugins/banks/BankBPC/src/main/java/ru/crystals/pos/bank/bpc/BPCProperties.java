package ru.crystals.pos.bank.bpc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

public class BPCProperties {
    private static final Logger log = LoggerFactory.getLogger(BankBPCServiceImpl.class);
    private static final String PATH_MODULES_BANK_BPC = "modules/bank/bpc/";
    private static final String LAST_OPERATION = "last.ern";
    private static final String DAILY_LOG_EXPECTED = "daily.log.expected";
    private File lastSaleFile;
    private Properties lastSaleProperties = new Properties();

    public void load() throws IOException {
        lastSaleFile = new File(StringUtils.stripEnd(System.getProperty("user.dir").replace('\\', '/'), "/") + "/" + PATH_MODULES_BANK_BPC + "/" +
                "last.sale.properties");
        loadProperties();
    }

    public String getERN() {
        return lastSaleProperties.getProperty(LAST_OPERATION, "1");
    }

    public void clear() throws IOException {
        lastSaleProperties.setProperty(LAST_OPERATION, String.valueOf(Boolean.FALSE));
        writeProperties();
    }

    public void increaseERN() {
        try {
            lastSaleProperties.setProperty(LAST_OPERATION, String.valueOf(Long.valueOf(getERN()) + 1));
            writeProperties();
        } catch (IOException ioe) {
            log.warn("", ioe);
        }
    }

    public boolean isDailyLogExpected() {
        return Boolean.parseBoolean(lastSaleProperties.getProperty(DAILY_LOG_EXPECTED, "false"));
    }

    public void setDailyLogExpected(boolean dailyLogExpected) {
        try {
            lastSaleProperties.setProperty(DAILY_LOG_EXPECTED, String.valueOf(dailyLogExpected));
            writeProperties();
        } catch (IOException ioe) {
            log.warn("", ioe);
        }
    }

    protected void loadProperties() throws IOException {
        if (!lastSaleFile.exists()) {
            if (lastSaleFile.getParent() != null) {
                new File(lastSaleFile.getParent()).mkdirs();
            }
            lastSaleFile.createNewFile();
        }
        lastSaleProperties.load(new StringReader(FileUtils.readFileToString(lastSaleFile)));
    }

    protected void writeProperties() throws IOException {
        StringWriter sw = new StringWriter();
        lastSaleProperties.store(sw, null);
        FileUtils.write(lastSaleFile, sw.toString());
    }
}
