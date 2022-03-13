package ru.crystals.pos.card.replacement;

/**
 * Слушатель процесса замены карты Siebel.
 *
 * @since 10.2.83.0
 */
public interface SiebelCardReplacementListener {
    /**
     * Оповещает слушателя о завершении процесса замены карты (успешного или неуспешного).
     */
    void onReplacementComplete();
}
