package ru.crystals.pos.loyal.cash.issues;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.loyal.calculation.LoyTransactionCalculator;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.loyal.cash.utils.BonusPositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.BonusTxDescriptor;
import ru.crystals.pos.loyal.cash.utils.DiscountPositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.loyal.cash.utils.PositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.PurchaseDescriptor;

/**
 * тест слежения за корректностью TX лояльности при разделении чека
 *
 * Created by v.osipov on 29.03.2017.
 */
@RunWith(value = Parameterized.class)
public class LoyTransactionForDividedCalculatorTest {


    private static final long DEFAULT_MONEY_TO_BONUSES_RATE = 1L;



    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {

                /**
                * Чек с двумя позициями
                * На первую позицию применяется скидка, а за вторую позицию начислены бонусы
                * Считаем, что чек должен поделиться на два и на две транзакции соответственно
                */
                {
                        new PurchaseDescriptor(
                                1L,
                                new PositionDescriptor(1, 1, 100, 1L),
                                new PositionDescriptor(2, 1, 200, 2L)
                        ),
                        Arrays.asList(
                                new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.DISCOUNT_GOODS)
                        ),
                        Arrays.asList(
                                new BonusPositionDescriptor(2, 60, 2L)
                        ),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        30L,
                        Arrays.asList(
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                2L,
                                                new PositionDescriptor(1, 1, 70, 1L)
                                        )),
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Arrays.asList()
                                ),
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                3L,
                                                new PositionDescriptor(1, 1, 200, 2L)
                                        )),
                                        Arrays.asList(new BonusTxDescriptor(0, 60, 2L)),
                                        Arrays.asList(),
                                        Arrays.asList()
                                )
                        )
                },
                /**
                * Чек с двумя позициями
                * На первую позицию применяются бонусы SET10 как скидка
                * Считаем, что чек должен поделиться на два и на две транзакции соответственно
                */
                {
                        new PurchaseDescriptor(
                                1L,
                                new PositionDescriptor(1, 1, 100, 1L),
                                new PositionDescriptor(2, 1, 200, 2L)
                        ),
                        Arrays.asList(
                                new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.BONUS_SR10)
                        ),
                        Arrays.asList(),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        30L,
                        Arrays.asList(
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                2L,
                                                new PositionDescriptor(1, 1, 70, 1L)
                                        )),
                                        Arrays.asList(new BonusTxDescriptor(-30L, -30L, 1L)),
                                        Arrays.asList(),
                                        Arrays.asList()
                                ),
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                3L,
                                                new PositionDescriptor(1, 1, 200, 2L)
                                        )),
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Arrays.asList()
                                )
                        )
                },
                /**
                 * Чек с двумя позициями
                 * На первую позицию применяются бонусы ЦФТ (Спасибо от сбербанка) как скидка
                 * Считаем, что чек должен поделиться на два и на две транзакции соответственно
                */
                {
                        new PurchaseDescriptor(
                                1L,
                                new PositionDescriptor(1, 1, 100, 1L),
                                new PositionDescriptor(2, 1, 200, 2L)
                        ),
                        Arrays.asList(
                                new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.BONUS_CFT)
                        ),
                        Arrays.asList(),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        30L,
                        Arrays.asList(
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                2L,
                                                new PositionDescriptor(1, 1, 70, 1L)
                                        )),
                                        Arrays.asList(),
                                        Arrays.asList(new BonusTxDescriptor(0L, 0L, 0L, -30L, 0L)),
                                        Arrays.asList()
                                ),
                                new ExpectedResult(
                                        EntitiesTestUtils.createReceipt(new PurchaseDescriptor(
                                                3L,
                                                new PositionDescriptor(1, 1, 200, 2L)
                                        )),
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Arrays.asList()
                                )
                        )
                },
        });
    }





    public LoyTransactionForDividedCalculatorTest( //--Preconditions start
                                                  PurchaseDescriptor purchase,
                                                  Collection<DiscountPositionDescriptor> discounts,
                                                  Collection<BonusPositionDescriptor> bonuses,
                                                  Long moneyToBonusesRate,
                                                  //--Preconditions end
                                                  //--Expected results below
                                                  Long expectedDiscountValue,
                                                  Collection<ExpectedResult> expectedResults) {
        currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

        //Создаем оригинальный чек
        originalPurchase = EntitiesTestUtils.createReceipt(currencyHandler, purchase);

        prepareExpectedChecks(expectedResults);

        //Выцепим все позиции из чека в мапу
        Map<Long, PositionEntity> posMap = getLongPositionEntityMap();

        //Применим скидки
        Collection<LoyDiscountPositionEntity> discountPositions = new ArrayList<>();
        for (DiscountPositionDescriptor discount : discounts) {
            discountPositions.add(EntitiesTestUtils.createDiscountPosition(posMap.get(discount.number), discount.qnty, discount.value, discount.guid, discount.actionType ,currencyHandler));
        }

        //Запишем бонусы
        Collection<LoyBonusPositionEntity> bonusPositions = new ArrayList<>();
        for (BonusPositionDescriptor bonus : bonuses) {
            bonusPositions.add(EntitiesTestUtils.createBonusPosition(posMap.get(bonus.number), bonus.value, bonus.guid));
        }

        loyTransaction = EntitiesTestUtils.createLoyTx(originalPurchase, discountPositions, bonusPositions, moneyToBonusesRate);

        this.results = expectedResults;
    }

    private void prepareExpectedChecks(Collection<ExpectedResult> expectedResults) {
        for(ExpectedResult expectedResult : expectedResults){
            prepareExpectedPositions(expectedResult);
        }
    }

    private void prepareExpectedPositions(ExpectedResult expectedResult) {
        for(PositionEntity position : expectedResult.purchaseEntity.getPositions()){
            for(PositionEntity positionEntityInOriginal : originalPurchase.getPositions()){
                if(positionEntityInOriginal.getId().equals(position.getId())){
                    correctPosDiscount(position, positionEntityInOriginal);
                    correctNumberInOriginalPurchase(position, positionEntityInOriginal);
                }
            }
        }
    }

    private void correctNumberInOriginalPurchase(PositionEntity position, PositionEntity positionEntityInOriginal) {
        position.setNumberInOriginal(positionEntityInOriginal.getNumber());
    }

    private void correctPosDiscount(PositionEntity position, PositionEntity positionEntityInOriginal) {
        long sum = position.getPriceStart() *  position.getQnty();
        long discount = positionEntityInOriginal.getPriceStart() * positionEntityInOriginal.getQnty() - sum;
        position.setSumDiscount(discount);
        position.setSum(sum - discount);
    }

    private Map<Long, PositionEntity> getLongPositionEntityMap() {
        Map<Long, PositionEntity> posMap = new HashMap<>();
        for (PositionEntity p : originalPurchase.getPositions()) {
            posMap.put(p.getNumber(), p);
        }
        return posMap;
    }

    /**
     * Оригинальный чек
     */
    private PurchaseEntity originalPurchase;

    /**
     * TX лояльности оригинального чека
     */
    private LoyTransactionEntity loyTransaction;
    
    /**
     * Подчек
     */
    private Collection<ExpectedResult> results;

    /**
     * текущий округлятор денег
     */
    private CurrencyHandler currencyHandler;




    @Test
    public void calculateLoyTransactionForDividedTest() {

        for(ExpectedResult result : results){

            PurchaseEntity subPurchase = result.purchaseEntity;

            // when
            LoyTransactionEntity loyTx = LoyTransactionCalculator.calculateLoyTransactionForDividedPurchase(subPurchase, loyTransaction);

            // then
            // размеры позиционных скидок должны совпасть:
            Map<Long, Long> expected = new HashMap<>();
            Map<Long, Long> actual = new HashMap<>();
            for (PositionEntity p : subPurchase.getPositions()) {
                expected.put(p.getNumber(), p.getSumDiscount());
                actual.put(p.getNumber(), 0L);
            }

            for (LoyDiscountPositionEntity ldpe : loyTx.getDiscountPositions()) {
                if (!actual.containsKey((long) ldpe.getPositionOrder())) {
                    actual.put((long) ldpe.getPositionOrder(), ldpe.getDiscountAmount());
                } else {
                    actual.put((long) ldpe.getPositionOrder(), actual.get((long) ldpe.getPositionOrder()) + ldpe.getDiscountAmount());
                }
            }

            testPositionDiscounts(expected, actual);

            // сумма скидок должна совпасть:
            testDoscountSumm(subPurchase, loyTx);

            // проверка бонусных транзакций в разделенном чеке:
            testBonusTransactions(result, loyTx);

            testSberbankTransactions(result, loyTx);
        }

    }

    private void testSberbankTransactions(ExpectedResult result, LoyTransactionEntity loyTx) {
        if(CollectionUtils.isNotEmpty(result.expectedSberBonusTxs)){
            Collection<BonusTxDescriptor> actualSberBonusTxs = EntitiesTestUtils.collectSberBonusTxs(loyTx);
            assertThat(actualSberBonusTxs).containsOnlyElementsOf(result.expectedSberBonusTxs).hasSize(result.expectedSberBonusTxs.size());
        }
    }

    private void testBonusTransactions(ExpectedResult result, LoyTransactionEntity loyTx) {
        if(CollectionUtils.isNotEmpty(result.expectedBonusTxs)){
            Collection<BonusTxDescriptor> actualBonusTxs = EntitiesTestUtils.collectBonusTxs(loyTx);
            assertThat(actualBonusTxs).containsOnlyElementsOf(result.expectedBonusTxs).hasSize(result.expectedBonusTxs.size());
        }
    }

    private void testDoscountSumm(PurchaseEntity subPurchase, LoyTransactionEntity loyTx) {
        assertThat((Long) loyTx.getDiscountValueTotal()).isEqualTo(subPurchase.getDiscountValueTotal());
    }

    private void testPositionDiscounts(Map<Long, Long> expected, Map<Long, Long> actual) {
        assertThat(actual).isEqualTo(expected);
    }

    public static class ExpectedResult {
        public PurchaseEntity purchaseEntity;
        public Collection<BonusTxDescriptor> expectedBonusTxs;
        public Collection<BonusTxDescriptor> expectedSberBonusTxs;
        public Collection<BonusTxDescriptor> expectedPlastekBonusTxs;

        public ExpectedResult(PurchaseEntity purchaseEntity, Collection<BonusTxDescriptor> expectedBonusTxs, Collection<BonusTxDescriptor> expectedSberBonusTxs, Collection<BonusTxDescriptor> expectedPlastekBonusTxs) {
            this.purchaseEntity = purchaseEntity;
            this.expectedBonusTxs = expectedBonusTxs;
            this.expectedSberBonusTxs = expectedSberBonusTxs;
            this.expectedPlastekBonusTxs = expectedPlastekBonusTxs;
        }
    }

}
