package ru.crystals.pos.loyal.cash.transport.discountresults;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.commons.amf.io.Utils;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity;
import ru.crystals.discount.processing.entity.LoyChequeCouponEntity;
import ru.crystals.discount.processing.entity.LoyDiscountCardEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyGiftNoteEnity;
import ru.crystals.discount.processing.entity.LoyProcessingCouponEntity;
import ru.crystals.discount.processing.entity.LoyQuestionaryEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.CashTransportBeanRemote;
import ru.crystals.esb.Set10ESBConfigurationConstants;
import ru.crystals.httpclient.TransportDisabledException;
import ru.crystals.operday.transport.request.cash.ExtendedDocumentDescription;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.registry.Registry;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.transport.ConnectionState;
import ru.crystals.pos.transport.ModuleConnection;
import ru.crystals.transport.TransferObject;
import ru.crystals.transport.TransferObjectsManager;
import ru.crystals.transport.TransferObjectsManagerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Set Retail 10 developers
 * @author A.Martynov
 */
public class TransferManager extends ModuleConnection implements ConnectionState {
    private static Logger log = LoggerFactory.getLogger(TransferManager.class);

    private static final int DEFAULT_RESEND_PERIOD_SEC = 60;
    private static final int DEFAULT_REQUEST_PERIOD_SEC = 120;
    private static final int DEFAULT_FETCH_LIMIT = 100;

    @Autowired
    private InternalCashPoolExecutor executor;

    @Autowired
    private Loyal loyalService;

    @Autowired
    private PropertiesManager propertiesManager;

    @Autowired
    private TechProcessEvents techProcessEvents;

    private static final String SERVER_MODULE_NAME = "processing-discounts";

    private static final long LOY_TRANSACTION_SEARCH_TIMEOUT = DateUtils.MILLIS_PER_SECOND * 20;// TODO

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private Integer loyTransactionReSendTimeout = DEFAULT_RESEND_PERIOD_SEC;
    private Integer loyTransactionRequestToResendTimeout = DEFAULT_REQUEST_PERIOD_SEC;
    private Integer maxLoytransactionCountInPack = DEFAULT_FETCH_LIMIT;

    private String processingDiscountServiceURL = "SET-ProcessingDiscount/ProcessingDiscountWS?wsdl";
    private CashTransportBeanRemote cashTransportManager = null;

    // injected
    /**
     * Через эту штуку будем сохранять/читать {@link LoyTransactionEntity TX лояльности} в/из БД.
     */
    private LoyTxDao loyTxDao;

    /**
     * Фоновый отправщик данных. Вызывается по таймеру и при сохранении транзакции
     *
     * @author ppavlov
     */
    class RepeatSender implements Runnable {
        @Override
        public void run() {
            try {
                if (!isSendDocsToESBEnabled()) {
                    List<LoyTransactionEntity> txes = getTransactions();
                    if (CollectionUtils.isNotEmpty(txes)) {
                        sendLoyTransactions(txes);
                    }
                }
            } catch (Exception e) {
                log.error("Error during attempt to send loyal transactions", e);
            }
        }
    }

    /**
     * Периодически спрашивает у сервера не нужно ли чего переотправить
     *
     * @author ppavlov
     */
    private class ResendRequester implements Runnable {

        @Override
        public void run() {
            try {
                if (!isSendDocsToESBEnabled()) {
                    Collection<String> notProcessedFiles = cashTransportManager.askForNotProcessedFiles((int) getCashNumber(), (int) getShopNumber());
                    if (notProcessedFiles != null && !notProcessedFiles.isEmpty()) {
                        if (log.isInfoEnabled()) {
                            StringBuilder sb = new StringBuilder().append("\n");
                            for (String file : notProcessedFiles) {
                                sb.append(file).append("\n");
                            }
                            log.info("Try to resend not processed transactions by server request from files: {}", sb.toString());
                        }

                        int count = loyTxDao.markLoyTxesAsNotSent(notProcessedFiles);
                            log.info("{} transactions out of {} requested were scheduled for re-send", count, notProcessedFiles.size());
                    } else {
                        log.info("Nothing yet not processed on server to resend");
                    }
                }
            } catch (Throwable ex) {
                if (ex instanceof TransportDisabledException) {
                        log.warn(ex.toString());
                } else {
                    log.error("Error during attempt to resend not processed transactions", ex);
                }
            }
        }
    }

    /**
     * Включена ли отправка чеков и транзакций лояльности через ESB ?
     * Транзакции уходят в ESB одним сообщением вместе с чеком.
     *
     * @return true - да, false - нет
     */
    private boolean isSendDocsToESBEnabled() {
        return Boolean.parseBoolean(propertiesManager.getProperty(Set10ESBConfigurationConstants.ESB_MODULE_NAME, null, Set10ESBConfigurationConstants.POS_TO_KAFKA_ENABLED, "false"));
    }


    public void start() {
        cashTransportManager = getHttpConnect().find(CashTransportBeanRemote.class, CashTransportBeanRemote.JNDI_NAME);
        setURL();

        executor.scheduleWithFixedDelay(new RepeatSender(), 30, getLoyTransactionReSendTimeout(), TimeUnit.SECONDS);
        executor.scheduleWithFixedDelay(new ResendRequester(), 30, getLoyTransactionRequestToResendTimeout(), TimeUnit.SECONDS);
    }

    private long getShopNumber() {
        long shopNumber = 0L;

        if (techProcessEvents.getCashProperties() != null && techProcessEvents.getCashProperties().getShopIndex() != null) {
            shopNumber = techProcessEvents.getCashProperties().getShopIndex();
        }

        return shopNumber;
    }

    private long getCashNumber() {
        long cashNumber = 0L;

        if (techProcessEvents.getCashProperties() != null && techProcessEvents.getCashProperties().getCashNumber() != null) {
            cashNumber = techProcessEvents.getCashProperties().getCashNumber();
        }

        return cashNumber;
    }

    public String getProcessingDiscountServiceURL() {
        return processingDiscountServiceURL;
    }

    public void setProcessingDiscountServiceURL(String processingDiscountServiceURL) {
        this.processingDiscountServiceURL = processingDiscountServiceURL;
    }

    public List<LoyTransactionEntity> getTransactions() {
        return loyTxDao.getLoyTxesByStatus(Arrays.asList(new SentToServerStatus[] {
                SentToServerStatus.NO_SENT, SentToServerStatus.WAIT_ACKNOWLEDGEMENT, SentToServerStatus.SENT_ERROR}),
                getMaxLoytransactionCountInPack());
    }

    private void sendLoyTransactions(List<LoyTransactionEntity> loyTransactions) {

        long st = System.currentTimeMillis();
        try {
            if (CollectionUtils.isEmpty(loyTransactions)) {
                throw new Exception("Nothing to send, loyTransactions is empty");
            }

            // данные о файле на сервере и количестве транзакций в нём, используется для регистрации
            Map<String, Integer> nginxFiles = new HashMap<>();
            // использовать ли архивирование; только для пакетной отправки
            List<LoyTransactionEntity> loyTransactionsToUpload = new ArrayList<>();
            List<LoyTransactionEntity> loyTransactionsToUpdate = new ArrayList<>();

            for (LoyTransactionEntity loyTransaction : loyTransactions) {
                // если у транзакции не проставлен номер чека, считаем её незавершенной и не отправляем
                if (loyTransaction.getPurchaseNumber() == -1) {
                    log.info(String.format("Can't send loyal transaction entity with empty purchase number, transaction id: %d, shift number: %d",
                            loyTransaction.getId(), loyTransaction.getShiftNumber()));
                    loyTransactionsToUpdate.add(loyTransaction);
                    continue;
                }
                // если у транзакции проставлен номер файла, и статус 1 (WAIT_ACKNOWLEDGEMENT), считаем её уже отправленной
                if (loyTransaction.getFilename() != null && !loyTransaction.getFilename().isEmpty() &&
                        (loyTransaction.getSentToServerStatus() == SentToServerStatus.WAIT_ACKNOWLEDGEMENT)) {
                    nginxFiles.put(loyTransaction.getFilename(),
                            (nginxFiles.get(loyTransaction.getFilename()) != null) ? nginxFiles.get(loyTransaction.getFilename()) + 1 : 1);
                    loyTransactionsToUpdate.add(loyTransaction);
                    continue;
                }
                // готова ли транзакция лояльности к отправке на сервер
                if (!loyalService.isLoyTransactionComplete(loyTransaction)) {
                    continue;
                }
                // в противном случае добавляем в список для отправки
                loyTransactionsToUpdate.add(loyTransaction);
                loyTransactionsToUpload.add(loyTransaction);
            }

            if (CollectionUtils.isEmpty(loyTransactionsToUpdate)) {
                log.warn("No loyTransactions to send: some transactions were considered unready to send");
                return;
            }

            for (LoyTransactionEntity loyTransaction : loyTransactionsToUpload) {
                Date now = new Date();
                String transactionName = "LoyTransaction_" + formatter.format(now) + "_" + (now.getTime() % 1000) + "_" + getShopNumber() + "_" + getCashNumber();
                String serverFileName = getCashNumber() + "/" + transactionName + ".ser";

                Collection<TransferObject> transferObjects = new LinkedList<>();
                transferObjects.add(new TransferObject(Utils.serialize(loyTransaction), loyTransaction.getDataType()));

                TransferObjectsManager transferObjectsManager = TransferObjectsManagerFactory.createHttpTransferObjectsManager(getHttpFileURL("/loyaltransactions/" + serverFileName));

                if (transferObjectsManager != null && transferObjectsManager.sendData(transferObjects)) {
                    setStatusForNewTransactions(Arrays.asList(loyTransaction), SentToServerStatus.WAIT_ACKNOWLEDGEMENT, serverFileName);
                    //add transactions to nginx files
                    nginxFiles.put(serverFileName, (nginxFiles.get(serverFileName) != null) ? nginxFiles.get(serverFileName) + 1 : 1);
                } else {
                    setStatusForNewTransactions(Arrays.asList(loyTransaction), SentToServerStatus.NO_SENT, null);
                    log.error("Failed to upload transactions file " + serverFileName);
                    setURL();
                }
            }

            // try to register transactions on server
            try {
                if (cashTransportManager == null) {
                    throw new Exception("cashTransportManager is null");
                }

                for (Entry<String, Integer> entry : nginxFiles.entrySet()) {
                    String filePath = entry.getKey();
                    try {
                        Long result = cashTransportManager.registerLoyalTransactions(filePath, (int) getCashNumber(), (int) getShopNumber(), entry.getValue());
                        if (result != null && result > 0) {
                            setStatusForUploadedTransactions(loyTransactionsToUpdate, SentToServerStatus.SENT, filePath);

                            log.info("File " + filePath + " sucessfully registered");
                        } else {
                            log.warn("Can't register file " + filePath + ", result: " + result + ". Server offline maybe, we try it later again.");
                        }
                    } catch (Exception e) {
                        log.error("Error registering file " + filePath + " on server, " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error registering loyal transactions on server, " + e.getMessage());
            }

            if (log.isInfoEnabled()) {
                StringBuilder s = new StringBuilder();
                s.append(loyTransactionsToUpload.size()).append(" transactions processed to upload; ").append(loyTransactionsToUpdate.size());
                s.append(" transactions processed to register; Time: ").append(System.currentTimeMillis() - st).append(" ms.");
                log.info(s.toString());
            }
        } catch (Exception e) {
            log.error("Error during sending transactions", e);
            setURL();
        }
    }

    private void setStatusForNewTransactions(List<LoyTransactionEntity> loyTransactions, SentToServerStatus status, String uploadedFileName) {
        setStatus(loyTransactions, status, uploadedFileName, true);
    }

    private void setStatusForUploadedTransactions(List<LoyTransactionEntity> loyTransactions, SentToServerStatus status, String uploadedFileName) {
        setStatus(loyTransactions, status, uploadedFileName, false);
    }

    private void setStatus(List<LoyTransactionEntity> loyTransactions, SentToServerStatus status, String fileName, boolean updateFileName) {
        List<Long> loyTransactionEntitiesIds = new LinkedList<Long>();

        // 1. Вытянуть ID'шники
        for (LoyTransactionEntity loyTransactionEntity : loyTransactions) {
            loyTransactionEntitiesIds.add(loyTransactionEntity.getId());
        }

        // 2. И сделать в БД что просили:
        int updated;
        if (updateFileName) {
            // надо и статус установить и название файла обновить:
            updated = loyTxDao.setTxStatusAndFileNames(loyTransactionEntitiesIds, fileName, status);
        } else {
            // надо только статус проставить:
            updated = loyTxDao.setTxStatus(loyTransactionEntitiesIds, fileName, status);
        }
        log.trace("setStatus: [{}] loy-txes were updated", updated);

    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    public Integer getLoyTransactionReSendTimeout() {
        return (loyTransactionReSendTimeout != null) ? loyTransactionReSendTimeout : 60;
    }

    public void setLoyTransactionReSendTimeout(Integer loyTransactionReSendTimeout) {
        this.loyTransactionReSendTimeout = loyTransactionReSendTimeout;
    }

    public Integer getLoyTransactionRequestToResendTimeout() {
        return (loyTransactionRequestToResendTimeout != null) ? loyTransactionRequestToResendTimeout : DEFAULT_REQUEST_PERIOD_SEC;
    }

    public void setLoyTransactionRequestToResendTimeout(Integer loyTransactionRequestToResendTimeout) {
        this.loyTransactionRequestToResendTimeout = loyTransactionRequestToResendTimeout;
    }

    public Integer getMaxLoytransactionCountInPack() {
        return (maxLoytransactionCountInPack != null) ? maxLoytransactionCountInPack : 0;
    }

    public void setMaxLoytransactionCountInPack(Integer maxLoytransactionCountInPack) {
        this.maxLoytransactionCountInPack = maxLoytransactionCountInPack;
    }

    /**
     * Соберет и вернет из указанной транзакции описания всех различных РА, что сработали в этом чеке.
     *
     * @param tx
     *            транзакция лояльности на чек
     * @return не {@code null}; ключ - {@link LoyAdvActionInPurchaseEntity#getGuid() идентификатор описания РА}, значение - само описание РА
     */
    private Map<Long, LoyAdvActionInPurchaseEntity> collectActions(LoyTransactionEntity tx) {
        Map<Long, LoyAdvActionInPurchaseEntity> result = new HashMap<>();

        if (tx == null) {
            return result;
        }

        // у семи (сейчас - 2016-08-15) полей-коллекций есть ссылки на описания РА
        // 1. bonusPositions
        for (LoyBonusPositionEntity b : tx.getBonusPositions()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        }// for b

        // 2. bonusTransactions
        for (LoyBonusTransactionEntity b : tx.getBonusTransactions()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 3. chequeCoupons
        for (LoyChequeCouponEntity b : tx.getChequeCoupons()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 4. discountCards
        for (LoyDiscountCardEntity b : tx.getDiscountCards()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 5. discountPositions
        for (LoyDiscountPositionEntity b : tx.getDiscountPositions()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 6. processingCoupons
        for (LoyProcessingCouponEntity b : tx.getProcessingCoupons()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 7. questionaries
        for (LoyQuestionaryEntity b : tx.getQuestionaries()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        // 8. giftNotes
        for (LoyGiftNoteEnity b : tx.getGiftNotes()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        }

        // 3. chequeAdverts
        for (LoyChequeAdvertiseEntity b : tx.getChequeAdverts()) {
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                continue;
            }
            result.put(b.getAdvAction().getGuid(), b.getAdvAction());
        } // for b

        return result;
    }

    /**
     * просто делает так, чтобы в дереве объектов. образованной указанной транзакцией не было 2х не совпадающих (в смысле != (не равно))
     * {@link LoyAdvActionInPurchaseEntity описаний РА}, имеющих одинаковый GUID.
     *
     * @param tx
     */
    private void siftOffAdvActions(LoyTransactionEntity tx) {
        if (tx == null) {
            return;
        }

        // все уникальные описания РА. что встречаются в этом чеке:
        Map<Long, LoyAdvActionInPurchaseEntity> allActions = collectActions(tx);

        // а теперь у элементов семи (2016-08-15) коллекций надо прописать правильные ссылки
        // 1. bonusPositions
        for (Iterator<LoyBonusPositionEntity> it = tx.getBonusPositions().iterator(); it.hasNext();) {
            LoyBonusPositionEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 2. bonusTransactions
        for (Iterator<LoyBonusTransactionEntity> it = tx.getBonusTransactions().iterator(); it.hasNext();) {
            LoyBonusTransactionEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 3. chequeCoupons
        for (Iterator<LoyChequeCouponEntity> it = tx.getChequeCoupons().iterator(); it.hasNext();) {
            LoyChequeCouponEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 4. discountCards
        for (Iterator<LoyDiscountCardEntity> it = tx.getDiscountCards().iterator(); it.hasNext();) {
            LoyDiscountCardEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 5. discountPositions
        for (Iterator<LoyDiscountPositionEntity> it = tx.getDiscountPositions().iterator(); it.hasNext();) {
            LoyDiscountPositionEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 6. processingCoupons
        for (Iterator<LoyProcessingCouponEntity> it = tx.getProcessingCoupons().iterator(); it.hasNext();) {
            LoyProcessingCouponEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 7. questionaries
        for (Iterator<LoyQuestionaryEntity> it = tx.getQuestionaries().iterator(); it.hasNext();) {
            LoyQuestionaryEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it

        // 8. giftNotes
        for (Iterator<LoyGiftNoteEnity> it = tx.getGiftNotes().iterator(); it.hasNext();) {
            LoyGiftNoteEnity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        }

        // 9. chequeAdverts
        for (Iterator<LoyChequeAdvertiseEntity> it = tx.getChequeAdverts().iterator(); it.hasNext();) {
            LoyChequeAdvertiseEntity b = it.next();
            if (b == null || b.getAdvAction() == null || b.getAdvAction().getGuid() == null) {
                it.remove();
                continue;
            }
            b.setAdvAction(allActions.get(b.getAdvAction().getGuid()));
        } // for it
    }

    private LoyTransactionEntity saveLoyTransaction(LoyTransactionEntity loyTransactionEntity) {
        if (loyTransactionEntity == null) {
            log.error("saveLoyTransaction: the argument is NULL!");
            return null;
        }

        // удалим дубли описаний РА:
        siftOffAdvActions(loyTransactionEntity);

        // само сохранение
        try {
            LoyTransactionEntity saved = loyTxDao.saveLoyTx(loyTransactionEntity);
            log.trace("loy-tx [{}] saved", saved);
            return saved;
        } catch (Exception e) {
            log.warn("Can't save loyTransacton", e);
        }
        return null;
    }

    private LoyTransactionEntity getLoyTransactionByNumber(ExtendedDocumentDescription request) {
        if (log.isDebugEnabled()) {
            log.debug("get loy tx by number; request: {}", request);
        }

        LoyTransactionEntity result = null;

        if (request != null && request.isComplete()) {
            log.trace("get loy tx by number; request completed; ok");

            try {
                if (cashTransportManager == null) {
                    throw new Exception("cashTransportManager is null");
                }

                result = cashTransportManager.getLoyTransactionByParameters(request);

                if (result == null) {
                    log.warn("Can't receive LoyTransaction by {}; server returns null", request);
                }

            } catch (Exception e) {
                log.error("Error receiving loyTransactionEntity from server", e);
            }
        } else {
            log.debug("get loy tx by number; request is not completed; return null");
        }

        return result;
    }

    public Long receiveLoyTransaction(final PurchaseEntity purchase) {
        final Long[] result = new Long[1];
        ShiftEntity shift = purchase.getShift();
        log.info("receive loy tx; shift: " + shift);

        if (shift != null) {
            final ExtendedDocumentDescription request = new ExtendedDocumentDescription(shift.getCashNum(), shift.getNumShift(),
                                                    purchase.getDateCommit(), purchase.getNumber(), shift.getShopIndex(), purchase.getInn());
            log.info("receive loy tx; document request: " + request);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("receive loy tx; getLoyTransactionByNumber, request: " + request);
                    LoyTransactionEntity loyTransactionEntity = getLoyTransactionByNumber(request);
                    log.info("receive loy tx; loy tx: " + loyTransactionEntity);

                    if (loyTransactionEntity != null) {
                        log.info("receive loy tx; saving loy tx; loy tx: " + loyTransactionEntity);
                        loyTransactionEntity = saveLoyTransaction(loyTransactionEntity);
                        log.info("receive loy tx; loy tx saved; loy tx: " + loyTransactionEntity);
                        if (loyTransactionEntity != null) {
                            result[0] = loyTransactionEntity.getId();
                        }
                    }
                }
            });
            t.start();

            try {
                t.join(LOY_TRANSACTION_SEARCH_TIMEOUT);
                log.info("receive loy tx; waiting end");
            } catch (InterruptedException e) {
                log.error("receive loy tx - thread interrupted; request: " + request);
                e.printStackTrace();
            }
        } else {
            log.warn("Can't request loyTransacion from server, purchase shift is empty");
        }

        return result[0];
    }

    @Override
    public String getServerModuleName() {
        return SERVER_MODULE_NAME;
    }

    public LoyTxDao getLoyTxDao() {
        return loyTxDao;
    }

    public void setLoyTxDao(LoyTxDao loyTxDao) {
        this.loyTxDao = loyTxDao;
    }

    @Override
    public boolean getConnectionState() {
        return isConnectExists(CashTransportBeanRemote.JNDI_NAME);
    }
}
