package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.controller;

import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.GiftCardHelper;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.model.GiftCardInfoModel;

/**
 * Контроллер для сценария "Информация по подарочной карте".
 */
public class GiftCardInfoController {
    private GiftCardInfoModel model;
    private PresentCards presentCardService = GiftCardHelper.getPresentCardsService();

    public void setModel(GiftCardInfoModel model) {
        this.model = model;
    }

    public void askGiftCardNumber() {
        model.setState(GiftCardInfoModel.State.CARD_NUMBER_ENTER);
        model.changed();
    }

    public void enterGiftCardNumber(String cardNumber, boolean msrInput) {
        model.setState(GiftCardInfoModel.State.LOADING_CARD_DATA_FROM_CENTRUM);
        model.changed();
        try {
            //Номер карты мог прилететь от msr, тогда в треке может быть ненужная инфа по контрольным разрядам
            //Вырежем её
            cardNumber = Factory.getTechProcessImpl().getCards().parseCardNumber(null, cardNumber, null, null, PresentCards.MODULE_NAME);
            PresentCardInformationVO presentCard = presentCardService.getCardData(cardNumber, msrInput);
            model.setCardInformation(presentCard);
            model.setState(GiftCardInfoModel.State.SHOW_CARD_DATA);
            model.changed();
        } catch (CardsException e) {
            model.setErrorMessage(e.getMessage());
            model.setState(GiftCardInfoModel.State.LOADING_DATA_ERROR);
            model.changed();
        }
    }

    public void exit() {
        model.changed();
        Factory.getInstance().tryToSwitchToMainMode();
    }

    /**
     * Распечатать подарочную карту.
     */
    public void printGiftCard() {
        try {
            model.setState(GiftCardInfoModel.State.PRINTING_CARD_DATA);
            model.changed();
            Factory.getTechProcessImpl().printPresentCard(model.getCardInformation());
            exit();
        } catch (FiscalPrinterException e) {
            e.printStackTrace();
        }
    }
}
