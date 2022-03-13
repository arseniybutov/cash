package ru.crystals.pos.services.cyberplat;

import org.CyberPlat.IPrivException;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator;
import ru.crystals.cash.settings.ModuleConfigDocument;
import ru.crystals.cash.settings.PropertyDocument.Property;
import ru.crystals.pos.check.PositionServiceEntity;
import ru.crystals.pos.services.ServicesResult;
import ru.crystals.pos.services.ServicesService;
import ru.crystals.pos.services.cyberplat.xml.Constants;
import ru.crystals.pos.services.cyberplat.xml.Converter;
import ru.crystals.pos.services.cyberplat.xml.Range;
import ru.crystals.pos.services.exception.ServicesException;
import ru.crystals.pos.utils.CommonLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CyberplatServiceImpl implements ServicesService {

    private static final Logger commonLogger = CommonLogger.getCommonLogger();
    public static final Logger LOG = LoggerFactory.getLogger(CyberplatServiceImpl.class);

    private String serviceFolder;
    private String password;
    private String bankKeySerialNumber;

    private String dealerCode;
    private String acquiringPointCode;
    private String operatorCode;

    private String numcapacityFile;
    private String operatorsFile;

    private double commission;
    private long minCommission;
    private long maxCommission;

    private Connector connector;
    private Helper helper;

    private String sessionId;

    private OperatorsDocument operators;
    Map<Range, List<Integer>> rangeMap;

    private Operator operator;

    private SlipCreator slipCreator;
    private Calculator calculator;

    private boolean isAvailable = true;

    @Override
    public void start() throws ServicesException {
        try {

            connector = new Connector(serviceFolder, password, bankKeySerialNumber);
            connector.start();
            helper = new Helper();
            calculator = new Calculator(commission, minCommission, maxCommission);
            slipCreator = new SlipCreator();
            slipCreator.setMaintenancePhoneNumber(getMaintenancePhoneNumber());

            Converter xmlConverter = new Converter(serviceFolder, numcapacityFile, operatorsFile);
            if (xmlConverter.isUpdatePossible()) {
                long time = System.currentTimeMillis();
                xmlConverter.updateDocuments();
                String logText = "Cyberplat data base has been updated in " + (System.currentTimeMillis() - time) + " ms";
                commonLogger.debug(logText);
                LOG.info(logText);
            }

            operators = OperatorsDocument.Factory.parse(new File(serviceFolder + operatorsFile));
            rangeMap = helper.getRangeMap(serviceFolder + numcapacityFile);
        } catch (ServicesException e) {
            isAvailable = false;
            LOG.error("", e);
            throw e;
        } catch (Exception e) {
            isAvailable = false;
            LOG.error("", e);
            throw new ServicesException(ResBundleServicesCyberPlat.getString("ERROR_START"));
        }

    }

    @Override
    public void stop() {
        connector.stop();
    }

    @Override
    public ServicesResult opportunity(PositionServiceEntity positionService) {
        ServicesResult result = new ServicesResult();
        try {
            sessionId = helper.generateSessionId();
            Request request = createRequestForOpportunity(positionService);
            List<Operator> fitOperators = helper.getFitOperators(rangeMap, operators, positionService.getAccountNumber());

            for (Operator op : fitOperators) {
                Response response = null;
                try {
                    String responseString = connector.sendRequest(helper.getURLs(op).getCheckURL(), request.toString());
                    response = helper.parseResponse(responseString);
                } catch (IOException e) {
                    LOG.error("", e);
                } catch (IPrivException e) {
                    LOG.warn("", e);
                }

                if (response != null && response.isResult()) {
                    result.setResult(response.isResult());
                    this.operator = op;
                    break;
                }
            }
            if (!result.isResult()) {
                killSession();
            }
        } catch (ServicesException e) {
            killSession();
            LOG.error("", e);
        }
        return result;
    }

    @Override
    public ServicesResult act(PositionServiceEntity positionService) {
        ServicesResult servicesResult = new ServicesResult();
        if (sessionId != null) {
            servicesResult.setResult(true);
        }
        return servicesResult;
    }

    @Override
    public ServicesResult confirm(PositionServiceEntity positionService) {
        ServicesResult servicesResult = new ServicesResult();
        try {
            if (sessionId != null && operator != null) {
                try {
                    Request request = createRequestForConfirm(positionService);
                    String responseString = connector.sendRequest(helper.getURLs(operator).getPaymentURL(), request.toString());
                    Response response = helper.parseResponse(responseString);
                    servicesResult.setResult(response.isResult());
                    servicesResult.setErrorMessage(response.getErrorMessage());

                    List<List<String>> slips = new ArrayList<>();
                    slips.add(slipCreator.createSlip(operator, request, sessionId));
                    servicesResult.setSlips(slips);
                } catch (IOException | IPrivException e) {
                    LOG.error("", e);
                }
            }
        } catch (ServicesException e) {
            LOG.error("", e);
        } finally {
            killSession();
        }
        return servicesResult;
    }

    @Override
    public ServicesResult cancel(PositionServiceEntity positionService) {
        ServicesResult servicesResult = new ServicesResult();
        servicesResult.setResult(true);
        killSession();
        return servicesResult;
    }

    @Override
    public boolean getStatus() {
        return isAvailable;
    }

    private void killSession() {
        sessionId = null;
        operator = null;
    }

    private Request createRequestForOpportunity(PositionServiceEntity positionService) throws ServicesException {
        Request request = new Request();
        request.setDealerCode(dealerCode);
        request.setAcquiringPointCode(acquiringPointCode);
        request.setOperatorCode(operatorCode);
        request.setSessionId(sessionId);
        request.setAccountNumber(positionService.getAccountNumber());
        request.setAmount(helper.toStringFormat(getAmount(positionService.getSum())));
        request.setAmountAll(helper.toStringFormat(positionService.getSum()));
        return request;
    }

    private Request createRequestForConfirm(PositionServiceEntity positionService) throws ServicesException {
        Request request = new Request();
        request.setDealerCode(dealerCode);
        request.setAcquiringPointCode(acquiringPointCode);
        request.setOperatorCode(operatorCode);
        request.setSessionId(sessionId);
        request.setAccountNumber(positionService.getAccountNumber());
        request.setAmount(helper.toStringFormat(getAmount(positionService.getSum())));
        request.setAmountAll(helper.toStringFormat(positionService.getSum()));
        return request;
    }

    private long getAmount(long sum) throws ServicesException {
        return sum - calculator.calculateComission(sum);
    }

    /* Getters & setters */

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceFolder() {
        return serviceFolder;
    }

    public void setServiceFolder(String serviceFolder) {
        this.serviceFolder = serviceFolder;
    }

    public String getBankKeySerialNumber() {
        return bankKeySerialNumber;
    }

    public void setBankKeySerialNumber(String bankKeySerialNumber) {
        this.bankKeySerialNumber = bankKeySerialNumber;
    }

    public String getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(String dealerCode) {
        this.dealerCode = dealerCode;
    }

    public String getAcquiringPointCode() {
        return acquiringPointCode;
    }

    public void setAcquiringPointCode(String acquiringPointCode) {
        this.acquiringPointCode = acquiringPointCode;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public String getNumcapacity() {
        return numcapacityFile;
    }

    public void setNumcapacity(String numcapacity) {
        this.numcapacityFile = numcapacity;
    }

    public String getOperators() {
        return operatorsFile;
    }

    public void setOperators(String operators) {
        this.operatorsFile = operators;
    }

    public String getMaintenancePhoneNumber() throws XmlException, IOException {
        String maintenancePhoneNumber = null;
        File file = new File("config/plugins/goods-mobilePay-config.xml");
        ModuleConfigDocument goodsMobilePayConfig = ModuleConfigDocument.Factory.parse(file);
        for (Property property : goodsMobilePayConfig.getModuleConfig().getPropertyArray()) {
            if ("maintenancePhoneNumber".equals(property.getKey())) {
                maintenancePhoneNumber = property.getValue();
                break;
            }
        }
        return maintenancePhoneNumber;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public long getMinCommission() {
        return minCommission;
    }

    public void setMinCommission(long minCommission) {
        this.minCommission = minCommission;
    }

    public String getMaxCommission() {
        return Long.valueOf(maxCommission).toString();
    }

    public void setMaxCommission(String maxCommission) {
        if (!maxCommission.equals(Constants.UNBOUNDED)) {
            this.maxCommission = Long.valueOf(maxCommission);
        } else {
            this.maxCommission = -1;
        }

    }

}
