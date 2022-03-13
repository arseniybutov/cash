package ru.crystals.pos.loyal.calculation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.loyal.calculation.DiscountCalculationUtils;
import ru.crystals.loyal.calculation.DoProcessing;
import ru.crystals.loyal.calculation.LoyPurchaseValidator;
import ru.crystals.loyal.calculation.PositionUtils;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.BonusAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.model.GoodsIDType;
import ru.crystals.loyal.product.LoyalMinPriceRestrictionEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.FixPriceDiscountDescriptor;
import ru.crystals.loyal.test.utils.PositionDescriptor;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.pos.catalog.ProductTags;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.test.loyalty.actions.AdvertisingActionBuilder;
import ru.crystals.test.loyalty.actions.BonusActionResultBuilder;
import ru.crystalservice.setv6.discounts.plugins.BonusActionResult;
import ru.crystalservice.setv6.discounts.plugins.CompositeApplyObject;
import ru.crystalservice.setv6.discounts.plugins.IntervalType;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Проверка LoyPurchaseValidator, только негативные сценарии:
 * - проверка МРЦ;
 * - проверка проверка неотрицательных цен, стоимостей, скидок и начислений бонусов;
 * - проверка скидки в более 100%
 * - проверка отсутствия скидок у нескидочного товара;
 * - проверка начисления бонусов на позиции, которым нельзя начислять бонусы
 * Позитивные внесены в соседние тесты
 * @see ru.crystals.pos.loyal.cash.service.LoyalServiceImplTest_doDiscount
 * @see ru.crystals.pos.loyal.cash.service.LoyalServiceImplTest_partDiscount
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LoyPurchaseValidator.class, LoggerFactory.class})
public class LoyPurchaseValidatorTest {
    private static Logger logger = mock(Logger.class);
    private Purchase purchase;
    private PurchaseEntity purchaseEntity;
    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    @BeforeClass
    public static void beforeClass() {
        mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
        when(LoggerFactory.getLogger(anyString())).thenReturn(logger);
    }

    @Before
    public void before() {
        String marking = "94832";
        purchase = TestUtils.createPurchase(new PositionDescriptor(marking, "Пряник клубничный сладкий",
                850.00, LoyalProductType.ProductType.PIECE, 2.0));

        purchaseEntity = new PurchaseEntity();
        PositionEntity positionEntity = new PositionEntity();
        positionEntity.setNumber(1L);
        positionEntity.setItem(marking);
        positionEntity.setDateTime(new Date());
        positionEntity.setPriceStart(85000L);
        positionEntity.setQnty(2L * BigDecimalConverter.getQuantityMultiplier());
        purchaseEntity.getPositions().add(positionEntity);
    }

    // проверка МРЦ
    @Test
    public void purchaseValidationMRCTest() {
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(800_00)); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        String testLog = "EndPrice=76500 below MinimalPrice=80000!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationOldMRCTest() {
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        LoyalMinPriceRestrictionEntity restriction = new LoyalMinPriceRestrictionEntity(800_00);
        restriction.setTillDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)));
        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(restriction); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationMRCNoDiscountTest() {
        AdvertisingActionEntity[] actionsArray = new AdvertisingActionEntity[0];
        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(1800_00)); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationMRCIgnoreTest() {
        AdvertisingActionEntity percentDiscountAction = AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000);
        percentDiscountAction.setUseRestrictions(false);
        Set<AdvertisingActionEntity> actions = Collections.singleton(percentDiscountAction);
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(800_00)); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationMRCIgnoreTest2() {
        String marking = "94832";
        Purchase purchase = TestUtils.createPurchase(new PositionDescriptor(marking, "Пряник клубничный сладкий",
                182.70, LoyalProductType.ProductType.WEIGHT, 0.598));
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        PositionEntity positionEntity = new PositionEntity();
        positionEntity.setItem(marking);
        positionEntity.setDateTime(new Date());
        positionEntity.setPriceStart(18270L);
        positionEntity.setQnty(2L * BigDecimalConverter.getQuantityMultiplier());
        purchaseEntity.getPositions().add(positionEntity);

        AdvertisingActionEntity percentDiscountAction = AdvertiseActionTestUtils.createFixPriceAction(1L, null,
                true, new FixPriceDiscountDescriptor(marking, 127_89));
        percentDiscountAction.setUseRestrictions(false);
        Set<AdvertisingActionEntity> actions = Collections.singleton(percentDiscountAction);
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(182_70)); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationMRCIgnoreTest3() {
        String marking = "94832";
        Purchase purchase = TestUtils.createPurchase(new PositionDescriptor(marking, "Пряник клубничный сладкий",
                0.45, LoyalProductType.ProductType.PIECE, 2));
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        PositionEntity positionEntity = new PositionEntity();
        positionEntity.setItem(marking);
        positionEntity.setDateTime(new Date());
        positionEntity.setPriceStart(18270L);
        positionEntity.setQnty(2L * BigDecimalConverter.getQuantityMultiplier());
        purchaseEntity.getPositions().add(positionEntity);

        AdvertisingActionEntity percentDiscountAction = AdvertiseActionTestUtils.createPercentDiscountAction(1L, 3000);
        percentDiscountAction.setUseRestrictions(false);
        Set<AdvertisingActionEntity> actions = Collections.singleton(percentDiscountAction);
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        ILoyTechProcess loyTechProcess = TestUtils.createLoyTechProcess(false, false);
        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(loyTechProcess, actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(45)); // min price

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                loyTechProcess, actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }


    @Test
    public void purchaseValidationUpperMRCIgnoreTest() {
        AdvertisingActionEntity percentDiscountAction = AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000);
        percentDiscountAction.setUseRestrictions(false);
        Set<AdvertisingActionEntity> actions = Collections.singleton(percentDiscountAction);
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchase.getPositions().get(0).getGoods().getSaleRestrictions().add(new LoyalMinPriceRestrictionEntity(1800_00)); // min price > original

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        Assert.assertTrue(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationAMRCTest() {
        String marking = "97895";
        purchase = TestUtils.createPurchase(new PositionDescriptor(marking, "Ром клубничный сладкий",
                850.00, LoyalProductType.ProductType.SPIRITS, 2.0));

        purchaseEntity = new PurchaseEntity();
        PositionSpiritsEntity positionEntity = new PositionSpiritsEntity();
        positionEntity.setItem(marking);
        positionEntity.setExciseToken("12345");
        positionEntity.setCategoryMask(positionEntity.getCategoryMask() | ProductTags.EXCISE_TAG_BITMASK);
        positionEntity.setPriceStart(85000L);
        positionEntity.setQnty(2L * BigDecimalConverter.getQuantityMultiplier());
        purchaseEntity.getPositions().add(positionEntity);
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));

        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);
        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        positionEntity = (PositionSpiritsEntity) purchaseEntity.getPositions().get(0);
        positionEntity.setAlcoMinPrice(800_00L);

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        String testLog = "EndPrice=76500 below MinimalPrice=80000!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    // проверка неотрицательных цен, стоимостей, скидок и начислений бонусов
    @Test
    public void purchaseValidationNegativeTest() {
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));

        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);
        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchaseWithDiscounts.getPositions().get(0).getDiscountBatch().setDiscountValue(-1); // negative discount

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        String testLog = "EndPrice=76500, Sum=153000 or Discount=-1 below zero!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationNegativeBonusTest() throws Exception {
        BonusPosition bonusPosition = new BonusPosition(purchase.getPositions().get(0), 123L);
        bonusPosition.setBonusAmount(-1);
        purchase.getBonusPositions().add(bonusPosition);

        BonusActionResultBuilder barb = new BonusActionResultBuilder();
        barb.addBonusType(BonusActionResult.BonusType.DISCBONUS);
        barb.addIntervalType(IntervalType.ABSOLUTE);
        barb.addExchengeRate(100L);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {new AdvertisingActionBuilder()
                .addCompositeApplyObject("1234", GoodsIDType.ITEM, CompositeApplyObject.ValueType.PERCENT, 5000L,
                        null, null, CompositeApplyObject.ApplyType.ALL)
                .addActionResultPlugin(barb.build())
                .addMode(ApplyMode.UNCONDITIONAL)
                .addGuid(123L)
                .build()};
        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchase, currencyHandler,
                getLoyTechProcess(), actions);

        String testLog = "Bonuses charged below zero!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertTrue(isValid);
        reset(logger);
    }

    // проверка скидки в более 100%
    @Test
    public void purchaseValidationOverflowTest() {
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));

        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);
        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchaseWithDiscounts.getPositions().get(0).getDiscountBatch().setDiscountValue(10000_00); // overflow discount

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        String testLog = "Discount=1000000 to position with marking=94832 overflow the positions sum=170000";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    // проверка отсутствия скидок у нескидочного товара
    @Test
    public void purchaseValidationDiscountableTest() {
        Set<AdvertisingActionEntity> actions = Collections.singleton(AdvertiseActionTestUtils.createPercentDiscountAction(1L, 1000));
        AdvertisingActionEntity[] actionsArray = actions.toArray(new AdvertisingActionEntity[0]);

        Purchase purchaseWithDiscounts = DoProcessing.doDiscount(getLoyTechProcess(), actionsArray, purchase);

        purchaseWithDiscounts.getPositions().get(0).setDiscountable(false); // not discountable

        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchaseWithDiscounts, currencyHandler,
                getLoyTechProcess(), actionsArray);

        String testLog = "Position not discountable, but have Discount=17000!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    // проверка списания бонусов на позиции, с которых нельзя списать бонусы
    @Test
    public void purchaseValidationBonusTest() {
        long bonusActionGuid = 123L;
        long bonusDiscount = 1L;
        Position position = purchase.getPositions().get(0);
        position.setBonusApplicable(false); // no bonuses
        DiscountPositionEntity discount = new DiscountPositionEntity();
        discount.setValueCalculated(bonusDiscount);
        discount.setAdvertisingActionGUID(bonusActionGuid);
        position.addPositionDiscount(discount);
        BonusAdvertisingActionResultEntity bonusDiscountResult = new BonusAdvertisingActionResultEntity();
        bonusDiscountResult.setSumValue(-bonusDiscount);
        bonusDiscountResult.setAdvertisingActionGUID(bonusActionGuid);
        purchase.getAdvertisingActionResults().add(bonusDiscountResult);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {AdvertiseActionTestUtils.createBonusDiscountAction(bonusActionGuid, 10000, BonusDiscountType.BONUS_SR10)};
        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchase, currencyHandler,
                getLoyTechProcess(), actions);

        String testLog = "Marking=94832 not applicable for bonuses, but have bonuses charged";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        Assert.assertTrue(captor.getValue().startsWith(testLog));
        Assert.assertFalse(isValid);
        reset(logger);
    }

    @Test
    public void purchaseValidationPriceTest() throws Exception {
        long decreasePrice = 400_00;
        Position position = purchase.getPositions().get(0);
        purchase.getPositions().add(position.cloneLight());
        purchase.getPositions().get(1).setNumber(2L);
        PositionEntity positionEntity = purchaseEntity.getPositions().get(0);
        purchaseEntity.getPositions().add(positionEntity.cloneLight());
        purchaseEntity.getPositions().get(1).setNumber(2L);
        // понизим цену у первой позиции
        position.setCost(decreasePrice);
        positionEntity.setPriceStart(decreasePrice);
        // на вторую позицию добавим скидку 100%
        purchase.getPositions().get(1).addPositionDiscount(
                DiscountCalculationUtils.createDiscountPosition(position.getCount(), 1L, position.getSum(), "", ""));
        PositionUtils.updatePosition(purchase.getPositions().get(1));

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {};
        boolean isValid = LoyPurchaseValidator.validatePurchase(purchaseEntity, purchase, currencyHandler,
                getLoyTechProcess(), actions);
        // все должно быть хорошо
        Assert.assertTrue(isValid);
        reset(logger);
    }

    /**
     * Настройки лояльности
     */
    private ILoyTechProcess loyTechProcess;
    private ILoyTechProcess getLoyTechProcess() {
        if (loyTechProcess == null) {
            loyTechProcess = TestUtils.createLoyTechProcess(false, true);
        }
        return loyTechProcess;
    }
}
