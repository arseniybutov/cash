package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.ClientEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionPluginEntity;
import ru.crystals.discounts.ActionPluginPropertyEntity;
import ru.crystals.discounts.ActionPluginType;
import ru.crystals.discounts.ActionType;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.discounts.ResultType;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.interfaces.ClientRestriction;
import ru.crystals.loyal.interfaces.IRestrictedActionPlugin;
import ru.crystals.loyal.interfaces.RestrictionPeriodType;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.loyal.providers.ClassicSetLoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistryImpl;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.RowDescriptor;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.cards.service.WholesaleRestrictionsDao;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.discountresults.AdvActionInPurchaseEntity;
import ru.crystals.pos.check.discountresults.DiscountPositionEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PositionCouponsReportDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.loyal.LoyaltyResults;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.loyalty.LoyaltyRestrictionsServiceCompositor;
import ru.crystals.pos.loyalty.PluggableLoyaltyRestrictionsService;
import ru.crystals.pos.loyalty.sls.SLSLoyaltyRestrictionsServiceImpl;
import ru.crystals.pos.payments.BankCardPaymentController;
import ru.crystals.pos.payments.CashPaymentController;
import ru.crystals.pos.payments.PaymentType;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.restrictions.LoyaltyRestrictionsHelper;
import ru.crystals.setretailx.cash.CashVO;
import ru.crystals.test.MockInjectors;
import ru.crystals.test.loyalty.actions.AdvertisingActionBuilder;
import ru.crystals.test.loyalty.actions.BonusActionResultBuilder;
import ru.crystals.test.loyalty.purchase.LoyalProductBuilder;
import ru.crystalservice.setv6.discounts.plugins.ActionResultsQuantityCondition;
import ru.crystalservice.setv6.discounts.plugins.BonusActionResult;
import ru.crystalservice.setv6.discounts.plugins.DefaultSetApplyObject;
import ru.crystalservice.setv6.discounts.plugins.DiscountActionResult;
import ru.crystalservice.setv6.discounts.plugins.IntervalType;
import ru.crystalservice.setv6.discounts.plugins.PaymentTypeCondition;
import ru.crystalservice.setv6.discounts.plugins.TimestampPeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

/**
 * Частичный перерасчет скидок
 */
public class LoyalServiceImplTest_partDiscount {
    private static final Long PRICE = 1000L;
    private static final long ROUND = 17L;
    private static final long OLD_LOY_TX_ID = 122L;

    private LoyalServiceImpl service;

    private CatalogService catalogService;

    private LoyProvidersRegistryImpl registry;

    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    private ClassicSetLoyProvider provider = new ClassicSetLoyProvider();
    private AdvActionsCacheImpl cache = spy(new AdvActionsCacheImpl());
    private SLSLoyaltyRestrictionsServiceImpl slsImpl;


    @Before
    public void setUp() throws FiscalPrinterException, NoSuchFieldException, IllegalAccessException {
        service = spy(new LoyalServiceImpl());
        service.setLoyTxDao(mock(LoyTxDao.class));
        service.setActionsDao(mock(CashAdvertisingActionDao.class));
        service.setWrDao(mock(WholesaleRestrictionsDao.class));
        service.setCheckService(mock(CheckService.class));
        service.setCache(cache);
        cache.setActionsDao(service.getActionsDao());

        LoyaltyPropertiesFactory loyaltyPropertiesFactory = mock(LoyaltyPropertiesFactory.class);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        doReturn(loyaltyProperties).when(service).getLoyaltyProperties();
        Mockito.when(loyaltyPropertiesFactory.get()).thenReturn(loyaltyProperties);

        service.start();

        LoyalProductsConverter.setUseCache(true);
        Whitebox.setInternalState(service, "currencyHandler", currencyHandler);

        catalogService = mock(CatalogService.class);
        Mockito.doReturn(Collections.emptyList()).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());
        Whitebox.setInternalState(service, "catalogService", catalogService);

        Mockito.doReturn(null).when(service).getDiscountsReport(Mockito.any());

        TechProcessInterface tpi = mock(TechProcessInterface.class);
        Mockito.doReturn(1L).when(tpi).getExpectedDocNum(Mockito.any());
        Mockito.doReturn(new CashVO()).when(tpi).getCashParams();
        Properties properties = new Properties();
        Mockito.doReturn(properties).when(tpi).getProperties();
        MockInjectors.inject(service, tpi, "tp");

        registry = new LoyProvidersRegistryImpl();
        registry.getProviders().add(provider);
        Whitebox.setInternalState(registry, "techProcess", tpi);
        Whitebox.setInternalState(registry, "catalogService", catalogService);
        AbstractPluginsAdapter pluginsAdapter = mock(AbstractPluginsAdapter.class);
        Whitebox.setInternalState(provider, "pluginsAdapter", pluginsAdapter);
        LoyProvidersRegistryWrapper loyProvidersWrapper = mock(LoyProvidersRegistryWrapper.class);
        doReturn(registry).when(loyProvidersWrapper).getLoyProviders();
        Whitebox.setInternalState(service, "loyProvidersWrapper", loyProvidersWrapper);
        Whitebox.setInternalState(provider, "currencyHandler", currencyHandler);


        slsImpl = spy(new SLSLoyaltyRestrictionsServiceImpl());
        doReturn(false).when(slsImpl).isEnabled();
        Whitebox.setInternalState(service, "restrictionsService", slsImpl);

        List<PluggableLoyaltyRestrictionsService> restrPlugins = new ArrayList<>();
        restrPlugins.add(slsImpl);
        LoyaltyRestrictionsServiceCompositor compositor = new LoyaltyRestrictionsServiceCompositor(restrPlugins);
        Whitebox.setInternalState(provider, "restrictionsService", compositor);

        Mockito.when(service.getLoyTxDao().saveLoyAdvAction(Mockito.any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        Mockito.when(service.getLoyTxDao().saveLoyTx(Mockito.any())).thenAnswer(invocationOnMock -> {
            LoyTransactionEntity loyTx = (LoyTransactionEntity) invocationOnMock.getArguments()[0];
            loyTx.setId(123L);
            return loyTx;
        });

        PropertiesManager propertiesManager = mock(PropertiesManager.class);
        Whitebox.setInternalState(service, "propertiesManager", propertiesManager);
        Whitebox.setInternalState(service, "loyaltyPropertiesFactory", loyaltyPropertiesFactory);

    }

    @Test
    public void testPartDiscountRound() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, BigDecimalConverter.getQuantityMultiplier(), PRICE, 1L, "1"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, BigDecimalConverter.getQuantityMultiplier(), PRICE + ROUND, 2L, "2"));
        PositionEntity couponPos = EntitiesTestUtils.createPosition(currencyHandler, 3L, BigDecimalConverter.getQuantityMultiplier(), PRICE, 3L, "3");
        positionEntities.add(couponPos);
        PurchaseEntity purchase = EntitiesTestUtils.createReceipt(positionEntities);

        PurchaseCardsEntity coupon = new PurchaseCardsEntity("11115", CardTypes.InternalCard, purchase);
        coupon.setId(5L);
        coupon.setPosition(couponPos);
        purchase.getCards().add(coupon);

        purchase.setLoyalTransactionId(OLD_LOY_TX_ID);

        LoyTransactionEntity oldLoyTx = new LoyTransactionEntity();
        oldLoyTx.setId(OLD_LOY_TX_ID);

        LoyDiscountPositionEntity dpe = EntitiesTestUtils.createDiscountPosition(couponPos, 1000L, 0L, 9L, ActionType.DISCOUNT, currencyHandler);
        dpe.setPositionalCouponNumber(coupon.getId());
        oldLoyTx.getDiscountPositions().add(dpe);

        Mockito.doReturn(oldLoyTx).when(service).findLoyTransaction(Mockito.eq(purchase));

        AdvertisingActionEntity actionEntity = createRoundAction();
        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{actionEntity};

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());

        service.doPartDiscount(purchase, actionEntity);

        LoyTransactionEntity loyTx = service.getLoyTransaction(purchase.getLoyalTransactionId());
        Assert.assertEquals(ROUND, loyTx.getDiscountValueTotal());
        Assert.assertEquals(ROUND, purchase.getDiscountValueTotal().longValue());

        // SR-2149: при частичном перерасчете при удалении тр. лояльности должны также удаляться потраченные опт. ограничения
        Mockito.verify(service.getWrDao()).removeRestrictionsByLoyTxId(Mockito.eq(OLD_LOY_TX_ID));

        // SR-2177: купон применен при обычном расчете, поэтому при частичном перерасчете не должно добавляться отчета о непримененных купонах
        assertTrue(purchase.getServiceDocs().stream().noneMatch(doc -> doc instanceof PositionCouponsReportDocument));
    }

    @Test
    public void testPartDiscountMutex() throws Exception {
        LoyalProductBuilder goodsBuilder = new LoyalProductBuilder();
        goodsBuilder.addMarkingOfTheGood("00001");
        goodsBuilder.addProductType(LoyalProductType.ProductType.WEIGHT);
        goodsBuilder.addPrecision(0.001);
        Mockito.doReturn(Lists.newArrayList(goodsBuilder.build())).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(6), 100_00L, 1L, "00001"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber("10001");

        CardBonusBalance balance = new CardBonusBalance(BigDecimal.valueOf(1_000_000));
        balance.setSponsorId(BonusDiscountType.BONUS_SR10);
        balance.setNotAppliedBonuses(100_00L);
        balance.setChargeOffAmount(100_00L);
        cardEntity.setCardBonusBalance(balance);

        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.getCards().add(cardEntity);

        PurchaseCardsEntity clientCard = new PurchaseCardsEntity();
        clientCard.setNumber("10001");
        clientCard.setCardTypeGUID(10001L);
        clientCard.setCardStatus(CardStatus.Active);
        clientCard.setType(CardTypes.InternalCard);
        clientCard.setCardType(cardType);
        originalPurchase.setCards(Collections.singletonList(clientCard));
        originalPurchase.getBonusDiscountCards().put(BonusDiscountType.BONUS_SR10, cardType.getCards());

        AdvertisingActionEntity bonusDiscountAction = AdvertiseActionTestUtils.createBonusDiscountAction(666L, 9999L, BonusDiscountType.BONUS_SR10);
        bonusDiscountAction.setWorksAnytime(true);
        AdvertisingActionEntity percentDiscountAction = AdvertiseActionTestUtils.createPercentDiscountAction(9L, 100L);
        percentDiscountAction.setExemptFromBonusDiscounts(true);
        percentDiscountAction.setWorksAnytime(false);
        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                percentDiscountAction, bonusDiscountAction
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        LoyaltyResults loyaltyResults = service.doDiscount(originalPurchase);

        Assert.assertFalse(loyaltyResults.getLoyalPurchase().getAppliedActionsInfo().stream().anyMatch(disc -> disc.getActionGuid() == 666L));
        Assert.assertFalse(originalPurchase.getDiscountPurchaseEntity().getDiscountPositions().stream().anyMatch(disc -> disc.getAdvAction().getId() == 666L));

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        Mockito.when(service.getLoyTxDao().getLoyTxByReceipt(Mockito.any())).thenReturn(loyTx);

        List<AdvertisingActionEntity> allActions = new ArrayList<>();
        allActions.add(percentDiscountAction);
        allActions.add(bonusDiscountAction);

        actions = new AdvertisingActionEntity[]{bonusDiscountAction};
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(allActions).when(cache).getActiveActions(Mockito.any(), Mockito.any());

        service.doPartDiscount(originalPurchase, actions);

        Assert.assertFalse(originalPurchase.getDiscountPurchaseEntity().getDiscountPositions().stream().anyMatch(disc -> disc.getAdvAction().getId() == 666L));
    }

    @Test
    public void testPartDiscountUndo() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        PositionEntity position = EntitiesTestUtils.createPosition(currencyHandler, 1L, BigDecimalConverter.getQuantityMultiplier(), PRICE + ROUND, 1L, "1");
        position.setSumDiscount(ROUND);
        positionEntities.add(position);
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, BigDecimalConverter.getQuantityMultiplier(), PRICE, 2L, "2"));
        PurchaseEntity purchase = EntitiesTestUtils.createReceipt(positionEntities);
        PaymentType paymentType = new BankCardPaymentController();
        purchase.setPaymentType(paymentType);
        purchase.setDiscountValueTotal(ROUND);
        DiscountPurchaseEntity discountPurchaseEntity = new DiscountPurchaseEntity();
        DiscountPositionEntity discountPositionEntity = new DiscountPositionEntity();

        AdvertisingActionEntity actionEntity = createRoundAction();
        AdvActionInPurchaseEntity advAction = new AdvActionInPurchaseEntity();
        advAction.setId(actionEntity.getGuid());
        advAction.setActionName(actionEntity.getName());

        discountPositionEntity.setAdvAction(advAction);
        discountPositionEntity.setDiscountAmount(ROUND);
        discountPositionEntity.setPositionOrder(1);
        discountPurchaseEntity.setDiscountPositions(Collections.singletonList(discountPositionEntity));
        purchase.setDiscountPurchaseEntity(discountPurchaseEntity);

        service.getLoyTechProcess().setAdvertisingActions(new AdvertisingActionEntity[]{actionEntity});
        Mockito.doReturn(Collections.singletonList(actionEntity)).when(cache).getActiveActions(Mockito.any(), Mockito.any());

        service.doPartDiscount(purchase, actionEntity);

        Assert.assertEquals(Long.valueOf(0), purchase.getDiscountValueTotal()); // скидку на округление отменили
        Assert.assertNull(purchase.getDiscountPurchaseEntity());
    }

    /**
     * Воспроизводит дефект SR-2314.
     * Делается полный расчет с акцией округления, а далее частичный перерасчет без акции округления.
     * В результате в чеке товар 116514 нарушал ФЗ-54.
     */
    @Test
    public void test_SR2314() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(2), 57_00L, 1L, "Квас"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(2), 3_20L, 2L, "Пакет"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(12), 69_00L, 3L, "116514"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 4L, TestUtils.convertCount(1.746), 129_00L, 4L, "Цыпа"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 5L, TestUtils.convertCount(1), 89_00L, 5L, "Зефир"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 6L, TestUtils.convertCount(1), 59_00L, 6L, "Драже"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 7L, TestUtils.convertCount(1), 259_00L, 7L, "Чай"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 8L, TestUtils.convertCount(2), 29_00L, 8L, "Мороженное Сладкое"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 9L, TestUtils.convertCount(3), 4_90L, 9L, "Бумага"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 10L, TestUtils.convertCount(2), 32_00L, 10L, "Мороженное Surfrele"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 11L, TestUtils.convertCount(10), 13_00L, 11L, "Приправа"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 12L, TestUtils.convertCount(3.698), 36_00L, 12L, "Сахар"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 13L, TestUtils.convertCount(1), 165_00L, 13L, "Колбаса"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 14L, TestUtils.convertCount(1), 39_00L, 14L, "Икра"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 15L, TestUtils.convertCount(2.514), 219_00L, 15L, "Шашлык"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 16L, TestUtils.convertCount(1), 5_90L, 16L, "Крышка"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 17L, TestUtils.convertCount(1.794), 299_00L, 17L, "Грудинка"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 18L, TestUtils.convertCount(1), 54_00L, 18L, "Штопор"));

        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity roundingAction = createRoundAction("10");

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createApplyObjectAction(false, 666L, "666L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.FIXPRICE, false, false, null,
                                new RowDescriptor(6, 61.00, false, "116514")
                        )
                ),
                roundingAction
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        PaymentType paymentType = new BankCardPaymentController();
        originalPurchase.setPaymentType(paymentType);

        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundingAction);

        checkByFL54(originalPurchase);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * В рамках дефекта SR-2314.
     * Весовой товар нарушал ФЗ-54.
     */
    @Test
    public void test_SR2314_and_weight_position() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(1.770), 49_54L, 1L, "22399"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity roundingAction = createRoundAction("10");

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createPercentDiscountAction(666L, 5153),
                roundingAction
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        PaymentType paymentType = new BankCardPaymentController();
        originalPurchase.setPaymentType(paymentType);

        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundingAction);

        checkByFL54(originalPurchase);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * В рамках дефекта SR-2314.
     * Двойная смена типа оплаты приводит к отрицательной сумме по позиции.
     */
    @Test
    public void test_SR2314_and_payment_type_changed_twice() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(6), 69_00L, 1L, "116514"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(4), 83_00L, 2L, "11592"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(18), 79_00L, 3L, "123517"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 4L, TestUtils.convertCount(18), 79_00L, 4L, "116313"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 5L, TestUtils.convertCount(1.916), 120_00L, 5L, "Kitekat"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 6L, TestUtils.convertCount(10), 20_00L, 6L, "Крупа"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 7L, TestUtils.convertCount(3), 86_00L, 7L, "Смак"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 8L, TestUtils.convertCount(20), 8_00L, 8L, "Соль"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 9L, TestUtils.convertCount(8), 37_00L, 9L, "Кукурузо"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity roundingAction = createRoundAction("10");

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createApplyObjectAction(false, 666L, "666L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.FIXPRICE, false, false, null,
                                new RowDescriptor(6, 69.00, false, "123517")
                        )
                ),
                AdvertiseActionTestUtils.createApplyObjectAction(false, 667L, "667L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.FIXPRICE, false, false, null,
                                new RowDescriptor(6, 69.00, false, "116313")
                        )
                ),
                AdvertiseActionTestUtils.createApplyObjectAction(false, 668L, "668L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.FIXPRICE, false, false, null,
                                new RowDescriptor(6, 61.00, false, "116514")
                        )
                ),
                AdvertiseActionTestUtils.createApplyObjectAction(false, 669L, "669L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.FIXPRICE, false, false, null,
                                new RowDescriptor(4, 73.00, false, "11592")
                        )
                ),
                roundingAction
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        PaymentType paymentType = new BankCardPaymentController();
        originalPurchase.setPaymentType(paymentType);

        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundingAction);

        // и еще раз
        LoyTransactionEntity loyTx2 = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());
        originalPurchase.setPaymentType(new CashPaymentController());
        Mockito.doReturn(loyTx2).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundingAction);

        checkByFL54(originalPurchase);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * CORE_500
     */
    @Test
    public void test_CORE_500_SR_2521() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(1), 131_10L, 1L, "00999"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity roundAction = createRoundAction();
        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                roundAction,
                AdvertiseActionTestUtils.createPercentDiscountAction(666L, 50_00L)
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        originalPurchase.setPaymentType(new CashPaymentController());

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundAction);

        PositionEntity pos = originalPurchase.getPositions().get(0);
        assertEquals(6500L, pos.getPriceEnd().longValue());
        assertEquals(6500L, pos.getSum().longValue());
        long actual = currencyHandler.getPositionSum(pos.getPriceEnd(), pos.getQnty());
        long expected = pos.getSum();
        assertFalse(Math.abs(actual - expected) > 1);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * SRTECH-557
     */
    @Test
    public void test_SRTECH_557()
             throws Exception {
        LoyalProductBuilder goodsBuilder = new LoyalProductBuilder();
        goodsBuilder.addMarkingOfTheGood("00001");
        goodsBuilder.addProductType(LoyalProductType.ProductType.WEIGHT);
        goodsBuilder.addPrecision(0.001);
        Mockito.doReturn(Lists.newArrayList(goodsBuilder.build())).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(6), 100_00L, 1L, "00001"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber("10001");

        CardBonusBalance balance = new CardBonusBalance(BigDecimal.valueOf(1_000_000));
        balance.setSponsorId(BonusDiscountType.BONUS_SR10);
        balance.setNotAppliedBonuses(100_00L);
        balance.setChargeOffAmount(100_00L);
        cardEntity.setCardBonusBalance(balance);

        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.getCards().add(cardEntity);

        PurchaseCardsEntity clientCard = new PurchaseCardsEntity();
        clientCard.setNumber("10001");
        clientCard.setCardTypeGUID(10001L);
        clientCard.setCardStatus(CardStatus.Active);
        clientCard.setType(CardTypes.InternalCard);
        clientCard.setCardType(cardType);
        originalPurchase.setCards(Collections.singletonList(clientCard));
        originalPurchase.getBonusDiscountCards().put(BonusDiscountType.BONUS_SR10, cardType.getCards());

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                AdvertiseActionTestUtils.createBonusDiscountAction(666L, 9999L, BonusDiscountType.BONUS_SR10)
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        originalPurchase.setPaymentType(new CashPaymentController());

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        Mockito.when(service.getLoyTxDao().getLoyTxByReceipt(Mockito.any())).thenReturn(loyTx);
        service.doPartDiscount(originalPurchase, actions);

        checkByFL54(originalPurchase);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    @Test
    public void test_SR_2758() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(3), 78_94L, 1L, "91763"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                AdvertiseActionTestUtils.createApplyObjectAction(false, 666L, "666L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.PERCENT, false, false, null,
                                new RowDescriptor(3, 33.33, false, "91763")
                        )
                ),
                AdvertiseActionTestUtils.createRoundingAction(665L, 100L)
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        persistPurchase(originalPurchase);
        Collection<Long> posIds = collectPositionsIds(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // для конкретного кейса необходимо чтобы акция не сработала
        actions = new AdvertisingActionEntity[] {
                new AdvertisingActionBuilder()
                        .addGuid(667L)
                        .addName("Charge on action")
                        .addWorksAnyTime(true)
                        .addInternalCardCategoryCondition(600L)
                        .addPercentDiscountActionResult(50L)
                        .build()
        };
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        Mockito.when(service.getLoyTxDao().getLoyTxByReceipt(Mockito.any())).thenReturn(loyTx);
        service.doPartDiscount(originalPurchase, actions);

        // все позиции, сохраненные в БД перед частичным перерасчетом, должны остаться и в его итоговом результате
        assertThat(collectPositionsIds(originalPurchase)).containsOnlyElementsOf(posIds).hasSize(posIds.size());
        assertEquals(2, originalPurchase.getPositions().size());

        checkByFL54(originalPurchase);
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    @Test
    public void test_SR_2902() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(20), 13_70L, 1L, "66625"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(72), 16_60L, 2L, "66626"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);
        originalPurchase.setPaymentType(new CashPaymentController());

        AdvertisingActionEntity roundAction = createRoundAction("10");
        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                AdvertiseActionTestUtils.createFixedDiscountAction(666L, 456_55L, false),
                roundAction
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        // имитация смены типа оплаты
        originalPurchase.setPaymentType(new BankCardPaymentController());

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(loyTx).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, roundAction);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        Mockito.verify(service, Mockito.times(2)).doSave(Mockito.any(), captor.capture(), Mockito.any(), Mockito.any());
        captor.getAllValues().forEach(TestUtils::validateCheck);

        checkByFL54(originalPurchase);
        assertEquals(1012_65L, originalPurchase.getCheckSumEnd().longValue());
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }


    @Test
    public void test_SLS_349() throws Exception {

        final Map<String, AdvertisingActionEntity> cacheLoyaltyRestrictionsService = new HashMap<>();
        reset(slsImpl);
        doReturn(true).when(slsImpl).isEnabled();

        doAnswer(invocation -> {
            Collection<AdvertisingActionEntity> argumentAt = (Collection<AdvertisingActionEntity>)invocation.getArgumentAt(1, Collection.class);
            for (Iterator<AdvertisingActionEntity> actionsIt = argumentAt.iterator(); actionsIt.hasNext(); ) {
                AdvertisingActionEntity action = actionsIt.next();
                if (LoyaltyRestrictionsHelper.hasRestrictionPlugin(action)) {
                    AdvertisingActionEntity clone = action.clone();
                    clone.getDeserializedPlugins().stream()
                            .filter(IRestrictedActionPlugin.class::isInstance)
                            .map(IRestrictedActionPlugin.class::cast)
                            .forEach(plugin -> plugin.setExternalRestriction(1));
                    cacheLoyaltyRestrictionsService.put(action.getExternalCode(), clone);
                    actionsIt.remove();
                }
            }

            argumentAt.addAll(cacheLoyaltyRestrictionsService.values());
            return null;

        }).when(slsImpl).checkRestrictions(any(), any());

        doAnswer(invocation -> {
            AdvertisingActionEntity[] argumentAt = invocation.getArgumentAt(1, AdvertisingActionEntity[].class);
            for (int i = 0; i < argumentAt.length; i++) {
                AdvertisingActionEntity advertisingActionEntity = argumentAt[i];
                AdvertisingActionEntity cached = cacheLoyaltyRestrictionsService.get(advertisingActionEntity.getExternalCode());
                if (cached != null) {
                    argumentAt[i] = cached;
                }
            }
            return null;

        }).when(slsImpl).partCheckRestrictions(any(), any());

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L,
                TestUtils.convertCount(1), 122_45L, 1L, "1"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber("10001");
        ClientEntity client = new ClientEntity();
        client.setGuid(1L);
        cardEntity.setClient(client);

        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.getCards().add(cardEntity);

        PurchaseCardsEntity clientCard = new PurchaseCardsEntity();
        clientCard.setNumber("10001");
        clientCard.setCardTypeGUID(10001L);
        clientCard.setCardStatus(CardStatus.Active);
        clientCard.setType(CardTypes.InternalCard);
        clientCard.setCardType(cardType);
        originalPurchase.setCards(Collections.singletonList(clientCard));
        originalPurchase.getBonusDiscountCards().put(BonusDiscountType.BONUS_INFORMIX, cardType.getCards());

        ActionResultsQuantityCondition actionResultsQuantityCondition = new ActionResultsQuantityCondition();
        ClientRestriction clientRestriction = new ClientRestriction();
        clientRestriction.setMaxValue(1);
        clientRestriction.setPeriodType(RestrictionPeriodType.MONTHS);
        clientRestriction.setPeriod(1);
        actionResultsQuantityCondition.setClientRestriction(clientRestriction);

        AdvertisingActionEntity actionWithRestrictionCondition = new AdvertisingActionBuilder()
                .addGuid(1L)
                .addExternalCode("23")
                .addConditionalPlugin(actionResultsQuantityCondition)
                .addActionResultPlugin(new BonusActionResultBuilder()
                        .addBonusType(BonusActionResult.BonusType.DISCBONUS)
                        .addIntervalType(IntervalType.ABSOLUTE)
                        .addExchengeRate(100L)
                        .addSponsorId(BonusDiscountType.BONUS_INFORMIX)
                        .addWorkPeriodRange(new TimestampPeriod(null, null))
                        .build())
                .build();

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                actionWithRestrictionCondition
        };

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        MockInjectors.injectField(registry, provider.getActionsProvider(), "actionsProvider");

        service.doDiscount(originalPurchase);

        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());
        Assert.assertFalse(loyTx.getBonusTransactions().isEmpty());

        service.getLoyTechProcess().setAdvertisingActions(actions);
        Mockito.doReturn(null).when(service).findLoyTransaction(Mockito.any());
        service.doPartDiscount(originalPurchase, actionWithRestrictionCondition);

        loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());
        Assert.assertFalse(loyTx.getBonusTransactions().isEmpty());
    }

    /**
     * Эмуляция сохранения чека в БД: проставляет id позиций
     */
    private void persistPurchase(PurchaseEntity purchase) {
        long id = purchase.getPositions().stream().mapToLong(p -> p.getId() == null ? 0L : p.getId()).max().orElse(0L);
        for (PositionEntity position : purchase.getPositions()) {
            if (position.getId() == null) {
                position.setId(++id);
            }
        }
    }

    private Set<Long> collectPositionsIds(PurchaseEntity purchase) {
        return purchase.getPositions().stream().map(PositionEntity::getId).collect(Collectors.toSet());
    }

    private void checkByFL54(PurchaseEntity purchase) {
        // проверяем чек на соответствие ФЗ-54
        for (PositionEntity pos: purchase.getPositions()) {
            assertTrue(pos.getPriceEnd() > 0);
            assertTrue(pos.getSum() > 0);
            long actual = currencyHandler.getPositionSum(pos.getPriceEnd(), pos.getQnty());
            long expected = pos.getSum();
            assertEquals("Problem position: " + pos.getNumberInt(), actual, expected);
        }
    }

    public static AdvertisingActionEntity createRoundAction() {
        return createRoundAction("100");
    }

    public static AdvertisingActionEntity createRoundAction(String value) {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        action.setGuid((long) RandomUtils.nextInt(Integer.MAX_VALUE - 1));
        action.setPlugins(new HashSet<>());

        // плагины
        ActionPluginEntity p = new ActionPluginEntity();
        p.setClassName("ru.crystalservice.setv6.discounts.plugins.PaymentTypeCondition");
        p.setType(ActionPluginType.CONDITIONAL_PLUGIN);
        p.setProperties(conditionProperties(p));
        action.getPlugins().add(p);

        p = new ActionPluginEntity();
        p.setClassName("ru.crystalservice.setv6.discounts.plugins.DiscountActionResult");
        p.setType(ActionPluginType.APPLY_OBJECT);
        p.setProperties(resultProperties(p, value));
        action.getPlugins().add(p);

        // всякие обязательные поля
        action.setName(RandomStringUtils.randomAlphabetic(10));
        action.setWorkPeriod(new TimestampPeriodEntity());
        action.getWorkPeriod().setStart(DateUtils.addDays(new Date(), -10));
        action.getWorkPeriod().setFinish(DateUtils.addDays(new Date(), 10));
        action.setDisplayStyleName(RandomStringUtils.randomAlphabetic(2));
        action.setMode(ApplyMode.AUTOMATIC);
        action.setWorksAnytime(true);
        action.setUseRestrictions(false);
        action.setActive(true);
        action.setResultTypes(new HashSet<ResultType>());
        action.setPriority(1000. + RandomUtils.nextDouble());
        action.setLastChanges(new Date());
        action.setAllNodes(true);
        action.setVersion(1);

        return action;
    }


    private static Set<ActionPluginPropertyEntity> conditionProperties(ActionPluginEntity plugin) {
        Set<ActionPluginPropertyEntity> set = new HashSet<ActionPluginPropertyEntity>();

        set.add(makeActionPluginPropertyEntity(PaymentTypeCondition.TypeOfSumComparison.class.getName(), "typeOfSumComparison",
                PaymentTypeCondition.TypeOfSumComparison.MORE.name()));
        set.add(makeActionPluginPropertyEntity(Long.class.getName(), "amount", "0"));
        set.add(makeActionPluginPropertyEntity(Boolean.class.getName(), "anyAmount", "true"));
        set.add(makeActionPluginPropertyEntity(String.class.getName(), "paymentType", "CashPaymentEntity"));

        return set;
    }

    private static Set<ActionPluginPropertyEntity> resultProperties(ActionPluginEntity plugin, String value) {
        Set<ActionPluginPropertyEntity> set = new HashSet<ActionPluginPropertyEntity>();

        set.add(makeActionPluginPropertyEntity(DiscountActionResult.ValueType.class.getName(), "valueType", DiscountActionResult.ValueType.ROUND.name()));
        set.add(makeActionPluginPropertyEntity(DiscountActionResult.PriceType.class.getName(), "priceType", DiscountActionResult.PriceType.UNDEFINED.name()));
        set.add(makeActionPluginPropertyEntity(Long.class.getName(), "value", value));
        set.add(makeActionPluginPropertyEntity(String.class.getName(), "additionalValue", ""));

        return set;
    }

    private static ActionPluginPropertyEntity makeActionPluginPropertyEntity(String className, String name, String value) {
        ActionPluginPropertyEntity entity = new ActionPluginPropertyEntity();
        entity.setClassName(className);
        entity.setName(name);
        entity.setValue(value);
        entity.setProperties(new HashSet<>());
        return entity;
    }

}
