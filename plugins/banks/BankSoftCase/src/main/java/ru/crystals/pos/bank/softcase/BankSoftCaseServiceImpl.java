package ru.crystals.pos.bank.softcase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.cm.utils.JAXBContextFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.softcase.utils.SoftCaseAnswerParser;
import ru.crystals.pos.bank.softcase.utils.SoftCaseDocumentTemplate;
import ru.crystals.pos.bank.softcase.utils.SoftCaseMessage;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.utils.TCPPortAdapter;
import ru.crystals.pos.utils.Timer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BankSoftCaseServiceImpl extends AbstractBankPluginImpl {
    private static final long DEFAULT_RESPONSE_TIMEOUT = 200 * DateUtils.MILLIS_PER_SECOND;
    private static final int CONNECT_TIMEOUT = 3 * (int) DateUtils.MILLIS_PER_SECOND;
    private static final Logger log = LoggerFactory.getLogger(BankSoftCaseServiceImpl.class);
    private static final String TEMPLATE_PATH = "modules" + File.separator + "bank" + File.separator + "templates" + File.separator;
    private static final long ANSWER_WAITING_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String LOCALHOST = "127.0.0.1";

    private TCPPortAdapter portAdapter;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private Long cashNumber;

    private boolean isLocalApplication;
    private String processingCatalog = "banks/softcase/";
    private String fullPathToProcessingFolder;

    public BankSoftCaseServiceImpl() {
        super.setResponseTimeout(DEFAULT_RESPONSE_TIMEOUT);
        super.setCharset(DEFAULT_CHARSET);
        super.setTerminalTcpPort(3232);
    }

    @Override
    public boolean requestTerminalStateIfOnline() {
        return true;
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        return true;
    }

    @Override
    public void start() throws CashException {
        if (Objects.equals(getTerminalConfiguration().getTerminalIp(), LOCALHOST)) {
            isLocalApplication = true;
            if (SystemUtils.IS_OS_WINDOWS) {
                isLocalApplication = false;
                log.error("Local efthcxml server is not supported on Windows. Please use remote eftchxml.");
            }
        }
        if (isLocalApplication) {

            if (!checkProcessIsStarted(getFullPathToProcessingExecutable())) {
                startProcessWithTimeout(getFullPathToProcessingExecutable());
                log.info("efthcxml is started");
            } else {
                log.info("efthcxml is already started");
            }
            readEfthcxmlParameters(getFullPathToProcessingFolder() + "/" + "config.cfg");
        }
        initJAXB();
        initSocket();
    }

    private static boolean checkProcessIsStarted(String processString) {
        try {
            Process proc = Runtime.getRuntime().exec("ps");
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (StringUtils.contains(line, processString)) {
                    input.close();
                    return true;
                }
            }
            input.close();
        } catch (IOException ioe) {
            log.warn("Unable to check if efthcxml is started", ioe);
        }
        return false;
    }

    private void readEfthcxmlParameters(String fileName) {
        try {
            List<String> settings = FileUtils.readLines(new File(fileName));
            for (String string : settings) {
                String[] splitted = StringUtils.trimToEmpty(string).split("\\s");
                if (splitted.length > 1) {
                    String key = StringUtils.trimToEmpty(splitted[0]);
                    String value = StringUtils.trimToEmpty(splitted[1]);
                    if (key.startsWith("#")) {
                        continue;
                    } else if (key.equals("utf")) {
                        setCharset(value.equals("1") ? "utf-8" : "cp866");
                    } else if (key.equals("xmlport")) {
                        setTerminalTcpPort(Integer.parseInt(StringUtils.defaultString(StringUtils.trimToNull(value), "3232")));
                    } else if (key.startsWith("port_")) {
                        setTerminalID(Integer.valueOf(key.substring(key.indexOf("port_") + "port_".length())).toString());
                    }
                }
            }
            setUseCashNumberAsTerminalID(false);
        } catch (IOException e) {
            log.warn("Unable to read efthcxml settings");
        }
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        return makeAuthorization(saleData, BankOperationType.SALE);
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        return makeAuthorization(reversalData, BankOperationType.REVERSAL);
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        return makeAuthorization(refundData, BankOperationType.REFUND);
    }

    /**
     * Выполняет указанную банковскую операцию указанного типа.
     *
     * @param saleData      описание банковской операции, что надо выполнить
     * @param operationType тип операции, что надо выполнить
     * @return ответ от банковского процессинга
     * @throws BankException            при возникновении ошибок во время информационного обмена, либо уже при парсинге ответа от процессинга
     * @throws NullPointerException     если хоть один аргумент == <code>null</code>; это <em>приватный</em> метод - следите аккуратнее за тем, что сюда шлете
     * @throws IllegalArgumentException если <code>saleData</code> не соответствует <code>operationType</code>; это <em>приватный</em> метод - следите аккуратнее за тем,
     *                                  что сюда шлете
     */
    private AuthorizationData makeAuthorization(SaleData saleData, BankOperationType operationType) throws BankException {
        AuthorizationData result = doTcpProcess(saleData, operationType);
        result.setCashTransId(saleData.getCashTransId());
        result.setCurrencyCode(saleData.getCurrencyCode());
        return result;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        SoftCaseDocumentTemplate template = getTemplate(TEMPLATE_PATH + "softcase-dailyLog.xml");
        SoftCaseMessage message = new SoftCaseMessage(Long.parseLong(getTerminalID()));
        writeMessage(message);
        SoftCaseMessage answer = parseAnswer(readAnswerAsString());
        if (!"00".equals(answer.getCode())) {
            throw new BankException(StringUtils.defaultIfBlank(answer.getResp(), ResBundleBankSoftcase.getString("UNKNOWN_ERROR")) + " (" + answer
                    .getCode()
                    + ")");
        }
        return SoftCaseAnswerParser.getDailyLog(answer, template);
    }

    /**
     * Выполняет указанную банковскую операцию указанного типа.
     *
     * @param saleData      описание банковской операции, что надо выполнить
     * @param operationType тип операции, что надо выполнить
     * @return ответ от банковского процессинга
     * @throws BankException            при возникновении ошибок во время информационного обмена, либо уже при парсинге ответа от процессинга
     * @throws NullPointerException     если хоть один аргумент == <code>null</code>; это <em>приватный</em> метод - следите аккуратнее за тем, что сюда шлете
     * @throws IllegalArgumentException если <code>saleData</code> не соответствует <code>operationType</code>; это <em>приватный</em> метод - следите аккуратнее за тем,
     *                                  что сюда шлете
     */
    private AuthorizationData doTcpProcess(SaleData saleData, BankOperationType operationType) throws BankException {
        // 1. валидация
        if (saleData == null || operationType == null) {
            log.error("doTcpProcess(SaleData, BankOperationType): at least one of the arguments: " +
                    "either saleData [{}], or operationType [{}] is NULL!", saleData, operationType);
            throw new NullPointerException(String.format("doTcpProcess(SaleData, BankOperationType): at least one of the arguments: " +
                    "either saleData [%s], or operationType [%s] is NULL!", saleData, operationType));
        }
        // проверим только refund & reversal
        if (BankOperationType.REFUND.equals(operationType) && !(saleData instanceof RefundData) ||
                BankOperationType.REVERSAL.equals(operationType) && !(saleData instanceof ReversalData)) {
            log.error("doTcpProcess(SaleData, BankOperationType): the arguments (saleData [class: {}] operationType [{}]) " +
                    "do not correspond to each other!", saleData.getClass().getSimpleName(), operationType);
            throw new IllegalArgumentException(String.format("doTcpProcess(SaleData, BankOperationType): " +
                            "the arguments (saleData [class: %s] operationType [%s]) do not correspond to each other!",
                    saleData.getClass().getSimpleName(), operationType));
        }

        // 2. сам запрос
        log.info("doTcpProcess: about to process {} operation", operationType);
        try {
            SoftCaseDocumentTemplate template = getTemplate(TEMPLATE_PATH + "softcase-sale.xml");

            SoftCaseMessage message;
            switch (operationType) {
                case REVERSAL:
                    ReversalData rd = (ReversalData) saleData;
                    message = new SoftCaseMessage(rd);
                    if (rd.isPartial() && !isUsePartialReversal()) {
                        log.info("Using refund instead of partial reversal due to confugration (see parameter usePartialReversal in config)");
                        message.setType(SoftCaseMessage.REFUND_TYPE);
                    }
                    break;
                case REFUND:
                    message = new SoftCaseMessage((RefundData) saleData);
                    break;
                default:
                    message = new SoftCaseMessage(saleData);
                    break;
            }

            message.setKkm(Long.parseLong(getTerminalID()));

            writeMessage(message);

            AuthorizationData ad = SoftCaseAnswerParser.getAuthorizationData(parseAnswer(readAnswerAsString()), template);
            ad.setAmount(saleData.getAmount());
            return ad;
        } catch (BankException be) {
            if (be.getAuthorizationData() != null) {
                be.getAuthorizationData().setPrintNegativeSlip(isPrintNegativeSlip());
            }
            throw be;
        }
    }

    protected void initSocket() {
        portAdapter = new TCPPortAdapter()
                .setTcpPort(getTerminalConfiguration().getTerminalTcpPort())
                .setTcpAddress(getTerminalConfiguration().getTerminalIp())
                .setConnectTimeOut(CONNECT_TIMEOUT)
                .setSocketTimeOut((int) getResponseTimeout()).setLogger(log);
    }

    protected void initJAXB() throws BankException {
        try {
            JAXBContext context = JAXBContextFactory.getContext(SoftCaseMessage.class);
            unmarshaller = context.createUnmarshaller();
            marshaller = context.createMarshaller();
        } catch (Exception e) {
            throw new BankException(e);
        }
    }

    protected void writeMessage(SoftCaseMessage message) throws BankCommunicationException {
        StringWriter stringWriter = new StringWriter();
        try {
            marshaller.marshal(message, stringWriter);
        } catch (Exception e) {
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("ERROR_ON_SENDING_DATA"));
        }
        log.debug("Connecting to {}:{}", portAdapter.getTcpAddress(), portAdapter.getTcpPort());
        try {
            portAdapter.openPort();
            log.debug("Connected");
            log.debug("Write to socket:\n" + stringWriter.getBuffer().toString());
            portAdapter.write(stringWriter.getBuffer().toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            if (portAdapter != null) {
                portAdapter.close();
            }
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("ERROR_ON_SENDING_DATA"));
        }
    }

    protected String readAnswerAsString() throws BankCommunicationException {
        Timer responseTimer = new Timer(getResponseTimeout());
        StringBuilder allReadData = new StringBuilder();
        try {
            while (responseTimer.isNotExpired()) {
                String response = portAdapter.readAll(getCharset());
                if (response != null) {
                    response = response.trim();
                    response = response.replaceAll("\u0000", "");
                    log.debug("Read data:\n" + response);
                    allReadData.append(response);
                    int indexOfXML = allReadData.indexOf("<?xml");
                    if (indexOfXML != -1) {
                        portAdapter.close();
                        log.debug("Port closed");
                        return allReadData.substring(indexOfXML);
                    }
                }
                try {
                    Thread.sleep(ANSWER_WAITING_TIMEOUT);
                } catch (InterruptedException ignore) {
                }
            }
            portAdapter.close();
            log.debug("Port closed");
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("TERMINAL_TIMEOUT"));
        } catch (IOException e) {
            portAdapter.close();
            log.debug("Port closed");
            log.error("Error on read answer", e);
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("TERMINAL_TIMEOUT"));
        }
    }

    protected SoftCaseMessage parseAnswer(String answer) throws BankCommunicationException {
        try {
            return (SoftCaseMessage) unmarshaller.unmarshal(new StringReader(answer));
        } catch (Exception e) {
            log.error("", e);
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("ERROR_ON_RECEIVING_DATA"));
        }
    }

    protected SoftCaseDocumentTemplate getTemplate(String pathToTemplate) throws BankCommunicationException {
        try {
            return SoftCaseDocumentTemplate.getInstance(pathToTemplate);
        } catch (Exception e) {
            log.error("", e);
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("ERROR_ON_PARSING_SLIP_TEMPLATE"));
        }
    }

    public Long getCashNumber() {
        if (cashNumber == null) {
            cashNumber = 0L;
            TechProcessEvents tpe = BundleManager.get(TechProcessEvents.class);
            if (tpe != null && tpe.getCashProperties() != null) {
                cashNumber = tpe.getCashProperties().getCashNumber();
            }
        }
        return cashNumber;
    }

    public void setPortAdapter(TCPPortAdapter portAdapter) {
        this.portAdapter = portAdapter;
    }

    @Override
    public String getTerminalID() {
        if (getTerminalConfiguration().isUseCashNumberAsTerminalID()) {
            return getCashNumber().toString();
        }
        return super.getTerminalID();
    }

    public void setProcessing(String processing) {
        this.processingCatalog = processing;
    }

    public String getProcessing() {
        return processingCatalog;
    }

    protected String getFullPathToProcessingExecutable() {
        return getFullPathToProcessingFolder() + "/" + "efthcxml --config config.cfg";
    }

    protected String getFullPathToProcessingFolder() {
        if (fullPathToProcessingFolder == null) {
            fullPathToProcessingFolder = StringUtils.stripEnd(System.getProperty("user.dir").replace('\\', '/'), "/") + "/" +
                    StringUtils.strip(processingCatalog.replace('\\', '/'), "/") + "/" +
                    (SystemUtils.IS_OS_WINDOWS ? "windows" : "linux") + "/efthcxml";
        }
        return fullPathToProcessingFolder;
    }

    private void startProcessWithTimeout(final String commandString) throws BankCommunicationException {
        log.info("Start executable: " + commandString);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> task = executor.submit(() -> Runtime.getRuntime().exec(commandString, null, new File(getFullPathToProcessingFolder())).waitFor());
            task.get();
            log.debug("Executable is finished");
        } catch (Exception e) {
            log.error("Error processing executable", e);
            throw new BankCommunicationException(ResBundleBankSoftcase.getString("EXECUTION_ERROR"));
        }
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) throws BankException {
        SoftCaseDocumentTemplate template = getTemplate(TEMPLATE_PATH + "softcase-dailyLog.xml");
        SoftCaseMessage message = new SoftCaseMessage(Long.parseLong(getTerminalID()));
        writeMessage(message);
        SoftCaseMessage answer = parseAnswer(readAnswerAsString());
        if (!"00".equals(answer.getCode())) {
            throw new BankException(StringUtils.defaultIfBlank(answer.getResp(), ResBundleBankSoftcase.getString("UNKNOWN_ERROR")) + " (" + answer.getCode() + ")");
        }
        return SoftCaseAnswerParser.getDailyLog(answer, template);
    }

}
