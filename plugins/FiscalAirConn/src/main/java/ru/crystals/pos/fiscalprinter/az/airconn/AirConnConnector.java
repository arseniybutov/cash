package ru.crystals.pos.fiscalprinter.az.airconn;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.ExtraFiscalDocType;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ReportCounters;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.az.airconn.audit.AuditConnector;
import ru.crystals.pos.fiscalprinter.az.airconn.audit.AuditHandler;
import ru.crystals.pos.fiscalprinter.az.airconn.audit.ProtectedCatalog;
import ru.crystals.pos.fiscalprinter.az.airconn.audit.ProtectedCatalogImpl;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.GetInfoCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.SaveSoftChecksumCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.ToLoginCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.VerifySoftChecksumCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.AuditCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.CloseShiftCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.CreateDocumentCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.GetShiftStatusCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.GetXReportCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized.OpenShiftCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.Currency;
import ru.crystals.pos.fiscalprinter.az.airconn.model.Item;
import ru.crystals.pos.fiscalprinter.az.airconn.model.ItemCodeType;
import ru.crystals.pos.fiscalprinter.az.airconn.model.QuantityTypes;
import ru.crystals.pos.fiscalprinter.az.airconn.model.Taxes;
import ru.crystals.pos.fiscalprinter.az.airconn.model.VatAmount;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AccessToken;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AuditData;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.LoginCredentials;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents.DocumentBody;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents.DocumentRequest;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents.DocumentTypes;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.documents.PaymentTypes;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.DocumentResponse;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.InfoData;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ReportData;
import ru.crystals.pos.fiscalprinter.az.airconn.transport.AirConnTransport;
import ru.crystals.pos.fiscalprinter.az.airconn.transport.MockTransport;
import ru.crystals.pos.fiscalprinter.az.airconn.transport.SocketTransport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.property.Properties;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/**
 * Используется в Азербайджане
 */
@PrototypedComponent
public class AirConnConnector implements FiscalConnector, AuditConnector, Configurable<AirConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(AirConnConnector.class);
    private static final String DOCUMENT_ID = "documentId";
    private static final String SHORT_DOCUMENT_ID = "shortDocumentId";
    private static final String FISCAL_BARCODE = "fiscalbarcode";
    /**
     * Если адрес off_line вместо подключения к службе используем эмулятор
     */
    private static final String OFF_LINE = "off_line";

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .fiscalDocTypes(ExtraFiscalDocType.onlyMoney())
            .skipMoneyDocumentOnExpiredShift(true)
            .canReturnFullLastDocInfo(false)
            .canMakeArbitraryRefund(false)
            .build();

    private AirConnTransport transport;
    private ValueAddedTaxCollection taxes;
    private InfoData lastInfoData;
    private ReportData lastReportData;
    private final LoginCredentials loginCredentials = new LoginCredentials();
    private Long storeNumber;

    private final AirConnKPKEmulator kpkEmulator = new AirConnKPKEmulator();
    private String auditPassword = "324012";
    private AuditHandler auditHandler;
    private ProtectedCatalog protectedCatalog;
    private AirConfig config;

    private final Properties properties;

    @Autowired
    public AirConnConnector(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Class<AirConfig> getConfigClass() {
        return AirConfig.class;
    }

    @Override
    public void setConfig(AirConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        loginCredentials.setPin(config.getPin());
        loginCredentials.setRole(config.getUserRole());
        loginCredentials.setCashRegisterFactoryNumber(config.getCashRegisterFactoryNumber());

        final String tokenAddress = config.getTokenAddress();
        transport = OFF_LINE.equals(tokenAddress) ? new MockTransport() : new SocketTransport();
        transport.init(tokenAddress);
        transport.connect();

        //Проверяем доступ к фискализатору и сохраняем регистрационные данные
        updateAirConnInfo();
        //Проверяем данные авторизции в AirConn
        String accessToken = getAccessToken();
        updateXReportData(accessToken);
        try {
            kpkEmulator.loadState();
        } catch (Exception e) {
            LOG.error("Unable to init counters", e);
            throw new FiscalPrinterException(e.getMessage());
        }
        storeNumber = properties.getShopIndex();
        try {
            if (auditHandler == null) {
                auditHandler = new AuditHandler(this);
                auditHandler.startWork(config.getAuditPort(), config.getAuditUrl(), auditPassword);
            }
            if (protectedCatalog == null) {
                protectedCatalog = new ProtectedCatalogImpl(Paths.get(config.getAuditPath()));
                protectedCatalog.init();
            }
        } catch (Exception e) {
            LOG.error("Unable to start audit module", e);
            throw new FiscalPrinterException(ResBundleFiscalAirConn.getString("INTERNAL_CASH_ERROR"));
        }
    }

    @Override
    public void stop() {
        transport.disconnect();
        transport.close();
        if (auditHandler != null) {
            auditHandler.stopWork();
        }
    }

    @Override
    public String getINN() {
        return lastInfoData.getCompanyTaxNumber();
    }

    @Override
    public String getRegNum() {
        return "AirConn";
    }

    @Override
    public String getFactoryNum() {
        return lastInfoData.getCashboxFactoryNumber();
    }

    @Override
    public FnInfo getFnInfo() throws FiscalPrinterException {
        FnInfo fnInfo = new FnInfo();
        fnInfo.setLastFDNumber(String.valueOf(getLastFiscalDocId()));
        String lastOnlineTime = updateAirConnInfo().getLastOnlineTime();
        lastOnlineTime = AirConnUtils.getDateTimeString(lastOnlineTime);
        fnInfo.setFirstNotSendedFDDate(lastOnlineTime);
        return fnInfo;
    }

    @Override
    public void openShift(Cashier cashier) throws FiscalPrinterException {
        if (!protectedCatalog.validate()) {
            LOG.error("Invalid protected files");
            throw new FiscalPrinterException(ResBundleFiscalAirConn.getString("INTERNAL_CASH_ERROR"));
        }
        String accessToken = getAccessToken();
        transport.executeCommand(new OpenShiftCommand(accessToken));
        updateXReportData(accessToken);
        kpkEmulator.incKPK();
    }

    @Override
    public long getShiftNum() throws FiscalPrinterException {
        return kpkEmulator.getShiftNum();
    }

    @Override
    public long getLastFiscalDocId() {
        return Optional.ofNullable(lastReportData).map(ReportData::getLastDocNumber).orElse(-1L);
    }

    @Override
    public long getCashInCount() {
        return Optional.ofNullable(lastReportData).map(ReportData::getCurrency).map(Currency::getDepositCount).orElse(-1L);
    }

    @Override
    public long getCashOutCount() {
        return Optional.ofNullable(lastReportData).map(ReportData::getCurrency).map(Currency::getWithdrawCount).orElse(-1L);
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        return kpkEmulator.getCashAmount();
    }

    @Override
    public boolean isShiftOpen() throws FiscalPrinterException {
        return isShiftOpen(getAccessToken());
    }

    private boolean isShiftOpen(String accessToken) throws FiscalPrinterException {
        return transport.executeCommand(new GetShiftStatusCommand(accessToken)).getResponseData().getShiftOpen();
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        try {
            updateXReportData(getAccessToken());
            ShiftCounters shiftCounters = new ShiftCounters();

            /* Номер смены */
            shiftCounters.setShiftNum(getShiftNum());

            /* Сумма наличных в денежном ящике */
            long sumCashEnd = getCashAmount();
            shiftCounters.setSumCashEnd(sumCashEnd);

            if (lastReportData != null && lastReportData.getCurrency() != null) {
                Currency currencyCountersAZN = lastReportData.getCurrency();

                /* Продажи */
                long sumCash = CurrencyUtil.convertMoney(currencyCountersAZN.getSaleCashSum());
                shiftCounters.setSumCashPurchase(sumCash);

                long sumCashless = CurrencyUtil.convertMoney(currencyCountersAZN.getSaleCashlessSum());
                long sumCredit = CurrencyUtil.convertMoney(currencyCountersAZN.getSaleCreditSum());
                shiftCounters.setSumCashlessPurchase(sumCashless + sumCredit);

                long sumTotal = CurrencyUtil.convertMoney(currencyCountersAZN.getSaleSum());
                shiftCounters.setSumSale(sumTotal);

                /* Возвраты */
                sumCash = CurrencyUtil.convertMoney(currencyCountersAZN.getMoneyBackCashSum());
                shiftCounters.setSumCashReturn(sumCash);

                sumCashless = CurrencyUtil.convertMoney(currencyCountersAZN.getMoneyBackCashlessSum());
                sumCredit = CurrencyUtil.convertMoney(currencyCountersAZN.getMoneyBackCreditSum());
                shiftCounters.setSumCashlessReturn(sumCashless + sumCredit);

                sumTotal = CurrencyUtil.convertMoney(currencyCountersAZN.getMoneyBackSum());
                shiftCounters.setSumReturn(sumTotal);

                /* Количество чеков по типам операций */
                shiftCounters.setCountSale(currencyCountersAZN.getSaleCount());
                shiftCounters.setCountReturn(currencyCountersAZN.getMoneyBackCount());
                shiftCounters.setCountCashIn(currencyCountersAZN.getDepositCount());
                shiftCounters.setCountCashOut(currencyCountersAZN.getWithdrawCount());

                long sumCashIn = CurrencyUtil.convertMoney(currencyCountersAZN.getDepositSum());
                shiftCounters.setSumCashIn(sumCashIn);
                long sumCashOut = CurrencyUtil.convertMoney(currencyCountersAZN.getWithdrawSum());
                shiftCounters.setSumCashOut(sumCashOut);
            }

            /* Количество оплат по чекам. Нет данных */
            shiftCounters.setCountCashPurchase(0L);
            shiftCounters.setCountCashlessPurchase(0L);
            shiftCounters.setCountCashReturn(0L);
            shiftCounters.setCountCashlessReturn(0L);

            LOG.debug("getShiftCounters result: {}", shiftCounters);
            return shiftCounters;
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalAirConn.getString("DEVICE_NAME");
    }

    @Override
    public void registerAnnulCheck(Check check) throws FiscalPrinterException {
        //В AirConn нефискальные документы не нужны, только увеличиваем счетчики
        kpkEmulator.incSPND();
    }

    @Override
    public void processCopyDocument(FiscalDocument document) throws FiscalPrinterException {
        //В AirConn нефискальные документы не нужны, только увеличиваем SPND
        kpkEmulator.incSPND();
        updateDocumentInfo(document, null);
    }

    @Override
    public void processNonFiscal(AbstractDocument document) throws FiscalPrinterException {
        //В AirConn нефискальные документы не нужны, только увеличиваем SPND
        kpkEmulator.incSPND();
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        try {
            String accessToken = getAccessToken();

            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setAccessToken(accessToken);
            documentRequest.setDocType(check.getType() == CheckType.RETURN ? DocumentTypes.MONEY_BACK : DocumentTypes.SALE);
            if (check.getType() == CheckType.RETURN) {
                documentRequest.getData().setParentDocument(check.getSuperCheck().getFiscalizationValuesMap().get(DOCUMENT_ID));
            }
            // add document information
            documentRequest.getData().setCashier(check.getCashier().getLastnameAndInitials());
            documentRequest.getData().setSum(BigDecimalConverter.convertMoney(check.getCheckSumEnd()));

            // add goods
            for (Goods good : check.getGoods()) {
                addPosition(documentRequest.getData(), good);
            }

            // add payments
            documentRequest.getData().initPayments();
            for (Payment payment : check.getPayments()) {
                addPayment(documentRequest.getData(), payment);
            }
            // Убираем сдачу т.к. по документации Sum = cashSum+cashlessSum+prepaymentSum+creditSum+bonusSum
            documentRequest.getData().removerChangeFromPayment();

            DocumentResponse fiscalDoc = transport.executeCommand(new CreateDocumentCommand(documentRequest)).getResponseData();
            check.getFiscalizationValuesMap().put(DOCUMENT_ID, fiscalDoc.getDocumentId());
            check.getFiscalizationValuesMap().put(SHORT_DOCUMENT_ID, fiscalDoc.getShortDocumentId());
            updateDocumentInfo(check, fiscalDoc.getDocumentId());
            long cashAmount = BigDecimalConverter.convertMoney(documentRequest.getData().getCashSum());
            kpkEmulator.incCashAmount(check.getType() == CheckType.RETURN ? -cashAmount : cashAmount);
            kpkEmulator.incKPK();
            kpkEmulator.incSPND();
            //обновляем данные по счетчикам
            updateXReportData(accessToken);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void updateDocumentInfo(FiscalDocument document, String documentID) {
        Map<String, Object> dataForPrint = document.getMap();
        Map<String, String> dataForSave = document.getFiscalizationValuesMap();
        dataForPrint.put("tokennumber", lastInfoData.getCashboxTaxNumber());
        if (dataForSave.get(SHORT_DOCUMENT_ID) != null) {
            dataForPrint.put("fiscalid", dataForSave.get(SHORT_DOCUMENT_ID));
        }
        dataForPrint.put("cashboxmodel", lastInfoData.getCashregisterModel());
        dataForPrint.put("taxpayername", lastInfoData.getObjectName());
        dataForPrint.put("taxpayerobject", lastInfoData.getObjectTaxNumber());
        if (documentID != null) {
            dataForPrint.put(DOCUMENT_ID.toLowerCase(), documentID);
            dataForPrint.put(FISCAL_BARCODE, lastInfoData.getQrCodeUrl().concat(documentID));
        }
        dataForPrint.put("cashboxfactorynum", loginCredentials.getCashRegisterFactoryNumber());
        dataForPrint.put("storenumber", storeNumber);

    }

    /**
     * Добавить товарную позицию в данные запроса документа AirConn
     *
     * @param documentBody - структура данных документа из запроса на фискализацию
     * @param good         - товарная позиция из кассового чека
     */
    private void addPosition(DocumentBody documentBody, Goods good) {
        LOG.debug("addPosition {} price: {} quantity: {} tax: {}", good.getName(), good.getEndPricePerUnit(), good.getQuant(), good.getTax());
        Item newItem = new Item();

        QuantityTypes quantityType = ProductDiscriminators.PRODUCT_WEIGHT_ENTITY.equals(good.getProductType()) ? QuantityTypes.WEIGHT : QuantityTypes.PIECE;
        setPositionItemCode(quantityType, good, newItem);
        newItem.setItemName(good.getName());
        newItem.setItemPrice(BigDecimalConverter.convertMoney(good.getEndPricePerUnit()));
        newItem.setItemQuantityType(quantityType.getId());
        newItem.setItemQuantity(BigDecimalConverter.convertQuantity(good.getQuant()));
        newItem.setItemSum(BigDecimalConverter.convertMoney(good.getEndPositionPrice()));
        documentBody.addItem(newItem);

        VatAmount itemVatAmount = new VatAmount();
        itemVatAmount.setVatSum(newItem.getItemSum());
        //Если процент налога безНДС "-1.0" он не указывается в VatAmount
        if (good.getTax() >= 0) {
            newItem.setItemVatPercent(good.getTax());
            itemVatAmount.setVatPercent(newItem.getItemVatPercent());
        }
        documentBody.addVatAmount(itemVatAmount);
    }

    private void setPositionItemCode(QuantityTypes quantityType, Goods good, Item item) {
        switch (quantityType) {
            case WEIGHT:
                item.setItemCodeType(ItemCodeType.PLAIN_TEXT.getId());
                item.setItemCode(good.getItem());
                break;
            case PIECE:
            default:
                item.setItemCodeType(ItemCodeType.getItemCodeTypeByBarcode(good.getBarcode()).getId());
                item.setItemCode(StringUtils.isBlank(good.getBarcode()) ? good.getItem() : good.getBarcode());
                break;
        }
    }

    /**
     * Добавление оплат в данные запроса документа AirConn
     *
     * @param documentBody - структура данных документа из запроса на фискализацию
     * @param payment      - платеж из кассового чека
     */
    private void addPayment(DocumentBody documentBody, Payment payment) throws FiscalPrinterException {
        LOG.debug("addPayment: {}, index({}), indexFFD100({})", payment.toString(), payment.getIndexPayment(), payment.getIndexPaymentFDD100());

        BigDecimal bigDecimalAmount = BigDecimalConverter.convertMoney(payment.getSum());
        PaymentTypes paymentType = PaymentTypes.typeFromCode(payment.getIndexPayment());
        switch (paymentType) {
            case PAYMENT_CASH:
                documentBody.addCashSum(bigDecimalAmount);
                break;
            case PAYMENT_CARD:
                documentBody.addCashlessSum(bigDecimalAmount);
                break;
            case PREPAYMENT:
                documentBody.addPrepaymentSum(bigDecimalAmount);
                break;
            case PAYMENT_CREDIT:
                documentBody.addCreditSum(bigDecimalAmount);
                break;
            case PAYMENT_BONUS:
                documentBody.addBonusSum(bigDecimalAmount);
                break;
            default:
                break;
        }
    }

    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        try {
            if (report.isZReport()) {
                fiscalizeZReport(report);
            } else if (report.isXReport()) {
                fiscalizeXReport(report);
            } else {
                processNonFiscal(report);
            }
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    private void fiscalizeZReport(Report report) throws FiscalPrinterException {
        lastReportData = transport.executeCommand(new CloseShiftCommand(getAccessToken())).getResponseData();
        kpkEmulator.incSPND();
        kpkEmulator.incKPK();
        kpkEmulator.incShiftNum();
        report.getMap().put("shiftcreatedatetime", AirConnUtils.getDateTimeString(lastReportData.getShiftOpenAtUtc()));
        report.getFiscalizationValuesMap().put(DOCUMENT_ID, lastReportData.getDocumentId());
        updateDocumentInfo(report, lastReportData.getDocumentId());
        updateReportInfo(report);
    }

    private void fiscalizeXReport(Report report) throws FiscalPrinterException {
        updateXReportData(getAccessToken());
        kpkEmulator.incSPND();

        updateDocumentInfo(report, null);
        updateReportInfo(report);
    }

    private void updateReportInfo(Report report) throws FiscalPrinterException {
        Map<String, Object> dataForPrint = report.getMap();
        final ReportCounters counters = report.getReportCounters();
        final long lastDocumentNum = kpkEmulator.getSPND();
        final long numberOfFirstCheck = lastDocumentNum
                - counters.getCountSale()
                - counters.getCountReturn()
                - counters.getCountCashIn()
                - counters.getCountCashOut()
                - counters.getCountAnnul()
                - counters.getCountCorrections();
        dataForPrint.put("numberdocumentfirst", numberOfFirstCheck);
        dataForPrint.put("numberdocumentlast", lastDocumentNum);
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        String accessToken = getAccessToken();

        DocumentRequest moneyRequest = new DocumentRequest();
        moneyRequest.setAccessToken(accessToken);
        moneyRequest.setDocType(money.getOperationType() == InventoryOperationType.CASH_IN ? DocumentTypes.DEPOSIT : DocumentTypes.WITHDRAW);

        moneyRequest.getData().setCashier(money.getCashier().getLastnameAndInitials());
        moneyRequest.getData().setSum(BigDecimalConverter.convertMoney(money.getValue()));

        transport.executeCommand(new CreateDocumentCommand(moneyRequest));

        kpkEmulator.incCashAmount(money.getOperationType() == InventoryOperationType.CASH_IN ? money.getValue() : -money.getValue());

        updateXReportData(accessToken);
        kpkEmulator.incSPND();
        updateDocumentInfo(money, null);
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        if (taxes != null) {
            return taxes;
        }

        taxes = new ValueAddedTaxCollection();
        taxes.addTax(new ValueAddedTax(Taxes.NDS18.getId(), Taxes.NDS18.getValue(), Taxes.NDS18.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NDS20.getId(), Taxes.NDS20.getValue(), Taxes.NDS20.name()));
        taxes.addTax(new ValueAddedTax(Taxes.NO_NDS.getId(), Taxes.NO_NDS.getValue(), Taxes.NO_NDS.name()));

        LOG.debug("getTaxes(): {}", taxes);
        return taxes;
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    /**
     * Авторизация в токене и получение ключа доступа. Рекомендуется обновлять ключ доступа
     * перед каждой требующей его операцией, т.к. в процессе работы службы авторизация может сбрасываться.
     * Каждый запрос ToLogin формирует новый AccessToken, старый становится невалидным
     *
     * @return Сессионный ключ доступа
     */
    private String getAccessToken() throws FiscalPrinterException {
        final AccessToken responseData = transport.executeCommand(new ToLoginCommand(loginCredentials)).getResponseData();
        if (responseData == null) {
            throw new IllegalStateException("No response data for ToLoginCommand");
        }
        if (StringUtils.isEmpty(responseData.getAccessToken())) {
            throw new IllegalStateException("No access token in response of ToLoginCommand");
        }
        return responseData.getAccessToken();
    }

    /**
     * Запрашивает данные {@link InfoData} из AirConn и сохраняет в поле lastInfoData коннектора
     *
     * @return регистрационные данные из AirConn
     */
    private InfoData updateAirConnInfo() throws FiscalPrinterException {
        lastInfoData = transport.executeCommand(new GetInfoCommand()).getResponseData();
        return lastInfoData;
    }

    /**
     * Запрашивает данные {@link ReportData} из AirConn и сохраняет в поле lastReportData коннектора
     * Работает только на открытой смене
     */
    private void updateXReportData(String accessToken) throws FiscalPrinterException {
        if (isShiftOpen(accessToken)) {
            LOG.info("updating AirConn counters");
            lastReportData = transport.executeCommand(new GetXReportCommand(accessToken)).getResponseData();
        }
    }

    /**
     * Отправка на сервер результата проведения процедуры аудита.
     * Данная операция формирует отчет о проведении проверки кассового оборудования.
     *
     * @param author имя инженера проводящего аудит
     */
    @Override
    public void softwareAudit(String author) throws FiscalPrinterException {
        AuditData data = new AuditData();
        data.setAccessToken(getAccessToken());
        data.setAuthor(author);
        transport.executeCommand(new AuditCommand(data));
    }

    @Override
    public void saveSoftwareChecksum() throws FiscalPrinterException {
        transport.executeCommand(new SaveSoftChecksumCommand());
    }

    @Override
    public void verifySoftwareChecksum() throws FiscalPrinterException {
        transport.executeCommand(new VerifySoftChecksumCommand());
    }

    @Override
    public String getFNNum() {
        if (config.getTokenReplacement() == null) {
            return "-no-";
        }
        return config.getTokenReplacement();
    }
}
