package ru.crystals.pos.visualization.products.discountcard.product.mvc;

/**
 * Возможные состояния автомата "Продажа ДК (Дисконтной Карты)".
 * 
 * @author aperevozchikov
 *
 */
public enum DiscountCardPluginState {
    /**
     * Состояние ввода номера продаваемой ДК
     */
    ENTER_CARD_NUMBER,
    
    /**
     * Состояние диалога на ввод/отказ-от-ввода анкетных данных клиента, дял которого покупается ДК
     */
    FILL_HOLDER_APPLICATION_DLG,
    
    /**
     * Состояние ввода идентификатора анкеты клиента, дял которого покупается ДК
     */
    ENTER_HOLDER_ID,

    /**
     * Активация карт во время продажи.
     */
    ACTIVATE_CARD,
    /**
     * Состояние применения проданной карты в чеке (как дисконтной карты)
     */
    APPLY_CARD;
}