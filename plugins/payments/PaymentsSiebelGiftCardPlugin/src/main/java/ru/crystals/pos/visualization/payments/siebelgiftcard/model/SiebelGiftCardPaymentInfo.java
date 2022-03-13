package ru.crystals.pos.visualization.payments.siebelgiftcard.model;

import ru.crystals.pos.cards.siebel.SiebelGiftCardResult;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Класс для хранения всех данных, сопутствующих оплате.
 */
public class SiebelGiftCardPaymentInfo extends DefaultPaymentInfo {
    private SiebelGiftCardResult siebelGiftCardResult;

    public SiebelGiftCardResult getSiebelGiftCardResult() {
        return siebelGiftCardResult;
    }

    public void setSiebelGiftCardResult(SiebelGiftCardResult siebelGiftCardResult) {
        this.siebelGiftCardResult = siebelGiftCardResult;
    }
}
