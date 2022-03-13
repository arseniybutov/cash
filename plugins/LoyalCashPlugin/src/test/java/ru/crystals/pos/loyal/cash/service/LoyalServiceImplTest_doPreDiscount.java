package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import ru.crystals.api.adapters.AbstractPluginsAdapter;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.CalculateSession;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.model.GoodsIDType;
import ru.crystals.loyal.providers.ClassicSetLoyProvider;
import ru.crystals.loyal.providers.LoyProvider;
import ru.crystals.loyal.providers.LoyProvidersRegistryImpl;
import ru.crystals.loyal.test.utils.AdvertiseActionTestUtils;
import ru.crystals.loyal.test.utils.ApplyObjectDiscountDescriptor;
import ru.crystals.loyal.test.utils.CompositeRowDescription;
import ru.crystals.loyal.test.utils.TestUtils;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.CashException;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystals.pos.loyal.cash.converter.LoyalProductsConverter;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.setretailx.cash.CashVO;
import ru.crystalservice.setv6.discounts.plugins.CompositeApplyObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * Глобальный тест пред-расчета скидок
 */
public class LoyalServiceImplTest_doPreDiscount {

    private LoyalServiceImpl service;

    private CurrencyHandler currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

    private ClassicSetLoyProvider provider = new ClassicSetLoyProvider();

    private LoyActionsProvider loyActionsProvider;

    @Before
    public void setUp() throws FiscalPrinterException {
        service = Mockito.spy(new LoyalServiceImpl());

        LoyaltyPropertiesFactory loyaltyPropertiesFactory = mock(LoyaltyPropertiesFactory.class);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        Mockito.when(loyaltyPropertiesFactory.get()).thenReturn(loyaltyProperties);

        LoyalProductsConverter.setUseCache(true);
        Whitebox.setInternalState(service, "currencyHandler", currencyHandler);

        CatalogService catalogService = mock(CatalogService.class);
        doReturn(Collections.emptyList()).when(catalogService).getLoyalGoodsBatchByItems(Mockito.any());
        Whitebox.setInternalState(service, "catalogService", catalogService);

        TechProcessInterface tpi = mock(TechProcessInterface.class);
        doReturn(1L).when(tpi).getExpectedDocNum(Mockito.any());
        doReturn(1L).when(tpi).getExpectedDocNum(any());

        CashVO cashVO = new CashVO();
        cashVO.setFactoryNum("123456789");
        doReturn(cashVO).when(tpi).getCashParams();

        loyActionsProvider = mock(LoyActionsProvider.class);

        LoyProvidersRegistryImpl registry = new LoyProvidersRegistryImpl();
        Whitebox.setInternalState(registry, "actionsProvider", loyActionsProvider);
        Whitebox.setInternalState(registry, "techProcess", tpi);
        Whitebox.setInternalState(registry, "catalogService", catalogService);
        registry.getProviders().add(provider);

        AbstractPluginsAdapter pluginsAdapter = mock(AbstractPluginsAdapter.class);
        Whitebox.setInternalState(provider, "pluginsAdapter", pluginsAdapter);
        Whitebox.setInternalState(provider, "currencyHandler", currencyHandler);

        LoyProvidersRegistryWrapper loyProvidersWrapper = mock(LoyProvidersRegistryWrapper.class);
        doReturn(registry).when(loyProvidersWrapper).getLoyProviders();
        Whitebox.setInternalState(service, "loyProvidersWrapper", loyProvidersWrapper);

        Whitebox.setInternalState(service, "loyaltyPropertiesFactory", loyaltyPropertiesFactory);
        Whitebox.setInternalState(service, "propertiesManager", mock(PropertiesManager.class));
        Whitebox.setInternalState(service, "tp", tpi);

        doReturn(loyaltyProperties).when(service).getLoyaltyProperties();

        service.start();
        service.getLoyTechProcess().getProcessingSession().put(CalculateSession.CURRENCY_HANDLER, currencyHandler);
    }

    @Test
    public void doPreDiscountTest() {
        //given
        Collection<PositionEntity> positionEntities = new ArrayList<>();
        positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, 1L, TestUtils.convertCount(1), 100_00L, 1L, "PIECE1"));
        PurchaseEntity originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);

        AdvertisingActionEntity[] actions = new AdvertisingActionEntity[]{
                AdvertiseActionTestUtils.createApplyObjectAction(false, 66601L, "66601L",
                        new ApplyObjectDiscountDescriptor(Collections.singletonList(
                                new CompositeRowDescription(new ArrayList<>(Collections.singletonList("PIECE1")), GoodsIDType.ITEM)),
                                CompositeApplyObject.ValueType.PERCENT, 20, null, null, null,
                                false, null, false)
                )
        };

        service.getLoyTechProcess().getLoyaltyProperties().setSegregateDiscounts(true);
        service.getLoyTechProcess().getLoyaltyProperties().setFz54Compatible(true);

        service.getLoyTechProcess().setAdvertisingActions(actions);
        doReturn(actions).when(service).getAdvertisingActions(Mockito.any(), Mockito.any());
        doReturn(Lists.newArrayList(actions)).when(loyActionsProvider).getActions(Mockito.any(Purchase.class));
        provider.setActionsProvider(LoyalServiceTestUtils.generateLoyActionsProvider(Lists.newArrayList(actions)));

        //when
        service.doPreDiscount(originalPurchase);

        //then
        Assert.assertEquals(80_00L, originalPurchase.getPositions().get(0).getLoyPosition().getSum());
        Assert.assertEquals(20_00L, originalPurchase.getPositions().get(0).getLoyPosition().getDiscountValue());
        Assert.assertEquals(Long.valueOf(80_00L), CheckUtils.getPurchaseSumWithPreDiscount(originalPurchase).get());
        Assert.assertEquals(20_00L, CheckUtils.getPreDiscountSum(originalPurchase));
        Mockito.verify(service).putArguments(Mockito.any(), Mockito.any());
        //given
        List<String> onlyProviders = (List<String>) service.getLoyTechProcess().getProcessingSession().getValue(CalculateSession.ONLY_LOYALTY_PROCESSINGS);
        Assert.assertEquals(1, onlyProviders.size());
        Assert.assertEquals(LoyProvider.SET10_PROVIDER_NAME, onlyProviders.get(0));
        // проверим, что следующий расчет скидок будет со сброшеной сессией
        try {
            service.doDiscount(originalPurchase, null);
        } catch (CashException ignore) {
        }
        Mockito.verify(service, times(2)).putArguments(Mockito.any(), Mockito.any());
        onlyProviders = (List<String>) service.getLoyTechProcess().getProcessingSession().getValue(CalculateSession.ONLY_LOYALTY_PROCESSINGS);
        Assert.assertTrue(onlyProviders.isEmpty());
    }
}
