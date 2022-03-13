package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import ru.crystals.discounts.ActionPluginEntity;
import ru.crystals.discounts.ActionPluginPropertyEntity;
import ru.crystals.discounts.ActionPluginType;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.TimestampPeriodEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActionIntrospectorImpl.class)
public class ActionIntrospectorTest {

    @Test
    public void getActionTriggeringCouponsTest1() throws Exception {
        // проверяем что из кэша берётся правильно по дате и что кэш в принципе используется
        // given
        ActionIntrospectorImpl actionIntrospector = PowerMockito.spy(new ActionIntrospectorImpl());
        Map<String, Set<?>> cache = new HashMap<>();
        cache.put("test1", Collections.singleton(new ActionIntrospectorImpl.ActionRange(1L,
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
                new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))));
        cache.put("test2", Collections.singleton(new ActionIntrospectorImpl.ActionRange(2L,
                new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)),
                new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)))));
        cache.put("test3", Collections.singleton(new ActionIntrospectorImpl.ActionRange(3L,
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
                new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))));
        cache.put("test4", Collections.singleton(new ActionIntrospectorImpl.ActionRange(4L,
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)),
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))));
        PowerMockito.doReturn(cache).when(actionIntrospector, PowerMockito.method(ActionIntrospectorImpl.class, "getActionTriggeringCouponsFromDB"))
                .withNoArguments();
        Whitebox.invokeMethod(actionIntrospector, "updateCache");

        // when
        Set<String> coupons = actionIntrospector.getActionTriggeringCoupons(null);
        Assert.assertEquals(Sets.newHashSet("test1", "test3"), coupons);
        coupons = actionIntrospector.getActionTriggeringCoupons(null);
        Assert.assertEquals(Sets.newHashSet("test1", "test3"), coupons);
        coupons = actionIntrospector.getActionTriggeringCoupons(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)));
        Assert.assertEquals(Sets.newHashSet("test4"), coupons);

        PowerMockito.verifyPrivate(actionIntrospector, Mockito.times(1)).invoke("getActionTriggeringCouponsFromDB");
    }

    @Test
    public void getActionTriggeringCouponsTest2() throws Exception {
        // проверяем, что при изменении РА кэш перезагружается
        // given
        ActionIntrospectorImpl actionIntrospector = PowerMockito.spy(new ActionIntrospectorImpl());
        Map<String, Set<?>> cache = new HashMap<>();
        cache.put("test1", new HashSet<>(Collections.singleton(new ActionIntrospectorImpl.ActionRange(1L,
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
                new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))))));
        PowerMockito.doReturn(cache).when(actionIntrospector, PowerMockito.method(ActionIntrospectorImpl.class, "getActionTriggeringCouponsFromDB"))
                .withNoArguments();
        Whitebox.invokeMethod(actionIntrospector, "updateCache");
        actionIntrospector.updateActions(Collections.singletonList(createAction("test2", 123L)), Collections.emptyList());

        // when
        Set<String> coupons = actionIntrospector.getActionTriggeringCoupons(null);
        Assert.assertEquals(Sets.newHashSet("test1", "test2"), coupons);
        actionIntrospector.updateActions(Collections.emptyList(), Collections.singletonList(createAction("test2", 123L)));
        coupons = actionIntrospector.getActionTriggeringCoupons(null);
        Assert.assertEquals(Sets.newHashSet("test1"), coupons);

        PowerMockito.verifyPrivate(actionIntrospector, Mockito.times(1)).invoke("getActionTriggeringCouponsFromDB");
    }

    public static AdvertisingActionEntity createAction(String couponNumber, Long guid) {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        action.setGuid(guid);
        action.setPlugins(new HashSet<>());

        // плагины
        ActionPluginEntity p = new ActionPluginEntity();
        p.setClassName("ru.crystalservice.setv6.discounts.plugins.CouponsCondition");
        p.setType(ActionPluginType.CONDITIONAL_PLUGIN);

        Set<ActionPluginPropertyEntity> set = new HashSet<>();

        ActionPluginPropertyEntity entity = new ActionPluginPropertyEntity();
        entity.setClassName(String.class.getName());
        entity.setName("couponNumber");
        entity.setValue(couponNumber);
        entity.setProperties(new HashSet<>());

        set.add(entity);

        p.setProperties(set);
        action.getPlugins().add(p);

        action.setWorkPeriod(new TimestampPeriodEntity());
        action.getWorkPeriod().setStart(DateUtils.addDays(new Date(), -10));
        action.getWorkPeriod().setFinish(DateUtils.addDays(new Date(), 10));

        return action;
    }
}
