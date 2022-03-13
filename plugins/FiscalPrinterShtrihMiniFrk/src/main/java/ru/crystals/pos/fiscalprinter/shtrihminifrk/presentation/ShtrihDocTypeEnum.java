package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Перечисление возможных режимов/состояний, в которых могут находиться ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public enum ShtrihDocTypeEnum {

    /**
     * Приход
     */
    SALE((byte) 1),

    /**
     * Возврат прихода
     */
    RETURN_SALE((byte) 2);

    /**
     * Код
     */
    private byte code;

    ShtrihDocTypeEnum(byte code) {
        this.code = code;
    }


    public byte getCode() {
        return code;
    }
    
    /**
     * Вернет enum по его коду
     * 
     * @param code {@link #getCode() код} enum'а, что надо вернуть
     * @return <code>null</code>, если нету enum'а с таким кодом
     */
    public static ShtrihDocTypeEnum getByCode(byte code) {
        for (ShtrihDocTypeEnum s : ShtrihDocTypeEnum.values()) {
            if (code == s.getCode()) {
                return s;
            }
        }
        return null;
    }
}