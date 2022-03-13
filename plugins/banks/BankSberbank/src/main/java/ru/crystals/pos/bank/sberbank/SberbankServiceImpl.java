package ru.crystals.pos.bank.sberbank;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.bank.BankCommon;
import ru.crystals.pos.bank.BankDialogEvent;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.BankEnterClientRequisites;
import ru.crystals.pos.bank.BankPlugin;
import ru.crystals.pos.bank.CardInfoBankPlugin;
import ru.crystals.pos.bank.ClientRequisitesType;
import ru.crystals.pos.bank.TerminalCardReaderBankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankPayRefuseException;
import ru.crystals.pos.bank.filebased.AbstractFileBasedBank;
import ru.crystals.pos.bank.filebased.ResponseData;
import ru.crystals.pos.bank.sberbank.operations.FullReportOperation;
import ru.crystals.pos.bank.sberbank.operations.ShortReportOperation;
import ru.crystals.pos.payments.PaymentSuspensionData;
import ru.crystals.pos.payments.PaymentSuspensionResult;
import ru.crystals.pos.properties.PropertiesManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SberbankServiceImpl extends AbstractFileBasedBank implements
        CardInfoBankPlugin, TerminalCardReaderBankPlugin, BankEnterClientRequisites, SberbankServiceConfigure {

    /**
     * SRTZ-487
     * иногда банк присылает пустой RequestID, в случае "52" (OPERATION_STATUS_REQUEST) это не должно вызывать проблем
     */
    public static final String EMPTY_REQUEST_ID = "00000000";

    private static final Logger log = LoggerFactory.getLogger(SberbankServiceImpl.class);
    private static final String CHARSET_LINUX = "KOI8-R";
    private static final String CHARSET_WIN = "cp866";
    private static final String DEFAULT_END_SLIP_STRING = "======";
    private static final String DEFAULT_SLIP_DELIMITER = "\u0001";
    private static final String P_FILE = "p";
    private static final String E_FILE = "e";
    private static final List<String> EXEC_WINDOWS = ImmutableList.of("upwinln.exe", "sb_pilot.exe");
    private static final String EXEC_LINUX = "sb_pilot";
    private static final String SETTINGS_FILE_NAME = "pinpad.ini";
    private static final String SETTINGS_FILE_ENCODING = "UTF8";
    private static final String CHECKING_SETTINGS_PARAM = "ComPort";

    // commands
    private static final String SALE = "1";
    private static final String REFUND = "3";
    private static final String CLOSE = "7";
    private static final String CANCEL_PAYMENT_AT_BANK = "8";
    private static final String PRINT_CONTROL = "9";
    private static final String CARD_INFO = "20";
    private static final String SHOW_SCREEN = "27";
    private static final String OPEN_READER = "29";
    private static final String CLOSE_READER = "31";
    private static final String READ_CARD = "30";
    private static final String ENTER_CLIENT_REQUISITES = "49";
    private static final String OPERATION_STATUS_REQUEST = "52";
    private static final String SALE_WITH_CASH_OUT = "61";
    private static final String USER_PAY_REFUSE = "2000";
    private static final String OPERATIONS_NOT_FOUND = "4118";

    private static final String[] SHORT_REPORT_OPERATION_CODE = new String[]{"9", "0"};
    private static final String[] FULL_REPORT_OPERATION_CODE = new String[]{"9", "1"};

    private static final String CARD_TYPE_AUTO = "0";
    private static final String WITHOUT_CARD = "QSELECT";

    private static final String READER_SCREEN_CHARSET = "cp866";

    private static final String GENERATE_ID_PROPERTY = "sberbank.config.is.need.generate.request.id";
    private static final String PLUGIN_NAME = "sberbank";

    // Разделитель слипов (задается в TLV - Общие/Параметры для терминалов/"Последовательность в конце чека", по умолчанию 01, но может отсутствовать
    // или быть другим)
    private String slipDelimiter = DEFAULT_SLIP_DELIMITER;
    // Нижняя граница слипа
    private String endOfSlipString = DEFAULT_END_SLIP_STRING;
    private boolean isUseTerminalCardReader;
    private String firstFiscalPrinterDepartment;
    private String secondFiscalPrinterDepartment;

    private boolean needGenerateRequestId = false;
    private String lastRequestId = null;

    private DialogListener dialogListener = new DialogListener();
    // ждать пока отображается диалог
    private volatile AtomicBoolean waitDialog = new AtomicBoolean(true);
    private volatile AtomicBoolean isNeedRepeat = new AtomicBoolean(false);

    /**
     * Доступные данному банку сервисные операции
     */
    private List<ServiceBankOperation> availableOperations;

    /**
     * Надстройка над таблицей sales_management_properties, позволяющая читать оттуда данные.
     */
    @Autowired
    private PropertiesManager propertiesManager;

    @Override
    public void start() {
        setResponseData(new SberbankResponseData());
        setResponseFileName(E_FILE);
        setSlipFileName(P_FILE);
        if (SystemUtils.IS_OS_WINDOWS) {
            setExecutableFileNameForWindows();
            setSlipAndResponseFileCharset(CHARSET_WIN);
        } else {
            setExecutableFileNameForLinux();
            setSlipAndResponseFileCharset(CHARSET_LINUX);
        }
        String generateIdProperty = StringUtils.trimToNull(propertiesManager.getProperty(BankCommon.BANK_CONFIG_MODULE_NAME, PLUGIN_NAME, GENERATE_ID_PROPERTY, null));
        if (generateIdProperty != null) {
            needGenerateRequestId = Boolean.parseBoolean(generateIdProperty);
        }
    }

    @Override
    public AuthorizationData cancelAtBank() throws BankException {
        return super.makeTransaction(new SaleData(), BankOperationType.CANCEL_AT_BANK);
    }

    @Override
    protected AuthorizationData makeTransaction(SaleData saleData, BankOperationType operationType) throws BankException {
        if (isNeedGenerateRequestId()) {
            return makeTransactionWithRequestId(saleData, operationType);
        } else {
            return super.makeTransaction(saleData, operationType);
        }
    }

    @Override
    public boolean shouldBeProcessedAsRefundIfReversalFailed(ReversalData reversalData, BankAuthorizationException bankAuthorizationException) {
        return bankAuthorizationException.getAuthorizationData() != null
                && OPERATIONS_NOT_FOUND.equals(bankAuthorizationException.getAuthorizationData().getResponseCode());
    }

    private AuthorizationData makeTransactionWithRequestId(SaleData saleData, BankOperationType operationType) throws BankException {
        log.info("Operation {} will be processed", operationType);
        dialogListener.addListeners(getListeners());
        dialogListener.removeServiceOperationListener();
        verifySaleData(saleData, operationType);
        makeRequestFile(saleData, operationType);
        // Запуск исполняемого модуля, Чтение и разбор файла ответа
        SberbankResponseData responseData = null;
        try {
            responseData = (SberbankResponseData) runExecutableAndGetResponseData(prepareExecutableParameters(saleData, operationType));
            responseData = (SberbankResponseData) transactionSuspensionHandle(saleData, responseData, operationType);
        } catch (BankCommunicationException e) {
            //не найден файл ответа, норм, попробуем перезапросить
            log.error("Answer file not found! Try to re-ask...");
        }
        // сравнение requestId в текущем файле ответа
        if (checkResponseDataAndStatusFailed(responseData, false, lastRequestId)) {
            // Не ОК, перезапросим данные по операции
            String lastOperationRequestId = lastRequestId;
            do {
                boolean isNeedShowDialog = true;
                if (saleData instanceof ReversalData) {
                    ReversalData reversalData = (ReversalData) saleData;
                    isNeedShowDialog = reversalData.getOperationType() != BankOperationType.REVERSAL;
                }
                waitDialog.set(isNeedShowDialog);
                isNeedRepeat.set(isNeedShowDialog);
                try {
                    responseData = (SberbankResponseData) runExecutableAndGetResponseData(prepareOperationStatusRequest(lastOperationRequestId));
                } catch (BankCommunicationException e) {
                    // снова нет ответа... обработаем ниже
                    log.error("Answer file not found!");
                }
                if (responseData != null && "4311".equals(responseData.getResponseCode())) {
                    responseData.putMessage(ResBundleBankSberbank.getString("OPERATION_NOT_PERFORMED"));
                }
                if (checkResponseDataAndStatusFailed(responseData, true, lastRequestId)) {
                    dialogListener.showDialogScreen(SberbankBankDialog.createDialog(ResBundleBankSberbank.getString("TERMINAL_IS_NOT_AVAILABLE")));
                    while (waitDialog.get()) {
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            log.error("", e);
                        }
                    }
                    if (checkResponseDataFailed(responseData, true, lastRequestId)) {
                        responseData = new SberbankResponseData();
                        responseData.putMessage(ResBundleBankSberbank.getString("OPERATION_INTERRUPTED"));
                    }
                } else {
                    isNeedRepeat.set(false);
                }
            } while (isNeedRepeat.get());
        }

        AuthorizationData ad = new AuthorizationData();

        // Заполнение общих и специфичных полей объекта AuthorizationData
        fillCommonFields(ad, saleData, responseData, operationType);
        log.debug("fillCommonFields finished");
        fillSpecificFields(ad, responseData, operationType);
        log.debug("fillSpecificFields finished");

        // Подготовка слипа для объекта AuthorizationData
        makeSlip(ad, responseData, logSlipFile(readSlipFile()), operationType);
        log.debug("makeSlip finished");
        if (ad.isStatus()) {
            actionsOnSuccess(ad, saleData, responseData, operationType);
            log.info("Operation {} successful", operationType);
            return ad;
        } else {
            actionsOnFault(ad, saleData, responseData, operationType);
            log.info("Operation {} failed ({}, {})", operationType, ad.getResponseCode(), ad.getMessage());
            throw new BankAuthorizationException(ad);
        }
    }

    @Override
    protected ResponseData transactionSuspensionHandle(SaleData saleData, ResponseData responseData, BankOperationType operationType) throws BankCommunicationException {
        if (!SberbankResponseData.SUSPENSION_CODE.equals(responseData.getResponseCode()) || !BankOperationType.SALE.equals(operationType)) {
            //Оплата выполнена или произошла ошибка, перерасчет не нужен
            return responseData;
        }

        Map<String, String> attributes = new HashMap<>();
        attributes.put(BankPlugin.LOYALTY_PROGRAM_CODE_NAME, responseData.getLoyaltyProgramCode());
        attributes.put(BankPlugin.CARD_NUMBER_HASH, responseData.getBankCard().getCardNumberHash());

        final PaymentSuspensionData suspensionData = new PaymentSuspensionData();
        suspensionData.setAttributes(attributes);

        final Long effectiveAmount = saleData.getSuspensionCallback()
                .onPaymentSuspended(suspensionData)
                .map(PaymentSuspensionResult::getNewPaymentSum)
                .orElseGet(saleData::getAmount);

        //Формируем повторный запрос на оплату, с хэшкодом карты из ответа-прерывания
        List<String> parameters = Arrays.asList(SALE,
                effectiveAmount.toString(),
                CARD_TYPE_AUTO,
                responseData.getBankCard().getCardNumberHash(),
                generateDepartNumber(saleData.isFirstFiscalPrinter()),
                generateRequestIdCommandParam(true));
        return runExecutableAndGetResponseData(parameters);
    }

    @Override
    public void actionsOnFault(AuthorizationData ad, SaleData originalData, ResponseData responseData, BankOperationType operationType)
            throws BankException {
        if (USER_PAY_REFUSE.equals(ad.getResponseCode())) {
            // Authorization data in null, because of no need
            throw new BankPayRefuseException(ResBundleBankSberbank.getString("USER_CARD_PAY_REFUSE"));
        }
    }

    static boolean checkResponseDataAndStatusFailed(SberbankResponseData responseData, boolean emptyRequestIdAllowed, String lastRequestId) {
        return checkResponseDataFailed(responseData, emptyRequestIdAllowed, lastRequestId)
                || ErrorHandler.isCriticalError(responseData.getResponseCode());
    }

    private static boolean checkResponseDataFailed(SberbankResponseData responseData, boolean emptyRequestIdAllowed, String lastRequestId) {
        return responseData == null
                || responseData.getRequestId() == null
                || !checkLastRequestId(responseData.getRequestId(), emptyRequestIdAllowed, lastRequestId);
    }

    private static boolean checkLastRequestId(String requestId, boolean emptyRequestIdAllowed, String lastRequestId) {
        return requestId.equalsIgnoreCase(lastRequestId) || (emptyRequestIdAllowed && EMPTY_REQUEST_ID.equals(requestId));
    }

    protected List<String> prepareOperationStatusRequest(String lastGeneratedRequestId) {
        return Arrays.asList(OPERATION_STATUS_REQUEST,
                lastGeneratedRequestId,
                generateRequestIdCommandParam(true));
    }

    @Override
    public void eventLocalizationChanged(String customResBundle, Locale locale) {
        ResBundleBankSberbank.setCustomResBundle(customResBundle, locale);
    }

    @Override
    public void showTerminalCardReaderReadCardScreen() {
        showScreen("TERMINAL_READER_SHOP_LOGO_SCREEN", "TERMINAL_READER_SWIPE_CARD");
    }

    @Override
    public synchronized String readCardNumberFromTerminal() throws BankException {
        runExecutable(READ_CARD, generateRequestIdCommandParam(false));
        SberbankResponseData responseData = (SberbankResponseData) parseResponseFile(readResponseFile());
        if (responseData.isSuccessful()) {
            return responseData.getCardNumber();
        }
        return null;
    }

    @Override
    public void activateTerminalCardReader() {
        if (activateReader()) {
            showScreen("TERMINAL_READER_SHOP_LOGO_SCREEN", "TERMINAL_READER_SWIPE_CARD");
        }
    }

    @Override
    public void deactivateTerminalCardReader() {
        if (deactivateReader()) {
            showScreen("TERMINAL_READER_DEFAULT_SCREEN");
        }
    }

    @Override
    public void eventReadCardIsNotApplied() {
        showScreen("TERMINAL_READER_CARD_NOT_FOUND_SCREEN", "TERMINAL_READER_SWIPE_CARD");
        reactivateReader();
    }

    @Override
    public void eventReadCardIsApplied() {
        showScreen("TERMINAL_READER_CARD_ACCEPTED_SCREEN", "TERMINAL_READER_CARD_ACCEPTED");
        reactivateReader();
    }

    @Override
    public void eventReadCardIsBankCard() {
        showScreen("TERMINAL_READER_BANK_CARD_READ_SCREEN", "TERMINAL_READER_SWIPE_CARD");
        reactivateReader();
    }

    private void reactivateReader() {
        if (deactivateReader() && activateReader()) {
            log.debug("Reader reactivated");
        } else {
            log.error("Error on reactivating reader");
        }
    }

    private boolean activateReader() {
        return activateOrDeactivateReader(OPEN_READER, "activating");
    }

    private boolean deactivateReader() {
        return activateOrDeactivateReader(CLOSE_READER, "deactivating");
    }

    private boolean activateOrDeactivateReader(String command, String message) {
        try {
            ResponseData rd = runExecutableAndGetResponseData(command, generateRequestIdCommandParam(false));
            if (!rd.isSuccessful()) {
                log.error("Error on " + message + " reader");
                return false;
            }
        } catch (BankCommunicationException e) {
            log.error("Error on " + message + " reader", e);
            return false;
        }
        return true;
    }

    /**
     * Вывод на экран пинпада экранной формы с указанным номером
     */
    private void showScreen(String... screen) {
        List<String> showScreenCommand = prepareShowScreenCommand(screen);
        if (!showScreenCommand.isEmpty()) {
            try {
                ResponseData rd = runExecutableAndGetResponseData(showScreenCommand);
                if (!rd.isSuccessful()) {
                    log.error("Error on show custom message on terminal ({}, {})", rd.getResponseCode(), rd.getMessage());
                }
            } catch (BankCommunicationException e) {
                log.error("Error on show custom message on terminal", e);
            }
        } else {
            log.info("Screen ID and Text not set in config file, no screen will be shown on terminal");
        }
    }

    protected List<String> prepareShowScreenCommand(String... screenIDAndText) {
        if (screenIDAndText.length > 0) {
            List<String> parameters = new ArrayList<>();
            parameters.add(SHOW_SCREEN);
            parameters.add(ResBundleBankSberbank.getString(screenIDAndText[0]));
            for (int i = 1; i < screenIDAndText.length; i++) {
                parameters.add(StringUtils.replace(ResBundleBankSberbank.getString(screenIDAndText[i]), " ", "_"));
            }
            parameters.add(generateRequestIdCommandParam(false));
            return parameters;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        return isProcessingConfigured() && requestTerminalStateIfOnline();
    }

    @Override
    public boolean isProcessingConfigured() {
        File executableFile = getExecutableFile();
        if (!executableFile.exists()) {
            log.error("Executable file ({}) DOES not exist", executableFile.getAbsolutePath());
            return false;
        } else if (checkSettingsFileFound() && !checkSettingsFileCorrect()) {
            log.error("Settings file is empty or doesn't exists");
            return false;
        }
        return true;
    }

    protected boolean checkSettingsFileCorrect() {
        try {
            Properties properties = getProperties(getFullPathToProcessingFolder() + SETTINGS_FILE_NAME, SETTINGS_FILE_ENCODING);
            if (properties != null) {
                Set<String> propertyNames = properties.stringPropertyNames();
                if (propertyNames != null) {
                    if (StringUtils.trimToNull(properties.getProperty(CHECKING_SETTINGS_PARAM)) != null) {
                        return true;
                    } else {
                        for (String propertyName : propertyNames) {
                            if (propertyName.equalsIgnoreCase(CHECKING_SETTINGS_PARAM)) {
                                return StringUtils.trimToNull(properties.getProperty(propertyName)) != null;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<? extends ServiceBankOperation> getAvailableServiceOperations() {
        if (availableOperations == null) {
            availableOperations = new ArrayList<>();
            availableOperations.add(new ShortReportOperation());
            availableOperations.add(new FullReportOperation());
        }
        return availableOperations;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        if (operation instanceof ShortReportOperation) {
            return executeAndParseData(operation, SHORT_REPORT_OPERATION_CODE);
        } else if (operation instanceof FullReportOperation) {
            return executeAndParseData(operation, FULL_REPORT_OPERATION_CODE);
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
    private List<List<String>> executeAndParseData(ServiceBankOperation operation, String... params) throws BankException {
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

    protected boolean checkSettingsFileFound() {
        return new File(getFullPathToProcessingFolder() + SETTINGS_FILE_NAME).exists();
    }

    @Override
    public synchronized boolean requestTerminalStateIfOnline() {
        try {
            // Почему-то изначально решили проверять доступность терминала выполнением команды Печать контрольной ленты
            runExecutable(PRINT_CONTROL, generateRequestIdCommandParam(false));
            // В случае положительного завершения проверки не будем логировать файл ответа
            ResponseData responseData = parseResponseFile(readResponseFile());
            if (!responseData.isSuccessful()) {
                log.error("Invalid terminal state ({}, {})", responseData.getResponseCode(), responseData.getMessage());
            }
            return responseData.isSuccessful();
        } catch (Exception e) {
            log.error("Unable to perform terminal state check ({})", e.getMessage());
            return false;
        }
    }

    @Override
    public BankCard getBankCardInfo() throws BankException {
        ResponseData responseData = runExecutableAndGetResponseData(CARD_INFO, generateRequestIdCommandParam(false));
        if (responseData.isSuccessful()) {
            return responseData.getBankCard();
        } else {
            throw new BankAuthorizationException(responseData.getMessage());
        }
    }

    @Override
    protected List<String> prepareCommandString(List<String> parameters, List<String> preparedCommand) {
        // Поскольку модуль sb_pilot требует получения параметров в кодировке cp866 (что при вызове консольного приложения из Java под Linux не так),
        // сделано обходное решение с запуском промежуточного скрипта в нужной кодировке для команды SHOW_SCREEN с двумя параметрами (номер экрана
        // и текст)
        if (parameters.size() > 2 && SHOW_SCREEN.equals(parameters.get(0)) && !SystemUtils.IS_OS_WINDOWS) {
            log.info("Executable command: {}", preparedCommand);
            File showScreen = new File(getFullPathToProcessingFolder() + "showscreen.sh");
            try {
                FileUtils.writeStringToFile(showScreen, String.join(" ", preparedCommand), READER_SCREEN_CHARSET);
                if (showScreen.exists() && !showScreen.setExecutable(true)) {
                    log.error("Error on chmod temp executable script");
                }
            } catch (IOException e) {
                log.error("Error on save temp executable script", e);
            }
            ArrayList<String> showScreenCommand = new ArrayList<>();
            showScreenCommand.add(getFullPathToProcessingFolder() + "showscreen.sh");
            return showScreenCommand;
        } else {
            if (!parameters.isEmpty() && READ_CARD.equals(parameters.get(0))) {
                // чтобы не забивать лог ежесекундными сообщениями
                log.trace("Executable command: {}", preparedCommand);
                return preparedCommand;
            } else {
                return super.prepareCommandString(parameters, preparedCommand);
            }
        }
    }

    @Override
    public void fillSpecificFields(AuthorizationData ad, ResponseData responseData, BankOperationType operationType) {
        ad.setOperationCode(operationType == BankOperationType.SALE ? Long.parseLong(SALE) : Long.parseLong(REFUND));
    }

    @Override
    public void makeSlip(AuthorizationData ad, ResponseData responseData, List<String> rawSlip, BankOperationType operationType) {
        List<List<String>> result = new ArrayList<>();
        if (!rawSlip.isEmpty()) {
            boolean filterBlankLinesAfterFirstSlip = false;
            int firstLineOfSlip = 0;
            // в одном файле ответа может быть 1 или 2 слипа
            for (int currentLine = 0; currentLine < rawSlip.size(); currentLine++) {
                // При достижении нижней границы слипа
                if (rawSlip.get(currentLine).contains(getEndOfSlipString())) {
                    // кладем пройденную часть в качестве первого слипа, включая границу
                    result.add(rawSlip.subList(firstLineOfSlip, currentLine + 1));
                    // и начинаем отфильтровывать пустые строки после границы...
                    filterBlankLinesAfterFirstSlip = true;
                    // ... до первой непустой строки
                } else if (filterBlankLinesAfterFirstSlip && !StringUtils.isBlank(rawSlip.get(currentLine))) {
                    // Если встретится разделитель слипа - отрезаем его
                    if (rawSlip.get(currentLine).startsWith(getSlipDelimiter())) {
                        rawSlip.set(currentLine, rawSlip.get(currentLine).substring(1));
                    }
                    firstLineOfSlip = currentLine;
                    filterBlankLinesAfterFirstSlip = false;
                }
            }
            if (result.isEmpty()) {
                result.add(rawSlip);
            }
            // если количество слипов не совпадает с настройкой - удалим или добавим копии
            if (responseData.isSuccessful() && getInnerSlipCount() != null && getInnerSlipCount() > 0 && result.size() != getInnerSlipCount()) {
                if (getInnerSlipCount() < result.size()) {
                    result = result.subList(0, getInnerSlipCount());
                } else {
                    // будем клонировать последний элемент
                    List<String> slipCopy = result.get(result.size() - 1);
                    while (getInnerSlipCount() > result.size()) {
                        result.add(new ArrayList<>(slipCopy));
                    }
                }
            }
        }
        ad.setSlips(result);
    }

    @Override
    public List<String> prepareParametersForDailyLog(Long cashTransId) {
        return Arrays.asList(CLOSE, generateRequestIdCommandParam(false));
    }

    @Override
    public List<String> prepareParametersForDailyReport(Long cashTransId) {
        return Arrays.asList(PRINT_CONTROL, generateRequestIdCommandParam(false));
    }

    @Override
    public List<String> prepareExecutableParameters(SaleData saleData, BankOperationType operationType) {
        switch (operationType) {
            case REVERSAL:
                return prepareReversalParameters(saleData);
            case CANCEL_AT_BANK:
                return Collections.singletonList(CANCEL_PAYMENT_AT_BANK);
            case SALE:
                return prepareSaleParameters(saleData);
            default:
                return prepareRefundParameters(saleData);
        }
    }

    private List<String> prepareReversalParameters(SaleData saleData) {
        return Arrays.asList(CANCEL_PAYMENT_AT_BANK,
                saleData.getAmount().toString(),
                CARD_TYPE_AUTO,
                WITHOUT_CARD,
                generateRRN(saleData),
                generateRequestIdCommandParam(true));
    }

    private List<String> prepareSaleParameters(SaleData saleData) {
        if (saleData.getAmountCashOut() != null) {
            return Arrays.asList(SALE_WITH_CASH_OUT,
                    saleData.getAmount().toString(),
                    generateAmountCashOut(saleData),
                    generateDepartNumber(saleData.isFirstFiscalPrinter()),
                    generateRequestIdCommandParam(true));
        }
        return Arrays.asList(SALE,
                saleData.getAmount().toString(),
                CARD_TYPE_AUTO,
                generateTrack2Data(saleData),
                generateDepartNumber(saleData.isFirstFiscalPrinter()),
                generateRequestIdCommandParam(true));
    }

    private List<String> prepareRefundParameters(SaleData saleData) {
        return Arrays.asList(REFUND,
                saleData.getAmount().toString(),
                CARD_TYPE_AUTO,
                generateTrack2Data(saleData),
                generateRRN(saleData),
                generateDepartNumber(saleData.isFirstFiscalPrinter()),
                generateRequestIdCommandParam(true));
    }

    /**
     * Получить строку с суммой выдачи наличных.
     *
     * @param saleData - исходные данные для выполнения транзакции
     * @return строка с параметром для передачи в sb_pilot {@code "/a=<сумма 12 цифр с лидирующими нулями>"} или {@code null} если нет данных по выдаче
     */
    private String generateAmountCashOut(SaleData saleData) {
        return saleData.getAmountCashOut() != null ? String.format("/a=%012d", saleData.getAmountCashOut()) : null;
    }

    /**
     * Получить данные второй дорожки.
     *
     * @param saleData - исходные данные для выполнения транзакции
     * @return строка с параметром для передачи в sb_pilot {@code "/t;<данные второй дорожки>?"} или {@code "0"} если данных нет
     */
    private String generateTrack2Data(SaleData saleData) {
        return (saleData.getCard() != null && StringUtils.isNotBlank(saleData.getCard().getTrack2())) ?
                "/t;" + StringUtils.trim(saleData.getCard().getTrack2()) + "?" : "0";
    }

    /**
     * Получить строку для передачи номера отдела. {@code "/d=1"} or {@code null}
     *
     * @param isFirstFiscalPrinter saleData.isFirstFiscalPrinter - оплата по первому отделу?
     * @return строка с параметром для передачи в sb_pilot номера отдела или {@code null} если один фискальник
     */
    private String generateDepartNumber(Boolean isFirstFiscalPrinter) {
        if (isFirstFiscalPrinter == null || StringUtils.isEmpty(getFirstFiscalPrinterDepartment())
                || StringUtils.isEmpty(getSecondFiscalPrinterDepartment())) {
            return null;
        }
        return "/d=" + (isFirstFiscalPrinter ? getFirstFiscalPrinterDepartment() : getSecondFiscalPrinterDepartment());
    }

    /**
     * Получить строку для передачи RRN. Используется для выполнения обратных и дополнительных операций
     * (возврат, отмена, предавторизация, завершение расчета, отмена авторизации).
     *
     * @param saleData - исходные данные для выполнения транзакции
     * @return строка с параметром для передачи RRN в sb_pilot или {@code null} если RRN не указан
     */
    private String generateRRN(SaleData saleData) {
        return StringUtils.isNotBlank(((RefundData) saleData).getRefNumber()) ? ((RefundData) saleData).getRefNumber() : null;
    }

    private String getEndOfSlipString() {
        return endOfSlipString;
    }

    public void setEndOfSlipString(String endOfSlipString) {
        this.endOfSlipString = endOfSlipString;
    }

    private String getSlipDelimiter() {
        return slipDelimiter;
    }

    public void setSlipDelimiter(String slipDelimiter) {
        this.slipDelimiter = slipDelimiter;
    }

    @Override
    public boolean isUseTerminalCardReader() {
        return isUseTerminalCardReader;
    }

    public void setUseTerminalCardReader(boolean isUseTerminalCardReader) {
        this.isUseTerminalCardReader = isUseTerminalCardReader;
    }

    private String getFirstFiscalPrinterDepartment() {
        return firstFiscalPrinterDepartment;
    }

    public void setFirstFiscalPrinterDepartment(String firstFiscalPrinterDepartment) {
        this.firstFiscalPrinterDepartment = firstFiscalPrinterDepartment;
    }

    private String getSecondFiscalPrinterDepartment() {
        return secondFiscalPrinterDepartment;
    }

    public void setSecondFiscalPrinterDepartment(String secondFiscalPrinterDepartment) {
        this.secondFiscalPrinterDepartment = secondFiscalPrinterDepartment;
    }

    private void setExecutableFileNameForWindows() {
        // если уже есть executableFileName - значит он пришел из конфига, будем использовать его
        if (getExecutableFileName() == null) {
            // попробуем файлы со старыми именами
            for (String fileName : EXEC_WINDOWS) {
                File defaultProcessingFile = new File(getFullPathToProcessingFolder() + fileName);
                if (defaultProcessingFile.exists()) {
                    setExecutableFileName(fileName);
                    return;
                }
            }
            throw new IllegalStateException("Executable file for Sberbank processing not found! Checked files: " +
                    String.join(", ", EXEC_WINDOWS));
        }
    }

    private void setExecutableFileNameForLinux() {
        // если уже есть executableFileName - значит он пришел из конфига, будем использовать его
        if (getExecutableFileName() == null) {
            setExecutableFileName(EXEC_LINUX);
        }
    }

    public void setCustomExecutableFileName(String customExecutableFileName) {
        setExecutableFileName(customExecutableFileName);
    }

    //Method for tests

    protected File getExecutableFile() {
        return new File(getFullPathToExecutableFile());
    }

    @Override
    public synchronized String getClientRequisites(ClientRequisitesType type) throws BankCommunicationException {
        log.info("getClientRequisites type: {}", type);
        List<String> command = null;
        switch (type) {
            case EMAIL: {
                command = Arrays.asList(ENTER_CLIENT_REQUISITES, "-/o=as-email", generateRequestIdCommandParam(false));
                break;
            }
            case PHONE_NUMBER: {
                command = Arrays.asList(ENTER_CLIENT_REQUISITES, "-/o=as-phone", generateRequestIdCommandParam(false));
                break;
            }
            case PINPAD: {
                command = null;
                break;
            }
            default:
                break;
        }
        runExecutable(command);
        List<String> resp = readResponseFile();

        String requisites;

        for (String r : resp) {
            requisites = r.trim();
            if (type == ClientRequisitesType.PINPAD && requisites.length() > 0) {
                return resp.get(18);
            }
            if (requisites.length() > 0) {
                log.info("Entered client requisites: {}", requisites);
                return requisites;
            }
        }
        log.info("Client requisites is empty");
        return null;
    }

    public boolean isNeedGenerateRequestId() {
        return needGenerateRequestId;
    }

    @Override
    public void setNeedGenerateRequestId(boolean needGenerateRequestId) {
        this.needGenerateRequestId = needGenerateRequestId;
    }

    /**
     * Генерация параметра requestId для команды
     *
     * @return /q=[hex]
     */
    protected String generateRequestIdCommandParam(boolean isNeedSaveRequestId) {
        return isNeedGenerateRequestId() ? "/q=" + generateRequestId(isNeedSaveRequestId) : null;
    }

    private String generateRequestId(boolean isNeedSaveRequestId) {
        String requestId = Long.toHexString(0x80000000 | System.currentTimeMillis()).substring(8);
        if (isNeedSaveRequestId) {
            lastRequestId = requestId;
        }
        return requestId;
    }

    @Override
    public void addDialogListener(BankDialogEvent bankDialogListener) {
        dialogListener.addServiceOperationListener(bankDialogListener);
    }

    /**
     * Если нажали в диалоге левую кнопку
     */
    @Override
    public void sendDialogResponse(BankDialogType dialogType, String response) {
        waitDialog.set(false);
        isNeedRepeat.set(true);
    }

    /**
     * Если нажали в диалоге правую кнопку
     */
    @Override
    public void closeDialog() {
        waitDialog.set(false);
        isNeedRepeat.set(false);
        dialogListener.removeBankListeners();
    }

    @Override
    public boolean isCancelAtBankAvailable() {
        return true;
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return Stream.of(BankPaymentType.CARD, BankPaymentType.CARD_CASHOUT)
                .collect(Collectors.toSet());
    }
}
