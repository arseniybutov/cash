package ru.crystals.loyal.calculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.BonusAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.DiscountEntity;
import ru.crystals.loyal.check.discount.DiscountSource;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.model.ActionSelectCondition;
import ru.crystals.loyal.model.LoyMetrics;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.utils.UnboxingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Валидатор чека лояльности. Проверки:
 * - проверка МРЦ;
 * - проверка неотрицательных цен, стоимостей, скидок и начислений бонусов;
 * - проверка скидки в более 100%
 * - проверка отсутствия скидок у нескидочного товара;
 * - проверка списания бонусов на позиции, с которых нельзя списать бонусы
 */
public abstract class LoyPurchaseValidator {
    private static final Logger log = LoggerFactory.getLogger(LoyPurchaseValidator.class);

    private LoyPurchaseValidator() {
    }

    /**
     * Валидация чека лояльности после всех рассчетов.
     *
     * @param originalPurchase кассовый чек, по которому делали рассчет скидок
     * @param purchase         чек лояльности
     * @param currencyHandler  округлятор
     * @param actions          все акции, что могли подействовать на чек
     * @return true если валидация прошла успешно
     */
    public static boolean validatePurchase(PurchaseEntity originalPurchase, Purchase purchase,
                                           CurrencyHandler currencyHandler, ILoyTechProcess techProcess,
                                           AdvertisingActionEntity[] actions) {
        if (currencyHandler == null) {
            currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);
        }
        try {
            List<Position> positions = purchase.getPositions();
            // вытащим гуиды акций на списание
            List<Long> bonusAsDiscountGuids = AdvertisingActionUtils.selectActions(actions,
                    ActionSelectCondition.BONUS_DISCOUNT_CONDITION).stream()
                    .map(AdvertisingActionEntity::getGuid).collect(Collectors.toList());
            // вытащим гуиды акций на начисление
            List<Long> bonusGuids = Stream.concat(AdvertisingActionUtils.selectActions(actions,
                    ActionSelectCondition.makeBonusResultActions()).stream(), AdvertisingActionUtils.selectActions(actions,
                    ActionSelectCondition.makeTokensResultActions()).stream())
                    .map(AdvertisingActionEntity::getGuid).collect(Collectors.toList());
            // вытащим стартовые цены по номеру позиции
            Map<Long, Long> startPriceMap = new HashMap<>();
            originalPurchase.getPositions().forEach(position -> startPriceMap.putIfAbsent(position.getNumber(), position.getPriceStart()));
            // вытащим гуиды акций с игнором ограничений
            Set<Long> ignoredRestrictionGuids = Stream.of(actions)
                    .filter(action -> !UnboxingUtils.valueOf(action.getUseRestrictions()))
                    .map(AdvertisingActionEntity::getGuid)
                    .collect(Collectors.toSet());
            // проверка неотрицательных начислений и списаний
            checkBonuses(purchase, bonusAsDiscountGuids, bonusGuids);

            for (Position position : positions) {
                // проверка отсутствия скидок у нескидочного товара
                if ((!position.isDiscountable() || position.isNoActions())
                        && position.getDiscountBatch() != null && position.getDiscountBatch().getDiscounts().stream()
                        .anyMatch(d -> d.getDiscountSource() != DiscountSource.SOFT_CHEQUE && UnboxingUtils.valueOf(d.getValueCalculated()) > 0)) {
                    throw new ValidateException(String.format("Position not discountable, but have Discount=%d!",
                            position.getDiscountValue()));
                }
                // проверка МРЦ
                validateMRC(position, originalPurchase, ignoredRestrictionGuids, currencyHandler, techProcess);

                // проверка неотрицательных цен, стоимостей, скидок
                long endPrice = position.getEndPrice(currencyHandler);
                if (endPrice < 0 || position.getSum() < 0 || position.getDiscountValue() < 0) {
                    throw new ValidateException(String.format("EndPrice=%d, Sum=%d or Discount=%d below zero!", endPrice,
                            position.getSum(), position.getDiscountValue()));
                }
                // списания бонусов на позиции, с которых нельзя списать бонусы
                if (!position.isBonusApplicable() && position.getDiscountBatch() != null && position.getDiscountBatch().getDiscounts().stream()
                        .anyMatch(discount -> bonusAsDiscountGuids.contains(discount.getAdvertisingActionGUID()))) {
                    throw new ValidateException(String.format("Marking=%s not applicable for bonuses, but have bonuses charged",
                            position.getGoodsItem()));
                }
                // проверка скидки в более 100%
                Long startPrice = startPriceMap.get(position.getNumber());
                if (startPrice != null) {
                    Long sum = CurrencyUtil.getPositionSum(startPrice, position.getCount());
                    Long discount = position.getDiscountValue();
                    if (sum < discount) {
                        throw new ValidateException(String.format("Discount=%d to position with marking=%s overflow the positions sum=%d",
                                discount, position.getGoodsItem(), sum));
                    }
                }
            }
            return true;
        } catch (ValidateException ve) {
            log.error(ve.getLocalizedMessage() + "; Purchase: " + purchase.toStringDetailed());
            return false;
        } catch (Exception e) { // если проверка пошла неожиданно, то не стоит портить весь рассчет
            log.error(e.getLocalizedMessage() + "; Purchase: " + purchase.toStringDetailed(), e);
            return true;
        }
    }

    private static void validateMRC(Position position, PurchaseEntity originalPurchase,
                                    Set<Long> ignoredRestrictionGuids, CurrencyHandler currencyHandler,
                                    ILoyTechProcess techProcess) throws ValidateException {
        if (position.getDiscountValue() > 0) { // если скидок на позицию не дано, то МРЦ проверять не стоит, вдруг оно выше первоначальной цены по ошибке
            long endPrice = position.getEndPrice(currencyHandler);
            Optional<PositionEntity> cashPosition = originalPurchase.getPositions().stream()
                    .filter(pos -> Objects.equals(pos.getItem(), position.getGoodsItem())).findFirst();
            long startPrice = cashPosition.isPresent() ? cashPosition.get().getPriceStart() :
                    currencyHandler.roundEndPriceForSale(position.getSum() + position.getDiscountValue(), position.getCount());
            // если МРЦ выше первоначальной цены, то это ошибка не калькулятора скидок, приводим МРЦ к первоначальной цене
            long minPrice = Math.min(getMinPrice(position, currencyHandler, techProcess), startPrice);
            // на всякий случай сравним с АМРЦ кассового чека
            if (!RestrictionUtils.canIgnoreRestrictions(position) ||
                    cashPosition.filter(pos -> pos instanceof PositionSpiritsEntity && pos.isExcise()).isPresent()) {
                long alcoMinPrice = Math.max(minPrice,
                        cashPosition.map(pos -> ((PositionSpiritsEntity) pos).getAlcoMinPrice()).orElse(0L));
                if (alcoMinPrice > 0 && endPrice < alcoMinPrice) {
                    throw new ValidateException(String.format("EndPrice=%d below MinimalPrice=%d!", endPrice, alcoMinPrice));
                }
            } else { // можно игнорировать ограничения по некоторым акциям
                long ignoredDiscountSum = position.getDiscountBatch() == null ? 0L :
                        position.getDiscountBatch().getDiscounts().stream()
                                .filter(dis -> ignoredRestrictionGuids.contains(dis.getAdvertisingActionGUID())
                                        || dis.getDiscountSource() == DiscountSource.SOFT_CHEQUE)
                                .mapToLong(DiscountEntity::getValueCalculated).sum();
                // на скакую сумму опустились ниже МРЦ (в идеале отрицалельно)
                long positionSum = currencyHandler.getPositionSum(minPrice, position.getCount()) -
                        currencyHandler.getPositionSum(endPrice, position.getCount());
                long delta = techProcess.getLoyaltyProperties().isFz54Compatible() ? 0L :
                        (position.getCount() / BigDecimalConverter.getQuantityMultiplier() - 1); // без ФЗ54 возможна погрешность
                if (minPrice > 0 && positionSum - ignoredDiscountSum > delta) {
                    throw new ValidateException(String.format("EndPrice=%d below MinimalPrice=%d! Ignored discount sum = %d",
                            endPrice, minPrice, ignoredDiscountSum));
                }
            }
        }
    }

    private static void checkBonuses(Purchase purchase, List<Long> bonusAsDiscountGuids, List<Long> bonusGuids) throws ValidateException {
        // начисления
        if (purchase.getBonusPositions().stream()
                .filter(bonusPosition -> bonusGuids.contains(bonusPosition.getAdvertActGuid()))
                .anyMatch(bonusPosition -> bonusPosition.getBonusAmount() < 0)) {
            log.error("Bonuses charged below zero!; Purchase: " + purchase.toStringDetailed());
        }
        if (purchase.getAdvertisingActionResults().stream()
                .filter(result -> bonusGuids.contains(result.getAdvertisingActionGUID()))
                .filter(result -> result instanceof BonusAdvertisingActionResultEntity)
                .anyMatch(bonusPosition -> ((BonusAdvertisingActionResultEntity) bonusPosition).getSumValue() < 0)) {
            log.error("Bonuses charged below zero!; Purchase: " + purchase.toStringDetailed());
        }
        // списания
        if (purchase.getBonusPositions().stream()
                .filter(bonusPosition -> bonusAsDiscountGuids.contains(bonusPosition.getAdvertActGuid()))
                .anyMatch(bonusPosition -> bonusPosition.getBonusAmount() > 0)) {
            throw new ValidateException("Bonuses written off below zero!");
        }
        if (purchase.getAdvertisingActionResults().stream()
                .filter(result -> bonusAsDiscountGuids.contains(result.getAdvertisingActionGUID()))
                .filter(result -> result instanceof BonusAdvertisingActionResultEntity)
                .anyMatch(bonusPosition -> ((BonusAdvertisingActionResultEntity) bonusPosition).getSumValue() > 0)) {
            throw new ValidateException("Bonuses written off below zero!");
        }
    }

    // локальное исключение
    private static class ValidateException extends Exception {
        private ValidateException(String message) {
            super(message);
        }
    }

    /**
     * Вычисляет минимальную цену без учета ограничения noActions, прокинутого из мягкого чека
     * (в отличии от ru.crystals.loyal.calculation.RestrictionUtils#getMinPrice )
     */
    private static long getMinPrice(Position position, CurrencyHandler currencyHandler, ILoyTechProcess loyTechProcess) {
        long originalCost = RestrictionUtils.getOriginalCatalogPrice(position);
        Function<Double, Long> fromMaxPercent = maxPercent ->
                (originalCost - currencyHandler.roundDown(1.0 * originalCost * maxPercent / LoyMetrics.PRICE_PRECISION));
        return RestrictionUtils.getRestrictionsAsValues(
                position, loyTechProcess, position.getDateTime(), false, 0, fromMaxPercent, Function.identity())
                .max(Long::compareTo).orElse(position.getOriginalCost());
    }
}
