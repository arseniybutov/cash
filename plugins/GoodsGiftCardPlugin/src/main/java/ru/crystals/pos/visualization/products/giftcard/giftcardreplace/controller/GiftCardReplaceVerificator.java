package ru.crystals.pos.visualization.products.giftcard.giftcardreplace.controller;

import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.cards.PresentCardToString;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.visualization.products.giftcard.GiftCardHelper;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;

import java.util.Date;

import static ru.crystals.cards.common.CardStatus.Active;
import static ru.crystals.cards.common.CardStatus.Blocked;
import static ru.crystals.cards.common.CardStatus.Create;
import static ru.crystals.cards.common.CardStatus.Inactive;
import static ru.crystals.cards.common.CardStatus.Used;


/**
 * Проверяет можно ли заменять карту.
 */
public class GiftCardReplaceVerificator {
    private static final String EXPIRED = ResBundleGoodsGiftCard.getString("EXPIRED");

    /**
     * Проверяет, может ли подарочная карта быть заменена?
     */
    public static void canBeReplaced(PresentCardInformationVO cardInfo) throws CardsException {
        verifyStatusBeReplaced(cardInfo);
        if (cardInfo.getExpirationDate() != null) {
            verifyExpirationDate(cardInfo.getExpirationDate());
        }
    }

    private static void verifyExpirationDate(Date expireDate) throws CardsException {
        Date now = new Date();
        if (expireDate.before(now)) {
            throw new CardsException(String.format("%s %s", EXPIRED, PresentCardToString.dateToString(expireDate)));
        }
    }

    private static void verifyStatusBeReplaced(PresentCardInformationVO cardInfo) throws CardsException {
        CardStatus status = cardInfo.getStatus();
        if (status == Blocked || status == Inactive || status == Used || status == Create) {
            throw new CardsException(GiftCardHelper.statusToString(cardInfo));
        }
    }

    /**
     * Проверяет, может ли подарочная карта заменить другую?
     */
    public static void canReplace(PresentCardInformationVO cardInfo) throws CardsException {
        verifyStatusReplace(cardInfo);
    }

    private static void verifyStatusReplace(PresentCardInformationVO cardInfo) throws CardsException {
        CardStatus status = cardInfo.getStatus();
        if (status == Active || status == Used || status == Blocked) {
            throw new CardsException(GiftCardHelper.statusToString(cardInfo));
        }
    }
}
