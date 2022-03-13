package ru.crystals.pos.loyal.cash.issues;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.loyal.calculation.LoyTransactionCalculator;
import ru.crystals.loyal.model.SimpleShiftInfo;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;
import ru.crystals.pos.loyal.cash.utils.BonusPositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.BonusTxDescriptor;
import ru.crystals.pos.loyal.cash.utils.DiscountPositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.EntitiesTestUtils;
import ru.crystals.pos.loyal.cash.utils.PositionDescriptor;
import ru.crystals.pos.loyal.cash.utils.ReturnPositionDescriptor;

/**
 * Аналогично SRL889TestCase, тест слежения за корректностью TX лояльности в чеках возврата,
 * а также за корректностью бонусных транзакций возврата.
 * В отличие от SRL889TestCase со случайной генерацией кейсов, в данном тесте приводятся конкретные кейсы
 *
 * Created by v.osipov on 28.03.2017.
 */
@RunWith(value = Parameterized.class)
public class LoyTransactionForReturnCalculatorTest {


    private static final long DEFAULT_MONEY_TO_BONUSES_RATE = 1L;



    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {

                {
                        Arrays.asList(new PositionDescriptor(1, 1, 100, 1L), new PositionDescriptor(2, 1, 200, 1L)),
                        Arrays.asList(new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.DISCOUNT_GOODS)),
                        Arrays.asList(new BonusPositionDescriptor(2, 60, 2L)),
                        Arrays.asList(new ReturnPositionDescriptor(2, 1)),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        Arrays.asList(new BonusTxDescriptor(0, -60, 2L)),
                },

                {
                        Arrays.asList(new PositionDescriptor(1, 1, 100, 1L), new PositionDescriptor(2, 1, 200, 1L)),
                        Arrays.asList(new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.DISCOUNT_GOODS)),
                        Arrays.asList(new BonusPositionDescriptor(2, 60, 2L)),
                        Arrays.asList(new ReturnPositionDescriptor(1, 1)),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        Arrays.asList(),
                },

                {
                        Arrays.asList(new PositionDescriptor(1, 1, 100, 1L), new PositionDescriptor(2, 1, 200, 1L)),
                        Arrays.asList(new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.BONUS_SR10)),
                        Arrays.asList(),
                        Arrays.asList(new ReturnPositionDescriptor(2, 1)),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        Arrays.asList(),
                },

                {
                        Arrays.asList(new PositionDescriptor(1, 1, 100, 1L), new PositionDescriptor(2, 1, 200, 1L)),
                        Arrays.asList(new DiscountPositionDescriptor(1, 1, 30, 1L, ActionType.BONUS_SR10)),
                        Arrays.asList(),
                        Arrays.asList(new ReturnPositionDescriptor(1, 1)),
                        DEFAULT_MONEY_TO_BONUSES_RATE,
                        Arrays.asList(new BonusTxDescriptor(30, 30, 1L)),
                }
        });
    }


    public LoyTransactionForReturnCalculatorTest(Collection<PositionDescriptor> positions, Collection<DiscountPositionDescriptor> discounts,
                                                 Collection<BonusPositionDescriptor> bonuses, Collection<ReturnPositionDescriptor> rets,
                                                 long moneyToBonusesRate, Collection<BonusTxDescriptor> expectedBonusTxs) {

        currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(null);

        Collection<PositionEntity> positionEntities = new ArrayList<>();
        for (PositionDescriptor position : positions) {
            positionEntities.add(EntitiesTestUtils.createPosition(currencyHandler, position.number, position.qnty, position.price, 0l));
        }
        originalPurchase = EntitiesTestUtils.createReceipt(positionEntities);



        Map<Long, PositionEntity> posMap = new HashMap<>();
        for (PositionEntity p : originalPurchase.getPositions()) {
            posMap.put(p.getNumber(), p);
        }


        Collection<LoyDiscountPositionEntity> discountPositions = new ArrayList<>();
        for (DiscountPositionDescriptor discount : discounts) {
            discountPositions.add(EntitiesTestUtils.createDiscountPosition(posMap.get(discount.number), discount.qnty, discount.value, discount.guid, discount.actionType ,currencyHandler));
        }

        Collection<LoyBonusPositionEntity> bonusPositions = new ArrayList<>();
        for (BonusPositionDescriptor bonus : bonuses) {
            bonusPositions.add(EntitiesTestUtils.createBonusPosition(posMap.get(bonus.number), bonus.value, bonus.guid));
        }

        loyTransaction = EntitiesTestUtils.createLoyTx(originalPurchase, discountPositions, bonusPositions, moneyToBonusesRate);


        Collection<PositionEntity> retPositions = new ArrayList<>();
        for (ReturnPositionDescriptor ret : rets) {
            retPositions.add(EntitiesTestUtils.createRetPos(posMap.get(ret.number), ret.qnty, currencyHandler));
        }


        returnPurchase = EntitiesTestUtils.createRetReceipt(originalPurchase, retPositions);

        this.expectedBonusTxs = expectedBonusTxs;
    }

    /**
     * Оригинальный чек (продажи)
     */
    private PurchaseEntity originalPurchase;

    /**
     * TX лояльности оригинального чека (продажи)
     */
    private LoyTransactionEntity loyTransaction;

    /**
     * Возвратный чек
     */
    private PurchaseEntity returnPurchase;

    /**
     * текущий округлятор денег
     */
    private CurrencyHandler currencyHandler;

    /**
     * Ожидаемые операции возврата / отмены начисления бонусов
     */
    private Collection<BonusTxDescriptor> expectedBonusTxs;



    @Test
    public void calculateLoyTransactionForReturnTest() {

        // when
        LoyTransactionEntity loyTx = LoyTransactionCalculator.calculateLoyTransactionForReturn(loyTransaction,
            returnPurchase, originalPurchase, new SimpleShiftInfo(), currencyHandler, true, true);

        // then
        // размеры позиционных скидок должны совпасть:
        Map<Long, Long> expected = new HashMap<Long, Long>();
        Map<Long, Long> actual = new HashMap<Long, Long>();
        for (PositionEntity p : returnPurchase.getPositions()) {
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

        assertThat(actual).isEqualTo(expected);


        // сумма скидок должна совпасть:
        assertThat((Long) loyTx.getDiscountValueTotal()).isEqualTo(returnPurchase.getDiscountValueTotal());


        // проверка бонусных транзакций для чека возврата:
        Collection<BonusTxDescriptor> actualBonusTxs = EntitiesTestUtils.collectBonusTxs(loyTx);
        assertThat(actualBonusTxs).containsOnlyElementsOf(expectedBonusTxs).hasSize(expectedBonusTxs.size());
    }


}
