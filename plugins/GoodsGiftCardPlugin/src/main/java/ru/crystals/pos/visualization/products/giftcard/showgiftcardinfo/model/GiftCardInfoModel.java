package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.model;


import java.util.ArrayList;
import java.util.List;
import ru.crystals.cards.presentcards.PresentCardInformationVO;

/**
 * Модель для сценария "Информация по подарочной карте".
 */
public class GiftCardInfoModel {
    private State state;
    private List<GiftCardInfoModelListener> listeners = new ArrayList<>();
    private PresentCardInformationVO cardInformation;
    private String errorMessage;
    private Long shopIndex;
    private Long cashNum;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void addListener(GiftCardInfoModelListener listener) {
        listeners.add(listener);
    }

    public void changed() {
        for (GiftCardInfoModelListener listener : listeners) {
            listener.changedEvent(this);
        }
    }

    public PresentCardInformationVO getCardInformation() {
        return cardInformation;
    }

    public void setCardInformation(PresentCardInformationVO card) {
        this.cardInformation = card;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getShopIndex() {
        return shopIndex;
    }

    public void setShopIndex(Long shopIndex) {
        this.shopIndex = shopIndex;
    }

    public Long getCashNum() {
        return cashNum;
    }

    public void setCashNum(Long cashNum) {
        this.cashNum = cashNum;
    }

    public enum State {
        CARD_NUMBER_ENTER,
        LOADING_CARD_DATA_FROM_CENTRUM,
        LOADING_DATA_ERROR,
        SHOW_CARD_DATA,
        PRINTING_CARD_DATA,
        SHOW_SERVER_ERROR
    }
}
