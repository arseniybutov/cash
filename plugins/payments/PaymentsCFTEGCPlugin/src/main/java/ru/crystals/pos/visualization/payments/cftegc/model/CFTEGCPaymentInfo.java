package ru.crystals.pos.visualization.payments.cftegc.model;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Простой класс для хранения всей мишуры, сопутствующей оплате.
 * Данные попадают сюда из адаптера, используем где надо на визуализации.
 */
public class CFTEGCPaymentInfo extends DefaultPaymentInfo {
    private String cardNumber;
    private CardType cardType;
    private String pinCode;

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

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String buildTrack2() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(cardNumber)) {
            sb.append(cardNumber);
        }
        if (!StringUtils.isEmpty(pinCode)) {
            sb.append("=" + pinCode);
        }
        return sb.toString();
    }
}
