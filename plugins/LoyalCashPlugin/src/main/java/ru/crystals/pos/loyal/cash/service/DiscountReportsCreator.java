package ru.crystals.pos.loyal.cash.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdvertisingAction4Position;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdvertisingAction4Purchase;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscountsReport;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.user.UserEntity;

import java.util.Map;
import java.util.Set;

/**
 * Формирует отчет по примененным рекламным акциям.
 *
 * @author a.yablokov
 */
class DiscountReportsCreator {
    private static final Logger LOG = LoggerFactory.getLogger(DiscountReportsCreator.class);
    private final PurchaseEntity purchase;
    private final LoyTransactionEntity loyTransactionEntity;
    private final TechProcessInterface techProcess;
    private final Set<Long> sectionNumbers;

    /**
     * GUID'ы РА, что требуют применения внутренней карты для своего срабатывания
     */
    private final Set<Long> cardActions;
    private final CatalogService catalogService;

    /**
     * кэш акций
     */
    private Map<Long, AdvertisingActionEntity> actionsCache;


    public DiscountReportsCreator(PurchaseEntity purchase, LoyTransactionEntity loyTransactionEntity, TechProcessInterface techProcess,
                                  Set<Long> sectionNumbers, Set<Long> cardActions, CatalogService catalogService) {
        this.purchase = purchase;
        this.loyTransactionEntity = loyTransactionEntity;
        this.techProcess = techProcess;
        this.sectionNumbers = sectionNumbers;
        this.cardActions = cardActions;
        this.catalogService = catalogService;
    }

    public DiscountsReport createDiscountsReport() {
        if (loyTransactionEntity == null || loyTransactionEntity.getDiscountPositions().isEmpty() ||
            loyTransactionEntity.getDiscountValueTotal() <= 0) {
            return null;
        }

        //CAUTION! do not remove, AdvertisingAction4Position creation will be break down
        LoyalProductsConverter.setUseCache(true);
        LoyalProductsConverter.findLoyalProductsForPurchase(purchase, catalogService);
        LoyalProductsConverter.setUseCache(false);

        DiscountsReport result = new DiscountsReport(purchase);
        result.setCashier(getCashier(purchase));
        result.setDepart(getDepart());

        for (LoyDiscountPositionEntity discountPosition : loyTransactionEntity.getDiscountPositions()) {
            if (discountPosition == null) {
                continue;
            }

            result.setTotalDiscountSum(result.getTotalDiscountSum() + discountPosition.getDiscountAmount());

            LoyAdvActionInPurchaseEntity advAction = discountPosition.getAdvAction();
            if (discountPosition.isDiscountPurchase()) {
                // Если скидка на весь чек, суммируем для получения конечного значения
                boolean needNewAction = true;

                //поищем, может быть эта скидка уже есть в списке
                for (AdvertisingAction4Purchase action : result.getPurchaseActions()) {
                    //если guid совпадает, то вот она
                    if (action.getActionGuid().equals(advAction.getGuid())) {
                        //обновим данные
                        action.addDiscountPosition(discountPosition.getDiscountAmount(), discountPosition.getPositionOrder());
                        needNewAction = false;
                        break;
                    }
                }
                if (needNewAction) {
                    //если не нашли - создадим новую
                    AdvertisingAction4Purchase newAction =
                            AdvertisingAction4Purchase.create(advAction, discountPosition);
                    if (actionsCache != null) {
                        AdvertisingActionEntity actionEntity = actionsCache.get(advAction.getGuid());
                        newAction.setLabels(actionEntity != null ? actionEntity.getLabels() : null);
                    }
                    result.getPurchaseActions().add(newAction);
                }
            } else {
                //пропустить если нет кода товара
                if (StringUtils.isBlank(discountPosition.getGoodCode())) {
                    continue;
                }

                AdvertisingAction4Position action4Position = AdvertisingAction4Position.create(advAction, discountPosition);
                // для хардкода выделения наборов: по типу SECOND_PRICE - не работает
                action4Position.setExternalCode(advAction.getExternalCode());
                if (actionsCache != null) {
                    AdvertisingActionEntity actionEntity = actionsCache.get(advAction.getGuid());
                    action4Position.setLabels(actionEntity != null ? actionEntity.getLabels() : null);
                }
                //включение кэша только для поиска товаров для расшифровки скидок
                //для операции расчета может повлечь проблемы с изменениями цены
                // Поиск товара лояльности. Предварительно должен быть вызван метод LoyalProductsConverter.findLoyalProductsForPurchase
                LoyalProductEntity loyalProductEntity = LoyalProductsConverter.getLoyalProductByItem(discountPosition.getGoodCode(), false);
                action4Position.setGoodsName(getGoodsName(loyalProductEntity, discountPosition));
                action4Position.setCount(discountPosition.getQnty() != null ? discountPosition.getQnty() : 1000L);
                action4Position.setOriginalSetCount(discountPosition.getOriginalSetQnty());
                result.getAllPositionActions().add(action4Position);
                //если размер скидки равен нулю - не будем добавлять в скидки на позиции
                if (discountPosition.getDiscountAmount() > 0) {
                    result.getPositionActions().add(action4Position);
                }
            }

            // SR-1234: сумма скидок по внутренней карте
            if (CollectionUtils.isNotEmpty(cardActions) && discountPosition.getAdvAction() != null &&
                    cardActions.contains(discountPosition.getAdvAction().getGuid())) {
                result.setTotalDiscountByCard(result.getTotalDiscountByCard() + discountPosition.getDiscountAmount());
            }

        } // for discountPosition
        return result;
    }

    /**
     * Если товар не был найден в товарах лояльности, попробуем найти его в кассовых товарах.
     */
    private String getGoodsName(LoyalProductEntity loyalProductEntity, LoyDiscountPositionEntity discountPosition) {
        String goodsName;
        if (loyalProductEntity == null) {
            LOG.warn("Can't find loyal product with item {} for discounts report. Try to find in catalog...", discountPosition.getGoodCode());
            ProductEntity product = null;
            try {
                product = techProcess.searchProductWithoutBeep(discountPosition.getGoodCode());
            } catch (Exception e) {
                LOG.error(String.format("Error during searching product with item %s", discountPosition.getGoodCode()), e);
            }
            if (product == null) {
                LOG.warn("Can't find product in catalog with item {} for discounts report.", discountPosition.getGoodCode());
                goodsName = "";
            } else {
                goodsName = product.getName();
            }
        } else {
            goodsName = loyalProductEntity.getName();
        }
        return goodsName;
    }

    private long getDepart() {
        if (sectionNumbers == null || !sectionNumbers.iterator().hasNext()) {
            return 0L;
        } else {
            return sectionNumbers.iterator().next();
        }
    }

    /**
     * Возвращает кассира, пробившего чек.
     */
    private Cashier getCashier(PurchaseEntity purchase) {
        UserEntity currentUser = techProcess.getCurrentUser();
        if (currentUser != null) {
            Cashier cashier = new Cashier();
            cashier.setName(currentUser.getLastnameFirstnameMiddleName());
            cashier.setTabNum(currentUser.getTabNum());
            return cashier;
        } else {
            //если вдруг он не задан, возьмем из чека
            return new Cashier(purchase.getSession().getUser());
        }
    }

    public void setActionsCache(Map<Long, AdvertisingActionEntity> actionsCache) {
        this.actionsCache = actionsCache;
    }
}
