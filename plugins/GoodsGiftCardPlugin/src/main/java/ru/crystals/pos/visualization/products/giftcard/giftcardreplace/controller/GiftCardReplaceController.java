package ru.crystals.pos.visualization.products.giftcard.giftcardreplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.bl.keylisteners.EnterListener;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model.GiftCardReplaceModel;

/**
 * Контроллер для сценария "Замена подарочной карты на кассе".
 */
public class GiftCardReplaceController {
    private static final Logger LOG = LoggerFactory.getLogger(GiftCardReplaceController.class);

    private static final String GETTING_CARD_INFORMATION = ResBundleGoodsGiftCard.getString("GETTING_CARD_INFORMATION");
    private static final String NEW_CARD_NUMBER = ResBundleGoodsGiftCard.getString("NEW_CARD_NUMBER");
    private static final String OLD_CARD_NUMBER = ResBundleGoodsGiftCard.getString("OLD_CARD_NUMBER");
    private static final String CARD_REPLACING = ResBundleGoodsGiftCard.getString("CARD_REPLACING");
    private static final String TICKETS_PRINTING = ResBundleGoodsGiftCard.getString("TICKETS_PRINTING");

    private final GiftCardReplaceModel model;
    private final PresentCards presentCardService;
    private final TechProcessInterface techProcess;
    private final EnterListener<String> newCardNumberEnteredListener = this::newCardNumberEntered;

    private final EnterListener<String> oldCardNumberEnteredListener = this::oldCardNumberEntered;

    private final XKeyListener enterOldCardNumberListener = e -> enterOldCardNumber();

    private final XKeyListener enterNewCardNumberListener = e -> enterNewCardNumber();

    public GiftCardReplaceController(GiftCardReplaceModel model, PresentCards presentCardService,
                                     TechProcessInterface techProcess) {
        this.model = model;
        this.presentCardService = presentCardService;
        this.techProcess = techProcess;
    }

    /**
     * Ввести номер заменяемой карты.
     */
    public void enterOldCardNumber() {
        LOG.debug("Запрашиваю номер старой карты");
        model.setState(GiftCardReplaceModel.State.CARD_NUMBER_ENTER);
        model.setEnterListener(oldCardNumberEnteredListener);
        model.setErrorListener(enterOldCardNumberListener);
        model.setMessage(OLD_CARD_NUMBER);
        model.setHandEnterEnabled(true);
        model.changed();
    }

    public void oldCardNumberEntered(String cardNumber) {
        LOG.debug("Введен номер старой карты: {}", cardNumber);
        model.setCurrentCardNumber(cardNumber);
        model.setState(GiftCardReplaceModel.State.WAIT);
        model.setMessage(GETTING_CARD_INFORMATION);
        model.setHandEnterEnabled(false);
        model.changed();
        try {
            PresentCardInformationVO oldCardInfo = presentCardService.getCardData(cardNumber.toString());
            GiftCardReplaceVerificator.canBeReplaced(oldCardInfo);
            model.setOldCardInfo(oldCardInfo);
            enterNewCardNumber();
        } catch (CardsException e) {
            showErrorForm(e);
        }
    }

    private void enterNewCardNumber() {
        LOG.debug("Запрашиваю номер новой карты");
        model.setCurrentCardNumber(null);
        model.setState(GiftCardReplaceModel.State.CARD_NUMBER_ENTER);
        model.setMessage(NEW_CARD_NUMBER);
        model.setEnterListener(newCardNumberEnteredListener);
        model.setErrorListener(enterNewCardNumberListener);
        model.changed();
    }

    public void newCardNumberEntered(String cardNumber) {
        LOG.debug("Введен номер новой карты: {}", cardNumber);
        model.setCurrentCardNumber(cardNumber);
        model.setState(GiftCardReplaceModel.State.WAIT);
        model.setMessage(GETTING_CARD_INFORMATION);
        model.changed();
        try {
            PresentCardInformationVO newCardInfo = presentCardService.getCardData(cardNumber.toString());
            GiftCardReplaceVerificator.canReplace(newCardInfo);
            model.setNewCardInfo(newCardInfo);
            replaceCards();
            printTicket(2);
            exit();
        } catch (CardsException | FiscalPrinterException e) {
            showErrorForm(e);
        }
    }

    private void showErrorForm(Exception e) {
        LOG.warn(e.getMessage(), e);
        model.setMessage(e.getMessage());
        model.setState(GiftCardReplaceModel.State.SHOW_ERROR);
        model.changed();
    }

    private void replaceCards() throws CardsException {
        PresentCardInformationVO oldCardInfo = model.getOldCardInfo();
        PresentCardInformationVO newCardInfo = model.getNewCardInfo();
        LOG.debug("Заменяю карту: {} --> {}", oldCardInfo, newCardInfo);
        model.setState(GiftCardReplaceModel.State.WAIT);
        model.setMessage(CARD_REPLACING);
        model.changed();

        PresentCardInformationVO replacedCardInfo = presentCardService.cardReplacement(oldCardInfo.getCardNumber(),
                newCardInfo.getCardNumber(), techProcess.getCurrentUser());
        model.setNewCardInfo(replacedCardInfo);
    }

    private void printTicket(int copies) throws FiscalPrinterException {
        LOG.debug("Печатаю квитанции");
        model.setState(GiftCardReplaceModel.State.WAIT);
        model.setMessage(TICKETS_PRINTING);
        model.changed();
        for (int i = 0; i < copies; i++) {
            techProcess.printPresentCardReplace(model.getOldCardInfo(), model.getNewCardInfo());
        }
    }

    public void reset() {
        model.setCurrentCardNumber(null);
    }

    public void exit() {
        LOG.debug("Выход");
        reset();
        Factory.getInstance().tryToSwitchToMainMode();
    }
}