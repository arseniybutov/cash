package ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary;

/**
 * Тип поля в настройках.
 * 
 * @author aperevozchikov
 *
 */
public enum ShtrihFieldType {
    /**
     * Числовой тип поля
     */
    NUMBER(0),
    
    /**
     * Тип поля - строковый
     */
    STRING(1);
    
    /**
     * Код типа
     */
    private int code;
    
    /**
     * Создает тип с указанным кодом.
     * 
     * @param code код создаваемого перечисления
     */
    private ShtrihFieldType(int code) {
        this.code = code;
    }
    
    /**
     * Вернет тип поля с указанным кодом.
     * 
     * @param code код, тип поля соответсвующий которому надо вернуть
     * @return <code>null</code>, если типа поля с таким кодом не нашлось
     */
    public static ShtrihFieldType getTypeByCode(int code) {
        for (ShtrihFieldType s : ShtrihFieldType.values()) {
            if (code == s.code) {
                return s;
            }
        }
        return null;
    }
    
}