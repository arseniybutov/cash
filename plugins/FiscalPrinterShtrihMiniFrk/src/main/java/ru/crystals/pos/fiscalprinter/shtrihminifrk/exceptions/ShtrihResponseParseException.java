package ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions;

/**
 * Данная ошибка возникает при парсинге ответа от ФР семейства "Штрих": если ответ не удалось распарсить: возможно, ответ не соответсвует протоколу.
 * 
 * @author aperevozchikov
 */
public class ShtrihResponseParseException extends ShtrihException {
    private static final long serialVersionUID = 1L;
    
    public ShtrihResponseParseException(String message) {
        super(message);
    }
}