package ru.crystals.pos.bank.tusson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.tusson.printer.DocumentType;
import ru.crystals.pos.bank.tusson.printer.TerminalSlip;
import ru.crystals.pos.bank.tusson.printer.TussonSlipsReceiver;
import ru.crystals.pos.bank.tusson.protocol.Operation;
import ru.crystals.pos.bank.tusson.protocol.Request;
import ru.crystals.pos.bank.tusson.protocol.Response;
import ru.crystals.pos.bank.tusson.protocol.ResponseStatus;
import ru.crystals.pos.bank.tusson.serviceoperations.BankTussonManualDailyReportOperation;
import ru.crystals.pos.bank.tusson.serviceoperations.BankTussonServiceOperation;
import ru.crystals.pos.bank.tusson.serviceoperations.ServiceOperationRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TussonPluginImpl extends AbstractBankPluginImpl {

    private static final Logger log = LoggerFactory.getLogger(TussonPluginImpl.class);
    private static final long SLIPS_AWAITING_TIMEOUT = 20L;
    private ExecutorService operationExecutor = Executors.newSingleThreadExecutor();
    private TussonSlipsReceiver slipsReceiver;
    private TerminalConnector terminalConnector;
    private List<BankTussonServiceOperation> serviceOperations = Collections.emptyList();
    private int printerPort = 7070;
    private long operationTimeout;

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        final Request request = Request.getRequestForDailyLogOperation();
        final Response response = performOperation(request);
        List<TerminalSlip> slips = getSlips(Operation.DAILY_LOG_DOCUMENTS);
        if (response.getStatus() == ResponseStatus.CANCELED) {
            log.info("Daily log operation failed");
            throw new BankAuthorizationException(ResBundleBankTusson.getString("OPERATION_FAILED"));
        }
        return makeDailyLogData(slips);
    }

    DailyLogData makeDailyLogData(List<TerminalSlip> slips) {
        final DailyLogData result = new DailyLogData();
        result.setSlip(slips.stream()
                .map(TerminalSlip::getContent)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        return performOperation(saleData, Request.getRequestForSaleOperation(saleData.getAmount().intValue()));
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        if (reversalData.getAmount().equals(reversalData.getOriginalSaleTransactionAmount())) {
            return performOperation(reversalData,
                    Request.getRequestForReversalOperation(Long.parseLong(reversalData.getRefNumber()),
                            reversalData.getAmount().intValue()));
        }
        return refund(reversalData);
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        return performOperation(refundData, Request.getRequestForRefundOperation(refundData.getAmount().intValue()));
    }

    AuthorizationData performOperation(SaleData saleData, Request request) throws BankException {
        final Response response = performOperation(request);
        if (request.getOperation() == Operation.REVERSAL && !response.isTransactionCanceled()) {
            response.setStatus(ResponseStatus.CANCELED);
        }
        final Collection<DocumentType> requiredDocuments = response.getStatus() == ResponseStatus.SUCCESS ? Operation.SUCCESS_DOCUMENTS : Operation.FAILED_DOCUMENTS;
        List<TerminalSlip> slips = getSlips(requiredDocuments);
        //Слипы получены, сформируем AuthorizationData
        return makeAuth(saleData, response, request.getOperation(), slips);
    }

    private List<TerminalSlip> getSlips(Collection<DocumentType> requiredDocuments) {
        return slipsReceiver.getSlips(requiredDocuments, SLIPS_AWAITING_TIMEOUT, TimeUnit.SECONDS);
    }

    private Response performOperation(Request request) throws BankException {
        operationExecutor.execute(slipsReceiver);
        Response response = terminalConnector.processOperation(request);
        switch (response.getStatus()) {
            case BUSY:
                throw new BankCommunicationException(ResBundleBankTusson.getString("TERMINAL_BUSY"));
            case CANCELED:
            case SUCCESS:
                return response;
            case IN_PROGRESS:
            default:
                throw new BankCommunicationException(ResBundleBankTusson.getString("UNEXPECTED_CRITICAL_ERROR"));
        }
    }

    /**
     * Формирует данные о транзакции
     *
     * @param saleData кассовая операция
     * @param response ответ от терминала
     * @param slips    слипы
     * @return
     */
    AuthorizationData makeAuth(SaleData saleData, Response response, Operation requestedOperation, List<TerminalSlip> slips) throws BankAuthorizationException {
        AuthorizationData result = new AuthorizationData();
        result.setAmount(saleData.getAmount());
        if (saleData instanceof ReversalData) {
            result.setOperationType(BankOperationType.REVERSAL);
        } else if (saleData instanceof RefundData) {
            result.setOperationType(BankOperationType.REFUND);
        } else {
            result.setOperationType(BankOperationType.SALE);
        }
        //Иногда в ответе может отсутствовать операция
        Operation operation = response.getOperation() == null ? requestedOperation : response.getOperation();
        result.setCard(makeCard(response));
        result.setDate(new Date());
        result.setAuthCode(response.getBankAnswer());
        result.setPrintNegativeSlip(isPrintNegativeSlip());
        result.setRefNumber(String.valueOf(response.getUniqueNumber()));
        result.setOperationCode((long) operation.getOperationCode());
        fillOperationStatusDependentFields(result, response.getStatus() == ResponseStatus.SUCCESS, slips);
        if (!result.isStatus()) {
            throw new BankAuthorizationException(result);
        }
        return result;
    }

    void fillOperationStatusDependentFields(AuthorizationData authorizationData, boolean operationStatus, List<TerminalSlip> slips) {
        authorizationData.setStatus(operationStatus);
        authorizationData.setSlips(getSlips(operationStatus ? Operation.SUCCESS_DOCUMENTS : Operation.FAILED_DOCUMENTS, slips));
        authorizationData.setMessage(getMessage(operationStatus ? null : DocumentType.CASHIER_NOTIFY, slips, operationStatus));
    }

    List<List<String>> getSlips(Collection<DocumentType> documents, Collection<TerminalSlip> slips) {
        List<List<String>> result = new ArrayList<>();
        for (TerminalSlip slip : slips) {
            if (documents.contains(slip.getDocumentType())) {
                result.add(slip.getContent());
            }
        }
        return result;
    }

    String getMessage(DocumentType documentType, Collection<TerminalSlip> slips, boolean successOperation) {
        String result = null;
        for (TerminalSlip slip : slips) {
            if (slip.getDocumentType() == documentType) {
                result = slip.getContent().get(0);
                break;
            }
        }
        return result != null ? result : successOperation ? ResBundleBankTusson.getString("OPERATION_SUCCESS") : ResBundleBankTusson.getString("OPERATION_FAILED");
    }

    BankCard makeCard(Response response) {
        BankCard result = new BankCard();
        result.setCardNumber(response.getPAN());
        return result;
    }

    @Override
    public void start() {
        terminalConnector = new TerminalConnector(getTerminalConfiguration().getTerminalIp(), getTerminalConfiguration().getTerminalTcpPort(), operationTimeout);
        slipsReceiver = new TussonSlipsReceiver(printerPort);
        if (!isDailyLog()) {
            log.info("Automatic daily log configured");
            serviceOperations = Collections.singletonList(new BankTussonManualDailyReportOperation(slipsReceiver, this::eventManualDailyLogComplete));
        } else {
            log.info("Manual daily log configured");
        }
    }

    private void eventManualDailyLogComplete() {
        for (BankEvent listener : getListeners()) {
            listener.eventDailyLogComplete(this);
        }
    }

    @Override
    public List<? extends ServiceBankOperation> getAvailableServiceOperations() {
        return serviceOperations;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        return ServiceOperationRunner.doOperation((BankTussonServiceOperation) operation);
    }

    @Override
    public void closeDialog() {
        ServiceOperationRunner.suspendCurrentOperation();
    }

    public int getPrinterPort() {
        return printerPort;
    }

    public void setPrinterPort(int printerPort) {
        this.printerPort = printerPort;
    }

    public long getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(long operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

}
