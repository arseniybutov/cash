package ru.crystals.pos.loyal.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.calculation.PurchaseUtils;
import ru.crystals.loyal.check.Purchase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Утилитарный класс для извлечения из чека всех сработавших рекламных акций, включая и те, которые не привели к образованию скидки.
 * @since 10.2.71.0
 */
public class AppliedAdvertisingActionCollector {
    private static final Logger logger = LoggerFactory.getLogger(AppliedAdvertisingActionCollector.class);

    private LoyalServiceImpl loyalService;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link AppliedAdvertisingActionCollector}.
     * @param loyalService сервис лояльности.
     */
    public AppliedAdvertisingActionCollector(LoyalServiceImpl loyalService) {
        if(loyalService == null) {
            throw new IllegalArgumentException(LoyalServiceImpl.class + " cannot be null!");
        }
        this.loyalService = loyalService;
    }

    /**
     * Из переданного чека лояльности соберёт коллекцию всех сработавших в нём рекламных акций, включая те, что не дали скидок, но дали
     * какие-либо иные результаты.
     * @param receipt чек лояльности, из которого требуется собрать сработавшие рекламаные акции.
     * @return коллекция сработавших в чеке рекламных акций или пустая коллекция, если таких нет.
     */
    public Collection<AdvertisingActionEntity> collectAppliedActions(Purchase receipt) {
        ArrayList<AdvertisingActionEntity> appliedActions = new ArrayList<>();
        logger.debug("Collecting applied advertising actions guids from receipt {}", receipt.getNumber());
        Set<Long> appliedActionGuids = collectAppliedActionsGuids(receipt);
        logger.debug("From receipt {} collected {} advertising action guids: {}", receipt.getNumber(), appliedActionGuids.size(), appliedActionGuids);
        if(appliedActionGuids.isEmpty()) {
            return appliedActions;
        }
        AdvertisingActionEntity[] actionEntities = loyalService.getAdvertisingActions(receipt);
        Map<Long, AdvertisingActionEntity> actionsMap = Stream.of(actionEntities)
                .collect(Collectors.toMap(AdvertisingActionEntity::getGuid, Function.identity(), (a1, a2) -> a1));
        for (Long appliedActionGuid : appliedActionGuids) {
            AdvertisingActionEntity action = actionsMap.get(appliedActionGuid);
            if (action != null) {
                appliedActions.add(action);
                continue;
            }

            // акции может не быть на кассе, но информация о ней может быть в самой скидке
            receipt.getPositions().stream().filter(pos -> pos.getDiscountBatch() != null)
                    .flatMap(pos -> pos.getDiscountBatch().getDiscounts().stream())
                    .filter(disc -> appliedActionGuid.equals(disc.getAdvertisingActionGUID())).findFirst().ifPresent(disc -> {
                AdvertisingActionEntity a = new AdvertisingActionEntity();
                a.setGuid(disc.getAdvertisingActionGUID());
                a.setExternalCode(disc.getActionExternalCode());
                appliedActions.add(a);
            });
        }
        return appliedActions;
    }

    private Set<Long> collectAppliedActionsGuids(Purchase receipt) {
        Set<Long> appliedActionGuids = PurchaseUtils.getAppliedActionsGuids(receipt);
        appliedActionGuids.addAll(PurchaseUtils.getSecondaryActionsGuids(receipt));
        appliedActionGuids.addAll(PurchaseUtils.getManualActionsGuids(receipt));
        return appliedActionGuids;
    }
}
