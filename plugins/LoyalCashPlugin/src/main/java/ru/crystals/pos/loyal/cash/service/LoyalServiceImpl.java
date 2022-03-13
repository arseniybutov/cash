package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardBonusAccount;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.common.WholesaleRestriction;
import ru.crystals.cards.coupons.CouponsEntity;
import ru.crystals.cards.internalcards.InternalCards;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.cards.internalcards.WholesaleRestrictionEntity;
import ru.crystals.discount.processing.WholesaleRestrictionsExtractor;
import ru.crystals.discount.processing.entity.CFTProcessingUser;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyBonusDiscountTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyGiftNoteEnity;
import ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discount.processing.entity.LoyUpdateCounterEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.enums.ActionPluginAttributes;
import ru.crystals.discounts.enums.ActionPluginRegistry;
import ru.crystals.discounts.enums.GiftDisplayTime;
import ru.crystals.discounts.interfaces.ActionPluginAttributable;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.calculation.AdvertisingActionUtils;
import ru.crystals.loyal.calculation.BonusDistributer;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.calculation.CycleProcessing;
import ru.crystals.loyal.calculation.DirectProcessing;
import ru.crystals.loyal.calculation.DiscountCalculationUtils;
import ru.crystals.loyal.calculation.DoProcessing;
import ru.crystals.loyal.calculation.FL54Tuner;
import ru.crystals.loyal.calculation.LoyPurchaseValidator;
import ru.crystals.loyal.calculation.LoyTechProcess;
import ru.crystals.loyal.calculation.LoyTransactionCalculator;
import ru.crystals.loyal.calculation.PurchaseUtils;
import ru.crystals.loyal.calculation.filter.bydepartment.FilterPositionByDepartments;
import ru.crystals.loyal.calculation.filters.FilterPack;
import ru.crystals.loyal.calculation.filters.FilterPositionHolder;
import ru.crystals.loyal.calculation.filters.byproduct.FilerPositionByProduct;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.PositionDescription;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.AppliedActionInfo;
import ru.crystals.loyal.check.discount.BonusAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeCouponAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.DiscountEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.check.discount.DiscountType;
import ru.crystals.loyal.check.discount.GiftNoteDescription;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.crutches.FL54LoyUtils;
import ru.crystals.loyal.crutches.HardcodedAction;
import ru.crystals.loyal.interfaces.ActionIntrospector;
import ru.crystals.loyal.interfaces.IActionResultPlugin;
import ru.crystals.loyal.interfaces.IApplyObjectPlugin;
import ru.crystals.loyal.interfaces.IBonusActionResultPlugin;
import ru.crystals.loyal.interfaces.IExternalConditionPlugin;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.interfaces.LoyTransactionProvider;
import ru.crystals.loyal.interfaces.LoyTransactionProviderCash;
import ru.crystals.loyal.model.ActionSelectCondition;
import ru.crystals.loyal.model.SimpleShiftInfo;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.providers.LoyProcessingException;
import ru.crystals.loyal.providers.LoyProcessingTryItAgainException;
import ru.crystals.loyal.providers.LoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistry;
import ru.crystals.pos.CashException;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.PKGenerated;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.CardsEvent;
import ru.crystals.pos.cards.api.SetApiBonusesChargeOffService;
import ru.crystals.pos.cards.coupons.SetCouponingCashSettings;
import ru.crystals.pos.cards.crm.CRMCardsService;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.cards.external.ExternalLoyaltyService;
import ru.crystals.pos.cards.informix.CoBrandService;
import ru.crystals.pos.cards.service.WholesaleRestrictionsDao;
import ru.crystals.pos.cards.types.ExtendedCardType;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.ManualAdvertisingActionEntity;
import ru.crystals.pos.check.ManualPositionAdvertisingActionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionProductionDateEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusPositionReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BonusesReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.fiscalprinter.datastruct.documents.LoyReports;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PositionCouponsReportDocument;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.loyal.LoyalEvent;
import ru.crystals.pos.loyal.LoyaltyArguments;
import ru.crystals.pos.loyal.LoyaltyResults;
import ru.crystals.pos.loyal.bridge.service.LoyalBridgeServiceImpl;
import ru.crystals.pos.loyal.cash.converter.DiscountLoyTransactionConverter;
import ru.crystals.pos.loyal.cash.converter.DiscountPurchaseConverter;
import ru.crystals.pos.loyal.cash.converter.LoyalCalculatorConverter;
import ru.crystals.pos.loyal.cash.converter.LoyalCashConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.loyal.cash.transport.discountresults.TransferManager;
import ru.crystals.pos.loyalty.LoyaltyRestrictionsService;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.salemetrics.JmxMetrics;
import ru.crystals.pos.speed.SpeedLog;
import ru.crystals.pos.techprocess.GiftsScanningScenario;
import ru.crystals.pos.techprocess.SubtotalScenarioInterface;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.techprocess.TechProcessStage;
import ru.crystals.pos.transport.ObjectForSendFeedback;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.pos.utils.FL54Utils;
import ru.crystals.utils.UnboxingUtils;
import ru.crystals.utils.discount.ChequeCouponAdvertisingActionResultHelper;
import ru.crystalservice.setv6.discounts.common.vo.ActionPriceInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Реализация {@link Loyal интерфейса модуля "скидки"}, позволяющая производить расчёт скидок на кассе. Кроме этого класс предоставляет возможность
 * работы с транзакицями расчёта скидок.
 *
 * @author ppavlov
 * @author A.Martynov
 * @author aperevozchikov
 */
public class LoyalServiceImpl extends LoyalBridgeServiceImpl implements LoyTransactionProvider, LoyTransactionProviderCash, LoyalPropertiesUpdater {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PART_DISCOUNT_ENABLE_PROPERTY = "part.discount.enabled";
    private static final String DISABLED_PURCHASE_VALIDATOR_PROPERTY = "loy.purchase.validator.disabled";

    private final List<BonusDiscountType> bonusDiscountTypes = Arrays.asList(BonusDiscountType.BONUS_SR10, BonusDiscountType.BONUS_INFORMIX);
    private final List<BonusDiscountType> tokensDiscountTypes = Arrays.asList(BonusDiscountType.TOKENS_SR10, BonusDiscountType.TOKENS_LENTA);

    private Properties properties = new Properties();

    private LoyTransactionEntity cachedLoyTransaction = null;

    private boolean started = false;

    private ILoyTechProcess loyTechProcess = null;
    private TransferManager transferManager = null;

    @Autowired
    private CheckService checkService = null;

    @Autowired(required = false)
    private CoBrandService coBrandService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private PropertiesManager propertiesManager;

    @Autowired
    private CardsEvent cardsEvent;

    @Autowired
    private CurrencyHandler currencyHandler;

    @Autowired
    private TechProcessEvents techProcessEvents;

    @Autowired(required = false)
    private LoyaltyRestrictionsService restrictionsService;

    @Autowired(required = false)
    private CRMCardsService crmCardsService;

    // обновленный механизм работы с БД пошел:

    // injected
    /**
     * Через эту штуку будем сохранять/читать {@link LoyTransactionEntity TX лояльности} в/из БД.
     */
    private LoyTxDao loyTxDao;

    // injected
    /**
     * А через эту штуку будем добывать из БД инфу о существующих активных {@link AdvertisingActionEntity РА}.
     */
    private CashAdvertisingActionDao actionsDao;

    // injected
    /**
     * Кэш рекламных акций; заполняется в момент старта активными (и будущими акциями)
     */
    private AdvActionsCache cache;

    // injected
    /**
     * Различная информация о существующих РА
     */
    private ActionIntrospector actionsIntrospector;

    /**
     * Доступ к локальным значениям оптовых ограничений
     */
    private WholesaleRestrictionsDao wrDao;

    /**
     * Реестр поставщиков "услуг лояльности". Через него считаем скидки на чек
     */
    @Autowired
    private LoyProvidersRegistryWrapper loyProvidersWrapper;

    @Autowired
    private InternalCashPoolExecutor executor;

    @Autowired
    private InternalCards internalCards;

    @Autowired
    private SetApiBonusesChargeOffService setApiBonusesChargeOffService;

    @Autowired(required = false)
    private SetCouponingCashSettings setCouponingCashSettings;

    /**
     * @return Объект для работы с поставщиками лояльности
     */
    private LoyProvidersRegistry getLoyProviders() {
        return loyProvidersWrapper.getLoyProviders();
    }

    @Override
    public void start() {
        long time = SpeedLog.getTime();
        started = true;
        SpeedLog.logModuleLoadingTime("Loyal", time);
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * Обрабатывает указанные импорттированные РА: записывает их в БД и обновляет кэш РА.
     *
     * @param importedActions список {@link AdvertisingActionEntity РА}, что были импортированы - именнов в этом порядке (порядок элементов важен: более поздняя
     *                        версия той же самой РА (по GUID'у) имеет приоритет: более ранняя версия будет проигнорена полностью).
     * @throws RuntimeException если не удалось сохранить в БД указанные имопртированные РА
     */
    public void actionsImportedEvent(List<AdvertisingActionEntity> importedActions) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering actionsImportedEvent(List). The argument is: {}", importedActions);
        if (importedActions != null && !importedActions.isEmpty()) {
            // 1. удалим дубли
            Map<Long, AdvertisingActionEntity> actionsAsMap = new HashMap<>(importedActions.size());
            for (AdvertisingActionEntity aae : importedActions) {
                if (aae == null || aae.getGuid() == null) {
                    // как этот левак сюда попал?
                    log.error("actionsImportedEvent(List): an INVALID element [{}] was detected in the argument: " +
                            "either NULL, or its GUID is NULL!", aae);
                    continue;
                }
                // акция валидна
                if (actionsAsMap.containsKey(aae.getGuid())) {
                    // та же самая РА дважды в одной пачке. первую выкинем. просто предупредим:
                    if (log.isWarnEnabled()) {
                        log.warn("actionsImportedEvent(List): duplicate action [guid: {}] was detected! " +
                                        "So, the first one/version [{}] will be removed from further processing and " +
                                        "the latter [{}] will be passed further",
                                aae.getGuid(), actionsAsMap.get(aae.getGuid()), aae);
                    }
                }
                actionsAsMap.put(aae.getGuid(), aae);
            }

            // 2. зальем эти акции без дублей в БД:
            Map<Long, AdvertisingActionEntity> persistedActive = actionsDao.removeExistingAndSave(actionsAsMap.values());
            log.trace("[{}] active actions out of total [{}] were persisted", persistedActive.size(), actionsAsMap.size());

            // 3. Если не вылетели при сохранении - обновим кэш:
            // 3.1. активные обновим:
            log.trace("updating [{}] actions in the cache", persistedActive.size());
            for (AdvertisingActionEntity aae : persistedActive.values()) {
                cache.update(aae);
            }

            // 3.2. остальные удалим:
            actionsAsMap.keySet().removeAll(persistedActive.keySet());
            log.trace("removing [{}] stale actions from the cache", actionsAsMap.size());
            for (AdvertisingActionEntity aae : actionsAsMap.values()) {
                cache.remove(aae);
            }

            // 3.3. активные и удаленные обновим у ActionIntrospector
            actionsIntrospector.updateActions(persistedActive.values(), actionsAsMap.values());
        } else {
            log.warn("actionsImportedEvent(List): the argument is EMPTY!");
        }
        log.trace("leaving actionsImportedEvent(List). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    public ILoyTechProcess getLoyTechProcess() {
        if (loyTechProcess == null) {
            log.trace("creating LoyTechProcess..");
            long t = System.currentTimeMillis();
            try {
                loyTechProcess = new LoyTechProcess(getLoyaltyProperties());
                loyTechProcess.getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);
                // SRL-564, SR-4553: не будем сбрасывать счетчик купонов при каждом расчете скидок, а сделаем это один раз в начале расчета
                // лояльности (doLoyalty), т.к. за один "проход" может быть несколько расчетов в поставщиках услуг лояльности.
                // Например, CustomSetLoyProvider может быть вызван несколько раз с разным набором фильтров РА.
                loyTechProcess.getProcessingSession().put(CalculateSession.RESET_UNIQUE_COUPON_SEQUENCE_NUMBER, Boolean.FALSE.toString());
                if (setCouponingCashSettings != null) {
                    loyTechProcess.getLoyaltyProperties().setCouponProcessingEnabled(setCouponingCashSettings.isEnabled());
                    loyTechProcess.getLoyaltyProperties().setCashGeneratedProcessingUniqueCouponPrefix(setCouponingCashSettings.getCashPrefix());
                    loyTechProcess.getLoyaltyProperties().setServerGeneratedProcessingUniqueCouponPrefix(setCouponingCashSettings.getServerPrefix());
                }
            } finally {
                log.trace("LoyTechProcess created; time: {} ms", System.currentTimeMillis() - t);
            }
        }

        return loyTechProcess;
    }

    @Override
    public DiscountsReport getDiscountsReport(PurchaseEntity purchase) {
        return getLoyReports(purchase).getDiscounts();
    }

    private LoyTransactionEntity getLoyTransactionEntity(PurchaseEntity purchase) {
        LoyTransactionEntity loyTransactionEntity = getDiscountResults(purchase, false);
        if (loyTransactionEntity == null) {
            loyTransactionEntity = getDiscountResults(purchase, true);
        }
        return loyTransactionEntity;
    }

    @Override
    public ManualAdvertisingActionEntity[] getManualAction() {
        long t = System.currentTimeMillis();
        try {
            List<ManualAdvertisingActionEntity> manualActions = cache.getManualActions();
            Collections.sort(manualActions, Comparator.comparing(ManualAdvertisingActionEntity::getActionName, Comparator.naturalOrder()));
            return manualActions.toArray(new ManualAdvertisingActionEntity[manualActions.size()]);
        } finally {
            log.trace("time of getManualActions: {} ms", System.currentTimeMillis() - t);
        }
    }

    private Set<Long> getManualAdvertisingActionsGuids(PurchaseEntity purchase) {
        log.trace("get manual advertising actions guids in purchase..");

        Set<Long> manualActionGuids = new HashSet<>();
        if (purchase == null) {
            return manualActionGuids;
        }

        if (purchase.getPositions() != null) {
            for (PositionEntity position : purchase.getPositions()) {
                if (position.getManualAdvertisingActions() != null) {
                    for (ManualPositionAdvertisingActionEntity manualAdvertisingAction : position.getManualAdvertisingActions()) {
                        manualActionGuids.add(manualAdvertisingAction.getActionGuid());
                    }
                }
            }
        }

        log.trace("number of manual advertising actions guids in purchase = {}", manualActionGuids.size());

        return manualActionGuids;
    }

    public AdvertisingActionEntity[] getAdvertisingActions(Purchase purchase) {
        return getAdvertisingActions(PurchaseUtils.getManualActionsGuids(purchase), purchase.getExternalActions());
    }

    public AdvertisingActionEntity[] getAdvertisingActions(PurchaseEntity purchase) {
        Set<Long> manualActionsGuids = getManualAdvertisingActionsGuids(purchase);
        return getAdvertisingActions(manualActionsGuids, Collections.emptySet());
    }

    public AdvertisingActionEntity[] getAdvertisingActions(Set<Long> manualActionGuids, Collection<AdvertisingActionEntity> externalActions) {
        log.debug("getAdvertisingActions.. number of manual actions = " + (manualActionGuids == null ? 0 : manualActionGuids.size()));

        long tm = System.currentTimeMillis();

        List<AdvertisingActionEntity> result = new ArrayList<>();

        List<AdvertisingActionEntity> activeActions = this.cache.getActiveActions(new Date(), manualActionGuids);
        log.debug("time of get active actions: {}", System.currentTimeMillis() - tm);

        result.addAll(activeActions);
        result.addAll(externalActions);

        return result.toArray(new AdvertisingActionEntity[0]);
    }

    /**
     * Применение ограничения на количество выдаваемых купонов в чеке
     * Учитываем только те, у которых есть тип выдаваемого купона (серийные и несерийные купоны)
     * Если настройка maxCountOfGeneratedCouponsInCheck = 0, то считаем что нет ограничения
     *
     * @param purchase                          кассовый чек
     * @param maxCountOfGeneratedCouponsInCheck максимальное кол-во купонов выдавамемых в чеке
     * @param actions                           список рекламных акции действующих на чек
     */
    void checkCouponsCountAndDeleteExcess(Purchase purchase, int maxCountOfGeneratedCouponsInCheck, AdvertisingActionEntity[] actions) {
        log.trace("starting checkCouponsCountAndDeleteExcess(Purchase, int). The arguments are: purchase [{}], maxCountOfGeneratedCouponsInCheck = {}",
                purchase, maxCountOfGeneratedCouponsInCheck);

        if (maxCountOfGeneratedCouponsInCheck < 1) {
            log.trace("leaving checkCouponsCountAndDeleteExcess(Purchase, int). maxCountOfGeneratedCouponsInCheck = {} therefore we have no restriction",
                    maxCountOfGeneratedCouponsInCheck);
            return;
        }

        if (purchase == null || CollectionUtils.isEmpty(purchase.getAdvertisingActionResults())) {
            log.trace("leaving checkCouponsCountAndDeleteExcess(Purchase, int). Purchase have not result of actions");
            return;
        }

        List<ChequeCouponAdvertisingActionResultEntity> couponsResult = purchase.getAdvertisingActionResults().stream()
                .filter(a -> a instanceof ChequeCouponAdvertisingActionResultEntity)
                .map(ChequeCouponAdvertisingActionResultEntity.class::cast)
                .collect(Collectors.toList());
        if (couponsResult.size() <= maxCountOfGeneratedCouponsInCheck) {
            log.trace("leaving checkCouponsCountAndDeleteExcess(Purchase, int). There are {} coupons in purchase", couponsResult.size());
            return;
        }

        Set<Long> secondaryActionsGuids = PurchaseUtils.getSecondaryActionsGuids(purchase);

        Map<Long, Double> actionsWithPriority = Arrays.stream(actions)
                .filter(Objects::nonNull)
                .filter(a -> secondaryActionsGuids.contains(a.getGuid()))
                .collect(Collectors.toMap(AdvertisingActionEntity::getGuid, AdvertisingActionEntity::getPriority));

        Comparator<ChequeCouponAdvertisingActionResultEntity> comparingByActions =
                ChequeCouponAdvertisingActionResultHelper.compareByActionPriority(actionsWithPriority);

        // удалим все результаты связанные с выдачей купона
        purchase.getAdvertisingActionResults().removeIf(a -> a instanceof ChequeCouponAdvertisingActionResultEntity);

        // определим наиболее приоритетные купоны
        List<ChequeCouponAdvertisingActionResultEntity> maxPriorityCoupons = couponsResult.stream()
                .sorted(ChequeCouponAdvertisingActionResultHelper.comparingByPriority.thenComparing(comparingByActions))
                .limit(maxCountOfGeneratedCouponsInCheck)
                .collect(Collectors.toList());

        purchase.getAdvertisingActionResults().addAll(maxPriorityCoupons);

        log.trace("leaving checkCouponsCountAndDeleteExcess(Purchase, int). There are {} coupons in purchase, excess {} coupons were deleted",
                maxPriorityCoupons.size(), couponsResult.size() - maxPriorityCoupons.size());
    }

    /**
     * Генерирует транзакцию лояльности
     *
     * @param purchase - чек, для которого нужна транзакция
     * @return - транзакция
     */
    public LoyTransactionEntity getLoyTx(Purchase purchase) {
        AdvertisingActionEntity[] actions = getAdvertisingActions(purchase);
        CashAdvResultPersistenceManager cashAdvResultPersistenceManager = new CashAdvResultPersistenceManager(getLoyTxDao(), actions);
        return getLoyTx(purchase, cashAdvResultPersistenceManager);
    }

    /**
     * @param purchase                        - чек, для которого нужна транзакция
     * @param cashAdvResultPersistenceManager - проинициализированный CashAdvResultPersistenceManager
     * @return - транзакция
     */
    public LoyTransactionEntity getLoyTx(Purchase purchase, CashAdvResultPersistenceManager cashAdvResultPersistenceManager) {
        return cashAdvResultPersistenceManager.createLoyTx(purchase, false, currencyHandler);
    }

    /**
     * Сохраняет транзакцию в БД
     *
     * @param loyTransactionEntity - транзакция
     * @return - транзакцию после сохранения
     */
    public LoyTransactionEntity saveLoyTx(LoyTransactionEntity loyTransactionEntity) {
        CashAdvResultPersistenceManager cashAdvResultPersistenceManager = new CashAdvResultPersistenceManager(getLoyTxDao(), null);
        return saveLoyTx(loyTransactionEntity, cashAdvResultPersistenceManager);
    }

    /**
     * Сохраняет транзакцию в БД
     *
     * @param loyTransactionEntity            - транзакция
     * @param cashAdvResultPersistenceManager - проинициализированный CashAdvResultPersistenceManager
     * @return - транзакцию после сохранения
     */
    public LoyTransactionEntity saveLoyTx(LoyTransactionEntity loyTransactionEntity, CashAdvResultPersistenceManager cashAdvResultPersistenceManager) {
        return cashAdvResultPersistenceManager.saveProcessedLoyTransaction(loyTransactionEntity, false);
    }

    private LoyTransactionEntity saveDiscountResults(Purchase purchase, AdvertisingActionEntity[] actions) {
        long st = System.currentTimeMillis();

        if (actions == null) {
            actions = getAdvertisingActions(purchase);
        }

        CashAdvResultPersistenceManager cashAdvResultPersistenceManager = new CashAdvResultPersistenceManager(getLoyTxDao(), actions);
        //Сгенерируем транзакцию
        LoyTransactionEntity tx = getLoyTx(purchase, cashAdvResultPersistenceManager);
        //Сохраним транзакцию
        tx = saveLoyTx(tx, cashAdvResultPersistenceManager);
        cachedLoyTransaction = tx;

        if (log.isDebugEnabled()) {
            StringBuilder s = new StringBuilder();
            s.append("Save processed purchase and discounts (Purchase #").append(purchase.getNumber() == null ? "new" : purchase.getNumber());
            s.append("; Number of positions: ").append(purchase.getPositions() == null ? 0 : purchase.getPositions().size());
            s.append("). Time: ").append(System.currentTimeMillis() - st).append(" ms.");
            log.info(s.toString());
        }

        return tx;
    }


    public LoyTransactionEntity searchDiscountResultsInDB(Long loyTransactionId, boolean useCached) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering searchDiscountResultsInDB(Long, boolean). The arguments are: loyTransactionId [{}], useCached [{}]", loyTransactionId, useCached);

        if (loyTransactionId != null) {
            if (useCached && (cachedLoyTransaction != null) && loyTransactionId.equals(cachedLoyTransaction.getId())) {
                result = cachedLoyTransaction;
            } else {
                result = loyTxDao.getLoyTxById(loyTransactionId);
            }
        } else {
            log.warn("searchDiscountResultsInDB(Long, boolean): the \"loyTransactionId\" argument is NULL! NULL will be returned!");
        }

        log.trace("leaving searchDiscountResultsInDB(Long, boolean). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    public void updateLoyTransactionNumber(PurchaseEntity purchase) {
        LoyTransactionEntity transaction = getLoyTransactionEntity(purchase);
        if (purchase != null && purchase.getLoyalTransactionId() != null && transaction != null && transaction.getPurchaseNumber() == -1 && purchase.getNumber() != null) {
            transaction.setPurchaseNumber(purchase.getNumber());
        }
        updateLoyTransaction(transaction);
    }

    private LoyTransactionEntity getDiscountResults(PurchaseEntity purchase, boolean useCached) {
        LoyTransactionEntity result = null;

        if (purchase != null && !EMPTY_LOYTRANSACTION_ID.equals(purchase.getLoyalTransactionId())) {
            if (purchase.getLoyalTransactionId() != null) {
                result = searchDiscountResultsInDB(purchase.getLoyalTransactionId(), useCached);
            }

            // Если транзакция не найдена в кэше и базе данных, пытаемся
            // запросить с сервера
            if (result == null) {
                log.debug("get loy tx from server..");
                Long loyTransactionId = getTransferManager().receiveLoyTransaction(purchase);
                log.debug("loy tx id from server: {}", loyTransactionId);

                purchase.setLoyalTransactionId(loyTransactionId);

                PurchaseEntity originalPurchase = getPurchaseFullData(purchase);
                if (originalPurchase != null) {
                    originalPurchase.setLoyalTransactionId(loyTransactionId);
                    updateDocumentEntity(originalPurchase);
                } else {
                    log.warn("Could't update original purchase fro received loyTransaction, can't find original purchase");
                }

                purchase.setLoyalTransactionId(loyTransactionId);
                result = searchDiscountResultsInDB(purchase.getLoyalTransactionId(), useCached);
            }
        }
        return result;
    }

    public void updateDocumentEntity(PurchaseEntity purchase) {
        checkService.updateDocumentEntity(purchase);
    }

    public PurchaseEntity getPurchaseFullData(PurchaseEntity purchase) {
        return checkService.getPurchaseByID(purchase.getId());
    }

    /**
     * Пересчитывает сумм позиций и скидок в соответствии с изменившимся количествами
     *
     * @param purchase чек возврата
     */
    private void calculateSumsForReturnPurchase(PurchaseEntity purchase) {
        if (purchase != null && purchase.getPositions() != null) {
            long checkSumStart = 0L;
            long discountValueTotal = 0L;
            for (PositionEntity position : purchase.getPositions()) {
                checkSumStart += position.getSum() + position.getSumDiscount();
                discountValueTotal += position.getSumDiscount();
            }
            purchase.setCheckSumStart(checkSumStart);
            purchase.setDiscountValueTotal(discountValueTotal);
        }
    }

    @Override
    public synchronized PurchaseEntity doDiscountForReturn(PurchaseEntity returnPurchase) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering doDiscountForReturn(PurchaseEntity). The argument is: returnPurchase [{}]", returnPurchase);
        if (returnPurchase != null) {
            // пересчет сумм позиций и скидок для чека возврата
            calculateSumsForReturnPurchase(returnPurchase);

            // удаление сохраненных результатов, если таковые имеются
            deleteLoyTransaction(returnPurchase.getLoyalTransactionId());

            createLoyTxForReturnPurchase(returnPurchase);
        } else {
            log.warn("doDiscountForReturn(PurchaseEntity): the argument is NULL!");
        }
        log.trace("leaving doDiscountForReturn(PurchaseEntity). The result is: {}; it took {} [ms]",
                returnPurchase, System.currentTimeMillis() - stopWatch);

        return null;
    }

    @Override
    public synchronized PurchaseEntity createLoyTxForDividedPurchases(PurchaseEntity parentPurchase) {

        if (parentPurchase == null || parentPurchase.getDividedPurchases().isEmpty()) {
            return parentPurchase;
        }

        // поиск транзакции рассчитанных скидок для исходного чека
        LoyTransactionEntity parentTransaction = findLoyTransaction(parentPurchase);
        if (parentTransaction == null) {
            return parentPurchase;
        }

        for (PurchaseEntity subPurchase : parentPurchase.getDividedPurchases()) {

            LoyTransactionEntity subTransaction = LoyTransactionCalculator.calculateLoyTransactionForDividedPurchase(subPurchase, parentTransaction);

            if (subTransaction != null) {
                //Сохраняем транзакцию лояльности и привязываем к чеку
                subTransaction = loyTxDao.saveLoyTx(subTransaction);
                subPurchase.setLoyalTransactionId(subTransaction.getId());
                DiscountLoyTransactionConverter.applyPurchaseDiscount(subPurchase, subTransaction);
            }

        }

        return parentPurchase;
    }


    @Override
    public synchronized PurchaseEntity createLoyTxForReturnPurchase(PurchaseEntity returnPurchase) {
        // поиск транзакции рассчитанных скидок для исходного чека
        LoyTransactionEntity originalLoyTransaction = getDiscountResults(returnPurchase.getSuperPurchase(), false);
        if (originalLoyTransaction != null) {
            PurchaseEntity originalSuperPurchase = getPurchaseFullData(returnPurchase);
            if (originalSuperPurchase.getSuperPurchase() != null) {
                originalSuperPurchase = originalSuperPurchase.getSuperPurchase();
                originalSuperPurchase = getPurchaseFullData(originalSuperPurchase);
            }
            // расчет скидок для чека возврата на основании результатов расчета исходного чека
            ShiftEntity shift = returnPurchase.getShift() != null ? returnPurchase.getShift() : tp.getShift();
            SimpleShiftInfo shiftInfo = new SimpleShiftInfo();
            shiftInfo.cashNumber = shift != null ? shift.getCashNum() : techProcessEvents.getCashProperties().getCashNumber();
            shiftInfo.shiftNumber = shift != null ? shift.getNumShift() : -1;
            shiftInfo.shopNumber = shift != null ? shift.getShopIndex() : techProcessEvents.getCashProperties().getShopIndex();

            boolean retAccrued = getLoyaltyProperties().isReturnAccruedBonuses();
            boolean retChargedOff = getLoyaltyProperties().isReturnChargedOffBonuses();

            // и "округлятор" в техпроцесс расчета скидок сразу же запихнем:
            LoyTransactionEntity loyTx = LoyTransactionCalculator.calculateLoyTransactionForReturn(originalLoyTransaction, returnPurchase, originalSuperPurchase,
                    shiftInfo, currencyHandler, retAccrued, retChargedOff);

            // SR-1506: ФЗ 54: подхачим размер скидки на чек
            FL54Utils.fixNonDistributedDiscountValue(returnPurchase, originalSuperPurchase);

            // сохранение рассчитанного
            if (loyTx != null) {
                try {
                    loyTx = loyTxDao.saveLoyTx(loyTx);

                    returnPurchase.setLoyalTransactionId(loyTx.getId());
                    DiscountLoyTransactionConverter.applyPurchaseDiscount(returnPurchase, loyTx);
                } catch (Exception t) {
                    log.error(String.format("createLoyTxForReturnPurchase(PurchaseEntity): failed to save loy-tx [%s]", loyTx), t);
                }
            } else {
                // а это необычно. Сюда попадаем, если либо чек, либо оригинальная TX == NULL. Т.е., по алгоритму выше - никогда:
                log.error("createLoyTxForReturnPurchase(PurchaseEntity): failed to create loy-tx for the money-back receipt [{}]!", returnPurchase);
            }
        } else {
            returnPurchase.setLoyalTransactionId(EMPTY_LOYTRANSACTION_ID);
        }

        return returnPurchase;
    }

    @Override
    public void doPreDiscount(PurchaseEntity purchase) {
        try {
            LoyaltyArguments arguments = new LoyaltyArguments();
            arguments.setPreDiscount(true);
            arguments.getOnlyProviders().add(LoyProvider.SET10_PROVIDER_NAME);
            LoyaltyResults loyResult = doLoyalty(purchase, arguments);
            purchase.setLoyPurchase(loyResult.getLoyalPurchase());
            purchase.getPositions().forEach(pos ->
                    pos.setLoyPosition(loyResult.getLoyalPurchase().getPositionById(pos.getId(), pos.getNumber())));
        } catch (Exception e) {
            LOG.error("doPreDiscount failed!", e);
        }
    }

    /**
     * Вычисляет всю "лояльность" (все результаты РА) и кладёт в результат
     *
     * @param originalPurchase кассовый чек
     * @return результаты вычисления лояльности
     * @throws LoyProcessingException Ошибки, возникшие при процессинге "лояльности"
     */
    public LoyaltyResults doLoyalty(PurchaseEntity originalPurchase, LoyaltyArguments arguments) throws LoyProcessingException {
        LoyaltyResults result = new LoyaltyResults(originalPurchase);

        log.trace("entering doLoyalty(PurchaseEntity)");

        long time = System.currentTimeMillis();
        ILoyTechProcess loyTechProcess = getLoyTechProcess();
        // Нужно в сессию положить filter
        putFilter(loyTechProcess);
        // Положим заводский номер кассы
        putFactoryNum(loyTechProcess);
        //Положим список провайдеров, для которых не нужно считать скидки
        putArguments(arguments, loyTechProcess);
        putProperties(loyTechProcess);
        // SRL-564, SR-4553: "обнулим" последовательный номер последнего сгенеренного купона
        loyTechProcess.getProcessingSession().put(CalculateSession.UNIQUE_COUPON_SEQUENCE_NUMBER, null);
        long prepareBefore = System.currentTimeMillis() - time;

        //Сам расчёт скидок
        Purchase purchase = getLoyProviders().process(originalPurchase, loyTechProcess, getTechProcess());

        time = System.currentTimeMillis();
        //Сохраним калькулированный чек
        result.setLoyalPurchase(purchase);

        if (purchase != null) {
            log.info("Discount calculation (Purchase #{}; Positions: {}",
                    purchase.getNumber() == null ? "new" : purchase.getNumber(),
                    purchase.getPositions() == null ? 0 : purchase.getPositions().size()
            );

            // 4. Для бонусных карт "подпилим" высчитанные потолки списаний: чтоб они не были больше остаточной суммы чека
            adjustBonusDiscountCeilings(purchase);

            // 5. Достанем выходные данные и техпроцесса лояльности
            attachProvidersOutputData(result, loyTechProcess);
        }
        long prepareAfter = System.currentTimeMillis() - time;
        JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_CALCULATION_PREPARE, prepareBefore + prepareAfter);

        log.trace("leaving doLoyalty(PurchaseEntity)");
        return result;
    }

    private void attachProvidersOutputData(LoyaltyResults result, ILoyTechProcess loyTechProcess) {
        Object objOutputData = loyTechProcess.getProcessingSession().getValue(CalculateSession.LOYALTY_PROVIDER_OUTPUT_DATA);
        if (objOutputData instanceof Map) {
            Map<String, Object> outputData = (Map<String, Object>) objOutputData;
            result.setProviderOutputData(outputData);
        }
    }

    /**
     * Для кассы Android - рассчет скидок
     *
     * @param originalPurchase оригинальный чек
     * @param arguments        посчитанные скидки
     * @return результаты расчета скидок
     * @throws LoyProcessingException ошибка
     */
    public LoyaltyResults doLoyaltyAndApply(PurchaseEntity originalPurchase, LoyaltyArguments arguments) throws LoyProcessingException {
        log.trace("entering doLoyaltyAndApply(PurchaseEntity)");
        LoyaltyResults results = doLoyalty(originalPurchase, arguments);
        if (results.getLoyalPurchase() != null) {
            // Приложим сработавщие транзакции
            results.setTransactionEntity(getLoyTx(results.getLoyalPurchase()));
        }
        AdvertisingActionEntity[] actions = getAdvertisingActions(originalPurchase);
        // применим новое распределение позиций
        LoyalCashConverter.applyPurchasePositions(results.getReceipt(), results.getLoyalPurchase(), currencyHandler);
        applyPurchaseResult(results.getReceipt(), results.getLoyalPurchase(), actions, results.getTransactionEntity());
        log.trace("leaving doLoyaltyAndApply(PurchaseEntity)");
        return results;
    }

    protected void putArguments(LoyaltyArguments arguments, ILoyTechProcess loyTechProcess) {
        loyTechProcess.getProcessingSession().put(CalculateSession.EXCLUDED_LOYALTY_PROCESSINGS,
                arguments != null ? arguments.getExcludedProviders() : Collections.emptyList());
        loyTechProcess.getProcessingSession().put(CalculateSession.LOYALTY_PROVIDER_INPUT_DATA,
                arguments != null ? arguments.getProviderInputData() : Collections.emptyMap());
        loyTechProcess.getProcessingSession().put(CalculateSession.ONLY_LOYALTY_PROCESSINGS,
                arguments != null ? arguments.getOnlyProviders() : Collections.emptyList());
        loyTechProcess.getProcessingSession().put(CalculateSession.PRE_DISCOUNT_ENABLED,
                arguments != null ? arguments.isPreDiscount() : Boolean.FALSE);
    }

    protected void putFactoryNum(ILoyTechProcess loyTechProcess) {
        try {
            loyTechProcess.getProcessingSession().put(CalculateSession.CASH_FACTORY_NUMBER, tp.getCashParams().getFactoryNum());
        } catch (Exception e) {
            log.error("Couldn't get factory num", e);
        }
    }

    private void putProperties(ILoyTechProcess loyTechProcess) {
        if (tp != null && tp.getProperties() != null) {
            loyTechProcess.getProcessingSession().put(CalculateSession.CASH_TEMPLATE_GUID, tp.getProperties().getCashTemplateGuid());
        }
    }

    public synchronized LoyaltyResults doDiscount(PurchaseEntity originalPurchase) throws CashException {
        return doDiscount(originalPurchase, null);
    }

    @Override
    public synchronized LoyaltyResults doDiscount(PurchaseEntity originalPurchase, LoyaltyArguments arguments) throws CashException {
        LoyaltyResults result = new LoyaltyResults(originalPurchase);

        log.trace("entering doDiscount(PurchaseEntity)");

        try {
            long stopTime = System.currentTimeMillis();

            result = doLoyalty(originalPurchase, arguments);

            JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_CALCULATION_WITH_PREPARE, System.currentTimeMillis() - stopTime);

            if (result.getReceipt() != null) {
                // "округлятор"
                AdvertisingActionEntity[] actions = getAdvertisingActions(originalPurchase);

                // 2. сохраним TX лояльности в БД
                doSave(originalPurchase, result.getLoyalPurchase(), currencyHandler, actions);

                // SR-840: забытые подарки:
                processGifts(result);
            }
        } catch (LoyProcessingTryItAgainException e) {
            log.error("One of the calculation step throws Try again exception! Discount calculation will be canceled", e);
            throw e;
        } catch (Exception t) {
            log.error(String.format("Error discount calculation for purchase %s", originalPurchase), t);
        } finally {
            if (listeners != null) {
                for (LoyalEvent listener : listeners) {
                    listener.loyalStopProcessing();
                }
            }
        }

        log.trace("leaving doDiscount(PurchaseEntity)");

        return result;
    }

    private void processGifts(LoyaltyResults result) {
        Map<String, PositionDescription> giftsMap = new HashMap<>();
        for (PositionDescription pd : result.getLoyalPurchase().getForgottenGifts()) {
            if (pd == null || pd.getMarking() == null || pd.getQuantity() <= 0) {
                continue;
            }

            if (pd.getAdvActionGuid() != null) {
                // с guid'ом - это плагинные подарки и надо ли их мержить решает плагин
                result.getForgottenGifts().add(pd);
            } else {
                // одинаковые подарки, предлагаемые для формирования наборов, мержатся друг с другом (исторически сложившаяся логика)
                PositionDescription epd = giftsMap.get(pd.getMarking());
                if (epd == null) {
                    result.getForgottenGifts().add(pd);
                    giftsMap.put(pd.getMarking(), pd);
                } else {
                    epd.setQuantity(epd.getQuantity() + pd.getQuantity());
                }
            }
        }
    }

    private void putFilter(ILoyTechProcess loyTechProcess) {
        if (tp != null) {
            FilterPack filters = new FilterPack();

            putFilterByDepartments(filters);
            putFilterByProducts(filters);

            loyTechProcess.getProcessingSession().put(CalculateSession.FILTER, filters);
        }
    }

    private void putFilterByProducts(FilterPack filters) {
        filters.put(FilterPack.SET10_DISCOUNT, new FilerPositionByProduct());
    }

    private void putFilterByDepartments(FilterPack filters) {
        final Properties properties = tp.getProperties();
        if (properties != null) {
            //Если включен режим с двумя фискальниками
            if (properties.isSplitCheckByDepart()) {
                FilterPositionHolder filterPositionHolder = getFilterHolder(properties);
                filters.put(FilterPack.SOBS_DISCOUNT, filterPositionHolder);
            }
        }
    }

    private FilterPositionHolder getFilterHolder(Properties properties) {
        Set<Long> departForSecondDF = properties.getDepartForSecondFR();
        return new FilterPositionByDepartments(departForSecondDF);
    }

    /**
     * У бонусных карт указанного чека уменьшит потолки списаний бонусов как скидка - если надо.
     *
     * @param receipt чек. в котором определяли потолки списаний бонусов
     */
    private void adjustBonusDiscountCeilings(Purchase receipt) {
        log.trace("adjusting bonus discount ceilings of receipt: {}", receipt);
        if (receipt == null || MapUtils.isEmpty(receipt.getBonusDiscountCards())) {
            // в этом чеке нету бонусных карт
            return;
        }

        long sum = receipt.calcSum();
        log.trace("calculating max bonus discount. receipt sum is: {}", sum);

        for (BonusDiscountType bdt : receipt.getBonusDiscountCards().keySet()) {
            List<CardEntity> cards = receipt.getBonusDiscountCards().get(bdt);
            if (CollectionUtils.isEmpty(cards)) {
                continue;
            }
            for (CardEntity ce : cards) {
                if (ce == null || ce.getCardBonusBalance() == null) {
                    // у этой карты почему-то нету бонусного счета
                    continue;
                }
                ce.getCardBalancesReadonly().stream().filter(balance -> balance.getChargeOffAmount() == null).forEach(balance -> {
                    long currentAvailable = UnboxingUtils.valueOf(balance.getAvailableChargeOffBalance());
                    if (currentAvailable > sum) {
                        log.info("trimming down discount \"ceiling\" of balance [{}] from {} to {}",
                                balance, currentAvailable, sum);

                        balance.setAvailableChargeOffBalance(sum);
                    }
                });
            }
        }

        log.trace("adjusting bonus discount ceilings complete");
    }

    @Override
    public LoyTransactionEntity saveLoyTransaction(LoyTransactionEntity loyTransactionEntity) {
        return loyTxDao.saveLoyTx(loyTransactionEntity);
    }

    private boolean isValidatePurchaseDisabled() {
        String disabledFlag = propertiesManager.getProperty(Loyal.MODULE_NAME, null,
                DISABLED_PURCHASE_VALIDATOR_PROPERTY, "false");
        return "true".equalsIgnoreCase(disabledFlag);
    }

    public void doSave(PurchaseEntity originalPurchase, Purchase purchase, CurrencyHandler currencyHandler, AdvertisingActionEntity[] actions) throws Exception {
        if (purchase == null) {
            throw new Exception("Discounts calculator returns null");
        }
        long stopTime = System.currentTimeMillis();
        boolean validationDisabled = isValidatePurchaseDisabled();
        // Валидация чека лояльности
        JmxMetrics.runWithMetric(() -> {
            if (!validationDisabled && !LoyPurchaseValidator.validatePurchase(originalPurchase, purchase, currencyHandler, getLoyTechProcess(), actions)) {
                // псевдосохранение для отмены начислений бонусов
                LoyTransactionEntity loyTransaction = saveDiscountResults(purchase, actions);
                Long backedLoyTxId = originalPurchase.getLoyalTransactionId();
                originalPurchase.setLoyalTransactionId(loyTransaction.getId());
                // вот тут произойдёт отмена списания бонусов
                cancelDiscount(originalPurchase);
                deleteLoyTransaction(originalPurchase.getLoyalTransactionId());
                originalPurchase.setLoyalTransactionId(backedLoyTxId);

                PurchaseUtils.removeAllDiscounts(purchase);
                DiscountCalculationUtils.calcPositionsEndSums(purchase, currencyHandler);
                purchase.setCheckSum(purchase.calcOriginalSum());
                purchase.setDiscountValueTotal(purchase.calcDiscountValueTotal());

                MessageAdvertisingActionResultEntity message = new MessageAdvertisingActionResultEntity();
                message.setAdvertisingActionGUID(-1L);
                message.getOperatorMsg().add(CoreResBundle.getStringCommon("LOY_VALIDATION_ERROR"));
                purchase.getAdvertisingActionResults().add(message);
            }}, JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_VALIDATE");
        Long loyTxId = originalPurchase.getLoyalTransactionId();
        LoyTransactionEntity previousLoyTransaction = loyTxId != null && !EMPTY_LOYTRANSACTION_ID.equals(loyTxId) ?
                searchDiscountResultsInDB(loyTxId, false) : null;

        // удаление сохраненных результатов, если таковые имеются
        // вообще, есть подобное: аналог: updateLoyTransaction - вызывается при фискализации чека - тоже был костыль чтоб не терять CRM'овские описательные
        //  поля
        // вот здесь мы сейчас будем прибивать TX лояльности с ID = originalPurchase.getLoyalTransactionId()
        //  проблема в том, что к транзакциям лольности пока что (2013-09-19) могут быть прикреплены не только непосредственно скидки,
        //  но и некие _описательные_ поля: например, транзакции сторонних бонусных систем, выполненные в рамках этой TX лояльности (этого расчета скидок)
        //  - так вот: эти _описательные_ поля надо бы сохранить: здесь мы хотим просто очитить результаты расчета скидок (чтобы пересчитать их по-новой чуть позже)
        //  (реализовать через listener'ов события пред-удаления TX лояльности, что ли):
        LoyTransactionEntity backedUpLoyTxFields = JmxMetrics.runWithMetric(() -> extractNonDiscountRelatedFields(previousLoyTransaction),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_EXTRACT_NONDISCOUNT_RELATED_FIELDS");

        // само удаление пошло:
        JmxMetrics.runWithMetric(() -> deleteWRE(originalPurchase),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_DELETE_WRE");
        JmxMetrics.runWithMetric(() -> deleteLoyTransaction(loyTxId),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_DELETE_LOYTRANSACTION");
        JmxMetrics.runWithMetric(() -> deleteSplittedPurchaseTransactions(originalPurchase),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_DELETE_SPLITTED");
        JmxMetrics.runWithMetric(() -> deleteProductionDates(originalPurchase),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_DELETE_PRODUCTION_DATES");

        // применим ограничение на количество выдаваемых купонов
        JmxMetrics.runWithMetric(() -> checkCouponsCountAndDeleteExcess(purchase, getLoyaltyProperties().getMaxCountOfGeneratedCouponsInCheck(), actions),
            JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_CHECK_COUPONS_COUNT_AND_DELETE_EXCESS");

        // добавим сообщение, если достигли потолка скидок (SR-3028)
        JmxMetrics.runWithMetric(() -> checkMaxDiscountAndAddMessage(purchase, currencyHandler),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_CHECK_MAX_DISCOUNT_AND_ADD_MESSAGE");

        // спишем автоматом списанные бонусы если они есть
        JmxMetrics.runWithMetric(() -> autoDiscountWriteOff(originalPurchase, purchase, actions, previousLoyTransaction),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_AUTO_DISCOUNT_WRITE_OFF");

        // применим новое распределение позиций перед формированием транзакции лояльности для корректного сопоставления скидок и позиций
        JmxMetrics.runWithMetric(() -> LoyalCashConverter.applyPurchasePositions(originalPurchase, purchase, currencyHandler),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_APPLY_PURCHASE_POSITIONS");

        // сохранение вновь рассчитанных результатов
        LoyTransactionEntity loyTransaction = JmxMetrics.runWithMetric(() -> saveDiscountResults(purchase, actions),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_SAVE_DISCOUNT_RESULTS");

        if (loyTransaction != null) {
            // а вот тут вернем описательные поля TX лояльности обратно: потом придумать нормальное решение и переделать
            //  (например, не удалять TX лояльности вообще, а корректно его редактировать)
            if (backedUpLoyTxFields != null) {
                JmxMetrics.runWithMetric(() -> restoreBackupedTx(originalPurchase, loyTransaction, backedUpLoyTxFields),
                        JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_RESTORE_BACKUPED_TX");
            }
            originalPurchase.setLoyalTransactionId(loyTransaction.getId());
            // Проверим, была ли применена внутренняя карта
            Collection<InternalCardsEntity> internalCardsEntities = originalPurchase.getCardsWithType(InternalCardsEntity.class);
            // Если для карты определены оптовые ограничения
            if (CollectionUtils.isNotEmpty(internalCardsEntities) && internalCardsEntities.iterator().next().getWholesaleRestrictions() != null) {
                // обновим их локальные значения
                JmxMetrics.runWithMetric(() -> saveWholesaleRestrictionsFromTransaction(loyTransaction, actions),
                        JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_SAVE_WHOLESALE_RESTRICTIONS_FROM_TRANSACTION");
            }
        } else {
            originalPurchase.setLoyalTransactionId(EMPTY_LOYTRANSACTION_ID);
        }

        // заполним коллекцию применимых (в т.ч. не примененных) в чеке карт
        originalPurchase.getAppliableCardsInfo().clear();
        for (AppliedActionInfo actionInfo : purchase.getAppliableActionsInfo()) {
            originalPurchase.getAppliableCardsInfo().addAll(actionInfo.getAppliedCards());
        }

        originalPurchase.setDiscountPurchaseEntity(null);

        //Применим результаты расчёта
        JmxMetrics.runWithMetric(() -> applyPurchaseResult(originalPurchase, purchase, actions, loyTransaction),
                JmxMetrics.MetricsTypes.DISCOUNTS, "SAVE_CALCULATION_APPLY_PURCHASE_RESULT");

        JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.SAVE_CALCULATION, System.currentTimeMillis() - stopTime);
    }

    private void checkMaxDiscountAndAddMessage(Purchase purchase, CurrencyHandler currencyHandler) {
        long discountCeiling = FL54LoyUtils.getDiscountCeiling(purchase.getPositions(), currencyHandler,
                getLoyTechProcess(), purchase.getDateCreate(), 0);
        if (discountCeiling <= 0) {
            // отображаем, если есть многократные купоны
            boolean hasMultiCoupons = purchase.getCardsInfo().stream()
                    .anyMatch(card -> card.getCardType() instanceof CouponsEntity && ((CouponsEntity) card.getCardType()).isMultiUse());
            if (hasMultiCoupons) {
                MessageAdvertisingActionResultEntity message = new MessageAdvertisingActionResultEntity();
                message.setAdvertisingActionGUID(-1L);
                message.getOperatorMsg().add(CoreResBundle.getStringCommon("MAX_DISCOUNT_GIVEN"));
                purchase.getAdvertisingActionResults().add(message);
            }
        }
    }

    private void restoreBackupedTx(PurchaseEntity originalPurchase, LoyTransactionEntity loyTransaction, LoyTransactionEntity backedUpLoyTxFields) {
        boolean needUpdate = false;

        // будем возвращать только инфу по CFT бонусам (пока остальное и не требуется)
        if (!backedUpLoyTxFields.getBonusSberbankTransactions().isEmpty()) {
            log.trace("about to attach [{}] CFT-backed bonus discount infos back to the loy-tx [{}]",
                    backedUpLoyTxFields.getBonusSberbankTransactions().size(), loyTransaction);
            // прикрепим
            Optional.ofNullable(backedUpLoyTxFields.getBonusSberbankTransactions()).orElse(Collections.emptyList()).stream()
                    .filter(Objects::nonNull).forEach(lbst -> {
                        lbst.setTransaction(loyTransaction);
                        loyTransaction.getBonusSberbankTransactions().add(lbst);
                    }
            );

            needUpdate = true;
        } else {
            // бонусных операций CFT не было - хорошо: не надо ничего делать
            log.trace("no CFT-backed bonus discount operation was performed on receipt: {}", originalPurchase);
        }

        //Вернём информацию о марках
        if (!backedUpLoyTxFields.getTokensSiebelTransactions().isEmpty()) {
            log.trace("about to attach [{}] Siebel-backed tokens discout infos back to the loy-tx [{}]",
                    backedUpLoyTxFields.getTokensSiebelTransactions().size(), loyTransaction);
            // прикрепим
            Optional.ofNullable(backedUpLoyTxFields.getTokensSiebelTransactions()).orElse(Collections.emptyList()).stream()
                    .filter(Objects::nonNull).forEach(loyEntity -> {
                        loyEntity.setTransaction(loyTransaction);
                        loyTransaction.getTokensSiebelTransactions().add(loyEntity);
                    }
            );

            needUpdate = true;
        } else {
            // бонусных операций CFT не было - хорошо: не надо ничего делать
            log.trace("no Siebel-backed bonus discount operation was performed on receipt: {}", originalPurchase);
        }

        if (!backedUpLoyTxFields.getGiftNotes().isEmpty()) {
            log.trace("Attaching [{}] Gift notes [{}]", backedUpLoyTxFields.getGiftNotes().size(), loyTransaction);
            for (LoyGiftNoteEnity oldLoyGift : backedUpLoyTxFields.getGiftNotes()) {
                boolean found = false;
                if (oldLoyGift == null) {
                    continue;
                }
                for (LoyGiftNoteEnity loyGiftNote : loyTransaction.getGiftNotes()) {
                    if (loyGiftNote != null) {
                        if (loyGiftNote.getAdvAction() != null && oldLoyGift.getAdvAction() != null &&
                                Objects.equals(loyGiftNote.getAdvAction().getGuid(), oldLoyGift.getAdvAction().getGuid())) {
                            loyGiftNote.setScannedCount(oldLoyGift.getScannedCount());
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    loyTransaction.getGiftNotes().add(oldLoyGift);
                    oldLoyGift.setTransaction(loyTransaction);
                }
            }
            needUpdate = true;
        } else {
            log.trace("No gifts were attached to transaction {}", loyTransaction);
        }

        if (!backedUpLoyTxFields.getChequeAdverts().isEmpty()) {
            log.trace("about to attach [{}] cheque advertise infos back to the loy-tx [{}]",
                    backedUpLoyTxFields.getChequeAdverts().size(), loyTransaction);
            Optional.ofNullable(backedUpLoyTxFields.getChequeAdverts())
                    .orElse(Collections.emptyList()).stream()
                    .filter(Objects::nonNull)
                    .forEach(loyEntity -> {
                                loyEntity.setTransaction(loyTransaction);
                                loyTransaction.getChequeAdverts().add(loyEntity);
                            }
                    );
            needUpdate = true;
        } else {
            log.trace("no cheque advertise on receipt: {}", originalPurchase);
        }

        if (!backedUpLoyTxFields.getUpdateCounters().isEmpty()) {
            log.trace("about to attach [{}] update counters back to the loy-tx [{}]",
                    backedUpLoyTxFields.getUpdateCounters().size(), loyTransaction);
            for (LoyUpdateCounterEntity loyUpdateCounterEntity : backedUpLoyTxFields.getUpdateCounters()) {
                loyUpdateCounterEntity.setTransaction(loyTransaction);
                loyTransaction.getUpdateCounters().add(loyUpdateCounterEntity);
            }
            needUpdate = true;
        } else {
            log.trace("No update counters were attached to transaction {}", loyTransaction);
        }

        if (needUpdate) {
            // и сохраним в БД (описательная инфа сохранится каскадом):
            updateLoyTransaction(loyTransaction);
            log.trace("loy-tx [{}] was updated", loyTransaction);
        }
    }

    /**
     * Автосписание скидок - будет, если есть акция на автосписание бонусов сет10 (@see BonusDiscountApplyObject.autoWriteOff)
     * @param originalPurchase кассовый чек
     * @param purchase         чек лояльности
     * @param actions          акции
     * @param previousLoyTx    предыдущая ТХ лояльности
     */
    private void autoDiscountWriteOff(PurchaseEntity originalPurchase, Purchase purchase, AdvertisingActionEntity[] actions, LoyTransactionEntity previousLoyTx) {
        // берём из позиций те карты-скидки, которые были сформированы автоматически
        Set<Long> resultsBonusGuids = new HashSet<>();
        Map<String, Long> cardToDicountValue = purchase.getPositions().stream().filter(pos -> pos.getDiscountBatch() != null)
                .flatMap(pos -> pos.getDiscountBatch().getDiscounts().stream()).filter(DiscountEntity::isBonusDiscountAuto)
                .peek(discount -> resultsBonusGuids.add(discount.getAdvertisingActionGUID()))
                .collect(Collectors.toMap(DiscountEntity::getCardNumber, DiscountEntity::getValue, Long::sum));
        cardToDicountValue.forEach((cardNumber, discountValue) -> {
            // для каждой скидки вытянем аккаунт и сумму списания из кассового чека
            originalPurchase.getCards().stream().filter(it -> it.getCardType() != null
                    && it.getCardType().getCards().stream().anyMatch(card -> card.getNumber().equals(cardNumber) && card.getCardBonusBalance() != null))
                    .findFirst().ifPresent(purchaseCard -> {
                CardEntity card = purchaseCard.getCardType().getCards().get(0);
                // соберём автоскидки для транзакции лояльности, они могли подрезаться
                Optional<BonusAdvertisingActionResultEntity> loyBonusDiscountResult = purchase.getAdvertisingActionResults().stream()
                        .filter(it -> resultsBonusGuids.contains(it.getAdvertisingActionGUID()) && it instanceof BonusAdvertisingActionResultEntity)
                        .map(it -> (BonusAdvertisingActionResultEntity) it).filter(it -> it.getCardNumber().equals(cardNumber))
                        .findFirst();
                try {
                    CardBonusBalance cardBonusBalance = card.getCardBonusBalance();
                    if (cardBonusBalance.getSponsorId() == BonusDiscountType.SET_API) {
                        // возможно уже списывали автоматически, проверим
                        LoySetApiPluginTransactionEntity transaction = createSetApiTransaction(purchaseCard, cardBonusBalance, discountValue, null);
                        LoySetApiPluginTransactionEntity previousApiTx = previousLoyTx == null ? null : previousLoyTx.getSetApiLoyaltyTransactions().stream()
                                .filter(tx -> Objects.equals(transaction.getCardNumber(), tx.getCardNumber())
                                        && Objects.equals(transaction.getAdvertisingActionGuid(), tx.getAdvertisingActionGuid()))
                                .findFirst().orElse(null);
                        String txId = previousApiTx != null ? previousApiTx.getTransactionId() : setApiBonusesChargeOffService.writeOff(purchaseCard, cardBonusBalance, discountValue);
                        transaction.setTransactionId(txId);
                        purchase.getSetApiLoyaltyTransactions().add(transaction);
                    } else {
                        // ищем первый аккаунт с ненулевым балансом
                        Optional<CardBonusAccount> bonusAccount = card.getBonusAccounts().stream().filter(it -> it.getBalanceElementary() > 0).findFirst();
                        if (!bonusAccount.isPresent()) {
                            return;
                        }
                        // возможно уже списывали автоматически, проверим
                        LoyBonusTransactionEntity previousBonusTx = previousLoyTx == null ? null : previousLoyTx.getBonusTransactions().stream()
                                .filter(tx -> Objects.equals(cardNumber, tx.getDiscountCard())
                                && Objects.equals(loyBonusDiscountResult.map(AdvertisingActionResultEntity::getAdvertisingActionGUID).orElse(null),
                                tx.getActionGuid())).findFirst().orElse(null);
                        if (previousBonusTx == null) {
                            ShiftEntity shift = originalPurchase.getShift() != null ? originalPurchase.getShift() : tp.getShift();
                            internalCards.writeOffFromBonusAccount(cardNumber, bonusAccount.get().getBonusAccountTypeCode(),
                                    discountValue, originalPurchase, shift);
                        }
                        originalPurchase.getBonusDiscountCards().put(BonusDiscountType.BONUS_SR10, new LinkedList<>());
                        originalPurchase.getBonusDiscountCards().get(BonusDiscountType.BONUS_SR10).add(card);
                    }

                    if (UnboxingUtils.valueOf(cardBonusBalance.getChargeOffAmount()) == 0L) {
                        cardBonusBalance.setChargeOffAmount(discountValue);
                    }
                    cardBonusBalance.setNotAppliedBonuses(discountValue);
                    loyBonusDiscountResult.ifPresent(actionResult -> {
                        actionResult.setSumValue(-discountValue);
                        actionResult.setValueBonus(-DiscountCalculationUtils.getBonuses(discountValue, cardBonusBalance.getMultiplier()));
                    });
                } catch (CardsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    loyBonusDiscountResult.ifPresent(actionResult -> purchase.getAdvertisingActionResults().remove(actionResult));
                    long revertDiscountValue = purchase.getPositions().stream().filter(pos -> pos.getDiscountBatch() != null).mapToLong(pos -> {
                        List<DiscountPositionEntity> autoDiscounts = pos.getDiscountBatch().getDiscounts().stream()
                                .filter(DiscountEntity::isBonusDiscountAuto).collect(Collectors.toList());
                        long sum = autoDiscounts.stream().mapToLong(DiscountEntity::getValue).sum();
                        pos.getDiscountBatch().setDiscountValue(pos.getDiscountBatch().getDiscountValue() - sum);
                        pos.getDiscountBatch().getDiscounts().removeAll(autoDiscounts);
                        pos.setSum(pos.getSum() + sum);
                        return sum;
                    }).sum();
                    purchase.setDiscountValueTotal(purchase.getDiscountValueTotal() - revertDiscountValue);
                    purchase.setDiscountValue(purchase.getDiscountValue() - revertDiscountValue);
                    purchase.getBonusPositions().removeIf(it -> resultsBonusGuids.contains(it.getAdvertActGuid()));
                    new FL54Tuner().tune(getLoyTechProcess(), actions, purchase);
                }
            });
        });
    }

    private void applyPurchaseResult(PurchaseEntity originalPurchase, Purchase purchase, AdvertisingActionEntity[] actions, LoyTransactionEntity loyTransaction) {
        // Прислюнявим к нему инфу о сработавших РА от сторонних поставщиков лояльности
        getLoyProviders().attachAppliedDiscountInfo(originalPurchase, purchase);
        //Применим чек лояльности к кассовому чеку (BonusPosition'ы, карты, результаты рекламных акций)
        DiscountPurchaseConverter.applyPurchaseDiscount(originalPurchase, purchase, actions);
        //Применим транзакцию лояльности к кассовому чеку (Позиционные скидки и карты, по которым сработали акции (если карта сработала в 2-х РА, то две сущности будут
        // там)
        DiscountLoyTransactionConverter.applyPurchaseDiscount(originalPurchase, loyTransaction);
        // сохраним калькуляторный чек
        originalPurchase.setLoyPurchase(purchase);
    }

    private void saveWholesaleRestrictionsFromTransaction(LoyTransactionEntity loyTransaction, AdvertisingActionEntity[] actions) {
        Set<Long> wrActions = WholesaleRestrictionsExtractor.filterActionWithWholesaleRestriction(Lists.newArrayList(actions));
        // вытаскиваем ограничения из транзакции лояльности
        Collection<WholesaleRestrictionEntity> restrictions = WholesaleRestrictionsExtractor.extractWholesaleRestriction(loyTransaction, wrActions);
        // группируем ограничения по номерам карт
        Map<String, Collection<WholesaleRestriction>> map = new HashMap<>();
        for (WholesaleRestrictionEntity wre : restrictions) {
            if (!map.containsKey(wre.getCardNumber())) {
                map.put(wre.getCardNumber(), new ArrayList<>());
            }
            map.get(wre.getCardNumber()).add(new WholesaleRestriction(wre.getAdvertisingActionGuid(), wre.getProductsId(), wre.getQuantity()));
        }
        // обновляем локальные значения
        for (Map.Entry<String, Collection<WholesaleRestriction>> entry : map.entrySet()) {
            String cardNumber = entry == null ? null : entry.getKey();
            Collection<WholesaleRestriction> wrs = entry == null ? null : entry.getValue();
            if (CollectionUtils.isEmpty(wrs)) {
                continue;
            }
            for (WholesaleRestriction wr : wrs) {
                wrDao.addRestrictionsQuantity(cardNumber, wr.getActionGuid(), wr.getProductsId(), wr.getQuantity(), loyTransaction.getId());
            }
        }
    }

    /**
     * Просто извлекает из {@link LoyTransactionEntity TX лояльности} с указанным идентификатором чисто <em>описательные</em> поля. Под чисто
     * <em>описательными</em> полями понимаются поля, что не имеют непосредственного отношения количеству начисленных скидок, а просто содержат
     * описание, например, о произведенных операциях в сторонних системах лояльности в рамках данной транзакции лояльности. См. например: {@link
     * LoyTransactionEntity#getBonusSberbankTransactions() список операций, произведенных в CFT при списании/начислении бонусов СоСБ}, {@link
     * LoyTransactionEntity#getBonusDiscountTransactions() список операций, произведенных в рамках SAP CRM}.
     * <p/>
     * NOTE: в возвращенном объекте все поля, кроме описательных, будут пустыми.
     *
     * @param loyTx {@link LoyTransactionEntity} TX лояльности, чьи описательные поля надо извлечь и сохранить в результате
     * @return <code>null</code>, если TX лояльности с указанным ID не существует; иначе - не вернет <code>null</code> - хоть пустой объект (в крайнем
     * случае) да вернет
     */
    private LoyTransactionEntity extractNonDiscountRelatedFields(LoyTransactionEntity loyTx) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering extractNonDiscountRelatedFields(Long). The argument is: {}", loyTx);
        if (loyTx != null) {
            result = new LoyTransactionEntity();

            // CRM:
            result.getBonusDiscountTransactions().addAll(loyTx.getBonusDiscountTransactions());
            log.trace("[{}] CRM-related TXes were backed-up", loyTx.getBonusDiscountTransactions().size());
            // обнулим ID'шники:
            for (LoyBonusDiscountTransactionEntity lbdt : result.getBonusDiscountTransactions()) {
                lbdt.setId(null);
            }
            // CFT:
            result.getBonusSberbankTransactions().addAll(loyTx.getBonusSberbankTransactions());
            log.trace("[{}] CFT-related TXes were backed-up", loyTx.getBonusSberbankTransactions().size());
            // обнулим ID'шники:
            for (LoyBonusSberbankTransactionEntity lbst : result.getBonusSberbankTransactions()) {
                lbst.setId(null);
            }
            //Siebel Tokens
            result.getTokensSiebelTransactions().addAll(loyTx.getTokensSiebelTransactions());
            log.trace("[{}] Siebel-related TXes were backed-up", loyTx.getBonusSberbankTransactions().size());

            // Gifts
            result.getGiftNotes().addAll(loyTx.getGiftNotes());
            log.trace("[{}] Gift notes", result.getGiftNotes());

            // Update Counter
            result.getUpdateCounters().addAll(loyTx.getUpdateCounters());
            log.trace("[{}] getUpdateCounters", result.getUpdateCounters());

            // Advertise cheques
            result.getChequeAdverts().addAll(loyTx.getChequeAdverts());
            log.trace("[{}] Advertise cheques", result.getChequeAdverts());
        } else {
            log.warn("extractNonDiscountRelatedFields: the argument is NULL! Null will be returned!");
        }
        log.trace("leaving extractNonDiscountRelatedFields(Long). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public void cancelDiscount(PurchaseEntity purchase) {
        log.debug("entering cancelDiscount(PurchaseEntity). The argument is: {}", purchase);

        // Надо удалить транзакцию лояльности этого чека, но сначала:
        // 1. это что-то типа обработки PreDeleteEvent транзакции лояльности:

        // отмена списаний бонусов пошла:
        // CRM:
        if (getCrmCardsService() != null) {
            getCrmCardsService().cancelChargeOffBonuses(purchase);
        }
        // CFT и Plastek:
        getTechProcess().getExternalProcessings().discountCanceled(purchase);

        //Set Retail
        cancelChargeOffInternal(purchase);

        //отмена оптовых ограничений:
        deleteWRE(purchase);

        // удалим не актуальный отчет по позиционным купонам
        deletePositionCouponsReport(purchase);

        // оповестим сторонних поставщиков лояльности
        LoyProvidersRegistry lpr = getLoyProviders();
        if (lpr != null) {
            lpr.cancelDiscount(purchase);
        }

        deleteSplittedPurchaseTransactions(purchase);
        log.debug("leaving cancelDiscount(PurchaseEntity)");
    }

    @Override
    public void checkCanceled(PurchaseEntity purchase) {
        if (getLoyProviders() != null) {
            getLoyProviders().checkCanceled(purchase);
        }
    }

    private void deleteSplittedPurchaseTransactions(PurchaseEntity purchase) {
        if (purchase == null) {
            return;
        }
        for (PurchaseEntity purchaseEntity : purchase.getDividedPurchases()) {
            deleteLoyTransaction(purchaseEntity.getLoyalTransactionId());
        }
    }

    private void deleteProductionDates(PurchaseEntity originalPurchase) {
        if (originalPurchase == null) {
            return;
        }
        Map<PositionEntity, Set<PositionProductionDateEntity>> collect = originalPurchase.getPositions()
                .stream().collect(Collectors.toMap(Function.identity(), item -> new HashSet<>(item.getProductionDates())));
        // если есть даты продукта, то они придут новые, а текущие надо удалить
        checkService.clearProductionDatesInfo(originalPurchase);
        collect.forEach(PositionEntity::setProductionDates);
    }

    private void deleteWRE(PurchaseEntity purchase) {
        if (purchase != null && purchase.getLoyalTransactionId() != null) {
            int count = wrDao.removeRestrictionsByLoyTxId(purchase.getLoyalTransactionId());
            log.trace("WRE was delete by loyTxId = {}, count = {}", purchase.getLoyalTransactionId(), count);
        }
    }

    private void deletePositionCouponsReport(PurchaseEntity purchase) {
        if (purchase != null && purchase.getServiceDocs() != null) {
            purchase.getServiceDocs().removeIf(serviceDocument -> serviceDocument instanceof PositionCouponsReportDocument);
        }
    }

    private void cancelChargeOffInternal(final PurchaseEntity purchase) {
        // проверим есть ли в чеке информация о бонусной карте BONUS_SR10 или Informix
        if (purchase != null && purchase.getBonusDiscountCards() != null &&
                (!Collections.disjoint(purchase.getBonusDiscountCards().keySet(), bonusDiscountTypes)
                        || !Collections.disjoint(purchase.getBonusDiscountCards().keySet(), tokensDiscountTypes))) {

            LoyTransactionEntity loyTransaction = purchase.getLoyalTransactionId() == null ? null : loyTxDao.getLoyTxById(purchase.getLoyalTransactionId());

            if (loyTransaction != null && !loyTransaction.getBonusTransactions().isEmpty()) {
                loyTransaction.getBonusTransactions().stream()
                        .filter(bt -> bt.getSumAmount() < 0 && bt.getSponsorId() != null).forEach(bt -> {
                    if (bonusDiscountTypes.contains(bt.getSponsorId())) {
                        cancelChargeOffInternalBonuses(bt, purchase);
                    }
                    if (tokensDiscountTypes.contains(bt.getSponsorId())) {
                        cancelChargeOffInternalTokens(bt, purchase);
                    }
                });
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Error getting loyTransaction with id = " + purchase.getLoyalTransactionId());
                }
            }
        }
    }

    private void cancelChargeOffInternalBonuses(final LoyBonusTransactionEntity loyBonusTransactionEntity, final PurchaseEntity purchase) {
        //сделаем в другом потоке - в любом случае даже если нет связи с сервером, создастся отложенная операция
        executor.submit(() -> {
            try {
                // так как сохраняли мы с минусом - нужно добавить минус тут
                internalCards.stornoBonusAccount(
                        loyBonusTransactionEntity.getDiscountCard(),
                        loyBonusTransactionEntity.getBonusAccountType(),
                        loyBonusTransactionEntity.getAuthCode(),
                        -loyBonusTransactionEntity.getSumAmount(),
                        purchase,
                        tp.getShift());
            } catch (Exception e) {
                log.error("Error during bonuses chargeoff canceling: " + e.getMessage());
            }
        });
        //затрем инфу о списаниях
        purchase.getBonusDiscountCards().entrySet().stream()
                .filter(entry -> (bonusDiscountTypes.contains(entry.getKey())))
                .flatMap(entry -> entry.getValue().stream())
                .map(CardEntity::getCardBonusBalance)
                .filter(Objects::nonNull).forEach(cardBalance -> {
            if (!cardBalance.isSaveWriteOffAmount()) {
                cardBalance.clearChargeOffAmount();
                cardBalance.setNotAppliedBonuses(null);
                purchase.getBonusDiscountCards().remove(cardBalance.getSponsorId());
            }
        });
        //затрем инфу о этой бонусной карте
    }

    private void cancelChargeOffInternalTokens(final LoyBonusTransactionEntity loyBonusTransactionEntity, final PurchaseEntity purchase) {
        executor.submit(() -> {
            try {
                // так как сохраняли мы с минусом - нужно добавить минус тут
                internalCards.stornoTokensAccount(
                        loyBonusTransactionEntity.getDiscountCard(),
                        loyBonusTransactionEntity.getBonusAccountType(),
                        loyBonusTransactionEntity.getAuthCode(),
                        -loyBonusTransactionEntity.getBonusAmount(),
                        purchase,
                        tp.getShift());
            } catch (Exception e) {
                log.error("Error during bonuses chargeoff canceling: " + e.getMessage());
            }
        });
        //затрем инфу о списаниях
        purchase.getBonusDiscountCards().entrySet().stream()
                .filter(entry -> (tokensDiscountTypes.contains(entry.getKey())))
                .flatMap(entry -> entry.getValue().stream())
                .map(CardEntity::getCardTokensBalance)
                .filter(Objects::nonNull).forEach(cardBalance -> {
            cardBalance.clearChargeOffAmount();
            cardBalance.setNotAppliedBonuses(null);
            cardBalance.setTokenResult(null);
            purchase.getBonusDiscountCards().remove(cardBalance.getSponsorId());
        });
        //затрем инфу о этой бонусной карте
    }

    @Override
    public void cancelBonusesForReturn(PurchaseEntity purchase) {
        //выполняем метод только для чеков возврата
        if (purchase == null || purchase.isSale()) {
            return;
        }

        LoyTransactionEntity loyTransaction = loyTxDao.getLoyTxByReceipt(purchase);
        if (loyTransaction == null || CollectionUtils.isEmpty(loyTransaction.getBonusTransactions())) {
            log.info("No bonus transactions found for loyTransaction with id = " + purchase.getLoyalTransactionId());
            return;
        }

        for (final LoyBonusTransactionEntity loyBonusTransactionEntity : loyTransaction.getBonusTransactions()) {
            executor.submit(() -> {
                try {
                    if (BonusDiscountType.isTokenType(loyBonusTransactionEntity.getSponsorId())) {
                        internalCards.refundTokens(loyBonusTransactionEntity.getDiscountCard(),
                                loyBonusTransactionEntity.getBonusAccountType(), loyBonusTransactionEntity.getAuthCode(),
                                loyBonusTransactionEntity.getBonusAmount(), purchase, tp.getShift());
                    } else {
                        internalCards.refundBonuses(loyBonusTransactionEntity.getDiscountCard(),
                                loyBonusTransactionEntity.getBonusAccountType(), loyBonusTransactionEntity.getAuthCode(),
                                loyBonusTransactionEntity.getBonusAmount(), loyBonusTransactionEntity.getSumAmount(), purchase, tp.getShift());
                    }
                } catch (Exception e) {
                    log.error("Error during bonuses chargeoff canceling: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void commitCanceledLoyTransaction(PurchaseEntity purchase) {
        LoyTransactionEntity loyTx = null;

        if (purchase != null && purchase.getLoyalTransactionId() != null &&
                !EMPTY_LOYTRANSACTION_ID.equals(purchase.getLoyalTransactionId())) {
            loyTx = searchDiscountResultsInDB(purchase.getLoyalTransactionId(), false);
        }
        if (loyTx != null) {
            loyTx.setStatus(LoyTransactionEntity.STATUS_CANCELED);
            loyTxDao.linkTxToReceipt(loyTx, purchase);
        }
    }

    @Override
    public void confirmDiscount(PurchaseEntity purchase) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering confirmDiscount(PurchaseEntity). The argument is: purchase [{}]", purchase);

        LoyTransactionEntity loyTransaction = getDiscountResults(purchase, false);
        if (loyTransaction != null) {
            loyTransaction = loyTxDao.linkTxToReceipt(loyTransaction, purchase);
            List<LoyBonusTransactionEntity> bonusTransactions = loyTransaction.getBonusTransactions();
            bonusTransactions.stream().filter(b -> b.getBonusAmount() > 0).forEach(bonus ->
                    cardsEvent.eventChargeBonuses(bonus.getDiscountCard(), bonus.getBonusAmount()));
        } else {
            if (purchase != null && purchase.getLoyalTransactionId() != null && purchase.getLoyalTransactionId() != -1) {
                log.error("Can't commit transaction for purchase with id = " + purchase.getId() +
                        ", transaction with id = " + purchase.getLoyalTransactionId() + " not found.");
            }
        }
        if (getLoyProviders() != null) {
            getLoyProviders().confirmDiscount(purchase, loyTransaction, FeedbackTime.AFTER_FISCALIZE);
        }
        log.trace("leaving confirmDiscount(PurchaseEntity). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void beforeFiscalize(PurchaseEntity purchase) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering beforeFiscalize(PurchaseEntity). The argument is: purchase [{}]", purchase);

        getLoyProviders().beforeFiscalize(purchase);

        log.trace("leaving beforeFiscalize(PurchaseEntity). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void preparePurchaseFiscalization(PurchaseEntity purchase, Check check) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering preparePurchaseFiscalization(PurchaseEntity). The argument is: purchase [{}]", purchase);

        processBonusResultActions(purchase);

        LoyTransactionEntity loyTransactionEntity = getLoyTransactionEntityIfRequired(purchase, TechProcessStage.PREPARE_PURCHASE_FISCALIZATION);

        getLoyProviders().preparePurchaseFiscalization(purchase, check, loyTransactionEntity);

        if (loyTransactionEntity != null) {
            updateLoyTransaction(loyTransactionEntity);
        }

        log.trace("leaving preparePurchaseFiscalization(PurchaseEntity). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void purchaseFiscalized(PurchaseEntity purchase) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering purchaseFiscalized(PurchaseEntity). The argument is: purchase [{}]", purchase);

        LoyTransactionEntity loyTransactionEntity = getLoyTransactionEntityIfRequired(purchase, TechProcessStage.PURCHASE_FISCALIZED);

        getLoyProviders().purchaseFiscalized(purchase, loyTransactionEntity);
        updateAdvActions(purchase, loyTransactionEntity);

        log.trace("leaving purchaseFiscalized(PurchaseEntity). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private LoyTransactionEntity getLoyTransactionEntityIfRequired(PurchaseEntity purchase, TechProcessStage stage) {
        LoyTransactionEntity loyTransactionEntity = null;
        if (getLoyProviders().isLoyTransactionRequiredForStage(stage)) {
            loyTransactionEntity = findLoyTransaction(purchase);
            if (purchase.isReturn() && loyTransactionEntity != null) {
                loyTransactionEntity.setSuperTransaction(findLoyTransaction(purchase.getSuperPurchase()));
            }
            if (purchase.getMainPurchase() != null && loyTransactionEntity != null) {
                loyTransactionEntity.setMainTransaction(findLoyTransaction(purchase.getMainPurchase()));
            }
        }
        return loyTransactionEntity;
    }

    @Override
    public void updateAdvActions(PurchaseEntity purchase, LoyTransactionEntity loyTransactionEntity) {
        if (purchase == null || loyTransactionEntity == null) {
            return;
        }
        if (purchase.isSale()) {
            // актуализация сработавших акции необходима только для чека продажи
            AdvertisingActionEntity[] actions = getAdvertisingActions(purchase);
            CashAdvResultPersistenceManager cashAdvResultPersistenceManager = new CashAdvResultPersistenceManager(getLoyTxDao(), actions);
            cashAdvResultPersistenceManager.updateAdvActions(loyTransactionEntity);
        }
        saveLoyTransaction(loyTransactionEntity);
    }

    @Override
    public ExtendedCardType getCouponType(CardData couponData) {
        return getLoyProviders().getCouponType(couponData, getLoyTechProcess());
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public List<LoyalEvent> getListeners() {
        return listeners;
    }

    public void setListeners(List<LoyalEvent> listeners) {
        this.listeners = listeners;
    }

    public TechProcessServiceAsync getTechProcess() {
        return techProcess;
    }

    public void setTechProcess(TechProcessServiceAsync techProcess) {
        this.techProcess = techProcess;
    }

    public TransferManager getTransferManager() {
        return transferManager;
    }

    public void setTransferManager(TransferManager transferManager) {
        this.transferManager = transferManager;
    }

    public CheckService getCheckService() {
        return checkService;
    }

    public void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }

    @Override
    public LoyTransactionEntity getLoyTransaction(Long transactionId) {
        return searchDiscountResultsInDB(transactionId, true);
    }

    @Override
    public LoyTransactionEntity findLoyTransaction(PurchaseEntity purchase) {
        return loyTxDao.getLoyTxByReceipt(purchase);
    }

    @Override
    public ObjectForSendFeedback findLoyTransactionForSend(PurchaseEntity purchaseEntity) {
        LoyTransactionEntity loyTransaction = loyTxDao.getLoyTxByReceipt(purchaseEntity);
        return new ObjectForSendFeedback() {
            @Override
            public boolean isReadyToSend() {
                return loyTransaction == null || isLoyTransactionComplete(loyTransaction);
            }

            @Override
            public Serializable getPayload() {
                return loyTransaction;
            }

            @Override
            public void afterSendSuccess() {
                if (loyTransaction != null) {
                    loyTxDao.setTxStatus(Collections.singletonList(loyTransaction.getId()), null, SentToServerStatus.SENT);
                }
            }
        };
    }

    @Override
    public void deleteSberbankTransactions(List<Long> ids) {
        loyTxDao.deleteSberbankTransactions(ids);
    }

    @Override
    public LoyTransactionEntity save(List<LoyBonusSberbankTransactionEntity> bonusTxes) {
        return loyTxDao.saveSosbs(bonusTxes);
    }

    @Override
    public LoyTransactionEntity updateLoyTransaction(LoyTransactionEntity loyTransaction) {
        LoyTransactionEntity result;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering updateLoyTransaction(LoyTransactionEntity). The argument is: loyTransaction [{}]", loyTransaction);
        result = loyTxDao.saveLoyTx(loyTransaction);
        log.trace("leaving updateLoyTransaction(LoyTransactionEntity). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public boolean deleteLoyTransaction(Long transactionId) {
        boolean result = false;

        log.trace("entering deleteLoyTransaction(Long). The argument is: {}", transactionId);
        if (transactionId != null && !EMPTY_LOYTRANSACTION_ID.equals(transactionId)) {
            try {
                loyTxDao.removeLoyTxByIds(Collections.singleton(transactionId));
                cachedLoyTransaction = null;
                // если не вылетели по Exception'у - значит. все ок
                result = true;
            } catch (Exception t) {
                log.error("deleteLoyTransaction(Long) failed!", t);
                result = false;
            }
        }
        log.trace("leaving deleteLoyTransaction(Long). The result is: {}", result);

        return result;
    }

    @Override
    public boolean isLoyTransactionComplete(LoyTransactionEntity loyTransaction) {
        long stopWatch = System.currentTimeMillis();
        log.trace("entering isLoyTransactionComplete(LoyTransactionEntity). The argument is: [{}]", loyTransaction);

        if (loyTransaction != null) {
            Long purchaseId = checkService.getPurchaseIdByLoyTransactionId(loyTransaction.getId());
            loyTransaction.setPurchaseId(purchaseId);
        }
        boolean result = getLoyProviders().isLoyTransactionComplete(loyTransaction);

        log.trace("leaving isLoyTransactionComplete(LoyTransactionEntity). The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    public CRMCardsService getCrmCardsService() {
        return crmCardsService;
    }

    public void setCrmCardsService(CRMCardsService crmCardsService) {
        this.crmCardsService = crmCardsService;
    }

    @Override
    public Map<BonusDiscountType, AdvertisingActionEntity> getBonusDiscountActions() {
        Map<BonusDiscountType, AdvertisingActionEntity> result;

        log.trace("entering getBonusDiscountActions()");
        List<AdvertisingActionEntity> activeActions = this.cache.getActiveActions(new Date(), null);
        log.trace("[{}] active actions were detected... about to peek those of bonus-discount type...",
                activeActions == null ? "(NULL)" : activeActions.size());
        if (activeActions != null && !activeActions.isEmpty()) {
            result = AdvertisingActionUtils.getBonusDiscountActions(activeActions, null, null);
        } else {
            log.trace("no one active action was detected. so an empty collection (of bonus-discount actions) " +
                    "will be returned!");
            result = Collections.emptyMap();
        }

        log.trace("leaving getBonusDiscountActions(). the result is: {}", result);

        return result;
    }

    private void processBonusResultActions(PurchaseEntity purchase) {
        if (verifyCard(purchase)) {
            log.trace("entering processBonusResultActions()");
            List<AdvertisingActionEntity> activeActions = this.cache.getActiveActions(new Date(), getManualAdvertisingActionsGuids(purchase));
            log.trace("[{}] active actions were detected... about to peek those of bonus-result type...", activeActions.size());
            List<AdvertisingActionEntity> bonusActions = AdvertisingActionUtils.getBonusResultActions(activeActions);
            List<AdvertisingActionEntity> withOnlyBonusActions = AdvertisingActionUtils.filterActionResultForActions(bonusActions,
                    plugin -> !(plugin instanceof IBonusActionResultPlugin) || BonusDiscountType.isTokenType(((IBonusActionResultPlugin) plugin).getSponsorId()));

            if (!(withOnlyBonusActions).isEmpty()) {
                try {
                    doPartDiscount(purchase, AdvertisingActionUtils.PREDICATE_BONUS_ACTION_RESULT, withOnlyBonusActions.toArray(new AdvertisingActionEntity[0]));
                    checkService.updatePurchaseEntity(purchase);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            } else {
                log.trace("no one active action was detected. so an empty collection (of bonus-result actions) will be returned!");
            }

            log.trace("leaving processBonusResultActions().");
        }
    }

    private boolean verifyCard(PurchaseEntity purchase) {
        // пока только для Ко-Бренда
        return coBrandService != null && coBrandService.isCoBrandConfigured() && purchase.isSale() && purchase.hasCard(CoBrandService.isCoBrandCard);
    }

    @Override
    public Map<BonusDiscountType, List<CardEntity>> getBonusDiscountCards(PurchaseEntity receipt) {
        Map<BonusDiscountType, List<CardEntity>> result = new HashMap<>();

        log.trace("entering getBonusDiscountCards(PurchaseEntity)");
        if (receipt != null && receipt.getLoyalTransactionId() != null) {
            // 1. возьмем эту TX лояльности:
            LoyTransactionEntity loyTx = getLoyTransaction(receipt.getLoyalTransactionId());
            if (loyTx == null) {
                // вот это не хорошо: у чека есть ID TX лояльности. но по этой ID нет записи!
                log.warn("The receipt [{}] point to non-existent loy-tx (by id: {})",
                        receipt, receipt.getLoyalTransactionId());
                return result;
            }
            // TX лояльности найдена - начнем:

            // CFT: СоСБ
            if (!loyTx.getBonusSberbankTransactions().isEmpty()) {
                // были в этом чеке какие-то операции через процессинг ЦФТ
                log.trace("[{}] CFT-backed loy-TXes are \"attached\" to the receipt [{}]", loyTx.getBonusSberbankTransactions().size(), receipt);
                List<CardEntity> sosbs = getSosbBonusDiscountCards(loyTx);
                // добавим эти карты в результат
                if (CollectionUtils.isNotEmpty(sosbs)) {
                    result.put(BonusDiscountType.CFT, sosbs);
                }
            }
            // CFT: ЦФТ
            if (!loyTx.getBonusSberbankTransactions().isEmpty()) {
                // были в этом чеке какие-то операции через процессинг ЦФТ
                log.trace("[{}] CFT-backed loy-TXes are \"attached\" to the receipt [{}]", loyTx.getBonusSberbankTransactions().size(), receipt);
                List<CardEntity> cft = getCftBonusDiscountCards(loyTx);
                // добавим эти карты в результат
                if (CollectionUtils.isNotEmpty(cft)) {
                    result.put(BonusDiscountType.BONUS_CFT, cft);
                }
            }

            //PT
            if (!loyTx.getBonusPlastekTransactions().isEmpty()) {
                // были в этом чеке какие-то операции по PlasTek
                log.trace("[{}] PlasTek-backed loy-TXes are \"attached\" to the receipt [{}]", loyTx.getBonusPlastekTransactions().size(), receipt);
                List<CardEntity> pt = getPTBonusDiscountCards(loyTx);
                // добавим эти карты в результат
                if (CollectionUtils.isNotEmpty(pt)) {
                    result.put(BonusDiscountType.PT, pt);
                }

            }

            //SetRetail10 - сделаем так же и для наших бонусов
            if (!loyTx.getBonusTransactions().isEmpty()) {
                List<CardEntity> sr10 = getInternalBonusDiscountCards(loyTx);
                // добавим эти карты в результат
                if (CollectionUtils.isNotEmpty(sr10)) {
                    for (CardEntity cardEntity : sr10) {
                        result.computeIfAbsent(cardEntity.getCardBonusBalance().getSponsorId(), k -> new ArrayList<>()).add(cardEntity);
                    }
                }
            }
        } else {
            // похоже, у этого чека еще не подсчитаны скидки. Хорошо: бонусные карты мы пока извлекаем из TX лояльности - значит вернем пусто:
            log.trace("either the argument is null, or its tx-id field is NULL -> so, an empty collection will be returned");
        }
        log.trace("leaving getBonusDiscountCards(PurchaseEntity). the result is: {}", result);

        return result;
    }

    @Override
    public LoyTransactionEntity discardDiscountsAndSave(LoyTransactionEntity loyTx, Collection<LoyDiscountPositionEntity> positionDiscounts,
                                                        Collection<LoyDiscountPositionEntity> correctDiscounts) {
        return loyTxDao.discardDiscountsAndSave(loyTx, positionDiscounts, correctDiscounts);
    }

    /**
     * Тащит карты SR10 и INformix из транзакции лояльности
     *
     * @param loyTx транзакция лояльности
     * @return список бонусных карт
     */
    private List<CardEntity> getInternalBonusDiscountCards(LoyTransactionEntity loyTx) {
        List<CardEntity> result = new LinkedList<>();
        for (LoyBonusTransactionEntity loyBonusTransactionEntity : loyTx.getBonusTransactions()) {
            //если было списание - то значение отрицательное
            if (loyBonusTransactionEntity.getBonusAmount() < 0) {

                // нашли списание:
                log.trace("write-off tx was detected: {}", loyBonusTransactionEntity);

                // начнем заполнять результат:
                // карта:
                CardEntity ce = new CardEntity();
                // номер карты
                ce.setNumber(loyBonusTransactionEntity.getDiscountCard());

                // счет с которого списано было
                CardBonusAccount cba = new CardBonusAccount();
                cba.setBonusAccountTypeCode(loyBonusTransactionEntity.getBonusAccountType());

                // балланс:
                CardBonusBalance cbb = new CardBonusBalance();
                cbb.setSponsorId(loyBonusTransactionEntity.getSponsorId());
                cbb.setChargeOffAmount(loyBonusTransactionEntity.getSumAmount());
                cbb.setNotAppliedBonuses(cbb.getChargeOffAmount());
                cbb.setAvailableChargeOffBalance(cbb.getChargeOffAmount());
                cbb.setCurrentBonusAccount(cba);
                cbb.setAutCode(loyBonusTransactionEntity.getAuthCode());
                ce.setCardBonusBalance(cbb);

                // добавим эту карту в результат
                result.add(ce);

                // и выйдем сразу же: списание может быть только одно:
                break;
            }
        }
        return result;
    }

    private List<CardEntity> getPTBonusDiscountCards(LoyTransactionEntity loyTx) {
        List<CardEntity> result = new LinkedList<>();
        // из этих TX нас интересует только списание. Оно может быть максимум одно:
        for (LoyBonusPlastekTransactionEntity lbst : loyTx.getBonusPlastekTransactions()) {
            if (lbst.getBnsChange().compareTo(0L) > 0) {
                // нашли списание:
                log.trace("write-off tx was detected: {}", lbst);

                // начнем заполнять результат:
                // карта:
                CardEntity ce = new CardEntity();
                // номер карты
                ce.setNumber(lbst.getCardNumber());

                // баланс:
                CardBonusBalance cbb = new CardBonusBalance();
                cbb.setSponsorId(BonusDiscountType.PT);
                cbb.setChargeOffAmount(lbst.getBnsChange());
                cbb.setNotAppliedBonuses(cbb.getChargeOffAmount());
                cbb.setAvailableChargeOffBalance(cbb.getChargeOffAmount());

                ce.setCardBonusBalance(cbb);

                // добавим эту карту в результат
                result.add(ce);

                // и выйдем сразу же: списание может быть только одно:
                break;
            }
        }
        return result;
    }

    /**
     * Из указанной транзакции вернет записи о списаниях бонусов указанного пользователя процессинга ЦФТ.
     *
     * @param loyTx транзакция
     * @param user  пользователь; если {@code null}, то вернет списания всех пользоателей процессинга ЦФТ
     * @return не {@code null}
     */
    private Collection<LoyBonusSberbankTransactionEntity> getCftWriteOffsByUser(LoyTransactionEntity loyTx, CFTProcessingUser user) {
        Collection<LoyBonusSberbankTransactionEntity> result = new LinkedList<>();

        if (loyTx == null) {
            return result;
        }

        for (LoyBonusSberbankTransactionEntity lbst : loyTx.getBonusSberbankTransactions()) {
            if (!LoyBonusSberbankTransactionEntity.Type.WRITE_OFF.equals(lbst.getTransactionType())) {
                continue;
            }
            // это СПИСАНИЕ бонусов через процессинг ЦФТ
            if (user == null || user.equals(lbst.getUser())) {
                result.add(lbst);
            }
        }

        return result;
    }

    /**
     * Для указанного пользователя процессинга ЦФТ вернет тип скидки. что дается по бонусам от этого пользователя.
     *
     * @param user пользователь процессинга ЦФТ
     * @return не {@code null} - в крайнем случае вернет {@link BonusDiscountType#CFT СоСБ} (просто потому, что он был введен в сисетму самым первым)
     */
    private BonusDiscountType getBonusDiscountType(CFTProcessingUser user) {
        BonusDiscountType result;

        if (user == null) {
            return BonusDiscountType.CFT;
        }
        if (user == CFTProcessingUser.CFT) {
            result = BonusDiscountType.BONUS_CFT;
        } else {
            result = BonusDiscountType.CFT;
        }

        return result;
    }

    /**
     * Вернет описание карты, с которой были списаны бонусы в указанной транзакции списания бонусов через процессинг ЦФТ.
     *
     * @param cftWriteOff списание бонусов через процессинг ЦФТ
     * @return {@code null}, если аргумент невалиден
     */
    private CardEntity createCard(LoyBonusSberbankTransactionEntity cftWriteOff) {
        CardEntity result;

        if (cftWriteOff == null) {
            return null;
        }

        result = new CardEntity();
        // номер карты
        result.setNumber(cftWriteOff.getPan4());
        // хэш номера
        result.setCardNumberHash(cftWriteOff.getClientId());

        // балланс:
        CardBonusBalance cbb = new CardBonusBalance();
        cbb.setSponsorId(getBonusDiscountType(cftWriteOff.getUser()));
        cbb.setChargeOffAmount(-cftWriteOff.getBnsChange());
        cbb.setNotAppliedBonuses(cbb.getChargeOffAmount());
        cbb.setAvailableChargeOffBalance(cbb.getChargeOffAmount());

        result.setCardBonusBalance(cbb);

        return result;
    }

    /**
     * Вернет описания карт. с которых были списаны бонусы СоСБ в указанной транзакции.
     *
     * @param loyTx транзакция
     * @return не {@code null}
     */
    private List<CardEntity> getSosbBonusDiscountCards(LoyTransactionEntity loyTx) {
        List<CardEntity> result = new LinkedList<>();
        // из этих TX нас интересует только списание. Оно может быть максимум одно:
        for (LoyBonusSberbankTransactionEntity lbst : getCftWriteOffsByUser(loyTx, CFTProcessingUser.SOSB)) {
            // нашли списание:
            log.trace("write-off tx was detected: {}", lbst);

            CardEntity ce = createCard(lbst);
            if (ce == null) {
                // невалидная транзакция, видимо
                log.error("invalid cft bonuses write-off tx was detected: {}", lbst);
                continue;
            }

            // добавим эту карту в результат
            result.add(ce);

            // и выйдем сразу же: списание может быть только одно:
            break;
        }
        return result;
    }

    /**
     * Вернет описания карт. с которых были списаны бонусы ЦФТ в указанной транзакции.
     *
     * @param loyTx транзакция
     * @return не {@code null}
     */
    private List<CardEntity> getCftBonusDiscountCards(LoyTransactionEntity loyTx) {
        List<CardEntity> result = new LinkedList<>();
        // из этих TX нас интересует только списание. Оно может быть максимум одно:
        for (LoyBonusSberbankTransactionEntity lbst : getCftWriteOffsByUser(loyTx, CFTProcessingUser.CFT)) {
            // нашли списание:
            log.trace("write-off tx was detected: {}", lbst);

            CardEntity ce = createCard(lbst);
            if (ce == null) {
                // невалидная транзакция, видимо
                log.error("invalid cft bonuses write-off tx was detected: {}", lbst);
                continue;
            }

            // добавим эту карту в результат
            result.add(ce);

            // и выйдем сразу же: списание может быть только одно:
            break;
        }
        return result;
    }

    @Override
    public List<PurchaseCardsEntity> getCardsSuitableForWriteOff(PurchaseEntity purchase, BonusDiscountType bonusType) {
        if (purchase == null || !purchase.isSale() || !purchase.isNoPayments()) {
            return new ArrayList<>();
        }

        LinkedList<PurchaseCardsEntity> result = new LinkedList<>();
        Map<String, PurchaseCardsEntity> apiCards = new HashMap<>();

        boolean noWriteOffByPhone = getLoyaltyProperties().isNoWriteOffByPhone();

        for (PurchaseCardsEntity card : purchase.getCards()) {
            Collection<CardBonusBalance> balances = bonusType == null ? CheckUtils.getBonusBalances(card) :
                    CheckUtils.getBalancesByType(card, bonusType);
            if (balances.isEmpty() || (card.isAddedByPhoneNumber() && noWriteOffByPhone)) {
                continue;
            }

            // Карты неплагинных поставщиков добавляем в конец списка, т.к. расчет потолка и распределение списанных бонусов как скидки
            // для них выполняется суммирующимися акциями Set10.
            if (balances.stream().anyMatch(balance -> balance.getSponsorId() == BonusDiscountType.SET_API)) {
                balances.stream()
                        .filter(balance -> UnboxingUtils.valueOf(balance.getAvailableChargeOffBalance()) > 0
                                || UnboxingUtils.valueOf(balance.getChargeOffAmount()) > 0)
                        .forEach(balance -> apiCards.putIfAbsent(balance.getStrSponsorId(), card));
            } else if (balances.stream().allMatch(balance -> UnboxingUtils.valueOf(balance.getChargeOffAmount()) > 0)) {
                // На данный момент не реализована возможность списания в одном чеке с нескольких неплагинных карт.
                // Поэтому если найден неплагинный поставщик, по которому уже выполнялось списание, считаем что списать с какой-либо карты уже нельзя.
                return new ArrayList<>();
            } else if (bonusType == null || balances.stream().anyMatch(balance -> bonusType == balance.getSponsorId())) {
                result.add(card);
            }
        }

        if (bonusType != null && bonusType != BonusDiscountType.SET_API) {
            return result;
        }

        // плагинные карты сортируются в порядке следования акций соответствующих поставщиков (порядке срабатывания плагинов)
        Map<String, AdvertisingActionEntity> apiActions = AdvertisingActionUtils
                .getActionsByProviders(cache.getActiveActions(new Date(), null), apiCards.keySet());
        List<PurchaseCardsEntity> sortedApiCards = apiActions.keySet().stream()
                .map(apiCards::get).filter(Objects::nonNull).collect(LinkedList::new, LinkedList::addFirst, LinkedList::addAll);
        for (PurchaseCardsEntity card : sortedApiCards) {
            Collection<CardBonusBalance> balances = CheckUtils.getBonusBalances(card);
            if (balances.stream().allMatch(balance -> UnboxingUtils.valueOf(balance.getChargeOffAmount()) > 0
                    || UnboxingUtils.valueOf(balance.getAvailableChargeOffBalance()) == 0)) {
                // карты плагинов, срабатывающих раньше тех, с которых уже было произведено списание, считаются недоступными к списанию,
                // т.к. для них невозможен корректный расчет потолка списания
                break;
            }
            result.addFirst(card);
        }
        return result;
    }

    @Override
    public LoyReports getLoyReports(PurchaseEntity receipt) {
        LoyReports result = new LoyReports();

        // 1. Найдем TX лояльности для указанного чека
        LoyTransactionEntity loyTransactionEntity = getLoyTransactionEntity(receipt);

        // 1.1. GUID'ы РА, что требуют применения внутренней карты для своего срабатывания
        Set<Long> cardActions = getCardDependentActionGuids(receipt);

        // 2. а теперь будем извлекать из нее информацию, необходимую модулю фискализации чека
        // 2.1. скидки:
        DiscountReportsCreator creator = new DiscountReportsCreator(receipt, loyTransactionEntity, tp, properties.getSectionNumbers(),
                cardActions, catalogService);
        List<AdvertisingActionEntity> activeActions = this.cache.getActiveActions(new Date(), getManualAdvertisingActionsGuids(receipt));
        creator.setActionsCache(Optional.ofNullable(activeActions).orElse(Collections.emptyList())
                .stream().collect(Collectors.toMap(AdvertisingActionEntity::getGuid, action -> action)));
        result.setDiscounts(creator.createDiscountsReport());

        // 2.2. начисления/списания бонусов
        result.setBonusesOperations(extractBonusesReports(loyTransactionEntity));

        // 2.3. начисления бонусов на позиции
        result.setBonusesPositions(extractBonusPositionsReports(loyTransactionEntity));

        return result;
    }

    /**
     * Вернет GUID'ы, что требуют применения внутренней карты для своего срабатывания
     *
     * @param receipt чек, на который могут сработать РА, GUID'ы которых надо вернуть
     * @return не {@code null}
     */
    private Set<Long> getCardDependentActionGuids(PurchaseEntity receipt) {
        Set<Long> result = new HashSet<>();

        AdvertisingActionEntity[] actions = getAdvertisingActions(receipt);
        ActionSelectCondition condition = ActionSelectCondition.makeHasInternalCardsConditionActions();
        List<AdvertisingActionEntity> selected = AdvertisingActionUtils.selectActions(actions, condition);
        for (AdvertisingActionEntity aae : selected) {
            if (aae != null && aae.getGuid() != null) {
                result.add(aae.getGuid());
            }
        }

        return result;
    }

    /**
     * Извлечет из указанной TX лояльности информацию об операциях списания/начисления бонусов.
     *
     * @param loyTx TX лояльности, из которой будем извлекать данные
     * @return никогда не вернет <code>null</code> - в крайнем случае вернет пустую коллекцию
     */
    private Collection<BonusesReport> extractBonusesReports(LoyTransactionEntity loyTx) {
        Collection<BonusesReport> result = new LinkedList<>();
        if (loyTx != null) {
            // NOTE: пока (2014-10-22) надо возвращать только наши BONUS_SR10 бонусы
            for (LoyBonusTransactionEntity bte : loyTx.getBonusTransactions()) {
                if (bte != null && bte.getDiscountCard() != null) {
                    BonusesReport br = new BonusesReport();

                    br.setCardNumber(bte.getDiscountCard());
                    br.setBonusType(bte.getSponsorId());
                    br.setActivationDate(bte.getBonusPeriodStart());
                    br.setBestBeforeDate(bte.getBonusPeriodFinish());
                    br.setBonusAccountTypeId(bte.getBonusAccountType());
                    br.setBonusAccountName(bte.getBonusAccountName());
                    br.setBonusesAccrued(bte.getBonusAmount());

                    result.add(br);
                }
            }
        }
        return result;
    }

    /**
     * Извлечет из указанной TX лояльности информацию о начислениях бонусов на позиции.
     *
     * @param loyTx TX лояльности, из которой будем извлекать данные
     * @return никогда не вернет <code>null</code> - в крайнем случае вернет пустую коллекцию
     */
    private Collection<BonusPositionReport> extractBonusPositionsReports(LoyTransactionEntity loyTx) {
        Collection<BonusPositionReport> result = new LinkedList<>();
        if (loyTx == null) {
            return result;
        }
        for (LoyBonusPositionEntity bpe : loyTx.getBonusPositions()) {
            if (bpe != null) {
                BonusPositionReport bpr = new BonusPositionReport();

                bpr.setBonusAmount(bpe.getBonusAmount());
                bpr.setGoodCode(bpe.getGoodCode());
                bpr.setPositionOrder(bpe.getPositionOrder());

                result.add(bpr);
            }
        }
        return result;
    }

    // getters and setters

    public AdvActionsCache getCache() {
        return cache;
    }

    public void setCache(AdvActionsCache cache) {
        this.cache = cache;
    }

    public ActionIntrospector getActionsIntrospector() {
        return actionsIntrospector;
    }

    public void setActionsIntrospector(ActionIntrospector actionsIntrospector) {
        this.actionsIntrospector = actionsIntrospector;
    }

    public LoyTxDao getLoyTxDao() {
        return loyTxDao;
    }

    public void setLoyTxDao(LoyTxDao loyTxDao) {
        this.loyTxDao = loyTxDao;
    }

    public CardsEvent getCardsEvent() {
        return cardsEvent;
    }

    public void setCardsEvent(CardsEvent cardsEvent) {
        this.cardsEvent = cardsEvent;
    }

    public CashAdvertisingActionDao getActionsDao() {
        return actionsDao;
    }

    public void setActionsDao(CashAdvertisingActionDao actionsDao) {
        this.actionsDao = actionsDao;
    }

    public WholesaleRestrictionsDao getWrDao() {
        return wrDao;
    }

    public void setWrDao(WholesaleRestrictionsDao wrDao) {
        this.wrDao = wrDao;
    }

    @Override
    public void updateReturnAccruedBonuses(boolean status) {
        if (loyTechProcess != null) {
            loyTechProcess.getLoyaltyProperties().setReturnAccruedBonuses(status);
        }
    }

    @Override
    public void updateReturnChargedOffBonuses(boolean status) {
        if (loyTechProcess != null) {
            loyTechProcess.getLoyaltyProperties().setReturnChargedOffBonuses(status);
        }
    }

    @Override
    public void updateLossOfProfitNotification(boolean status) {
        if (loyTechProcess != null) {
            loyTechProcess.getLoyaltyProperties().setLossOfProfitNotification(status);
        }
    }

    @Override
    public void updateCalculateOnPayments(boolean status) {
        if (loyTechProcess != null) {
            loyTechProcess.getLoyaltyProperties().setCalculateOnPayments(status);
        }
    }

    @Override
    public void processRecalculateAfterPaymentTypeChange(PurchaseEntity purchase, Consumer<AdvertisingActionEntity> recalculateConsumer) {
        if (getLoyProviders().isNeedSubtotalAfterFirstPaymentTypeChanged(purchase)) {
            recalculateConsumer.accept(null);
            return;
        }

        Map<Object, AdvertisingActionEntity> actions = new HashMap<>();
        AdvertisingActionEntity[] advertisingActions = getAdvertisingActions(purchase);
        getLoyTechProcess().setAdvertisingActions(advertisingActions);

        for (AdvertisingActionEntity actionEntity : advertisingActions) {
            boolean isConditionPaymentType = false;
            List<IExternalConditionPlugin> externalConditionPlugins = getLoyTechProcess().getExternalConditionPlugins(actionEntity.getGuid());
            for (IExternalConditionPlugin plugin : externalConditionPlugins) {
                if (ActionPluginRegistry.CONDITION_PAYMENT_TYPE.equals(plugin.getPluginType())) {
                    isConditionPaymentType = true;
                }
            }
            if (isConditionPaymentType) {
                List<IApplyObjectPlugin> applyObjectPlugins = getLoyTechProcess().getApplyObjectPlugins(actionEntity.getGuid());
                for (IApplyObjectPlugin plugin : applyObjectPlugins) {
                    actions.put(plugin, actionEntity);
                }
                List<IActionResultPlugin> actionResultPlugins = getLoyTechProcess().getActionResultPlugins(actionEntity.getGuid());
                for (IActionResultPlugin plugin : actionResultPlugins) {
                    actions.put(plugin, actionEntity);
                }
            }
        }

        if (!actions.isEmpty()) {
            // по-умолчанию не включаем
            boolean enabled = propertiesManager.getBooleanProperty(Loyal.MODULE_NAME, null, PART_DISCOUNT_ENABLE_PROPERTY, false);
            // если запрещено или чек разделен - полный перерасчет
            if (!enabled || purchase.isPurchaseDivided()) {
                recalculateConsumer.accept(null);
                return;
            }

            // если плагин работает со списанием бонусов, то даже при округлении, необходимо пересчитывать потолок списания
            // поэтому проверим если ли карты с положительным доступным к списанию балансом
            List<PurchaseCardsEntity> cardsSuitableForWriteOff = getCardsSuitableForWriteOff(purchase, null);
            // да, это костыль для Set5
            cardsSuitableForWriteOff.removeIf(card -> ExternalLoyaltyService.SET5_CARDS_PROCESSING_NAME.equals(card.getProcessingName()));
            if (!cardsSuitableForWriteOff.isEmpty()) {
                recalculateConsumer.accept(null);
                return;
            }

            Object first;
            if (actions.size() == 1 && (first = actions.keySet().iterator().next()) instanceof IApplyObjectPlugin &&
                    DiscountType.ROUND.equals(((IApplyObjectPlugin) first).getDiscountType()) && actions.get(first).getWorksAnytime()) {
                // частичный перерасчет, если у нас только одна акция с условием по типу оплаты, суммирующаяся со всеми и она на округление
                IApplyObjectPlugin roundAction = (IApplyObjectPlugin) first;
                recalculateConsumer.accept(actions.get(roundAction));
            } else {
                recalculateConsumer.accept(null);
            }
        }
    }

    @Override
    public void doPartDiscount(PurchaseEntity originalPurchase,
                               Predicate<AdvertisingActionResultEntity> conditionToRemoveActionResult,
                               AdvertisingActionEntity... actions) throws Exception {
        long partStart = System.currentTimeMillis();
        long saveTime = 0;
        // получим копии акций с актуальными значениями ограничений из кэша сервиса ограничений
        if (restrictionsService != null) {
            restrictionsService.partCheckRestrictions(originalPurchase, actions);
        }

        // в кэш техпроцесса положим все возможные акции
        AdvertisingActionEntity[] advertisingActions = getAdvertisingActions(originalPurchase);
        List<AdvertisingActionEntity> allActions = Arrays.asList(advertisingActions);
        getLoyTechProcess().setAdvertisingActions(advertisingActions);

        // если нет скидочных акций, механизмы ФЗ54 на данном этапе не нужны
        boolean discountActionsFound = Arrays.stream(actions).anyMatch(a ->
                !(getLoyTechProcess().getApplyObjectPlugins(a.getGuid()).isEmpty()
                        || getLoyTechProcess().getActionResultPlugins(a.getGuid()).stream()
                        .anyMatch(IActionResultPlugin::isDiscountReplacingResult)));

        final List<PositionEntity> positions = originalPurchase.getPositions();
        // идентификаторы акций которые будут удалены
        List<Long> actionGuids = Arrays.stream(actions).map(AdvertisingActionEntity::getGuid).collect(Collectors.toList());
        if (discountActionsFound) {
            actionGuids.add(HardcodedAction.FZ54_BONUS_CORRECTION.getGuid());
        }
        // чистим эти акции
        Long discountValueToDecrement = Optional.ofNullable(originalPurchase.getDiscountPurchaseEntity())
                .map(DiscountPurchaseEntity::getDiscountPositions)
                .orElse(Collections.emptyList()).stream()
                .filter(discount -> actionGuids.contains(Optional.ofNullable(discount.getAdvAction())
                        .map(PKGenerated::getId)
                        .orElse(0L)))
                .mapToLong(positionDiscount -> {
                    long revertDiscountValue = positionDiscount.getDiscountAmount();
                    // позиция
                    PositionEntity positionEntity = positions.get(positionDiscount.getPositionOrder() - 1);
                    positionEntity.setSum(positionEntity.getSum() + revertDiscountValue);
                    positionEntity.setSumDiscount(positionEntity.getSumDiscount() - revertDiscountValue);
                    positionEntity.setPriceEnd(CurrencyUtil.roundEndPriceForSale(positionEntity.getSum(), positionEntity.getQnty()));
                    return revertDiscountValue;
                }).sum();

        // чек
        originalPurchase.setDiscountValueTotal(originalPurchase.getDiscountValueTotal() - discountValueToDecrement);

        // "округлятор"
        getLoyTechProcess().getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);

        // рассчитываем их по-новому
        Purchase original = LoyalCalculatorConverter.convertPurchase(originalPurchase, techProcess, tp, this, techProcessEvents,
                catalogService, true, true);

        // очистим результаты пересчитываемых акций
        filterPurchaseByActionGuids(original, actionGuids, conditionToRemoveActionResult);
        Purchase receiptCopy = original.cloneWithDisc();
        DiscountCalculationUtils.updateOriginalCosts(receiptCopy, currencyHandler);
        DiscountCalculationUtils.updateAppliedActions(receiptCopy, DiscountCalculationUtils.getFinalActionsGuids(allActions));
        getLoyTechProcess().getProcessingSession().put(CalculateSession.OUTBOUND_MUTEX_ACTIONS,
                DiscountCalculationUtils.getMutexActions(allActions));
        // заполним сработавшие ограничительные акции
        DiscountCalculationUtils.fillMutexActionsData(receiptCopy, getLoyTechProcess());
        PurchaseUtils.removeAllDiscounts(receiptCopy);
        getLoyTechProcess().setAdvertisingActions(actions);

        // DoProcessing.doDiscount - портит список активных РА, что не нужно
        Purchase purchase = CycleProcessing.calculate(getLoyTechProcess(), actions, receiptCopy);
        if (purchase != null) {
            DiscountCalculationUtils.restoreOriginalPrices(purchase, original);

            // сохраним карты
            AppliedActionInfo info = new AppliedActionInfo();
            info.getAppliedCards().addAll(originalPurchase.getAppliableCardsInfo());
            purchase.getAppliableActionsInfo().add(info);

            // смержим изменения в чеке и проверим на ФЗ-54
            Purchase mergedPurchase = DiscountCalculationUtils.mergePurchase(original, purchase, currencyHandler);
            if (discountActionsFound) {
                mergedPurchase = new FL54Tuner().tune(getLoyTechProcess(), actions, mergedPurchase);
            }

            // сохраним TX лояльности в БД
            long saveStart = System.currentTimeMillis();
            doSave(originalPurchase, mergedPurchase, currencyHandler, getAdvertisingActions(purchase));
            saveTime = System.currentTimeMillis() - saveStart;

            // Для бонусных карт "подпилим" высчитанные потолки списаний: чтоб они не были больше остаточной суммы чека
            adjustBonusDiscountCeilings(purchase);
        }

        JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_PART_CALCULATION, (System.currentTimeMillis() - partStart) - saveTime);
    }

    @Override
    public List<AdvertisingActionEntity> getAutoWriteOffActions(PurchaseEntity check) {
        AdvertisingActionEntity[] actions = getAdvertisingActions(check);

        if (ArrayUtils.isEmpty(actions)) {
            return Collections.emptyList();
        }

        Purchase purchase = LoyalCalculatorConverter.convertPurchase(check, techProcess, tp, this, techProcessEvents, catalogService);

        ILoyTechProcess loyTechProcess = new LoyTechProcess(getLoyaltyProperties());
        loyTechProcess.getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);
        loyTechProcess.setAdvertisingActions(actions);

        CalculateSession session = DirectProcessing.createSession(loyTechProcess);
        return AdvertisingActionUtils.selectActions(actions, ActionSelectCondition.makeBonusDiscountActions(BonusDiscountType.BONUS_SR10, true))
                .stream()
                .filter(action -> DiscountCalculationUtils.areConditionsMet(action, purchase, loyTechProcess, session))
                .collect(Collectors.toList());
    }

    @Override
    public AdvertisingActionEntity getActionByGuid(Long guid) {
        return cache.getCache().get(guid);
    }

    /**
     * Очистим результаты акций, которые надо пересчитать
     *
     * @param purchase                      чек
     * @param actionGuids                   идентификаторы ра
     * @param conditionToRemoveActionResult - условие, при котором надо убирать результат
     */
    private void filterPurchaseByActionGuids(Purchase purchase,
                                             List<Long> actionGuids,
                                             Predicate<AdvertisingActionResultEntity> conditionToRemoveActionResult) {
        Optional.ofNullable(actionGuids).ifPresent(guids -> {
            purchase.getAdvertisingActionResults().removeIf(result -> conditionToRemoveActionResult.test(result)
                    && guids.contains(result.getAdvertisingActionGUID()));
            purchase.getAppliedActionsInfo().removeIf(result -> guids.contains(result.getActionGuid()));
            purchase.getAppliableActionsInfo().removeIf(result -> guids.contains(result.getActionGuid()));
            purchase.getDiscounts().removeIf(result -> guids.contains(result.getAdvertisingActionGUID()));
            purchase.getBonusPositions().removeIf(result -> guids.contains(result.getAdvertActGuid()));
            purchase.getPositions().stream().filter(position -> position.getDiscountBatch() != null)
                    .forEach(position -> position.getDiscountBatch().getDiscounts()
                            .removeIf(result -> guids.contains(result.getAdvertisingActionGUID())));
        });
    }


    /**
     * Обновляет начисленные бонусы в транзакции лояльности, если например начислилось меньше или не начислилось вообще
     *
     * @param purchase     чек
     * @param actions      примененные РА
     * @param realAdded    сколько по факту начислено бонусов
     * @param mutexActions - "запрещающие" акции
     */
    public void updateBonusesLoyTransaction(PurchaseEntity purchase, List<AdvertisingActionEntity> actions, Long realAdded, Map<Long, Set<Long>> mutexActions) {
        Purchase original = LoyalCalculatorConverter.convertPurchase(purchase, techProcess, tp, this, techProcessEvents, catalogService, false);
        LoyTransactionEntity loyTx = searchDiscountResultsInDB(purchase.getLoyalTransactionId(), false);

        for (AdvertisingActionEntity entity : actions) {
            List<LoyBonusPositionEntity> bonusPositions = loyTx.getBonusPositions().stream()
                    .filter(loyBonusPositionEntity -> entity.getGuid().equals(loyBonusPositionEntity.getAdvAction().getGuid())).collect(Collectors.toList());
            List<LoyBonusTransactionEntity> bonusTransactions = loyTx.getBonusTransactions().stream()
                    .filter(loyBonusTransactionEntity -> entity.getGuid().equals(loyBonusTransactionEntity.getAdvAction().getGuid())).collect(Collectors.toList());
            // подправим
            if (realAdded > 0) {
                BonusDistributer.distributeBonusPositions(original, entity.getGuid(), realAdded, mutexActions, currencyHandler);
                for (BonusPosition src : original.getBonusPositions()) {
                    bonusPositions.stream().filter(bonusPosition -> bonusPosition.getPositionOrder() == src.getPositionOrder()).findFirst().ifPresent(loyPosition -> {
                        loyPosition.setBonusAmount(src.getBonusAmount());
                        // эту мы модифицировали и потом удалять не будем
                        bonusPositions.remove(loyPosition);
                    });
                }
                Long finalRealAdded = realAdded;
                bonusTransactions.stream().findFirst().ifPresent(bonusTransaction -> {
                    bonusTransaction.setBonusAmount(finalRealAdded);
                    // эту мы модифицировали и потом удалять не будем
                    bonusTransactions.remove(bonusTransaction);
                });

                realAdded = 0L;
            }
            loyTx.getBonusPositions().removeAll(bonusPositions);
            loyTx.getBonusTransactions().removeAll(bonusTransactions);
        }
        updateLoyTransaction(loyTx);
    }

    @Override
    public void processScanGifts(
            PurchaseEntity purchase,
            GiftDisplayTime giftDisplayTime,
            GiftsScanningScenario scanningScenario,
            Runnable afterProcessingCallback) {
        LoyTransactionEntity loyTransaction = findLoyTransaction(purchase);
        if (loyTransaction == null) {
            return;
        }

        List<LoyGiftNoteEnity> loyGifts = loyTransaction.getGiftNotes();
        if (CollectionUtils.isEmpty(loyGifts)) {
            return;
        }

        boolean updated = false;
        for (LoyGiftNoteEnity loyGift : loyGifts) {
            if (loyGift != null && Objects.equals(loyGift.getDisplayTime(), giftDisplayTime) && loyGift.notProcessed()) {
                boolean showScanGiftDialog = loyGift.isRequireScan() && !loyGift.isAddAsProducts();
                if (!loyGift.isAddAsProducts()) {
                    scanningScenario.showMessages(loyGift.getCashierMessage(), loyGift.getCustomerMessage(), showScanGiftDialog);
                }
                if (showScanGiftDialog) {
                    processScanGifts(loyGift, scanningScenario);
                } else if (loyGift.isAddAsProducts() && loyGift.isRequireScan()
                        && loyGift.getAdvAction() != null && loyGift.getAdvAction().getGuid() != null) {
                    // подарки уже отсканированы и добавлены как товары, осталось только посчитать их
                    int scannedCount = purchase.getPositions().stream()
                            .filter(pos -> loyGift.getAdvAction().getGuid().equals(pos.getAdvActionGuid()))
                            .mapToInt(pos -> pos.getQnty() != null ? pos.getQntyBigDecimal().intValue() : 1).sum();
                    loyGift.setScannedCount(scannedCount);
                } else {
                    loyGift.setScannedCount(0);
                }
                updated = true;
            }
        }

        if (updated) {
            updateLoyTransaction(loyTransaction);
        }

        if (afterProcessingCallback != null) {
            afterProcessingCallback.run();
        }
    }

    private void processScanGifts(LoyGiftNoteEnity loyGift, GiftsScanningScenario scanningScenario) {
        AtomicInteger scannedCount = new AtomicInteger();
        scanningScenario.showScanGiftDialog(loyGift.getTotalCount(),
                giftCode -> {
                    if (!loyGift.getBarcodes().contains(giftCode)) {
                        throw new CashException(CoreResBundle.getStringCommon("GIFT_UNKNOWN_BARCODE"));
                    }
                    scannedCount.incrementAndGet();
                });
        loyGift.setScannedCount(scannedCount.get());
    }

    @Override
    public boolean processScanGiftsAsProducts(SubtotalScenarioInterface scenaryImpl, Purchase loyPurchase) {
        if (loyPurchase == null || loyPurchase.getAdvertisingActionResults().stream().noneMatch(GiftNoteDescription.class::isInstance)
                || tp.getCheck().getPositions().stream().anyMatch(PositionEntity::isAddedByAdvAction)) {
            // если в чеке уже есть хотя бы одна позиция-подарок, значит это уже не первый расчет скидок
            // и повторно запрашивать сканирование подарков не надо
            return false;
        }
        boolean productsAdded = false;
        for (AdvertisingActionResultEntity actionResult : loyPurchase.getAdvertisingActionResults()) {
            if (actionResult instanceof GiftNoteDescription) {
                productsAdded = processScanGiftsAsProducts(scenaryImpl, (GiftNoteDescription) actionResult) || productsAdded;
            }
        }
        return productsAdded;
    }

    private boolean processScanGiftsAsProducts(SubtotalScenarioInterface scenaryImpl, GiftNoteDescription giftNote) {
        if (giftNote == null || giftNote.getAdvertisingActionGUID() == null
                || !giftNote.isAddAsProducts() || !Objects.equals(giftNote.getDisplayTime(), GiftDisplayTime.SUBTOTAL)) {
            return false;
        }
        AtomicBoolean added = new AtomicBoolean(false);
        scenaryImpl.showMessages(StringUtils.join(giftNote.getCashierMessage(), "\n"),
                StringUtils.join(giftNote.getCustomerMessage(), "\n"), true);
        scenaryImpl.showScanGiftDialog((int) giftNote.getTotalQnty(),
                giftCode -> {
                    try {
                        ProductEntity product = tp.searchProductWithoutBeep(giftCode);
                        if (product == null) {
                            throw new CashException(CoreResBundle.getStringCommon("PRODUCT_NOT_FOUND"));
                        }
                        ProductConfig productConfig = product.getProductConfig();
                        if (!productConfig.isSaleAllowed()) {
                            throw new CashException(CoreResBundle.getStringCommon("PRODUCT_SALE_PROHIBITED"));
                        }
                        if (!giftNote.getBarcodes().contains(product.getItem())) {
                            throw new CashException(CoreResBundle.getStringCommon("GIFT_UNKNOWN_BARCODE"));
                        }
                        product.getProductPositionData().setAdvActionGuid(giftNote.getAdvertisingActionGUID());
                        if (scenaryImpl.addProduct(product, BigDecimalConverter.getQuantityMultiplier())) {
                            scenaryImpl.updateCheck(tp.getCheck());
                            added.set(true);
                        } else {
                            throw new CashException(CoreResBundle.getStringCommon("GIFT_IS_NOT_ADDED"));
                        }
                    } catch (CashException e) {
                        log.error("processScanGiftsAsProducts: failed to process giftCode = " + giftCode, e);
                        throw e;
                    } catch (Exception e) {
                        log.error("processScanGiftsAsProducts: failed to process giftCode = " + giftCode, e);
                        throw new CashException(CoreResBundle.getStringCommon("UNKNOWN_ERROR"));
                    }
                });
        return added.get();
    }

    @Override
    public LoySetApiPluginTransactionEntity createSetApiTransaction(PurchaseCardsEntity purchaseCard, CardBonusBalance balance,
                                                                    long amount, String txId) {
        if (purchaseCard == null || purchaseCard.getCardType() == null || balance == null) {
            return null;
        }

        LoySetApiPluginTransactionEntity transaction = new LoySetApiPluginTransactionEntity();
        transaction.setProcessingName(purchaseCard.getProcessingName());
        transaction.setCardNumber(purchaseCard.getNumber());
        transaction.setMultiplier(balance.getMultiplier());
        // Величина отрицательна потому что мы списали бонусы.
        transaction.setAmount(-amount);
        transaction.setTransactionId(txId);
        transaction.setSponsorId(balance.getSponsorId());
        if (balance.getCurrentBonusAccount() != null) {
            transaction.setAccountName(balance.getCurrentBonusAccount().getBonusAccountsTypeName());
        }
        // РА бонусы SetAPI является приоритетной
        AdvertisingActionEntity chargeOffAction = getBonusDiscountActions().get(balance.getSponsorId());
        if (chargeOffAction == null) {
            // если такая не создана, используется основная РА с идентификатором соответствующего плагина лояльности
            String loyaltyPluginId = balance.getStrSponsorId();
            if (StringUtils.isBlank(loyaltyPluginId)) {
                LOG.warn("No sponsor found for card \"{}\"", purchaseCard.getNumber());
                return transaction;
            }
            List<AdvertisingActionEntity> actions = findAdvertisingActionsForPlugin(loyaltyPluginId);
            if (actions.size() > 1) {
                LOG.warn("Found several advertising actions for \"{}\" [{}]", loyaltyPluginId, actions);
            }
            if (actions.isEmpty()) {
                LOG.warn("No advertising actions found for \"{}\"", loyaltyPluginId);
                return transaction;
            }

            chargeOffAction = actions.get(0);
        }

        transaction.setAdvertisingActionGuid(chargeOffAction.getGuid());
        transaction.setAdvertisingActionName(chargeOffAction.getName());
        return transaction;
    }

    public Map<String, ActionPriceInfo> getMinimalPricesForGoodList(List<LoyalProductEntity> loyProducts, Date date, CardEntity cardEntity) {
        if (CollectionUtils.isEmpty(loyProducts)) {
            return Collections.emptyMap();
        }
        long stopWatch = System.currentTimeMillis();
        long allWatch = stopWatch;
        if (date == null) {
            date = new Date();
        }

        log.info("in getMinimalPricesForGoodList. Get {} products from DB, it took {} [ms]", loyProducts.size(), System.currentTimeMillis() - stopWatch);
        stopWatch = System.currentTimeMillis();
        Collection<AdvertisingActionEntity> actionsAtDate = getCache().getActiveActions(date, null);
        // оставим только РА со скидкой на товары
        List<AdvertisingActionEntity> selectActions = AdvertisingActionUtils.selectActions(actionsAtDate.toArray(new AdvertisingActionEntity[0]),
                ActionSelectCondition.makeGoodsDiscountResultActions());
        log.info("in getMinimalPricesForGoodList. Get and filter {} actions from DB, it took {} [ms]", actionsAtDate.size(), System.currentTimeMillis() - stopWatch);
        stopWatch = System.currentTimeMillis();
        // заполняем фиктивный чек списком товаров
        Map<String, ActionPriceInfo> goodsPrices = new HashMap<>();

        for (LoyalProductEntity loyProduct : loyProducts) {
            Purchase purchase = PurchaseUtils.makeOneProductPurchase(loyProduct, date, cardEntity);

            if (purchase != null) {
                // считаем скидки на товар чека
                Purchase result = DoProcessing.doDiscount(getLoyTechProcess(), selectActions.toArray(new AdvertisingActionEntity[0]), purchase);
                // достаем из чека товар со скидкой и преобразуем в VO
                for (Position resultPosition : result.getPositions()) {
                    goodsPrices.put(resultPosition.getGoodsItem(), null);

                    DiscountBatchEntity discountBatch = resultPosition.getDiscountBatch();

                    if (discountBatch != null && CollectionUtils.isNotEmpty(discountBatch.getDiscounts())) {
                        ActionPriceInfo priceVO = new ActionPriceInfo();
                        priceVO.setMarking(resultPosition.getGoodsItem());
                        priceVO.setPrice(resultPosition.getSum());
                        goodsPrices.put(resultPosition.getGoodsItem(), priceVO);
                    }
                }
            }
        }
        log.info("in getMinimalPricesForGoodList. Discount calculating of {} products finished, it took {} [ms]",
                loyProducts.size(), System.currentTimeMillis() - stopWatch);

        log.info("finished getMinimalPricesForGoodList. Of {} products, it took {} [ms]",
                loyProducts.size(), System.currentTimeMillis() - allWatch);
        return goodsPrices;
    }


    private List<AdvertisingActionEntity> findAdvertisingActionsForPlugin(String pluginId) {
        List<AdvertisingActionEntity> actions = new ArrayList<>();
        Collection<AdvertisingActionEntity> allActions = cache.getActiveActions(new Date(), null);
        for (AdvertisingActionEntity action : allActions) {
            for (IActionPlugin actionPlugin : action.getDeserializedPlugins()) {
                if (isExternalLoyaltyPlugin(actionPlugin, pluginId)) {
                    actions.add(action);
                    break;
                }
            }
        }
        return actions;
    }

    private boolean isExternalLoyaltyPlugin(IActionPlugin plugin, String provider) {
        if (!(plugin instanceof ActionPluginAttributable)) {
            return false;
        }
        ActionPluginAttributable aPlugin = (ActionPluginAttributable) plugin;
        return provider.equals(aPlugin.getPluginAttributesValues().get(ActionPluginAttributes.EXTERNAL_LOYALTY));
    }
}
