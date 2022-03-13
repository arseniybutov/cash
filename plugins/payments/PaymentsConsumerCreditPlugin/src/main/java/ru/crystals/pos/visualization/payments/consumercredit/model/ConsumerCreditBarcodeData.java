package ru.crystals.pos.visualization.payments.consumercredit.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.utils.Base64;

import java.math.BigDecimal;
import java.nio.charset.Charset;

public class ConsumerCreditBarcodeData {

    private static final int MAX_BANK_CODE_LENGTH = 20;
    private static final int MAX_PRODUCT_CODE_LENGTH = 20;
    private static final String DELIMITER = "#";
    private static final int EXPECTED_DATA_LENGTH = 7;
    private static final String CHARS_MARKERS = "аеиоуыэюянтвлкч";
    private static final Logger log = LoggerFactory.getLogger(ConsumerCreditBarcodeData.class);
    private String bankCode = "";
    private String productCode = "";
    private String contractNumber = "";
    private Double paymentAmount;
    private long creditSum;
    private long creditBaseSum;
    private long creditFirstPaySum;
    private String FIO = "";

    private ConsumerCreditBarcodeData(String[] barcodeData) {
        bankCode = barcodeData[0];
        productCode = barcodeData[1];
        contractNumber = barcodeData[2];
        creditBaseSum = convertToLong(barcodeData[4]);
        creditSum = convertToLong(barcodeData[5]);
        creditFirstPaySum = convertToLong(barcodeData[3]);
        creditSum = (creditSum > 0 ? creditSum : creditBaseSum - creditFirstPaySum);
        FIO = decodeBase64(barcodeData[6]);
    }

    public long getCreditSum() {
        return creditSum;
    }

    public long getCreditBaseSum() {
        return creditBaseSum;
    }

    private Long convertToLong(String doubleString) {
        return BigDecimal.valueOf(Double.valueOf(doubleString)).setScale(2, BigDecimal.ROUND_HALF_EVEN).movePointRight(2).longValue();
    }

    /**
     * Декодирует Base64, предполагая, что в нем может быть как cp1251, так и UTF-8
     */
    protected static String decodeBase64(String encoded) {
        if (StringUtils.isBlank(encoded)) {
            return "";
        }
        String result = new String(Base64.decode(encoded), Charset.forName("cp1251"));
        if (!StringUtils.containsAny(result.toLowerCase(), CHARS_MARKERS)) {
            String resultUTF8 = new String(Base64.decode(encoded), Charset.forName("utf8"));
            if (StringUtils.containsAny(resultUTF8.toLowerCase(), CHARS_MARKERS)) {
                return resultUTF8;
            }
        }
        return result;
    }

    public static ConsumerCreditBarcodeData getEntity(String barcode) {
        if (StringUtils.isNotBlank(barcode)) {
            String[] data = barcode.split(DELIMITER);
            if (data.length == EXPECTED_DATA_LENGTH && data[0].length() <= MAX_BANK_CODE_LENGTH && data[1].length() <= MAX_PRODUCT_CODE_LENGTH) {
                try {
                    return new ConsumerCreditBarcodeData(data);
                } catch (Exception e) {
                    log.error("Invalid barcode data [{}]", barcode, e);
                }
            }
        }
        log.error("Invalid barcode data [{}]", barcode);
        return null;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getFIO() {
        return FIO;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
}
