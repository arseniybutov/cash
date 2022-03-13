package ru.crystals.pos.bank.arcom;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;

public class LastSale {

    private static final String PATH_MODULES_BANK_ARCOM = "modules/bank/arcom/";

    protected static final String CASH_TRANS_ID = "cash.trans.id";
    protected static final String AMOUNT = "amount";
    protected static final String TIME_STAMP = "time.stamp";
    protected static final String LAST_OPERATION = "last.operation";
    protected static final String LAST_OPERATION_RRN = "last.rrn";

    protected static final long LAST_OPERATION_REVERSAL_TIMEOUT = 30 * DateUtils.MILLIS_PER_MINUTE;

    private File lastSaleFile;
    private Properties lastSaleProperties = new Properties();

    public LastSale() throws IOException {
        lastSaleFile = new File(StringUtils.stripEnd(System.getProperty("user.dir").replace('\\', '/'), "/")
                + "/" + PATH_MODULES_BANK_ARCOM + "/" + "last.sale.properties");
        loadProperties();
    }

    public boolean isLastSale(ReversalData saleData) {
        if (saleData == null) {
            return false;
        }
        boolean isLastOperation = Boolean.parseBoolean(lastSaleProperties.getProperty(LAST_OPERATION, Boolean.FALSE.toString()));
        Long cashTransId = Long.valueOf(lastSaleProperties.getProperty(CASH_TRANS_ID, "0"));
        Long amount = Long.valueOf(lastSaleProperties.getProperty(AMOUNT, "0"));
        long timeStamp = Long.parseLong(lastSaleProperties.getProperty(TIME_STAMP, "0"));
        String rrn = lastSaleProperties.getProperty(LAST_OPERATION_RRN);

        return isLastOperation && cashTransId.equals(saleData.getCashTransId())
                && amount.equals(saleData.getAmount())
                && (System.currentTimeMillis() - timeStamp < LAST_OPERATION_REVERSAL_TIMEOUT)
                && (StringUtils.isNotBlank(rrn) && rrn.equals(saleData.getRefNumber()) || StringUtils.isBlank(rrn));
    }

    public void clear() throws IOException {
        lastSaleProperties.setProperty(LAST_OPERATION, Boolean.FALSE.toString());
        writeProperties();
    }

    public void saveLastSale(ResponseData responseData, SaleData saleData) throws IOException {
        if (saleData != null) {
            lastSaleProperties.setProperty(CASH_TRANS_ID, (saleData.getCashTransId() != null) ? saleData.getCashTransId().toString() : "0");
            lastSaleProperties.setProperty(AMOUNT, (saleData.getAmount() != null) ? saleData.getAmount().toString() : "0");
            lastSaleProperties.setProperty(TIME_STAMP, Long.valueOf(System.currentTimeMillis()).toString());
            lastSaleProperties.setProperty(LAST_OPERATION, Boolean.TRUE.toString());
            lastSaleProperties.setProperty(LAST_OPERATION_RRN, Optional.ofNullable(responseData.getReferenceNumber()).orElse(StringUtils.EMPTY));
            writeProperties();
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
        try (FileOutputStream out = new FileOutputStream(lastSaleFile)) {
            lastSaleProperties.store(out, null);
        }
    }

    protected void setProperties(Properties properties) {
        lastSaleProperties = properties;
    }

    protected Properties getProperties() {
        return lastSaleProperties;
    }

}
