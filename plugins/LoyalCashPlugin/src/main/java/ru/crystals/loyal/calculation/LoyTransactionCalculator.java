package ru.crystals.loyal.calculation;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.model.SimpleShiftInfo;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ru.crystals.discount.processing.entity.LoyTransactionEntity.OPERATION_TYPE_RETURN;

/**
 * Рассчитывает размеры скидок для чеков возврата.
 *
 * @author P.Pavlov
 * @author Anton Martynov &lt;amartynov@crystals.ru&gt;
 * @author aperevozchikov
 */
@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S135", "squid:S2864"})
public class LoyTransactionCalculator {

    /**
     * Черный ящик
     */
    private static final Logger log = LoggerFactory.getLogger(LoyTransactionCalculator.class);


    /**
     * Вернет соотвествие м/у номером позиции указанного чека возврата и номером позиции оригинального чека (продажи).
     *
     * @param returnPurchase
     *            чек возврата
     * @return ключ - номер позиции в чеке возврата; значение - номер соотвествующей [возвращаемой] позиции в оригинальном чеке (продажи).
     *         <p/>
     *         Никогда не вернет <code>null</code> - в крайнем случае вернет пустую коллекцию
     */
    private static Map<Long, Long> getPosNumsMapping(PurchaseEntity returnPurchase) {
        Map<Long, Long> result = new HashMap<>();

        if (returnPurchase == null || CollectionUtils.isEmpty(returnPurchase.getPositions())) {
            log.error("getPosNumsMapping(PurchaseEntity): the argument is EMPTY!");
            return result;
        }

        for (PositionEntity p : returnPurchase.getPositions()) {
            if (p == null || p.getNumberInOriginal() == null) {
                log.error("getPosNumsMapping(PurchaseEntity): invalid position was detected: {}", p);
                continue;
            }

            result.put(p.getNumber(), p.getNumberInOriginal());
        }

        return result;
    }

    /**
     * Вернет соотвествие м/у номером позиции оригинального чека и номером позиции указанного производного чека (подчека / чека возврата)
     *
     * @param subPurchase производный чек
     * @return соотвествие м/у номером позиции оригинального чека и номером позиции указанного производного чека
     */
    private static Map<Long, Long> getOrigToNewPosNumsMapping(PurchaseEntity subPurchase) {
        Map<Long, Long> result = new HashMap<>();

        if (subPurchase == null || CollectionUtils.isEmpty(subPurchase.getPositions())) {
            log.error("getOrigToNewPosNumsMapping(PurchaseEntity): the argument is EMPTY!");
            return result;
        }

        for (PositionEntity p : subPurchase.getPositions()) {
            if (p == null || p.getNumberInOriginal() == null) {
                log.error("getOrigToNewPosNumsMapping(PurchaseEntity): invalid position was detected: {}", p);
                continue;
            }

            result.put(p.getNumberInOriginal(), p.getNumber());
        }

        return result;
    }

    /**
     * вернет размеры скидок, данных на позиции указанного чека возврата.
     *
     * @param returnPurchase
     *            чек возврата, размеры скидок по позициям которого надо вернуть
     * @param currencyHandler
     *            "округлятор" - знает как обращаться с деньгами
     * @return номер позиции в чеке возврата - в качестве ключа; размер скидки данной на эту позицию - в качестве значения, в копейках.
     *         <p/>
     *         Никогда не вернет <code>null</code>; во множестве {@link Map#values() значений} тоже не будет <code>null</code>'ей
     */
    private static Map<Long, Long> getDiscountValues(PurchaseEntity returnPurchase, CurrencyHandler currencyHandler) {
        Map<Long, Long> result = new HashMap<>();

        if (returnPurchase == null || CollectionUtils.isEmpty(returnPurchase.getPositions()) || currencyHandler == null) {
            log.error("getDiscountValues(PurchaseEntity, CurrencyHandler): at least one of the arguments is empty: " +
                "either returnPurchase [{}], or currencyHandler [{}]", returnPurchase, currencyHandler);
            return result;
        }

        for (PositionEntity p : returnPurchase.getPositions()) {
            if (p == null || p.getPriceStart() == null || p.getSum() == null || p.getQnty() == null) {
                log.error("getDiscountValues(PurchaseEntity, CurrencyHandler): invalid position [{}] was detected!", p);
                continue;
            }

            // Оригинальная стоимость этой позиции:
            long originalSum = currencyHandler.getPositionSum(p.getPriceStart(), p.getQnty());

            // скидка - есть разность м/у оригинальной и итоговой суммами:
            result.put(p.getNumber(), originalSum - p.getSum());
        }

        return result;
    }

    /**
     * Просто вернет количество товара в позициях указанного чека.
     *
     * @param receipt
     *            чек, количества товаров по позициям которого надо вернуть
     * @return {@link PositionEntity#getNumber() номер позиции} - в качестве ключа; {@link PositionEntity#getQnty() количество товара} в этой позиции
     *         - в качестве значения.
     *         <p/>
     *         Никогда не вернет <code>null</code>
     */
    private static Map<Long, Long> getProductsCounts(PurchaseEntity receipt) {
        Map<Long, Long> result = new HashMap<>();

        if (receipt == null || CollectionUtils.isEmpty(receipt.getPositions())) {
            log.error("getProductsCounts(PurchaseEntity): the receipt [{}] is EMPTY!", receipt);
            return result;
        }

        for (PositionEntity p : receipt.getPositions()) {
            if (p == null || p.getQnty() == null) {
                log.error("getProductsCounts(PurchaseEntity): invalid position [{}] was detected!", p);
                continue;
            }

            result.put(p.getNumber(), p.getQnty());
        }

        return result;
    }

    /**
     * Вернет TX лояльности для указанного чека возврата.
     *
     * @param loyTransaction
     *            TX лояльности оригинального чека (продажи)
     * @param returnPurchase
     *            чек возврата, для которого надо подсчитать скидки
     * @param originalSuperPurchase
     *            оригинальный чек (продажи) возврат по которому оформляем
     * @param shiftInfo
     *            информация о текущей смене (магазин - касса - номер смены) - просто чтоб заполнить соотвествующие поля в возвращаемой TX лоялности
     * @param currencyHandler
     *            "округлятор" - знает как обращаться с деньгами
     * @param retAccrued признак возврата начисленных бонусов
     * @param retChargedOff признак возврата списанных бонусов
     * @return <code>null</code> - если не удалось создать TX лояльности по любой причине (в т.ч. и в случае если в этом чеке возврата не должно быть
     *         никаких скидок)
     */
    public static LoyTransactionEntity calculateLoyTransactionForReturn(LoyTransactionEntity loyTransaction, PurchaseEntity returnPurchase,
        PurchaseEntity originalSuperPurchase, SimpleShiftInfo shiftInfo, CurrencyHandler currencyHandler, boolean retAccrued, boolean retChargedOff) {
        LoyTransactionEntity result = null;

        if (log.isTraceEnabled()) {
            log.trace("entering calculateLoyTransactionForReturn(LoyTransactionEntity, PurchaseEntity, PurchaseEntity, SimpleShiftInfo, CurrencyHandler, boolean, boolean). " +
                "The arguments are: loyTransaction [{}], returnPurchase [{}], originalSuperPurchase [{}], shiftInfo [{}], retAccrued [{}], retChargedOff [{}]",
                    loyTransaction, returnPurchase, originalSuperPurchase, shiftInfo, retAccrued, retChargedOff);
        }

        if (loyTransaction == null || returnPurchase == null) {
            log.warn("leaving calculateLoyTransactionForReturn(LoyTransactionEntity, PurchaseEntity, PurchaseEntity, SimpleShiftInfo, CurrencyHandler, boolean, boolean). " +
                    "Either the original loy-TX [{}], or the return receipt [{}] is NULL!", loyTransaction, returnPurchase);
            return result;
        }

        // 1. определим какие позиции оригинального чека возвращаем:
        //  <№ позиции в чеке возврата, № позиции в оригинальном чеке>:
        Map<Long, Long> retToOrigPosMapping = getPosNumsMapping(returnPurchase);
        //  обратная мапа:
        Map<Long, Long> origToRetPosMapping = new HashMap<>();
        for (Long retPosNo : retToOrigPosMapping.keySet()) {
            Long origPosNo = retToOrigPosMapping.get(retPosNo);
            origToRetPosMapping.put(origPosNo, retPosNo);
        }

        // 2. выясним какое количество товара было в оригинальном чеке
        //  <№ позиции в чеке возврата, количество товара в этой возвращаемой позиции в оригинальном чеке>:
        Map<Long, Long> retPosToOrigQnty = new HashMap<>();
        Map<Long, Long> origPosToQnty = getProductsCounts(originalSuperPurchase);
        for (Long posNo : retToOrigPosMapping.keySet()) {
            Long origPosNo = retToOrigPosMapping.get(posNo);
            Long qnty = origPosNo == null ? null : origPosToQnty.get(origPosNo);

            retPosToOrigQnty.put(posNo, qnty == null ? 0L : qnty);
        }

        // 3. и сколько товара из позиции сейчас возвращаем
        //  <№ позиции в чеке возврата, количество возвращаемого товара>:
        Map<Long, Long> retPosToQnty = getProductsCounts(returnPurchase);

        // 4. а теперь: скидка в чеке возврата == скидка в оригинальной позиции * долю возвращаемого товара из этой позиции
        //  - вот так и будем считать (примерно)

        // 4.1. начнем готовить результат:
        result = loyTransaction.clone();
        result.setFilename(null);
        result.setOperationType(OPERATION_TYPE_RETURN);
        result.setSaleTime(new Date());
        result.setTransactionTime(result.getSaleTime());
        if (shiftInfo != null) {
            result.setCashNumber(shiftInfo.cashNumber);
            result.setShiftNumber(shiftInfo.shiftNumber);
            result.setShopNumber(shiftInfo.shopNumber);
        }
        result.setInn(returnPurchase.getInn());
        result.setPurchaseNumber(returnPurchase.getNumber() != null ? returnPurchase.getNumber() : -1);
        result.setPurchaseAmount(returnPurchase.getCheckSumStart());

        result.setChequeCoupons(null);
        result.setQuestionaries(null);
        result.setBonusSberbankTransactions(null);
        result.setTokensSiebelTransactions(null);
        result.setSetApiLoyaltyTransactions(null);
        result.setChequeAdverts(null);

        // 4.2. выкинем скидки, данные на позиции, что сейчас не возвращаем все равно:
        for (Iterator<LoyDiscountPositionEntity> it = result.getDiscountPositions().iterator(); it.hasNext();) {
            LoyDiscountPositionEntity ldpe = it.next();
            if (ldpe == null || !origToRetPosMapping.containsKey((long) ldpe.getPositionOrder())) {
                // либо неликвид, либо эту позицию не возвращаем:
                it.remove();
                continue;
            }

            // 4.3. и заодно перенумеруем позиции:
            //  чтобы LoyDiscountPositionEntity.positionOrder указывал на номер позиции в чеке возврата:
            ldpe.setPositionOrder(origToRetPosMapping.get((long) ldpe.getPositionOrder()).intValue());
        } // for it

        // 4.4. осталось размеры скидок откорретировать
        //  включая размер скидки на весь чек:
        result.setDiscountValueTotal(0L);
        // скидки по позициям: <номер позиции в чеке возврата, размер скидки в копейках>:
        Map<Long, Long> posDiscounts = new HashMap<>();
        // скидки по позициям: <номер позиции в чеке возврата, все скики на эту позицию>:
        Map<Long, Collection<LoyDiscountPositionEntity>> posLdpes = new HashMap<>();
        for (LoyDiscountPositionEntity ldpe : result.getDiscountPositions()) {
            long posNo = ldpe.getPositionOrder();
            // количество в оригинальном чеке:
            long origQnty = retPosToOrigQnty.get(posNo) == null ? 0 : retPosToOrigQnty.get(posNo);
            // возвращаемое количество
            long retQnty = retPosToQnty.get(posNo) == null ? 0 : retPosToQnty.get(posNo);
            // доля возвращаемого от оригинального количества товара в позиции:
            double share = origQnty == 0 ? 0.0 : 1.0 * retQnty / origQnty;

            // типа "точное" количество, на которое сработала скидка:
            //  == количество товара в оригинальной TX лояльности * долю возвращвемого товара (долю от оригинального количества в чеке продажи):
            double exactQnty = 1.0 * share * ldpe.getQnty();
            ldpe.setQnty(Math.round(exactQnty));

            // типа "точный" размер скидки на эту позицию
            //  == размер скидки на позицию в оригинальной TX лояльности * долю:
            double exactDiscount = 1.0 * share * ldpe.getDiscountAmount();
            ldpe.setDiscountAmount(currencyHandler.round(exactDiscount));

            // полный размер скидки на позицию:
            if (!posDiscounts.containsKey(posNo)) {
                posDiscounts.put(posNo, ldpe.getDiscountAmount());
            } else {
                posDiscounts.put(posNo, posDiscounts.get(posNo) + ldpe.getDiscountAmount());
            }

            // состав скидок на эту позицию
            if (!posLdpes.containsKey(posNo)) {
                posLdpes.put(posNo, new LinkedList<>());
            }
            posLdpes.get(posNo).add(ldpe);

            result.setDiscountValueTotal(result.getDiscountValueTotal() + ldpe.getDiscountAmount());
        }

        // валидация: откорретировать размеры скидок немного? чтоб рамер скидки по чеку совпал с суммой скидок?
        //  result.getDiscountValueTotal() == returnPurchase.getDiscountValueTotal()

        // <номер позиции в чеке возврата, общий размер скидки на эту позицию>:
        Map<Long, Long> discountValues = getDiscountValues(returnPurchase, currencyHandler);
        for (Long posNo : discountValues.keySet()) {
            long expected = discountValues.get(posNo);
            Long actual = posDiscounts.get(posNo);
            if (actual != null && actual == expected || actual == null && expected == 0) {
                // на эту позицию скидки посчитали правильно - продолжим
                continue;
            }
            // видимо, возникли ошибки округления при распределении скидок на эту позицию
            if (actual == null) {
                // вообще плохо! на эту позицию (по TX лояльности) вообще не было скидок, а должны быть (по чеку продажи)!
                //  такого вообще не может быть. в любом случае как исправить не знаем
                log.error("calculateLoyTransactionForReturn(LoyTransactionEntity, PurchaseEntity, PurchaseEntity, SimpleShiftInfo, CurrencyHandler): " +
                    "impossible to distribute discounts! Position #{} of refund receipt [{}] has no discount positions " +
                    "BUT its total discount is positive [{}]",
                    posNo, returnPurchase, expected);
                // продолжим
                continue;
            }

            // попытаемся эту разницу в размерах скидок "размазать" по LDPE:
            //  вот столько скидки на эту позицию надо добавить:
            long delta = expected - actual;
            boolean distributedSuccessfully = distributeExtraDiscount(posLdpes.get(posNo), delta);
            if (distributedSuccessfully) {
                // не забудем пересчитать общий размер скидки:
                result.setDiscountValueTotal(result.getDiscountValueTotal() + delta);
            } else {
                // не вышло!
                log.error("calculateLoyTransactionForReturn(LoyTransactionEntity, PurchaseEntity, PurchaseEntity, SimpleShiftInfo, CurrencyHandler): " +
                        "impossible to distribute discounts! extra discount [{}] to distribute over position #{} of refund receipt [{}] having " +
                        "total discount of [{}]",
                        delta, posNo, returnPurchase, actual);
            }
        } // for posNo


        // 5. обрабатываем бонусы, данные согласно возвращаемым позициям, остальные удаляем
        for (Iterator<LoyBonusPositionEntity> it = result.getBonusPositions().iterator(); it.hasNext();) {
            LoyBonusPositionEntity lbpe = it.next();
            Long newPositionNumber = (lbpe != null) ? origToRetPosMapping.get((long) lbpe.getPositionOrder()) : null;
            if (newPositionNumber == null) {
                it.remove();
                continue;
            }
            lbpe.setPositionOrder(newPositionNumber.intValue());

            long posNo = lbpe.getPositionOrder();
            long origQnty = retPosToOrigQnty.get(posNo) == null ? 0 : retPosToOrigQnty.get(posNo);
            long retQnty = retPosToQnty.get(posNo) == null ? 0 : retPosToQnty.get(posNo);
            // доля возвращаемого от оригинального количества товара в позиции:
            double share = origQnty == 0 ? 0.0 : 1.0 * retQnty / origQnty;

            // кол-во возвращаемых бонусов
            double exactBonuses = 1.0 * share * lbpe.getBonusAmount();
            lbpe.setBonusAmount(currencyHandler.round(exactBonuses));

        }

        reCalculateLoyBonusTransactions(result, loyTransaction, retAccrued, retChargedOff);

        log.trace("leaving calculateLoyTransactionForReturn(LoyTransactionEntity, PurchaseEntity, PurchaseEntity, SimpleShiftInfo, CurrencyHandler). " +
            "The result is: {}", result);

        return result;
    }

    /**
     * Вернет TX лояльности для указанного подчека
     *
     * @param subPurchase подчек
     * @param parentTransaction TX лояльности оригинального чека
     * @return TX лояльности подчека
     */
    public static LoyTransactionEntity calculateLoyTransactionForDividedPurchase(PurchaseEntity subPurchase, LoyTransactionEntity parentTransaction) {

        Map<Long, Long> origToRetPosMapping = getOrigToNewPosNumsMapping(subPurchase);

        LoyTransactionEntity subTransaction = parentTransaction.clone();
        subTransaction.setInn(subPurchase.getInn());
        subTransaction.setPurchaseAmount(subPurchase.getCheckSumStart());

        long discountAmount = 0L;

        for (Iterator<LoyDiscountPositionEntity> it = subTransaction.getDiscountPositions().iterator(); it.hasNext();) {
            LoyDiscountPositionEntity ldpe = it.next();

            Long newPositionNumber = (ldpe != null) ? origToRetPosMapping.get((long) ldpe.getPositionOrder()) : null;
            if (newPositionNumber == null) {
                it.remove();
                continue;
            }
            ldpe.setPositionOrder(newPositionNumber.intValue());
            discountAmount += ldpe.getDiscountAmount();
        }

        for (Iterator<LoyBonusPositionEntity> it = subTransaction.getBonusPositions().iterator(); it.hasNext();) {
            LoyBonusPositionEntity lbpe = it.next();
            Long newPositionNumber = (lbpe != null) ? origToRetPosMapping.get((long) lbpe.getPositionOrder()) : null;
            if (newPositionNumber == null) {
                it.remove();
                continue;
            }
            lbpe.setPositionOrder(newPositionNumber.intValue());
        }

        subTransaction.setDiscountValueTotal(discountAmount);
        subPurchase.setDiscountValueTotal(discountAmount);

        reCalculateLoyBonusTransactions(subTransaction, parentTransaction, true, true);
        return calculateLoySberPlastekTransaction(subTransaction);
    }

    private static LoyTransactionEntity calculateLoySberPlastekTransaction(LoyTransactionEntity subTransaction) {
        //Поделим DiscountValueTotal: Это нужно для того, чтобы в базу сохранились правильные loy_bonus_sberbank_transactions и loy_bonus_plastek_transactions
        long sberbankBonuses = 0L;
        long plastekBonuses = 0L;

        List<LoyBonusSberbankTransactionEntity> sberbankTransactionsEntity  = subTransaction.getBonusSberbankTransactions();
        List<LoyBonusPlastekTransactionEntity> plastekTransactionsEntity    = subTransaction.getBonusPlastekTransactions();

        for(LoyDiscountPositionEntity loyDiscountPositionEntity : subTransaction.getDiscountPositions()){
            LoyAdvActionInPurchaseEntity advAction = loyDiscountPositionEntity.getAdvAction();
            if(advAction != null){
                switch (advAction.getActionType()){
                    case BONUS_CFT:
                        sberbankBonuses += loyDiscountPositionEntity.getDiscountAmount();
                        break;
                    case BONUS_PT:
                        plastekBonuses += loyDiscountPositionEntity.getDiscountAmount();
                        break;
                    default:
                        // остальные кейсы не интересуют
                }
            }
        }

        //Нулевые транзакции писать не надо

        for (Iterator<LoyBonusSberbankTransactionEntity> iterSber = sberbankTransactionsEntity.iterator(); iterSber.hasNext();){
            LoyBonusSberbankTransactionEntity loyBonusSberbankTr = iterSber.next();
            if(sberbankBonuses == 0L){
                iterSber.remove();
                continue;
            }
            loyBonusSberbankTr.setBnsChange(-sberbankBonuses);
            loyBonusSberbankTr.setAmount(sberbankBonuses);
        }
        //Нулевые транзакции писать не надо
        for (Iterator<LoyBonusPlastekTransactionEntity> iterPlastek = plastekTransactionsEntity.iterator(); iterPlastek.hasNext(); ){
            LoyBonusPlastekTransactionEntity loyBonusPlastekTr = iterPlastek.next();
            if(plastekBonuses == 0L){
                iterPlastek.remove();
                continue;
            }
            loyBonusPlastekTr.setBnsChange(-plastekBonuses);
        }
        return subTransaction;
    }

    /**
     * Перерассчитывает бонусные транзакции для транзакции лояльности на основе скидок и бонусов данных на позиции
     *
     * @param newLoyTx новая TX лояльности (чека возврата / дочернего чека)
     * @param originalLoyTx TX лояльности оригинального чека
     * @param retAccrued признак возврата начисленных бонусов
     * @param retChargedOff признак возврата списанных бонусов
     */
    private static void reCalculateLoyBonusTransactions(LoyTransactionEntity newLoyTx, LoyTransactionEntity originalLoyTx, boolean retAccrued, boolean retChargedOff) {

        if (newLoyTx == null || CollectionUtils.isEmpty(newLoyTx.getBonusTransactions())) {
            // нет бонусных транзакций для обработки
            return;
        }

        for (Iterator<LoyBonusTransactionEntity> it = newLoyTx.getBonusTransactions().iterator(); it.hasNext(); ) {

            LoyBonusTransactionEntity bonusTx = it.next();

            if (bonusTx.getAdvAction() == null || bonusTx.getAdvAction().getGuid() == null || bonusTx.getBonusAmount() == 0) {
                it.remove();
                continue;
            }
            // списание бонусов как скидки или отмена списания

            if (bonusTx.getBonusAmount() < 0) {
                if (OPERATION_TYPE_RETURN == newLoyTx.isOperationType() && !retChargedOff) {
                    // запрет возврата списанных как скидка бонусов
                    it.remove();
                    continue;
                }

                long operTypeMultiplier = newLoyTx.isOperationType() ? -1 : 1;
                long bonusesOfTypeDiscountAmount = getDiscountAmountOfAction(newLoyTx, bonusTx.getAdvAction().getGuid());

                bonusTx.setBonusAmount(operTypeMultiplier * (long) Math.floor(1.0 * bonusesOfTypeDiscountAmount *
                        bonusTx.getBonusAmount() / bonusTx.getSumAmount()));
                bonusTx.setSumAmount(operTypeMultiplier * bonusesOfTypeDiscountAmount);

            } else {
                // начисление бонусов или отмена начисления

                if (OPERATION_TYPE_RETURN == newLoyTx.isOperationType() && !retAccrued) {
                    // запрет возврата начисленных бонусов
                    it.remove();
                    continue;
                }

                long operTypeMultiplier = newLoyTx.isOperationType() ? 1 : -1;

                if (originalLoyTx != null && originalLoyTx.getBonusPositions().isEmpty()) {
                    // если в Tx лояльности оригинального чека нет информации о распределении бонусов по позициям,
                    // рассчитываем пропорционально:
                    // доля суммы части чека от оригинальной суммы чека:
                    double share = originalLoyTx.getPurchaseAmount() == 0 ? 0.0 : 1.0 * newLoyTx.getPurchaseAmount() / originalLoyTx.getPurchaseAmount();
                    long bonusesPart = (long) Math.floor(share * bonusTx.getBonusAmount());

                    bonusTx.setBonusAmount(operTypeMultiplier * bonusesPart);

                } else {
                    long bonusesOfTypeAmount = getBonusesAmountOfAction(newLoyTx, bonusTx.getAdvAction().getGuid());
                    bonusTx.setBonusAmount(operTypeMultiplier * bonusesOfTypeAmount);
                }

                bonusTx.setSumAmount(0L);
            }

            if (bonusTx.getBonusAmount() == 0) {
                it.remove();
            }
        }
    }

    private static long getDiscountAmountOfAction(LoyTransactionEntity newLoyTx, long bonusTxActionGuid) {
        long discountAmount = 0;
        for (LoyDiscountPositionEntity discountPosition : newLoyTx.getDiscountPositions()) {
            if (discountPosition.getAdvAction() != null && discountPosition.getAdvAction().getGuid() != null &&
                    discountPosition.getAdvAction().getGuid() == bonusTxActionGuid) {
                discountAmount += discountPosition.getDiscountAmount();
            }
        }
        return discountAmount;
    }

    private static long getBonusesAmountOfAction(LoyTransactionEntity newLoyTx, long bonusTxActionGuid) {
        long bonusesAmount = 0;
        for (LoyBonusPositionEntity bonusPosition : newLoyTx.getBonusPositions()) {
            if (bonusPosition.getBonusAmount() > 0 && bonusPosition.getAdvAction() != null &&
                    bonusPosition.getAdvAction().getGuid() != null && bonusPosition.getAdvAction().getGuid() == bonusTxActionGuid) {
                bonusesAmount += bonusPosition.getBonusAmount();
            }
        }
        return bonusesAmount;
    }

    /**
     * "Размажет" указанный дополнительный размер скидки по указанным позиционным скидкам.
     *
     * @param dicountPositions
     *            позиционные скидки, по {@link LoyDiscountPositionEntity#getDiscountAmount() суммам} которых надо "размазать" указанную
     *            дополнительную скидку
     * @param delta
     *            скидка, что надо добавить к указанным позиционным скидкам;
     *            <p/>
     *            NOTE: должна быть примерно +-1 коп.
     * @return <code>true</code>, если "размазать" скидку удалось, иначе - <code>false</code>
     * @throws NullPointerException
     *             если в аргументе-коллекции есть хоть один <code>null</code>
     */
    private static boolean distributeExtraDiscount(Collection<LoyDiscountPositionEntity> dicountPositions, long delta) {
        boolean result;

        if (CollectionUtils.isEmpty(dicountPositions)) {
            // нет скидок, по которым "размазывать" скидку
            return false;
        }

        // общая сумма позиционных скидок:
        long totalDiscount = 0L;
        for (LoyDiscountPositionEntity ldpe : dicountPositions) {
            totalDiscount += ldpe.getDiscountAmount();
        }
        if (delta < 0 && totalDiscount < Math.abs(delta)) {
            // скидку распределить не сможем. никак
            return false;
        }
        // скидку распределить сможем:
        result = true;
        List<LoyDiscountPositionEntity> ldpes = new ArrayList<>(dicountPositions);
        // самые большие скидки - вперед:
        ldpes.sort((o1, o2) -> (int) Math.signum(o2.getDiscountAmount() - o1.getDiscountAmount()));
        if (delta >= 0) {
            // просто увеличим на эту дельту самую большую скидку - и все:
            ldpes.get(0).setDiscountAmount(ldpes.get(0).getDiscountAmount() + delta);
        } else {
            // будем мучительно уменьшать скидки - начиная с самой большой
            for (LoyDiscountPositionEntity ldpe : ldpes) {
                long distributeNow = Math.min(ldpe.getDiscountAmount(), Math.abs(delta));
                ldpe.setDiscountAmount(ldpe.getDiscountAmount() - distributeNow);
                delta += distributeNow;
                if (delta == 0) {
                    // размазывание завершено
                    break;
                }
            } // for ldpe
        }

        return result;
    }

}
