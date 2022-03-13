package ru.crystals.pos.bank.belinvest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cm.utils.JAXBContextFactory;
import ru.crystals.pos.bank.belinvest.ds.OperationDataRq;
import ru.crystals.pos.bank.belinvest.ds.PerformCardOperation;
import ru.crystals.pos.bank.belinvest.exceptions.BankMakeRequestException;
import ru.crystals.pos.bank.belinvest.exceptions.BankParseResponseException;
import ru.crystals.pos.bank.belinvest.exceptions.ParseXmlException;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.AbstractFileBasedBank;
import ru.crystals.pos.bank.filebased.ResponseData;
import ru.crystals.utils.time.DateConverters;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Татаrinov E. on 16.11.16.
 */
public class BelinvestBankServiceImpl extends AbstractFileBasedBank {
    private static final Logger log = LoggerFactory.getLogger(BelinvestBankServiceImpl.class);
    private static final String EXEC_FILENAME = "Epos";
    private static final String OUT_XML = "EPOSRQ.XML";
    private static final String IN_XML = "EPOSRS.XML";
    private static final String CHARSET = "UTF8";

    private static final String SALE = "Sale";
    private static final Long SALE_CODE = 1L;
    private static final String REVERSAL = "Reversal";
    private static final Long REVERSAL_CODE = 2L;
    private static final String REFUND = "Return";
    private static final Long REFUND_CODE = 3L;
    private static final String REVERSE_LAST = "ReverseLast";
    private static final Long REVERSE_LAST_CODE = 4L;
    private static final String SCENARIO = "Simple";

    @Override
    public void start() {
        setResponseData(new BelinvestResponseData());
        setExecutableFileName(EXEC_FILENAME);
        setResponseFileName(IN_XML);
        setResponseFileCharset(CHARSET);
    }

    @Override
    public List<? extends ServiceBankOperation> getAvailableServiceOperations() {
        List<ServiceOperation> operations = new ArrayList<>();
        operations.add(new ServiceOperation(ServiceBankOperationType.GET_BALANCE));
        operations.add(new ServiceOperation(ServiceBankOperationType.GET_DAY_REPORT));
        return operations;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        return getReport(new ServiceOperation(ServiceBankOperationType.GET_DAY_LOG));
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        return executeOperation((ServiceOperation) operation);
    }

    @Override
    public void actionsOnSuccess(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType) throws BankException {
        deleteRequestFile();
        super.actionsOnSuccess(ad, originalData, responseData, operationType);
    }

    @Override
    public void actionsOnFault(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType) throws BankException {
        deleteRequestFile();
        super.actionsOnFault(ad, originalData, responseData, operationType);
    }

    @Override
    public void fillSpecificFields(AuthorizationData ad, ResponseData responseData, BankOperationType operationType) {
        Long code = 0L;
        switch (operationType) {
            case SALE:
                code = SALE_CODE;
                break;
            case REFUND:
                code = REFUND_CODE;
                break;
            case REVERSAL:
                code = REVERSAL_CODE;
                break;
            case REVERSE_LAST:
                code = REVERSE_LAST_CODE;
                break;
            default:
                break;
        }
        ad.setOperationCode(code);
        ad.setDate(DateConverters.toDate(((BelinvestResponseData) responseData).getDate()));
    }

    @Override
    public void makeSlip(AuthorizationData ad, ResponseData responseData, List<String> slip, BankOperationType operationType) {
        ad.setSlips(((BelinvestResponseData) responseData).getSlip());
    }

    @Override
    public List<String> prepareParametersForDailyLog(Long cashTransId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> prepareParametersForDailyReport(Long cashTransId) {
        return new ArrayList<>();
    }

    @Override
    public List<String> prepareExecutableParameters(SaleData saleData, BankOperationType operationType) {
        return new ArrayList<>();
    }

    // операции продажи, отмены, возврата, отмена последней операции
    @Override
    protected void makeRequestFile(SaleData saleData, BankOperationType operationType) throws BankMakeRequestException {
        OperationDataRq operationDataRq = new OperationDataRq();
        operationDataRq.setAmount(saleData.getAmount());
        switch (operationType) {
            case SALE:
                operationDataRq.setOperationType(SALE);
                break;
            case REVERSAL:
                operationDataRq.setOperationType(REVERSAL);
                //Для операции reversal необходим authCode
                operationDataRq.setOriginalCode(Integer.valueOf(((RefundData) saleData).getAuthCode()));
                break;
            case REFUND:
                operationDataRq.setOperationType(REFUND);
                break;
            case REVERSE_LAST:
                // операция добавлена но ее использование под вопросом
                operationDataRq.setOperationType(REVERSE_LAST);
                break;
            default:
                break;
        }
        if (operationType != BankOperationType.REVERSE_LAST) {
            operationDataRq.setCurrency(saleData.getCurrencyCode());
        }
        operationDataRq.setScenary(SCENARIO);

        PerformCardOperation outXml = new PerformCardOperation(operationDataRq);
        makeXML(outXml);
    }

    private DailyLogData getReport(ServiceOperation operation) throws BankException {
        DailyLogData result = new DailyLogData();
        List<String> logSlip = new ArrayList<>();
        List<List<String>> slips = executeOperation(operation);
        for (List slip : slips) {
            logSlip.addAll(slip);
            logSlip.add("");
        }
        result.setSlip(logSlip);
        return result;
    }

    private List<List<String>> executeOperation(ServiceOperation operation) throws BankException {
        BelinvestResponseData responseData;
        makeRequesFileForServiceOperation(operation);
        try {
            responseData = (BelinvestResponseData) runExecutableAndGetResponseData(new ArrayList<String>());
        } catch (ParseXmlException e) {
            throw new BankParseResponseException(e.getMessage());
        } finally {
            deleteRequestFile();
        }
        if (responseData.isSuccessful()) {
            log.info("Operation " + operation.getOperation() + " successful");
            return responseData.getSlip();
        } else {
            log.info("Operation " + operation.getOperation() + " failed");
            throw new BankAuthorizationException(responseData.getMessage());
        }
    }

    private String getFullPathToRequestFile() {
        return getFullPathToProcessingFolder() + OUT_XML;
    }

    private void deleteRequestFile() {
        deleteFile(new File(getFullPathToRequestFile()));
    }

    private void makeRequesFileForServiceOperation(ServiceBankOperation operation) throws BankMakeRequestException {
        OperationDataRq operationDataRq = new OperationDataRq();
        operationDataRq.setScenary(SCENARIO);
        operationDataRq.setOperationType(((ServiceOperation) operation).getParams()[0]);
        if ("Balance".equals(((ServiceOperation) operation).getOperation())) {
            operationDataRq.setCurrency(((ServiceOperation) operation).getParams()[1]);
        } else if ("DayReport".equals(((ServiceOperation) operation).getOperation())) {
            operationDataRq.setDetailReport(((ServiceOperation) operation).getParams()[1]);
        }
        PerformCardOperation outXml = new PerformCardOperation(operationDataRq);
        makeXML(outXml);
    }

    private void makeXML(PerformCardOperation cardOperation) throws BankMakeRequestException {
        try {
            JAXBContext jaxbContext = JAXBContextFactory.getContext(PerformCardOperation.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(cardOperation, new File(getFullPathToRequestFile()));
        } catch (JAXBException e) {
            log.error("Error make request file " + OUT_XML, e);
            throw new BankMakeRequestException("Error make request file " + OUT_XML);
        }
    }

}
