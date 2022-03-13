package ru.crystals.pos.bank.softcase.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.softcase.ResBundleBankSoftcase;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoftCaseAnswerParser {
    private static final Logger log = LoggerFactory.getLogger(SoftCaseAnswerParser.class);
    private static final String EXPIRY_DATE_FORMAT = "MMyy";
    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final String PEM_POSITION3_PIN_ENTERED_FLAG = "1";
    private static final String PEM_POSITION12_CONTACTLESS_MS_FLAG = "91";
    private static final String PEM_POSITION12_CONTACTLESS_CHIP_FLAG = "07";
    private static final long PAYPASS_CREDENTIALS_NEEDED_AMOUNT = 100000L;
    private static final String SUCCESSFUL_CODE = "00";

    public static AuthorizationData getAuthorizationData(SoftCaseMessage message, SoftCaseDocumentTemplate template) throws BankAuthorizationException {
        AuthorizationData result = new AuthorizationData();
        result.setStatus(message.getCode().equals(SUCCESSFUL_CODE));
        result.setResponseCode(message.getCode());
        result.setAmount(message.getAmount());
        result.setAuthCode(message.getAuth());
        result.setDate(new Date());
        result.setRefNumber(String.valueOf(message.getTrace()));
        BankCard card = new BankCard();
        if (message.getExpdt() != null) {
            try {
                card.setExpiryDate(new SimpleDateFormat(EXPIRY_DATE_FORMAT).parse(message.getExpdt().toString()));
            } catch (ParseException e) {
                //
            }
        }
        card.setCardNumber(BankUtils.maskCardNumber(message.getCard(), true, false));
        card.setCardType(message.getApplabel());
        result.setCard(card);
        result.setMessage(message.getResp());
        List<List<String>> slips = new ArrayList<>();
        try {
            slips.addAll(template.processDocument(getDataSet(message)));
            if (result.isStatus()) {
                slips.addAll(slips);
            }
        } catch (Exception e) {
            log.error("Error on processing template", e);
            if (SUCCESSFUL_CODE.equals(message.getCode())) {
                slips.add(Arrays.asList(ResBundleBankSoftcase.getString("OPERATION_SUCCESSFUL")));
            } else {
                slips.add(Arrays.asList(ResBundleBankSoftcase.getString("OPERATION_NOT_SUCCESSFUL") + " (" + message.getCode() + ")"));
            }
        }
        result.setSlips(slips);
        if (message.getType() != null) {
            if (message.getType().equals(SoftCaseMessage.PAY_ANSWER_TYPE)) {
                result.setOperationType(BankOperationType.SALE);
                result.setOperationCode(Long.valueOf(SoftCaseMessage.PAY_ANSWER_TYPE));
            } else if (message.getType().equals(SoftCaseMessage.REVERSAL_ANSWER_TYPE)) {
                result.setOperationType(BankOperationType.REVERSAL);
                result.setOperationCode(Long.valueOf(SoftCaseMessage.REVERSAL_ANSWER_TYPE));
            } else if (message.getType().equals(SoftCaseMessage.REFUND_ANSWER_TYPE)) {
                result.setOperationType(BankOperationType.REFUND);
                result.setOperationCode(Long.valueOf(SoftCaseMessage.REFUND_ANSWER_TYPE));
            }
        }
        if (!result.isStatus()) {
            throw new BankAuthorizationException(result);
        }
        return result;
    }

    private static Map<String, Object> getDataSet(SoftCaseMessage message) {
        Map<String, Object> result = new HashMap<>();
        result.put("kkm", message.getKkm());
        result.put("code", message.getCode());
        result.put("type", message.getType());
        result.put("card", BankUtils.maskCardNumber(message.getCard(), true, false));
        result.put("track3", message.getTrack3());
        if (message.getAmount() != null) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setMaximumFractionDigits(2);
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setGroupingUsed(false);
            result.put("amount", decimalFormat.format((double) message.getAmount() / 100));
        }
        result.put("trace", message.getTrace());
        result.put("tdt", message.getTdt());
        result.put("expdt", message.getExpdt());
        result.put("rrn", message.getRrn());
        result.put("auth", message.getAuth());
        result.put("termid", message.getTermid());
        if (StringUtils.isEmpty(message.getResp())) {
            if (message.getCode().equals(SUCCESSFUL_CODE)) {
                result.put("resp", ResBundleBankSoftcase.getString("OPERATION_SUCCESSFUL"));
            } else {
                result.put("resp", ResBundleBankSoftcase.getString("OPERATION_NOT_SUCCESSFUL"));
            }
        } else {
            result.put("resp", message.getResp());
        }
        if (StringUtils.isNotEmpty(message.getCardholder())) {
            result.put("cardholder", message.getCardholder().trim());
        }
        if (StringUtils.isNotEmpty(message.getApplabel())) {
            result.put("applabel", message.getApplabel().trim());
        }
        result.put("aid", message.getAid());
        result.put("trancert", message.getTrancert());
        result.put("pem", message.getPem());
        result.put("cardid", message.getCardid());
        result.put("crc", message.getCrc());
        result.put("date", new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date()));
        if (StringUtils.isNotBlank(message.getPem())) {
            String pem2 = message.getPem().substring(0, 2);
            boolean isPayPass = pem2.equals(PEM_POSITION12_CONTACTLESS_MS_FLAG) || pem2.equals(PEM_POSITION12_CONTACTLESS_CHIP_FLAG);
            result.put("ispaypass", isPayPass);
            result.put("needcredentials", (!message.getPem().substring(2, 3).equals(PEM_POSITION3_PIN_ENTERED_FLAG) &&
                    (!isPayPass || (isPayPass && message.getAmount() > PAYPASS_CREDENTIALS_NEEDED_AMOUNT))));
        }
        result.put("hascardholder", StringUtils.isNotBlank(message.getCardholder()));
        result.put("hastrancert", StringUtils.isNotBlank(message.getTrancert()));
        result.put("hasaid", StringUtils.isNotBlank(message.getAid()));
        if (message.getType().equals(SoftCaseMessage.PAY_ANSWER_TYPE)) {
            result.put("operationtype", ResBundleBankSoftcase.getString("OPERATION_TYPE_FOR_RECEIPT_SALE"));
        } else if (message.getType().equals(SoftCaseMessage.REVERSAL_ANSWER_TYPE)) {
            result.put("operationtype", ResBundleBankSoftcase.getString("OPERATION_TYPE_FOR_RECEIPT_REVERSAL"));
        } else if (message.getType().equals(SoftCaseMessage.REFUND_ANSWER_TYPE)) {
            result.put("operationtype", ResBundleBankSoftcase.getString("OPERATION_TYPE_FOR_RECEIPT_REFUND"));
        } else if (message.getType().equals(SoftCaseMessage.DAILY_LOG_ANSWER_TYPE)) {
            result.put("operationtype", ResBundleBankSoftcase.getString("OPERATION_TYPE_FOR_RECEIPT_DAILY_LOG"));
        }
        return result;
    }

    public static DailyLogData getDailyLog(SoftCaseMessage message, SoftCaseDocumentTemplate template) {
        DailyLogData result = new DailyLogData();
        List<List<String>> slips = new ArrayList<>();
        try {
            slips = template.processDocument(getDataSet(message));
        } catch (Exception e) {
            log.error("Error on preparing slip", e);
            slips.add(Arrays.asList(ResBundleBankSoftcase.getString("OPERATION_TYPE_FOR_RECEIPT_DAILY_LOG"),
                    ("00".equals(message.getCode()) ? ResBundleBankSoftcase.getString("OPERATION_SUCCESSFUL") :
                            ResBundleBankSoftcase.getString("OPERATION_NOT_SUCCESSFUL"))));
        }
        result.setSlip(!slips.isEmpty() ? slips.get(0) : null);
        return result;

    }
}
