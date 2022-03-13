package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.model.GoodsIDType;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.loyal.providers.ClassicSetLoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistryImpl;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.ApplyObjectDiscountDescriptor;
import ru.crystals.loyal.test.utils.CompositeRowDescription;
import ru.crystals.loyal.test.utils.RowDescriptor;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.loyal.LoyaltyResults;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.setretailx.cash.CashVO;
import ru.crystals.test.loyalty.actions.AdvertisingActionBuilder;
import ru.crystals.test.loyalty.purchase.LoyalProductBuilder;
import ru.crystalservice.setv6.discounts.plugins.CompositeApplyObject;
import ru.crystalservice.setv6.discounts.plugins.CompositeRow;
import ru.crystalservice.setv6.discounts.plugins.DefaultSetApplyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Глобальный тест расчета скидок
 */
public class LoyalServiceImplTest_doDiscount {

    private LoyalServiceImpl service;

    private CatalogService catalogService;

    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    private ClassicSetLoyProvider provider = new ClassicSetLoyProvider();

    private LoyActionsProvider loyActionsProvider;

    @Before
    public void setUp() throws FiscalPrinterException {
        service = Mockito.spy(new LoyalServiceImpl());

        LoyaltyPropertiesFactory loyaltyPropertiesFactory = mock(LoyaltyPropertiesFactory.class);
        Whitebox.setInternalState(service, "loyaltyPropertiesFactory", loyaltyPropertiesFactory);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        Mockito.when(loyaltyPropertiesFactory.get()).thenReturn(loyaltyProperties);

        LoyalProductsConverter.setUseCache(true);
        Whitebox.setInternalState(service, "currencyHandler", currencyHandler);

        catalogService = mock(CatalogService.class);
        doReturn(Collections.emptyList()).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());
        Whitebox.setInternalState(service, "catalogService", catalogService);

        TechProcessInterface tpi = mock(TechProcessInterface.class);
        doReturn(1L).when(tpi).getExpectedDocNum(Mockito.any());
        doReturn(1L).when(tpi).getExpectedDocNum(any());

        CashVO cashVO = new CashVO();
        cashVO.setFactoryNum("123456789");
        doReturn(cashVO).when(tpi).getCashParams();

        Whitebox.setInternalState(service, "tp", tpi);

        loyActionsProvider = mock(LoyActionsProvider.class);

        LoyProvidersRegistryImpl registry = new LoyProvidersRegistryImpl();
        Whitebox.setInternalState(registry, "actionsProvider", loyActionsProvider);
        Whitebox.setInternalState(registry, "techProcess", tpi);
        Whitebox.setInternalState(registry, "catalogService", catalogService);
        Whitebox.setInternalState(registry, "currencyHandler", currencyHandler);
        registry.getProviders().add(provider);

        AbstractPluginsAdapter pluginsAdapter = mock(AbstractPluginsAdapter.class);
        Whitebox.setInternalState(provider, "pluginsAdapter", pluginsAdapter);
        Whitebox.setInternalState(provider, "currencyHandler", currencyHandler);

        LoyProvidersRegistryWrapper loyProvidersWrapper = mock(LoyProvidersRegistryWrapper.class);
        doReturn(registry).when(loyProvidersWrapper).getLoyProviders();
        Whitebox.setInternalState(service, "loyProvidersWrapper", loyProvidersWrapper);
        Whitebox.setInternalState(service, "propertiesManager", mock(PropertiesManager.class));

        doReturn(loyaltyProperties).when(service).getLoyaltyProperties();

        service.setLoyTxDao(mock(LoyTxDao.class));
        service.setActionsDao(mock(CashAdvertisingActionDao.class));
        service.setCheckService(mock(CheckService.class));

        service.start();
        service.getLoyTechProcess().getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);


        Mockito.when(service.getLoyTxDao().saveLoyAdvAction(Mockito.any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        Mockito.when(service.getLoyTxDao().saveLoyTx(Mockito.any())).thenAnswer(invocationOnMock -> {
            LoyTransactionEntity loyTx = (LoyTransactionEntity) invocationOnMock.getArguments()[0];
            loyTx.setId(123L);
            return loyTx;
        });
    }

    @Test
    public void test_SR2095() throws Exception {
        //given
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(2), 100_00L, 1L, "PIECE1"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(1), 150_00L, 2L, "PIECE2"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createApplyObjectAction(false, 66601L, "66601L",
                        new ApplyObjectDiscountDescriptor(Collections.singletonList(
                                new CompositeRowDescription(new ArrayList<>(Collections.singletonList("PIECE1")), GoodsIDType.ITEM)),
                                CompositeApplyObject.ValueType.PERCENT, 20, 1, null, null,
                                false, null, false)
                ),
                AdvertiseActionTestUtils.createApplyObjectAction(false, 66602L, "66602L",
                        new ApplyObjectDiscountDescriptor(Collections.singletonList(
                                new CompositeRowDescription(new ArrayList<>(Collections.singletonList("PIECE2")), GoodsIDType.ITEM)),
                                CompositeApplyObject.ValueType.PERCENT, 10, null, null, null,
                                false, null, false)
                )
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doDiscount(originalPurchase);

        //then
        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        for (PositionEntity position : originalPurchase.getPositions()) {
            long dpeSum = loyTx.getDiscountPositions().stream().filter(dpe -> dpe.getPositionOrder() == position.getNumberInt())
                    .mapToLong(LoyDiscountPositionEntity::getDiscountAmount).sum();
            assertEquals(position.getSumDiscount(), dpeSum);
        }
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
        verify(loyActionsProvider).onDiscountCalculationStarted(any(Purchase.class), anyMap());
    }

    @Test
    public void test_SR2126() throws Exception {
        //given
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(10), 96_20L, 1L, "PIECE1"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(10), 87_99L, 2L, "PIECE2"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createApplyObjectAction(false, 66617L, "66617L",
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.PERCENT, true, true, null,
                                new RowDescriptor(6, 16.67, false, "PIECE1")
                        ),
                        AdvertiseActionTestUtils.createSetApplyObject(0, null, DefaultSetApplyObject.ValueType.PERCENT, true, true, null,
                                new RowDescriptor(6, 16.67, false, "PIECE2")
                        )
                ),
                AdvertiseActionTestUtils.createSetAction(false, 66612L, "66612L", 0, null, DefaultSetApplyObject.ValueType.PERCENT, true, true, null,
                        new RowDescriptor(6, 16.67, false, "PIECE2", "PIECE1")
                )
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doDiscount(originalPurchase);

        //then
        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        for (PositionEntity position : originalPurchase.getPositions()) {
            long dpeSum = loyTx.getDiscountPositions().stream().filter(dpe -> dpe.getPositionOrder() == position.getNumberInt())
                    .mapToLong(LoyDiscountPositionEntity::getDiscountAmount).sum();
            assertEquals(position.getSumDiscount(), dpeSum);
        }

        assertEquals(0L, originalPurchase.getPositionAndCheckSumsDelta().longValue());
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * Учет АМРЦ при продаже алкоголя, самый простой сценарий.
     * Для задачи <a href="https://crystals.atlassian.net/browse/SR-2246">SR-2246</a>.
     */
    @Test
    public void alcoMinPriceTest() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createSpiritsPosition(
                currencyHandler, 1L, TestUtils.convertCount(1), 110_00L, 100_00,  1L, "Вино"));
        positionEntities.add(EntitiesTestUtils.createSpiritsPosition(
                currencyHandler, 2L, TestUtils.convertCount(1), 500_00L, 400_00L, 2L, "Водка"));
        positionEntities.add(EntitiesTestUtils.createSpiritsPosition(
                currencyHandler, 3L, TestUtils.convertCount(1), 400_00L, 300_00L, 3L, "Коньяк"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity ignoreRestrictionsAction = AdvertiseActionTestUtils.createPercentDiscountAction(666L, 50_00);
        ignoreRestrictionsAction.setUseRestrictions(false);
        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{ ignoreRestrictionsAction };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        service.doDiscount(originalPurchase);

        Function<String, Long> getPositionSum = name -> originalPurchase.getPositions().stream()
                .filter(p -> p.getItem().equals(name))
                .findFirst().orElseThrow(NoSuchElementException::new)
                .getSum();

        assertEquals((Long) 100_00L, getPositionSum.apply("Вино"));
        assertEquals((Long) 400_00L, getPositionSum.apply("Водка"));
        assertEquals((Long) 300_00L, getPositionSum.apply("Коньяк"));
    }

    @Test
    public void test_SR2384_1()
            throws Exception {
        // when
        final String[] markings = new String[] {"00001", "00002", "00003", "00004"};

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(2), 69_90L, 1L, "00001"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(2), 89_90L, 2L, "00002"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(3), 29_90L, 3L, "00003"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 4L, TestUtils.convertCount(2), 17_90L, 4L, "00004"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        // округление до 0.50
        AdvertisingActionEntity roundingAction = AdvertiseActionTestUtils.createRoundingAction(111L,50);
        roundingAction.setPriority(900.0);

        // набор за 3 бесплатно 1
        final long action31Guid = 10001L;
        AdvertisingActionEntity action31 = AdvertiseActionTestUtils.createSetAction(action31Guid, "Set 3+1", null, null, DefaultSetApplyObject.ValueType.PERCENT, false, false,
                new RowDescriptor(3, 0, true, markings),
                new RowDescriptor(1, 100, false, markings));
        action31.setWorksAnytime(true);
        action31.setPriority(1002.0);

        // набор со скидкой
        final long actionDiscountGuid = 10002L;
        AdvertisingActionEntity actionDiscount = AdvertiseActionTestUtils.createSetAction(actionDiscountGuid, "Set discount", null, null, DefaultSetApplyObject.ValueType.PERCENT, false, false,
                new RowDescriptor(1, 15, false, markings));
        actionDiscount.setWorksAnytime(true);
        actionDiscount.setUseRestrictions(true);
        actionDiscount.setPriority(1001.0);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {action31, actionDiscount, roundingAction};

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFairSets(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        // then
        LoyaltyResults loyaltyResults = service.doDiscount(originalPurchase);

        // given
        PurchaseEntity receipt = loyaltyResults.getReceipt();
        for (PositionEntity positionEntity : receipt.getPositions()) {
            assertTrue("Position " + positionEntity.getItem() + " with negative sum",positionEntity.getSum() >= 0);
        }
        Assert.assertEquals("Wrong check sum", Long.valueOf(30350L), receipt.getCheckSumEnd());
        Assert.assertEquals("Wrong total discount value", Long.valueOf(14160L), receipt.getDiscountValueTotal());
    }

    @Test
    public void test_SR2384_2()
            throws Exception {
        // when
        final String[] markings = new String[] {"00001", "00002", "00003", "00004", "00005", "00006"};

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(2), 89_90L, 1L, "00001"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(5), 5_90L, 2L, "00002"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(1), 29_90L, 3L, "00003"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 4L, TestUtils.convertCount(1), 199_90L, 4L, "00004"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 5L, TestUtils.convertCount(5), 29_90L, 5L, "00005"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 6L, TestUtils.convertCount(2), 49_90L, 6L, "00006"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        // округление до 1
        AdvertisingActionEntity roundingAction = AdvertiseActionTestUtils.createRoundingAction(111L,100);
        roundingAction.setPriority(900.0);

        // набор за 3 бесплатно 1
        final long action31Guid = 10001L;
        AdvertisingActionEntity action31 = AdvertiseActionTestUtils.createSetAction(action31Guid, "Set 3+1", null, null, DefaultSetApplyObject.ValueType.PERCENT, false, false,
                new RowDescriptor(3, 0, true, markings),
                new RowDescriptor(1, 100, false, markings));
        action31.setWorksAnytime(true);
        action31.setPriority(1002.0);

        // набор со скидкой
        final long actionDiscountGuid = 10002L;
        AdvertisingActionEntity actionDiscount = AdvertiseActionTestUtils.createSetAction(actionDiscountGuid, "Set discount", null, null, DefaultSetApplyObject.ValueType.PERCENT, false, false,
                new RowDescriptor(1, 15, false, markings));
        actionDiscount.setWorksAnytime(true);
        actionDiscount.setUseRestrictions(true);
        actionDiscount.setPriority(1001.0);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {action31, actionDiscount, roundingAction};

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFairSets(true);
        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        // then
        LoyaltyResults loyaltyResults = service.doDiscount(originalPurchase);

        // given
        PurchaseEntity receipt = loyaltyResults.getReceipt();
        for (PositionEntity positionEntity : receipt.getPositions()) {
            assertTrue("Position " + positionEntity.getItem() + " with negative sum",positionEntity.getSum() >= 0);
        }
        Assert.assertEquals("Wrong check sum", Long.valueOf(50700L), receipt.getCheckSumEnd());
        Assert.assertEquals("Wrong total discount value", Long.valueOf(18140L), receipt.getDiscountValueTotal());
        Assert.assertEquals(0L, originalPurchase.getPositionAndCheckSumsDelta().longValue());
    }

    @Test
    public void test_SR2505() throws Exception {
        //given
        LoyalProductBuilder goodsBuilder = new LoyalProductBuilder();
        goodsBuilder.addMarkingOfTheGood("13003");
        goodsBuilder.addProductType(LoyalProductType.ProductType.WEIGHT);
        goodsBuilder.addPrecision(0.001);
        doReturn(Lists.newArrayList(goodsBuilder.build())).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(27), 287_23L, 1L, "52002"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(18), 287_23L, 2L, "52002"));
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(5.356), 430_23L, 3L, "13003"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createPercentDiscountAction(666L, 7_00)
        };

        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doDiscount(originalPurchase);

        //then
        LoyTransactionEntity loyTx = service.getLoyTransaction(originalPurchase.getLoyalTransactionId());

        for (PositionEntity position : originalPurchase.getPositions()) {
            long dpeSum = loyTx.getDiscountPositions().stream().filter(dpe -> dpe.getPositionOrder() == position.getNumberInt())
                    .mapToLong(LoyDiscountPositionEntity::getDiscountAmount).sum();
            assertEquals(position.getSumDiscount(), dpeSum);
        }

        System.out.println(originalPurchase.getCheckSumEndBigDecimal());

        assertEquals(0L, originalPurchase.getPositionAndCheckSumsDelta().longValue());
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    /**
     * По мотивам <a href="https://crystals.atlassian.net/browse/SR-2500">SR-2500</a>.
     */
    @Test
    public void test_SR2500() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 1L, TestUtils.convertCount(0.774), 619_90L, 1L, "PH076680"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 2L, TestUtils.convertCount(9.620), 49_90L, 2L, "PH064942"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 3L, TestUtils.convertCount(2), 79_90L, 3L, "PH002678"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 4L, TestUtils.convertCount(9.650), 12_90L, 4L, "PH012049"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 5L, TestUtils.convertCount(1), 125_90L, 5L, "PH009495"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 6L, TestUtils.convertCount(1), 199_90L, 6L, "PH097793"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 7L, TestUtils.convertCount(0.426), 239_90L, 7L, "PH000675"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 8L, TestUtils.convertCount(0.852), 79_90L, 8L, "PH064984"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 9L, TestUtils.convertCount(1.394), 125_90L, 9L, "PH056438"));
        positionEntities.add(EntitiesTestUtils.createPosition(
                currencyHandler, 10L, TestUtils.convertCount(2), 85_90L, 10L, "PH010481"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createRoundingAction(666L, 100),
                AdvertiseActionTestUtils.createPercentDiscountAction(777L, 200)
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(false);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        Mockito.doAnswer(answerOfLoyalWeightGoods("PH076680", "PH064942", "PH012049", "PH000675", "PH064984", "PH056438"))
                .when(catalogService).getLoyalGoodsBatchByItems(Mockito.anyList());

        service.doDiscount(originalPurchase);

        // проверяем чек на соответствие ФЗ-54
        for (PositionEntity pos: originalPurchase.getPositions()) {
            Assert.assertTrue(pos.getPriceEnd() > 0);
            Assert.assertTrue(pos.getSum() > 0);
            long actual = currencyHandler.getPositionSum(pos.getPriceEnd(), pos.getQnty());
            long expected = pos.getSum();
            Assert.assertFalse(Math.abs(expected - actual) > 1);
        }
        Assert.assertTrue(CollectionUtils.isEmpty(originalPurchase.getDiscountPurchaseEntity().getMessages()));
    }

    @Test
    public void testDeleteLoyTransactionFromCache() {
        // создадим сначала то, что удалять будем
        LoyTransactionEntity tx = LoyalServiceTestUtils.buildLoyTx();
        long loyTxId = 100L;
        tx.setId(loyTxId);
        Whitebox.setInternalState(service, "cachedLoyTransaction", tx);

        boolean result = service.deleteLoyTransaction(loyTxId);
        Assert.assertTrue("Could not delete loy-tx with id = " + loyTxId, result);

        tx = service.searchDiscountResultsInDB(loyTxId, true);
        Assert.assertNull("Tx cached not deleted", tx);
    }

    private Answer<List<?>> answerOfLoyalWeightGoods(String... items) {
        return (InvocationOnMock invocation) -> {
            List<String> requestItems = (List<String>) invocation.getArguments()[0];
            return Arrays.stream(items)
                    .filter(requestItems::contains)
                    .map(item -> {
                        LoyalProductEntity lpe = new LoyalProductEntity();
                        lpe.setMarkingOfTheGood(item);
                        lpe.setPrecision(0.001);
                        lpe.setProductType(new LoyalProductType(LoyalProductType.ProductType.WEIGHT));
                        return lpe;
                    }).collect(Collectors.toList());
        };
    }

    @Test
    public void test_SR4695() throws Exception {
        //given
        PositionEntity p1 = EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(1), 89_90L, 1L, "PIECE1");
        p1.setCalculateDiscount(false);
        p1.setNds(12f);
        p1.getProduct().setSalesTax(2_00L);
        PositionEntity p2 = EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(1), 40_50L, 2L, "PIECE2");
        p2.setNds(12f);
        p2.getProduct().setSalesTax(1_00L);
        PositionEntity p3 = EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(1), 100_00L, 3L, "PIECE3");
        p3.setNds(12f);
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(Lists.newArrayList(p1, p2, p3));

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                AdvertiseActionTestUtils.createSalesTaxDiscountAction(66619L)
        };

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doDiscount(originalPurchase);

        //then
        Assert.assertEquals(1_58L, originalPurchase.getPositions().stream().filter(p -> p1.getItem().equals(p.getItem())).findAny().get().getSalesTaxSum());
        Assert.assertEquals(0L, originalPurchase.getPositions().stream().filter(p -> p2.getItem().equals(p.getItem())).findAny().get().getSalesTaxSum());
        Assert.assertEquals(0L, originalPurchase.getPositions().stream().filter(p -> p3.getItem().equals(p.getItem())).findAny().get().getSalesTaxSum());
    }

    @Test
    public void test_SR4928() throws Exception {
        //given
        String label = "LABEL";
        PositionEntity p1 = EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(1), 120_00L, 1L, "PIECE1");
        PositionEntity p2 = EntitiesTestUtils.createPosition(currencyHandler, 2L, TestUtils.convertCount(1), 100_00L, 2L, "PIECE2");
        PositionEntity p3 = EntitiesTestUtils.createPosition(currencyHandler, 3L, TestUtils.convertCount(1), 180_00L, 3L, "PIECE3");
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(Lists.newArrayList(p1, p2, p3));

        // 50 руб тем, кто не получил скидки от РА с меткой LABEL
        AdvertisingActionBuilder builder = new AdvertisingActionBuilder();
        builder.addGuid(234L).addName("Not for label 'LABEL'").addWorksAnyTime(true);
        CompositeApplyObject cao = new CompositeApplyObject();
        cao.setForAllGoods(true);
        cao.getActionLabelExclude().add(label);
        cao.setValueType(CompositeApplyObject.ValueType.FIXSUMM);
        cao.setValue(CurrencyUtil.convertMoneyToLong(50.0));
        builder.addApplyObjectPlugin(cao);
        AdvertisingActionEntity compositeAction = builder.build();
        builder.clean();

        // Акция с меткой LABEL - 20 руб на третью позицию
        builder.addGuid(123L).addName("Third position action with label 'LABEL'").addWorksAnyTime(false).addActionLabel(label);
        cao = new CompositeApplyObject();
        cao.getRows().add(new CompositeRow(new ArrayList<>(Collections.singletonList("PIECE3")), GoodsIDType.ITEM));
        cao.setValueType(CompositeApplyObject.ValueType.FIXSUMM);
        cao.setValue(CurrencyUtil.convertMoneyToLong(20.0));
        builder.addApplyObjectPlugin(cao);
        AdvertisingActionEntity actionForPosition3 = builder.build();

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                actionForPosition3, compositeAction
        };

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doDiscount(originalPurchase);

        //then
        Assert.assertEquals(50_00L, originalPurchase.getPositions().stream().filter(p -> p1.getItem().equals(p.getItem())).findAny().get().getSumDiscount());
        Assert.assertEquals(50_00L, originalPurchase.getPositions().stream().filter(p -> p2.getItem().equals(p.getItem())).findAny().get().getSumDiscount());
        Assert.assertEquals(20_00L, originalPurchase.getPositions().stream().filter(p -> p3.getItem().equals(p.getItem())).findAny().get().getSumDiscount());
    }
}
