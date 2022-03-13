package ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions;

/**
 * Общая (generic) ошибка при взаимодействии с ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihException extends Exception {
    private static final long serialVersionUID = 1L;

    public ShtrihException(){
    }
    public ShtrihException(String message) {
        super(message);
    }
    public ShtrihException(String message, Throwable cause) {
        super(message, cause);
    }
}