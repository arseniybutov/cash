package ru.crystals.pos.loyal.calculation;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.testng.asserts.Assertion;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.calculation.LoyTransactionCalculator;
import ru.crystals.loyal.model.SimpleShiftInfo;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandlerFactory;

public class LoyTransactionCalculatorTest {

    @Test
    public void calculateLoyTransactionForReturnAllBonusReturnAllowedTest() {
        PurchaseEntity retPurchase = getPurchaseEntity();

        LoyTransactionEntity entity = getLoyTransactionEntity();
        LoyTransactionEntity ret = LoyTransactionCalculator.calculateLoyTransactionForReturn(entity, retPurchase, retPurchase, new SimpleShiftInfo(),
                new CurrencyHandlerFactory().getCurrencyHandler(null), true, true);
        Assertions.assertThat(ret.getBonusTransactions()).hasSize(2);
        Assertions.assertThat(ret.getBonusTransactions().stream().anyMatch(v -> v.getAdvAction().getGuid().equals(200L) && v.getBonusAmount() == -100)).isTrue();
        Assertions.assertThat(ret.getBonusTransactions().stream().anyMatch(v -> v.getAdvAction().getGuid().equals(201L) && v.getBonusAmount() == 100)).isTrue();
    }

    @Test
    public void calculateLoyTransactionForReturnAllBonusReturnRestrictedTest() {
        PurchaseEntity retPurchase = getPurchaseEntity();

        LoyTransactionEntity entity = getLoyTransactionEntity();
        LoyTransactionEntity ret = LoyTransactionCalculator.calculateLoyTransactionForReturn(entity, retPurchase, retPurchase, new SimpleShiftInfo(),
                new CurrencyHandlerFactory().getCurrencyHandler(null), false, false);
        Assertions.assertThat(ret.getBonusTransactions()).hasSize(0);
    }

    @Test
    public void calculateLoyTransactionForReturnOnlyAccrueBonusReturnAllowedTest() {
        PurchaseEntity retPurchase = getPurchaseEntity();

        LoyTransactionEntity entity = getLoyTransactionEntity();
        LoyTransactionEntity ret = LoyTransactionCalculator.calculateLoyTransactionForReturn(entity, retPurchase, retPurchase, new SimpleShiftInfo(),
                new CurrencyHandlerFactory().getCurrencyHandler(null), true, false);
        Assertions.assertThat(ret.getBonusTransactions()).hasSize(1);
        Assertions.assertThat(ret.getBonusTransactions().stream().anyMatch(v -> v.getAdvAction().getGuid().equals(200L) && v.getBonusAmount() == -100)).isTrue();
    }

    @Test
    public void calculateLoyTransactionForReturnOnlyWriteOffBonusReturnAllowedTest() {
        PurchaseEntity retPurchase = getPurchaseEntity();

        LoyTransactionEntity entity = getLoyTransactionEntity();
        LoyTransactionEntity ret = LoyTransactionCalculator.calculateLoyTransactionForReturn(entity, retPurchase, retPurchase, new SimpleShiftInfo(),
                new CurrencyHandlerFactory().getCurrencyHandler(null), false, true);
        Assertions.assertThat(ret.getBonusTransactions()).hasSize(1);
        Assertions.assertThat(ret.getBonusTransactions().stream().anyMatch(v -> v.getAdvAction().getGuid().equals(201L) && v.getBonusAmount() == 100)).isTrue();
    }

    private LoyTransactionEntity getLoyTransactionEntity() {
        LoyTransactionEntity entity = new LoyTransactionEntity();
        entity.setOperationType(LoyTransactionEntity.OPERATION_TYPE_RETURN);
        entity.setPurchaseAmount(100);
        entity.getDiscountPositions().add(new LoyDiscountPositionEntity());
        entity.getDiscountPositions().get(0).setAdvAction(new LoyAdvActionInPurchaseEntity());
        entity.getDiscountPositions().get(0).getAdvAction().setGuid(201L);
        entity.getDiscountPositions().get(0).setDiscountAmount(100L);
        entity.getDiscountPositions().get(0).setQnty(1L);
        entity.getDiscountPositions().get(0).setPositionOrder(1);

        LoyBonusTransactionEntity tx = new LoyBonusTransactionEntity();
        tx.setAdvAction(new LoyAdvActionInPurchaseEntity());
        tx.getAdvAction().setGuid(200L);
        tx.setBonusAmount(100);
        tx.setSumAmount(100);
        entity.getBonusTransactions().add(tx);
        tx = new LoyBonusTransactionEntity();
        tx.setAdvAction(new LoyAdvActionInPurchaseEntity());
        tx.getAdvAction().setGuid(201L);
        tx.setBonusAmount(-100);
        tx.setSumAmount(-100);
        entity.getBonusTransactions().add(tx);
        return entity;
    }

    private PurchaseEntity getPurchaseEntity() {
        PurchaseEntity retPurchase = new PurchaseEntity();
        retPurchase.setCheckSumStart(100L);
        retPurchase.getPositions().add(new PositionEntity());
        retPurchase.getPositions().get(0).setNumber(1L);
        retPurchase.getPositions().get(0).setQnty(1L);
        retPurchase.getPositions().get(0).setNumberInOriginal(1L);
        return retPurchase;
    }
}
