package ru.crystals.pos.loyal.cash.service.issues;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.loyal.providers.CustomSetLoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistryImpl;
import ru.crystals.loyal.providers.set.filters.NonWorksAnyTimeSetActionsFilter;
import ru.crystals.loyal.providers.set.filters.WorksAnyTimeSetActionsFilter;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.loyal.cash.service.AdvActionsCache;
import ru.crystals.pos.loyal.cash.service.CashAdvertisingActionDao;
import ru.crystals.pos.loyal.cash.service.LoyProvidersRegistryWrapper;
import ru.crystals.pos.loyal.cash.service.LoyalServiceImpl;
import ru.crystals.pos.loyal.cash.service.LoyalServiceTestUtils;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.setretailx.cash.CashVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * Если в РА не суммирующейся, выставить галочки на запрет списания или начисления бонусов,
 * то они должны сработать даже когда в ext-loyalty-providers.xml калькулятор разбит на провайдеры
 * <a href="https://crystals.atlassian.net/browse/SR-3012">SR-3012</a>
 */
public class SR3012TestCase {

    private static final String ITEM = "33234";

    private LoyalServiceImpl service;

    private LoyActionsProvider loyActionsProvider;
    private AdvActionsCache cache = mock(AdvActionsCache.class);

    private CustomSetLoyProvider notWorkAnyTimeProvider;
    private CustomSetLoyProvider workAnyTimeProvider;

    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    @Before
    public void setUp() throws FiscalPrinterException {
        service = spy(new LoyalServiceImpl());

        LoyaltyPropertiesFactory loyaltyPropertiesFactory = mock(LoyaltyPropertiesFactory.class);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        when(loyaltyPropertiesFactory.get()).thenReturn(loyaltyProperties);

        LoyalProductsConverter.setUseCache(true);
        Whitebox.setInternalState(service, "currencyHandler", currencyHandler);

        CatalogService catalogService = mock(CatalogService.class);
        doReturn(generateProducts()).when(catalogService).getLoyalGoodsBatchByItems(any());
        Whitebox.setInternalState(service, "catalogService", catalogService);

        TechProcessInterface tpi = mock(TechProcessInterface.class);
        doReturn(1L).when(tpi).getExpectedDocNum(any());

        CashVO cashVO = new CashVO();
        cashVO.setFactoryNum("123456789");
        doReturn(cashVO).when(tpi).getCashParams();

        notWorkAnyTimeProvider = new CustomSetLoyProvider();
        notWorkAnyTimeProvider.setFilter(new NonWorksAnyTimeSetActionsFilter());

        AbstractPluginsAdapter pluginsAdapter = mock(AbstractPluginsAdapter.class);
        Whitebox.setInternalState(notWorkAnyTimeProvider, "pluginsAdapter", pluginsAdapter);


        workAnyTimeProvider = new CustomSetLoyProvider();
        workAnyTimeProvider.setFilter(new WorksAnyTimeSetActionsFilter());

        Whitebox.setInternalState(workAnyTimeProvider, "pluginsAdapter", pluginsAdapter);

        loyActionsProvider = mock(LoyActionsProvider.class);

        LoyProvidersRegistryImpl registry = new LoyProvidersRegistryImpl();
        Whitebox.setInternalState(registry, "actionsProvider", loyActionsProvider);
        Whitebox.setInternalState(registry, "techProcess", tpi);
        Whitebox.setInternalState(registry, "catalogService", catalogService);
        registry.getProviders().add(notWorkAnyTimeProvider);
        registry.getProviders().add(workAnyTimeProvider);

        Whitebox.setInternalState(notWorkAnyTimeProvider, "currencyHandler", currencyHandler);
        Whitebox.setInternalState(workAnyTimeProvider, "currencyHandler", currencyHandler);

        Whitebox.setInternalState(service, "loyaltyPropertiesFactory", loyaltyPropertiesFactory);
        Whitebox.setInternalState(service, "tp", tpi);
        LoyProvidersRegistryWrapper loyProvidersWrapper = mock(LoyProvidersRegistryWrapper.class);
        doReturn(registry).when(loyProvidersWrapper).getLoyProviders();
        Whitebox.setInternalState(service, "loyProvidersWrapper", loyProvidersWrapper);
        Whitebox.setInternalState(service, "propertiesManager", mock(PropertiesManager.class));

        doReturn(loyaltyProperties).when(service).getLoyaltyProperties();
        service.setLoyTxDao(mock(LoyTxDao.class));
        service.setActionsDao(mock(CashAdvertisingActionDao.class));
        service.setCheckService(mock(CheckService.class));
        service.setCache(cache);

        service.start();
        service.getLoyTechProcess().getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        when(service.getLoyTxDao().saveLoyAdvAction(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(service.getLoyTxDao().saveLoyTx(any())).thenAnswer(
                invocationOnMock -> setupTxId((LoyTransactionEntity) invocationOnMock.getArguments()[0]));
        doAnswer(invocationOnMock -> setupTxId(new LoyTransactionEntity())).when(service).getLoyTx(any());
        doAnswer(invocationOnMock -> setupTxId(new LoyTransactionEntity())).when(service).getLoyTx(any(), any());
    }

    @Test
    public void testOuboundMutexActions() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(6),
                100_00L, 1L, ITEM));
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

        Mockito.doReturn(Arrays.asList(actions)).when(cache).getActiveActions(Mockito.any(), Mockito.any());
        notWorkAnyTimeProvider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        workAnyTimeProvider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));
        when(loyActionsProvider.getActions(any(Purchase.class))).thenReturn(Lists.newArrayList(actions));

        service.doDiscount(originalPurchase);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        Mockito.verify(service).doSave(any(), captor.capture(), any(), any());
        Assert.assertFalse(captor.getValue().getAppliedActionsInfo().stream().anyMatch(aai -> aai.getActionGuid() == 666L));
    }

    private static LoyTransactionEntity setupTxId(LoyTransactionEntity entity) {
        entity.setId(666L);
        return entity;
    }

    private static List<?> generateProducts() {
        List<LoyalProductEntity> result = new ArrayList<>();
        LoyalProductEntity entity = new LoyalProductEntity();
        entity.setMarkingOfTheGood(ITEM);
        entity.setProductType(new LoyalProductType(LoyalProductType.ProductType.WEIGHT));
        entity.setPrecision(0.001);
        result.add(entity);
        return result;
    }

}
