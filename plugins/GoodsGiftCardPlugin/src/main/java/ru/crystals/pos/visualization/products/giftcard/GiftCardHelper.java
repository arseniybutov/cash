package ru.crystals.pos.visualization.products.giftcard;

import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.cards.PluginCard;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.visualization.Factory;

import java.util.Date;

/**
 * Вспомогательные методы для работы с подарочными картами.
 */
public class GiftCardHelper {
    private static final String CREATE_STATUS = ResBundleGoodsGiftCard.getString("GIFT_CARD_STATUS_CREATE");
    private static final String ACTIVE_STATUS = ResBundleGoodsGiftCard.getString("GIFT_CARD_STATUS_ACTIVE");
    private static final String BLOCKED_STATUS = ResBundleGoodsGiftCard.getString("GIFT_CARD_STATUS_BLOCKED");
    private static final String USED_STATUS = ResBundleGoodsGiftCard.getString("GIFT_CARD_STATUS_USED");
    private static final String EXPIRED_STATUS = ResBundleGoodsGiftCard.getString("GIFT_CARD_STATUS_EXPIRED");
    private static PresentCards presentCardService;

    /**
     * Возвращает сервис подарочных карт.
     */
    public static PresentCards getPresentCardsService() {
        if (presentCardService == null) {
            for (PluginCard pluginCard : Factory.getInstance().getCards()) {
                if (pluginCard instanceof PresentCards) {
                    presentCardService = (PresentCards) pluginCard;
                    break;
                }
            }
        }
        return presentCardService;
    }

    /**
     * Локализованные названия статусов подарочной карты.
     */
    public static String statusToString(PresentCardInformationVO cardInformation) {
        switch (cardInformation.getStatus()) {
            case Create:
                return CREATE_STATUS;
            case Active:
                if (cardInformation.getExpirationDate() != null && cardInformation.getExpirationDate().before(new Date())) {
                    return EXPIRED_STATUS;
                }
                return ACTIVE_STATUS;
            case Blocked:
                return BLOCKED_STATUS;
            case Used:
                return USED_STATUS;
            default:
                throw new IllegalArgumentException(String.format("There isn't russian text for present card status: %s", cardInformation.getStatus().toString()));
        }
    }
}
