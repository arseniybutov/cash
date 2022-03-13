package ru.crystals.pos.loyal.cash.service.issues;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.reflect.Whitebox;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.check.discount.DiscountType;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.loyal.providers.ClassicSetLoyProvider;
import ru.crystals.loyal.providers.LoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistryImpl;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.loyal.LoyaltyResults;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.loyal.cash.service.CashAdvertisingActionDao;
import ru.crystals.pos.loyal.cash.service.LoyProvidersRegistryWrapper;
import ru.crystals.pos.loyal.cash.service.LoyalServiceImpl;
import ru.crystals.pos.loyal.cash.service.LoyalServiceTestUtils;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.setretailx.cash.CashVO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * Тюнинг ФЗ-54 после работы внешнего провайдера лояльности.
 * В оригинальном дефекте происходит отклонение в уравнении ЦЕНА * КОЛИЧЕСТВО == СТОИМОСТЬ на три копейки по весовой позиции.
 * Добавлен тюнинг весовых позиций после взаимодействий со всеми внешними провайдерами лояльности.
 * <a href="https://crystals.atlassian.net/browse/SR-2392">SR-2392</a>
 */
public class SR2392TestCase {

    private static final String ITEM = "33234";

    private LoyalServiceImpl service;

    private LoyaltyPropertiesFactory loyaltyPropertiesFactory;

    private LoyProvider setApiProvider;

    private LoyActionsProvider loyActionsProvider;

    private ClassicSetLoyProvider setClassicProvider;

    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    @Before
    public void setUp() throws FiscalPrinterException {
        service = spy(new LoyalServiceImpl());
        loyaltyPropertiesFactory = mock(LoyaltyPropertiesFactory.class);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        when(loyaltyPropertiesFactory.get()).thenReturn(loyaltyProperties);

        CatalogService catalogService = mock(CatalogService.class);
        doReturn(generateProducts()).when(catalogService).getLoyalGoodsBatchByItems(any());
        Whitebox.setInternalState(service, "catalogService", catalogService);

        LoyalProductsConverter.setUseCache(true);
        Whitebox.setInternalState(service, "currencyHandler", currencyHandler);

        TechProcessInterface tpi = mock(TechProcessInterface.class);
        doReturn(1L).when(tpi).getExpectedDocNum(any());
        doReturn(new CashVO()).when(tpi).getCashParams();

        setApiProvider = mock(LoyProvider.class);
        setClassicProvider = new ClassicSetLoyProvider();
        AbstractPluginsAdapter pluginsAdapter = mock(AbstractPluginsAdapter.class);
        Whitebox.setInternalState(setClassicProvider, "pluginsAdapter", pluginsAdapter);
        Whitebox.setInternalState(setClassicProvider, "currencyHandler", currencyHandler);

        loyActionsProvider = mock(LoyActionsProvider.class);

        LoyProvidersRegistryImpl registry = new LoyProvidersRegistryImpl();
        Whitebox.setInternalState(registry, "actionsProvider", loyActionsProvider);
        Whitebox.setInternalState(registry, "techProcess", tpi);
        Whitebox.setInternalState(registry, "catalogService", catalogService);
        registry.getProviders().add(setClassicProvider);
        registry.getProviders().add(setApiProvider);

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
        service.start();

        service.getLoyTechProcess().getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        when(service.getLoyTxDao().saveLoyAdvAction(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(service.getLoyTxDao().saveLoyTx(any())).thenAnswer(
                invocationOnMock -> setupTxId((LoyTransactionEntity) invocationOnMock.getArguments()[0]));
        doAnswer(invocationOnMock -> setupTxId(new LoyTransactionEntity())).when(service).getLoyTx(any());
        doAnswer(invocationOnMock -> setupTxId(new LoyTransactionEntity())).when(service).getLoyTx(any(), any());
    }

    /**
     * Весовой товар в чеке после ответа от внешного провайдера лояльности получает скидку.
     * Проверка соответствия чека ФЗ-54.
     */
    @Test
    public void noFl54ViolationsAfterCalculation() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        PositionEntity originalPosition = EntitiesTestUtils.createPosition(
                currencyHandler, 1L, TestUtils.convertCount(5.000), 424_99L, 1L, ITEM);
        originalPosition.setPrecision(0.001);
        positionEntities.add(originalPosition);
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{};

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(any(), any());
        setClassicProvider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        when(setApiProvider.isPurchaseSuitable(any())).thenReturn(true);
        when(setApiProvider.process(any(), any(), any())).thenAnswer(SR2392TestCase::addDiscounts);
        when(loyActionsProvider.getActions(any(Purchase.class))).thenReturn(Lists.newArrayList(actions));

        service.doDiscount(originalPurchase);

        for (PositionEntity pos : originalPurchase.getPositions()) {
            long actual = currencyHandler.getPositionSum(pos.getPriceEnd(), pos.getQnty());
            long expected = pos.getSum();
            assertFalse(Math.abs(actual - expected) > 1);
        }
    }

    /**
     * Проверка округления чека.
     */
    @Test
    public void roundingDiscountDuringFl54Tuning() throws Exception {
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        PositionEntity originalPosition = EntitiesTestUtils.createPosition(
                currencyHandler, 1L, TestUtils.convertCount(1.744), 19_99L, 1L, ITEM);
        originalPosition.setPrecision(0.001);
        positionEntities.add(originalPosition);
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[] {
                AdvertiseActionTestUtils.createRoundingAction(666L, 100)
        };

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(any(), any());
        setClassicProvider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        when(setApiProvider.isPurchaseSuitable(any())).thenReturn(false);
        when(loyActionsProvider.getActions(any(Purchase.class))).thenReturn(Lists.newArrayList(actions));

        LoyaltyResults loyaltyResults = service.doDiscount(originalPurchase);

        Set<String> strings = TestUtils.validateCheck(loyaltyResults.getLoyalPurchase());
        if(!strings.isEmpty()) {
            Assert.fail(strings.stream().collect(Collectors.joining(", ")));
        }

        assertEquals(1, originalPurchase.getPositions().stream().mapToLong(PositionEntity::getSum).sum() % 100);
    }


    private static Purchase addDiscounts(InvocationOnMock invocation) {
        Purchase original = (Purchase) invocation.getArguments()[0];
        Purchase result = new Purchase();
        original.clone(result);
        Position position = result.getLastPosition();

        DiscountBatchEntity dbe = new DiscountBatchEntity();
        DiscountPositionEntity dpe = new DiscountPositionEntity();
        dpe.setValue(6_77L);
        dpe.setCount(5000L);
        dpe.setType(DiscountType.SUM);
        dpe.setValueCalculated(dpe.getValue());
        dpe.setDiscountBatch(dbe);
        dbe.getDiscounts().add(dpe);
        dbe.setDiscountValue(dpe.getValueCalculated());
        position.setDiscountBatch(dbe);

        return result;
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
