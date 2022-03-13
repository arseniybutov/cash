package ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.pos.bl.keylisteners.EnterListener;
import ru.crystals.pos.listeners.XKeyListener;

import java.util.List;
import ru.crystals.cards.presentcards.PresentCardInformationVO;

/**
 * Модель для сценария "Замена подарочной карты на кассе".
 */
public class GiftCardReplaceModel {
    private static final Logger LOG = LoggerFactory.getLogger(GiftCardReplaceModel.class);
    private State state;
    private List<GiftCardReplaceModelListener> listeners;
    private PresentCardInformationVO oldCardInfo;
    private PresentCardInformationVO newCardInfo;
    private String message;
    private EnterListener<String> enterListener;
    private boolean handEnterEnabled;
    private XKeyListener errorListener;
    private String currentCardNumber;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        LOG.debug("Обновлено состояние модели: {} --> {}", this.state, state);
        this.state = state;
    }

    /**
     * Модель изменилась.
     */
    public void changed() {
        LOG.debug("Событие: модель изменилась");
        if (listeners != null) {
            for (GiftCardReplaceModelListener listener : listeners) {
                listener.modelChanged(this);
            }
        }
    }

    public EnterListener<String> getEnterListener() {
        return enterListener;
    }

    public void setEnterListener(EnterListener<String> enterListener) {
        this.enterListener = enterListener;
    }

    public void setListeners(List<GiftCardReplaceModelListener> listeners) {
        this.listeners = listeners;
    }

    public PresentCardInformationVO getOldCardInfo() {
        return oldCardInfo;
    }

    public void setOldCardInfo(PresentCardInformationVO oldCardInfo) {
        LOG.debug("Обновлена информация по старой карте: {}", oldCardInfo);
        this.oldCardInfo = oldCardInfo;
    }

    public PresentCardInformationVO getNewCardInfo() {
        return newCardInfo;
    }

    public void setNewCardInfo(PresentCardInformationVO newCardInfo) {
        LOG.debug("Обновлена информация по новой карте: {}", newCardInfo);
        this.newCardInfo = newCardInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        LOG.debug("Обновлен текст сообщения: \"{}\" --> \"{}\"", this.message, message);
        this.message = message;
    }

    public boolean isHandEnterEnabled() {
        return handEnterEnabled;
    }

    public void setHandEnterEnabled(boolean handEnterEnabled) {
        LOG.debug("Ручной ввод разрешен: {}", handEnterEnabled);
        this.handEnterEnabled = handEnterEnabled;
    }

    public XKeyListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(XKeyListener errorListener) {
        this.errorListener = errorListener;
    }

    public String getCurrentCardNumber() {
        return currentCardNumber;
    }

    public void setCurrentCardNumber(String currentCardNumber) {
        this.currentCardNumber = currentCardNumber;
    }

    public enum State {
        /**
         * Ввод номера карты.
         */
        CARD_NUMBER_ENTER,
        /**
         * Крутилка.
         */
        WAIT,
        /**
         * Сообщение об ошибке.
         */
        SHOW_ERROR
    }
}
