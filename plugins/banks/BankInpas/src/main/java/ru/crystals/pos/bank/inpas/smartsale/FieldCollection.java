package ru.crystals.pos.bank.inpas.smartsale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.InpasConstants;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.inpas.smartsale.PrintData.PrintDataTags;
import ru.crystals.utils.time.DateConverters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldCollection {
    private static final DateTimeFormatter EXPIRY_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyMM")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter();
    private static final DateTimeFormatter OPERATION_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String CHARSET = "cp1251";
    private static final Logger logger = LoggerFactory.getLogger(FieldCollection.class);
    /**
     * Режим выполнения команды
     */
    private Byte commandMode;
    /**
     * Режим выполнения команды 2
     */
    private Long commandMode2;
    /**
     * Статус выполнения команды
     */
    private Long commandExecutionStatus;
    /**
     * Параметры соединения
     */
    private byte[] tcpParams;
    /**
     * Сумма операции
     */
    private Long amount;
    /**
     * Дополнительная сумма операции
     */
    private Long additionalAmount;
    /**
     * Код валюты
     */
    private Long currencyCode;
    /**
     * Дата и время операции на хосте
     */
    private LocalDateTime hostDateTime;
    /**
     * Способ ввода карты (3 - считана на пинпаде
     */
    private Byte cardEntryMode = (byte) 3;
    /**
     * Номер карты
     */
    private String PAN;
    /**
     * Срок действия карты
     */
    private LocalDate cardExpiryDate;
    /**
     * Данные второй дорожки
     */
    private String track2;
    /**
     * Код авторизации
     */
    private String authCode;
    /**
     * Номер ссылки на транзакцию
     */
    private String refNumber;
    /**
     * Код ответа от хоста
     */
    private String responseCodeHost;
    /**
     * Дата и время операции на стороне внешнего устройства
     */
    private LocalDateTime cashDateTime;
    /**
     * Идентификатор транзакции в комуникационном сервере
     */
    private Long hostTransId;
    /**
     * Код операции
     */
    private Long operationCode;
    /**
     * Уникальный номер транзакции на стороне внешнего устройства
     */
    private Long cashTransId;
    /**
     * Идентификатор фнешнего устройства
     */
    private String terminalId;
    /**
     * Идентификатор продавца
     */
    private String merchantId;
    /**
     * Статус порведения транзакции
     */
    private Long status;
    /**
     * Данные для печати на чеке
     */
    private List<PrintData> printData = new ArrayList<>();
    /**
     * Данные о типе платежной системы (Visa, MasterCard etc)
     */
    private String cardType = "";
    /**
     * Дополнительные данные ответа
     */
    private String textResponse;
    private static Map<Long, BankOperationType> operationTypeMap = new HashMap<>();

    static {
        operationTypeMap.put(InpasConstants.SALE, BankOperationType.SALE);
        operationTypeMap.put(InpasConstants.REFUND, BankOperationType.REFUND);
        operationTypeMap.put(InpasConstants.REVERSAL, BankOperationType.REVERSAL);
    }

    public FieldCollection() {

    }

    public FieldCollection(byte[] data, int offset, int length) throws UnsupportedEncodingException {
        int ptr = 0;

        while (ptr < Math.min(data.length, length)) {
            byte fieldNum = data[offset + ptr++];
            int len = (data[offset + ptr++] & 0xFF);
            len |= ((data[offset + ptr++] << 8) & 0xFF00);
            byte[] dest = new byte[len];
            System.arraycopy(data, offset + ptr, dest, 0, len);
            String fieldData = new String(dest, CHARSET);
            ptr += len;

            switch (fieldNum) {
                case 0: {
                    this.amount = Long.parseLong(fieldData);
                    break;
                }
                case 1: {
                    this.additionalAmount = Long.parseLong(fieldData);
                    break;
                }
                case 4: {
                    this.currencyCode = Long.parseLong(fieldData);
                    break;
                }
                case 6: {
                    this.hostDateTime = LocalDateTime.parse(fieldData, OPERATION_DATE_TIME_FORMAT);
                    break;
                }
                case 10: {
                    this.PAN = fieldData;
                    break;
                }
                case 11: {
                    this.cardExpiryDate = LocalDate.parse(fieldData, EXPIRY_DATE_FORMAT).with(TemporalAdjusters.lastDayOfMonth());
                    break;
                }
                case 13: {
                    this.authCode = fieldData;
                    break;
                }
                case 14: {
                    this.refNumber = fieldData;
                    break;
                }
                case 15: {
                    this.responseCodeHost = fieldData;
                    break;
                }
                case 19: {
                    this.textResponse = fieldData;
                    break;
                }
                case 21: {
                    this.cashDateTime = LocalDateTime.parse(fieldData, OPERATION_DATE_TIME_FORMAT);
                    break;
                }
                case 23: {
                    this.hostTransId = Long.parseLong(fieldData);
                    break;
                }
                case 25: {
                    this.operationCode = Long.parseLong(fieldData);
                    break;
                }
                case 26: {
                    this.cashTransId = Long.parseLong(fieldData);
                    break;
                }
                case 27: {
                    this.terminalId = fieldData;
                    break;
                }
                case 28: {
                    this.merchantId = fieldData;
                    break;
                }
                case 39: {
                    this.status = Long.parseLong(fieldData);
                    break;
                }
                case 64: {
                    this.commandMode = Byte.parseByte(fieldData);
                    break;
                }
                case 65: {
                    this.commandMode2 = Long.parseLong(fieldData);
                    break;
                }
                case 67: {
                    this.commandExecutionStatus = Long.parseLong(fieldData);
                    break;
                }
                case 70: {
                    this.tcpParams = dest;
                    break;
                }
                case 90: {
                    String[] rows = fieldData.split("~");
                    for (String row : rows) {
                        String[] rowItems = row.split("\\^");
                        if (rowItems.length == 3) {
                            PrintData pd = new PrintData();
                            pd.setTag(PrintDataTags.getPrintDataTagsByTag(rowItems[0]));
                            pd.setName(rowItems[1]);
                            pd.setValue(rowItems[2]);
                            if (pd.getTag().equals(PrintDataTags.CARD_TYPE_TAG)) {
                                cardType = pd.getValue();
                            }
                            this.getPrintData().add(pd);
                        }
                    }
                    break;
                }
                default:
                    // do nothing
                    break;
            }
        }
    }

    private void writeField(int position, String field, ByteArrayOutputStream out) throws IOException {
        if (field != null) {
            writeField(position, field.getBytes(), out);
        }
    }

    private void writeField(int position, byte[] field, ByteArrayOutputStream out) throws IOException {
        if (field != null) {
            out.write((byte) position);
            out.write((byte) field.length);
            out.write((byte) (field.length >> 8));
            out.write(field);
        }
    }

    private void writeField(int position, Long field, ByteArrayOutputStream out) throws IOException {
        if (field != null) {
            writeField(position, Long.toString(field), out);
        }
    }

    private void writeField(int position, Byte field, ByteArrayOutputStream out) throws IOException {
        if (field != null) {
            writeField(position, Byte.toString(field), out);
        }
    }

    private void writeField(int position, TemporalAccessor field, DateTimeFormatter dateFormat, ByteArrayOutputStream out) throws IOException {
        if (field != null) {
            writeField(position, dateFormat.format(field), out);
        }
    }

    public byte[] toArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeField(0, this.amount, out);
        writeField(1, this.additionalAmount, out);
        writeField(4, this.currencyCode, out);
        writeField(8, this.cardEntryMode, out);
        writeField(10, this.PAN, out);
        writeField(11, this.cardExpiryDate, EXPIRY_DATE_FORMAT, out);
        writeField(12, this.track2, out);
        writeField(13, this.authCode, out);
        writeField(14, this.refNumber, out);
        writeField(21, this.cashDateTime, OPERATION_DATE_TIME_FORMAT, out);
        writeField(25, this.operationCode, out);
        writeField(26, this.cashTransId, out);
        writeField(27, this.terminalId, out);
        writeField(39, this.status, out);
        writeField(64, this.commandMode, out);
        if (commandMode2 != null) {
            writeField(65, new DecimalFormat("00000").format(this.commandMode2), out);
        }
        writeField(67, this.commandExecutionStatus, out);
        writeField(70, this.tcpParams, out);
        out.flush();
        return out.toByteArray();
    }

    public AuthorizationData toAuthorizationData() {
        AuthorizationData result = new AuthorizationData();
        result.setAmount(amount);

        result.setCurrencyCode(numericToAlphaCode(currencyCode));
        result.setDate(DateConverters.toDate(hostDateTime));

        if (PAN != null) {
            BankCard card = new BankCard();
            card.setCardNumber(BankUtils.maskCardNumber(PAN));
            card.setCardType(cardType);
            result.setCard(card);
        }

        result.setAuthCode(authCode);
        result.setRefNumber(refNumber);
        result.setResponseCode(responseCodeHost);
        result.setMessage(textResponse);
        result.setHostTransId(hostTransId);
        result.setOperationCode(operationCode);
        result.setCashTransId(cashTransId);
        result.setTerminalId(terminalId);
        result.setMerchantId(merchantId);
        result.setResultCode(status);
        result.setStatus(status != null && status == 1);
        result.setOperationType(operationTypeMap.get(operationCode));

        return result;
    }

    private String numericToAlphaCode(Long currencyCode) {
        return InpasCurrencies.nameFromCode(currencyCode);
    }

    private Long alphaToNumericCode(String currencyCode) {
        return InpasCurrencies.codeFromName(currencyCode);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getClass().getSimpleName()).append("\n");

        if (amount != null) {
            result.append("[0] amount=").append(amount).append("\n");
        }
        if (additionalAmount != null) {
            result.append("[1] additionalAmount=").append(additionalAmount).append("\n");
        }
        if (currencyCode != null) {
            result.append("[4] currencyCode=").append(currencyCode).append("\n");
        }
        if (hostDateTime != null) {
            result.append("[6] hostDateTime=").append(hostDateTime).append("\n");
        }
        if (cardEntryMode != null) {
            result.append("[8] cardEntryMode=").append(cardEntryMode).append("\n");
        }
        if (PAN != null) {
            result.append("[10] PAN=").append(BankUtils.maskCardNumber(PAN)).append("\n");
        }
        if (cardExpiryDate != null) {
            result.append("[11] cardExpiryDate=").append(cardExpiryDate).append("\n");
        }
        if (track2 != null) {
            result.append("[12] track2=").append(BankUtils.maskCardNumber(track2)).append("\n");
        }
        if (authCode != null) {
            result.append("[13] authCode=").append(authCode).append("\n");
        }
        if (refNumber != null) {
            result.append("[14] refNumber=").append(refNumber).append("\n");
        }
        if (responseCodeHost != null) {
            result.append("[15] responseCodeHost=").append(responseCodeHost).append("\n");
        }
        if (textResponse != null) {
            result.append("[19] textResponse=").append(textResponse).append("\n");
        }
        if (cashDateTime != null) {
            result.append("[21] cashDateTime=").append(cashDateTime).append("\n");
        }
        if (hostTransId != null) {
            result.append("[23] hostTransId=").append(hostTransId).append("\n");
        }
        if (operationCode != null) {
            result.append("[25] operationCode=").append(operationCode).append("\n");
        }
        if (cashTransId != null) {
            result.append("[26] cashTransId=").append(cashTransId).append("\n");
        }
        if (terminalId != null) {
            result.append("[27] terminalId=").append(terminalId).append("\n");
        }
        if (merchantId != null) {
            result.append("[28] merchantId=").append(merchantId).append("\n");
        }
        if (status != null) {
            result.append("[39] status=").append(status).append("\n");
        }
        if (printData != null) {
            for (PrintData row : printData) {
                result.append("[90] printData=").append(row.getTag())
                        .append(" ").append(row.getName())
                        .append(" ").append(row.getValue())
                        .append("\n");
            }
        }
        if (commandMode != null) {
            result.append("[64] commandMode = ").append(commandMode).append("\n");
        }
        if (commandMode2 != null) {
            result.append("[65] commandMode2 = ").append(commandMode2).append("\n");
        }
        if (tcpParams != null) {
            result.append("[70] tcpParams = ").append(getTcpString()).append("\n");
        }
        if (commandExecutionStatus != null) {
            result.append("[67] commandExecutionStatus = ").append(commandExecutionStatus).append("\n");
        }

        return result.toString();
    }

    public String getCardType() {
        return cardType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = alphaToNumericCode(currencyCode);
    }

    public LocalDateTime getHostDateTime() {
        return hostDateTime;
    }

    public void setHostDateTime(LocalDateTime hostDateTime) {
        this.hostDateTime = hostDateTime;
    }

    public Byte getCardEntryMode() {
        return cardEntryMode;
    }

    public void setCardEntryMode(Byte cardEntryMode) {
        this.cardEntryMode = cardEntryMode;
    }

    public String getPAN() {
        return PAN;
    }

    public void setPAN(String pAN) {
        PAN = pAN;
    }

    public LocalDate getCardExpiryDate() {
        return cardExpiryDate;
    }

    public void setCardExpiryDate(LocalDate cardExpiryDate) {
        this.cardExpiryDate = cardExpiryDate;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getResponseCodeHost() {
        return responseCodeHost;
    }

    public void setResponseCodeHost(String responseCodeHost) {
        this.responseCodeHost = responseCodeHost;
    }

    public LocalDateTime getCashDateTime() {
        return cashDateTime;
    }

    public void setCashDateTime(LocalDateTime cashDateTime) {
        this.cashDateTime = cashDateTime;
    }

    public Long getHostTransId() {
        return hostTransId;
    }

    public void setHostTransId(Long hostTransId) {
        this.hostTransId = hostTransId;
    }

    public Long getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(Long operationCode) {
        this.operationCode = operationCode;
    }

    public Long getCashTransId() {
        return cashTransId;
    }

    public void setCashTransId(Long cashTransId) {
        this.cashTransId = cashTransId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public List<PrintData> getPrintData() {
        return printData;
    }

    public void setPrintData(List<PrintData> printData) {
        this.printData = printData;
    }

    public Long getAdditionalAmount() {
        return additionalAmount;
    }

    public void setAdditionalAmount(Long additionalAmount) {
        this.additionalAmount = additionalAmount;
    }

    public String getTextResponse() {
        return textResponse;
    }

    public void setTextResponse(String textResponse) {
        this.textResponse = textResponse;
    }

    public Byte getCommandMode() {
        return commandMode;
    }

    public Long getCommandMode2() {
        return commandMode2;
    }

    public Long getCommandExecutionStatus() {
        return commandExecutionStatus;
    }

    public void setCommandMode(Byte commandMode) {
        this.commandMode = commandMode;
    }

    public void setCommandMode2(Long commandMode2) {
        this.commandMode2 = commandMode2;
    }

    public void setCommandExecutionStatus(Long commandExecutionStatus) {
        this.commandExecutionStatus = commandExecutionStatus;
    }

    public byte[] getTcpParams() {
        return tcpParams;
    }

    public void setTcpParams(byte[] tcpParams) {
        this.tcpParams = tcpParams;
    }

    public String getTcpString() {
        try {
            if (tcpParams != null) {
                return new String(tcpParams, CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }
        return "";
    }
}
