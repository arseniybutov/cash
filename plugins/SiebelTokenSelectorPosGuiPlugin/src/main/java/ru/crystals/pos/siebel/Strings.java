package ru.crystals.pos.siebel;

import ru.crystals.util.ExtendedResourceBundleControl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum Strings {

    TOKEN_SELECTOR_TABLE_COLUMN_TOKEN_AMOUNT("table.column.token.amount"),
    TOKEN_SELECTOR_TABLE_COLUMN_PRICE("table.column.token.price"),
    TOKEN_SELECTOR_LABEL_TOKEN_BALANCE("label.token.balance"),
    TOKEN_SELECTOR_HEADER("token.selector.header"),
    TOKEN_TABLE_ROW_BEST_OFFER("token.selector.best.offer")
    //Специально торчит
    ;

    /**
     * Доступ к строковым ресурсам модуля.
     */
    private static final ResourceBundle BUNDLE;

    static {
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle("siebel-token-selector-gui", new ExtendedResourceBundleControl());
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
