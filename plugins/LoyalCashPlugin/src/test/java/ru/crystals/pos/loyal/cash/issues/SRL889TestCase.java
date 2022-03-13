package ru.crystals.pos.loyal.cash.issues;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.loyal.calculation.LoyTransactionCalculator;
import ru.crystals.loyal.model.SimpleShiftInfo;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;

/**
 * Данный примитивный тест должен следить за тем, чтобы TX лояльности в чеках возврата были корректны/валидны (как минимум суммы скидок на позицию
 * совпадали с {@link PositionEntity#getSumDiscount() размером скидки на позицию}) - и чтоб ошибки, подобные <a
 * href="https://crystals.atlassian.net/browse/SRL-889">SRL-889</a> не возникали/повторялись после рефакторингов кода.
 *
 * @author aperevozchikov
 */
@RunWith(value = Parameterized.class)
public class SRL889TestCase {

    /**
     * минималное количество товара в позиции
     */
    private static final long MIN_POS_QNTY = 1L;

    /**
     * максимальное количество товара в позиции
     */
    private static final long MAX_POS_QNTY = 10000L;

    /**
     * минимальная ЦЕНА позиции, копеек
     */
    private static final long MIN_POS_PRICE = 1L;

    /**
     * максимальная ЦЕНА позиции, копеек
     */
    private static final long MAX_POS_PRICE = 10000000L;

    /**
     * минимальное количество позиций в чеке
     */
    private static final long MIN_POS_COUNT = 1L;

    /**
     * максимальное количество позиций в чеке
     */
    private static final long MAX_POS_COUNT = 10L;

    /**
     * минимальное количество скидок на позицию
     */
    private static final long MIN_POS_DISCOUNTS = 0L;

    /**
     * максимальное количество скидок на позицию
     */
    private static final long MAX_POS_DISCOUNTS = 3L;

    /**
     * Минимальный процент скидки (от стоимости позиции)
     */
    private static final long MIN_DISCOUNT_PERCENT = 0L;

    /**
     * Максимальный процент скидки (от стоимости позиции)
     */
    private static final long MAX_DISCOUNT_PERCENT = 100L;

    /**
     * Минимальное кол-во уже существующих чеков возврата
     */
    private static final long MIN_RET_RECEIPTS = 0L;

    /**
     * Максимальное кол-во уже существующих чеков возврата
     */
    private static final long MAX_RET_RECEIPTS = 2L;

    /**
     * Минимальное кол-во бонусных транзакций
     */
    private static final long MIN_BONUS_TX_COUNT = 0L;

    /**
     * Максимальное кол-во уже бонусных транзакций
     */
    private static final long MAX_BONUS_TX_COUNT = 3L;

    /**
     * Минимальное кол-во списываемых/начисляемых бонусов
     */
    private static final long MIN_BONUSES_AMOUNT = 1L;

    /**
     * Максимальное кол-во списываемых/начисляемых бонусов
     */
    private static final long MAX_BONUSES_AMOUNT = 10000000L;

    /**
     * Прогоним тест для Российского и Белорусского калькуляторов.
     *
     * @return
     */
    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] { {null}, {"BYR"}});
    }

    /**
     * Конструктор, принимающий на вход наш тестовый параметр
     *
     * @param currencyCode
     */
    public SRL889TestCase(String currencyCode) {
        currencyHandler = new CurrencyHandlerFactory().getCurrencyHandler(currencyCode);
    }

    /**
     * TX лояльности оригинального чека (продажи)
     */
    private LoyTransactionEntity loyTransaction;

    /**
     * Возвратный чек
     */
    private PurchaseEntity returnPurchase;

    /**
     * Оригинальный чек (продажи)
     */
    private PurchaseEntity originalSuperPurchase;

    /**
     * текущий округлятор денег
     */
    private CurrencyHandler currencyHandler;

    @Before
    public void init() {
        originalSuperPurchase = createRandomReceipt(currencyHandler);
        loyTransaction = createRandomTx(originalSuperPurchase, currencyHandler);

        // создадим несколько чеков возврата - до нашего тестового возврата:
        for (int i = 0; i < getRandomValue(MIN_RET_RECEIPTS, MAX_RET_RECEIPTS); i++) {
            PurchaseEntity ret = createRandomRetReceipt(originalSuperPurchase, currencyHandler);
            originalSuperPurchase.getSubPurchases().add(ret);
        }

        // а теперь - наш тестовый чек возврата:
        returnPurchase = createRandomRetReceipt(originalSuperPurchase, currencyHandler);
    }

    /**
     * сам тест: всегда должен корректно считать скидки для чеков возврата.
     */
    @Test
    public void shouldCalcDiscountsCorrectlyForRefundReceiptsAlways() {
        // given
        //  уже все настроено

        // when
        LoyTransactionEntity loyTx = LoyTransactionCalculator.calculateLoyTransactionForReturn(loyTransaction,
            returnPurchase, originalSuperPurchase, new SimpleShiftInfo(), currencyHandler, true, true);

        // then
        //  размеры позиционных скидок должны совпасть:
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

        Assert.assertEquals(String.format("Position discounts are not equal!%n" +
            "original receipt was: %s%n" +
            "its loy-tx was: %s%n" +
            "and the refund receipt was: %s%n" +
            "the currency handler was: %s%n",
            receiptToString("", originalSuperPurchase),
            loyTxToString("", loyTx),
            receiptToString("", returnPurchase),
            currencyHandler),
            expected, actual);

        // ну и сумма скидок должна совпасть:
        Assert.assertEquals(String.format("Total discounts are not equal!%n" +
                "original receipt was: %s%n" +
                "its loy-tx was: %s%n" +
                "and the refund receipt was: %s%n" +
                "the currency handler was: %s%n",
                receiptToString("", originalSuperPurchase),
                loyTxToString("", loyTx),
                receiptToString("", returnPurchase),
                currencyHandler),
                returnPurchase.getDiscountValueTotal(), (Long) loyTx.getDiscountValueTotal());

    }


    // возможно, в какой *Utils можно будет потом вынести:

    /**
     * Просто для логгирования: вернет представление указанной TX лояльности в виде строки.
     *
     * @param prefix
     *            префикс, что надо добавить к результату
     * @param loyTx
     *            скидка, чье строковое представление надо вернуть
     * @return не <code>null</code>
     */
    private static String loyTxToString(String prefix, LoyTransactionEntity loyTx) {
        StringBuilder result = new StringBuilder();

        result.append(prefix).append(String.format("loy-tx [total-disc: %s]", loyTx.getDiscountValueTotal())).append("\n");
        if (!loyTx.getDiscountPositions().isEmpty()) {
            result.append(prefix).append("pos-discounts:").append("\n");
            for (LoyDiscountPositionEntity pos : loyTx.getDiscountPositions()) {
                result.append(prefix).append(loyPosToString("\t", pos)).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Просто для логгирования: вернет представление указанной скидки на позицию в виде строки.
     *
     * @param prefix
     *            префикс, что надо добавить к результату
     * @param ldpe
     *            скидка на позицию, чье строковое представление надо вернуть
     * @return не <code>null</code>
     */
    private static String loyPosToString(String prefix, LoyDiscountPositionEntity ldpe) {
        return String.format("%sloy-pos[pos-num: %s, qnty: %s, disc-amount: %s]",
            prefix, ldpe.getPositionOrder(), ldpe.getQnty(), ldpe.getDiscountAmount());
    }

    /**
     * Просто для логгирования: вернет представление указанного чека в виде строки.
     *
     * @param prefix
     *            префикс, что надо добавить к результату
     * @param receipt
     *            чек, чье строковое представление надо вернуть
     * @return не <code>null</code>
     */
    private static String receiptToString(String prefix, PurchaseEntity receipt) {
        StringBuilder result = new StringBuilder();

        result.append(prefix).append(String.format("receipt [sum-start: %s, sum-end: %s]", receipt.getCheckSumStart(), receipt.getCheckSumEnd())).append("\n");
        if (!receipt.getPositions().isEmpty()) {
            result.append(prefix).append("positions:").append("\n");
            for (PositionEntity pos : receipt.getPositions()) {
                result.append(prefix).append(posToString("\t", pos)).append("\n");
            }
        }
        if (!receipt.getSubPurchases().isEmpty()) {
            result.append(prefix).append("sub-purchases:").append("\n");
            for (PurchaseEntity p : receipt.getSubPurchases()) {
                result.append(prefix).append(receiptToString("\t", p)).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Просто для логгирования: вернет представление указанной позиции в виде строки.
     *
     * @param prefix
     *            префикс, что надо добавить к результату
     * @param pos
     *            позиция, чье строковое представление надо вернуть
     * @return не <code>null</code>
     */
    private static String posToString(String prefix, PositionEntity pos) {
        return String.format("%spos[num: %s, price-start: %s, qnty: %s, disc: %s, num-in-orig: %s]",
            prefix, pos.getNumber(), pos.getPriceStart(), pos.getQnty(), pos.getSumDiscount(), pos.getNumberInOriginal());
    }

    /**
     * Создаст случайный чек возврата для указанного чека продажи.
     *
     * @param originalReceipt
     *            оригинальный чек продажи
     * @param currencyHandler
     *            округлятор денег
     * @return не <code>null</code>
     */
    private static PurchaseEntity createRandomRetReceipt(PurchaseEntity originalReceipt, CurrencyHandler currencyHandler) {
        PurchaseEntity result = new PurchaseEntity();

        // для начала узнаем сколько осталось вернуть по каждой позиции
        // <номер позиции в оригинальном чеке, сколько товара в этой позиции осталось не возвращенным>:
        Map<Long, Long> qntyLeft = new HashMap<Long, Long>();
        // <номер позиции в оригинальном чеке, стоимость еще не возвращенного товара в этой позиции>:
        Map<Long, Long> sumLeft = new HashMap<Long, Long>();
        for (PositionEntity p : originalReceipt.getPositions()) {
            qntyLeft.put(p.getNumber(), p.getQnty());
            sumLeft.put(p.getNumber(), p.getSum());
        }
        for (PurchaseEntity p : originalReceipt.getSubPurchases()) {
            for (PositionEntity pos : p.getPositions()) {
                qntyLeft.put(pos.getNumberInOriginal(), qntyLeft.get(pos.getNumberInOriginal()) - pos.getQnty());
                sumLeft.put(pos.getNumberInOriginal(), sumLeft.get(pos.getNumberInOriginal()) - pos.getSum());
            }
        }

        // а теперь создаем чек возврата
        result.setReturn();
        result.setCheckSumStart(0L);
        result.setDiscountValueTotal(0L);
        long posNo = 0;
        for (PositionEntity p : originalReceipt.getPositions()) {
            boolean retIt = RandomUtils.nextBoolean();
            if (!retIt) {
                continue;
            }
            posNo++;
            PositionEntity retPos = createRandomRetPos(p, qntyLeft.get(p.getNumber()), sumLeft.get(p.getNumber()), currencyHandler);
            retPos.setNumber(posNo);

            result.getPositions().add(retPos);
            result.setCheckSumStart(result.getCheckSumStart() + retPos.getSum() + retPos.getSumDiscount());
            result.setDiscountValueTotal(result.getDiscountValueTotal() + retPos.getSumDiscount());
        }

        return result;
    }

    /**
     * Создаст и вернет случайную "позицию возврата" для указанной оригинальной позиции.
     *
     * @param originalPosition
     *            оригинальная позиция, что собираемся возвращать
     * @param qntyLeft
     *            количество товара, что осталось в этой позиции для возврата
     * @param sumLeft
     *            сумма, что осталось вернуть по этой позиции, копеек
     * @param currencyHandler
     *            округлятор денег
     * @return не <code>null</code>
     */
    private static PositionEntity createRandomRetPos(PositionEntity originalPosition, long qntyLeft, long sumLeft, CurrencyHandler currencyHandler) {
        PositionEntity result = new PositionEntity();

        long qnty = getRandomValue(MIN_POS_COUNT, Math.min(qntyLeft, originalPosition.getQnty()));
        long sum = qnty == qntyLeft || qntyLeft == 0 ? sumLeft : currencyHandler.round(1.0 * qnty / qntyLeft * sumLeft);

        result.setQnty(qnty);
        result.setPriceStart(originalPosition.getPriceStart());
        result.setSum(sum);
        long discount = currencyHandler.getPositionSum(originalPosition.getPriceStart(), qnty) - sum;
        if (originalPosition.getSumDiscount() == 0) {
            discount = 0L;
        }
        result.setSumDiscount(discount);
        result.setNumberInOriginal(originalPosition.getNumber());

        return result;
    }

    /**
     * Создает случайную TX лояльности на указанный чек.
     * <p/>
     * NOTE: чек, переданный в этот метод, будет "пересчитан": конечные стоимости и размеры скидок у позиций и самого чека будут пересчитаны.
     *
     * @param receipt
     *            чек (продажи), на для которой надо создать TX лояльности
     * @param currencyHandler
     *            округлятор денег
     * @return TX лояльности для этого чека
     */
    private static LoyTransactionEntity createRandomTx(PurchaseEntity receipt, CurrencyHandler currencyHandler) {
        LoyTransactionEntity result = new LoyTransactionEntity();

        result.setDiscountValueTotal(0L);

        long bonusTransactionsAmount = getRandomValue(MIN_BONUS_TX_COUNT, MAX_BONUS_TX_COUNT);
        for (int i = 0;  i < bonusTransactionsAmount; i++) {
            LoyBonusTransactionEntity bte = createRandomBonusTx(receipt, result, currencyHandler);
            result.getBonusTransactions().add(bte);
        }

        for (PositionEntity pos : receipt.getPositions()) {
            long discountsAmount = getRandomValue(MIN_POS_DISCOUNTS, MAX_POS_DISCOUNTS);
            for (int i = 0;  i < discountsAmount; i++) {
                LoyDiscountPositionEntity ldpe = createRandomLoyTx(pos, currencyHandler, pos.getSum());

                result.getDiscountPositions().add(ldpe);
                result.setDiscountValueTotal(result.getDiscountValueTotal() + ldpe.getDiscountAmount());
            }
        }

        receipt.setDiscountValueTotal(result.getDiscountValueTotal());

        return result;
    }

    /**
     * Создает случайную бонусную транзакцию
     *
     * @param receipt чек продажи
     * @param loyTx TX лояльности для этого чека
     * @param currencyHandler округлятор денег
     * @return бонусная транзакция
     */
    private static LoyBonusTransactionEntity createRandomBonusTx(PurchaseEntity receipt, LoyTransactionEntity loyTx, CurrencyHandler currencyHandler) {
        LoyBonusTransactionEntity bte = new LoyBonusTransactionEntity();

        LoyAdvActionInPurchaseEntity appliedAction = new LoyAdvActionInPurchaseEntity();
        int actionTypeOrdinal = (int) getRandomValue(0, ActionType.values().length-1);
        appliedAction.setActionType(ActionType.values()[actionTypeOrdinal]);

        bte.setAdvAction(appliedAction);
        bte.setDiscountCard(RandomStringUtils.randomNumeric(8));

        boolean bonusesChargeOff = RandomUtils.nextBoolean();
        if (bonusesChargeOff) {
            long sumAmount = 0;
            long discountPercent = getRandomValue(MIN_DISCOUNT_PERCENT, MAX_DISCOUNT_PERCENT);
            for (PositionEntity pos : receipt.getPositions()) {
                LoyDiscountPositionEntity ldpe = createBonusLoyTx(pos, currencyHandler, discountPercent, bte);
                sumAmount += ldpe.getDiscountAmount();
                loyTx.getDiscountPositions().add(ldpe);
                loyTx.setDiscountValueTotal(loyTx.getDiscountValueTotal() + ldpe.getDiscountAmount());
            }
            bte.setSumAmount(- sumAmount);
            if (sumAmount!=0) bte.setBonusAmount(- getRandomValue(MIN_BONUSES_AMOUNT, MAX_BONUSES_AMOUNT));
        } else {
            bte.setBonusAmount(getRandomValue(MIN_BONUSES_AMOUNT, MAX_BONUSES_AMOUNT));
        }

        return bte;
    }

    /**
     * Создаст случайную скидку на указанную позицию.
     * <p/>
     * NOTE: позиция, переданная в качестве аргумента, будет пересчитана: будут изменены окончательная стоимость и размер скидки.
     *
     * @param pos
     *            позиция, на которую надо дать скидку
     * @param currencyHandler
     *            округлятор денег
     * @param maxDiscount
     *            максимальный размер скидки, что можно дать на эту позицию, в копейках
     * @return скидку на позицию
     */
    private static LoyDiscountPositionEntity createRandomLoyTx(PositionEntity pos, CurrencyHandler currencyHandler, long maxDiscount) {
        LoyDiscountPositionEntity result = new LoyDiscountPositionEntity();

        long discountPercent = getRandomValue(MIN_DISCOUNT_PERCENT, MAX_DISCOUNT_PERCENT);
        long sumDiscount = currencyHandler.roundDown(Math.min(maxDiscount, Math.round(0.01 * discountPercent * pos.getSum())));

        // количество товара, на которое сработала скидка, должно быть таким, чтобы оригинальная стоимость
        //  этого количества товара до скидки была не меньше самой скидки:
        long minCount = pos.getSum() == 0 ? 0 : Math.round(1.0 * pos.getQnty() * sumDiscount / pos.getSum());
        long count = getRandomValue(minCount, pos.getQnty());

        result.setPositionOrder(pos.getNumberInt());
        result.setDiscountAmount(sumDiscount);
        result.setQnty(count);

        pos.setSumDiscount(pos.getSumDiscount() + sumDiscount);
        pos.setSum(pos.getSum() - sumDiscount);

        return result;
    }

    /**
     * Создаст бонусную скидку на указанную позицию.
     *
     * @param pos позиция, на которую надо дать скидку
     * @param currencyHandler округлятор денег
     * @param discountPercent процент скидки
     * @param bte бонусная транзакция списания бонусов
     * @return скидку на позицию
     */
    private static LoyDiscountPositionEntity createBonusLoyTx(PositionEntity pos, CurrencyHandler currencyHandler, long discountPercent, LoyBonusTransactionEntity bte) {
        LoyDiscountPositionEntity result = new LoyDiscountPositionEntity();

        result.setAdvAction(bte.getAdvAction());
        result.setCardNumber(bte.getDiscountCard());

        long sumDiscount = currencyHandler.roundDown(Math.min(pos.getSum(), Math.round(0.01 * discountPercent * pos.getSum())));
        long count = pos.getQnty();

        result.setPositionOrder(pos.getNumberInt());
        result.setDiscountAmount(sumDiscount);
        result.setQnty(count);

        pos.setSumDiscount(pos.getSumDiscount() + sumDiscount);
        pos.setSum(pos.getSum() - sumDiscount);

        return result;
    }

    /**
     * Вернет случайное число в указанном диапазоне.
     *
     * @param min
     * @param max
     * @return
     */
    private static long getRandomValue(long min, long max) {
        return min + Math.round(Math.random() * (max - min));
    }

    /**
     * Создает и возвращает какой-то случайный чек продажи.
     *
     * @param currencyHandler
     *            округлятор денег для сумм этого чека
     * @return чек
     */
    private static PurchaseEntity createRandomReceipt(CurrencyHandler currencyHandler) {
        PurchaseEntity result = new PurchaseEntity();

        long sum = 0;
        long posCount = getRandomValue(MIN_POS_COUNT, MAX_POS_COUNT);
        for (int i = 0; i < posCount; i++) {
            PositionEntity pos = createRandomPosition(currencyHandler);
            pos.setNumber(1L + i);
            result.getPositions().add(pos);

            sum += pos.getSum();
        }
        result.setCheckSumStart(sum);
        result.setSale();

        return result;
    }

    /**
     * Создает и возвращает какую-то случайную позицию.
     *
     * @param currencyHandler
     *            округлятор денег для сумм этой позиции
     * @return позицию
     */
    private static PositionEntity createRandomPosition(CurrencyHandler currencyHandler) {
        PositionEntity result = new PositionEntity();

        long qnty = getRandomValue(MIN_POS_QNTY, MAX_POS_QNTY);
        long price = currencyHandler.roundUp(getRandomValue(MIN_POS_PRICE, MAX_POS_PRICE));

        result.setQnty(qnty);
        result.setPriceStart(price);
        result.setSum(currencyHandler.getPositionSum(price, qnty));
        result.setSumDiscount(0L);

        return result;
    }

}
