package ru.crystals.loyal.providers;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.enums.ActionPluginAttributes;
import ru.crystals.discounts.enums.MessageDisplayTime;
import ru.crystals.discounts.interfaces.ActionPluginAttributable;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.calculation.PurchaseUtils;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.loyal.LoyaltyScenario;
import ru.crystals.pos.loyal.cash.persistence.LoyFeedbackDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Родительский класс для провайдеров лояльности, которым необходим доступ к некоторому набору общих функций
 * <p>
 * Created by v.osipov on 07.09.2017.
 */
public abstract class AbstractLoyProvider implements LoyProvider {

    // injected
    /**
     * Поставщик РА SET10. Нужен для поиска специально выделенной РА, соответствующей данному провайдеру лояльности
     */
    private LoyActionsProvider actionsProvider;

    /**
     * Для поиска необработанных feedback'ов
     */
    @Autowired
    private LoyFeedbackDao loyFeedbackDao;

    /**
     * Сценарий взаимодействия с визуализацией
     */
    @Autowired(required = false)
    private LoyaltyScenario scenario;

    public LoyActionsProvider getActionsProvider() {
        return actionsProvider;
    }

    public void setActionsProvider(LoyActionsProvider actionsProvider) {
        this.actionsProvider = actionsProvider;
    }

    @Override
    public CardTypes getCouponType(CardData couponData, ILoyTechProcess techProcess) {
        return null;
    }

    /**
     * Создаст отклик с единственным результатом: показать указанное предупредительное сообщение кассиру после подытога
     * @param receipt чек, поступивший в обработку данному поставщику лояльности
     * @param guid    GUID РА, на основе которой предоставляем преференции
     * @param message сам текст сообщения, что надо показать кассиру
     * @return не {@code null} - в крайнем случае будет NPE - если 1й аргумент == null
     */
    protected Purchase createWarnResponse(Purchase receipt, Long guid, String message) {
        Purchase result = receipt.cloneWithDisc();
        PurchaseUtils.removeAllDiscounts(result);
        MessageAdvertisingActionResultEntity msg = new MessageAdvertisingActionResultEntity();
        msg.setAdvertisingActionGUID(guid);
        msg.setPurchase(result);
        msg.setDisplayTime(MessageDisplayTime.SUBTOTAL);
        msg.getOperatorMsg().add(message);
        ArrayList<AdvertisingActionResultEntity> actionResult = new ArrayList<>();
        actionResult.add(msg);
        result.setAdvertisingActionResults(actionResult);
        return result;
    }

    /**
     * Вернет соответствующую провайдеру действующую РА типа "скидка определяется внешним провайдером"
     * @param receipt чек, для которого ищем РА
     * @return {@code null}, если не удалось обнаружить такую РА или не указано имя провайдера
     */
    protected AdvertisingActionEntity peekAction(PurchaseEntity receipt) {
        if (actionsProvider == null || getProviderName() == null) {
            return null;
        }
        Collection<AdvertisingActionEntity> allActions = actionsProvider.getActions(receipt);
        return allActions.stream().filter(this::isExternalLoyaltyAction).findFirst().orElse(null);
    }

    private boolean isExternalLoyaltyAction(AdvertisingActionEntity action) {
        if (action == null) {
            return false;
        }
        return action.getDeserializedPlugins().stream().anyMatch(this::isExternalLoyaltyPlugin);
    }

    private boolean isExternalLoyaltyPlugin(IActionPlugin plugin) {
        if (!(plugin instanceof ActionPluginAttributable)) {
            return false;
        }
        ActionPluginAttributable aPlugin = (ActionPluginAttributable) plugin;
        return getProviderName().equals(aPlugin.getPluginAttributesValues().get(ActionPluginAttributes.EXTERNAL_LOYALTY));
    }

    protected boolean isHavingFeedbacks(LoyTransactionEntity loyTransactionEntity) {
        // если имеются необработанные фидбеки (неотправленные запросы) в соотв. процессинг лояльности по соотв. чеку
        Collection<LoyExtProviderFeedback> feedbacks = getLoyFeedbackDao().getFeedbackByProviderAndChequeId(getProviderName(), loyTransactionEntity.getPurchaseId());
        return CollectionUtils.isNotEmpty(feedbacks);
    }

    protected LoyFeedbackDao getLoyFeedbackDao() {
        return loyFeedbackDao;
    }

    protected Optional<LoyaltyScenario> getScenario() {
        return Optional.ofNullable(scenario);
    }
}
