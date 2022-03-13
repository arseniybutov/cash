package ru.crystals.pos.fiscalprinter.de.fcc;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.json.DefaultJsonParser;
import ru.crystals.json.http.JsonHttpClient;
import ru.crystals.json.http.RemoteCallException;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCClient;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCGetRegistredClientsRequest;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCOpenTransactionsGet;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCOpenTransactionsResp;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCRegRequest;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCRegistredClientsResp;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTokenRequest;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTokenResp;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransaction;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransactionFinishRequest;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransactionFinishResp;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransactionGet;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransactionStartRequest;
import ru.crystals.pos.fiscalprinter.de.fcc.model.FCCTransactionStartResp;
import ru.crystals.pos.fiscalprinter.de.fcc.model.Request;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxInfo;
import ru.crystals.pos.fiscalprinter.templates.customer.vo.PurchaseTaxes;
import ru.crystals.set10dto.TaxVO;
import ru.crystals.set10dto.purchase.payments.PaymentTypes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author dalex
 *
 * Для истории:
 * login = "admin";
 * password = "password";
 * connectorAddress;
 * ersSecret = "88888888";
 * uniqueClientId = "93476g2083m-596ui098";
 * registrationToken = "272ed954-4f6d-4f62-873a-5027a31f936d";
 */
public class FCCImpl {

    private static final Logger LOG = LoggerFactory.getLogger(FCCImpl.class);

    private static final int MONEY_SCALE = 2;

    private static final String EUR_CURRENCY = "EUR";
    private static final String CLIENT_STATUS_ACTIVE = "active";
    private static final String TRANSACTION_PROCESS_TYPE = "Kassenbeleg-V1";

    private static final int ERROR_CLIENT_ALREADY_REGISTERED = 409;

    private static final String OFF_LINE = "off_line";

    private String workToken;

    private JsonHttpClient httpClient;

    private KPKCounters counters;

    private Map<Long, TaxVO> taxIndexTable = new LinkedHashMap<>();

    /**
     * There are five types of taxes in Germany
     */
    private String[] taxIndexes = new String[]{"A", "B", "C", "D", "E"};

    private Comparator<Payment> paymentComparator = new PaymentComparator();

    private DateTimeFormatter dateFormatter;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private FCConfig config;

    class PaymentComparator implements Comparator<Payment> {

        @Override
        public int compare(Payment p1, Payment p2) {
            String p1S = (PaymentTypes.CASH_TYPE.equals(p1.getPaymentType()) ? "A_" : "B_")
                    + (EUR_CURRENCY.equals(p1.getCurrency()) ? "A_" : "B_")
                    + p1.getCurrency();
            String p2S = (PaymentTypes.CASH_TYPE.equals(p2.getPaymentType()) ? "A_" : "B_")
                    + (EUR_CURRENCY.equals(p2.getCurrency()) ? "A_" : "B_")
                    + p2.getCurrency();
            return p1S.compareTo(p2S);
        }
    }

    public FCCImpl(KPKCounters counters) {
        this.counters = counters;
        initHttpClient();
        dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    public void setConfig(FCConfig config) {
        this.config = config;
    }

    public void initHttpClient() {
        httpClient = new JsonHttpClient("", null, false);
        httpClient.setCompressor(null);
        httpClient.setJsonParser(new DefaultJsonParser(true, false, "yyyy-MM-dd'T'HH:mm:sss"));
    }

    private void checkValue(String value, String message) throws FCCException {
        if (value == null || value.length() == 0) {
            throw new FCCException(ResBundleFCC.getString("SETTINGS_ERROR") + " " + message);
        }
    }

    public void setTaxes(Collection<TaxVO> taxes) {
        List<TaxVO> sortedTaxes = taxes.stream()
                .sorted(Comparator.comparingInt(TaxVO::getIndex))
                .collect(Collectors.toList());

        for (TaxVO tax : sortedTaxes) {
            taxIndexTable.put(tax.getValue(), tax);
        }
    }

    private void checkAllSettings() throws FCCException {
        checkValue(config.getConnectorAddress(), "Connection url is empty");
        checkValue(config.getLogin(), "Client login is empty");
        checkValue(config.getPassword(), "Client password is empty");
        checkValue(config.getErsSecret(), "ErsSecret is empty");
        checkValue(config.getUniqueClientId(), "Unique client id is empty");
        checkValue(config.getRegistrationToken(), "Registration token is empty");
    }

    public void connect() throws IOException, FCCException {
        LOG.debug("Try to connect to FCC");
        workToken = null;
        if (OFF_LINE.equals(config.getConnectorAddress())) {
            return;
        }

        // 0. Check all settings
        checkAllSettings();

        // 1. Check registartion
        FCCGetRegistredClientsRequest getRegistredClients = new FCCGetRegistredClientsRequest();
        boolean clientIsAlreadyRegistered = false;
        LOG.debug("Check client registration");
        FCCRegistredClientsResp regClients = call(getRegistredClients);
        if (regClients != null) {
            for (FCCClient cl : regClients) {
                if (config.getUniqueClientId().equals(cl.getClientId()) && CLIENT_STATUS_ACTIVE.equals(cl.getClientState())) {
                    LOG.debug("Client is already registered");
                    clientIsAlreadyRegistered = true;
                    break;
                }
            }
        }

        // 2. If not registered - will do it
        if (!clientIsAlreadyRegistered) {
            FCCRegRequest reg = new FCCRegRequest();
            reg.getRequestData().setBriefDescription("Linux POS");
            reg.getRequestData().setRegistrationToken(config.getRegistrationToken());
            reg.getRequestData().setTypeOfSystem("POS");
            reg.getRequestData().setUniqueClientId(config.getUniqueClientId());

            String regResp;
            try {
                regResp = call(reg);
            } catch (RemoteCallException e) {

                // We can skip this error - It doesn't matter.
                if (e.getExceptionCode() == ERROR_CLIENT_ALREADY_REGISTERED) {
                    LOG.debug("registration FCC - already registered");
                    regResp = "already registered";
                } else {
                    throw e;
                }
            }
            if (regResp == null || regResp.trim().length() == 0) {
                throw new FCCException("Cannot connect to FCC: registration");
            }
            LOG.debug("registration FCC - ok");
        }

        // 3. Get work token
        FCCTokenRequest token = new FCCTokenRequest();
        token.setUniqueClientId(config.getUniqueClientId());
        token.setErsSecret(config.getErsSecret());
        FCCTokenResp tokenResp = call(token);
        if (tokenResp == null || tokenResp.getAccessToken() == null) {
            throw new FCCException("Cannot get FCC token");
        }
        LOG.debug("get FCC token - ok");
        workToken = tokenResp.getAccessToken();
    }

    private String convertEpochMilliToDate(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()).format(dateFormatter);
    }

    public void updatePurchase(FiscalDocument document) throws FCCException, IOException {
        Map<String, String> fiscalizeValues = document.getFiscalizationValuesMap();
        String set10TransactionID = fiscalizeValues.get(FCConnector.PURCHASE_SET10_TRANSACTION_ID);
        if (set10TransactionID != null) {
            String fccTransactionId = fiscalizeValues.get(FCConnector.PURCHASE_FCC_TRANSACTION_ID);
            if (fccTransactionId != null) {
                BelegData belegData = getTrasactionById(fccTransactionId);
                if (belegData != null) {
                    updatePurchaseInfo(belegData, document, set10TransactionID);
                }
            }
        }
    }

    public void fiscalize(Check purchase) throws FCCException, IOException {
        String belegString = "Beleg^" + getFCCPurchaseTaxes(purchase) + "^" + getFCCPaymentString(purchase);
        LOG.info("Beleg string: {}", belegString);
        String belegBase64 = encodeBase64(belegString);

        boolean purchaseAlreadyStarted = false;

        String set10TransactionID = purchase.getFiscalizationValuesMap().get(FCConnector.PURCHASE_SET10_TRANSACTION_ID);

        BelegData belegData = null;
        if (!OFF_LINE.equals(config.getConnectorAddress())) {
            String fccTransactionId = null;
            if (set10TransactionID != null) {
                fccTransactionId = purchase.getFiscalizationValuesMap().get(FCConnector.PURCHASE_FCC_TRANSACTION_ID);

                FCCTransaction startedTransaction = getStartedTrasaction(set10TransactionID);
                if (startedTransaction != null) {
                    LOG.info("Purchase '{}' already started", fccTransactionId);
                    purchaseAlreadyStarted = true;
                }
            } else {
                set10TransactionID = counters.getTransactionID();
                purchase.getFiscalizationValuesMap().put(FCConnector.PURCHASE_SET10_TRANSACTION_ID, set10TransactionID);
            }

            if (!purchaseAlreadyStarted) {
                fccTransactionId = startTransaction(set10TransactionID);
                purchase.getFiscalizationValuesMap().put(FCConnector.PURCHASE_FCC_TRANSACTION_ID, fccTransactionId);
            }

            if (purchaseAlreadyStarted) {
                belegData = getTrasactionById(fccTransactionId);
            }

            if (belegData == null) {
                FCCTransactionFinishResp resp = finishTransaction(belegBase64, fccTransactionId);
                if (resp == null) {
                    LOG.error("Purchase fiscalization fail on finish transaction");
                    throw new FCCException(ResBundleFCC.getString("FISCALIZATION_FAIL"));
                }
                belegData = getTrasactionById(fccTransactionId);
                if (belegData == null || belegData.getSignature() == null || !belegData.getSignature().equals(resp.getSignatureValue())) {
                    LOG.error("Purchase fiscalization fail on finish transaction");
                    throw new FCCException(ResBundleFCC.getString("FISCALIZATION_FAIL"));
                }
            }

            if (!belegString.equals(belegData.getProcessData())) {
                LOG.error("FCC beleg doesn't equals local beleg:\n"
                        + "Local beleg: " + belegString + "\n"
                        + "FCC beleg: " + belegData.getProcessData());
            }
        } else {
            belegData = new BelegData();
            belegData.setSignature(UUID.randomUUID().toString());
            belegData.setSignaturecounter(new Random(100).nextInt());
            belegData.setCertificate(UUID.randomUUID().toString().getBytes());
            belegData.setHashalgorithm("Local");
            belegData.setPublickey(UUID.randomUUID().toString());
            belegData.setProcessData(belegString);
            belegData.setDatatimestoptransaction(Instant.now().toEpochMilli());
        }
        updatePurchaseInfo(belegData, purchase, set10TransactionID);
    }

    private void updatePurchaseInfo(BelegData belegData, FiscalDocument document, String set10TransactionID) {
        Map<String, Object> map = document.getMap();
        map.put("transactionnumber", set10TransactionID);
        map.put("timeformat", "unixTime");

        // fill fiscal data
        map.put("signature", belegData.getSignature());
        final String timeStartStopTransaction = convertEpochMilliToDate(belegData.getDatatimestoptransaction());
        map.put("datatimestarttransaction", timeStartStopTransaction);
        map.put("datatimestoptransaction", timeStartStopTransaction);
        map.put("cashserialnumber", config.getDeviceSerial());
        map.put("signaturecounter", belegData.getSignaturecounter());
        map.put("hashalgorithm", belegData.getHashalgorithm());
        map.put("publickey", belegData.getPublickey());
    }

    protected FCCTransaction getStartedTrasaction(String externalTransactionId) throws FCCException, IOException {
        FCCOpenTransactionsGet req = new FCCOpenTransactionsGet();
        req.setClientId(config.getUniqueClientId());
        req.setToken(workToken);
        try {
            FCCOpenTransactionsResp transactions = call(req);

            if (transactions != null && !transactions.isEmpty()) {
                Map<String, FCCTransaction> transactionsMap = transactions.stream()
                        .collect(Collectors.toMap(FCCTransaction::getExternalTransactionId, t -> t, (o, n) -> n));

                FCCTransaction transaction = transactionsMap.get(externalTransactionId);
                if (transaction != null && "started".equals(transaction.getState())) {
                    return transaction;
                }
            }
        } catch (RemoteCallException e) {
            LOG.error("Cannot get transaction by id", e);
            throw new FCCException(ResBundleFCC.getString("FISCALIZATION_FAIL"));
        }
        return null;
    }

    protected BelegData getTrasactionById(String transactionId) throws FCCException, IOException {
        FCCTransactionGet req = new FCCTransactionGet();
        req.setTransactionId(transactionId);
        req.setClientId(config.getUniqueClientId());
        req.setToken(workToken);
        String belegBase64;

        BelegData belegData = null;
        belegBase64 = call(req);
        if (belegBase64 != null) {
            byte[] data = Base64.decodeBase64(belegBase64);
            belegData = BelegTools.getBelegInfo(data);
        }
        return belegData;
    }

    private String startTransaction(String externalTransactionID) throws IOException, FCCException {
        LOG.debug("Start FCC transaction");
        FCCTransactionStartRequest req = new FCCTransactionStartRequest();
        req.getRequestData().setClientId(config.getUniqueClientId());
        req.getRequestData().setProcessType("");
        req.getRequestData().setExternalTransactionId(externalTransactionID);
        req.getRequestData().setProcessData(encodeBase64(""));
        req.setToken(workToken);
        FCCTransactionStartResp resp = call(req);
        if (resp == null || resp.getTransactionNumber() == null) {
            LOG.error("Cannot start transaction");
            throw new FCCException(ResBundleFCC.getString("FISCALIZATION_FAIL"));
        }
        return resp.getTransactionNumber();
    }

    private FCCTransactionFinishResp finishTransaction(String belegBase64, String transactionID) throws IOException {
        LOG.debug("Finish FCC transaction");
        FCCTransactionFinishRequest req = new FCCTransactionFinishRequest();
        req.getRequestData().setClientId(config.getUniqueClientId());
        req.setTransactionId(transactionID);
        req.getRequestData().setProcessType(TRANSACTION_PROCESS_TYPE);
        req.getRequestData().setProcessData(belegBase64);
        req.setToken(workToken);
        FCCTransactionFinishResp result = call(req);
        return result;
    }

    private static BigDecimal convertMoney(long value) {
        return BigDecimal.valueOf(value, MONEY_SCALE);
    }

    protected String getFCCPaymentString(Check purchase) {
        StringBuilder result = new StringBuilder();
        List<Payment> euroPayments = new ArrayList<>(purchase.getPayments());
        euroPayments.sort(paymentComparator);
        String split = "";
        boolean refund = purchase.getType() == CheckType.RETURN;
        for (Payment p : euroPayments) {
            if (p.getSum() > 0) {
                result.append(split);
                result.append(refund ? "-" : "");
                result.append(convertMoney(p.getSum()));
                result.append(PaymentTypes.CASH_TYPE.equals(p.getPaymentType()) ? ":Bar" : ":Unbar");

                if (!EUR_CURRENCY.equals(p.getCurrency())) {
                    result.append(":");
                    result.append(p.getCurrency());
                }
            }
            split = "_";
        }
        return result.toString();
    }

    protected String getFCCPurchaseTaxes(Check purchase) throws FCCException {
        StringBuilder result = new StringBuilder();

        Map<String, Long> purchasesTaxes = new LinkedHashMap<>();
        for (TaxVO tax : taxIndexTable.values()) {
            purchasesTaxes.put(tax.getCode(), 0L);
        }

        Map<String, PurchaseTaxInfo> purchaseTaxMap = new HashMap<>();
        List<PurchaseTaxInfo> taxesList = new ArrayList<>();
        final AtomicInteger taxIndex = new AtomicInteger(0);

        Long purchaseTax;
        PurchaseTaxInfo purchaseTaxInfo;
        for (Goods pos : purchase.getGoods()) {
            purchaseTaxInfo = purchaseTaxMap.computeIfAbsent(pos.getTaxName(), p -> {

                // it allows us collect taxes in order by taxIndex
                PurchaseTaxInfo pti = new PurchaseTaxInfo(taxIndexes[taxIndex.getAndIncrement()], pos.getTaxName());
                taxesList.add(pti);
                return pti;
            });
            purchaseTaxInfo.add(pos.getTaxSum(), pos.getEndPositionPrice());
            pos.setTaxIndexName(purchaseTaxInfo.getTaxIndex());

            purchaseTax = purchasesTaxes.get(pos.getTaxName());
            if (purchaseTax == null) {
                LOG.error("Tax not found in tax table: " + pos.getTaxName());
                throw new FCCException(ResBundleFCC.getString("ERROR_TAX_VALUE"));
            }
            purchaseTax += pos.getTaxSum();
            purchasesTaxes.put(pos.getTaxName(), purchaseTax);
        }

        PurchaseTaxes purchaseTaxes = new PurchaseTaxes();
        purchaseTaxes.setPurchaseTaxes(taxesList);
        purchase.getMap().put(PurchaseTaxes.DE_TAXES_FIELD, purchaseTaxes);

        String split = "";
        boolean refund = purchase.getType() == CheckType.RETURN;
        for (Entry<String, Long> e : purchasesTaxes.entrySet()) {
            result.append(split);
            result.append(refund && e.getValue() > 0 ? "-" : "");
            result.append(convertMoney(e.getValue()));
            split = "_";
        }

        return result.toString();
    }

    private <R, T> T call(Request<R, T> request) throws IOException {
        String fullUrl = "http://" + config.getConnectorAddress() + "/" + request.getUrlPath();
        Map<String, String> head = getRequestHeader(request);
        return httpClient.requestAsJson(fullUrl, head, request.getRequestData(), request.getResponseType());
    }

    private Map<String, String> getRequestHeader(Request request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String basicAuth = null;

        // Bearer authorization
        if (request.token() != null) {
            basicAuth = "Bearer " + request.token();
        } else {

            // Register authorization
            if (request.login() != null && request.password() != null) {
                basicAuth = "Basic " + encodeBase64(request.login() + ":" + request.password());
            } else if (request.isUseAuthorization()) {

                // Default authorization
                basicAuth = "Basic " + encodeBase64(config.getLogin() + ":" + config.getPassword());
            }
        }

        if (basicAuth != null) {
            headers.put("Authorization", basicAuth);
        }

        return headers;
    }

    private String encodeBase64(String value) {
        return Base64.encodeBase64String(value.getBytes(StandardCharsets.UTF_8));
    }
}
