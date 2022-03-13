package ru.crystals.pos.visualization.payments.siebelbonuscard.model;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Простой класс для хранения всей мишуры, сопутствующей оплате.
 * Данные попадают сюда из адаптера, используем где надо на визуализации.
 */
public class SiebelBonusCardPaymentInfo extends DefaultPaymentInfo {
    private String cardNumber;
    private CardType cardType;
    private String exceptionText;

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

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }
}
