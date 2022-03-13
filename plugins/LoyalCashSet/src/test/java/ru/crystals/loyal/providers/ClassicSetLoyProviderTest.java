package ru.crystals.loyal.providers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.api.adapters.AdvertisingActionConditionRoutine;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.coupons.CouponsEntity;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.loyal.ExternalLoyaltyPurchaseConsumer;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.DoProcessing;
import ru.crystals.loyal.calculation.LoyTechProcess;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.api.plugin.AdvertisingActionConditionPlugin;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.cards.informix.CoBrandService;
import ru.crystals.pos.cards.informix.InformixService;
import ru.crystals.pos.cards.informix.InformixServiceConnectionException;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.check.discountresults.ChequeAdvertEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.RubCurrencyHandler;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CouponTemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.LongExtended;
import ru.crystals.pos.fiscalprinter.datastruct.documents.LoyReports;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.settings.PrintCheckSettings;
import ru.crystals.pos.loyal.cash.persistence.LoyFeedbackDao;
import ru.crystals.pos.loyalty.LoyaltyRestrictionsService;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.salemetrics.JmxMetrics;
import ru.crystalservice.setv6.discounts.plugins.DiscountActionResult;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Проверка корректировки значений бонусных баллов в фискальном чеке перед печатью для CoBrand.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ClassicSetLoyProvider.class, DoProcessing.class, JmxMetrics.class, ManagementFactory.class})
@PowerMockIgnore( {
        "org.apache.commons.logging.*",
        "javax.management.*",
})
public class ClassicSetLoyProviderTest {

    private static long CARD_OWNER_ID = 1;
    private static long BONUS_BALANCE = 100000;
    private static long REAL_ADDED_BONUS = 10000;

    private static String CARD_NUMBER = "780018724088";

    @InjectMocks
    private ClassicSetLoyProvider classicProvider = new ClassicSetLoyProvider();

    @Mock
    private InformixService informixService;

    @Spy
    private CurrencyHandler currencyHandler = new RubCurrencyHandler();

    @Mock
    private CoBrandService coBrandService;

    @Spy
    private Properties properties = new Properties();

    @Mock
    private CardsService cardsService;

    @Mock
    private LoyFeedbackDao loyFeedbackDao;

    @Spy
    private Set<ExternalLoyaltyPurchaseConsumer> externalLoyaltyPurchaseConsumers = new HashSet<>();

    @Mock
    private LoyaltyRestrictionsService restrictionsService;

    @Mock
    private AbstractPluginsAdapter pluginsAdapter;

//    @BeforeClass
//    public static void setup() {
//        PowerMockito.mockStatic(ManagementFactory.class);
//        PowerMockito.mockStatic(JmxMetrics.class);
//    }

    @Before
    public void setupLoyProvider() {
        when(coBrandService.isCoBrandConfigured()).thenReturn(true);
        AdvertisingActionConditionRoutine advertisingActionConditionRoutine = new AdvertisingActionConditionRoutine(pluginsAdapter);
        when(pluginsAdapter.getActionConditionRoutine()).thenReturn(advertisingActionConditionRoutine);
        externalLoyaltyPurchaseConsumers.add(mock(ExternalLoyaltyPurchaseConsumer.class));
    }

    /**
     * CoBrand начисляет предложенное количество бонусов.
     */
    @Test
    public void preparePurchaseFiscalization() throws Exception {
        when(coBrandService.addBonus(anyObject(), anyObject())).thenReturn(REAL_ADDED_BONUS);
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(generateBonusBalance(REAL_ADDED_BONUS));

        PurchaseEntity purchase = generatePurchaseEntity();
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000, 5000, 5000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(true, map.get("isdisccard"));
        assertEquals(CARD_NUMBER, map.get("internalcardnumber"));
        assertEquals(BigDecimalConverter.convertMoney(REAL_ADDED_BONUS), map.get("accruedbonuses"));
        assertEquals(new Long(BONUS_BALANCE + REAL_ADDED_BONUS), ((LongExtended) map.get("internalcardbalance")).getValue());
    }

    /**
     * CoBrand начисляет больше бонусов.
     */
    @Test
    public void preparePurchaseFiscalizationInformixRequiredMore() throws Exception {
        when(coBrandService.addBonus(anyObject(), anyObject())).thenReturn(REAL_ADDED_BONUS);
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(generateBonusBalance(REAL_ADDED_BONUS));

        PurchaseEntity purchase = generatePurchaseEntity();
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(BigDecimalConverter.convertMoney(REAL_ADDED_BONUS), map.get("accruedbonuses"));
        assertEquals(new Long(BONUS_BALANCE + REAL_ADDED_BONUS), ((LongExtended) map.get("internalcardbalance")).getValue());
    }

    /**
     * CoBrand начисляет меньше бонусов.
     */
    @Test
    public void preparePurchaseFiscalizationInformixRequiredLess() throws Exception {
        final long lost = 5000;

        when(coBrandService.addBonus(anyObject(), anyObject())).thenReturn(REAL_ADDED_BONUS - lost);
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(generateBonusBalance(REAL_ADDED_BONUS - lost));

        PurchaseEntity purchase = generatePurchaseEntity();
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000, 2000, 1000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(BigDecimalConverter.convertMoney(REAL_ADDED_BONUS - lost), map.get("accruedbonuses"));
        assertEquals(new Long(BONUS_BALANCE + REAL_ADDED_BONUS - lost), ((LongExtended) map.get("internalcardbalance")).getValue());
    }

    /**
     * От CoBrand не приходит новый бонусный баланс.
     */
    @Test
    public void preparePurchaseFiscalizationNoBonusFromInformix() throws Exception {
        when(coBrandService.addBonus(anyObject(), anyObject())).thenReturn(REAL_ADDED_BONUS);
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(null);

        PurchaseEntity purchase = generatePurchaseEntity();
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000, 6000, 7000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(true, map.get("isdisccard"));
        assertEquals(CARD_NUMBER, map.get("internalcardnumber"));
        assertEquals(BigDecimalConverter.convertMoney(REAL_ADDED_BONUS), map.get("accruedbonuses"));
        assertEquals((Object)0L, ((LongExtended) map.get("internalcardbalance")).getValue());
    }

    /**
     * От CoBrand не приходит новый бонусный баланс, но мы берем значение от сервера.
     */
    @Test
    public void preparePurchaseFiscalizationBonusOnlyFromServer() throws Exception {
        when(coBrandService.addBonus(anyObject(), anyObject())).thenReturn(REAL_ADDED_BONUS);
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(null);

        PurchaseEntity purchase = generatePurchaseEntity();
        purchase.getCards().get(0).getCardType().getCards().get(0).setCardBonusBalance(generateBonusBalance(0));
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000, 5000, 5000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(true, map.get("isdisccard"));
        assertEquals(CARD_NUMBER, map.get("internalcardnumber"));
        assertEquals(new Long(BONUS_BALANCE + REAL_ADDED_BONUS), ((LongExtended) map.get("internalcardbalance")).getValue());
        assertEquals(new Long(BONUS_BALANCE + REAL_ADDED_BONUS), ((LongExtended) map.get("internalcardbalance")).getValue());
    }

    /**
     * Нет связи с CoBrand.
     */
    @Test
    public void preparePurchaseFiscalizationWithoutConnection() throws Exception {
        when(coBrandService.addBonus(anyObject(), anyObject())).thenThrow(new InformixServiceConnectionException("Ошибка установки связи с Лента"));
        when(coBrandService.getBalance(CARD_OWNER_ID)).thenReturn(null);

        PurchaseEntity purchase = generatePurchaseEntity();
        LoyTransactionEntity loyTransaction = generateLoyTransactionEntity(5000);

        Map<String, Object> map = prepareFiscalizationAndReturnCheckMap(purchase, loyTransaction);

        assertEquals(true, map.get("isdisccard"));
        assertEquals(CARD_NUMBER, map.get("internalcardnumber"));
        assertEquals(BigDecimalConverter.convertMoney(0), map.get("accruedbonuses"));
        assertEquals((Object)0L, ((LongExtended) map.get("internalcardbalance")).getValue());
    }


    private Map<String, Object> prepareFiscalizationAndReturnCheckMap(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        Check check = new Check(purchase, new Cashier(), 0L, new PrintCheckSettings());
        check.setLoyReports(new LoyReports());

        classicProvider.preparePurchaseFiscalization(purchase, check, loyTransaction);

        return check.getMap();
    }

    @Test
    public void beforeFiscalizeTest() {
        // given
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setSale();
        purchase.setDiscountPurchaseEntity(new DiscountPurchaseEntity());

        ChequeAdvertEntity adv = new ChequeAdvertEntity();
        adv.setChequeText(new TemplatedServiceDocument(null, ImmutableMap.of("adv", "adv")));
        purchase.getDiscountPurchaseEntity().getChequeAdverts().add(adv);

        ChequeCouponEntity coupon = new ChequeCouponEntity();
        CouponTemplatedServiceDocument ctsd = new CouponTemplatedServiceDocument(null, ImmutableMap.of("cpn", "cpn"));
        coupon.setCouponText(ctsd);
        coupon.setCouponTypeGuid(22L);
        purchase.getDiscountPurchaseEntity().setChequeCoupons(Lists.newArrayList(coupon));
        // when
        CouponsEntity couponType = mock(CouponsEntity.class);
        when(cardsService.getCouponByGuid(eq(coupon.getCouponTypeGuid()))).thenReturn(couponType);
        classicProvider.beforeFiscalize(purchase);
        // проверка кейса SR-3212: множественная печать слипов после ошибок ФР
        classicProvider.beforeFiscalize(purchase);
        // then
        assertEquals(2, purchase.getServiceDocs().size());
        assertSame(coupon.getCouponText(), purchase.getServiceDocs().get(0));
        assertSame(adv.getChequeText(), purchase.getServiceDocs().get(1));
        assertSame(couponType, ctsd.getCouponType());
    }

    @Test
    public void processTest() {
        // given
        Purchase purchase = new Purchase();
        PurchaseEntity purchaseEntity = mock(PurchaseEntity.class);
        ILoyTechProcess techProcess = new LoyTechProcess();

        Purchase doProcessingResult = new Purchase();
        PowerMockito.mockStatic(DoProcessing.class);
        PowerMockito.when(DoProcessing.doDiscount(same(techProcess), any(AdvertisingActionEntity[].class), any(Purchase.class))).thenReturn(doProcessingResult);

        Collection<AdvertisingActionEntity> actions = Lists.newArrayList(mock(AdvertisingActionEntity.class));
        LoyActionsProvider actionsProvider = mock(LoyActionsProvider.class);
        when(actionsProvider.getActions(same(purchaseEntity))).thenReturn(actions);

        classicProvider.setActionsProvider(actionsProvider);

        // when
        Purchase result = classicProvider.process(purchase, purchaseEntity, techProcess);

        // then
        verify(actionsProvider, times(2)).getActions(same(purchaseEntity));
         assertSame(doProcessingResult, result);
    }

    @Test
    public void slaveWorkAnyTimeActionTest() throws Exception {
        final int cost = 300099;
        ILoyTechProcess techProcess = new LoyTechProcess();
        Purchase purchase = new Purchase();
        purchase.setOperationType(Purchase.OPERATION_TYPE_SALE);
        Position position = new Position();
        position.setId(1L);
        position.setNumber(1);
        position.setDiscountable(true);
        position.setCost(cost);
        position.setOriginalCost(position.getCost());
        position.setCount(BigDecimalConverter.getQuantityMultiplier());
        position.setSum(position.getCost());
        position.setDateTime(new Date());
        position.setDepartNumber(1L);
        position.setGoods(new LoyalProductEntity("12345"));
        purchase.getPositions().add(position);
        PurchaseEntity purchaseEntity = mock(PurchaseEntity.class);

        // Рекламная акция, суммирующаяся и зависимая
        AdvertisingActionEntity roundAction = new AdvertisingActionEntity();
        roundAction.setUseRestrictions(false);
        roundAction.setGuid(1L);
        DiscountActionResult actionResult = new DiscountActionResult(DiscountActionResult.ValueType.FIXSUMM, 1000L);
        Field f = DiscountActionResult.class.getDeclaredField("priceType");
        f.setAccessible(true);
        f.set(actionResult, DiscountActionResult.PriceType.UNDEFINED);

        f = DiscountActionResult.class.getDeclaredField("additionalValue");
        f.setAccessible(true);
        f.set(actionResult, "");
        roundAction.getDeserializedPlugins().add(actionResult);
        roundAction.setMode(ApplyMode.AUTOMATIC);
        roundAction.setWorksAnytime(true);
        roundAction.setMasterActionGuids(Collections.singleton(0L));

        List<AdvertisingActionEntity> actions = Collections.singletonList(roundAction);
        LoyActionsProvider actionsProvider = mock(LoyActionsProvider.class);
        when(actionsProvider.getActions(any(PurchaseEntity.class))).thenReturn(actions);
        when(actionsProvider.getActions(any(Purchase.class))).thenReturn(actions);

        classicProvider.setActionsProvider(actionsProvider);

        // Добавим скидку на позицию
        DiscountPositionEntity discount = new DiscountPositionEntity();
        discount.setActionType(ActionType.DISCOUNT_GOODS);
        discount.setAdvertisingActionGUID(0L);
        discount.setCount(position.getCount());
        position.addPositionDiscount(discount);

        // when
        Purchase result = classicProvider.process(purchase, purchaseEntity, techProcess);

        assertTrue("Зависимая акция не сработала", result.getAppliedActionsInfo().stream().anyMatch(aai -> 1L == aai.getActionGuid()));
    }

    @Test
    public void fixPricePositionTest() throws Exception {
        final int cost = 300099;
        ILoyTechProcess techProcess = new LoyTechProcess();
        Purchase purchase = new Purchase();
        purchase.setOperationType(Purchase.OPERATION_TYPE_SALE);
        Position position = new Position();
        position.setId(1L);
        position.setNumber(1);
        position.setDiscountable(true);
        position.setCost(cost);
        position.setOriginalCost(position.getCost());
        position.setCount(BigDecimalConverter.getQuantityMultiplier());
        position.setSum(position.getCost());
        position.setDateTime(new Date());
        position.setDepartNumber(1L);
        position.setGoods(new LoyalProductEntity("12345"));
        position.setFixedPrice(true);
        purchase.getPositions().add(position);
        PurchaseEntity purchaseEntity = mock(PurchaseEntity.class);

        // Рекламная акция, суммирующаяся и зависимая
        AdvertisingActionEntity roundAction = new AdvertisingActionEntity();
        roundAction.setUseRestrictions(false);
        roundAction.setGuid(1L);
        DiscountActionResult actionResult = new DiscountActionResult(DiscountActionResult.ValueType.FIXSUMM, 1000L);
        Field f = DiscountActionResult.class.getDeclaredField("priceType");
        f.setAccessible(true);
        f.set(actionResult, DiscountActionResult.PriceType.UNDEFINED);

        f = DiscountActionResult.class.getDeclaredField("additionalValue");
        f.setAccessible(true);
        f.set(actionResult, "");
        roundAction.getDeserializedPlugins().add(actionResult);
        roundAction.setMode(ApplyMode.AUTOMATIC);
        roundAction.setWorksAnytime(true);

        List<AdvertisingActionEntity> actions = Collections.singletonList(roundAction);
        LoyActionsProvider actionsProvider = mock(LoyActionsProvider.class);
        when(actionsProvider.getActions(any(PurchaseEntity.class))).thenReturn(actions);
        when(actionsProvider.getActions(any(Purchase.class))).thenReturn(actions);

        classicProvider.setActionsProvider(actionsProvider);

        // Добавим скидку на позицию
        DiscountPositionEntity discount = new DiscountPositionEntity();
        discount.setActionType(ActionType.DISCOUNT_GOODS);
        discount.setAdvertisingActionGUID(0L);
        discount.setCount(position.getCount());
        position.addPositionDiscount(discount);

        // when
        Purchase result = classicProvider.process(purchase, purchaseEntity, techProcess);

        assertFalse("Акция на товар с признаком fixprice=true сработала",
                result.getAppliedActionsInfo().stream().anyMatch(aai -> 1L == aai.getActionGuid()));
    }

    @Test
    public void purchaseFiscalizedTest() {
        // given
        PurchaseEntity purchaseEntity = generatePurchaseEntity();
        LoyTransactionEntity loyTx = mock(LoyTransactionEntity.class);

        InternalCashPoolExecutor executor = mock(InternalCashPoolExecutor.class);
        when(executor.submit(any(Runnable.class))).then(inv -> {
            ((Runnable) inv.getArguments()[0]).run();
            return null;
        });
        LoyActionsProvider actionsProvider = mock(LoyActionsProvider.class);
        classicProvider.setActionsProvider(actionsProvider);
        // when
        classicProvider.purchaseFiscalized(purchaseEntity, loyTx);

        // then
        verify(externalLoyaltyPurchaseConsumers.iterator().next()).consume(same(purchaseEntity), same(loyTx), any());
        verifyNoMoreInteractions(restrictionsService);

    }

    @Test
    public void onDiscountCalculationStartedTest() {
        // given

        String pluginId = "LOY_PLUGIN";
        Map<String, AdvertisingActionConditionPlugin> resolversMap = new HashMap<>();

        AdvertisingActionConditionPlugin resolver = Mockito.mock(AdvertisingActionConditionPlugin.class);
        Mockito.when(resolver.isConditionExecuted(Mockito.anyObject(), Mockito.anyObject())).thenReturn(true);

        resolversMap.put(pluginId, resolver);
        Mockito.when(pluginsAdapter.getPluginsOfClass(AdvertisingActionConditionPlugin.class)).thenAnswer(invocationOnMock -> resolversMap);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        Purchase purchase = new Purchase();

        // when
        LoyActionsProvider actionsProvider = mock(LoyActionsProvider.class);
        Mockito.when(actionsProvider.getActions(Mockito.any(PurchaseEntity.class))).thenReturn(new ArrayList<>());
        classicProvider.setActionsProvider(actionsProvider);
        classicProvider.onDiscountCalculationStarted(purchase, purchaseEntity, new LoyTechProcess());
        // эмулируем использование полученных обработчиков при проверке условия РА
        AdvertisingActionEntity action = Mockito.mock(AdvertisingActionEntity.class);
        Purchase calcPurchase = Mockito.mock(Purchase.class);
        boolean result = purchase.getExternalSystemsPredicates().get(pluginId).test(action, calcPurchase);

        // then
        assertEquals(true, result);
        Mockito.verify(resolversMap.get(pluginId)).isConditionExecuted(Mockito.any(), Mockito.any());

        verify(restrictionsService).checkRestrictions(same(purchaseEntity), anyCollectionOf(AdvertisingActionEntity.class));
    }

    private static CardBonusBalance generateBonusBalance(long added) {
        CardBonusBalance cardBonusBalance = new CardBonusBalance();
        cardBonusBalance.setBalance(BigDecimal.valueOf(BONUS_BALANCE + added));
        cardBonusBalance.setFinishSum(BigDecimal.valueOf(BONUS_BALANCE));
        return cardBonusBalance;
    }

    private static PurchaseEntity generatePurchaseEntity() {
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setClientGUID(CARD_OWNER_ID);
        PurchaseCardsEntity card = new PurchaseCardsEntity();
        card.setNumber(CARD_NUMBER);
        card.setType(CardTypes.InternalCard);
        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.getCards().add(new CardEntity());
        card.setCardType(cardType);
        purchase.addCard(card);
        ShiftEntity shift = new ShiftEntity();
        shift.setCashNum(0L);
        shift.setShopIndex(1000L);
        purchase.setShift(shift);
        return purchase;
    }

    private static LoyTransactionEntity generateLoyTransactionEntity(long... amounts) {
        LoyTransactionEntity loyTransaction = new LoyTransactionEntity();
        for (long amount: amounts) {
            loyTransaction.getBonusTransactions().add(newLoyBonusTransaction(amount));
        }
        return loyTransaction;
    }

    private static LoyBonusTransactionEntity newLoyBonusTransaction(long bonusAmount) {
        LoyBonusTransactionEntity result = new LoyBonusTransactionEntity();
        result.setDiscountCard(CARD_NUMBER);
        result.setSponsorId(BonusDiscountType.BONUS_INFORMIX);
        result.setBonusAmount(bonusAmount);
        return result;
    }

}