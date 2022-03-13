package ru.crystals.pos.card.replacement.visualization;

/**
 * Перечисление возможных состояний процесса замены карты Siebel.
 *
 * @since 10.2.83.0
 */
public enum SiebelCardReplacementModelState {
    /**
     * Начальное состояние, отображение диалога с предложением заменить карту.
     */
    CARD_REPLACEMENT_DIALOG,
    /**
     * Ожидание сканирования карты.
     */
    CARD_SCANNING,
    /**
     * Отображение сообщения о сканировании негодной для замены карты.
     */
    INVALID_CARD_SCANNED_MESSAGE,
    /**
     * Окно ожидания, развлекает пользователя крутилкой на время обращения к процессингу за заменой карты.
     */
    CARD_REPLACEMENT_IN_PROGRESS,
    /**
     * Отображение сообщения об ошибке.
     */
    ERROR_RAISED
}
