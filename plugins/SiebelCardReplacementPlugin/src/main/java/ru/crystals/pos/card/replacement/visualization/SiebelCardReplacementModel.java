package ru.crystals.pos.card.replacement.visualization;

import ru.crystals.pos.cards.siebel.results.SiebelCardResult;
import ru.crystals.pos.check.PurchaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель данных, хранящее в себе текущее состояние процесса замены карты и всякие дополнительные данные.
 *
 * @since 10.2.83.0
 */
public class SiebelCardReplacementModel {
    private PurchaseEntity purchase;
    private String oldCardNumber;
    private SiebelCardReplacementModelState state;
    private List<SiebelCardReplacementModelListener> listeners = new ArrayList<>();
    private SiebelCardResult result;
    private String errorText;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SiebelCardReplacementModel}.
     */
    public SiebelCardReplacementModel() {
        state = SiebelCardReplacementModelState.CARD_REPLACEMENT_DIALOG;
    }

    /**
     * Добавляет данной модели слушатель изменения её состояния.
     *
     * @param listener слушатель изменения состояния модели.
     * @see #setState(SiebelCardReplacementModelState)
     */
    public void addListener(SiebelCardReplacementModelListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (SiebelCardReplacementModelListener listener : listeners) {
            listener.modelChanged(state);
        }
    }

    /**
     * Возвращает чек, в рамках которого выполняется замена карты.
     *
     * @return чек, в рамках которого выполняется замена карты или null, если такого нет.
     */
    public PurchaseEntity getPurchase() {
        return purchase;
    }

    /**
     * Устанавливает чек, в рамках которого выполняется замена карты.
     *
     * @param purchase чек, в рамках которого выполняется замена карты или null, если такого нет.
     */
    public void setPurchase(PurchaseEntity purchase) {
        this.purchase = purchase;
    }

    /**
     * Возвращает номер заменяемой карты.
     *
     * @return номер заменяемой карты.
     */
    public String getOldCardNumber() {
        return oldCardNumber;
    }

    /**
     * Устанавливает номер заменяемой карты.
     *
     * @param oldCardNumber номер заменяемой карты.
     */
    public void setOldCardNumber(String oldCardNumber) {
        this.oldCardNumber = oldCardNumber;
    }

    /**
     * Возвращает текущее состояние модели.
     *
     * @return текущее состояние модели.
     */
    public SiebelCardReplacementModelState getState() {
        return state;
    }

    /**
     * Возвращает результат замены карты.
     *
     * @return результат замены карты или null, если такого нет.
     */
    public SiebelCardResult getResult() {
        return result;
    }

    /**
     * Устанавливает результат замены карты.
     *
     * @param result результат замены карты или null, если такого нет.
     */
    public void setResult(SiebelCardResult result) {
        this.result = result;
    }

    /**
     * Устанавливает состояние модели. Вызов данного метода приводит к оповещению её слушателей.
     *
     * @param state новое состояние модели.
     * @see #addListener(SiebelCardReplacementModelListener)
     */
    public void setState(SiebelCardReplacementModelState state) {
        this.state = state;
        notifyListeners();
    }

    /**
     * Возвращает текст сообщения об ошибке, который следует показать пользователю.
     *
     * @return текст сообщения об ошибке, который следует показать пользователю или null, если такого нет.
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * Устанавливает текст сообщения об ошибке, который следует показать пользователю.
     *
     * @param errorText текст сообщения об ошибке, который следует показать пользователю или null, если такого нет.
     */
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Override
    public String toString() {
        return "SiebelCardReplacementModel{" +
                "purchase=" + purchase +
                ", oldCardNumber='" + oldCardNumber + '\'' +
                ", state=" + state +
                ", listeners=" + listeners +
                ", result=" + result +
                ", errorText='" + errorText + '\'' +
                '}';
    }
}
