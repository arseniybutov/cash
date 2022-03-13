package ru.crystals.pos.bank.inpas.smartsale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.InpasConstants;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankOpenPortException;
import ru.crystals.pos.bank.inpas.smartsale.PrintData.PrintDataTags;
import ru.crystals.pos.bank.inpas.smartsale.serial.JsscSerialPortConnector;
import ru.crystals.pos.bank.inpas.smartsale.serial.RxTxSerialPortConnector;
import ru.crystals.pos.bank.inpas.smartsale.serial.SimpleSerialPortConnector;
import ru.crystals.pos.bank.inpas.smartsale.serial.TcpPortConnector;
import ru.crystals.pos.properties.PropertiesManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InpasBankServiceImpl extends AbstractBankPluginImpl {

    private static final String MODULE_NAME = "CASH_BANK";
    private static final String PLUGIN_NAME = "inpas";
    private static final String PAD_STRING = "0";
    private static final int SIZE_RRN = 12;
    private static final DateTimeFormatter SLIP_OPERATION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd\\HH:mm:ss");

    protected String terminalID = null;
    protected boolean bankPaysPrint = true;
    protected boolean cancelPossible = true;
    private boolean innerSlip = true;
    private int innerSlipCount = 1;
    /**
     * Эту statefull хрень надо выпилить
     */
    protected FieldCollection data;
    /**
     * При частичном возврате использовать частичную отмену (коррректировку) или
     * нет (использовать возврат)
     */
    protected boolean usePartialReversal = false;

    private String port = "COM1";
    private String baudRate = "9600";
    private String dataBits = "8";
    private String stopBits = "1";
    private String parity = "NONE";

    private InpasConnector connector;
    private TCPConnector tcpConnector = new TCPConnector();
    public static final Logger LOG = LoggerFactory.getLogger(InpasBankServiceImpl.class);

    @Override
    public void start() throws CashException {
        if (connector == null) {
            connector = new InpasConnector(createSerialPortConnector());
        }
        try {
            connector.open(port, baudRate, dataBits, stopBits, parity);
        } catch (Exception e) {
            LOG.error("", e);
            throw new BankOpenPortException(ResBundleBankInpas.getString("ERROR_OPEN_PORT"), e);
        }
    }

    private SimpleSerialPortConnector createSerialPortConnector() {
        final TerminalConfiguration terminalConfig = getTerminalConfiguration();
        if (terminalConfig.getConnectionType() == TerminalConfiguration.TerminalConnectionType.TCP) {
            final int tcpPort = terminalConfig.getTerminalTcpPort();
            final String terminalIp = terminalConfig.getTerminalIp();
            LOG.info("Using TCP connector {}:{}", terminalIp, tcpPort);
            return new TcpPortConnector(terminalIp, tcpPort);
        }

        final String serialPortConnectorType = BundleManager.get(PropertiesManager.class)
                .getProperty(MODULE_NAME, PLUGIN_NAME, "serialPortConnector", RxTxSerialPortConnector.ID);
        if (StringUtils.equalsIgnoreCase(serialPortConnectorType, JsscSerialPortConnector.ID)) {
            LOG.info("Using jssc serial port connector");
            return new JsscSerialPortConnector();
        } else if (StringUtils.equalsIgnoreCase(serialPortConnectorType, RxTxSerialPortConnector.ID)) {
            LOG.info("Using rxtx serial port connector");
            return new RxTxSerialPortConnector();
        }
        LOG.info("Using default (rxtx) serial port connector");
        return new RxTxSerialPortConnector();
    }

    @Override
    public synchronized boolean requestTerminalStateIfOffline() {
        try {
            createNewData();
            processTerminalResponse(executeCommand(InpasConstants.CHECK_STATE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected FieldCollection executeCommand(long operationCode) throws BankException {
        return executeCommand(operationCode, connector);
    }

    protected FieldCollection executeCommand(long operationCode, InpasConnector connector) throws BankException {
        if (data == null) {
            throw new BankException(ResBundleBankInpas.getString("ERROR_CONFIG"));
        }
        data.setCashDateTime(LocalDateTime.now());
        data.setTerminalId(terminalID);
        data.setOperationCode(operationCode);
        connector.sendPacket(data);
        return connector.readPacket();
    }

    @Override
    public synchronized void stop() {
        connector.close();
    }

    protected List<List<String>> generateBankSlip(FieldCollection result) {
        List<List<String>> slip = new ArrayList<>();
        List<String> rows = new ArrayList<>();
        String aid = null;
        String cardType = null;

        if (isInnerSlip()) {
            if (result.getPrintData() != null) {
                for (PrintData printData : result.getPrintData()) {
                    if (printData.getTag() == PrintDataTags.AID_TAG) {
                        aid = printData.getName() + " " + printData.getValue();
                    } else if (printData.getTag() == PrintDataTags.CARD_TYPE_TAG) {
                        cardType = printData.getName() + " " + printData.getValue();
                    }
                }
            }

            rows.add(ResBundleBankInpas.getString("BC_HEADER"));
            rows.add(ResBundleBankInpas.getString("BC_TERMINAL_ID") + result.getTerminalId());
            rows.add("");
            if (result.getOperationCode().equals(InpasConstants.SALE)) {
                rows.add(ResBundleBankInpas.getString("BC_SALE"));
            } else if (result.getOperationCode().equals(InpasConstants.REVERSAL)) {
                rows.add(ResBundleBankInpas.getString("BC_REVERSAL"));
            } else if (result.getOperationCode().equals(InpasConstants.REFUND)) {
                rows.add(ResBundleBankInpas.getString("BC_REFUND"));
            } else if (result.getOperationCode().equals(InpasConstants.DAILY_LOG)) {
                rows.add(ResBundleBankInpas.getString("BC_DAILYLOG"));
            }

            rows.add(ResBundleBankInpas.getString("BC_DATE_TIME") + result.getHostDateTime().format(SLIP_OPERATION_DATE_FORMAT));

            if (cardType != null) {
                rows.add(cardType);
            }

            if (result.getPAN() != null) {
                rows.add(ResBundleBankInpas.getString("BC_CARD_NUMBER") + BankUtils.maskCardNumber(result.getPAN()));
            }

            if (data.getCardEntryMode() != null) {
                switch (data.getCardEntryMode()) {
                    case 1: {
                        rows.add(ResBundleBankInpas.getString("BC_ENTRY_CARD_MANUAL"));
                        break;
                    }
                    case 2: {
                        rows.add(ResBundleBankInpas.getString("BC_ENTRY_CARD_READ_CASH"));
                        break;
                    }
                    default: {
                        rows.add(ResBundleBankInpas.getString("BC_ENTRY_CARD_READ_TERMINAL"));
                    }
                }
            }

            if (result.getRefNumber() != null) {
                rows.add(ResBundleBankInpas.getString("BC_REF_NUMBER") + result.getRefNumber());
            }

            if (result.getAuthCode() != null) {
                rows.add(ResBundleBankInpas.getString("BC_AUTH_CODE") + result.getAuthCode());
            }

            if (result.getMerchantId() != null) {
                rows.add(ResBundleBankInpas.getString("BC_MERCHANTID_ID") + result.getMerchantId());
            }

            if (result.getTextResponse() != null) {
                rows.add(ResBundleBankInpas.getString("BC_RESPONSE_TEXT") + result.getTextResponse().toUpperCase());
            }

            if (result.getStatus() != null) {
                rows.add(ResBundleBankInpas.getString("BC_RESULT_CODE") + result.getStatus());
            }

            if (aid != null) {
                rows.add(ResBundleBankInpas.getString("BC_AID") + aid);
            }

            if (result.getAdditionalAmount() != null) {
                rows.add(ResBundleBankInpas.getString("BC_AMOUNT") + BigDecimal.valueOf(result.getAdditionalAmount() / 100.0).setScale(2, RoundingMode.HALF_UP));
            } else if (result.getAmount() != null) {
                rows.add(ResBundleBankInpas.getString("BC_AMOUNT") + BigDecimal.valueOf(result.getAmount() / 100.0).setScale(2, RoundingMode.HALF_UP));
            }

            rows.add("");
            for (int i = 0; i < getInnerSlipCount(); i++) {
                slip.add(rows);
            }
        } else {
            if (result.getPrintData() != null) {
                generateNotInnerSlip(result, slip);
            }
        }

        return slip;
    }

    public static void generateNotInnerSlip(FieldCollection result, List<List<String>> slip) {
        for (PrintData printData : result.getPrintData()) {
            if (printData.getTag() == PrintDataTags.FIRST_SLIP_TAG || printData.getTag() == PrintDataTags.SECOND_SLIP_TAG) {
                List<String> slipItem = new ArrayList<>();
                Collections.addAll(slipItem, printData.getValue().split("\r\n"));
                slip.add(slipItem);
            }
        }
    }

    @Override
    public synchronized AuthorizationData sale(SaleData saleData) throws BankException {
        LOG.info("Sale");
        createNewData();
        data.setAmount(saleData.getAmount());
        data.setCurrencyCode(saleData.getCurrencyCode());
        data.setCashTransId(saleData.getCashTransId());
        return processBankOperation(InpasConstants.SALE);
    }

    protected AuthorizationData processBankOperation(long command) throws BankCommunicationException {
        try {
            return processBankOperationInner(command, connector);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"));
        }
    }

    protected AuthorizationData processBankOperationInner(long command, InpasConnector connector) throws Exception {
        FieldCollection fc = executeCommand(command, connector);
        while (InpasConstants.USER_INPUT_CODE.equals(fc.getOperationCode())) {
            LOG.debug("received operationCode 52, waiting for different data");
            fc = connector.readPacket();
        }
        AuthorizationData result = processTerminalResponseAuthorizationData(fc);

        result.setStatus(result.getResultCode() != null && ((result.getResultCode() == 1) || (result.getResultCode() == 17)));

        setAuthorizationDataMessage(result);

        setAthorizationDataSlips(result, fc);

        return result;
    }

    protected FieldCollection processTerminalResponse(FieldCollection fc) throws Exception {
        if (fc.getOperationCode() != InpasConstants.EXECUTE_USER_COMMAND) {
            return fc;
        }
        tcpConnector.fillData(fc, createNewData());
        return processTerminalResponse(executeCommand(InpasConstants.EXECUTE_USER_COMMAND));
    }

    protected AuthorizationData processTerminalResponseAuthorizationData(FieldCollection fc) throws Exception {
        return processTerminalResponse(fc).toAuthorizationData();
    }

    @Override
    public synchronized AuthorizationData reversal(ReversalData reversalData) throws BankException {
        LOG.info("Reversal");
        if (!cancelPossible) {
            throw new BankException(ResBundleBankInpas.getString("CANCEL_IMPOSSIBLE"));
        }
        createNewData();

        data.setAmount(reversalData.getAmount());
        long operationCode = InpasConstants.REVERSAL;
        LOG.debug("reversalData.getAmount() " + reversalData.getAmount());
        LOG.debug("reversalData.getOriginalSaleTransactionAmount() " + reversalData.getOriginalSaleTransactionAmount());

        if (reversalData.isPartial()) {
            if (usePartialReversal) {
                LOG.info("Using partial reversal");
                data.setAdditionalAmount(reversalData.getOriginalSaleTransactionAmount());
            } else {
                LOG.info("Using refund");
                operationCode = InpasConstants.REFUND;
            }
        }

        data.setCurrencyCode(reversalData.getCurrencyCode());
        data.setAuthCode(reversalData.getAuthCode());
        data.setRefNumber(formatRefNumber(reversalData.getRefNumber()));

        data.setCashTransId(reversalData.getCashTransId());
        return processBankOperation(operationCode);
    }

    protected String formatRefNumber(String refNumber) {
        return refNumber == null ? null : StringUtils.leftPad(refNumber, SIZE_RRN, PAD_STRING);
    }

    protected void setAthorizationDataSlips(AuthorizationData result, FieldCollection fc) {
        if (bankPaysPrint) {
            for (BankEvent listener : getListeners()) {
                listener.eventAuthorizationComplete(result);
            }
            List<List<String>> slips = this.generateBankSlip(fc);
            result.setSlips(slips);
        }
    }

    @Override
    public synchronized AuthorizationData refund(RefundData refundData) throws BankException {
        LOG.info("Refund");
        if (!cancelPossible) {
            throw new BankException(ResBundleBankInpas.getString("CANCEL_IMPOSSIBLE"));
        }

        createNewData();

        data.setAmount(refundData.getAmount());
        data.setCurrencyCode(refundData.getCurrencyCode());
        data.setAuthCode(refundData.getAuthCode());
        data.setRefNumber(formatRefNumber(refundData.getRefNumber()));
        data.setCashTransId(refundData.getCashTransId());

        return processBankOperation(InpasConstants.REFUND);
    }

    protected void setAuthorizationDataMessage(AuthorizationData ad) {
        if (StringUtils.isBlank(ad.getMessage()) && ad.getResultCode() != null
                && InpasConstants.MESSAGES_FOR_RESULT_CODES.containsKey(ad.getResultCode().intValue())) {
            ad.setMessage(ResBundleBankInpas.getString(InpasConstants.MESSAGES_FOR_RESULT_CODES.get(ad.getResultCode().intValue())));
        }
    }

    @Override
    public synchronized DailyLogData dailyLog(Long cashTransId) throws BankException {
        LOG.info("DailyLog");
        DailyLogData result = new DailyLogData();
        try {
            dailyLogInner(cashTransId, result, connector);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new BankCommunicationException(ResBundleBankInpas.getString("ERROR_COMMUNICATION"));
        }
        return result;
    }

    protected DailyLogData dailyLogInner(Long cashTransId, DailyLogData result, InpasConnector connector) throws Exception {
        createNewData();

        data.setCashTransId(cashTransId);

        FieldCollection fc = executeCommand(InpasConstants.DAILY_LOG, connector);
        while (InpasConstants.USER_INPUT_CODE.equals(fc.getOperationCode())) {
            LOG.debug("received operationCode 52, waiting for different data");
            fc = connector.readPacket();
        }
        fc = processTerminalResponse(fc);

        List<List<String>> slips = generateBankSlip(fc);
        if (!slips.isEmpty()) {
            // соберем все слипы в один
            result.setSlip(slips.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) {
        return new DailyLogData();
    }

    @Override
    public String getTerminalID() {
        return terminalID;
    }

    @Override
    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public void setBankPaysPrint(boolean bankPaysPrint) {
        this.bankPaysPrint = bankPaysPrint;
    }

    public void setCancelPossible(boolean cancelPossible) {
        this.cancelPossible = cancelPossible;
    }

    public String getPort() {
        return port;
    }

    @Override
    public void setPort(String port) {
        this.port = port;
    }

    public String getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(String baudRate) {
        this.baudRate = baudRate;
    }

    public String getDataBits() {
        return dataBits;
    }

    @Override
    public void setDataBits(String dataBits) {
        this.dataBits = dataBits;
    }

    public String getStopBits() {
        return stopBits;
    }

    @Override
    public void setStopBits(String stopBits) {
        this.stopBits = stopBits;
    }

    public String getParity() {
        return parity;
    }

    @Override
    public void setParity(String parity) {
        this.parity = parity;
    }

    public boolean isInnerSlip() {
        return innerSlip;
    }

    public void setInnerSlip(boolean innerSlip) {
        this.innerSlip = innerSlip;
    }

    public int getInnerSlipCount() {
        return innerSlipCount;
    }

    public void setInnerSlipCount(int innerSlipCount) {
        this.innerSlipCount = innerSlipCount;
    }

    @Override
    public boolean isUsePartialReversal() {
        return usePartialReversal;
    }

    @Override
    public void setUsePartialReversal(boolean usePartialCancel) {
        this.usePartialReversal = usePartialCancel;
    }

    public void setTimeoutT1(long timeoutT1) {
        connector.setOverallTimeOut(timeoutT1);
    }

    public void setTimeoutT2(long timeoutT2) {
        connector.setReadByteTimeOut(timeoutT2);
    }

    protected FieldCollection createNewData() {
        data = new FieldCollection();
        return data;
    }

}
