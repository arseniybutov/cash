package ru.crystals.pos.visualization.payments.cftgiftcard.model;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Простой класс для хранения всей мишуры, сопутствующей оплате.
 * Данные попадают сюда из адаптера, используем где надо на визуализации.
 */
public class CFTGiftCardPaymentInfo extends DefaultPaymentInfo {
    private String cardNumber;
    private CardType cardType;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }
}
