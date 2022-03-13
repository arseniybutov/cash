package ru.crystals.pos.visualization.products.giftcard.giftcardreplace;

import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.GiftCardHelper;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.controller.GiftCardReplaceController;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model.GiftCardReplaceModel;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model.GiftCardReplaceModelListener;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.view.GiftCardReplaceView;

import java.util.Arrays;

/**
 * Сценарий "Замена подарочной карты на кассе".
 */
public class GiftCardReplaceScenary {
    final GiftCardReplaceController controller;

    public GiftCardReplaceScenary() {
        GiftCardReplaceModel model = new GiftCardReplaceModel();

        PresentCards presentCards = GiftCardHelper.getPresentCardsService();
        TechProcessInterface techProcess = Factory.getTechProcessImpl();
        controller = new GiftCardReplaceController(model, presentCards, techProcess);

        GiftCardReplaceView view = new GiftCardReplaceView(controller);
        model.setListeners(Arrays.<GiftCardReplaceModelListener>asList(view));
    }

    public void execute() {
        controller.reset();
        controller.enterOldCardNumber();
    }
}