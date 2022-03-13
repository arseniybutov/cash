package ru.crystals.pos.loyal.cash.converter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.common.ClientType;
import ru.crystals.loyal.check.CardInfo;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionProductionDateEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.currency.RubCurrencyHandler;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Тестирование конвертации
 */
public class LoyalCashConverterTest {
    private static final long MAIN_PURCHASE_ID = 5544;
    private static final long MAIN_PURCHASE_NUMBER = 1;
    private static final long CLIENT_GUID = 324;
    private static final long BONUS_AMOUNT = 1024;
    private static final long BONUS_QUANTITY = 5L;
    private static final ClientType CLIENT_TYPE = ClientType.JURISTIC_PERSON;
    private static final boolean REPEAT_PURCHASE = true;
    private static final Date DATE_COMMIT = new Date(654234);
    private static final Date DATE_CREATE = new Date(786233);
    private static final SentToServerStatus SENT_TO_SERVER_STATUS = SentToServerStatus.UNCOMMITED;
    private static final String COLLAPSIBLE_PRODUCT_TYPE = "COLLAPSIBLE_PRODUCT_TYPE";

    private static final long FIRST_POSITION_ID = 1111;
    private static final String FIRST_POSITION_BARCODE = "FIRST_POSITION_BARCODE";
    private static final String FIRST_POSITION_NAME = "FIRST_POSITION_NAME";
    private static final long FIRST_POSITION_QUANTITY = 6000;
    private static final long FIRST_POSITION_PRICE_START = 2999;
    private static final long FIRST_POSITION_PRICE_END = 2098;
    private static final long FIRST_POSITION_DATE_CREATE = 3212246;
    private static final long FIRST_POSITION_DEPART_NUM = 1;


    /**
     * Проверим, что после разбиения позиции, ProductionDates - распределились
     */
    @Test
    public void testSplittingPositions() throws Exception {
        PurchaseEntity purchaseEntity = getPurchaseEntity();
        LoyalCashConverter.applyPurchasePositions(purchaseEntity, getPurchase(), new RubCurrencyHandler());
        List<PositionEntity> positions = purchaseEntity.getPositions();
        Assert.assertEquals(2, positions.size());
        for (PositionEntity onePosition : positions) {
            Assert.assertEquals(onePosition.getQnty(), Long.valueOf(onePosition.getProductionDates().stream().mapToLong(PositionProductionDateEntity::getQuantity).sum()));
            Assert.assertEquals(FIRST_POSITION_PRICE_END, onePosition.getSum().longValue());
        }
    }

    /**
     * Проверим, что после разбиения позиции, BonusPositions - распределились
     */
    @Test
    public void testSplittingBonuses() throws Exception {
        PurchaseEntity purchaseEntity = getPurchaseEntity();
        Purchase purchase = getPurchase();
        BonusPosition bonusPosition = new BonusPosition();
        bonusPosition.setBonusAmount(BONUS_AMOUNT);
        Position position = purchase.getPositions().iterator().next();
        bonusPosition.setPositionId(position.getId());
        bonusPosition.setPositionOrder(position.getNumber());
        bonusPosition.setGoodsCode(position.getGoodsItem());
        bonusPosition.setQuantity(BONUS_QUANTITY * BigDecimalConverter.getQuantityMultiplier());
        purchase.getBonusPositions().add(bonusPosition);
        LoyalCashConverter.applyPurchasePositions(purchaseEntity, purchase, new RubCurrencyHandler());
        List<BonusPosition> bonusPositions = purchase.getBonusPositions();
        Assert.assertEquals(purchase.getPositions().get(0).getCount(), bonusPositions.get(0).getQuantity().longValue());
        Assert.assertEquals(BONUS_QUANTITY * BigDecimalConverter.getQuantityMultiplier()
                - purchase.getPositions().get(0).getCount(), bonusPositions.get(1).getQuantity().longValue());
        Assert.assertEquals(BONUS_AMOUNT * 4 / BONUS_QUANTITY, bonusPositions.get(0).getBonusAmount());
        Assert.assertEquals(BONUS_AMOUNT - bonusPositions.get(0).getBonusAmount(), bonusPositions.get(1).getBonusAmount());
        Assert.assertEquals(2, bonusPositions.size());
        Assert.assertEquals(BONUS_AMOUNT, bonusPositions.stream().mapToLong(BonusPosition::getBonusAmount).sum());
    }

    /**
     * Проверим, что после расщепления позициям из калькуляторного чека корректно соотнеслись позиции из кассового чека.
     */
    @Test
    public void testSplittingsPositionsId() throws Exception {
        // given
        List<PositionEntity> cashPositions = new ArrayList<>();
        // Позиция, которая в калькуляторном чеке будет разделена на две
        PositionEntity cashPosition = new PositionEntity();
        cashPosition.setId(1L);
        cashPosition.setNumber(1L);
        cashPositions.add(cashPosition);
        // Обычная позиция
        cashPosition = new PositionEntity();
        cashPosition.setId(2L);
        cashPosition.setNumber(2L);
        cashPositions.add(cashPosition);

        List<Position> loyPositions = new LinkedList<>();
        // Отщепленная позиция оказалась перед родительской
        Position loyPosition = new Position();
        loyPosition.setId(3L);
        loyPosition.setNumber(1L);
        loyPosition.setParentId(1L);
        loyPositions.add(loyPosition);
        // Позиция, которая была расщеплена
        loyPosition = new Position();
        loyPosition.setId(1L);
        loyPosition.setNumber(1L);
        loyPosition.setParentId(null);
        loyPositions.add(loyPosition);
        // Обычная позиция
        loyPosition = new Position();
        loyPosition.setId(2L);
        loyPosition.setNumber(2L);
        loyPosition.setParentId(null);
        loyPositions.add(loyPosition);

        // when
        List<LoyalCashConverter.PairPositions> pairs = LoyalCashConverter.getAssociatedPositions(cashPositions, loyPositions);

        // then
        // Родительская позиция в калькуляторном чеке должна корректно смапиться на уже существующую позицию в кассовом чеке
        LoyalCashConverter.PairPositions pair = pairs.stream().filter(p -> p.position.getParentId() == null).findFirst().get();
        Assert.assertEquals(pair.getPosition().getId(), pair.getPositionEntity().getId());
        // Отщепленной позиции должна соответствовать новая клонированная позиция в кассовом чеке без id, который будет сгенерирован позже
        Assert.assertNull(pairs.stream().filter(p -> p.getPosition().getParentId() != null).findFirst().get().getPositionEntity().getId());
    }

    /**
     * Проверим, что на акцию на начисление бонусов из скидки по позиц. купону - купон гасится
     * @throws Exception ошибка
     */
    @Test
    public void testUsingPositionalCouponBonuses() throws Exception {
        Long couponNumber = 1234L;
        PurchaseEntity purchaseEntity = getPurchaseEntity();
        Purchase purchase = getPurchase();
        BonusPosition bonusPosition = new BonusPosition();
        bonusPosition.setBonusAmount(BONUS_AMOUNT);
        bonusPosition.getPositionalCouponNumbers().add(couponNumber);
        Position position = purchase.getPositions().iterator().next();
        bonusPosition.setPositionId(position.getId());
        bonusPosition.setPositionOrder(position.getNumber());
        bonusPosition.setGoodsCode(position.getGoodsItem());
        PurchaseCardsEntity positionCoupon = new PurchaseCardsEntity(String.valueOf(couponNumber), CardTypes.CardCoupon, purchaseEntity);
        positionCoupon.setId(couponNumber);
        positionCoupon.setPosition(purchaseEntity.getPositions().iterator().next());
        purchaseEntity.addCard(positionCoupon);
        CardInfo cardInfo = new CardInfo(String.valueOf(couponNumber), CardTypes.CardCoupon);
        cardInfo.setCardId(couponNumber);
        purchase.getCardsInfo().add(cardInfo);
        purchase.getBonusPositions().add(bonusPosition);
        LoyalCashConverter.applyPurchasePositions(purchaseEntity, purchase, new RubCurrencyHandler());
        Assert.assertEquals(0, purchaseEntity.getServiceDocs().size());
    }

    /**
     * Проверим, что внешний позиционный купон не участвует в проверке использования
     *
     * @throws Exception ошибка
     */
    @Test
    public void testUsingExternalPositionalCoupon() throws Exception {
        Long couponNumber = 1234L;
        PurchaseEntity purchaseEntity = getPurchaseEntity();
        Purchase purchase = getPurchase();
        PurchaseCardsEntity positionCoupon = new PurchaseCardsEntity(String.valueOf(couponNumber), CardTypes.ExternalCoupon, purchaseEntity);
        positionCoupon.setId(couponNumber);
        positionCoupon.setPosition(purchaseEntity.getPositions().iterator().next());
        purchaseEntity.addCard(positionCoupon);
        CardInfo cardInfo = new CardInfo(String.valueOf(couponNumber), CardTypes.ExternalCoupon);
        cardInfo.setCardId(couponNumber);
        purchase.getCardsInfo().add(cardInfo);
        LoyalCashConverter.applyPurchasePositions(purchaseEntity, purchase, new RubCurrencyHandler());
        Assert.assertEquals(0, purchaseEntity.getServiceDocs().size());
    }

    @Test
    public void testPrice() throws Exception {
        PurchaseEntity purchaseEntity = getPurchaseEntity();
        purchaseEntity.getPositions().clear();
        purchaseEntity.getPositions().add(createPosition(purchaseEntity, "13456", "13456", 331L, 17999L,
                17999L, 1L, new Date(FIRST_POSITION_DATE_CREATE), 1L, COLLAPSIBLE_PRODUCT_TYPE, FIRST_POSITION_DEPART_NUM));
        purchaseEntity.getPositions().add(createPosition(purchaseEntity, "2233", "2233", 210L, 10999L,
                10999L, 2L, new Date(FIRST_POSITION_DATE_CREATE), 2L, COLLAPSIBLE_PRODUCT_TYPE, FIRST_POSITION_DEPART_NUM));

        Purchase purchase = LoyalCalculatorConverter.convertPurchase(purchaseEntity, Mockito.mock(TechProcessServiceAsync.class),
                Mockito.mock(TechProcessInterface.class), Mockito.mock(Loyal.class), Mockito.mock(TechProcessEvents.class), Mockito.mock(CatalogService.class));
        purchase.getPositions().forEach(position -> position.setDiscountBatch(new DiscountBatchEntity()));
        LoyalCashConverter.applyPurchasePositions(purchaseEntity, purchase, new RubCurrencyHandler());

        purchaseEntity.getPositions().forEach(positionEntity -> Assert.assertEquals(positionEntity.getPriceStart(), positionEntity.getPriceEnd()));
    }

    private PurchaseEntity getPurchaseEntity() throws Exception {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(MAIN_PURCHASE_ID);
        purchaseEntity.setNumber(MAIN_PURCHASE_NUMBER);
        purchaseEntity.setOperationType(true);
        purchaseEntity.setClientGUID(CLIENT_GUID);
        purchaseEntity.setClientType(CLIENT_TYPE);
        purchaseEntity.setRepeatPurchase(REPEAT_PURCHASE);
        purchaseEntity.setDateCommit(DATE_COMMIT);
        purchaseEntity.setDateCreate(DATE_CREATE);
        purchaseEntity.setSentToServerStatus(SENT_TO_SERVER_STATUS);
        purchaseEntity.setDiscountValueTotal(1000L);
        purchaseEntity.setCheckSumStart(600 * 100L);
        PositionEntity position = createPosition(purchaseEntity, FIRST_POSITION_BARCODE, FIRST_POSITION_NAME, FIRST_POSITION_QUANTITY, FIRST_POSITION_PRICE_START,
                FIRST_POSITION_PRICE_END, FIRST_POSITION_ID, new Date(FIRST_POSITION_DATE_CREATE), 1L, COLLAPSIBLE_PRODUCT_TYPE, FIRST_POSITION_DEPART_NUM);

        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        PositionProductionDateEntity dateEntity = new PositionProductionDateEntity();
        dateEntity.setPosition(position);
        dateEntity.setQuantity(3000L);
        dateEntity.setProductionDate(sfd.parse("11.12.2016"));
        position.getProductionDates().add(dateEntity);
        dateEntity = new PositionProductionDateEntity();
        dateEntity.setPosition(position);
        dateEntity.setQuantity(3000L);
        dateEntity.setProductionDate(sfd.parse("10.11.2017"));
        position.getProductionDates().add(dateEntity);

        purchaseEntity.getPositions().add(position);
        return purchaseEntity;
    }

    private PositionEntity createPosition(PurchaseEntity check, String barcode, String name, Long quantity, Long priceStart,
                                          Long priceEnd, Long id, Date dateCreate, long number, String productType, Long depart) {
        PositionEntity pos = new PositionEntity();
        pos.setBarCode(barcode);
        pos.setName(name);
        pos.setQnty(quantity);
        pos.setPriceStart(priceStart);
        pos.setPriceEnd(priceEnd);
        pos.setSumDiscount(CurrencyUtil.getPositionSum(priceStart, quantity) - CurrencyUtil.getPositionSum(priceEnd, quantity));
        pos.setId(id);
        pos.setDateTime(dateCreate);
        pos.setPurchase(check);
        pos.setItem(name);
        pos.setNumber(number);
        pos.setInsertType(InsertType.SCANNER);
        pos.setNds(10.0f);
        pos.setSum(CurrencyUtil.getPositionSum(priceEnd, quantity));
        pos.setProductType(productType);
        pos.setDepartNumber(depart);
        return pos;
    }

    private Purchase getPurchase() {
        Purchase purchase = new Purchase();

        Position position = new Position();
        position.setId(FIRST_POSITION_ID);
        position.setGoods(getLoyProduct());
        position.setSum(FIRST_POSITION_PRICE_END);
        position.setCost(FIRST_POSITION_PRICE_END/FIRST_POSITION_QUANTITY);
        position.setOriginalCost(FIRST_POSITION_PRICE_START/FIRST_POSITION_QUANTITY);
        position.setCount(FIRST_POSITION_QUANTITY - 2000);
        position.setDiscountable(true);
        position.getGoods().setMarkingOfTheGood(FIRST_POSITION_BARCODE);

        DiscountBatchEntity dbe = new DiscountBatchEntity();
        dbe.setDiscounts(new ArrayList<>());

        DiscountPositionEntity dpe = new DiscountPositionEntity();
        dpe.setCount(1L);
        dpe.setDiscountBatch(dbe);
        dpe.setValue(FIRST_POSITION_PRICE_START - FIRST_POSITION_PRICE_END);

        dbe.getDiscounts().add(dpe);

        dbe.setDiscountValue(FIRST_POSITION_PRICE_START - FIRST_POSITION_PRICE_END);

        position.setDiscountBatch(dbe);

        Position position2 = position.cloneWithoutDisc();
        position2.setCount(2000);
        position2.setParentId(position.getId());
        purchase.getPositions().add(position);
        purchase.getPositions().add(position2);

        return purchase;
    }

    private LoyalProductEntity getLoyProduct() {
        LoyalProductType pt = new LoyalProductType();
        pt.setType(LoyalProductType.ProductType.PIECE);

        LoyalProductEntity lpe = new LoyalProductEntity();
        lpe.setPrecision(1.0);
        lpe.setProductType(pt);
        return lpe;
    }
}
