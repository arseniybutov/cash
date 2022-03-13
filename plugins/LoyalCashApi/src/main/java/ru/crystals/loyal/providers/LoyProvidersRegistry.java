package ru.crystals.loyal.providers;

import java.util.Map;
import java.util.List;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.types.ExtendedCardType;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.loyal.cash.service.LoyProvidersRegistryWrapper;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.techprocess.TechProcessStage;

/**
 * Знаток всех {@link LoyProvider поставщиков "услуг лояльности"} (и их очередности/приоритетов), что позволяет реализациям данного интерфейса
 * производить вычисление всех "преференций" (от всех "поставщиков"), что можно получить по чеку.
 * Для привязки Autowire получать надо через {@link LoyProvidersRegistryWrapper}, иначе может быть неактуальный реестр
 *
 * @author aperevozchikov
 * @since 10.2.23.0
 */
public interface LoyProvidersRegistry {

    /**
     * Подсчитает все "преференции" для указанного чека.
     *
     * @param originalReceipt
     *            оригинальное представление чека - еще без всяких скидок
     * @param loyTechProcess
     *            техпроцесс лояльности
     * @param techProcess
     *            кассовый техпроцесс.
     *            <br/>
     *            NOTE: эта хрень нужна только ради алкогольных ограничений
     * @return {@code null}, если список "поставщиков" "услуг лояльности", готовых предоставить перференцию по данному чеку, пуст (в т.ч., если сам
     *         чек невалиден)
     * @throws LoyProcessingException
     *             при возникновении ошибок при "калькуляции" "преференций"
     */
    Purchase process(PurchaseEntity originalReceipt, ILoyTechProcess loyTechProcess, TechProcessServiceAsync techProcess)
        throws LoyProcessingException;

    /**
     * Данный метод вызывается по завершению расчета скидок и позволяет сторонним поставщикам лояльности добавить в указанный кассовый чек информацию о "своих" скидках.
     *
     * @param originalReceipt кассовый чек
     * @param receipt все скидки, примененные на этот чек; среди них каждый поставщик лояльности может поискать "свои" и отредактировать кассовый чек. если надо
     */
    void attachAppliedDiscountInfo(PurchaseEntity originalReceipt, Purchase receipt);

    /**
     * Получить расширеный тип купона
     * @param couponData номер купона и другие входные данные
     * @param loyTechProcess техпроцесс лояльности
     * @return Расширеный тип купона. Никогда не вернет {@code null}. Если ненайдена вернет {@code CardTypes.CardNotFound}
     */
    ExtendedCardType getCouponType(CardData couponData, ILoyTechProcess loyTechProcess);

    void confirmDiscount(PurchaseEntity purchaseEntity, LoyTransactionEntity loyTransactionEntity, FeedbackTime feedbackTime);

    /**
     * Оповещение поставщиков лояльности о том, что будет запущен процесс формирования фискального чека.
     *
     * @param purchase Чек, процесс фискализации которого идет
     */
    void beforeFiscalize(PurchaseEntity purchase);

    /**
     * Оповещение поставщиков лояльности о том, что будет запущен процесс фискализации указанного чека.
     *
     * @param purchase
     *            Чек, процесс фискализации которого идет
     * @param check
     *            Cам фискальный чек
     * @param loyTransaction
     *            Транзакция лояльности
     */
    void preparePurchaseFiscalization(PurchaseEntity purchase, Check check, LoyTransactionEntity loyTransaction);

    /**
     * Оповещение поставщиков лояльности о том, что запущен процесс фискализации указанного чека. Метод вызывается после фискализации и печати чека,
     * но ДО гашения купонов и печати сервисных документов.
     *
     * @param purchase
     *            Чек, процесс фискализации которого идет
     */
    void purchaseFiscalized(PurchaseEntity purchase, LoyTransactionEntity loyTransaction);

    /**
     * Отмена расчета скидок
     * @param purchase Чек
     */
    void checkCanceled(PurchaseEntity purchase);

    /**
     * Оповещение поставщиков лояльности о том, что расчет скидок на указанный чек отменен (нажали отмену подытога, прокатали [другую] карту после подытога,
     * аннулировали чек). Метод вызывается ДО удаления из БД TX расчета скидок
     *
     * @param purchase чек, расчитанные скидки которого собираются удалить
     */
    void cancelDiscount(PurchaseEntity purchase);

    /**
     * Полностью ли сформирована транзакция лояльности с точки зрения всех провайдеров лояльности
     * @param loyTransaction
     * @return
     */
    boolean isLoyTransactionComplete(LoyTransactionEntity loyTransaction);

    /**
     * Необходима ли транзакция лояльности для обработки на указанном этапе тех процесса
     * (например во время и после фискализации или при отправке отложенных запросов)
     * Выполнение опроса всех провайдеров лояльности
     * @return
     */
    boolean isLoyTransactionRequiredForStage(TechProcessStage stage);

    /**
     * Нужно ли пересчитывать скидки при смене типа оплаты
     * @param purchase чек
     * @return true если надо пересчитывать скидки при смене типа оплаты
     */
    boolean isNeedSubtotalAfterFirstPaymentTypeChanged(PurchaseEntity purchase);

    Map<String, BonusDiscountType> getDefaultBonusDiscountTypes();

    List<String> getAllProvidersExclude(List<String> excludeList);
}
