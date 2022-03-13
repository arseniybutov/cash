package ru.crystals.pos.bank.arcom;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.bundles.BundleRef;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.arcom.operations.CashierMenuOperation;
import ru.crystals.pos.bank.arcom.operations.FullReportOperation;
import ru.crystals.pos.bank.arcom.operations.ShortReportOperation;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.AbstractFileBasedBank;
import ru.crystals.pos.bank.filebased.ResponseData;
import ru.crystals.pos.catalog.CurrencyEntity;
import ru.crystals.pos.payments.PaymentSuspensionData;
import ru.crystals.pos.payments.PaymentSuspensionResult;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BankArcomServiceImpl extends AbstractFileBasedBank {

    private static final Logger log = LoggerFactory.getLogger(BankArcomServiceImpl.class);

    static final String INI_FILE_CHARSET = "utf-8";

    private static final int SALE_OPERATION_CODE = 1;
    private static final int LAST_OPERATION_REVERSAL_OPERATION_CODE = 2;
    private static final int REFUND_OPERATION_CODE = 3;
    private static final int CANCEL_PAYMENT_AT_BANK = 5;
    private static final int SHORT_REPORT_OPERATION_CODE = 7;
    private static final int FULL_REPORT_OPERATION_CODE = 8;
    private static final int REPORT_OPERATION_CODE = 11;
    private static final int CLOSE_OPERATION_CODE = 12;
    private static final int CASHIER_MENU_OPERATION_CODE = 98;

    private static final String AMOUNT_PARAMETER = "/a";
    private static final String CURRENCY_PARAMETER = "/c";
    private static final String OPERATION_PARAMETER = "/o";
    private static final String RRN_PARAMETER = "/r";
    private static final String LOYALTY_CODE_PARAMETER = "/l";

    /**
     * При добавление этого ключа, терминал начнет возвращать строки для отображения на экране.
     * Строки имеют вид "STATUS: [сообщение от терминала]"
     */
    private static final String SHOW_OPERATION_ON_CASH_KEY = "/console";

    private String firstFiscalPrinterTerminalID;
    private String secondFiscalPrinterTerminalID;

    /**
     * ini файл с настройками
     */
    private String iniFile;
    /**
     * Папака содержащая ini файл
     */
    private String iniDirectory;
    /**
     * Папка содержащая файлы для работы с терминалом
     */
    private String workingDirectory;
    /**
     * Исполнительный файл для работы с терминалом
     */
    private String binaryFile;
    /**
     * Файл ответа процессинга
     */
    private String outputFile;

    /**
     * Файл с дополнительными дынными о операции, содержит "код лояльности"
     */
    private String extendedOutputFile;
    /**
     * Файл слипа
     */
    private String slipFile;
    /**
     * Кодировка для файла ответа и слипа
     */
    private String charsetOfResponse;

    /**
     * Начало строк с консоли ответа терминала, которые выводить кассиру на экран
     */
    private static final String START_DIALOG_WORD = "STATUS:";
    private DialogListener dialogListener = new DialogListener();

    private LastSale lastSale;

    @BundleRef
    private TechProcessInterface techProcess;

    private PropertiesManager propertiesManager;
    private static final String BANK_CONFIG_PLUGIN_NAME = "arcom";
    /**
     * Ключ настройки "Показывать сообщения пин-пада на экране кассира."
     */
    private static final String BANK_CONFIG_STATUS_MESSAGE = "show.status.message";
    /**
     * Показывать сообщения пин-пада на экране кассира.
     */
    private boolean showStatusMessage = false;

    /**
     * Доступные данному банку сервисные операции
     */
    private List<ServiceBankOperation> availableOperations;

    public BankArcomServiceImpl() {
        BundleManager.applyWhenAvailable(TechProcessInterface.class, it -> techProcess = it);
        propertiesManager = BundleManager.get(PropertiesManager.class);
    }

    @Override
    public void start() throws CashException {
        setResponseData(new ArcomResponseData());
        setExecutableFileName(getWorkingDirectory() + getBinaryFile());
        setResponseFileName(getWorkingDirectory() + getOutputFile());
        String iniFilePath = getWorkingDirectory().concat(getIniDirectory()).concat(getIniFile());
        Properties properties = getProperties(getFullPathToProcessingFolder() + iniFilePath, INI_FILE_CHARSET);
        setSlipAndResponseFileCharset(StringUtils.defaultString(getResponseFileCharset(), properties.getProperty("OPCHARSET", getCharsetOfResponse())));
        setSlipFileName(getWorkingDirectory() + properties.getProperty("CHEQ_FILE", getSlipFile()));

        propertyChanged();
        propertiesManager.addListener(BANK_CONFIG_MODULE_NAME, BANK_CONFIG_PLUGIN_NAME, changedProperties -> {
            log.info("Bank Module properties changed event!");
            propertyChanged();
        });

        try {
            setLastSale(new LastSale());
        } catch (IOException e) {
            throw new BankConfigException(ResBundleBankArcom.getString("IO_ERROR"));
        }
    }

    private void propertyChanged() {
        Map<String, String> config = propertiesManager.getByModulePlugin(BANK_CONFIG_MODULE_NAME, BANK_CONFIG_PLUGIN_NAME);
        showStatusMessage = Boolean.parseBoolean(config.getOrDefault(BANK_CONFIG_STATUS_MESSAGE, Boolean.FALSE.toString()));
    }

    private int getCodeOperationByOperationType(BankOperationType operationType) {
        switch (operationType) {
            case SALE:
                return SALE_OPERATION_CODE;
            case REVERSAL:
                return LAST_OPERATION_REVERSAL_OPERATION_CODE;
            case REFUND:
                return REFUND_OPERATION_CODE;
            case CANCEL_AT_BANK:
                return CANCEL_PAYMENT_AT_BANK;
            default:
                log.error("Unsupported operation {}", operationType);
                throw new IllegalArgumentException("Unsupported operation " + operationType);
        }
    }

    @Override
    public List<String> prepareExecutableParameters(SaleData saleData, BankOperationType operationType) {
        List<String> result = new ArrayList<>();
        final int codeOperationByOperationType = getCodeOperationByOperationType(operationType);
        result.add(OPERATION_PARAMETER + codeOperationByOperationType);
        if (operationType != BankOperationType.REVERSAL && operationType != BankOperationType.CANCEL_AT_BANK) {
            makeAmountParameter(saleData).ifPresent(result::add);
            String currencyCode = makeCurrencyCodeParameter(saleData);
            if (currencyCode != null) {
                String terminalID = makeTerminalIDParameter(saleData);
                result.add(currencyCode + terminalID);
            }
        }
        makeRRNParameter(saleData, operationType, codeOperationByOperationType).ifPresent(result::add);

        if (needShowOperationsOnCash(codeOperationByOperationType)) {
            result.add(SHOW_OPERATION_ON_CASH_KEY);
        }
        return result;
    }

    /**
     * Определяем необходимо ли показывать операции на экране кассира
     * Включается по настройки и при выполнение команд требующих участия покупателя(Продажа, возврат, отмена последней операции)
     *
     * @param codeOperationByOperationType тип операции
     * @return true - показываем информаци, false - не показываем
     */
    private boolean needShowOperationsOnCash(int codeOperationByOperationType) {
        return isShowStatusMessage() &&
                (codeOperationByOperationType == SALE_OPERATION_CODE
                        || codeOperationByOperationType == REFUND_OPERATION_CODE
                        || codeOperationByOperationType == LAST_OPERATION_REVERSAL_OPERATION_CODE);
    }

    @Override
    public AuthorizationData cancelAtBank() throws BankException {
        return makeTransaction(new SaleData(), BankOperationType.CANCEL_AT_BANK);
    }

    @Override
    protected AuthorizationData makeTransaction(SaleData saleData, BankOperationType operationType) throws BankException {
        dialogListener.addListeners(getListeners());
        dialogListener.removeServiceOperationListener();
        return super.makeTransaction(saleData, operationType);
    }

    @Override
    protected ResponseData transactionSuspensionHandle(SaleData saleData, ResponseData responseData, BankOperationType operationType) throws BankCommunicationException {
        if (!ArcomResponseData.SUSPENSION_RESPONSE_CODE.equals(responseData.getResponseCode()) || !BankOperationType.SALE.equals(operationType)) {
            return responseData;
        }
        String loyaltyProgramCode = Optional.ofNullable(parseExtendedResponseFile())
                .map(ResponseData::getLoyaltyProgramCode)
                .orElseGet(responseData::getLoyaltyProgramCode);

        final PaymentSuspensionData suspensionData = new PaymentSuspensionData();
        Map<String, String> attributes = Collections.emptyMap();
        if (loyaltyProgramCode != null) {
            attributes = Collections.singletonMap(BankPlugin.LOYALTY_PROGRAM_CODE_NAME, loyaltyProgramCode);
        }
        suspensionData.setAttributes(attributes);

        final Long effectiveAmount = saleData.getSuspensionCallback()
                .onPaymentSuspended(suspensionData)
                .map(PaymentSuspensionResult::getNewPaymentSum)
                .orElseGet(saleData::getAmount);

        List<String> saleResumeParameters = new ArrayList<>();
        saleResumeParameters.add(OPERATION_PARAMETER + SALE_OPERATION_CODE);
        saleResumeParameters.add(AMOUNT_PARAMETER + effectiveAmount);
        String currencyCode = makeCurrencyCodeParameter(saleData);
        if (currencyCode != null) {
            String terminalID = makeTerminalIDParameter(saleData);
            saleResumeParameters.add(currencyCode + terminalID);
        }
        saleResumeParameters.add(LOYALTY_CODE_PARAMETER + loyaltyProgramCode);

        try {
            //Без задержки между прерыванием транзакции и ответом не него, в приложении Arcom теряется связь с банковским терминалом
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("BankTransaction thread interrupted Exception", e);
            Thread.currentThread().interrupt();
        }
        try {
            return runExecutableAndGetResponseData(saleResumeParameters);
        } catch (BankCommunicationException bce) {
            String saleResumeResponseCode = Optional.ofNullable(parseExtendedResponseFile())
                    .map(ResponseData::getResponseCode)
                    .orElse(null);
            if (ArcomResponseExtendedData.PIN_PAD_CONNECTION_LOST_RESPONSE_CODE.equals(saleResumeResponseCode)) {
                //Терминал может быть недоступен некоторое время после прерывания, повторяем запрос
                log.warn("Bank terminal connection lost, repeating saleResume request");
                return runExecutableAndGetResponseData(saleResumeParameters);
            }
            throw bce;
        }
    }

    /**
     * Получить данные файла с дополнительными дынными о операции
     *
     * @return данные из доп. файла ответа в {@link ArcomResponseExtendedData} или null если файл не найден
     */
    private ResponseData parseExtendedResponseFile() {
        try {
            setResponseData(new ArcomResponseExtendedData());
            return parseResponseFile(readFileAndDelete(getFullPathToExtendedOutputFile(), getResponseFileCharset()));
        } catch (Exception e) {
            log.warn("Unable to read extended response file ({})", getFullPathToExtendedOutputFile(), e);
            return null;
        } finally {
            setResponseData(new ArcomResponseData());
        }
    }

    private Optional<String> makeRRNParameter(SaleData saleData, BankOperationType operationType, int codeOperationByOperationType) {
        if (codeOperationByOperationType == LAST_OPERATION_REVERSAL_OPERATION_CODE) {
            return Optional.empty();
        }
        if (operationType == BankOperationType.CANCEL_AT_BANK || operationType == BankOperationType.SALE) {
            return Optional.empty();
        }
        if (StringUtils.isBlank(((RefundData) saleData).getRefNumber())) {
            return Optional.empty();
        }
        return Optional.of(RRN_PARAMETER + ((RefundData) saleData).getRefNumber());
    }

    @Override
    protected void startProcessWithTimeout(final String[] commandString) throws BankCommunicationException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> task = executor.submit(() -> {
                Process process = Runtime.getRuntime()
                        .exec(commandString, SystemUtils.IS_OS_WINDOWS ? new String[]{} : null, new File(getFullPathToProcessingFolder()));
                if (isShowStatusMessage()) {
                    showStatusMessage(process);
                }
                log.debug("Wait bank process finish.");
                return process.waitFor();

            });
            if (getResponseTimeout() != 0) {
                task.get(getResponseTimeout(), TimeUnit.MILLISECONDS);
            } else {
                task.get();
            }
            log.trace("Executable is finished");
        } catch (TimeoutException te) {
            log.error("Error processing executable", te);
            throw new BankCommunicationException(ResBundleBank.getString("EXECUTION_TIMEOUT"));
        } catch (Exception e) {
            log.error("Error processing executable", e);
            throw new BankCommunicationException(ResBundleBank.getString("EXECUTION_ERROR"));
        }
    }

    /**
     * Читаем потоки из процесса и выводим сообщения на экран кассира.
     *
     * @param process процесс банковского терминала
     */
    private void showStatusMessage(Process process) throws IOException {
        try (BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {

            StringBuilder line = new StringBuilder();
            int c;
            // читаем по символам из-за зависания readLine() в случае ошибок(см. коммент ниже)
            while ((c = lineReader.read()) != -1) {
                char ch = (char) c;
                if (ch == '\n' || ch == '\r') {
                    log.debug("Bank process console out: '" + line + "'");
                    if (line.length() > 0 && line.indexOf(START_DIALOG_WORD) == 0) {
                        ArcomBankDialog dialog = ArcomBankDialog.createDialog(line.substring(START_DIALOG_WORD.length()).trim());
                        log.info("Dialog message: '" + dialog.getMessage() + "'");
                        dialogListener.showDialogScreen(dialog);
                    }
                    line = new StringBuilder();
                } else {
                    line.append(ch);
                }
                // грязный хак. проблема в том, что банк не возвращает никаких символов после "Press any key ...", даже -1
                // и если не проверить так и не "нажать" Enter, то просто зависним на очередном read() до наступления таймаута потока
                // процесс банка останется висеть навсегда, а если не прописан таймаут в getResponseTimeout(), то и касса тоже
                if (line.indexOf("Press any key") != -1) {
                    log.debug("Press Enter");
                    writer.write("\n\r");
                    writer.flush();
                    break;
                }
            }
        }
    }

    @Override
    public List<String> prepareParametersForDailyLog(Long cashTransId) {
        return Collections.singletonList(OPERATION_PARAMETER + CLOSE_OPERATION_CODE);
    }

    @Override
    public List<String> prepareParametersForDailyReport(Long cashTransId) {
        return Collections.singletonList(OPERATION_PARAMETER + REPORT_OPERATION_CODE);
    }

    @Override
    public void fillSpecificFields(AuthorizationData ad, ResponseData responseData, BankOperationType operationType) {
        ad.setOperationCode((long) getCodeOperationByOperationType(operationType));

        String extendedLoyaltyProgramCode = Optional.ofNullable(parseExtendedResponseFile()).map(ResponseData::getLoyaltyProgramCode).orElse(null);
        if (extendedLoyaltyProgramCode != null) {
            ad.getExtendedData().put(LOYALTY_PROGRAM_CODE_NAME, extendedLoyaltyProgramCode);
        }
    }

    @Override
    public void makeSlip(AuthorizationData ad, ResponseData responseData, List<String> slip, BankOperationType operationType) {
        List<List<String>> result = new ArrayList<>();
        if (!slip.isEmpty()) {
            result.add(slip);
            if (responseData.isSuccessful() && getInnerSlipCount() != null && getInnerSlipCount() > 0) {
                // один слип уже добавили
                for (int i = 1; i < getInnerSlipCount(); i++) {
                    result.add(new ArrayList<>(slip));
                }
            }
        }
        ad.setSlips(result);
    }

    @Override
    public void actionsOnSuccess(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType)
            throws BankException {
        if (operationType == BankOperationType.SALE) {
            saveLastSale(responseData, originalData);
        } else {
            clearLastSale();
        }
    }

    @Override
    public void actionsOnFault(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType) throws BankException {
        if (operationType == BankOperationType.REVERSAL) {
            // Если выполнялась отмена, есть вероятность, что ошибка произошла из-за невозможности отменить последнюю операцию,
            // поэтому сбросим ее, чтобы можно было выполнить возврат вместо отмены
            log.info("lastOperation is set to FALSE due to error on REVERSAL");
            clearLastSale();
        }
    }

    @Override
    public boolean canBeProcessedAsReversal(ReversalData saleData) {
        return lastSale.isLastSale(saleData);
    }

    private Optional<String> makeAmountParameter(SaleData saleData) {
        if (saleData != null && saleData.getAmount() != null) {
            return Optional.of(AMOUNT_PARAMETER + saleData.getAmount());
        }
        return Optional.empty();
    }

    private String makeCurrencyCodeParameter(SaleData saleData) {
        if (saleData != null && saleData.getCurrencyCode() != null) {
            return CURRENCY_PARAMETER + getCurrencyCodeByName(saleData.getCurrencyCode());
        }
        return null;
    }

    private String makeTerminalIDParameter(SaleData saleData) {
        return (saleData.isFirstFiscalPrinter() == null || StringUtils.isEmpty(getFirstFiscalPrinterTerminalID())
                || StringUtils.isEmpty(getSecondFiscalPrinterTerminalID()))
                ? StringUtils.EMPTY : "," +
                (saleData.isFirstFiscalPrinter() ? getFirstFiscalPrinterTerminalID() : getSecondFiscalPrinterTerminalID());
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        // вообще-то, clearLastSale() должно выполняться в случае успешного завершения сверки итогов, но кто нам даст гарантию, что
        // неуспешная сверка итогов не отнимет у нас возможность сделать отмену последней операции?
        clearLastSale();
        return super.dailyLog(cashTransId);
    }

    protected void clearLastSale() throws BankException {
        try {
            getLastSale().clear();
        } catch (IOException e) {
            log.error("", e);
            throw new BankException(ResBundleBankArcom.getString("IO_ERROR"));
        }
    }

    protected void saveLastSale(ResponseData responseData, SaleData saleData) throws BankException {
        try {
            getLastSale().saveLastSale(responseData, saleData);
        } catch (IOException e) {
            log.error("", e);
            throw new BankException(ResBundleBankArcom.getString("IO_ERROR"));
        }
    }

    protected void setLastSale(LastSale lastSale) {
        this.lastSale = lastSale;
    }

    private LastSale getLastSale() throws IOException {
        if (lastSale == null) {
            setLastSale(new LastSale());
        }
        return lastSale;
    }

    @Override
    public List<? extends ServiceBankOperation> getAvailableServiceOperations() {
        if (availableOperations == null) {
            availableOperations = new ArrayList<>();
            availableOperations.add(new CashierMenuOperation());
            availableOperations.add(new ShortReportOperation());
            availableOperations.add(new FullReportOperation());
        }
        return availableOperations;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        if (operation instanceof CashierMenuOperation) {
            runExecutable(OPERATION_PARAMETER + CASHIER_MENU_OPERATION_CODE);
        } else if (operation instanceof ShortReportOperation) {
            return executeAndParseData(OPERATION_PARAMETER + SHORT_REPORT_OPERATION_CODE, operation);
        } else if (operation instanceof FullReportOperation) {
            return executeAndParseData(OPERATION_PARAMETER + FULL_REPORT_OPERATION_CODE, operation);
        }
        return Collections.emptyList();
    }

    /**
     * Выполнит команду с нужными аргументами и распарсит результат, если команда выполнена успешна
     *
     * @param params аргументы команды
     * @return слипы
     * @throws BankCommunicationException
     */
    private List<List<String>> executeAndParseData(String params, ServiceBankOperation operation) throws BankException {
        ResponseData responseData = runExecutableAndGetResponseData(params);
        List<List<String>> slips = new ArrayList<>();
        slips.add(logSlipFile(readSlipFile()));
        if (responseData.isSuccessful()) {
            log.info("Operation [ {} ] successful", operation.getTypeOperation().name());
            return slips;
        } else {
            log.info("Operation [ {} ] failed ", operation.getTypeOperation().name());
            throw new BankAuthorizationException(responseData.getMessage());
        }
    }

    private String getFullPathToExtendedOutputFile() {
        return getFullPathToProcessingFolder() + getExtendedOutputFile();
    }

    public String getFirstFiscalPrinterTerminalID() {
        return firstFiscalPrinterTerminalID;
    }

    public void setFirstFiscalPrinterTerminalID(String firstFiscalPrinterTerminalID) {
        this.firstFiscalPrinterTerminalID = firstFiscalPrinterTerminalID;
    }

    public String getSecondFiscalPrinterTerminalID() {
        return secondFiscalPrinterTerminalID;
    }

    public void setSecondFiscalPrinterTerminalID(String secondFiscalPrinterTerminalID) {
        this.secondFiscalPrinterTerminalID = secondFiscalPrinterTerminalID;
    }

    public String getWorkingDirectory() {
        if (workingDirectory == null) {
            workingDirectory = SystemUtils.IS_OS_WINDOWS ? "Arcus2/" : StringUtils.EMPTY;
        }
        return workingDirectory;
    }

    public String getIniDirectory() {
        if (iniDirectory == null) {
            iniDirectory = SystemUtils.IS_OS_WINDOWS ? "INI/" : StringUtils.EMPTY;
        }
        return iniDirectory;
    }

    public String getIniFile() {
        if (StringUtils.isEmpty(iniFile)) {
            iniFile = "cashreg.ini";
        }
        return iniFile;
    }

    public String getBinaryFile() {
        if (StringUtils.isEmpty(binaryFile)) {
            binaryFile = SystemUtils.IS_OS_WINDOWS ? "CommandLineTool.exe" : "cashreg";
        }
        return binaryFile;
    }

    public String getOutputFile() {
        if (StringUtils.isEmpty(outputFile)) {
            outputFile = SystemUtils.IS_OS_WINDOWS ? "output.dat" : "output.out";
        }
        return outputFile;
    }

    public String getExtendedOutputFile() {
        if (StringUtils.isEmpty(extendedOutputFile)) {
            extendedOutputFile = "output.dat";
        }
        return extendedOutputFile;
    }

    public String getSlipFile() {
        if (StringUtils.isEmpty(slipFile)) {
            slipFile = "p";
        }
        return slipFile;
    }

    public String getCharsetOfResponse() {
        if (StringUtils.isEmpty(charsetOfResponse)) {
            charsetOfResponse = "cp1251";
        }
        return charsetOfResponse;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = formalizePath(workingDirectory);
    }

    public void setIniDirectory(String iniDirectory) {
        this.iniDirectory = formalizePath(iniDirectory);
    }

    public void setIniFile(String iniFile) {
        this.iniFile = iniFile;
    }

    public void setBinaryFile(String binaryFile) {
        this.binaryFile = binaryFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setExtendedOutputFile(String extendedOutputFile) {
        this.extendedOutputFile = extendedOutputFile;
    }

    public void setSlipFile(String slipFile) {
        this.slipFile = slipFile;
    }

    public void setCharsetOfResponse(String charsetOfResponse) {
        this.charsetOfResponse = charsetOfResponse;
    }

    /**
     * Устанавливает слэш в конце, в случае отсутствия
     *
     * @param path путь к каталогу
     * @return нормальный путь к каталогу
     */
    protected String formalizePath(String path) {
        return StringUtils.isNotEmpty(path) && !StringUtils.endsWith(path, "/") ? path.concat("/") : path;
    }

    protected String getCurrencyCodeByName(String name) {
        CurrencyEntity currency = techProcess.getCurrency(name);
        return currency == null ? StringUtils.EMPTY : String.valueOf(currency.getGuid());
    }

    @Override
    public boolean isCancelAtBankAvailable() {
        return true;
    }

    public boolean isShowStatusMessage() {
        return showStatusMessage;
    }
}
