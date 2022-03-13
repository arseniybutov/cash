package ru.crystals.cards.customers.good.processing;

import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.pos.customers.SetCustomersCashException;

/**
 * Хранитель результата запроса в Покупатели, чтобы не делать лишних запросов в приложение.
 */
class InternalCardAsGoodVO extends DiscountCardAsGoodVO {

    private CardTypeEntity cardsType;
    private SetCustomersCashException.Reason reason;

    CardTypeEntity getCardsType() {
        return cardsType;
    }

    void setCardsType(CardTypeEntity cardsType) {
        this.cardsType = cardsType;
    }

    SetCustomersCashException.Reason getErrorReason() {
        return reason;
    }

    void setErrorReason(SetCustomersCashException.Reason reason) {
        this.reason = reason;
    }

}
