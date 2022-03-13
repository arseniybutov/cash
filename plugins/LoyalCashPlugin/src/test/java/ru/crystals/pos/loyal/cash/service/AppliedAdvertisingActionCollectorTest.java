package ru.crystals.pos.loyal.cash.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;

import java.util.Collection;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppliedAdvertisingActionCollectorTest {

    private LoyalServiceImpl loyalService;

    private AppliedAdvertisingActionCollector collector;

    @Before
    public void before() {
        loyalService = mock(LoyalServiceImpl.class);
        collector = new AppliedAdvertisingActionCollector(loyalService);
    }

    @Test
    public void collectAppliedActionsTest() {
        Purchase receipt = new Purchase();
        Position pos = new Position();
        receipt.getPositions().add(pos);
        pos.setDiscountBatch(new DiscountBatchEntity());
        DiscountPositionEntity disc = new DiscountPositionEntity();
        disc.setAdvertisingActionGUID(4L);
        disc.setActionExternalCode("ext5");
        pos.getDiscountBatch().getDiscounts().add(disc);
        DiscountPositionEntity disc2 = new DiscountPositionEntity();
        disc2.setAdvertisingActionGUID(6L);
        disc2.setActionExternalCode("ext7");
        pos.getDiscountBatch().getDiscounts().add(disc2);

        AdvertisingActionEntity existingAction = new AdvertisingActionEntity();
        existingAction.setGuid(disc2.getAdvertisingActionGUID());
        existingAction.setExternalCode("ext8");
        when(loyalService.getAdvertisingActions(same(receipt))).thenReturn(new AdvertisingActionEntity[]{existingAction});

        Collection<AdvertisingActionEntity> result = collector.collectAppliedActions(receipt);

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.stream().anyMatch(aa -> disc.getAdvertisingActionGUID().equals(aa.getGuid())
                && disc.getActionExternalCode().equals(aa.getExternalCode())));
        Assert.assertTrue(result.stream().anyMatch(aa -> disc2.getAdvertisingActionGUID().equals(aa.getGuid())
                && existingAction.getExternalCode().equals(aa.getExternalCode())));
    }
}
