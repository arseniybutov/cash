package ru.crystals.cards.internal.good.processing;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internal.good.processing.i18n.MLDCSellerResourceBundle;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.ml.exception.MLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ML POS (Manzana Loyalty) реализация обработчика, помогающего добавить в чек внутреннюю карту как товарную позицию.
 * 
 * @author aperevozchikov
 */
public class MLBackedDiscountCardAsGoodHandler implements DiscountCardAsGoodHandler {
    private static final Logger log = LoggerFactory.getLogger(MLBackedDiscountCardAsGoodHandler.class);


    // looked up through BundleManager
    /**
     * Наш карточный процессинг "под капотом" данного фасада
     */
    @Autowired
    private CardsService cardsService;

    /**
     * Сервис Manzana Loyalty
     */
    @Autowired(required = false)
    private MLService service;
    
    // injected
    /**
     * Полный техпроцесс добавления карты в чек как товарной позиции
     */
    private List<DCSellSteps> processSteps;

    @Override
    public HandlerIds getId() {
        return HandlerIds.ML;
    }

    @Override
    public boolean isActive() {
        // Это наш дефолтный обработчик. Он всегда активен:
        return getService() != null;
    }

    @Override
    public boolean canGetInfo(String cardNumber) {
        boolean result = false;

        log.trace("entering canGetInfo(String). The argument is: cardNumber [{}]", cardNumber);
        
        result = getService().isMlCard(cardNumber);
        
        log.trace("leaving canGetInfo(String). The result is: {}", result);

        return result;
    }

    @Override
    public DiscountCardAsGoodVO getCardInfo(String cardNumber) {
        DiscountCardAsGoodVO result = null;
        long stopWatch = System.currentTimeMillis();
        
        log.trace("entering getCardInfo(String). The argument is: \"{}\"", cardNumber);
        
        // если это не наша карта по какой причине - тупо вернем null
        if (!getService().isMlCard(cardNumber)) {
            log.warn("leaving getCardInfo(String). The card [{}] is NOT an ML card", cardNumber);
            return null;
        }
        
        // сформируем результат:
        result = new DiscountCardAsGoodVO();
        result.setNumber(cardNumber);
        
        // все карты ML персонализированные
        result.setPersonalized(true);
        // обязательность заполнения анкеты определяется процессингом
        result.setHolderMandatory(getService().isHolderMandatory(cardNumber));
        // если карты ML заведены в Set10, необходимо получить их GUID
        result.setTypeGuid(getCardsService().getGuidFromLocal(cardNumber));

        log.trace("leaving getCardInfo(String). The result is: {}; it took {} [ms]", 
            result, System.currentTimeMillis() - stopWatch);
        
        return result;
    }

    @Override
    public List<String> validateCard(DiscountCardAsGoodVO card) {
        return Collections.emptyList();
    }

    @Override
    public List<String> activateCard(String holderId, DiscountCardAsGoodVO card) {
        List<String> result = new ArrayList<>();
        String cardNumber = card == null ? null : card.getNumber();
        try {
            this.getService().bind(cardNumber, holderId);
        } catch (MLException mle) {
            log.error(String.format("failed to bing card [%s] to application [%s]", cardNumber, holderId), mle);
            result = new ArrayList<>();
            result.add(mle.getMessage());
        }
        return result;
    }

    @Override
    public CardTypeEntity getCardType(DiscountCardAsGoodVO card) {
        if (card == null) {
            return null;
        }
        CardTypeEntity result = CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME);
        result.setGuid(card.getTypeGuid());
        result.setApplicableOnCheck(true);
        return result;
    }

    @Override
    public List<String> validateHolderId(String holderId, DiscountCardAsGoodVO card) {
        List<String> result = new ArrayList<>();
        long stopWatch = System.currentTimeMillis();
        
        log.trace("entering validateHolderId(String, DiscountCardAsGoodVO). The arguments are: holderId [{}], card [{}]", holderId, card);
        
        String cardNumber = card == null ? null : card.getNumber();
        if (StringUtils.isBlank(cardNumber)) {
            log.error("leaving validateHolderId(String, DiscountCardAsGoodVO). The \"card\" argument is INVALID!");
            result.add(MLDCSellerResourceBundle.getLocalValue("card.number.is.empty"));
            return result;
        }
        log.trace("leaving validateHolderId(String, DiscountCardAsGoodVO). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);
        
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("card-as-good-handler [id: \"%s\"]", getId().getValue());
    }
    
    @Override
    public List<DCSellSteps> getProcess(DiscountCardAsGoodVO card) {
        List<DCSellSteps> result = new ArrayList<>(getProcessSteps());
        
        log.trace("entering getProcess(DiscountCardAsGoodVO). The argument is: {}", card);
        
        if (card == null) {
            log.warn("leaving getProcess(DiscountCardAsGoodVO). The argument is NULL!");
            result.clear();
            return result;
        }
        
        // удалим лишние этапы
        for (Iterator<DCSellSteps> it = result.iterator(); it.hasNext();) {
            DCSellSteps step = it.next();
            if (step == null) {
                it.remove();
                continue;
            }
            
            switch (step) {
                case FILL_CLIENT_ID_DLG:
                    // если карта не-персонализированная, либо заполнение анкетных данных картоносца обязательно, 
                    //  то этот этап техпроцесса надо пропустить
                    if (!card.isPersonalized() || card.isHolderMandatory()) {
                        it.remove();
                    }
                    break;
                case ENTER_CLIENT_ID:
                    // если карта не-персонализированная, то этот этап техпроцесса надо пропустить
                    if (!card.isPersonalized()) {
                        it.remove();
                    }
                    break;
                default:
                    // про остальные этапы ничего не можем сказать
                    break;
            } // switch step
        } // for it
        
        log.trace("leaving getProcess(DiscountCardAsGoodVO). The result is: {}", result);
        
        return result;
    }
    
    
    // getters & setters
    
    public MLService getService() {
        return service;
    }

    public CardsService getCardsService() {
        return cardsService;
    }

    public List<DCSellSteps> getProcessSteps() {
        if (processSteps == null) {
            processSteps = new LinkedList<>();
        }
        return processSteps;
    }

    public void setProcessSteps(List<DCSellSteps> processSteps) {
        this.processSteps = processSteps;
    }

    @Override
    public FillHolderIdFormMessages getHolderIdFormMessages(DiscountCardAsGoodVO card) {
        FillHolderIdFormMessages result = new FillHolderIdFormMessages();
        
        // на форме ввода "идентификатора клиента" всех карт используем тот же самый текст:
        result.setTitle(MLDCSellerResourceBundle.getLocalValue("client.id.title"));
        result.setWelcomeText(MLDCSellerResourceBundle.getLocalValue("enter.client.id.msg"));
        
        return result;
    }
}
