package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandler;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.cards.internal.good.processing.FillHolderIdFormMessages;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

/**
 * Содержит состояние автомата "Продажа Дисконтной Карты": собственно описание самого продаваемого объекта (ДК), а также этап техпроцесса продажи
 * (добавления этого товара в чек: ввод номера карты, запрос идентификатора анкеты клиента, запрос на применение продаваемой ДК в текущем чеке и
 * проч.).
 * 
 * @author aperevozchikov
 */
public class DiscountCardModel extends CommonProductPluginModel {

    /**
     * Описание продаваемой позиции (ДК)
     */
    private DiscountCardAsGoodVO card = null;

    /**
     * Индекс текущего этапа (в {@link #getProcessSteps()}) процесса продажи ДК
     */
    private int stepIdx = 0;
    
    /**
     * Этапы техпроцесса, через которые надо пройти при продаже этой карты
     */
    private List<DiscountCardPluginState> processSteps;

    /**
     * Обработчик, что поможет нам продать эту ДК
     */
    private DiscountCardAsGoodHandler handler;
    
    /**
     * Сообщения, что должны быть показаны на форме ввода "идентификатора клиента"
     */
    private FillHolderIdFormMessages holderIdFormMessages;

    /**
     * Артикулы товаров-подарков для которых все шаги добавления пройдены.
     */
    private Set<String> markingsOfGiftsThatPassedSteps = new HashSet<>();

    /**
     * Возврат ДК.
     */
    private boolean isRefund = false;


    /**
     * Переводит состояние модели в первый этап техпроцесса.
     */
    public void moveFirst() {
        stepIdx = 0;
    }

    /**
     * Переводит состояние модели в следующий этап техпроцесса.
     * 
     * @return {@code false}, если следующего этапа уже нет: модель и так была в состоянии отображения финальной формы - на ней модель и останется
     */
    public boolean moveForward() {
        if (getProcessSteps().size() <= stepIdx + 1) {
            return false;
        }
        stepIdx++;
        return true;
    }
    
    /**
     * Переводит состояние модели в предыдущий этап техпроцесса.
     * 
     * @return {@code false}, если предыдущего этапа уже нет: модель и так была в состоянии отображения первой формы - на ней модель и останется
     */
    public boolean moveBackward() {
        if (stepIdx == 0) {
            return false;
        }
        stepIdx--;
        return true;
    }

    public DiscountCardAsGoodVO getCard() {
        if (card == null) {
            card = new DiscountCardAsGoodVO();
        }
        return card;
    }

    public void setCard(DiscountCardAsGoodVO card) {
        this.card = card;
    }

    public DiscountCardPluginState getStage() {
        if (getProcessSteps().size() < stepIdx + 1) {
            // видимо. модель еще не проинициализирована
            return DiscountCardPluginState.ENTER_CARD_NUMBER;
        } else {
            return getProcessSteps().get(stepIdx);
        }
    }
    
    public DiscountCardAsGoodHandler getHandler() {
        return handler;
    }

    public void setHandler(DiscountCardAsGoodHandler handler) {
        this.handler = handler;
    }

    public List<DiscountCardPluginState> getProcessSteps() {
        if (processSteps == null) {
            processSteps = new LinkedList<>();
        }
        return processSteps;
    }

    public DiscountCardPluginState getLastProcessStep() {
        List<DiscountCardPluginState> processSteps = getProcessSteps();
        return CollectionUtils.isEmpty(processSteps) ? null : processSteps.get(processSteps.size() - 1);
    }

    public FillHolderIdFormMessages getHolderIdFormMessages() {
        return holderIdFormMessages;
    }

    public void setHolderIdFormMessages(FillHolderIdFormMessages holderIdFormMessages) {
        this.holderIdFormMessages = holderIdFormMessages;
    }

    /**
     * Модель запоминает факт добавления товара {@link CommonProductPluginModel#product} по его артикулу, если это подарок.
     *
     * @return <tt>true</tt> если можно не проходить все шаги для добавления текущего товара
     */
    public boolean areStepsPassed() {
        return (getProduct() != null) && (getProduct().isGift()) && markingsOfGiftsThatPassedSteps.contains(getProduct().getItem());
    }

    /**
     * Запомним, что товар был успешно добавлен, если это подарок.
     */
    public void stepsPassed() {
        if (getProduct() != null && getProduct().isGift()) {
            markingsOfGiftsThatPassedSteps.add(getProduct().getItem());
        }
    }

    /**
     * Забыть ранее добавленные товары-подарки.
     */
    public void forgetProductsThatPassedSteps() {
        markingsOfGiftsThatPassedSteps.clear();
    }

    @Override
    public boolean ableToAddPosition() {
        boolean isGift = (getProduct() != null) && getProduct().isGift();
        if (!isGift) {
            return super.ableToAddPosition();
        } else {
            return areStepsPassed() && super.ableToAddPosition();
        }
    }

    public boolean isRefund() {
        return isRefund;
    }

    public boolean isNotRefund() {
        return !isRefund;
    }

    public void setRefund(boolean refund) {
        isRefund = refund;
    }

}
