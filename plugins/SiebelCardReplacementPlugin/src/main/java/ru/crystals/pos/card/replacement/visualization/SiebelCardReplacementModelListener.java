package ru.crystals.pos.card.replacement.visualization;

/**
 * Слушатель изменения состояния модели {@link SiebelCardReplacementModel}.
 *
 * @since 10.2.83.0
 */
public interface SiebelCardReplacementModelListener {
    /**
     * Событие, зажигаемое при изменении состояния модели.
     *
     * @param newState новое состояние модели.
     */
    void modelChanged(SiebelCardReplacementModelState newState);
}
