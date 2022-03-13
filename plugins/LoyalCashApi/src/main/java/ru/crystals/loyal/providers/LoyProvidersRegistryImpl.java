package ru.crystals.loyal.providers;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.calculation.DiscountCalculationUtils;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.types.ExtendedCardType;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.loyal.cash.converter.LoyalCalculatorConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyFeedbackDao;
import ru.crystals.pos.loyal.cash.service.LoyProvidersRegistryWrapper;
import ru.crystals.pos.salemetrics.JmxMetrics;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.techprocess.TechProcessStage;
import ru.crystals.pos.utils.CheckUtils;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Дефолтная реализация {@link LoyProvidersRegistry}.
 * <p/>
 * NOTE: создается и инициализируется через Spring, но потом кладет себя в Bundle.
 * Для привязки Autowire получать надо через {@link LoyProvidersRegistryWrapper}, иначе может быть неактуальный реестр
 *
 * @author aperevozchikov
 */
@SuppressWarnings({"squid:S1188", "unused"})
public class LoyProvidersRegistryImpl implements LoyProvidersRegistry {
    private static final Logger log = LoggerFactory.getLogger(LoyProvidersRegistryImpl.class);

    // injected
    /**
     * Список поставщиков "услуг лояльности"
     */
    private List<LoyProvider> providers;

    /**
     * Поставщик информации о фидбеках
     */
    @Autowired(required = false)
    private LoyFeedbackDao loyFeedbackDao;

    /**
     * Максимальное кол-во отложенных фидбеков для отправки за одну итерацию для каждого провайдера
     */
    private Integer maxStaleFeedbackToSend;

    /**
     * Интервал отправки отложенных фидбеков по скидкам
     */
    private int staleFeedbackSendIntervalSeconds = 120;


    @Autowired
    private TechProcessInterface techProcess;

    @Autowired
    private Loyal loyalService;

    @Autowired
    private InternalCashPoolExecutor executor;

    @Autowired
    private LoyActionsProvider actionsProvider;

    @Autowired
    private TechProcessEvents techProcessEvents;

    @Autowired
    private CatalogService catalogService;

    /**
     * "округлятор"
     */
    @Autowired
    private CurrencyHandler currencyHandler;

    private ScheduledFuture<?> feedbackScheduled;

    /**
     * Схлопывает преференции от текущего "поставщика лояльности" с преференциями от всех предыдущих "поставщиков".
     *
     * @param previous        чек с "преференциями", полученными от предыдущих поставщиков лояльности
     * @param current         тот же самый (не equals! логически тот же самый) чек с преференциями, полученными от текущего "поставщика лояльности"
     * @param currencyHandler округлятор денег
     * @return {@code null}, если оба аргумента == {@code null}; иначе - результат схлопывания "преференций"
     */
    public static Purchase merge(Purchase previous, Purchase current, CurrencyHandler currencyHandler) {
        Purchase result;

        if (log.isTraceEnabled()) {
            log.trace("entering merge(Purchase, Purchase). The arguments are: previous [{}], current [{}]",
                    previous == null ? "(NULL)" : previous.toStringShort(), current == null ? "(NULL)" : current.toStringShort());
        }

        if (current == null) {
            // здорово! текущий "поставщик лояльности" скидок не дал (потому что он откючен, видимо)
            //  тогда previous - это результат схлопывания в любом случае
            log.trace("the current loy provider did not give any preferences!");
            result = previous;
        } else if (previous == null) {
            // а вот это уже настораживает. по техпроцессу сюда попасть не должны: самый первый чек в любом случае не null
            log.warn("the previous receipt was NULL somehow. the current receipt is the result then");
            result = current;
        } else {
            // самый общий случай: и предыдущий чек не null, и текущий "поставщик услуг лоялности" что-то насчитал
            // просто "перелить"
            result = DiscountCalculationUtils.mergePurchase(previous, current, currencyHandler);
        }

        if (log.isTraceEnabled()) {
            log.trace("leaving merge(Purchase, Purchase). The result is: {}", result == null ? "(NULL)" : result.toStringShort());
        }

        return result;
    }

    @Override
    public Purchase process(PurchaseEntity originalReceipt, ILoyTechProcess loyTechProcess, TechProcessServiceAsync techProcessServiceAsync)
            throws LoyProcessingException {
        Purchase result;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering process(PurchaseEntity, ILoyTechProcess, TechProcessServiceAsync). The arguments are: originalReceipt [{}]", originalReceipt);

        if (originalReceipt == null) {
            log.error("leaving process(PurchaseEntity, ILoyTechProcess, TechProcessServiceAsync). The \"originalReceipt\" argument is NULL");
            return null;
        }

        long time = System.currentTimeMillis();
        // оригинальный чек в виде калькуляторного чека:
        Purchase original = LoyalCalculatorConverter.convertPurchase(originalReceipt, techProcessServiceAsync, techProcess, loyalService,
                techProcessEvents, catalogService);
        JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_PURCHASE_CONVERT", System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        Collection<String> excludedProviders = loyTechProcess.getProcessingSession().getValue(CalculateSession.EXCLUDED_LOYALTY_PROCESSINGS, Collection.class);
        Collection<String> onlyProviders = loyTechProcess.getProcessingSession().getValue(CalculateSession.ONLY_LOYALTY_PROCESSINGS, Collection.class);

        actionsProvider.onDiscountCalculationStarted(original, CheckUtils.getExtData(originalReceipt));
        JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_ON_DISCOUNT_CALCULATION_STARTED", "provider", "InMemoryLoyActionsProvider",
                System.currentTimeMillis() - time);

        for (LoyProvider provider : getProvidersCollapsedByType()) {
            time = System.currentTimeMillis();
            if (isExcludeProvider(provider, original, excludedProviders, onlyProviders)) {
                continue;
            }
            provider.onDiscountCalculationStarted(original, originalReceipt, loyTechProcess);
            JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_PROVIDER_ON_STARTED", "provider", provider.getProviderName(),
                    System.currentTimeMillis() - time);
        }

        time = System.currentTimeMillis();
        Purchase previous = original.cloneWithDisc();
        JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_CLONE_PURCHASE", System.currentTimeMillis() - time);

        for (LoyProvider lp : getProviders()) {
            time = System.currentTimeMillis();
            if (isExcludeProvider(lp, original, excludedProviders, onlyProviders)) {
                continue;
            }
            // подсчитаем скидки от текущего "поставщика лояльности":
            Purchase current = lp.process(previous, originalReceipt, loyTechProcess);

            // схлопнем скидки от текущего поставщика со скидками от всех предыдущих:
            previous = merge(previous, current, currencyHandler);
            JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_PROVIDER_PROCESS", "provider", lp.getProviderName(),
                    System.currentTimeMillis() - time);
        }
        // здесь previous - это "сумма" преференций от всех поставщиков. Т.е., уже результат

        result = previous;

        // Наивный вызов метода пострасчета скидок, ничего не мержим, просто изменяем чек.
        // Потому что всё, что мы делаем сейчас, это правим потолки к списанию здесь.
        for (LoyProvider provider : getProviders()) {
            time = System.currentTimeMillis();
            if (isExcludeProvider(provider, original, excludedProviders, onlyProviders)) {
                continue;
            }
            Purchase tuned = provider.onDiscountCalculationFinished(result, originalReceipt, loyTechProcess);
            if (tuned != null) {
                result = tuned;
            }
            JmxMetrics.addMetricTime(JmxMetrics.MetricsTypes.DISCOUNTS, "DO_CALCULATION_PROVIDER_ON_FINISHED", "provider", provider.getProviderName(),
                    System.currentTimeMillis() - time);
        }

        long calculateTime = System.currentTimeMillis() - stopWatch;
        log.trace("leaving process(PurchaseEntity, ILoyTechProcess, TechProcessServiceAsync). The result is: {}; it took {} [ms]", result,
                calculateTime);
        JmxMetrics.addMetricTime(JmxMetrics.MetricsNames.DO_CALCULATION, calculateTime);

        return result;
    }

    private boolean isExcludeProvider(LoyProvider provider, Purchase purchase, Collection<String> exclusionList, Collection<?> inclusionList) {
        if (provider == null) {
            log.error("process: NULL was detected in the list of LoyProviders!");
            return true;
        }
        if (!provider.isPurchaseSuitable(purchase) || CollectionUtils.isNotEmpty(inclusionList) && !inclusionList.contains(provider.getProviderName())) {
            return true;
        }
        if (exclusionList != null && exclusionList.contains(provider.getProviderName())) {
            log.info("Skipped discount calculation for provider \"{}\" as it has been added in exclusion list", provider.getProviderName());
            return true;
        }
        return false;
    }

    @Override
    public void attachAppliedDiscountInfo(PurchaseEntity originalReceipt, Purchase receipt) {
        log.trace("entering attachAppliedDiscountInfo(PurchaseEntity, Purchase). The arguments are: originalReceipt [{}], receipt [{}]", originalReceipt, receipt);

        for (LoyProvider lp : getProviders()) {
            if (lp == null) {
                log.error("process: NULL was detected in the list of LoyProviders!");
                continue;
            }
            lp.attachAppliedDiscountInfo(originalReceipt, receipt);
        }

        log.trace("leaving attachAppliedDiscountInfo(PurchaseEntity, Purchase)");
    }

    /**
     * По факту - \@PostConstruct - метод инициализации - будет вызван Spring'ом после создания данного объекта и inject'а всех зависимостей.
     */
    public void init() {
        log.trace("entering init()");

        log.trace("Schedule resend stale feedback operation sender with timeout = {} seconds", staleFeedbackSendIntervalSeconds);
        feedbackScheduled = executor.scheduleWithFixedDelay(() -> {
            log.info("Resend stale feedback");

            LoyFeedbackDao dao = getLoyFeedbackDao();
            if (dao == null) {
                log.warn("seems, LoyFeedbackDao was NOT injected into Bundle yet!");
                return;
            }

            for (LoyProvider provider : getProvidersCollapsedByType()) {
                log.info("Search for stale feedback provider = {}", provider.getProviderName());
                try {
                    Collection<LoyExtProviderFeedback> feedbackToSend = dao.getFeedbackByProviderAndTime(provider.getProviderName(), FeedbackTime.AS_SOON_AS_POSSIBLE,
                            maxStaleFeedbackToSend);
                    if (CollectionUtils.isNotEmpty(feedbackToSend)) {
                        log.info("Found {} stale feedback for {}. Submit to executor", feedbackToSend.size(), provider.getProviderName());
                        executor.submit(new SendFeedbackTask(techProcess, loyalService, feedbackToSend, provider));
                    } else {
                        log.info("There is no stale feedback for {}", provider.getProviderName());
                    }
                } catch (Exception t) {
                    log.error("failed to send stale feedback... this time", t);
                }
            }

        }, staleFeedbackSendIntervalSeconds, (long) staleFeedbackSendIntervalSeconds, TimeUnit.SECONDS);

        log.trace("leaving init()");
    }

    // getters & setters

    public List<LoyProvider> getProviders() {
        if (providers == null) {
            providers = new LinkedList<>();
        }
        return providers;
    }

    public void setProviders(List<LoyProvider> providers) {
        this.providers = providers;
    }

    public Integer getMaxStaleFeedbackToSend() {
        return maxStaleFeedbackToSend;
    }

    public void setMaxStaleFeedbackToSend(Integer maxStaleFeedbackToSend) {
        this.maxStaleFeedbackToSend = maxStaleFeedbackToSend;
    }

    public int getStaleFeedbackSendIntervalSeconds() {
        return staleFeedbackSendIntervalSeconds;
    }

    public void setStaleFeedbackSendIntervalSeconds(int staleFeedbackSendIntervalSeconds) {
        this.staleFeedbackSendIntervalSeconds = staleFeedbackSendIntervalSeconds;
    }

    private LoyFeedbackDao getLoyFeedbackDao() {
        return loyFeedbackDao;
    }

    public void setLoyFeedbackDao(LoyFeedbackDao loyFeedbackDao) {
        this.loyFeedbackDao = loyFeedbackDao;
    }

    /**
     * Получить список провайдеров без дубликатов, чтобы при поиске купонов не искать несколько раз в одной и той же реализации
     *
     * @return Схлопнутый список провайдеров
     */
    private Collection<LoyProvider> getProvidersCollapsedByType() {
        Map<Class, LoyProvider> providersMap = new HashMap<>();
        for (LoyProvider loyProvider : getProviders()) {
            if (!providersMap.containsKey(loyProvider.getClass())) {
                providersMap.put(loyProvider.getClass(), loyProvider);
            }
        }
        return providersMap.values();
    }

    @Override
    public ExtendedCardType getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        ExtendedCardType result = new ExtendedCardType(CardTypes.CardNotFound);
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            CardTypes couponType = loyProvider.getCouponType(couponData, techProcess);
            if (couponType != null && !CardTypes.CardNotFound.equals(couponType)) {
                return new ExtendedCardType(couponType, loyProvider.getProviderName());
            }
        }
        return result;
    }

    @Override
    public void confirmDiscount(PurchaseEntity purchaseEntity, LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime) {
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.confirmDiscount(purchaseEntity, loyTransactionEntity, feedbackTime);
        }
    }

    @Override
    public void beforeFiscalize(PurchaseEntity purchase) {
        log.trace("entering beforeFiscalize(PurchaseEntity). The argument is: {}", purchase);
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.beforeFiscalize(purchase);
        }
        log.trace("leaving beforeFiscalize(PurchaseEntity)");
    }

    @Override
    public void preparePurchaseFiscalization(PurchaseEntity purchase, Check check, LoyTransactionEntity loyTransaction) {
        log.trace("entering preparePurchaseFiscalization(PurchaseEntity). The argument is: {}", purchase);
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.preparePurchaseFiscalization(purchase, check, loyTransaction);
        }
        log.trace("leaving preparePurchaseFiscalization(PurchaseEntity)");
    }

    @Override
    public void purchaseFiscalized(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        log.trace("entering purchaseFiscalized(PurchaseEntity). The argument is: {}", purchase);
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.purchaseFiscalized(purchase, loyTransaction);
        }
        actionsProvider.onPurchaseProcessingFinished();
        log.trace("leaving purchaseFiscalized(PurchaseEntity)");
    }

    @Override
    public void checkCanceled(PurchaseEntity purchase) {
        LoyFeedbackDao dao = getLoyFeedbackDao();
        if (dao != null) {
            dao.removeFeedbackByChequeId(purchase.getId());
        }
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.checkCanceled(purchase);
        }
        actionsProvider.onPurchaseProcessingFinished();
    }

    @Override
    public void cancelDiscount(PurchaseEntity purchase) {
        log.trace("entering cancelDiscount(PurchaseEntity). The argument is: {}", purchase);
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            loyProvider.cancelDiscount(purchase);
        }
        actionsProvider.onPurchaseProcessingFinished();
        log.trace("leaving cancelDiscount(PurchaseEntity)");
    }

    @Override
    public boolean isLoyTransactionComplete(LoyTransactionEntity loyTransaction) {
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            if (!loyProvider.isLoyTransactionComplete(loyTransaction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isLoyTransactionRequiredForStage(TechProcessStage stage) {
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            if (loyProvider.isLoyTransactionRequiredForStage(stage)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNeedSubtotalAfterFirstPaymentTypeChanged(PurchaseEntity purchase) {
        for (LoyProvider loyProvider : getProvidersCollapsedByType()) {
            if (loyProvider.isNeedSubtotalAfterFirstPaymentTypeChanged(purchase)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, BonusDiscountType> getDefaultBonusDiscountTypes() {
        Map<String, BonusDiscountType> result = new HashMap<>();
        for (LoyProvider loyProvider : getProviders()) {
            result.put(loyProvider.getProviderName(), loyProvider.getDefaultBonusDiscountType());
        }
        return result;
    }

    @Override
    public List<String> getAllProvidersExclude(List<String> excludeList) {
        if (getProviders().isEmpty()) {
            return Collections.emptyList();
        }

        return getProviders()
                .stream()
                .filter(provider -> !excludeList.contains(provider.getProviderName()))
                .map(LoyProvider::getProviderName)
                .collect(Collectors.toList());

    }

    @PreDestroy
    public void destroy() {
        feedbackScheduled.cancel(false);
    }
}
