package ru.crystals.loyal.providers;

import java.util.Collection;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.techprocess.TechProcessStage;

/**
 * Поставщик "услуг лояльности": может дать скидку на чек, сгенерить купон, рекламный буклет, насчитать бонусы, и другую лояльную хрень.
 * 
 * @author aperevozchikov
 */
public interface LoyProvider {
    /**
     * Идентификтор поставщика лояльности Set10
     */
    String SET10_PROVIDER_NAME = "set-loy-provider";

    /**
     * Подсчитает скидки и другие "преференции" лояльности для указанного чека.
     * <p/>
     * NOTE: разрешается делить позиции, что пришли в {@code receipt}, но запрещено их "схлопывать".
     * <p/>
     * NOTE2: {@code Position#getCost() цены} и {@code Position#getOriginalCost() оригинальные цены} позиций результата должны быть такими же, какими
     * они пришли в {@code receipt}.
     * <p/>
     * NOTE3: допускается модификация (вплоть до полной отмены/удаления) скидок, данных предыдущими поставщиками "услуг лояльности" -
     * т.е. {@code receipt} может быть отмодифицирован.
     *
     * @param receipt
     *            чек, <em>с уже данными скидками/преференциями другими "поставщиками лояльности"</em>
     *            NOTE: из данного чека могут быть удалены "преференции" (и заменены "преференциями" данного поставщика лольяности)
     * @param originalReceipt
     *            оригинальное представление чека - еще без всяких скидок
     * @param techProcess
     *            техпроцесс лояльности
     * @return {@code null}, если данный "поставщик лояльности" не активен (отключен), либо аргументы невалидны; иначе - вернет копию чека-аргумента с
     *         расситанными "преференциями" <em>только этого "поставщика лояльности"</em>
     * @throws LoyProcessingException
     *             при возникновении ошибок при "калькуляции" "преференций"
     */
    Purchase process(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) throws LoyProcessingException;

    /**
     * Данный метод вызывается по завершению расчета скидок и позволяет поставщику лояльности добавить в указанный кассовый чек информацию о "своих" скидках.
     *
     * @param originalReceipt кассовый чек
     * @param receipt все скидки, примененные на этот чек; среди них каждый поставщик лояльности может поискать "свои" и отредактировать кассовый чек. если надо
     * @see #onDiscountCalculationFinished(Purchase, PurchaseEntity, ILoyTechProcess)
     */
    default void attachAppliedDiscountInfo(PurchaseEntity originalReceipt, Purchase receipt) {}

    /**
     * Получить наименование процессинга
     * @return Наименование процессинга
     */
    String getProviderName();

    /**
     * Получить тип купона
     * @param couponData номер купона и другие входные данные
     * @param techProcess техпроцесс лояльности
     * @return Тип купона. Никогда не вернет {@code null}. Если ненайдена вернет {@code CardTypes.CardNotFound}
     */
    CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess);

    /**
     * Подтвердить скидки. Метод вызывается после печати сервисных документов
     * @param purchaseEntity чек
     * @param loyTransactionEntity транзакция лояльности
     * @param feedbackTime время срабатывания
     */
    default void confirmDiscount(PurchaseEntity purchaseEntity, LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime) {}

    /**
     * Оповещение данного поставщика лояльности о том, что запущен процесс фискализации указанного чека. Метод вызывается после фискализации и печати чека,
     * но ДО гашения купонов и печати сервисных документов.
     *
     * @param purchase
     *            Чек, процесс фискализации которого идет
     */
    default void purchaseFiscalized(PurchaseEntity purchase) {}

    /**
     * Отсылает фидбек (отложенное задание на отправку) в внешнюю систему.
     * @param feedback структура для передачи данных на отложенную отправку.
     * @since 10.2.71.0
     * @see #sendFeedback(Collection)
     */
    default void sendFeedback(FeedbackBundle feedback) {}

    /**
     * Отсылает коллекцию фидбеков на отложенную отправку.
     * @param feedbacks коллекция фидбеков на отложенную отправку или пустая коллекция, если таких нет.
     * @implNote в реализации по умолчанию этот метод просто вызывает {@link #sendFeedback(FeedbackBundle)} в цикле!
     * @since 10.2.71.0
     * @see #sendFeedback(FeedbackBundle)
     */
    default void sendFeedback(Collection<FeedbackBundle> feedbacks) {
        for(FeedbackBundle feedback : feedbacks) {
            sendFeedback(feedback);
        }
    }

    /**
     * Чек аннулирован\удален
     * @param purchase
     */
    default void checkCanceled(PurchaseEntity purchase) {}

    /**
     * Оповещение данного поставщика лояльности о том, что расчет скидок на указанный чек отменен (нажали отмену подытога, прокатали [другую] карту после подытога,
     * аннулировали чек). Метод вызывается ДО удаления из БД TX расчета скидок
     *
     * @param purchase чек, расчитанные скидки которого собираются удалить
     */
    default void cancelDiscount(PurchaseEntity purchase) {};

    /**
     * Полностью ли сформирована транзакция лояльности с точки зрения провайдера лояльности
     *
     * @param loyTransactionEntity транзакция лояльности
     * @return
     */
    default boolean isLoyTransactionComplete(LoyTransactionEntity loyTransactionEntity) {
        return true;
    }

    /**
     * Оповещение данного поставщика лояльности о том, что будет запущен процесс формирования фискального чека.
     * @param purchase кассовый чек
     */
    default void beforeFiscalize(PurchaseEntity purchase) {}

    /**
     * Оповещение данного поставщика лояльности о том, что будет запущен процесс фискализации указанного чека.
     *
     * @param purchase кассовый чек
     * @param check фискальный чек
     * @param loyTransaction транзакция лояльности
     * @return
     */
    default void preparePurchaseFiscalization(PurchaseEntity purchase, Check check, LoyTransactionEntity loyTransaction) {}

    /**
     * Оповещение данного поставщика лояльности о том, что запущен процесс фискализации указанного чека.
     * Метод вызывается после фискализации и печати чека, но ДО гашения купонов и печати сервисных документов.
     * @param purchase
     * @return
     */
    default void purchaseFiscalized(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        purchaseFiscalized(purchase);
    }

    /**
     * Необходима ли транзакция лояльности для обработки на указанном этапе тех процесса
     * (например во время и после фискализации или при отправке отложенных запросов)
     * @return
     */
    default boolean isLoyTransactionRequiredForStage(TechProcessStage stage) {
        return false;
    }

    /**
     * Нужно ли с этим чеком что-то делать в провайдере лояльности
     * Например, Manzana считает скидки только для чеков, которые содержат свою карту
     * @param purchase
     * @return
     */
    default boolean isPurchaseSuitable(Purchase purchase) {
        return true;
    }

    /**
     * Нужно ли пересчитывать скидки при смене типа оплаты
     * @param purchase чек
     * @return true если надо пересчитывать скидки при смене типа оплаты
     */
    default boolean isNeedSubtotalAfterFirstPaymentTypeChanged(PurchaseEntity purchase) {
        return false;
    }

    default BonusDiscountType getDefaultBonusDiscountType() {
        return BonusDiscountType.BONUS_SR10;
    }

    /**
     * Оповещает провайдер лояльности о начале расчета скидок. На этом этапе провайдер лояльности
     * может добавить дополнительную информацию, необходимую для основного расчета.
     * @param purchase чек лояльности
     * @param originalPurchase чек
     * @param techProcess техпроцесс лояльности
     * @since 10.2.71.0
     */
    default void onDiscountCalculationStarted(Purchase purchase, PurchaseEntity originalPurchase, ILoyTechProcess techProcess) {
    }

    /**
     * Оповещает провайдер лояльности о завершении расчета скидок. На этом этапе провайдер лояльности
     * может поправить расчет скидок, например, изменить потолки списания бонусов по своей карте.
     * @param receipt финальный результат расчета скидок
     * @param originalReceipt исходный чек
     * @param techProcess техпроцесс лояльности
     * @return результат правки чека провайдером лояльности
     * @throws LoyProcessingException если в процессе обработки события произошли ошибки
     * @see #attachAppliedDiscountInfo(PurchaseEntity, Purchase)
     */
    default Purchase onDiscountCalculationFinished(Purchase receipt, PurchaseEntity originalReceipt, ILoyTechProcess techProcess) throws LoyProcessingException {
        return receipt;
    }
}
