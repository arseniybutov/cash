package ru.crystals.loyal.actions.provider;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.ManualPositionAdvActionEntity;
import ru.crystals.pos.check.ManualAdvertisingActionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.loyal.cash.service.AdvActionsCache;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Быстрая" реализация {@link LoyActionsProvider}: ведет поиск РА из оперативки (из кэша).
 *
 * @author aperevozchikov
 */
public class InMemoryLoyActionsProvider implements LoyActionsProvider {
    private static final Logger log = LoggerFactory.getLogger(InMemoryLoyActionsProvider.class);

    @Autowired(required = false)
    private AdvertisingActionProviderRoutine actionProviderRoutine;
    // injected
    /**
     * Кэш рекламных акций; заполняется в момент старта активными (и будущими акциями)
     */
    private AdvActionsCache cache;

    // Вызывается спрингом потому что так написали в конфиге.
    // Не помечен как @PostConstruct, потому что потому или это могло замедлить стартуп кассы.
    // Подробности: https://stackoverflow.com/questions/1088550/how-to-call-a-method-after-bean-initialization-is-complete
    @SuppressWarnings("unused")
    protected void start() {
        log.info("{} init method called", this.getClass());
    }

    @Override
    public void onDiscountCalculationStarted(Purchase startPurchase, Map<String, String> purchaseExtData) {
        if (actionProviderRoutine != null) {
            actionProviderRoutine.updateActionsCache(startPurchase, purchaseExtData, cache);
        }
    }

    @Override
    public void onPurchaseProcessingFinished() {
        if (actionProviderRoutine != null) {
            actionProviderRoutine.clearActionsCache(cache);
        }
    }

    @Override
    public Collection<AdvertisingActionEntity> getActions(PurchaseEntity receipt) {
        return getActions(() -> Optional.ofNullable(receipt).map(PurchaseEntity::getPositions).map(List::stream).orElse(Stream.empty())
                .map(PositionEntity::getManualAdvertisingActions)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(ManualAdvertisingActionEntity::getActionGuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    @Override
    public Collection<AdvertisingActionEntity> getActions(Purchase purchase) {
        return getActions(() -> Optional.ofNullable(purchase).map(Purchase::getPositions).map(List::stream).orElse(Stream.empty())
                .map(Position::getManualAdvActions)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(ManualPositionAdvActionEntity::getActionGuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    /**
     * Вернет список РА, что действуют на указанный чек в данным момент времени.
     *
     * @param manualActionGuidsSupplier функция для получения ручных акций
     * @return не {@code null}
     */
    private Collection<AdvertisingActionEntity> getActions(Supplier<Set<Long>> manualActionGuidsSupplier) {
        Collection<AdvertisingActionEntity> result;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getActions.");

        // 1. Получим GUID'ы ручных РА:
        Set<Long> manualActions = manualActionGuidsSupplier.get();

        // 2. и действующие в данный момент акции, с учетом этих ручных
        List<AdvertisingActionEntity> activeActions = getCache().getActiveActions(new Date(), manualActions);

        result = activeActions == null ? new LinkedList<>() : activeActions;

        log.trace("leaving getActions. The result size is: {}; it took {} [ms]",
                result.size(), System.currentTimeMillis() - stopWatch);

        return result;
    }


    // getters & setters:

    public AdvActionsCache getCache() {
        return cache;
    }

    public void setCache(AdvActionsCache cache) {
        this.cache = cache;
    }
}