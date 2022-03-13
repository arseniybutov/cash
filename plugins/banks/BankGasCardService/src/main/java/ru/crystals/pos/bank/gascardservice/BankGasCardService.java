package ru.crystals.pos.bank.gascardservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.datastruct.ServiceBankOperationParameter;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.AbstractFileBasedBank;
import ru.crystals.pos.bank.filebased.ResponseData;
import ru.crystals.pos.property.Properties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BankGasCardService extends AbstractFileBasedBank {
    private static final Map<BankOperationType, String> operationTypes = new EnumMap<>(BankOperationType.class);
    static final Logger log = LoggerFactory.getLogger(BankGasCardService.class);
    private static final String DAILY_LOG_COMMAND = "4";
    private static final String DEFAULT_DAILY_LOG_SUM = "0";
    private static final String DEFAULT_CHECK_NUMBER = "0";
    private static final String DEFAULT_CASH_NUMBER = "1";
    private static final String RESULT_DATA_FILE_NAME = "arecpar.txt";
    private static final String PROTOCOL_NUMBER = "15";
    private static final String ANSWER_ERROR_CODE = "957";
    public static final String SLIP_FILE_NAME = "arecimg.txt";
    public static final String EXECUTABLE_FILE_NAME = "gcsgatew";
    public static final String SLIP_FILE_CHARSET = "cp1251";
    public static final String RESPONSE_FILE_CHARSET = "cp1251";
    private final String[] commonResponseFileNames = {"aresult.txt", RESULT_DATA_FILE_NAME};
    private boolean responseContainsOnlyOneFile;
    private String cashNumber;
    private Map<ServiceBankOperationType, ServiceOperation> operationsMap =
            new EnumMap<>(ServiceBankOperationType.class);
    private boolean splittedUpdateOperations;
    private LastOperation lastOperation = new LastOperation();

    private void initServiceOperations() {
        ServiceOperation getSlipCopyOperation = new ServiceOperation(ServiceBankOperationType.GET_SLIP_COPY);
        getSlipCopyOperation.setParameter(new ServiceBankOperationParameter(ResBundleBankGasCardService.getString("CHECK_NUMBER_PARAMETER_NAME"),
                ResBundleBankGasCardService.getString("CHECK_NUMBER_WELCOME_TEXT")));
        operationsMap.put(ServiceBankOperationType.GET_SLIP_COPY, getSlipCopyOperation);
        operationsMap.put(ServiceBankOperationType.GET_FULL_REPORT, new ServiceOperation(ServiceBankOperationType.GET_FULL_REPORT));
        operationsMap.put(ServiceBankOperationType.GET_WORKING_KEY, new ServiceOperation(ServiceBankOperationType.GET_WORKING_KEY));
        if (isSplittedUpdateOperations()) {
            operationsMap
                    .put(ServiceBankOperationType.UPDATE_TERMINAL_SOFTWARE, new ServiceOperation(ServiceBankOperationType.UPDATE_TERMINAL_SOFTWARE));
            operationsMap.put(ServiceBankOperationType.LOAD_TERMINAL_PARAMS, new ServiceOperation(ServiceBankOperationType.LOAD_TERMINAL_PARAMS));
        } else {
            operationsMap.put(ServiceBankOperationType.LOAD_TERMINAL_PARAMS_AND_SOFTWARE,
                    new ServiceOperation(ServiceBankOperationType.LOAD_TERMINAL_PARAMS_AND_SOFTWARE));
        }
    }

    @Override
    public void start() throws BankException {
        setExecutableFileName(EXECUTABLE_FILE_NAME);
        setResponseData(new GasCardServiceResponseData());
        operationTypes.put(BankOperationType.SALE, "1");
        operationTypes.put(BankOperationType.REVERSAL, "2");
        operationTypes.put(BankOperationType.REFUND, "3");
        setSlipFileName(SLIP_FILE_NAME);
        setSlipFileCharset(SLIP_FILE_CHARSET);
        setResponseFileCharset(RESPONSE_FILE_CHARSET);
        initServiceOperations();
        BundleManager.addListener(Properties.class, () -> {
            try {
                getWorkingKey();
            } catch (BankException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void fillSpecificFields(AuthorizationData ad, ResponseData responseData, BankOperationType operationType) {
        GasCardServiceResponseData response = (GasCardServiceResponseData) responseData;
        ad.setRefNumber(response.getRRN());
        ad.setHostTransId(response.getInvoiceNumber());
    }

    @Override
    public void makeSlip(AuthorizationData ad, ResponseData responseData, List<String> slip, BankOperationType operationType) {
        List<List<String>> slips = new ArrayList<>();
        slips.add(slip);
        if (getInnerSlipCount() != null && getInnerSlipCount() > 0) {
            for (int i = 1; i < getInnerSlipCount(); i++) {
                slips.add(new ArrayList<>(slip));
            }
        }
        ad.setSlips(slips);
    }

    @Override
    public List<String> prepareParametersForDailyLog(Long cashTransId) {
        List<String> result = new ArrayList<>(prepareCommonParameters());
        result.add(DAILY_LOG_COMMAND);
        result.add(DEFAULT_DAILY_LOG_SUM);
        result.add(DEFAULT_CHECK_NUMBER);
        responseContainsOnlyOneFile = true;
        return result;
    }

    @Override
    public List<String> prepareParametersForDailyReport(Long cashTransId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> prepareExecutableParameters(SaleData saleData, BankOperationType operationType) {
        responseContainsOnlyOneFile = false;
        BankOperationType resultOperationType = operationType;
        List<String> result = new ArrayList<>(prepareCommonParameters());
        if (resultOperationType == BankOperationType.REVERSAL) {
            ReversalData reversalData = (ReversalData) saleData;
            if (reversalData.isPartial()) {
                log.info("Executing REFUND instead of partial REVERSAL");
                resultOperationType = BankOperationType.REFUND;
            }
        }
        result.add(operationTypes.get(resultOperationType));
        result.add(String.valueOf(saleData.getAmount()));
        if (resultOperationType == BankOperationType.REVERSAL) {
            ReversalData reversalData = (ReversalData) saleData;
            result.add(String.valueOf(reversalData.getHostTransId()));
            result.add(String.valueOf(reversalData.getRefNumber()));
        } else {
            result.add("0");
        }
        return result;
    }

    void getWorkingKey() throws BankException {
        processServiceOperation(operationsMap.get(ServiceBankOperationType.GET_WORKING_KEY));
    }

    String getCashNumber() {
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

    @Override
    public List<String> readResponseFile() throws BankCommunicationException {
        List<String> result = new ArrayList<>();
        for (String responseFileName : commonResponseFileNames) {
            try {
                List<String> file = readFileAndDelete(getFullPathToProcessingFolder() + responseFileName, getResponseFileCharset());
                log.debug("File {}: {}", responseFileName, file);
                result.addAll(file);
            } catch (IOException e) {
                if (!RESULT_DATA_FILE_NAME.equals(responseFileName) && responseContainsOnlyOneFile) {
                    log.error("Response file not found", e);
                    throw new BankCommunicationException(ResBundleBank.getString("RESPONSE_FILE_NOT_FOUND"));
                }
            }
        }
        return result;
    }

    @Override
    protected void deleteResponseFiles() {
        for (String fileName : commonResponseFileNames) {
            deleteFile(new File(getFullPathToProcessingFolder() + fileName));
        }
        deleteFile(new File(getFullPathToProcessingFolder() + getSlipFileName()));
    }

    @Override
    public List<ServiceOperation> getAvailableServiceOperations() {
        List<ServiceOperation> operations = new ArrayList<>(operationsMap.values());
        for (ServiceOperation serviceOperation : operations) {
            refreshParameterValue(serviceOperation);
        }
        return operations;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation serviceBankOperation) throws BankException {
        ServiceOperation operation = (ServiceOperation) serviceBankOperation;
        List<String> params = new ArrayList<>(prepareCommonParameters());
        Collections.addAll(params, operation.getType().getParams());
        if (operation.getParameter() != null) {
            params.add(operation.getParameter().getInputValue());
        }
        log.info("Executing {} command", operation.getType());
        ResponseData responseData = runExecutableAndGetResponseData(params);
        List<List<String>> slips = new ArrayList<>();
        if (responseData.isSuccessful()) {
            List<String> slip = logSlipFile(readSlipFile());
            slips.add(slip);
            return slips;
        } else {
            if (!responseData.getResponseCode().equals(ANSWER_ERROR_CODE) && operation.getType() != ServiceBankOperationType.LOAD_TERMINAL_PARAMS) {
                throw new BankAuthorizationException(responseData.getMessage());
            }
            return Collections.emptyList();
        }
    }

    private List<String> prepareCommonParameters() {
        List<String> result = new ArrayList<>();
        result.add(PROTOCOL_NUMBER);
        result.add(getCashNumber());
        return result;
    }

    public boolean isSplittedUpdateOperations() {
        return splittedUpdateOperations;
    }

    public void setSplittedUpdateOperations(boolean splittedUpdateOperations) {
        this.splittedUpdateOperations = splittedUpdateOperations;
    }

    @Override
    public void actionsOnSuccess(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType)
            throws BankException {
        try {
            lastOperation.saveLastSale(ad);
        } catch (IOException e) {
            throw new BankException(e);
        }
    }

    private void refreshParameterValue(ServiceOperation operation) {
        if (operation.getType().needsInLastTransactionID()) {
            operation.getParameter().setInputValue(getLastTransactionID());
        }
    }

    private String getLastTransactionID() {
        return lastOperation.getLastTransactionID();
    }

}
