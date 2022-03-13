package ru.crystals.pos.card.replacement;

import ru.crystals.cards.CardTypeEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.techprocess.ReplaceCardScenarioInterface;

/**
 * Created by agaydenger on 08.08.16.
 */
public class MLReplaceCardResult extends ReplaceCardScenarioInterface.ReplaceCardResult {
    private PositionEntity cardPositionToAdd;

    public MLReplaceCardResult(String newCardNumber, CardTypeEntity cardType, PositionEntity cardPositionToAdd) {
        super(newCardNumber, cardType);
        this.cardPositionToAdd = cardPositionToAdd;
    }

    public PositionEntity getCardPositionToAdd() {
        return cardPositionToAdd;
    }

}
