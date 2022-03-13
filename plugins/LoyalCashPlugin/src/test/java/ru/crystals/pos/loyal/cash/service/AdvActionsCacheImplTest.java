package ru.crystals.pos.loyal.cash.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.discounts.AdvertisingActionEntity;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тесты AdvActionsCacheImpl
 */
public class AdvActionsCacheImplTest {

    private CashAdvertisingActionDao actionsDao;

    private AdvActionsCacheImpl cache;

    @Before
    public void before() {
        actionsDao = Mockito.mock(CashAdvertisingActionDao.class);
        cache = new AdvActionsCacheImpl();
        cache.setThreadStart(false);
        cache.setActionsDao(actionsDao);
    }

    @Test
    public void getActiveActionsTest_emptyResult() {
        Map<Long, AdvertisingActionEntity> dbActions = new HashMap<>();
        Mockito.when(actionsDao.getActionsByGuids(Mockito.any(), Mockito.any(Date.class))).thenReturn(dbActions);
        cache.postConstruct();

        Map<Long, AdvertisingActionEntity> pluginsActions = new HashMap<>();
        cache.setPurchaseCache(pluginsActions);

        List<AdvertisingActionEntity> result = cache.getActiveActions(new Date(), Collections.emptySet());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getActiveActionsTest() {
        Map<Long, AdvertisingActionEntity> dbActions = new HashMap<>();
        dbActions.put(1L, createAction(1L));
        Mockito.when(actionsDao.getActionsByGuids(Mockito.any(), Mockito.any(Date.class))).thenReturn(dbActions);
        cache.postConstruct();

        Map<Long, AdvertisingActionEntity> pluginsActions = new HashMap<>();
        pluginsActions.put(2L, createAction(2L));
        cache.setPurchaseCache(pluginsActions);

        List<AdvertisingActionEntity> result = cache.getActiveActions(new Date(), Collections.emptySet());
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getActiveActionsTest_duplicateGuid() {
        Map<Long, AdvertisingActionEntity> dbActions = new HashMap<>();
        dbActions.put(2L, createAction(2L));
        Mockito.when(actionsDao.getActionsByGuids(Mockito.any(), Mockito.any(Date.class))).thenReturn(dbActions);
        cache.postConstruct();

        Map<Long, AdvertisingActionEntity> pluginsActions = new HashMap<>();
        pluginsActions.put(2L, createAction(2L));
        cache.setPurchaseCache(pluginsActions);

        List<AdvertisingActionEntity> result = cache.getActiveActions(new Date(), Collections.emptySet());
        Assert.assertEquals(1, result.size());
        Assert.assertSame(pluginsActions.values().stream().findFirst().get(), result.get(0));
    }

    private AdvertisingActionEntity createAction(long guid) {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        action.setGuid(guid);
        action.setActive(true);
        return action;
    }
}
