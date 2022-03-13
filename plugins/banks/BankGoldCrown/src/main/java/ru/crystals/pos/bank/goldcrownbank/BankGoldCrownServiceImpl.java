package ru.crystals.pos.bank.goldcrownbank;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.AbstractFileBasedBank;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Интеграция с банковским процессингом (эквайрингом) "Золотая корона" (не путать с системой лояльности "Золотая корона")
 */
public class BankGoldCrownServiceImpl extends AbstractFileBasedBank {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    private static final String REPORT_OPERATION = "report";
    private static final String PAYMENT_DIRECTION_PARAMETER = "-r";
    private static final String REVERSAL_OPERATION = "-reversal";
    private static final String TRANZ_ID_PARAMETER = "-tranz_id";
    private static final String REFUND_OPERATION = "-refund";
    private static final String ORIG_SUM_PARAMETER = "-orig_sum";
    private static final String ORIG_NUM_PARAMETER = "-orig_num";
    private static final String ORIG_TERM_PARAMETER = "-orig_term";
    private static final String ORIG_TIME_PARAMETER = "-orig_time";
    /**
     * Префикс терминалов Золотая корона, используемых для эквайринга (никогда не будет меняться)
     */
    private static final String TERMINAL_PREFIX = "J";

    private static final String TIME_PARAMETER_DATE_FORMAT = "ddMMyyyyHHmmss";
    private static final long DEFAULT_TIMEOUT_BEFORE_FALLBACK_REFUND = 2000;

    /**
     * Имя конфигурационного файла процессинга
     */
    private String netcashConfigName = "netcash.cfg";
    /**
     * Номер терминала
     */
    private String terminalNumber;

    private static final String DEFAULT_RESPONSE_FILE_NAME = "status.txt";

    private static final String SLIP_AND_RESPONSE_FILE_CHARSET = "cp866";
    private static final String DEFAULT_SLIP_FILE_NAME = "check.txt";
    private static final String DEFAULT_TERMINAL_ID = "J000000";
    /**
     * Направление платежа. Если направление не заполнено подставлять 0. Формат – целое число 2 байта. Банковский параметр для раскрашивания платежей
     * (допустим на разные юр.лица). Может быть задано одно значение на одно юр.лицо без визуализации единожды при установке терминала.
     */
    private int paymentDirection = 0;
    /**
     * Признак печати второй копии слипа (процессинг всегда возвращает один слип - он и слип кассира, и слип покупателя
     */
    private boolean printSecondSlip = true;

    /**
     * Таймаут между перед выполнением возврата, выполняемого в случае неуспешной операции отмены
     */
    private long timeoutBeforeFallbackRefund = DEFAULT_TIMEOUT_BEFORE_FALLBACK_REFUND;


    @Override
    public void start() {

        setProcessing(StringUtils.defaultIfBlank(getProcessing(), "banks/goldcrownbank/"));
        setResponseData(new GoldCrownResponseData());
        setExecutableFileName(StringUtils.defaultIfBlank(getExecutableFileName(), "netcash" + (SystemUtils.IS_OS_WINDOWS ? "32.exe" : "")));
        setSlipAndResponseFileCharset(SLIP_AND_RESPONSE_FILE_CHARSET);

        Properties netcashCfg = new Properties();
        try {
            netcashCfg = getProperties(getFullPathToProcessingFolder() + netcashConfigName, "utf-8");
        } catch (BankConfigException e) {
            log.warn("Default values for responseFileName ({}), slipFileName ({}), terminal ({}), will be used",
                    DEFAULT_RESPONSE_FILE_NAME, DEFAULT_SLIP_FILE_NAME, StringUtils.defaultIfBlank(terminalNumber, DEFAULT_TERMINAL_ID));
        }
        setResponseFileName(netcashCfg.getProperty("STATUS", DEFAULT_RESPONSE_FILE_NAME));
        setSlipFileName(netcashCfg.getProperty("PRNIMAGE", DEFAULT_SLIP_FILE_NAME));
        terminalNumber = netcashCfg.getProperty("NAME_TERM", StringUtils.defaultIfBlank(terminalNumber, DEFAULT_TERMINAL_ID));
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public int getPaymentDirection() {
        return paymentDirection;
    }

    public void setPaymentDirection(int paymentDirection) {
        this.paymentDirection = paymentDirection;
    }

    protected static String normalizeTerminalID(String asLong) {
        // когда (если) в БД сервера поле terminalId станет String, сохранять без преобразования (но оставить разворачивание для совместимости
        // при условии, что внедрения были). Впрочем, у Золотой короны префикс для эквайринговых терминалов никогда не поменяется, соответственно,
        // можно смело хранить в БД только число, разворачивая его до формата Jxxxxxx
        if (asLong.startsWith(TERMINAL_PREFIX)) {
            return asLong;
        } else {
            try {
                return TERMINAL_PREFIX + StringUtils.leftPad(asLong, 6, '0');
            } catch (Exception e) {
                return asLong;
            }
        }
    }

    @Override
    public void fillSpecificFields(AuthorizationData ad, ResponseData responseData, BankOperationType operationType) {
        GoldCrownResponseData rd = (GoldCrownResponseData) responseData;
        ad.setDate(rd.getDate());
        ad.setRefNumber(String.valueOf(rd.getTerminalTransId()));
        // идентификатор транзакции - при отмене использовать ровно то, что пришло по оплате (поэтому значение хранится в строковом поле
        // MerchantId, чтобы ради Золотой Короны не заниматься изменением БД, которое, тем не менее, требуется сделатьв будущем).
        ad.setMerchantId(rd.getHostTransId());
        if (StringUtils.startsWith(terminalNumber, TERMINAL_PREFIX)) {
            ad.setTerminalId(terminalNumber.substring(1));
        } else {
            ad.setTerminalId(terminalNumber);
        }
    }

    @Override
    public void makeSlip(AuthorizationData ad, ResponseData responseData, List<String> slip, BankOperationType operationType) {
        if (responseData.isSuccessful()) {
            List<List<String>> result = new ArrayList<>();
            result.add(slip);
            if (printSecondSlip) {
                result.add(slip);
            }
            ad.setSlips(result);
        }
    }

    @Override
    public List<String> prepareParametersForDailyLog(Long cashTransId) {
        return Arrays.asList(netcashConfigName, REPORT_OPERATION);
    }

    @Override
    public List<String> prepareParametersForDailyReport(Long cashTransId) {
        return new ArrayList<>();
    }

    private String prepareAmount(Long amount) {
        return BigDecimal.valueOf(amount, 2).toString();
    }

    @Override
    public AuthorizationData refund(RefundData rd) throws BankException {
        if (rd != null && isSaleDataValidForOperation(rd, BankOperationType.REFUND)) {
            return super.refund(rd);
        } else {
            throw new BankException(ResBundleBankGoldCrown.getString("REFUND_IS_NOT_AVAILABLE_WITHOUT_CHECK"));
        }
    }

    @Override
    public boolean shouldBeProcessedAsRefundIfReversalFailed(ReversalData reversalData, BankAuthorizationException bankAuthorizationException) {
        return true;
    }

    @Override
    public AuthorizationData refundIfReversalFailed(ReversalData reversalData) throws BankException {
        try {
            Thread.sleep(getTimeoutBeforeFallbackRefund());
        } catch (InterruptedException ignored) {
        }
        return refund(reversalData);
    }

    @Override
    public boolean canBeProcessedAsReversal(ReversalData rd) {
        return (rd.getMerchantId() != null && rd.getAmount().equals(rd.getOriginalSaleTransactionAmount()));
    }

    @Override
    public boolean isSaleDataValidForOperation(SaleData saleData, BankOperationType operationType) {
        if (saleData.getAmount() == null) {
            log.error("SaleData.getAmount() == null");
            return false;
        } else if (operationType == BankOperationType.REVERSAL) {
            if (((ReversalData) saleData).getMerchantId() == null) {
                log.error("ReversalData.getHostTransId() == null");
                return false;
            }
        } else if (operationType == BankOperationType.REFUND) {
            RefundData rd = (RefundData) saleData;
            if (rd.getRefNumber() == null || StringUtils.isBlank(rd.getTerminalId()) ||
                    rd.getOriginalSaleTransactionDate() == null) {
                log.error("RefundData has null values: getOriginalSaleTransactionAmount() == {}, " +
                                "getRefNumber() == {}, getTerminalId() == ({}), getOriginalSaleTransactionDate() == {}",
                        rd.getOriginalSaleTransactionAmount(), rd.getRefNumber(), rd.getTerminalId(),
                        rd.getOriginalSaleTransactionDate());
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> prepareExecutableParameters(SaleData saleData, BankOperationType operationType) {
        List<String> result = new ArrayList<>();
        result.add(getNetcashConfigName());
        result.add(prepareAmount(saleData.getAmount()));
        switch (operationType) {
            case SALE:
                result.add(PAYMENT_DIRECTION_PARAMETER);
                result.add(Integer.toString(getPaymentDirection()));
                break;
            case REFUND:
                RefundData rd = (RefundData) saleData;
                result.add(REFUND_OPERATION);
                // оригинальная сумма платежа (в минимальных единицах валюты – копейках)
                result.add(ORIG_SUM_PARAMETER);
                result.add(rd.getOriginalSaleTransactionAmount().toString());
                // номер операции по терминалу (целое число 0….999999) - поле TERM_NUM в файле ответа
                result.add(ORIG_NUM_PARAMETER);
                result.add(rd.getRefNumber());
                // номер терминала (формат – Jxxxxxx, где х – целое число от 0 до 9)
                result.add(ORIG_TERM_PARAMETER);
                result.add(normalizeTerminalID(rd.getTerminalId()));

                // дата и время оригинальной операции (формат DDMMYYYYHHMMSS) - поле TIME в файле ответа
                result.add(ORIG_TIME_PARAMETER);
                result.add(new SimpleDateFormat(TIME_PARAMETER_DATE_FORMAT).format(rd.getOriginalSaleTransactionDate()));
                break;
            case REVERSAL:
                result.add(REVERSAL_OPERATION);
                // идентификатор транзакции - при отмене использовать ровно то, что пришло по оплате (поэтому значение хранится в строковом поле
                // MerchantId, чтобы ради Золотой Короны не заниматься изменением БД, которое, тем не менее, требуется сделатьв будущем).
                result.add(TRANZ_ID_PARAMETER);
                result.add(((ReversalData) saleData).getMerchantId());
                break;
            default:
                break;
        }
        return result;
    }

    public String getNetcashConfigName() {
        return netcashConfigName;
    }

    public void setNetcashConfigName(String configName) {
        this.netcashConfigName = configName;
    }


    public long getTimeoutBeforeFallbackRefund() {
        return timeoutBeforeFallbackRefund;
    }

    public void setTimeoutBeforeFallbackRefund(long timeoutBeforeFallbackRefund) {
        this.timeoutBeforeFallbackRefund = Math.max(DEFAULT_TIMEOUT_BEFORE_FALLBACK_REFUND, timeoutBeforeFallbackRefund);
    }

}
