package ru.crystals.cards.internal.good.processing;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardRangeEntity;
import ru.crystals.cards.CardSearchResult;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.ClientEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.internal.good.processing.i18n.SetDCSellerResourceBundle;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.customers.SetCustomersCashService;
import ru.crystals.pos.persistence.cards.CardsDao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Дефолтная (SET10) реализация обработчика, помогающего добавить в чек внутреннюю карту как товарную позицию.
 *
 * @author aperevozchikov
 */
public class SetBackedDiscountCardAsGoodHandler implements DiscountCardAsGoodHandler {
    private static final Logger log = LoggerFactory.getLogger(SetBackedDiscountCardAsGoodHandler.class);

    // injected
    /**
     * Наш карточный процессинг "под капотом" данного фасада
     */
    private CardsService service;

    /**
     * Для получения типа карты из диапазонов.
     */
    @Autowired(required = false)
    private CardsDao cardsDao;

    @Autowired(required = false)
    private SetCustomersCashService customersService;

    // injected
    /**
     * Полный техпроцесс добавления карты в чек как товарной позиции
     */
    private List<DCSellSteps> processSteps;

    // injected
    /**
     * Проверять, что в БД SET10 карты либо нет, либо карта в состоянии {@link CardStatus.Create}.
     */
    private boolean checkCardIsNotInUse = false;


    @Override
    public HandlerIds getId() {
        return HandlerIds.SET10;
    }

    @Override
    public boolean isActive() {
        // Это наш дефолтный обработчик.
        return getService() != null && getCardsDao() != null &&
                // Он активен, если не включена интегация с приложением Покупатели.
                (customersService == null || !customersService.isEnabled());
    }

    @Override
    public boolean canGetInfo(String cardNumber) {
        log.trace("entering canGetInfo(String). The argument is: cardNumber [{}]", cardNumber);

        boolean result = Optional.ofNullable(cardNumber)
                .filter(StringUtils::isNotEmpty)
                .map(getCardsDao()::getCardRange)
                .map(CardRangeEntity::getCardType)
                // тип карты должен быть "внутренняя/дисконтная"
                .filter(InternalCardsEntity.class::isInstance)
                .isPresent();

        log.trace("leaving canGetInfo(String). The result is: {}", result);
        return result;
    }

    @Override
    public DiscountCardAsGoodVO getCardInfo(String cardNumber) {
        DiscountCardAsGoodVO result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getCardInfo(String). The argument is: \"{}\"", cardNumber);

        InternalCardsEntity ct = findCardAsGood(cardNumber);
        log.trace("type of card #{} is: {}", cardNumber, ct);

        if (ct != null) {
            result = new InternalCardAsGoodVO(ct);
            result.setNumber(cardNumber);
            result.setPersonalized(ct.isPersonalized());
            // карты SET10 не требуют ввода анкеты
            result.setHolderMandatory(false);
            result.setTypeGuid(ct.getGuid());
            result.setApplicableOnCheck(ct.isApplicableOnCheck());

            // процент скидки
            Long pd = ct.getPercentageDiscount();
            result.setPercentageDiscount(pd == null ? 0 : pd);
        }

        log.trace("leaving getCardInfo(String). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public List<String> validateCard(DiscountCardAsGoodVO card) {
        List<String> errors = new ArrayList<>();

        if (getCheckCardIsNotInUse()) {
            boolean cardNotActivated = fromVO(card).map(InternalCardsEntity::getCards).map(List::stream).orElseGet(Stream::empty)
                    // К сожалению при отсутствии карты на стороне сервера, тот возвращает фэйковую сущность со статусом Active -
                    // необходимо её отфильтровать тут.
                    .filter(c -> c.getId() != null || c.getGuid() != null)
                    .map(CardEntity::getStatus).allMatch(CardStatus.Create::equals);
            if (!cardNotActivated) {
                errors.add(SetDCSellerResourceBundle.getLocalValue("internal.card.activated"));
            }
        }

        return errors;
    }

    private InternalCardsEntity findCardAsGood(String cardNumber) {
        try {
            CardData cardData = new CardData(cardNumber);
            cardData.setCardAsGood(true);
            CardSearchResult cardSearchResult = service.getCardType(cardData);
            return Optional.ofNullable(cardSearchResult)
                    .map(CardSearchResult::getCard)
                    .filter(InternalCardsEntity.class::isInstance)
                    .map(InternalCardsEntity.class::cast)
                    .orElse(null);
        } catch (Exception t) {
            // не критично. просто не "знаем" эту карту
            if (log.isTraceEnabled()) {
                log.trace(String.format("canGetInfo: failed to infer type of card #%s", cardNumber), t);
            }
        }
        return null;
    }

    @Override
    public List<String> validateHolderId(String holderId, DiscountCardAsGoodVO card) {
        List<String> result = new ArrayList<>();
        long stopWatch = System.currentTimeMillis();
        log.trace("entering validateHolderId(String, DiscountCardAsGoodVO). The arguments are: holderId [{}], card [{}]", holderId, card);

        Long guid = null;
        try {
            guid = Long.valueOf(holderId);
        } catch (NumberFormatException t) {
            log.error(String.format("validateHolderId: seems that the holderId [%s] is not a number", holderId), t);
            // ошибка! "идентификатор клиента" у нас - число!
            result.add(SetDCSellerResourceBundle.getLocalValue("client.guid.has.to.be.a.number"));
        }

        if (guid != null) {
            // карта для нас вообще побоку: у нас демократия: мы не сегрегируем быдло-картоносцев по "позволенности" купить карту какой-либо категории
            boolean foundClientByGuid = fromVO(card)
                    .map(CardTypeEntity::getCards)
                    .filter(cards -> cards.size() == 1)
                    .map(cards -> cards.get(0))
                    .map(CardEntity::getClient)
                    .map(ClientEntity::getGuid)
                    .filter(guid::equals)
                    .isPresent();

            if (!foundClientByGuid) {
                // клиент не найден - надо такую ошибку и показать
                log.trace("validateHolderId: client by guid [{}] was NOT found!", guid);
                result.add(MessageFormat.format(SetDCSellerResourceBundle.getLocalValue("client.by.guid.is.absent"), guid));
            }
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
        for (Iterator<DCSellSteps> it = result.iterator(); it.hasNext(); ) {
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

    public CardsService getService() {
        return service;
    }

    public void setService(CardsService service) {
        this.service = service;
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

    public boolean getCheckCardIsNotInUse() {
        return checkCardIsNotInUse;
    }

    public void setCheckCardIsNotInUse(boolean checkCardIsNotInUse) {
        this.checkCardIsNotInUse = checkCardIsNotInUse;
    }

    @Override
    public FillHolderIdFormMessages getHolderIdFormMessages(DiscountCardAsGoodVO card) {
        FillHolderIdFormMessages result = new FillHolderIdFormMessages();

        // на форме ввода "идентификатора клиента" всех карт используем тот же самый текст:
        result.setTitle(SetDCSellerResourceBundle.getLocalValue("client.id.title"));
        result.setWelcomeText(SetDCSellerResourceBundle.getLocalValue("enter.client.id.msg"));

        return result;
    }

    /**
     * Эта реализация обработчика не занимается активирвоанием карт,
     * всегда возвращает пустой список.
     *
     * @param holderId идентификатор владельца карты.
     * @param card     активируемая карта.
     * @return пустой список.
     */
    @Override
    public List<String> activateCard(String holderId, DiscountCardAsGoodVO card) {
        // Empty in this implementation.
        return new ArrayList<>();
    }

    @Override
    public CardTypeEntity getCardType(DiscountCardAsGoodVO card) {
        return fromVO(card).orElseGet(() -> newInternalCardEntity(card));
    }

    private CardsDao getCardsDao() {
        return cardsDao;
    }

    private Optional<InternalCardsEntity> fromVO(DiscountCardAsGoodVO vo) {
        return Optional.ofNullable(vo)
                .filter(InternalCardAsGoodVO.class::isInstance)
                .map(InternalCardAsGoodVO.class::cast)
                .map(InternalCardAsGoodVO::getCardsType);
    }

    private InternalCardsEntity newInternalCardEntity(DiscountCardAsGoodVO vo) {
        InternalCardsEntity result = null;
        if (vo != null) {
            result = new InternalCardsEntity();
            result.setDeleted(false);
            result.setGuid(vo.getTypeGuid());
            result.setPercentageDiscount(vo.getPercentageDiscount());
            result.setApplicableOnCheck(vo.isApplicableOnCheck());
        }
        return result;
    }

    /**
     * Хранитель {@link InternalCardsEntity}, чтобы не делать лишних запросов на сервер.
     */
    private static class InternalCardAsGoodVO extends DiscountCardAsGoodVO {

        final InternalCardsEntity cardsType;

        InternalCardAsGoodVO(InternalCardsEntity cardsType) {
            this.cardsType = cardsType;
        }

        public InternalCardsEntity getCardsType() {
            return cardsType;
        }

    }

}