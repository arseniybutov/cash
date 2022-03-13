package ru.crystals.pos.loyal.cash.converter;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.coupons.CouponsEntity;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity;
import ru.crystals.discount.processing.entity.LoyDiscountCardEntity;
import ru.crystals.discount.processing.entity.LoyGiftNoteByConditionEnity;
import ru.crystals.discount.processing.entity.LoyGiftNoteEnity;
import ru.crystals.discount.processing.entity.LoyProcessingCouponEntity;
import ru.crystals.discount.processing.entity.LoyQuestionaryEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.check.CardInfo;
import ru.crystals.loyal.check.Payment;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.PositionProductionDate;
import ru.crystals.loyal.check.PrintObject;
import ru.crystals.loyal.check.PrintObjectType;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.Shift;
import ru.crystals.loyal.check.TemplatedPrintObject;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.AnswerType;
import ru.crystals.loyal.check.discount.AppliedActionInfo;
import ru.crystals.loyal.check.discount.BonusAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeAdvertiseActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeCouponAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.check.discount.DiscountPurchaseEntity;
import ru.crystals.loyal.check.discount.ExternalLoyaltyCheckMessageActionResult;
import ru.crystals.loyal.check.discount.GiftByConditionDescription;
import ru.crystals.loyal.check.discount.GiftNoteDescription;
import ru.crystals.loyal.check.discount.ManualPositionAdvActionEntity;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.ProcessingCouponActionResultEntity;
import ru.crystals.loyal.check.discount.QuestionnaireAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.QuestionnaireQuestionEntity;
import ru.crystals.loyal.model.LoyMetrics;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType.ProductType;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.ManualPositionAdvertisingActionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseCardExtDataEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.check.discountresults.AdvActionInPurchaseEntity;
import ru.crystals.pos.check.discountresults.ChequeAdvertEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.MessageEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CouponTemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ExternalLoyaltyCheckMessageDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Row;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Text;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.payments.PaymentPropertyEntity;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.utils.PrintObjectJsonUtil;
import ru.crystals.utils.UnboxingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ppavlov
 * @author A.Martynov
 */
public class LoyalCalculatorConverter {
    private static final Logger LOG = LoggerFactory.getLogger(LoyalCalculatorConverter.class);

    private static List<List<PrintObject>> convertServiceDocs(List<ServiceDocument> cashServiceDocs) {
        if (cashServiceDocs == null || cashServiceDocs.isEmpty()) {
            return null;
        }

        List<List<PrintObject>> serviceDocs = new ArrayList<>();

        for (ServiceDocument cashDoc : cashServiceDocs) {
            if (cashDoc instanceof SimpleServiceDocument) {
                List<PrintObject> row = new ArrayList<>();
                for (Row cashRow : ((SimpleServiceDocument) cashDoc).getRows()) {
                    PrintObjectType printObjectType;
                    if (cashRow instanceof Text) {
                        printObjectType = PrintObjectType.TEXT;
                    } else if (cashRow instanceof BarCode && ((BarCode) cashRow).getType() == BarCodeType.QR) {
                        printObjectType = PrintObjectType.QR;
                    } else {
                        printObjectType = PrintObjectType.BARCODE;
                    }
                    row.add(new PrintObject(printObjectType, cashRow.getValue()));
                }
                serviceDocs.add(row);
            }
        }
        return serviceDocs;
    }

    private static Shift convertShift(ShiftEntity cashShift) {
        if (cashShift == null) {
            return null;
        }

        Shift shift = new Shift();

        shift.setCashNum(cashShift.getCashNum());
        shift.setEklzNum(cashShift.getEklzNum());
        shift.setFiscalNum(cashShift.getFiscalNum());
        shift.setFiscalSum(cashShift.getFiscalSum());
        shift.setNumShift(cashShift.getNumShift());
        shift.setShiftClose(cashShift.getShiftClose());
        shift.setShiftOpen(cashShift.getShiftOpen());
        shift.setShopIndex(cashShift.getShopIndex());
        shift.setSumCashBegin(cashShift.getSumCashBegin());

        return shift;
    }

    private static Payment convertPayment(PaymentEntity cashPayment, Purchase purchase) {
        Payment payment = new Payment();

        payment.setCurrency(cashPayment.getCurrency());
        payment.setId(cashPayment.getId());
        payment.setPaymentType(cashPayment.getPaymentType());
        payment.setPurchase(purchase);
        payment.setSumPay(cashPayment.getSumPay() == null ? 0L : cashPayment.getSumPay());
        payment.setSumPayBaseCurrency(payment.getSumPayBaseCurrency());
        payment.setProperties(cashPayment.getProperties().stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getKey() != null && StringUtils.isNotBlank(it.getKey().getName()))
                .collect(Collectors.toMap(it -> it.getKey().getName(), PaymentPropertyEntity::getValue, (a, b) -> a)));

        // CR-1019: нужен номер карты в случае оплаты по банку
        if (cashPayment instanceof BankCardPaymentEntity) {
            String cardNo = ((BankCardPaymentEntity) cashPayment).getCardNumber();
            payment.setCardNo(cardNo);
        }

        return payment;
    }

    private static ManualPositionAdvActionEntity convertManualPositionAdvertisingAction(ManualPositionAdvertisingActionEntity actionEntity, Position position) {
        ManualPositionAdvActionEntity manualPositionAdvActionEntity = new ManualPositionAdvActionEntity();
        manualPositionAdvActionEntity.setPosition(position);
        manualPositionAdvActionEntity.setCount(position.getCount() < actionEntity.getQnty() ? position.getCount() : actionEntity.getQnty());
        manualPositionAdvActionEntity.setActionGuid(actionEntity.getActionGuid());
        manualPositionAdvActionEntity.setId(null);
        return manualPositionAdvActionEntity;
    }

    private static Position convertPosition(PositionEntity cashPosition, Purchase purchase, Map<Long, Collection<Long>> positionToPositionalCoupon,
                                            TechProcessServiceAsync techProcess, TechProcessEvents techProcessEvents, boolean withDiscount,
                                            boolean withBonus, LoyTransactionEntity loyTransaction, Map<Long, Long> siebelPositionsMinPrices) {
        Position position = new Position();
        position.setId(cashPosition.getId());
        if (withDiscount && cashPosition.getPriceEnd() != null) {
            position.setOriginalCost(cashPosition.getPriceStart());
            position.setCost(cashPosition.getPriceEnd());
            position.setSum(cashPosition.getSum());
            if (withBonus) {
                position.setDiscountBatch(convertDiscountBatchEntity(loyTransaction, cashPosition, cashPosition.getSumDiscount()));
            } else {
                position.setDiscountValue(cashPosition.getSumDiscount());
            }
        } else {
            position.setCost(cashPosition.getPriceStart());
            position.setSum(cashPosition.getSum());
        }
        position.setNds(UnboxingUtils.valueOf(cashPosition.getNds(), 0));
        position.setCount(cashPosition.getQnty());
        position.setDateTime(cashPosition.getDateTime());
        position.setBarcode(cashPosition.getBarCode());
        position.setDepartNumber(cashPosition.getDepartNumber());

        position.setNoActions(cashPosition.isNoActions());
        position.setFixedPrice(cashPosition.isFixedPrice());
        position.setSoftChequeId(cashPosition.getSoftCheckNumber());
        position.setAdvActionGuid(cashPosition.getAdvActionGuid());
        position.setDiscountable(UnboxingUtils.valueOf(cashPosition.getCalculateDiscount(), true));

        // Поиск товара лояльности. Предварительно должен быть вызван метод LoyalProductsConverter.findLoyalProductsForPurchase
        LoyalProductEntity loyalProductEntity = LoyalProductsConverter.getLoyalProductByItem(cashPosition.getItem());
        if (cashPosition.getProduct() != null) {
            loyalProductEntity.setGoodBitMask(cashPosition.getProduct().getCategoryMask());
            loyalProductEntity.setDiscountable(!Boolean.FALSE.equals(cashPosition.getProduct().getIsDiscountApplicable()));
        }
        SpiritsRestrictionsHandler.addSpiritsMinPriceRestrictions(cashPosition, loyalProductEntity, techProcess, techProcessEvents);

        position.setProductCount(convertCountToProductCount(loyalProductEntity.getProductType().getType(), cashPosition.getQnty()));

        //Применимы ли бонусы к это позиции
        if (cashPosition.getProductSettings() != null) {
            position.setBonusApplicable(UnboxingUtils.valueOf(cashPosition.getProductSettings().getIsBonusApplicable()));
        }

        position.setGoods(loyalProductEntity);
        position.setNumber(cashPosition.getNumber());
        position.setPurchase(purchase);
        position.setTypePriceNumber(cashPosition.getTypePriceNumber());
        position.setInsertType(cashPosition.getInsertType());
        position.setExpirationDate(cashPosition.getExpirationDate());
        cashPosition.getProductionDates().stream().filter(pd -> pd.getProductionDate() != null && pd.getQuantity() != null)
                .forEach(pd -> position.getProductionDates().add(new PositionProductionDate(pd.getProductionDate(), pd.getQuantity())));

        //Если были позиционные купоны, то прилепим их к позиции
        position.setPositionalCouponNumbers(positionToPositionalCoupon.get(cashPosition.getId()));

        // convert manual advertising actions
        List<ManualPositionAdvertisingActionEntity> manualAdvertisingActions = cashPosition.getManualAdvertisingActions();
        if (manualAdvertisingActions != null) {
            List<ManualPositionAdvActionEntity> manualAdvActions = new ArrayList<>();
            for (ManualPositionAdvertisingActionEntity entity : cashPosition.getManualAdvertisingActions()) {
                manualAdvActions.add(convertManualPositionAdvertisingAction(entity, position));
            }
            position.setManualAdvActions(manualAdvActions);
        }

        position.setExternalMinPriceRestriction(calculateExternalMinPriceRestriction(cashPosition, siebelPositionsMinPrices));
        position.setErpCode(cashPosition.getErpCode());

        return position;
    }

    /**
     * Определит МРЦ на единицу товара от внешних систем и вернет максимальное значение.
     * Вернет <code>null</code>, если ни одной МРЦ не было задано.
     */
    private static Long calculateExternalMinPriceRestriction(PositionEntity cashPosition, Map<Long, Long> siebelPositionsMinPrices) {
        List<Long> minPrices = new ArrayList<>();
        if (cashPosition instanceof PositionSpiritsEntity) {
            minPrices.add(((PositionSpiritsEntity) cashPosition).getAlcoMinPrice());
        }
        if (siebelPositionsMinPrices != null) {
            minPrices.add(siebelPositionsMinPrices.get(cashPosition.getNumber()));
        }
        return minPrices.stream().filter(Objects::nonNull).reduce(Long::max).orElse(null);
    }

    private static Map<Long, Long> getSiebelPositionsMinPrices(PurchaseEntity purchase) {
        CardEntity card = CheckUtils.getFirstBonusCard(purchase, BonusDiscountType.BONUS_SIEBEL);
        if (card == null || card.getCardType() == null || card.getCardType().getTransientPropertyBag() == null) {
            return null;
        }
        return (Map<Long, Long>) card.getCardType().getTransientPropertyBag().get(SiebelService.SIEBEL_POSITIONS_MIN_PRICES);
    }

    private static Integer convertCountToProductCount(ProductType productType, Long count) {
        // Есть варианты, что товары схлопнулись.
        // Но после схлопывания информация потерялась о том, какие позиции были
        // Это значений корректно, если выключено схлопывание. Ограничение согласовано с ПО
        if (ProductType.WEIGHT.equals(productType)) {
            return 1;
        }
        return count >= LoyMetrics.COUNT_PRECISION ? (int) (count / LoyMetrics.COUNT_PRECISION) : 1;
    }

    private static DiscountBatchEntity convertDiscountBatchEntity(LoyTransactionEntity loyTx, PositionEntity cashPosition, long discountValue) {
        String productCode = cashPosition.getItem();
        int positionOrder = cashPosition.getNumberInt();
        DiscountBatchEntity discountBatchEntity = new DiscountBatchEntity();
        discountBatchEntity.setDiscountValue(discountValue);

        if (loyTx != null && loyTx.getDiscountPositions() != null) {
            loyTx.getDiscountPositions().stream().filter(posEntity -> posEntity.getPositionOrder() == positionOrder &&
                    posEntity.getGoodCode().equals(productCode)).forEach(discountPositionEntity -> {

                DiscountPositionEntity discount = new DiscountPositionEntity();

                LoyAdvActionInPurchaseEntity advAction = discountPositionEntity.getAdvAction();
                if (advAction != null) {
                    discount.setActionName(advAction.getActionName());
                    discount.setActionDiscountType(advAction.getDiscountType());
                    discount.setAdvertisingActionGUID(advAction.getGuid());
                }
                discount.setValueCalculated(discountPositionEntity.getDiscountAmount());
                if (discountPositionEntity.isDiscountPurchase()) {
                    discount.setDescription(DiscountPurchaseEntity.class.getSimpleName());
                }
                discount.setCount(discountPositionEntity.getQnty());
                discount.setDiscountIdentifier(discountPositionEntity.getDiscountIdentifier());
                discount.setTxId(discountPositionEntity.getTxId());
                discount.setPan(discountPositionEntity.getPan());
                discount.setDiscountFullId(discountPositionEntity.getDiscountFullId());
                discount.setOriginalCountInSet(discountPositionEntity.getOriginalSetQnty());
                discount.setWreQnty(discountPositionEntity.getWreQnty());
                discount.setCardNumber(discountPositionEntity.getCardNumber());
                discount.setPositionalCouponNumber(discountPositionEntity.getPositionalCouponNumber());
                discount.setPositionalCoupon(discountPositionEntity.getPositionalCoupon());
                discount.setDiscountSource(discountPositionEntity.getDiscountSource());
                discount.setTxId(discountPositionEntity.getTxId());
                discount.setType(discountPositionEntity.getDiscountType());

                discountBatchEntity.getDiscounts().add(discount);
            });
        }
        return discountBatchEntity;
    }

    public static Purchase convertPurchase(PurchaseEntity cashPurchase, TechProcessServiceAsync techProcess,
                                           TechProcessInterface tpi, Loyal loyal, TechProcessEvents techProcessEvents,
                                           CatalogService catalogService) {
        return convertPurchase(cashPurchase, techProcess, tpi, loyal, techProcessEvents, catalogService, false);
    }

    /**
     * К позициям указанного чека прикрепит {@link Position#getPositionalCouponNumbers() информацию} о "наклеенных" на них позиционных купонах.
     *
     * @param purchase чек
     */
    private static void attachPositionalCoupons(Purchase purchase) {
        if (purchase == null || purchase.getCardsInfo().isEmpty()) {
            // нету купонов
            return;
        }
        // позиционные купоны:

        // Количество товара. на которое уже "наклеили" позиционный купон. ключ - номер позиции, значение - количество. в граммах:
        Map<Long, Long> alreadyAttached = new HashMap<>();
        for (CardInfo ci : purchase.getCardsInfo()) {
            if (ci == null || ci.getCardType() == null || !(ci.getCardType() instanceof CouponsEntity)) {
                // это не купон
                continue;
            }
            if (ci.getMarking() == null || ci.getOriginalPrice() == null || ci.getQuantity() == null) {
                // это купон. но не позиционный
                continue;
            }
            // нашли позиционный купон

            for (Position pos : purchase.getPositions()) {
                if (pos == null || pos.getGoods() == null || !ci.getMarking().equals(pos.getGoodsItem()) || !ci.getOriginalPrice().equals(pos.getOriginalCost())) {
                    continue;
                }

                // количество товара в этой позиции, на которое уже наклеили позиционных купонов:
                long used = alreadyAttached.get(pos.getNumber()) == null ? 0 : alreadyAttached.get(pos.getNumber());
                long left = pos.getCount() - used;
                if (ci.getQuantity() > left) {
                    // НЕТ!
                    continue;
                }

                if (ProductType.WEIGHT.equals(pos.getGoods().getProductType().getType())) {
                    // весовой - нужно ТОЧНОЕ совпадение количества в позиции и в купоне
                    if (used == 0 && ci.getQuantity().equals(pos.getCount())) {
                        alreadyAttached.put(pos.getNumber(), used + ci.getQuantity());
                        pos.getPositionalCouponNumbers().add(ci.getCardId());
                    }
                } else {
                    // штучный
                    alreadyAttached.put(pos.getNumber(), used + ci.getQuantity());
                    pos.getPositionalCouponNumbers().add(ci.getCardId());
                }
            } // for pos
        } // for ci
    }

    private static CardInfo convertCard(PurchaseCardsEntity purchaseCard, Map<Long, Collection<Long>> positionToPositionalCoupons) {
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardId(purchaseCard.getId());
        cardInfo.setCardNumber(purchaseCard.getNumber());
        cardInfo.setCardType(purchaseCard.getCardType());
        cardInfo.setCardTypeEnum(purchaseCard.getType());
        cardInfo.setInputType(purchaseCard.getAddedBy() == null ? null : purchaseCard.getAddedBy().getInsertType());
        cardInfo.setProcessingName(purchaseCard.getProcessingName());
        if (purchaseCard.getPosition() != null) {
            //Это позиционный купон. Заполним для карты originalPrice и прилепим к позиции.
            fillPositionalCouponFields(purchaseCard, positionToPositionalCoupons, cardInfo);
        }

        if (cardInfo.getCardType() == null && purchaseCard.getType() == CardTypes.InternalCard) {
            cardInfo.setCardType(CardTypeEntity.getFakeInstanceByType(CardTypes.InternalCard, null));
        }

        //если внутреняя - надо еще и оптовые ограничения добавить
        if (purchaseCard.getCardType() instanceof InternalCardsEntity) {
            cardInfo.setWholesaleRestrictions(((InternalCardsEntity) purchaseCard.getCardType()).getWholesaleRestrictions());
        }

        if (purchaseCard.getExtendedAttributes() != null) {
            for (PurchaseCardExtDataEntity pced : purchaseCard.getExtendedAttributes().values()) {
                cardInfo.getExtendedAttributes().put(pced.getKey(), pced.getValue());
            }
        }
        return cardInfo;
    }


    public static Purchase convertPurchase(PurchaseEntity cashPurchase, TechProcessServiceAsync techProcess,
                                           TechProcessInterface tpi, Loyal loyal, TechProcessEvents techProcessEvents,
                                           CatalogService catalogService, boolean withDisount) {
        return convertPurchase(cashPurchase, techProcess, tpi, loyal, techProcessEvents, catalogService, withDisount, false);
    }

    public static Purchase convertPurchase(PurchaseEntity cashPurchase, TechProcessServiceAsync techProcess,
                                           TechProcessInterface tpi, Loyal loyal, TechProcessEvents techProcessEvents,
                                           CatalogService catalogService, boolean withDisount, boolean withBonus) {
        Purchase purchase = new Purchase();
        //Тут будем хранить привязки идентификаторов позиции к номерам купонов, которые к ней применили
        Map<Long, Collection<Long>> positionToPositionalCoupons = new HashMap<>();
        //convert cards and cardTypes
        if (cashPurchase.getCards() != null) {
            for (PurchaseCardsEntity purchaseCard : cashPurchase.getCards()) {
                purchase.getCardsInfo().add(convertCard(purchaseCard, positionToPositionalCoupons));
            }
        }

        purchase.setCheckSum(withDisount ? cashPurchase.getCheckSumEnd() : cashPurchase.getCheckSumStart());
        purchase.setCommited(cashPurchase.getCommited());

        purchase.setCurrentCheckNum(cashPurchase.getCurrentCheckNum());
        purchase.setDateCommit(cashPurchase.getDateCommit());
        purchase.setDateCreate(cashPurchase.getDateCreate());
        purchase.setDeferred(cashPurchase.getDeferred());
        purchase.setDiscountValueTotal(cashPurchase.getDiscountValueTotal());
        purchase.setInn(tpi.getExpectedInn(cashPurchase));
        if (cashPurchase.getNumber() != null) {
            purchase.setNumber(cashPurchase.getNumber());
        } else {
            purchase.setNumber(tpi.getExpectedDocNum(purchase.getInn()));
        }
        purchase.setOperationType(cashPurchase.getOperationType());
        purchase.setPaymentType(Optional.ofNullable(cashPurchase.getPaymentType()).map(PaymentType::getPaymentType).orElse("CashPaymentEntity"));

        // convert payments
        List<PaymentEntity> cashPayments = cashPurchase.getPayments();
        if (cashPayments != null) {
            List<Payment> payments = new ArrayList<>();
            for (PaymentEntity entity : cashPayments) {
                payments.add(convertPayment(entity, purchase));
            }
            purchase.setPayments(payments);
        }
        if (cashPurchase.getPaymentSuspensionData() != null) {
            Payment payment = new Payment();
            payment.setPaymentType(cashPurchase.getPaymentSuspensionData().getPaymentType());
            payment.setSumPay(cashPurchase.getPaymentSuspensionData().getPaymentSum());
            payment.getProperties().putAll(cashPurchase.getPaymentSuspensionData().getAttributes());
            purchase.getPayments().add(payment);
        }

        List<PositionEntity> cashPositions = cashPurchase.getPositions();
        purchase.setPosCounter(cashPositions.size());

        //CAUTION! do not remove, convertPosition will be break down
        LoyalProductsConverter.findLoyalProductsForPurchase(cashPurchase, catalogService);

        LoyTransactionEntity loyTransaction = null;
        if ((withDisount || cashPurchase.getLoyalTransactionId() != null) && loyal != null) {
            loyTransaction = loyal.findLoyTransaction(cashPurchase);
            // Пускай лежит в чеке, чтобы снова не дёргать
            cashPurchase.setLoyTransaction(loyTransaction);
        }
        // convert positions
        List<Position> positions = new ArrayList<>();
        long time = System.currentTimeMillis();
        Map<Long, Long> siebelPositionsMinPrices = getSiebelPositionsMinPrices(cashPurchase);
        for (PositionEntity entity : cashPurchase.getPositions()) {
            positions.add(convertPosition(entity, purchase, positionToPositionalCoupons, techProcess, techProcessEvents,
                    withDisount, withBonus, loyTransaction, siebelPositionsMinPrices));
        }

        // прикрепить позиционные купоны к позициям калькуляторного чека:
        attachPositionalCoupons(purchase);

        LOG.info("Time converting " + positions.size() + " positions = " + (System.currentTimeMillis() - time) + " ms");
        purchase.setPositions(positions);

        purchase.setQuestion("");
        purchase.setServiceDocs(convertServiceDocs(cashPurchase.getServiceDocs()));

        ShiftEntity shift = cashPurchase.getShift();
        if (shift == null) {
            shift = tpi.getShift();
        }
        purchase.setShift(convertShift(shift));

        if (withDisount && cashPurchase.getId() != null) {
            processDiscountPurchaseEntity(cashPurchase, purchase);
            processDiscountsAndBonuses(purchase, loyTransaction, withBonus);
            processLoyaltyServiceDocs(cashPurchase, purchase);
        } else if (loyTransaction != null) {
            processDiscountsAndBonuses(purchase, loyTransaction, withBonus);
        }

        // и на последок еще и ссылки на бонусные карты скопируем:
        // NOTE: калькулятор может (будет!) править "потолки" списания бонусов с баллансов этих карт -
        //  и эти изменения будут "видны" в самом оригинальном чеке (cashPurchase) - так и хотим
        purchase.setBonusDiscountCards(cashPurchase.getBonusDiscountCards());

        return purchase;
    }

    private static void processDiscountsAndBonuses(Purchase purchase, LoyTransactionEntity loyTransaction, boolean withBonus) {
        if (loyTransaction != null) {
            List<DiscountPurchaseEntity> discounts = new ArrayList<>();
            loyTransaction.getDiscountPositions().forEach(pos -> {
                DiscountPurchaseEntity d = new DiscountPurchaseEntity();
                d.setValue(pos.getDiscountAmount());
                d.setDiscountFullId(String.valueOf(pos.getPositionOrder()));
                LoyAdvActionInPurchaseEntity advAction = pos.getAdvAction();
                if (advAction != null) {
                    d.setAdvertisingActionGUID(advAction.getGuid());
                    d.setActionName(advAction.getActionName());
                    d.setActionType(advAction.getActionType());
                    d.setDescription(advAction.getActionName() + ";" + pos.getQnty());
                    d.setCardNumber(advAction.getExternalCode());
                }
                discounts.add(d);
            });
            purchase.setDiscounts(discounts);

            purchase.getSetApiLoyaltyTransactions().addAll(loyTransaction.getSetApiLoyaltyTransactions());
        }


        if (loyTransaction != null && withBonus) { // чтобы преобразовать например инфу о списании
            for (LoyBonusTransactionEntity loyBonusTx : loyTransaction.getBonusTransactions()) {
                BonusAdvertisingActionResultEntity result = new BonusAdvertisingActionResultEntity();
                result.setAccountTypeId(loyBonusTx.getBonusAccountType());
                result.setAccountName(loyBonusTx.getBonusAccountName());
                result.setAdvertisingActionGUID(loyBonusTx.getAdvAction().getGuid());
                result.setCardNumber(loyBonusTx.getDiscountCard());
                result.setValueBonus(loyBonusTx.getBonusAmount());
                result.setSumValue(loyBonusTx.getSumAmount());
                result.setPaymentSum(loyBonusTx.getPaymentSum());
                result.setDateStart(loyBonusTx.getBonusPeriodStart());
                result.setDateFinish(loyBonusTx.getBonusPeriodFinish());
                result.setTransactionId(loyBonusTx.getAuthCode());
                result.setSponsorId(loyBonusTx.getSponsorId());
                result.setMultiplier(loyBonusTx.getMultiplier());

                purchase.getAdvertisingActionResults().add(result);
            }

            for (LoyBonusPositionEntity loyBonusPos : loyTransaction.getBonusPositions()) {
                BonusPosition bonusPosition = new BonusPosition();
                bonusPosition.setPositionOrder(loyBonusPos.getPositionOrder());
                bonusPosition.setGoodsCode(loyBonusPos.getGoodCode());
                bonusPosition.setBonusAmount(loyBonusPos.getBonusAmount());
                bonusPosition.setQuantity(loyBonusPos.getQuantity());
                bonusPosition.setAdvertActGuid(loyBonusPos.getAdvAction().getGuid());
                bonusPosition.setAdvertActName(loyBonusPos.getAdvAction().getActionName());
                bonusPosition.setAdvertActType(loyBonusPos.getAdvAction().getActionType());

                purchase.getBonusPositions().add(bonusPosition);
            }

            for (LoyDiscountCardEntity discountCard : loyTransaction.getDiscountCards()) {
                AppliedActionInfo appliedActionInfo = new AppliedActionInfo();
                LoyAdvActionInPurchaseEntity advAction = discountCard.getAdvAction();
                if (advAction != null) {
                    appliedActionInfo.setActionGuid(advAction.getGuid());
                    appliedActionInfo.setActionName(advAction.getActionName());
                    appliedActionInfo.setActionType(advAction.getActionType());
                }
                CardTypes cardTypeEnum;
                try {
                    cardTypeEnum = CardTypes.valueOf(discountCard.getCardType());
                } catch (Exception e) {
                    cardTypeEnum = CardTypes.InternalCard;
                }
                CardInfo cardInfo = new CardInfo(discountCard.getCardNumber(), cardTypeEnum);
                appliedActionInfo.getAppliedCards().add(cardInfo);
                purchase.getAppliedActionsInfo().add(appliedActionInfo);
            }

            for (LoyProcessingCouponEntity src : loyTransaction.getProcessingCoupons()) {
                ProcessingCouponActionResultEntity dest = new ProcessingCouponActionResultEntity();
                LoyAdvActionInPurchaseEntity advAction = src.getAdvAction();
                if (advAction != null) {
                    dest.setAdvertisingActionGUID(advAction.getGuid());
                }
                dest.setCouponBarcode(src.getCouponBarcode());
                dest.setCouponPrefix(src.getCouponPrefix());
                dest.setDiscountAmount(src.getDiscountAmount());
                dest.setCardNumber(src.getCardNumber());
                dest.setMaxDiscount(src.getMaxDiscount());
                dest.setCouponPeriodFinish(src.getCouponPeriodFinish());
                dest.setCouponPeriodStart(src.getCouponPeriodStart());
                dest.setDiscountType(src.getDiscountType());
                dest.setUsed(src.isUsed());

                purchase.getAdvertisingActionResults().add(dest);
            }

            if (!loyTransaction.getQuestionaries().isEmpty()) {
                QuestionnaireAdvertisingActionResultEntity dest = new QuestionnaireAdvertisingActionResultEntity();
                dest.setQuestions(new ArrayList<>());
                for (LoyQuestionaryEntity src : loyTransaction.getQuestionaries()) {
                    LoyAdvActionInPurchaseEntity advAction = src.getAdvAction();
                    if (advAction != null) {
                        dest.setAdvertisingActionGUID(advAction.getGuid());
                    }

                    QuestionnaireQuestionEntity questionEntity = new QuestionnaireQuestionEntity();
                    if (src.getAnswerBoolean() != null) {
                        questionEntity.setAnswerType(AnswerType.YES_OR_NO);
                        questionEntity.setAnswer(String.valueOf(src.getAnswerBoolean()));
                    } else if (src.getAnswerNumber() != null) {
                        questionEntity.setAnswerType(AnswerType.INTEGER_VALUE);
                        questionEntity.setAnswer(String.valueOf(src.getAnswerNumber()));
                    }
                    questionEntity.setQuestionnaireAdvertisingActionResult(dest);
                    dest.getQuestions().add(questionEntity);

                    purchase.getAdvertisingActionResults().add(dest);
                }
            }

            for (LoyGiftNoteEnity src : loyTransaction.getGiftNotes()) {
                GiftNoteDescription dest = new GiftNoteDescription();
                LoyAdvActionInPurchaseEntity advAction = src.getAdvAction();
                if (advAction != null) {
                    dest.setAdvertisingActionGUID(advAction.getGuid());
                }
                dest.setTotalQnty(src.getTotalCount());
                dest.setRequireScan(src.isRequireScan());
                dest.setAddAsProducts(src.isAddAsProducts());
                dest.setBarcodes(src.getBarcodes());
                dest.setDisplayTime(src.getDisplayTime());
                dest.setCashierMessage(Lists.newArrayList(src.getCashierMessage()));
                dest.setCustomerMessage(Lists.newArrayList(src.getCustomerMessage()));
                for (LoyGiftNoteByConditionEnity note : src.getDetails()) {
                    GiftByConditionDescription child = new GiftByConditionDescription();
                    child.setCalculatorId(note.getConditionId());
                    child.setQnty(note.getCalculatedQnty());
                    dest.getDetails().add(child);
                }

                purchase.getAdvertisingActionResults().add(dest);
            }

            for (LoyChequeAdvertiseEntity src : loyTransaction.getChequeAdverts()) {
                if (src != null) {
                    ChequeAdvertiseActionResultEntity dest = new ChequeAdvertiseActionResultEntity();
                    LoyAdvActionInPurchaseEntity advAction = src.getAdvAction();
                    if (advAction != null) {
                        dest.setAdvertisingActionGUID(advAction.getGuid());
                    }
                    PrintObjectJsonUtil.getInstance().deserialisePrintObjectsFromJsonString(src.getText())
                            .ifPresent(dest::setPrintObjects);
                    purchase.getAdvertisingActionResults().add(dest);
                }
            }
        }
    }

    private static void processDiscountPurchaseEntity(PurchaseEntity cashPurchase, Purchase purchase) {
        if (cashPurchase.getDiscountPurchaseEntity() != null) {
            List<ChequeCouponEntity> chequeCoupons = cashPurchase.getDiscountPurchaseEntity().getChequeCoupons();
            if (chequeCoupons != null) {
                for (ChequeCouponEntity src : chequeCoupons) {
                    ChequeCouponAdvertisingActionResultEntity dest = new ChequeCouponAdvertisingActionResultEntity();
                    AdvActionInPurchaseEntity advAction = src.getAdvAction();
                    if (advAction != null) {
                        dest.setAdvertisingActionGUID(advAction.getId());
                    }
                    dest.setCouponBarcode(src.getCouponBarcode());
                    CouponTemplatedServiceDocument couponText = src.getCouponText();
                    if (couponText != null) {
                        dest.getPrintObjects().add(new TemplatedPrintObject(couponText.getTemplate(), couponText.getDataset()));
                        if (couponText.getBarcode() != null) {
                            PrintObjectType barcodeType = PrintObjectType.BARCODE;
                            if (couponText.getBarcode().getType() == BarCodeType.QR) {
                                barcodeType = PrintObjectType.QR;
                            }
                            dest.getPrintObjects().add(new PrintObject(barcodeType, couponText.getBarcode().getValue()));
                        }
                        dest.setSeparatePrint(couponText.isSeparatePrint());
                    }

                    dest.setCouponTypeGuid(src.getCouponTypeGuid());
                    dest.setPurchase(purchase);

                    purchase.getAdvertisingActionResults().add(dest);
                }
            }

            List<ChequeAdvertEntity> chequeAdverts = cashPurchase.getDiscountPurchaseEntity().getChequeAdverts();
            if (chequeAdverts != null) {
                for (ChequeAdvertEntity advertEntity : chequeAdverts) {
                    if (advertEntity == null || !advertEntity.isRestorableToPurchase()) {
                        continue;
                    }
                    AdvertisingActionResultEntity dest = new AdvertisingActionResultEntity();
                    AdvActionInPurchaseEntity advAction = advertEntity.getAdvAction();
                    if (advAction != null) {
                        dest.setAdvertisingActionGUID(advAction.getId());
                    }
                    dest.setPurchase(purchase);
                    TemplatedServiceDocument serviceText = (TemplatedServiceDocument) advertEntity.getChequeText();
                    serviceText.setPromo(true);
                    if (serviceText != null) {
                        dest.getPrintObjects().add(new TemplatedPrintObject(serviceText.getTemplate(), serviceText.getDataset()));
                        if (serviceText.getBarcode() != null) {
                            PrintObjectType barcodeType = PrintObjectType.BARCODE;
                            if (serviceText.getBarcode().getType() == BarCodeType.QR) {
                                barcodeType = PrintObjectType.QR;
                            }
                            dest.getPrintObjects().add(new PrintObject(barcodeType, serviceText.getBarcode().getValue()));
                        }
                        purchase.getAdvertisingActionResults().add(dest);
                        dest.setSeparatePrint(serviceText.isSeparatePrint());
                    }
                }
            }

            List<MessageEntity> messageEntities = cashPurchase.getDiscountPurchaseEntity().getMessages();
            if (messageEntities != null) {
                for (MessageEntity src : messageEntities) {
                    MessageAdvertisingActionResultEntity dest = new MessageAdvertisingActionResultEntity();

                    AdvActionInPurchaseEntity advAction = src.getAdvAction();
                    if (advAction != null) {
                        dest.setAdvertisingActionGUID(advAction.getId());
                    } else {
                        dest.setAdvertisingActionGUID(-1L);
                    }
                    dest.setDisplayTime(src.getDisplayTime());

                    if (src.getCashierMessage() != null) {
                        dest.getOperatorMsg().addAll(Arrays.asList(src.getCashierMessage().split(" \n")));
                    }
                    if (src.getCustomerMessage() != null) {
                        dest.getClientMsg().addAll(Arrays.asList(src.getCustomerMessage().split(" \n")));
                    }

                    dest.setPurchase(purchase);

                    purchase.getAdvertisingActionResults().add(dest);
                }
            }
        }
    }

    /**
     * Преобразование купонов/слипов внешних программ лояльности
     *
     * @param cashPurchase кассовый чек
     * @param purchase     чек лояльности
     */
    private static void processLoyaltyServiceDocs(PurchaseEntity cashPurchase, Purchase purchase) {
        cashPurchase.getServiceDocs().stream().filter(serviceDoc -> serviceDoc instanceof ExternalLoyaltyCheckMessageDocument)
                .forEach(serviceDoc -> {
                    ExternalLoyaltyCheckMessageActionResult messageResult = new ExternalLoyaltyCheckMessageActionResult();
                    for (Row row : ((SimpleServiceDocument) serviceDoc).getRows()) {
                        if (row instanceof Text) {
                            messageResult.getPrintObjects().add(new PrintObject(PrintObjectType.TEXT, row.getValue()));
                        }
                        if (row instanceof BarCode) {
                            messageResult.getPrintObjects().add(new PrintObject(PrintObjectType.BARCODE, row.getValue()));
                        }
                    }
                    messageResult.setSeparatePrint(serviceDoc.isSeparatePrint());
                    messageResult.setAdvertisingActionGUID(0L);
                    purchase.getAdvertisingActionResults().add(messageResult);
                });
    }

    /**
     * Заполняет поля позиционного купонинга для @param cardInfo
     *
     * @param purchaseCard                Оригинальный купон
     * @param positionToPositionalCoupons Связка - позиции / номера позиционных купонов
     * @param cardInfo                    Сущность инициализируемой карты в модуле лояльности
     */
    private static void fillPositionalCouponFields(PurchaseCardsEntity purchaseCard, Map<Long, Collection<Long>> positionToPositionalCoupons, CardInfo cardInfo) {
        Long positionId = purchaseCard.getPosition().getId();

        if (positionId != null) {
            //Позиция найдена проставляем значения
            cardInfo.setMarking(purchaseCard.getPosition().getItem());
            cardInfo.setQuantity(purchaseCard.getQuantity());
            cardInfo.setOriginalPrice(purchaseCard.getPosition().getPriceEnd());
            cardInfo.setQuantityForWeightProduct(purchaseCard.getPosition().getQnty());
            Collection<Long> coupons = positionToPositionalCoupons.get(positionId);
            if (coupons == null) {
                coupons = new ArrayList<>();
            }
            coupons.add(purchaseCard.getId());
            positionToPositionalCoupons.put(positionId, coupons);
        }

    }
}
