package ru.crystals.pos.bank.zvt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.zvt.commands.AuthCommand;
import ru.crystals.pos.bank.zvt.commands.EndOfDayCommand;
import ru.crystals.pos.bank.zvt.commands.RefundCommand;
import ru.crystals.pos.bank.zvt.commands.ReversalCommand;
import ru.crystals.pos.bank.zvt.protocol.TransactionField;
import ru.crystals.pos.bank.zvt.protocol.ZVTResponse;
import ru.crystals.pos.bank.zvt.utils.EncodingUtils;
import ru.crystals.utils.time.DateConverters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Интеграция с банковскими терминалами, работающими по протоколам, основанным на протоколе ZVT (спецификация Немецкой ассоциации производителей
 * терминалов: https://www.terminalhersteller.de).
 * <p>
 * На текущий момент поддерживается реализация Concardis Ingenico (SRTZ-100), скрытая под отдельным классом.
 * Предполагается, что для других реализаций ZVT будет докручиваться этот плагин, а не множиться новые.
 */
public class BankZVT extends AbstractBankPluginImpl {

    private static final Logger log = LoggerFactory.getLogger(BankZVT.class);

    private ZVTConnector connector;
    private ZVTTerminalConfig terminalConfig = new ZVTTerminalConfig();
    private String password;

    @Override
    public void start() {
        terminalConfig.setBaseConfiguration(getTerminalConfiguration());
        connector = new ZVTConnector(terminalConfig);
        password = Optional.ofNullable(terminalConfig.getPassword()).orElse("000000");
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        try (final ZVTSession zvtSession = connector.newSession()) {
            ZVTResponse response = zvtSession.sendRequestWithLoginIfRequired(new EndOfDayCommand(password), password);
            checkIsSuccessful("Daily log", response);
            return new DailyLogData();
        }
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        try (final ZVTSession zvtSession = connector.newSession()) {
            ZVTResponse response = zvtSession.sendRequestWithLoginIfRequired(new AuthCommand(saleData.getAmount()), password);

            checkIsSuccessful(BankOperationType.SALE, response);

            final AuthorizationData ad = new AuthorizationData();
            ad.setOperationType(BankOperationType.SALE);
            fillCommonFields(saleData, ad, response);
            return ad;
        }
    }

    private void fillCommonFields(SaleData sd, AuthorizationData ad, ZVTResponse response) {
        final Map<TransactionField, String> fields = response.getFields();
        ad.setStatus(response.isSuccessful());
        ad.setAmount(sd.getAmount());
        ad.setBankid(sd.getBankId());
        ad.setCurrencyCode(sd.getCurrencyCode());
        ad.setResponseCode(response.getResponseCode());
        ad.setDate(extractDate(fields));
        ad.setTerminalId(fields.get(TransactionField.TERMINAL_ID));
        ad.setRefNumber(fields.get(TransactionField.RECEIPT_NO));
        ad.setAuthCode(fields.get(TransactionField.TRACE));
        ad.setMessage(EncodingUtils.decodeHexAscii(fields.get(TransactionField.ADDITIONAL_TEXT)));

        final String pan = fields.get(TransactionField.PAN);
        if (pan != null) {
            final BankCard card = new BankCard();
            card.setCardNumber(BankUtils.maskCardNumber(pan));
            ad.setCard(card);
        }
    }

    private Date extractDate(Map<TransactionField, String> fields) {
        final String date = fields.get(TransactionField.DATE);
        final String time = fields.get(TransactionField.TIME);
        if (date == null || time == null) {
            return new Date();
        }
        final DateTimeFormatter df = new DateTimeFormatterBuilder()
                .appendPattern("MMddHHmmss")
                .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear()).toFormatter();
        return DateConverters.toDate(LocalDateTime.parse(date + time, df));
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        try (final ZVTSession zvtSession = connector.newSession()) {

            final ReversalCommand request = new ReversalCommand(password, reversalData.getRefNumber());
            ZVTResponse response = zvtSession.sendRequestWithLoginIfRequired(request, password);
            checkIsSuccessful(BankOperationType.REVERSAL, response);

            final AuthorizationData ad = new AuthorizationData();
            ad.setOperationType(BankOperationType.REVERSAL);
            fillCommonFields(reversalData, ad, response);
            return ad;
        }
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        try (final ZVTSession zvtSession = connector.newSession()) {

            final RefundCommand request = new RefundCommand(password, refundData.getAmount());
            ZVTResponse response = zvtSession.sendRequestWithLoginIfRequired(request, password);
            checkIsSuccessful(BankOperationType.REFUND, response);

            final AuthorizationData ad = new AuthorizationData();
            ad.setOperationType(BankOperationType.REFUND);
            fillCommonFields(refundData, ad, response);
            return ad;
        }
    }

    private void checkIsSuccessful(BankOperationType operationType, ZVTResponse response) throws BankAuthorizationException {
        checkIsSuccessful(operationType.name(), response);
    }

    private void checkIsSuccessful(String operationType, ZVTResponse response) throws BankAuthorizationException {
        if (!response.isSuccessful()) {
            final String errorMessage = Optional.ofNullable(response.getFields().get(TransactionField.ADDITIONAL_TEXT))
                    .map(EncodingUtils::decodeHexAscii)
                    .map(EncodingUtils::extractMessage)
                    .orElseGet(() -> ResBundleBankZVT.getRC(response.getResponseCode()));
            log.error("{} operation failed: RC={} ({})", operationType, response.getResponseCode(), errorMessage);
            throw new BankAuthorizationException(errorMessage);
        }
    }

    public void setTimeoutT3(String timeoutT3) {
        terminalConfig.setTimeoutT3(NumberUtils.toInt(timeoutT3, 0));
    }

    public void setTimeoutT4(String timeoutT4) {
        terminalConfig.setTimeoutT4(NumberUtils.toInt(timeoutT4, 0));
    }

    public void setPassword(String password) {
        terminalConfig.setPassword(StringUtils.trimToNull(password));
    }
}
