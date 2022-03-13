package ru.crystals.pos.bank.ucs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.exceptions.NoPreviousTransactionWithSuchRefNumberException;
import ru.crystals.pos.bank.ucs.messages.requests.CreditRequest;
import ru.crystals.pos.bank.ucs.messages.requests.ReversalRequest;
import ru.crystals.pos.bank.ucs.messages.requests.SaleRequest;
import ru.crystals.pos.bank.ucs.serviceoperations.GetFullReportOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.GetShortReportOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.GetSlipCopyOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.UCSServiceOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BankUcsEftpos extends AbstractBankPluginImpl {
    private static Logger log = LoggerFactory.getLogger(BankUcsEftpos.class);
    private EftposConnector connector = new EftposConnector();
    private TerminalDelay terminalDelay = new TerminalDelay();
    private RequestManager requestManager = new RequestManager(terminalDelay);
    private TerminalMessageListener messageListener = new BankUcsEftposMessageListener();
    private List<UCSServiceOperation> serviceOperations = new ArrayList<>();
    private LastOperation lastOperation = new LastOperation();

    @Override
    public void start() {
        TerminalConfiguration terminalConfiguration = getTerminalConfiguration();
        connector.setTerminalConfiguration(terminalConfiguration);
        requestManager.setTerminalID(getTerminalConfiguration().getTerminalID());
        if (terminalConfiguration.isUseTerminalIDAutoDetection()) {
            log.debug("Using terminal ID auto detection");
            requestManager.setTerminalID(null);
        }
        try {
            if (terminalConfiguration.getConnectionType() == TerminalConfiguration.TerminalConnectionType.TCP) {
                log.info("Connecting to terminal {}:{}", terminalConfiguration.getTerminalIp(), terminalConfiguration.getTerminalTcpPort());
            } else {
                log.info("Connecting to terminal {}, {}", terminalConfiguration.getPort(), terminalConfiguration.getBaudRate());
            }
            connector.init();
            log.info("Connection complete");
            messageListener.addAll(getListeners());
            requestManager.setTerminalMessageListener(messageListener);
            requestManager.setConnector(connector);
        } catch (BankException e) {
            log.info("Start UCS plugin failed", e);
        }
        initServiceOperations();
    }

    private void initServiceOperations() {
        serviceOperations.add(new GetSlipCopyOperation());
        serviceOperations.add(new GetShortReportOperation());
        serviceOperations.add(new GetFullReportOperation());
    }

    @Override
    public void stop() {
        connector.close();
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        try {
            log.info("Executing login with terminalID {}", StringUtils.defaultString(getTerminalID(), "0000000000 (terminal ID auto detection)"));
            requestManager.login();
            log.info("Login successful");
            return true;
        } catch (BankException e) {
            log.error("Login failed", e);
        }
        return false;
    }

    @Override
    public boolean requestTerminalStateIfOnline() {
        return true;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        log.info("Executing finalize day totals");
        DailyLogData result = requestManager.dailyLog();
        log.info("dailyLog complete");
        lastOperation.clear();
        return result;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) {
        log.info("Executing day report without finalize");
        lastOperation.clear();
        return new DailyLogData();
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        log.info("Executing sale");
        AuthorizationData result = requestManager.makeTransaction(new SaleRequest(saleData));
        result.setOperationType(BankOperationType.SALE);
        result.setCashTransId(saleData.getCashTransId());
        log.info("Sale complete");
        saveLastOperation(result);
        return result;
    }

    private void saveLastOperation(AuthorizationData authorizationData) throws BankException {
        try {
            if (authorizationData.isStatus()) {
                lastOperation.saveLastSale(authorizationData);
            }
        } catch (IOException e) {
            throw new BankException(e);
        }
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        log.info("URN :{}  SUM: {}", reversalData.getRefNumber(), reversalData.getAmount());
        AuthorizationData authorizationData = reversalData.isPartial() ? partialReversal(reversalData) : fullReversal(reversalData);
        saveLastOperation(authorizationData);
        return authorizationData;
    }

    private AuthorizationData fullReversal(ReversalData reversalData) throws BankException {
        AuthorizationData ad;
        try {
            ad = makeReversal(reversalData);
        } catch (NoPreviousTransactionWithSuchRefNumberException ignored) {
            log.info("No previous transaction with such reference number");
            log.info("Executing refund instead of unsuccessful reversal");
            ad = refund(reversalData);
        }
        return ad;
    }

    private AuthorizationData partialReversal(ReversalData reversalData) throws BankException {
        AuthorizationData ad;
        log.info("Executing refund instead of partial reversal");
        ad = refund(reversalData);
        return ad;
    }

    private AuthorizationData makeReversal(ReversalData reversalData) throws BankException {
        log.info("Executing reversal");
        AuthorizationData result = requestManager.makeTransaction(new ReversalRequest(reversalData));
        result.setOperationType(BankOperationType.REVERSAL);
        result.setCashTransId(reversalData.getCashTransId());
        log.info("Reversal complete");
        return result;
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        log.info("Executing refund");
        AuthorizationData result = requestManager.makeTransaction(new CreditRequest(refundData));
        result.setOperationType(BankOperationType.REFUND);
        result.setCashTransId(refundData.getCashTransId());
        log.info("Refund complete");
        saveLastOperation(result);
        return result;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        List<List<String>> slips = new ArrayList<>();
        List<String> slip = requestManager.processServiceOperation((UCSServiceOperation) operation);
        slips.add(slip);
        return slips;
    }

    @Override
    public List<UCSServiceOperation> getAvailableServiceOperations() {
        for (UCSServiceOperation serviceOperation : serviceOperations) {
            refreshParameterValue(serviceOperation);
        }
        return serviceOperations;
    }

    private void refreshParameterValue(UCSServiceOperation operation) {
        if (operation.needsInLastTransactionID()) {
            operation.getParameter().setInputValue(getLastTransactionID());
        }
    }

    private String getLastTransactionID() {
        return lastOperation.getLastTransactionID();
    }

    public void setTerminalDelayAfterSuccessfulDailyLog(long terminalDelayAfterSuccessfulDailyLog) {
        terminalDelay.setTerminalDelayAfterSuccessfulDailyLog(terminalDelayAfterSuccessfulDailyLog);
    }

    public void setTerminalDelayAfterSuccessfulLogin(long terminalDelayAfterSuccessfulLogin) {
        terminalDelay.setTerminalDelayAfterSuccessfulLogin(terminalDelayAfterSuccessfulLogin);
    }

    public void setTerminalDelayAfterSuccessfulResponse(long terminalDelayAfterSuccessfulResponse) {
        terminalDelay.setTerminalDelayAfterSuccessfulResponse(terminalDelayAfterSuccessfulResponse);
    }

    public void setTerminalDelayAfterUnsuccessfulOperation(long terminalDelayAfterUnsuccessfulOperation) {
        terminalDelay.setTerminalDelayAfterUnsuccessfulOperation(terminalDelayAfterUnsuccessfulOperation);
    }
}
