package ru.crystals.pos.loyal.cash.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeCouponAdvertisingActionResultEntity;

/**
 * Кейсы для тестирования метода {@link LoyalServiceImpl#checkCouponsCountAndDeleteExcess(Purchase, int, AdvertisingActionEntity[])}
 */
public class LoyalServiceImplCheckCouponsCount {

    private LoyalServiceImpl service;

    @Before
    public void setUp() {
        service = new LoyalServiceImpl();
    }

    @Test
    public void testCheckCouponWithoutRestriction() {
        // given
        Purchase purchase = new PurchaseBuilder()
                .addAction(couponActionResult(1000L, 2000L, null))
                .build();

        // when
        service.checkCouponsCountAndDeleteExcess(purchase, 0, new AdvertisingActionEntity[0]);

        // then
        Map<Long, ChequeCouponAdvertisingActionResultEntity> couponsByActionGuids = extractActions(purchase);
        assertThat(couponsByActionGuids.size()).isEqualTo(1);
        assertCouponResult(couponsByActionGuids.get(1000L), 2000L, null);
    }

    @Test
    public void testCheckCouponLessRestriction() {
        // given
        Purchase purchase = new PurchaseBuilder()
                .addAction(couponActionResult(1000L, 2000L, 15L))
                .addAction(couponActionResult(1001L, 2001L, 23L))
                .build();

        AdvertisingActionEntity[] actions = {action(1000L, 10.0), action(1001L, 10.1)};

        // when
        service.checkCouponsCountAndDeleteExcess(purchase, 3, actions);

        // then
        Map<Long, ChequeCouponAdvertisingActionResultEntity> couponsByActionGuids = extractActions(purchase);
        assertThat(couponsByActionGuids.size()).isEqualTo(2);
        assertCouponResult(couponsByActionGuids.get(1000L), 2000L, 15L);
        assertCouponResult(couponsByActionGuids.get(1001L), 2001L, 23L);
    }

    @Test
    public void testCheckCouponApplyRestriction() {
        // given
        Purchase purchase = new PurchaseBuilder()
                .addAction(couponActionResult(1000L, 2000L, 1L))
                .addAction(couponActionResult(1001L, 2001L, 999L))
                .addAction(couponActionResult(1002L, 2002L, null))
                .addAction(couponActionResult(1003L, 2003L, 0L))
                .addAction(couponActionResult(1004L, 2004L, 100L))
                .addAction(couponActionResult(1005L, 2005L, 100L))
                .addAction(couponActionResult(1006L, 2006L, 555L))
                .addAction(couponActionResult(1007L, 2007L, 555L))
                .build();

        AdvertisingActionEntity[] actions = {
                action(1000L, 10.0),
                action(1001L, 10.1),
                action(1002L, 10.2),
                action(1003L, 9.8),
                action(1004L, 9.7),
                action(1005L, 8.0),
                action(1006L, 12.2),
                action(1007L, 12.3),
        };

        // when
        service.checkCouponsCountAndDeleteExcess(purchase, 5, actions);

        // then
        Map<Long, ChequeCouponAdvertisingActionResultEntity> couponsByActionGuids = extractActions(purchase);
        assertThat(couponsByActionGuids.size()).isEqualTo(5);
        assertCouponResult(couponsByActionGuids.get(1000L), 2000L, 1L);
        assertCouponResult(couponsByActionGuids.get(1005L), 2005L, 100L);
        assertCouponResult(couponsByActionGuids.get(1004L), 2004L, 100L);
        assertCouponResult(couponsByActionGuids.get(1006L), 2006L, 555L);
        assertCouponResult(couponsByActionGuids.get(1007L), 2007L, 555L);
    }

    @Test
    public void testCheckCouponLowPriority() {
        // given
        Purchase purchase = new PurchaseBuilder()
                .addAction(couponActionResult(1000L, 2000L, 998L))
                .addAction(couponActionResult(1001L, 2001L, 9999L))
                .addAction(couponActionResult(1002L, 2002L, null))
                .addAction(couponActionResult(1003L, 2003L, 0L))
                .build();

        AdvertisingActionEntity[] actions = {
                action(1000L, 99.0),
                action(1001L, 10.2),
                action(1002L, 10.1),
                action(1003L, 10.0)
        };

        // when
        service.checkCouponsCountAndDeleteExcess(purchase, 2, actions);

        // then
        Map<Long, ChequeCouponAdvertisingActionResultEntity> couponsByActionGuids = extractActions(purchase);
        assertThat(couponsByActionGuids.size()).isEqualTo(2);
        assertCouponResult(couponsByActionGuids.get(1000L), 2000L, 998L);
        assertCouponResult(couponsByActionGuids.get(1003L), 2003L, 0L);
    }

    @Test
    public void testCheckCouponNonUniqueCoupons() {
        // given
        Purchase purchase = new PurchaseBuilder()
                .addAction(couponActionResult(1000L, 2000L, 1L))
                .addAction(couponActionResult(1001L, null, 2L))
                .addAction(couponActionResult(1002L, 2002L, 3L))
                .build();

        AdvertisingActionEntity[] actions = {
                action(1000L, 10.2),
                action(1001L, 10.1),
                action(1002L, 10.0),
        };

        // when
        service.checkCouponsCountAndDeleteExcess(purchase, 2, actions);

        // then
        Map<Long, ChequeCouponAdvertisingActionResultEntity> couponsByActionGuids = extractActions(purchase);
        assertThat(couponsByActionGuids.size()).isEqualTo(2);
        assertCouponResult(couponsByActionGuids.get(1000L), 2000L, 1L);
        assertCouponResult(couponsByActionGuids.get(1001L), null, 2L);
    }

    private void assertCouponResult(AdvertisingActionResultEntity actionResult, Long couponTypeGuid, Long priority) {
        assertThat(actionResult).isInstanceOf(ChequeCouponAdvertisingActionResultEntity.class);

        ChequeCouponAdvertisingActionResultEntity couponResult = (ChequeCouponAdvertisingActionResultEntity) actionResult;
        assertThat(couponResult.getCouponTypeGuid()).isEqualTo(couponTypeGuid);
        assertThat(couponResult.getPriority()).isEqualTo(priority);
    }

    private Map<Long, ChequeCouponAdvertisingActionResultEntity> extractActions(Purchase purchase) {
        return purchase.getAdvertisingActionResults().stream()
                .filter(a -> a instanceof ChequeCouponAdvertisingActionResultEntity)
                .map(ChequeCouponAdvertisingActionResultEntity.class::cast)
                .collect(Collectors.toMap(AdvertisingActionResultEntity::getAdvertisingActionGUID, Function.identity()));
    }

    private AdvertisingActionEntity action(long guid, double priority) {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        action.setGuid(guid);
        action.setPriority(priority);
        return action;
    }

    private ChequeCouponAdvertisingActionResultEntity couponActionResult(long actionGuid, Long couponTypeGuid, Long priority) {
        ChequeCouponAdvertisingActionResultEntity couponResult = new ChequeCouponAdvertisingActionResultEntity();
        couponResult.setPriority(priority);
        couponResult.setAdvertisingActionGUID(actionGuid);
        couponResult.setCouponTypeGuid(couponTypeGuid);
        return couponResult;
    }

    private static class PurchaseBuilder {

        private List<AdvertisingActionResultEntity> actionResults;

        PurchaseBuilder() {
            actionResults = new ArrayList<>();
        }

        PurchaseBuilder addAction(AdvertisingActionResultEntity action) {
            actionResults.add(action);
            return this;
        }

        Purchase build() {
            Purchase purchase = new Purchase();
            purchase.getAdvertisingActionResults().addAll(actionResults);
            return purchase;
        }
    }
}
