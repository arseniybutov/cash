package ru.crystals.loyal.actions.provider;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.ManualPositionAdvActionEntity;
import ru.crystals.pos.check.ManualPositionAdvertisingActionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.loyal.cash.service.AdvActionsCache;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Проверяется в основном поиск идентификаторов ручных рекламных акций в чеках.
 */
public class InMemoryLoyActionsProviderTest {

    private LoyActionsProvider loyActionsProvider;
    private AdvActionsCache cache;
    private AdvertisingActionProviderRoutine actionProviderRoutine;


    @Before
    public void setupMockAndStartBean() {
        cache = mock(AdvActionsCache.class);
        actionProviderRoutine = mock(AdvertisingActionProviderRoutine.class);
        InMemoryLoyActionsProvider inMemoryLoyActionsProvider = new InMemoryLoyActionsProvider();
        inMemoryLoyActionsProvider.setCache(cache);
        inMemoryLoyActionsProvider.start();
        loyActionsProvider = inMemoryLoyActionsProvider;

        Whitebox.setInternalState(loyActionsProvider, "actionProviderRoutine", actionProviderRoutine);
    }

    @Test
    public void returnNotNull() {
        when(cache.getActiveActions(any(Date.class), anySet())).thenReturn(null);

        assertFalse(loyActionsProvider.getActions(new PurchaseEntity()) == null);
        assertFalse(loyActionsProvider.getActions(new Purchase()) == null);

        verify(cache, times(2)).getActiveActions(any(Date.class), anySet());
    }

    @Test
    public void ifNullArgument() {
        PurchaseEntity purchaseEntity = null;
        Purchase purchase = null;

        when(cache.getActiveActions(any(Date.class), anySet())).thenAnswer(invocationOnMock -> {
            Set<Long> manualActions = (Set<Long>) invocationOnMock.getArguments()[1];
            assertFalse(manualActions == null);
            assertTrue(manualActions.isEmpty());
            return Arrays.asList(new AdvertisingActionEntity());
        });

        assertEquals(1, loyActionsProvider.getActions(purchaseEntity).size());
        assertEquals(1, loyActionsProvider.getActions(purchase).size());

        verify(cache, times(2)).getActiveActions(any(Date.class), anySet());
    }

    @Test
    public void positionWithoutManualAdvertisingActions() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.getPositions().add(new PositionEntity());
        Purchase purchase = new Purchase();
        purchase.getPositions().add(new Position());

        when(cache.getActiveActions(any(Date.class), anySet())).thenAnswer(invocationOnMock -> {
            Set<Long> manualActions = (Set<Long>) invocationOnMock.getArguments()[1];
            assertFalse(manualActions == null);
            assertTrue(manualActions.isEmpty());
            return Collections.EMPTY_LIST;
        });

        loyActionsProvider.getActions(purchaseEntity);
        loyActionsProvider.getActions(purchase);

        verify(cache, times(2)).getActiveActions(any(Date.class), anySet());
    }

    @Test
    public void positionHasManualAdvertisingActionsWithNullGuid() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        PositionEntity positionEntity = new PositionEntity();
        purchaseEntity.getPositions().add(positionEntity);
        positionEntity.setManualAdvertisingActions(new LinkedList<>());
        positionEntity.getManualAdvertisingActions().add(new ManualPositionAdvertisingActionEntity());

        Purchase purchase = new Purchase();
        Position position = new Position();
        purchase.getPositions().add(position);
        position.setManualAdvActions(new LinkedList<>());
        position.getManualAdvActions().add(new ManualPositionAdvActionEntity());

        when(cache.getActiveActions(any(Date.class), anySet())).thenAnswer(invocationOnMock -> {
            Set<Long> manualActions = (Set<Long>) invocationOnMock.getArguments()[1];
            assertFalse(manualActions == null);
            assertTrue(manualActions.isEmpty());
            return Collections.EMPTY_LIST;
        });

        loyActionsProvider.getActions(purchaseEntity);
        loyActionsProvider.getActions(purchase);

        verify(cache, times(2)).getActiveActions(any(Date.class), anySet());
    }

    @Test
    public void positionHasManualAdvertisingActions() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        PositionEntity positionEntity = new PositionEntity();
        purchaseEntity.getPositions().add(positionEntity);
        positionEntity.setManualAdvertisingActions(new LinkedList<>());
        positionEntity.getManualAdvertisingActions().add(new ManualPositionAdvertisingActionEntity());
        positionEntity.getManualAdvertisingActions().get(0).setActionGuid(666L);

        Purchase purchase = new Purchase();
        Position position = new Position();
        purchase.getPositions().add(position);
        position.setManualAdvActions(new LinkedList<>());
        position.getManualAdvActions().add(new ManualPositionAdvActionEntity());
        position.getManualAdvActions().get(0).setActionGuid(666L);

        when(cache.getActiveActions(any(Date.class), anySet())).thenAnswer(invocationOnMock -> {
            Set<Long> manualActions = (Set<Long>) invocationOnMock.getArguments()[1];
            assertEquals(1, manualActions.size());
            assertTrue(manualActions.contains(666L));
            return Collections.EMPTY_LIST;
        });

        loyActionsProvider.getActions(purchaseEntity);
        loyActionsProvider.getActions(purchase);

        verify(cache, times(2)).getActiveActions(any(Date.class), anySet());
    }

    @Test
    public void onDiscountCalculationStartedTest() {
        Purchase purchase = mock(Purchase.class);
        Map extData = mock(Map.class);
        loyActionsProvider.onDiscountCalculationStarted(purchase, extData);

        verify(actionProviderRoutine).updateActionsCache(same(purchase), same(extData), same(cache));
    }

    @Test
    public void onPurchaseProcessingFinishedTest() {
        loyActionsProvider.onPurchaseProcessingFinished();

        verify(actionProviderRoutine).clearActionsCache(same(cache));
    }

}