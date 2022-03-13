package ru.crystals.pos.visualization.products.giftcard.product.controller;

import org.springframework.stereotype.Component;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.cards.exception.CardIsBlockedException;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionGiftCardEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.giftcard.GiftCardHelper;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.product.model.GiftCardPluginModel;
import ru.crystals.pos.visualization.products.giftcard.product.model.GiftCardPluginState;

import java.math.BigDecimal;
import java.util.function.Supplier;

/**
 * Контроллер для товара Подарочная Карта
 * Выполняет логику плагина.
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY)
public class GiftCardPluginController extends AbstractProductController<GiftCardPluginModel> {
    private final Supplier<PresentCards> presentCardService = GiftCardHelper::getPresentCardsService;

    @Override
    public boolean eventMSR(String Track1, String Track2, String Track3, String Track4) {
        return super.eventMSR(Track1, Track2, Track3, Track4);
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return super.barcodeScanned(barcode);
    }

    /**
     * Проверяет возможно ли добавить ПК в чек, так как номинал ПК не известен в этот момент берет стоимость из продукта
     *
     * @return String - текст ошибки или null, если все хорошо
    * */
    public String isPossibleToAddPosition() {
        PositionGiftCardEntity entity = new PositionGiftCardEntity();
        fillDefaultPosition(BigDecimal.ONE, getModel().getProduct().getPrice().getPriceBigDecimal(), getModel().getProduct(), entity);
        return getAdapter().isPossibleToAddPosition(entity);
    }

    /**
     * Проверяет разрешено ли сканировать ПК по штрихкоду
     *
     * @return да или нет
     */
    public boolean isAllowedToScanBarcode() {
       return getModel().getProduct().getProductConfig().isAllowBarcodeScan();
    }

    /**
    * Находит информацию по конкретной ПК и добавляет эту инфомацию в модель
    *
    * @throws CardsException - при отсутсвии связи, при некорректном состоянии ПК, при некорректном заведении ПК
    * */
    public void findAndAddGiftCardInfo(String enteredNumber, boolean msrInput) throws CardsException {

        if (presentCardService.get() == null) {
            throw new CardsException(ResBundleGoodsGiftCard.getString("NO_CONNECTION"));
        }

        PresentCardInformationVO presentCard = presentCardService.get().getCardData(enteredNumber, msrInput);

        checkCardStatus(CardStatus.Create, presentCard.getStatus());

        getModel().setPresentCard(presentCard);
        if ((presentCard.getMaxAmount() == null || presentCard.getMaxAmount() == 0) && (presentCard.getMultiplicity() == null || presentCard.getMultiplicity() == 0)) {
            checkStartPrice(presentCard.getAmount());
            checkSumm(presentCard.getAmount());
            getModel().setInternalState(GiftCardPluginState.FIX_GIFT_CARD);
        } else {
            checkSumm(presentCard.getMaxAmount());
            checkSumm(presentCard.getMultiplicity());
            //странно что ошибка ввода сервера проверялась на кассе
            //checkMultiplicity(presentCard.getMaxAmount(), presentCard.getMultiplicity(), ResBundleGoodsGiftCard.getString("ACTIVATION_IS_IMPOSSIBLE"));
            getModel().setInternalState(GiftCardPluginState.NOT_FIX_GIFT_CARD);
        }
    }

    /**
     * Активирует ПК с уже известным номиналом и добавляет эту ПК в чек
     *
     * @throws CardsException - при отсутсвии связи, при невозможности добавить ПК в чек, при ошибке активации ПК
     * */
    public void activateAndAddFixGiftCard() throws CardsException {
        PresentCardInformationVO presentCard = getModel().getPresentCard();

        if (presentCardService.get() == null) {
            throw new CardsException(ResBundleGoodsGiftCard.getString("NO_CONNECTION"));
        }

        String msg = isPossibleToAddPosition();
        if (msg != null) {
            throw new CardsException(msg);
        }

        PositionGiftCardEntity position = getPositionGiftCardEntity(presentCard);
        presentCard = presentCardService.get().activateCard(position, Factory.getInstance().getTechProcess().getCurrentUser());
        position.setExpirationDate(presentCard.getExpirationDate());
        getModel().setPosition(position);
        getAdapter().doPositionAdd(position);
    }

    /**
     * Активирует ПК с не фиксированным номиналом и добавляет эту ПК в чек
     * @param enteredAmount - введенный номинал
     *
     * @throws CardsException - при некорректном номинале, при отсутсвии связи, при невозможности добавить ПК в чек, при ошибке активации ПК
     * */
    public void activateAndAddNonFixedGiftCard(Long enteredAmount) throws CardsException {
        PresentCardInformationVO presentCard = getModel().getPresentCard();

        // Проверяет сумму денег на корректность: кратность
        if (enteredAmount % presentCard.getMultiplicity() != 0) {
            throw new CardsException(ResBundleGoodsGiftCard.getString("SUMMA_MULTIPLICITY") + " " + CurrencyUtil.formatSum(presentCard.getMultiplicity()));
        }

        // максимально возможнную сумму
        if (enteredAmount > presentCard.getMaxAmount()) {
            throw new CardsException(ResBundleGoodsGiftCard.getString("GIFT_CARD_MAX_LIMIT_EXCEPTION") + " " + CurrencyUtil.formatSum(presentCard.getMaxAmount()));
        }

        presentCard.setAmount(enteredAmount);
        activateAndAddFixGiftCard();
    }

    /**
     * Создает экземпляр позиции по ПК
     *
     * @param presentCard - ПК
     *
     * @return PositionGiftCardEntity - позиция
     * */
    private PositionGiftCardEntity getPositionGiftCardEntity(PresentCardInformationVO presentCard) {
        PositionGiftCardEntity position = new PositionGiftCardEntity();
        fillDefaultPosition(BigDecimal.ONE, BigDecimalConverter.convertMoney(presentCard.getAmount()), getModel().getProduct(), position);
        position.setCardNumber(presentCard.getCardNumber());
        position.setAmount(presentCard.getAmount());
        position.setExpirationDate(presentCard.getExpirationDate());
        return position;
    }

    /**
     * Проверяет статус ПК и если он не равен needStatus - порождает ошибку
     *
     * @param needStatus - ожидаемый статус
     * @param cardStatus - статус ПК
     * */
    private void checkCardStatus(CardStatus needStatus, CardStatus cardStatus) throws CardsException {
        if (needStatus != cardStatus) {
            switch (cardStatus) {
                case Active:
                    throw new CardsException(ResBundleGoodsGiftCard.getString("CARD_HAS_BEEN_ALREADY_ACTIVATED"));
                case Blocked:
                    throw new CardIsBlockedException(ResBundleGoodsGiftCard.getString("CARD_HAS_BEEN_BLOCKED"));
                case Used:
                    throw new CardsException(ResBundleGoodsGiftCard.getString("CARD_HAS_BEEN_ALREADY_USED"));
                default:
                    throw new CardsException(ResBundleGoodsGiftCard.getString("CARD_COULD_NOT_BE_ACTIVATED"));
            }
        }
    }

    /**
     * Если включена настройка сheckGiftCardNominalAndPrice -
     * проверяет соответствие цены товара и номинала конкретной карты с фиксированным номиналом
     *
     * @param amount - номинал
     * @throws CardsException
     */
    private void checkStartPrice(Long amount) throws CardsException {
        if (getModel().getProduct().getProductConfig().isCheckGiftCardNominalAndPrice()) {
            if (amount == null || getModel().getProduct() == null || !amount.equals(getModel().getProduct().getPrice().getPrice())){
                throw new CardsException(ResBundleGoodsGiftCard.getString("START_PRICE_AND_NOMINAL_ARE_NOT_EQUALS"));
            }
        }
    }

    /**
     * Проверяет сумму денег на корректность
     *
     * @param summ - сумма
     *
     * @throws CardsException - при некорректной с точки зрения округления суммы, при сумме равной 0
     * */
    private void checkSumm(Long summ) throws CardsException {
        if (!summ.equals(CurrencyUtil.round(summ)) || summ.equals(0L)) {
            throw new CardsException(ResBundleGoodsGiftCard.getString("ACTIVATION_IS_IMPOSSIBLE"));
        }
    }
}
