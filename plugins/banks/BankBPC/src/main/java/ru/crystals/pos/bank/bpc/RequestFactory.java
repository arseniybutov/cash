package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.currency.CurrencyUtil;

import java.util.HashMap;
import java.util.Map;

public class RequestFactory {
    private static final int AMOUNT_ZERO_COUNT = 12;
    private static final int ERN_ZERO_COUNT = 10;
    private static final String PURCHASE_OPERATION_MESSAGE_ID = "PUR";
    private static final String REFUND_OPERATION_MESSAGE_ID = "REF";
    private static final String REVERSAL_OPERATION_MESSAGE_ID = "VOI";
    private static final String DAILY_LOG_OPERATION_MESSAGE_ID = "STL";
    private static final String JRN_OPERATION_MESSAGE_ID = "JRN";
    private static final String SRV_OPERATION_MESSAGE_ID = "SRV";
    private static final Map<String, String> CURRENCY_CODES = new HashMap<>();
    protected static final String RRN = "RETRIEVAL_REFERENCE_NUMBER";

    static {
        CURRENCY_CODES.put("BYN", "933");
        CURRENCY_CODES.put("RUB", "643");
        CURRENCY_CODES.put("KZT", "398");
        CURRENCY_CODES.put("KGS", "417");
    }

    public static Request createSaleRequest(SaleData saleData, String ecr, String ern) {
        return createRequest(ecr, ern, PURCHASE_OPERATION_MESSAGE_ID)
                .addField(Tag.Input.TRANSACTION_AMOUNT, longToStringWithLeadingZero(CurrencyUtil.getSumForPrint(saleData.getAmount()), AMOUNT_ZERO_COUNT))
                .addField(Tag.Input.CURRENCY, CURRENCY_CODES.get(saleData.getCurrencyCode().toUpperCase()));
    }

    public static Request createRefundRequest(RefundData refundData, String ecr, String ern, boolean includeRRN) {
        Request request = createRequest(ecr, ern, REFUND_OPERATION_MESSAGE_ID)
                .addField(Tag.Input.TRANSACTION_AMOUNT, longToStringWithLeadingZero(CurrencyUtil.getSumForPrint(refundData.getAmount()), AMOUNT_ZERO_COUNT))
                .addField(Tag.Input.CURRENCY, CURRENCY_CODES.get(refundData.getCurrencyCode().toUpperCase()));
        return includeRRN ? request.addField(Tag.Input.RRN, refundData.getExtendedData().get(RRN)) : request;
    }

    public static Request createAutoReversalRequest(String ecrNumber, String ern) {
        return createRequest(ecrNumber, StringUtils.leftPad(ern, ERN_ZERO_COUNT, '0'), REVERSAL_OPERATION_MESSAGE_ID);
    }

    public static Request createDailyLogRequest(String ecrNumber, String ern) {
        return createRequest(ecrNumber, ern, DAILY_LOG_OPERATION_MESSAGE_ID);
    }

    private static Request createRequest(String ecr, String ern, String messageId) {
        return createRequest(ecr, messageId).addField(Tag.Input.ERN, ern);
    }

    private static Request createRequest(String ecr, String messageId) {
        return new Request().addField(Tag.Input.MESSAGE_ID, messageId).addField(Tag.Input.ECR_NUMBER, ecr);
    }

    private static String longToStringWithLeadingZero(Long ern, int zeroCount) {
        return StringUtils.leftPad(String.valueOf(ern), zeroCount, '0');
    }

    public static Request createReversalRequest(ReversalData reversalData, String ecrNumber) {
        return createRequest(ecrNumber, StringUtils.leftPad(reversalData.getRefNumber(), ERN_ZERO_COUNT, '0'), REVERSAL_OPERATION_MESSAGE_ID)
                .addField(Tag.Input.TRANSACTION_AMOUNT, longToStringWithLeadingZero(CurrencyUtil.getSumForPrint(reversalData.getAmount()), AMOUNT_ZERO_COUNT))
                .addField(Tag.Input.CURRENCY, CURRENCY_CODES.get(reversalData.getCurrencyCode().toUpperCase()));
    }

    public static Request createJRNOperationRequest(String ecr, String ern) {
        return createRequest(ecr, ern, JRN_OPERATION_MESSAGE_ID);
    }

    public static Request createServiceOperationRequest(String ecr, int operationCode) {
        return createRequest(ecr, SRV_OPERATION_MESSAGE_ID).addField(Tag.Input.SRV_SUB_FUNCTION, operationCode);
    }
}
