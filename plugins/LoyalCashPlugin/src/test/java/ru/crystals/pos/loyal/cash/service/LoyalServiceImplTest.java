package ru.crystals.pos.loyal.cash.service;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardBonusSubBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internalcards.InternalCards;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.discounts.enums.GiftDisplayTime;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.calculation.LoyTechProcess;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.check.discount.GiftNoteDescription;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.model.LoyMetrics;
import ru.crystals.loyal.product.LoyalDepartEntity;
import ru.crystals.loyal.product.LoyalPriceEntity;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.providers.LoyProvidersRegistry;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.FixPriceDiscountDescriptor;
import ru.crystals.pos.cards.CardsEvent;
import ru.crystals.pos.cards.informix.CoBrandService;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.billets.PriceType;
import ru.crystals.pos.check.ManualAdvertisingActionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.gift.GiftCodeConsumer;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.SubtotalScenarioInterface;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.test.loyalty.actions.AdvertisingActionBuilder;
import ru.crystals.test.loyalty.purchase.CardBuilder;
import ru.crystals.test.loyalty.purchase.PurchaseBuilder;
import ru.crystalservice.setv6.discounts.common.GeneralInteractionMethod;
import ru.crystalservice.setv6.discounts.common.vo.ActionPriceInfo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты методов сервиса работы с транзакциями лояльности
 */
public class LoyalServiceImplTest {
    private static final long CARD_BALLANCE = 1234L; // балланс бонусов на карте, в копейках
    private static final String CARD_NUMBER = "123";

    private static TechProcessInterface tp;
    private static LoyalServiceImpl service;

    @BeforeClass
    public static void setUp() {
        service = spy(new LoyalServiceImpl());
        ILoyTechProcess loyTechProcess = getLoyTechProcess();
        doReturn(loyTechProcess.getLoyaltyProperties()).when(service).getLoyaltyProperties();
        doReturn(loyTechProcess).when(service).getLoyTechProcess();

        LoyProvidersRegistry loyReg = mock(LoyProvidersRegistry.class);
        service.setCardsEvent(mock(CardsEvent.class));
        service.setLoyTxDao(mock(LoyTxDao.class));
        service.setActionsDao(mock(CashAdvertisingActionDao.class));
        tp = mock(TechProcessInterface.class);
        doReturn(1L).when(tp).getExpectedDocNum(any());
        doNothing().when(loyReg).checkCanceled(any());
        Whitebox.setInternalState(service, "tp", tp);
        LoyProvidersRegistryWrapper loyProvidersWrapper = mock(LoyProvidersRegistryWrapper.class);
        doReturn(loyReg).when(loyProvidersWrapper).getLoyProviders();
        Whitebox.setInternalState(service, "loyProvidersWrapper", loyProvidersWrapper);
        PropertiesManager propertiesManager = mock(PropertiesManager.class);
        doReturn(true).when(propertiesManager).getBooleanProperty(eq(Loyal.MODULE_NAME), any(),
                eq(Whitebox.getInternalState(LoyalServiceImpl.class, "PART_DISCOUNT_ENABLE_PROPERTY")), anyBoolean());
        Whitebox.setInternalState(service, "propertiesManager", propertiesManager);
    }

    @Test
    public void testUpdateLoyTransactions() throws Exception {
        LoyTransactionEntity tx1 = mock(LoyTransactionEntity.class);
        LoyTransactionEntity tx2 = mock(LoyTransactionEntity.class);
        when(service.getLoyTxDao().saveLoyTx(same(tx1))).thenReturn(tx2);
        // when
        LoyTransactionEntity tx3 = service.updateLoyTransaction(tx1);
        // then
        Assert.assertSame(tx2, tx3);
    }

    @Test
    public void testConfirmDiscountSendsPrismaEvent() {
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setLoyalTransactionId(1L);
        LoyTransactionEntity tx = mock(LoyTransactionEntity.class);
        LoyTransactionEntity txWithBonuses = new LoyTransactionEntity();

        LoyBonusTransactionEntity b1 = new LoyBonusTransactionEntity();
        String card_1 = "Card_1";
        long bonusAmount_1 = 1000;
        b1.setDiscountCard(card_1);
        b1.setBonusAmount(bonusAmount_1);

        LoyBonusTransactionEntity b2 = new LoyBonusTransactionEntity();
        String card_2 = "Card_2";
        long bonusAmount_2 = 2000;
        b2.setDiscountCard(card_2);
        b2.setBonusAmount(bonusAmount_2);

        LoyBonusTransactionEntity b3 = new LoyBonusTransactionEntity();
        String card_3 = "Card_3";
        long bonusAmount_3 = -3000;
        b3.setDiscountCard(card_3);
        b3.setBonusAmount(bonusAmount_3);

        txWithBonuses.setBonusTransactions(Arrays.asList(b1,b2,b3));

        when(service.getLoyTxDao().getLoyTxById(eq(purchase.getLoyalTransactionId()))).thenReturn(tx);
        when(service.getLoyTxDao().linkTxToReceipt(tx, purchase)).thenReturn(txWithBonuses);

        service.confirmDiscount(purchase);
        verify(service.getCardsEvent()).eventChargeBonuses(card_1, bonusAmount_1);
        verify(service.getCardsEvent()).eventChargeBonuses(card_2, bonusAmount_2);
        verify(service.getCardsEvent(), never()).eventChargeBonuses(card_3, bonusAmount_3);
    }

    @Test
    public void testDeleteLoyTransaction() throws Exception {
        long txId = 9L;
        // when
        boolean result = service.deleteLoyTransaction(txId);
        // then
        ArgumentCaptor<Collection> idsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(service.getLoyTxDao()).removeLoyTxByIds(idsCaptor.capture());
        assertThat(idsCaptor.getValue()).containsOnlyElementsOf(Arrays.asList(txId)).hasSize(1);
        Assert.assertTrue(result);
    }

    @Test
    public void testAutoWriteOff() throws Exception {
        PurchaseBuilder purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder.addPosition().addPosition(); // две позиции
        CardBuilder cb = new CardBuilder(CardTypes.InternalCard).addNumber(CARD_NUMBER);
        cb.addBonusBalance(new CardBonusBalance(BigDecimal.TEN));
        cb.addBonusAccount(new CardBonusSubBalance(1L, null, 10_00L));
        purchaseBuilder.addCard(cb.build());
        Purchase purchase = purchaseBuilder.build();
        DiscountPositionEntity discount = new DiscountPositionEntity();
        discount.setCardNumber(CARD_NUMBER);
        discount.setValue(CARD_BALLANCE / 2);
        discount.setBonusDiscountAuto(true);
        purchase.getPositions().get(0).addPositionDiscount(discount);
        discount = new DiscountPositionEntity();
        discount.setCardNumber(CARD_NUMBER);
        discount.setValue(CARD_BALLANCE / 2);
        discount.setBonusDiscountAuto(true);
        purchase.getPositions().get(1).addPositionDiscount(discount);

        // теперь проверяем списание
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        PurchaseCardsEntity purchaseCardsEntity = new PurchaseCardsEntity();
        purchaseCardsEntity.setCardType(purchase.getCardsInfo().get(0).getCardType());
        purchaseEntity.getCards().add(purchaseCardsEntity);
        InternalCards mock = mock(InternalCards.class);
        Mockito.doReturn(null).when(mock).writeOffFromBonusAccount(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any());
        Whitebox.setInternalState(service, "internalCards", mock);

        Whitebox.invokeMethod(service, "autoDiscountWriteOff", purchaseEntity, purchase, null, null);

        ArgumentCaptor<String> cardCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> writeOffCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(mock).writeOffFromBonusAccount(cardCaptor.capture(), Mockito.anyLong(), writeOffCaptor.capture(), Mockito.any(), Mockito.any());

        Assert.assertEquals("Wrong card number!", CARD_NUMBER, cardCaptor.getValue());
        Assert.assertEquals("Wrong write off sum!", Long.valueOf(CARD_BALLANCE), writeOffCaptor.getValue());
    }

    @Test
    public void testValidationCard()
            throws Exception {
        // given - good verification
        PurchaseBuilder purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder.addCard(new CardBuilder(CardTypes.InternalCard)
                .addNumber(CARD_NUMBER)
                .build());
        Purchase purchase = purchaseBuilder.build();

        PurchaseCardsEntity purchaseCardsEntity = new PurchaseCardsEntity();
        purchaseCardsEntity.setProcessingName(CoBrandService.PROVIDER_NAME);
        purchaseCardsEntity.setCardType(purchase.getCardsInfo().get(0).getCardType());

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setSale();
        purchaseEntity.getCards().add(purchaseCardsEntity);

        CoBrandService coBrandService = mock(CoBrandService.class);
        when(coBrandService.isCoBrandConfigured()).thenReturn(true);
        Whitebox.setInternalState(service, "coBrandService", coBrandService);

        // when
        Boolean verified = Whitebox.invokeMethod(service, "verifyCard", purchaseEntity);

        // then
        Assert.assertTrue(verified);

        // ------------------------
        // given - bad verification: not supported processing
        purchaseEntity.getCards().get(0).setProcessingName("NotCoBrandCard");

        // when
        verified = Whitebox.invokeMethod(service, "verifyCard", purchaseEntity);

        // then
        Assert.assertFalse(verified);

        // ------------------------
        // given - bad verification: no card
        purchaseEntity.getCards().clear();

        // when
        verified = Whitebox.invokeMethod(service, "verifyCard", purchaseEntity);

        // then
        Assert.assertFalse(verified);
    }

    @Test
    public void testCancelDiscountInvokation() {
        service.checkCanceled(new PurchaseEntity());

        verify(service, never()).cancelDiscount(any());
    }

    @Test
    public void testProcessRecalculateAfterPaymentTypeChangeDivided() throws Exception {
        AdvertisingActionEntity action = new AdvertisingActionBuilder().addGuid(1L).addPriority(1000.0)
                .addPercentDiscountActionResult(100L)
                .addPaymentTypeCondition("CashPayment", 0L, true)
                .build();
        doReturn(new AdvertisingActionEntity[]{action}).when(service).getAdvertisingActions(Mockito.any(PurchaseEntity.class));
        Consumer consumer = mock(Consumer.class);
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.getDividedPurchases().add(new PurchaseEntity());
        purchase.getDividedPurchases().add(new PurchaseEntity());
        service.processRecalculateAfterPaymentTypeChange(purchase, consumer);
        verify(consumer).accept(isNull());
    }

    @Test
    public void testProcessRecalculateAfterPaymentTypeChangeBonuses() throws Exception {
        AdvertisingActionEntity action = new AdvertisingActionBuilder().addGuid(1L).addPriority(1000.0)
                .addPercentDiscountActionResult(100L)
                .addPaymentTypeCondition("CashPayment", 0L, true)
                .build();
        doReturn(new AdvertisingActionEntity[]{action}).when(service).getAdvertisingActions(Mockito.any(PurchaseEntity.class));
        Consumer consumer = mock(Consumer.class);
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setSale();
        CardEntity cardEntity = new CardEntity();
        CardBonusBalance cardBonusBalance = new CardBonusBalance();
        cardBonusBalance.setAvailableChargeOffBalance(100L);
        cardEntity.setCardBonusBalance(cardBonusBalance);
        PurchaseCardsEntity purchaseCard = new PurchaseCardsEntity("1234", new InternalCardsEntity(), purchase);
        purchaseCard.getCardType().getCards().add(cardEntity);
        purchase.getCards().add(purchaseCard);
        service.setCache(new AdvActionsCacheImpl());
        service.processRecalculateAfterPaymentTypeChange(purchase, consumer);
        verify(consumer).accept(isNull());
    }

    @Test
    public void testCommitCanceledLoyTransaction() {
        Mockito.reset(service.getLoyTxDao());
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setLoyalTransactionId(4L);
        LoyTransactionEntity tx = mock(LoyTransactionEntity.class);
        when(service.getLoyTxDao().getLoyTxById(eq(purchase.getLoyalTransactionId()))).thenReturn(tx);
        service.commitCanceledLoyTransaction(purchase);
        verify(service.getLoyTxDao()).linkTxToReceipt(same(tx), same(purchase));
    }

    @Test
    public void testCommitCanceledNoLoyTransaction() {
        Mockito.reset(service.getLoyTxDao());
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setLoyalTransactionId(null);
        service.commitCanceledLoyTransaction(purchase);
        verify(service.getLoyTxDao(), never()).linkTxToReceipt(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMinimalPricesForGoodList() {
        AdvActionsCache cache = service.getCache();
        try {
            AdvActionsCacheImpl actionCache = new AdvActionsCacheImpl();
            actionCache.getCache().put(1L, AdvertiseActionTestUtils.createFixPriceAction(1L, null, true,
                    new FixPriceDiscountDescriptor("12345", 5000L), new FixPriceDiscountDescriptor("12346", 9000L)));
            service.setCache(actionCache);
            ArrayList<LoyalProductEntity> loyProducts = new ArrayList<>();
            loyProducts.add(getLoyalProductEntity("test1", "12345", 10000L, new LoyalDepartEntity(1L, "depart1")));
            loyProducts.add(getLoyalProductEntity("test2", "12346", 30000L, new LoyalDepartEntity(2L, "depart2")));
            Map<String, ActionPriceInfo> minimalPricesForGoodList = service.getMinimalPricesForGoodList(loyProducts, new Date(), null);
            ActionPriceInfo actionPriceInfo1 = minimalPricesForGoodList.get("12345");
            ActionPriceInfo actionPriceInfo2 = minimalPricesForGoodList.get("12346");
            Assert.assertNotNull(actionPriceInfo1);
            Assert.assertNotNull(actionPriceInfo2);
            Assert.assertEquals(5000L, actionPriceInfo1.getPrice().longValue());
            Assert.assertEquals(9000L, actionPriceInfo2.getPrice().longValue());
        } finally {
            service.setCache(cache);
        }
    }

    private static ILoyTechProcess getLoyTechProcess() {
        LoyTechProcess loyTechProcess = new LoyTechProcess();
        loyTechProcess.getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, new CurrencyHandlerFactory().getCurrencyHandler(null));
        loyTechProcess.getLoyaltyProperties().setGeneralInteractionMethod(GeneralInteractionMethod.MAXIMUM_DISCOUNT);
        loyTechProcess.getLoyaltyProperties().setFz54Compatible(true);
        loyTechProcess.getLoyaltyProperties().setSegregateDiscounts(true);
        return loyTechProcess;
    }

    private LoyalProductEntity getLoyalProductEntity(String name, String item, Long priceLong, LoyalDepartEntity depart) {
        LoyalProductEntity productEntity = new LoyalProductEntity();
        productEntity.setName(name);
        productEntity.setMarkingOfTheGood(item);
        HashSet<LoyalPriceEntity> loyalPrices = new HashSet<>();
        LoyalPriceEntity price = new LoyalPriceEntity();
        price.setPriceType(PriceType.PRICE);
        price.setNumber(1L);
        price.setProduct(productEntity);
        price.setPrice(priceLong);
        price.setDepartment(depart);
        loyalPrices.add(price);
        productEntity.setPrices(loyalPrices);
        return productEntity;
    }

    @Test
    public void testProcessScanGiftsAsProducts() throws Exception {
        when(tp.getCheck()).thenReturn(new PurchaseEntity());

        String giftItem = "76543";
        Purchase purchase = new Purchase();
        GiftNoteDescription giftNote = new GiftNoteDescription();
        giftNote.setAddAsProducts(true);
        giftNote.setAdvertisingActionGUID(222L);
        giftNote.setTotalQnty(3);
        giftNote.setDisplayTime(GiftDisplayTime.SUBTOTAL);
        giftNote.getBarcodes().add(giftItem);
        purchase.getAdvertisingActionResults().add(giftNote);

        String giftCode = "9876543";
        ProductEntity product = new ProductEntity();
        product.setItem(giftItem);
        ProductConfig productConfig = new ProductConfig();
        productConfig.setSaleAllowed(true);
        product.setProductConfig(productConfig);
        when(tp.searchProductWithoutBeep(eq(giftCode))).thenReturn(product);
        SubtotalScenarioInterface scenario = mock(SubtotalScenarioInterface.class);
        doAnswer(invOnMock -> {
            GiftCodeConsumer codeConsumer = invOnMock.getArgumentAt(1, GiftCodeConsumer.class);
            codeConsumer.accept(giftCode);
            return null;
        }).when(scenario).showScanGiftDialog(eq(3), any(GiftCodeConsumer.class));
        when(scenario.addProduct(same(product), eq(LoyMetrics.COUNT_PRECISION))).thenReturn(true);

        boolean added = service.processScanGiftsAsProducts(scenario, purchase);

        verify(scenario).addProduct(same(product), eq(LoyMetrics.COUNT_PRECISION));
        Assert.assertEquals(giftNote.getAdvertisingActionGUID(), product.getProductPositionData().getAdvActionGuid());
        Assert.assertTrue(added);
    }

    @Test
    public void testManualActionsSort() {
        final String[] actionNames = new String[] {"1-я скидка на пирожок", "10% на 100500", "120719 Скидка 20%", "2-я скидка на пирожок",
                "211563 Discount 30%", "211563 Скидка 20%", "Apples 20", "NATURAL COSMETIC", "Price reduction 1", "Price reduction 2",
                "Sunrise 2021", "Акция BREAD", "Акция MILK", "Скидка 30% на позицию", "Яблоки 10"};
        AdvActionsCache cache = service.getCache();
        try {
            List<String> shuffledActions = Arrays.asList(actionNames.clone());
            Collections.shuffle(shuffledActions);
            AdvActionsCacheImpl actionCache = new AdvActionsCacheImpl();
            for (int i = 0; i < shuffledActions.size(); i++) {
                AdvertisingActionEntity action = new AdvertisingActionEntity();
                action.setGuid((long) i);
                action.setWorkPeriod(new TimestampPeriodEntity());
                action.getWorkPeriod().setStart(Date.from(Instant.now().minus(Duration.ofDays(1L))));
                action.getWorkPeriod().setFinish(Date.from(Instant.now().plus(Duration.ofDays(1L))));
                action.setName(shuffledActions.get(i));
                action.setMode(ApplyMode.MANUAL);
                action.setActive(true);
                actionCache.getCache().put((long) i, action);
            }
            service.setCache(actionCache);
            ManualAdvertisingActionEntity[] manualActions = service.getManualAction();
            for (int i = 0; i < manualActions.length; i++) {
                Assert.assertEquals(actionNames[i], manualActions[i].getActionName());
            }
        } finally {
            service.setCache(cache);
        }
    }
}
