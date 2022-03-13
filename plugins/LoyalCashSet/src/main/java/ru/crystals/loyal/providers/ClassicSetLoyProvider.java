package ru.crystals.loyal.providers;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.api.adapters.AdvertisingActionConditionRoutine;
import ru.crystals.cards.BonusAccountsType;
import ru.crystals.cards.CardBonusAccount;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardBonusSubBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.ExternalLoyaltyPurchaseConsumer;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.calculation.DiscountCalculationUtils;
import ru.crystals.loyal.calculation.DoProcessing;
import ru.crystals.loyal.calculation.FL54Tuner;
import ru.crystals.loyal.calculation.PurchaseUtils;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.interfaces.PluginWithExternalValue;
import ru.crystals.loyalty.exporter.PDConfig;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.cards.coupons.SetCouponingCashSettings;
import ru.crystals.pos.cards.informix.CoBrandService;
import ru.crystals.pos.cards.informix.InformixService;
import ru.crystals.pos.cards.informix.InformixServiceConnectionException;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.discountresults.ChequeAdvertEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusesReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CouponTemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TemplatedServiceDocument;
import ru.crystals.pos.loyal.cash.persistence.LoyFeedbackDao;
import ru.crystals.pos.loyalty.LoyaltyCountersService;
import ru.crystals.pos.loyalty.LoyaltyRestrictionsService;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.salemetrics.JmxMetrics;
import ru.crystals.pos.techprocess.TechProcessStage;
import ru.crystals.pos.utils.CheckUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Классическая реализация нашего (SET10) поставщика "услуг лояльности": считает скидки по всем РА - без деления на РА с округлением и без.
 *
 * @author aperevozchikov
 */
public class ClassicSetLoyProvider extends AbstractLoyProvider {

    private static final Logger log = LoggerFactory.getLogger(ClassicSetLoyProvider.class);

    @Autowired(required = false)
    private CoBrandService coBrandService;

    @Autowired(required = false)
    private LoyaltyCountersService loyaltyCountersService;

    @Autowired
    private AbstractPluginsAdapter pluginsAdapter;

    @Autowired(required = false)
    private LoyaltyRestrictionsService restrictionsService;

    @Autowired(required = false)
    private SetCouponingCashSettings setCouponingCashSettings;

    @Autowired
    private CardsService cardsService;

    @Autowired(required = false)
    private InformixService informixService;

    @Autowired
    private PropertiesManager propertiesManager;

    @Autowired
    private CurrencyHandler currencyHandler;

    @Autowired(required = false)
    private Set<ExternalLoyaltyPurchaseConsumer> externalLoyaltyPurchaseConsumers = new HashSet();

    public ClassicSetLoyProvider() {
    }

    @Override
    public Purchase process(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) {
        Purchase result;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering process(Purchase, PurchaseEntity, ILoyTechProcess). The arguments are: receipt [{}], originalReceipt [{}]",
                receipt, originalReceipt);

        if (receipt == null || originalReceipt == null) {
            log.error("leaving process(Purchase, PurchaseEntity, ILoyTechProcess). either the \"receipt\" [{}] or the \"originalReceipt\" [{}] " +
                    "argument is NULL", receipt, originalReceipt);
            return null;
        }

        // "округлятор" в техпроцесс расчета скидок сразу же запихнем:
        techProcess.getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);

        // 1. для начала надо из пришедшего калькуляторного чека выкинуть все "преференции", что там есть
        //  но при этом понизив ЦЕНЫ позиций в соответствии с уже данными скидками
        Collection<AdvertisingActionEntity> actionsList = getBaseActionsCollected(originalReceipt, techProcess);
        Purchase receiptCopy = receipt.cloneWithDisc();
        receiptCopy.setPreviousPurchase(receipt);
        DiscountCalculationUtils.updateOriginalCosts(receiptCopy, currencyHandler);
        DiscountCalculationUtils.updateAppliedActions(receiptCopy, getFinalActionsGuids(originalReceipt, actionsList));
        techProcess.getProcessingSession().put(CalculateSession.OUTBOUND_MUTEX_ACTIONS,
                DiscountCalculationUtils.getMutexActions(actionsList));
        PurchaseUtils.removeAllDiscounts(receiptCopy);

        // 2. получить коллекцию РА и проверить условия
        actionsList = getFilteredActions(originalReceipt, techProcess);

        if (log.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            actionsList.forEach(it -> sb.append(it.toStringShort()));
            log.trace("Actions to calculate is: {}", sb.toString());
        }

        boolean onlineCounters = true;
        if (loyaltyCountersService != null && !DiscountCalculationUtils.isPreDiscountEnabled(techProcess)) {
            onlineCounters = loyaltyCountersService.checkCounters(originalReceipt, actionsList);
        }

        AdvertisingActionEntity[] actions = actionsList.toArray(new AdvertisingActionEntity[0]);

        // 3. рассчитать
        result = DoProcessing.doDiscount(techProcess, actions, receiptCopy);

        if (!onlineCounters) {
            MessageAdvertisingActionResultEntity e = new MessageAdvertisingActionResultEntity();
            e.setAdvertisingActionGUID(-1L);
            e.getOperatorMsg().add("Акции на основании истории покупок не применены! Сервис счетчиков недоступен");
            result.getAdvertisingActionResults().add(e);
        }

        // 4. а теперь надо вернуть позициям их оригинальные ЦЕНЫ
        DiscountCalculationUtils.restoreOriginalPrices(result, receipt);

        long classicTime = System.currentTimeMillis() - stopWatch;
        log.trace("leaving process(Purchase, PurchaseEntity, ILoyTechProcess). The result is: {}; it took {} [ms]",
                result, classicTime);
        JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_CALCULATION_CLASSIC, classicTime);

        return result;
    }

    @Override
    public void onDiscountCalculationStarted(Purchase purchase, PurchaseEntity originalPurchase, ILoyTechProcess techProcess) {
        final AdvertisingActionConditionRoutine actionConditionRoutine = pluginsAdapter.getActionConditionRoutine();
        if (actionConditionRoutine != null) {
            actionConditionRoutine.onDiscountCalculationStarted(purchase, CheckUtils.getExtData(originalPurchase));
        }

        Collection<AdvertisingActionEntity> baseActions = DiscountCalculationUtils.preCheckActionsConditionsFor(
                getBaseActions(originalPurchase), purchase, techProcess, PluginWithExternalValue.isPlugin);

        // получим соответствующие РА ограничения из процессинга ограничений
        if (restrictionsService != null && !DiscountCalculationUtils.isPreDiscountEnabled(techProcess)) {
            long restrictionStartTime = System.currentTimeMillis();
            restrictionsService.checkRestrictions(originalPurchase, baseActions);
            JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_CALCULATION_CLASSIC_RESTRICTIONS,
                    System.currentTimeMillis() - restrictionStartTime);
        }

        techProcess.getProcessingSession().put(CalculateSession.BASE_ACTIONS, baseActions);

        // Обновим настройки купонинга
        if (setCouponingCashSettings != null) {
            techProcess.getLoyaltyProperties().setCouponProcessingEnabled(setCouponingCashSettings.isEnabled());
            techProcess.getLoyaltyProperties().setCashGeneratedProcessingUniqueCouponPrefix(setCouponingCashSettings.getCashPrefix());
            techProcess.getLoyaltyProperties().setServerGeneratedProcessingUniqueCouponPrefix(setCouponingCashSettings.getServerPrefix());
        }
    }

    @Override
    public Purchase onDiscountCalculationFinished(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) {
        if (techProcess.getProcessingSession().getValue(CalculateSession.BASE_ACTIONS) == null) {
            return receipt;
        }
        Collection<AdvertisingActionEntity> baseActions =
                (Collection<AdvertisingActionEntity>) techProcess.getProcessingSession().getValue(CalculateSession.BASE_ACTIONS);

        receipt = new FL54Tuner().tune(techProcess, baseActions.toArray(new AdvertisingActionEntity[0]), receipt);


        final AdvertisingActionConditionRoutine actionConditionRoutine = pluginsAdapter.getActionConditionRoutine();
        if (actionConditionRoutine != null) {
            actionConditionRoutine.onDiscountCalculationFinished(receipt, CheckUtils.getExtData(originalReceipt));
        }
        return receipt;
    }

    @Override
    public String getProviderName() {
        return SET10_PROVIDER_NAME;
    }

    @Override
    public CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        return techProcess.getCardType(couponData.getCardNumber());
    }

    @Override
    public void beforeFiscalize(PurchaseEntity purchase) {
        if (purchase == null || !purchase.isSale() || purchase.getDiscountPurchaseEntity() == null) {
            return;
        }

        DiscountPurchaseEntity dpe = purchase.getDiscountPurchaseEntity();

        // формирование сервисных документов купонов
        if (dpe.getChequeCoupons() != null) {
            for (ChequeCouponEntity chequeCoupon : dpe.getChequeCoupons()) {
                CouponTemplatedServiceDocument document = chequeCoupon.getCouponText();
                if (purchase.getServiceDocs().contains(document)) {
                    continue;
                }

                if (chequeCoupon.getCouponTypeGuid() != null) {
                    // вытянем инфу о купоне (в дальнейшем для подстановок)
                    if (cardsService != null) {
                        document.setCouponType(cardsService.getCouponByGuid(chequeCoupon.getCouponTypeGuid()));
                        document.setIssueDate(dpe.getSaleTime());
                    }
                }
                document.setPurchase(purchase);
                purchase.getServiceDocs().add(document);
            }
        }

        for (ChequeAdvertEntity chequeAdvert : dpe.getChequeAdverts()) {
            ServiceDocument document = chequeAdvert.getChequeText();
            if (document instanceof TemplatedServiceDocument && !purchase.getServiceDocs().contains(document)) {
                ((TemplatedServiceDocument) document).setPurchase(purchase);
                purchase.getServiceDocs().add(document);
            }
        }
    }

    @Override
    public void preparePurchaseFiscalization(PurchaseEntity purchase, Check check, LoyTransactionEntity loyTransaction) {
        log.trace("entering preparePurchaseFiscalization()");
        try {
            if (purchase != null && loyTransaction != null && !purchase.isReturn()) {
                if (informixService != null) {
                    preparePurchaseWithInformixBonusesFiscalization(loyTransaction, purchase, check);
                }

                if (propertiesManager != null) {
                    String prolongBonusesEnabled = propertiesManager.getProperty(PDConfig.MODULE_NAME, null, PDConfig.PROLONG_BONUSES, null);
                    if (Boolean.parseBoolean(prolongBonusesEnabled)) {
                        prolongBonuses(check, loyTransaction);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Purchase fiscalization preparation failed", e);
        }
        log.trace("leaving preparePurchaseFiscalization()");
    }

    private void prolongBonuses(Check check, LoyTransactionEntity loyTransaction) {
        // Если были операции по начисления или списанию
        if (!loyTransaction.getBonusTransactions().isEmpty()) {
            loyTransaction.getBonusTransactions().stream()
                    .filter(bt -> bt.getSumAmount() > 0 && BonusDiscountType.BONUS_SR10.equals(bt.getSponsorId()))
                    .forEach(bt -> {
                        // Есть бонусные операции по начислению
                        List<CardEntity> cardEntities = check.getBonusDiscountCards().get(BonusDiscountType.BONUS_SR10);

                        if (CollectionUtils.isEmpty(cardEntities)) {
                            return;
                        }

                        CardEntity cardEntity = cardEntities.get(0);
                        List<CardBonusAccount> bonusAccounts = cardEntity.getBonusAccounts();

                        if (CollectionUtils.isEmpty(bonusAccounts)) {
                            return;
                        }

                        for (CardBonusAccount cba : bonusAccounts) {
                            if (cba != null
                                    && cba.getBonusAccountTypeCode() != null
                                    && cba.getBonusAccountTypeCode() == bt.getBonusAccountType()
                                    && !cba.getSubBallances().isEmpty()) {

                                // Просто обновим дату сгорания. На чеке будет норм
                                cba.getSubBallances().forEach(cardSubBalance -> updateFinishDateForBonuses(bt.getBonusPeriodFinish(), cardSubBalance));
                                cba.getNonActiveSubBalances().forEach(cardSubBalance -> updateFinishDateForBonuses(bt.getBonusPeriodFinish(), cardSubBalance));
                            }
                        }

                    });

        }
    }

    private void updateFinishDateForBonuses(Date newExpirationDate, CardBonusSubBalance subBalance) {
        if (subBalance == null) {
            return;
        }
        if (subBalance.getFinishDate() != null && subBalance.getFinishDate().before(newExpirationDate)) {
            subBalance.setFinishDate(newExpirationDate);
        }

    }

    @Override
    public void purchaseFiscalized(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        // отправить данные о примененных объектах в процессинги
        externalLoyaltyPurchaseConsumers.forEach(consumer -> consumer.consume(purchase, loyTransaction, getProviderName()));
        if (loyaltyCountersService != null) {
            loyaltyCountersService.releaseHold(purchase);
        }
    }

    @Override
    public boolean isLoyTransactionRequiredForStage(TechProcessStage stage) {
        return getConfiguredCoBrandService() != null ||
                (restrictionsService != null
                        && restrictionsService.isEnabled()
                        && stage != TechProcessStage.PREPARE_PURCHASE_FISCALIZATION);
    }

    private CoBrandService getConfiguredCoBrandService() {
        if (coBrandService != null && coBrandService.isCoBrandConfigured()) {
            return coBrandService;
        }
        return null;
    }

    @Override
    public void sendFeedback(FeedbackBundle feedback) {
        if (feedback == null
                || feedback.getFeedback() == null
                || feedback.getFeedback().getDocNumber() == null
                || feedback.getFeedback().getShiftNumber() == null
                || feedback.getFeedback().getCashNumber() == null
                || feedback.getFeedback().getShopNumber() == null
                || feedback.getPurchase() == null
                || feedback.getLoyTransaction() == null) {
            log.warn("sendFeedback failed. Invalid feedback data");
            return;
        }

        externalLoyaltyPurchaseConsumers
                .forEach(consumer -> consumer.consume(feedback.getFeedback(), feedback.getPurchase(), feedback.getLoyTransaction(), getProviderName()));

        if (CoBrandService.PROVIDER_NAME.equals(feedback.getFeedback().getProcessingName())) {
            sendFeedbackToCoBrandService(feedback.getFeedback(), feedback.getPurchase(), feedback.getLoyTransaction());
        }
    }

    /**
     * @return подготовленные для расчета акции
     */
    protected Collection<AdvertisingActionEntity> getFilteredActions(PurchaseEntity purchase, ILoyTechProcess techProcess) {
        return getBaseActionsCollected(purchase, techProcess);
    }

    private Collection<AdvertisingActionEntity> getBaseActionsCollected(PurchaseEntity purchase, ILoyTechProcess techProcess) {
        Collection<AdvertisingActionEntity> baseActions =
                (Collection<AdvertisingActionEntity>) techProcess.getProcessingSession().getValue(CalculateSession.BASE_ACTIONS);
        if (baseActions == null) {
            baseActions = getBaseActions(purchase);
        }
        return baseActions;
    }

    private void sendFeedbackToCoBrandService(LoyExtProviderFeedback feedback, PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        CoBrandService coBrandService = getConfiguredCoBrandService();
        if (coBrandService != null) {
            try {
                if (purchase.isSale()) {
                    coBrandService.addBonus(purchase, loyTransaction);
                }
                LoyFeedbackDao dao = getLoyFeedbackDao();
                if (dao != null) {
                    dao.remove(feedback);
                } else {
                    log.warn("LoyFeedbackDao not initialized! Nothing will be done");
                }
            } catch (InformixServiceConnectionException ce) {
                log.error("", ce);
            }
        }
    }

    @Override
    public void checkCanceled(PurchaseEntity purchase) {
        if (loyaltyCountersService != null) {
            loyaltyCountersService.releaseHold(purchase);
        }
    }

    /**
     * Вернет множество РА, что могут быть применены (теоретически) к указанному чеку в данный момент момент времени.
     * <p/>
     * Implementation Note: предполагается, что в потомках данный будет переопределен.
     *
     * @param receipt чек
     * @return не {@code null}; в крайнем случае вернет пустую коллекцию
     */
    private Collection<AdvertisingActionEntity> getBaseActions(PurchaseEntity receipt) {
        Collection<AdvertisingActionEntity> result;

        log.trace("entering getFilteredActions(PurchaseEntity). The argument is: {}", receipt);

        // 1. Получим РА, действущие на чек:
        result = getActionsProvider().getActions(receipt);
        log.trace("{} actions (before sifting out in accordance with SR-933) were detected", result.size());

        // 2. и вычтем те РА, что не должны сработать (у белорусов):
        //  Проверим, нужно ли исключить акции из калькуляции (SR-933)
        if (receipt != null && CollectionUtils.isNotEmpty(receipt.getAllowedDiscountTypesForPurchase())) {
            // for it
            result.removeIf(aae -> aae == null || !receipt.getAllowedDiscountTypesForPurchase().contains(aae.getDiscountType()));
        }

        log.trace("leaving getFilteredActions(PurchaseEntity). The result size is: {}", result.size());

        return result;
    }

    /**
     * Возвращает только гуиды завершающих РА, которые могут примениться в чеке лояльности
     *
     * @param receipt чек лояльности
     * @param actions РА, действущие на чек
     * @return гуиды завершающих РА
     */
    private Collection<Long> getFinalActionsGuids(PurchaseEntity receipt, Collection<AdvertisingActionEntity> actions) {
        log.trace("entering getFinalActionsGuids(PurchaseEntity).");
        actions = new ArrayList<>(actions);

        // вычтем те РА, что не должны сработать (у белорусов):
        //  Проверим, нужно ли исключить акции из калькуляции (SR-933)
        if (receipt != null && CollectionUtils.isNotEmpty(receipt.getAllowedDiscountTypesForPurchase())) {
            // for it
            actions.removeIf(aae -> aae == null || !receipt.getAllowedDiscountTypesForPurchase().contains(aae.getDiscountType()));
        }

        Collection<Long> resultGuids = DiscountCalculationUtils.getFinalActionsGuids(actions);

        log.trace("leaving getFinalActionsGuids(PurchaseEntity). The result size is: {}", resultGuids.size());

        return resultGuids;
    }

    @Override
    public String toString() {
        return "classic-set-loy-provider";
    }

    /**
     * Для CoBrand выполняет запрос на начисление бонусов/фишек.
     * Корректирует значения бонусного баланса, начисленных и списанных бонусов в фискальном чеке.
     * Выполняется перед фискализацией чека, только для бонусов Informix.
     *
     * @param loyTransaction транзакция лояльности
     * @param purchase       кассовый чек
     * @param check          фискальный чек
     */
    private void preparePurchaseWithInformixBonusesFiscalization(LoyTransactionEntity loyTransaction, PurchaseEntity purchase, Check check) {
        final Optional<PurchaseCardsEntity> card = purchase.getFirstInternalCard();
        if (!card.isPresent() || (purchase.getClientGUID() == null)) {
            return;
        }

        Optional<CardBonusBalance> cardBonusBalance = Optional.empty();
        Long realAddedBonuses = 0L;

        CoBrandService coBrandService = getConfiguredCoBrandService();
        if (coBrandService != null) {
            try {
                realAddedBonuses = coBrandService.addBonus(purchase, loyTransaction);
                cardBonusBalance = Optional.ofNullable(coBrandService.getBalance(purchase.getClientGUID()));
            } catch (InformixServiceConnectionException e) {
                coBrandService.formOfflineSlip(purchase);
                LoyFeedbackDao dao = getLoyFeedbackDao();
                if (dao != null) {
                    LoyExtProviderFeedback feedback = new LoyExtProviderFeedback();
                    feedback.setProviderId(getProviderName());
                    feedback.setFeedbackTime(FeedbackTime.AS_SOON_AS_POSSIBLE);
                    feedback.setShopNumber(purchase.getShift().getShopIndex());
                    feedback.setCashNumber(purchase.getShift().getCashNum());
                    feedback.setShiftNumber(purchase.getShift().getNumShift());
                    feedback.setDocNumber(purchase.getNumber());
                    feedback.setInn(purchase.getInn());
                    feedback.setDateCreate(purchase.getDateCreate());
                    feedback.setProcessingName(CoBrandService.PROVIDER_NAME);
                    dao.saveOrUpdate(feedback);
                } else {
                    log.warn("LoyFeedbackDao not initialized! Nothing will be done");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                try {
                    coBrandService.formOfflineSlip(purchase);
                } catch (Exception e2) {
                    log.error(e2.getLocalizedMessage(), e2);
                }
            }
        }
        if (check != null) {
            adjustInformixBonusBalanceInCheck(cardBonusBalance, realAddedBonuses, card.get(), check);
            Long accruedBonuses = adjustInformixAccruedInCheck(card.get().getNumber(), realAddedBonuses, loyTransaction, check,
                    BonusDiscountType.BONUS_INFORMIX);
            if (accruedBonuses != null) {
                check.setAccruedBonuses(accruedBonuses);
            }
        }
        log.debug("Real added bonuses for card {} equals {}", card.get().getNumber(), realAddedBonuses);

        Long realAddedTokens = 0L;
        if (informixService != null) {
            realAddedTokens = informixService.addTokens(purchase, loyTransaction);
        }
        if (check != null) {
            Long accruedTokens = adjustInformixAccruedInCheck(card.get().getNumber(), realAddedTokens, loyTransaction, check,
                    BonusDiscountType.TOKENS_LENTA);
            if (accruedTokens != null) {
                check.setAccruedTokens(accruedTokens);
            }
        }
        log.debug("Real added tokens for card {} equals {}", card.get().getNumber(), realAddedTokens);
    }

    /**
     * Корректировка баланса бонусов в фискальном чеке.
     *
     * @param realBonusBalance баланс бонусов из сервиса
     * @param realAddedBonuses бонусы добавленные сервисом
     * @param card             карта из чека
     * @param check            фискальный чек
     */
    private static void adjustInformixBonusBalanceInCheck(Optional<CardBonusBalance> realBonusBalance,
                                                          Long realAddedBonuses,
                                                          PurchaseCardsEntity card,
                                                          Check check) {
        InternalCardsEntity cardType = (InternalCardsEntity) card.getCardType();
        if (cardType.getCards().isEmpty()) {
            return;
        }

        CardBonusBalance currentBonusBalance = realBonusBalance.orElse(cardType.getCards().get(0).getCardBonusBalance());
        if (currentBonusBalance != null && currentBonusBalance.getBalance() != null) {
            log.trace("Using bonus balance: {}", currentBonusBalance.toString());

            cardType.setCreditLimit(currentBonusBalance.getBalance().longValue()
                    + ((!realBonusBalance.isPresent() && realAddedBonuses != null) ? realAddedBonuses : 0));
            check.setDiscountCards(Collections.singletonList(card));

            Long finishSum = currentBonusBalance.getFinishSumElementary();
            Date finishDate = currentBonusBalance.getFinishDate();
            if (finishSum != null && finishDate != null) {
                List<CardBonusAccount> bonusAccounts = cardType.getCards().get(0).getBonusAccounts();
                CardBonusAccount bonusAccount = bonusAccounts.stream()
                        .filter(ba -> !BonusAccountsType.TOKENS.equals(ba.getAccountsType())).findFirst().orElse(null);
                if (bonusAccount == null) {
                    bonusAccount = new CardBonusAccount();
                    bonusAccount.setBonusAccountTypeCode(-1L);
                    bonusAccounts.add(bonusAccount);
                }
                // Есть окончательное текущее значение бонусов к сгоранию, полученных от Ленты,
                // однако здесь нет информации какая часть этих бонусов к какому бонусному счету относится,
                // поэтому выставляем фейковое значение типа бонусного счета.
                // Такой под-баланс будет обработан в LentaPurchaseDocumentTemplate.extractBonusChangeInfo().
                CardBonusSubBalance cardBonusSubBalance = new CardBonusSubBalance(0, finishDate, finishSum);
                bonusAccount.getSubBallances().add(cardBonusSubBalance);

                check.getBonusDiscountCards().put(currentBonusBalance.getSponsorId(), cardType.getCards());
            }
        }
    }

    /**
     * Корректировка величины начисленных бонусов/фишек в фискальном чеке.
     *
     * @param cardNumber       номер карты
     * @param realAddedBonuses бонусы добавленные сервисом
     * @param loyTransaction   транзакция лояльности
     * @param check            фискальный чек
     * @return сколько начислено
     */
    private static Long adjustInformixAccruedInCheck(String cardNumber,
                                                     Long realAddedBonuses,
                                                     LoyTransactionEntity loyTransaction,
                                                     Check check, BonusDiscountType bonusDiscountType) {
        if (realAddedBonuses == null || realAddedBonuses < 0) {
            return null;
        }

        // Заполняем для Informix начисленные бонусы снова, потому что на момент создания фискального чека, их количества были неизвестны
        Collection<BonusesReport> actualInformixReports = loyTransaction.getBonusTransactions().stream()
                .filter(t -> t.getDiscountCard().equals(cardNumber))
                .filter(t -> bonusDiscountType.equals(t.getSponsorId()))
                .map(ClassicSetLoyProvider::bonusTransactionToReport)
                .collect(Collectors.toCollection(LinkedList::new));

        final long expectedAddedBonuses = actualInformixReports.stream()
                .mapToLong(BonusesReport::getBonusesAccrued).filter(val -> (val > 0)).sum();
        final long lostBonuses = expectedAddedBonuses - realAddedBonuses;

        // Корректирует суммы операций по начислению бонусов, таким образом чтобы их сумма =realAddedBonuses
        if (lostBonuses > 0) {
            long diff = lostBonuses;
            for (BonusesReport report : actualInformixReports) {
                long accrued = report.getBonusesAccrued();
                if (accrued >= diff) {
                    report.setBonusesAccrued(accrued - diff);
                    break;
                } else {
                    report.setBonusesAccrued(0);
                    diff = diff - accrued;
                }
            }
        } else if (lostBonuses < 0) {
            actualInformixReports.stream()
                    .filter(r -> (r.getBonusesAccrued() >= 0))
                    .findFirst().ifPresent(r -> r.setBonusesAccrued(r.getBonusesAccrued() - lostBonuses));
        }

        Collection<BonusesReport> bonusesOperations = check.getLoyReports().getBonusesOperations();
        bonusesOperations.removeIf(bonusesReport -> bonusesReport.getBonusType() == bonusDiscountType);
        bonusesOperations.addAll(actualInformixReports);
        return actualInformixReports.stream().mapToLong(BonusesReport::getBonusesAccrued).filter(val -> (val > 0)).sum();
    }

    private static BonusesReport bonusTransactionToReport(LoyBonusTransactionEntity entity) {
        BonusesReport result = new BonusesReport();
        result.setCardNumber(entity.getDiscountCard());
        result.setBonusType(entity.getSponsorId());
        result.setBonusesAccrued(entity.getBonusAmount());
        result.setActivationDate(entity.getBonusPeriodStart());
        result.setBestBeforeDate(entity.getBonusPeriodFinish());
        result.setBonusAccountTypeId(entity.getBonusAccountType());
        return result;
    }

    public void setPluginsAdapter(AbstractPluginsAdapter pluginsAdapter) {
        this.pluginsAdapter = pluginsAdapter;
    }

    public void setCardsService(CardsService cardsService) {
        this.cardsService = cardsService;
    }

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    public void setCurrencyHandler(CurrencyHandler currencyHandler) {
        this.currencyHandler = currencyHandler;
    }
}
