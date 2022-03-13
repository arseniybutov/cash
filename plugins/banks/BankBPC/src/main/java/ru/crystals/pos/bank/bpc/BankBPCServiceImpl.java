package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankDialogEvent;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.bpc.exceptions.BankBPCAutoReversalException;
import ru.crystals.pos.bank.bpc.exceptions.BankTimeoutException;
import ru.crystals.pos.bank.bpc.exceptions.DailyLogExpectedException;
import ru.crystals.pos.bank.bpc.serviceoperations.BPCServiceOperation;
import ru.crystals.pos.bank.bpc.serviceoperations.ServiceMenuOperation;
import ru.crystals.pos.bank.bpc.serviceoperations.TestHostOperation;
import ru.crystals.pos.bank.bpc.serviceoperations.TestPinpadOperation;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.property.Properties;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankBPCServiceImpl extends AbstractBankPluginImpl {
    protected static final String REVERSAL_OPERATION_MESSAGE_ID = "VOI";
    private static final Logger log = LoggerFactory.getLogger(BankBPCServiceImpl.class);
    protected static final List<String> INCORRECT_RESPONSE_CODES_FOR_SALE_REFUND_RESPONSE = Arrays.asList("RT", "TT");
    private static final int ERN_ZERO_COUNT = 10;
    protected static final String CORRECT_RESPONSE_VALUE = "00";
    protected static final String B_4_RESPONSE_CODE = "B4";
    protected static final String APPROVE_SUCCESS = "Y";
    protected static final String AUTO_REVERSAL_OPERATION_NEEDED_RESPONSE_CODE = "B9";
    private static final String DEFAULT_CASH_NUMBER = "01";
    private BPCProperties bpcProperties = new BPCProperties();
    private BPCConnector bpcConnector = new BPCConnector();
    private List<BPCServiceOperation> serviceOperations = new ArrayList<>();
    private DialogListener dialogListener = new DialogListener();
    private ExecutorService dialogListenerExecutor = Executors.newSingleThreadExecutor();
    private boolean useAutomaticHostTest;
    /**
     * Включать ли RRN поле в запрос при возврате
     */
    private boolean includeRRNForRefund;
    private String cashNumber;
    private String connectorServiceAddress = "localhost";
    private String responseCharset = "cp1251";
    private String dialogCharset = "cp866";

    @Override
    public void start() throws CashException {
        try {
            bpcProperties.load();
        } catch (IOException e) {
            throw new BankConfigException(e.getMessage());
        }
        bpcConnector.setTcpAddress(connectorServiceAddress);
        initServiceOperations();
        bpcConnector.connect();
        dialogListener.start();
        dialogListenerExecutor.execute(dialogListener);
    }

    private void initServiceOperations() {
        serviceOperations.add(new TestHostOperation());
        serviceOperations.add(new TestPinpadOperation());
        serviceOperations.add(new ServiceMenuOperation());
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        dialogListener.addListeners(getListeners());
        dialogListener.removeServiceOperationListener();
        checkDailyLogExpected();
        AuthorizationData ad;
        try {
            ad = innerSale(saleData);
        } catch (BankBPCAutoReversalException e) {
            processAutoReversalOperation(e.getRefNumber());
            ad = innerSale(saleData);
        }
        dialogListener.removeBankListeners();
        return ad;
    }

    protected AuthorizationData innerSale(SaleData saleData) throws BankException {
        Map<Integer, DataByte> response = bpcConnector.makeTransaction(RequestFactory.createSaleRequest(saleData, getCashNumber(), getERN()));
        if (response.isEmpty()) {
            dialogListener.closeDialog();
            throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        }
        AuthorizationData ad = processSaleRefundResponse(saleData, response);
        ad.setOperationType(BankOperationType.SALE);
        bpcProperties.increaseERN();
        return ad;
    }

    protected String getERN() {
        return StringUtils.leftPad(bpcProperties.getERN(), ERN_ZERO_COUNT, '0');
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        if (reversalData.isPartial()) {
            return refund(reversalData);
        }
        dialogListener.addListeners(getListeners());
        dialogListener.removeServiceOperationListener();
        checkDailyLogExpected();
        Map<Integer, DataByte> response;
        try {
            response = bpcConnector.makeTransaction(RequestFactory.createReversalRequest(reversalData, getCashNumber()));
        } catch (Exception e) {
            AuthorizationData result = createAuthDataOnFault(reversalData);
            result.setOperationType(BankOperationType.REVERSAL);
            result.setMessage(e.getMessage() == null ? "" : e.getMessage());
            throw new BankException(result);
        } finally {
            dialogListener.removeBankListeners();
        }
        if (response.isEmpty()) {
            dialogListener.closeDialog();
            throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        }
        AuthorizationData ad = processReversalResponse(reversalData, response);
        ad.setOperationType(BankOperationType.REVERSAL);
        return ad;
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        dialogListener.removeServiceOperationListener();
        dialogListener.addListeners(getListeners());
        checkDailyLogExpected();
        AuthorizationData ad;
        try {
            ad = innerRefund(refundData);
        } catch (BankBPCAutoReversalException e) {
            processAutoReversalOperation(e.getRefNumber());
            ad = innerRefund(refundData);
        }
        dialogListener.removeBankListeners();
        return ad;
    }

    protected AuthorizationData innerRefund(RefundData refundData) throws BankException {
        Map<Integer, DataByte> response = bpcConnector
                .makeTransaction(RequestFactory.createRefundRequest(refundData, getCashNumber(), getERN(), isIncludeRRNForRefund()));
        if (response.isEmpty()) {
            dialogListener.closeDialog();
            throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        }
        AuthorizationData ad = processSaleRefundResponse(refundData, response);
        ad.setOperationType(BankOperationType.REFUND);
        bpcProperties.increaseERN();
        return ad;
    }

    protected void processAutoReversalOperation(String ern) throws BankException {
        Map<Integer, DataByte> response = bpcConnector.makeTransaction(RequestFactory.createAutoReversalRequest(getCashNumber(), ern));
        if (response.isEmpty()) {
            dialogListener.closeDialog();
            throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        }
        if (response.get(Tag.Output.RESPONSE_CODE).toString().equals(CORRECT_RESPONSE_VALUE) ||
                response.get(Tag.Output.RESPONSE_CODE).toString().equals(B_4_RESPONSE_CODE)) {
            bpcProperties.increaseERN();
        } else {
            throw new BankException(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
        }
    }

    protected AuthorizationData processSaleRefundResponse(SaleData saleData, Map<Integer, DataByte> response) throws BankException {
        AuthorizationData authorizationData = createAuthDataWithCommonParams(saleData, response);
        if (INCORRECT_RESPONSE_CODES_FOR_SALE_REFUND_RESPONSE.contains(response.get(Tag.Output.RESPONSE_CODE).toString())) {
            throw new BankException(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
        }
        boolean needToAutoReversal = response.get(Tag.Output.RESPONSE_CODE).toString().equals(AUTO_REVERSAL_OPERATION_NEEDED_RESPONSE_CODE);
        authorizationData.setStatus(response.get(Tag.Output.APPROVE).toString().equals(APPROVE_SUCCESS));
        if (response.containsKey(Tag.Output.RESPONSE_CODE)) {
            authorizationData.setResponseCode(response.get(Tag.Output.RESPONSE_CODE).toString());
        }
        if (needToAutoReversal) {
            throw new BankBPCAutoReversalException(authorizationData.getRefNumber());
        }
        return authorizationData;
    }

    protected AuthorizationData processReversalResponse(SaleData saleData, Map<Integer, DataByte> response) throws BankException {
        AuthorizationData authorizationData = createAuthDataWithCommonParams(saleData, response);
        boolean isApprove = response.get(Tag.Output.APPROVE).toString().equals(APPROVE_SUCCESS);
        if (response.containsKey(Tag.Output.RESPONSE_CODE)) {
            authorizationData.setResponseCode(response.get(Tag.Output.RESPONSE_CODE).toString());
            authorizationData.setStatus(CORRECT_RESPONSE_VALUE.equals(authorizationData.getResponseCode()));
        }
        if (!isApprove) {
            if (authorizationData.getResponseCode().equals(B_4_RESPONSE_CODE)) {
                authorizationData.setStatus(processJRNOperation());
            } else {
                authorizationData.setStatus(false);
                if (StringUtils.isEmpty(authorizationData.getMessage())) {
                    authorizationData.setMessage(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
                }
            }
        }
        return authorizationData;
    }

    private AuthorizationData createAuthDataOnFault(SaleData saleData) {
        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setAmount(saleData.getAmount());
        authorizationData.setPrintNegativeSlip(true);
        authorizationData.setCard(saleData.getCard() == null ? new BankCard() : saleData.getCard());
        authorizationData.setStatus(false);
        authorizationData.setDate(new Date());
        return authorizationData;
    }

    private AuthorizationData createAuthDataWithCommonParams(SaleData saleData, Map<Integer, DataByte> response) {
        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setAmount(saleData.getAmount());
        authorizationData.setPrintNegativeSlip(true);
        authorizationData.setCard(saleData.getCard() == null ? new BankCard() : saleData.getCard());
        if (response.containsKey(Tag.Output.ERN)) {
            authorizationData.setRefNumber(response.get(Tag.Output.ERN).toString());
        }
        if (response.containsKey(Tag.Output.RRN)) {
            Map<String, String> data = new HashMap<>();
            data.put(RequestFactory.RRN, response.get(Tag.Output.RRN).toString());
            authorizationData.setExtendedData(data);
        }
        if (response.containsKey(Tag.Output.PAN)) {
            authorizationData.getCard().setCardNumber(response.get(Tag.Output.PAN).toString());
        }
        if (response.containsKey(Tag.Output.VISUAL_HOST_RESPONSE)) {
            authorizationData.setMessage(response.get(Tag.Output.VISUAL_HOST_RESPONSE).toString());
        }
        if (response.containsKey(Tag.Output.RECEIPT)) {
            String rawSlip = response.get(Tag.Output.RECEIPT).toString();
            authorizationData.setSlips(Parser.parseSlips(rawSlip));
        }
        authorizationData.setDate(new Date());
        return authorizationData;
    }

    protected boolean processJRNOperation() throws BankException {
        Map<Integer, DataByte> response = bpcConnector.makeTransaction(RequestFactory.createJRNOperationRequest(getCashNumber(), getERN()));
        if (response.isEmpty()) {
            dialogListener.closeDialog();
            throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        }
        return response.get(Tag.Output.RESPONSE_CODE).toString().equals(CORRECT_RESPONSE_VALUE) &&
                response.get(Tag.Input.MESSAGE_ID).toString().equals(REVERSAL_OPERATION_MESSAGE_ID);
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        dialogListener.removeServiceOperationListener();
        DailyLogData dailyLogData;
        try {
            dailyLogData = innerDailyLog();
        } catch (BankBPCAutoReversalException e) {
            processAutoReversalOperation(e.getRefNumber());
            dailyLogData = innerDailyLog();
        }
        return dailyLogData;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) throws BankException {
        return new DailyLogData();
    }

    private String decodeResponseMsg(DataByte msg) {
        return new String(msg.getData(), Charset.forName(getResponseCharset()));
    }

    private DailyLogData innerDailyLog() throws BankException {
        try {
            DailyLogData data = new DailyLogData();
            Map<Integer, DataByte> response = bpcConnector.makeTransaction(RequestFactory.createDailyLogRequest(getCashNumber(), getERN()));
            if (response.isEmpty()) {
                dialogListener.closeDialog();
                throw new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
            }
            if (response.containsKey(Tag.Output.RESPONSE_CODE)) {
                if (response.get(Tag.Output.RESPONSE_CODE).toString().equals(AUTO_REVERSAL_OPERATION_NEEDED_RESPONSE_CODE)) {
                    throw new BankBPCAutoReversalException(bpcProperties.getERN());
                }
                if (!CORRECT_RESPONSE_VALUE.equals(response.get(Tag.Output.RESPONSE_CODE).toString())) {
                    if (response.containsKey(Tag.Output.VISUAL_HOST_RESPONSE)) {
                        throw new BankAuthorizationException(decodeResponseMsg(response.get(Tag.Output.VISUAL_HOST_RESPONSE)) + " (" + response.get(Tag.Output.RESPONSE_CODE) + ')');
                    } else {
                        throw new BankException(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
                    }
                }
            } else {
                throw new BankException(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
            }
            if (response.containsKey(Tag.Output.RECEIPT)) {
                String rawSlip = response.get(Tag.Output.RECEIPT).toString();
                List<List<String>> slips = Parser.parseSlips(rawSlip);
                data.setSlip(slips.isEmpty() ? new ArrayList<>() : slips.get(0));
            }
            bpcProperties.setDailyLogExpected(false);
            return data;
        } catch (BankException e) {
            bpcProperties.setDailyLogExpected(true);
            throw e;
        }
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        BPCServiceOperation bpcServiceOperation = (BPCServiceOperation) operation;
        Map<Integer, DataByte> response = bpcConnector.makeTransaction(bpcServiceOperation.createRequest(getCashNumber(), getERN()));
        if (response.isEmpty()) {
            BankTimeoutException bankTimeoutException = new BankTimeoutException(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
            bankTimeoutException.setErrorType(CashErrorType.NOT_CRITICAL_ERROR_WITHOUT_REPEAT);
            dialogListener.closeDialog();
            throw bankTimeoutException;
        }
        if (response.containsKey(Tag.Output.RESPONSE_CODE)) {
            if (!CORRECT_RESPONSE_VALUE.equals(response.get(Tag.Output.RESPONSE_CODE).toString())) {
                if (!"B5".equals(response.get(Tag.Output.RESPONSE_CODE).toString()) && !"BB".equals(response.get(Tag.Output.RESPONSE_CODE).toString())) {
                    throw new BankAuthorizationException(decodeResponseMsg(response.get(Tag.Output.VISUAL_HOST_RESPONSE)));
                } else {
                    BankAuthorizationException bankAuthorizationException =
                            new BankAuthorizationException(decodeResponseMsg(response.get(Tag.Output.VISUAL_HOST_RESPONSE)));
                    bankAuthorizationException.setErrorType(CashErrorType.AUTO_FIXE);
                    throw bankAuthorizationException;
                }
            }
        } else {
            throw new BankException(ResBundleBankBPC.getString("OPERATION_NOT_SUCCESSFUL"));
        }
        if (response.containsKey(Tag.Output.RECEIPT)) {
            String rawSlip = response.get(Tag.Output.RECEIPT).toString();
            return Parser.parseSlips(rawSlip);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean requestTerminalStateIfOnline() {
        return isHostOnline();
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        return isHostOnline();
    }

    boolean isHostOnline() {
        if (useAutomaticHostTest) {
            dialogListener.removeBankListeners();
            dialogListener.removeServiceOperationListener();
            try {
                processServiceOperation(new TestHostOperation());
                return true;
            } catch (BankException e) {
                log.error("", e);
                return false;
            }
        } else {
            return true;
        }
    }

    private void checkDailyLogExpected() throws BankException {
        if (bpcProperties.isDailyLogExpected()) {
            throw new DailyLogExpectedException();
        }
    }

    @Override
    public List<BPCServiceOperation> getAvailableServiceOperations() {
        return serviceOperations;
    }

    public void setProcessing(String processingCatalog) {
        bpcConnector.setProcessingCatalog(processingCatalog);
    }

    public void setTcpAddress(String tcpAddress) {
        bpcConnector.setTcpAddress(tcpAddress);
    }

    public void setTcpPort(int tcpPort) {
        bpcConnector.setTcpPort(tcpPort);
    }

    @Override
    public void addDialogListener(BankDialogEvent bankDialogListener) {
        dialogListener.addServiceOperationListener(bankDialogListener);
    }

    @Override
    public void sendDialogResponse(BankDialogType dialogType, String response) {
        try {
            dialogListener.answer(dialogType, response);
        } catch (IOException e) {
            log.error("Failed to send dialog response", e);
        }
    }

    @Override
    public void closeDialog() {
        dialogListener.closeDialog();
    }

    public void setUseAutomaticHostTest(boolean useAutomaticHostTest) {
        this.useAutomaticHostTest = useAutomaticHostTest;
    }

    private String getCashNumber() {
        if (cashNumber == null) {
            Properties properties = BundleManager.get(Properties.class);
            if (properties != null) {
                cashNumber = String.valueOf(properties.getCashNumber());
            }
        }
        if (cashNumber == null) {
            log.error("NULL_CASH_NUMBER, CASH NUMBER MUST BE SET");
            cashNumber = DEFAULT_CASH_NUMBER;
        }
        return cashNumber;
    }

    public void setSlipsSeparator(int slipsSeparator) {
        Parser.setSlipsSeparator(slipsSeparator);
    }

    public String getConnectorServiceAddress() {
        return connectorServiceAddress;
    }

    public void setConnectorServiceAddress(String connectorServiceAddress) {
        this.connectorServiceAddress = connectorServiceAddress;
    }

    public boolean isIncludeRRNForRefund() {
        return includeRRNForRefund;
    }

    public void setIncludeRRNForRefund(boolean includeRRNForRefund) {
        this.includeRRNForRefund = includeRRNForRefund;
    }

    public String getResponseCharset() {
        return responseCharset;
    }

    public void setResponseCharset(String responseCharset) {
        this.responseCharset = responseCharset;
    }

    public String getDialogCharset() {
        return dialogCharset;
    }

    public void setDialogCharset(String dialogCharset) {
        dialogListener.setDialogCharset(dialogCharset);
        this.dialogCharset = dialogCharset;
    }
}
