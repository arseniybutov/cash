package ru.crystals.pos.visualization.products.siebelcard.model;

import ru.crystals.pos.cards.siebel.SiebelGiftCardResult;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

/**
 * Содержит состояние продукта "Подарочная карта Siebel".
 */
public class SiebelGiftCardModel extends CommonProductPluginModel {

    /**
     * Описание продаваемой позиции (подарочная карта).
     */
    private SiebelGiftCardResult card = null;

    /**
     * Состояние плагина.
     */
    private SiebelGiftCardState internalState = null;

    public SiebelGiftCardResult getCard() {
        return card;
    }

    public void setCard(SiebelGiftCardResult card) {
        this.card = card;
    }

    public SiebelGiftCardState getInternalState() {
        return internalState;
    }

    public void setInternalState(SiebelGiftCardState state) {
        this.internalState = state;
    }
}
