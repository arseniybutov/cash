package ru.crystals.pos.fiscalprinter.retailforce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResourceAccessException;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.ExtraFiscalDocType;
import ru.crystals.pos.fiscalprinter.FiscalConnector;
import ru.crystals.pos.fiscalprinter.RegulatoryFeatures;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.ReportCounters;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.ValueAddedTaxCollection;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.LongExtended;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.retailforce.api.RFApiConnector;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.BusinessTransactionType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.Document;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPayment;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPosition;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionBase;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionBooking;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionItem;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionReference;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionTotal;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentPositionType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentReference;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.DocumentType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalClientState;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalClientStatus;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalResponse;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.FiscalResponseAdditionalFields;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.PaymentTypes;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.ReferenceType;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.User;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayDocumentResponse;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayPosition;
import ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses.EndOfDayPositionType;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxInfo;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxes;
import ru.crystals.pos.i18n.Country;
import ru.crystals.pos.i18n.I18nConfig;
import ru.crystals.pos.property.Properties;
import ru.crystals.set10dto.TaxVO;
import ru.crystals.util.JsonMappers;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.NetworkInterface;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.crystals.pos.fiscalprinter.retailforce.RFUtils.asMoney;
import static ru.crystals.pos.fiscalprinter.retailforce.RFUtils.asQuantity;
import static ru.crystals.pos.fiscalprinter.retailforce.RFUtils.convertPositionNumber;

@PrototypedComponent
public class RFFiscalConnector implements FiscalConnector, Configurable<RetailForceConfig> {

    private static final Logger log = LoggerFactory.getLogger(RFFiscalConnector.class);

    private static final String[] TAX_INDEXES = new String[]{"A", "B", "C", "D", "E", "F", "G"};
    private static final Map<Country, Integer> ZERO_TAX_IDS = ImmutableMap.<Country, Integer>builder()
                                                                                    .put(Country.DE, 5)
                                                                                    .put(Country.AT, 4)
                                                                                    .build();

    private static final RegulatoryFeatures REGULATORY_FEATURES = RegulatoryFeatures.defaultFeaturesTemplate()
            .fiscalDocTypes(ExtraFiscalDocType.onlyMoney())
            .build();
    private static final String RF_DATA = "rf.data";
    private static final int MAX_ITEM_NAME_LENGTH = 40;
    private static final String DEFAULT_HASH_ALGORITHM = "ecdsa-plain-SHA256";
    private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();

    private final Properties properties;
    private final I18nConfig i18nConfig;

    private RetailForceConfig config;
    private RFApiConnector api;
    private String clientId;
    private String factoryNumber;
    private RFCounters counters;
    private List<TaxVO> taxes;
    private int zeroTaxId;

    private Map<Float, Integer> taxesByRate;

    private RandomSource randomSource;

    @Autowired
    public RFFiscalConnector(Properties properties, I18nConfig i18nConfig) {
        this(properties, i18nConfig, new RandomSource());
    }

    public RFFiscalConnector(Properties properties, I18nConfig i18nConfig, RandomSource randomSource) {
        this.properties = properties;
        this.i18nConfig = i18nConfig;
        this.randomSource = randomSource;
    }

    @Override
    public Class<RetailForceConfig> getConfigClass() {
        return RetailForceConfig.class;
    }

    @Override
    public void setConfig(RetailForceConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws FiscalPrinterException {
        start(new RFApiConnector(config));
    }

    void start(RFApiConnector connector) throws FiscalPrinterCommunicationException {
        this.api = connector;
        initialize();
    }

    @Override
    public RegulatoryFeatures regulatoryFeatures() {
        return REGULATORY_FEATURES;
    }

    private void initialize() throws FiscalPrinterCommunicationException {
        if (config.getStoreNumber() == null) {
            config.setStoreNumber(String.valueOf(properties.getShopIndex()));
        }
        if (config.getTerminalNumber() == null) {
            config.setTerminalNumber(String.valueOf(properties.getCashNumber()));
        }
        if (config.getDeviceId() == null) {
            config.setDeviceId(getLocalMacAddress());
        }

        log.debug("Configuration: {}", config);

        factoryNumber = String.format("%s__%s", config.getStoreNumber(), config.getTerminalNumber());

        try {
            final String rfVersion = api.getVersion();
            log.debug("RetailForce: {}", rfVersion);
        } catch (ResourceAccessException rae) {
            log.error("Unable to connect to RetailForce", rae);
            throw new FiscalPrinterCommunicationException(ResBundleRetailForce.getString("CONNECTION_ERROR"));
        }
        try {
            clientId = getClientIdOrRegister();
            api.connect(clientId);

            final FiscalClientStatus status = api.getStatus(clientId);
            log.debug("Fiscal client state: {}", status.getState());
            if (status.getState() == FiscalClientState.NOTINITIALIZED) {
                api.initialize(makeInitDoc());
            }
        } catch (Exception rae) {
            log.error("Unable to initialize", rae);
            throw new FiscalPrinterCommunicationException(ResBundleRetailForce.getString("CONNECTION_ERROR"));
        }

        counters = new RFCounters(clientId);

        taxes = fillTaxes();
        taxesByRate = fillTaxesByRate();
        zeroTaxId = ZERO_TAX_IDS.getOrDefault(i18nConfig.getCountry(), ZERO_TAX_IDS.get(Country.DE));
    }

    private String getLocalMacAddress() {
        StringBuilder strMac = new StringBuilder();
        try {
            Enumeration<NetworkInterface> ints = NetworkInterface.getNetworkInterfaces();
            NetworkInterface network = null;
            while (ints.hasMoreElements()) {
                network = ints.nextElement();
                if (!network.isLoopback() && network.getHardwareAddress() != null) {
                    break;
                }
            }
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                for (byte i : mac) {
                    strMac.append(Integer.toHexString(i));
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return strMac.toString();
    }

    @NonNull
    private String getClientIdOrRegister() {
        final String licenseConsumerId = api.getLicenseConsumerId();
        log.debug("licenseConsumerId = {}", licenseConsumerId);

        if (licenseConsumerId == null) {
            log.debug("Need to register");
            api.registerClient();
            return api.getClientId(api.getLicenseConsumerId());
        } else {
            final String clientId = api.getClientId(licenseConsumerId);
            log.debug("clientId = {}", clientId);

            if (clientId != null) {
                return clientId;
            }
            log.debug("Need to register");
            api.registerClient();
        }

        return api.getClientId(licenseConsumerId);
    }

    private Document makeInitDoc() {
        return makeDocTemplate()
                .documentType(DocumentType.NULL_RECEIPT)
                .fiscalDocumentNumber(1)
                .fiscalDocumentRevision(1)
                .fiscalDocumentStartTime(Instant.now().getEpochSecond())
                .user(new User("1", "System"))
                .documentTypeCaption("Startbeleg").build();
    }

    private Document.DocumentBuilder makeDocTemplate() {
        final String documentId = randomSource.getUuid();
        final OffsetDateTime date = randomSource.now();
        return Document.builder()
                .uniqueClientId(clientId)
                .documentId(documentId)
                .documentGuid(documentId)
                .createDate(date)
                .bookDate(date);
    }

    @Override
    public String getRegNum() {
        return clientId;
    }

    @Override
    public String getFNNum() {
        return clientId;
    }

    @Override
    public String getFactoryNum() {
        return factoryNumber;
    }

    @Override
    public void openShift(Cashier cashier) {
        counters.openShift();
    }

    @Override
    public long getShiftNum() {
        return counters.getShiftNumber();
    }

    @Override
    public long getLastFiscalDocId() {
        return counters.getDocNumber();
    }

    @Override
    public long getCashAmount() throws FiscalPrinterException {
        final List<DocumentPayment> actualStock;
        try {
            actualStock = api.getActualStock(clientId);
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleRetailForce.getString("CONNECTION_ERROR"), CashErrorType.FISCAL_ERROR);
        }
        if (actualStock == null) {
            return 0L;
        }
        final BigDecimal cashAmount = actualStock.stream()
                .map(DocumentPayment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return BigDecimalConverter.convertMoney(cashAmount);
    }

    @Override
    public boolean isShiftOpen() {
        return counters.isShiftOpened();
    }

    @Override
    public ShiftCounters getShiftCounters() throws FiscalPrinterException {
        final ShiftCounters shiftCounters = new ShiftCounters();
        shiftCounters.setShiftNum(getShiftNum());
        shiftCounters.setSumCashEnd(getCashAmount());
        if (clientId == null) {
            return shiftCounters;
        }
        final EndOfDayDocumentResponse endOfDayDocument = api.getEndOfDayDocument(clientId);

        final Map<BusinessTransactionType, List<EndOfDayPosition>> byType =
                endOfDayDocument.getPositions().stream().filter(p -> p.getType() == EndOfDayPositionType.BOOKING)
                        .collect(Collectors.groupingBy(EndOfDayPosition::getBusinessTransactionType));

        shiftCounters.setSumCashOut(getMoneySum(byType.get(BusinessTransactionType.PAY_OUT)));
        shiftCounters.setSumCashIn(getMoneySum(byType.get(BusinessTransactionType.PAY_IN)));

        final Map<Integer, BigDecimal> sumBySign = byType.getOrDefault(BusinessTransactionType.REVENUE, Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(p -> p.getGrossValue().signum(),
                        Collectors.mapping(EndOfDayPosition::getGrossValue, Collectors.reducing(BigDecimal.ZERO,
                                BigDecimal::add))));

        shiftCounters.setSumSale(BigDecimalConverter.convertMoney(sumBySign.getOrDefault(1, BigDecimal.ZERO)));
        shiftCounters.setSumReturn(BigDecimalConverter.convertMoney(sumBySign.getOrDefault(-1, BigDecimal.ZERO).abs()));

        return shiftCounters;
    }

    private long getMoneySum(List<EndOfDayPosition> money) {
        if (money == null || money.isEmpty()) {
            return 0L;
        }
        return BigDecimalConverter.convertMoney(money.get(0).getGrossValue().abs());
    }

    @Override
    public String getDeviceName() {
        return "RetailForce";
    }

    @Override
    public void processCopyDocument(FiscalDocument document) {
        final Map<String, Object> printedMap = document.getMap();
        printedMap.putAll(makeCommonMap());
        if (document instanceof Check) {
            final Check check = (Check) document;
            getData(check).map(this::toPrintedMap).ifPresent(printedMap::putAll);
            printedMap.put(PurchaseTaxes.DE_TAXES_FIELD, addCheckTaxes(check));
            printedMap.putAll(makeCommonMap());
        }
    }

    private Optional<RFPurchaseData> getData(Check check) {
        final String content = check.getFiscalizationValuesMap().get(RF_DATA);
        if (content == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(JsonMappers.getDefaultMapper().readValue(content, RFPurchaseData.class));
        } catch (IOException e) {
            log.error("Error on parse data: {}", content, e);
            return Optional.empty();
        }
    }

    @Override
    public void registerCheck(Check check) throws FiscalPrinterException {
        try {
            registerCheckInner(check);
            counters.incDocNumber();
        } catch (Exception e) {
            log.error("Error on register check", e);
            throw new FiscalPrinterException(ResBundleRetailForce.getString("DOC_REG_ERROR"), CashErrorType.FISCAL_ERROR);
        }
    }

    private void registerCheckInner(Check check) throws JsonProcessingException {
        final boolean negative = (check.getType() == CheckType.SALE && check.getOperation() == CashOperation.EXPENSE)
                || (check.getType() == CheckType.RETURN && check.getOperation() == CashOperation.INCOME);

        final Document.DocumentBuilder documentBuilder = makeDocTemplate()
                .user(new User(check.getCashier().getTabNum(), check.getCashier().getNullSafeName()))
                .documentType(DocumentType.RECEIPT);

        final Check superCheck = check.getSuperCheck();
        final DocumentReference positionReference;

        final RFPurchaseData originalCheckData = Optional.ofNullable(superCheck).flatMap(this::getData).orElse(null);

        if (originalCheckData != null) {
            final String docId = originalCheckData.getDocumentId();
            final Integer fiscalDocumentNumber = originalCheckData.getFiscalNumber();
            final String terminalNumber = Optional.ofNullable(originalCheckData.getTerminal())
                    .orElseGet(() -> String.valueOf(superCheck.getCashNumber()));
            final String storeNumber = Optional.ofNullable(originalCheckData.getStore())
                    .orElseGet(() -> String.valueOf(superCheck.getShopIndex()));

            final DocumentReference reference = DocumentReference.builder()
                    .documentId(docId)
                    .documentGuid(docId)
                    .fiscalDocumentNumber(fiscalDocumentNumber)
                    .referenceType(ReferenceType.CANCELLATION)
                    .storeNumber(storeNumber)
                    .terminalNumber(terminalNumber)
                    .documentType(DocumentType.RECEIPT)
                    .documentBookDate(originalCheckData.getBookDate())
                    .build();
            if (check.getCheckSumStart().equals(superCheck.getCheckSumStart())) {
                positionReference = null;
                documentBuilder.cancellationDocument(true);
                documentBuilder.documentReference(reference);
            } else {
                positionReference = reference;
            }
        } else {
            positionReference = null;
        }

        final List<DocumentPosition> positions = check.getGoods().stream()
                .map(goods -> mapCheckPosition(negative, goods, positionReference))
                .collect(Collectors.toCollection(ArrayList::new));

        final DocumentPositionTotal total = new DocumentPositionTotal();
        total.setCommon(DocumentPositionBase.builder()
                .positionNumber(positions.size())
                .type(DocumentPositionType.TOTAL)
                .build());
        total.setValue(asMoney(check.getCheckSumEnd(), negative));
        total.setBaseValue(total.getValue());

        positions.add(total);

        documentBuilder.positions(positions);

        final ArrayList<DocumentPayment> payments = new ArrayList<>();
        for (Payment payment : check.getPayments()) {
            payments.add(DocumentPayment.builder()
                    .currencyIsoCode("EUR")
                    .amount(asMoney(payment.getSum(), negative))
                    .paymentType(payment.getIndexPayment() == PaymentType.NonFFDFiscalType.PAYMENT_FISCAL_INDEX_CASH.getIndex() ?
                            PaymentTypes.CASH : PaymentTypes.EC_CARD)
                    .build());
            if (payment.isContainsChange()) {
                // сдача
                payments.add(DocumentPayment.builder()
                        .currencyIsoCode("EUR")
                        .isCash(true)
                        .amount(asMoney(payment.getChangeSum(), true))
                        .paymentType(PaymentTypes.CASH)
                        .build());
            }
        }
        documentBuilder.payments(payments);

        final FiscalResponse fiscalResponse = api.create(clientId, DocumentType.RECEIPT);
        fillFiscalResponse(documentBuilder, fiscalResponse);
        final Document doc = documentBuilder.build();
        final FiscalResponse fiscalResponseFinal = api.storeDocument(doc);
        final RFPurchaseData data = makePurchaseData(check, doc, fiscalResponseFinal);

        check.getFiscalizationValuesMap().put(RF_DATA, JsonMappers.getDefaultMapper().writeValueAsString(data));

        final Map<String, Object> printedMap = check.getMap();
        printedMap.putAll(toPrintedMap(data));
        printedMap.put(PurchaseTaxes.DE_TAXES_FIELD, addCheckTaxes(check));
        printedMap.putAll(makeCommonMap());
        if (originalCheckData != null) {
            printedMap.put("cashserialnumber", originalCheckData.getDeviceId());
        }
    }

    private RFPurchaseData makePurchaseData(Check check, Document doc, FiscalResponse fiscalResponseFinal) {
        final FiscalResponseAdditionalFields additionalFields = fiscalResponseFinal.getAdditionalFields();
        final RFPurchaseData data = new RFPurchaseData();
        data.setBookDate(doc.getBookDate());
        data.setDocumentId(doc.getDocumentId());
        data.setFiscalNumber(doc.getFiscalDocumentNumber());
        if (!config.getStoreNumber().equals(String.valueOf(check.getShopIndex()))) {
            data.setStore(config.getStoreNumber());
        }
        if (!config.getTerminalNumber().equals(String.valueOf(check.getCashNumber()))) {
            data.setTerminal(config.getTerminalNumber());
        }
        data.setDeviceId(config.getDeviceId());
        data.setSignature(fiscalResponseFinal.getSignature());
        data.setQr(additionalFields.getQrCodeDataString());
        Long startTime = additionalFields.getTransactionStartTime();
        Long finishTime = additionalFields.getTransactionEndTime();
        data.setTxStartTime(startTime);
        if (startTime != finishTime) {
            data.setTxFinishTime(finishTime);
        }

        data.setPublicKey(additionalFields.getTsePublicKey());
        data.setSignatureCounter(additionalFields.getTseSignatureCounter());
        final String tseHashAlgorithm = additionalFields.getTseHashAlgorithm();
        if (tseHashAlgorithm != null && !DEFAULT_HASH_ALGORITHM.equalsIgnoreCase(tseHashAlgorithm)) {
            data.setHashAlgorithm(tseHashAlgorithm);
        }
        return data;
    }

    private Map<String, Object> toPrintedMap(RFPurchaseData data) {
        final Map<String, Object> map = new HashMap<>();
        Optional.ofNullable(data.getQr()).map(v -> map.put("fiscalbarcode", v));
        map.put("timeformat", "utcTime");
        map.put("signature", data.getSignature());
        if (data.getTxStartTime() != null) {
            final String startTime = fromEpoch(data.getTxStartTime());
            map.put("datatimestarttransaction", startTime);
            map.put("datatimestoptransaction", Optional.ofNullable(data.getTxFinishTime())
                    .map(this::fromEpoch)
                    .orElse(startTime));
        }
        map.put("signaturecounter", data.getSignatureCounter());
        map.put("hashalgorithm", Optional.ofNullable(data.getHashAlgorithm()).orElse(DEFAULT_HASH_ALGORITHM));
        map.put("publickey", data.getPublicKey());
        map.put("transactionnumber", data.getDocumentId());
        return map;
    }

    private String fromEpoch(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime().format(TIME_FORMAT);
    }

    private DocumentPositionItem mapCheckPosition(boolean negative, Goods goods, DocumentReference positionReference) {
        final BigDecimal baseNetValue = asMoney(CurrencyUtil.calculateNdsSum(goods.getEndPricePerUnit(), goods.getTax()), negative);
        final BigDecimal baseGrossValue = asMoney(goods.getEndPricePerUnit(), negative);
        final BigDecimal grossValue = asMoney(goods.getEndPositionPrice(), negative);
        final BigDecimal taxValue = asMoney(goods.getTaxSum(), negative);
        final DocumentPositionBase.DocumentPositionBaseBuilder commonBuilder = DocumentPositionBase.builder()
                .positionNumber(convertPositionNumber(goods.getPositionNum()))
                .type(DocumentPositionType.ITEM);
        if (positionReference != null) {
            commonBuilder.positionReference(new DocumentPositionReference(positionReference, convertPositionNumber(goods.getPositionNumInOriginal())));
            commonBuilder.cancellationPosition(true);
        }
        final DocumentPositionItem.DocumentPositionItemBuilder builder = DocumentPositionItem.builder()
                .common(commonBuilder.build())
                .itemCaption(StringUtils.left(goods.getName(), MAX_ITEM_NAME_LENGTH))
                .quantity(asQuantity(goods.getQuant(), negative))
                .itemId(goods.getItem())
                .vatIdentification(getTaxId(goods))
                .vatPercent(BigDecimalConverter.convertMoney(goods.getTax()))

                .baseNetValue(baseGrossValue.subtract(baseNetValue))
                .baseGrossValue(baseGrossValue)
                .baseTaxValue(baseNetValue)

                .netValue(grossValue.subtract(taxValue))
                .grossValue(grossValue)
                .taxValue(taxValue);

        return builder
                .build();
    }


    private int getTaxId(Goods goods) {
        return taxesByRate.getOrDefault(goods.getTax(), 1);
    }


    @Override
    public void registerReport(Report report) throws FiscalPrinterException {
        updateReportParams(report);
        if (!report.isZReport()) {
            return;
        }
        try {
            final EndOfDayDocumentResponse endOfDayDocument = api.getEndOfDayDocument(clientId);

            if (endOfDayDocument.getPayments().isEmpty()) {
                log.debug("Closing empty shift");
                counters.closeShift();
                return;
            }

            final Document.DocumentBuilder documentBuilder = makeDocTemplate()
                    .user(new User(report.getCashier().getTabNum(), report.getCashier().getNullSafeName()))
                    .documentType(DocumentType.END_OF_DAY);

            final FiscalResponse fiscalResponse = api.create(clientId, DocumentType.END_OF_DAY);
            fillFiscalResponse(documentBuilder, fiscalResponse);

            api.storeDocument(documentBuilder.build());

        } catch (Exception e) {
            log.error("Error on register report", e);
            throw new FiscalPrinterException(ResBundleRetailForce.getString("DOC_REG_ERROR"));
        }
        counters.closeShift();
    }

    private void fillFiscalResponse(Document.DocumentBuilder documentBuilder, FiscalResponse fiscalResponse) {
        documentBuilder
                .fiscalDocumentNumber(fiscalResponse.getFiscalisationDocumentNumber())
                .fiscalDocumentRevision(fiscalResponse.getFiscalisationDocumentRevision())
                .fiscalDocumentStartTime(fiscalResponse.getFiscalDocumentStartTime())
                .fiscalResponse(fiscalResponse);
    }

    private void updateReportParams(Report report) {
        ReportCounters repCounters = report.getReportCounters();
        final Map map = report.getMap();
        map.put("totalcountcheck", new LongExtended(repCounters.getCountSale() + repCounters.getCountReturn()));
        map.putAll(makeCommonMap());
    }

    private Map<String, Object> makeCommonMap() {
        return ImmutableMap.of(
                "taxpayerid", config.getIdentification(),
                "cashserialnumber", config.getDeviceId()
        );
    }

    @Override
    public void registerMoneyOperation(Money money) throws FiscalPrinterException {
        try {
            registerMoneyInner(money);
        } catch (Exception e) {
            log.error("Error on register money document", e);
            throw new FiscalPrinterException(ResBundleRetailForce.getString("DOC_REG_ERROR"));
        }
    }

    private void registerMoneyInner(Money money) {
        final boolean cashOut = (money.getOperationType() == InventoryOperationType.CASH_OUT);

        final BigDecimal value = asMoney(money.getValue(), cashOut);

        final DocumentType documentType = cashOut ? DocumentType.PAYOUT : DocumentType.PAYIN;
        final Document.DocumentBuilder documentBuilder = makeDocTemplate()
                .user(new User(money.getCashier().getTabNum(), money.getCashier().getNullSafeName()))
                .documentType(documentType)
                .positions(
                        Collections.singletonList(
                                DocumentPositionBooking.builder()
                                        .common(DocumentPositionBase.builder()
                                                .positionNumber(0)
                                                .type(DocumentPositionType.BOOKING)
                                                .build())
                                        .grossValue(value)
                                        .netValue(value)
                                        .taxValue(BigDecimal.ZERO)
                                        .businessTransactionType(cashOut ? BusinessTransactionType.PAY_OUT : BusinessTransactionType.PAY_IN)
                                        .vatIdentification(zeroTaxId)
                                        .caption(cashOut ? "Pay out" : "Pay in")
                                        .build()
                        )
                )
                .payments(Collections.singletonList(
                        DocumentPayment.builder()
                                .isCash(true)
                                .currencyIsoCode("EUR")
                                .amount(value)
                                .paymentType(PaymentTypes.CASH)
                                .build()
                ));

        final FiscalResponse fiscalResponse = api.create(clientId, documentType);
        fillFiscalResponse(documentBuilder, fiscalResponse);

        final Document doc = documentBuilder.build();

        api.storeDocument(doc);
        counters.incDocNumber();

        money.getMap().putAll(makeCommonMap());
    }

    @Override
    public ValueAddedTaxCollection getTaxes() {
        ValueAddedTaxCollection taxes = new ValueAddedTaxCollection();

        switch (i18nConfig.getCountry()) {
            case AT:
                taxes.addTax(new ValueAddedTax(1, 20.00f, "20%"));
                taxes.addTax(new ValueAddedTax(2, 10.00f, "10%"));
                taxes.addTax(new ValueAddedTax(3, 13.00f, "13%"));
                taxes.addTax(new ValueAddedTax(ZERO_TAX_IDS.get(Country.AT), 0.00f, "0%"));
                taxes.addTax(new ValueAddedTax(5, 19.00f, "19%"));
                break;
            case DE:
            default:
                taxes.addTax(new ValueAddedTax(1, 19.00f, "19%"));
                taxes.addTax(new ValueAddedTax(2, 7.00f, "7%"));
                taxes.addTax(new ValueAddedTax(3, 10.70f, "10.7%"));
                taxes.addTax(new ValueAddedTax(4, 5.50f, "5.5%"));
                taxes.addTax(new ValueAddedTax(ZERO_TAX_IDS.get(Country.DE), 0.0f, "0%"));
                taxes.addTax(new ValueAddedTax(6, 16.00f, "16%"));
                taxes.addTax(new ValueAddedTax(7, 5.00f, "5%"));
                break;
        }

        return taxes;
    }

    private List<TaxVO> fillTaxes() {
        switch (i18nConfig.getCountry()) {
            case AT:
                return Arrays.asList(new TaxVO(1, 2000L, "20%"),
                        new TaxVO(2, 1000L, "10%"),
                        new TaxVO(3, 1300L, "13%"),
                        new TaxVO(ZERO_TAX_IDS.get(Country.AT), 0L, "0%"),
                        new TaxVO(5, 1900L, "19%")
                );
            case DE:
            default:
                return Arrays.asList(new TaxVO(1, 1900L, "19%"),
                        new TaxVO(2, 700L, "7%"),
                        new TaxVO(3, 1070L, "10.7%"),
                        new TaxVO(4, 550L, "5.5%"),
                        new TaxVO(ZERO_TAX_IDS.get(Country.DE), 0L, "0%"),
                        new TaxVO(6, 1600L, "16%"),
                        new TaxVO(7, 500L, "5%")
                );
        }
    }

    private Map<Float, Integer> fillTaxesByRate() {
        switch (i18nConfig.getCountry()) {
            case AT:
                return ImmutableMap.<Float, Integer>builder()
                        .put(20.00f, 1)
                        .put(10.00f, 2)
                        .put(13.00f, 3)
                        .put(0.00f, ZERO_TAX_IDS.get(Country.AT))
                        .put(19.00f, 5)
                        .build();
            case DE:
            default:
                return ImmutableMap.<Float, Integer>builder()
                        .put(19.0f, 1)
                        .put(16.0f, 1)
                        .put(7.0f, 2)
                        .put(5.0f, 2)
                        .put(10.7f, 3)
                        .put(5.5f, 4)
                        .put(0.0f, ZERO_TAX_IDS.get(Country.DE))
                        .build();
        }
    }

    private PurchaseTaxes addCheckTaxes(Check check) {
        Map<String, Long> purchasesTaxes = new LinkedHashMap<>();
        for (TaxVO tax : taxes) {
            purchasesTaxes.put(tax.getCode(), 0L);
        }

        Map<String, PurchaseTaxInfo> purchaseTaxMap = new HashMap<>();
        List<PurchaseTaxInfo> taxesList = new ArrayList<>();
        final AtomicInteger taxIndex = new AtomicInteger(0);

        Long purchaseTax;
        PurchaseTaxInfo purchaseTaxInfo;
        for (Goods pos : check.getGoods()) {
            purchaseTaxInfo = purchaseTaxMap.computeIfAbsent(pos.getTaxName(), p -> {
                PurchaseTaxInfo pti = new PurchaseTaxInfo(TAX_INDEXES[taxIndex.getAndIncrement()], pos.getTaxName());
                taxesList.add(pti);
                return pti;
            });
            purchaseTaxInfo.add(pos.getTaxSum(), pos.getEndPositionPrice());
            pos.setTaxIndexName(purchaseTaxInfo.getTaxIndex());

            purchaseTax = purchasesTaxes.get(pos.getTaxName());
            if (purchaseTax == null) {
                log.error("Tax not found in tax table: {}", pos.getTaxName());
                continue;
            }
            purchaseTax += pos.getTaxSum();
            purchasesTaxes.put(pos.getTaxName(), purchaseTax);
        }

        PurchaseTaxes purchaseTaxes = new PurchaseTaxes();
        purchaseTaxes.setPurchaseTaxes(taxesList);
        return purchaseTaxes;
    }
}
