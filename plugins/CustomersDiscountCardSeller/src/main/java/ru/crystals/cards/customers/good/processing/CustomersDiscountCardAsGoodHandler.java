package ru.crystals.cards.customers.good.processing;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardRangeEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.ClientEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.customers.good.processing.i18n.CustomersDCSellerResourceBundle;
import ru.crystals.cards.internal.good.processing.DCSellSteps;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandler;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.cards.internal.good.processing.FillHolderIdFormMessages;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.pos.customers.SetCustomersCashException;
import ru.crystals.pos.customers.SetCustomersCashService;
import ru.crystals.pos.persistence.cards.CardsDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Интегрированная и приложением Покупатели реализация обработчика,
 * помогающего добавить в чек внутреннюю карту как товарную позицию.
 */
public class CustomersDiscountCardAsGoodHandler implements DiscountCardAsGoodHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomersDiscountCardAsGoodHandler.class);

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

    @Override
    public HandlerIds getId() {
        return HandlerIds.CUSTOMERS;
    }

    @Override
    public boolean isActive() {
        return cardsDao != null && customersService != null && customersService.isEnabled();
    }

    @Override
    public boolean canGetInfo(String cardNumber) {
        log.trace("entering canGetInfo(String). The argument is: cardNumber [{}]", cardNumber);

        boolean result = Optional.ofNullable(cardNumber)
                .filter(StringUtils::isNotEmpty)
                .map(cardsDao::getCardRange)
                .map(CardRangeEntity::getCardType)
                // тип карты должен быть "внутренняя/дисконтная"
                .filter(InternalCardsEntity.class::isInstance)
                .isPresent();

        log.trace("leaving canGetInfo(String). The result is: {}", result);
        return result;
    }

    @Override
    public DiscountCardAsGoodVO getCardInfo(String cardNumber) {
        long stopWatch = System.currentTimeMillis();
        log.trace("entering getCardInfo(String). The argument is: \"{}\"", cardNumber);
        DiscountCardAsGoodVO result;
        // Если судить по названию метода, то здесь мы должны только получить информацию по карте,
        // и далее уже вызвать метод активации карты, но чтобы не делать лишних запросов в SLS,
        // сразу покупаем карту и получаем всю информацию по карте для добавления в чек.
        try {
            result = customersService.purchaseCard(cardNumber).getCards().stream()
                     .filter(c -> c.getNumber().equals(cardNumber))
                     .map(this::newInternalCardAsGoodVO)
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException("Received client without expected card"));
        } catch (SetCustomersCashException e) {
            log.warn("Card purchase error", e);
            result = newInternalCardAsGoodVO(cardNumber, e.reason());
        } catch (Exception e) {
            log.warn("Card purchase failed", e);
            result = null;
        }
        log.trace("leaving getCardInfo(String). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    @Override
    public DiscountCardAsGoodVO getCardInfo(String cardNumber, boolean refund) {
        if (!refund) {
            return getCardInfo(cardNumber);
        } else {
            Set<CardStatus> acceptableStatuses = new HashSet<>(Arrays.asList(
                    CardStatus.Create,
                    CardStatus.Active
            ));
            try {
                return customersService.findClientByCardNumber(cardNumber).map(ClientEntity::getCards)
                        .orElseGet(Collections::emptyList).stream()
                        .filter(c -> c.getNumber().equals(cardNumber))
                        .filter(c -> acceptableStatuses.contains(c.getStatus()))
                        .map(this::newInternalCardAsGoodVO)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No client with such card number"));
            } catch (Exception e) {
                log.error("Refund card not found", e);
                return null;
            }
        }
    }

    @Override
    public List<String> validateCard(DiscountCardAsGoodVO card) {
        List<String> result = new ArrayList<>();
        if (card instanceof InternalCardAsGoodVO && ((InternalCardAsGoodVO) card).getErrorReason() != null) {
            switch (((InternalCardAsGoodVO) card).getErrorReason()) {
                case INVALID_RANGE:
                    result.add(CustomersDCSellerResourceBundle.getLocalValue("error.invalid.range"));
                    break;
                case CARD_EXISTS:
                    result.add(CustomersDCSellerResourceBundle.getLocalValue("error.card.exists"));
                    break;
                case VIRTUAL_CARD:
                    result.add(CustomersDCSellerResourceBundle.getLocalValue("error.virtual.card"));
                    break;
                case OFFLINE:
                    result.add(CustomersDCSellerResourceBundle.getLocalValue("error.offline"));
                    break;
                default:
                    result.add(CustomersDCSellerResourceBundle.getLocalValue("error.other"));
            }
        }
        return result;
    }

    @Override
    public List<String> validateHolderId(String holderId, DiscountCardAsGoodVO card) {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("card-as-good-handler [id: \"%s\"]", getId().getValue());
    }

    @Override
    public List<DCSellSteps> getProcess(DiscountCardAsGoodVO card) {
        return getProcessSteps();
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
        result.setTitle(CustomersDCSellerResourceBundle.getLocalValue("client.id.title"));
        result.setWelcomeText(CustomersDCSellerResourceBundle.getLocalValue("enter.client.id.msg"));

        return result;
    }

    @Override
    public List<String> activateCard(String holderId, DiscountCardAsGoodVO card) {
        return new ArrayList<>();
    }

    @Override
    public void deactivateCard(String cardNumber) {
        try {
            customersService.refundCard(cardNumber);
        } catch (Exception e) {
            log.error("Failed to refund card \"" + cardNumber + "\"", e);
        }
    }

    @Override
    public CardTypeEntity getCardType(DiscountCardAsGoodVO card) {
        return fromVO(card).orElse(null);
    }

    private Optional<CardTypeEntity> fromVO(DiscountCardAsGoodVO vo) {
        return Optional.ofNullable(vo)
                .filter(InternalCardAsGoodVO.class::isInstance)
                .map(InternalCardAsGoodVO.class::cast)
                .map(InternalCardAsGoodVO::getCardsType);
    }

    private InternalCardAsGoodVO newInternalCardAsGoodVO(CardEntity cardEntity) {
        InternalCardAsGoodVO result = new InternalCardAsGoodVO();
        result.setCardsType(cardEntity.getCardType());
        result.setNumber(cardEntity.getNumber());
        result.setTypeGuid(cardEntity.getCardType().getGuid());
        result.setPersonalized(true);
        result.setApplicableOnCheck(true);
        return result;
    }

    private InternalCardAsGoodVO newInternalCardAsGoodVO(
            String cardNumber, SetCustomersCashException.Reason reason) {
        InternalCardAsGoodVO result = new InternalCardAsGoodVO();
        result.setErrorReason(reason);
        result.setNumber(cardNumber);
        result.setPersonalized(true);
        result.setHolderMandatory(false);
        result.setApplicableOnCheck(false);
        result.setPercentageDiscount(0);
        return result;
    }

}