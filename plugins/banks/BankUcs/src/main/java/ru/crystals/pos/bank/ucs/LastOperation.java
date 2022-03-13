package ru.crystals.pos.bank.ucs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.datastruct.AuthorizationData;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

public class LastOperation {
    private static Logger log = LoggerFactory.getLogger(LastOperation.class);
    private static final String PATH_MODULES_BANK_UCS = "modules/bank/ucs/";
    protected static final String CASH_TRANS_ID = "cash.trans.id";
    protected static final String AMOUNT = "amount";
    protected static final String TIME_STAMP = "time.stamp";
    private File lastSaleFile;
    private Properties lastSaleProperties = new Properties();

    public LastOperation() {
        lastSaleFile = new File(StringUtils.stripEnd(System.getProperty("user.dir").replace('\\', '/'), "/") + "/" + PATH_MODULES_BANK_UCS + "/" +
                "last.sale.properties");
        loadProperties();
    }

    public String getLastTransactionID() {
        return lastSaleProperties.getProperty(CASH_TRANS_ID, "");
    }

    public void clear() {
        lastSaleProperties.setProperty(CASH_TRANS_ID, "");
        try {
            writeProperties();
        } catch (IOException e) {
            log.debug("Unable to clear last operation data", e);
        }
    }

    public void saveLastSale(AuthorizationData authorizationData) throws IOException {
        if (authorizationData != null) {
            lastSaleProperties.setProperty(CASH_TRANS_ID,
                    authorizationData.getRefNumber() != null ? StringUtils.right(String.valueOf(authorizationData.getRefNumber()), 4) : "0");
            lastSaleProperties.setProperty(AMOUNT, String.valueOf(authorizationData.getAmount() != null ? authorizationData.getAmount() : "0"));
            lastSaleProperties.setProperty(TIME_STAMP, String.valueOf(Long.valueOf(System.currentTimeMillis())));
            writeProperties();
        }
    }

    private void loadProperties() {
        try {
            if (!lastSaleFile.exists()) {
                if (lastSaleFile.getParent() != null) {
                    new File(lastSaleFile.getParent()).mkdirs();
                }
                lastSaleFile.createNewFile();
            }
            lastSaleProperties.load(new StringReader(FileUtils.readFileToString(lastSaleFile)));
        } catch (IOException e) {
            log.error("Failed to load last operation", e);
        }
    }

    private void writeProperties() throws IOException {
        StringWriter sw = new StringWriter();
        lastSaleProperties.store(sw, null);
        FileUtils.write(lastSaleFile, sw.toString());
    }

    protected void setProperties(Properties properties) {
        lastSaleProperties = properties;
    }

    protected Properties getProperties() {
        return lastSaleProperties;
    }
}
