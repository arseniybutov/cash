package ru.crystals.pos.card.replacement.visualization;

import ru.crystals.util.ExtendedResourceBundleControl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Доступ к строковым ресурсам модуля. Сделан в виде перечисления, потому что есть уверенность, строк здесь будет малое количество
 * и их вполне можно уместить в элементы перечисления.
 *
 * @since 10.2.83.0
 */
enum Strings {
    /**
     * Текст диалогового окна о предложении карты. "Предложите покупателю получить карту Вкусомания.".
     */
    OFFER_CARD("offer.card"),
    /**
     * Текст кнопки с положительным выбором в диалоговом окне предложения карты. "Выдать карту".
     */
    BUTTON_TEXT_OFFER_CARD("button.text.offer.card"),
    /**
     * Текст кнопки отказа от выдачи карты. "Отменить".
     */
    BUTTON_TEXT_CANCEL("button.text.cancel"),
    /**
     * Текст крутилки о замене карты. "Замена карты...".
     */
    SPINNER_TEXT("spinner.text"),
    /**
     * В общих чертах сформулированное сообщение об ошибке при замене карты. "Ошибка при замене карты Siebel.".
     */
    GENERIC_ERROR_TEXT("generic.error.text"),
    /**
     * Сообщение о попытке сканировать карту, которая не годится в качестве замены. "Карта не принадлежит Siebel, попробуйте другую карту.".
     */
    INVALID_CARD_TEXT("invalid.card.try.another.one");

    /**
     * Доступ к строковым ресурсам модуля.
     */
    private static final ResourceBundle BUNDLE;

    static {
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle("siebel-card-replacement", new ExtendedResourceBundleControl());
        } catch (Exception ex) {
            //
        }
        BUNDLE = resourceBundle;
    }

    /**
     * Ключ строки в ресурсах.
     */
    private String key;

    /**
     * Конструктор класса. Создаёт новые элементы перечисления {@link Strings}.
     *
     * @param key ключ данной строки в ресурсах.
     */
    Strings(String key) {
        this.key = key;
    }

    /**
     * Возвращает из ресурсов строку с данным ключом.
     *
     * @return строка с данным ключом или сам ключ, если строки в ресурсах нет.
     */
    public String get() {
        return get(this.key);
    }

    /**
     * Предоставляет доступ к строковым ресурсам модуля в обход перечисления.
     *
     * @param key ключ, по которому следует получить строку.
     * @return строка, полученная по ключу или сам ключ, если строки нет.
     */
    public static String get(String key) {
        if (BUNDLE == null) {
            return key;
        }
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException mex) {
            return key;
        }
    }
}
